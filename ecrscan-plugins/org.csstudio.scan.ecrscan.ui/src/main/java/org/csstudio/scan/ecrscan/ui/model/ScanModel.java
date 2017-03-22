package org.csstudio.scan.ecrscan.ui.model;

import java.util.HashMap;
import java.util.Map;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

public class ScanModel<T> {
    private final StringProperty scanServer = new SimpleStringProperty( this , "", null);
    private final StringProperty xformula = new SimpleStringProperty( this , "", null);
    private final ObjectProperty<T> selectedScan = new SimpleObjectProperty<>(null);
    private final ObservableMap<String, ObservableList<TraceItem>> dataStore = FXCollections.observableHashMap();
    
	public final StringProperty scanServerProperty() {
		return this.scanServer;
	}
	
	public final java.lang.String getScanServer() {
		return this.scanServerProperty().get();
	}
	
	public final void setScanServer(final java.lang.String scanServer) {
		this.scanServerProperty().set(scanServer);
	}
	
	public final StringProperty xformulaProperty() {
		return this.xformula;
	}
	
	public final java.lang.String getXformula() {
		return this.xformulaProperty().get();
	}
	
	public final void setXformula(final java.lang.String xformula) {
		this.xformulaProperty().set(xformula);
	}
	
	public final ObjectProperty<T> selectedScanProperty() {
		return this.selectedScan;
	}
	
	public final T getSelectedScan() {
		return this.selectedScanProperty().get();
	}
	
	public final void setSelectedScan(final T selectedScan) {
		this.selectedScanProperty().set(selectedScan);
	}

	public ObservableMap<String, ObservableList<TraceItem>> getDataStore() {
		return dataStore;
	}
}
