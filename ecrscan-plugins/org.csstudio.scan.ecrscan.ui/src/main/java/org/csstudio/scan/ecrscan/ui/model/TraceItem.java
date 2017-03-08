package org.csstudio.scan.ecrscan.ui.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import org.csstudio.javafx.rtplot.PointType;
import org.csstudio.javafx.rtplot.TraceType;
import org.csstudio.javafx.rtplot.data.PlotDataItem;
import org.csstudio.javafx.rtplot.data.PlotDataProvider;
import org.csstudio.scan.ecrscan.ui.data.ScanValueDataProvider;

import javafx.collections.FXCollections;
import javafx.scene.paint.Color;

//implementing Trace<Double> doesn't mean it becomes a handle for traces
//public class TraceItem extends AbstractScanTreeItem<AbstractScanTreeItem<?>> implements Trace<Double> {
public class TraceItem extends AbstractScanTreeItem<AbstractScanTreeItem<?>> {
    private volatile Optional<PlotDataItem<Double>> selected_sample = Optional.empty();
    private volatile String units;
    final private ScanValueDataProvider data;
    
    protected TraceItem(
            String name,
            Number id, 
            Instant finished,
            Instant created,
            Number percent,
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
                yformula,
                color,
                traceType,
                traceWidth,
                pointType,
                pointSize,
                yaxis,
                FXCollections.emptyObservableList());
        this.data = new ScanValueDataProvider("0",yformula);
        this.units = units == null ? "" : units;
    }
    
    public TraceItem(final ScanValueDataProvider data, String yformula, Color color, TraceType traceType, Integer traceWidth, PointType pointType, Integer pointSize, Integer yaxis) {
        super( "trace",
                null,
                null,
                null,
                null,
                yformula,
                color,
                traceType,
                traceWidth,
                pointType,
                pointSize,
                yaxis,
                FXCollections.emptyObservableList());
        this.data = Objects.requireNonNull(data);
        this.units = units == null ? "" : units;
    }
    @Override
    public void createAndAddItem(
            String name,
            Number id, 
            Instant finished,
            Instant created,
            Number percent,
            String yformula,
            Color color,
            TraceType traceType,
            Integer traceWidth,
            PointType pointType,
            Integer pointSize,
            Integer yaxis) {
        throw new UnsupportedOperationException("Trace has no sub items");
    }
    
    public Optional<PlotDataItem<Double>> getSelectedSample()
    {
        return selected_sample;
    }
    
    public String getUnits()
    {
        return units;
    }

    public void setUnits(final String units)
    {
        this.units = units == null ? "" : units;
    }
    
    public PlotDataProvider<Double> getData()
    {
        return data;
    }
    
}
