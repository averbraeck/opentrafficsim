package org.opentrafficsim.swing.graphs;

import java.util.LinkedHashMap;
import java.util.Map;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.draw.graphs.AbstractPlot;
import org.opentrafficsim.draw.graphs.PlotScheduler;

import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEvent;

/**
 * This scheduler allows plots to work live with an OTS simulation.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class OtsPlotScheduler implements PlotScheduler
{

    /** Simulator. */
    private final OtsSimulatorInterface simulator;

    /** Lastest update event per plot. */
    private final Map<AbstractPlot, SimEvent<Duration>> events = new LinkedHashMap<>();

    /**
     * Constructor.
     * @param simulator OtsSimulatorInterface; simulator.
     */
    public OtsPlotScheduler(final OtsSimulatorInterface simulator)
    {
        this.simulator = simulator;
    }

    /** {@inheritDoc} */
    @Override
    public Time getTime()
    {
        return this.simulator.getSimulatorAbsTime();
    }

    /** {@inheritDoc} */
    @Override
    public void cancelEvent(final AbstractPlot abstractPlot)
    {
        SimEvent<Duration> event = this.events.get(abstractPlot);
        if (event != null)
        {
            this.simulator.cancelEvent(event);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void scheduleUpdate(final Time time, final AbstractPlot abstractPlot)
    {
        this.events.put(abstractPlot, this.simulator.scheduleEventAbsTime(time, abstractPlot, "update", null));
    }

}
