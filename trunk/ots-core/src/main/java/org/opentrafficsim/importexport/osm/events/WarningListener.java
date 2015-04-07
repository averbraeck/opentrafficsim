package org.opentrafficsim.importexport.osm.events;

import java.util.EventListener;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 20.03.2015 <br>
 * @author <a>Moritz Bergmann</a>
 */
public interface WarningListener extends EventListener
{
    /**
     * Process one WarningEvent.
     * @param warningEvent WarningEvent; the warning event
     */
    void warning(WarningEvent warningEvent);
}
