package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypes;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedTacticalPlannerFactory;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsUtil;

/**
 * Factory for a tactical planner using LMRS with any car-following model.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Aug 2, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */

public class LMRSFactory implements LaneBasedTacticalPlannerFactory<LMRS>
{

    /** Constructor for the car-following model. */
    private final Constructor<? extends CarFollowingModel> carFollowingModelConstructor;

    /** Default set of parameters for the car-following model. */
    private final BehavioralCharacteristics defaultCarFollowingBehavioralCharacteristics;

    /**
     * Constructor with car-following model class. The class should have an accessible empty constructor.
     * @param carFollowingModelClass class of the car-following model
     * @param defaultCarFollowingBehavioralCharacteristics default set of parameters for the car-following model
     * @param <T> class of the car-following model
     * @throws GTUException if the supplied car-following model does not have an accessible empty constructor
     */
    public <T extends CarFollowingModel> LMRSFactory(final Class<T> carFollowingModelClass,
        final BehavioralCharacteristics defaultCarFollowingBehavioralCharacteristics) throws GTUException
    {
        try
        {
            this.carFollowingModelConstructor = carFollowingModelClass.getConstructor();
            // test constructor
            this.carFollowingModelConstructor.newInstance();
        }
        catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
            | IllegalArgumentException | InvocationTargetException exception)
        {
            throw new GTUException(
                "LMRS factory can only work with stateless car-following models with empty accesible constructors.",
                exception);
        }
        this.defaultCarFollowingBehavioralCharacteristics = defaultCarFollowingBehavioralCharacteristics;
    }
    
    /** {@inheritDoc} */
    @Override
    public final BehavioralCharacteristics getDefaultBehavioralCharacteristics()
    {
        BehavioralCharacteristics behavioralCharacteristics = new BehavioralCharacteristics();
        behavioralCharacteristics.setAll(this.defaultCarFollowingBehavioralCharacteristics);
        behavioralCharacteristics.setDefaultParameters(ParameterTypes.class);
        behavioralCharacteristics.setDefaultParameters(LmrsUtil.class);
        return behavioralCharacteristics;
    }

    /** {@inheritDoc} */
    @Override
    public final LMRS create(final LaneBasedGTU gtu) throws GTUException
    {
        try
        {
            return new LMRS(this.carFollowingModelConstructor.newInstance(), gtu);
        }
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException exception)
        {
            throw new GTUException("Exception occured during creation of a car-following model.", exception);
        }
    }
    
    /** {@inheritDoc} */
    public final String toString()
    {
        return "LMRSFactory [car-following=" + this.carFollowingModelConstructor + "]";
    }

}
