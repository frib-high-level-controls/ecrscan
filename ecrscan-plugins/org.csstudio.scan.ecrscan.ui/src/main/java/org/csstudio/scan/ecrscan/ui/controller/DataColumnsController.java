package org.csstudio.scan.ecrscan.ui.controller;

import static org.diirt.datasource.ExpressionLanguage.channel;

import java.util.List;
import org.csstudio.scan.ecrscan.ui.model.AbstractScanTreeItem;
import org.csstudio.scan.ecrscan.ui.model.ModelTreeTable;
import org.diirt.datasource.PVManager;
import org.diirt.datasource.PVReader;
import org.diirt.datasource.PVReaderEvent;
import org.diirt.javafx.util.Executors;
import org.diirt.util.time.TimeDuration;
import org.diirt.vtype.VTable;
import org.diirt.vtype.table.VTableFactory;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;

public class DataColumnsController<T extends AbstractScanTreeItem<?>> {
    
    @FXML
    private ListView<String> columnListView;
    
    private ModelTreeTable<T> model;
    private static PVReader<Object> pvReader;
    
    public DataColumnsController() {
        
        columnListView.setOnDragDetected(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                /* drag was detected, start a drag-and-drop gesture*/
                /* allow any transfer mode */
                Dragboard db = columnListView.startDragAndDrop(TransferMode.ANY);
                /* Put a string on a dragboard */
                ClipboardContent content = new ClipboardContent();
                content.putString(columnListView.getSelectionModel().getSelectedItem());
                db.setContent(content);
                event.consume();
            }
        });
        columnListView.setOnDragDone(new EventHandler<DragEvent>() {
            public void handle(DragEvent event) {
                /* the drag and drop gesture ended */
                /* if the data was successfully moved, you could clear it, but don't*/
                if (event.getTransferMode() == TransferMode.MOVE) {
                    //
                }
                event.consume();
            }
        });
        
        model.selectedScanProperty().addListener((observable, oldValue, newValue) -> {
            if (pvReader != null) {
                pvReader.close();
            }
            String channel = model.getScanServer()+"/"+String.valueOf(newValue.getId())+"/data";
            pvReader = PVManager.read(channel(channel))
                    .readListener((PVReaderEvent<Object> e) -> {
                        Object readObject = e.getPvReader().getValue();
                        if (readObject instanceof VTable) {
                            VTable readVTable = (VTable)readObject;
                            List<String> columnNames = VTableFactory.columnNames(readVTable);
                            setItems(columnNames);
                        }
                    })
                    .timeout(TimeDuration.ofSeconds(1), "Still connecting...")
                    .notifyOn(Executors.javaFXAT())
                    .maxRate(TimeDuration.ofHertz(1));
        });
        
    }
    
    public void initModel(ModelTreeTable<T> model) {
        this.model = model;
    }
    
    private void setItems(List<String> columnNames) {
        columnListView.getItems().clear();
        columnListView.getItems().addAll(columnNames);
    }
}
