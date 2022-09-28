package org.opentrafficsim.road.gtu.lane.tactical.following;

import java.util.Collection;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;
import org.opentrafficsim.road.gtu.lane.tactical.AbstractLaneBasedTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedTacticalPlanner;

/**
 * Code shared between various car following models. <br>
 * Note: many of the methods have a maxDistance, which may be "behind" the location of the next GTU, or stand-alone. Note that
 * the maxDistance is equivalent to a GTU with zero speed, and not equivalent to a moving GTU.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public abstract class AbstractGtuFollowingModelMobil implements GtuFollowingModelOld
{

    /** Prohibitive deceleration used to construct the TOODANGEROUS result below. */
    private static final AccelerationStep PROHIBITIVEACCELERATIONSTEP =
            new AccelerationStep(new Acceleration(Double.NEGATIVE_INFINITY, AccelerationUnit.SI),
                    new Time(Double.NaN, TimeUnit.DEFAULT), new Duration(Double.NaN, DurationUnit.SI));

    /** Return value if lane change causes immediate collision. */
    public static final DualAccelerationStep TOODANGEROUS =
            new DualAccelerationStep(PROHIBITIVEACCELERATIONSTEP, PROHIBITIVEACCELERATIONSTEP);

    /** {@inheritDoc} */
    @Override
    public final DualAccelerationStep computeDualAccelerationStep(final LaneBasedGtu referenceGTU,
            final Collection<Headway> otherGTUs, final Length maxDistance, final Speed speedLimit) throws GtuException
    {
        return computeDualAccelerationStep(referenceGTU, otherGTUs, maxDistance, speedLimit, getStepSize());
    }

    /** {@inheritDoc} */
    @Override
    public final DualAccelerationStep computeDualAccelerationStep(final LaneBasedGtu referenceGTU,
            final Collection<Headway> otherHeadways, final Length maxDistance, final Speed speedLimit, final Duration stepSize)
            throws GtuException
    {
        // Find out if there is an immediate collision
        for (Headway headway : otherHeadways)
        {
            // XXX: Under which circumstances can getDistance() be NULL? Should that indeed result in TOODANGEROUS?
            if (headway.getDistance() == null)
            {
                return TOODANGEROUS;
            }
            if (!headway.getId().equals(referenceGTU.getId()) && Double.isNaN(headway.getDistance().si))
            {
                return TOODANGEROUS;
            }
        }
        AccelerationStep followerAccelerationStep = null;
        AccelerationStep referenceGTUAccelerationStep = null;
        LaneBasedTacticalPlanner tp = referenceGTU.getTacticalPlanner();
        if (null == tp)
        {
            referenceGTU.getTacticalPlanner();
            System.err.println("tactical planner is null");
        }
        GtuFollowingModelOld gfm = (GtuFollowingModelOld) ((AbstractLaneBasedTacticalPlanner) referenceGTU.getTacticalPlanner())
                .getCarFollowingModel();
        // Find the leader and the follower that cause/experience the least positive (most negative) acceleration.
        for (Headway headway : otherHeadways)
        {
            if (headway.getId().equals(referenceGTU.getId()))
            {
                continue;
            }
            if (headway.getDistance().si < 0)
            {
                // This one is behind; assume our CFM holds also for the GTU behind us
                AccelerationStep as = gfm.computeAccelerationStep(headway.getSpeed(), referenceGTU.getSpeed(),
                        new Length(-headway.getDistance().si, LengthUnit.SI), speedLimit,
                        referenceGTU.getSimulator().getSimulatorAbsTime(), stepSize);
                if (null == followerAccelerationStep || as.getAcceleration().lt(followerAccelerationStep.getAcceleration()))
                {
                    followerAccelerationStep = as;
                }
            }
            else
            {
                // This one is ahead
                AccelerationStep as = gfm.computeAccelerationStep(referenceGTU, headway.getSpeed(), headway.getDistance(),
                        maxDistance, speedLimit, stepSize);
                if (null == referenceGTUAccelerationStep
                        || as.getAcceleration().lt(referenceGTUAccelerationStep.getAcceleration()))
                {
                    referenceGTUAccelerationStep = as;
                }
            }
        }
        if (null == followerAccelerationStep)
        {
            followerAccelerationStep = gfm.computeAccelerationStepWithNoLeader(referenceGTU, maxDistance, speedLimit, stepSize);
        }
        if (null == referenceGTUAccelerationStep)
        {
            referenceGTUAccelerationStep =
                    gfm.computeAccelerationStepWithNoLeader(referenceGTU, maxDistance, speedLimit, stepSize);
        }
        return new DualAccelerationStep(referenceGTUAccelerationStep, followerAccelerationStep);
    }

    /** {@inheritDoc} */
    @Override
    public final AccelerationStep computeAccelerationStep(final LaneBasedGtu gtu, final Speed leaderSpeed, final Length headway,
            final Length maxDistance, final Speed speedLimit) throws GtuException
    {
        return computeAccelerationStep(gtu, leaderSpeed, headway, maxDistance, speedLimit, getStepSize());
    }

    /** {@inheritDoc} */
    @Override
    public final AccelerationStep computeAccelerationStep(final LaneBasedGtu gtu, final Speed leaderSpeed, final Length headway,
            final Length maxDistance, final Speed speedLimit, final Duration stepSize) throws GtuException
    {
        Length distance;
        Speed leaderOrBlockSpeed;
        if (maxDistance.lt(headway) || headway == null)
        {
            distance = maxDistance;
            leaderOrBlockSpeed = Speed.ZERO;
        }
        else
        {
            distance = headway;
            leaderOrBlockSpeed = leaderSpeed;
        }
        final Speed followerSpeed = gtu.getSpeed();
        final Speed followerMaximumSpeed = gtu.getMaximumSpeed();
        Acceleration newAcceleration =
                computeAcceleration(followerSpeed, followerMaximumSpeed, leaderOrBlockSpeed, distance, speedLimit, stepSize);
        Time nextEvaluationTime = gtu.getSimulator().getSimulatorAbsTime().plus(stepSize);
        return new AccelerationStep(newAcceleration, nextEvaluationTime, stepSize);
    }

    /** {@inheritDoc} */
    @Override
    public final AccelerationStep computeAccelerationStep(final Speed followerSpeed, final Speed leaderSpeed,
            final Length headway, final Speed speedLimit, final Time currentTime)
    {
        return computeAccelerationStep(followerSpeed, leaderSpeed, headway, speedLimit, currentTime, getStepSize());
    }

    /** {@inheritDoc} */
    @Override
    public final AccelerationStep computeAccelerationStep(final Speed followerSpeed, final Speed leaderSpeed,
            final Length headway, final Speed speedLimit, final Time currentTime, final Duration stepSize)
    {
        final Speed followerMaximumSpeed = speedLimit; // the best approximation we can do...
        Acceleration newAcceleration =
                computeAcceleration(followerSpeed, followerMaximumSpeed, leaderSpeed, headway, speedLimit, stepSize);
        Time nextEvaluationTime = currentTime.plus(stepSize);
        return new AccelerationStep(newAcceleration, nextEvaluationTime, stepSize);
    }

    /** {@inheritDoc} */
    @Override
    public final AccelerationStep computeAccelerationStepWithNoLeader(final LaneBasedGtu gtu, final Length maxDistance,
            final Speed speedLimit) throws GtuException
    {
        return computeAccelerationStepWithNoLeader(gtu, maxDistance, speedLimit, getStepSize());
    }

    /** {@inheritDoc} */
    @Override
    public final AccelerationStep computeAccelerationStepWithNoLeader(final LaneBasedGtu gtu, final Length maxDistance,
            final Speed speedLimit, final Duration stepSize) throws GtuException
    {
        Length stopDistance = new Length(
                gtu.getMaximumSpeed().si * gtu.getMaximumSpeed().si / (2.0 * getMaximumSafeDeceleration().si), LengthUnit.SI);
        return computeAccelerationStep(gtu, gtu.getSpeed(), stopDistance, maxDistance, speedLimit, stepSize);
        /*-
        return computeAcceleration(gtu, gtu.getSpeed(), Calc.speedSquaredDividedByDoubleAcceleration(gtu
            .getMaximumSpeed(), maximumSafeDeceleration()), speedLimit);
         */
    }

    /** {@inheritDoc} */
    @Override
    public final Length minimumHeadway(final Speed followerSpeed, final Speed leaderSpeed, final Length precision,
            final Length maxDistance, final Speed speedLimit, final Speed followerMaximumSpeed)
    {
        if (precision.getSI() <= 0)
        {
            throw new Error("Precision has bad value (must be > 0; got " + precision + ")");
        }
        double maximumDeceleration = -getMaximumSafeDeceleration().getSI();
        // Find a decent interval to bisect
        double minimumSI = 0;
        double minimumSIDeceleration =
                computeAcceleration(followerSpeed, followerSpeed, leaderSpeed, new Length(minimumSI, LengthUnit.SI), speedLimit)
                        .getSI();
        if (minimumSIDeceleration >= maximumDeceleration)
        {
            // Weird... The GTU following model allows zero headway
            return Length.ZERO;
        }
        double maximumSI = 1; // this is - deliberately - way too small
        double maximumSIDeceleration = Double.NaN;
        // Double the value of maximumSI until the resulting deceleration is less severe than the maximum
        for (int step = 0; step < 20; step++)
        {
            maximumSIDeceleration = computeAcceleration(followerSpeed, followerMaximumSpeed, leaderSpeed,
                    new Length(maximumSI, LengthUnit.SI), speedLimit).getSI();
            if (maximumSIDeceleration > maximumDeceleration)
            {
                break;
            }
            maximumSI *= 2;
        }
        if (maximumSIDeceleration < maximumDeceleration)
        {
            System.out.println();
            maximumSIDeceleration = computeAcceleration(followerSpeed, followerMaximumSpeed, leaderSpeed,
                    new Length(maximumSI, LengthUnit.SI), speedLimit).getSI();
            throw new Error("Cannot find headway that results in an acceptable deceleration");
        }
        // Now bisect until the error is less than the requested precision
        final int maximumStep = (int) Math.ceil(Math.log((maximumSI - minimumSI) / precision.getSI()) / Math.log(2));
        for (int step = 0; step < maximumStep; step++)
        {
            double midSI = (minimumSI + maximumSI) / 2;
            double midSIAcceleration = computeAcceleration(followerSpeed, followerMaximumSpeed, leaderSpeed,
                    new Length(midSI, LengthUnit.SI), speedLimit).getSI();
            if (midSIAcceleration < maximumDeceleration)
            {
                minimumSI = midSI;
            }
            else
            {
                maximumSI = midSI;
            }
        }
        Length result = new Length(Math.min((minimumSI + maximumSI) / 2, maxDistance.si), LengthUnit.SI);
        return result;
    }

}
