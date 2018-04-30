package org.opentrafficsim.road.gtu.lane.plan.operational;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.ValueException;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan.Segment;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan.SpeedSegment;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.math.Solver;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneDirection;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.language.Throw;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * Builder for several often used operational plans. E.g., decelerate to come to a full stop at the end of a shape; accelerate
 * to reach a certain speed at the end of a curve; drive constant on a curve; decelerate or accelerate to reach a given end
 * speed at the end of a curve, etc.<br>
 * TODO driving with negative speeds (backward driving) is not yet supported.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 15, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public final class LaneOperationalPlanBuilder
{

    /** Use instant lane changes. */
    public static boolean INSTANT_LANE_CHANGES = false;

    /** Maximum acceleration for unbounded accelerations: 1E12 m/s2. */
    private static final Acceleration MAX_ACCELERATION = new Acceleration(1E12, AccelerationUnit.SI);

    /** Maximum deceleration for unbounded accelerations: -1E12 m/s2. */
    private static final Acceleration MAX_DECELERATION = new Acceleration(-1E12, AccelerationUnit.SI);

    /**
     * Minimum distance of an operational plan path; anything shorter will be truncated to 0. <br>
     * If objects related to e.g. molecular movements are simulated using this code, a setter for this parameter will be needed.
     */
    private static final Length MINIMUM_CREDIBLE_PATH_LENGTH = new Length(0.001, LengthUnit.METER);

    /** Private constructor. */
    LaneOperationalPlanBuilder()
    {
        // class should not be instantiated
    }

    /**
     * Build a plan with a path and a given start speed to try to reach a provided end speed, exactly at the end of the curve.
     * The acceleration (and deceleration) are capped by maxAcceleration and maxDeceleration. Therefore, there is no guarantee
     * that the end speed is actually reached by this plan.
     * @param gtu the GTU for debugging purposes
     * @param distance distance to drive for reaching the end speed
     * @param startTime the current time or a time in the future when the plan should start
     * @param startSpeed the speed at the start of the path
     * @param endSpeed the required end speed
     * @param maxAcceleration the maximum acceleration that can be applied, provided as a POSITIVE number
     * @param maxDeceleration the maximum deceleration that can be applied, provided as a NEGATIVE number
     * @return the operational plan to accomplish the given end speed
     * @throws OperationalPlanException when the plan cannot be generated, e.g. because of a path that is too short
     * @throws OperationalPlanException when the length of the path and the calculated driven distance implied by the
     *             constructed segment list differ more than a given threshold
     * @throws OTSGeometryException in case the lanes are not connected or firstLanePosition is larger than the length of the
     *             first lane
     */
    public static LaneBasedOperationalPlan buildGradualAccelerationPlan(final LaneBasedGTU gtu, final Length distance,
            final Time startTime, final Speed startSpeed, final Speed endSpeed, final Acceleration maxAcceleration,
            final Acceleration maxDeceleration) throws OperationalPlanException, OTSGeometryException
    {
        OTSLine3D path = createPathAlongCenterLine(gtu, distance);
        Segment segment;
        if (startSpeed.eq(endSpeed))
        {
            segment = new SpeedSegment(distance.divideBy(startSpeed));
        }
        else
        {
            try
            {
                // t = 2x / (vt + v0); a = (vt - v0) / t
                Duration duration = distance.multiplyBy(2.0).divideBy(endSpeed.plus(startSpeed));
                Acceleration acceleration = endSpeed.minus(startSpeed).divideBy(duration);
                if (acceleration.si < 0.0 && acceleration.lt(maxDeceleration))
                {
                    acceleration = maxDeceleration;
                    duration = new Duration(Solver.firstSolutionAfter(0, acceleration.si / 2, startSpeed.si, -distance.si),
                            DurationUnit.SI);
                }
                if (acceleration.si > 0.0 && acceleration.gt(maxAcceleration))
                {
                    acceleration = maxAcceleration;
                    duration = new Duration(Solver.firstSolutionAfter(0, acceleration.si / 2, startSpeed.si, -distance.si),
                            DurationUnit.SI);
                }
                segment = new OperationalPlan.AccelerationSegment(duration, acceleration);
            }
            catch (ValueException ve)
            {
                throw new OperationalPlanException(ve);
            }
        }
        ArrayList<Segment> segmentList = new ArrayList<>();
        segmentList.add(segment);
        return new LaneBasedOperationalPlan(gtu, path, startTime, startSpeed, segmentList, false);
    }

    /**
     * Build a plan with a path and a given start speed to reach a provided end speed, exactly at the end of the curve.
     * Acceleration and deceleration are virtually unbounded (1E12 m/s2) to reach the end speed (e.g., to come to a complete
     * stop).
     * @param gtu the GTU for debugging purposes
     * @param distance distance to drive for reaching the end speed
     * @param startTime the current time or a time in the future when the plan should start
     * @param startSpeed the speed at the start of the path
     * @param endSpeed the required end speed
     * @return the operational plan to accomplish the given end speed
     * @throws OperationalPlanException when the length of the path and the calculated driven distance implied by the
     *             constructed segment list differ more than a given threshold
     * @throws OTSGeometryException in case the lanes are not connected or firstLanePositiion is larger than the length of the
     *             first lane
     */
    public static LaneBasedOperationalPlan buildGradualAccelerationPlan(final LaneBasedGTU gtu, final Length distance,
            final Time startTime, final Speed startSpeed, final Speed endSpeed)
            throws OperationalPlanException, OTSGeometryException
    {
        return buildGradualAccelerationPlan(gtu, distance, startTime, startSpeed, endSpeed, MAX_ACCELERATION, MAX_DECELERATION);
    }

    /**
     * Build a plan with a path and a given start speed to try to reach a provided end speed. Acceleration or deceleration is as
     * provided, until the end speed is reached. After this, constant end speed is used to reach the end point of the path.
     * There is no guarantee that the end speed is actually reached by this plan. If the end speed is zero, and it is reached
     * before completing the path, a truncated path that ends where the GTU stops is used instead.
     * @param gtu the GTU for debugging purposes
     * @param distance distance to drive for reaching the end speed
     * @param startTime the current time or a time in the future when the plan should start
     * @param startSpeed the speed at the start of the path
     * @param endSpeed the required end speed
     * @param acceleration the acceleration to use if endSpeed &gt; startSpeed, provided as a POSITIVE number
     * @param deceleration the deceleration to use if endSpeed &lt; startSpeed, provided as a NEGATIVE number
     * @return the operational plan to accomplish the given end speed
     * @throws OperationalPlanException when the construction of the operational path fails
     * @throws OTSGeometryException in case the lanes are not connected or firstLanePositiion is larger than the length of the
     *             first lane
     */
    public static LaneBasedOperationalPlan buildMaximumAccelerationPlan(final LaneBasedGTU gtu, final Length distance,
            final Time startTime, final Speed startSpeed, final Speed endSpeed, final Acceleration acceleration,
            final Acceleration deceleration) throws OperationalPlanException, OTSGeometryException
    {
        OTSLine3D path = createPathAlongCenterLine(gtu, distance);
        ArrayList<Segment> segmentList = new ArrayList<>();
        if (startSpeed.eq(endSpeed))
        {
            segmentList.add(new OperationalPlan.SpeedSegment(distance.divideBy(startSpeed)));
        }
        else
        {
            try
            {
                if (endSpeed.gt(startSpeed))
                {
                    Duration t = endSpeed.minus(startSpeed).divideBy(acceleration);
                    Length x = startSpeed.multiplyBy(t).plus(acceleration.multiplyBy(0.5).multiplyBy(t).multiplyBy(t));
                    if (x.ge(distance))
                    {
                        // we cannot reach the end speed in the given distance with the given acceleration
                        Duration duration =
                                new Duration(Solver.firstSolutionAfter(0, acceleration.si / 2, startSpeed.si, -distance.si),
                                        DurationUnit.SI);
                        segmentList.add(new OperationalPlan.AccelerationSegment(duration, acceleration));
                    }
                    else
                    {
                        // we reach the (higher) end speed before the end of the segment. Make two segments.
                        segmentList.add(new OperationalPlan.AccelerationSegment(t, acceleration));
                        Duration duration = distance.minus(x).divideBy(endSpeed);
                        segmentList.add(new OperationalPlan.SpeedSegment(duration));
                    }
                }
                else
                {
                    Duration t = endSpeed.minus(startSpeed).divideBy(deceleration);
                    Length x = startSpeed.multiplyBy(t).plus(deceleration.multiplyBy(0.5).multiplyBy(t).multiplyBy(t));
                    if (x.ge(distance))
                    {
                        // we cannot reach the end speed in the given distance with the given deceleration
                        Duration duration =
                                new Duration(Solver.firstSolutionAfter(0, deceleration.si / 2, startSpeed.si, -distance.si),
                                        DurationUnit.SI);
                        segmentList.add(new OperationalPlan.AccelerationSegment(duration, deceleration));
                    }
                    else
                    {
                        if (endSpeed.si == 0.0)
                        {
                            // if endSpeed == 0, we cannot reach the end of the path. Therefore, build a partial path.
                            OTSLine3D partialPath = path.truncate(x.si);
                            segmentList.add(new OperationalPlan.AccelerationSegment(t, deceleration));
                            return new LaneBasedOperationalPlan(gtu, partialPath, startTime, startSpeed, segmentList, false);
                        }
                        // we reach the (lower) end speed, larger than zero, before the end of the segment. Make two segments.
                        segmentList.add(new OperationalPlan.AccelerationSegment(t, deceleration));
                        Duration duration = distance.minus(x).divideBy(endSpeed);
                        segmentList.add(new OperationalPlan.SpeedSegment(duration));
                    }
                }
            }
            catch (ValueException ve)
            {
                throw new OperationalPlanException(ve);
            }

        }
        return new LaneBasedOperationalPlan(gtu, path, startTime, startSpeed, segmentList, false);
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
     * @return the operational plan to accomplish the given end speed
     * @throws OperationalPlanException when the construction of the operational path fails
     * @throws OTSGeometryException in case the lanes are not connected or firstLanePositiion is larger than the length of the
     *             first lane
     */
    public static LaneBasedOperationalPlan buildAccelerationPlan(final LaneBasedGTU gtu, final Time startTime,
            final Speed startSpeed, final Acceleration acceleration, final Duration timeStep)
            throws OperationalPlanException, OTSGeometryException
    {
        if (startSpeed.si <= OperationalPlan.DRIFTING_SPEED_SI && acceleration.le(Acceleration.ZERO))
        {
            return new LaneBasedOperationalPlan(gtu, gtu.getLocation(), startTime, timeStep, false);
        }

        Duration brakingTime = brakingTime(acceleration, startSpeed, timeStep);
        Length distance =
                Length.createSI(startSpeed.si * brakingTime.si + .5 * acceleration.si * brakingTime.si * brakingTime.si);
        List<Segment> segmentList = createAccelerationSegments(startSpeed, acceleration, brakingTime, timeStep);
        if (distance.le(MINIMUM_CREDIBLE_PATH_LENGTH))
        {
            return new LaneBasedOperationalPlan(gtu, gtu.getLocation(), startTime, timeStep, false);
        }

        OTSLine3D path = createPathAlongCenterLine(gtu, distance);
        return new LaneBasedOperationalPlan(gtu, path, startTime, startSpeed, segmentList, false);
    }

    /**
     * Creates a path along lane center lines.
     * @param gtu LaneBasedGTU; gtu
     * @param distance Length; minimum distance
     * @return OTSLine3D; path along lane center lines
     * @throws OTSGeometryException
     */
    public static OTSLine3D createPathAlongCenterLine(final LaneBasedGTU gtu, final Length distance) throws OTSGeometryException
    {
        OTSLine3D path = null;
        try
        {
            DirectedLanePosition ref = gtu.getReferencePosition();
            double f = ref.getLane().fraction(ref.getPosition());
            path = ref.getGtuDirection().isPlus() ? ref.getLane().getCenterLine().extractFractional(f, 1.0)
                    : ref.getLane().getCenterLine().extractFractional(0.0, f).reverse();
            LaneDirection from = ref.getLaneDirection();
            int n = 0;
            while (path.getLength().si < distance.si + n * Lane.MARGIN.si)
            {
                n++;
                from = from.getNextLaneDirection(gtu);
                path = OTSLine3D.concatenate(Lane.MARGIN.si, path, from.getDirection().isPlus() ? from.getLane().getCenterLine()
                        : from.getLane().getCenterLine().reverse());
            }
        }
        catch (GTUException exception)
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
     * @throws OTSGeometryException in case the lanes are not connected or firstLanePositiion is larger than the length of the
     *             first lane
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public static LaneBasedOperationalPlan buildAccelerationLaneChangePlan(final LaneBasedGTU gtu,
            final LateralDirectionality laneChangeDirectionality, final DirectedPoint startPosition, final Time startTime,
            final Speed startSpeed, final Acceleration acceleration, final Duration timeStep, final LaneChange laneChange)
            throws OperationalPlanException, OTSGeometryException
    {

        // on first call during lane change, use laneChangeDirectionality as laneChange.getDirection() is NONE
        // on successive calls, use laneChange.getDirection() as laneChangeDirectionality is NONE (i.e. no LC initiated)
        LateralDirectionality direction = laneChange.isChangingLane() ? laneChange.getDirection() : laneChangeDirectionality;

        Duration brakingTime = brakingTime(acceleration, startSpeed, timeStep);
        Length planDistance =
                Length.createSI(startSpeed.si * brakingTime.si + .5 * acceleration.si * brakingTime.si * brakingTime.si);
        List<Segment> segmentList = createAccelerationSegments(startSpeed, acceleration, brakingTime, timeStep);

        try
        {
            // get position on from lane
            Map<Lane, Length> positions = gtu.positions(gtu.getReference());
            DirectedLanePosition ref = gtu.getReferencePosition();
            Iterator<Lane> iterator = ref.getLane()
                    .accessibleAdjacentLanesPhysical(direction, gtu.getGTUType(), ref.getGtuDirection()).iterator();
            Lane adjLane = iterator.hasNext() ? iterator.next() : null;
            DirectedLanePosition from = null;
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
                    if (lane.accessibleAdjacentLanesPhysical(direction, gtu.getGTUType(), ref.getGtuDirection())
                            .contains(ref.getLane()))
                    {
                        from = new DirectedLanePosition(lane, positions.get(lane), ref.getGtuDirection());
                        break;
                    }
                }
            }
            Throw.when(from == null, RuntimeException.class, "From lane could not be determined during lane change.");

            // get path and make plan
            OTSLine3D path = laneChange.getPath(timeStep, gtu, from, startPosition, planDistance, direction);
            LaneBasedOperationalPlan plan = new LaneBasedOperationalPlan(gtu, path, startTime, startSpeed, segmentList, true);
            return plan;
        }
        catch (GTUException exception)
        {
            throw new RuntimeException("Error during creation of lane change plan.", exception);
        }
    }

    /**
     * Returns the effective braking time, which stops if stand-still is reached.
     * @param acceleration Acceleration; acceleration
     * @param startSpeed Speed; start speed
     * @param time Duration; intended time step
     * @return Duration; effective braking time
     */
    public static Duration brakingTime(final Acceleration acceleration, final Speed startSpeed, final Duration time)
    {
        if (acceleration.ge0())
        {
            return time;
        }
        double t = startSpeed.si / -acceleration.si;
        if (t >= time.si)
        {
            return time;
        }
        return Duration.createSI(t);
    }

    /**
     * Creates 1 or 2 segments in an operational plan. Two segments are returned of stand-still is reached within the time step.
     * @param startSpeed Speed; start speed
     * @param acceleration Acceleration; acceleration
     * @param brakingTime Duration; braking time until stand-still
     * @param timeStep Duration; time step
     * @return 1 or 2 segments in an operational plan
     */
    private static List<Segment> createAccelerationSegments(final Speed startSpeed, final Acceleration acceleration,
            final Duration brakingTime, final Duration timeStep)
    {
        List<Segment> segmentList = new ArrayList<>();
        if (brakingTime.si < timeStep.si)
        {
            if (brakingTime.si > 0.0)
            {
                segmentList.add(new OperationalPlan.AccelerationSegment(brakingTime, acceleration));
            }
            segmentList.add(new OperationalPlan.SpeedSegment(timeStep.minus(brakingTime)));
        }
        else
        {
            segmentList.add(new OperationalPlan.AccelerationSegment(timeStep, acceleration));
        }
        return segmentList;
    }

    /**
     * Build an operational plan based on a simple operational plan and status info.
     * @param gtu gtu
     * @param startTime start time for plan
     * @param simplePlan simple operational plan
     * @param laneChange lane change status
     * @return operational plan
     * @throws ParameterException if parameter is not defined
     * @throws GTUException gtu exception
     * @throws NetworkException network exception
     * @throws OperationalPlanException operational plan exeption
     */
    public static LaneBasedOperationalPlan buildPlanFromSimplePlan(final LaneBasedGTU gtu, final Time startTime,
            final SimpleOperationalPlan simplePlan, final LaneChange laneChange)
            throws ParameterException, GTUException, NetworkException, OperationalPlanException
    {
        if (INSTANT_LANE_CHANGES)
        {
            if (simplePlan.isLaneChange())
            {
                gtu.changeLaneInstantaneously(simplePlan.getLaneChangeDirection());
            }
            try
            {
                return LaneOperationalPlanBuilder.buildAccelerationPlan(gtu, startTime, gtu.getSpeed(),
                        simplePlan.getAcceleration(), simplePlan.getDuration());
            }
            catch (OTSGeometryException exception)
            {
                throw new OperationalPlanException(exception);
            }
        }

        // gradual lane change
        try
        {
            if ((!simplePlan.isLaneChange() && !laneChange.isChangingLane())
                    || (gtu.getSpeed().si == 0.0 && simplePlan.getAcceleration().si <= 0.0))
            {
                return LaneOperationalPlanBuilder.buildAccelerationPlan(gtu, startTime, gtu.getSpeed(),
                        simplePlan.getAcceleration(), simplePlan.getDuration());
            }
            return LaneOperationalPlanBuilder.buildAccelerationLaneChangePlan(gtu, simplePlan.getLaneChangeDirection(),
                    gtu.getLocation(), startTime, gtu.getSpeed(), simplePlan.getAcceleration(), simplePlan.getDuration(),
                    laneChange);
        }
        catch (OTSGeometryException exception)
        {
            throw new OperationalPlanException(exception);
        }
    }

    /**
     * Schedules a lane change finalization after the given distance is covered. This distance is known as the plan is created,
     * but at that point no time can be derived as the plan is required for that. Hence, this method can be scheduled at the
     * same time (sequentially after creation of the plan) to then schedule the actual finalization by deriving time from
     * distance with the plan.
     * @param gtu LaneBasedGTU; gtu
     * @param distance Length; distance
     * @param laneChangeDirection LateralDirectionality; lane change direction
     * @throws SimRuntimeException on bad time
     */
    public static void scheduleLaneChangeFinalization(final LaneBasedGTU gtu, final Length distance,
            final LateralDirectionality laneChangeDirection) throws SimRuntimeException
    {
        Time time = gtu.getOperationalPlan().timeAtDistance(distance);
        if (Double.isNaN(time.si))
        {
            // rounding...
            time = gtu.getOperationalPlan().getEndTime();
        }
        gtu.getSimulator().scheduleEventAbs(time, (short) 6, gtu, gtu, "finalizeLaneChange",
                new Object[] { laneChangeDirection });
    }

    /**
     * Build a plan with a path and a given start speed to try to come to a stop with a given deceleration. If the GTU can stop
     * before completing the given path, a truncated path that ends where the GTU stops is used instead. There is no guarantee
     * that the OperationalPlan will lead to a complete stop.
     * @param gtu the GTU for debugging purposes
     * @param distance distance to drive for reaching the end speed
     * @param startTime the current time or a time in the future when the plan should start
     * @param startSpeed the speed at the start of the path
     * @param deceleration the deceleration to use if endSpeed &lt; startSpeed, provided as a NEGATIVE number
     * @return the operational plan to accomplish the given end speed
     * @throws OperationalPlanException when construction of the operational path fails
     * @throws OTSGeometryException in case the lanes are not connected or firstLanePositiion is larger than the length of the
     *             first lane
     */
    public static LaneBasedOperationalPlan buildStopPlan(final LaneBasedGTU gtu, final Length distance, final Time startTime,
            final Speed startSpeed, final Acceleration deceleration) throws OperationalPlanException, OTSGeometryException
    {
        return buildMaximumAccelerationPlan(gtu, distance, startTime, startSpeed, new Speed(0.0, SpeedUnit.SI),
                new Acceleration(1.0, AccelerationUnit.SI), deceleration);
    }

}
