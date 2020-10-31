package org.opentrafficsim.sim0mq.kpi;

import org.opentrafficsim.kpi.interfaces.GtuDataInterface;
import org.opentrafficsim.kpi.interfaces.GtuTypeDataInterface;
import org.opentrafficsim.kpi.interfaces.NodeDataInterface;
import org.opentrafficsim.kpi.interfaces.RouteDataInterface;

/**
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 13 okt. 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class GtuData implements GtuDataInterface
{
    /** id. */
    private final String id;

    /** gtu type. */
    private final GtuTypeData gtuType;

    /** route. */
    private final RouteData route;

    /**
     * @param id String; the id
     * @param gtuType GtuTypeData; the gtu type
     * @param route RouteData; the route
     */
    public GtuData(final String id, final GtuTypeData gtuType, final RouteData route)
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
    public final NodeDataInterface getOriginNodeData()
    {
        return this.route.getStartNode();
    }

    /** {@inheritDoc} */
    @Override
    public final NodeDataInterface getDestinationNodeData()
    {
        return this.route.getEndNode();
    }

    /** {@inheritDoc} */
    @Override
    public final GtuTypeDataInterface getGtuTypeData()
    {
        return this.gtuType;
    }

    /** {@inheritDoc} */
    @Override
    public final RouteDataInterface getRouteData()
    {
        return this.route;
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
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GtuData other = (GtuData) obj;
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
