package org.opentrafficsim.road.gtu.lane.tactical;

import java.io.Serializable;
import java.util.List;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.geometry.OtsLine2d;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * This class provides the following information for an operational plan:
 * <ul>
 * <li>the lanes to follow, with the direction to drive on them</li>
 * <li>the starting point on the first lane</li>
 * <li>the path to follow when staying on the same lane</li>
 * </ul>
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param path OtsLine2d; the path it the GTU keeps driving in the same lane, and follows the route if possible in the same
 *            lane. The path stops when the lane or a continuation lane does not lead in the direction of the route provided by
 *            the strategical planner.
 * @param laneList List&lt;Lane&gt;; the current lane on which the reference point of the GTU is registered (if the GTU is
 *            registered on multiple lanes with the reference point, one lane is chosen where the reference point has a
 *            fractional lane position between 0.0 and 1.0), and consecutive lanes that follow the route if possible in the same
 *            lane. The list of lanes stops when a continuation lane does not lead in the direction of the route provided by the
 *            strategical planner.
 * @param referencePosition Length; the start point on the first lane in the laneList. When this is a point that represents a
 *            GTU position, it should represent the reference point of the GTU.
 */
public record LanePathInfo(OtsLine2d path, List<Lane> laneList, Length referencePosition) implements Serializable
{

    /** */
    private static final long serialVersionUID = 20151231L;

    /**
     * The reference lane is the widest lane on which the reference point of the GTU is fully registered.
     * @return the reference lane on which the GTU is registered, or null if the GTU is not registered on any lane.
     */
    public final Lane getReferenceLane()
    {
        return this.laneList.isEmpty() ? null : this.laneList.get(0);
    }

}
