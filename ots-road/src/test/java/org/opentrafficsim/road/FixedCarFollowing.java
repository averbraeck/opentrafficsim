package org.opentrafficsim.road;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterSet;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.road.FixedCarFollowing.FixedCarFollowingModel;
import org.opentrafficsim.road.gtu.perception.PerceptionIterable;
import org.opentrafficsim.road.gtu.perception.object.PerceivedObject;
import org.opentrafficsim.road.gtu.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.tactical.following.CarFollowingModelFactory;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

/**
 * Very simple car-following model factory only for testing.
 * <p>
 * Copyright (c) 2024-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class FixedCarFollowing implements CarFollowingModelFactory<FixedCarFollowingModel>
{
    /** Fixed acceleration. */
    private final Acceleration acceleration;

    /** Fixed desired speed. */
    private final Speed desiredSpeed;

    /** Fixed desired headway. */
    private final Length desiredHeadway;

    /**
     * Constructor.
     */
    public FixedCarFollowing()
    {
        this(Acceleration.ZERO, Speed.ONE, Length.ONE);
    }

    /**
     * Constructor.
     * @param acceleration fixed acceleration
     */
    public FixedCarFollowing(final Acceleration acceleration)
    {
        this(acceleration, Speed.ONE, Length.ONE);
    }

    /**
     * Constructor.
     * @param acceleration fixed acceleration
     * @param desiredSpeed fixed desired speed
     * @param desiredHeadway fixed desired headway
     */
    public FixedCarFollowing(final Acceleration acceleration, final Speed desiredSpeed, final Length desiredHeadway)
    {
        this.acceleration = acceleration;
        this.desiredHeadway = desiredHeadway;
        this.desiredSpeed = desiredSpeed;
    }

    @Override
    public Parameters getParameters(final GtuType gtuType) throws ParameterException
    {
        return new ParameterSet();
    }

    @Override
    public FixedCarFollowingModel get()
    {
        return new FixedCarFollowingModel();
    }

    /**
     * Fixed car-following model.
     */
    public class FixedCarFollowingModel implements CarFollowingModel
    {

        /**
         * Constructor.
         */
        public FixedCarFollowingModel()
        {
            //
        }

        @Override
        public Length desiredHeadway(final Parameters parameters, final Speed speed) throws ParameterException
        {
            return FixedCarFollowing.this.desiredHeadway;
        }

        @Override
        public Speed desiredSpeed(final Parameters parameters, final SpeedLimitInfo speedInfo) throws ParameterException
        {
            return FixedCarFollowing.this.desiredSpeed;
        }

        @Override
        public Acceleration followingAcceleration(final Parameters parameters, final Speed speed,
                final SpeedLimitInfo speedLimitInfo, final PerceptionIterable<? extends PerceivedObject> leaders)
                throws ParameterException
        {
            return FixedCarFollowing.this.acceleration;
        }

        @Override
        public String getName()
        {
            return "FixedAcceleration";
        }

        @Override
        public String getLongName()
        {
            return "FixedAcceleration";
        }

    }
}
