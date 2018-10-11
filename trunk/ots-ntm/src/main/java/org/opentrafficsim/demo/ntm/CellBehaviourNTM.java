package org.opentrafficsim.demo.ntm;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.LinearDensityUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.LinearDensity;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.demo.ntm.fundamentaldiagrams.FundamentalDiagram;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version 26 Sep 2014 <br>
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

    /** CurrentSpeed: average current speed of Cars in this CELL. */
    private Speed freeSpeed;

    /** CurrentSpeed: average current speed of Cars in this CELL. */
    private Speed currentSpeed;

    /** CurrentSpeed: average current speed of Cars in this CELL. */
    private Duration currentTravelTime;

    /** */
    private Frequency maxCapacityNTMArea;

    /**
     * parametersNTM are: - id ID - accCritical1 low param - accCritical2 high param - accJam jam param - freeSpeed -
     * uncongested speed - roadLength length of all roads.
     */
    private ParametersNTM parametersNTM;

    /** */
    private Area area;

    /**
     * @param parametersNTM ParametersNTM; contains a set of params
     * @param area Area; that contains this behaviour
     */
    public CellBehaviourNTM(final Area area, final ParametersNTM parametersNTM)
    {
        this.parametersNTM = parametersNTM;
        // double maxCap = parametersNTM.getAccCritical().get(0) *
        // parametersNTM.getFreeSpeed().getInUnit(SpeedUnit.KM_PER_HOUR)
        // * parametersNTM.getRoadLength().getInUnit(LengthUnit.KILOMETER);
        double maxCap = parametersNTM.getCapacity().getInUnit(FrequencyUnit.PER_HOUR)
                * parametersNTM.getRoadLength().getInUnit(LengthUnit.KILOMETER);
        this.maxCapacityNTMArea = new Frequency(maxCap, FrequencyUnit.PER_HOUR);
        this.area = area;
    }

    /**
     * @param accumulatedCars double;
     * @return actualSpeed.
     */
    public Speed retrieveCurrentSpeed(final double accumulatedCars, final Length roadLength)
    {
        double densityPerUnitDouble = this.getAccumulatedCars() / roadLength.getInUnit(LengthUnit.KILOMETER);
        double speedDouble;
        if (densityPerUnitDouble > this.getParametersNTM().getAccCritical().get(0))
        {
            LinearDensity densityPerUnit = new LinearDensity(densityPerUnitDouble, LinearDensityUnit.PER_KILOMETER);
            Frequency capacityPerUnit = retrieveSupplyPerLengthUnit(accumulatedCars, roadLength, this.getParametersNTM());
            speedDouble = capacityPerUnit.getInUnit(FrequencyUnit.PER_HOUR)
                    / densityPerUnit.getInUnit(LinearDensityUnit.PER_KILOMETER);
        }
        else
        {
            speedDouble = this.getParametersNTM().getFreeSpeed().getInUnit(SpeedUnit.KM_PER_HOUR);
        }
        return this.setCurrentSpeed(new Speed(speedDouble, SpeedUnit.KM_PER_HOUR));
    }

    /**
     * {@inheritDoc}
     * @param accumulatedCars
     * @param roadLength
     * @param parametersNTM
     * @return
     */
    // @Override
    public Frequency retrieveSupplyPerLengthUnit(final Double accumulatedCars, final Length roadLength,
            final ParametersNTM parametersNTM)
    {
        Frequency supply = parametersNTM.getCapacity();
        double densityPerUnitDouble = this.getAccumulatedCars() / roadLength.getInUnit(LengthUnit.KILOMETER);
        if (densityPerUnitDouble > this.getParametersNTM().getAccCritical().get(1))
        {
            supply = retrieveDemandPerLengthUnit(accumulatedCars, roadLength, parametersNTM);
        }
        return supply;
    }

    /**
     * Retrieves car production from network fundamental diagram.
     * @param accumulatedCars double; number of cars in Cell
     * @param maximumCapacityArea based on area information
     * @param parametersNTM ParametersNTM;
     * @return carProduction
     */
    public final Frequency retrieveDemandPerLengthUnit(final double accumulatedCars, final Length roadLength,
            final ParametersNTM parametersNTM)
    {
        double densityPerUnitDouble = this.getAccumulatedCars() / roadLength.getInUnit(LengthUnit.KILOMETER);
        ArrayList<Point2D> xyPairs = new ArrayList<Point2D>();
        Point2D p = new Point2D.Double();
        p.setLocation(0, 0);
        xyPairs.add(p);
        p = new Point2D.Double();
        p.setLocation(parametersNTM.getAccCritical().get(0), parametersNTM.getCapacity().getInUnit(FrequencyUnit.PER_HOUR));
        xyPairs.add(p);
        p = new Point2D.Double();
        p.setLocation(parametersNTM.getAccCritical().get(1), parametersNTM.getCapacity().getInUnit(FrequencyUnit.PER_HOUR));
        xyPairs.add(p);
        p = new Point2D.Double();
        p.setLocation(parametersNTM.getAccCritical().get(2), 0);
        xyPairs.add(p);
        double carProduction =
                FundamentalDiagram.PieceWiseLinear(xyPairs, densityPerUnitDouble).getInUnit(FrequencyUnit.PER_HOUR);
        if (densityPerUnitDouble > parametersNTM.getAccCritical().get(1))
        {
            Double MINCAP = 0.1;
            carProduction = Math.max(MINCAP * parametersNTM.getCapacity().getInUnit(FrequencyUnit.PER_HOUR), carProduction);
        }
        return new Frequency(carProduction, FrequencyUnit.PER_HOUR);
    }

    /**
     * @return parametersNTM.
     */
    public final ParametersNTM getParametersNTM()
    {
        return this.parametersNTM;
    }

    /**
     * @param parametersNTM ParametersNTM; set parametersNTM.
     */
    public void setParametersNTM(ParametersNTM parametersNTM)
    {
        this.parametersNTM = parametersNTM;
    }

    /**
     * @return area.
     */
    public final Area getArea()
    {
        return this.area;
    }

    /**
     * @param area Area; set area.
     */
    public void setArea(final Area area)
    {
        this.area = area;
    }

    /**
     * @return maxCapacity
     */
    public final Frequency getMaxCapacityNTMArea()
    {
        return this.maxCapacityNTMArea;
    }

    /**
     * @param maxCapacity set maxCapacity.
     */
    public final void setMaxCapacityNTMArea(final Frequency maxCapacityNTMArea)
    {
        this.maxCapacityNTMArea = maxCapacityNTMArea;
    }

    /**
     * @return averageSpeed
     */
    public final Speed getCurrentSpeed()
    {
        return this.currentSpeed;
    }

    /**
     * @param currentSpeed Speed; set currentSpeed.
     * @return
     */
    public Speed setCurrentSpeed(Speed currentSpeed)
    {
        return this.currentSpeed = currentSpeed;
    }

    /**
     * @return currentTravelTime.
     */
    public Duration getCurrentTravelTime()
    {
        return this.currentTravelTime;
    }

    /**
     * @param time Duration; set currentTravelTime.
     * @return
     */
    public Duration setCurrentTravelTime(Duration time)
    {
        this.currentTravelTime = time;
        return null;
    }

    /**
     * @return freeSpeed.
     */
    public Speed getFreeSpeed()
    {
        this.freeSpeed = this.area.getAverageSpeed();
        return this.freeSpeed;
    }

    /**
     * @param freeSpeed Speed; set freeSpeed.
     */
    public void setFreeSpeed(Speed freeSpeed)
    {
        this.freeSpeed = freeSpeed;
    }

}
