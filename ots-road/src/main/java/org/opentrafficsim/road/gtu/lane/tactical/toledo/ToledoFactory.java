package org.opentrafficsim.road.gtu.lane.tactical.toledo;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterSet;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedTacticalPlannerFactory;

/**
 * Factory for tactical planner using Toledo's model and car-following model.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
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
    public final Toledo create(final LaneBasedGtu gtu) throws GtuException
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
