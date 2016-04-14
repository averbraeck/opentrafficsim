package org.opentrafficsim.road.gtu.lane.tactical.following;

import java.io.Serializable;
import java.util.Collection;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.Headway;
import org.opentrafficsim.road.gtu.lane.tactical.AbstractLaneBasedTacticalPlanner;

/**
 * Code shared between various car following models. <br>
 * Note: many of the methods have a maxDistance, which may be "behind" the location of the next GTU, or stand-alone. Note that
 * the maxDistance is equivalent to a GTU with zero speed, and not equivalent to a moving GTU.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision: 1408 $, $LastChangedDate: 2015-09-24 15:17:25 +0200 (Thu, 24 Sep 2015) $, by $Author: pknoppers $,
 *          initial version 19 feb. 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public abstract class AbstractGTUFollowingModelMobil implements GTUFollowingModelOld, Serializable
{
    /** */
    private static final long serialVersionUID = 20150219L;

    /** Prohibitive deceleration used to construct the TOODANGEROUS result below. */
    private static final AccelerationStep PROHIBITIVEACCELERATIONSTEP = new AccelerationStep(new Acceleration(
            Double.NEGATIVE_INFINITY, AccelerationUnit.SI), new Time.Abs(Double.NaN, TimeUnit.SI), new Time.Rel(Double.NaN,
            TimeUnit.SI));

    /** Return value if lane change causes immediate collision. */
    public static final DualAccelerationStep TOODANGEROUS = new DualAccelerationStep(PROHIBITIVEACCELERATIONSTEP,
            PROHIBITIVEACCELERATIONSTEP);

    /** {@inheritDoc} */
    @Override
    public final DualAccelerationStep computeDualAccelerationStep(final LaneBasedGTU referenceGTU,
            final Collection<Headway> otherGTUs, final Length.Rel maxDistance, final Speed speedLimit) throws GTUException
    {
        return computeDualAccelerationStep(referenceGTU, otherGTUs, maxDistance, speedLimit, getStepSize());
    }

    /** {@inheritDoc} */
    @Override
    public final DualAccelerationStep computeDualAccelerationStep(final LaneBasedGTU referenceGTU,
            final Collection<Headway> otherHeadways, final Length.Rel maxDistance, final Speed speedLimit,
            final Time.Rel stepSize) throws GTUException
    {
        // Find out if there is an immediate collision
        for (Headway headway : otherHeadways)
        {
            if (!headway.getId().equals(referenceGTU.getId()) && null == headway.getDistance())
            {
                return TOODANGEROUS;
            }
        }
        AccelerationStep followerAccelerationStep = null;
        AccelerationStep referenceGTUAccelerationStep = null;
        GTUFollowingModelOld gfm =
                (GTUFollowingModelOld) ((AbstractLaneBasedTacticalPlanner) referenceGTU.getTacticalPlanner())
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
                AccelerationStep as =
                        gfm.computeAccelerationStep(headway.getSpeed(), referenceGTU.getVelocity(), new Length.Rel(
                                -headway.getDistance().si, LengthUnit.SI), speedLimit, referenceGTU.getSimulator()
                                .getSimulatorTime().getTime(), stepSize);
                if (null == followerAccelerationStep || as.getAcceleration().lt(followerAccelerationStep.getAcceleration()))
                {
                    followerAccelerationStep = as;
                }
            }
            else
            {
                // This one is ahead
                AccelerationStep as =
                        gfm.computeAccelerationStep(referenceGTU, headway.getSpeed(), headway.getDistance(), maxDistance,
                                speedLimit, stepSize);
                if (null == referenceGTUAccelerationStep
                        || as.getAcceleration().lt(referenceGTUAccelerationStep.getAcceleration()))
                {
                    referenceGTUAccelerationStep = as;
                }
            }
            // if (referenceGTU.getId().contains("39"))
            // {
            // System.out.println("39 - fas=" + followerAccelerationStep + ", ras=" + referenceGTUAccelerationStep);
            // }
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
    public final AccelerationStep computeAccelerationStep(final LaneBasedGTU gtu, final Speed leaderSpeed,
            final Length.Rel headway, final Length.Rel maxDistance, final Speed speedLimit) throws GTUException
    {
        return computeAccelerationStep(gtu, leaderSpeed, headway, maxDistance, speedLimit, getStepSize());
    }

    /** {@inheritDoc} */
    @Override
    public final AccelerationStep computeAccelerationStep(final LaneBasedGTU gtu, final Speed leaderSpeed,
            final Length.Rel headway, final Length.Rel maxDistance, final Speed speedLimit, final Time.Rel stepSize)
            throws GTUException
    {
        Length.Rel distance;
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
        final Speed followerSpeed = gtu.getVelocity();
        final Speed followerMaximumSpeed = gtu.getMaximumVelocity();
        Acceleration newAcceleration =
                computeAcceleration(followerSpeed, followerMaximumSpeed, leaderOrBlockSpeed, distance, speedLimit, stepSize);
        Time.Abs nextEvaluationTime = gtu.getSimulator().getSimulatorTime().getTime().plus(stepSize);
        return new AccelerationStep(newAcceleration, nextEvaluationTime, stepSize);
    }

    /** {@inheritDoc} */
    @Override
    public final AccelerationStep computeAccelerationStep(final Speed followerSpeed, final Speed leaderSpeed,
            final Length.Rel headway, final Speed speedLimit, final Time.Abs currentTime)
    {
        return computeAccelerationStep(followerSpeed, leaderSpeed, headway, speedLimit, currentTime, getStepSize());
    }

    /** {@inheritDoc} */
    @Override
    public final AccelerationStep computeAccelerationStep(final Speed followerSpeed, final Speed leaderSpeed,
            final Length.Rel headway, final Speed speedLimit, final Time.Abs currentTime, final Time.Rel stepSize)
    {
        final Speed followerMaximumSpeed = speedLimit; // the best approximation we can do...
        Acceleration newAcceleration =
                computeAcceleration(followerSpeed, followerMaximumSpeed, leaderSpeed, headway, speedLimit, stepSize);
        Time.Abs nextEvaluationTime = currentTime.plus(stepSize);
        return new AccelerationStep(newAcceleration, nextEvaluationTime, stepSize);
    }

    /** {@inheritDoc} */
    @Override
    public final AccelerationStep computeAccelerationStepWithNoLeader(final LaneBasedGTU gtu, final Length.Rel maxDistance,
            final Speed speedLimit) throws GTUException
    {
        return computeAccelerationStepWithNoLeader(gtu, maxDistance, speedLimit, getStepSize());
    }

    /** {@inheritDoc} */
    @Override
    public final AccelerationStep computeAccelerationStepWithNoLeader(final LaneBasedGTU gtu, final Length.Rel maxDistance,
            final Speed speedLimit, final Time.Rel stepSize) throws GTUException
    {
        Length.Rel stopDistance =
                new Length.Rel(gtu.getMaximumVelocity().si * gtu.getMaximumVelocity().si
                        / (2.0 * getMaximumSafeDeceleration().si), LengthUnit.SI);
        return computeAccelerationStep(gtu, gtu.getVelocity(), stopDistance, maxDistance, speedLimit, stepSize);
        /*-
        return computeAcceleration(gtu, gtu.getVelocity(), Calc.speedSquaredDividedByDoubleAcceleration(gtu
            .getMaximumVelocity(), maximumSafeDeceleration()), speedLimit);
         */
    }

    /** {@inheritDoc} */
    @Override
    public final Length.Rel minimumHeadway(final Speed followerSpeed, final Speed leaderSpeed, final Length.Rel precision,
            final Length.Rel maxDistance, final Speed speedLimit, final Speed followerMaximumSpeed)
    {
        if (precision.getSI() <= 0)
        {
            throw new Error("Precision has bad value (must be > 0; got " + precision + ")");
        }
        double maximumDeceleration = -getMaximumSafeDeceleration().getSI();
        // Find a decent interval to bisect
        double minimumSI = 0;
        double minimumSIDeceleration =
                computeAcceleration(followerSpeed, followerSpeed, leaderSpeed, new Length.Rel(minimumSI, LengthUnit.SI),
                        speedLimit).getSI();
        if (minimumSIDeceleration >= maximumDeceleration)
        {
            // Weird... The GTU following model allows zero headway
            return Length.Rel.ZERO;
        }
        double maximumSI = 1; // this is - deliberately - way too small
        double maximumSIDeceleration = Double.NaN;
        // Double the value of maximumSI until the resulting deceleration is less severe than the maximum
        for (int step = 0; step < 20; step++)
        {
            maximumSIDeceleration =
                    computeAcceleration(followerSpeed, followerMaximumSpeed, leaderSpeed,
                            new Length.Rel(maximumSI, LengthUnit.SI), speedLimit).getSI();
            if (maximumSIDeceleration > maximumDeceleration)
            {
                break;
            }
            maximumSI *= 2;
        }
        if (maximumSIDeceleration < maximumDeceleration)
        {
            System.out.println();
            maximumSIDeceleration =
                    computeAcceleration(followerSpeed, followerMaximumSpeed, leaderSpeed,
                            new Length.Rel(maximumSI, LengthUnit.SI), speedLimit).getSI();
            throw new Error("Cannot find headway that results in an acceptable deceleration");
        }
        // Now bisect until the error is less than the requested precision
        final int maximumStep = (int) Math.ceil(Math.log((maximumSI - minimumSI) / precision.getSI()) / Math.log(2));
        for (int step = 0; step < maximumStep; step++)
        {
            double midSI = (minimumSI + maximumSI) / 2;
            double midSIAcceleration =
                    computeAcceleration(followerSpeed, followerMaximumSpeed, leaderSpeed, new Length.Rel(midSI, LengthUnit.SI),
                            speedLimit).getSI();
            if (midSIAcceleration < maximumDeceleration)
            {
                minimumSI = midSI;
            }
            else
            {
                maximumSI = midSI;
            }
        }
        Length.Rel result = new Length.Rel(Math.min((minimumSI + maximumSI) / 2, maxDistance.si), LengthUnit.SI);
        return result;
    }
}
