package org.csstudio.scan.ecrscan.ui.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ScanServerItem extends AbstractScanTreeItem<ScanItem> {
    public ScanServerItem(String name) {
        super(  name,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                FXCollections.observableArrayList());
    }
    
    public ScanServerItem(String name, ObservableList<ScanItem> scans) {
        super(  name,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                scans, ScanItem::new);
    }
}
