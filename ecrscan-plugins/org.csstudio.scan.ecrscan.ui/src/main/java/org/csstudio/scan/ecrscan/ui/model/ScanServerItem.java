package org.csstudio.scan.ecrscan.ui.model;

import java.time.Instant;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

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
