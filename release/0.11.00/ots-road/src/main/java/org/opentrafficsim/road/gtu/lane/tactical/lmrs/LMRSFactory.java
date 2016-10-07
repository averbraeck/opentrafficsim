package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import java.io.Serializable;

import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypes;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedTacticalPlannerFactory;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModelFactory;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsUtil;

/**
 * Factory for a tactical planner using LMRS with any car-following model.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Aug 2, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */

public class LMRSFactory implements LaneBasedTacticalPlannerFactory<LMRS>, Serializable
{

    /** */
    private static final long serialVersionUID = 20160811L;

    /** Constructor for the car-following model. */
    private final CarFollowingModelFactory<? extends CarFollowingModel> carFollowingModelFactory;

    /** Default set of parameters for the car-following model. */
    private final BehavioralCharacteristics defaultCarFollowingBehavioralCharacteristics;

    /**
     * Constructor with car-following model class. The class should have an accessible empty constructor.
     * @param carFollowingModelFactory factory of the car-following model
     * @param defaultCarFollowingBehavioralCharacteristics default set of parameters for the car-following model
     * @throws GTUException if the supplied car-following model does not have an accessible empty constructor
     */
    public LMRSFactory(final CarFollowingModelFactory<? extends CarFollowingModel> carFollowingModelFactory,
        final BehavioralCharacteristics defaultCarFollowingBehavioralCharacteristics) throws GTUException
    {
        this.carFollowingModelFactory = carFollowingModelFactory;
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
        return new LMRS(this.carFollowingModelFactory.generateCarFollowingModel(), gtu);
    }
    
    /** {@inheritDoc} */
    public final String toString()
    {
        return "LMRSFactory [car-following=" + this.carFollowingModelFactory + "]";
    }

}
