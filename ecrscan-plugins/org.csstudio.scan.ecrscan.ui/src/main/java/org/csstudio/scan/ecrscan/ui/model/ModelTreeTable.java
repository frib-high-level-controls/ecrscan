package org.csstudio.scan.ecrscan.ui.model;

import static java.util.stream.Collectors.toList;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import org.csstudio.javafx.rtplot.PointType;
import org.csstudio.javafx.rtplot.TraceType;
import org.csstudio.scan.ecrscan.ui.data.ScanValueDataProvider;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.scene.control.TreeItem;
import javafx.scene.paint.Color;

public class ModelTreeTable<T> {
    
    private final Function<T, ObservableList<? extends T>> children ;
    private final StringProperty scanServer = new SimpleStringProperty( this , "", null);
    private final ObjectProperty<T> selectedScan = new SimpleObjectProperty<>(null);
    private final ObjectProperty<TreeItem<T>> tree = new SimpleObjectProperty<>(null);
    
    public ModelTreeTable(T rootItem, Function<T, ObservableList<? extends T>> children, 
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
        
        this.children = children;
        this.tree.setValue(createTreeItem(rootItem));
        
    }
    
    public ModelTreeTable( T rootItem,
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
        this(rootItem, children, name, id, finished, created, percent, 
                scanValueDataProvider, yformula, color, traceType, traceWidth, pointType, pointSize, yaxis, x -> null);
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

    public final StringProperty scanServerProperty() {
        return this.scanServer;
    }
    

    public final java.lang.String getScanServer() {
        return this.scanServerProperty().get();
    }
    

    public final void setScanServer(final java.lang.String scanServer) {
        this.scanServerProperty().set(scanServer);
    }

    public final ObjectProperty<T> selectedScanProperty() {
        return this.selectedScan;
    }
    

    public final T getSelectedScan() {
        return this.selectedScanProperty().get();
    }
    

    public final void setSelectedScan(final T selectedScan) {
        this.selectedScanProperty().set(selectedScan);
    }

    public final ObjectProperty<TreeItem<T>> treeProperty() {
        return this.tree;
    }
    

    public final javafx.scene.control.TreeItem<T> getTree() {
        return this.treeProperty().get();
    }
}
