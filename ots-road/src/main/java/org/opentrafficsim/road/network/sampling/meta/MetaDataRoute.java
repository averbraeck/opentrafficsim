package org.opentrafficsim.road.network.sampling.meta;

import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;

/**
 * Accepts trajectories with a Route included in a set in a query.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Sep 22, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class MetaDataRoute extends MetaDataType<Route>
{

    /**
     * @param id id
     */
    public MetaDataRoute(final String id)
    {
        super(id);
    }

    /** {@inheritDoc} */
    @Override
    public final Route getValue(final GTU gtu)
    {
        return gtu.getStrategicalPlanner().getRoute();
    }
    
    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "MetaDataRoute: " + super.toString();
    }

}
