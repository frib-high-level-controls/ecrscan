package org.csstudio.scan.ecrscan.ui.model;

import static java.util.stream.Collectors.toList;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import org.csstudio.javafx.rtplot.PointType;
import org.csstudio.javafx.rtplot.TraceType;
import org.csstudio.scan.ecrscan.ui.data.ScanValueDataProvider;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.ComboBoxTreeTableCell;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.util.Callback;

public class ModelTreeTable<T> {

    private final TreeTableView<T> treeTableView ;
    
    private final Function<T, ObservableList<? extends T>> children ;
    
    public ModelTreeTable(TreeTableView<T> treeTableView, Function<T, ObservableList<? extends T>> children, 
            Function<T, ObservableValue<String>> name,
            Function<T, ObservableValue<Number>> id,
            Function<T, ObservableValue<Instant>> finished,
            Function<T, ObservableValue<Instant>> created,
            Function<T, ObservableValue<Number>> percent,
            Function<T, ObservableValue<ScanValueDataProvider>> scanValueDataProvider,
            Function<T, ObservableValue<String>> yformula,
            Function<T, ObservableValue<Color>> color,
            Function<T, ObservableValue<TraceType>> traceType,
            Function<T, ObservableValue<Integer>> traceWidth,
            Function<T, ObservableValue<PointType>> pointType,
            Function<T, ObservableValue<Integer>> pointSize,
            Function<T, ObservableValue<Integer>> yaxis,
            Function<T, PseudoClass> pseudoClassMap) {
        
        this.children = children ;
        this.treeTableView = treeTableView;
        

        //treeTableView = new TreeTableView<>(createTreeItem(rootItem));

        //need to make this easier to add columns from a register
        Map<String,Class<?>> columnMap = new LinkedHashMap<String,Class<?>>();
        columnMap.put("name", String.class);
        columnMap.put("id", Number.class);
        columnMap.put("created", Instant.class);
        columnMap.put("finished", Instant.class);
        columnMap.put("percent", Number.class);
        columnMap.put("yformula", String.class);
        columnMap.put("color", Color.class);
        columnMap.put("traceType", TraceType.class);
        columnMap.put("traceWidth", Integer.class);
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
                this.treeTableView.getColumns().add(column);
            }
            if (clazz.equals(Integer.class)) {
                TreeTableColumn<T,Integer> column = new TreeTableColumn<T,Integer>(columnSet.getKey());
                column.setCellValueFactory(new TreeItemPropertyValueFactory<>(columnSet.getKey()));
                this.treeTableView.getColumns().add(column);
            }
            if (clazz.equals(TraceType.class)) {
                TreeTableColumn<T,TraceType> column = new TreeTableColumn<T,TraceType>(columnSet.getKey());
                ObservableList<TraceType> traceTypeList = FXCollections.observableArrayList(TraceType.values());
                column.setCellFactory(ComboBoxTreeTableCell.forTreeTableColumn(traceTypeList));
                column.setCellValueFactory(new TreeItemPropertyValueFactory<>(columnSet.getKey()));
                this.treeTableView.getColumns().add(column);
            }
            if (clazz.equals(PointType.class)) {
                TreeTableColumn<T,PointType> column = new TreeTableColumn<T,PointType>(columnSet.getKey());
                ObservableList<PointType> pointTypeList = FXCollections.observableArrayList(PointType.values());
                column.setCellFactory(ComboBoxTreeTableCell.forTreeTableColumn(pointTypeList));
                column.setCellValueFactory(new TreeItemPropertyValueFactory<>(columnSet.getKey()));
                this.treeTableView.getColumns().add(column);
            }
            if (clazz.equals(Instant.class)) {
                TreeTableColumn<T,Instant> column = new TreeTableColumn<T,Instant>(columnSet.getKey());
                column.setCellValueFactory(new TreeItemPropertyValueFactory<>(columnSet.getKey()));
                this.treeTableView.getColumns().add(column);
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
                
                this.treeTableView.getColumns().add(column);
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
/*                column.setOnEditCommit(new EventHandler<TreeTableColumn.CellEditEvent<T, Color>>() {
                    
                    @Override
                    public void handle(TreeTableColumn.CellEditEvent<T, Color> event) {
                        TreeItem<T> item = event.getRowValue();
                        Color color = event.getNewValue();
                        treeTableView.fireEvent(event);
                    }
                });*/
                column.setCellValueFactory(new TreeItemPropertyValueFactory<>(columnSet.getKey()));
                this.treeTableView.getColumns().add(column);
            }
/*            column.setCellValueFactory(tv -> new TreeCell<T>() {
                
                private Set<PseudoClass> pseudoClassesSet = new HashSet<>();
                
                @Override
                protected void updateItem(T item, boolean empty) {
                    super.updateItem(item, empty);
                    textProperty().unbind();
                    
                    pseudoClassesSet.forEach(pc -> pseudoClassStateChanged(pc, false));
                    if (empty) {
                        setText("");
                    } else {
                        textProperty().bind(text.apply(item));
                        PseudoClass itemPC = pseudoClassMap.apply(item);
                        if (itemPC != null) {
                            pseudoClassStateChanged(itemPC, true);
                            pseudoClassesSet.add(itemPC);
                        }
                    }
                }
            }); 
            treeTableView.getColumns().add(column);*/
        }
    }
    
