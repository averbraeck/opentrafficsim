package org.opentrafficsim.demo.ntm;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;

import org.opentrafficsim.core.unit.FrequencyUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.LinearDensityUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel;
import org.opentrafficsim.demo.ntm.fundamentaldiagrams.FundamentalDiagram;

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

    /** currentSpeed: average current speed of Cars in this CELL. */
    private DoubleScalar.Abs<SpeedUnit> freeSpeed;

    /** currentSpeed: average current speed of Cars in this CELL. */
    private DoubleScalar.Abs<SpeedUnit> currentSpeed;

    /** currentSpeed: average current speed of Cars in this CELL. */
    private Rel<TimeUnit> currentTravelTime;

    /** */
    private Abs<FrequencyUnit> maxCapacityNTMArea;

    /**
     * parametersNTM are: - id ID - accCritical1 low param - accCritical2 high param - accJam jam param - freeSpeed -
     * uncongested speed - roadLength length of all roads.
     */
    private ParametersNTM parametersNTM;

    /** */
    private Area area;

    /**
     * @param parametersNTM contains a set of params
     * @param area that contains this behaviour
     */
    public CellBehaviourNTM(final Area area, final ParametersNTM parametersNTM)
    {
        this.parametersNTM = parametersNTM;
        // double maxCap = parametersNTM.getAccCritical().get(0) *
        // parametersNTM.getFreeSpeed().getInUnit(SpeedUnit.KM_PER_HOUR)
        // * parametersNTM.getRoadLength().getInUnit(LengthUnit.KILOMETER);
        double maxCap =
                parametersNTM.getCapacity().getInUnit(FrequencyUnit.PER_HOUR)
                        * parametersNTM.getRoadLength().getInUnit(LengthUnit.KILOMETER);
        this.maxCapacityNTMArea = new Abs<FrequencyUnit>(maxCap, FrequencyUnit.PER_HOUR);
        this.area = area;
    }

    /**
     * @param accumulatedCars
     * @return actualSpeed.
     */
    public DoubleScalar.Abs<SpeedUnit> retrieveCurrentSpeed(final double accumulatedCars,
            final Rel<LengthUnit>roadLength)
    {
        double densityPerUnitDouble =
                this.getAccumulatedCars() / roadLength.getInUnit(LengthUnit.KILOMETER);
        double speedDouble;
        if (densityPerUnitDouble > this.getParametersNTM().getAccCritical().get(0))
        {
            Abs<LinearDensityUnit> densityPerUnit =
                    new DoubleScalar.Abs<LinearDensityUnit>(densityPerUnitDouble, LinearDensityUnit.PER_KILOMETER);
            Abs<FrequencyUnit> capacityPerUnit =
                    retrieveSupplyPerLengthUnit(accumulatedCars, roadLength, this.getParametersNTM());
            speedDouble =
                    capacityPerUnit.getInUnit(FrequencyUnit.PER_HOUR)
                            / densityPerUnit.getInUnit(LinearDensityUnit.PER_KILOMETER);
        }
        else
        {
            speedDouble = this.getParametersNTM().getFreeSpeed().getInUnit(SpeedUnit.KM_PER_HOUR);
        }
        return this.setCurrentSpeed(new DoubleScalar.Abs<SpeedUnit>(speedDouble, SpeedUnit.KM_PER_HOUR));
    }


    /**
     * {@inheritDoc}
     * @param accumulatedCars
     * @param roadLength 
     * @param parametersNTM
     * @return
     */
    // @Override
    public Abs<FrequencyUnit> retrieveSupplyPerLengthUnit(final Double accumulatedCars,
            final Rel<LengthUnit>roadLength, final ParametersNTM parametersNTM)
    {
        Abs<FrequencyUnit> supply = parametersNTM.getCapacity();
        double densityPerUnitDouble =
                this.getAccumulatedCars() / roadLength.getInUnit(LengthUnit.KILOMETER);
        if (densityPerUnitDouble > this.getParametersNTM().getAccCritical().get(1))
        {
            supply = retrieveDemandPerLengthUnit(accumulatedCars, roadLength, parametersNTM);
        }
        return supply;
    }

    /**
     * Retrieves car production from network fundamental diagram.
     * @param accumulatedCars number of cars in Cell
     * @param maximumCapacityArea based on area information
     * @param parametersNTM
     * @return carProduction
     */
    public final Abs<FrequencyUnit> retrieveDemandPerLengthUnit(final double accumulatedCars, final Rel<LengthUnit>roadLength, final ParametersNTM parametersNTM)
    {
        double densityPerUnitDouble =
                this.getAccumulatedCars() / roadLength.getInUnit(LengthUnit.KILOMETER);
        ArrayList<Point2D> xyPairs = new ArrayList<Point2D>();
        Point2D p = new Point2D.Double();
        p.setLocation(0, 0);
        xyPairs.add(p);
        p = new Point2D.Double();
        p.setLocation(parametersNTM.getAccCritical().get(0),
                parametersNTM.getCapacity().getInUnit(FrequencyUnit.PER_HOUR));
        xyPairs.add(p);
        p = new Point2D.Double();
        p.setLocation(parametersNTM.getAccCritical().get(1),
                parametersNTM.getCapacity().getInUnit(FrequencyUnit.PER_HOUR));
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
        return new DoubleScalar.Abs<FrequencyUnit>(carProduction, FrequencyUnit.PER_HOUR);
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
    public final Area getArea()
    {
        return this.area;
    }

    /**
     * @param area set area.
     */
    public void setArea(final Area area)
    {
        this.area = area;
    }

    /**
     * @return maxCapacity
     */
    public final Abs<FrequencyUnit> getMaxCapacityNTMArea()
    {
        return this.maxCapacityNTMArea;
    }

    /**
     * @param maxCapacity set maxCapacity.
     */
    public final void setMaxCapacityNTMArea(final Abs<FrequencyUnit> maxCapacityNTMArea)
    {
        this.maxCapacityNTMArea = maxCapacityNTMArea;
    }

    /**
     * @return averageSpeed
     */
    public final DoubleScalar.Abs<SpeedUnit> getCurrentSpeed()
    {
        return this.currentSpeed;
    }

    /**
     * @param currentSpeed set currentSpeed.
     * @return
     */
    public Abs<SpeedUnit> setCurrentSpeed(DoubleScalar.Abs<SpeedUnit> currentSpeed)
    {
        return this.currentSpeed = currentSpeed;
    }


    /**
     * @return currentTravelTime.
     */
    public Rel<TimeUnit> getCurrentTravelTime()
    {
        return currentTravelTime;
    }

    /**
     * @param time set currentTravelTime.
     * @return
     */
    public Rel<TimeUnit> setCurrentTravelTime(Rel<TimeUnit> time)
    {
        this.currentTravelTime = time;
        return null;
    }

    /**
     * @return freeSpeed.
     */
    public DoubleScalar.Abs<SpeedUnit> getFreeSpeed()
    {
        this.freeSpeed = this.area.getAverageSpeed();
        return this.freeSpeed;
    }

    /**
     * @param freeSpeed set freeSpeed.
     */
    public void setFreeSpeed(DoubleScalar.Abs<SpeedUnit> freeSpeed)
    {
        this.freeSpeed = freeSpeed;
    }

}
