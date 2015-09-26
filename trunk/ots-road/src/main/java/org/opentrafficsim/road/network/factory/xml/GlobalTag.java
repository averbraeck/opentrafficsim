package org.opentrafficsim.road.network.factory.xml;

import java.util.List;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.factory.xml.units.LengthUnits;
import org.opentrafficsim.core.network.factory.xml.units.SpeedUnits;
import org.opentrafficsim.road.network.factory.XMLParser;
import org.opentrafficsim.road.network.factory.xml.units.LaneAttributes;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;
import org.opentrafficsim.road.network.lane.changing.OvertakingConditions;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
class GlobalTag 
{
    /** default speed. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Speed defaultMaxSpeed = null;

    /** default lane width. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length.Rel defaultLaneWidth = null;

    /** default VelocityGTUColorer.maxSpeed. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Speed velocityGTUColorerMaxSpeed = null;

    /** default lane keeping policy. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    LaneKeepingPolicy defaultLaneKeepingPolicy = null;

    /** default overtaking conditions. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    OvertakingConditions defaultOvertakingConditions = null;

    // TODO add other GTUColorer tags

    /**
     * @param nodeList nodeList the top-level nodes of the XML-file
     * @param parser the parser with the lists of information
     * @throws NetworkException when parsing of units fails
     * @throws SAXException when parsing of GLOBAL tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseGlobal(final NodeList nodeList, final XmlNetworkLaneParser parser) throws NetworkException,
        SAXException
    {
        List<Node> nodes = XMLParser.getNodes(nodeList, "GLOBAL");
        if (nodes.size() > 1)
            throw new SAXException("GLOBAL: More than one tag GLOBAL in the XML-file");
        if (nodes.size() == 1)
        {
            Node node = nodes.get(0);
            NamedNodeMap attributes = node.getAttributes();

            parser.globalTag = new GlobalTag();

            Node speed = attributes.getNamedItem("DEFAULTMAXSPEED");
            if (speed != null)
                parser.globalTag.defaultMaxSpeed = SpeedUnits.parseSpeedAbs(speed.getNodeValue().trim());

            Node width = attributes.getNamedItem("DEFAULTLANEWIDTH");
            if (width != null)
                parser.globalTag.defaultLaneWidth = LengthUnits.parseLengthRel(width.getNodeValue().trim());

            // VELOCITYGTUCOLORER attributes
            List<Node> velocityGTUColorerNodes = XMLParser.getNodes(node.getChildNodes(), "VELOCITYGTUCOLORER");
            if (velocityGTUColorerNodes.size() > 1)
                throw new SAXException("GLOBAL: More than one tag VELOCITYGTUCOLORER in the XML-file");
            if (velocityGTUColorerNodes.size() == 1)
            {
                Node velocityGTUColorerNode = nodes.get(0);
                NamedNodeMap velocityGTUColorerAttributes = velocityGTUColorerNode.getAttributes();
                if (velocityGTUColorerAttributes.getNamedItem("MAXSPEED") == null)
                    throw new SAXException("GLOBAL: No attribute MAXSPEED for the tag VELOCITYGTUCOLORER");
                parser.globalTag.velocityGTUColorerMaxSpeed =
                    SpeedUnits.parseSpeedAbs(velocityGTUColorerAttributes.getNamedItem("MAXSPEED").getNodeValue());
            }

            Node lkp = attributes.getNamedItem("DEFAULTLANEKEEPING");
            if (lkp != null)
                parser.globalTag.defaultLaneKeepingPolicy =
                    LaneAttributes.parseLaneKeepingPolicy(lkp.getNodeValue().trim());
            
            Node oc = attributes.getNamedItem("DEFAULTOVERTAKING");
            if (oc != null)
                parser.globalTag.defaultOvertakingConditions =
                    LaneAttributes.parseOvertakingConditions(oc.getNodeValue().trim(), parser);

            // TODO parse other GTUColorer tags
        }
    }
}
