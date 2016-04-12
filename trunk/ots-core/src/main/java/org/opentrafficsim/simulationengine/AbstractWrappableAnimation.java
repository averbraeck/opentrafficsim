package org.opentrafficsim.simulationengine;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.naming.NamingException;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import nl.tudelft.simulation.dsol.SimRuntimeException;

import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.gtu.animation.DefaultSwitchableGTUColorer;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.gui.OTSAnimationPanel;
import org.opentrafficsim.gui.SimulatorFrame;
import org.opentrafficsim.simulationengine.properties.AbstractProperty;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
    protected ArrayList<AbstractProperty<?>> properties = new ArrayList<AbstractProperty<?>>();

    /** The properties after (possible) editing by the user. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected ArrayList<AbstractProperty<?>> savedUserModifiedProperties;

    /** Use EXIT_ON_CLOSE when true, DISPOSE_ON_CLOSE when false on closing of the window. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected boolean exitOnClose;

    /** The tabbed panel so other tabs can be added by the classes that extend this class. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected OTSAnimationPanel panel;

    /** Save the startTime for restarting the simulation. */
    private Time.Abs savedStartTime;

    /** Save the startTime for restarting the simulation. */
    private Time.Rel savedWarmupPeriod;

    /** Save the runLength for restarting the simulation. */
    private Time.Rel savedRunLength;

    /** {@inheritDoc} */
    @Override
    public final SimpleAnimator buildAnimator(final Time.Abs startTime, final Time.Rel warmupPeriod,
        final Time.Rel runLength, final ArrayList<AbstractProperty<?>> userModifiedProperties, final Rectangle rect,
        final boolean eoc) throws SimRuntimeException, NamingException, OTSSimulationException
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

        final SimpleAnimator simulator = new SimpleAnimator(startTime, warmupPeriod, runLength, model);
        try
        {
            this.panel =
                new OTSAnimationPanel(makeAnimationRectangle(), new Dimension(1024, 768), simulator, this, colorer);
        }
        catch (RemoteException exception)
        {
            throw new SimRuntimeException(exception);
        }
        JPanel charts = makeCharts();
        if (null != charts)
        {
            this.panel.getTabbedPane().addTab("statistics", charts);
        }

        SimulatorFrame frame = new SimulatorFrame(shortName(), this.panel);
        if (rect != null)
        {
            frame.setBounds(rect);
        }
        else
        {
            frame.setExtendedState(Frame.MAXIMIZED_BOTH);
        }

        frame.setDefaultCloseOperation(this.exitOnClose ? WindowConstants.EXIT_ON_CLOSE
            : WindowConstants.DISPOSE_ON_CLOSE);
        return simulator;
    }

    /**
     * @return the JPanel with the charts; the result will be put in the statistics tab. May return null; this causes no
     *         statistics tab to be created.
     * @throws OTSSimulationException in case the chart, axes or legend cannot be generated
     */
    protected abstract JPanel makeCharts() throws OTSSimulationException;

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
    public final ArrayList<AbstractProperty<?>> getProperties()
    {
        return new ArrayList<AbstractProperty<?>>(this.properties);
    }

    /** {@inheritDoc} */
    @Override
    public final SimpleSimulatorInterface rebuildSimulator(final Rectangle rect) throws SimRuntimeException,
        NetworkException, NamingException, OTSSimulationException
    {
        return buildAnimator(this.savedStartTime, this.savedWarmupPeriod, this.savedRunLength,
            this.savedUserModifiedProperties, rect, this.exitOnClose);
    }

    /** {@inheritDoc} */
    @Override
    public final ArrayList<AbstractProperty<?>> getUserModifiedProperties()
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
}
