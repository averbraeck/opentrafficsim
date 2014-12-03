package org.opentrafficsim.demo.ntm;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 9 Oct 2014 <br>
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
        this.tripInfoByNodeMap = new HashMap<Node, TripInfoByDestination>();
    }

    /** the first Area/Node encountered on the path to Destination. */
    private HashMap<Node, TripInfoByDestination> tripInfoByNodeMap;

    
    /** */
    private double supply;

    /** */
    private double demand;

    /** */
    private double accumulatedCars;

    /** The number of cars that are heading for this Cell. */
    private double demandToEnter;

    /** */
    private double flow;

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
     * @return flow.
     */
    public final double getFlow()
    {
        return this.flow;
    }

    /**
     * @param flow set flow.
     */
    public final void setFlow(final double flow)
    {
        this.flow = flow;
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
    public final HashMap<Node, TripInfoByDestination> getTripInfoByNodeMap()
    {
        return this.tripInfoByNodeMap;
    }

    /**
     * @param tripInfoByNodeMap set tripInfoNode.
     */
    public final void setTripInfoByNodeMap(final HashMap<Node, TripInfoByDestination> tripInfoByNodeMap)
    {
        this.tripInfoByNodeMap = tripInfoByNodeMap;
    }

}
