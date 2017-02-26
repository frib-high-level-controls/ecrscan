package org.csstudio.scan.ecrscan.ui.data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.csstudio.scan.ecrscan.ui.events.PVEvent;
import org.csstudio.scan.ecrscan.ui.events.ReadEvent;
import org.diirt.datasource.PVReaderEvent;
import org.diirt.datasource.PVReaderListener;

public class ScanData {
    private final Runnable callback;
    private final List<PVEvent> events = Collections.synchronizedList(new ArrayList<PVEvent>());

    public ScanData(Runnable callback) {
        this.callback = callback;
    }
    
    public <T> PVReaderListener<T> createReadListener() {
        return new PVReaderListener<T>() {

            @Override
            public void pvChanged(PVReaderEvent<T> event) {
                events.add(new ReadEvent(Instant.now(), event.getPvReader().getName(), event, event.getPvReader().isConnected(), event.getPvReader().getValue(), event.getPvReader().lastException()));
                callback.run();
            }
        };
    }
    
    public List<PVEvent> getEvents() {
        return events;
    }
}
