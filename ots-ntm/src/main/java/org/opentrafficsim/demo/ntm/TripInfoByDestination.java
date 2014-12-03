package org.opentrafficsim.demo.ntm;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 27 Nov 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */
public class TripInfoByDestination
{
    /** the first Area/Node encountered on the path to Destination. */
    private Node neighbour;

    /** the first Area/Node encountered on the path to Destination. */
    private Node destination;

    /** trips on their journey within an area. */
    private double accumulatedCarsToDestination;

    /** flow to neighbour. */
    private double demandToNeighbour;

    /**
     * @param neighbour
     * @param destination
     * @param accumulatedCarsToDestination
     * @param flowToNeighbour
     */
    public TripInfoByDestination(Node neighbour, Node destination, double accumulatedCarsToDestination, double demandToNeighbour)
    {
        super();
        this.neighbour = neighbour;
        this.destination = destination;
        this.accumulatedCarsToDestination = accumulatedCarsToDestination;
        this.demandToNeighbour = demandToNeighbour;
    }

    /**
     * @return neighbour.
     */
    public Node getNeighbour()
    {
        return this.neighbour;
    }

    /**
     * @param neighbour set neighbour.
     */
    public void setNeighbour(Node neighbour)
    {
        this.neighbour = neighbour;
    }


    /**
     * @return geef bestemmin g
     */
    public Node getDestination()
    {
        return this.destination;
    }

    /**
     * @param destination set destination.
     */
    public void setDestination(Node destination)
    {
        this.destination = destination;
    }

    /**
     * @return accumulatedCarsToDestination.
     */
    public double getAccumulatedCarsToDestination()
    {
        return this.accumulatedCarsToDestination;
    }

    /**
     * @param accumulatedCarsToDestination set accumulatedCarsToDestination.
     */
    public void setAccumulatedCarsToDestination(double accumulatedCarsToDestination)
    {
        this.accumulatedCarsToDestination = accumulatedCarsToDestination;
    }
    
    /**
     * @param accumulatedCarsToDestination set accumulatedCarsToDestination.
     */
    public void addAccumulatedCarsToDestination(double addAccumulatedCarsToDestination)
    {
        this.accumulatedCarsToDestination += addAccumulatedCarsToDestination;
    }

    /**
     * @return flowToNeighbour.
     */
    public double getDemandToNeighbour()
    {
        return this.demandToNeighbour;
    }

    /**
     * @param flowToNeighbour set flowToNeighbour.
     */
    public void addDemandToNeighbour(double addDemandToNeighbour)
    {
        this.demandToNeighbour += addDemandToNeighbour;
    }
    
    /**
     * @param flowToNeighbour set flowToNeighbour.
     */
    public void setDemandToNeighbour(double demandToNeighbour)
    {
        this.demandToNeighbour = demandToNeighbour;
    }

}
