package org.opentrafficsim.demo.ntm;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import org.opentrafficsim.core.unit.LengthUnit;
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
 * @param <ID>
 */
public class CellBehaviourNTM implements CellBehaviour
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

    /** */
    private double productionElse;

    /** */
    private double speedSupply;

    /** */
    private double speedDemand;

    /** */
    private double speedElse;

    /**
     * @param id ID
     * @param accCritical1 low param
     * @param accCritical2 high param
     * @param accJam jam param
     * @param freeSpeed uncongested speed
     * @param roadLength length of all roads
     */
    public CellBehaviourNTM(ParametersNTM parametersNTM)
    {
        super();
        this.parametersNTM = parametersNTM;
        double capacity =
                parametersNTM.getAccCritical1() * parametersNTM.getFreeSpeed().getValueInUnit(SpeedUnit.KM_PER_HOUR);
        this.maxCapacity = capacity;
    }

    /**
     * @param accumulatedCars number of cars in Cell
     * @return carProduction
     */
    public final double retrieveCarProduction(final double accumulatedCars)
    {
        ArrayList<Point2D> xyPairs = new ArrayList<Point2D>();
        Point2D p = new Point2D.Double();
        p.setLocation(0, 0);
        xyPairs.add(p);
        p.setLocation(this.parametersNTM.getAccCritical1(), this.maxCapacity);
        xyPairs.add(p);
        p.setLocation(this.parametersNTM.getAccCritical2(), this.maxCapacity);
        xyPairs.add(p);
        p.setLocation(this.parametersNTM.getAccJam(), 0);
        xyPairs.add(p);
        double carProduction = NetworkFundamentalDiagram.PieceWiseLinear(xyPairs, accumulatedCars);
        return carProduction;
    }

    /**
     * @param accumulatedCars number of cars in Cell
     */
    public final void computeProduction(final double accumulatedCars)
    {
        double carProduction = retrieveCarProduction(accumulatedCars);
        this.productionSupply = Math.min(this.maxCapacity, carProduction); // supply
        this.speedSupply = this.productionSupply / accumulatedCars;

        double lowerBoundProduction = Math.max(0.05 * this.maxCapacity, this.productionSupply);
        double maxDemand = this.parametersNTM.getFreeSpeed().getValueSI() * accumulatedCars; // ask Victor
        this.productionDemand = Math.min(maxDemand, this.maxCapacity); // / demand
        this.speedDemand = this.productionDemand / accumulatedCars;

        this.productionElse = Math.min(lowerBoundProduction, this.productionDemand); // / else
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
    public void setMaxCapacity(double maxCapacity)
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


}
