package org.opentrafficsim.demo.ntm;

import java.awt.geom.Point2D;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.media.j3d.Bounds;

import org.opentrafficsim.core.unit.FrequencyUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.LinearDensityUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs;
import org.opentrafficsim.demo.ntm.Node.TrafficBehaviourType;
import org.opentrafficsim.demo.ntm.fundamentaldiagrams.FundamentalDiagram;

import com.vividsolutions.jts.geom.Geometry;

import nl.tudelft.simulation.dsol.animation.LocatableInterface;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 14 Oct 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */
public class FlowCell implements LocatableInterface
{
    /** link length in a length unit. */
    private DoubleScalar.Rel<LengthUnit> cellLength;

    /** link capacity in vehicles per hour. This is a mutable property (e.g., blockage). */
    private DoubleScalar.Abs<FrequencyUnit> maxCapacity;

    /** lanes in a link */
    private int numberOfLanes;

    /** SPEEDAB class java.lang.Double 120.0. */
    private DoubleScalar.Abs<SpeedUnit> currentSpeed;

    /** SPEEDAB class java.lang.Double 120.0. */
    private DoubleScalar.Abs<TimeUnit> currentTravelTime;

    /** */
    private CellBehaviourFlow cellBehaviour;

    /**
     * @param cellLength
     * @param maxCapacity
     * @param speed
     * @param numberOfLanes
     * @param behaviourType
     */
    public FlowCell(final DoubleScalar.Rel<LengthUnit> cellLength, final DoubleScalar.Abs<FrequencyUnit> maxCapacity,
            DoubleScalar.Abs<SpeedUnit> speed, final int numberOfLanes, final TrafficBehaviourType behaviourType)
    {
        this.cellLength = cellLength;
        this.maxCapacity = maxCapacity;
        this.numberOfLanes = numberOfLanes;
        ParametersFundamentalDiagram parametersFD =
                new ParametersFundamentalDiagram(speed, maxCapacity, numberOfLanes, cellLength);
        if (behaviourType == TrafficBehaviourType.FLOW)
        {
            this.cellBehaviour = new CellBehaviourFlow(null, parametersFD);
        }
    }

    /**
     * Retrieves car production from network fundamental diagram.
     * @param accumulatedCars number of cars in Cell
     * @param maximumCapacity based on area information
     * @param param parameters FD
     * @return carProduction
     */
    public final Abs<FrequencyUnit> retrieveActualCapacity(final double accumulatedCars,
            final Abs<FrequencyUnit> maximumCapacity, final ParametersFundamentalDiagram param)
    {
        Abs<FrequencyUnit> actualCapacity;
        if (accumulatedCars > param.getAccCritical().get(0))
        {
            ArrayList<Point2D> xyPairs = new ArrayList<Point2D>();
            Point2D p = new Point2D.Double();
            p.setLocation(0, 0);
            xyPairs.add(p);
            p = new Point2D.Double();
            p.setLocation(param.getAccCritical().get(0), maximumCapacity.getInUnit());
            xyPairs.add(p);
            p = new Point2D.Double();
            p.setLocation(param.getAccCritical().get(1), 0);
            xyPairs.add(p);
            actualCapacity = FundamentalDiagram.PieceWiseLinear(xyPairs, accumulatedCars);
        }
        else
        {
            actualCapacity = maximumCapacity;
        }
        return actualCapacity;
    }

    /**
     * @param accumulatedCars
     * @return actualSpeed.
     */
    public DoubleScalar.Abs<SpeedUnit> retrieveCurrentSpeed(final double accumulatedCars)
    {
        Abs<FrequencyUnit> actualCapacity =
                retrieveActualCapacity(accumulatedCars, this.maxCapacity,
                        this.cellBehaviour.getParametersFundamentalDiagram());
        this.cellBehaviour.getParametersFundamentalDiagram();
        double densityDouble =
                this.cellLength.getInUnit(LengthUnit.KILOMETER) * this.cellBehaviour.getAccumulatedCars()
                        / this.numberOfLanes;
        Abs<LinearDensityUnit> density =
                new DoubleScalar.Abs<LinearDensityUnit>(densityDouble, LinearDensityUnit.PER_KILOMETER);
        double speedDouble =
                actualCapacity.getInUnit(FrequencyUnit.PER_HOUR) / density.getInUnit(LinearDensityUnit.PER_KILOMETER);
        return this.setActualSpeed(new DoubleScalar.Abs<SpeedUnit>(speedDouble, SpeedUnit.KM_PER_HOUR));
    }

    /**
     * @param accumulatedCars
     * @return actualSpeed.
     */
    public DoubleScalar.Abs<TimeUnit> retrieveCurrentTravelTime()
    {

        double timeDouble =
                this.cellLength.getInUnit(LengthUnit.KILOMETER)
                        / retrieveCurrentSpeed(this.getCellBehaviourFlow().getAccumulatedCars()).getInUnit(
                                SpeedUnit.KM_PER_HOUR);
        return this.setCurrentTravelTime(new DoubleScalar.Abs(timeDouble, TimeUnit.HOUR));
    }

    /** {@inheritDoc} */
    @Override
    public DirectedPoint getLocation() throws RemoteException
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Bounds getBounds() throws RemoteException
    {
        return null;
    }

    /**
     * @return cellLength.
     */
    public DoubleScalar<LengthUnit> getCellLength()
    {
        return this.cellLength;
    }

    /**
     * @param cellLength set cellLength.
     */
    public void setCellLength(DoubleScalar.Rel<LengthUnit> cellLength)
    {
        this.cellLength = cellLength;
    }

    /**
     * @return capacity.
     */
    public DoubleScalar.Abs<FrequencyUnit> getMaxCapacity()
    {
        return this.maxCapacity;
    }

    /**
     * @param maxCapacity set capacity.
     */
    public void setMaxCapacity(DoubleScalar.Abs<FrequencyUnit> maxCapacity)
    {
        this.maxCapacity = maxCapacity;
    }

    /**
     * @return cellBehaviour.
     */
    public CellBehaviourFlow getCellBehaviourFlow()
    {
        return this.cellBehaviour;
    }

    /**
     * @param cellBehaviour set cellBehaviour.
     */
    public void setCellBehaviourFlow(CellBehaviourFlow cellBehaviour)
    {
        this.cellBehaviour = cellBehaviour;
    }

    /**
     * @return numberOfLanes.
     */
    public int getNumberOfLanes()
    {
        return this.numberOfLanes;
    }

    /**
     * @param numberOfLanes set numberOfLanes.
     */
    public void setNumberOfLanes(int numberOfLanes)
    {
        this.numberOfLanes = numberOfLanes;
    }

    /**
     * @return actualSpeed.
     */
    public DoubleScalar.Abs<SpeedUnit> getActualSpeed()
    {
        return currentSpeed;
    }

    /**
     * @param actualSpeed set actualSpeed.
     */
    public DoubleScalar.Abs<SpeedUnit> setActualSpeed(DoubleScalar.Abs<SpeedUnit> actualSpeed)
    {
        this.currentSpeed = actualSpeed;
        return actualSpeed;
    }

    /**
     * @return currentTravelTime.
     */
    public DoubleScalar.Abs<TimeUnit> getCurrentTravelTime()
    {
        return currentTravelTime;
    }

    /**
     * @param currentTravelTime set currentTravelTime.
     */
    public DoubleScalar.Abs<TimeUnit> setCurrentTravelTime(DoubleScalar.Abs<TimeUnit> currentTravelTime)
    {
        this.currentTravelTime = currentTravelTime;
        return currentTravelTime;
    }

}
