package org.csstudio.scan.ecrscan.ui.layout;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.csstudio.scan.ecrscan.ui.events.DataItemSelectionEvent;
import org.csstudio.scan.ecrscan.ui.events.DataItemSelectionEvent.SELECTION;
import org.diirt.util.array.ListNumber;
import org.diirt.vtype.Display;
import org.diirt.vtype.VTable;
import org.diirt.vtype.VTypeToString;
import org.diirt.vtype.ValueUtil;

import com.sun.javafx.collections.ImmutableObservableList;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

public class ScanTable extends ScrollPane {
    
    @FXML
    private TitledPane tableMetadata;
    @FXML
    private TableView<VTableColumn> columnsTable;
    @FXML
    private TableColumn<VTableColumn, Boolean> columnActiveColumn;
    @FXML
    private TableColumn<VTableColumn, Number> columnIdColumn;
    @FXML
    private TableColumn<VTableColumn, Instant> columnCreatedColumn;
    @FXML
    private TableColumn<VTableColumn, Instant> columnFinishColumn;
    @FXML
    private TableColumn<VTableColumn, Number> columnPercentColumn;
    @FXML
    private ListView<String> labelsField;
    
    // allow multiple datasets from differnt datasources, ie. logbook or file
    private int dataSetIndex = 0;

    public ScanTable() {
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/fxml/ScanTable.fxml"));

        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        columnIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        columnCreatedColumn.setCellValueFactory(new PropertyValueFactory<>("created"));
        columnFinishColumn.setCellValueFactory(new PropertyValueFactory<>("finish"));
        columnPercentColumn.setCellValueFactory(new PropertyValueFactory<>("percent"));
        columnsTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        addSelectionListener();
        
        setValue(null, false);
    }
    
    private Object value;
    
    public void setValue(Object value, boolean connection) {
        tableMetadata(value);
        this.value = value;
    }
    
    public void setDataSetIndex(int index) {
        this.dataSetIndex = index;
    }
    
    public void selectDataItem(final int dataItemIndex) {
        columnsTable.getSelectionModel().clearAndSelect(dataItemIndex);
        columnsTable.getFocusModel().focus(dataItemIndex);
        columnsTable.scrollTo(dataItemIndex - 5);
    }

    public void clearTableSelection() {
        columnsTable.getSelectionModel().clearSelection();
    }
    
    private void addSelectionListener() {
        columnsTable.getSelectionModel().getSelectedIndices().addListener(new SelectedTableItemsChangeListener(columnsTable)); 
    }
    
    private final class SelectedTableItemsChangeListener implements ListChangeListener<Integer> {
        private final TableView<VTableColumn> tableView;

        public SelectedTableItemsChangeListener(final TableView<VTableColumn> tableView) {
                this.tableView = tableView;
        }

        @Override
        public void onChanged(final ListChangeListener.Change<? extends Integer> change) {
                final boolean next = change.next();
                if (next) {
                    final List<Number> sids = tableView.getSelectionModel().getSelectedItems().stream().map(VTableColumn::getId).collect(Collectors.toList());
                        tableView.fireEvent(new DataItemSelectionEvent(dataSetIndex, sids, SELECTION.SELECTED));
                        if (change.wasAdded()) {
                                final ArrayList<Integer> list = new ArrayList<>(change.getAddedSubList());
                                final List<Number> ids = tableView.getItems().stream().filter(t -> list.contains(t.columnIndex)).map(VTableColumn::getId).collect(Collectors.toList());
                                tableView.fireEvent(new DataItemSelectionEvent(dataSetIndex, ids, SELECTION.ADDED));
                        } else if (change.wasRemoved()) {
                                final ArrayList<Integer> list = new ArrayList<>(change.getRemoved());
                                final List<Number> ids = tableView.getItems().stream().filter(t -> list.contains(t.columnIndex)).map(VTableColumn::getId).collect(Collectors.toList());
                                tableView.fireEvent(new DataItemSelectionEvent(dataSetIndex, ids, SELECTION.REMOVED));
                        }
                }
        }
    }
    
    public static class VTableColumn {
        private final VTable vTable;
        private final int columnIndex;

        public VTableColumn(VTable vTable, int columnIndex) {
            this.vTable = vTable;
            this.columnIndex = columnIndex;
        }
        
        public Number getId() {
            Object data = vTable.getColumnData(0);
            if (data instanceof ListNumber) {
                return ((ListNumber) data).getInt(columnIndex);
            } else if (data instanceof List) {
                return (Number)((List) data).get(columnIndex);
            } else {
                return 0;
            }
        }
        
        public Instant getFinish() {
            Object data = vTable.getColumnData(4);
            if (data instanceof List) {
                return (Instant)((List) data).get(columnIndex);
            } else {
                return Instant.MIN;
            }
        }
        
        public Instant getCreated() {
            Object data = vTable.getColumnData(1);
            if (data instanceof List) {
                return (Instant)((List) data).get(columnIndex);
            } else {
                return Instant.MIN;
            }
        }
        
        public Number getPercent() {
            Object data = vTable.getColumnData(5);
            if (data instanceof ListNumber) {
                return ((ListNumber) data).getInt(columnIndex);
            } else if (data instanceof List) {
                return (Number)((List) data).get(columnIndex);
            } else {
                return 0;
            }
        }
        
        
    }
    
    private int getSize(VTable vTable, int column){
        Object data = vTable.getColumnData(column);
        if (data instanceof ListNumber) {
            return ((ListNumber) data).size();
        } else if (data instanceof List) {
            return ((List) data).size();
        } else {
            return 0;
        }
    }
    
    private void tableMetadata(Object value) {
        if (value instanceof org.diirt.vtype.VTable) {
            tableMetadata.setVisible(true);
            tableMetadata.setManaged(true);
            VTable vTable = (VTable) value;
            List<VTableColumn> columns = new ArrayList<>();
            for (int n = 0; n < getSize(vTable,0); n++) {
                columns.add(new VTableColumn(vTable, n));
            }
            columnsTable.setItems(FXCollections.observableList(columns));
        } else {
            tableMetadata.setVisible(false);
            tableMetadata.setManaged(false);
            columnsTable.setItems(new ImmutableObservableList<>());
        }
    }


}