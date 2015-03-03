package org.opentrafficsim.demo.ntm;

import java.util.HashMap;

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
    private HashMap<BoundedNode, Double> routeFractionToNeighbours;

    /** the first Area/Node encountered on the path to Destination. */
    private HashMap<BoundedNode, Double> accumulatedCarsToNeighbour;

    /** the first Area/Node encountered on the path to Destination. */
    private HashMap<BoundedNode, Double> demandToNeighbour;

    /** the first Area/Node encountered on the path to Destination. */
    private Node destination;

    /** trips on their journey within an area. */
    private double accumulatedCarsToDestination;

    /** demand to neighbour. */
    private double demandToDestination;

    /** demand to neighbour. */
    private double departedTrips;

    /** demand to neighbour. */
    private double arrivedTrips;

    /** */
    private double fluxToNeighbour;

    /**
     * @param routeFractionToNeighbours 
     * @param accumulatedCarsToNeighbour 
     * @param demandToNeighbour 
     * @param destination

     */
    public TripInfoByDestination(HashMap<BoundedNode, Double> routeFractionToNeighbours,
            HashMap<BoundedNode, Double> accumulatedCarsToNeighbour, HashMap<BoundedNode, Double> demandToNeighbour,
            Node destination)
    {
        super();
        this.routeFractionToNeighbours = routeFractionToNeighbours;
        this.accumulatedCarsToNeighbour = accumulatedCarsToNeighbour;
        this.demandToNeighbour = demandToNeighbour;
        this.destination = destination;
    }

    /**
     * @return neighbour.
     */
    public HashMap<BoundedNode, Double> getRouteFractionToNeighbours()
    {
        return this.routeFractionToNeighbours;
    }

    /**
     * @param neighbour set neighbour.
     */
    public void setRouteFractionToNeighbours(HashMap<BoundedNode, Double> neighbour)
    {
        this.routeFractionToNeighbours = neighbour;
    }

    /**
     * @return accumulationToNeighbour.
     */
    public HashMap<BoundedNode, Double> getAccumulatedCarsToNeighbour()
    {
        return accumulatedCarsToNeighbour;
    }

    /**
     * @param accumulationToNeighbour set accumulationToNeighbour.
     */
    public void setAccumulatedCarsToNeighbour(HashMap<BoundedNode, Double> accumulatedCarsToNeighbour)
    {
        this.accumulatedCarsToNeighbour = accumulatedCarsToNeighbour;
    }

    /**
     * @return demandToNeighbour.
     */
    public HashMap<BoundedNode, Double> getDemandToNeighbour()
    {
        return demandToNeighbour;
    }

    /**
     * @param demandToNeighbour set demandToNeighbour.
     */
    public void setDemandToNeighbour(HashMap<BoundedNode, Double> demandToNeighbour)
    {
        this.demandToNeighbour = demandToNeighbour;
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
    public double getDemandToDestination()
    {
        return this.demandToDestination;
    }

    /**
     * @param flowToNeighbour set flowToNeighbour.
     */
    public void addDemandToDestination(double addDemandToDestination)
    {
        this.demandToDestination += addDemandToDestination;
    }

    /**
     * @param flowToNeighbour set flowToNeighbour.
     */
    public void setDemandToDestination(double demandToDestination)
    {
        this.demandToDestination = demandToDestination;
    }

    /**
     * @return fluxToNeighbour.
     */
    public double getFluxToNeighbour()
    {
        return this.fluxToNeighbour;
    }

    /**
     * @param fluxToNeighbour set fluxToNeighbour.
     */
    public void setFluxToNeighbour(double fluxToNeighbour)
    {
        this.fluxToNeighbour = fluxToNeighbour;
    }

    /**
     * @param fluxToNeighbour set fluxToNeighbour.
     */
    public void addFluxToNeighbour(double fluxToNeighbour)
    {
        this.fluxToNeighbour += fluxToNeighbour;
    }

    /**
     * @return departedTrips.
     */
    public double getDepartedTrips()
    {
        return this.departedTrips;
    }

    /**
     * @param departedTrips set departedTrips.
     */
    public void setDepartedTrips(double departedTrips)
    {
        this.departedTrips = departedTrips;
    }

    /**
     * @param addDepartedTrips
     */
    public void addDepartedTrips(double addDepartedTrips)
    {
        this.departedTrips += addDepartedTrips;
    }

    /**
     * @return arrivedTrips.
     */
    public double getArrivedTrips()
    {
        return this.arrivedTrips;
    }

    /**
     * @param arrivedTrips set arrivedTrips.
     */
    public void setArrivedTrips(double arrivedTrips)
    {
        this.arrivedTrips = arrivedTrips;
    }

    /**
     * @param addArrivedTrips
     */
    public void addArrivedTrips(double addArrivedTrips)
    {
        this.arrivedTrips += addArrivedTrips;
    }

}
