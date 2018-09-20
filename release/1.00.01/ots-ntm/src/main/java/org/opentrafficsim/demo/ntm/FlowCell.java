package org.opentrafficsim.demo.ntm;

import java.awt.geom.Point2D;
import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.media.j3d.Bounds;

import nl.tudelft.simulation.dsol.animation.LocatableInterface;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.LinearDensityUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.djunits.value.vdouble.scalar.DoubleScalar.Abs;
import org.djunits.value.vdouble.scalar.Frequency;
import org.opentrafficsim.demo.ntm.Node.TrafficBehaviourType;
import org.opentrafficsim.demo.ntm.fundamentaldiagrams.FundamentalDiagram;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version 14 Oct 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */
public class FlowCell implements LocatableInterface
{
    /** Link length in a length unit. */
    private DoubleScalar.Rel<LengthUnit> cellLength;

    /** Link capacity in vehicles per hour. This is a mutable property (e.g., blockage). */
    private Frequency maxCapacity;

    /** Lanes in a link */
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
    public FlowCell(final DoubleScalar.Rel<LengthUnit> cellLength, final Frequency maxCapacity,
        DoubleScalar.Abs<SpeedUnit> speed, final int numberOfLanes, final TrafficBehaviourType behaviourType)
    {
        this.cellLength = cellLength;
        this.maxCapacity = maxCapacity;
        this.numberOfLanes = numberOfLanes;
        ParametersFundamentalDiagram parametersFD = new ParametersFundamentalDiagram(speed, maxCapacity, numberOfLanes);
        if (behaviourType == TrafficBehaviourType.FLOW)
        {
            this.cellBehaviour = new CellBehaviourFlow(null, parametersFD);
        }
    }

    /**
     * Retrieves car production from network fundamental diagram.
     * @param accumulatedCarsPerLengthUnit number of cars in Cell
     * @param maximumCapacity based on area information
     * @param param parameters FD
     * @return carProduction
     */
    public final Frequency retrieveCurrentInflowCapacity(final double accumulatedCarsPerLengthUnit,
        final Frequency maximumCapacity, final ParametersFundamentalDiagram param)
    {
        Frequency currentInflowCapacity;
        if (accumulatedCarsPerLengthUnit > param.getAccCritical().get(0))
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
            currentInflowCapacity = FundamentalDiagram.PieceWiseLinear(xyPairs, accumulatedCarsPerLengthUnit);
        }
        else
        {
            currentInflowCapacity = maximumCapacity;
        }
        return currentInflowCapacity;
    }

    /**
     * @param accumulatedCarsPerLengthUnit
     * @return actualSpeed.
     */
    public DoubleScalar.Abs<SpeedUnit> retrieveCurrentSpeed(final double accumulatedCarsPerLengthUnit)
    {
        double speedDouble;
        Frequency currentInflowCapacity =
            retrieveCurrentInflowCapacity(accumulatedCarsPerLengthUnit, this.maxCapacity, this.cellBehaviour
                .getParametersFundamentalDiagram());
        Abs<LinearDensityUnit> density =
            new DoubleScalar.Abs<LinearDensityUnit>(accumulatedCarsPerLengthUnit, LinearDensityUnit.PER_KILOMETER);
        if (density.getInUnit(LinearDensityUnit.PER_KILOMETER) > this.cellBehaviour.getParametersFundamentalDiagram()
            .getAccCritical().get(0))
        {
            speedDouble =
                currentInflowCapacity.getInUnit(FrequencyUnit.PER_HOUR) / density.getInUnit(LinearDensityUnit.PER_KILOMETER);
            // speedDouble =
            // Math.max(speedDouble, this.getCellBehaviourFlow().getParametersFundamentalDiagram().getFreeSpeed()
            // .getInUnit(SpeedUnit.KM_PER_HOUR));
        }
        else
        {
            speedDouble =
                this.cellBehaviour.getParametersFundamentalDiagram().getCapacity().getInUnit(FrequencyUnit.PER_HOUR)
                    / this.cellBehaviour.getParametersFundamentalDiagram().getAccCritical().get(0);
        }
        return this.setActualSpeed(new DoubleScalar.Abs<SpeedUnit>(speedDouble, SpeedUnit.KM_PER_HOUR));
    }

    /**
     * @param accumulatedCars
     * @return actualSpeed.
     */
    public DoubleScalar.Abs<TimeUnit> retrieveCurrentTravelTime()
    {
        double densityPerLengthUnit =
            this.getCellBehaviourFlow().getAccumulatedCars() / this.cellLength.getInUnit(LengthUnit.KILOMETER);
        double timeDouble =
            this.cellLength.getInUnit(LengthUnit.KILOMETER)
                / retrieveCurrentSpeed(densityPerLengthUnit).getInUnit(SpeedUnit.KM_PER_HOUR);
        double UPPERBOUND_TRAVELTIME_HOUR = 99;
        timeDouble = Math.min(UPPERBOUND_TRAVELTIME_HOUR, timeDouble);
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
    public Frequency getMaxCapacity()
    {
        return this.maxCapacity;
    }

    /**
     * @param maxCapacity set capacity.
     */
    public void setMaxCapacity(Frequency maxCapacity)
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
    public DoubleScalar.Abs<SpeedUnit> getCurrentSpeed()
    {
        return currentSpeed;
    }

    /**
     * @param currentSpeed set actualSpeed.
     */
    public DoubleScalar.Abs<SpeedUnit> setActualSpeed(DoubleScalar.Abs<SpeedUnit> currentSpeed)
    {
        this.currentSpeed = currentSpeed;
        return currentSpeed;
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
