package org.opentrafficsim.core.egtf;

import java.util.EventListener;

/**
 * Interface for EGTF listeners.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 8 okt. 2018 <br>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface EgtfListener extends EventListener
{

    /**
     * Notifies progress.
     * @param event EgtfEvent; event
     */
    void notifyProgress(EgtfEvent event);

}
