package org.opentrafficsim.road.network.lane.object.trafficlight;

import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;

/**
 * Standard fields and methods for traffic light controllers.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Oct 4, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public abstract class AbstractTrafficLightController implements TrafficLightController
{
    /** the controller id. */
    private final String id;

    /** the simulator. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected final DEVSSimulatorInterface.TimeDoubleUnit simulator;

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
     * @param simulator DEVSSimulatorInterface.TimeDoubleUnit; the simulator
     */
    public AbstractTrafficLightController(final String id, final DEVSSimulatorInterface.TimeDoubleUnit simulator)
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
