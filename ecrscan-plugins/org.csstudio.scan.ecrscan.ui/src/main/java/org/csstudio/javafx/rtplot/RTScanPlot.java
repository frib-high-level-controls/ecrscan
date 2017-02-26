package org.csstudio.javafx.rtplot;

import org.csstudio.javafx.rtplot.RTValuePlot;

public class RTScanPlot extends RTValuePlot {

    public RTScanPlot(boolean active) {
        super(active);
    }

    // Made this class, because I'm not sure how to pass an argument constructor in FXML
    // Also, it seems the RTValuePlot has a dependency on display.builder.utils
    public RTScanPlot() {
        this(true);
    }

}
