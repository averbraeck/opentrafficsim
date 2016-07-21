package org.opentrafficsim.road.gtu.lane.plan.operational;

import java.util.ArrayList;
import java.util.List;

import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.ValueException;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan.SpeedSegment;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.math.Solver;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * Builder for several often used operational plans, based on a list of lanes. E.g., decelerate to come to a full stop at the
 * end of a shape; accelerate to reach a certain speed at the end of a curve; drive constant on a curve; decelerate or
 * accelerate to reach a given end speed at the end of a curve, etc.<br>
 * TODO driving with negative speeds (backward driving) is not yet supported.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 15, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public final class LaneOperationalPlanBuilder
{
    /** Maximum acceleration for unbounded accelerations: 1E12 m/s2. */
    private static final Acceleration MAX_ACCELERATION = new Acceleration(1E12, AccelerationUnit.SI);

    /** Maximum deceleration for unbounded accelerations: -1E12 m/s2. */
    private static final Acceleration MAX_DECELERATION = new Acceleration(-1E12, AccelerationUnit.SI);

    /** Private constructor. */
    private LaneOperationalPlanBuilder()
    {
        // class should not be instantiated
    }

    /**
     * Build a plan with a path and a given start speed to try to reach a provided end speed, exactly at the end of the curve.
     * The acceleration (and deceleration) are capped by maxAcceleration and maxDeceleration. Therefore, there is no guarantee
     * that the end speed is actually reached by this plan.
     * @param gtu the GTU for debugging purposes
     * @param lanes a list of connected Lanes to do the driving on
     * @param firstLanePosition position on the first lane with the reference point of the GTU
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
    public static LaneBasedOperationalPlan buildGradualAccelerationPlan(final LaneBasedGTU gtu, final List<Lane> lanes,
        final Length firstLanePosition, final Length distance, final Time startTime, final Speed startSpeed,
        final Speed endSpeed, final Acceleration maxAcceleration, final Acceleration maxDeceleration)
        throws OperationalPlanException, OTSGeometryException
    {
        OTSLine3D path = makePath(lanes, firstLanePosition, distance);
        OperationalPlan.Segment segment;
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
                    duration =
                        new Duration(Solver.firstSolutionAfter(0, acceleration.si / 2, startSpeed.si, -distance.si),
                            TimeUnit.SI);
                }
                if (acceleration.si > 0.0 && acceleration.gt(maxAcceleration))
                {
                    acceleration = maxAcceleration;
                    duration =
                        new Duration(Solver.firstSolutionAfter(0, acceleration.si / 2, startSpeed.si, -distance.si),
                            TimeUnit.SI);
                }
                segment = new OperationalPlan.AccelerationSegment(duration, acceleration);
            }
            catch (ValueException ve)
            {
                throw new OperationalPlanException(ve);
            }
        }
        ArrayList<OperationalPlan.Segment> segmentList = new ArrayList<>();
        segmentList.add(segment);
        return new LaneBasedOperationalPlan(gtu, path, startTime, startSpeed, segmentList, lanes);
    }

    /**
     * Build a plan with a path and a given start speed to try to reach a provided end speed, exactly at the end of the curve.
     * The acceleration (and deceleration) are capped by maxAcceleration and maxDeceleration. Therefore, there is no guarantee
     * that the end speed is actually reached by this plan.
     * @param lanes a list of connected Lanes to do the driving on
     * @param firstLanePosition position on the first lane with the reference point of the GTU
     * @param distance distance to drive for reaching the end speed
     * @return the driving path as a line
     * @throws OperationalPlanException when the length of the lanes is less than the distance when we start at the
     *             firstLanePosition on the first lane, or when the lanes list contains no elements
     * @throws OTSGeometryException in case the lanes are not connected or firstLanePosition is larger than the length of the
     *             first lane
     */
    public static OTSLine3D makePath(final List<Lane> lanes, final Length firstLanePosition, final Length distance)
        throws OperationalPlanException, OTSGeometryException
    {
        if (lanes.size() == 0)
        {
            throw new OperationalPlanException("LaneOperationalPlanBuilder.makePath got a lanes list with size = 0");
        }
        OTSLine3D path = lanes.get(0).getCenterLine().extract(firstLanePosition, lanes.get(0).getLength());
        for (int i = 1; i < lanes.size(); i++)
        {
            path = OTSLine3D.concatenate(0.15, path, lanes.get(i).getCenterLine());
        }
        return path.extract(0.0, distance.si);
    }

    /**
     * Build a plan with a path and a given start speed to reach a provided end speed, exactly at the end of the curve.
     * Acceleration and deceleration are virtually unbounded (1E12 m/s2) to reach the end speed (e.g., to come to a complete
     * stop).
     * @param gtu the GTU for debugging purposes
     * @param lanes a list of connected Lanes to do the driving on
     * @param firstLanePosition position on the first lane with the reference point of the GTU
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
    public static LaneBasedOperationalPlan buildGradualAccelerationPlan(final LaneBasedGTU gtu, final List<Lane> lanes,
        final Length firstLanePosition, final Length distance, final Time startTime, final Speed startSpeed,
        final Speed endSpeed) throws OperationalPlanException, OTSGeometryException
    {
        return buildGradualAccelerationPlan(gtu, lanes, firstLanePosition, distance, startTime, startSpeed, endSpeed,
            MAX_ACCELERATION, MAX_DECELERATION);
    }

    /**
     * Build a plan with a path and a given start speed to try to reach a provided end speed. Acceleration or deceleration is as
     * provided, until the end speed is reached. After this, constant end speed is used to reach the end point of the path.
     * There is no guarantee that the end speed is actually reached by this plan. If the end speed is zero, and it is reached
     * before completing the path, a truncated path that ends where the GTU stops is used instead.
     * @param gtu the GTU for debugging purposes
     * @param lanes a list of connected Lanes to do the driving on
     * @param firstLanePosition position on the first lane with the reference point of the GTU
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
    public static LaneBasedOperationalPlan buildMaximumAccelerationPlan(final LaneBasedGTU gtu, final List<Lane> lanes,
        final Length firstLanePosition, final Length distance, final Time startTime, final Speed startSpeed,
        final Speed endSpeed, final Acceleration acceleration, final Acceleration deceleration)
        throws OperationalPlanException, OTSGeometryException
    {
        OTSLine3D path = makePath(lanes, firstLanePosition, distance);
        ArrayList<OperationalPlan.Segment> segmentList = new ArrayList<>();
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
                                TimeUnit.SI);
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
                                TimeUnit.SI);
                        segmentList.add(new OperationalPlan.AccelerationSegment(duration, deceleration));
                    }
                    else
                    {
                        if (endSpeed.si == 0.0)
                        {
                            // if endSpeed == 0, we cannot reach the end of the path. Therefore, build a partial path.
                            OTSLine3D partialPath = path.truncate(x.si);
                            segmentList.add(new OperationalPlan.AccelerationSegment(t, deceleration));
                            return new LaneBasedOperationalPlan(gtu, partialPath, startTime, startSpeed, segmentList, lanes);
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
        return new LaneBasedOperationalPlan(gtu, path, startTime, startSpeed, segmentList, lanes);
    }

    /**
     * Build a plan with a path and a given start speed to try to reach a provided end speed. Acceleration or deceleration is as
     * provided, until the end speed is reached. After this, constant end speed is used to reach the end point of the path.
     * There is no guarantee that the end speed is actually reached by this plan. If the end speed is zero, and it is reached
     * before completing the path, a truncated path that ends where the GTU stops is used instead.
     * @param gtu the GTU for debugging purposes
     * @param lanes a list of connected Lanes to do the driving on
     * @param firstLanePosition position on the first lane with the reference point of the GTU
     * @param startTime the current time or a time in the future when the plan should start
     * @param startSpeed the speed at the start of the path
     * @param acceleration the acceleration to use
     * @param timeStep time step for the plan
     * @return the operational plan to accomplish the given end speed
     * @throws OperationalPlanException when the construction of the operational path fails
     * @throws OTSGeometryException in case the lanes are not connected or firstLanePositiion is larger than the length of the
     *             first lane
     */
    public static LaneBasedOperationalPlan buildAccelerationPlan(final LaneBasedGTU gtu, final List<Lane> lanes,
        final Length firstLanePosition, final Time startTime, final Speed startSpeed, final Acceleration acceleration,
        final Duration timeStep) throws OperationalPlanException, OTSGeometryException
    {
        
        if (startSpeed.eq(Speed.ZERO) && acceleration.le(Acceleration.ZERO))
        {
            // stand-still
            return new LaneBasedOperationalPlan(gtu, gtu.getLocation(), startTime, timeStep, lanes.get(0));
        }
        Length distance;
        ArrayList<OperationalPlan.Segment> segmentList = new ArrayList<>();
        if (startSpeed.plus(acceleration.multiplyBy(timeStep)).lt(Speed.ZERO))
        {
            // will reach stand-still within time step
            Duration brakingTime = startSpeed.divideBy(acceleration.multiplyBy(-1.0));
            segmentList.add(new OperationalPlan.AccelerationSegment(brakingTime, acceleration));
            segmentList.add(new OperationalPlan.SpeedSegment(timeStep.minus(brakingTime)));
            distance =
                new Length(startSpeed.si * brakingTime.si + .5 * acceleration.si * brakingTime.si * brakingTime.si,
                    LengthUnit.SI);
        }
        else
        {
            segmentList.add(new OperationalPlan.AccelerationSegment(timeStep, acceleration));
            distance =
                new Length(startSpeed.si * timeStep.si + .5 * acceleration.si * timeStep.si * timeStep.si, LengthUnit.SI);
        }
        OTSLine3D path;
        try
        {
            path = makePath(lanes, firstLanePosition, distance);
        }
        catch (Exception e)
        {
            path = makePath(lanes, firstLanePosition, distance);
            throw new Error("Bad!");
        }
        return new LaneBasedOperationalPlan(gtu, path, startTime, startSpeed, segmentList, lanes);
    }

    /**
     * Build a plan with a path and a given start speed to try to reach a provided end speed. Acceleration or deceleration is as
     * provided, until the end speed is reached. After this, constant end speed is used to reach the end point of the path.
     * There is no guarantee that the end speed is actually reached by this plan. If the end speed is zero, and it is reached
     * before completing the path, a truncated path that ends where the GTU stops is used instead.
     * @param gtu the GTU for debugging purposes
     * @param fromLanes lanes where the GTU changes from
     * @param laneChangeDirectionality direction of lane change
     * @param startPosition current position
     * @param startTime the current time or a time in the future when the plan should start
     * @param startSpeed the speed at the start of the path
     * @param acceleration the acceleration to use
     * @param timeStep time step for the plan
     * @param numberOfSteps total number of steps for the lane change
     * @param stepNumber current step of the lane change, starts at 0
     * @return the operational plan to accomplish the given end speed
     * @throws OperationalPlanException when the construction of the operational path fails
     * @throws OTSGeometryException in case the lanes are not connected or firstLanePositiion is larger than the length of the
     *             first lane
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public static LaneBasedOperationalPlan buildAccelerationLaneChangePlan(final LaneBasedGTU gtu,
        final List<Lane> fromLanes, final LateralDirectionality laneChangeDirectionality, final DirectedPoint startPosition,
        final Time startTime, final Speed startSpeed, final Acceleration acceleration, final Duration timeStep,
        final int numberOfSteps, final int stepNumber) throws OperationalPlanException, OTSGeometryException
    {
        Length fromLaneDistance =
            new Length(startSpeed.si * timeStep.si + .5 * acceleration.si * timeStep.si * timeStep.si, LengthUnit.SI);
        // TODO also for other driving directions, additional arguments in projectFractional?
        double fractionalLinkPositionFirst =
            fromLanes.get(0).getCenterLine().projectFractional(
                fromLanes.get(0).getParentLink().getStartNode().getDirection(),
                fromLanes.get(0).getParentLink().getEndNode().getDirection(), startPosition.x, startPosition.y);
        Length fromLaneFirstPosition = fromLanes.get(0).position(fractionalLinkPositionFirst);
        Length cumulDistance = fromLanes.get(0).getLength().minus(fromLaneFirstPosition);
        int lastLaneIndex = 0;
        while (cumulDistance.lt(fromLaneDistance))
        {
            lastLaneIndex++;
            cumulDistance = cumulDistance.plus(fromLanes.get(lastLaneIndex).getLength());
        }
        double fractionalLinkPositionLast =
            fromLanes.get(lastLaneIndex).getLength().minus(cumulDistance.minus(fromLaneDistance)).si
                / fromLanes.get(lastLaneIndex).getLength().si;

        List<Lane> toLanes = new ArrayList<>();
        for (Lane lane : fromLanes)
        {
            if (!lane.accessibleAdjacentLanes(laneChangeDirectionality, gtu.getGTUType()).isEmpty())
            {
                toLanes.add(lane.accessibleAdjacentLanes(laneChangeDirectionality, gtu.getGTUType()).iterator().next());
            }
            else
            {
                new Exception().printStackTrace();
                System.exit(-1);
            }
        }

        Length toLaneFirstPosition = toLanes.get(0).position(fractionalLinkPositionFirst);
        Length fromLaneLastPosition = fromLanes.get(lastLaneIndex).position(fractionalLinkPositionLast);
        Length toLaneLastPosition = toLanes.get(lastLaneIndex).position(fractionalLinkPositionLast);

        DirectedPoint fromFirst = fromLanes.get(0).getCenterLine().getLocation(fromLaneFirstPosition);
        DirectedPoint toFirst = toLanes.get(0).getCenterLine().getLocation(toLaneFirstPosition);
        DirectedPoint fromLast = fromLanes.get(lastLaneIndex).getCenterLine().getLocation(fromLaneLastPosition);
        DirectedPoint toLast = toLanes.get(lastLaneIndex).getCenterLine().getLocation(toLaneLastPosition);

        double lastFraction = (stepNumber + 1.0) / (numberOfSteps * 1.0);
        OTSPoint3D lastPoint =
            new OTSPoint3D(fromLast.x * (1 - lastFraction) + toLast.x * lastFraction, fromLast.y * (1 - lastFraction)
                + toLast.y * lastFraction, fromLast.z * (1 - lastFraction) + toLast.z * lastFraction);
        OTSPoint3D firstPoint = new OTSPoint3D(startPosition);
        OTSLine3D path = new OTSLine3D(firstPoint, lastPoint);

        double t = timeStep.si;
        Acceleration a = new Acceleration((2.0 * (path.getLength().si - startSpeed.si * t)) / (t * t), AccelerationUnit.SI);
        Speed endSpeed = startSpeed.plus(a.multiplyBy(timeStep));
        ArrayList<OperationalPlan.Segment> segmentList = new ArrayList<>();
        if (endSpeed.lt(Speed.ZERO))
        {
            Duration brakingTime = startSpeed.divideBy(acceleration.multiplyBy(-1.0));
            segmentList.add(new OperationalPlan.AccelerationSegment(brakingTime, acceleration));
            segmentList.add(new OperationalPlan.SpeedSegment(timeStep.minus(brakingTime)));
        }
        else
        {
            segmentList.add(new OperationalPlan.AccelerationSegment(timeStep, acceleration));
        }
        return new LaneBasedOperationalPlan(gtu, path, startTime, startSpeed, segmentList, fromLanes);
    }

    /**
     * Build a plan with a path and a given start speed to try to come to a stop with a given deceleration. If the GTU can stop
     * before completing the given path, a truncated path that ends where the GTU stops is used instead. There is no guarantee
     * that the OperationalPlan will lead to a complete stop.
     * @param gtu the GTU for debugging purposes
     * @param lanes a list of connected Lanes to do the driving on
     * @param firstLanePosition position on the first lane with the reference point of the GTU
     * @param distance distance to drive for reaching the end speed
     * @param startTime the current time or a time in the future when the plan should start
     * @param startSpeed the speed at the start of the path
     * @param deceleration the deceleration to use if endSpeed &lt; startSpeed, provided as a NEGATIVE number
     * @return the operational plan to accomplish the given end speed
     * @throws OperationalPlanException when construction of the operational path fails
     * @throws OTSGeometryException in case the lanes are not connected or firstLanePositiion is larger than the length of the
     *             first lane
     */
    public static LaneBasedOperationalPlan buildStopPlan(final LaneBasedGTU gtu, final List<Lane> lanes,
        final Length firstLanePosition, final Length distance, final Time startTime, final Speed startSpeed,
        final Acceleration deceleration) throws OperationalPlanException, OTSGeometryException
    {
        return buildMaximumAccelerationPlan(gtu, lanes, firstLanePosition, distance, startTime, startSpeed, new Speed(0.0,
            SpeedUnit.SI), new Acceleration(1.0, AccelerationUnit.SI), deceleration);
    }

    /*-
    public static OperationalPlan buildSpatialPlan(final LaneBasedGTU gtu, final Time startTime,
        final Acceleration startAcceleration, final Speed maxSpeed, final Duration duration,
        final List<LaneDirection> fromLanes, List<LaneDirection> toLanes, final CurvatureType curvatureType,
        final double laneChangeProgress, final DirectedPoint endPoint) throws OperationalPlanException
    {
        return buildSpatialPlan(gtu, startTime, gtu.getLocation(), gtu.getSpeed(), startAcceleration, maxSpeed, duration,
            fromLanes, toLanes, curvatureType, laneChangeProgress, endPoint);
    }

    public static OperationalPlan buildSpatialPlan(final LaneBasedGTU gtu, final Time startTime,
        final DirectedPoint startPoint, final Speed startSpeed, final Acceleration startAcceleration, final Speed maxSpeed,
        final Duration duration, final List<LaneDirection> fromLanes, List<LaneDirection> toLanes,
        final CurvatureType curvatureType, final double laneChangeProgress, final DirectedPoint endPoint)
        throws OperationalPlanException
    {

        // check
        checkLaneDirections(fromLanes, toLanes);

        // get start fractional position on link
        final CrossSectionLink startLink = fromLanes.get(0).getLane().getParentLink();
        Direction start;
        Direction end;
        if (fromLanes.get(0).getDirection() == GTUDirectionality.DIR_PLUS)
        {
            start = startLink.getStartNode().getDirection();
            end = startLink.getEndNode().getDirection();
        }
        else
        {
            start = startLink.getEndNode().getDirection();
            end = startLink.getStartNode().getDirection();
        }
        double fStart = startLink.getDesignLine().projectFractional(start, end, startPoint.x, startPoint.y);

        // get end fractional position on link, and end link
        double fEnd = 0;
        CrossSectionLink endLink = null;
        for (int i = 0; i < toLanes.size(); i++)
        {
            CrossSectionLink l = toLanes.get(i).getLane().getParentLink();
            if (toLanes.get(i).getDirection() == GTUDirectionality.DIR_PLUS)
            {
                start = l.getStartNode().getDirection();
                end = l.getEndNode().getDirection();
            }
            else
            {
                start = l.getEndNode().getDirection();
                end = l.getStartNode().getDirection();
            }
            fEnd = l.getDesignLine().projectFractional(start, end, endPoint.x, endPoint.y);
            if (fEnd > 0 && fEnd < 1)
            {
                endLink = l;
                break;
            }
        }
        Throw.when(endLink == null, OperationalPlanException.class, "End point cannot be projected to to-lanes.");

        // build from-line and to-line
        OTSLine3D from = null;
        OTSLine3D to = null;
        for (int i = 0; i < toLanes.size(); i++)
        {
            CrossSectionLink l = toLanes.get(i).getLane().getParentLink();
            try
            {
                if (l == startLink)
                {
                    from = fromLanes.get(i).getLane().getCenterLine().extractFractional(fStart, 1);
                    to = toLanes.get(i).getLane().getCenterLine().extractFractional(fStart, 1);
                }
                else if (l == endLink)
                {
                    from =
                        OTSLine3D.concatenate(from, fromLanes.get(i).getLane().getCenterLine().extractFractional(0, fEnd));
                    to = OTSLine3D.concatenate(to, toLanes.get(i).getLane().getCenterLine().extractFractional(0, fEnd));
                    break;
                }
                from = OTSLine3D.concatenate(from, fromLanes.get(i).getLane().getCenterLine());
                to = OTSLine3D.concatenate(to, toLanes.get(i).getLane().getCenterLine());
            }
            catch (OTSGeometryException exception)
            {
                throw new RuntimeException("Bug in buildSpatialPlan method.");
            }
        }

        // interpolate path
        List<OTSPoint3D> line = new ArrayList<>();
        line.add(new OTSPoint3D(startPoint.x, startPoint.y, startPoint.z));
        if (curvatureType.equals(CurvatureType.LINEAR))
        {
            int n = (int) Math.ceil(32 * (1.0 - laneChangeProgress));
            for (int i = 1; i < n; i++)
            {
                double fraction = 1.0 * i / n;
                double f0 = laneChangeProgress + (1.0 - laneChangeProgress) * fraction;
                double f1 = 1.0 - f0;
                DirectedPoint p1;
                DirectedPoint p2;
                try
                {
                    p1 = from.getLocationFraction(fraction);
                    p2 = to.getLocationFraction(fraction);
                }
                catch (OTSGeometryException exception)
                {
                    throw new RuntimeException("Bug in buildSpatialPlan method.");
                }
                line.add(new OTSPoint3D(p1.x * f1 + p2.x * f0, p1.y * f1 + p2.y * f0, p1.z * f1 + p2.z * f0));
            }
        }
        OTSLine3D path;
        try
        {
            path = new OTSLine3D(line);
        }
        catch (OTSGeometryException exception)
        {
            throw new RuntimeException("Bug in buildSpatialPlan method.");
        }

        // acceleration segments
        List<Segment> segmentList = new ArrayList<>();
        Acceleration b = startAcceleration.multiplyBy(-1.0);
        if (startSpeed.lt(b.multiplyBy(duration)))
        {
            // will reach zero speed within duration
            Duration d = startSpeed.divideBy(b);
            segmentList.add(new AccelerationSegment(d, startAcceleration)); // decelerate to zero
            segmentList.add(new SpeedSegment(duration.minus(d))); // stay at zero for the remainder of duration
        }
        else
        {
            segmentList.add(new AccelerationSegment(duration, startAcceleration));
        }

        return new OperationalPlan(gtu, path, startTime, startSpeed, segmentList);
    }

    public static OperationalPlan buildSpatialPlan(final LaneBasedGTU gtu, final Acceleration startAcceleration,
        final Speed maxSpeed, final List<LaneDirection> fromLanes, List<LaneDirection> toLanes,
        final CurvatureType curvatureType, final Duration duration) throws OperationalPlanException
    {
        return buildSpatialPlan(gtu, gtu.getLocation(), gtu.getSpeed(), startAcceleration, maxSpeed, fromLanes, toLanes,
            curvatureType, duration);
    }

    public static OperationalPlan buildSpatialPlan(final LaneBasedGTU gtu, final DirectedPoint startPoint,
        final Speed startSpeed, final Acceleration startAcceleration, final Speed maxSpeed,
        final List<LaneDirection> fromLanes, List<LaneDirection> toLanes, final CurvatureType curvatureType,
        final Duration duration) throws OperationalPlanException
    {
        checkLaneDirections(fromLanes, toLanes);

        return null;
    }

    private final static void checkLaneDirections(final List<LaneDirection> fromLanes, List<LaneDirection> toLanes)
        throws OperationalPlanException
    {
        Throw.when(fromLanes == null || toLanes == null, OperationalPlanException.class, "Lane lists may not be null.");
        Throw.when(fromLanes.isEmpty(), OperationalPlanException.class, "Lane lists may not be empty.");
        Throw.when(fromLanes.size() != toLanes.size(), OperationalPlanException.class,
            "Set of from lanes has different length than set of to lanes.");
        for (int i = 0; i < fromLanes.size(); i++)
        {
            Throw.when(!fromLanes.get(i).getLane().getParentLink().equals(toLanes.get(i).getLane().getParentLink()),
                OperationalPlanException.class,
                "A lane in the from-lanes list is not on the same link as the lane at equal index in the to-lanes list.");
        }
    }

    /**
     * Defines curvature.
     * <p>
     * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version May 27, 2016 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    /*-
    public enum CurvatureType
    {
        /** Linear lateral movement. */
    /*-
    LINEAR
    {
        public double[] getFractions(final double strartFraction)
        {
            return new double[1];
        }
    },

    /** */
    /*-
    SCURVE
    {
        public double[] getFractions(final double strartFraction)
        {
            return new double[1];
        }
    };

    public abstract double[] getFractions(double startFraction);
    }
     */

}
