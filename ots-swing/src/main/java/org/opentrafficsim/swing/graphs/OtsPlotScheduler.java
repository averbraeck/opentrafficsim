package org.opentrafficsim.swing.graphs;

import java.util.LinkedHashMap;
import java.util.Map;

import org.djunits.value.vdouble.scalar.Duration;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.draw.graphs.AbstractPlot;
import org.opentrafficsim.draw.graphs.PlotScheduler;

import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEventInterface;

/**
 * This scheduler allows plots to work live with an OTS simulation.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class OtsPlotScheduler implements PlotScheduler
{

    /** Simulator. */
    private final OtsSimulatorInterface simulator;

    /** Lastest update event per plot. */
    private final Map<AbstractPlot, SimEventInterface<Duration>> events = new LinkedHashMap<>();

    /**
     * Constructor.
     * @param simulator simulator.
     */
    public OtsPlotScheduler(final OtsSimulatorInterface simulator)
    {
        this.simulator = simulator;
    }

    @Override
    public Duration getTime()
    {
        return this.simulator.getSimulatorTime();
    }

    @Override
    public void cancelEvent(final AbstractPlot abstractPlot)
    {
        SimEventInterface<Duration> event = this.events.get(abstractPlot);
        if (event != null)
        {
            this.simulator.cancelEvent(event);
        }
    }

    @Override
    public void scheduleUpdate(final Duration time, final AbstractPlot abstractPlot)
    {
        this.events.put(abstractPlot, this.simulator.scheduleEventAbs(time, () -> abstractPlot.update()));
    }

}
