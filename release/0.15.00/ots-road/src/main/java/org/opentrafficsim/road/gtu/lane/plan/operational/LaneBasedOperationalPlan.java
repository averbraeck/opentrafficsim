package org.opentrafficsim.road.gtu.lane.plan.operational;

import java.util.ArrayList;
import java.util.List;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.network.lane.Lane;

import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * An operational plan with some extra information about the lanes and lane changes so this information does not have to be
 * recalculated multiple times. Furthermore, it is quite expensive to check whether a lane change is part of the oprtational
 * plan based on geographical data.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jan 20, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class LaneBasedOperationalPlan extends OperationalPlan
{
    /** */
    private static final long serialVersionUID = 20160120L;

    /** The list of lanes that are part of this plan; these will be deregistered in case of lane change. */
    private final List<Lane> referenceLaneList;

    /** The list of new lanes to which the GTU is driving, parallel to the referenceLaneList. */
    private final List<Lane> secondLaneList;

    private final int lastLaneIndex;

    private final double lastFractionalPosition;

    /**
     * Construct an operational plan without a lane change.
     * @param gtu the GTU for debugging purposes
     * @param path the path to follow from a certain time till a certain time. The path should have <i>at least</i> the length
     * @param startTime the absolute start time when we start executing the path
     * @param startSpeed the GTU speed when we start executing the path
     * @param operationalPlanSegmentList the segments that make up the path with an acceleration, constant speed or deceleration
     *            profile
     * @param referenceLaneList the list of lanes that are part of this plan
     * @throws OperationalPlanException when the path is too short for the operation
     */
    public LaneBasedOperationalPlan(final LaneBasedGTU gtu, final OTSLine3D path, final Time startTime, final Speed startSpeed,
            final List<Segment> operationalPlanSegmentList, final List<Lane> referenceLaneList) throws OperationalPlanException
    {
        super(gtu, path, startTime, startSpeed, operationalPlanSegmentList);
        this.referenceLaneList = referenceLaneList;
        this.secondLaneList = null;
        this.lastLaneIndex = 0;
        this.lastFractionalPosition = 0;
    }

    /**
     * Construct an operational plan with a lane change.
     * @param gtu the GTU for debugging purposes
     * @param path the path to follow from a certain time till a certain time. The path should have <i>at least</i> the length
     * @param startTime the absolute start time when we start executing the path
     * @param startSpeed the GTU speed when we start executing the path
     * @param operationalPlanSegmentList the segments that make up the path with an acceleration, constant speed or deceleration
     *            profile
     * @param fromLaneList the list of lanes that the gtu comes from
     * @param toLaneList the list of lanes that the gtu goes towards
     * @param lastLaneIndex index in lane arrays of last position in plan
     * @param lastFractionalPosition fractional position of last postition in plan
     * @throws OperationalPlanException when the path is too short for the operation
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public LaneBasedOperationalPlan(final LaneBasedGTU gtu, final OTSLine3D path, final Time startTime, final Speed startSpeed,
            final List<Segment> operationalPlanSegmentList, final List<Lane> fromLaneList, final List<Lane> toLaneList,
            final int lastLaneIndex, final double lastFractionalPosition) throws OperationalPlanException
    {
        super(gtu, path, startTime, startSpeed, operationalPlanSegmentList);
        this.referenceLaneList = fromLaneList;
        this.secondLaneList = toLaneList;
        this.lastLaneIndex = lastLaneIndex;
        this.lastFractionalPosition = lastFractionalPosition;
    }

    /**
     * Build a plan where the GTU will wait for a certain time. Of course no lane change takes place.
     * @param gtu the GTU for debugging purposes
     * @param waitPoint the point at which the GTU will wait
     * @param startTime the current time or a time in the future when the plan should start
     * @param duration the waiting time
     * @param referenceLane the reference lane where the halting takes place
     * @throws OperationalPlanException when construction of a waiting path fails
     */
    public LaneBasedOperationalPlan(final LaneBasedGTU gtu, final DirectedPoint waitPoint, final Time startTime,
            final Duration duration, final Lane referenceLane) throws OperationalPlanException
    {
        super(gtu, waitPoint, startTime, duration);
        this.referenceLaneList = new ArrayList<>();
        this.referenceLaneList.add(referenceLane);
        this.secondLaneList = null;
        this.lastLaneIndex = 0;
        this.lastFractionalPosition = 0;
    }

    /**
     * Check if we deviate from the center line.
     * @return whether this maneuver involves deviation from the center line.
     */
    public final boolean isDeviative()
    {
        return this.secondLaneList != null;
    }

    /**
     * @return referenceLane
     */
    public final Lane getReferenceLane()
    {
        return this.referenceLaneList.get(0);
    }

    /**
     * @return referenceLaneList
     */
    public final List<Lane> getReferenceLaneList()
    {
        return this.referenceLaneList;
    }

    /**
     * @return secondLaneList
     */
    public final List<Lane> getSecondLaneList()
    {
        return this.secondLaneList;
    }

    /**
     * @return lastLaneIndex.
     */
    public final int getLastLaneIndex()
    {
        return this.lastLaneIndex;
    }

    /**
     * @return lastFractionalPosition.
     */
    public final double getLastFractionalPosition()
    {
        return this.lastFractionalPosition;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "LaneBasedOperationalPlan [referenceLaneList=" + this.referenceLaneList + ", secondLaneList="
                + this.secondLaneList + "]";
    }
}