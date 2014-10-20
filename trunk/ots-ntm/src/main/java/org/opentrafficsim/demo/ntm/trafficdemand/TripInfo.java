package org.opentrafficsim.demo.ntm.trafficdemand;

import org.opentrafficsim.demo.ntm.BoundedNode;
import org.opentrafficsim.demo.ntm.BoundedNode;
import org.opentrafficsim.demo.ntm.Node;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 15 Sep 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */
public class TripInfo
{
    /** total number of Trips for this simulation from the trip demand file. */
    private double numberOfTrips;

    /** the first Area/Node encountered on the path to Destination. */
    private Node neighbour;

    /** trips on their journey within an area. */
    private double accumulatedCarsToDestination;

    /** flow to neighbour. */
    private double flowToNeighbour;

    /**
     * @param numberOfTrips total number of Trips for this simulation
     */

    public TripInfo(final double numberOfTrips)
    {
        super();
        this.numberOfTrips = numberOfTrips;
    }

    /**
     * @return numberOfTrips
     */
    public final double getNumberOfTrips()
    {
        return this.numberOfTrips;
    }

    /**
     * @param numberOfTrips set numberOfTrips
     */
    public final void setNumberOfTrips(final double numberOfTrips)
    {
        this.numberOfTrips = numberOfTrips;
    }

    /**
     * @return neighbour.
     */
    public final Node getNeighbour()
    {
        return this.neighbour;
    }

    /**
     * @param endNode set neighbour.
     */
    public final void setNeighbour(final Node endNode)
    {
        this.neighbour = endNode;
    }

    /**
     * @return passingTrips.
     */
    public final double getAccumulatedCarsToDestination()
    {
        return this.accumulatedCarsToDestination;
    }

    /**
     * @param accumulatedCarsToDestination 
     */
    public final void setAccumulatedCarsToDestination(final double accumulatedCarsToDestination)
    {
        this.accumulatedCarsToDestination = accumulatedCarsToDestination;
    }

    /**
     * @param addTrips set accumulating Trips to a certain destination.
     */
    public final void addAccumulatedCarsToDestination(final double addTrips)
    {
        this.accumulatedCarsToDestination += addTrips;
    }

    /**
     * @return flow.
     */
    public final double getFlowToNeighbour()
    {
        return this.flowToNeighbour;
    }

    /**
     * @param flow set flow.
     */
    public final void setFlowToNeighbour(final double flow)
    {
        this.flowToNeighbour = flow;
    }

}
