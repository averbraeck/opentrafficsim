package org.opentrafficsim.graphs;

import org.opentrafficsim.core.gtu.lane.AbstractLaneBasedGTU;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.lane.Lane;

/**
 * Interface implemented by graphs that sample movements of a lane-based GTU.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version 5 jan. 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public interface LaneBasedGTUSampler
{
    /**
     * Add the movement of an AbstractLaneBasedGTU to a graph.
     * @param gtu AbstractLaneBasedGTU; the AbstractLaneBasedGTU
     * @param lane Lane; the Lane for which the movement must be added
     * @throws NetworkException on network-related inconsistency
     */
    void addData(AbstractLaneBasedGTU gtu, Lane lane) throws NetworkException;

    /**
     * Force redraw of the graph.
     */
    void reGraph();
}
