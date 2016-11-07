package org.opentrafficsim.road.gtu.lane.tactical;

import java.io.Serializable;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypes;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.tactical.following.GTUFollowingModelOld;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Aug 7, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */

public class LaneBasedGTUFollowingTacticalPlannerFactory implements
    LaneBasedTacticalPlannerFactory<LaneBasedGTUFollowingTacticalPlanner>, Serializable
{

    /** */
    private static final long serialVersionUID = 20160811L;
    
    /** The car following model. */
    private GTUFollowingModelOld carFollowingModel;

    /**
     * Constructor with fixed stateless car-following and lane change model.
     * @param carFollowingModel car following model
     */
    public LaneBasedGTUFollowingTacticalPlannerFactory(final GTUFollowingModelOld carFollowingModel)
    {
        this.carFollowingModel = carFollowingModel;
    }

    /** {@inheritDoc} */
    @Override
    public final BehavioralCharacteristics getDefaultBehavioralCharacteristics()
    {
        BehavioralCharacteristics bc =  new BehavioralCharacteristics().setDefaultParameters(ParameterTypes.class);
        try
        {
            bc.setParameter(ParameterTypes.LOOKAHEAD, new Length(250, LengthUnit.SI));
        }
        catch (ParameterException pe)
        {
            throw new RuntimeException("Parameter type 'LOOKAHEAD' could not be set.", pe);
        }
        return bc;
    }

    /** {@inheritDoc} */
    @Override
    public final LaneBasedGTUFollowingTacticalPlanner create(final LaneBasedGTU gtu) throws GTUException
    {
        return new LaneBasedGTUFollowingTacticalPlanner(this.carFollowingModel, gtu);
    }

    /** {@inheritDoc} */
    public final String toString()
    {
        return "LaneBasedGTUFollowingTacticalPlanner [car-following=" + this.carFollowingModel + "]";
    }
    
}
