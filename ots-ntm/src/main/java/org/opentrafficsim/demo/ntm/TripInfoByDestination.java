package org.opentrafficsim.demo.ntm;

import java.util.HashMap;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version 27 Nov 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */
public class TripInfoByDestination
{
    /** The first Area/Node encountered on the path to Destination. */
    private HashMap<BoundedNode, Double> routeFractionToNeighbours;

    /** The first Area/Node encountered on the path to Destination. */
    private HashMap<BoundedNode, Double> accumulatedCarsToNeighbour;

    /** The first Area/Node encountered on the path to Destination. */
    private HashMap<BoundedNode, Double> demandToNeighbour;

    /** The first Area/Node encountered on the path to Destination. */
    private NTMNode destination;

    /** Trips on their journey within an area. */
    private double accumulatedCarsToDestination;

    /** Demand to neighbour. */
    private double demandToDestination;

    /** Trips that could not yet injected. */
    private double tripsInReservoir;

    /** Demand to neighbour. */
    private double departedTrips;

    /** Demand to neighbour. */
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
            NTMNode destination)
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
     * @param neighbour HashMap&lt;BoundedNode,Double&gt;; set neighbour.
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
     * @param demandToNeighbour HashMap&lt;BoundedNode,Double&gt;; set demandToNeighbour.
     */
    public void setDemandToNeighbour(HashMap<BoundedNode, Double> demandToNeighbour)
    {
        this.demandToNeighbour = demandToNeighbour;
    }

    /**
     * @return geef bestemmin g
     */
    public NTMNode getDestination()
    {
        return this.destination;
    }

    /**
     * @param destination NTMNode; set destination.
     */
    public void setDestination(NTMNode destination)
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
     * @param accumulatedCarsToDestination double; set accumulatedCarsToDestination.
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
     * @param fluxToNeighbour double; set fluxToNeighbour.
     */
    public void setFluxToNeighbour(double fluxToNeighbour)
    {
        this.fluxToNeighbour = fluxToNeighbour;
    }

    /**
     * @param fluxToNeighbour double; set fluxToNeighbour.
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
     * @param departedTrips double; set departedTrips.
     */
    public void setDepartedTrips(double departedTrips)
    {
        this.departedTrips = departedTrips;
    }

    /**
     * @param addDepartedTrips double;
     */
    public void addDepartedTrips(double addDepartedTrips)
    {
        this.departedTrips += addDepartedTrips;
    }

    /**
     * @return tripsInReservoir.
     */
    public double getTripsInReservoir()
    {
        return tripsInReservoir;
    }

    /**
     * @param tripsInReservoir double; set tripsInReservoir.
     */
    public void setTripsInReservoir(double tripsInReservoir)
    {
        this.tripsInReservoir = tripsInReservoir;
    }

    /**
     * @param tripsInReservoir double; \
     */
    public void addTripsInReservoir(double tripsInReservoir)
    {
        this.tripsInReservoir += tripsInReservoir;
    }

    /**
     * @return arrivedTrips.
     */
    public double getArrivedTrips()
    {
        return this.arrivedTrips;
    }

    /**
     * @param arrivedTrips double; set arrivedTrips.
     */
    public void setArrivedTrips(double arrivedTrips)
    {
        this.arrivedTrips = arrivedTrips;
    }

    /**
     * @param addArrivedTrips double;
     */
    public void addArrivedTrips(double addArrivedTrips)
    {
        this.arrivedTrips += addArrivedTrips;
    }

}
