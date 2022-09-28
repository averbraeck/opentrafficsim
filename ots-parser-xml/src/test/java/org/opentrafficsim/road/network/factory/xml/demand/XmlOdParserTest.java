package org.opentrafficsim.road.network.factory.xml.demand;

import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.naming.NamingException;

import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.junit.Test;
import org.opentrafficsim.core.dsol.AbstractOTSModel;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimulator;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.strategical.od.Category;
import org.opentrafficsim.road.gtu.strategical.od.ODMatrix;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.factory.xml.XmlParserException;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.OTSRoadNode;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class XmlOdParserTest
{

    /** GTU types. */
    private Set<GTUType> gtuTypes = new LinkedHashSet<>();

    /** Simulator. */
    OTSSimulatorInterface simulator = new OTSSimulator("XmlOdParserTest");

    /** Network. */
    OTSRoadNetwork network = new OTSRoadNetwork("OD test", true, simulator);

    /** Parser. */
    private XmlOdParser parser;

    /**
     * Constructor.
     * @throws NetworkException on error
     * @throws OTSGeometryException on error
     * @throws SimRuntimeException on error
     * @throws NamingException on error
     */
    public XmlOdParserTest() throws NetworkException, OTSGeometryException, SimRuntimeException, NamingException
    {
        OTSModelInterface model = new AbstractOTSModel(this.simulator)
        {
            /** */
            private static final long serialVersionUID = 1L;

            @Override
            public void constructModel() throws SimRuntimeException
            {
                //
            }

            @Override
            public OTSRoadNetwork getNetwork()
            {
                return XmlOdParserTest.this.network;
            }

            @Override
            public Serializable getSourceId()
            {
                return "XmlOdParserTest.Model";
            }
        };
        this.simulator.initialize(Time.ZERO, Duration.ZERO, Duration.instantiateSI(3600.0), model);
        this.gtuTypes.add(this.network.getGtuType(GTUType.DEFAULTS.CAR));
        this.gtuTypes.add(this.network.getGtuType(GTUType.DEFAULTS.TRUCK));
        // TODO verify that Direction.ZERO will not cause problems...
        OTSRoadNode A = new OTSRoadNode(this.network, "A", new OTSPoint3D(0, 0, 0), Direction.ZERO);
        OTSRoadNode B = new OTSRoadNode(this.network, "B", new OTSPoint3D(1, 0, 0), Direction.ZERO);
        OTSRoadNode C = new OTSRoadNode(this.network, "C", new OTSPoint3D(0, 1, 0), Direction.ZERO);
        this.network.addRoute(this.network.getGtuType(GTUType.DEFAULTS.VEHICLE), new Route("AB").addNode(A).addNode(B));
        this.network.addRoute(this.network.getGtuType(GTUType.DEFAULTS.VEHICLE),
                new Route("AB2").addNode(A).addNode(C).addNode(B));
        this.network.addRoute(this.network.getGtuType(GTUType.DEFAULTS.VEHICLE), new Route("AC").addNode(A).addNode(C));
        this.network.addRoute(this.network.getGtuType(GTUType.DEFAULTS.VEHICLE),
                new Route("AC2").addNode(A).addNode(B).addNode(C));
        this.network.addRoute(this.network.getGtuType(GTUType.DEFAULTS.VEHICLE), new Route("BC").addNode(B).addNode(C));
        this.network.addRoute(this.network.getGtuType(GTUType.DEFAULTS.VEHICLE),
                new Route("BC2").addNode(B).addNode(A).addNode(C));
        this.network.addRoute(this.network.getGtuType(GTUType.DEFAULTS.VEHICLE), new Route("BA").addNode(B).addNode(A));
        this.network.addRoute(this.network.getGtuType(GTUType.DEFAULTS.VEHICLE),
                new Route("BA2").addNode(B).addNode(C).addNode(A));
        this.network.addRoute(this.network.getGtuType(GTUType.DEFAULTS.VEHICLE), new Route("CA").addNode(C).addNode(A));
        this.network.addRoute(this.network.getGtuType(GTUType.DEFAULTS.VEHICLE),
                new Route("CA2").addNode(C).addNode(B).addNode(A));
        this.network.addRoute(this.network.getGtuType(GTUType.DEFAULTS.VEHICLE), new Route("CB").addNode(C).addNode(B));
        this.network.addRoute(this.network.getGtuType(GTUType.DEFAULTS.VEHICLE),
                new Route("CB2").addNode(C).addNode(A).addNode(B));
        CrossSectionLink AB =
                new CrossSectionLink(this.network, "AB", A, B, this.network.getLinkType(LinkType.DEFAULTS.FREEWAY),
                        new OTSLine3D(A.getPoint(), B.getPoint()), LaneKeepingPolicy.KEEPRIGHT);
        new Lane(AB, "left", Length.ZERO, Length.ZERO, this.network.getLaneType(LaneType.DEFAULTS.FREEWAY),
                new LinkedHashMap<>());
    }

    /**
     * Tests OD validity. Checks that fails occur. Note: fails may occur for other reasons so these tests must be carefully
     * created and tested not to fail by using the correct input once.
     * @throws XmlParserException on error
     */
    @Test
    public void ValidityTest() throws XmlParserException
    {
        StringBuilder xml;
        ODMatrix od;

        // WS chosen to default to LINEAR, so may have no global interpolation
        // xml = new StringBuilder();
        // xml.append("<OD />");
        // shouldFail(xml.toString(), "Parser should fail without GLOBALINTERPOLATION.");

        xml = new StringBuilder();
        xml.append("<NOTOD GLOBALINTERPOLATION=\"LINEAR\">");
        xml.append("  <GLOBALTIME />");
        xml.append("</NOTOD>");
        shouldFail(xml.toString(), "Parser should fail if main tag is not 'OD'.");

        xml = new StringBuilder();
        xml.append("<OD GLOBALINTERPOLATION=\"LINEAR\">");
        xml.append("  <GLOBALTIME />");
        xml.append("  <GLOBALTIME />");
        xml.append("</OD>");
        shouldFail(xml.toString(), "Parser should fail on multiple GLOBALTIME tags.");

        xml = new StringBuilder();
        xml.append("<OD GLOBALINTERPOLATION=\"LINEAR\">");
        xml.append("  <GLOBALTIME>");
        xml.append("    <TIME />");
        xml.append("  </GLOBALTIME>");
        xml.append("</OD>");
        shouldFail(xml.toString(), "Parser should fail on missing VALUE in TIMEs.");

        xml = new StringBuilder();
        xml.append("<OD GLOBALINTERPOLATION=\"NOTLINEAR\">");
        xml.append("  <GLOBALTIME />");
        xml.append("</OD>");
        shouldFail(xml.toString(), "Parser should fail if GLOBALINTERPOLATION has bad value.");

        xml = new StringBuilder();
        xml.append("<OD GLOBALINTERPOLATION=\"LINEAR\" NAME=\"ODNAME\">");
        xml.append("  <GLOBALTIME />");
        xml.append("</OD>");
        // FAILS od = fromString(xml.toString());
        // FAILS assertTrue("NAME of OD not correctly parsed.", od.getId().equals("ODNAME"));

        xml = new StringBuilder();
        xml.append("<OD GLOBALINTERPOLATION=\"LINEAR\">");
        xml.append("  <GLOBALTIME>");
        xml.append("    <TIME VALUE=\"0s\" />");
        xml.append("    <TIME VALUE=\"1quark\" />");
        xml.append("  </GLOBALTIME>");
        xml.append("</OD>");
        shouldFail(xml.toString(), "Parser should fail on bad time unit 'quark'.");

        xml = new StringBuilder();
        xml.append("<OD GLOBALINTERPOLATION=\"LINEAR\">");
        xml.append("  <GLOBALTIME>");
        xml.append("    <TIME VALUE=\"0s\" />");
        xml.append("    <TIME VALUE=\"1h\" />");
        xml.append("  </GLOBALTIME>");
        xml.append("  <DEMAND ORIGIN=\"A\" DESTINATION=\"D\">");
        xml.append("    <LEVEL VALUE=\"100veh/s\" />");
        xml.append("    <LEVEL VALUE=\"200veh/s\" />");
        xml.append("  </DEMAND>");
        xml.append("</OD>");
        shouldFail(xml.toString(), "Parser should fail on unavailable node D.");

        xml = new StringBuilder();
        xml.append("<OD GLOBALINTERPOLATION=\"LINEAR\">");
        xml.append("  <GLOBALTIME>");
        xml.append("    <TIME VALUE=\"0s\" />");
        xml.append("    <TIME VALUE=\"1h\" />");
        xml.append("  </GLOBALTIME>");
        xml.append("  <DEMAND ORIGIN=\"A\" DESTINATION=\"B\" CATEGORY=\"CAR\">");
        xml.append("    <LEVEL VALUE=\"100veh/s\" />");
        xml.append("    <LEVEL VALUE=\"200veh/s\" />");
        xml.append("  </DEMAND>");
        xml.append("</OD>");
        shouldFail(xml.toString(), "Parser should fail if CATEGORY is used without it being defined.");

        xml = new StringBuilder();
        xml.append("<OD GLOBALINTERPOLATION=\"LINEAR\">");
        xml.append("  <GLOBALTIME />");
        xml.append("  <CATEGORY GTUTYPE=\"CAR\" />");
        xml.append("</OD>");
        shouldFail(xml.toString(), "Parser should fail if CATEGORY has no NAME.");

        xml = new StringBuilder();
        xml.append("<OD GLOBALINTERPOLATION=\"LINEAR\">");
        xml.append("  <GLOBALTIME />");
        xml.append("  <CATEGORY NAME=\"CAR\" />");
        xml.append("</OD>");
        shouldFail(xml.toString(), "Parser should fail if category is defined without object.");

        xml = new StringBuilder();
        xml.append("<OD GLOBALINTERPOLATION=\"LINEAR\">");
        xml.append("  <GLOBALTIME />");
        xml.append("  <CATEGORY NAME=\"BUS\" GTUTYPE=\"BUS\" />");
        xml.append("</OD>");
        shouldFail(xml.toString(), "Parser should fail if CATEGORY is defined with unavailable GTUType.");

        xml = new StringBuilder();
        xml.append("<OD GLOBALINTERPOLATION=\"LINEAR\">");
        xml.append("  <GLOBALTIME />");
        xml.append("  <CATEGORY NAME=\"BUS\" ROUTE=\"AD\" />");
        xml.append("</OD>");
        shouldFail(xml.toString(), "Parser should fail if CATEGORY is defined with unavailable ROUTE.");

        xml = new StringBuilder();
        xml.append("<OD GLOBALINTERPOLATION=\"LINEAR\">");
        xml.append("  <GLOBALTIME />");
        xml.append("  <CATEGORY NAME=\"BUS\" LANE=\"AB.right\" />");
        xml.append("</OD>");
        shouldFail(xml.toString(), "Parser should fail if CATEGORY is defined with unavailable LANE.");

        xml = new StringBuilder();
        xml.append("<OD GLOBALINTERPOLATION=\"LINEAR\">");
        xml.append("  <GLOBALTIME>");
        xml.append("    <TIME VALUE=\"0s\" />");
        xml.append("    <TIME VALUE=\"1h\" />");
        xml.append("  </GLOBALTIME>");
        xml.append("  <DEMAND ORIGIN=\"A\" DESTINATION=\"B\">");
        xml.append("    <LEVEL VALUE=\"100veh/s\" />");
        xml.append("    <LEVEL VALUE=\"200veh/s\" />");
        xml.append("  </DEMAND>");
        xml.append("  <DEMAND ORIGIN=\"A\" DESTINATION=\"B\">");
        xml.append("    <LEVEL VALUE=\"300veh/s\" />");
        xml.append("    <LEVEL VALUE=\"400veh/s\" />");
        xml.append("  </DEMAND>");
        xml.append("</OD>");
        shouldFail(xml.toString(), "Parser should fail if DEMAND is defined multiple times.");

        xml = new StringBuilder();
        xml.append("<OD GLOBALINTERPOLATION=\"LINEAR\">");
        xml.append("  <GLOBALTIME>");
        xml.append("    <TIME VALUE=\"0s\" />");
        xml.append("    <TIME VALUE=\"1h\" />");
        xml.append("  </GLOBALTIME>");
        xml.append("  <CATEGORY NAME=\"CAR\" GTUTYPE=\"CAR\" />");
        xml.append("  <DEMAND ORIGIN=\"A\" DESTINATION=\"B\">");
        xml.append("    <LEVEL VALUE=\"100veh/s\" />");
        xml.append("    <LEVEL VALUE=\"200veh/s\" />");
        xml.append("  </DEMAND>");
        xml.append("  <DEMAND ORIGIN=\"A\" DESTINATION=\"B\">");
        xml.append("    <LEVEL VALUE=\"300veh/s\" />");
        xml.append("    <LEVEL VALUE=\"400veh/s\" />");
        xml.append("  </DEMAND>");
        xml.append("  <DEMAND ORIGIN=\"A\" DESTINATION=\"B\" CATEGORY=\"CAR\" />");
        xml.append("</OD>");
        shouldFail(xml.toString(), "Parser should fail if main DEMAND is defined multiple times.");

        xml = new StringBuilder();
        xml.append("<OD GLOBALINTERPOLATION=\"LINEAR\">");
        xml.append("  <GLOBALTIME>");
        xml.append("    <TIME VALUE=\"0s\" />");
        xml.append("    <TIME VALUE=\"1h\" />");
        xml.append("  </GLOBALTIME>");
        xml.append("  <CATEGORY NAME=\"CAR\" GTUTYPE=\"CAR\" />");
        xml.append("  <DEMAND ORIGIN=\"A\" DESTINATION=\"B\">");
        xml.append("    <LEVEL VALUE=\"100veh/s\" />");
        xml.append("    <LEVEL VALUE=\"200veh/s\" />");
        xml.append("  </DEMAND>");
        xml.append("</OD>");
        shouldFail(xml.toString(), "Parser should fail if main DEMAND is defined multiple times.");

        xml = new StringBuilder();
        xml.append("<OD GLOBALINTERPOLATION=\"LINEAR\">");
        xml.append("  <GLOBALTIME />");
        xml.append("  <CATEGORY NAME=\"CAR\" GTUTYPE=\"CAR\" />");
        xml.append("  <CATEGORY NAME=\"TRUCK\" GTUTYPE=\"TRUCK\" ROUTE=\"AB\" />");
        xml.append("</OD>");
        shouldFail(xml.toString(), "Parser should fail if categories apply different categorizations.");

        xml = new StringBuilder();
        xml.append("<OD GLOBALINTERPOLATION=\"LINEAR\">");
        xml.append("  <GLOBALTIME />");
        xml.append("  <DEMAND ORIGIN=\"A\" DESTINATION=\"B\" />");
        xml.append("</OD>");
        shouldFail(xml.toString(), "Parser should fail if DEMAND is without category and LEVEL data.");

        xml = new StringBuilder();
        xml.append("<OD GLOBALINTERPOLATION=\"LINEAR\">");
        xml.append("  <GLOBALTIME />");
        xml.append("  <DEMAND ORIGIN=\"A\" DESTINATION=\"B\">");
        xml.append("    <LEVEL VALUE=\"2\" />");
        xml.append("    <LEVEL VALUE=\"3\" />");
        xml.append("  </DEMAND>");
        xml.append("</OD>");
        shouldFail(xml.toString(), "Parser should fail if DEMAND is without category and LEVEL data.");

        xml = new StringBuilder();
        xml.append("<OD GLOBALINTERPOLATION=\"LINEAR\">");
        xml.append("  <GLOBALTIME />");
        xml.append("  <DEMAND ORIGIN=\"A\" DESTINATION=\"B\">");
        xml.append("    <LEVEL VALUE=\"200veh/s\" />");
        xml.append("    <LEVEL />");
        xml.append("  </DEMAND>");
        xml.append("</OD>");
        shouldFail(xml.toString(), "Parser should fail if LEVEL is missing VALUE attribute.");
    }

    /**
     * Tests that demand levels are correct, including fractions.
     * @throws XmlParserException on error
     */
    @Test
    public void LevelTest() throws XmlParserException
    {
        StringBuilder xml;
        ODMatrix od;

        Node A = this.network.getNode("A");
        Node B = this.network.getNode("B");

        xml = new StringBuilder();
        xml.append("<OD GLOBALINTERPOLATION=\"LINEAR\">");
        xml.append("  <GLOBALTIME>");
        xml.append("    <TIME VALUE=\"0s\" />");
        xml.append("    <TIME VALUE=\"1h\" />");
        xml.append("  </GLOBALTIME>");
        xml.append("  <DEMAND ORIGIN=\"A\" DESTINATION=\"B\">");
        xml.append("    <LEVEL VALUE=\"100veh/s\" />");
        xml.append("    <LEVEL VALUE=\"200veh/s\" />");
        xml.append("  </DEMAND>");
        xml.append("</OD>");
        od = fromString(xml.toString());
        assertAboutEquals(od.getDemand(A, B, Category.UNCATEGORIZED, Time.instantiateSI(1800), true).si, 150);

        // the next tests will keep adding factors, and check whether the demand increases accordingly
        xml = new StringBuilder();
        xml.append("<OD GLOBALINTERPOLATION=\"LINEAR\">");
        xml.append("  <GLOBALTIME>");
        xml.append("    <TIME VALUE=\"0s\" />");
        xml.append("    <TIME VALUE=\"1h\" />");
        xml.append("  </GLOBALTIME>");
        xml.append("  <DEMAND ORIGIN=\"A\" DESTINATION=\"B\" FACTOR=\"2\">"); // x2
        xml.append("    <LEVEL VALUE=\"100veh/s\" />");
        xml.append("    <LEVEL VALUE=\"200veh/s\" />");
        xml.append("  </DEMAND>");
        xml.append("</OD>");
        od = fromString(xml.toString());
        assertAboutEquals(od.getDemand(A, B, Category.UNCATEGORIZED, Time.instantiateSI(1800), true).si, 300);

        xml = new StringBuilder();
        xml.append("<OD GLOBALINTERPOLATION=\"LINEAR\" GLOBALFACTOR=\"2\">"); // x2
        xml.append("  <GLOBALTIME>");
        xml.append("    <TIME VALUE=\"0s\" />");
        xml.append("    <TIME VALUE=\"1h\" />");
        xml.append("  </GLOBALTIME>");
        xml.append("  <DEMAND ORIGIN=\"A\" DESTINATION=\"B\" FACTOR=\"2\">");
        xml.append("    <LEVEL VALUE=\"100veh/s\" />");
        xml.append("    <LEVEL VALUE=\"200veh/s\" />");
        xml.append("  </DEMAND>");
        xml.append("</OD>");
        od = fromString(xml.toString());
        assertAboutEquals(od.getDemand(A, B, Category.UNCATEGORIZED, Time.instantiateSI(1800), true).si, 600);

        xml = new StringBuilder();
        xml.append("<OD GLOBALINTERPOLATION=\"LINEAR\" GLOBALFACTOR=\"2\">");
        xml.append("  <GLOBALTIME>");
        xml.append("    <TIME VALUE=\"0s\" />");
        xml.append("    <TIME VALUE=\"1h\" />");
        xml.append("  </GLOBALTIME>");
        xml.append("  <CATEGORY NAME=\"CAR\" GTUTYPE=\"CAR\" FACTOR=\"2\" />"); // x2
        xml.append("  <DEMAND ORIGIN=\"A\" DESTINATION=\"B\" CATEGORY=\"CAR\" FACTOR=\"2\">");
        xml.append("    <LEVEL VALUE=\"100veh/s\" />");
        xml.append("    <LEVEL VALUE=\"200veh/s\" />");
        xml.append("  </DEMAND>");
        xml.append("</OD>");
        od = fromString(xml.toString());
        assertAboutEquals(od.getDemand(A, B, getCategory("CAR"), Time.instantiateSI(1800), true).si, 1200);

        xml = new StringBuilder();
        xml.append("<OD GLOBALINTERPOLATION=\"LINEAR\" GLOBALFACTOR=\"2\">");
        xml.append("  <GLOBALTIME>");
        xml.append("    <TIME VALUE=\"0s\" />");
        xml.append("    <TIME VALUE=\"1h\" />");
        xml.append("  </GLOBALTIME>");
        xml.append("  <CATEGORY NAME=\"CAR\" GTUTYPE=\"CAR\" FACTOR=\"2\" />");
        xml.append("  <DEMAND ORIGIN=\"A\" DESTINATION=\"B\" >");
        xml.append("    <LEVEL VALUE=\"100veh/s\" />");
        xml.append("    <LEVEL VALUE=\"200veh/s\" />");
        xml.append("  </DEMAND>");
        xml.append("  <DEMAND ORIGIN=\"A\" DESTINATION=\"B\" CATEGORY=\"CAR\" FACTOR=\"2\" >");
        xml.append("    <LEVEL VALUE=\"2\" />"); // x2
        xml.append("    <LEVEL VALUE=\"2\" />");
        xml.append("  </DEMAND>");
        xml.append("</OD>");
        od = fromString(xml.toString());
        assertAboutEquals(od.getDemand(A, B, getCategory("CAR"), Time.instantiateSI(1800), true).si, 2400);

        xml = new StringBuilder();
        xml.append("<OD GLOBALINTERPOLATION=\"LINEAR\" GLOBALFACTOR=\"2\">");
        xml.append("  <GLOBALTIME>");
        xml.append("    <TIME VALUE=\"0s\" />");
        xml.append("    <TIME VALUE=\"1h\" />");
        xml.append("  </GLOBALTIME>");
        xml.append("  <CATEGORY NAME=\"CAR\" GTUTYPE=\"CAR\" FACTOR=\"2\" />");
        xml.append("  <DEMAND ORIGIN=\"A\" DESTINATION=\"B\" FACTOR=\"2\" >"); // x2
        xml.append("    <LEVEL VALUE=\"100veh/s\" />");
        xml.append("    <LEVEL VALUE=\"200veh/s\" />");
        xml.append("  </DEMAND>");
        xml.append("  <DEMAND ORIGIN=\"A\" DESTINATION=\"B\" CATEGORY=\"CAR\" FACTOR=\"2\" >");
        xml.append("    <LEVEL VALUE=\"2\" />");
        xml.append("    <LEVEL VALUE=\"2\" />");
        xml.append("  </DEMAND>");
        xml.append("</OD>");
        od = fromString(xml.toString());
        assertAboutEquals(od.getDemand(A, B, getCategory("CAR"), Time.instantiateSI(1800), true).si, 4800);
    }

    /**
     * Fails on value being more than 1e-6 apart.
     * @param obtained double; obtained value
     * @param expected double; expected value
     */
    private void assertAboutEquals(final double obtained, final double expected)
    {
        if (Math.abs(obtained - expected) > 1e-6)
        {
            fail(String.format("Demand incorrect, expected %.3f, obtained %.3f.", expected, obtained));
        }
    }

    /**
     * Returns the category of given name.
     * @param name String; name
     * @return Category; category of given name
     */
    private Category getCategory(final String name)
    {
        return this.parser.categories.get("CAR").getCategory(this.parser.categorization);
    }

    /**
     * Creates an OD from a string.
     * @param str String; xml code of OD
     * @return InputStream
     * @throws XmlParserException on error
     */
    private ODMatrix fromString(final String str) throws XmlParserException
    {
        this.parser = new XmlOdParser(this.simulator, this.network, this.gtuTypes);
        return this.parser.build(new ByteArrayInputStream(
                ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + str).getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Tests a build that should fail.
     * @param xml String; XML string
     * @param message String; message if it doesn't fail
     */
    public void shouldFail(final String xml, final String message)
    {
        try
        {
            fromString(xml);
            fail(message);
        }
        catch (XmlParserException e)
        {
            // e.printStackTrace(); // Can be used to check that fails occur for the right reason
            // expected
        }
    }

}
