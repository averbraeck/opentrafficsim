package org.opentrafficsim.demo.ntm;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;

import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.demo.ntm.fundamentaldiagrams.FundamentalDiagram;

/**The CellBehaviourFlow is used for cell transmission models and can be linked to the cells of a Link
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

    /** */
    private double maxCapacity;

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
            
            ArrayList<Double> critic1 = new ArrayList<Double>();  
            //TODO hard coded: should be adjusted!!!!!!!!!!!-
            
            critic1.add(1500.0);
            parametersFD = new ParametersFundamentalDiagram(critic1, new DoubleScalar.Abs<SpeedUnit>(70.0, SpeedUnit.KM_PER_HOUR));
            
        }

        this.parametersFundamentalDiagram = parametersFD;

        this.maxCapacity =
                parametersFD.getAccCritical().get(0) * parametersFD.getFreeSpeed().getInUnit(SpeedUnit.KM_PER_HOUR);
    }  
    
    /**
     * @return numberOfTripsTo.
     */
    public HashMap<String, Double> getNumberOfTripsTo()
    {
        return numberOfTripsTo;
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
    public double retrieveSupply(final Double accumulatedCars, final Double maximumCapacity, final ParametersNTM param)
    {
        double carProduction = maximumCapacity;
        if (accumulatedCars > param.getAccCritical().get(0))
        {
            carProduction = retrieveCarProduction(accumulatedCars, maximumCapacity, param);
        }
        double productionSupply = Math.min(maximumCapacity, carProduction); // supply
        return productionSupply;
    }

    /**
     * {@inheritDoc}
     * @param accumulatedCars
     * @param maxCapacity
     * @param param
     * @return
     */
    // @Override
    public double retrieveDemand(final Double accumulatedCars, final Double maximumCapacity, final ParametersNTM param)
    {
        double productionDemand = retrieveCarProduction(accumulatedCars, maximumCapacity, param);
        return productionDemand;
    }

    /** {@inheritDoc} */
    // @Override
    public double computeAccumulation()
    {
        double accumulation = 0.0;
        return accumulation;
    }

    /**
     * Retrieves car production from network fundamental diagram.
     * @param accumulatedCars number of cars in Cell
     * @param maximumCapacity based on area information
     * @param param  kkk
     * @return carProduction
     */
    public final double retrieveCarProduction(final double accumulatedCars, final double maximumCapacity,
            final ParametersNTM param)
    {
        ArrayList<Point2D> xyPairs = new ArrayList<Point2D>();
        Point2D p = new Point2D.Double();
        p.setLocation(0, 0);
        xyPairs.add(p);
        p = new Point2D.Double();
        p.setLocation(param.getAccCritical().get(0), maximumCapacity);
        xyPairs.add(p);
        p = new Point2D.Double();
        p.setLocation(param.getAccCritical().get(1), 0);
        xyPairs.add(p);
        double carProduction = FundamentalDiagram.PieceWiseLinear(xyPairs, accumulatedCars);
        return carProduction;
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
     * @param currentSpeed set currentSpeed.
     */
    public final void setCurrentSpeed(final DoubleScalar.Abs<SpeedUnit> currentSpeed)
    {
        this.currentSpeed = currentSpeed;
    }

    
}
