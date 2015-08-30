package org.opentrafficsim.core.network.factory.xml;

import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel;
import org.opentrafficsim.core.gtu.following.IDM;
import org.opentrafficsim.core.gtu.following.IDMPlus;
import org.opentrafficsim.core.gtu.lane.changing.Altruistic;
import org.opentrafficsim.core.gtu.lane.changing.Egoistic;
import org.opentrafficsim.core.gtu.lane.changing.LaneChangeModel;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.factory.XMLParser;
import org.opentrafficsim.core.network.factory.xml.units.Distributions;
import org.opentrafficsim.core.units.distributions.DistContinuousDoubleScalar;
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
class GTUTag
{
    /** name. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String name = null;

    /** type. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    GTUType gtuType = null;

    /** GTU length. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    DistContinuousDoubleScalar.Rel<LengthUnit> lengthDist = null;

    /** GTU width. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    DistContinuousDoubleScalar.Rel<LengthUnit> widthDist = null;

    /** GTU following model. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    GTUFollowingModel followingModel = null;

    /** lane change model. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    LaneChangeModel laneChangeModel = null;

    /** max speed. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    DistContinuousDoubleScalar.Abs<SpeedUnit> maxSpeedDist = null;

    /**
     * @param nodeList nodeList the top-level nodes of the XML-file
     * @param parser the parser with the lists of information
     * @throws SAXException when parsing of GTU tag fails
     * @throws NetworkException when parsing of GTU tag fails
     * @throws GTUException if GTUType defined twice
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseGTUs(final NodeList nodeList, final XmlNetworkLaneParser parser) throws SAXException, NetworkException,
        GTUException
    {
        for (Node node : XMLParser.getNodes(nodeList, "GTU"))
        {
            NamedNodeMap attributes = node.getAttributes();
            GTUTag gtuTag = new GTUTag();

            Node name = attributes.getNamedItem("NAME");
            if (name == null)
                throw new SAXException("GTU: missing attribute NAME");
            gtuTag.name = name.getNodeValue().trim();
            if (parser.gtuTypes.keySet().contains(gtuTag.name))
                throw new SAXException("GTU: NAME " + gtuTag.name + " defined twice");

            Node gtuType = attributes.getNamedItem("GTUTYPE");
            if (gtuType == null)
                throw new SAXException("GTU: missing attribute GTUTYPE");
            gtuTag.gtuType = parseGTUType(gtuType.getNodeValue().trim(), parser);

            Node length = attributes.getNamedItem("LENGTH");
            if (length == null)
                throw new SAXException("GTU: missing attribute LENGTH");
            gtuTag.lengthDist = Distributions.parseLengthDistRel(length.getNodeValue());

            Node width = attributes.getNamedItem("WIDTH");
            if (width == null)
                throw new SAXException("GTU: missing attribute WIDTH");
            gtuTag.widthDist = Distributions.parseLengthDistRel(width.getNodeValue());

            Node following = attributes.getNamedItem("FOLLOWING");
            if (following == null)
                throw new SAXException("GTU: missing attribute FOLLOWING");
            gtuTag.followingModel = parseFollowingModel(following.getNodeValue());

            Node laneChange = attributes.getNamedItem("LANECHANGE");
            if (laneChange == null)
                throw new SAXException("GTU: missing attribute LANECHANGE");
            gtuTag.laneChangeModel = parseLaneChangeModel(laneChange.getNodeValue());

            Node maxSpeed = attributes.getNamedItem("MAXSPEED");
            if (maxSpeed == null)
                throw new SAXException("GTU: missing attribute LENGTH");
            gtuTag.maxSpeedDist = Distributions.parseSpeedDistAbs(maxSpeed.getNodeValue());

            parser.gtuTags.put(gtuTag.name, gtuTag);
        }
    }

    /**
     * @param typeName the name of the GTU type.
     * @param parser the parser with the lists of information
     * @return the GTUType that was retrieved or created.
     * @throws GTUException if GTUType defined twice
     */
    static GTUType parseGTUType(final String typeName, final XmlNetworkLaneParser parser) throws GTUException
    {
        if (!parser.gtuTypes.containsKey(typeName))
        {
            GTUType gtuType = GTUType.makeGTUType(typeName);
            parser.gtuTypes.put(typeName, gtuType);
        }
        return parser.gtuTypes.get(typeName);
    }

    /**
     * XXX probably ok to generate a new model for each GTU 'type'.
     * @param modelName the name of the GTU following model.
     * @return the model.
     * @throws NetworkException in case of unknown model.
     */
    static GTUFollowingModel parseFollowingModel(final String modelName) throws NetworkException
    {
        if (modelName.equals("IDM"))
        {
            return new IDM();
        }
        else if (modelName.equals("IDM+"))
        {
            return new IDMPlus();
        }
        throw new NetworkException("Unknown GTU following model: " + modelName);
    }

    /**
     * XXX probably ok to generate a new model for each GTU 'type'.
     * @param modelName the name of the lane change model.
     * @return the model.
     * @throws NetworkException in case of unknown model.
     */
    static LaneChangeModel parseLaneChangeModel(final String modelName) throws NetworkException
    {
        if (modelName.equals("EGOISTIC"))
        {
            return new Egoistic();
        }
        else if (modelName.equals("ALTRUISTIC"))
        {
            return new Altruistic();
        }
        throw new NetworkException("Unknown lane change model: " + modelName);
    }

}
