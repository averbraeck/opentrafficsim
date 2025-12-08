package org.opentrafficsim.road.network.sampling;

import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.OtsRuntimeException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;

/**
 * GTU representation in road sampler.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class GtuDataRoad implements GtuData
{

    /** GTU. */
    private final LaneBasedGtu gtu;

    /**
     * Constructor.
     * @param gtu GTU
     */
    public GtuDataRoad(final LaneBasedGtu gtu)
    {
        this.gtu = gtu;
    }

    /**
     * Get GTU.
     * @return GTU.
     */
    public final LaneBasedGtu getGtu()
    {
        return this.gtu;
    }

    @Override
    public final String getId()
    {
        return this.gtu.getId();
    }

    @Override
    public final String getOriginId()
    {
        try
        {
            return this.gtu.getStrategicalPlanner().getRoute().originNode().getId();
        }
        catch (NetworkException exception)
        {
            throw new OtsRuntimeException("Could not get origin node.", exception);
        }
    }

    @Override
    public final String getDestinationId()
    {
        try
        {
            return this.gtu.getStrategicalPlanner().getRoute().destinationNode().getId();
        }
        catch (NetworkException exception)
        {
            throw new OtsRuntimeException("Could not get destination node.", exception);
        }
    }

    @Override
    public final String getGtuTypeId()
    {
        return this.gtu.getType().getId();
    }

    @Override
    public final String getRouteId()
    {
        return this.gtu.getStrategicalPlanner().getRoute().getId();
    }

    @Override
    public final Speed getReferenceSpeed()
    {
        try
        {
            double v1 = this.gtu.getPosition().lane().getSpeedLimit(this.gtu.getType()).si;
            double v2 = this.gtu.getMaximumSpeed().si;
            return Speed.ofSI(v1 < v2 ? v1 : v2);
        }
        catch (NetworkException exception)
        {
            throw new OtsRuntimeException("Could not obtain reference speed from GTU " + this.gtu, exception);
        }
    }

    @Override
    public final String toString()
    {
        return "GtuData [gtu=" + this.gtu + "]";
    }

}
