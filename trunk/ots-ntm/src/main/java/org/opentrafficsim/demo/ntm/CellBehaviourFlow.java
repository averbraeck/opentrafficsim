package org.opentrafficsim.demo.ntm;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;

import org.opentrafficsim.core.unit.FrequencyUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SIUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs;
import org.opentrafficsim.demo.ntm.fundamentaldiagrams.FundamentalDiagram;

/**
 * The CellBehaviourFlow is used for cell transmission models and can be linked to the cells of a Link
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
public class CellBehaviourFlow extends CellBehaviour
{
    /** */
    private static final long serialVersionUID = 20140903L;

    /** */
    private HashMap<String, Double> numberOfTripsTo;

    /** currentSpeed: average current speed of Cars in this CELL. */
    private DoubleScalar.Abs<SpeedUnit> currentSpeed;

    /**
     * parametersNTM are: - id ID - accCritical1 low param - accCritical2 high param - accJam jam param - freeSpeed -
     * uncongested speed - roadLength length of all roads.
     */
    private ParametersFundamentalDiagram parametersFundamentalDiagram;

    /** */
    private Area area;

    /**
     * @param parametersFD contains a set of params
     * @param area that contains this behaviour
     */
    public CellBehaviourFlow(final Area area, ParametersFundamentalDiagram parametersFD)
    {
        if (parametersFD == null)
        {
            parametersFD =
                    new ParametersFundamentalDiagram(new DoubleScalar.Abs<SpeedUnit>(70.0, SpeedUnit.KM_PER_HOUR),
                            new DoubleScalar.Abs<FrequencyUnit>(2000, FrequencyUnit.PER_HOUR));
            System.out.println("Cell BehaviourfLOW at line 60: should not happen that speed and capacity are not set");

        }
        this.setParametersFundamentalDiagram(parametersFD);
        // parametersFD.getAccCritical().get(0) * parametersFD.getFreeSpeed().getInUnit(SpeedUnit.KM_PER_HOUR);
    }

    /**
     * @param parametersFD contains a set of params
     * @param area that contains this behaviour
     * @param maxCapacityPerLane
     * @param maxSpeed
     */
    public CellBehaviourFlow(final Area area, ParametersFundamentalDiagram parametersFD,
            DoubleScalar.Abs<FrequencyUnit> maxCapacityPerLane, DoubleScalar.Abs<SpeedUnit> maxSpeed)
    {
        if (parametersFD == null)
        {
            parametersFD = new ParametersFundamentalDiagram(maxSpeed, maxCapacityPerLane);

        }
        this.setParametersFundamentalDiagram(parametersFD);
        // parametersFD.getAccCritical().get(0) * parametersFD.getFreeSpeed().getInUnit(SpeedUnit.KM_PER_HOUR);
    }

    /**
     * @return numberOfTripsTo.
     */
    public HashMap<String, Double> getNumberOfTripsTo()
    {
        return this.numberOfTripsTo;
    }

    /**
     * @param numberOfTripsTo set numberOfTripsTo.
     */
    public void setNumberOfTripsTo(HashMap<String, Double> numberOfTripsTo)
    {
        this.numberOfTripsTo = numberOfTripsTo;
    }

    /**
     * {@inheritDoc}
     * @param accumulatedCars
     * @param maximumCapacity
     * @param param
     * @return
     */
    // @Override
    public DoubleScalar.Abs<FrequencyUnit> retrieveSupply(final Double accumulatedCars,
            final ParametersFundamentalDiagram param, int numberOfLanes)
    {
        DoubleScalar.Abs<FrequencyUnit> carProduction;
        if (accumulatedCars / numberOfLanes > param.getAccCritical().get(0))
        {
            carProduction = retrieveDemand(accumulatedCars, param, numberOfLanes);
        }
        else
        {
            carProduction =
                    new DoubleScalar.Abs<FrequencyUnit>(param.getMaxCapacityPerLane().getSI() * numberOfLanes,
                            FrequencyUnit.PER_SECOND);
        }
        return carProduction;
    }

    /**
     * Retrieves car production from network fundamental diagram.
     * @param accumulatedCars number of cars in Cell
     * @param maximumCapacity based on area information
     * @param param kkk
     * @return carProduction
     */
    public final DoubleScalar.Abs<FrequencyUnit> retrieveDemand(final double accumulatedCars,
            final ParametersFundamentalDiagram param, int numberOfLanes)
    {
        ArrayList<Point2D> xyPairs = new ArrayList<Point2D>();
        Point2D p = new Point2D.Double();
        // starting point
        p.setLocation(0, 0);
        xyPairs.add(p);
        p = new Point2D.Double();
        // the point of maximum capacity
        p.setLocation(param.getAccCritical().get(0), param.getMaxCapacityPerLane().doubleValue() * 3600);
        xyPairs.add(p);
        p = new Point2D.Double();
        // the final breakdown
        p.setLocation(param.getAccCritical().get(1), 0);
        xyPairs.add(p);
        DoubleScalar.Abs<FrequencyUnit> carProductionPerLane =
                FundamentalDiagram.PieceWiseLinear(xyPairs, accumulatedCars / numberOfLanes);
        return new DoubleScalar.Abs<FrequencyUnit>(carProductionPerLane.getSI() * numberOfLanes,
                FrequencyUnit.PER_SECOND);
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
     */
    public final void setCurrentSpeed(final DoubleScalar.Abs<SpeedUnit> currentSpeed)
    {
        this.currentSpeed = currentSpeed;
    }

    /**
     * @return parametersFundamentalDiagram.
     */
    public ParametersFundamentalDiagram getParametersFundamentalDiagram()
    {
        return this.parametersFundamentalDiagram;
    }

    /**
     * @param parametersFundamentalDiagram set parametersFundamentalDiagram.
     */
    public void setParametersFundamentalDiagram(ParametersFundamentalDiagram parametersFundamentalDiagram)
    {
        this.parametersFundamentalDiagram = parametersFundamentalDiagram;
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

}
