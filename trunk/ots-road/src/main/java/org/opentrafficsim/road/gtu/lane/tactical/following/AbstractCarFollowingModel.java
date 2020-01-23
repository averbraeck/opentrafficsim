package org.opentrafficsim.road.gtu.lane.tactical.following;

import org.djunits.unit.AccelerationUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionIterable;
import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

/**
 * Default implementation where desired speed and headway are pre-calculated for car-following.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 2016 <br>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public abstract class AbstractCarFollowingModel implements CarFollowingModel
{

    /** Desired headway model. */
    private DesiredHeadwayModel desiredHeadwayModel;

    /** Desired speed model. */
    private DesiredSpeedModel desiredSpeedModel;

    /**
     * @param desiredHeadwayModel DesiredHeadwayModel; desired headway model
     * @param desiredSpeedModel DesiredSpeedModel; desired speed model
     */
    public AbstractCarFollowingModel(final DesiredHeadwayModel desiredHeadwayModel, final DesiredSpeedModel desiredSpeedModel)
    {
        this.desiredHeadwayModel = desiredHeadwayModel;
        this.desiredSpeedModel = desiredSpeedModel;
    }

    /** {@inheritDoc} */
    @Override
    public final Length desiredHeadway(final Parameters parameters, final Speed speed) throws ParameterException
    {
        Throw.whenNull(parameters, "Parameters may not be null.");
        Throw.whenNull(speed, "Speed may not be null.");
        return this.desiredHeadwayModel.desiredHeadway(parameters, speed);
    }

    /** {@inheritDoc} */
    @Override
    public final Speed desiredSpeed(final Parameters parameters, final SpeedLimitInfo speedInfo) throws ParameterException
    {
        Throw.whenNull(parameters, "Parameters may not be null.");
        Throw.whenNull(speedInfo, "Speed limit info may not be null.");
        return this.desiredSpeedModel.desiredSpeed(parameters, speedInfo);
    }

    /** {@inheritDoc} */
    @Override
    public final Acceleration followingAcceleration(final Parameters parameters, final Speed speed,
            final SpeedLimitInfo speedLimitInfo, final PerceptionIterable<? extends Headway> leaders) throws ParameterException
    {
        Throw.whenNull(parameters, "Parameters may not be null.");
        Throw.whenNull(speed, "Speed may not be null.");
        Throw.whenNull(speedLimitInfo, "Speed limit info may not be null.");
        Throw.whenNull(leaders, "Leaders may not be null.");
        // Catch negative headway
        if (!leaders.isEmpty() && leaders.first().getDistance().si <= 0)
        {
            return new Acceleration(Double.NEGATIVE_INFINITY, AccelerationUnit.SI);
        }
        // Forward to method with desired speed and headway predetermined by this car-following model.
        Acceleration acc = followingAcceleration(parameters, speed, desiredSpeed(parameters, speedLimitInfo),
                desiredHeadway(parameters, speed), leaders);
        return acc;
    }

    /**
     * Determination of car-following acceleration, possibly based on multiple leaders.
     * @param parameters Parameters; parameters
     * @param speed Speed; current speed
     * @param desiredSpeed Speed; desired speed
     * @param desiredHeadway Length; desired headway
     * @param leaders PerceptionIterable&lt;? extends Headway&gt;; set of leader headways (guaranteed positive) and speeds,
     *            ordered by headway (closest first)
     * @return car-following acceleration
     * @throws ParameterException if parameter exception occurs
     */
    protected abstract Acceleration followingAcceleration(Parameters parameters, Speed speed, Speed desiredSpeed,
            Length desiredHeadway, PerceptionIterable<? extends Headway> leaders) throws ParameterException;

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public String toString()
    {
        return getLongName();
    }

    /** {@inheritDoc} */
    @Override
    public final void init(final LaneBasedGTU gtu)
    {
        if (this.desiredHeadwayModel instanceof Initialisable)
        {
            ((Initialisable) this.desiredHeadwayModel).init(gtu);
        }
        if (this.desiredSpeedModel instanceof Initialisable)
        {
            ((Initialisable) this.desiredSpeedModel).init(gtu);
        }
    }

}
