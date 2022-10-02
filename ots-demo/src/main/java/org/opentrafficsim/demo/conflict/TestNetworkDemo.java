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
import org.opentrafficsim.core.animation.gtu.colorer.DefaultSwitchableGtuColorer;
import org.opentrafficsim.core.dsol.AbstractOtsModel;
import org.opentrafficsim.core.dsol.OtsAnimator;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.demo.conflict.TestNetworkDemo.TestNetworkModel;
import org.opentrafficsim.draw.core.OtsDrawingException;
import org.opentrafficsim.road.network.OtsRoadNetwork;
import org.opentrafficsim.road.network.factory.xml.parser.XmlNetworkLaneParser;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.conflict.ConflictBuilder;
import org.opentrafficsim.road.network.lane.conflict.LaneCombinationList;
import org.opentrafficsim.swing.gui.OtsAnimationPanel;
import org.opentrafficsim.swing.gui.OtsSimulationApplication;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.language.DSOLException;

/**
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class TestNetworkDemo extends OtsSimulationApplication<TestNetworkModel>
{
    /** */
    private static final long serialVersionUID = 20161211L;

    /**
     * Create a network test demo.
     * @param title String; the title of the Frame
     * @param panel OTSAnimationPanel; the tabbed panel to display
     * @param model TestNetworkModel; the model
     * @throws OtsDrawingException on animation error
     */
    public TestNetworkDemo(final String title, final OtsAnimationPanel panel, final TestNetworkModel model)
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
            OtsAnimator simulator = new OtsAnimator("TestNetworkDemo");
            final TestNetworkModel networkModel = new TestNetworkModel(simulator);
            simulator.initialize(Time.ZERO, Duration.ZERO, Duration.instantiateSI(3600.0), networkModel);
            OtsAnimationPanel animationPanel =
                    new OtsAnimationPanel(networkModel.getNetwork().getExtent(), new Dimension(800, 600), simulator,
                            networkModel, new DefaultSwitchableGtuColorer(), networkModel.getNetwork());
            TestNetworkDemo app = new TestNetworkDemo("Network test demo", animationPanel, networkModel);
            app.setExitOnClose(exitOnClose);
            animationPanel.enableSimulationControlButtons();
        }
        catch (SimRuntimeException | NamingException | RemoteException | OtsDrawingException | DSOLException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * The simulation model.
     */
    static class TestNetworkModel extends AbstractOtsModel
    {

        /** */
        private static final long serialVersionUID = 20161211L;

        /** The network. */
        private OtsRoadNetwork network;

        /**
         * @param simulator OTSSimulatorInterface; the simulator for this model
         */
        TestNetworkModel(final OtsSimulatorInterface simulator)
        {
            super(simulator);
        }

        /** {@inheritDoc} */
        @Override
        public void constructModel() throws SimRuntimeException
        {
            try
            {
                URL xmlURL = URLResource.getResource("/resources/conflict/Test-Network-14.xml");
                this.network = new OtsRoadNetwork("Test-Network-14", true, getSimulator());
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
                ConflictBuilder.buildConflicts(this.network, this.network.getGtuType(GtuType.DEFAULTS.VEHICLE), this.simulator,
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
        public OtsRoadNetwork getNetwork()
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
