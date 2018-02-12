package org.csstudio.scan.ecrscan.ui.controller;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.csstudio.javafx.rtplot.PointType;
import org.csstudio.javafx.rtplot.TraceType;
import org.csstudio.javafx.rtplot.util.RGBFactory;
import org.csstudio.scan.ecrscan.ui.data.AbstractValueDataProvider;
import org.csstudio.scan.ecrscan.ui.data.ScanValueDataProvider;
import org.csstudio.scan.ecrscan.ui.model.AbstractScanTreeItem;
import org.csstudio.scan.ecrscan.ui.model.ModelTreeTable;
import org.csstudio.scan.ecrscan.ui.model.ScanItem;
import org.csstudio.scan.ecrscan.ui.model.ScanModel;
import org.csstudio.scan.ecrscan.ui.model.ScanServerItem;
import org.csstudio.scan.ecrscan.ui.model.TraceItem;
import org.diirt.datasource.PV;
import org.diirt.datasource.PVManager;
import org.diirt.datasource.PVReaderEvent;
import org.diirt.datasource.formula.ExpressionLanguage;
import org.diirt.javafx.util.Executors;
import org.diirt.util.array.ListNumber;
import org.diirt.util.time.TimeDuration;
import org.diirt.vtype.VTable;



import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTablePosition;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.ComboBoxTreeTableCell;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import javafx.util.converter.IntegerStringConverter;

public class ScanTreeTableController<T extends AbstractScanTreeItem<?>> {

    private RGBFactory RGBFactory = new RGBFactory();
    private PV<?, Object> pvScanServer;
    private AtomicBoolean atomicBoolean = new AtomicBoolean();
    private Map<String,Class<?>> columnMap = new LinkedHashMap<String,Class<?>>();
    private ScanModel<T> model;
    private ModelTreeTable<T> tree;
    private final static ScanServerItem scanServerItem = new ScanServerItem("ecrscan");
    private ListListener listListener;
    
    @FXML
    private TreeTableView<T> treeTableView;
    @FXML
    private TextField xformula;
    @FXML
    private TextField yformula;
    @FXML
    private Button addButton;
    @FXML
    private Button deleteButton;
    @FXML
    private CheckBox useAsDefault;
    @FXML
    private void onChannelChanged(ActionEvent event) {
        model.setXformula(xformula.getText());
        
        Optional<ObservableList<TraceItem>> optList = Optional.ofNullable(model.getDataStore().get(scanServerItem.getName()));
        if(optList.isPresent()){
        	optList.get().forEach(trace ->{
	        	((AbstractValueDataProvider)trace.getData()).close();
	        	String channel = model.getScanServer()+"/"+String.valueOf(trace.getId().intValue())+"/data";
	        	int i = optList.get().indexOf(trace);
	        	optList.get().set(i, new TraceItem(trace.getId(), new ScanValueDataProvider(channel, model.getXformula(),trace.getYformula()), trace.getYformula(), trace.getColor(),
	        			trace.getType(), trace.getWidth(), trace.getPointType(), trace.getPointSize(), trace.getYAxis()));
	        });
        }
    }
    
    public ScanTreeTableController() {
        
        
    }
    
    private final class SelectedTableItemsChangeListener implements ListChangeListener<TreeItem<T>> {
        private final TreeTableView<T> treeTableView;

        public SelectedTableItemsChangeListener(final TreeTableView<T> treeTableView) {
                this.treeTableView = treeTableView;
        }

