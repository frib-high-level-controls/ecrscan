package org.csstudio.scan.ecrscan.ui.controller;

import static org.diirt.datasource.ExpressionLanguage.channel;

import java.io.StringReader;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.csstudio.scan.command.ScanCommand;
import org.csstudio.scan.command.ScanCommandProperty;
import org.csstudio.scan.command.UnknownScanCommandPropertyException;
import org.csstudio.scan.command.XMLCommandWriter;
import org.csstudio.scan.ecrscan.ui.model.AbstractScanTreeItem;
import org.csstudio.scan.ecrscan.ui.model.ModelTreeTable;
import org.csstudio.scan.ecrscan.ui.model.ScanTreeModel;
import org.diirt.datasource.PVManager;
import org.diirt.datasource.PVWriterEvent;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.diirt.datasource.PVWriter;

import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;

public class SidePanelController<T extends AbstractScanTreeItem<?>> {
    
    @FXML
    private TextField channelField;
    @FXML
    private GridPane gridPane;
    @FXML
    private Button start;
    @FXML
    private Button abort;
    @FXML
    private Button pause;
    @FXML
    private Button resume;
    
    private ModelTreeTable<T> model;
    private ScanTreeModel inputModel;
    private PVWriter<Object> pvWriter;
    
    public void initModel(ScanTreeModel inputModel, ModelTreeTable<T> model) {
        this.model = model;
        this.inputModel = inputModel;
        gridPane.getChildren().clear();

        createInputs(this.inputModel.getCommands());
        
        model.selectedScanProperty().addListener(listener);

        start.setOnAction((event) -> {
        	try {
	        	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
		        DocumentBuilder builder = factory.newDocumentBuilder();  
				Document document = builder.parse( new InputSource( new StringReader( XMLCommandWriter.toXMLString(this.inputModel.getCommands()))));
				pvWriter.write(document);
        	} catch (Exception e1) {
				e1.printStackTrace();
			}
        	
        });
        abort.setOnAction((event) -> {
        	// needs to write to id, need to find running scans first
        	pvWriter.write("abort");
        });  
        pause.setOnAction((event) -> {
        	pvWriter.write("pause");
        });
        resume.setOnAction((event) -> {
        	pvWriter.write("resume");
        });
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
    
    private ChangeListener<? super T> listener = (observable, oldValue, newValue) -> {
    	if (pvWriter != null) {
            pvWriter.close();
        }
        String channel = model.getScanServer();
        pvWriter = PVManager.write(channel(channel))
                .writeListener((PVWriterEvent<Object> e) -> {
                	if (e.isWriteSucceeded()) {
                		System.out.println("Write finished");
                	}
                	if (e.isWriteFailed()) {
                		System.out.println("Write failed");
                	}
                })
                .async();
    };
    
    public void closeConnections() {
        if (pvWriter != null) {
            pvWriter.close();
        }
        model.selectedScanProperty().removeListener(listener);
    }
}
