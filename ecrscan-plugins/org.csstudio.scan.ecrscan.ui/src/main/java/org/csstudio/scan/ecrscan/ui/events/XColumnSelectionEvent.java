package org.csstudio.scan.ecrscan.ui.events;

import java.util.List;

import javafx.event.Event;
import javafx.event.EventType;

public class XColumnSelectionEvent extends Event {
    /**
     * 
     */
    private static final long serialVersionUID = -6385721548100370106L;


    public static final EventType<XColumnSelectionEvent> TYPE = new EventType<>("X_ITEM_SELECTION_CHANGED");

    private final String event;

    public XColumnSelectionEvent(final String event) {
        super(TYPE);
        this.event = event;
    }
    
    public String getEvent() {
        return event;
    }
    
}
