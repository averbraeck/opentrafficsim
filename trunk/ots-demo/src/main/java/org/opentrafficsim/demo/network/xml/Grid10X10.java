package org.opentrafficsim.demo.network.xml;

import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;

import javax.naming.NamingException;
import javax.swing.SwingUtilities;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.dsol.AbstractOTSModel;
import org.opentrafficsim.core.dsol.OTSAnimator;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.demo.network.xml.Grid10X10.TestXMLModelGrid;
import org.opentrafficsim.draw.core.OTSDrawingException;
import org.opentrafficsim.draw.factory.DefaultAnimationFactory;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.factory.xml.XmlParserException;
import org.opentrafficsim.road.network.factory.xml.parser.XmlNetworkLaneParser;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLight;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLightColor;
import org.opentrafficsim.swing.gui.OTSAnimationPanel;
import org.opentrafficsim.swing.gui.OTSSimulationApplication;
import org.xml.sax.SAXException;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * Grid10X10.java.
 * <p>
 * Copyright (c) 2019-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://djunits.org/docs/license.html">DJUNITS License</a>.
 * <p>
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
@SuppressWarnings({"checkstyle:operatorwrap", "checkstyle:linelength"})
public class Grid10X10 extends OTSSimulationApplication<TestXMLModelGrid>
{
    /** */
    private static final long serialVersionUID = 1L;

