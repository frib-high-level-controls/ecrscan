package org.csstudio.scan.ecrscan.ui.layout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.csstudio.javafx.rtplot.PointType;
import org.csstudio.javafx.rtplot.TraceType;
import org.csstudio.javafx.rtplot.util.RGBFactory;
import org.csstudio.scan.ecrscan.ui.data.ScanValueDataProvider;
import org.csstudio.scan.ecrscan.ui.events.ScanItemSelectionEvent;
import org.csstudio.scan.ecrscan.ui.events.ScanItemSelectionEvent.SELECTION;
import org.csstudio.scan.ecrscan.ui.model.AbstractScanTreeItem;
import org.csstudio.scan.ecrscan.ui.model.ModelTreeTable;
import org.csstudio.scan.ecrscan.ui.model.ScanItem;
import org.csstudio.scan.ecrscan.ui.model.ScanServerItem;
import org.csstudio.scan.ecrscan.ui.model.TraceItem;
import org.diirt.util.array.ListNumber;
import org.diirt.vtype.VTable;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;

public class ScanTreeTableView extends BorderPane {

    private ScanServerItem scanServerItem;
    private ModelTreeTable<AbstractScanTreeItem<?>> tree;
    private RGBFactory RGBFactory = new RGBFactory();
    
    @FXML
    private TreeTableView<AbstractScanTreeItem<?>> treeTableView;
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

