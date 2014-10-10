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
public class CellBehaviourNTM extends CellBehaviour
{
    /** */
    private static final long serialVersionUID = 20140903L;

    /** */
    private double accumulatedCars;

    /** currentSpeed: average current speed of Cars in this CELL. */
    private DoubleScalar.Abs<SpeedUnit> currentSpeed;

    /** */
    private double maxCapacity;

    /** */
    private double supply;

    /** */
    private double demand;

    /** The number of cars that are heading for this Cell. */
    private double demandToEnter;

    /** */
    private double speedSupply;

    /** */
    private double speedDemand;


    /** */
    private double flow;

    /**
     * parametersNTM are: - id ID - accCritical1 low param - accCritical2 high param - accJam jam param - freeSpeed -
     * uncongested speed - roadLength length of all roads
     */
    private ParametersNTM parametersNTM;

    @SuppressWarnings("javadoc")
    private Area area;

    /**
     * @param parametersNTM contains a set of params
     * @param area that contains this behaviour
     */
    public CellBehaviourNTM(Area area, ParametersNTM parametersNTM)
    {
        this.parametersNTM = parametersNTM;
        this.maxCapacity =
                parametersNTM.getAccCritical1()
                        * parametersNTM.getFreeSpeed().getValueInUnit(SpeedUnit.KM_PER_HOUR);
    }

    /** {@inheritDoc} */
    //@Override
    public double retrieveSupply(final Double accumulatedCars, final Double maxCapacity, final ParametersNTM param)
    {
        double carProduction = retrieveCarProduction(accumulatedCars, maxCapacity, param);
        double productionSupply = Math.min(maxCapacity, carProduction); // supply
        return productionSupply;

    }

    /** {@inheritDoc} */
    //@Override
    public double retrieveDemand(final Double accumulatedCars, final Double maxCapacity, final ParametersNTM param)
    {
        double maxDemand = param.getFreeSpeed().getValueSI() * accumulatedCars; // ask Victor
        double productionDemand = Math.min(maxDemand, maxCapacity); // / demand
        return productionDemand;
    }

    /** {@inheritDoc} */
    //@Override
    public double computeAccumulation()
    {
        double accumulation = 0.0;
        return accumulation;
    }

    /**
     * Retrieves car production from network fundamental diagram.
     * @param accumulatedCars number of cars in Cell
     * @param maxCapacity
     * @param param
     * @return carProduction
     */
    public final double retrieveCarProduction(final double accumulatedCars, final double maxCapacity,
            final ParametersNTM param)
    {
        ArrayList<Point2D> xyPairs = new ArrayList<Point2D>();
        Point2D p = new Point2D.Double();
        p.setLocation(0, 0);
        xyPairs.add(p);
        p = new Point2D.Double();
        p.setLocation(param.getAccCritical1(), maxCapacity);
        xyPairs.add(p);
        p = new Point2D.Double();
        p.setLocation(param.getAccCritical2(), maxCapacity);
        xyPairs.add(p);
        p = new Point2D.Double();
        p.setLocation(param.getAccJam(), 0);
        xyPairs.add(p);
        double carProduction = NetworkFundamentalDiagram.PieceWiseLinear(xyPairs, accumulatedCars);
        return carProduction;
    }

    /**
     * @return parametersNTM.
     */
    public final ParametersNTM getParametersNTM()
    {
        return this.parametersNTM;
    }

    /**
     * @param parametersNTM set parametersNTM.
     */
    public void setParametersNTM(ParametersNTM parametersNTM)
    {
        this.parametersNTM = parametersNTM;
    }

    /**
     * @return area.
     */
    public Area getArea()
    {
        return this.area;
    }

    /**
     * @param area set area.
     */
    public void setArea(Area area)
    {
        this.area = area;
    }

    /**
     * @return averageSpeed
     */
    public final DoubleScalar.Abs<SpeedUnit> getCurrentSpeed()
    {
        return this.currentSpeed;
    }

    /**
     * @return accumulatedCars.
     */
    public double getAccumulatedCars()
    {
        return this.accumulatedCars;
    }

    /**
     * @param accumulatedCars set accumulatedCars.
     */
    public void setAccumulatedCars(double accumulatedCars)
    {
        this.accumulatedCars = accumulatedCars;
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
    public final double getSupply()
    {
        return this.supply;
    }

    /**
     * @param currentSpeed set currentSpeed.
     */
    public void setCurrentSpeed(DoubleScalar.Abs<SpeedUnit> currentSpeed)
    {
        this.currentSpeed = currentSpeed;
    }

    /**
     * @param supply set supply.
     */
    public void setSupply(double supply)
    {
        this.supply = supply;
    }

    /**
     * @param demand set demand.
     */
    public void setDemand(double demand)
    {
        this.demand = demand;
    }

    /**
     * @param speedSupply set speedSupply.
     */
    public void setSpeedSupply(double speedSupply)
    {
        this.speedSupply = speedSupply;
    }

    /**
     * @return productionDemand
     */
    public final double getDemand()
    {
        return this.demand;
    }

    // this.speedSupply = this.productionSupply / accumulatedCars;
    /**
     * @return speedSupply
     */
    public final double getSpeedSupply()
    {
        return this.speedSupply;
    }

    // this.speedDemand = this.productionDemand / accumulatedCars;
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
     * @return demandToEnter.
     */
    public final double getDemandToEnter()
    {
        return this.demandToEnter;
    }

    /**
     * @return flow.
     */
    public double getFlow()
    {
        return flow;
    }

    /**
     * @param flow set flow.
     */
    public void setFlow(double flow)
    {
        this.flow = flow;
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

    /*    *//**
     * not used
     * @param accumulatedCars number of cars in Cell
     */
    /*
     * public final void computeProductionElse(final double accumulatedCars) { double carProduction =
     * retrieveCarProduction(accumulatedCars); double production = Math.min(this.maxCapacity, carProduction); // supply
     * double lowerBoundProduction = Math.max(0.05 * this.maxCapacity, production); double maxDemand =
     * this.parametersNTM.getFreeSpeed().getValueSI() * accumulatedCars; // ask Victor double demand =
     * Math.min(maxDemand, this.maxCapacity); // / demand this.productionElse = Math.min(lowerBoundProduction, demand);
     * // / else this.speedElse = this.productionElse / accumulatedCars; // if (accumulationCars > 0) { //
     * this.currentSpeed = new DoubleScalarAbs<SpeedUnit>(carProduction / accumulatedCars, SpeedUnit.KM_PER_HOUR); // }
     * }
     */
}
