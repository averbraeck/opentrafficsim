package org.opentrafficsim.road.gtu.lane.plan.operational;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.ValueRuntimeException;
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
import org.opentrafficsim.core.geometry.OtsLine3D;
import org.opentrafficsim.core.geometry.OtsPoint3D;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan.Segment;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan.SpeedSegment;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.math.Solver;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LanePosition;
import org.opentrafficsim.road.network.lane.object.detector.Detector;
import org.opentrafficsim.road.network.lane.object.detector.SinkDetector;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEventInterface;

/**
 * Builder for several often used operational plans. E.g., decelerate to come to a full stop at the end of a shape; accelerate
 * to reach a certain speed at the end of a curve; drive constant on a curve; decelerate or accelerate to reach a given end
 * speed at the end of a curve, etc.<br>
 * TODO driving with negative speeds (backward driving) is not yet supported.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public final class LaneOperationalPlanBuilder // class package private for scheduling static method on an instance
{

    /** Maximum acceleration for unbounded accelerations: 1E12 m/s2. */
    private static final Acceleration MAX_ACCELERATION = new Acceleration(1E12, AccelerationUnit.SI);

    /** Maximum deceleration for unbounded accelerations: -1E12 m/s2. */
    private static final Acceleration MAX_DECELERATION = new Acceleration(-1E12, AccelerationUnit.SI);

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
     * Build a plan with a path and a given start speed to try to reach a provided end speed, exactly at the end of the curve.
     * The acceleration (and deceleration) are capped by maxAcceleration and maxDeceleration. Therefore, there is no guarantee
     * that the end speed is actually reached by this plan.
     * @param gtu LaneBasedGtu; the GTU for debugging purposes
     * @param distance Length; distance to drive for reaching the end speed
     * @param startTime Time; the current time or a time in the future when the plan should start
     * @param startSpeed Speed; the speed at the start of the path
     * @param endSpeed Speed; the required end speed
     * @param maxAcceleration Acceleration; the maximum acceleration that can be applied, provided as a POSITIVE number
     * @param maxDeceleration Acceleration; the maximum deceleration that can be applied, provided as a NEGATIVE number
     * @return the operational plan to accomplish the given end speed
     * @throws OperationalPlanException when the plan cannot be generated, e.g. because of a path that is too short
     * @throws OperationalPlanException when the length of the path and the calculated driven distance implied by the
     *             constructed segment list differ more than a given threshold
     * @throws OtsGeometryException in case the lanes are not connected or firstLanePosition is larger than the length of the
     *             first lane
     */
    public static LaneBasedOperationalPlan buildGradualAccelerationPlan(final LaneBasedGtu gtu, final Length distance,
            final Time startTime, final Speed startSpeed, final Speed endSpeed, final Acceleration maxAcceleration,
            final Acceleration maxDeceleration) throws OperationalPlanException, OtsGeometryException
    {
        OtsLine3D path = createPathAlongCenterLine(gtu, distance);
        Segment segment;
        if (startSpeed.eq(endSpeed))
        {
            segment = new SpeedSegment(distance.divide(startSpeed));
        }
        else
        {
            try
            {
                // t = 2x / (vt + v0); a = (vt - v0) / t
                Duration duration = distance.times(2.0).divide(endSpeed.plus(startSpeed));
                Acceleration acceleration = endSpeed.minus(startSpeed).divide(duration);
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
            catch (ValueRuntimeException ve)
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
     * @param gtu LaneBasedGtu; the GTU for debugging purposes
     * @param distance Length; distance to drive for reaching the end speed
     * @param startTime Time; the current time or a time in the future when the plan should start
     * @param startSpeed Speed; the speed at the start of the path
     * @param endSpeed Speed; the required end speed
     * @return the operational plan to accomplish the given end speed
     * @throws OperationalPlanException when the length of the path and the calculated driven distance implied by the
     *             constructed segment list differ more than a given threshold
     * @throws OtsGeometryException in case the lanes are not connected or firstLanePositiion is larger than the length of the
     *             first lane
     */
    public static LaneBasedOperationalPlan buildGradualAccelerationPlan(final LaneBasedGtu gtu, final Length distance,
            final Time startTime, final Speed startSpeed, final Speed endSpeed)
            throws OperationalPlanException, OtsGeometryException
    {
        return buildGradualAccelerationPlan(gtu, distance, startTime, startSpeed, endSpeed, MAX_ACCELERATION, MAX_DECELERATION);
    }

    /**
     * Build a plan with a path and a given start speed to try to reach a provided end speed. Acceleration or deceleration is as
     * provided, until the end speed is reached. After this, constant end speed is used to reach the end point of the path.
     * There is no guarantee that the end speed is actually reached by this plan. If the end speed is zero, and it is reached
     * before completing the path, a truncated path that ends where the GTU stops is used instead.
     * @param gtu LaneBasedGtu; the GTU for debugging purposes
     * @param distance Length; distance to drive for reaching the end speed
     * @param startTime Time; the current time or a time in the future when the plan should start
     * @param startSpeed Speed; the speed at the start of the path
     * @param endSpeed Speed; the required end speed
     * @param acceleration Acceleration; the acceleration to use if endSpeed &gt; startSpeed, provided as a POSITIVE number
     * @param deceleration Acceleration; the deceleration to use if endSpeed &lt; startSpeed, provided as a NEGATIVE number
     * @return the operational plan to accomplish the given end speed
     * @throws OperationalPlanException when the construction of the operational path fails
     * @throws OtsGeometryException in case the lanes are not connected or firstLanePositiion is larger than the length of the
     *             first lane
     */
    public static LaneBasedOperationalPlan buildMaximumAccelerationPlan(final LaneBasedGtu gtu, final Length distance,
            final Time startTime, final Speed startSpeed, final Speed endSpeed, final Acceleration acceleration,
            final Acceleration deceleration) throws OperationalPlanException, OtsGeometryException
    {
        OtsLine3D path = createPathAlongCenterLine(gtu, distance);
        ArrayList<Segment> segmentList = new ArrayList<>();
        if (startSpeed.eq(endSpeed))
        {
            segmentList.add(new OperationalPlan.SpeedSegment(distance.divide(startSpeed)));
        }
        else
        {
            try
            {
                if (endSpeed.gt(startSpeed))
                {
                    Duration t = endSpeed.minus(startSpeed).divide(acceleration);
                    Length x = startSpeed.times(t).plus(acceleration.times(0.5).times(t).times(t));
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
                        Duration duration = distance.minus(x).divide(endSpeed);
                        segmentList.add(new OperationalPlan.SpeedSegment(duration));
                    }
                }
                else
                {
                    Duration t = endSpeed.minus(startSpeed).divide(deceleration);
                    Length x = startSpeed.times(t).plus(deceleration.times(0.5).times(t).times(t));
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
                            OtsLine3D partialPath = path.truncate(x.si);
                            segmentList.add(new OperationalPlan.AccelerationSegment(t, deceleration));
                            return new LaneBasedOperationalPlan(gtu, partialPath, startTime, startSpeed, segmentList, false);
                        }
                        // we reach the (lower) end speed, larger than zero, before the end of the segment. Make two segments.
                        segmentList.add(new OperationalPlan.AccelerationSegment(t, deceleration));
                        Duration duration = distance.minus(x).divide(endSpeed);
                        segmentList.add(new OperationalPlan.SpeedSegment(duration));
                    }
                }
            }
            catch (ValueRuntimeException ve)
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
        if (startSpeed.si <= OperationalPlan.DRIFTING_SPEED_SI && acceleration.le(Acceleration.ZERO))
        {
            return new LaneBasedOperationalPlan(gtu, gtu.getLocation(), startTime, timeStep, deviative);
        }

        Duration brakingTime = brakingTime(acceleration, startSpeed, timeStep);
        Length distance =
                Length.instantiateSI(startSpeed.si * brakingTime.si + .5 * acceleration.si * brakingTime.si * brakingTime.si);
        List<Segment> segmentList = createAccelerationSegments(startSpeed, acceleration, brakingTime, timeStep);
        if (distance.le(MINIMUM_CREDIBLE_PATH_LENGTH))
        {
            return new LaneBasedOperationalPlan(gtu, gtu.getLocation(), startTime, timeStep, deviative);
        }
        OtsLine3D path = createPathAlongCenterLine(gtu, distance);
        return new LaneBasedOperationalPlan(gtu, path, startTime, startSpeed, segmentList, deviative);
    }

    /**
     * Creates a path along lane center lines.
     * @param gtu LaneBasedGtu; gtu
     * @param distance Length; minimum distance
     * @return OTSLine3D; path along lane center lines
     * @throws OtsGeometryException when any of the OTSLine3D operations fails
     */
    public static OtsLine3D createPathAlongCenterLine(final LaneBasedGtu gtu, final Length distance) throws OtsGeometryException
    {
        // if (gtu.getId().equals("1669") && gtu.getSimulator().getSimulatorTime().si >= 2508.9)
        // {
        // System.out.println("processing gtu " + gtu);
        // try
        // {
        // for (Lane l : gtu.fractionalPositions(RelativePosition.REFERENCE_POSITION).keySet())
        // {
        // System.out.println("fractional position on lane " + l + ": "
        // + gtu.fractionalPositions(RelativePosition.REFERENCE_POSITION).get(l));
        // }
        // System.out.println("reference position is " + gtu.getReferencePosition());
        // System.out.println("operational plan path is " + gtu.getOperationalPlan().getPath());
        // }
        // catch (GTUException e)
        // {
        // e.printStackTrace();
        // }
        // }
        OtsLine3D path = null;
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
            int n = 1;
            boolean alternativeTried = false;
            while (path == null || path.getLength().si < distance.si + n * Lane.MARGIN.si)
            {
                n++;
                prevFrom = from;
                if (null == from)
                {
                    CategoryLogger.always().warn("About to die: GTU {} has null from value", gtu.getId());
                }
                from = gtu.getNextLaneForRoute(from);
                if (from == null)
                {
                    // check sink detector
                    Length pos = prevFrom.getLength();
                    for (Detector detector : prevFrom.getDetectors(pos, pos, gtu.getType()))
                    {
                        // XXX for now, the same is not done for the DestinationSensor (e.g., decrease speed for parking)
                        if (detector instanceof SinkDetector)
                        {
                            // just add some length so the GTU is happy to go to the sink
                            DirectedPoint end = path.getLocationExtendedSI(distance.si + n * Lane.MARGIN.si);
                            List<OtsPoint3D> points = new ArrayList<>(Arrays.asList(path.getPoints()));
                            points.add(new OtsPoint3D(end));
                            return new OtsLine3D(points);
                        }
                    }
                    // START CLEVER
                    /*-
                    if (!alternativeTried)
                    {
                        for (Lane l : gtu.fractionalPositions(RelativePosition.REFERENCE_POSITION).keySet())
                        {
                            if (ref.getLane().equals(l))
                            {
                                continue;
                            }
                            CategoryLogger.always().warn("GTU {} dead end on {}; but reference position is on {}; trying that",
                                    gtu.getId(), ref, l);
                            // Figure out the driving direction and position on Lane l
                            // For now assume that lane l and ref are lanes on the same parent link. If not, chaos may occur
                            if (!l.getParentLink().equals(ref.getLane().getParentLink()))
                            {
                                CategoryLogger.always()
                                        .error("Assumption that l and ref.getLane are on same Link does not hold");
                            }
                            from = new LaneDirection(l, ref.getGtuDirection());
                            if (ref.getGtuDirection().isPlus() && f < 1.0)
                            {
                                if (f >= 0.0)
                                {
                                    path = l.getCenterLine().extractFractional(f, 1.0);
                                }
                                else
                                {
                                    path = l.getCenterLine().extractFractional(0.0, 1.0);
                                }
                            }
                            else if (ref.getGtuDirection().isMinus() && f > 0.0)
                            {
                                if (f <= 1.0)
                                {
                                    path = l.getCenterLine().extractFractional(0.0, f).reverse();
                                }
                                else
                                {
                                    path = l.getCenterLine().extractFractional(0.0, 1.0).reverse();
                                }
                            }
                            alternativeTried = true;
                        }
                        if (null != from)
                        {
                            continue;
                        }
                    }
                    */
                    // END CLEVER
                    CategoryLogger.always().error("GTU {} has nowhere to go and no sink detector either", gtu);
                    // gtu.getReferencePosition(); // CLEVER
                    gtu.destroy();
                    return path;
                }
                if (path == null)
                {
                    path = from.getCenterLine();
                }
                else
                {
                    path = OtsLine3D.concatenate(Lane.MARGIN.si, path, from.getCenterLine());
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

        Duration brakingTime = brakingTime(acceleration, startSpeed, timeStep);
        Length planDistance =
                Length.instantiateSI(startSpeed.si * brakingTime.si + .5 * acceleration.si * brakingTime.si * brakingTime.si);
        List<Segment> segmentList = createAccelerationSegments(startSpeed, acceleration, brakingTime, timeStep);

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
            OtsLine3D path = laneChange.getPath(timeStep, gtu, from, startPosition, planDistance, direction);
            LaneBasedOperationalPlan plan = new LaneBasedOperationalPlan(gtu, path, startTime, startSpeed, segmentList, true);
            return plan;
        }
        catch (GtuException exception)
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
        return Duration.instantiateSI(t);
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

    /**
     * Build a plan with a path and a given start speed to try to come to a stop with a given deceleration. If the GTU can stop
     * before completing the given path, a truncated path that ends where the GTU stops is used instead. There is no guarantee
     * that the OperationalPlan will lead to a complete stop.
     * @param gtu LaneBasedGtu; the GTU for debugging purposes
     * @param distance Length; distance to drive for reaching the end speed
     * @param startTime Time; the current time or a time in the future when the plan should start
     * @param startSpeed Speed; the speed at the start of the path
     * @param deceleration Acceleration; the deceleration to use if endSpeed &lt; startSpeed, provided as a NEGATIVE number
     * @return the operational plan to accomplish the given end speed
     * @throws OperationalPlanException when construction of the operational path fails
     * @throws OtsGeometryException in case the lanes are not connected or firstLanePositiion is larger than the length of the
     *             first lane
     */
    public static LaneBasedOperationalPlan buildStopPlan(final LaneBasedGtu gtu, final Length distance, final Time startTime,
            final Speed startSpeed, final Acceleration deceleration) throws OperationalPlanException, OtsGeometryException
    {
        return buildMaximumAccelerationPlan(gtu, distance, startTime, startSpeed, new Speed(0.0, SpeedUnit.SI),
                new Acceleration(1.0, AccelerationUnit.SI), deceleration);
    }

}
