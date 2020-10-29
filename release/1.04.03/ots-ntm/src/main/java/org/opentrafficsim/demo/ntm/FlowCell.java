package org.opentrafficsim.demo.ntm;

import java.awt.geom.Point2D;
import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.media.j3d.Bounds;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.LinearDensityUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.LinearDensity;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.demo.ntm.NTMNode.TrafficBehaviourType;
import org.opentrafficsim.demo.ntm.fundamentaldiagrams.FundamentalDiagram;

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.language.d3.DirectedPoint;

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
public class FlowCell implements Locatable
{
    /** Link length in a length unit. */
    private Length cellLength;

    /** Link capacity in vehicles per hour. This is a mutable property (e.g., blockage). */
    private Frequency maxCapacity;

    /** Lanes in a link */
    private int numberOfLanes;

    /** SPEEDAB class java.lang.Double 120.0. */
    private Speed currentSpeed;

    /** SPEEDAB class java.lang.Double 120.0. */
    private Duration currentTravelDuration;

    /** */
    private CellBehaviourFlow cellBehaviour;

    /**
     * @param cellLength Length;
     * @param maxCapacity Frequency;
     * @param speed Speed;
     * @param numberOfLanes int;
     * @param behaviourType TrafficBehaviourType;
     */
    public FlowCell(final Length cellLength, final Frequency maxCapacity, Speed speed, final int numberOfLanes,
            final TrafficBehaviourType behaviourType)
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
     * @param accumulatedCarsPerLengthUnit double; number of cars in Cell
     * @param maximumCapacity Frequency; based on area information
     * @param param ParametersFundamentalDiagram; parameters FD
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
     * @param accumulatedCarsPerLengthUnit double;
     * @return actualSpeed.
     */
    public Speed retrieveCurrentSpeed(final double accumulatedCarsPerLengthUnit)
    {
        double speedDouble;
        Frequency currentInflowCapacity = retrieveCurrentInflowCapacity(accumulatedCarsPerLengthUnit, this.maxCapacity,
                this.cellBehaviour.getParametersFundamentalDiagram());
        LinearDensity density = new LinearDensity(accumulatedCarsPerLengthUnit, LinearDensityUnit.PER_KILOMETER);
        if (density.getInUnit(LinearDensityUnit.PER_KILOMETER) > this.cellBehaviour.getParametersFundamentalDiagram()
                .getAccCritical().get(0))
        {
            speedDouble = currentInflowCapacity.getInUnit(FrequencyUnit.PER_HOUR)
                    / density.getInUnit(LinearDensityUnit.PER_KILOMETER);
            // speedDouble =
            // Math.max(speedDouble, this.getCellBehaviourFlow().getParametersFundamentalDiagram().getFreeSpeed()
            // .getInUnit(SpeedUnit.KM_PER_HOUR));
        }
        else
        {
            speedDouble = this.cellBehaviour.getParametersFundamentalDiagram().getCapacity().getInUnit(FrequencyUnit.PER_HOUR)
                    / this.cellBehaviour.getParametersFundamentalDiagram().getAccCritical().get(0);
        }
        return this.setActualSpeed(new Speed(speedDouble, SpeedUnit.KM_PER_HOUR));
    }

    /**
     * @param accumulatedCars
     * @return actualSpeed.
     */
    public Duration retrieveCurrentTravelDuration()
    {
        double densityPerLengthUnit =
                this.getCellBehaviourFlow().getAccumulatedCars() / this.cellLength.getInUnit(LengthUnit.KILOMETER);
        double timeDouble = this.cellLength.getInUnit(LengthUnit.KILOMETER)
                / retrieveCurrentSpeed(densityPerLengthUnit).getInUnit(SpeedUnit.KM_PER_HOUR);
        double UPPERBOUND_TRAVELTIME_HOUR = 99;
        timeDouble = Math.min(UPPERBOUND_TRAVELTIME_HOUR, timeDouble);
        return this.setCurrentTravelDuration(new Duration(timeDouble, DurationUnit.HOUR));
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
    public Length getCellLength()
    {
        return this.cellLength;
    }

    /**
     * @param cellLength Length; set cellLength.
     */
    public void setCellLength(Length cellLength)
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
     * @param maxCapacity Frequency; set capacity.
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
     * @param cellBehaviour CellBehaviourFlow; set cellBehaviour.
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
     * @param numberOfLanes int; set numberOfLanes.
     */
    public void setNumberOfLanes(int numberOfLanes)
    {
        this.numberOfLanes = numberOfLanes;
    }

    /**
     * @return actualSpeed.
     */
    public Speed getCurrentSpeed()
    {
        return this.currentSpeed;
    }

    /**
     * @param currentSpeed Speed; set actualSpeed.
     */
    public Speed setActualSpeed(Speed currentSpeed)
    {
        this.currentSpeed = currentSpeed;
        return currentSpeed;
    }

    /**
     * @return currentTravelTime.
     */
    public Duration getCurrentTravelDuration()
    {
        return this.currentTravelDuration;
    }

    /**
     * @param currentTravelDuration Duration; set currentTravelTime.
     */
    public Duration setCurrentTravelDuration(Duration currentTravelDuration)
    {
        this.currentTravelDuration = currentTravelDuration;
        return currentTravelDuration;
    }

}
