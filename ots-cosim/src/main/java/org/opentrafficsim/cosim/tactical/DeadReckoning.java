package org.opentrafficsim.cosim.tactical;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.draw.point.DirectedPoint2d;
import org.opentrafficsim.base.geometry.OtsGeometryUtil;
import org.opentrafficsim.base.geometry.OtsLine2d;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan;
import org.opentrafficsim.core.gtu.plan.operational.Segments;
import org.opentrafficsim.road.gtu.LaneBasedGtu;
import org.opentrafficsim.road.gtu.operational.LaneOperationalPlanBuilder;

/**
 * DeadReckoning.java.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.<br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * @author Wouter Schakel
 */
public class DeadReckoning
{

    /** GTU. */
    private final LaneBasedGtu gtu;

    /** Duration to extrapolate dead reckoning. */
    private Duration horizon;

    /** Duration since start of simulation when the dead reckoning location applies. */
    private Duration time;

    /** Location for dead reckoning. */
    private DirectedPoint2d location;

    /** Speed for dead reckoning. */
    private Speed speed;

    /** Acceleration for dead reckoning. */
    private Acceleration acceleration;

    /**
     * Constructor.
     * @param gtu GTU
     */
    public DeadReckoning(final LaneBasedGtu gtu)
    {
        this.gtu = gtu;
    }

    /**
     * Sets the dead reckoning horizon.
     * @param horizon horizon
     */
    public void setHorizon(final Duration horizon)
    {
        this.horizon = horizon;
    }

    /**
     * Sets time, location, speed and acceleration for dead reckoning.
     * @param time duration since start of simulation when the location applies
     * @param location location
     * @param speed speed
     * @param accel acceleration
     */
    @SuppressWarnings("hiddenfield")
    public void setInformation(final Duration time, final DirectedPoint2d location, final Speed speed, final Acceleration accel)
    {
        this.time = time;
        this.location = location;
        this.speed = speed;
        this.acceleration = accel;
    }

    /**
     * Generates an operation plan based on dead reckoning. Dead reckoning is performed by looking a time horizon in to the
     * future. Based on the direction of the last known location, a target point is derived some distance ahead in this
     * direction. The distance is based on the kinematics of speed and acceleration over the time horizon. The path is
     * determined as a Bezier to the target point. Acceleration is adjusted to move the length of this path over the time
     * horizon. Consequently, dead reckoning aims to reach the target point after the time horizon.
     * <p>
     * To prevent strong lateral moves as the current GTU position is slightly drifted to the side relative to the last given
     * position, a minimum distance to determine the target point of 1m is applied. This only applies when speed is very low. As
     * the target point in this case is further away than the kinematic distance would suggest, the acceleration is not adjusted
     * to the length of the path, but taken as given.
     * @param startTime start time for the operational plan
     * @param locationAtStartTime start location at start time
     * @return Operation plan based on dead reckoning
     */
    public OperationalPlan getPlan(final Duration startTime, final DirectedPoint2d locationAtStartTime)
    {
        double tHorizon = Math.max(0.0, startTime.si - this.time.si) + this.horizon.si;
        double tMove = this.acceleration.si < 0.0 ? Math.min(tHorizon, this.speed.si / -this.acceleration.si) : tHorizon;
        double sHorizon = this.speed.si * tMove + .5 * this.acceleration.si * tMove * tMove;
        Speed v0 = this.gtu.getSpeed();
        double sMin = 1.0;
        OtsLine2d path;
        Acceleration aAdjusted;
        if (sHorizon >= sMin)
        {
            DirectedPoint2d target = OtsGeometryUtil.translatePoint(this.location, sHorizon);
            path = new OtsLine2d(LaneOperationalPlanBuilder.bezierToTarget(locationAtStartTime, target));
            double pathLength = path.getLength();
            // do not drift
            if (pathLength < 1e-3)
            {
                pathLength = 0.0;
            }
            aAdjusted = Acceleration.ofSI(2.0 * (pathLength - v0.si * tMove) / (tMove * tMove));
        }
        else
        {
            // create path to target that is beyond the kinematics distance
            DirectedPoint2d target = OtsGeometryUtil.translatePoint(this.location, sMin);
            path = new OtsLine2d(LaneOperationalPlanBuilder.bezierToTarget(locationAtStartTime, target));
            aAdjusted = this.acceleration; // not adjusted as path is longer than kinematic distance would suggest
        }
        // Segments.off() takes care of standstill
        return new OperationalPlan(this.gtu, path, startTime, Segments.off(v0, this.horizon, aAdjusted));
    }

}