    public ScanTreeTableView() {
        
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/fxml/ScanTreeTable.fxml"));

        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        // default is to use the current x/y formula for the next scan
        useAsDefault.setSelected(true);
        setScanTreeTableRoots();
        setValue(null, new ArrayList<TraceItem>(), false);
        //maybe from saved editor
        //setDefaultTraces();
        treeTableView.setShowRoot(false);
        treeTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        treeTableView.getSelectionModel().getSelectedItems().addListener(new SelectedTableItemsChangeListener(treeTableView));
        
        addButton.disableProperty().bind(Bindings.createBooleanBinding(() -> treeTableView.getSelectionModel().getSelectedItem() == null || 
                treeTableView.getSelectionModel().getSelectedItem().getValue() instanceof TraceItem, 
                treeTableView.getSelectionModel().selectedItemProperty()));
        
        EventHandler<ActionEvent> addHandler = e -> {
            if (treeTableView.getSelectionModel().getSelectedItem() == null 
                    || treeTableView.getSelectionModel().getSelectedItem().getValue() instanceof TraceItem) {
                return ;
            }
            final List<TreeItem<AbstractScanTreeItem<?>>> selectedItems = treeTableView.getSelectionModel().getSelectedItems();
            for (TreeItem<AbstractScanTreeItem<?>> selectedItem : selectedItems){
                if (selectedItem.getValue() instanceof TraceItem) {
                    return;
                }
                if (selectedItem.getValue() instanceof ScanItem) {
                    ScanItem scanItem = (ScanItem)selectedItem.getValue();
                    TraceItem traceItem = new TraceItem(new ScanValueDataProvider(), yformula.getText(), RGBFactory.next(),
                            TraceType.AREA, 1, PointType.NONE, 1, 0);
                    traceItem.colorProperty().addListener((observable, oldValue, newValue) -> {
                        System.out.print("color: "+newValue.toString()+"\n");
                    });
                    traceItem.traceTypeProperty().addListener((observable, oldValue, newValue) -> {
                        System.out.print("type: "+newValue.toString()+"\n");
                     });
                    scanItem.getItems().add(traceItem);
                }
            }
        };
        addButton.setOnAction(addHandler);
        
        deleteButton.disableProperty().bind(Bindings.createBooleanBinding(() -> 
        treeTableView.getSelectionModel().getSelectedItem() == null ||
                treeTableView.getSelectionModel().getSelectedItem().getValue() instanceof ScanItem ||
                treeTableView.getSelectionModel().getSelectedItem().getValue() == scanServerItem, 
                        treeTableView.getSelectionModel().selectedItemProperty()));
    
        deleteButton.setOnAction(e -> {
            TreeItem<AbstractScanTreeItem<?>> selected = treeTableView.getSelectionModel().getSelectedItem() ;
            ScanItem scanItem = (ScanItem)selected.getParent().getValue();
            // work around because parent is lost, and I need to know which parent this trace was deleted from
            selected.getValue().setId(scanItem.getId());
            selected.getParent().getValue().getItems().remove(selected.getValue());
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
    
    public String getXFormula() {
        return xformula.getText();
    }
    
    public TreeItem<AbstractScanTreeItem<?>> getTreeTableRoot() {
        return treeTableView.getRoot();
    }
    
    private final class SelectedTableItemsChangeListener implements ListChangeListener<TreeItem<AbstractScanTreeItem<?>>> {
        private final TreeTableView<AbstractScanTreeItem<?>> treeTableView;

        public SelectedTableItemsChangeListener(final TreeTableView<AbstractScanTreeItem<?>> treeTableView) {
                this.treeTableView = treeTableView;
        }

        @Override
        public void onChanged(final ListChangeListener.Change<? extends TreeItem<AbstractScanTreeItem<?>>> change) {
                final boolean next = change.next();
                if (next) {
                    final List<TreeItem<AbstractScanTreeItem<?>>> dataItems = treeTableView.getSelectionModel().getSelectedItems();
                    treeTableView.fireEvent(new ScanItemSelectionEvent(dataItems, SELECTION.SELECTED));
                        if (change.wasAdded()) {
                                final ArrayList<? extends TreeItem<AbstractScanTreeItem<?>>> dataItemsAdded = new ArrayList<>(change.getAddedSubList());
                                treeTableView.fireEvent(new ScanItemSelectionEvent(dataItemsAdded, SELECTION.ADDED));
                        } else if (change.wasRemoved()) {
                                final ArrayList<? extends TreeItem<AbstractScanTreeItem<?>>> dataItemsRemoved = new ArrayList<>(change.getRemoved());
                                treeTableView.fireEvent(new ScanItemSelectionEvent(dataItemsRemoved, SELECTION.REMOVED));
                        }
                }
        }
    }
    
    private void setScanTreeTableRoots() {
        //This should be plugins to support different roots (ie. scanserver, logbook)
        scanServerItem = new ScanServerItem("ecrscan");
        tree = new ModelTreeTable<AbstractScanTreeItem<?>>(treeTableView,
                AbstractScanTreeItem::getItems,
                AbstractScanTreeItem::nameProperty,
                AbstractScanTreeItem::idProperty,
                AbstractScanTreeItem::finishedProperty,
                AbstractScanTreeItem::createdProperty,
                AbstractScanTreeItem::percentProperty,
                AbstractScanTreeItem::scanValueDataProviderProperty,
                AbstractScanTreeItem::yformulaProperty,
                AbstractScanTreeItem::colorProperty,
                AbstractScanTreeItem::traceTypeProperty,
                AbstractScanTreeItem::traceWidthProperty,
                AbstractScanTreeItem::pointTypeProperty,
                AbstractScanTreeItem::pointSizeProperty,
                AbstractScanTreeItem::yaxisProperty,
                item -> PseudoClass.getPseudoClass(item.getClass().getSimpleName().toLowerCase()));
        
    }

    public void setValue(VTable value, List<TraceItem> traces, boolean connection) {
        fillTreeTable(value, traces);   
        treeTableView = tree.getTreeTableView(scanServerItem);
    }
    
    private int getSize(VTable vTable, int column){
        Object data = vTable.getColumnData(column);
        if (data instanceof ListNumber) {
            return ((ListNumber) data).size();
        } else if (data instanceof List) {
            return ((List) data).size();
        } else {
            return 0;
        }
    }
    
    final private Set<AbstractScanTreeItem<?>> set = new TreeSet<AbstractScanTreeItem<?>>(new Comparator<AbstractScanTreeItem<?>> () {
        @Override
        public int compare(AbstractScanTreeItem<?> o1, AbstractScanTreeItem<?> o2) {
            if(o1.getId().intValue() < o2.getId().intValue()) return -1;
            else if(o1.getId().intValue() > o2.getId().intValue()) return 1;
            return o1.getName().compareTo(o2.getName());
        }
       });

    private void fillTreeTable(Object value, List<TraceItem> traces) {
        //This should be plugins to support different roots (ie. scanserver, logbook)
        if (value instanceof org.diirt.vtype.VTable) {
            VTable vTable = (VTable) value;
            List<ScanItem> columns = new ArrayList<ScanItem>();
            for (int n = 0; n < getSize(vTable,0); n++) {
                ScanItem scanItem = new ScanItem(vTable, n);
                if(n==0 && useAsDefault.isSelected() == true) {
                    TraceItem traceItem = new TraceItem(new ScanValueDataProvider(), yformula.getText(), RGBFactory.next(),
                            TraceType.AREA, 1, PointType.NONE, 1, 0);
                    scanItem.getItems().add(traceItem);
                }
                columns.add(scanItem);
            }
            set.clear();
            set.addAll(treeTableView.getRoot().getValue().getItems());
            set.addAll(columns);
            scanServerItem.getItems().addAll(FXCollections.observableArrayList(columns));
        }
    }
    
    
}
