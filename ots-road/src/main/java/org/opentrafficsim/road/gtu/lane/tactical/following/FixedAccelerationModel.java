package org.opentrafficsim.road.gtu.lane.tactical.following;

import java.io.Serializable;

import org.djunits.unit.AccelerationUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionIterable;
import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

/**
 * Fixed GTU following model. This GTU following model does not react in any way to other GTUs. Instead it has a predetermined
 * acceleration for a predetermined duration.<br>
 * Primary use is testing of lane based GTU movement.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class FixedAccelerationModel extends AbstractGTUFollowingModelMobil implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150206L;

    /** Acceleration that will be returned in GTUFollowingModelResult by computeAcceleration. */
    private Acceleration acceleration;

    /** Valid until time that will be returned in GTUFollowingModelResult by computeAcceleration. */
    private Duration duration;

    /**
     * Create a new FixedAccelerationModel.
     * @param acceleration Acceleration; the acceleration that will be returned by the computeAcceleration methods
     * @param duration Duration; the duration that the acceleration will be maintained
     */
    public FixedAccelerationModel(final Acceleration acceleration, final Duration duration)
    {
        this.acceleration = acceleration;
        this.duration = duration;
    }

    /**
     * Retrieve the duration of this FixedAccelerationModel.
     * @return Duration; the duration of this FixedAccelerationModel
     */
    public final Duration getDuration()
    {
        return this.duration;
    }

    /**
     * Retrieve the acceleration of this FixedAccelerationModel.
     * @return Acceleration; the acceleration of this FixedAccelerationModel
     */
    public final Acceleration getAcceleration()
    {
        return this.acceleration;
    }

    /** {@inheritDoc} */
    @Override
    public final Acceleration computeAcceleration(final Speed followerSpeed, final Speed followerMaximumSpeed,
            final Speed leaderSpeed, final Length headway, final Speed speedLimit, final Duration stepSize)
    {
        return this.acceleration;
    }

    /** {@inheritDoc} */
    @Override
    public final Acceleration computeAcceleration(final Speed followerSpeed, final Speed followerMaximumSpeed,
            final Speed leaderSpeed, final Length headway, final Speed speedLimit)
    {
        return this.acceleration;
    }

    /** {@inheritDoc} */
    @Override
    public final Acceleration getMaximumSafeDeceleration()
    {
        // TODO should be specified in constructor
        return new Acceleration(2, AccelerationUnit.METER_PER_SECOND_2);
    }

    /** {@inheritDoc} */
    @Override
    public final Duration getStepSize()
    {
        return this.duration;
    }

    /** {@inheritDoc} */
    @Override
    public final String getName()
    {
        return "Fixed";
    }

    /** {@inheritDoc} */
    @Override
    public final String getLongName()
    {
        return "Fixed GTU following model";
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "FixedAccelerationModel " + this.duration + ", " + this.acceleration;
    }

    /** {@inheritDoc} */
    @Override
    public final void setA(final Acceleration a)
    {
        //
    }

    /** {@inheritDoc} */
    @Override
    public final void setT(final Duration t)
    {
        //
    }

    /** {@inheritDoc} */
    @Override
    public final void setFspeed(final double fSpeed)
    {
        //
    }

    // The following is inherited from CarFollowingModel

    /** {@inheritDoc} */
    @Override
    public final Speed desiredSpeed(final Parameters parameters, final SpeedLimitInfo speedInfo) throws ParameterException
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final Length desiredHeadway(final Parameters parameters, final Speed speed) throws ParameterException
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final Acceleration followingAcceleration(final Parameters parameters, final Speed speed,
            final SpeedLimitInfo speedInfo, final PerceptionIterable<? extends Headway> leaders) throws ParameterException
    {
        return null;
    }

}
