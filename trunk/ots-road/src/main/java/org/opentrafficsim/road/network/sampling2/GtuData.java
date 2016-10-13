package org.opentrafficsim.road.network.sampling2;

import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.kpi.interfaces.GtuDataInterface;
import org.opentrafficsim.kpi.interfaces.GtuTypeDataInterface;
import org.opentrafficsim.kpi.interfaces.NodeDataInterface;
import org.opentrafficsim.kpi.interfaces.RouteDataInterface;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 13 okt. 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class GtuData implements GtuDataInterface
{

    /** Gtu. */
    private final LaneBasedGTU gtu;
    
    /**
     * @param gtu gtu
     */
    public GtuData(final LaneBasedGTU gtu)
    {
        this.gtu = gtu;
    }
    
    /**
     * @return gtu.
     */
    public final LaneBasedGTU getGtu()
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
    public final NodeDataInterface getOriginNodeData()
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
    public final NodeDataInterface getDestinationNodeData()
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
    public final GtuTypeDataInterface getGtuTypeData()
    {
        return new GtuTypeData(this.gtu.getGTUType());
    }

    /** {@inheritDoc} */
    @Override
    public final RouteDataInterface getRouteData()
    {
        return new RouteData(this.gtu.getStrategicalPlanner().getRoute());
    }

    /** {@inheritDoc} */
    @Override
    public final  String toString()
    {
        return "GtuData [gtu=" + this.gtu + "]";
    }
    
}
