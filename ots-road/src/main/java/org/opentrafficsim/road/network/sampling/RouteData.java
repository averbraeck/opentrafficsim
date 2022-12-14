package org.opentrafficsim.road.network.sampling;

import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.kpi.interfaces.RouteDataInterface;

/**
 * Route representation in road sampler.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
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
