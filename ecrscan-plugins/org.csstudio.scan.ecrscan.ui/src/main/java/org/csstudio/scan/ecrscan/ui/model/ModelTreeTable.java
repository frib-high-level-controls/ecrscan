package org.csstudio.scan.ecrscan.ui.model;

import static java.util.stream.Collectors.toList;

import java.time.Instant;
import java.util.function.Function;

import org.csstudio.javafx.rtplot.PointType;
import org.csstudio.javafx.rtplot.TraceType;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.scene.control.TreeItem;
import javafx.scene.paint.Color;

public class ModelTreeTable<T> {
    
    private final Function<T, ObservableList<? extends T>> children ;

    private final ObjectProperty<TreeItem<T>> tree = new SimpleObjectProperty<>(null);
    
    public ModelTreeTable(T rootItem, Function<T, ObservableList<? extends T>> children, 
            Function<T, ObservableValue<String>> name,
            Function<T, ObservableValue<Number>> id,
            Function<T, ObservableValue<Instant>> finished,
            Function<T, ObservableValue<Instant>> created,
            Function<T, ObservableValue<Number>> percent,
            Function<T, ObservableValue<String>> yformula,
            Function<T, ObservableValue<Color>> color,
            Function<T, ObservableValue<TraceType>> type,
            Function<T, ObservableValue<Integer>> width,
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
            Function<T, ObservableValue<String>> yformula,
            Function<T, ObservableValue<Color>> color,
            Function<T, ObservableValue<TraceType>> type,
            Function<T, ObservableValue<Integer>> width,
            Function<T, ObservableValue<PointType>> pointType,
            Function<T, ObservableValue<Integer>> pointSize,
            Function<T, ObservableValue<Integer>> yaxis) {
        this(rootItem, children, name, id, finished, created, percent, 
                yformula, color, type, width, pointType, pointSize, yaxis, x -> null);
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

    public final ObjectProperty<TreeItem<T>> treeProperty() {
        return this.tree;
    }

    public final javafx.scene.control.TreeItem<T> getTree() {
        return this.treeProperty().get();
    }   
}
