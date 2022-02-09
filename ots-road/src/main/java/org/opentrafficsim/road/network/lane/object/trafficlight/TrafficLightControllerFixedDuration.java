package org.opentrafficsim.road.network.lane.object.trafficlight;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.djunits.value.vdouble.scalar.Duration;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;

/**
 * A traffic light controller with fixed durations. Red, yellow and green times of each phase can be set, as well as the time
 * between phases.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Oct 4, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class TrafficLightControllerFixedDuration implements TrafficLightController
{
    /** the controller id. */
    private final String id;

    /** the simulator. */
    private final DEVSSimulatorInterface.TimeDoubleUnit simulator;

    /** the phases with a number identifying them to create a sorted map. */
    private final SortedMap<Integer, Set<TrafficLight>> phases = new TreeMap<>();

    /** the fixed yellow duration per phase. */
    private final Map<Integer, Duration> yellowDurations = new LinkedHashMap<>();

    /** the fixed green duration per phase. */
    private final Map<Integer, Duration> greenDurations = new LinkedHashMap<>();

    /** the current phase. */
    private int currentPhase;

    /** the current light in the current phase. */
    private TrafficLightColor currentColor = TrafficLightColor.RED;

    /** fixed clearance duration between phases. */
    private Duration clearanceDuration = Duration.ZERO;

    /**
     * Create a fixed time controller.
     * @param id String; the controller id
     * @param simulator DEVSSimulatorInterface.TimeDoubleUnit; the simulator
     * @throws TrafficLightException when scheduling of thhe start event fails
     */
    public TrafficLightControllerFixedDuration(final String id, final DEVSSimulatorInterface.TimeDoubleUnit simulator)
            throws TrafficLightException
    {
        this.id = id;
        this.simulator = simulator;
        try
        {
            this.simulator.scheduleEventNow(this, this, "changePhase", null);
        }
        catch (SimRuntimeException exception)
        {
            throw new TrafficLightException(exception);
        }
    }

    /**
     * Change the phase and/or color of the traffic lights.
     * @throws TrafficLightException when scheduling of thhe start event fails
     */
    protected final void changePhase() throws TrafficLightException
    {
        try
        {
            if (this.currentColor.isGreen())
            {
                for (TrafficLight trafficLight : this.phases.get(this.currentPhase))
                {
                    trafficLight.setTrafficLightColor(TrafficLightColor.YELLOW);
                }
                this.currentColor = TrafficLightColor.YELLOW;
                this.simulator.scheduleEventRel(this.yellowDurations.get(this.currentPhase), this, this, "changePhase", null);
                return;
            }
            else if (this.currentColor.isYellow())
            {
                for (TrafficLight trafficLight : this.phases.get(this.currentPhase))
                {
                    trafficLight.setTrafficLightColor(TrafficLightColor.RED);
                }
                this.currentColor = TrafficLightColor.RED;
            }

            if (this.currentColor.isRed())
            {
                Integer nextPhase = ((NavigableSet<Integer>) this.phases.keySet()).higher(this.currentPhase);
                if (nextPhase == null)
                {
                    nextPhase = this.phases.firstKey();
                }
                this.currentPhase = nextPhase;
                this.currentColor = TrafficLightColor.GREEN;
                this.simulator.scheduleEventRel(this.greenDurations.get(this.currentPhase), this, this, "changePhase", null);
            }
        }
        catch (SimRuntimeException exception)
        {
            throw new TrafficLightException(exception);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final int getNumberOfPhases()
    {
        return this.phases.size();
    }

    /** {@inheritDoc} */
    @Override
    public final int getCurrentPhase()
    {
        return this.currentPhase;
    }

    /** {@inheritDoc} */
    @Override
    public final Duration getClearanceDurationToNextPhase()
    {
        return this.clearanceDuration;
    }

    /**
     * Add a new phase.
     * @param phaseId int; the id of the phase to be added.
     * @param yellowDuration Duration; the yellow time
     * @param greenDuration Duration; the green time
     * @throws TrafficLightException when the phase already existed
     */
    public final void addPhase(final int phaseId, final Duration yellowDuration, final Duration greenDuration)
            throws TrafficLightException
    {
        if (this.phases.containsKey(phaseId))
        {
            throw new TrafficLightException("Phase " + phaseId + " for traffic light " + this.id + " was already defined");
        }
        this.phases.put(phaseId, new LinkedHashSet<TrafficLight>());
        this.yellowDurations.put(phaseId, yellowDuration);
        this.greenDurations.put(phaseId, greenDuration);
    }

    /** {@inheritDoc} */
    @Override
    public final void addTrafficLightToPhase(final int phaseId, final TrafficLight trafficLight) throws TrafficLightException
    {
        if (!this.phases.containsKey(phaseId))
        {
            throw new TrafficLightException("Phase " + phaseId + " for traffic light " + this.id + " was not defined");
        }
        this.phases.get(phaseId).add(trafficLight);
        trafficLight.setTrafficLightColor(TrafficLightColor.RED);
    }

    /** {@inheritDoc} */
    @Override
    public final String getId()
    {
        return this.id;
    }

    /**
     * @param clearanceDuration Duration; set clearanceDuration
     */
    public final void setClearanceDuration(final Duration clearanceDuration)
    {
        this.clearanceDuration = clearanceDuration;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "TrafficLightControllerFixedDuration [id=" + this.id + ", phases=" + this.phases + ", yellowDurations="
                + this.yellowDurations + ", greenDurations=" + this.greenDurations + ", currentPhase=" + this.currentPhase
                + ", currentColor=" + this.currentColor + ", clearanceDuration=" + this.clearanceDuration + "]";
    }

}
