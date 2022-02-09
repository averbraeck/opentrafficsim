package org.opentrafficsim.road.gtu.lane.tactical;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneDirection;

/**
 * This class provides the following information for an operational plan:
 * <ul>
 * <li>the lanes to follow, with the direction to drive on them</li>
 * <li>the starting point on the first lane</li>
 * <li>the path to follow when staying on the same lane</li>
 * </ul>
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Dec 31, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class LanePathInfo implements Serializable
{
    /** */
    private static final long serialVersionUID = 20151231L;

    /**
     * The path it the GTU keeps driving in the same lane, and follows the route if possible in the same lane. The path stops
     * when the lane or a continuation lane does not lead in the direction of the route provided by the strategical planner.
     */
    private final OTSLine3D path;

    /**
     * The current lane on which the reference point of the GTU is registered (if the GTU is registered on multiple lanes with
     * the reference point, one lane is chosen where the reference point has a fractional lane position between 0.0 and 1.0),
     * and consecutive lanes that follow the route if possible in the same lane. The list of lanes stops when a continuation
     * lane does not lead in the direction of the route provided by the strategical planner. For each lane, the direction to
     * drive is provided.
     */
    private final List<LaneDirection> laneDirectionList;

    /**
     * The start point on the first lane in the laneDirectionList. When this is a point that represents a GTU position, it
     * should represent the reference point of the GTU.
     */
    private final Length referencePosition;

    /**
     * @param path OTSLine3D; the path it the GTU keeps driving in the same lane, and follows the route if possible in the same
     *            lane. The path stops when the lane or a continuation lane does not lead in the direction of the route provided
     *            by the strategical planner.
     * @param laneDirectionList List&lt;LaneDirection&gt;; the current lane on which the reference point of the GTU is
     *            registered (if the GTU is registered on multiple lanes with the reference point, one lane is chosen where the
     *            reference point has a fractional lane position between 0.0 and 1.0), and consecutive lanes that follow the
     *            route if possible in the same lane. The list of lanes stops when a continuation lane does not lead in the
     *            direction of the route provided by the strategical planner. For each lane, the direction to drive is provided.
     * @param referencePosition Length; the start point on the first lane in the laneDirectionList. When this is a point that
     *            represents a GTU position, it should represent the reference point of the GTU.
     */
    public LanePathInfo(final OTSLine3D path, final List<LaneDirection> laneDirectionList, final Length referencePosition)
    {
        this.path = path;
        this.laneDirectionList = laneDirectionList;
        this.referencePosition = referencePosition;
    }

    /**
     * @return path the path it the GTU keeps driving in the same lane, and follows the route if possible in the same lane. The
     *         path stops when the lane or a continuation lane does not lead in the direction of the route provided by the
     *         strategical planner.
     */
    public final OTSLine3D getPath()
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
    public final List<LaneDirection> getLaneDirectionList()
    {
        return this.laneDirectionList;
    }

    /**
     * @return list of lanes
     */
    public final List<Lane> getLanes()
    {
        List<Lane> lanes = new ArrayList<>();
        for (LaneDirection ld : this.laneDirectionList)
        {
            lanes.add(ld.getLane());
        }
        return lanes;
    }

    /**
     * The reference lane is the widest lane on which the reference point of the GTU is fully registered.
     * @return the reference lane on which the GTU is registered, plus the driving direction on this lane, or null if the GTU is
     *         not registered on any lane.
     */
    public final LaneDirection getReferenceLaneDirection()
    {
        return this.laneDirectionList.isEmpty() ? null : this.laneDirectionList.get(0);
    }

    /**
     * The reference lane is the widest lane on which the reference point of the GTU is fully registered.
     * @return the reference lane on which the GTU is registered, or null if the GTU is not registered on any lane.
     */
    public final Lane getReferenceLane()
    {
        return this.laneDirectionList.isEmpty() ? null : this.laneDirectionList.get(0).getLane();
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
        return "LanePathInfo [path=" + this.path + ", laneDirectionList=" + this.laneDirectionList + ", referencePosition="
                + this.referencePosition + "]";
    }

}
