package org.opentrafficsim.road.network.sampling;

import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.OtsRuntimeException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.road.gtu.LaneBasedGtu;
import org.opentrafficsim.road.gtu.tactical.util.SpeedLimitUtil;

/**
 * GTU representation in road sampler.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
    public LaneBasedGtu getGtu()
    {
        return this.gtu;
    }

    @Override
    public String getId()
    {
        return this.gtu.getId();
    }

    @Override
    public String getOriginId()
    {
        try
        {
            return this.gtu.getStrategicalPlanner().getRoute().get().originNode().getId();
        }
        catch (NetworkException | NullPointerException exception)
        {
            throw new OtsRuntimeException("Could not get origin node.", exception);
        }
    }

    @Override
    public String getDestinationId()
    {
        try
        {
            return this.gtu.getStrategicalPlanner().getRoute().get().destinationNode().getId();
        }
        catch (NetworkException | NullPointerException exception)
        {
            throw new OtsRuntimeException("Could not get destination node.", exception);
        }
    }

    @Override
    public String getGtuTypeId()
    {
        return this.gtu.getType().getId();
    }

    @Override
    public String getRouteId()
    {
        return this.gtu.getStrategicalPlanner().getRoute()
                .orElseThrow(() -> new OtsRuntimeException("Could not get id of route.")).getId();
    }

    @Override
    public Speed getReferenceSpeed()
    {
        return SpeedLimitUtil.getDesiredSpeedProxy(this.gtu.getPosition().lane().getSpeedLimits(this.gtu.getType()),
                this.gtu.getMaximumSpeed());
    }

    @Override
    public String toString()
    {
        return "GtuData [gtu=" + this.gtu + "]";
    }

}
