package org.csstudio.scan.ecrscan.ui.events;

import java.time.Instant;

/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */



/**
 *
 * @author carcassi
 */
public interface PVEvent {
    
    public Instant getTimestamp();
    
    public String getPvName();
    
    public abstract Object getEvent();
}
