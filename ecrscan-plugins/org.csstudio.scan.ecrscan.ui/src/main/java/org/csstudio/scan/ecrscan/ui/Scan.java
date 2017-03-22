package org.csstudio.scan.ecrscan.ui;

import java.io.IOException;
import org.csstudio.scan.ecrscan.ui.controller.DataColumnsController;
import org.csstudio.scan.ecrscan.ui.controller.ScanController;
import org.csstudio.scan.ecrscan.ui.controller.ScanTreeTableController;
import org.csstudio.scan.ecrscan.ui.controller.SidePanelController;
import org.csstudio.scan.ecrscan.ui.model.AbstractScanTreeItem;
import org.csstudio.scan.ecrscan.ui.model.ModelTreeTable;
import org.csstudio.scan.ecrscan.ui.model.ScanModel;
import org.csstudio.scan.ecrscan.ui.model.ScanTreeModel;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;

public class Scan extends VBox {
    private final static FXMLLoader scanPlotLoader;
    private final static FXMLLoader scanTreeTableLoader;
    private final static FXMLLoader sidePanelLoader;
    private final static FXMLLoader dataColumnLoader;
    private final static GridPane root = new GridPane();
    private final static Scene scene;
    
    
    
    static {
        ColumnConstraints firstCol = new ColumnConstraints();
        firstCol.setHgrow(Priority.ALWAYS);
        firstCol.setMaxWidth(Integer.MAX_VALUE);
        firstCol.setMinWidth(456.0);
        firstCol.setPrefWidth(488.0);
        
        ColumnConstraints secondCol = new ColumnConstraints();
        secondCol.setMaxWidth(Integer.MAX_VALUE);
        secondCol.setMinWidth(Region.USE_COMPUTED_SIZE);
        secondCol.setPrefWidth(Region.USE_COMPUTED_SIZE);
        
        root.getColumnConstraints().add(firstCol);
        root.getColumnConstraints().add(secondCol);
        
        RowConstraints firstRow = new RowConstraints();
        firstRow.setMaxHeight(Integer.MAX_VALUE);
        firstRow.setMinHeight(248.0);
        firstRow.setPrefHeight(256.0);
        firstRow.setVgrow(Priority.ALWAYS);
        
        RowConstraints secondRow = new RowConstraints();
        secondRow.setMaxHeight(150.0);
        secondRow.setMinHeight(100.0);
        secondRow.setPrefHeight(150.0);
        
        root.getRowConstraints().add(firstRow);
        root.getRowConstraints().add(secondRow);
        try {
            scanPlotLoader = new FXMLLoader(Scan.class.getResource("/fxml/Scan.fxml"));
            scanTreeTableLoader = new FXMLLoader(Scan.class.getResource("/fxml/ScanTreeTable.fxml"));
            SplitPane splitPane = new SplitPane(scanPlotLoader.load(),scanTreeTableLoader.load());
            splitPane.setDividerPositions(0.90);
            splitPane.setOrientation(Orientation.VERTICAL);
            root.add(splitPane, 0, 0, 1, 2);
            
            sidePanelLoader = new FXMLLoader(Scan.class.getResource("/fxml/SidePanel.fxml"));
            root.add(sidePanelLoader.load(), 1, 0);
            
            dataColumnLoader = new FXMLLoader(Scan.class.getResource("/fxml/DataColumnsViewer.fxml"));
            root.add(dataColumnLoader.load(), 1, 1);  
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        scene = new Scene(root);
        scene.getStylesheets().add(Scan.class.getResource("/styles/Styles.css").toExternalForm());
    }

    public static Scene createScene(ScanTreeModel inputModel, ScanModel<AbstractScanTreeItem<?>> model, ModelTreeTable<AbstractScanTreeItem<?>> tree ) throws Exception {
        ScanController<AbstractScanTreeItem<?>> scanController = scanPlotLoader.getController();
        ScanTreeTableController<AbstractScanTreeItem<?>> scanTreeTableController = scanTreeTableLoader.getController();
        SidePanelController<AbstractScanTreeItem<?>> sidePanelController = sidePanelLoader.getController();
        DataColumnsController<AbstractScanTreeItem<?>> dataColumnsController = dataColumnLoader.getController();
        
        // order matters here
        sidePanelController.initModel(inputModel,model, tree);
        scanController.initModel(model);
        scanTreeTableController.initModel(model, tree);
        dataColumnsController.initModel(model);
        
        return scene;
    }
    
    public static void closeConnections() {
        ScanController<AbstractScanTreeItem<?>> scanController = scanPlotLoader.getController();
        ScanTreeTableController<AbstractScanTreeItem<?>> scanTreeTableController = scanTreeTableLoader.getController();
        SidePanelController<AbstractScanTreeItem<?>> sidePanelController = sidePanelLoader.getController();
        DataColumnsController<AbstractScanTreeItem<?>> dataColumnsController = dataColumnLoader.getController();
        
        scanController.closeConnections();
        scanTreeTableController.closeConnections();
        sidePanelController.closeConnections();
        dataColumnsController.closeConnections();
    }
}