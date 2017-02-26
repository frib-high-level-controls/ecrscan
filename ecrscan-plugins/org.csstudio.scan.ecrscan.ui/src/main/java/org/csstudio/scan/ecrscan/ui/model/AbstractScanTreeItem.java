package org.csstudio.scan.ecrscan.ui.model;

import java.time.Instant;

import org.csstudio.javafx.rtplot.PointType;
import org.csstudio.javafx.rtplot.TraceType;
import org.csstudio.scan.ecrscan.ui.data.ScanValueDataProvider;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

/**
 * @author berryman
 *
 * @param <T>
 */
public abstract class AbstractScanTreeItem<T extends AbstractScanTreeItem<?>> {

    private final StringProperty name = new SimpleStringProperty();
    private final Property< Number > id = new SimpleObjectProperty< Number >( this , "id" );
    private final Property< Instant > finished = new SimpleObjectProperty< Instant >( this , "finished" );
    private final Property< Instant > created = new SimpleObjectProperty< Instant >( this , "created" );
    private final Property< Number > percent = new SimpleObjectProperty< Number >( this , "percent" );
    
    private final Property< ScanValueDataProvider> scanValueDataProvider = new SimpleObjectProperty< ScanValueDataProvider >( this, "scanValueDataProvider" );
    private final StringProperty yformula = new SimpleStringProperty();
    private final Property< Color > color = new SimpleObjectProperty< Color >( this , "color" );
    private final Property< TraceType > traceType = new SimpleObjectProperty< TraceType >( this , "traceType" );
    private final Property< Integer > traceWidth = new SimpleObjectProperty< Integer >( this , "traceWidth" );
    private final Property< PointType > pointType = new SimpleObjectProperty< PointType >( this , "pointType" );
    private final Property< Integer > pointSize = new SimpleObjectProperty< Integer >( this , "pointSize" );
    private final Property< Integer > yaxis = new SimpleObjectProperty< Integer >( this , "yaxis" );

    private final Function13<String, Number, Instant, Instant, Number, ScanValueDataProvider, String, Color, TraceType, Integer, PointType, Integer, Integer, T> itemFunction;
    private final ObservableList<T> items;
    
    @FunctionalInterface
    interface Function13<One, Two, Three, Four, Five, Six, Seven, Eight, Nine, Ten, Eleven, Twelve, Thirteen, Fourteen> {
        public Fourteen apply(One one, Two two, Three three, Four four, Five five, Six six, Seven seven, Eight eight, Nine nine, Ten ten, Eleven eleven, Twelve twelve, Thirteen thirteen);
    }
    

    public AbstractScanTreeItem(
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
            Integer yaxis,
            ObservableList<T> items, Function13<String, Number, Instant, Instant, Number, ScanValueDataProvider, String, Color, TraceType, Integer, PointType, Integer, Integer, T> itemFunction) {
        this.items = items;
        this.itemFunction = itemFunction;
        setName(name);
        setId(id);
        setFinished(finished);
        setCreated(created);
        setPercent(percent);
        
        setScanValueDataProvider(scanValueDataProvider);
        setYformula(yformula);
        setColor(color);
        setTraceType(traceType);
        setTraceWidth(traceWidth);
        setPointType(pointType);
        setPointSize(pointSize);
        setYaxis(yaxis);
    }
    

    public AbstractScanTreeItem(
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
            Integer yaxis,
            ObservableList<T> items) {
        this(
                name,
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
                items, (one, two, three, four, five, six, seven, eight, nine, ten, eleven, tweleve, thirteen) -> null);
    }
    

    public AbstractScanTreeItem(
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
            Integer yaxis,
            Function13<String, Number, Instant, Instant, Number, ScanValueDataProvider, String, Color, TraceType, Integer, PointType, Integer, Integer, T>  itemFunction) {
        this(
                name,
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
                FXCollections.observableArrayList(), itemFunction);
    }
    

