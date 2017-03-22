package org.csstudio.scan.ecrscan.ui.data;

import org.csstudio.javafx.rtplot.data.PlotDataProvider;

public abstract interface AbstractValueDataProvider extends PlotDataProvider<Double> {

	public void close();
}
