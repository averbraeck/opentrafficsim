package org.opentrafficsim.core.gtu.perception;

import java.io.Serializable;
import java.util.Set;

import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.network.NetworkException;

/**
 * The perception module of a GTU. It is responsible for perceiving (sensing) the environment of the GTU, which includes the
 * locations of other GTUs. Perception is done at a certain time, and the perceived information might have a limited validity.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 15, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public interface Perception extends Serializable
{
    /**
     * Update the perceived information and store it in a stateful manner.
     * @throws GTUException when GTU has not been properly initialized.
     * @throws NetworkException in case of inconsistencies in the network during perception calculations.
     */
    void perceive() throws GTUException, NetworkException;

    /**
     * Return information about the perceived objects with their (estimated) location, speed, and state.
     * @return a set of objects within the perception range and angle
     */
    Set<PerceivedObject> getPerceivedObjects();
}
