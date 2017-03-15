package org.csstudio.scan.ecrscan.ui.model;

import java.time.Instant;
import java.util.List;

import org.csstudio.javafx.rtplot.PointType;
import org.csstudio.javafx.rtplot.TraceType;
import org.diirt.util.array.ListNumber;
import org.diirt.vtype.VTable;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

public class ScanItem extends AbstractScanTreeItem<TraceItem>{

    protected ScanItem(
            String name,
            Number id, 
            Instant finished,
            Instant created,
            Number percent,
            String status,
            String yformula,
            Color color,
            TraceType traceType,
            Integer traceWidth,
            PointType pointType,
            Integer pointSize,
            Integer yaxis) {
        super(  name,
                id,
                finished,
                created,
                percent,
                status,
                yformula,
                color,
                traceType,
                traceWidth,
                pointType,
                pointSize,
                yaxis,
                FXCollections.emptyObservableList());
    }
    
    public ScanItem(VTable vTable, int columnIndex, ObservableList<TraceItem> traces) {
        super(
                "scan",
                getId(vTable,columnIndex),
                getFinished(vTable,columnIndex),
                getCreated(vTable,columnIndex),
                getPercent(vTable,columnIndex),
                getStatus(vTable,columnIndex),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                traces, TraceItem::new);
    }
    
    public ScanItem(VTable vTable, int columnIndex) {
        this(vTable, columnIndex, FXCollections.observableArrayList());
    }
    
    private static Number getId(VTable vTable, int columnIndex) {
        Object data = vTable.getColumnData(0);
        if (data instanceof ListNumber) {
            return ((ListNumber) data).getInt(columnIndex);
        } else if (data instanceof List) {
            return (Number)((List<?>) data).get(columnIndex);
        } else {
            return 0;
        }
    }
    
    private static Instant getFinished(VTable vTable, int columnIndex) {
        Object data = vTable.getColumnData(4);
        if (data instanceof List) {
            return (Instant)((List<?>) data).get(columnIndex);
        } else {
            return Instant.MIN;
        }
    }
    
    private static Instant getCreated(VTable vTable, int columnIndex) {
        Object data = vTable.getColumnData(1);
        if (data instanceof List) {
            return (Instant)((List<?>) data).get(columnIndex);
        } else {
            return Instant.MIN;
        }
    }
    
    private static Number getPercent(VTable vTable, int columnIndex) {
        Object data = vTable.getColumnData(5);
        if (data instanceof ListNumber) {
            return ((ListNumber) data).getInt(columnIndex);
        } else if (data instanceof List) {
            return (Number)((List<?>) data).get(columnIndex);
        } else {
            return 0;
        }
    }
    
    private static String getStatus(VTable vTable, int columnIndex) {
        Object data = vTable.getColumnData(6);
        if (data instanceof List) {
            return (String)((List<?>) data).get(columnIndex);
        } else {
            return "";
        }
    }
}
