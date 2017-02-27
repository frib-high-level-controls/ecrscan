package org.csstudio.scan.ecrscan.ui.controller;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import org.diirt.datasource.formula.ExpressionLanguage;
import static org.diirt.datasource.ExpressionLanguage.*;

import org.csstudio.scan.ecrscan.ui.controller.DataColumnsViewer;
import org.csstudio.scan.ecrscan.ui.controller.ScanTable;
import org.csstudio.scan.ecrscan.ui.controller.ScanTreeTableView;
import org.csstudio.scan.ecrscan.ui.data.ScanValueDataProvider;
import org.csstudio.scan.ecrscan.ui.model.AbstractScanTreeItem;
import org.csstudio.scan.ecrscan.ui.model.ModelTreeTable;
import org.csstudio.scan.ecrscan.ui.model.ScanItem;
import org.csstudio.scan.ecrscan.ui.model.TraceItem;
import org.csstudio.scan.ecrscan.ui.events.ScanItemSelectionEvent;
import org.csstudio.scan.ecrscan.ui.events.ScanItemSelectionEvent.SELECTION;
import org.csstudio.javafx.rtplot.PointType;
import org.csstudio.javafx.rtplot.RTScanPlot;
import org.csstudio.javafx.rtplot.Trace;
import org.csstudio.javafx.rtplot.TraceType;
import org.csstudio.javafx.rtplot.util.NamedThreadFactory;
import org.csstudio.javafx.rtplot.util.RGBFactory;
import org.diirt.datasource.PV;
import org.diirt.datasource.PVManager;
import org.diirt.datasource.PVReader;
import org.diirt.datasource.PVReaderEvent;
import org.diirt.datasource.PVReaderListener;
import org.diirt.util.time.TimeDuration;
import org.diirt.vtype.Alarm;
import org.diirt.vtype.AlarmSeverity;
import org.diirt.vtype.VTable;
import org.diirt.vtype.ValueUtil;
import org.diirt.vtype.table.VTableFactory;
import org.diirt.javafx.graphene.ScanLineGraph;
import org.diirt.javafx.util.Executors;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeItem.TreeModificationEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

public class ScanController<T extends AbstractScanTreeItem<?>>  {
    

