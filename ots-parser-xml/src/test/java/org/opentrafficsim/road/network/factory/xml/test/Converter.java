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

import org.djutils.io.URLResource;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Converter.java.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class Converter
{
    /** */
    private final String fName = "/EindhovenOld.xml"; // "/A58Old.xml";

    /** map from roadType to LinkType. */
    private Map<String, String> roadTypeToLinkTypeMap = new LinkedHashMap<>();

    /**
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
        URL in = URLResource.getResource(this.fName);
        InputStream stream = new BufferedInputStream(new FileInputStream(in.getFile()));
        Document document = builder.parse(stream);
        NodeList xmlNodeList = document.getDocumentElement().getChildNodes();

        try (PrintWriter pw =
                new PrintWriter(in.getFile().replaceAll("Old", "").replaceAll("target/test-classes", "src/test/resources")))
        {
            pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" + "<OTS xmlns=\"http://www.opentrafficsim.org/ots\" "
                    + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                    + "  xsi:schemaLocation=\"http://www.opentrafficsim.org/ots "
                    + "https://opentrafficsim.org/docs/xsd/1.03.00/ots.xsd\"\n"
                    + "  xmlns:xi=\"http://www.w3.org/2001/XInclude\">\n" + "\n" + "  <DEFINITIONS>\n"
                    + "    <xi:include href=\"https://opentrafficsim.org/docs/xsd/1.03.00/defaults/default_gtutypes.xml\" />\n");

            NodeList defNodeList = nodesOfType(xmlNodeList, "DEFINITIONS").get(0).getChildNodes();

            List<Node> gtuTypeTagList = nodesOfType(defNodeList, "GTUTYPE");
            writeGtuTypes(pw, gtuTypeTagList);

            List<Node> roadTypeTagList = nodesOfType(defNodeList, "ROADTYPE");
            writeRoadLaneTypes(pw, roadTypeTagList);

            List<Node> roadLayoutTagList = nodesOfType(defNodeList, "ROADLAYOUT");
            writeRoadLayoutTypes(pw, roadLayoutTagList);

            pw.println("  </DEFINITIONS>\n" + "\n" + "  <NETWORK>\n");
            List<Node> networkNodeList = nodesOfType(xmlNodeList, "NODE");
            writeNodes(pw, networkNodeList);

            List<Node> networkLinkList = nodesOfType(xmlNodeList, "LINK");
            writeLinks(pw, networkLinkList);

            List<Node> networkRouteList = nodesOfType(xmlNodeList, "ROUTE");

            pw.println("  </NETWORK>\n" + "  <NETWORKDEMAND>");

            pw.println("  </NETWORKDEMAND>\n" + "\n" + "  <SCENARIO></SCENARIO>\n" + "\n" + "  <RUN>\n"
                    + "    <RUNLENGTH>1h</RUNLENGTH>\n" + "    <RANDOMSTREAMS>\n" + "      <RANDOMSTREAM ID=\"default\">\n"
                    + "        <REPLICATION ID=\"1\" SEED=\"1\" />\n" + "      </RANDOMSTREAM>\n"
                    + "      <RANDOMSTREAM ID=\"generation\">\n" + "        <REPLICATION ID=\"1\" SEED=\"1\" />\n"
                    + "      </RANDOMSTREAM>\n" + "    </RANDOMSTREAMS>\n" + "  </RUN>\n" + "\n" + "  <ANIMATION>\n"
                    + "    <DEFAULTS>\n" + "      <SHOULDER COLOR=\"GREEN\" />\n" + "    </DEFAULTS>\n" + "  </ANIMATION>\n");
            pw.println("\n</OTS>");

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void writeGtuTypes(final PrintWriter pw, final List<Node> gtuTypeTagList)
    {
        /*-
            <GTUTEMPLATES>
              <GTUTEMPLATE DEFAULT="true" GTUTYPE="CAR" ID="CAR">
                <LENGTHDIST LENGTHUNIT="m">
                  <UNIFORM MIN="3.0" MAX="7.0" />
                </LENGTHDIST>
                <WIDTHDIST LENGTHUNIT="m">
                  <UNIFORM MIN="1.7" MAX="2.0" />
                </WIDTHDIST>
                <MAXSPEEDDIST SPEEDUNIT="km/h">
                  <CONSTANT C="120" />
                </MAXSPEEDDIST>
              </GTUTEMPLATE>
            </GTUTEMPLATES>
        */
        pw.println("    <GTUTYPES>");
        for (Node gtuTypeTag : gtuTypeTagList)
        {
            String id = gtuTypeTag.getAttributes().getNamedItem("NAME").getNodeValue();
            if (!id.equals("CAR") && !id.equals("TRUCK") && (!id.equals("VEHICLE")))
            {
                pw.println("      <GTUTYPE ID=\"" + id + "\" PARENT=\"VEHICLE\" DEFAULT=\"false\" />");
            }
        }
        pw.println("    </GTUTYPES>\n");

        pw.println("    <GTUTEMPLATES>");
        for (Node gtuTypeTag : gtuTypeTagList)
        {
            String id = gtuTypeTag.getAttributes().getNamedItem("NAME").getNodeValue();
            pw.println("      <GTUTEMPLATE ID=\"" + id + "\" GTUTYPE=\"" + id + "\" DEFAULT=\"false\">");
            if (id.toLowerCase().contains("car"))
            {
                pw.println("        <LENGTHDIST LENGTHUNIT=\"m\">\n" + "          <UNIFORM MIN=\"3.0\" MAX=\"7.0\" />\n"
                        + "        </LENGTHDIST>\n" + "        <WIDTHDIST LENGTHUNIT=\"m\">\n"
                        + "          <UNIFORM MIN=\"1.7\" MAX=\"2.0\" />\n" + "        </WIDTHDIST>\n"
                        + "        <MAXSPEEDDIST SPEEDUNIT=\"km/h\">\n" + "          <CONSTANT C=\"120\" />\n"
                        + "        </MAXSPEEDDIST>");
            }
            else
            {
                pw.println("        <LENGTHDIST LENGTHUNIT=\"m\">\n" + "          <UNIFORM MIN=\"12.0\" MAX=\"18.0\" />\n"
                        + "        </LENGTHDIST>\n" + "        <WIDTHDIST LENGTHUNIT=\"m\">\n"
                        + "          <UNIFORM MIN=\"2.2\" MAX=\"2.4\" />\n" + "        </WIDTHDIST>\n"
                        + "        <MAXSPEEDDIST SPEEDUNIT=\"km/h\">\n" + "          <CONSTANT C=\"100\" />\n"
                        + "        </MAXSPEEDDIST>");
            }
            pw.println("      </GTUTEMPLATE>\n");
        }
        pw.println("    </GTUTEMPLATES>\n\n");
    }

    private void writeRoadLaneTypes(final PrintWriter pw, final List<Node> roadTypeTagList)
    {
        /*-
        <LINKTYPES>
          <LINKTYPE ID="STREET">
            <COMPATIBILITY GTUTYPE="VEHICLE" DIRECTION="FORWARD" />
            <SPEEDLIMIT GTUTYPE="CAR" LEGALSPEEDLIMIT="40km/h" />
            <SPEEDLIMIT GTUTYPE="TRUCK" LEGALSPEEDLIMIT="40km/h" />
          </LINKTYPE>
        </LINKTYPES>
        
        <LANETYPES>
          <LANETYPE ID="STREET">
            <COMPATIBILITY GTUTYPE="VEHICLE" DIRECTION="FORWARD" />
          </LANETYPE>
        </LANETYPES>
         */

        pw.println("    <LINKTYPES>");
        for (Node roadTypeTag : roadTypeTagList)
        {
            String id = roadTypeTag.getAttributes().getNamedItem("NAME").getNodeValue();
            pw.println("      <LINKTYPE ID=\"" + id + "\">");
            List<String> gtuTypeList = new ArrayList<>();
            List<String> gtuSpeedList = new ArrayList<>();
            for (Node compNode : nodesOfType(roadTypeTag.getChildNodes(), "SPEEDLIMIT"))
            {
                String gtuType = compNode.getAttributes().getNamedItem("GTUTYPE").getNodeValue();
                gtuTypeList.add(gtuType);
                gtuSpeedList.add(compNode.getAttributes().getNamedItem("LEGALSPEEDLIMIT").getNodeValue());
                pw.println("        <COMPATIBILITY GTUTYPE=\"" + gtuType + "\" DIRECTION=\"FORWARD\" />");
            }
            for (int i = 0; i < gtuTypeList.size(); i++)
            {
                pw.println("        <SPEEDLIMIT GTUTYPE=\"" + gtuTypeList.get(i) + "\" LEGALSPEEDLIMIT=\"" + gtuSpeedList.get(i)
                        + "\" />");
            }
            pw.println("      </LINKTYPE>");
        }
        pw.println("    </LINKTYPES>\n");

        pw.println("    <LANETYPES>");
        for (Node roadTypeTag : roadTypeTagList)
        {
            String id = roadTypeTag.getAttributes().getNamedItem("NAME").getNodeValue();
            pw.println("      <LANETYPE ID=\"" + id + "\">");
            for (Node compNode : nodesOfType(roadTypeTag.getChildNodes(), "SPEEDLIMIT"))
            {
                String gtuType = compNode.getAttributes().getNamedItem("GTUTYPE").getNodeValue();
                pw.println("        <COMPATIBILITY GTUTYPE=\"" + gtuType + "\" DIRECTION=\"FORWARD\" />");
            }
            pw.println("      </LANETYPE>");
        }
        pw.println("    </LANETYPES>\n\n");
    }

    private void writeRoadLayoutTypes(final PrintWriter pw, final List<Node> roadLayoutTagList)
    {
        /*-
        <LINKTYPES>
          <LINKTYPE ID="STREET">
            <COMPATIBILITY GTUTYPE="VEHICLE" DIRECTION="FORWARD" />
            <SPEEDLIMIT GTUTYPE="CAR" LEGALSPEEDLIMIT="40km/h" />
            <SPEEDLIMIT GTUTYPE="TRUCK" LEGALSPEEDLIMIT="40km/h" />
          </LINKTYPE>
        </LINKTYPES>
        
        <LANETYPES>
          <LANETYPE ID="STREET">
            <COMPATIBILITY GTUTYPE="VEHICLE" DIRECTION="FORWARD" />
          </LANETYPE>
        </LANETYPES>
         */

        pw.println("    <ROADLAYOUTS>");
        for (Node roadTypeTag : roadLayoutTagList)
        {
            String id = roadTypeTag.getAttributes().getNamedItem("ID").getNodeValue();
            String linkType = roadTypeTag.getAttributes().getNamedItem("LINKTYPE").getNodeValue();
            this.roadTypeToLinkTypeMap.put(id, linkType);
            pw.println("      <ROADLAYOUT ID=\"" + id + "\" LINKTYPE= \"" + linkType + "\">");
            NodeList cseList = roadTypeTag.getChildNodes();
            for (int i = 0; i < cseList.getLength(); i++)
            {
                Node cseNode = cseList.item(i);
                if (cseNode.getNodeName().equals("SHOULDER"))
                {
                    pw.println("        <SHOULDER>");
                    String width = cseNode.getAttributes().getNamedItem("WIDTH").getNodeValue();
                    String offset;
                    if (cseNode.getAttributes().getNamedItem("OFFSET") != null)
                        offset = cseNode.getAttributes().getNamedItem("OFFSET").getNodeValue();
                    else if (cseNode.getAttributes().getNamedItem("CENTEROFFSET") != null)
                        offset = cseNode.getAttributes().getNamedItem("CENTEROFFSET").getNodeValue();
                    else
                    {
                        offset = "";
                        System.err.println("offset for SHOULDER in ROADLAYOUT " + id + " unknown");
                    }
                    if (offset.length() > 0)
                        pw.println("          <CENTEROFFSET>" + offset + "</CENTEROFFSET>");
                    pw.println("          <WIDTH>" + width + "</WIDTH>");
                    pw.println("        </SHOULDER>");
                }
                else if (cseNode.getNodeName().equals("LANE"))
                {
                    /*-
                      <LANE ID="M" LANETYPE="STREET" DESIGNDIRECTION="true">
                        <CENTEROFFSET>0m</CENTEROFFSET>
                        <WIDTH>3.6m</WIDTH>
                      </LANE>
                     */
                    String laneId = cseNode.getAttributes().getNamedItem("ID").getNodeValue();
                    pw.println("        <LANE ID=\"" + laneId + "\" LANETYPE=\"" + linkType + "\" DESIGNDIRECTION=\"true\">");
                    String width = cseNode.getAttributes().getNamedItem("WIDTH").getNodeValue();
                    String offset;
                    if (cseNode.getAttributes().getNamedItem("OFFSET") != null)
                        offset = cseNode.getAttributes().getNamedItem("OFFSET").getNodeValue();
                    else if (cseNode.getAttributes().getNamedItem("CENTEROFFSET") != null)
                        offset = cseNode.getAttributes().getNamedItem("CENTEROFFSET").getNodeValue();
                    else
                    {
                        offset = "";
                        System.err.println("offset for SHOULDER in ROADLAYOUT " + id + " unknown");
                    }
                    if (offset.length() > 0)
                        pw.println("          <CENTEROFFSET>" + offset + "</CENTEROFFSET>");
                    pw.println("          <WIDTH>" + width + "</WIDTH>");
                    pw.println("        </LANE>");
                }
                else if (cseNode.getNodeName().equals("NOTRAFFICLANE"))
                {
                    pw.println("        <NOTRAFFICLANE>");
                    String width = cseNode.getAttributes().getNamedItem("WIDTH").getNodeValue();
                    String offset;
                    if (cseNode.getAttributes().getNamedItem("OFFSET") != null)
                        offset = cseNode.getAttributes().getNamedItem("OFFSET").getNodeValue();
                    else if (cseNode.getAttributes().getNamedItem("CENTEROFFSET") != null)
                        offset = cseNode.getAttributes().getNamedItem("CENTEROFFSET").getNodeValue();
                    else
                    {
                        offset = "";
                        System.err.println("offset for SHOULDER in ROADLAYOUT " + id + " unknown");
                    }
                    if (offset.length() > 0)
                        pw.println("          <CENTEROFFSET>" + offset + "</CENTEROFFSET>");
                    pw.println("          <WIDTH>" + width + "</WIDTH>");
                    pw.println("        </NOTRAFFICLANE>");
                }
                else if (cseNode.getNodeName().equals("STRIPE"))
                {
                    String type = cseNode.getAttributes().getNamedItem("TYPE").getNodeValue();
                    pw.println("        <STRIPE TYPE=\"" + type + "\" />");
                }
            }
            pw.println("      </ROADLAYOUT>");
        }
        pw.println("    </ROADLAYOUTS>\n");
    }

    private void writeNodes(final PrintWriter pw, final List<Node> networkNodeList)
    {
        for (Node nodeTag : networkNodeList)
        {
            String id = nodeTag.getAttributes().getNamedItem("ID").getNodeValue();
            String coord = nodeTag.getAttributes().getNamedItem("COORDINATE").getNodeValue();
            String dir = "";
            if (nodeTag.getAttributes().getNamedItem("DIRECTION") != null)
                dir = nodeTag.getAttributes().getNamedItem("DIRECTION").getNodeValue();
            pw.print("      <NODE ID=\"" + id + "\" COORDINATE=\"" + coord + "\" ");
            if (dir.length() > 1)
                pw.println("DIRECTION=\"" + dir + "\" />");
            else
                pw.println(" />");
        }
        pw.println();
    }

    private void writeLinks(final PrintWriter pw, final List<Node> networkLinkList)
    {
        /*-
         OLD:
           <LINK ID="L2EB" NODESTART="N2EB" NODEEND="N3EB" ROADLAYOUT="HW3AFSLAG">
             <POLYLINE INTERMEDIATEPOINTS="(137792,395679) (137816,395665) (137902.6725,395617.2567)" />
           </LINK>
           
           NEW:
             <LINK ID="NS23" NODESTART="NS2" NODEEND="NS3" TYPE="STREET">
               <STRAIGHT />
               <DEFINEDLAYOUT>r3</DEFINEDLAYOUT>
             </LINK>
         */

        for (Node linkTag : networkLinkList)
        {
            String id = linkTag.getAttributes().getNamedItem("ID").getNodeValue();
            String snode = linkTag.getAttributes().getNamedItem("NODESTART").getNodeValue();
            String enode = linkTag.getAttributes().getNamedItem("NODEEND").getNodeValue();
            String type = linkTag.getAttributes().getNamedItem("ROADLAYOUT").getNodeValue();
            pw.println("    <LINK ID=\"" + id + "\" NODESTART=\"" + snode + "\" NODEEND=\"" + enode + "\" TYPE=\""
                    + this.roadTypeToLinkTypeMap.get(type) + "\">");
            NodeList layoutList = linkTag.getChildNodes();
            for (int i = 0; i < layoutList.getLength(); i++)
            {
                Node layoutNode = layoutList.item(i);
                if (layoutNode.getNodeName().equals("STRAIGHT"))
                {
                    pw.println("      <STRAIGHT />");
                }
                else if (layoutNode.getNodeName().equals("BEZIER"))
                {
                    pw.print("      <BEZIER ");
                    if (layoutNode.getAttributes().getNamedItem("SHAPE") != null)
                        pw.print("SHAPE=\"" + layoutNode.getAttributes().getNamedItem("SHAPE").getNodeValue() + "\" ");
                    pw.println("/>");
                }
                else if (layoutNode.getNodeName().equals("POLYLINE"))
                {
                    pw.println("      <POLYLINE>");
                    String[] coords = layoutNode.getAttributes().getNamedItem("INTERMEDIATEPOINTS").getNodeValue().split(" ");
                    for (String c : coords)
                    {
                        pw.println("        <COORDINATE>" + c + "</COORDINATE>");
                    }
                    pw.println("      </POLYLINE>");
                }
                else if (layoutNode.getNodeName().equals("ARC"))
                {
                    // <ARC DIRECTION="lr" RADIUS="xm"/>
                    String dir = layoutNode.getAttributes().getNamedItem("DIRECTION").getNodeValue();
                    String radius = layoutNode.getAttributes().getNamedItem("RADIUS").getNodeValue();
                    pw.println("      <ARC DIRECTION=\"" + dir + "\" RADIUS=\"" + radius + "\"/>");
                }
            }
            pw.println("      <DEFINEDLAYOUT>" + type + "</DEFINEDLAYOUT>");
            pw.println("    </LINK>\n");
        }
        pw.println();
    }

    private List<Node> nodesOfType(NodeList parent, String type)
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
