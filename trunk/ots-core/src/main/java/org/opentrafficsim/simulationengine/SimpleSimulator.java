package org.opentrafficsim.simulationengine;

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.rmi.RemoteException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.animation.D2.AnimationPanel;
import nl.tudelft.simulation.dsol.experiment.ReplicationMode;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEvent;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEventInterface;
import nl.tudelft.simulation.dsol.gui.swing.DSOLPanel;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulator;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.event.Event;

import org.opentrafficsim.core.dsol.OTSDEVSAnimator;
import org.opentrafficsim.core.dsol.OTSDEVSSimulator;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSReplication;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * Construct a DSOL DEVSSimulator or DEVSAnimator the easy way.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 12 nov. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class SimpleSimulator
{
    /** Counter for replication. */
    private int lastReplication = 0;

    /** The JPanel that contains the simulator controls, a status bar and a JTabbedPane with switchable sub panels. */
    private final DSOLPanel<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> panel;

    /** The simulator engine. */
    private final DEVSSimulator<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> simulator;

    /**
     * Internal constructor that performs the tasks that must be executed for any kind of SimpleSimulator.
     * @param simulator DEVSSimulator; either a OTSDEVSSimulator, or a OTSDEVSAnimator.
     * @param startTime OTSSimTimeDouble; the start time of the simulation
     * @param warmupPeriod DoubleScalar.Rel&lt;TimeUnit&gt;; the warm up period of the simulation (use new
     *            DoubleScalar.Rel&lt;TimeUnit&gt;(0, TimeUnit.SECOND) if you don't know what this is)
     * @param runLength DoubleScalar.Rel&lt;TimeUnit&gt;; the duration of the simulation
     * @param model OTSModelInterface; the simulation to execute
     * @throws RemoteException on communications failure
     * @throws SimRuntimeException on ???
     */
    private SimpleSimulator(
            final DEVSSimulator<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> simulator,
            final DoubleScalar.Abs<TimeUnit> startTime, final DoubleScalar.Rel<TimeUnit> warmupPeriod,
            final DoubleScalar.Rel<TimeUnit> runLength, final OTSModelInterface model) throws RemoteException,
            SimRuntimeException
    {
        this.simulator = simulator;
        this.simulator.setPauseOnError(true);
        this.simulator.initialize(new OTSReplication("rep" + ++this.lastReplication, new OTSSimTimeDouble(startTime),
                warmupPeriod, runLength, model), ReplicationMode.TERMINATING);
        this.panel =
                new DSOLPanel<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble>(model,
                        this.simulator);
    }

    /**
     * Create a simulation engine without animation; the easy way. PauseOnError is set to true;
     * @param startTime OTSSimTimeDouble; the start time of the simulation
     * @param warmupPeriod DoubleScalar.Rel&lt;TimeUnit&gt;; the warm up period of the simulation (use new
     *            DoubleScalar.Rel&lt;TimeUnit&gt;(0, TimeUnit.SECOND) if you don't know what this is)
     * @param runLength DoubleScalar.Rel&lt;TimeUnit&gt;; the duration of the simulation
     * @param model OTSModelInterface; the simulation to execute
     * @throws RemoteException on communications failure
     * @throws SimRuntimeException on ???
     */
    public SimpleSimulator(final DoubleScalar.Abs<TimeUnit> startTime, final DoubleScalar.Rel<TimeUnit> warmupPeriod,
            final DoubleScalar.Rel<TimeUnit> runLength, final OTSModelInterface model) throws RemoteException,
            SimRuntimeException
    {
        this(new OTSDEVSSimulator(), startTime, warmupPeriod, runLength, model);
    }

    /**
     * Create a simulation engine with animation; the easy way. PauseOnError is set to true;
     * @param startTime DoubleScalar.Abs&lt;TimeUnit&gt;; the start time of the simulation
     * @param warmupPeriod DoubleScalar.Rel&lt;TimeUnit&gt;; the warm up period of the simulation (use new
     *            DoubleScalar.Rel&lt;TimeUnit&gt;(0, TimeUnit.SECOND) if you don't know what this is)
     * @param runLength DoubleScalar.Rel&lt;TimeUnit&gt;; the duration of the simulation
     * @param model OTSModelInterface; the simulation to execute
     * @param extent Rectangle2D; bottom left corner, length and width of the area (world) to animate.
     * @throws RemoteException on communications failure
     * @throws SimRuntimeException on ???
     */
    public SimpleSimulator(final DoubleScalar.Abs<TimeUnit> startTime, final DoubleScalar.Rel<TimeUnit> warmupPeriod,
            final DoubleScalar.Rel<TimeUnit> runLength, final OTSModelInterface model, final Rectangle2D extent)
            throws RemoteException, SimRuntimeException
    {
        this(new OTSDEVSAnimator(), startTime, warmupPeriod, runLength, model);
        Dimension size = new Dimension(1024, 768);
        AnimationPanel animationPanel = new AnimationPanel(extent, size, this.simulator);
        this.panel.getTabbedPane().addTab(0, "animation", animationPanel);
        animationPanel.notify(new Event(SimulatorInterface.START_REPLICATION_EVENT, this.simulator, null));
        this.panel.getTabbedPane().setSelectedIndex(0); // Select the animation panel
    }

    /**
     * To use in a Swing application add the DSOLPanel to a JFrame.
     * @return the simulation panel (extends JPanel).
     */
    public final DSOLPanel<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> getPanel()
    {
        return this.panel;
    }

    /**
     * Access to the simulator is needed to create simulated objects.
     * @return simulator.
     */
    public final DEVSSimulator<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> getSimulator()
    {
        return this.simulator;
    }

    /**
     * Run the simulation up to the specified time.
     * @param when DoubleScalar.Abs&lt;TimeUnit&gt;; the stop time.
     * @throws SimRuntimeException when the specified time lies in the past
     */
    public final void runUpTo(final DoubleScalar.Abs<TimeUnit> when) throws SimRuntimeException
    {
        scheduleEvent(when, SimEventInterface.MAX_PRIORITY, this, this, "autoPauseSimulator", null);
        while (this.simulator.getSimulatorTime().get().getSI() < when.getSI())
        {
            this.simulator.step();
        }
    }

    /**
     * Pause the simulator.
     */
    @SuppressWarnings("unused")
    private void autoPauseSimulator()
    {
        if (this.simulator.isRunning())
        {
            this.simulator.stop();
        }
    }

    /**
     * Construct and schedule a SimEvent using a DoubleScalar.Abs&lt;TimeUnit&gt; to specify the execution time.
     * @param executionTime DoubleScalar.Abs&lt;TimeUnit&gt;; the time at which the event must happen
     * @param priority short; should be between <cite>SimEventInterface.MAX_PRIORITY</cite> and
     *            <cite>SimEventInterface.MIN_PRIORITY</cite>; most normal events should use
     *            <cite>SimEventInterface.NORMAL_PRIORITY</cite>
     * @param source Object; the object that creates/schedules the event
     * @param target Object; the object that must execute the event
     * @param method String; the name of the method of <code>target</code> that must execute the event
     * @param args Object[]; the arguments of the <code>method</code> that must execute the event
     * @return SimEvent&lt;OTSSimTimeDouble&gt;; the event that was scheduled (the caller should save this if a need to
     *         cancel the event may arise later)
     * @throws SimRuntimeException when the <code>executionTime</code> is in the past
     */
    public final SimEvent<OTSSimTimeDouble> scheduleEvent(final DoubleScalar.Abs<TimeUnit> executionTime,
            final short priority, final Object source, final Object target, final String method, final Object[] args)
            throws SimRuntimeException
    {
        SimEvent<OTSSimTimeDouble> result =
                new SimEvent<OTSSimTimeDouble>(new OTSSimTimeDouble(new DoubleScalar.Abs<TimeUnit>(
                        executionTime.getSI(), TimeUnit.SECOND)), priority, source, target, method, args);
        this.simulator.scheduleEvent(result);
        return result;
    }

}
