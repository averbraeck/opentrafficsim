package org.opentrafficsim.simulationengine;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;
import javax.swing.WindowConstants;

import nl.tudelft.simulation.dsol.SimRuntimeException;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.modelproperties.Property;
import org.opentrafficsim.base.modelproperties.PropertyException;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.gtu.animation.DefaultSwitchableGTUColorer;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.gui.OTSAnimationPanel;
import org.opentrafficsim.gui.SimulatorFrame;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Jun 18, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public abstract class AbstractWrappableAnimation implements WrappableAnimation, Serializable
{
    /** */
    private static final long serialVersionUID = 20150000L;

    /** The properties exhibited by this simulation. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected List<Property<?>> properties = new ArrayList<>();

    /** The properties after (possible) editing by the user. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected List<Property<?>> savedUserModifiedProperties;

    /** Use EXIT_ON_CLOSE when true, DISPOSE_ON_CLOSE when false on closing of the window. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected boolean exitOnClose;

    /** The tabbed panel so other tabs can be added by the classes that extend this class. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected OTSAnimationPanel panel;

    /** Save the startTime for restarting the simulation. */
    private Time savedStartTime;

    /** Save the startTime for restarting the simulation. */
    private Duration savedWarmupPeriod;

    /** Save the runLength for restarting the simulation. */
    private Duration savedRunLength;

    /**
     * Build the animator.
     * @param startTime Time; the start time
     * @param warmupPeriod Duration; the warm up period
     * @param runLength Duration; the duration of the simulation / animation
     * @param model OTSModelInterface; the simulation model
     * @return SimpleAnimator; a newly constructed animator
     * @throws SimRuntimeException on ???
     * @throws NamingException when context for the animation cannot be created
     * @throws PropertyException when one of the user modified properties has the empty string as key
     */
    @SuppressWarnings("checkstyle:designforextension")
    protected SimpleAnimator buildSimpleAnimator(final Time startTime, final Duration warmupPeriod, final Duration runLength,
            final OTSModelInterface model) throws SimRuntimeException, NamingException, PropertyException
    {
        return new SimpleAnimator(startTime, warmupPeriod, runLength, model);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public SimpleAnimator buildAnimator(final Time startTime, final Duration warmupPeriod, final Duration runLength,
            final List<Property<?>> userModifiedProperties, final Rectangle rect, final boolean eoc)
            throws SimRuntimeException, NamingException, OTSSimulationException, PropertyException
    {
        this.savedUserModifiedProperties = userModifiedProperties;
        this.exitOnClose = eoc;

        this.savedStartTime = startTime;
        this.savedWarmupPeriod = warmupPeriod;
        this.savedRunLength = runLength;

        GTUColorer colorer = new DefaultSwitchableGTUColorer();
        OTSModelInterface model = makeModel(colorer);

        if (null == model)
        {
            return null; // Happens when the user cancels the file open dialog in the OpenStreetMap demo.
        }

        final SimpleAnimator simulator = buildSimpleAnimator(startTime, warmupPeriod, runLength, model);
        try
        {
            this.panel = new OTSAnimationPanel(makeAnimationRectangle(), new Dimension(1024, 768), simulator, this, colorer);
        }
        catch (RemoteException exception)
        {
            throw new SimRuntimeException(exception);
        }
        addTabs(simulator);

        SimulatorFrame frame = new SimulatorFrame(shortName(), this.panel);
        if (rect != null)
        {
            frame.setBounds(rect);
        }
        else
        {
            frame.setExtendedState(Frame.MAXIMIZED_BOTH);
        }

        frame.setDefaultCloseOperation(this.exitOnClose ? WindowConstants.EXIT_ON_CLOSE : WindowConstants.DISPOSE_ON_CLOSE);
        return simulator;
    }

    /**
     * Make additional tabs in the main simulation window.
     * @param simulator SimpleSimulatorInterface; the simulator
     * @throws OTSSimulationException in case the chart, axes or legend cannot be generated
     * @throws PropertyException when one of the user modified properties has the empty string as key
     */
    protected void addTabs(final SimpleSimulatorInterface simulator) throws OTSSimulationException, PropertyException
    {
        // Override this method to add custom tabs
    }

    /**
     * @param colorer the GTU colorer to use.
     * @return the demo model. Don't forget to keep a local copy.
     * @throws OTSSimulationException in case the construction of the model fails
     */
    protected abstract OTSModelInterface makeModel(GTUColorer colorer) throws OTSSimulationException;

    /**
     * @return the initial rectangle for the animation.
     */
    protected abstract Rectangle2D.Double makeAnimationRectangle();

    /** {@inheritDoc} */
    @Override
    public final ArrayList<Property<?>> getProperties()
    {
        return new ArrayList<Property<?>>(this.properties);
    }

    /** {@inheritDoc} */
    @Override
    public final SimpleSimulatorInterface rebuildSimulator(final Rectangle rect) throws SimRuntimeException, NetworkException,
            NamingException, OTSSimulationException, PropertyException
    {
        return buildAnimator(this.savedStartTime, this.savedWarmupPeriod, this.savedRunLength,
                this.savedUserModifiedProperties, rect, this.exitOnClose);
    }

    /** {@inheritDoc} */
    @Override
    public final List<Property<?>> getUserModifiedProperties()
    {
        return this.savedUserModifiedProperties;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public void stopTimersThreads()
    {
        if (this.panel != null && this.panel.getStatusBar() != null)
        {
            this.panel.getStatusBar().cancelTimer();
        }
        this.panel = null;
    }

    /**
     * @return panel
     */
    public final OTSAnimationPanel getPanel()
    {
        return this.panel;
    }

    /**
     * Add a tab to the simulation window. This method can not be called from constructModel because the TabbedPane has not yet
     * been constructed at that time; recommended: override addTabs and call this method from there.
     * @param index int; index of the new tab; use <code>getTabCount()</code> to obtain the valid range
     * @param caption String; caption of the new tab
     * @param container Container; content of the new tab
     */
    public final void addTab(final int index, final String caption, final Container container)
    {
        this.panel.getTabbedPane().addTab(index, caption, container);
    }

    /**
     * Report the current number of tabs in the simulation window. This method can not be called from constructModel because the
     * TabbedPane has not yet been constructed at that time; recommended: override addTabs and call this method from there.
     * @return int; the number of tabs in the simulation window
     */
    public final int getTabCount()
    {
        return this.panel.getTabbedPane().getTabCount();
    }

}
