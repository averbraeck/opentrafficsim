package org.opentrafficsim.demo.conflict;

import java.awt.Dimension;
import java.io.Serializable;
import java.net.URL;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.io.URLResource;
import org.opentrafficsim.core.animation.gtu.colorer.DefaultSwitchableGTUColorer;
import org.opentrafficsim.core.dsol.AbstractOTSModel;
import org.opentrafficsim.core.dsol.OTSAnimator;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.demo.conflict.TestNetworkDemo.TestNetworkModel;
import org.opentrafficsim.draw.core.OTSDrawingException;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.factory.xml.parser.XmlNetworkLaneParser;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.conflict.ConflictBuilder;
import org.opentrafficsim.road.network.lane.conflict.LaneCombinationList;
import org.opentrafficsim.swing.gui.OTSAnimationPanel;
import org.opentrafficsim.swing.gui.OTSSimulationApplication;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.language.DSOLException;

/**
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 11 dec. 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class TestNetworkDemo extends OTSSimulationApplication<TestNetworkModel>
{
    /** */
    private static final long serialVersionUID = 20161211L;

    /**
     * Create a network test demo.
     * @param title String; the title of the Frame
     * @param panel OTSAnimationPanel; the tabbed panel to display
     * @param model TestNetworkModel; the model
     * @throws OTSDrawingException on animation error
     */
    public TestNetworkDemo(final String title, final OTSAnimationPanel panel, final TestNetworkModel model)
            throws OTSDrawingException
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
            OTSAnimator simulator = new OTSAnimator("TestNetworkDemo");
            final TestNetworkModel networkModel = new TestNetworkModel(simulator);
            simulator.initialize(Time.ZERO, Duration.ZERO, Duration.instantiateSI(3600.0), networkModel);
            OTSAnimationPanel animationPanel =
                    new OTSAnimationPanel(networkModel.getNetwork().getExtent(), new Dimension(800, 600), simulator,
                            networkModel, new DefaultSwitchableGTUColorer(), networkModel.getNetwork());
            TestNetworkDemo app = new TestNetworkDemo("Network test demo", animationPanel, networkModel);
            app.setExitOnClose(exitOnClose);
        }
        catch (SimRuntimeException | NamingException | RemoteException | OTSDrawingException | DSOLException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * The simulation model.
     */
    static class TestNetworkModel extends AbstractOTSModel
    {

        /** */
        private static final long serialVersionUID = 20161211L;

        /** The network. */
        private OTSRoadNetwork network;

        /**
         * @param simulator OTSSimulatorInterface; the simulator for this model
         */
        TestNetworkModel(final OTSSimulatorInterface simulator)
        {
            super(simulator);
        }

        /** {@inheritDoc} */
        @Override
        public void constructModel() throws SimRuntimeException
        {
            try
            {
                URL xmlURL = URLResource.getResource("/conflict/Test-Network-14.xml");
                this.network = new OTSRoadNetwork("Test-Network-14", true, getSimulator());
                XmlNetworkLaneParser.build(xmlURL, this.network, false);

                LaneCombinationList ignoreList = new LaneCombinationList();
                // ignoreList.addLinkCombination((CrossSectionLink) this.network.getLink("L_D3b-D3a"),
                // (CrossSectionLink) this.network.getLink("L_B3a-A3b"));
                // ignoreList.addLinkCombination((CrossSectionLink) this.network.getLink("L_A3a-D3a"),
                // (CrossSectionLink) this.network.getLink("L_C3b-B3b"));
                // ignoreList.addLinkCombination((CrossSectionLink) this.network.getLink("L_H3b-H3a"),
                // (CrossSectionLink) this.network.getLink("L_F3a-E3b"));
                // ignoreList.addLinkCombination((CrossSectionLink) this.network.getLink("L_E3a-H3a"),
                // (CrossSectionLink) this.network.getLink("L_G3b-F3b"));
                LaneCombinationList permittedList = new LaneCombinationList();
                permittedList.addLinkCombination((CrossSectionLink) this.network.getLink("L_D3b-D3a"),
                        (CrossSectionLink) this.network.getLink("L_B3a-A3b"));
                permittedList.addLinkCombination((CrossSectionLink) this.network.getLink("L_A3a-D3a"),
                        (CrossSectionLink) this.network.getLink("L_C3b-B3b"));
                permittedList.addLinkCombination((CrossSectionLink) this.network.getLink("L_H3b-H3a"),
                        (CrossSectionLink) this.network.getLink("L_F3a-E3b"));
                permittedList.addLinkCombination((CrossSectionLink) this.network.getLink("L_E3a-H3a"),
                        (CrossSectionLink) this.network.getLink("L_G3b-F3b"));
                ConflictBuilder.buildConflicts(this.network, this.network.getGtuType(GTUType.DEFAULTS.VEHICLE), this.simulator,
                        new ConflictBuilder.FixedWidthGenerator(new Length(2.0, LengthUnit.SI)), ignoreList, permittedList);
                // new ConflictBuilder.FixedWidthGenerator(new Length(1.0, LengthUnit.SI))
                // ConflictBuilder.DEFAULT_WIDTH_GENERATOR

            }
            catch (Exception exception)
            {
                exception.printStackTrace();
            }
        }

        /** {@inheritDoc} */
        @Override
        public OTSRoadNetwork getNetwork()
        {
            return this.network;
        }

        /** {@inheritDoc} */
        @Override
        public Serializable getSourceId()
        {
            return "TestNetworkModel";
        }

    }
}
