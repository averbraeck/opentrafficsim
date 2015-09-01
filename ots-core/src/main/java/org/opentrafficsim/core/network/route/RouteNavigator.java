package org.opentrafficsim.core.network.route;

import java.io.Serializable;

import org.opentrafficsim.core.OTS_SCALAR;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;

/**
 * A RouteNavigator maintains a position on a route.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-16 10:20:53 +0200 (Thu, 16 Jul 2015) $, @version $Revision: 1124 $, by $Author: pknoppers $,
 * initial version Jul 22, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public interface RouteNavigator extends Serializable, OTS_SCALAR
{
    /**
     * @return the last visited node of the route, and null when no nodes have been visited yet.
     * @throws NetworkException when the index is out of bounds (should never happen).
     */
    Node lastVisitedNode() throws NetworkException;

    /**
     * This method does <b>not</b> advance the route pointer.
     * @return the next node of the route to visit, and null when we already reached the destination.
     * @throws NetworkException when the index is out of bounds (should never happen).
     */
    Node nextNodeToVisit() throws NetworkException;

    /**
     * This method <b>does</b> advance the route pointer (if possible).
     * @return the next node of the route to visit, and null when we already reached the destination.
     * @throws NetworkException when the index is out of bounds (should never happen).
     */
    Node visitNextNode() throws NetworkException;
}
