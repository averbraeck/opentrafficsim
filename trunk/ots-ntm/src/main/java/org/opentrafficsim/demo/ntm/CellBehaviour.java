package org.opentrafficsim.demo.ntm;

import java.util.HashMap;

import org.djunits.unit.FrequencyUnit;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.djunits.value.vdouble.scalar.DoubleScalar.Rel;
import org.djunits.value.vdouble.scalar.Frequency;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version 9 Oct 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */
public class CellBehaviour
{
    /**
     * 
     */
    public CellBehaviour()
    {
        super();
        this.tripInfoByDestinationMap = new HashMap<Node, TripInfoByDestination>();
    }

    /** The first Area/Node encountered on the path to Destination. */
    private HashMap<Node, TripInfoByDestination> tripInfoByDestinationMap;

    /** Maximum in-flow */
    private double supply;

    /** Demand generated to leave */
    private double demand;

    /** */
    private double accumulatedCars;

    /** The number of cars that are heading for this Cell. */
    private double demandToEnter;

    /** */
    private double arrivals;

    /** */
    private double departures;

    /** */
    private HashMap<BoundedNode, Frequency> borderCapacity;

    /** */
    private HashMap<BoundedNode, Frequency> borderDemand;

    /**
     * @return supply.
     */
    public final double getSupply()
    {
        return this.supply;
    }

    /**
     * @param supply set supply.
     */
    public final void setSupply(final double supply)
    {
        this.supply = supply;
    }

    /**
     * @return demand.
     */
    public final double getDemand()
    {
        return this.demand;
    }

    /**
     * @param demand set demand.
     */
    public final void setDemand(final double demand)
    {
        this.demand = demand;
    }

    /**
     * @param tripByStep
     */
    public void addDemand(double trips)
    {
        this.demand += trips;
    }

    public final double getArrivals()
    {
        return this.arrivals;
    }

    /**
     * @param arrivals set flow.
     */
    public final void setArrivals(final double arrivals)
    {
        this.arrivals = arrivals;
    }

    /**
     * @param arrivals set flow.
     */
    public final void addArrivals(final double arrivals)
    {
        this.arrivals += arrivals;
    }

    /**
     * @return accumulatedCars.
     */
    public final double getAccumulatedCars()
    {
        return this.accumulatedCars;
    }

    /**
     * @param accumulatedCars set accumulatedCars.
     */
    public final void setAccumulatedCars(final double accumulatedCars)
    {
        this.accumulatedCars = accumulatedCars;
    }

    /**
     * @param addCars add accumulatedCars.
     */
    public final void addAccumulatedCars(final double addCars)
    {
        this.accumulatedCars += addCars;
    }

    /**
     * @return demandToEnter.
     */
    public final double getDemandToEnter()
    {
        return this.demandToEnter;
    }

    /**
     * @param demandToEnter set demandToEnter.
     */
    public final void setDemandToEnter(final double demandToEnter)
    {
        this.demandToEnter = demandToEnter;
    }

    /**
     * @param addDemandToEnter adds demandToEnter.
     */
    public final void addDemandToEnter(final double addDemandToEnter)
    {
        this.demandToEnter += addDemandToEnter;
    }

    /**
     * @return tripInfoNode.
     */
    public final HashMap<Node, TripInfoByDestination> getTripInfoByDestinationMap()
    {
        return this.tripInfoByDestinationMap;
    }

    /**
     * @param tripInfoByDestinationMap set tripInfoNode.
     */
    public final void setTripInfoByDestinationMap(final HashMap<Node, TripInfoByDestination> tripInfoByDestinationMap)
    {
        this.tripInfoByDestinationMap = tripInfoByDestinationMap;
    }

    /**
     * @return departures.
     */
    public double getDepartures()
    {
        return departures;
    }

    /**
     * @param departures set departures.
     */
    public void setDepartures(double departures)
    {
        this.departures = departures;
    }

    /**
     * @param departures set departures.
     */
    public void addDepartures(double departures)
    {
        this.departures += departures;
    }

    /**
     * @return borderCapacity.
     */
    public HashMap<BoundedNode, Frequency> getBorderCapacity()
    {
        return borderCapacity;
    }

    /**
     * @param borderCapacity set borderCapacity.
     */
    public void setBorderCapacity(HashMap<BoundedNode, Frequency> borderCapacity)
    {
        this.borderCapacity = borderCapacity;
    }

    /**
     * @param demand
     * @param linkData set linkData.
     */
    public void addBorderDemand(BoundedNode node, Frequency demand)
    {
        double cap = demand.getInUnit(FrequencyUnit.PER_HOUR);
        Frequency addCap = new Frequency(cap, FrequencyUnit.PER_HOUR);

        if (this.getBorderDemand().get(node) == null)
        {
            Frequency zeroCap = new Frequency(0.0, FrequencyUnit.PER_HOUR);
            this.getBorderDemand().put(node, zeroCap);
        }
        Frequency total = this.getBorderDemand().get(node).plus(addCap);
        this.getBorderDemand().put(node, total);
    }

    /**
     * @return borderDemand.
     */
    public HashMap<BoundedNode, Frequency> getBorderDemand()
    {
        return borderDemand;
    }

    /**
     * @param borderDemand set borderDemand.
     */
    public void setBorderDemand(HashMap<BoundedNode, Frequency> borderDemand)
    {
        this.borderDemand = borderDemand;
    }

}
