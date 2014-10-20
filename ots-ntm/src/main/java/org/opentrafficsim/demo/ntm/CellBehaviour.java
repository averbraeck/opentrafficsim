package org.opentrafficsim.demo.ntm;


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
 * @param <Cars> 
 * @param <Capacity> 
 * @param <Param> 
 */
public abstract class CellBehaviour
{
   

    /** */
    private double supply;

    /** */
    private double demand;

    
    
    /**
     * @return supply.
     */
    public double getSupply()
    {
        return this.supply;
    }

    /**
     * @param supply set supply.
     */
    public void setSupply(double supply)
    {
        this.supply = supply;
    }

    /**
     * @return demand.
     */
    public double getDemand()
    {
        return this.demand;
    }

    /**
     * @param demand set demand.
     */
    public void setDemand(double demand)
    {
        this.demand = demand;
    }
    
/*    *//**
     * @param cars 
     * @param maxCapacity 
     * @param parameters 
     * @return 
     *//*
    double retrieveSupply(Cars cars, Capacity maxCapacity,  Param parameters);

    *//**
     * @param cars 
     * @param maxCapacity 
     * @param parameters 
     * @return 
     *//*
    double retrieveDemand(Cars cars, Capacity maxCapacity,  Param parameters);

    *//**
     * @return 
     * 
     *//*
    double computeAccumulation();*/
}