    //@formatter:off
    /** xml heading. */
    private static final String XML_HEADER = "<?xml version='1.0' encoding='UTF-8'?>\r\n" + 
        "<OTS xmlns=\"http://www.opentrafficsim.org/ots\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + 
        "  xsi:schemaLocation=\"http://www.opentrafficsim.org/ots ../../../../../ots-xsd/src/main/resources/xsd/1.03.00/ots.xsd\"\r\n" + 
        "  xmlns:xi=\"http://www.w3.org/2001/XInclude\">\r\n" + 
        "\r\n" + 
        "  <DEFINITIONS>\r\n" + 
        "    <xi:include href=\"https://opentrafficsim.org/docs/xsd/1.03.00/defaults/default_gtutypes.xml\">\r\n" + 
        "      <xi:fallback>\r\n" + 
        "        <xi:include href=\"../xsd/1.03.00/defaults/default_gtutypes.xml\">\r\n" + 
        "          <xi:fallback>\r\n" + 
        "            <xi:include href=\"xsd/1.03.00/defaults/default_gtutypes.xml\" />\r\n" + 
        "          </xi:fallback>\r\n" + 
        "        </xi:include>\r\n" + 
        "      </xi:fallback>\r\n" + 
        "    </xi:include>\r\n" + 
        "    \r\n" + 
        "    <xi:include href=\"https://opentrafficsim.org/docs/xsd/1.03.00/defaults/default_linktypes.xml\">\r\n" + 
        "      <xi:fallback>\r\n" + 
        "        <xi:include href=\"../xsd/1.03.00/defaults/default_linktypes.xml\">\r\n" + 
        "          <xi:fallback>\r\n" + 
        "            <xi:include href=\"xsd/1.03.00/defaults/default_linktypes.xml\" />\r\n" + 
        "          </xi:fallback>\r\n" + 
        "        </xi:include>\r\n" + 
        "      </xi:fallback>\r\n" + 
        "    </xi:include>\r\n" + 
        "\r\n" + 
        "    <xi:include href=\"https://opentrafficsim.org/docs/xsd/1.03.00/defaults/default_lanetypes.xml\">\r\n" + 
        "      <xi:fallback>\r\n" + 
        "        <xi:include href=\"../xsd/1.03.00/defaults/default_lanetypes.xml\">\r\n" + 
        "          <xi:fallback>\r\n" + 
        "            <xi:include href=\"xsd/1.03.00/defaults/default_lanetypes.xml\" />\r\n" + 
        "          </xi:fallback>\r\n" + 
        "        </xi:include>\r\n" + 
        "      </xi:fallback>\r\n" + 
        "    </xi:include>\r\n" + 
        "\r\n" + 
        "    <xi:include href=\"https://opentrafficsim.org/docs/xsd/1.03.00/defaults/default_gtutemplates.xml\">\r\n" + 
        "      <xi:fallback>\r\n" + 
        "        <xi:include href=\"../xsd/1.03.00/defaults/default_gtutemplates.xml\">\r\n" + 
        "          <xi:fallback>\r\n" + 
        "            <xi:include href=\"xsd/1.03.00/defaults/default_gtutemplates.xml\" />\r\n" + 
        "          </xi:fallback>\r\n" + 
        "        </xi:include>\r\n" + 
        "      </xi:fallback>\r\n" + 
        "    </xi:include>\r\n" + 
        "\r\n" + 
        "    <LANETYPES>\r\n" + 
        "      <LANETYPE ID=\"STREET\">\r\n" + 
        "        <COMPATIBILITY GTUTYPE=\"ROAD_USER\" DIRECTION=\"FORWARD\" />\r\n" + 
        "      </LANETYPE>\r\n" + 
        "    </LANETYPES>\r\n" + 
        "\r\n" + 
        "    <GTUTYPES>\r\n" + 
        "      <!-- <GTUTYPE PARENT=\"ROAD_USER\" ID=\"CAR\" /> <GTUTYPE PARENT=\"ROAD_USER\" ID=\"TRUCK\" /> -->\r\n" + 
        "    </GTUTYPES>\r\n" + 
        "\r\n" + 
        "    <GTUTEMPLATES>\r\n" + 
        "      <GTUTEMPLATE GTUTYPE=\"CAR\" ID=\"CARS\">\r\n" + 
        "        <LENGTHDIST LENGTHUNIT=\"m\">\r\n" + 
        "          <UNIFORM MIN=\"4\" MAX=\"7\" />\r\n" + 
        "        </LENGTHDIST>\r\n" + 
        "        <WIDTHDIST LENGTHUNIT=\"m\">\r\n" + 
        "          <UNIFORM MIN=\"1.7\" MAX=\"2\" />\r\n" + 
        "        </WIDTHDIST>\r\n" + 
        "        <MAXSPEEDDIST SPEEDUNIT=\"km/h\">\r\n" + 
        "          <CONSTANT C=\"120\" />\r\n" + 
        "        </MAXSPEEDDIST>\r\n" + 
        "      </GTUTEMPLATE>\r\n" + 
        "      <GTUTEMPLATE GTUTYPE=\"TRUCK\" ID=\"TRUCKS\">\r\n" + 
        "        <LENGTHDIST LENGTHUNIT=\"m\">\r\n" + 
        "          <UNIFORM MIN=\"16\" MAX=\"24\" />\r\n" + 
        "        </LENGTHDIST>\r\n" + 
        "        <WIDTHDIST LENGTHUNIT=\"m\">\r\n" + 
        "          <UNIFORM MIN=\"2.2\" MAX=\"2.7\" />\r\n" + 
        "        </WIDTHDIST>\r\n" + 
        "        <MAXSPEEDDIST SPEEDUNIT=\"km/h\">\r\n" + 
        "          <CONSTANT C=\"100\" />\r\n" + 
        "        </MAXSPEEDDIST>\r\n" + 
        "\r\n" + 
        "      </GTUTEMPLATE>\r\n" + 
        "    </GTUTEMPLATES>\r\n" + 
        "\r\n" + 
        "    <LINKTYPES>\r\n" + 
        "      <LINKTYPE ID=\"STREET\">\r\n" + 
        "        <COMPATIBILITY GTUTYPE=\"ROAD_USER\" DIRECTION=\"FORWARD\" />\r\n" + 
        "      </LINKTYPE>\r\n" + 
        "    </LINKTYPES>\r\n" + 
        "\r\n" + 
        "    <ROADLAYOUTS>\r\n" + 
        "      <ROADLAYOUT ID=\"r1\" LINKTYPE=\"STREET\">\r\n" + 
        "        <SHOULDER>\r\n" + 
        "          <WIDTH>2m</WIDTH>\r\n" + 
        "        </SHOULDER>\r\n" + 
        "        <STRIPE TYPE=\"SOLID\">\r\n" + 
        "        </STRIPE>\r\n" + 
        "        <LANE ID=\"FORWARD3\" LANETYPE=\"STREET\" DESIGNDIRECTION=\"true\">\r\n" + 
        "          <WIDTH>3.3m</WIDTH>\r\n" + 
        "          <SPEEDLIMIT GTUTYPE=\"CAR\" LEGALSPEEDLIMIT=\"50 km/h\"></SPEEDLIMIT>\r\n" + 
        "        </LANE>\r\n" + 
        "        <STRIPE TYPE=\"DASHED\">\r\n" + 
        "        </STRIPE>\r\n" + 
        "        <LANE ID=\"FORWARD2\" LANETYPE=\"STREET\" DESIGNDIRECTION=\"true\">\r\n" + 
        "          <CENTEROFFSET>-6.2m</CENTEROFFSET>\r\n" + 
        "          <WIDTH>3.3m</WIDTH>\r\n" + 
        "          <SPEEDLIMIT GTUTYPE=\"CAR\" LEGALSPEEDLIMIT=\"50 km/h\"></SPEEDLIMIT>\r\n" + 
        "        </LANE>\r\n" + 
        "        <STRIPE TYPE=\"DASHED\">\r\n" + 
        "        </STRIPE>\r\n" + 
        "        <LANE ID=\"FORWARD1\" LANETYPE=\"STREET\" DESIGNDIRECTION=\"true\">\r\n" + 
        "          <WIDTH>3.3m</WIDTH>\r\n" + 
        "          <SPEEDLIMIT GTUTYPE=\"CAR\" LEGALSPEEDLIMIT=\"50 km/h\"></SPEEDLIMIT>\r\n" + 
        "        </LANE>\r\n" + 
        "        <STRIPE TYPE=\"SOLID\">\r\n" + 
        "        </STRIPE>\r\n" + 
        "      </ROADLAYOUT>\r\n" + 
        "    </ROADLAYOUTS>\r\n" + 
        "  </DEFINITIONS>\r\n";
    
