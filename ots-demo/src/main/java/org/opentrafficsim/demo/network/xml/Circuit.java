package org.opentrafficsim.demo.network.xml;

import java.awt.Dimension;
import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.RemoteException;

import javax.naming.NamingException;
import javax.swing.SwingUtilities;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.io.URLResource;
import org.opentrafficsim.core.dsol.AbstractOtsModel;
import org.opentrafficsim.core.dsol.OtsAnimator;
import org.opentrafficsim.core.dsol.OtsModelInterface;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.draw.core.OtsDrawingException;
import org.opentrafficsim.road.network.OtsRoadNetwork;
import org.opentrafficsim.road.network.factory.xml.XmlParserException;
import org.opentrafficsim.road.network.factory.xml.parser.XmlNetworkLaneParser;
import org.opentrafficsim.swing.gui.OtsAnimationPanel;
import org.opentrafficsim.swing.gui.OtsSimulationApplication;
import org.opentrafficsim.trafficcontrol.TrafficControlException;
import org.xml.sax.SAXException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.language.DSOLException;

/**
 * Circuit demo
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author Wouter Schakel
 */
public class Circuit extends OtsSimulationApplication<OtsModelInterface>
{
    /** */
    private static final long serialVersionUID = 1L;

    /**
     * @param model the model
     * @param animationPanel the animation panel
     * @throws OtsDrawingException on drawing error
     */
    public Circuit(final OtsModelInterface model, final OtsAnimationPanel animationPanel) throws OtsDrawingException
    {
        super(model, animationPanel);
    }

    /**
     * Main program.
     * @param args String[]; the command line arguments (not used)
     * @throws SimRuntimeException should never happen
     */
    public static void main(final String[] args) throws SimRuntimeException
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    OtsAnimator simulator = new OtsAnimator("Circuit");
                    TestXMLModel xmlModel = new TestXMLModel(simulator);
                    simulator.initialize(Time.ZERO, Duration.ZERO, Duration.instantiateSI(3600.0), xmlModel);
                    OtsAnimationPanel animationPanel = new OtsAnimationPanel(xmlModel.getNetwork().getExtent(),
                            new Dimension(800, 600), simulator, xmlModel, DEFAULT_COLORER, xmlModel.getNetwork());
                    new Circuit(xmlModel, animationPanel);
                    animationPanel.enableSimulationControlButtons();
                }
                catch (SimRuntimeException | NamingException | RemoteException | OtsDrawingException | DSOLException exception)
                {
                    exception.printStackTrace();
                }
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "Circuit []";
    }

    /**
     * Model to test the XML parser.
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. <br>
     * All rights reserved. BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim
     * License</a>.
     * <p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     */
    static class TestXMLModel extends AbstractOtsModel
    {
        /** */
        private static final long serialVersionUID = 20141121L;

        /** the network. */
        private OtsRoadNetwork network = null;

        /**
         * @param simulator the simulator
         */
        TestXMLModel(final OtsSimulatorInterface simulator)
        {
            super(simulator);
        }

        /** {@inheritDoc} */
        @Override
        public final void constructModel() throws SimRuntimeException
        {
            try
            {
                URL xmlURL = URLResource.getResource("/resources/xml/Circuit.xml");
                this.network = new OtsRoadNetwork("Circuit", true, getSimulator());
                XmlNetworkLaneParser.build(xmlURL, this.network, false);
            }
            catch (NetworkException | ParserConfigurationException | SAXException | GtuException | OtsGeometryException
                    | JAXBException | URISyntaxException | XmlParserException | IOException | TrafficControlException exception)
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
        public final String toString()
        {
            return "TestXMLModel [simulator=" + this.simulator + "]";
        }

        /** {@inheritDoc} */
        @Override
        public Serializable getSourceId()
        {
            return "TestXMLModel";
        }

    }

}
