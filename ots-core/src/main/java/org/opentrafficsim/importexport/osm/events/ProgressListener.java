package org.opentrafficsim.importexport.osm.events;

import java.util.EventListener;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author: pknoppers
 * $, initial version20.03.2015 <br>
 * @author <a>Moritz Bergmann</a>
 */
public interface ProgressListener extends EventListener
{
    /**
     * Process one ProgressEvent.
     * @param progressEvent ProgressEvent; the event
     */
    void progress(ProgressEvent progressEvent);
}