    /** xml footer. */
    private static final String XML_FOOTER = "  <CONTROL>\r\n" + 
            "  </CONTROL>\r\n" + 
            "  <MODEL />\r\n" + 
            "  <SCENARIO />\r\n" + 
            "  <RUN>\r\n" + 
            "    <RUNLENGTH>3600s</RUNLENGTH>\r\n" + 
            "    <NUMBERREPLICATIONS>1</NUMBERREPLICATIONS>\r\n" + 
            "    <RANDOMSTREAMS>\r\n" + 
            "      <RANDOMSTREAM ID=\"default\">\r\n" + 
            "        <REPLICATION SEED=\"1\" ID=\"1\" />\r\n" + 
            "      </RANDOMSTREAM>\r\n" + 
            "      <RANDOMSTREAM ID=\"generation\">\r\n" + 
            "        <REPLICATION SEED=\"1\" ID=\"1\" />\r\n" + 
            "      </RANDOMSTREAM>\r\n" + 
            "    </RANDOMSTREAMS>\r\n" + 
            "  </RUN>\r\n" + 
            "\r\n" + 
            "</OTS>\r\n";
    
    //@formatter:on

    /** number of x/y. */
    private static final int SIZE = 100;

    /** spacing. */
    private static final int SPACING = 50;

    /**
     * @param model model
     * @param panel panel
     * @throws OTSDrawingException on error
     */
    public Grid10X10(final TestXMLModelGrid model, final OTSAnimationPanel panel) throws OTSDrawingException
    {
        super(model, panel);
        //System.out.println("ANIMATEMAP.SIZE = " + this.defaultAnimationFactory.animatedObjects.size());
    }

    /** */
    private DefaultAnimationFactory defaultAnimationFactory;

    /**
     * Creates the animation objects. This method is overridable. The default uses {@code DefaultAnimationFactory}.
     * @throws OTSDrawingException on animation error
     */
    @Override
    protected void animateNetwork() throws OTSDrawingException
    {
        this.defaultAnimationFactory = DefaultAnimationFactory.animateNetwork(getModel().getNetwork(),
                getModel().getSimulator(), getAnimationPanel().getGTUColorer());
    }

