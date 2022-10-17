package org.opentrafficsim.road.network.sampling;

import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.kpi.interfaces.GtuDataInterface;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;

/**
 * Gtu representation in road sampler.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class GtuData implements GtuDataInterface
{

    /** Gtu. */
    private final LaneBasedGtu gtu;

    /**
     * @param gtu LaneBasedGtu; gtu
     */
    public GtuData(final LaneBasedGtu gtu)
    {
        this.gtu = gtu;
    }

    /**
     * @return gtu.
     */
    public final LaneBasedGtu getGtu()
    {
        return this.gtu;
    }

    /** {@inheritDoc} */
    @Override
    public final String getId()
    {
        return this.gtu.getId();
    }

    /** {@inheritDoc} */
    @Override
    public final NodeData getOriginNodeData()
    {
        try
        {
            return new NodeData(this.gtu.getStrategicalPlanner().getRoute().originNode());
        }
        catch (NetworkException exception)
        {
            throw new RuntimeException("Could not get origin node.", exception);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final NodeData getDestinationNodeData()
    {
        try
        {
            return new NodeData(this.gtu.getStrategicalPlanner().getRoute().destinationNode());
        }
        catch (NetworkException exception)
        {
            throw new RuntimeException("Could not get destination node.", exception);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final GtuTypeData getGtuTypeData()
    {
        return new GtuTypeData(this.gtu.getType());
    }

    /** {@inheritDoc} */
    @Override
    public final RouteData getRouteData()
    {
        return new RouteData(this.gtu.getStrategicalPlanner().getRoute());
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "GtuData [gtu=" + this.gtu + "]";
    }

}
