package org.csstudio.scan.ecrscan.ui.model;

import java.time.Instant;

import org.csstudio.javafx.rtplot.PointType;
import org.csstudio.javafx.rtplot.TraceType;
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
    private final StringProperty status = new SimpleStringProperty();
    
    private final StringProperty yformula = new SimpleStringProperty();
    private final Property< Color > color = new SimpleObjectProperty< Color >( this , "color" );
    private final Property< TraceType > type = new SimpleObjectProperty< TraceType >( this , "type" );
    private final Property< Integer > width = new SimpleObjectProperty< Integer >( this , "width" );
    private final Property< PointType > pointType = new SimpleObjectProperty< PointType >( this , "pointType" );
    private final Property< Integer > pointSize = new SimpleObjectProperty< Integer >( this , "pointSize" );
    private final Property< Integer > yaxis = new SimpleObjectProperty< Integer >( this , "yaxis" );

    private final Function13<String, Number, Instant, Instant, Number, String, String, Color, TraceType, Integer, PointType, Integer, Integer, T> itemFunction;
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
            String status,
            String yformula,
            Color color,
            TraceType traceType,
            Integer traceWidth,
            PointType pointType,
            Integer pointSize,
            Integer yaxis,
            ObservableList<T> items, Function13<String, Number, Instant, Instant, Number, String, String, Color, TraceType, Integer, PointType, Integer, Integer, T> itemFunction) {
        this.items = items;
        this.itemFunction = itemFunction;
        setName(name);
        setId(id);
        setFinished(finished);
        setCreated(created);
        setPercent(percent);
        setStatus(status);
        
        setYformula(yformula);
        setColor(color);
        setType(traceType);
        setWidth(traceWidth);
        setPointType(pointType);
        setPointSize(pointSize);
        setYAxis(yaxis);
    }
    

    public AbstractScanTreeItem(
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
            Integer yaxis,
            ObservableList<T> items) {
        this(
                name,
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
                items, (one, two, three, four, five, six, seven, eight, nine, ten, eleven, tweleve, thirteen) -> null);
    }
    

    public AbstractScanTreeItem(
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
            Integer yaxis,
            Function13<String, Number, Instant, Instant, Number, String, String, Color, TraceType, Integer, PointType, Integer, Integer, T>  itemFunction) {
        this(
                name,
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
                FXCollections.observableArrayList(), itemFunction);
    }
    

    public AbstractScanTreeItem(
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
        this(
                name,
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
            String status,
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
                status,
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
    
    

    public final StringProperty yformulaProperty() {
        return this.yformula;
    }
    

	public final StringProperty statusProperty() {
		return this.status;
	}
	

	public final java.lang.String getStatus() {
		return this.statusProperty().get();
	}
	


	public final void setStatus(final java.lang.String status) {
		this.statusProperty().set(status);
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
    


    public final Property<TraceType> typeProperty() {
        return this.type;
    }
    


    public final org.csstudio.javafx.rtplot.TraceType getType() {
        return this.typeProperty().getValue();
    }
    


    public final void setType(final org.csstudio.javafx.rtplot.TraceType traceType) {
        this.typeProperty().setValue(traceType);
    }
    


    public final Property<Integer> widthProperty() {
        return this.width;
    }
    


    public final Integer getWidth() {
        return this.widthProperty().getValue();
    }
    


    public final void setWidth(final Integer traceWidth) {
        this.widthProperty().setValue(traceWidth);
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
    


    public final Integer getPointSize() {
        return this.pointSizeProperty().getValue();
    }
    


    public final void setPointSize(final Integer pointSize) {
        this.pointSizeProperty().setValue(pointSize);
    }
    


    public final Property<Integer> yaxisProperty() {
        return this.yaxis;
    }
    


    public final Integer getYAxis() {
        return this.yaxisProperty().getValue();
    }
    


    public final void setYAxis(final Integer yaxis) {
        this.yaxisProperty().setValue(yaxis);
    }
}