        @Override
        public void onChanged(final ListChangeListener.Change<? extends TreeItem<T>> change) {
                final boolean next = change.next();
                if (next) {
                    final List<TreeItem<T>> dataItems = treeTableView.getSelectionModel().getSelectedItems();
                    if (!dataItems.isEmpty() && !atomicBoolean.get()){
                        if (dataItems.get(0).getValue() instanceof TraceItem) {
                            T scanItem = dataItems.get(0).getParent().getValue();
                            if(scanItem instanceof ScanItem) {
                                model.setSelectedScan(scanItem);
                            }
                        }else if (dataItems.get(0).getValue() instanceof ScanItem) {
                            T scanItem = dataItems.get(0).getValue();
                            model.setSelectedScan(scanItem);
                        }
                    }
                }
        }
    }
    private final class ListListener implements ListChangeListener<TraceItem> {
		@Override
		public void onChanged(javafx.collections.ListChangeListener.Change<? extends TraceItem> c) {
            while(c.next()) {
				if(c.wasAdded()){
					for(TraceItem trace: c.getAddedSubList()) {
						 Optional<TreeItem<T>> treeItemOpt = getTreeItem(trace.getId());
						 treeItemOpt.ifPresent(t -> ((ScanItem)t.getValue()).getItems().add(trace));
					}
				}
				if(c.wasRemoved()){
					for (TraceItem trace: c.getRemoved()){
						Optional<TreeItem<T>> treeItemOpt = getTreeItem(trace.getId());
						 treeItemOpt.ifPresent(t -> t.getValue().getItems().remove(trace));
					}
				}
            }
		}
    }

