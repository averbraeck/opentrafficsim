package org.opentrafficsim.road.gtu.lane.plan.operational;

import java.util.List;

import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.geometry.DirectedPoint;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSLine3D.FractionalFallback;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.LaneDirection;

/**
 * An operational plan with some extra information about the lanes and lane changes so this information does not have to be
 * recalculated multiple times. Furthermore, it is quite expensive to check whether a lane change is part of the oprtational
 * plan based on geographical data.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class LaneBasedOperationalPlan extends OperationalPlan
{
    /** */
    private static final long serialVersionUID = 20160120L;

    /** Deviative; meaning not along lane center lines. */
    private final boolean deviative;

    /**
     * Construct an operational plan with or without a lane change.
     * @param gtu LaneBasedGTU; the GTU for debugging purposes
     * @param path OTSLine3D; the path to follow from a certain time till a certain time. The path should have &lt;i&gt;at
     *            least&lt;/i&gt; the length
     * @param startTime Time; the absolute start time when we start executing the path
     * @param startSpeed Speed; the GTU speed when we start executing the path
     * @param operationalPlanSegmentList List&lt;Segment&gt;; the segments that make up the path with an acceleration, constant
     *            speed or deceleration profile
     * @param deviative boolean; whether the path is not along lane center lines
     * @throws OperationalPlanException when the path is too short for the operation
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public LaneBasedOperationalPlan(final LaneBasedGTU gtu, final OTSLine3D path, final Time startTime, final Speed startSpeed,
            final List<Segment> operationalPlanSegmentList, final boolean deviative) throws OperationalPlanException
    {
        super(gtu, path, startTime, startSpeed, operationalPlanSegmentList);
        this.deviative = deviative;
    }

    /**
     * Build a plan where the GTU will wait for a certain time. Of course no lane change takes place.
     * @param gtu LaneBasedGTU; the GTU for debugging purposes
     * @param waitPoint DirectedPoint; the point at which the GTU will wait
     * @param startTime Time; the current time or a time in the future when the plan should start
     * @param duration Duration; the waiting time
     * @param deviative boolean; whether the path is not along lane center lines
     * @throws OperationalPlanException when construction of a waiting path fails
     */
    public LaneBasedOperationalPlan(final LaneBasedGTU gtu, final DirectedPoint waitPoint, final Time startTime,
            final Duration duration, final boolean deviative) throws OperationalPlanException
    {
        super(gtu, waitPoint, startTime, duration);
        this.deviative = deviative;
    }

    /**
     * Check if we deviate from the center line.
     * @return whether this maneuver involves deviation from the center line.
     */
    public final boolean isDeviative()
    {
        return this.deviative;
    }

    /**
     * Returns the total length along the reference lane that the GTU travels. In case of a deviative plan this involves
     * projection of the actual path to the lane center lines.
     * @param gtu LaneBasedGTU; GTU
     * @return Length; total length along the path
     * @throws GTUException if the GTU has not reference position
     */
    public final Length getTotalLengthAlongLane(final LaneBasedGTU gtu) throws GTUException
    {
        if (!this.deviative)
        {
            // along the lane center lines
            return getTotalLength();
        }

        // let's project the end position of the plan
        return getDistanceAlongLane(gtu, getEndLocation());
    }

    /**
     * Helper method to get rotation at start or end of lane.
     * @param lane LaneDirection; lane
     * @param start boolean; start (or end)
     * @return rotation at start or end of lane
     */
    private double getRotZAtFraction(final LaneDirection lane, final boolean start)
    {
        double f = start ? 0.0 : 1.0;
        try
        {
            return (lane.getDirection().isPlus() ? lane.getLane().getCenterLine().getLocationFraction(f)
                    : lane.getLane().getCenterLine().getLocationFraction(1.0 - f)).getRotZ();
        }
        catch (OTSGeometryException exception)
        {
            // should not occur, we use 0.0 and 1.0
            throw new RuntimeException("Unexpected exception while assessing if a GTU is between lanes.", exception);
        }
    }

    /**
     * Returns the distance along the reference lane that the GTU travels from the current location up to the point.
     * @param gtu LaneBasedGTU; GTU
     * @param point DirectedPoint; point where the GTU is or will be
     * @return Length; total length along the path
     * @throws GTUException if the GTU has not reference position
     */
    public final Length getDistanceAlongLane(final LaneBasedGTU gtu, final DirectedPoint point) throws GTUException
    {

        // start lane center lines at current reference lane
        DirectedLanePosition pos = gtu.getReferencePosition();
        LaneDirection lane = pos.getLaneDirection();

        // initialize loop data
        double length = -lane.coveredDistance(pos.getPosition().si / pos.getLane().getLength().si).si;
        double f = Double.NaN;
        Direction prevDir = Direction.instantiateSI(getRotZAtFraction(lane, true));

        // move to next lane while projection fails
        while (Double.isNaN(f))
        {
            LaneDirection nextLane = lane.getNextLaneDirection(gtu);
            Direction nextDir = Direction.instantiateSI(nextLane == null ? getRotZAtFraction(lane, false)
                    : .5 * getRotZAtFraction(lane, false) + .5 * getRotZAtFraction(nextLane, true));
            f = lane.getLane().getCenterLine().projectFractional(prevDir, nextDir, point.x, point.y, FractionalFallback.NaN);

            // check if the GTU is adjacent to the bit between the lanes (if there is such a bit)
            if (Double.isNaN(f))
            {
                if (nextLane == null)
                {
                    // projection error on dad-end lane, add the length of the lane
                    f = 1.0;
                    length += lane.coveredDistance(f).si;
                }
                else
                {
                    try
                    {
                        // compose gap line
                        OTSPoint3D last = lane.getDirection().isPlus() ? lane.getLane().getCenterLine().getLast()
                                : lane.getLane().getCenterLine().get(0);
                        OTSPoint3D first = nextLane.getDirection().isPlus() ? nextLane.getLane().getCenterLine().get(0)
                                : nextLane.getLane().getCenterLine().getLast();
                        if (!(last).equals(first))
                        {
                            OTSLine3D gap = new OTSLine3D(last, first);
                            double fGap = gap.projectFractional(null, null, point.x, point.y, FractionalFallback.NaN);
                            if (!Double.isNaN(fGap))
                            {
                                f = (lane.getLength().si + fGap * gap.getLengthSI()) / lane.getLength().si;
                            }
                            else
                            {
                                // gap, but no successful projection, use next lane in next loop, increase length so far
                                length += lane.getLength().si;
                                lane = nextLane;
                                prevDir = nextDir;
                            }
                        }
                        else
                        {
                            // no gap, use next lane in next loop, increase length so far
                            length += lane.getLength().si;
                            lane = nextLane;
                            prevDir = nextDir;
                        }
                    }
                    catch (OTSGeometryException exception)
                    {
                        // should not occur, we use get(0) and getLast()
                        throw new RuntimeException("Unexpected exception while assessing if a GTU is between lanes.",
                                exception);
                    }
                }
            }
            else
            {
                // projection is ok on lane, increase length so far
                length += lane.coveredDistance(f).si;
            }
        }
        // add length on lane where the reference position was projected to (or to it's consecutive gap between lanes)
        return Length.instantiateSI(length);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "LaneBasedOperationalPlan [deviative=" + this.deviative + "]";
    }
}
