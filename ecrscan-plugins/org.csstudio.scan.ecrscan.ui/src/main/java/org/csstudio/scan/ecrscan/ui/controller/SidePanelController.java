package org.csstudio.scan.ecrscan.ui.controller;

import org.csstudio.scan.ecrscan.ui.model.AbstractScanTreeItem;
import org.csstudio.scan.ecrscan.ui.model.ModelTreeTable;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class SidePanelController<T extends AbstractScanTreeItem<?>> {
    
    @FXML
    private TextField channelField;
    
    private ModelTreeTable<T> model;
    
    public void initModel(ModelTreeTable<T> model) {
        this.model = model;
    }
    
    @FXML
    private void onChannelChanged(ActionEvent event) {
        model.setScanServer(channelField.getText());
    }
}
