package org.opentrafficsim.road.network.lane.object.trafficlight;

import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.opentrafficsim.core.dsol.OtsSimulatorInterface;

/**
 * Standard fields and methods for traffic light controllers.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public abstract class AbstractTrafficLightController implements TrafficLightController
{
    /** the controller id. */
    private final String id;

    /** the simulator. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected final OtsSimulatorInterface simulator;

    /** the phases with a number identifying them to create a sorted map. */
    private final SortedMap<Integer, Set<TrafficLight>> phases = new TreeMap<>();

    /** the current phase. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected int currentPhase;

    /** the current light in the current phase. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected TrafficLightColor currentColor = TrafficLightColor.RED;

    /**
     * Create a fixed time controller.
     * @param id String; the controller id
     * @param simulator OTSSimulatorInterface; the simulator
     */
    public AbstractTrafficLightController(final String id, final OtsSimulatorInterface simulator)
    {
        this.id = id;
        this.simulator = simulator;
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

}
