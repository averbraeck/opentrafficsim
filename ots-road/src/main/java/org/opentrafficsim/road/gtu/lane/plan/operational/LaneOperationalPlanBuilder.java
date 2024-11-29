package org.opentrafficsim.road.gtu.lane.plan.operational;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.draw.point.OrientedPoint2d;
import org.djutils.draw.point.Point2d;
import org.djutils.exceptions.Throw;
import org.djutils.logger.CategoryLogger;
import org.opentrafficsim.base.geometry.OtsLine2d;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.gtu.plan.operational.Segment;
import org.opentrafficsim.core.gtu.plan.operational.Segments;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LanePosition;
import org.opentrafficsim.road.network.lane.object.detector.LaneDetector;
import org.opentrafficsim.road.network.lane.object.detector.SinkDetector;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEventInterface;

/**
 * Builder for several often used operational plans. E.g., decelerate to come to a full stop at the end of a shape; accelerate
 * to reach a certain speed at the end of a curve; drive constant on a curve; decelerate or accelerate to reach a given end
 * speed at the end of a curve, etc.<br>
 * TODO driving with negative speeds (backward driving) is not yet supported.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public final class LaneOperationalPlanBuilder
{

    /**
     * Minimum distance of an operational plan path; anything shorter will be truncated to 0. <br>
     * If objects related to e.g. molecular movements are simulated using this code, a setter for this parameter will be needed.
     */
    private static final Length MINIMUM_CREDIBLE_PATH_LENGTH = new Length(0.001, LengthUnit.METER);

    /** Constructor. */
    LaneOperationalPlanBuilder()
    {
        // class should not be instantiated
    }

    /**
     * Build a plan with a path and a given start speed to try to reach a provided end speed. Acceleration or deceleration is as
     * provided, until the end speed is reached. After this, constant end speed is used to reach the end point of the path.
     * There is no guarantee that the end speed is actually reached by this plan. If the end speed is zero, and it is reached
     * before completing the path, a truncated path that ends where the GTU stops is used instead.
     * @param gtu the GTU for debugging purposes
     * @param startTime the current time or a time in the future when the plan should start
     * @param startSpeed the speed at the start of the path
     * @param acceleration the acceleration to use
     * @param timeStep time step for the plan
     * @param deviative whether the plan is deviative
     * @return the operational plan to accomplish the given end speed
     * @throws OperationalPlanException when the construction of the operational path fails
     */
    public static LaneBasedOperationalPlan buildAccelerationPlan(final LaneBasedGtu gtu, final Time startTime,
            final Speed startSpeed, final Acceleration acceleration, final Duration timeStep, final boolean deviative)
            throws OperationalPlanException
    {
        Segments segments = Segments.off(startSpeed, timeStep, acceleration);
        Length distance = Length.ZERO;
        for (Segment segment : segments.getSegments())
        {
            distance = distance.plus(segment.totalDistance());
        }

        if (startSpeed.si <= OperationalPlan.DRIFTING_SPEED_SI && acceleration.le(Acceleration.ZERO)
                || distance.le(MINIMUM_CREDIBLE_PATH_LENGTH))
        {
            OrientedPoint2d point = gtu.getLocation();
            Point2d p2 = new Point2d(point.x + Math.cos(point.getDirZ()), point.y + Math.sin(point.getDirZ()));
            OtsLine2d path = new OtsLine2d(point, p2);
            return new LaneBasedOperationalPlan(gtu, path, startTime, Segments.standStill(timeStep), deviative);
        }

        OtsLine2d path = createPathAlongCenterLine(gtu, distance);
        return new LaneBasedOperationalPlan(gtu, path, startTime, segments, deviative);
    }

    /**
     * Creates a path along lane center lines.
     * @param gtu gtu
     * @param distance minimum distance
     * @return path along lane center lines
     */
    public static OtsLine2d createPathAlongCenterLine(final LaneBasedGtu gtu, final Length distance)
    {
        OtsLine2d path = null;
        try
        {
            LanePosition ref = gtu.getReferencePosition();
            double f = ref.lane().fraction(ref.position());
            if (f < 1.0)
            {
                if (f >= 0.0)
                {
                    path = ref.lane().getCenterLine().extractFractional(f, 1.0);
                }
                else
                {
                    path = ref.lane().getCenterLine().extractFractional(0.0, 1.0);
                }
            }
            Lane prevFrom = null;
            Lane from = ref.lane();
            Length prevPos = null;
            Length pos = ref.position();
            int n = 1;
            while (path == null || path.getLength() < distance.si + n * Lane.MARGIN.si)
            {
                n++;
                prevFrom = from;
                if (null == from)
                {
                    CategoryLogger.always().warn("About to die: GTU {} has null from value", gtu.getId());
                }
                from = gtu.getNextLaneForRoute(from);
                // if (from != null && from.getType().equals(Lane.SHOULDER))
                // {
                // CategoryLogger.always().warn("GTU {} on link {} will move on to shoulder.", gtu.getId(),
                // ref.getLane().getLink().getId());
                // }
                prevPos = pos;
                pos = Length.ZERO;
                if (from == null)
                {
                    // check sink detector
                    for (LaneDetector detector : prevFrom.getDetectors(prevPos, prevFrom.getLength(), gtu.getType()))
                    {
                        if (detector instanceof SinkDetector && ((SinkDetector) detector).willDestroy(gtu))
                        {
                            // just add some length so the GTU is happy to go to the sink
                            OrientedPoint2d end = path.getLocationExtendedSI(distance.si + n * Lane.MARGIN.si);
                            List<Point2d> points = path.getPointList();
                            points.add(end);
                            return new OtsLine2d(points);
                        }
                    }
                    // force lane change, and create path from there
                    for (LateralDirectionality lat : new LateralDirectionality[] {LateralDirectionality.LEFT,
                            LateralDirectionality.RIGHT})
                    {
                        Lane latLane = prevFrom.getAdjacentLane(lat, gtu.getType());
                        if (latLane != null && gtu.getNextLaneForRoute(latLane) != null)
                        {
                            gtu.changeLaneInstantaneously(lat);
                            CategoryLogger.always().warn("GTU {} on link {} is forced to change lane towards {}", gtu.getId(),
                                    ref.lane().getLink().getId(), lat);
                            return createPathAlongCenterLine(gtu, distance);
                        }
                    }
                    CategoryLogger.always().error("GTU {} on link {} has nowhere to go and no sink detector either",
                            gtu.getId(), ref.lane().getLink().getId());
                    gtu.destroy();
                    return path;
                }
                if (path == null)
                {
                    path = from.getCenterLine();
                }
                else
                {
                    path = OtsLine2d.concatenate(Lane.MARGIN.si, path, from.getCenterLine());
                }
            }
        }
        catch (GtuException exception)
        {
            throw new RuntimeException("Error during creation of path.", exception);
        }
        return path;
    }

    /**
     * Build a plan with a path and a given start speed to try to reach a provided end speed. Acceleration or deceleration is as
     * provided, until the end speed is reached. After this, constant end speed is used to reach the end point of the path.
     * There is no guarantee that the end speed is actually reached by this plan. If the end speed is zero, and it is reached
     * before completing the path, a truncated path that ends where the GTU stops is used instead.
     * @param gtu the GTU for debugging purposes
     * @param laneChangeDirectionality direction of lane change (on initiation only, after that not important)
     * @param startPosition current position
     * @param startTime the current time or a time in the future when the plan should start
     * @param startSpeed the speed at the start of the path
     * @param acceleration the acceleration to use
     * @param timeStep time step for the plan
     * @param laneChange lane change status
     * @return the operational plan to accomplish the given end speed
     * @throws OperationalPlanException when the construction of the operational path fails
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public static LaneBasedOperationalPlan buildAccelerationLaneChangePlan(final LaneBasedGtu gtu,
            final LateralDirectionality laneChangeDirectionality, final OrientedPoint2d startPosition, final Time startTime,
            final Speed startSpeed, final Acceleration acceleration, final Duration timeStep, final LaneChange laneChange)
            throws OperationalPlanException
    {

        // on first call during lane change, use laneChangeDirectionality as laneChange.getDirection() is NONE
        // on successive calls, use laneChange.getDirection() as laneChangeDirectionality is NONE (i.e. no LC initiated)
        LateralDirectionality direction = laneChange.isChangingLane() ? laneChange.getDirection() : laneChangeDirectionality;

        Segments segments = Segments.off(startSpeed, timeStep, acceleration);
        Length distance = Length.ZERO;
        for (Segment segment : segments.getSegments())
        {
            distance = distance.plus(segment.totalDistance());
        }

        try
        {
            // get position on from lane
            Map<Lane, Length> positions = gtu.positions(gtu.getReference());
            LanePosition ref = gtu.getReferencePosition();
            Iterator<Lane> iterator = ref.lane().accessibleAdjacentLanesPhysical(direction, gtu.getType()).iterator();
            Lane adjLane = iterator.hasNext() ? iterator.next() : null;
            LanePosition from = null;
            if (laneChange.getDirection() == null || (adjLane != null && positions.containsKey(adjLane)))
            {
                // reference lane is from lane, this is ok
                from = ref;
            }
            else
            {
                // reference lane is to lane, this should be accounted for
                for (Lane lane : positions.keySet())
                {
                    if (lane.accessibleAdjacentLanesPhysical(direction, gtu.getType()).contains(ref.lane()))
                    {
                        from = new LanePosition(lane, positions.get(lane));
                        break;
                    }
                }
            }
            Throw.when(from == null, RuntimeException.class, "From lane could not be determined during lane change.");

            // get path and make plan
            OtsLine2d path = laneChange.getPath(timeStep, gtu, from, startPosition, distance, direction);
            LaneBasedOperationalPlan plan = new LaneBasedOperationalPlan(gtu, path, startTime, segments, true);
            return plan;
        }
        catch (GtuException exception)
        {
            throw new RuntimeException("Error during creation of lane change plan.", exception);
        }
    }

    /**
     * Build an operational plan based on a simple operational plan and status info.
     * @param gtu gtu
     * @param startTime start time for plan
     * @param simplePlan simple operational plan
     * @param laneChange lane change status
     * @return operational plan
     * @throws ParameterException if parameter is not defined
     * @throws GtuException gtu exception
     * @throws NetworkException network exception
     */
    public static LaneBasedOperationalPlan buildPlanFromSimplePlan(final LaneBasedGtu gtu, final Time startTime,
            final SimpleOperationalPlan simplePlan, final LaneChange laneChange)
            throws ParameterException, GtuException, NetworkException
    {
        Acceleration acc = gtu.getVehicleModel().boundAcceleration(simplePlan.getAcceleration(), gtu);

        if (gtu.isInstantaneousLaneChange())
        {
            if (simplePlan.isLaneChange())
            {
                gtu.changeLaneInstantaneously(simplePlan.getLaneChangeDirection());
            }
            return LaneOperationalPlanBuilder.buildAccelerationPlan(gtu, startTime, gtu.getSpeed(), acc,
                    simplePlan.getDuration(), false);
        }

        // gradual lane change
        if (!simplePlan.isLaneChange() && !laneChange.isChangingLane())
        {
            return LaneOperationalPlanBuilder.buildAccelerationPlan(gtu, startTime, gtu.getSpeed(), acc,
                    simplePlan.getDuration(), true);
        }
        if (gtu.getSpeed().si == 0.0 && acc.si <= 0.0)
        {
            return LaneOperationalPlanBuilder.buildAccelerationPlan(gtu, startTime, gtu.getSpeed(), acc,
                    simplePlan.getDuration(), false);
        }
        return LaneOperationalPlanBuilder.buildAccelerationLaneChangePlan(gtu, simplePlan.getLaneChangeDirection(),
                gtu.getLocation(), startTime, gtu.getSpeed(), acc, simplePlan.getDuration(), laneChange);
    }

    /**
     * Schedules a lane change finalization after the given distance is covered. This distance is known as the plan is created,
     * but at that point no time can be derived as the plan is required for that. Hence, this method can be scheduled at the
     * same time (sequentially after creation of the plan) to then schedule the actual finalization by deriving time from
     * distance with the plan.
     * @param gtu gtu
     * @param distance distance
     * @param laneChangeDirection lane change direction
     * @throws SimRuntimeException on bad time
     */
    public static void scheduleLaneChangeFinalization(final LaneBasedGtu gtu, final Length distance,
            final LateralDirectionality laneChangeDirection) throws SimRuntimeException
    {
        Time time = gtu.getOperationalPlan().timeAtDistance(distance);
        if (Double.isNaN(time.si))
        {
            // rounding...
            time = gtu.getOperationalPlan().getEndTime();
        }
        SimEventInterface<Duration> event = gtu.getSimulator().scheduleEventAbsTime(time, (short) 6, gtu, "finalizeLaneChange",
                new Object[] {laneChangeDirection});
        gtu.setFinalizeLaneChangeEvent(event);
    }

}
