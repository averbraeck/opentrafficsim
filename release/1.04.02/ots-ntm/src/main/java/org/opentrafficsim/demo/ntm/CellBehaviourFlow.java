package org.opentrafficsim.demo.ntm;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import org.djunits.unit.FrequencyUnit;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.demo.ntm.fundamentaldiagrams.FundamentalDiagram;

/**
 * The CellBehaviourFlow is used for cell transmission models and can be linked to the cells of a Link
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
public class CellBehaviourFlow extends CellBehaviour
{
    /** */
    private static final long serialVersionUID = 20140903L;

    /** CurrentSpeed: average current speed of Cars in this CELL. */
    private Speed currentSpeed;

    /**
     * parametersNTM are: - id ID - accCritical1 low param - accCritical2 high param - accJam jam param - freeSpeed -
     * uncongested speed - roadLength length of all roads.
     */
    private ParametersFundamentalDiagram parametersFundamentalDiagram;

    /**
     * @param parametersFD ParametersFundamentalDiagram; contains a set of params
     * @param area Area; that contains this behaviour
     */
    public CellBehaviourFlow(final Area area, ParametersFundamentalDiagram parametersFD)
    {
        if (parametersFD == null)
        {
            parametersFD = new ParametersFundamentalDiagram();
            System.out.println("Cell BehaviourfLOW at line 60: should not happen that speed and capacity are not set");

        }
        this.setParametersFundamentalDiagram(parametersFD);
        // parametersFD.getAccCritical().get(0) * parametersFD.getFreeSpeed().getInUnit(SpeedUnit.KM_PER_HOUR);
    }

    /**
     * {@inheritDoc}
     * @param accumulatedCars
     * @param param
     * @param numberOfLanes
     * @return car
     */
    // @Override
    public Frequency retrieveSupply(final Double accumulatedCars, final ParametersFundamentalDiagram param)
    {
        Frequency supply;
        if (accumulatedCars > param.getAccCritical().get(0))
        {
            supply = retrieveFD(accumulatedCars, param);
        }
        else
        {
            supply = param.getCapacity();
        }
        return supply;
    }

    /**
     * Retrieves car production from network fundamental diagram.
     * @param accumulatedCars double; number of cars in Cell
     * @param maximumCapacity based on area information
     * @param param ParametersFundamentalDiagram; kkk
     * @param numberOfLanes
     * @return carProduction
     */
    public final Frequency retrieveDemand(final double accumulatedCars, final ParametersFundamentalDiagram param)
    {
        Frequency demand;
        if (accumulatedCars <= param.getAccCritical().get(0))
        {
            demand = retrieveFD(accumulatedCars, param);
        }
        else
        {
            demand = param.getCapacity();
        }
        return demand;
    }

    /**
     * Retrieves car production from network fundamental diagram.
     * @param accumulatedCars double; number of cars in Cell
     * @param maximumCapacity based on area information
     * @param param ParametersFundamentalDiagram; kkk
     * @param numberOfLanes
     * @return carProduction
     */
    public final Frequency retrieveFD(final double accumulatedCars, final ParametersFundamentalDiagram param)
    {
        ArrayList<Point2D> xyPairs = new ArrayList<Point2D>();
        Point2D p = new Point2D.Double();
        // starting point
        p.setLocation(0, 0);
        xyPairs.add(p);
        p = new Point2D.Double();
        // the point of maximum capacity
        p.setLocation(param.getAccCritical().get(0), param.getCapacity().getInUnit(FrequencyUnit.PER_HOUR));
        xyPairs.add(p);
        p = new Point2D.Double();
        // the final breakdown
        p.setLocation(param.getAccCritical().get(1), 0);
        xyPairs.add(p);
        return FundamentalDiagram.PieceWiseLinear(xyPairs, accumulatedCars);
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
     */
    public final void setCurrentSpeed(final Speed currentSpeed)
    {
        this.currentSpeed = currentSpeed;
    }

    /**
     * @return parametersFundamentalDiagram.
     */
    public final ParametersFundamentalDiagram getParametersFundamentalDiagram()
    {
        return this.parametersFundamentalDiagram;
    }

    /**
     * @param parametersFundamentalDiagram ParametersFundamentalDiagram; set parametersFundamentalDiagram.
     */
    public final void setParametersFundamentalDiagram(final ParametersFundamentalDiagram parametersFundamentalDiagram)
    {
        this.parametersFundamentalDiagram = parametersFundamentalDiagram;
    }

}