    public void initModel(ScanModel<T> model, ModelTreeTable<T> tree ) {
        this.model = model;
        this.tree = tree;
        treeTableView.getColumns().clear();
        treeTableView.setRoot(tree.getTree());
        model.setXformula("");
        model.setXformula(xformula.getText());
        Optional<ObservableList<TraceItem>> optList = Optional.ofNullable(model.getDataStore().get(scanServerItem.getName()));
        
        if(listListener != null){
        	optList.ifPresent(l -> l.removeListener(listListener));
        }
        listListener = new ListListener();
        optList.ifPresent(l -> l.addListener(listListener));
        
        if(optList.isPresent()){
        	optList.get().forEach(trace ->{
	        	((AbstractValueDataProvider)trace.getData()).close();
	        	String channel = model.getScanServer()+"/"+String.valueOf(trace.getId().intValue())+"/data";
	        	int i = optList.get().indexOf(trace);
	        	optList.get().set(i, new TraceItem(trace.getId(), new ScanValueDataProvider(channel, model.getXformula(),trace.getYformula()), trace.getYformula(), trace.getColor(),
	        			trace.getType(), trace.getWidth(), trace.getPointType(), trace.getPointSize(), trace.getYAxis()));
	        });
        }
        
        this.model.scanServerProperty().addListener((observable, oldValue, newValue) ->{
            if (pvScanServer != null) {
                pvScanServer.close();
            }

            pvScanServer = PVManager.readAndWrite(ExpressionLanguage.formula(newValue))
                    .readListener((PVReaderEvent<Object> e) -> {
                        fillTreeTable(e.getPvReader().getValue(), e.getPvReader().isConnected());
                    })
                    .timeout(TimeDuration.ofSeconds(1), "Still connecting...")
                    .notifyOn(Executors.javaFXAT())
                    .asynchWriteAndMaxReadRate(TimeDuration.ofHertz(10));
        });
        
        columnMap.clear();
        columnMap.put("name", String.class);
        columnMap.put("id", Number.class);
        columnMap.put("created", Instant.class);
        columnMap.put("finished", Instant.class);
        columnMap.put("percent", Number.class);
        columnMap.put("yformula", String.class);
        columnMap.put("color", Color.class);
        columnMap.put("type", TraceType.class);
        columnMap.put("width", Integer.class);
        columnMap.put("pointType", PointType.class);
        columnMap.put("pointSize", Integer.class);
        for (Entry<String, Class<?>> columnSet : columnMap.entrySet()){
            Class<?> clazz = columnSet.getValue();
            if (clazz.equals(Number.class)) {
                TreeTableColumn<T,Number> column = new TreeTableColumn<T,Number>(columnSet.getKey());
                column.setCellValueFactory(new TreeItemPropertyValueFactory<>(columnSet.getKey()));
                if(columnSet.getKey().equals("percent")){
                    // can't use ProgressBarTableCell, because I need a double/100
                    column.setCellFactory(new Callback<TreeTableColumn<T, Number>, TreeTableCell<T, Number>>() {
                        @Override
                        public TreeTableCell<T, Number> call(TreeTableColumn<T, Number> param) {
                            TreeTableCell<T, Number> cell = new TreeTableCell<T, Number>() {
                                private ProgressBar pb = new ProgressBar();
                                @Override
                                protected void updateItem(Number item, boolean empty) {
                                    super.updateItem(item, empty);  
                                    if (empty || item == null) {
                                        setGraphic(null);
                                    } else {
                                        pb.setProgress(item.doubleValue()/100);
                                        setGraphic(pb);
                                    }                   
                                }
                            };
                            cell.setEditable(false);
                            return cell;
                        }
                    });
                }
                treeTableView.getColumns().add(column);
            }
            if (clazz.equals(Integer.class)) {
                TreeTableColumn<T,Integer> column = new TreeTableColumn<T,Integer>(columnSet.getKey());
                column.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn(new IntegerStringConverter()));
                column.setCellValueFactory(new TreeItemPropertyValueFactory<>(columnSet.getKey()));
                treeTableView.getColumns().add(column);
            }
            if (clazz.equals(TraceType.class)) {
                TreeTableColumn<T,TraceType> column = new TreeTableColumn<T,TraceType>(columnSet.getKey());
                ObservableList<TraceType> traceTypeList = FXCollections.observableArrayList(TraceType.values());
                column.setCellFactory(ComboBoxTreeTableCell.forTreeTableColumn(traceTypeList));
                column.setCellValueFactory(new TreeItemPropertyValueFactory<>(columnSet.getKey()));
                treeTableView.getColumns().add(column);
            }
            if (clazz.equals(PointType.class)) {
                TreeTableColumn<T,PointType> column = new TreeTableColumn<T,PointType>(columnSet.getKey());
                ObservableList<PointType> pointTypeList = FXCollections.observableArrayList(PointType.values());
                column.setCellFactory(ComboBoxTreeTableCell.forTreeTableColumn(pointTypeList));
                column.setCellValueFactory(new TreeItemPropertyValueFactory<>(columnSet.getKey()));
                treeTableView.getColumns().add(column);
            }
            if (clazz.equals(Instant.class)) {
                TreeTableColumn<T,Instant> column = new TreeTableColumn<T,Instant>(columnSet.getKey());
                column.setCellValueFactory(new TreeItemPropertyValueFactory<>(columnSet.getKey()));
                column.setEditable(true);
                treeTableView.getColumns().add(column);
            }
            if (clazz.equals(String.class)) {
                TreeTableColumn<T,String> column = new TreeTableColumn<T,String>(columnSet.getKey());
                column.setCellValueFactory(new TreeItemPropertyValueFactory<>(columnSet.getKey()));
/*                column.setCellFactory(tv -> new TreeTableCell<T,String>() {
                    private Set<PseudoClass> pseudoClassesSet = new HashSet<>();
                    
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        textProperty().unbind();
                        
                        pseudoClassesSet.forEach(pc -> pseudoClassStateChanged(pc, false));
                        if (empty) {
                            setText("");
                        } else {
                           // textProperty().bind(name.apply(item));
                          //  PseudoClass itemPC = pseudoClassMap.apply(item);
                           // if (itemPC != null) {
                           //     pseudoClassStateChanged(itemPC, true);
                           //     pseudoClassesSet.add(itemPC);
                           // }
                        }
                    }
                });*/
                
                treeTableView.getColumns().add(column);
            }
            if (clazz.equals(Color.class)) {
                TreeTableColumn<T,Color> column = new TreeTableColumn<T,Color>(columnSet.getKey());
                column.setEditable(true);
                column.setCellFactory(new Callback<TreeTableColumn<T, Color>, TreeTableCell<T, Color>>() {
                    @Override
                    public TreeTableCell<T, Color> call(TreeTableColumn<T, Color> param) {
                        TreeTableCell<T, Color> cell = new ColorTreeTableCell<T>(param);
                        cell.setEditable(true);
                        return cell;
                    }
                });
                column.setCellValueFactory(new TreeItemPropertyValueFactory<>(columnSet.getKey()));
                treeTableView.getColumns().add(column);
            }

            
        }

