package org.opentrafficsim.road.network.sampling;

import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;

/**
 * Gtu representation in road sampler.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class GtuDataRoad implements GtuData
{

    /** Gtu. */
    private final LaneBasedGtu gtu;

    /**
     * @param gtu LaneBasedGtu; gtu
     */
    public GtuDataRoad(final LaneBasedGtu gtu)
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
    public final String getOriginId()
    {
        try
        {
            return this.gtu.getStrategicalPlanner().getRoute().originNode().getId();
        }
        catch (NetworkException exception)
        {
            throw new RuntimeException("Could not get origin node.", exception);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final String getDestinationId()
    {
        try
        {
            return this.gtu.getStrategicalPlanner().getRoute().destinationNode().getId();
        }
        catch (NetworkException exception)
        {
            throw new RuntimeException("Could not get destination node.", exception);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final String getGtuId()
    {
        return this.gtu.getType().getId();
    }

    /** {@inheritDoc} */
    @Override
    public final String getRouteId()
    {
        return this.gtu.getStrategicalPlanner().getRoute().getId();
    }

    /** {@inheritDoc} */
    @Override
    public final Speed getReferenceSpeed()
    {
        try
        {
            double v1 = this.gtu.getReferencePosition().getLane().getSpeedLimit(this.gtu.getType()).si;
            double v2 = this.gtu.getMaximumSpeed().si;
            return Speed.instantiateSI(v1 < v2 ? v1 : v2);
        }
        catch (GtuException exception)
        {
            // GTU was destroyed and is without a reference location
            return Speed.NaN;
        }
        catch (NetworkException exception)
        {
            throw new RuntimeException("Could not obtain reference speed from GTU " + this.gtu, exception);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "GtuData [gtu=" + this.gtu + "]";
    }

}
