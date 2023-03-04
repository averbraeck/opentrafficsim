package org.opentrafficsim.road.gtu.lane.tactical;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.geometry.OtsLine3d;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * This class provides the following information for an operational plan:
 * <ul>
 * <li>the lanes to follow, with the direction to drive on them</li>
 * <li>the starting point on the first lane</li>
 * <li>the path to follow when staying on the same lane</li>
 * </ul>
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class LanePathInfo implements Serializable
{
    /** */
    private static final long serialVersionUID = 20151231L;

    /**
     * The path it the GTU keeps driving in the same lane, and follows the route if possible in the same lane. The path stops
     * when the lane or a continuation lane does not lead in the direction of the route provided by the strategical planner.
     */
    private final OtsLine3d path;

    /**
     * The current lane on which the reference point of the GTU is registered (if the GTU is registered on multiple lanes with
     * the reference point, one lane is chosen where the reference point has a fractional lane position between 0.0 and 1.0),
     * and consecutive lanes that follow the route if possible in the same lane. The list of lanes stops when a continuation
     * lane does not lead in the direction of the route provided by the strategical planner.
     */
    private final List<Lane> laneList;

    /**
     * The start point on the first lane in the laneDirectionList. When this is a point that represents a GTU position, it
     * should represent the reference point of the GTU.
     */
    private final Length referencePosition;

    /**
     * @param path OtsLine3d; the path it the GTU keeps driving in the same lane, and follows the route if possible in the same
     *            lane. The path stops when the lane or a continuation lane does not lead in the direction of the route provided
     *            by the strategical planner.
     * @param laneList List&lt;Lane&gt;; the current lane on which the reference point of the GTU is registered (if the GTU is
     *            registered on multiple lanes with the reference point, one lane is chosen where the reference point has a
     *            fractional lane position between 0.0 and 1.0), and consecutive lanes that follow the route if possible in the
     *            same lane. The list of lanes stops when a continuation lane does not lead in the direction of the route
     *            provided by the strategical planner.
     * @param referencePosition Length; the start point on the first lane in the laneList. When this is a point that represents
     *            a GTU position, it should represent the reference point of the GTU.
     */
    public LanePathInfo(final OtsLine3d path, final List<Lane> laneList, final Length referencePosition)
    {
        this.path = path;
        this.laneList = laneList;
        this.referencePosition = referencePosition;
    }

    /**
     * @return path the path it the GTU keeps driving in the same lane, and follows the route if possible in the same lane. The
     *         path stops when the lane or a continuation lane does not lead in the direction of the route provided by the
     *         strategical planner.
     */
    public final OtsLine3d getPath()
    {
        return this.path;
    }

    /**
     * @return laneList the current lane on which the reference point of the GTU is registered (if the GTU is registered on
     *         multiple lanes with the reference point, one lane is chosen where the reference point has a fractional lane
     *         position between 0.0 and 1.0), and consecutive lanes that follow the route if possible in the same lane. The list
     *         of lanes stops when a continuation lane does not lead in the direction of the route provided by the strategical
     *         planner. For each lane, the direction to drive is provided.
     */
    public final List<Lane> getLaneList()
    {
        return this.laneList;
    }

    /**
     * @return list of lanes
     */
    public final List<Lane> getLanes()
    {
        List<Lane> lanes = new ArrayList<>();
        for (Lane lane : this.laneList)
        {
            lanes.add(lane);
        }
        return lanes;
    }

    /**
     * The reference lane is the widest lane on which the reference point of the GTU is fully registered.
     * @return the reference lane on which the GTU is registered, or null if the GTU is not registered on any lane.
     */
    public final Lane getReferenceLane()
    {
        return this.laneList.isEmpty() ? null : this.laneList.get(0);
    }

    /**
     * @return the start point on the first lane in the laneDirectionList. When this is a point that represents a GTU position,
     *         it should represent the reference point of the GTU.
     */
    public final Length getReferencePosition()
    {
        return this.referencePosition;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "LanePathInfo [path=" + this.path + ", laneDirectionList=" + this.laneList + ", referencePosition="
                + this.referencePosition + "]";
    }

}
