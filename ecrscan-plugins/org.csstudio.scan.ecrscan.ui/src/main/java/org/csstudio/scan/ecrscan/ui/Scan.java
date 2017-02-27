package org.csstudio.scan.ecrscan.ui;

import java.io.IOException;
import org.csstudio.scan.ecrscan.ui.controller.DataColumnsController;
import org.csstudio.scan.ecrscan.ui.controller.ScanController;
import org.csstudio.scan.ecrscan.ui.controller.ScanTreeTableController;
import org.csstudio.scan.ecrscan.ui.controller.SidePanelController;
import org.csstudio.scan.ecrscan.ui.model.AbstractScanTreeItem;
import org.csstudio.scan.ecrscan.ui.model.ModelTreeTable;
import org.csstudio.scan.ecrscan.ui.model.ScanServerItem;
import javafx.css.PseudoClass;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class Scan extends VBox {
    
    public Scan() {
       FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/fxml/Scan_main.fxml"));

        fxmlLoader.setRoot(this);
        
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static Scene createScene() throws Exception {
        //Parent root = FXMLLoader.load(Scan.class.getResource("/fxml/Scan_main.fxml"));
        GridPane root = new GridPane();
        
        FXMLLoader scanPlotLoader = new FXMLLoader(Scan.class.getResource("/fxml/Scan.fxml"));
        root.add(scanPlotLoader.load(), 0, 0);
        ScanController<AbstractScanTreeItem<?>> scanController = scanPlotLoader.getController();
        
        FXMLLoader scanTreeTableLoader = new FXMLLoader(Scan.class.getResource("/fxml/ScanTreeTable.fxml"));
        root.add(scanTreeTableLoader.load(), 0, 1);
        ScanTreeTableController<AbstractScanTreeItem<?>> scanTreeTableController = scanTreeTableLoader.getController();
        
        FXMLLoader sidePanelLoader = new FXMLLoader(Scan.class.getResource("/fxml/SidePanel.fxml"));
        root.add(sidePanelLoader.load(), 1, 0);
        SidePanelController<AbstractScanTreeItem<?>> sidePanelController = sidePanelLoader.getController();
        
        FXMLLoader dataColumnLoader = new FXMLLoader(Scan.class.getResource("/fxml/DataColumnsViewer.fxml"));
        root.add(dataColumnLoader.load(), 1, 1);
        DataColumnsController<AbstractScanTreeItem<?>> dataColumnsController = dataColumnLoader.getController();
        

        ScanServerItem scanServerItem = new ScanServerItem("ecrscan");
        ModelTreeTable<AbstractScanTreeItem<?>> model = new ModelTreeTable<AbstractScanTreeItem<?>>(
                scanServerItem,
                AbstractScanTreeItem::getItems,
                AbstractScanTreeItem::nameProperty,
                AbstractScanTreeItem::idProperty,
                AbstractScanTreeItem::finishedProperty,
                AbstractScanTreeItem::createdProperty,
                AbstractScanTreeItem::percentProperty,
                AbstractScanTreeItem::scanValueDataProviderProperty,
                AbstractScanTreeItem::yformulaProperty,
                AbstractScanTreeItem::colorProperty,
                AbstractScanTreeItem::traceTypeProperty,
                AbstractScanTreeItem::traceWidthProperty,
                AbstractScanTreeItem::pointTypeProperty,
                AbstractScanTreeItem::pointSizeProperty,
                AbstractScanTreeItem::yaxisProperty,
                item -> PseudoClass.getPseudoClass(item.getClass().getSimpleName().toLowerCase()));
        
        scanController.initModel(model);
        scanTreeTableController.initModel(model);
        sidePanelController.initModel(model);
        dataColumnsController.initModel(model);
        
        
        Scene scene = new Scene(root);
        scene.getStylesheets().add("/styles/Styles.css");
        return scene;
    }
}