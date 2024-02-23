package org.opentrafficsim.draw.egtf;

import java.util.EventListener;

/**
 * Interface for EGTF listeners.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public interface EgtfListener extends EventListener
{

    /**
     * Notifies progress.
     * @param event EgtfEvent; event
     */
    void notifyProgress(EgtfEvent event);

}
