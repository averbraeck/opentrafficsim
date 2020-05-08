package org.opentrafficsim.demo.network.xml;

import java.awt.Dimension;
import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.RemoteException;

import javax.naming.NamingException;
import javax.swing.SwingUtilities;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.io.URLResource;
import org.opentrafficsim.core.dsol.AbstractOTSModel;
import org.opentrafficsim.core.dsol.OTSAnimator;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gis.CoordinateTransformWGS84toRDNew;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.draw.core.OTSDrawingException;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.factory.OTSRoadNetworkUtils;
import org.opentrafficsim.road.network.factory.xml.XmlParserException;
import org.opentrafficsim.road.network.factory.xml.parser.XmlNetworkLaneParser;
import org.opentrafficsim.swing.gui.OTSAnimationPanel;
import org.opentrafficsim.swing.gui.OTSSimulationApplication;
import org.opentrafficsim.trafficcontrol.TrafficControlException;
import org.xml.sax.SAXException;

import nl.javel.gisbeans.io.esri.CoordinateTransform;
import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.animation.D2.GisRenderable2D;
import nl.tudelft.simulation.language.DSOLException;

/**
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, @version $Revision: 1401 $, by $Author: averbraeck $,
 * initial version Oct 17, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class TestXMLParserWriteXstream extends OTSSimulationApplication<OTSModelInterface>
{
    /** */
    private static final long serialVersionUID = 1L;

    /**
     * @param model the model
     * @param animationPanel the animation panel
     * @throws OTSDrawingException on drawing error
     */
    public TestXMLParserWriteXstream(final OTSModelInterface model, final OTSAnimationPanel animationPanel)
            throws OTSDrawingException
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
                    OTSAnimator simulator = new OTSAnimator("TestXMLParserWriteXstream");
                    TestXMLModelWriteXStream xmlModel = new TestXMLModelWriteXStream(simulator);
                    simulator.initialize(Time.ZERO, Duration.ZERO, Duration.instantiateSI(3600.0), xmlModel);
                    OTSAnimationPanel animationPanel = new OTSAnimationPanel(xmlModel.getNetwork().getExtent(),
                            new Dimension(800, 600), simulator, xmlModel, DEFAULT_COLORER, xmlModel.getNetwork());
                    new TestXMLParserWriteXstream(xmlModel, animationPanel);
                }
                catch (SimRuntimeException | NamingException | RemoteException | OTSDrawingException | DSOLException exception)
                {
                    exception.printStackTrace();
                }
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "TestXMLParser []";
    }

    /**
     * Model to test the XML parser.
     * <p>
     * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. <br>
     * All rights reserved. BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim
     * License</a>.
     * <p>
     * $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, @version $Revision: 1401 $, by $Author: averbraeck $,
     * initial version un 27, 2015 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    static class TestXMLModelWriteXStream extends AbstractOTSModel
    {
        /** */
        private static final long serialVersionUID = 20141121L;

        /** the network. */
        private OTSRoadNetwork network;

        /**
         * @param simulator the simulator
         */
        TestXMLModelWriteXStream(final OTSSimulatorInterface simulator)
        {
            super(simulator);
        }

        /** {@inheritDoc} */
        @Override
        public final void constructModel() throws SimRuntimeException
        {
            long millis = System.currentTimeMillis();
            URL xmlURL = URLResource.getResource("/xml/N201.xml");
            this.network = new OTSRoadNetwork("Example network", true, getSimulator());
            try
            {
                XmlNetworkLaneParser.build(xmlURL, this.network, false);

                System.out.println("parsing took : " + (System.currentTimeMillis() - millis) + " ms");

                millis = System.currentTimeMillis();
                String xml = OTSRoadNetworkUtils.toXml(this.network);
                System.out.println("making XML took : " + (System.currentTimeMillis() - millis) + " ms");
                millis = System.currentTimeMillis();
                Files.write(Paths.get("e://temp/network.txt"), xml.getBytes());
                System.out.println("writing took : " + (System.currentTimeMillis() - millis) + " ms");
            }
            catch (NetworkException | ParserConfigurationException | SAXException | OTSGeometryException | JAXBException
                    | URISyntaxException | XmlParserException | IOException | GTUException | TrafficControlException exception)
            {
                exception.printStackTrace();
            }

            URL gisURL = URLResource.getResource("/xml/N201/map.xml");
            System.err.println("GIS-map file: " + gisURL.toString());
            CoordinateTransform rdto0 = new CoordinateTransformWGS84toRDNew(0, 0);
            new GisRenderable2D(this.simulator, gisURL, rdto0);
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
            return "TestXMLModel [simulator=" + this.simulator + "]";
        }

        /** {@inheritDoc} */
        @Override
        public Serializable getSourceId()
        {
            return "TestXMLModelWriteXStream";
        }

    }

}
