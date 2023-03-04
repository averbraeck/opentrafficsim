package org.opentrafficsim.sim0mq.kpi;

import org.opentrafficsim.kpi.interfaces.GtuData;

/**
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class GtuDataSim0 implements GtuData
{
    /** id. */
    private final String id;

    /** gtu type. */
    private final String gtuType;

    /** route. */
    private final RouteData route;

    /**
     * @param id String; the id
     * @param gtuType GtuTypeData; the gtu type
     * @param route RouteData; the route
     */
    public GtuDataSim0(final String id, final String gtuType, final RouteData route)
    {
        this.id = id;
        this.gtuType = gtuType;
        this.route = route;
    }

    /** {@inheritDoc} */
    @Override
    public final String getId()
    {
        return this.id;
    }

    /** {@inheritDoc} */
    @Override
    public final String getOriginId()
    {
        return this.route.getStartNode();
    }

    /** {@inheritDoc} */
    @Override
    public final String getDestinationId()
    {
        return this.route.getEndNode();
    }

    /** {@inheritDoc} */
    @Override
    public final String getGtuId()
    {
        return this.gtuType;
    }

    /** {@inheritDoc} */
    @Override
    public final String getRouteId()
    {
        return this.route.getId();
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GtuDataSim0 other = (GtuDataSim0) obj;
        if (this.id == null)
        {
            if (other.id != null)
                return false;
        }
        else if (!this.id.equals(other.id))
            return false;
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "GtuData [id=" + this.id + ", gtuType=" + this.gtuType + ", route=" + this.route + "]";
    }

}
