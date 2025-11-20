package org.opentrafficsim.road.network.factory.xml.test;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.djutils.io.ResourceResolver;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Converter.java.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class Converter
{
    /** */
    private final String fName = "/EindhovenOld.xml"; // "/A58Old.xml";

    /** map from roadType to LinkType. */
    private Map<String, String> roadTypeToLinkTypeMap = new LinkedHashMap<>();

    /**
     * Constructor.
     * @throws ParserConfigurationException on error
     * @throws IOException on error
     * @throws SAXException on error
     */
    public Converter() throws ParserConfigurationException, SAXException, IOException
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setXIncludeAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        URL in = ResourceResolver.resolve(this.fName).asUrl();
        InputStream stream = new BufferedInputStream(new FileInputStream(in.getFile()));
        Document document = builder.parse(stream);
        NodeList xmlNodeList = document.getDocumentElement().getChildNodes();

        try (PrintWriter pw =
                new PrintWriter(in.getFile().replaceAll("Old", "").replaceAll("target/test-classes", "src/test/resources")))
        {
            pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" + "<Ots xmlns=\"http://www.opentrafficsim.org/ots\" "
                    + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                    + "  xsi:schemaLocation=\"http://www.opentrafficsim.org/ots "
                    + "https://opentrafficsim.org/docs/xsd/1.03.00/ots.xsd\"\n"
                    + "  xmlns:xi=\"http://www.w3.org/2001/XInclude\">\n" + "\n" + "  <Definitions>\n"
                    + "    <xi:include href=\"https://opentrafficsim.org/docs/xsd/1.03.00/defaults/default_gtutypes.xml\" />\n");

            NodeList defNodeList = nodesOfType(xmlNodeList, "Definitions").get(0).getChildNodes();

            List<Node> gtuTypeTagList = nodesOfType(defNodeList, "GtuType");
            writeGtuTypes(pw, gtuTypeTagList);

            List<Node> roadTypeTagList = nodesOfType(defNodeList, "RoadType");
            writeRoadLaneTypes(pw, roadTypeTagList);

            List<Node> roadLayoutTagList = nodesOfType(defNodeList, "RoadLayout");
            writeRoadLayoutTypes(pw, roadLayoutTagList);

            pw.println("  </Definitions>\n" + "\n" + "  <Network>\n");
            List<Node> networkNodeList = nodesOfType(xmlNodeList, "Node");
            writeNodes(pw, networkNodeList);

            List<Node> networkLinkList = nodesOfType(xmlNodeList, "Link");
            writeLinks(pw, networkLinkList);

            List<Node> networkRouteList = nodesOfType(xmlNodeList, "Route");

            pw.println("  </Network>\n" + "  <Demand>");

            pw.println("  </Demand>\n" + "\n" + "  <Scenario></Scenario>\n" + "\n" + "  <Run>\n"
                    + "    <RunLength>1h</RunLength>\n" + "    <RandomStreams>\n" + "      <RandomStream Id=\"default\">\n"
                    + "        <Replication Id=\"1\" Seed=\"1\" />\n" + "      </RandomStream>\n"
                    + "      <RandomStream Id=\"generation\">\n" + "        <Replication Id=\"1\" Seed=\"1\" />\n"
                    + "      </RandomStream>\n" + "    </RandomStream>\n" + "  </Run>\n" + "\n" + "  <Animation>\n"
                    + "    <Defaults>\n" + "      <Shoulder Color=\"GREEN\" />\n" + "    </Defaults>\n" + "  </Animation>\n");
            pw.println("\n</Ots>");

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Write GTU types.
     * @param pw writer
     * @param gtuTypeTagList GTU types
     */
    private void writeGtuTypes(final PrintWriter pw, final List<Node> gtuTypeTagList)
    {
        /*-
            <GtuTemplates>
              <GtuTemplate Default="true" GtuType="NL.CAR" Id="CAR">
                <LengthDist LengthUnit="m">
                  <Uniform Min="3.0" Max="7.0" />
                </LengthDist>
                <WidthDist LengthUnit="m">
                  <Uniform Min="1.7" Max="2.0" />
                </WidthDist>
                <MaxSpeedDist SpeedUnit="km/h">
                  <Constant C="120" />
                </MaxSpeedDist>
              </GtuTemplate>
            </GtuTemplates>
        */
        pw.println("    <GtuTypes>");
        for (Node gtuTypeTag : gtuTypeTagList)
        {
            String id = gtuTypeTag.getAttributes().getNamedItem("Name").getNodeValue();
            if (!id.equals("CAR") && !id.equals("TRUCK") && (!id.equals("VEHICLE")))
            {
                pw.println("      <GtuType Id=\"" + id + "\" Parent=\"VEHICLE\" Default=\"false\" />");
            }
        }
        pw.println("    </GtuTypes>\n");

        pw.println("    <GtuTemplates>");
        for (Node gtuTypeTag : gtuTypeTagList)
        {
            String id = gtuTypeTag.getAttributes().getNamedItem("Name").getNodeValue();
            pw.println("      <GtuTemplate Id=\"" + id + "\" GtuType=\"" + id + "\" Default=\"false\">");
            if (id.toLowerCase().contains("car"))
            {
                pw.println("        <LengthDist LengthUnit=\"m\">\n" + "          <Uniform Min=\"3.0\" Max=\"7.0\" />\n"
                        + "        </LengthDist>\n" + "        <WidthDist LengthUnit=\"m\">\n"
                        + "          <Uniform Min=\"1.7\" Max=\"2.0\" />\n" + "        </WidthDist>\n"
                        + "        <MaxSpeedDist SpeedUnit=\"km/h\">\n" + "          <Constant C=\"120\" />\n"
                        + "        </MaxSpeedDist>");
            }
            else
            {
                pw.println("        <LengthDist LengthUnit=\"m\">\n" + "          <Uniform Min=\"12.0\" Max=\"18.0\" />\n"
                        + "        </LengthDist>\n" + "        <WidthDist LengthUnit=\"m\">\n"
                        + "          <Uniform Min=\"2.2\" Max=\"2.4\" />\n" + "        </WidthDist>\n"
                        + "        <MaxSpeedDist SpeedUnit=\"km/h\">\n" + "          <Constant C=\"100\" />\n"
                        + "        </MaxSpeedDist>");
            }
            pw.println("      </GtuTemplate>\n");
        }
        pw.println("    </GtuTemplates>\n\n");
    }

    /**
     * Write lanes.
     * @param pw writer
     * @param roadTypeTagList lanes
     */
    private void writeRoadLaneTypes(final PrintWriter pw, final List<Node> roadTypeTagList)
    {
        /*-
        <LinkTypes>
          <LinkType Id="STREET">
            <Compatibility GtuType="NL.VEHICLE" />
            <SpeedLimit GtuType="NL.CAR" LegalSpeedLimit="40km/h" />
            <SpeedLimit GtuType="NL.TRUCK" LegalSpeedLimit="40km/h" />
          </LinkType>
        </LinkTypes>

        <LaneTypes>
          <LaneType Id="STREET">
            <Compatibility GtuType="NL.VEHICLE" />
          </LaneType>
        </LaneTypes>
         */

        pw.println("    <LinkTypes>");
        for (Node roadTypeTag : roadTypeTagList)
        {
            String id = roadTypeTag.getAttributes().getNamedItem("Name").getNodeValue();
            pw.println("      <LinkType Id=\"" + id + "\">");
            List<String> gtuTypeList = new ArrayList<>();
            List<String> gtuSpeedList = new ArrayList<>();
            for (Node compNode : nodesOfType(roadTypeTag.getChildNodes(), "SpeedLimit"))
            {
                String gtuType = compNode.getAttributes().getNamedItem("GtuType").getNodeValue();
                gtuTypeList.add(gtuType);
                gtuSpeedList.add(compNode.getAttributes().getNamedItem("LegalSpeedLimit").getNodeValue());
                pw.println("        <Compatibility GtuType=\"" + gtuType + "\" Direction=\"FORWARD\" />");
            }
            for (int i = 0; i < gtuTypeList.size(); i++)
            {
                pw.println("        <SpeedLimit GtuType=\"" + gtuTypeList.get(i) + "\" LegalSpeedLimit=\"" + gtuSpeedList.get(i)
                        + "\" />");
            }
            pw.println("      </LinkType>");
        }
        pw.println("    </LinkTypes>\n");

        pw.println("    <LaneTypes>");
        for (Node roadTypeTag : roadTypeTagList)
        {
            String id = roadTypeTag.getAttributes().getNamedItem("Name").getNodeValue();
            pw.println("      <LaneType Id=\"" + id + "\">");
            for (Node compNode : nodesOfType(roadTypeTag.getChildNodes(), "SpeedLimit"))
            {
                String gtuType = compNode.getAttributes().getNamedItem("GtuType").getNodeValue();
                pw.println("        <Compatibility GtuType=\"" + gtuType + "\" Direction=\"FORWARD\" />");
            }
            pw.println("      </LaneType>");
        }
        pw.println("    </LaneTypes>\n\n");
    }

    /**
     * Write road layouts.
     * @param pw writer
     * @param roadLayoutTagList road layouts
     */
    private void writeRoadLayoutTypes(final PrintWriter pw, final List<Node> roadLayoutTagList)
    {
        /*-
        <LinkTypes>
          <LinkType Id="STREET">
            <Compatibility GtuType="NL.VEHICLE" />
            <SpeedLimit GtuType="NL.CAR" LegalSpeedLimit="40km/h" />
            <SpeedLimit GtuType="NL.TRUCK" LegalSpeedLimit="40km/h" />
          </LinkType>
        </LinkTypes>

        <LaneTypes>
          <LaneType Id="STREET">
            <Compatibility GtuType="NL.VEHICLE" />
          </LaneType>
        </LaneTypes>
         */

        pw.println("    <RoadLayouts>");
        for (Node roadTypeTag : roadLayoutTagList)
        {
            String id = roadTypeTag.getAttributes().getNamedItem("Id").getNodeValue();
            String linkType = roadTypeTag.getAttributes().getNamedItem("LinkType").getNodeValue();
            this.roadTypeToLinkTypeMap.put(id, linkType);
            pw.println("      <RoadLayout Id=\"" + id + "\" LinkType= \"" + linkType + "\">");
            NodeList cseList = roadTypeTag.getChildNodes();
            for (int i = 0; i < cseList.getLength(); i++)
            {
                Node cseNode = cseList.item(i);
                if (cseNode.getNodeName().equals("Shoulder"))
                {
                    pw.println("        <Shoulder>");
                    String width = cseNode.getAttributes().getNamedItem("Width").getNodeValue();
                    String offset;
                    if (cseNode.getAttributes().getNamedItem("Offset") != null)
                    {
                        offset = cseNode.getAttributes().getNamedItem("Offset").getNodeValue();
                    }
                    else if (cseNode.getAttributes().getNamedItem("CenterOffset") != null)
                    {
                        offset = cseNode.getAttributes().getNamedItem("CenterOffset").getNodeValue();
                    }
                    else
                    {
                        offset = "";
                        System.err.println("offset for Shoulder in RoadLayout " + id + " unknown");
                    }
                    if (offset.length() > 0)
                    {
                        pw.println("          <CenterOffset>" + offset + "</CenterOffset>");
                    }
                    pw.println("          <Width>" + width + "</Width>");
                    pw.println("        </Shoulder>");
                }
                else if (cseNode.getNodeName().equals("Lane"))
                {
                    /*-
                      <Lane Id="M" LaneType="NL.STREET" DesignDirection="true">
                        <CenterOffset>0m</CenterOffset>
                        <Width>3.6m</Width>
                      </Lane>
                     */
                    String laneId = cseNode.getAttributes().getNamedItem("Id").getNodeValue();
                    pw.println("        <Lane Id=\"" + laneId + "\" LaneType=\"" + linkType + "\" DesignDirection=\"true\">");
                    String width = cseNode.getAttributes().getNamedItem("Width").getNodeValue();
                    String offset;
                    if (cseNode.getAttributes().getNamedItem("Offset") != null)
                    {
                        offset = cseNode.getAttributes().getNamedItem("Offset").getNodeValue();
                    }
                    else if (cseNode.getAttributes().getNamedItem("CenterOffset") != null)
                    {
                        offset = cseNode.getAttributes().getNamedItem("CenterOffset").getNodeValue();
                    }
                    else
                    {
                        offset = "";
                        System.err.println("offset for Shoulder in RoadLayout " + id + " unknown");
                    }
                    if (offset.length() > 0)
                    {
                        pw.println("          <CenterOffset>" + offset + "</CenterOffset>");
                    }
                    pw.println("          <Width>" + width + "</Width>");
                    pw.println("        </Lane>");
                }
                else if (cseNode.getNodeName().equals("NoTrafficLane"))
                {
                    pw.println("        <NoTrafficLane>");
                    String width = cseNode.getAttributes().getNamedItem("Width").getNodeValue();
                    String offset;
                    if (cseNode.getAttributes().getNamedItem("Offset") != null)
                    {
                        offset = cseNode.getAttributes().getNamedItem("Offset").getNodeValue();
                    }
                    else if (cseNode.getAttributes().getNamedItem("CenterOffset") != null)
                    {
                        offset = cseNode.getAttributes().getNamedItem("CenterOffset").getNodeValue();
                    }
                    else
                    {
                        offset = "";
                        System.err.println("offset for Shoulder in RoadLayout " + id + " unknown");
                    }
                    if (offset.length() > 0)
                    {
                        pw.println("          <CenterOffset>" + offset + "</CenterOffset>");
                    }
                    pw.println("          <Width>" + width + "</Width>");
                    pw.println("        </NoTrafficLane>");
                }
                else if (cseNode.getNodeName().equals("Stripe"))
                {
                    String type = cseNode.getAttributes().getNamedItem("Type").getNodeValue();
                    pw.println("        <Stripe Type=\"" + type + "\" />");
                }
            }
            pw.println("      </RoadLayout>");
        }
        pw.println("    </RoadLayouts>\n");
    }

    /**
     * Write nodes.
     * @param pw writer
     * @param networkNodeList node list
     */
    private void writeNodes(final PrintWriter pw, final List<Node> networkNodeList)
    {
        for (Node nodeTag : networkNodeList)
        {
            String id = nodeTag.getAttributes().getNamedItem("Id").getNodeValue();
            String coord = nodeTag.getAttributes().getNamedItem("Coordinate").getNodeValue();
            String dir = "";
            if (nodeTag.getAttributes().getNamedItem("Direction") != null)
            {
                dir = nodeTag.getAttributes().getNamedItem("Direction").getNodeValue();
            }
            pw.print("      <Node Id=\"" + id + "\" Coordinate=\"" + coord + "\" ");
            if (dir.length() > 1)
            {
                pw.println("Direction=\"" + dir + "\" />");
            }
            else
            {
                pw.println(" />");
            }
        }
        pw.println();
    }

    /**
     * Write links.
     * @param pw printer
     * @param networkLinkList link list
     */
    private void writeLinks(final PrintWriter pw, final List<Node> networkLinkList)
    {
        /*-
         OLD:
           <Link Id="L2EB" NodeStart="N2EB" NodeEnd="N3EB" RoadLayout="HW3AFSLAG">
             <Polyline IntermediatePoints="(137792,395679) (137816,395665) (137902.6725,395617.2567)" />
           </Link>

           NEW:
             <Link Id="NS23" NodeStart="NS2" NodeEnd="NS3" Type="NL.STREET">
               <Straight />
               <DefinedLayout>r3</DefinedLayout>
             </Link>
         */

        for (Node linkTag : networkLinkList)
        {
            String id = linkTag.getAttributes().getNamedItem("Id").getNodeValue();
            String snode = linkTag.getAttributes().getNamedItem("NodeStart").getNodeValue();
            String enode = linkTag.getAttributes().getNamedItem("NodeEnd").getNodeValue();
            String type = linkTag.getAttributes().getNamedItem("RoadLayout").getNodeValue();
            pw.println("    <Link Id=\"" + id + "\" NodeStart=\"" + snode + "\" NodeEnd=\"" + enode + "\" Type=\""
                    + this.roadTypeToLinkTypeMap.get(type) + "\">");
            NodeList layoutList = linkTag.getChildNodes();
            for (int i = 0; i < layoutList.getLength(); i++)
            {
                Node layoutNode = layoutList.item(i);
                if (layoutNode.getNodeName().equals("Straight"))
                {
                    pw.println("      <Straight />");
                }
                else if (layoutNode.getNodeName().equals("Bezier"))
                {
                    pw.print("      <Bezier ");
                    if (layoutNode.getAttributes().getNamedItem("Shape") != null)
                    {
                        pw.print("Shape=\"" + layoutNode.getAttributes().getNamedItem("Shape").getNodeValue() + "\" ");
                    }
                    pw.println("/>");
                }
                else if (layoutNode.getNodeName().equals("Polyline"))
                {
                    pw.println("      <Polyline>");
                    String[] coords = layoutNode.getAttributes().getNamedItem("IntermediatePoints").getNodeValue().split(" ");
                    for (String c : coords)
                    {
                        pw.println("        <Coordinate>" + c + "</Coordinate>");
                    }
                    pw.println("      </Polyline>");
                }
                else if (layoutNode.getNodeName().equals("Arc"))
                {
                    // <Arc Direction="lr" Radius="xm"/>
                    String dir = layoutNode.getAttributes().getNamedItem("Direction").getNodeValue();
                    String radius = layoutNode.getAttributes().getNamedItem("Radius").getNodeValue();
                    pw.println("      <Arc Direction=\"" + dir + "\" Radius=\"" + radius + "\"/>");
                }
            }
            pw.println("      <DefinedLayout>" + type + "</DefinedLayout>");
            pw.println("    </Link>\n");
        }
        pw.println();
    }

    /**
     * Retrieves nodes of type.
     * @param parent parent
     * @param type type
     * @return nodes of type
     */
    private List<Node> nodesOfType(final NodeList parent, final String type)
    {
        List<Node> nodeList = new ArrayList<>();
        for (int i = 0; i < parent.getLength(); i++)
        {
            Node n = parent.item(i);
            if (n.getNodeName().equals(type))
            {
                nodeList.add(n);
            }
        }
        return nodeList;
    }

    /**
     * Main method.
     * @param args args
     * @throws IOException on error
     * @throws SAXException on error
     * @throws ParserConfigurationException on error
     */
    public static void main(final String[] args) throws ParserConfigurationException, SAXException, IOException
    {
        new Converter();
    }

}
