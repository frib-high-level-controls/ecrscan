package org.csstudio.scan.ecrscan.ui.controller;

import java.util.List;

import org.csstudio.scan.command.ScanCommand;
import org.csstudio.scan.command.ScanCommandProperty;
import org.csstudio.scan.command.UnknownScanCommandPropertyException;
import org.csstudio.scan.command.XMLCommandWriter;
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
    private XMLCommandWriter xMLCommandWriter = new XMLCommandWriter();
    
    public void initModel(ScanTreeModel inputModel, ModelTreeTable<T> model) {
        this.model = model;
        this.inputModel = inputModel;
        gridPane.getChildren().clear();

        createInputs(this.inputModel.getCommands());
        
      /*  try {
	        // on submit
	        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
	        DocumentBuilder builder = factory.newDocumentBuilder();  
        
			Document document = builder.parse( new InputSource( new StringReader( xMLCommandWriter.toXMLString(this.inputModel.getCommands()) ) ) );
			//submit doc to pvmanager
        } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} */
    }
    
    @FXML
    private void onChannelChanged(ActionEvent event) {
        model.setScanServer(channelField.getText());
    }
    
    private void createInputs(List<ScanCommand> commands) {
    	for (ScanCommand command: commands){
    		if (command.getCommandID().equals("loop") || command.getCommandID().equals("delay") || command.getCommandID().equals("set")){
    			String loop = "";
    			if (command.getCommandID().equals("loop") || command.getCommandID().equals("set"))
					try {
						loop = command.getProperty("device_name").toString();
					} catch (UnknownScanCommandPropertyException e1) {
						e1.printStackTrace();
					}
    			Label commandLabel = new Label(command.getCommandID()+" - "+loop);
    			commandLabel.getStyleClass().add("commandLabel");
    			GridPane.setColumnSpan(commandLabel, GridPane.REMAINING);
    			commandLabel.setMinWidth(Region.USE_PREF_SIZE);
    			/*commandLabel.setPrefWidth(Region.USE_PREF_SIZE);*/
    			gridPane.addColumn(0, commandLabel);
    			for (ScanCommandProperty property:command.getProperties()){
    				if (property.getID().equals("device_name"))
    					continue;
    				Label propertyLabel = new Label(property.getName());
    				propertyLabel.getStyleClass().add("propertyLabel");
    				/*propertyLabel.setPrefWidth(Region.USE_PREF_SIZE);*/
    				propertyLabel.setMinWidth(Region.USE_COMPUTED_SIZE);
    				propertyLabel.setWrapText(true);
        			gridPane.addColumn(0, propertyLabel);
    				try {
    					TextField propertyValue = new TextField(command.getProperty(property).toString());
    					propertyValue.getStyleClass().add("editedPropertyValue");
    					propertyValue.textProperty().addListener((observable, oldValue, newValue) ->{
    						try {
								command.setProperty(property, newValue);
								propertyValue.getStyleClass().remove("editedPropertyValue");
							} catch (Exception e) {
								e.printStackTrace();
							}
    					});
    					/*propertyValue.getStyleClass().add("propertyValue");*/
    					/*propertyValue.setPrefWidth(Region.USE_PREF_SIZE);*/
    					propertyValue.setMinWidth(Region.USE_PREF_SIZE);
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
