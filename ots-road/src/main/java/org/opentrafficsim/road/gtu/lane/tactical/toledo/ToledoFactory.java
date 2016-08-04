package org.opentrafficsim.road.gtu.lane.tactical.toledo;

import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypes;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedTacticalPlannerFactory;

/**
 * Factory for tactical planner using Toledo's model and car-following model.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Aug 2, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */

public class ToledoFactory implements LaneBasedTacticalPlannerFactory<Toledo>
{

    /** {@inheritDoc} */
    @Override
    public final BehavioralCharacteristics getDefaultBehavioralCharacteristics()
    {
        BehavioralCharacteristics behavioralCharacteristics = new BehavioralCharacteristics();
        try
        {
            behavioralCharacteristics.setDefaultParameter(ParameterTypes.LOOKAHEAD);
            // TODO remove LOOKBACKOLD and insert LOOKBACK once NeighborsPerception uses LOOKBACK
            behavioralCharacteristics.setDefaultParameter(ParameterTypes.LOOKBACKOLD);
        }
        catch (ParameterException exception)
        {
            // should not happen for these 2 parameters
            throw new RuntimeException(exception);
        }
        behavioralCharacteristics.setDefaultParameters(ToledoLaneChangeParameters.class);
        behavioralCharacteristics.setDefaultParameters(ToledoCarFollowing.class);
        behavioralCharacteristics.setDefaultParameters(Toledo.class);
        return behavioralCharacteristics;
    }

    /** {@inheritDoc} */
    @Override
    public final Toledo create(final LaneBasedGTU gtu) throws GTUException
    {
        return new Toledo(new ToledoCarFollowing(), gtu);
    }
    
    /** {@inheritDoc} */
    public final String toString()
    {
        return "ToledoFactory";
    }
    
}
