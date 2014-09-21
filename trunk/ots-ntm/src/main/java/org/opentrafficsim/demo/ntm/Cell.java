package org.opentrafficsim.demo.ntm;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import org.opentrafficsim.core.network.Zone;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.demo.ntm.fundamentaldiagrams.NetworkFundamentalDiagram;

/**
 * A Cell extends a Zone and is used for the NetworkTransmissionModel The Cells cover a preferably homogeneous area and
 * have their specific characteristics such as their free speed, a capacity and an NFD diagram A trip matrix quantifies
 * the amount of trips between Cells in a network. The connection of neighbouring Cells are expressed by Links
 * (connectors) The cost to go from one to another Cell is quantified through the weights on the Connectors
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 4 Sep 2014 <br>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @param <ID>
 */

public class Cell<ID> extends Zone
{
    /** */
    private static final long serialVersionUID = 20140903L;

    /** id. */
    private final ID id;

    /** parameter accCritical1: when production stops to increase (at maxProduction). */
    private double accCritical1;

    /** parameter accCritical2: when production starts to decrease (from maxProduction to zero). */
    private double accCritical2;

    /** parameter accJam: complete grid lock. */
    private double accJam;

    /**
     * freeSpeed: average free speed of cars in CELL.
     */
    private final DoubleScalar.Abs<SpeedUnit> freeSpeed;

    /**
     * freeSpeed: average free speed of cars in CELL.
     */
    private final DoubleScalar.Abs<LengthUnit> roadLength;

    /**
     * neighbourCells: the Cells that are directly connected to this Cell.
     */
    private ArrayList<Cell<ID>> neighbourCells = new ArrayList<Cell<ID>>();

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
     * @param id
     * @param accCritical1
     * @param accCritical2
     * @param accJam
     * @param freeSpeed
     * @param roadLength
     */
    public Cell(final ID id, final double accCritical1, final double accCritical2, final double accJam,
            final DoubleScalar.Abs<SpeedUnit> freeSpeed, final DoubleScalar.Abs<LengthUnit> roadLength)
    {
        super();
        this.id = id;
        this.accCritical1 = accCritical1;
        this.accCritical2 = accCritical2;
        this.accJam = accJam;
        this.freeSpeed = freeSpeed;
        this.roadLength = roadLength;
        this.maxCapacity = this.freeSpeed.getValueSI() * this.accCritical1;
    }

    /**
     * @param accumulatedCars
     * @return carProduction
     */
    public double retrieveCarProduction(final double accumulatedCars)
    {
        ArrayList<Point2D> xyPairs = new ArrayList<Point2D>();
        Point2D p = new Point2D.Double();
        p.setLocation(0, 0);
        xyPairs.add(p);
        p.setLocation(this.accCritical1, this.maxCapacity);
        xyPairs.add(p);
        p.setLocation(this.accCritical2, this.maxCapacity);
        xyPairs.add(p);
        p.setLocation(this.accJam, 0);
        xyPairs.add(p);
        double carProduction = NetworkFundamentalDiagram.PieceWiseLinear(xyPairs, accumulatedCars);
        return carProduction;
    }

    /**
     * @param accumulatedCars
     */
    public void computeProduction(final double accumulatedCars)
    {
        double carProduction = retrieveCarProduction(accumulatedCars);
        this.productionSupply = Math.min(this.maxCapacity, carProduction); // supply
        this.speedSupply = this.productionSupply / accumulatedCars;

        double lowerBoundProduction = Math.max(0.05 * this.maxCapacity, this.productionSupply);
        double maxDemand = this.freeSpeed.getValueSI() * accumulatedCars; // ask Victor
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
    public DoubleScalar.Abs<SpeedUnit> getCurrentSpeed()
    {
        return this.currentSpeed;
    }

    /**
     * @return accCritical1
     */
    public double getAccCritical1()
    {
        return this.accCritical1;
    }

    /**
     * @param accCritical1
     */
    public void setAccCritical1(double accCritical1)
    {
        this.accCritical1 = accCritical1;
    }

    /**
     * @return this.id
     */
    public ID getId()
    {
        return this.id;
    }

    /**
     * @return this.accCritical2
     */
    public double getAccCritical2()
    {
        return this.accCritical2;
    }

    /**
     * @return this.accJam
     */
    public double getAccJam()
    {
        return this.accJam;
    }

    /**
     * @return maxCapacity
     */
    public double getMaxCapacity()
    {
        return this.maxCapacity;
    }

    /**
     * @return productionSupply
     */
    public double getProductionSupply()
    {
        return this.productionSupply;
    }

    /**
     * @return productionDemand
     */
    public double getProductionDemand()
    {
        return this.productionDemand;
    }

    /**
     * @return productionElse
     */
    public double getProductionElse()
    {
        return this.productionElse;
    }

    /**
     * @return speedSupply
     */
    public double getSpeedSupply()
    {
        return this.speedSupply;
    }

    /**
     * @return speedDemand
     */
    public double getSpeedDemand()
    {
        return this.speedDemand;
    }

    /**
     * @param speedDemand set speedDemand
     */
    public void setSpeedDemand(double speedDemand)
    {
        this.speedDemand = speedDemand;
    }

    /**
     * @return speedElse
     */
    public double getSpeedElse()
    {
        return this.speedElse;
    }

    /**
     * @return freeSpeed
     */
    public DoubleScalar.Abs<SpeedUnit> getFreeSpeed()
    {
        return this.freeSpeed;
    }

    /**
     * @return roadLength
     */
    public DoubleScalar.Abs<LengthUnit> getRoadLength()
    {
        return this.roadLength;
    }

    /**
     * @return neighbourCells
     */
    public ArrayList<Cell<ID>> getNeighbourCells()
    {
        return this.neighbourCells;
    }

    /**
     * @param neighbourCells set neighbourCells
     */
    public void setNeighbourCells(ArrayList<Cell<ID>> neighbourCells)
    {
        this.neighbourCells = neighbourCells;
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        DoubleScalar.Abs<SpeedUnit> freeSpeed = new DoubleScalar.Abs<SpeedUnit>(40, SpeedUnit.KM_PER_HOUR);
        System.out.println("freeSpeed is " + freeSpeed);
        System.out.println("freeSpeed in SI is " + freeSpeed.getValueSI());
        System.out.println("freeSpeed in mph is " + freeSpeed.getValueInUnit(SpeedUnit.MILE_PER_HOUR));

    }
}