        treeTableView.setEditable(true);
        // default is to use the current x/y formula for the next scan
        useAsDefault.setSelected(true);
        treeTableView.setShowRoot(false);
        // Change to multiple later
        treeTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        treeTableView.getSelectionModel().getSelectedItems().addListener(new SelectedTableItemsChangeListener(treeTableView));
        
        addButton.disableProperty().bind(Bindings.createBooleanBinding(() -> treeTableView.getSelectionModel().getSelectedItem() == null || 
                treeTableView.getSelectionModel().getSelectedItem().getValue() instanceof TraceItem, 
                treeTableView.getSelectionModel().selectedItemProperty()));
        
        EventHandler<ActionEvent> addHandler = e -> {
            if (treeTableView.getSelectionModel().getSelectedItem() == null 
                    || treeTableView.getSelectionModel().getSelectedItem().getValue() instanceof TraceItem) {
                return ;
            }
            final List<TreeItem<T>> selectedItems = treeTableView.getSelectionModel().getSelectedItems();
            
            for (TreeItem<T> selectedItem : selectedItems){
                if (selectedItem.getValue() instanceof TraceItem) {
                    return;
                }
                if (selectedItem.getValue() instanceof ScanItem && optList.isPresent()) {
                    ScanItem scanItem = (ScanItem)selectedItem.getValue();
                    String channel = model.getScanServer()+"/"+String.valueOf(scanItem.getId().intValue())+"/data";
                    TraceItem traceItem = new TraceItem(scanItem.getId(), new ScanValueDataProvider(channel,model.getXformula(),yformula.getText()), yformula.getText(), RGBFactory.next(),
                            TraceType.AREA, 1, PointType.NONE, 1, 0);
                    optList.get().add(traceItem);
                }
            }
        };
        addButton.setOnAction(addHandler);
        
        deleteButton.disableProperty().bind(Bindings.createBooleanBinding(() -> 
        treeTableView.getSelectionModel().getSelectedItem() == null ||
                treeTableView.getSelectionModel().getSelectedItem().getValue() instanceof ScanItem, 
                        treeTableView.getSelectionModel().selectedItemProperty()));
    
        deleteButton.setOnAction(e -> {
            TreeItem<T> selected = treeTableView.getSelectionModel().getSelectedItem() ;
            ScanItem scanItem = (ScanItem)selected.getParent().getValue();
            // work around because parent is lost, and I need to know which parent this trace was deleted from
            selected.getValue().setId(scanItem.getId());
            TraceItem traceItem = (TraceItem)selected.getValue();
            ((AbstractValueDataProvider)traceItem.getData()).close();
            model.getDataStore().get(scanServerItem.getName()).remove(selected.getValue());
            // fire trace change
        });
        
        xformula.setOnDragOver(new EventHandler<DragEvent>() {
            public void handle(DragEvent event) {
                /* data is dragged over the target */
                /* accept it only if it is not dragged from the same node 
                 * and if it has a string data */
                if (event.getGestureSource() != xformula &&
                        event.getDragboard().hasString()) {
                    /* allow for both copying and moving, whatever user chooses */
                    event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                }
                event.consume();
            }
        });
        
        xformula.setOnDragEntered(new EventHandler<DragEvent>() {
            public void handle(DragEvent event) {
                final PseudoClass dropClass = PseudoClass.getPseudoClass("drop");
                /* the drag-and-drop gesture entered the target */
                /* show to the user that it is an actual gesture target */
                 if (event.getGestureSource() != xformula &&
                         event.getDragboard().hasString()) {
                     xformula.pseudoClassStateChanged(dropClass, true);
                 }  
                 event.consume();
            }
        });
        
        xformula.setOnDragExited(new EventHandler<DragEvent>() {
            public void handle(DragEvent event) {
                final PseudoClass dropClass = PseudoClass.getPseudoClass("drop");
                /* mouse moved away, remove the graphical cues */
                xformula.pseudoClassStateChanged(dropClass, false);
                event.consume();
            }
        });
        
