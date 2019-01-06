package org.opentrafficsim.road.gtu.lane.tactical.toledo;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterSet;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedTacticalPlannerFactory;

/**
 * Factory for tactical planner using Toledo's model and car-following model.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
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
    public final Parameters getParameters()
    {
        ParameterSet parameters = new ParameterSet();
        try
        {
            parameters.setDefaultParameter(ParameterTypes.LOOKAHEAD);
            parameters.setDefaultParameter(ParameterTypes.LOOKBACK);
            parameters.setDefaultParameter(ParameterTypes.PERCEPTION);
        }
        catch (ParameterException exception)
        {
            // should not happen for these 2 parameters
            throw new RuntimeException(exception);
        }
        parameters.setDefaultParameters(ToledoLaneChangeParameters.class);
        parameters.setDefaultParameters(ToledoCarFollowing.class);
        parameters.setDefaultParameters(Toledo.class);
        return parameters;
    }

    /** {@inheritDoc} */
    @Override
    public final Toledo create(final LaneBasedGTU gtu) throws GTUException
    {
        return new Toledo(new ToledoCarFollowing(), gtu);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "ToledoFactory";
    }

}