    public AbstractScanTreeItem(
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
        this(
                name,
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
                FXCollections.observableArrayList(), (one, two, three, four, five, six, seven, eight, nine, ten, eleven, tweleve, thirteen) -> null);
    }
    


    
    public ObservableList<T> getItems() {
        return items;
    }
    
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
        getItems().add(itemFunction.apply(
                name,
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
                yaxis));
    }


    public final StringProperty nameProperty() {
        return this.name;
    }
    


    public final java.lang.String getName() {
        return this.nameProperty().get();
    }
    


    public final void setName(final java.lang.String name) {
        this.nameProperty().set(name);
    }
    


    public final Property<Number> idProperty() {
        return this.id;
    }
    


    public final java.lang.Number getId() {
        return this.idProperty().getValue();
    }
    


    public final void setId(final java.lang.Number id) {
        this.idProperty().setValue(id);
    }
    


    public final Property<Instant> finishedProperty() {
        return this.finished;
    }
    


    public final java.time.Instant getFinished() {
        return this.finishedProperty().getValue();
    }
    


    public final void setFinished(final java.time.Instant finished) {
        this.finishedProperty().setValue(finished);
    }
    


    public final Property<Instant> createdProperty() {
        return this.created;
    }
    


    public final java.time.Instant getCreated() {
        return this.createdProperty().getValue();
    }
    


    public final void setCreated(final java.time.Instant created) {
        this.createdProperty().setValue(created);
    }
    


    public final Property<Number> percentProperty() {
        return this.percent;
    }
    


    public final java.lang.Number getPercent() {
        return this.percentProperty().getValue();
    }
    


    public final void setPercent(final java.lang.Number percent) {
        this.percentProperty().setValue(percent);
    }
    


    public final Property<ScanValueDataProvider> scanValueDataProviderProperty() {
        return this.scanValueDataProvider;
    }
    


    public final org.csstudio.scan.ecrscan.ui.data.ScanValueDataProvider getScanValueDataProvider() {
        return this.scanValueDataProviderProperty().getValue();
    }
    


    public final void setScanValueDataProvider(
            final org.csstudio.scan.ecrscan.ui.data.ScanValueDataProvider scanValueDataProvider) {
        this.scanValueDataProviderProperty().setValue(scanValueDataProvider);
    }
    


    public final StringProperty yformulaProperty() {
        return this.yformula;
    }
    


    public final java.lang.String getYformula() {
        return this.yformulaProperty().get();
    }
    


    public final void setYformula(final java.lang.String yformula) {
        this.yformulaProperty().set(yformula);
    }
    


    public final Property<Color> colorProperty() {
        return this.color;
    }
    


    public final javafx.scene.paint.Color getColor() {
        return this.colorProperty().getValue();
    }
    


    public final void setColor(final javafx.scene.paint.Color color) {
        this.colorProperty().setValue(color);
    }
    


    public final Property<TraceType> traceTypeProperty() {
        return this.traceType;
    }
    


    public final org.csstudio.javafx.rtplot.TraceType getTraceType() {
        return this.traceTypeProperty().getValue();
    }
    


    public final void setTraceType(final org.csstudio.javafx.rtplot.TraceType traceType) {
        this.traceTypeProperty().setValue(traceType);
    }
    


    public final Property<Integer> traceWidthProperty() {
        return this.traceWidth;
    }
    


    public final java.lang.Integer getTraceWidth() {
        return this.traceWidthProperty().getValue();
    }
    


    public final void setTraceWidth(final java.lang.Integer traceWidth) {
        this.traceWidthProperty().setValue(traceWidth);
    }
    


    public final Property<PointType> pointTypeProperty() {
        return this.pointType;
    }
    


    public final org.csstudio.javafx.rtplot.PointType getPointType() {
        return this.pointTypeProperty().getValue();
    }
    


    public final void setPointType(final org.csstudio.javafx.rtplot.PointType pointType) {
        this.pointTypeProperty().setValue(pointType);
    }
    


    public final Property<Integer> pointSizeProperty() {
        return this.pointSize;
    }
    


    public final java.lang.Integer getPointSize() {
        return this.pointSizeProperty().getValue();
    }
    


    public final void setPointSize(final java.lang.Integer pointSize) {
        this.pointSizeProperty().setValue(pointSize);
    }
    


    public final Property<Integer> yaxisProperty() {
        return this.yaxis;
    }
    


    public final java.lang.Integer getYaxis() {
        return this.yaxisProperty().getValue();
    }
    


    public final void setYaxis(final java.lang.Integer yaxis) {
        this.yaxisProperty().setValue(yaxis);
    }
    



    
}
