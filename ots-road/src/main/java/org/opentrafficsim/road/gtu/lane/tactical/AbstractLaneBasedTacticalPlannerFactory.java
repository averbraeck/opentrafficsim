package org.opentrafficsim.road.gtu.lane.tactical;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionFactory;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModelFactory;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;
import org.opentrafficsim.road.network.speed.SpeedLimitTypes;

/**
 * Abstract tactical planner factory which uses a car-following model factory for supplying peeked desired speed and headway. To
 * this end the next car-following model is created and used throughout all peek invocations until an implementation of this
 * class calls {@code nextCarFollowingModel()} to generate a new tactical planner. Implementations should also use
 * {@code getCarFollowingParameters()} in the {@code getParameters()} method to include the parameters a car-following model
 * requires.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 * @param <T> class of the tactical planner generated
 */
public abstract class AbstractLaneBasedTacticalPlannerFactory<T extends LaneBasedTacticalPlanner>
        implements LaneBasedTacticalPlannerFactory<T>
{

    /** Constructor for the car-following model. */
    private final CarFollowingModelFactory<? extends CarFollowingModel> carFollowingModelFactory;

    /** Peeked car following model. */
    private CarFollowingModel peekedCarFollowingModel = null;

    /** Perception factory. */
    private final PerceptionFactory perceptionFactory;

    /**
     * Constructor.
     * @param carFollowingModelFactory CarFollowingModelFactory&lt;? extends CarFollowingModel&gt;; car-following model factory
     * @param perceptionFactory PerceptionFactory; perception factory
     */
    public AbstractLaneBasedTacticalPlannerFactory(
            final CarFollowingModelFactory<? extends CarFollowingModel> carFollowingModelFactory,
            final PerceptionFactory perceptionFactory)
    {
        this.carFollowingModelFactory = carFollowingModelFactory;
        this.perceptionFactory = perceptionFactory;
    }

    /**
     * Returns the next car following model, which will be a fixed peeked instance until {@code nextCarFollowingModel()} is
     * called.
     * @return CarFollowingModel; next car following model
     */
    private CarFollowingModel peekCarFollowingModel()
    {
        if (this.peekedCarFollowingModel != null)
        {
            return this.peekedCarFollowingModel;
        }
        this.peekedCarFollowingModel = this.carFollowingModelFactory.generateCarFollowingModel();
        return this.peekedCarFollowingModel;
    }

    /**
     * Returns the next car following model.
     * @param gtu LaneBasedGtu; gtu
     * @return CarFollowingModel; next car following model
     */
    protected final CarFollowingModel nextCarFollowingModel(final LaneBasedGTU gtu)
    {
        CarFollowingModel model = peekCarFollowingModel();
        model.init(gtu);
        this.peekedCarFollowingModel = null; // peek will create a new one
        return model;
    }

    /**
     * Returns the parameters for the car-following model using the factory. This method should be used in the
     * {@code getParameters()} method of implementing sub-classes.
     * @return Parameters; parameters for the car-following model using the factory
     * @throws ParameterException on illegal parameter value
     */
    protected final Parameters getCarFollowingParameters() throws ParameterException
    {
        return this.carFollowingModelFactory.getParameters();
    }

    /**
     * Returns a {@code String} representation of the car-following model factory. This method may be used in the
     * {@code toString()} method of implementing sub-classes.
     * @return String; representation of the car-following model factory
     */
    protected final String getCarFollowingModelFactoryString()
    {
        return this.carFollowingModelFactory.toString();
    }

    /** {@inheritDoc} */
    @Override
    public final Speed peekDesiredSpeed(final GtuType gtuType, final Speed speedLimit, final Speed maxGtuSpeed,
            final Parameters parameters) throws GtuException
    {
        try
        {
            SpeedLimitInfo sli = new SpeedLimitInfo();
            sli.addSpeedInfo(SpeedLimitTypes.MAX_VEHICLE_SPEED, maxGtuSpeed);
            sli.addSpeedInfo(SpeedLimitTypes.FIXED_SIGN, speedLimit);
            return peekCarFollowingModel().desiredSpeed(parameters, sli);
        }
        catch (ParameterException exception)
        {
            throw new GtuException(exception);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final Length peekDesiredHeadway(final GtuType gtuType, final Speed speed, final Parameters parameters)
            throws GtuException
    {
        try
        {
            return peekCarFollowingModel().desiredHeadway(parameters, speed);
        }
        catch (ParameterException exception)
        {
            throw new GtuException(exception);
        }
    }

    /**
     * Returns the perception factory.
     * @return PerceptionFactory; perception factory
     */
    public PerceptionFactory getPerceptionFactory()
    {
        return this.perceptionFactory;
    }

}
