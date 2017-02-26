package org.csstudio.scan.ecrscan.ui.model;

import java.time.Instant;

import org.csstudio.javafx.rtplot.PointType;
import org.csstudio.javafx.rtplot.TraceType;
import org.csstudio.scan.ecrscan.ui.data.ScanValueDataProvider;

import javafx.collections.FXCollections;
import javafx.scene.paint.Color;

public class TraceItem extends AbstractScanTreeItem<AbstractScanTreeItem<?>> {

    protected TraceItem(
            String name,
            Number id, 
            Instant finished,
            Instant created,
            Number percent,
            ScanValueDataProvider scanValueDataProvider,
            String yformula,
            Color color,
            TraceType traceType,
            Integer traceWidth,
            PointType pointType,
            Integer pointSize,
            Integer yaxis) {
        super( name,
                id,
                finished,
                created,
                percent,
                scanValueDataProvider,
                yformula,
                color,
                traceType,
                traceWidth,
                pointType,
                pointSize,
                yaxis,
                FXCollections.emptyObservableList());
    }
    
    public TraceItem(ScanValueDataProvider scanValueDataProvider,String yformula, Color color, TraceType traceType, Integer traceWidth, PointType pointType, Integer pointSize, Integer yaxis) {
        super( "trace",
                null,
                null,
                null,
                null,
                scanValueDataProvider,
                yformula,
                color,
                traceType,
                traceWidth,
                pointType,
                pointSize,
                yaxis,
                FXCollections.emptyObservableList());
    }
    @Override
    public void createAndAddItem(
            String name,
            Number id, 
            Instant finished,
            Instant created,
            Number percent,
            ScanValueDataProvider scanValueDataProvider,
            String yformula,
            Color color,
            TraceType traceType,
            Integer traceWidth,
            PointType pointType,
            Integer pointSize,
            Integer yaxis) {
        throw new UnsupportedOperationException("Trace has no sub items");
    }
    
}
