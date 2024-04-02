package org.opentrafficsim.demo.conflict;

import java.awt.Dimension;
import java.net.URL;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.io.URLResource;
import org.opentrafficsim.animation.gtu.colorer.DefaultSwitchableGtuColorer;
import org.opentrafficsim.core.dsol.AbstractOtsModel;
import org.opentrafficsim.core.dsol.OtsAnimator;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.demo.conflict.TurboRoundaboutDemo.TurboRoundaboutModel;
import org.opentrafficsim.draw.OtsDrawingException;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.factory.xml.parser.XmlParser;
import org.opentrafficsim.swing.gui.OtsAnimationPanel;
import org.opentrafficsim.swing.gui.OtsSimulationApplication;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.language.DsolException;

/**
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class TurboRoundaboutDemo extends OtsSimulationApplication<TurboRoundaboutModel>
{
    /** */
    private static final long serialVersionUID = 20161211L;

    /**
     * Create a T-Junction demo.
     * @param title String; the title of the Frame
     * @param panel OtsAnimationPanel; the tabbed panel to display
     * @param model TurboRoundaboutModel; the model
     * @throws OtsDrawingException on animation error
     */
    public TurboRoundaboutDemo(final String title, final OtsAnimationPanel panel, final TurboRoundaboutModel model)
            throws OtsDrawingException
    {
        super(model, panel);
    }

    /**
     * Main program.
     * @param args String[]; the command line arguments (not used)
     */
    public static void main(final String[] args)
    {
        demo(true);
    }

    /**
     * Start the demo.
     * @param exitOnClose boolean; when running stand-alone: true; when running as part of a demo: false
     */
    public static void demo(final boolean exitOnClose)
    {
        try
        {
            OtsAnimator simulator = new OtsAnimator("TurboRoundaboutDemo");
            final TurboRoundaboutModel junctionModel = new TurboRoundaboutModel(simulator);
            simulator.initialize(Time.ZERO, Duration.ZERO, Duration.instantiateSI(3600.0), junctionModel);
            OtsAnimationPanel animationPanel =
                    new OtsAnimationPanel(junctionModel.getNetwork().getExtent(), new Dimension(800, 600), simulator,
                            junctionModel, new DefaultSwitchableGtuColorer(), junctionModel.getNetwork());
            TurboRoundaboutDemo app = new TurboRoundaboutDemo("Turbo-Roundabout demo", animationPanel, junctionModel);
            app.setExitOnClose(exitOnClose);
            animationPanel.enableSimulationControlButtons();
        }
        catch (SimRuntimeException | NamingException | RemoteException | OtsDrawingException | DsolException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * The simulation model.
     */
    public static class TurboRoundaboutModel extends AbstractOtsModel
    {
        /** */
        private static final long serialVersionUID = 20161211L;

        /** The network. */
        private RoadNetwork network;

        /**
         * @param simulator OtsSimulatorInterface; the simulator for this model
         */
        public TurboRoundaboutModel(final OtsSimulatorInterface simulator)
        {
            super(simulator);
        }

        /** {@inheritDoc} */
        @Override
        public void constructModel() throws SimRuntimeException
        {
            try
            {
                URL xmlURL = URLResource.getResource("/resources/conflict/TurboRoundabout.xml");
                this.network = new RoadNetwork("TurboRoundabout", getSimulator());
                new XmlParser(this.network).setUrl(xmlURL).build();
            }
            catch (Exception exception)
            {
                exception.printStackTrace();
            }
        }

        /** {@inheritDoc} */
        @Override
        public RoadNetwork getNetwork()
        {
            return this.network;
        }

    }
}
