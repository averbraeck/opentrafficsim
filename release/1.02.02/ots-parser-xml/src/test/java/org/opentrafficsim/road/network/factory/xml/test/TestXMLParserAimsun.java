package org.opentrafficsim.road.network.factory.xml.test;

import java.awt.Dimension;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.RemoteException;

import javax.naming.NamingException;
import javax.swing.SwingUtilities;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.io.URLResource;
import org.opentrafficsim.core.dsol.AbstractOTSModel;
import org.opentrafficsim.core.dsol.OTSAnimator;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.draw.core.OTSDrawingException;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.factory.xml.XmlParserException;
import org.opentrafficsim.road.network.factory.xml.parser.XmlNetworkLaneParser;
import org.opentrafficsim.road.network.lane.conflict.ConflictBuilder;
import org.opentrafficsim.swing.gui.OTSAnimationPanel;
import org.opentrafficsim.swing.gui.OTSSimulationApplication;
import org.xml.sax.SAXException;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * New parser test.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Dec 1, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author Wouter Schakel
 */
public class TestXMLParserAimsun extends OTSSimulationApplication<OTSModelInterface>
{
    /** */
    private static final long serialVersionUID = 1L;

    /**
     * @param model the model
     * @param animationPanel the animation panel
     * @throws OTSDrawingException on drawing error
     */
    public TestXMLParserAimsun(final OTSModelInterface model, final OTSAnimationPanel animationPanel) throws OTSDrawingException
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
                    OTSAnimator simulator = new OTSAnimator();
                    TestAimsunModel xmlModel = new TestAimsunModel(simulator);
                    simulator.initialize(Time.ZERO, Duration.ZERO, Duration.createSI(3600.0), xmlModel);
                    OTSAnimationPanel animationPanel = new OTSAnimationPanel(xmlModel.getNetwork().getExtent(),
                            new Dimension(800, 600), simulator, xmlModel, DEFAULT_COLORER, xmlModel.getNetwork());
                    new TestXMLParserAimsun(xmlModel, animationPanel);
                }
                catch (SimRuntimeException | NamingException | RemoteException | OTSDrawingException exception)
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
        return "TestXMLParserAimsunNew []";
    }

    /**
     * Model to test the XML parser.
     * <p>
     * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. <br>
     * All rights reserved. BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim
     * License</a>.
     * <p>
     * $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, @version $Revision: 1401 $, by $Author: averbraeck $,
     * initial version un 27, 2015 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    static class TestAimsunModel extends AbstractOTSModel
    {
        /** */
        private static final long serialVersionUID = 20141121L;

        /** the network. */
        private OTSRoadNetwork network = null;

        /**
         * @param simulator the simulator
         */
        TestAimsunModel(final OTSSimulatorInterface simulator)
        {
            super(simulator);
        }

        /** {@inheritDoc} */
        @Override
        public final void constructModel() throws SimRuntimeException
        {
            URL url = URLResource.getResource("/AimsunOtsNetwork.xml");
            this.network = new OTSRoadNetwork("Aimsun network", true);
            try
            {
                XmlNetworkLaneParser.build(url.getPath(), this.network, getSimulator());
                ConflictBuilder.buildConflicts(this.network, this.network.getGtuType(GTUType.DEFAULTS.VEHICLE), this.simulator,
                        new ConflictBuilder.FixedWidthGenerator(new Length(2.0, LengthUnit.SI)));
            }
            catch (NetworkException | ParserConfigurationException | SAXException | OTSGeometryException | JAXBException
                    | URISyntaxException | XmlParserException | GTUException exception)
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
        public final String toString()
        {
            return "TestXMLParserAimsunNew [simulator=" + this.simulator + "]";
        }

    }

}