    /**
     * @param args args
     */
    public static void main(final String[] args)
    {
        StringBuilder xml = new StringBuilder();
        xml.append(XML_HEADER);
        xml.append("<NETWORK>\n");
        for (int x = 0; x < SIZE; x++)
        {
            xml.append(String.format(" <NODE ID=\"N%d\" COORDINATE=\"(%d,%d)\" DIRECTION=\"90 deg(E)\" />\n", x,
                    SPACING * x + SPACING / 2, 0));
            xml.append(String.format(" <NODE ID=\"S%d\" COORDINATE=\"(%d,%d)\" DIRECTION=\"90 deg(E)\" />\n", x,
                    SPACING * x + SPACING / 2, SIZE * SPACING));
        }
        for (int y = 0; y < SIZE; y++)
        {
            xml.append(String.format(" <NODE ID=\"W%d\" COORDINATE=\"(%d,%d)\" DIRECTION=\"0 deg(E)\" />\n", y, 0,
                    SPACING * y + SPACING / 2));
            xml.append(String.format(" <NODE ID=\"E%d\" COORDINATE=\"(%d,%d)\" DIRECTION=\"0 deg(E)\" />\n", y,
                    SIZE * SPACING, SPACING * y + SPACING / 2));
        }

        //@formatter:off
        final String link = 
                "    <LINK ID=\"%s\" NODESTART=\"%s\" NODEEND=\"%s\" TYPE=\"STREET\">\r\n" + 
                        "      <STRAIGHT />\r\n" + 
                        "      <DEFINEDLAYOUT>r1</DEFINEDLAYOUT>\r\n" + 
                        "    </LINK>\r\n";
        //@formatter:on

        for (int x = 0; x < SIZE; x++)
        {
            xml.append(String.format(link, "NS" + x, "N" + x, "S" + x));
        }
        for (int y = 0; y < SIZE; y++)
        {
            xml.append(String.format(link, "WE" + y, "W" + y, "E" + y));
        }

        xml.append("</NETWORK>\n<NETWORKDEMAND />\n");
        xml.append(XML_FOOTER);

        System.out.println(xml.toString());

        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    OTSAnimator simulator = new OTSAnimator();
                    TestXMLModelGrid xmlModel = new TestXMLModelGrid(simulator, xml.toString());
                    simulator.initialize(Time.ZERO, Duration.ZERO, Duration.instantiateSI(3600.0), xmlModel);
                    OTSAnimationPanel animationPanel = new OTSAnimationPanel(xmlModel.getNetwork().getExtent(),
                            new Dimension(800, 600), simulator, xmlModel, DEFAULT_COLORER, xmlModel.getNetwork());
                    new Grid10X10(xmlModel, animationPanel);
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
    public final String toString()
    {
        return "TestXMLParserGrid []";
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
    static class TestXMLModelGrid extends AbstractOTSModel
    {
        /** */
        private static final long serialVersionUID = 20141121L;

        /** the network. */
        private OTSRoadNetwork network;

        /** the xml string. */
        private final String xml;

        /**
         * @param simulator the simulator
         * @param xml xml string
         */
        TestXMLModelGrid(final OTSSimulatorInterface simulator, final String xml)
        {
            super(simulator);
            this.xml = xml;
        }

        /** {@inheritDoc} */
        @Override
        public final void constructModel() throws SimRuntimeException
        {

            this.network = new OTSRoadNetwork("Grid network", true);
            try
            {
                ByteArrayInputStream bos = new ByteArrayInputStream(this.xml.getBytes(StandardCharsets.UTF_8));
                XmlNetworkLaneParser.build(bos, this.network, getSimulator(), true);
            }
            catch (NetworkException | ParserConfigurationException | SAXException | OTSGeometryException | JAXBException
                    | URISyntaxException | XmlParserException | GTUException exception)
            {
                exception.printStackTrace();
            }

            for (TrafficLight tl : this.network.getObjectMap(TrafficLight.class).values())
            {
                tl.setTrafficLightColor(TrafficLightColor.GREEN);
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
            return "TestXMLModelGrid [simulator=" + this.simulator + "]";
        }

    }

}
