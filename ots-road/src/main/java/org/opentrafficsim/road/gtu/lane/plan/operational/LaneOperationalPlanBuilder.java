package org.opentrafficsim.road.gtu.lane.plan.operational;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.exceptions.Throw;
import org.djutils.logger.CategoryLogger;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.geometry.DirectedPoint;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.geometry.OtsLine3d;
import org.opentrafficsim.core.geometry.OtsPoint3d;
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
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
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
     * @param gtu LaneBasedGtu; the GTU for debugging purposes
     * @param startTime Time; the current time or a time in the future when the plan should start
     * @param startSpeed Speed; the speed at the start of the path
     * @param acceleration Acceleration; the acceleration to use
     * @param timeStep Duration; time step for the plan
     * @param deviative boolean; whether the plan is deviative
     * @return the operational plan to accomplish the given end speed
     * @throws OperationalPlanException when the construction of the operational path fails
     * @throws OtsGeometryException in case the lanes are not connected or firstLanePositiion is larger than the length of the
     *             first lane
     */
    public static LaneBasedOperationalPlan buildAccelerationPlan(final LaneBasedGtu gtu, final Time startTime,
            final Speed startSpeed, final Acceleration acceleration, final Duration timeStep, final boolean deviative)
            throws OperationalPlanException, OtsGeometryException
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
            DirectedPoint point = gtu.getLocation();
            OtsPoint3d p2 = new OtsPoint3d(point.x + Math.cos(point.getRotZ()), point.y + Math.sin(point.getRotZ()), point.z);
            OtsLine3d path = new OtsLine3d(new OtsPoint3d(point), p2);
            return new LaneBasedOperationalPlan(gtu, path, startTime, Segments.standStill(timeStep), deviative);
        }

        OtsLine3d path = createPathAlongCenterLine(gtu, distance);
        return new LaneBasedOperationalPlan(gtu, path, startTime, segments, deviative);
    }

    /**
     * Creates a path along lane center lines.
     * @param gtu LaneBasedGtu; gtu
     * @param distance Length; minimum distance
     * @return OtsLine3d; path along lane center lines
     * @throws OtsGeometryException when any of the OtsLine3d operations fails
     */
    public static OtsLine3d createPathAlongCenterLine(final LaneBasedGtu gtu, final Length distance) throws OtsGeometryException
    {
        OtsLine3d path = null;
        try
        {
            LanePosition ref = gtu.getReferencePosition();
            double f = ref.getLane().fraction(ref.getPosition());
            if (f < 1.0)
            {
                if (f >= 0.0)
                {
                    path = ref.getLane().getCenterLine().extractFractional(f, 1.0);
                }
                else
                {
                    path = ref.getLane().getCenterLine().extractFractional(0.0, 1.0);
                }
            }
            Lane prevFrom = null;
            Lane from = ref.getLane();
            Length prevPos = null;
            Length pos = ref.getPosition();
            int n = 1;
            while (path == null || path.getLength().si < distance.si + n * Lane.MARGIN.si)
            {
                n++;
                prevFrom = from;
                if (null == from)
                {
                    CategoryLogger.always().warn("About to die: GTU {} has null from value", gtu.getId());
                }
                from = gtu.getNextLaneForRoute(from);
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
                            DirectedPoint end = path.getLocationExtendedSI(distance.si + n * Lane.MARGIN.si);
                            List<OtsPoint3d> points = new ArrayList<>(Arrays.asList(path.getPoints()));
                            points.add(new OtsPoint3d(end));
                            return new OtsLine3d(points);
                        }
                    }
                    CategoryLogger.always().error("GTU {} on link {} has nowhere to go and no sink detector either", gtu,
                            ref.getLane().getParentLink().getId());
                    gtu.destroy();
                    return path;
                }
                if (path == null)
                {
                    path = from.getCenterLine();
                }
                else
                {
                    path = OtsLine3d.concatenate(Lane.MARGIN.si, path, from.getCenterLine());
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
     * @param gtu LaneBasedGtu; the GTU for debugging purposes
     * @param laneChangeDirectionality LateralDirectionality; direction of lane change (on initiation only, after that not
     *            important)
     * @param startPosition DirectedPoint; current position
     * @param startTime Time; the current time or a time in the future when the plan should start
     * @param startSpeed Speed; the speed at the start of the path
     * @param acceleration Acceleration; the acceleration to use
     * @param timeStep Duration; time step for the plan
     * @param laneChange LaneChange; lane change status
     * @return the operational plan to accomplish the given end speed
     * @throws OperationalPlanException when the construction of the operational path fails
     * @throws OtsGeometryException in case the lanes are not connected or firstLanePositiion is larger than the length of the
     *             first lane
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public static LaneBasedOperationalPlan buildAccelerationLaneChangePlan(final LaneBasedGtu gtu,
            final LateralDirectionality laneChangeDirectionality, final DirectedPoint startPosition, final Time startTime,
            final Speed startSpeed, final Acceleration acceleration, final Duration timeStep, final LaneChange laneChange)
            throws OperationalPlanException, OtsGeometryException
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
            Iterator<Lane> iterator = ref.getLane().accessibleAdjacentLanesPhysical(direction, gtu.getType()).iterator();
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
                    if (lane.accessibleAdjacentLanesPhysical(direction, gtu.getType()).contains(ref.getLane()))
                    {
                        from = new LanePosition(lane, positions.get(lane));
                        break;
                    }
                }
            }
            Throw.when(from == null, RuntimeException.class, "From lane could not be determined during lane change.");

            // get path and make plan
            OtsLine3d path = laneChange.getPath(timeStep, gtu, from, startPosition, distance, direction);
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
     * @param gtu LaneBasedGtu; gtu
     * @param startTime Time; start time for plan
     * @param simplePlan SimpleOperationalPlan; simple operational plan
     * @param laneChange LaneChange; lane change status
     * @return operational plan
     * @throws ParameterException if parameter is not defined
     * @throws GtuException gtu exception
     * @throws NetworkException network exception
     * @throws OperationalPlanException operational plan exeption
     */
    public static LaneBasedOperationalPlan buildPlanFromSimplePlan(final LaneBasedGtu gtu, final Time startTime,
            final SimpleOperationalPlan simplePlan, final LaneChange laneChange)
            throws ParameterException, GtuException, NetworkException, OperationalPlanException
    {
        Acceleration acc = gtu.getVehicleModel().boundAcceleration(simplePlan.getAcceleration(), gtu);

        if (gtu.isInstantaneousLaneChange())
        {
            if (simplePlan.isLaneChange())
            {
                gtu.changeLaneInstantaneously(simplePlan.getLaneChangeDirection());
            }
            try
            {
                return LaneOperationalPlanBuilder.buildAccelerationPlan(gtu, startTime, gtu.getSpeed(), acc,
                        simplePlan.getDuration(), false);
            }
            catch (OtsGeometryException exception)
            {
                throw new OperationalPlanException(exception);
            }
        }

        // gradual lane change
        try
        {
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
        catch (OtsGeometryException exception)
        {
            throw new OperationalPlanException(exception);
        }
    }

    /**
     * Schedules a lane change finalization after the given distance is covered. This distance is known as the plan is created,
     * but at that point no time can be derived as the plan is required for that. Hence, this method can be scheduled at the
     * same time (sequentially after creation of the plan) to then schedule the actual finalization by deriving time from
     * distance with the plan.
     * @param gtu LaneBasedGtu; gtu
     * @param distance Length; distance
     * @param laneChangeDirection LateralDirectionality; lane change direction
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
