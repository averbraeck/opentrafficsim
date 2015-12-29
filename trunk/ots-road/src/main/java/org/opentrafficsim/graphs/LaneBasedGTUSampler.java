package org.opentrafficsim.graphs;

import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.AbstractLaneBasedGTU;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * Interface implemented by graphs that sample movements of a lane-based GTU.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, @version $Revision: 1401 $, by $Author: averbraeck $,
 * initial version 5 jan. 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public interface LaneBasedGTUSampler
{
    /**
     * Add the movement of an AbstractLaneBasedGTU to a graph.
     * @param gtu AbstractLaneBasedGTU; the AbstractLaneBasedGTU
     * @param lane Lane; the Lane for which the movement must be added
     * @throws GTUException on problems obtaining the data from the GTU
     * @throws NetworkException on network-related inconsistency
     */
    void addData(AbstractLaneBasedGTU gtu, Lane lane) throws GTUException, NetworkException;

    /**
     * Force redraw of the graph.
     */
    void reGraph();
}
