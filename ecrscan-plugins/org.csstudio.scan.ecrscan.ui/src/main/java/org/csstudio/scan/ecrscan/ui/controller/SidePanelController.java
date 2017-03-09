package org.csstudio.scan.ecrscan.ui.controller;

import java.awt.Font;
import java.util.List;

import org.csstudio.scan.command.ScanCommand;
import org.csstudio.scan.command.ScanCommandProperty;
import org.csstudio.scan.command.UnknownScanCommandPropertyException;
import org.csstudio.scan.ecrscan.ui.model.AbstractScanTreeItem;
import org.csstudio.scan.ecrscan.ui.model.ModelTreeTable;
import org.csstudio.scan.ecrscan.ui.model.ScanTreeModel;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;

public class SidePanelController<T extends AbstractScanTreeItem<?>> {
    
    @FXML
    private TextField channelField;
    @FXML
    private GridPane gridPane;
    
    private ModelTreeTable<T> model;
    private ScanTreeModel inputModel;
    
    public void initModel(ScanTreeModel inputModel, ModelTreeTable<T> model) {
        this.model = model;
        this.inputModel = inputModel;
        gridPane.getChildren().clear();

        createInputs(this.inputModel.getCommands());
    }
    
    @FXML
    private void onChannelChanged(ActionEvent event) {
        model.setScanServer(channelField.getText());
    }
    
    private void createInputs(List<ScanCommand> commands) {
    	for (ScanCommand command: commands){
    		if (command.getCommandID().equals("loop") || command.getCommandID().equals("delay")){
    			Label commandLabel = new Label(command.getCommandID());
    			commandLabel.getStyleClass().add("commandLabel");
    			GridPane.setColumnSpan(commandLabel, GridPane.REMAINING);
    			/*commandLabel.setPrefWidth(Region.USE_PREF_SIZE);*/
    			gridPane.addColumn(0, commandLabel);
    			for (ScanCommandProperty property:command.getProperties()){
    				Label propertyLabel = new Label(property.getName());
    				propertyLabel.getStyleClass().add("propertyLabel");
    				/*propertyLabel.setPrefWidth(Region.USE_PREF_SIZE);*/
        			gridPane.addColumn(0, propertyLabel);
    				try {
    					TextField propertyValue = new TextField(command.getProperty(property).toString());
    					/*propertyValue.getStyleClass().add("propertyValue");*/
    					/*propertyValue.setPrefWidth(Region.USE_PREF_SIZE);*/
						gridPane.addColumn(1, propertyValue);
					} catch (UnknownScanCommandPropertyException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
    			}
    		}
    		if (this.inputModel.getChildren(command)!=null){
    			createInputs(this.inputModel.getChildren(command));
    		} else {
    			continue;
    		}
    	}
    }
}