        xformula.setOnDragDropped(new EventHandler<DragEvent>() {
            public void handle(DragEvent event) {
                /* data dropped */
                /* if there is a string data on dragboard, read it and use it */
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasString()) {
                    xformula.setText(xformula.getText()+db.getString());
                   success = true;
                }
                /* let the source know whether the string was successfully 
                 * transferred and used */
                event.setDropCompleted(success);
                event.consume();
             }
        });
        
        yformula.setOnDragOver(new EventHandler<DragEvent>() {
            public void handle(DragEvent event) {
                /* data is dragged over the target */
                /* accept it only if it is not dragged from the same node 
                 * and if it has a string data */
                if (event.getGestureSource() != yformula &&
                        event.getDragboard().hasString()) {
                    /* allow for both copying and moving, whatever user chooses */
                    event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                }
                event.consume();
            }
        });
        
        yformula.setOnDragEntered(new EventHandler<DragEvent>() {
            public void handle(DragEvent event) {
                final PseudoClass dropClass = PseudoClass.getPseudoClass("drop");
                /* the drag-and-drop gesture entered the target */
                /* show to the user that it is an actual gesture target */
                 if (event.getGestureSource() != yformula &&
                         event.getDragboard().hasString()) {
                     yformula.pseudoClassStateChanged(dropClass, true);
                 }  
                 event.consume();
            }
        });
        
        yformula.setOnDragExited(new EventHandler<DragEvent>() {
            public void handle(DragEvent event) {
                final PseudoClass dropClass = PseudoClass.getPseudoClass("drop");
                /* mouse moved away, remove the graphical cues */
                yformula.pseudoClassStateChanged(dropClass, false);
                event.consume();
            }
        });
        
        yformula.setOnDragDropped(new EventHandler<DragEvent>() {
            public void handle(DragEvent event) {
                /* data dropped */
                /* if there is a string data on dragboard, read it and use it */
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasString()) {
                    yformula.setText(yformula.getText()+db.getString());
                   success = true;
                }
                /* let the source know whether the string was successfully 
                 * transferred and used */
                event.setDropCompleted(success);
                event.consume();
             }
        });
    }
    
    public void closeConnections() {
        if (pvScanServer != null) {
            pvScanServer.close();
        }
        Optional<ObservableList<TraceItem>> optList = Optional.ofNullable(model.getDataStore().get(scanServerItem.getName()));
        optList.ifPresent(tlist -> tlist.stream().forEach(t ->((AbstractValueDataProvider)t.getData()).close()));
    }
    
    private int getSize(VTable vTable, int column){
        Object data = vTable.getColumnData(column);
        if (data instanceof ListNumber) {
            return ((ListNumber) data).size();
        } else if (data instanceof List) {
            return ((List<?>) data).size();
        } else {
            return 0;
        }
    }
    
    private class ScanTreeItem {
        private final Number scanId;
        private final Boolean isExpanded;
        /**
         * @param isExpanded
         * @param traces
         */
        public ScanTreeItem(Number scanId, Boolean isExpanded) {
            this.scanId = scanId;
            this.isExpanded = isExpanded;
        }
        /**
         * @return the scanId
         */
        public Number getScanId() {
            return scanId;
        }
        /**
         * @return the isExpanded
         */
        public Boolean isExpanded() {
            return isExpanded;
        } 
    }

    private static <T> BinaryOperator<T> toOnlyElement() {
            return toOnlyElementThrowing(IllegalArgumentException::new);
    }
     
    private static <T, E extends RuntimeException> BinaryOperator<T>
    toOnlyElementThrowing(Supplier<E> exception) {
            return (element, otherElement) -> {
                    throw exception.get();
            };
    }
    
    private Optional<TreeItem<T>> getTreeItem(Number scanId) {
        return tree.getTree().getChildren().stream().filter(treeItem -> treeItem.getValue().getId().equals(scanId)).reduce(toOnlyElement());
    }
    
    private void fillTreeTable(Object value, boolean connection) {

        TreeTablePosition<T, ?> currentEditingCell = treeTableView.getEditingCell();
        // don't fill tree if items are being edited
        if (currentEditingCell != null) {
            return;
        }
        if (value instanceof org.diirt.vtype.VTable) {
        	// save expanded state
            List<ScanTreeItem> scanTreeItems = new ArrayList<>();
            ObservableList<TreeItem<T>> scanlist = treeTableView.getRoot().getChildren();
            for (TreeItem<T> item:scanlist) {
                if ( item.getValue() instanceof ScanItem) {
                    ScanItem scanItem = (ScanItem)item.getValue();
                    if (!scanItem.getItems().isEmpty()) {
                        scanTreeItems.add(new ScanTreeItem(scanItem.getId(),item.isExpanded()));
                    }
                }
            }
            // load vtable into tree with model scans
            VTable vTable = (VTable) value;
            LinkedHashMap<Number, ScanItem> columns = new LinkedHashMap<Number,ScanItem>();
            Optional<ObservableList<TraceItem>> optList = Optional.ofNullable(model.getDataStore().get(scanServerItem.getName()));
            for (int n = 0; n < getSize(vTable,0); n++) {
                ScanItem scanItem = new ScanItem(vTable, n);
                // would be faster as a map
                if (optList.isPresent()){
	                List<TraceItem> traces = optList.get()
	        		.stream()
	        		.filter(trace -> trace.getId().equals(scanItem.getId()))
	        		.collect(Collectors.toList());
	                scanItem.getItems().setAll(traces);
                }
                columns.put(scanItem.getId(),scanItem);
            }
            
            // if useAsDefault selected, load traces from sibling scan on new running scan
            if (useAsDefault.isSelected()){
            	List<TreeItem<T>> runningScans = tree.getTree().getChildren().stream()
                		.filter(treeItem -> treeItem.getValue().getStatus().equals("Running"))
                		.collect(Collectors.toList());
            	for(TreeItem<T> runningScan:runningScans){
            		if(runningScan.getChildren().isEmpty() && runningScan.getValue() instanceof ScanItem && runningScan.nextSibling() != null){
            			ScanItem scanItem = (ScanItem)runningScan.getValue();
            			for(TreeItem<T> siblingTrace : runningScan.nextSibling().getChildren()){
            				if(siblingTrace.getValue() instanceof TraceItem){
            					String channel = model.getScanServer()+"/"+String.valueOf(scanItem.getId().intValue())+"/data";
            					TraceItem traceItem = (TraceItem)siblingTrace.getValue();
            					TraceItem newTrace = new TraceItem(scanItem.getId(),new ScanValueDataProvider(channel, model.getXformula(),traceItem.getYformula()), traceItem.getYformula(), RGBFactory.next(),
            							traceItem.getType(), traceItem.getWidth(), traceItem.getPointType(), traceItem.getPointSize(), traceItem.getYAxis());
            					columns.get(scanItem.getId()).getItems().add(newTrace);
            					model.getDataStore().get(scanServerItem.getName()).add(newTrace);
            				}
            			}
            		}
            	}
            }
            // Get previous selection, selection resets to the first item after refilling items
            int selectionId = treeTableView.getSelectionModel().getSelectedIndex();
            ScanServerItem scanServerItem = (ScanServerItem)(tree.getTree().getValue());
            // The treetable selector needs to be disabled during fill items, because it selects every item as added
            atomicBoolean.set(true);
            scanServerItem.getItems().setAll(FXCollections.observableArrayList(columns.values()));
            // set items that were expanded
            for (ScanTreeItem scanTreeItem:scanTreeItems.stream().filter(scanTreeItem -> scanTreeItem.isExpanded().equals(true)).collect(Collectors.toList())) {
                Optional<TreeItem<T>> treeItemOp = getTreeItem(scanTreeItem.getScanId());
                treeItemOp.ifPresent(t -> t.setExpanded(true));
            }
            if (selectionId > 0){
                treeTableView.getSelectionModel().selectIndices(selectionId);
                treeTableView.getSelectionModel().focus(selectionId);
            } else {
                treeTableView.getSelectionModel().selectFirst();
                treeTableView.getSelectionModel().focus(0);
            }
            atomicBoolean.set(false);
        }
    }
}
