package org.csstudio.scan.ecrscan.ui.controller;

import java.util.Map.Entry;
import java.util.Observable;
import java.util.concurrent.ConcurrentHashMap;

import org.csstudio.scan.ecrscan.ui.model.AbstractScanTreeItem;
import org.csstudio.scan.ecrscan.ui.model.ScanModel;
import org.csstudio.scan.ecrscan.ui.model.TraceItem;
import org.csstudio.javafx.rtplot.PointType;
import org.csstudio.javafx.rtplot.RTScanPlot;
import org.csstudio.javafx.rtplot.Trace;
import org.csstudio.javafx.rtplot.TraceType;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.paint.Color;

public class ScanController<T extends AbstractScanTreeItem<?>>  {
    
    private MapListener mapListener;
    private ChangeListener<? super String> xformulaChangelListener;
    private ConcurrentHashMap<String,ListListener> listListeners = new ConcurrentHashMap<String,ListListener>();
    private ConcurrentHashMap<TraceItem,Trace<Double>> traceMap = new ConcurrentHashMap<TraceItem,Trace<Double>>();

    @FXML
    private RTScanPlot plot;
    
    private ScanModel<T> model;
    
    public void initModel(ScanModel<T> model) {
        this.model = model;
        plot.getXAxis().setName("");
        plot.getYAxes().get(0).setName("");
        plot.getYAxes().get(0).setExponentialThreshold(2);
        plot.getXAxis().setAutoscale(true);
        plot.getYAxes().get(0).setAutoscale(true);
        plot.getXAxis().setGridVisible(true);

        if (mapListener != null) {
            model.getDataStore().removeListener(mapListener);
        }
        for (Entry<String, ObservableList<TraceItem>> map : model.getDataStore().entrySet()) {
            ScanController<T>.ListListener listener = new ListListener();
            listListeners.put(map.getKey(), listener);
            map.getValue().addListener(listener);
        }
        mapListener = new MapListener();
        model.getDataStore().addListener(mapListener);
        if (xformulaChangelListener != null) {
            model.xformulaProperty().removeListener(xformulaChangelListener);
        } else {
            xformulaChangelListener = (observable, oldValue, newValue) -> {
                plot.getXAxis().setName(newValue);
            };
            model.xformulaProperty().addListener(xformulaChangelListener);
        }  
    }
    
    public void closeConnections() {
        model.getDataStore().removeListener(mapListener);
        model.xformulaProperty().removeListener(xformulaChangelListener);
        for (Entry<String, ScanController<T>.ListListener> listListener : listListeners.entrySet()) {;
            model.getDataStore().get(listListener.getKey()).removeListener(listListener.getValue());
        }
    }
    
    private final class MapListener implements MapChangeListener<String,ObservableList<TraceItem>> {
		@Override
		public void onChanged(
				javafx.collections.MapChangeListener.Change<? extends String, ? extends ObservableList<TraceItem>> change) {
			
			if(change.getValueAdded()!=null){
				ScanController<T>.ListListener listener = new ListListener();
				listListeners.put(change.getKey(), listener);
				change.getValueAdded().addListener(listener);
			}
			if(change.getValueRemoved()!=null){
				change.getValueRemoved().removeListener(listListeners.get(change.getKey()));
				listListeners.remove(change.getKey());
			}
		}
    }
    
    private final class ListListener implements ListChangeListener<TraceItem> {
		@Override
		public void onChanged(javafx.collections.ListChangeListener.Change<? extends TraceItem> c) {
            while (c.next()) {
            	if(c.wasRemoved()){
					for (TraceItem trace: c.getRemoved()){
					    if(traceMap.get(trace) != null ){
					        plot.removeTrace(traceMap.get(trace));
					        traceMap.remove(trace);
					    }
					}
				}
				if(c.wasAdded()){
					for(TraceItem trace: c.getAddedSubList()) {
						Trace<Double> plotTrace = plot.addTrace(String.valueOf(trace.getId())+":"+trace.getYformula(), trace.getUnits(), trace.getData(), trace.getColor(), trace.getType(), trace.getWidth(), trace.getPointType(), trace.getPointSize(), trace.getYAxis());
	                    trace.colorProperty().addListener(new ColorChangeListener(plotTrace));
	                    trace.pointSizeProperty().addListener(new PointSizeChangeListener(plotTrace));
	                    trace.typeProperty().addListener(new TypeChangeListener(plotTrace));
	                    trace.widthProperty().addListener(new WidthChangeListener(plotTrace));
	                    trace.pointTypeProperty().addListener(new PointTypeChangeListener(plotTrace));
	                    if (trace.getData() instanceof Observable){
	                    	Observable observable =(Observable)trace.getData();
	                    	observable.addObserver((o, arg) -> {plot.requestUpdate();});
	                    }
	                    traceMap.put(trace, plotTrace);
					}
				}
				plot.requestUpdate();
            }
		}
    }
    
    private final class PointTypeChangeListener implements ChangeListener<PointType> {
        private final Trace<Double> plotTrace;
        public PointTypeChangeListener(final Trace<Double> plotTrace) {
                this.plotTrace = plotTrace;
        }
        @Override
        public void changed(ObservableValue<? extends PointType> observable, PointType oldValue, PointType newValue) {
            plotTrace.setPointType(newValue);
            plot.requestUpdate();
        }
    }
    
    private final class WidthChangeListener implements ChangeListener<Integer> {
        private final Trace<Double> plotTrace;
        public WidthChangeListener(final Trace<Double> plotTrace) {
                this.plotTrace = plotTrace;
        }
        @Override
        public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
            plotTrace.setWidth(newValue);
            plot.requestUpdate();
        }
    }
    
    private final class PointSizeChangeListener implements ChangeListener<Integer> {
        private final Trace<Double> plotTrace;
        public PointSizeChangeListener(final Trace<Double> plotTrace) {
                this.plotTrace = plotTrace;
        }
        @Override
        public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
            plotTrace.setPointSize(newValue);
            plot.requestUpdate();
        }
    }
    
    private final class TypeChangeListener implements ChangeListener<TraceType> {
        private final Trace<Double> plotTrace;
        public TypeChangeListener(final Trace<Double> plotTrace) {
                this.plotTrace = plotTrace;
        }
        @Override
        public void changed(ObservableValue<? extends TraceType> observable, TraceType oldValue, TraceType newValue) {
            plotTrace.setType(newValue);
            plot.requestUpdate();
        }
    }
    
    private final class ColorChangeListener implements ChangeListener<Color> {
        private final Trace<Double> plotTrace;
        public ColorChangeListener(final Trace<Double> plotTrace) {
                this.plotTrace = plotTrace;
        }
        @Override
        public void changed(ObservableValue<? extends Color> observable, Color oldValue, Color newValue) {
            plotTrace.setColor(newValue);
            plot.requestUpdate();
        }
    }
}
