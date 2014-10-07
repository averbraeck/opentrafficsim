package org.opentrafficsim.demo.ntm;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.demo.ntm.fundamentaldiagrams.NetworkFundamentalDiagram;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 26 Sep 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */
public class CellBehaviourNTM
{
    /** */
    private static final long serialVersionUID = 20140903L;


    /** The parameters for the NFD. */
    private ParametersNTM parametersNTM;
    
    /** currentSpeed: average current speed of Cars in this CELL. */
    private DoubleScalar.Abs<SpeedUnit> currentSpeed;

    /** */
    private double maxCapacity;

    /** */
    private double productionSupply;

    /** */
    private double productionDemand;

    /** The number of cars that are heading for this Cell. */
    private double demandToEnter;
    
    /** */
    private double productionElse;

    /** */
    private double speedSupply;

    /** */
    private double speedDemand;

    /** */
    private double speedElse;

    /**
     * @param parametersNTM are: 
     * - id ID
     * - accCritical1 low param
     * - accCritical2 high param
     * - accJam jam param
     * - freeSpeed uncongested speed
     * - roadLength length of all roads
     */
    public CellBehaviourNTM(final ParametersNTM parametersNTM)
    {
        super();
        this.parametersNTM = parametersNTM;
        this.maxCapacity =
                parametersNTM.getAccCritical1() * parametersNTM.getFreeSpeed().getValueInUnit(SpeedUnit.KM_PER_HOUR);
    }

    /** Retrieves car production from network fundamental diagram.
     * @param accumulatedCars number of cars in Cell
     * @return carProduction
     */
    public final double retrieveCarProduction(final double accumulatedCars)
    {
        ArrayList<Point2D> xyPairs = new ArrayList<Point2D>();
        Point2D p = new Point2D.Double();
        p.setLocation(0, 0);
        xyPairs.add(p);
        p = new Point2D.Double();
        p.setLocation(this.parametersNTM.getAccCritical1(), this.maxCapacity);
        xyPairs.add(p);
        p = new Point2D.Double();
        p.setLocation(this.parametersNTM.getAccCritical2(), this.maxCapacity);
        xyPairs.add(p);
        p = new Point2D.Double();
        p.setLocation(this.parametersNTM.getAccJam(), 0);
        xyPairs.add(p);
        double carProduction = NetworkFundamentalDiagram.PieceWiseLinear(xyPairs, accumulatedCars);
        return carProduction;
    }

    /**
     * @param accumulatedCars number of cars in Cell
     */
    public final void computeProductionDemand(final double accumulatedCars)
    {
        double maxDemand = this.parametersNTM.getFreeSpeed().getValueSI() * accumulatedCars; // ask Victor
        this.productionDemand = Math.min(maxDemand, this.maxCapacity); // / demand
        this.speedDemand = this.productionDemand / accumulatedCars;
    }

    /** determines the level incoming traffic
     * @param accumulatedCars number of cars in Cell
     */
    public final void computeProductionSupply(final double accumulatedCars)
    {
        double carProduction = retrieveCarProduction(accumulatedCars);
        this.productionSupply = Math.min(this.maxCapacity, carProduction); // supply
        this.speedSupply = this.productionSupply / accumulatedCars;
    }

    /** not used 
     * @param accumulatedCars number of cars in Cell
     */
    public final void computeProductionElse(final double accumulatedCars)
    {
        double carProduction = retrieveCarProduction(accumulatedCars);
        double production = Math.min(this.maxCapacity, carProduction); // supply
     
        double lowerBoundProduction = Math.max(0.05 * this.maxCapacity, production);
        double maxDemand = this.parametersNTM.getFreeSpeed().getValueSI() * accumulatedCars; // ask Victor
        double demand = Math.min(maxDemand, this.maxCapacity); // / demand
     
        this.productionElse = Math.min(lowerBoundProduction, demand); // / else
        this.speedElse = this.productionElse / accumulatedCars;
        // if (accumulationCars > 0) {
        // this.currentSpeed = new DoubleScalarAbs<SpeedUnit>(carProduction / accumulatedCars, SpeedUnit.KM_PER_HOUR);
        // }
    } 
    /**
     * @return averageSpeed
     */
    public final DoubleScalar.Abs<SpeedUnit> getCurrentSpeed()
    {
        return this.currentSpeed;
    }

    /**
     * @return maxCapacity
     */
    public final double getMaxCapacity()
    {
        return this.maxCapacity;
    }

    /**
     * @param maxCapacity set maxCapacity.
     */
    public final void setMaxCapacity(final double maxCapacity)
    {
        this.maxCapacity = maxCapacity;
    }

    /**
     * @return productionSupply
     */
    public final double getProductionSupply()
    {
        return this.productionSupply;
    }

    /**
     * @return productionDemand
     */
    public final double getProductionDemand()
    {
        return this.productionDemand;
    }

    /**
     * @return productionElse
     */
    public final double getProductionElse()
    {
        return this.productionElse;
    }

    /**
     * @return speedSupply
     */
    public final double getSpeedSupply()
    {
        return this.speedSupply;
    }

    /**
     * @return speedDemand
     */
    public final double getSpeedDemand()
    {
        return this.speedDemand;
    }

    /**
     * @param speedDemand set speedDemand
     */
    public final void setSpeedDemand(final double speedDemand)
    {
        this.speedDemand = speedDemand;
    }

    /**
     * @return speedElse
     */
    public final double getSpeedElse()
    {
        return this.speedElse;
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
    public final void addDemandToEnter(double addDemandToEnter)
    {
        this.demandToEnter += addDemandToEnter;
    }


    
}
