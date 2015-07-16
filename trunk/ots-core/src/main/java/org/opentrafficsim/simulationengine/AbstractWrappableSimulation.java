package org.opentrafficsim.simulationengine;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.naming.NamingException;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import nl.tudelft.simulation.dsol.SimRuntimeException;

import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.gtu.animation.DefaultSwitchableGTUColorer;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.gui.OTSAnimationPanel;
import org.opentrafficsim.gui.SimulatorFrame;
import org.opentrafficsim.simulationengine.properties.AbstractProperty;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author: pknoppers
 * $, initial version Jun 18, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public abstract class AbstractWrappableSimulation implements WrappableSimulation
{
    /** The properties exhibited by this simulation. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected ArrayList<AbstractProperty<?>> properties = new ArrayList<AbstractProperty<?>>();

    /** The properties after (possible) editing by the user. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected ArrayList<AbstractProperty<?>> savedUserModifiedProperties;

    /** Use EXIT_ON_CLOSE when true, DISPOSE_ON_CLOSE when false on closing of the window. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected boolean exitOnClose;

    /** the tabbed panel so other tabs can be added by the classes that extend this class. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected OTSAnimationPanel panel;

    /** {@inheritDoc} */
    @Override
    public final SimpleAnimator buildSimulator(final ArrayList<AbstractProperty<?>> userModifiedProperties,
            final Rectangle rect, final boolean eoc) throws RemoteException, SimRuntimeException, NamingException
    {
        this.savedUserModifiedProperties = userModifiedProperties;
        this.exitOnClose = eoc;

        GTUColorer colorer = new DefaultSwitchableGTUColorer();
        OTSModelInterface model = makeModel(colorer);

        if (null == model)
        {
            return null; // Happens when the user cancels the file open dialog in the OpenStreetMap demo.
        }

        final SimpleAnimator simulator =
                new SimpleAnimator(new DoubleScalar.Abs<TimeUnit>(0.0, TimeUnit.SECOND),
                        new DoubleScalar.Rel<TimeUnit>(0.0, TimeUnit.SECOND), new DoubleScalar.Rel<TimeUnit>(3600.0,
                                TimeUnit.SECOND), model);
        this.panel =
                new OTSAnimationPanel(makeAnimationRectangle(), new Dimension(1024, 768), simulator, this, colorer);
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
     */
    protected abstract JPanel makeCharts();

    /**
     * @param colorer the GTU colorer to use.
     * @return the demo model. Don't forget to keep a local copy.
     */
    protected abstract OTSModelInterface makeModel(GTUColorer colorer);

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
    public final SimpleSimulation rebuildSimulator(final Rectangle rect) throws SimRuntimeException, RemoteException,
            NetworkException, NamingException
    {
        return buildSimulator(this.savedUserModifiedProperties, rect, this.exitOnClose);
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
}
