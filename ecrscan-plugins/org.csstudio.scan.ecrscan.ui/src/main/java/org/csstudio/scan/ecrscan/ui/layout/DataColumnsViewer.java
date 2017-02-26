package org.csstudio.scan.ecrscan.ui.layout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.csstudio.scan.ecrscan.ui.data.ScanData;
import org.csstudio.scan.ecrscan.ui.events.PVEvent;
import org.csstudio.scan.ecrscan.ui.events.ReadEvent;
import org.csstudio.scan.ecrscan.ui.events.XColumnSelectionEvent;
import org.diirt.vtype.VTable;
import org.diirt.vtype.table.VTableFactory;

import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;

public class DataColumnsViewer extends BorderPane {
    
    @FXML
    private ListView<String> columnListView;

    private final ObjectProperty<ListCell<String>> dragSource = new SimpleObjectProperty<>();
    
    private ScanData eventScanData = new ScanData(()->{onNewEvent();});
    
    public DataColumnsViewer() {
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/fxml/DataColumnsViewer.fxml"));

        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        
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
        
        columnListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if(newValue != null) {
                columnListView.fireEvent(new XColumnSelectionEvent(columnListView.getSelectionModel().getSelectedItem()));
                }
            }
        });
    }
    
    public String getSelectedColumnListView() {
        return columnListView.getSelectionModel().getSelectedItem();
    }
    
    public ScanData eventScanData() {
        return eventScanData;
    }
     
    public void setItems(List<String> columnNames) {
        columnListView.getItems().clear();
        columnListView.getItems().addAll(columnNames);
    }
    
    private void onNewEvent() {
        PVEvent newValue = eventScanData.getEvents().get(eventScanData.getEvents().size() - 1);
        if (newValue instanceof ReadEvent) {
            ReadEvent readEvent = (ReadEvent) newValue;
            Object readObject = readEvent.getValue();
            if (readObject instanceof VTable) {
                columnListView.getItems().clear();
                VTable readVTable = (VTable)readObject;
                List<String> columnNames = VTableFactory.columnNames(readVTable);
                columnListView.getItems().addAll(columnNames);
            }
        }
    }
}
