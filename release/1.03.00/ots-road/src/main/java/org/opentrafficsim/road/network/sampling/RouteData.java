package org.opentrafficsim.road.network.sampling;

import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.kpi.interfaces.RouteDataInterface;

/**
 * Route representation in road sampler.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 13 okt. 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class RouteData implements RouteDataInterface
{

    /** Route. */
    private final Route route;

    /**
     * @param route Route; route
     */
    public RouteData(final Route route)
    {
        this.route = route;
    }

    /**
     * @return route.
     */
    public final Route getRoute()
    {
        return this.route;
    }

    /** {@inheritDoc} */
    @Override
    public final String getId()
    {
        return this.route.getId();
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.route == null) ? 0 : this.route.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        RouteData other = (RouteData) obj;
        if (this.route == null)
        {
            if (other.route != null)
            {
                return false;
            }
        }
        else if (!this.route.equals(other.route))
        {
            return false;
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "RouteData [route=" + this.route + "]";
    }

}