/*    private class ScanTreeTableViewCell<G> extends TreeTableCell<T,G> {
        private Set<PseudoClass> pseudoClassesSet = new HashSet<>();
        
        @Override
        protected void updateItem(G item, boolean empty) {
            super.updateItem(item, empty);
            textProperty().unbind();
            
            pseudoClassesSet.forEach(pc -> pseudoClassStateChanged(pc, false));
            if (empty) {
                setText("");
            } else {
                textProperty().bind(text.apply(item));
                PseudoClass itemPC = pseudoClassMap.apply(item);
                if (itemPC != null) {
                    pseudoClassStateChanged(itemPC, true);
                    pseudoClassesSet.add(itemPC);
                }
            }
        }
    }*/
    
    public ModelTreeTable(TreeTableView<T> treeTableView , 
            Function<T, ObservableList<? extends T>> children,
            Function<T, ObservableValue<String>> name,
            Function<T, ObservableValue<Number>> id,
            Function<T, ObservableValue<Instant>> finished,
            Function<T, ObservableValue<Instant>> created,
            Function<T, ObservableValue<Number>> percent,
            Function<T, ObservableValue<ScanValueDataProvider>> scanValueDataProvider,
            Function<T, ObservableValue<String>> yformula,
            Function<T, ObservableValue<Color>> color,
            Function<T, ObservableValue<TraceType>> traceType,
            Function<T, ObservableValue<Integer>> traceWidth,
            Function<T, ObservableValue<PointType>> pointType,
            Function<T, ObservableValue<Integer>> pointSize,
            Function<T, ObservableValue<Integer>> yaxis) {
        this(treeTableView, children, name, id, finished, created, percent, 
                scanValueDataProvider, yformula, color, traceType, traceWidth, pointType, pointSize, yaxis, x -> null);
    }

    public TreeTableView<T> getTreeTableView( T rootItem) {
        treeTableView.setEditable(true);
        treeTableView.setRoot(createTreeItem(rootItem));
        return treeTableView ;
    }
    
    private TreeItem<T> createTreeItem(T t) {
        TreeItem<T> item = new TreeItem<>(t);
        children.apply(t).stream().map(this::createTreeItem).forEach(item.getChildren()::add);
        
        children.apply(t).addListener((Change<? extends T> change) -> {
            while (change.next()) {
                
                if (change.wasAdded()) {
                    item.getChildren().addAll(change.getAddedSubList().stream()
                            .map(this::createTreeItem).collect(toList()));
                }
                if (change.wasRemoved()) {
                    item.getChildren().removeIf(treeItem -> change.getRemoved()
                            .contains(treeItem.getValue()));
                }
            }
        });
        
        return item ;
    }
    
}
