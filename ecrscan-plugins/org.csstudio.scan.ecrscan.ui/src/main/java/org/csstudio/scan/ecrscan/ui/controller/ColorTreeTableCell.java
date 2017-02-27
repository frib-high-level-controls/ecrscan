package org.csstudio.scan.ecrscan.ui.controller;

import javafx.scene.control.ColorPicker;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.paint.Color;

/**
 * @author Michael J. Simons, 2014-10-17
 * @param <T>
 * Modified from Simons
 */
public class ColorTreeTableCell<T> extends TreeTableCell<T, Color> {    
    private final ColorPicker colorPicker;
    
    public ColorTreeTableCell(TreeTableColumn<T, Color> column) {
        this.colorPicker = new ColorPicker();
        this.colorPicker.editableProperty().bind(column.editableProperty());
        this.colorPicker.disableProperty().bind(column.editableProperty().not());
        this.colorPicker.setOnShowing(event -> {
            final TreeTableView<T> tableView = getTreeTableView();
            tableView.getSelectionModel().select(getTreeTableRow().getIndex());
            tableView.edit(tableView.getSelectionModel().getSelectedIndex(), column);       
        });
        this.colorPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(isEditing()) {
                commitEdit(newValue);
            }
        });             
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }
         
    @Override
    protected void updateItem(Color item, boolean empty) {
        super.updateItem(item, empty);  
        setText(null);  
        if(empty || item == null) {         
            setGraphic(null);
        } else {            
            this.colorPicker.setValue(item);
            this.setGraphic(this.colorPicker);
        } 
    }
}
