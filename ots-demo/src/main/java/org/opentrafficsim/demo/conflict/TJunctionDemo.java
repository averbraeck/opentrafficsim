package org.opentrafficsim.demo.conflict;

import java.net.URL;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.io.URLResource;
import org.opentrafficsim.core.dsol.AbstractOtsModel;
import org.opentrafficsim.core.dsol.OtsAnimator;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.perception.HistoryManagerDevs;
import org.opentrafficsim.demo.DefaultsFactory;
import org.opentrafficsim.demo.conflict.TJunctionDemo.TJunctionModel;
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
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class TJunctionDemo extends OtsSimulationApplication<TJunctionModel>
{
    /** */
    private static final long serialVersionUID = 20161211L;

    /**
     * Create a T-Junction demo.
     * @param title the title of the Frame
     * @param panel the tabbed panel to display
     * @param model the model
     */
    public TJunctionDemo(final String title, final OtsAnimationPanel panel, final TJunctionModel model)
    {
        super(model, panel, DefaultsFactory.GTU_TYPE_MARKERS.toMap());
    }

    /**
     * Main program.
     * @param args the command line arguments (not used)
     */
    public static void main(final String[] args)
    {
        demo(true);
    }

    /**
     * Start the demo.
     * @param exitOnClose when running stand-alone: true; when running as part of a demo: false
     */
    public static void demo(final boolean exitOnClose)
    {
        try
        {
            OtsAnimator simulator = new OtsAnimator("TJunctionDemo");
            final TJunctionModel junctionModel = new TJunctionModel(simulator);
            simulator.initialize(Time.ZERO, Duration.ZERO, Duration.ofSI(3600.0), junctionModel,
                    HistoryManagerDevs.noHistory(simulator));
            OtsAnimationPanel animationPanel = new OtsAnimationPanel(junctionModel.getNetwork().getExtent(), simulator,
                    junctionModel, DEFAULT_GTU_COLORERS, junctionModel.getNetwork());
            TJunctionDemo app = new TJunctionDemo("T-Junction demo", animationPanel, junctionModel);
            app.setExitOnClose(exitOnClose);
            animationPanel.enableSimulationControlButtons();
        }
        catch (SimRuntimeException | NamingException | RemoteException | DsolException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * The simulation model.
     */
    public static class TJunctionModel extends AbstractOtsModel
    {
        /** */
        private static final long serialVersionUID = 20161211L;

        /** The network. */
        private RoadNetwork network;

        /**
         * Constructor.
         * @param simulator the simulator for this model
         */
        public TJunctionModel(final OtsSimulatorInterface simulator)
        {
            super(simulator);
        }

        @Override
        public void constructModel() throws SimRuntimeException
        {
            try
            {
                URL xmlURL = URLResource.getResource("/resources/conflict/TJunction.xml");
                this.network = new RoadNetwork("TJunction", getSimulator());
                new XmlParser(this.network).setUrl(xmlURL).setScenario("1").build();
            }
            catch (Exception exception)
            {
                exception.printStackTrace();
            }
        }

        @Override
        public RoadNetwork getNetwork()
        {
            return this.network;
        }

    }
}
