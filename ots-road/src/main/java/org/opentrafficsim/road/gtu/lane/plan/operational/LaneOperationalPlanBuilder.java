package org.opentrafficsim.road.gtu.lane.plan.operational;

import java.util.ArrayList;
import java.util.List;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.ValueException;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan.SpeedSegment;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.math.Solver;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * Builder for several often used operational plans, based on a list of lanes. E.g., decelerate to come to a full stop at the
 * end of a shape; accelerate to reach a certain speed at the end of a curve; drive constant on a curve; decelerate or
 * accelerate to reach a given end speed at the end of a curve, etc.<br>
 * TODO driving with negative speeds (backward driving) is not yet supported.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
        final Length.Rel firstLanePosition, final Length.Rel distance, final Time.Abs startTime,
        final Speed startSpeed, final Speed endSpeed, final Acceleration maxAcceleration,
        final Acceleration maxDeceleration) throws OperationalPlanException, OTSGeometryException
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
                Time.Rel duration = distance.multiplyBy(2.0).divideBy(endSpeed.plus(startSpeed));
                Acceleration acceleration = endSpeed.minus(startSpeed).divideBy(duration);
                if (acceleration.si < 0.0 && acceleration.lt(maxDeceleration))
                {
                    acceleration = maxDeceleration;
                    duration =
                        new Time.Rel(Solver.firstSolutionAfter(0, acceleration.si / 2, startSpeed.si, -distance.si),
                            TimeUnit.SI);
                }
                if (acceleration.si > 0.0 && acceleration.gt(maxAcceleration))
                {
                    acceleration = maxAcceleration;
                    duration =
                        new Time.Rel(Solver.firstSolutionAfter(0, acceleration.si / 2, startSpeed.si, -distance.si),
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
    public static OTSLine3D makePath(final List<Lane> lanes,
        final Length.Rel firstLanePosition, final Length.Rel distance) throws OperationalPlanException,
        OTSGeometryException
    {
        if (lanes.size() == 0)
        {
            throw new OperationalPlanException("LaneOperationalPlanBuilder.makePath got a lanes list with size = 0");
        }
        OTSLine3D path = lanes.get(0).getCenterLine().extract(firstLanePosition, lanes.get(0).getLength());
        for (int i = 1; i < lanes.size(); i++)
        {
            path = OTSLine3D.concatenate(path, lanes.get(i).getCenterLine());
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
        final Length.Rel firstLanePosition, final Length.Rel distance, final Time.Abs startTime,
        final Speed startSpeed, final Speed endSpeed) throws OperationalPlanException, OTSGeometryException
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
        final Length.Rel firstLanePosition, final Length.Rel distance, final Time.Abs startTime,
        final Speed startSpeed, final Speed endSpeed, final Acceleration acceleration, final Acceleration deceleration)
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
                    Time.Rel t = endSpeed.minus(startSpeed).divideBy(acceleration);
                    Length.Rel x =
                        startSpeed.multiplyBy(t).plus(acceleration.multiplyBy(0.5).multiplyBy(t).multiplyBy(t));
                    if (x.ge(distance))
                    {
                        // we cannot reach the end speed in the given distance with the given acceleration
                        Time.Rel duration =
                            new Time.Rel(
                                Solver.firstSolutionAfter(0, acceleration.si / 2, startSpeed.si, -distance.si),
                                TimeUnit.SI);
                        segmentList.add(new OperationalPlan.AccelerationSegment(duration, acceleration));
                    }
                    else
                    {
                        // we reach the (higher) end speed before the end of the segment. Make two segments.
                        segmentList.add(new OperationalPlan.AccelerationSegment(t, acceleration));
                        Time.Rel duration = distance.minus(x).divideBy(endSpeed);
                        segmentList.add(new OperationalPlan.SpeedSegment(duration));
                    }
                }
                else
                {
                    Time.Rel t = endSpeed.minus(startSpeed).divideBy(deceleration);
                    Length.Rel x =
                        startSpeed.multiplyBy(t).plus(deceleration.multiplyBy(0.5).multiplyBy(t).multiplyBy(t));
                    if (x.ge(distance))
                    {
                        // we cannot reach the end speed in the given distance with the given deceleration
                        Time.Rel duration =
                            new Time.Rel(
                                Solver.firstSolutionAfter(0, deceleration.si / 2, startSpeed.si, -distance.si),
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
                            return new LaneBasedOperationalPlan(gtu, partialPath, startTime, startSpeed, segmentList,
                                lanes);
                        }
                        // we reach the (lower) end speed, larger than zero, before the end of the segment. Make two segments.
                        segmentList.add(new OperationalPlan.AccelerationSegment(t, deceleration));
                        Time.Rel duration = distance.minus(x).divideBy(endSpeed);
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
        final Length.Rel firstLanePosition, final Length.Rel distance, final Time.Abs startTime,
        final Speed startSpeed, final Acceleration deceleration) throws OperationalPlanException, OTSGeometryException
    {
        return buildMaximumAccelerationPlan(gtu, lanes, firstLanePosition, distance, startTime, startSpeed, new Speed(
            0.0, SpeedUnit.SI), new Acceleration(1.0, AccelerationUnit.SI), deceleration);
    }
}
