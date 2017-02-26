package org.csstudio.scan.ecrscan.ui.events;

import java.util.List;

import javafx.event.Event;
import javafx.event.EventType;

public class DataItemSelectionEvent extends Event {
    /**
     * 
     */
    private static final long serialVersionUID = 3908258770467238012L;

    public static final EventType<DataItemSelectionEvent> TYPE = new EventType<>("DATA_ITEM_SELECTION_CHANGED");
    public enum SELECTION {
        ADDED, REMOVED, SELECTED
    }

    private final int dataSeriesIndex;
    private final List<Number> ids;
    private final SELECTION selection;

    public DataItemSelectionEvent(final int dataSeriesIndex, final List<Number> ids, final SELECTION selection) {
        super(TYPE);
        this.dataSeriesIndex = dataSeriesIndex;
        this.ids = ids;
        this.selection = selection;
    }

    public int getDataSeriesIndex() {
        return dataSeriesIndex;
    }
    
    public List<Number> getIds() {
        return ids;
    }
    
    public SELECTION getSelectionType() {
        return selection;
    }
    
}
