package org.csstudio.scan.ecrscan.ui.controller;

import static org.diirt.datasource.ExpressionLanguage.channel;

import java.io.StringReader;
import java.util.List;
import java.util.stream.Collectors;

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

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
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
    
    public void initModel(ScanTreeModel inputModel, ModelTreeTable<T> model) {
        this.model = model;
        this.inputModel = inputModel;
        gridPane.getChildren().clear();

        createInputs(this.inputModel.getCommands());

        start.setOnAction((event) -> {
			PVWriter<Object> pvSubmitWriter = PVManager.write(channel(model.getScanServer()))
	                .writeListener((PVWriterEvent<Object> e) -> {
	                	if (e.isWriteSucceeded()) {
	                		//System.out.println("Write finished");
	                	}
	                	if (e.isWriteFailed()) {
	                		//System.out.println("Write failed");
	                	}
	                })
	                .sync();
			try {
	        	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
		        DocumentBuilder builder = factory.newDocumentBuilder();  
				Document document = builder.parse( new InputSource( new StringReader( XMLCommandWriter.toXMLString(this.inputModel.getCommands()))));
				pvSubmitWriter.write(document);
        	} catch (Exception e1) {
				e1.printStackTrace();
			} finally {
				pvSubmitWriter.close();
			}
        });
        abort.setOnAction((event) -> {
        	List<TreeItem<T>> runningScans = model.getTree().getChildren().stream()
        		.filter(treeItem -> treeItem.getValue().getStatus().equals("Running") ||
        				treeItem.getValue().getStatus().equals("Paused"))
        		.collect(Collectors.toList());
        	for(TreeItem<T> runningScan:runningScans){
        		Number id = runningScan.getValue().getId();
        		if(id!=null){
        			PVWriter<Object> pvAbortWriter = PVManager.write(channel(model.getScanServer()+"/"+String.valueOf(id.intValue())))
        	                .writeListener((PVWriterEvent<Object> e) -> {
        	                	if (e.isWriteSucceeded()) {
        	                		//System.out.println("Write finished");
        	                	}
        	                	if (e.isWriteFailed()) {
        	                		//System.out.println("Write failed");
        	                	}
        	                })
        	                .sync();
        			pvAbortWriter.write("abort");
        			pvAbortWriter.close();
        		}
        	}
        });  
        pause.setOnAction((event) -> {
        	List<TreeItem<T>> runningScans = model.getTree().getChildren().stream()
            		.filter(treeItem -> treeItem.getValue().getStatus().equals("Running"))
            		.collect(Collectors.toList());
            	for(TreeItem<T> runningScan:runningScans){
            		Number id = runningScan.getValue().getId();
            		if(id!=null){
            			PVWriter<Object> pvPauseWriter = PVManager.write(channel(model.getScanServer()+"/"+String.valueOf(id.intValue())))
            	                .writeListener((PVWriterEvent<Object> e) -> {
            	                	if (e.isWriteSucceeded()) {
            	                		//System.out.println("Write finished");
            	                	}
            	                	if (e.isWriteFailed()) {
            	                		//System.out.println("Write failed");
            	                	}
            	                })
            	                .sync();
            			pvPauseWriter.write("pause");
            			pvPauseWriter.close();
            		}
            	}
        });
        resume.setOnAction((event) -> {
        	List<TreeItem<T>> runningScans = model.getTree().getChildren().stream()
            		.filter(treeItem -> treeItem.getValue().getStatus().equals("Paused"))
            		.collect(Collectors.toList());
            	for(TreeItem<T> runningScan:runningScans){
            		Number id = runningScan.getValue().getId();
            		if(id!=null){
            			PVWriter<Object> pvResumeWriter = PVManager.write(channel(model.getScanServer()+"/"+String.valueOf(id.intValue())))
            	                .writeListener((PVWriterEvent<Object> e) -> {
            	                	if (e.isWriteSucceeded()) {
            	                		//System.out.println("Write finished");
            	                	}
            	                	if (e.isWriteFailed()) {
            	                		//System.out.println("Write failed");
            	                	}
            	                })
            	                .sync();
            			pvResumeWriter.write("resume");
            			pvResumeWriter.close();
            		}
            	}
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
    
    
    public void closeConnections() {

    }
}
