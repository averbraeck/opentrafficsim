package org.opentrafficsim.simulationengine;

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.animation.D2.AnimationPanel;
import nl.tudelft.simulation.dsol.experiment.ReplicationMode;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEvent;
import nl.tudelft.simulation.dsol.gui.swing.DSOLPanel;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.event.Event;

import org.opentrafficsim.core.dsol.OTSDEVSRealTimeClock;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSReplication;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 11 mei 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class SimpleAnimator extends OTSDEVSRealTimeClock implements SimpleSimulation
{
    /** */
    private static final long serialVersionUID = 20150511L;

    /** Counter for replication. */
    private int lastReplication = 0;

    /** The JPanel that contains the simulator controls, a status bar and a JTabbedPane with switchable sub panels. */
    DSOLPanel<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> panel;
    
    /** The AnimationPanel. */
    private final AnimationPanel animationPanel;

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
     * @throws NamingException when context for the animation cannot be created
     */
    public SimpleAnimator(final DoubleScalar.Abs<TimeUnit> startTime, final DoubleScalar.Rel<TimeUnit> warmupPeriod,
            final DoubleScalar.Rel<TimeUnit> runLength, final OTSModelInterface model, final Rectangle2D extent)
            throws RemoteException, SimRuntimeException, NamingException
    {
        setPauseOnError(true);
        initialize(new OTSReplication("rep" + ++this.lastReplication, new OTSSimTimeDouble(startTime), warmupPeriod,
                runLength, model), ReplicationMode.TERMINATING);
        this.panel =
                new DSOLPanel<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble>(model, this);
        Dimension size = new Dimension(1024, 768);
        this.animationPanel = new AnimationPanel(extent, size, this);
        this.panel.getTabbedPane().addTab(0, "animation", this.animationPanel);
        this.animationPanel.notify(new Event(SimulatorInterface.START_REPLICATION_EVENT, this, null));
        this.panel.getTabbedPane().setSelectedIndex(0); // Show the animation panel
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
     * Easy access to the AnimationPanel.
     * @return AnimationPanel
     */
    public final AnimationPanel getAnimationPanel()
    {
        return this.animationPanel;
    }
    
    /**
     * {@inheritDoc}
     */
    public final SimEvent<OTSSimTimeDouble> scheduleEvent(final DoubleScalar.Abs<TimeUnit> executionTime,
            final short priority, final Object source, final Object target, final String method, final Object[] args)
            throws SimRuntimeException
    {
        SimEvent<OTSSimTimeDouble> result =
                new SimEvent<OTSSimTimeDouble>(new OTSSimTimeDouble(new DoubleScalar.Abs<TimeUnit>(
                        executionTime.getSI(), TimeUnit.SECOND)), priority, source, target, method, args);
        scheduleEvent(result);
        return result;
    }

}
