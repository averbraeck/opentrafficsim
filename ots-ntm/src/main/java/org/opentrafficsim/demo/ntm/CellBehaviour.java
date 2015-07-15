package org.opentrafficsim.demo.ntm;

import java.util.ArrayList;
import java.util.HashMap;

import org.opentrafficsim.core.unit.FrequencyUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author: pknoppers
 * $, initial version 9 Oct 2014 <br>
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

    /** the first Area/Node encountered on the path to Destination. */
    private HashMap<Node, TripInfoByDestination> tripInfoByDestinationMap;

    /** maximum in-flow */
    private double supply;

    /** demand generated to leave */
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
    private HashMap<BoundedNode, Abs<FrequencyUnit>> borderCapacity;

    /** */
    private HashMap<BoundedNode, Abs<FrequencyUnit>> borderDemand;

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
    public HashMap<BoundedNode, Abs<FrequencyUnit>> getBorderCapacity()
    {
        return borderCapacity;
    }

    /**
     * @param borderCapacity set borderCapacity.
     */
    public void setBorderCapacity(HashMap<BoundedNode, Abs<FrequencyUnit>> borderCapacity)
    {
        this.borderCapacity = borderCapacity;
    }

    /**
     * @param demand
     * @param linkData set linkData.
     */
    public void addBorderDemand(BoundedNode node, Abs<FrequencyUnit> demand)
    {
        double cap = demand.getInUnit(FrequencyUnit.PER_HOUR);
        Rel<FrequencyUnit> addCap = new Rel<FrequencyUnit>(cap, FrequencyUnit.PER_HOUR);

        if (this.getBorderDemand().get(node) == null)
        {
            Abs<FrequencyUnit> zeroCap = new Abs<FrequencyUnit>(0.0, FrequencyUnit.PER_HOUR);
            this.getBorderDemand().put(node, zeroCap);
        }
        Abs<FrequencyUnit> total = DoubleScalar.plus(this.getBorderDemand().get(node), addCap).immutable();
        this.getBorderDemand().put(node, total);
    }

    /**
     * @return borderDemand.
     */
    public HashMap<BoundedNode, Abs<FrequencyUnit>> getBorderDemand()
    {
        return borderDemand;
    }

    /**
     * @param borderDemand set borderDemand.
     */
    public void setBorderDemand(HashMap<BoundedNode, Abs<FrequencyUnit>> borderDemand)
    {
        this.borderDemand = borderDemand;
    }

}