    private PV<?, Object> pvScanServer;
    private static Map<PVReader<Object>,Set<PVReaderListener<Object>>> pvReaderListeners = new ConcurrentHashMap<>();
    private static Map<PVReader<Object>,Set<PVReaderListener<Object>>> pvReaderSelectedListeners = new ConcurrentHashMap<>();
    private EventHandler<ScanItemSelectionEvent> eventHandler;
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
    }
    
    private void setColumn(List<? extends TreeItem<AbstractScanTreeItem<?>>> scanItems, SELECTION selectionType) {
        if(selectionType == SELECTION.SELECTED) {
            if (!pvReaderSelectedListeners.isEmpty()){
                    for(Entry<PVReader<Object>, Set<PVReaderListener<Object>>>  pvReaderListener : pvReaderSelectedListeners.entrySet()) {
                        pvReaderListener.getKey().close();
                    }
                    pvReaderSelectedListeners.clear();
                }
            for(TreeItem<AbstractScanTreeItem<?>> scanItem : scanItems) {
                ScanItem parentScan = null;
                if (scanItem == null){
                    continue;
                }
                if (scanItem.getValue() instanceof TraceItem) {
                    if(scanItem.getParent().getValue() instanceof ScanItem) {
                        parentScan = (ScanItem)scanItem.getParent().getValue();
                    }
                } else if(scanItem.getValue() instanceof ScanItem) {
                    parentScan = (ScanItem)scanItem.getValue();
                } else {
                    continue;
                }
                // Right now, if the parent is selected and trace are select, there are two connections
                String channel = channelField.getText()+"/"+parentScan.getId().toString()+"/data";
                PVReaderListener<Object> listener = createColumnReadListener();
                Set<PVReaderListener<Object>> listeners = new HashSet<PVReaderListener<Object>>();
                listeners.add(listener);
                PVReader<Object> reader = PVManager.read(channel(channel))
                  .readListener(listener)
                  .timeout(TimeDuration.ofSeconds(1), "Still connecting...")
                  .notifyOn(Executors.javaFXAT())
                  .maxRate(TimeDuration.ofHertz(10));
                pvReaderSelectedListeners.put(reader, listeners);
            }
        }
    }
    
    private PVReaderListener<Object> createColumnReadListener() {
        return new PVReaderListener<Object>() {
            @Override
            public void pvChanged(PVReaderEvent<Object> e) {
                Object readObject = e.getPvReader().getValue();
                if (readObject instanceof VTable) {
                    VTable readVTable = (VTable)readObject;
                    List<String> columnNames = VTableFactory.columnNames(readVTable);
                    dataColumnsViewer.setItems(columnNames);
                }
            }
        };
    }
    
    private PVReaderListener<Object> createReadListener() {
        return new PVReaderListener<Object>() {

            @Override
            public void pvChanged(PVReaderEvent<Object> e) {
                Object readObject = e.getPvReader().getValue();
                if (readObject instanceof VTable) {
                    VTable readVTable = (VTable)readObject; 
                    for (TreeItem<AbstractScanTreeItem<?>> treeItem :scanTreeTableView.getTreeTableRoot().getChildren()){
                        if(treeItem.getValue() instanceof ScanItem && !treeItem.getChildren().isEmpty()){
                            ScanItem scanItem = (ScanItem)treeItem.getValue();
                            for (TreeItem<AbstractScanTreeItem<?>> traceTreeItem : treeItem.getChildren()){
                                String readerScanId = e.getPvReader().getName().replaceAll("[^0-9]", "");
                                if(traceTreeItem.getValue() instanceof TraceItem && scanItem.getId().intValue() == Integer.parseInt(readerScanId)){  
                                    TraceItem traceItem = (TraceItem)traceTreeItem.getValue();
                                    thread_pool.execute(() -> traceItem.getScanValueDataProvider().setValue(readVTable,scanTreeTableView.getXFormula(),traceItem.getYformula()));    
                                }
                            }
                        }
                    }
                }
            }
        };
    }
    
    private void setGraph(int dataSeriesIndex, List<Number> ids, SELECTION selectionType) {
        // pvmanager sscan datasource index
        String xaxis = "FE_SCS1:PSD_D0717:I_CSET";
        String yaxis = "ZFE_ISRC1:SIM:SCANDET";
        if (dataSeriesIndex == 0){
            StringJoiner xaxisSelected = new StringJoiner("\",\"","=arrayOf(\"","\")");
            StringJoiner yaxisSelected = new StringJoiner("\",\"","=arrayOf(\"","\")");
            StringJoiner tableOf = new StringJoiner(",","=tableOf(",")");
            if(selectionType == SELECTION.SELECTED) {
                for(Number id : ids) {
                    
                    StringJoiner columnOfx = new StringJoiner(",","column(\""+xaxis+id.toString()+"\",columnOf(","))");
                    columnOfx.add("'"+channelField.getText()+"/"+id.toString()+"/data'");
                    columnOfx.add("\""+xaxis+"\"");
                    
                    StringJoiner columnOfy = new StringJoiner(",","column(\""+yaxis+id.toString()+"\",columnOf(","))");
                    columnOfy.add("'"+channelField.getText()+"/"+id.toString()+"/data'");
                    columnOfy.add("\""+yaxis+"\"");
                    
                    tableOf.add(columnOfx.toString());
                    tableOf.add(columnOfy.toString());
                    
                    xaxisSelected.add(xaxis+id.toString());
                    yaxisSelected.add(yaxis+id.toString());
                }
               // multiLineGraphView.setFormula(tableOf.toString());
               // multiLineGraphView.setXColumn(xaxisSelected.toString());
               // multiLineGraphView.setYColumn(yaxisSelected.toString());
               // errorField.setText(Objects.toString(multiLineGraphView.getlastException(),""));
            }
        }
    }
    
    private void setAlarm(Object value, boolean connected) {
        Alarm alarm = ValueUtil.alarmOf(value, connected);
        //valueField.setBorder(BORDER_MAP.get(alarm.getAlarmSeverity()));
    }
    
    private static final Map<AlarmSeverity, Border> BORDER_MAP = createBorderMap();

    private static Map<AlarmSeverity, Border> createBorderMap() {
        Map<AlarmSeverity, Border> map = new EnumMap<>(AlarmSeverity.class);
        map.put(AlarmSeverity.NONE, null);
        map.put(AlarmSeverity.MINOR, new Border(new BorderStroke(Color.YELLOW, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(2))));
        map.put(AlarmSeverity.MAJOR, new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(2))));
        map.put(AlarmSeverity.INVALID, new Border(new BorderStroke(Color.PURPLE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(2))));
        map.put(AlarmSeverity.UNDEFINED, new Border(new BorderStroke(Color.PURPLE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(2))));
        return Collections.unmodifiableMap(map);
    }
    
    private void addSelectionListener(EventHandler<ScanItemSelectionEvent> eventHandler) {
        rootPane.addEventHandler(ScanItemSelectionEvent.TYPE, eventHandler);
    }
    
    private void removeSelectionListener(EventHandler<ScanItemSelectionEvent> eventHandler) {
        rootPane.removeEventHandler(ScanItemSelectionEvent.TYPE, eventHandler);
    }
    
    private final class ScanTreeTableEventHandler implements EventHandler<TreeItem.TreeModificationEvent<AbstractScanTreeItem<?>>> {
        @Override
        public void handle(TreeModificationEvent<AbstractScanTreeItem<?>> event) {
            if (event.wasAdded()) {
                for (TreeItem<AbstractScanTreeItem<?>> item : event.getAddedChildren()) {
                    PVReader<Object> reader = addReader(item);
                    addTrace(item, reader);
                }
            }
            if (event.wasRemoved()) {
                for (TreeItem<AbstractScanTreeItem<?>> item : event.getRemovedChildren()) {
                    removeReader(item);
                    removeTrace(item);
                }
            }
        }    
    }
    
    private void addTrace(TreeItem<AbstractScanTreeItem<?>> treeItem, PVReader<Object> reader) {
        if(treeItem.getValue() instanceof TraceItem && reader != null){  
            TraceItem traceItem = (TraceItem)treeItem.getValue();
            plot.addTrace(traceItem.getYformula(), reader.getName().replaceAll("[^0-9]", "") , traceItem.getScanValueDataProvider(), traceItem.getColor(), 
                    traceItem.getTraceType(), traceItem.getTraceWidth(), traceItem.getPointType(), traceItem.getPointSize(), traceItem.getYaxis());
            VTable tableValue = (VTable)reader.getValue();
            thread_pool.execute(() -> traceItem.getScanValueDataProvider().setValue(tableValue,scanTreeTableView.getXFormula(),traceItem.getYformula()));
        }
        plot.requestUpdate();
    }
    
    private void removeTrace(TreeItem<AbstractScanTreeItem<?>> treeItem) {
        if(treeItem.getValue() instanceof TraceItem){
            for(Trace<Double> trace : plot.getTraces()){
                if(treeItem.getValue().getColor() == trace.getColor()){
                    plot.removeTrace(trace);
                }
            }
        }
    }
    
    private PVReader<Object> addReader(TreeItem<AbstractScanTreeItem<?>> treeItem){
        if(treeItem.getValue() instanceof TraceItem){
            ScanItem scanItem = (ScanItem)(treeItem.getParent().getValue());
            String channel = channelField.getText()+"/"+scanItem.getId().toString()+"/data";
            PVReaderListener<Object> listener = createReadListener();
            //PVReaderListener<Object> dataListener = dataColumnsViewer.eventScanData().<Object>createReadListener();
            Set<PVReaderListener<Object>> listeners = new HashSet<PVReaderListener<Object>>();
            listeners.add(listener);
            //listeners.add(dataListener);
            PVReader<Object> reader = PVManager.read(channel(channel))
              //.readListener(dataListener)
              .readListener(listener)
              .timeout(TimeDuration.ofSeconds(1), "Still connecting...")
              .notifyOn(Executors.javaFXAT())
              .maxRate(TimeDuration.ofHertz(10));
            pvReaderListeners.put(reader, listeners);
            return reader;
        }
        return null;
    }
    
    private void removeReader(TreeItem<AbstractScanTreeItem<?>> treeItem){
        for(Entry<PVReader<Object>, Set<PVReaderListener<Object>>>pvReaderListener:pvReaderListeners.entrySet()){
            int readerScanId = Integer.parseInt(pvReaderListener.getKey().getName().replaceAll("[^0-9]", ""));
            int scanId = -1;
            if(treeItem.getValue() instanceof ScanItem && treeItem.getChildren().isEmpty()){
                ScanItem scanItem = (ScanItem)treeItem.getValue();
                scanId = scanItem.getId().intValue();
            }else if(treeItem.getValue() instanceof TraceItem){
                // Workaround, there is no parent, because it has been removed.  So, the id is inserted before delete
                AbstractScanTreeItem<?> abstractScanTreeItem = treeItem.getValue();
                scanId = abstractScanTreeItem.getId().intValue();
            }else {
                continue;
            }
            if(scanId == readerScanId){
                pvReaderListeners.remove(pvReaderListener.getKey());
            }
        }
    }
}
