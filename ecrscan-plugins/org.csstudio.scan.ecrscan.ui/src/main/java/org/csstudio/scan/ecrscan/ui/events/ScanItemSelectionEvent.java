package org.csstudio.scan.ecrscan.ui.events;

import java.util.List;

import org.csstudio.scan.ecrscan.ui.model.AbstractScanTreeItem;

import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.control.TreeItem;

public class ScanItemSelectionEvent<T> extends Event {
    /**
     * 
     */
    private static final long serialVersionUID = 3908258770467238012L;

    public static final EventType<ScanItemSelectionEvent<?>> TYPE = new EventType<>("SCANDATA_ITEM_SELECTION_CHANGED");
    public enum SELECTION {
        ADDED, REMOVED, SELECTED
    }

    private final List<? extends TreeItem<T>> scanItems;
    private final SELECTION selection;

    public ScanItemSelectionEvent(final List<? extends TreeItem<T>> dataItemsAdded, final SELECTION added) {
        super(TYPE);
        this.scanItems = dataItemsAdded;
        this.selection = added;
    }

    public  List<? extends TreeItem<T>> getScanItems() {
        return scanItems;
    }
    
    public SELECTION getSelectionType() {
        return selection;
    }
    
}
