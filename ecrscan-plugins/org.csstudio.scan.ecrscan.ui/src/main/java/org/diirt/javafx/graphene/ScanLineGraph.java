package org.diirt.javafx.graphene;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.input.MouseEvent;

import static org.diirt.datasource.formula.ExpressionLanguage.formula;
import static org.diirt.datasource.formula.ExpressionLanguage.formulaArg;
import static org.diirt.datasource.graphene.ExpressionLanguage.multilineGraphOf;

import java.util.StringJoiner;

import org.diirt.datasource.graphene.Graph2DExpression;
import org.diirt.datasource.graphene.MultilineGraph2DExpression;
import org.diirt.graphene.InterpolationScheme;
import org.diirt.graphene.LineGraph2DRendererUpdate;
import org.diirt.graphene.NumberColorMap;
import org.diirt.graphene.NumberColorMaps;

public class ScanLineGraph extends ScanBaseGraphView< LineGraph2DRendererUpdate > {

    private final Property< InterpolationScheme > interpolationScheme = new SimpleObjectProperty< InterpolationScheme >( this , "interpolationScheme" , InterpolationScheme.LINEAR );
    private final Property< NumberColorMap > valueColorScheme = new SimpleObjectProperty< NumberColorMap >( this , "valueColorScheme" , NumberColorMaps.HSV );
    private final BooleanProperty highlightFocusValue = new SimpleBooleanProperty( this , "highlightFocusValue" , true );
    private final StringProperty xColumn = new SimpleStringProperty( this , "xColumn", null);
    private final StringProperty yColumn = new SimpleStringProperty( this , "yColumn", null);
    private final ConfigurationDialog defaultConfigurationDialog = new ConfigurationDialog();
    private int a;
    
    @Override
    public Graph2DExpression<LineGraph2DRendererUpdate> createExpression(String dataFormula) {
        MultilineGraph2DExpression plot = multilineGraphOf(formula(dataFormula),
                formulaArg(xColumn.getValue()),
                formulaArg(yColumn.getValue()));

        return plot;
    }
    
    @Override
    public void reconnect( String data ) {
        super.reconnect( data );
        if ( graph != null ) {
             graph.update( graph.newUpdate().valueColorScheme(valueColorScheme.getValue())
                    .interpolation(interpolationScheme.getValue())
                    .highlightFocusValue(highlightFocusValue.getValue()) );
        }
    }
    
    @Override
    protected void onMouseMove( MouseEvent e ) {
        if( this.highlightFocusValue.getValue() && graph != null ){
            graph.update(graph.newUpdate().focusPixel( (int)e.getX()));
        }
    }
    
    public ScanLineGraph() {
        this.xColumn.addListener( new ChangeListener< String >() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                ScanLineGraph.super.reconnect();
            }
        });
        
        this.yColumn.addListener( new ChangeListener< String >() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                ScanLineGraph.super.reconnect();
            }
            
        });
        
        this.interpolationScheme.addListener( new ChangeListener< InterpolationScheme >() {
      
            @Override
            public void changed(ObservableValue<? extends InterpolationScheme> observable, InterpolationScheme oldValue, InterpolationScheme newValue) {
                graph.update( graph.newUpdate().interpolation( newValue ) );
            }
        });
        
        this.valueColorScheme.addListener( new ChangeListener< NumberColorMap >() {
            @Override
            public void changed(ObservableValue<? extends NumberColorMap> observable, NumberColorMap oldValue, NumberColorMap newValue) {
                graph.update( graph.newUpdate().valueColorScheme(newValue));
            }   
        });
        
        this.highlightFocusValue.addListener( new ChangeListener< Boolean >() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                graph.update( graph.newUpdate().highlightFocusValue(newValue));
            }   
        });
        
        defaultConfigurationDialog.addInterpolationSchemeListProperty( "Interpolation Scheme" , this.interpolationScheme , new InterpolationScheme[] { InterpolationScheme.NEAREST_NEIGHBOR , InterpolationScheme.LINEAR , InterpolationScheme.CUBIC } );
    }
    
    public void setInterpolationScheme( InterpolationScheme scheme ) {
        this.interpolationScheme.setValue( scheme );
    }
    
    public InterpolationScheme getInterpolationScheme() {
        return this.interpolationScheme.getValue();
    }
    
    public Property< InterpolationScheme > interpolationSchemeProperty() {
        return this.interpolationScheme;
    }
    
    public void setValueColorScheme( NumberColorMap numberColorMap ) {
        this.valueColorScheme.setValue( numberColorMap );
    }
    
    public NumberColorMap getValueColorScheme() {
        return this.valueColorScheme.getValue();
    }
    
    public Property< NumberColorMap > valueColorSchemeProperty() {
        return this.valueColorScheme;
    }
    
    public void setHighlightFocusValue( Boolean highlightFocusValue ) {
        this.highlightFocusValue.setValue( highlightFocusValue );
    }
    
    public Boolean isHighlightFocusValue() {
        return this.highlightFocusValue.getValue();
    }
    
    public BooleanProperty highlightFocusValue() {
        return this.highlightFocusValue;
    }
    
    public void setXColumn(String xColumn) {
        this.xColumn.setValue( xColumn );
    }
    
    public String getXColumn() {
        return xColumn.getValue();
    }
    
    public StringProperty xColumnProperty() {
        return this.xColumn;
    }
    
    public void setYColumn(String yColumn) {
        this.yColumn.setValue( yColumn );
    }
    
    public String getYColumn() {
        return yColumn.getValue();
    }
    
    public StringProperty yColumnProperty() {
        return this.yColumn;
    }
    
    public ConfigurationDialog getDefaultConfigurationDialog() {
        return this.defaultConfigurationDialog;
    }
    
}
