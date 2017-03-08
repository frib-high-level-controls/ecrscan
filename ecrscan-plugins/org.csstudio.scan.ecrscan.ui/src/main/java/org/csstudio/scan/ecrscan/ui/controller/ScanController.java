package org.csstudio.scan.ecrscan.ui.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import static org.diirt.datasource.ExpressionLanguage.*;

import org.csstudio.scan.ecrscan.ui.data.ScanValueDataProvider;
import org.csstudio.scan.ecrscan.ui.model.AbstractScanTreeItem;
import org.csstudio.scan.ecrscan.ui.model.ModelTreeTable;
import org.csstudio.scan.ecrscan.ui.model.ScanItem;
import org.csstudio.scan.ecrscan.ui.model.TraceItem;
import org.csstudio.javafx.rtplot.PointType;
import org.csstudio.javafx.rtplot.RTScanPlot;
import org.csstudio.javafx.rtplot.Trace;
import org.csstudio.javafx.rtplot.TraceType;
import org.csstudio.javafx.rtplot.util.NamedThreadFactory;
import org.diirt.datasource.PVManager;
import org.diirt.datasource.PVReader;
import org.diirt.datasource.PVReaderEvent;
import org.diirt.datasource.PVReaderListener;
import org.diirt.util.time.TimeDuration;
import org.diirt.vtype.VTable;
import org.diirt.javafx.util.Executors;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeItem.TreeModificationEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

public class ScanController<T extends AbstractScanTreeItem<?>>  {
    
    private static Map<PVReader<Object>,Set<PVReaderListener<Object>>> pvReaderListeners = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Number,List<TraceItem>> traces = new ConcurrentHashMap<>();
    private ScanTreeTableEventHandler scanTreeTableEventHandler;
    final public static ExecutorService thread_pool = java.util.concurrent.Executors.newCachedThreadPool(new NamedThreadFactory("ScanJobs"));
    
    @FXML
    private GridPane rootPane;

    @FXML
    private RTScanPlot plot;
    
    private ModelTreeTable<T> model;
    
    public void initModel(ModelTreeTable<T> model) {
        this.model = model;
        plot.getXAxis().setName("");
        plot.getYAxes().get(0).setName("");
        plot.getXAxis().setAutoscale(true);
        plot.getYAxes().get(0).setAutoscale(true);
        plot.getXAxis().setGridVisible(true);
        
        if(scanTreeTableEventHandler!=null){
            model.getTree().removeEventHandler(TreeItem.childrenModificationEvent(),scanTreeTableEventHandler);
        }
        scanTreeTableEventHandler = new ScanTreeTableEventHandler();
        model.getTree().addEventHandler(TreeItem.childrenModificationEvent(),scanTreeTableEventHandler);
        model.xformulaProperty().addListener((observable, oldValue, newValue) ->{
            plot.getXAxis().setName(newValue);
        });
    }
    
    public void closeConnections() {
        for (Entry<PVReader<Object>, Set<PVReaderListener<Object>>> pv:pvReaderListeners.entrySet()) {
            if (pv.getKey()!=null) {
                pv.getKey().close();
            }
        }
        pvReaderListeners.clear();
    }
   
    
    private PVReaderListener<Object> createReadListener() {
        return new PVReaderListener<Object>() {

            @Override
            public void pvChanged(PVReaderEvent<Object> e) {
                Object readObject = e.getPvReader().getValue();
                if (readObject instanceof VTable) {
                    VTable readVTable = (VTable)readObject; 
                    String readerScanId = e.getPvReader().getName().replaceAll("[^0-9]", "");
                    List<TraceItem> readTraces = traces.get(Integer.parseInt(readerScanId));
                    if(readTraces != null) {
                        for(TraceItem readTrace:readTraces) {
                            if(readTrace.getData() instanceof ScanValueDataProvider) {
                                ScanValueDataProvider scanValueDataProvider = (ScanValueDataProvider)readTrace.getData();
                                thread_pool.execute(() -> scanValueDataProvider.setValue(readVTable));
                                plot.requestUpdate();
                            }
                        }
                    }
                }
            }
        };
    }
    
   
    
    // This gets called every update because I update the whole treetable model with new objects while a scan is running or paused. 
    private final class ScanTreeTableEventHandler implements EventHandler<TreeItem.TreeModificationEvent<AbstractScanTreeItem<?>>> {
        @Override
        public void handle(TreeModificationEvent<AbstractScanTreeItem<?>> event) {
            Map<Number,List<TraceItem>> newTraces = new HashMap<>();
            List<?> scanlist = model.getTree().getValue().getItems();
            for (Object item:scanlist) {
                if ( item instanceof ScanItem) {
                    ScanItem scanItem = (ScanItem)item;
                    List<TraceItem> traceList = new ArrayList<TraceItem>();
                    for(TraceItem traceItem:scanItem.getItems()){
                        traceList.add(traceItem);
                    }
                    if (!traceList.isEmpty()) {
                        newTraces.put(scanItem.getId(), traceList);
                    }
                }
            }
            if(!newTraces.equals(traces)){
                traces.clear();
                traces.putAll(newTraces);
                closeConnections();
                for(Trace<Double> trace : plot.getTraces()){
                    plot.removeTrace(trace);
                }
                for (Entry<Number, List<TraceItem>> scan:traces.entrySet()) {
                    for(TraceItem trace:scan.getValue()) {
                        Trace<Double> plotTrace = plot.addTrace(String.valueOf(scan.getKey())+":"+trace.getYformula(), trace.getUnits(), trace.getData(), trace.getColor(), trace.getType(), trace.getWidth(), trace.getPointType(), trace.getPointSize(), trace.getYAxis());
                        trace.colorProperty().addListener(new ColorChangeListener(plotTrace));
                        trace.pointSizeProperty().addListener(new PointSizeChangeListener(plotTrace));
                        trace.typeProperty().addListener(new TypeChangeListener(plotTrace));
                        trace.widthProperty().addListener(new WidthChangeListener(plotTrace));
                        trace.pointTypeProperty().addListener(new PointTypeChangeListener(plotTrace));
                    }
                    PVReaderListener<Object> listener = createReadListener();
                    Set<PVReaderListener<Object>> listeners = new HashSet<PVReaderListener<Object>>();
                    listeners.add(listener);
                    String channel = model.getScanServer()+"/"+scan.getKey().toString()+"/data";
                    PVReader<Object> reader = PVManager.read(channel(channel))
                      .readListener(listener)
                      .timeout(TimeDuration.ofSeconds(1), "Still connecting...")
                      .notifyOn(Executors.javaFXAT())
                      .maxRate(TimeDuration.ofHertz(10));
                    pvReaderListeners.put(reader, listeners); 
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
