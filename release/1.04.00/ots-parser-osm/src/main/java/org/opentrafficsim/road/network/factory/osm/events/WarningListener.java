package org.opentrafficsim.road.network.factory.osm.events;

import java.util.EventListener;

/**
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-26 01:01:13 +0200 (Sun, 26 Jul 2015) $, @version $Revision: 1155 $, by $Author: averbraeck $,
 * initial version 20.03.2015 <br>
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
