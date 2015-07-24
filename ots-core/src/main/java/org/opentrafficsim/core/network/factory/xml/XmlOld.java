package org.opentrafficsim.core.network.factory.xml;

import java.awt.Color;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;
import javax.vecmath.Point3d;

import nl.tudelft.simulation.dsol.SimRuntimeException;

import org.opentrafficsim.core.car.LaneBasedIndividualCar;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.generator.GTUGeneratorIndividual;
import org.opentrafficsim.core.gtu.lane.LaneBlock;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.core.network.animation.LaneAnimation;
import org.opentrafficsim.core.network.animation.ShoulderAnimation;
import org.opentrafficsim.core.network.animation.StripeAnimation;
import org.opentrafficsim.core.network.factory.xml.units.Distributions;
import org.opentrafficsim.core.network.factory.xml.units.LengthUnits;
import org.opentrafficsim.core.network.factory.xml.units.SpeedUnits;
import org.opentrafficsim.core.network.factory.xml.units.TimeUnits;
import org.opentrafficsim.core.network.lane.CrossSectionElement;
import org.opentrafficsim.core.network.lane.CrossSectionLink;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.network.lane.LaneType;
import org.opentrafficsim.core.network.lane.NoTrafficLane;
import org.opentrafficsim.core.network.lane.Shoulder;
import org.opentrafficsim.core.network.lane.SinkLane;
import org.opentrafficsim.core.network.lane.Stripe;
import org.opentrafficsim.core.network.lane.Stripe.Permeable;
import org.opentrafficsim.core.network.route.CompleteRoute;
import org.opentrafficsim.core.network.route.FixedLaneBasedRouteGenerator;
import org.opentrafficsim.core.network.route.LaneBasedRouteGenerator;
import org.opentrafficsim.core.unit.AnglePlaneUnit;
import org.opentrafficsim.core.unit.AngleSlopeUnit;
import org.opentrafficsim.core.unit.FrequencyUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.LinearDensityUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DistContinuousDoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.vividsolutions.jts.geom.Coordinate;

/**
 */
@SuppressWarnings("checkstyle:methodlength")
public class XmlOld
{
    // ByteArrayInputStream bais = new ByteArrayInputStream(s.getBytes());

    /**
     * The Handler for SAX Events.
     */
    class SAXHandler extends DefaultHandler
    {
        @Override
        @SuppressWarnings("checkstyle:methodlength")
        public void startElement(final String uri, final String localName, final String qName, final Attributes attributes)
            throws SAXException
        {
            try
            {
                    if (!qName.equals("NETWORK"))
                    {
                        switch (this.stack.getLast())
                        {
                            case "NETWORK":
                                switch (qName)
                                {
                                    case "NODE":
                                        parseNodeTag(attributes);
                                        break;

                                    case "LINK":
                                        parseLinkTag(attributes);
                                        break;

                                    case "ROUTE":
                                        parseRouteTag(attributes);
                                        break;

                                    case "ROUTEMIX":
                                        parseRouteMixTag(attributes);
                                        break;

                                    case "SHORTESTROUTE":
                                        parseShortestRouteTag(attributes);
                                        break;

                                    case "SHORTESTROUTEMIX":
                                        parseShortestRouteMixTag(attributes);
                                        break;

                                    default:
                                        throw new SAXException("NETWORK: Received start tag " + qName
                                            + ", but stack contains: " + this.stack);
                                }
                                break;

                            case "LINK":
                                switch (qName)
                                {
                                    case "STRAIGHT":
                                        parseStraightTag(attributes);
                                        break;

                                    case "ARC":
                                        parseArcTag(attributes);
                                        break;

                                    case "LANEOVERRIDE":
                                        parseLaneOverrideTag(attributes);
                                        break;

                                    case "ROADTYPE":
                                        parseRoadTypeTag(attributes);
                                        break;

                                    case "GENERATOR":
                                        parseGeneratorTag(attributes);
                                        break;

                                    case "SINK":
                                        parseSinkTag(attributes);
                                        break;

                                    case "LISTGENERATOR":
                                        parseListGeneratorTag(attributes);
                                        break;

                                    case "BLOCK":
                                        parseBlockTag(attributes);
                                        break;

                                    case "FILL":
                                        parseFillTag(attributes);
                                        break;

                                    default:
                                        throw new SAXException("LINK: Received start tag " + qName
                                            + ", but stack contains: " + this.stack);
                                }
                                break;

                            case "ROUTEMIX":
                                switch (qName)
                                {
                                    case "ROUTE":
                                        parseRouteMixRouteTag(attributes);
                                        break;

                                    default:
                                        throw new SAXException("ROUTEMIX: Received start tag " + qName
                                            + ", but stack contains: " + this.stack);
                                }
                                break;

                            case "SHORTESTROUTEMIX":
                                switch (qName)
                                {
                                    case "SHORTESTROUTE":
                                        parseShortestRouteMixShortestRouteTag(attributes);
                                        break;

                                    default:
                                        throw new SAXException("SHORTESTROUTEMIX: Received start tag " + qName
                                            + ", but stack contains: " + this.stack);
                                }
                                break;

                            default:
                                throw new SAXException("startElement: Received start tag " + qName
                                    + ", but stack contains: " + this.stack);
                        }
                    }
            }
            catch (Exception e)
            {
                throw new SAXException(e);
            }
        }

        @Override
        public void endElement(final String uri, final String localName, final String qName) throws SAXException
        {
            try
            {
                if (!qName.equals("NETWORK"))
                {
                    switch (this.stack.getLast())
                    {
                        case "NETWORK":
                            switch (qName)
                            {
                                case "NODE":
                                    parser.nodeTags.put(this.nodeTag.name, this.nodeTag);
                                    if (this.nodeTag.coordinate != null)
                                    {
                                        // only make a node if we know the coordinate. Otherwise, wait till we can
                                        // calculate it.
                                        makeNode(this.nodeTag);
                                    }
                                    this.nodeTag = null;
                                    break;

                                case "LINK":
                                    if (this.linkTag.roadTypeTag == null)
                                    {
                                        throw new SAXException("LINK: " + this.linkTag.name
                                            + " does not have a ROADTYPE defined");
                                    }
                                    calculateNodeCoordinates(this.linkTag);
                                    @SuppressWarnings("rawtypes")
                                    CrossSectionLink link = makeLink(this.linkTag);
                                    applyRoadTypeToLink(this.linkTag.roadTypeTag, link, this.linkTag, this.globalTag);
                                    parser.links.put(link.getId().toString(), link);
                                    this.linkTag = null;
                                    break;

                                case "ROADTYPE":
                                    calculateRoadTypeOffsets(this.roadTypeTag, this.globalTag);
                                    parser.roadTypeTags.put(this.roadTypeTag.name, this.roadTypeTag);
                                    break;

                                case "ROUTE":
                                    break;

                                case "ROUTEMIX":
                                    this.routeMixTag = null;
                                    break;

                                case "SHORTESTROUTE":
                                    break;

                                case "SHORTESTROUTEMIX":
                                    this.shortestRouteMixTag = null;
                                    break;

                                default:
                                    throw new SAXException("NETWORK: Received end tag " + qName + ", but stack contains: "
                                        + this.stack);
                            }
                            break;

                        case "LINK":
                            switch (qName)
                            {
                                case "STRAIGHT":
                                    break;
                                case "ARC":
                                    break;
                                case "ROADTYPE":
                                    this.linkTag.roadTypeTag = this.roadTypeTag;
                                    this.roadTypeTag = null;
                                    break;
                                case "LANEOVERRIDE":
                                    break;
                                case "GENERATOR":
                                    break;
                                case "SINK":
                                    break;
                                case "LISTGENERATOR":
                                    break;
                                case "BLOCK":
                                    break;
                                case "FILL":
                                    break;
                                default:
                                    throw new SAXException("LINK: Received end tag " + qName + ", but stack contains: "
                                        + this.stack);
                            }
                            break;

                        case "SHORTESTROUTEMIX":
                            switch (qName)
                            {
                                case "SHORTESTROUTE":
                                    break;
                                default:
                                    throw new SAXException("SHORTESTROUTEMIX: Received end tag " + qName
                                        + ", but stack contains: " + this.stack);
                            }
                            break;

                        default:
                            throw new SAXException("Received end tag " + qName + ", but stack contains: " + this.stack);
                    }
                }
            }
            catch (Exception e)
            {
                throw new SAXException(e);
            }
        }

        /**
         * Parse the NODE tag with attributes of a Node.
         * @param attributes the attributes of the XML-tag.
         * @throws NetworkException in case of OTS logic error.
         * @throws SAXException in case of parse error.
         */
        @SuppressWarnings("checkstyle:needbraces")
        private void parseNodeTag(final Attributes attributes) throws NetworkException, SAXException
        {
            nodeTag = new NodeTag();

            String name = attributes.getNamedItem("NAME");
            if (name == null)
                throw new SAXException("NODE: missing attribute NAME");
            nodeTag.name = name.trim();
            if (parser.nodes.keySet().contains(nodeTag.name))
                throw new SAXException("NODE: NAME " + nodeTag.name + " defined twice");

            if (attributes.getNamedItem("COORDINATE") != null)
                nodeTag.coordinate = parseCoordinate(attributes.getNamedItem("COORDINATE"));

            if (attributes.getNamedItem("ANGLE") != null)
                nodeTag.angle = parseAngleAbs(attributes.getNamedItem("ANGLE"));
        }

        /**
         * Parse the LINK tag with attributes of a Link.
         * @param attributes the attributes of the XML-tag.
         * @throws NetworkException in case of OTS logic error.
         * @throws SAXException in case of parse error.
         */
        @SuppressWarnings("checkstyle:needbraces")
        private void parseLinkTag(final Attributes attributes) throws NetworkException, SAXException
        {
            linkTag = new LinkTag();

            String name = attributes.getNamedItem("NAME");
            if (name == null)
                throw new SAXException("LINK: missing attribute NAME");
            linkTag.name = name.trim();
            if (parser.links.keySet().contains(linkTag.name))
                throw new SAXException("LINK: NAME " + linkTag.name + " defined twice");

            String roadTypeName = attributes.getNamedItem("ROADTYPE");
            if (!parser.roadTypeTags.containsKey(roadTypeName))
                throw new SAXException("LINK: ROADTYPE " + roadTypeName + " not found for link " + name);
            linkTag.roadTypeTag = parser.roadTypeTags.get(roadTypeName);

            String fromNodeStr = attributes.getNamedItem("FROM");
            if (fromNodeStr == null)
                throw new SAXException("NODE: missing attribute FROM for link " + name);
            linkTag.nodeFromName = fromNodeStr.trim();
            @SuppressWarnings("rawtypes")
            Node fromNode = parser.nodes.get(fromNodeStr.trim());
            linkTag.nodeFrom = fromNode;

            String toNodeStr = attributes.getNamedItem("TO");
            if (toNodeStr == null)
                throw new SAXException("NODE: missing attribute TO for link " + name);
            linkTag.nodeToName = toNodeStr.trim();
            @SuppressWarnings("rawtypes")
            Node toNode = parser.nodes.get(toNodeStr.trim());
            linkTag.nodeTo = toNode;
        }

        /**
         * Parse the STRAIGHT tag with straight attributes for a Link.
         * @param attributes the attributes of the XML-tag.
         * @throws NetworkException in case of OTS logic error.
         * @throws SAXException in case of parse error.
         */
        @SuppressWarnings("checkstyle:needbraces")
        private void parseStraightTag(final Attributes attributes) throws NetworkException, SAXException
        {
            linkTag.straightTag = new StraightTag();
            String length = attributes.getNamedItem("LENGTH");
            if (length == null)
                throw new SAXException("STRAIGHT: missing attribute LENGTH");
            linkTag.straightTag.length = LengthUnits.parseLengthRel(length);
        }

        /**
         * Parse the ARC tag with arc attributes for a Link.
         * @param attributes the attributes of the XML-tag.
         * @throws NetworkException in case of OTS logic error.
         * @throws SAXException in case of parse error.
         */
        @SuppressWarnings("checkstyle:needbraces")
        private void parseArcTag(final Attributes attributes) throws NetworkException, SAXException
        {
            linkTag.arcTag = new ArcTag();

            String radius = attributes.getNamedItem("RADIUS");
            if (radius == null)
                throw new SAXException("ARC: missing attribute RADIUS");
            linkTag.arcTag.radius = LengthUnits.parseLengthRel(radius);

            String angle = attributes.getNamedItem("ANGLE");
            if (angle == null)
                throw new SAXException("ARC: missing attribute ANGLE");
            linkTag.arcTag.angle = parseAngleAbs(angle);

            String dir = attributes.getNamedItem("DIRECTION");
            if (dir == null)
                throw new SAXException("ARC: missing attribute DIRECTION");
            linkTag.arcTag.direction =
                (dir.equals("L") || dir.equals("LEFT") || dir.equals("COUNTERCLOCKWISE")) ? ArcDirection.LEFT
                    : ArcDirection.RIGHT;
        }

        /**
         * Parse the LANEOVERRIDE tag with lane attributes for a Link.
         * @param attributes the attributes of the XML-tag.
         * @throws NetworkException in case of OTS logic error.
         * @throws SAXException in case of parse error.
         */
        @SuppressWarnings("checkstyle:needbraces")
        private void parseLaneOverrideTag(final Attributes attributes) throws NetworkException, SAXException
        {
            String name = attributes.getNamedItem("NAME");
            if (name == null)
                throw new SAXException("LANEOVERRIDE: missing attribute NAME" + " for link " + linkTag.name);
            if (linkTag.roadTypeTag == null)
                throw new NetworkException("LANEOVERRIDE: NAME " + name.trim() + " no ROADTYPE for link " + linkTag.name);
            CrossSectionElementTag laneTag = linkTag.roadTypeTag.cseTags.get(name.trim());
            if (laneTag == null)
                throw new NetworkException("LANEOVERRIDE: Lane with NAME " + name.trim() + " not found in elements of link "
                    + linkTag.name + " - roadtype " + linkTag.roadTypeTag.name);
            if (linkTag.laneOverrideTags.containsKey(name))
                throw new SAXException("LANEOVERRIDE: LANE OVERRIDE with NAME " + name + " defined twice");

            LaneOverrideTag laneOverrideTag = new LaneOverrideTag();

            if (attributes.getNamedItem("SPEED") != null)
                laneOverrideTag.speed = SpeedUnits.parseSpeedAbs(attributes.getNamedItem("SPEED"));

            if (attributes.getNamedItem("DIRECTION") != null)
                laneOverrideTag.direction = parseDirection(attributes.getNamedItem("DIRECTION"));

            if (attributes.getNamedItem("COLOR") != null)
                laneOverrideTag.color = parseColor(attributes.getNamedItem("COLOR"));

            linkTag.laneOverrideTags.put(name.trim(), laneOverrideTag);
        }

        /**
         * Parse the GENERATOR tag with GTU generation attributes for a Lane. Note: Only one generator can be defined for the
         * same lane.
         * @param attributes the attributes of the XML-tag.
         * @throws NetworkException in case of OTS logic error.
         * @throws SAXException in case of parse error.
         */
        @SuppressWarnings("checkstyle:needbraces")
        private void parseGeneratorTag(final Attributes attributes) throws NetworkException, SAXException
        {
            GeneratorTag generatorTag = new GeneratorTag();

            String laneName = attributes.getNamedItem("LANE");
            if (laneName == null)
                throw new SAXException("GENERATOR: missing attribute LANE" + " for link " + linkTag.name);
            if (linkTag.roadTypeTag == null)
                throw new NetworkException("GENERATOR: LANE " + laneName.trim() + " no ROADTYPE for link " + linkTag.name);
            CrossSectionElementTag cseTag = linkTag.roadTypeTag.cseTags.get(laneName.trim());
            if (cseTag == null)
                throw new NetworkException("GENERATOR: LANE " + laneName.trim() + " not found in elements of link "
                    + linkTag.name + " - roadtype " + linkTag.roadTypeTag.name);
            if (cseTag.elementType != ElementType.LANE)
                throw new NetworkException("GENERATOR: LANE " + laneName.trim() + " not a real GTU lane for link "
                    + linkTag.name + " - roadtype " + linkTag.roadTypeTag.name);
            if (linkTag.generatorTags.containsKey(laneName))
                throw new SAXException("GENERATOR for LANE with NAME " + laneName + " defined twice");

            String posStr = attributes.getNamedItem("POSITION");
            generatorTag.position = parseBeginEndPosition(posStr == null ? "END" : posStr, linkTag);

            String gtuName = attributes.getNamedItem("GTU");
            if (gtuName != null)
            {
                if (!parser.gtuTags.containsKey(gtuName.trim()))
                    throw new NetworkException("GENERATOR: LANE " + laneName + " GTU " + gtuName.trim() + " in link "
                        + linkTag.name + " not defined");
                generatorTag.gtuTag = parser.gtuTags.get(gtuName.trim());
            }

            String gtuMixName = attributes.getNamedItem("GTUMIX");
            if (gtuMixName != null)
            {
                if (!parser.gtuMixTags.containsKey(gtuMixName.trim()))
                    throw new NetworkException("GENERATOR: LANE " + laneName + " GTUMIX " + gtuMixName.trim() + " in link "
                        + linkTag.name + " not defined");
                generatorTag.gtuMixTag = parser.gtuMixTags.get(gtuMixName.trim());
            }

            if (generatorTag.gtuTag == null && generatorTag.gtuMixTag == null)
                throw new SAXException("GENERATOR: missing attribute GTU or GTUMIX for Lane with NAME " + laneName
                    + " of link " + linkTag.name);

            if (generatorTag.gtuTag != null && generatorTag.gtuMixTag != null)
                throw new SAXException("GENERATOR: both attribute GTU and GTUMIX defined for Lane with NAME " + laneName
                    + " of link " + linkTag.name);

            String iat = attributes.getNamedItem("IAT");
            if (iat == null)
                throw new SAXException("GENERATOR: missing attribute IAT");
            generatorTag.iatDist = TimeUnits.parseTimeDistRel(iat);

            String initialSpeed = attributes.getNamedItem("INITIALSPEED");
            if (initialSpeed == null)
                throw new SAXException("GENERATOR: missing attribute INITIALSPEED");
            generatorTag.initialSpeedDist = Distributions.parseSpeedDistAbs(initialSpeed);

            String maxGTU = attributes.getNamedItem("MAXGTU");
            generatorTag.maxGTUs = maxGTU == null ? Integer.MAX_VALUE : Integer.parseInt(maxGTU);

            if (attributes.getNamedItem("STARTTIME") != null)
                generatorTag.startTime = TimeUnits.parseTimeAbs(attributes.getNamedItem("STARTTIME"));

            if (attributes.getNamedItem("ENDTIME") != null)
                generatorTag.endTime = TimeUnits.parseTimeAbs(attributes.getNamedItem("ENDTIME"));

            int numberRouteTags = 0;

            String routeName = attributes.getNamedItem("ROUTE");
            if (routeName != null)
            {
                if (!parser.routeTags.containsKey(routeName.trim()))
                    throw new NetworkException("GENERATOR: LANE " + laneName + " ROUTE " + routeName.trim() + " in link "
                        + linkTag.name + " not defined");
                generatorTag.routeTag = parser.routeTags.get(routeName.trim());
                numberRouteTags++;
            }

            String routeMixName = attributes.getNamedItem("ROUTEMIX");
            if (routeMixName != null)
            {
                if (!parser.routeMixTags.containsKey(routeMixName.trim()))
                    throw new NetworkException("GENERATOR: LANE " + laneName + " ROUTEMIX " + routeMixName.trim()
                        + " in link " + linkTag.name + " not defined");
                generatorTag.routeMixTag = parser.routeMixTags.get(routeMixName.trim());
                numberRouteTags++;
            }

            String shortestRouteName = attributes.getNamedItem("SHORTESTROUTE");
            if (shortestRouteName != null)
            {
                if (!parser.shortestRouteTags.containsKey(shortestRouteName.trim()))
                    throw new NetworkException("GENERATOR: LANE " + laneName + " SHORTESTROUTE " + shortestRouteName.trim()
                        + " in link " + linkTag.name + " not defined");
                generatorTag.shortestRouteTag = parser.shortestRouteTags.get(shortestRouteName.trim());
                numberRouteTags++;
            }

            String shortestRouteMixName = attributes.getNamedItem("SHORTESTROUTEMIX");
            if (shortestRouteMixName != null)
            {
                if (!parser.shortestRouteMixTags.containsKey(shortestRouteMixName.trim()))
                    throw new NetworkException("GENERATOR: LANE " + laneName + " SHORTESTROUTEMIX "
                        + shortestRouteMixName.trim() + " in link " + linkTag.name + " not defined");
                generatorTag.shortestRouteMixTag = parser.shortestRouteMixTags.get(shortestRouteMixName.trim());
                numberRouteTags++;
            }

            if (numberRouteTags > 1)
                throw new SAXException("GENERATOR: multiple ROUTE tags defined for Lane with NAME " + laneName + " of link "
                    + linkTag.name);

            // TODO GTUColorer

            linkTag.generatorTags.put(laneName, generatorTag);
        }

        /**
         * Parse the SINK tag for a Lane. Note: Only one sink can be defined for the same lane.
         * @param attributes the attributes of the XML-tag.
         * @throws NetworkException in case of OTS logic error.
         * @throws SAXException in case of parse error.
         */
        @SuppressWarnings("checkstyle:needbraces")
        private void parseSinkTag(final Attributes attributes) throws NetworkException, SAXException
        {
            String laneName = attributes.getNamedItem("LANE");
            if (laneName == null)
                throw new SAXException("SINK: missing attribute LANE" + " for link " + linkTag.name);
            if (linkTag.roadTypeTag == null)
                throw new NetworkException("SINK: LANE " + laneName.trim() + " no ROADTYPE for link " + linkTag.name);
            CrossSectionElementTag cseTag = linkTag.roadTypeTag.cseTags.get(laneName.trim());
            if (cseTag == null)
                throw new NetworkException("SINK: LANE " + laneName.trim() + " not found in elements of link "
                    + linkTag.name + " - roadtype " + linkTag.roadTypeTag.name);
            if (cseTag.elementType != ElementType.LANE)
                throw new NetworkException("SINK: LANE " + laneName.trim() + " not a real GTU lane for link " + linkTag.name
                    + " - roadtype " + linkTag.roadTypeTag.name);
            if (linkTag.sinkLanes.contains(laneName))
                throw new SAXException("SINK for LANE with NAME " + laneName + " defined twice");

            linkTag.sinkLanes.add(laneName);
        }

        /**
         * Parse the LISTGENERATOR tag with GTU generation attributes for a Lane. Note: Only one generator can be defined for
         * the same lane.
         * @param attributes the attributes of the XML-tag.
         * @throws NetworkException in case of OTS logic error.
         * @throws SAXException in case of parse error.
         */
        @SuppressWarnings("checkstyle:needbraces")
        private void parseListGeneratorTag(final Attributes attributes) throws NetworkException, SAXException
        {
            ListGeneratorTag listGeneratorTag = new ListGeneratorTag();

            String uriStr = attributes.getNamedItem("URI");
            try
            {
                listGeneratorTag.uri = new URI(uriStr);
            }
            catch (URISyntaxException exception)
            {
                throw new NetworkException("LISTGENERATOR: URI " + uriStr + " is not valid", exception);
            }

            String laneName = attributes.getNamedItem("LANE");
            if (laneName == null)
                throw new SAXException("LISTGENERATOR: missing attribute LANE" + " for link " + linkTag.name);
            if (linkTag.roadTypeTag == null)
                throw new NetworkException("LISTGENERATOR: LANE " + laneName.trim() + " no ROADTYPE for link "
                    + linkTag.name);
            CrossSectionElementTag cseTag = linkTag.roadTypeTag.cseTags.get(laneName.trim());
            if (cseTag == null)
                throw new NetworkException("LISTGENERATOR: LANE " + laneName.trim() + " not found in elements of link "
                    + linkTag.name + " - roadtype " + linkTag.roadTypeTag.name);
            if (cseTag.elementType != ElementType.LANE)
                throw new NetworkException("LISTGENERATOR: LANE " + laneName.trim() + " not a real GTU lane for link "
                    + linkTag.name + " - roadtype " + linkTag.roadTypeTag.name);
            if (linkTag.generatorTags.containsKey(laneName))
                throw new SAXException("LISTGENERATOR for LANE with NAME " + laneName + " defined twice");

            String posStr = attributes.getNamedItem("POSITION");
            listGeneratorTag.position = parseBeginEndPosition(posStr == null ? "END" : posStr, linkTag);

            String gtuName = attributes.getNamedItem("GTU");
            if (gtuName != null)
            {
                if (!parser.gtuTags.containsKey(gtuName.trim()))
                    throw new NetworkException("LISTGENERATOR: LANE " + laneName + " GTU " + gtuName.trim() + " in link "
                        + linkTag.name + " not defined");
                listGeneratorTag.gtuTag = parser.gtuTags.get(gtuName.trim());
            }

            String gtuMixName = attributes.getNamedItem("GTUMIX");
            if (gtuMixName != null)
            {
                if (!parser.gtuMixTags.containsKey(gtuMixName.trim()))
                    throw new NetworkException("LISTGENERATOR: LANE " + laneName + " GTUMIX " + gtuMixName.trim()
                        + " in link " + linkTag.name + " not defined");
                listGeneratorTag.gtuMixTag = parser.gtuMixTags.get(gtuMixName.trim());
            }

            if (listGeneratorTag.gtuTag == null && listGeneratorTag.gtuMixTag == null)
                throw new SAXException("LISTGENERATOR: missing attribute GTU or GTUMIX for Lane with NAME " + laneName
                    + " of link " + linkTag.name);

            if (listGeneratorTag.gtuTag != null && listGeneratorTag.gtuMixTag != null)
                throw new SAXException("LISTGENERATOR: both attribute GTU and GTUMIX defined for Lane with NAME " + laneName
                    + " of link " + linkTag.name);

            String initialSpeed = attributes.getNamedItem("INITIALSPEED");
            if (initialSpeed == null)
                throw new SAXException("LISTGENERATOR: missing attribute INITIALSPEED");
            listGeneratorTag.initialSpeedDist = Distributions.parseSpeedDistAbs(initialSpeed);

            // TODO GTUColorer

            // TODO linkTag.listGeneratorTags.put(laneName, listGeneratorTag);
        }

        /**
         * Parse the FILL tag with GTU fill attributes for a Lane. Note: Only one FILL can be defined for the same lane.
         * @param attributes the attributes of the XML-tag.
         * @throws NetworkException in case of OTS logic error.
         * @throws SAXException in case of parse error.
         */
        @SuppressWarnings("checkstyle:needbraces")
        private void parseFillTag(final Attributes attributes) throws NetworkException, SAXException
        {
            FillTag fillTag = new FillTag();

            String laneName = attributes.getNamedItem("LANE");
            if (laneName == null)
                throw new SAXException("FILL: missing attribute LANE" + " for link " + linkTag.name);
            if (linkTag.roadTypeTag == null)
                throw new NetworkException("FILL: NAME " + laneName.trim() + " no ROADTYPE for link " + linkTag.name);
            CrossSectionElementTag cseTag = linkTag.roadTypeTag.cseTags.get(laneName.trim());
            if (cseTag == null)
                throw new NetworkException("FILL: Lane with NAME " + laneName.trim() + " not found in elements of link "
                    + linkTag.name + " - roadtype " + linkTag.roadTypeTag.name);
            if (cseTag.elementType != ElementType.LANE)
                throw new NetworkException("FILL: Lane with NAME " + laneName.trim() + " not a real GTU lane for link "
                    + linkTag.name + " - roadtype " + linkTag.roadTypeTag.name);
            if (linkTag.fillTags.containsKey(laneName))
                throw new SAXException("FILL for LANE with NAME " + laneName + " defined twice");

            String gtuName = attributes.getNamedItem("GTU");
            if (gtuName != null)
            {
                if (!parser.gtuTags.containsKey(gtuName.trim()))
                    throw new NetworkException("FILL: LANE " + laneName + " GTU " + gtuName.trim() + " in link "
                        + linkTag.name + " not defined");
                fillTag.gtuTag = parser.gtuTags.get(gtuName.trim());
            }

            String gtuMixName = attributes.getNamedItem("GTUMIX");
            if (gtuMixName != null)
            {
                if (!parser.gtuMixTags.containsKey(gtuMixName.trim()))
                    throw new NetworkException("FILL: LANE " + laneName + " GTUMIX " + gtuMixName.trim() + " in link "
                        + linkTag.name + " not defined");
                fillTag.gtuMixTag = parser.gtuMixTags.get(gtuMixName.trim());
            }

            if (fillTag.gtuTag == null && fillTag.gtuMixTag == null)
                throw new SAXException("FILL: missing attribute GTU or GTUMIX for Lane with NAME " + laneName.trim()
                    + " of link " + linkTag.name);

            if (fillTag.gtuTag != null && fillTag.gtuMixTag != null)
                throw new SAXException("FILL: both attribute GTU and GTUMIX defined for Lane with NAME " + laneName
                    + " of link " + linkTag.name);

            String distance = attributes.getNamedItem("DISTANCE");
            if (distance == null)
                throw new SAXException("FILL: missing attribute DISTANCE");
            fillTag.distanceDist = Distributions.parseLengthDistRel(distance);

            String initialSpeed = attributes.getNamedItem("INITIALSPEED");
            if (initialSpeed == null)
                throw new SAXException("FILL: missing attribute INITIALSPEED");
            fillTag.initialSpeedDist = Distributions.parseSpeedDistAbs(initialSpeed);

            String maxGTU = attributes.getNamedItem("MAXGTU");
            fillTag.maxGTUs = maxGTU == null ? Integer.MAX_VALUE : Integer.parseInt(maxGTU);

            int numberRouteTags = 0;

            String routeName = attributes.getNamedItem("ROUTE");
            if (routeName != null)
            {
                if (!parser.routeTags.containsKey(routeName.trim()))
                    throw new NetworkException("FILL: LANE " + laneName + " ROUTE " + routeName.trim() + " in link "
                        + linkTag.name + " not defined");
                fillTag.routeTag = parser.routeTags.get(routeName.trim());
                numberRouteTags++;
            }

            String routeMixName = attributes.getNamedItem("ROUTEMIX");
            if (routeMixName != null)
            {
                if (!parser.routeMixTags.containsKey(routeMixName.trim()))
                    throw new NetworkException("FILL: LANE " + laneName + " ROUTEMIX " + routeMixName.trim() + " in link "
                        + linkTag.name + " not defined");
                fillTag.routeMixTag = parser.routeMixTags.get(routeMixName.trim());
                numberRouteTags++;
            }

            String shortestRouteName = attributes.getNamedItem("SHORTESTROUTE");
            if (shortestRouteName != null)
            {
                if (!parser.shortestRouteTags.containsKey(shortestRouteName.trim()))
                    throw new NetworkException("FILL: LANE " + laneName + " SHORTESTROUTE " + shortestRouteName.trim()
                        + " in link " + linkTag.name + " not defined");
                fillTag.shortestRouteTag = parser.shortestRouteTags.get(shortestRouteName.trim());
                numberRouteTags++;
            }

            String shortestRouteMixName = attributes.getNamedItem("SHORTESTROUTEMIX");
            if (shortestRouteMixName != null)
            {
                if (!parser.shortestRouteMixTags.containsKey(shortestRouteMixName.trim()))
                    throw new NetworkException("FILL: LANE " + laneName + " SHORTESTROUTEMIX " + shortestRouteMixName.trim()
                        + " in link " + linkTag.name + " not defined");
                fillTag.shortestRouteMixTag = parser.shortestRouteMixTags.get(shortestRouteMixName.trim());
                numberRouteTags++;
            }

            if (numberRouteTags > 1)
                throw new SAXException("FILL: multiple ROUTE tags defined for Lane with NAME " + laneName + " of link "
                    + linkTag.name);

            linkTag.fillTags.put(laneName, fillTag);
        }

        /**
         * Parse the BLOCK tag with a Lane end or blockage.
         * @param attributes the attributes of the XML-tag.
         * @throws NetworkException in case of OTS logic error.
         * @throws SAXException in case of parse error.
         */
        @SuppressWarnings("checkstyle:needbraces")
        private void parseBlockTag(final Attributes attributes) throws NetworkException, SAXException
        {
            BlockTag blockTag = new BlockTag();

            String laneName = attributes.getNamedItem("LANE");
            if (laneName == null)
                throw new SAXException("BLOCK: missing attribute LANE" + " for link " + linkTag.name);
            CrossSectionElementTag cseTag = linkTag.roadTypeTag.cseTags.get(laneName.trim());
            if (cseTag == null)
                throw new NetworkException("BLOCK: Lane with NAME " + laneName.trim() + " not found in elements of link "
                    + linkTag.name + " - roadtype " + linkTag.roadTypeTag.name);
            if (cseTag.elementType != ElementType.LANE)
                throw new NetworkException("BLOCK: Lane with NAME " + laneName.trim() + " not a real GTU lane for link "
                    + linkTag.name + " - roadtype " + linkTag.roadTypeTag.name);
            if (linkTag.blockTags.containsKey(laneName))
                throw new SAXException("BLOCK for LANE with NAME " + laneName + " defined twice");

            String posStr = attributes.getNamedItem("POSITION");
            if (posStr == null)
                throw new SAXException("BLOCK: missing attribute POSITION for link " + linkTag.name);
            blockTag.position = parseBeginEndPosition(posStr, linkTag);

            linkTag.blockTags.put(laneName, blockTag);
        }

        /**
         * Parse the ROUTE tag.
         * @param attributes the attributes of the XML-tag.
         * @throws NetworkException in case of OTS logic error.
         * @throws SAXException in case of parse error.
         */
        @SuppressWarnings("checkstyle:needbraces")
        private void parseRouteTag(final Attributes attributes) throws NetworkException, SAXException
        {
            RouteTag routeTag = new RouteTag();

            String name = attributes.getNamedItem("NAME");
            if (name == null)
                throw new SAXException("ROUTE: missing attribute NAME");
            routeTag.name = name.trim();
            if (parser.routeTags.keySet().contains(routeTag.name))
                throw new SAXException("ROUTE: NAME " + routeTag.name + " defined twice");

            String routeNodes = attributes.getNamedItem("NODELIST");
            if (routeNodes == null)
                throw new SAXException("ROUTE " + name.trim() + ": missing attribute NODELIST");
            routeTag.routeNodeTags = parseNodeList(routeNodes);

            parser.routeTags.put(name.trim(), routeTag);
        }

        /**
         * Parse the ROUTEMIX tag.
         * @param attributes the attributes of the XML-tag.
         * @throws NetworkException in case of OTS logic error.
         * @throws SAXException in case of parse error.
         */
        @SuppressWarnings("checkstyle:needbraces")
        private void parseRouteMixTag(final Attributes attributes) throws NetworkException, SAXException
        {
            routeMixTag = new RouteMixTag();

            String name = attributes.getNamedItem("NAME");
            if (name == null)
                throw new SAXException("ROUTEMIX: missing attribute NAME");
            routeMixTag.name = name.trim();
            if (parser.routeMixTags.keySet().contains(routeMixTag.name))
                throw new SAXException("ROUTEMIX: NAME " + routeMixTag.name + " defined twice");

            parser.routeMixTags.put(name.trim(), routeMixTag);
        }

        /**
         * Parse the ROUTEMIX's ROUTE tag.
         * @param attributes the attributes of the XML-tag.
         * @throws NetworkException in case of OTS logic error.
         * @throws SAXException in case of parse error.
         */
        @SuppressWarnings("checkstyle:needbraces")
        private void parseRouteMixRouteTag(final Attributes attributes) throws NetworkException, SAXException
        {
            if (routeMixTag == null)
                throw new NetworkException("ROUTEMIX: parse error");

            String routeName = attributes.getNamedItem("NAME");
            if (routeName == null)
                throw new NetworkException("ROUTEMIX: No ROUTE NAME defined");
            if (!parser.routeTags.containsKey(routeName.trim()))
                throw new NetworkException("ROUTEMIX: " + routeMixTag.name + " ROUTE " + routeName.trim() + " not defined");
            routeMixTag.routes.add(parser.routeTags.get(routeName.trim()));

            String weight = attributes.getNamedItem("WEIGHT");
            if (weight == null)
                throw new NetworkException("ROUTEMIX: " + routeMixTag.name + " ROUTE " + routeName.trim()
                    + ": weight not defined");
            routeMixTag.weights.add(Double.parseDouble(weight));
        }

        /**
         * Parse the SHORTESTROUTE tag.
         * @param attributes the attributes of the XML-tag.
         * @throws NetworkException in case of OTS logic error.
         * @throws SAXException in case of parse error.
         */
        @SuppressWarnings("checkstyle:needbraces")
        private void parseShortestRouteTag(final Attributes attributes) throws NetworkException, SAXException
        {
            ShortestRouteTag shortestRouteTag = new ShortestRouteTag();

            String name = attributes.getNamedItem("NAME");
            if (name == null)
                throw new SAXException("SHORTESTROUTE: missing attribute NAME");
            shortestRouteTag.name = name.trim();
            if (parser.shortestRouteTags.keySet().contains(shortestRouteTag.name))
                throw new SAXException("SHORTESTROUTE: NAME " + shortestRouteTag.name + " defined twice");

            String fromNode = attributes.getNamedItem("FROM");
            if (fromNode == null)
                throw new SAXException("SHORTESTROUTE: missing attribute FROM");
            if (!parser.nodeTags.containsKey(fromNode.trim()))
                throw new SAXException("SHORTESTROUTE " + name + ": FROM node " + fromNode.trim() + " not found");
            shortestRouteTag.from = parser.nodeTags.get(fromNode.trim());

            String viaNodes = attributes.getNamedItem("NODELIST");
            if (viaNodes != null)
                shortestRouteTag.via = parseNodeList(viaNodes);

            String toNode = attributes.getNamedItem("TO");
            if (toNode == null)
                throw new SAXException("SHORTESTROUTE: missing attribute TO");
            if (!parser.nodeTags.containsKey(toNode.trim()))
                throw new SAXException("SHORTESTROUTE " + name + ": TO node " + toNode.trim() + " not found");
            shortestRouteTag.to = parser.nodeTags.get(toNode.trim());

            String distanceCost = attributes.getNamedItem("DISTANCECOST");
            if (distanceCost == null)
                throw new SAXException("SHORTESTROUTE: missing attribute DISTANCECOST");
            shortestRouteTag.costPerDistance = parsePerLengthAbs(distanceCost);

            String timeCost = attributes.getNamedItem("TIMECOST");
            if (timeCost == null)
                throw new SAXException("SHORTESTROUTE: missing attribute TIMECOST");
            shortestRouteTag.costPerTime = parsePerTimeAbs(timeCost);

            parser.shortestRouteTags.put(name.trim(), shortestRouteTag);
        }

        /**
         * Parse the SHORTESTROUTEMIX tag.
         * @param attributes the attributes of the XML-tag.
         * @throws NetworkException in case of OTS logic error.
         * @throws SAXException in case of parse error.
         */
        @SuppressWarnings("checkstyle:needbraces")
        private void parseShortestRouteMixTag(final Attributes attributes) throws NetworkException, SAXException
        {
            shortestRouteMixTag = new ShortestRouteMixTag();

            String name = attributes.getNamedItem("NAME");
            if (name == null)
                throw new SAXException("SHORTESTROUTEMIX: missing attribute NAME");
            shortestRouteMixTag.name = name.trim();
            if (parser.shortestRouteMixTags.keySet().contains(shortestRouteMixTag.name))
                throw new SAXException("SHORTESTROUTEMIX: NAME " + shortestRouteMixTag.name + " defined twice");

            parser.shortestRouteMixTags.put(name.trim(), shortestRouteMixTag);
        }

        /**
         * Parse the SHORTESTROUTEMIX's SHORTESTROUTE tag.
         * @param attributes the attributes of the XML-tag.
         * @throws NetworkException in case of OTS logic error.
         * @throws SAXException in case of parse error.
         */
        @SuppressWarnings("checkstyle:needbraces")
        private void parseShortestRouteMixShortestRouteTag(final Attributes attributes) throws NetworkException,
            SAXException
        {
            if (shortestRouteMixTag == null)
                throw new NetworkException("SHORTESTROUTEMIX: parse error");

            String shortestRouteName = attributes.getNamedItem("NAME");
            if (shortestRouteName == null)
                throw new NetworkException("SHORTESTROUTEMIX: No SHORTESTROUTE NAME defined");
            if (!parser.shortestRouteTags.containsKey(shortestRouteName.trim()))
                throw new NetworkException("SHORTESTROUTEMIX: " + shortestRouteMixTag.name + " SHORTESTROUTE "
                    + shortestRouteName.trim() + " not defined");
            shortestRouteMixTag.shortestRoutes.add(parser.shortestRouteTags.get(shortestRouteName.trim()));

            String weight = attributes.getNamedItem("WEIGHT");
            if (weight == null)
                throw new NetworkException("SHORTESTROUTEMIX: " + shortestRouteMixTag.name + " SHORTESTROUTE "
                    + shortestRouteName.trim() + ": weight not defined");
            shortestRouteMixTag.weights.add(Double.parseDouble(weight));
        }

    }

    /*************************************************************************************************/
    /****************************************** PARSING CLASSES **************************************/
    /*************************************************************************************************/

    /**
     * Generate an ID of the right type.
     * @param clazz the class to instantiate.
     * @param ids the id as a String.
     * @return the object as an instance of the right class.
     * @throws NetworkException when id cannot be instantiated
     */
    protected final Object makeId(final Class<?> clazz, final String ids) throws NetworkException
    {
        Object id = null;
        try
        {
            if (String.class.isAssignableFrom(clazz))
            {
                id = new String(ids);
            }
            else if (int.class.isAssignableFrom(clazz))
            {
                id = Integer.valueOf(ids);
            }
            else if (long.class.isAssignableFrom(clazz))
            {
                id = Long.valueOf(ids);
            }
            else
            {
                throw new NetworkException("Parsing network. ID class " + clazz.getName() + ": cannot instantiate.");
            }
        }
        catch (NumberFormatException nfe)
        {
            throw new NetworkException("Parsing network. ID class " + clazz.getName() + ": cannot instantiate number: "
                + ids, nfe);
        }
        return id;
    }

    /**
     * @param nodeTag the tag with the info for the node.
     * @return a constructed node
     * @throws NetworkException when point cannot be instantiated
     * @throws NamingException when animation context cannot be found.
     * @throws RemoteException when communication error occurs when trying to find animation context.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected final Node makeNode(final NodeTag nodeTag) throws NetworkException, RemoteException, NamingException
    {
        Object id = makeId(this.nodeIdClass, nodeTag.name);
        DoubleScalar.Abs<AnglePlaneUnit> angle =
            nodeTag.angle == null ? new DoubleScalar.Abs<AnglePlaneUnit>(0.0, AnglePlaneUnit.SI) : nodeTag.angle;
        DoubleScalar.Abs<AngleSlopeUnit> slope =
            nodeTag.slope == null ? new DoubleScalar.Abs<AngleSlopeUnit>(0.0, AngleSlopeUnit.SI) : nodeTag.slope;
        Node node = new OTSNode(id, new OTSPoint3D(nodeTag.coordinate), angle, slope);
        this.nodes.put(node.getId().toString(), node);
        return node;
    }

    /**
     * One of the nodes probably has a coordinate and the other not. Calculate the other coordinate and save the Node.
     * @param linkTag the parsed information from the XML file.
     * @throws NetworkException when both nodes are null.
     * @throws RemoteException when coordinate cannot be reached.
     * @throws NamingException when node animation cannot link to the animation context.
     */
    @SuppressWarnings("methodlength")
    protected final void calculateNodeCoordinates(final LinkTag linkTag) throws RemoteException, NetworkException,
        NamingException
    {
        // calculate dx, dy and dz for the straight or the arc.
        if (linkTag.nodeFrom != null && linkTag.nodeTo != null)
        {
            if (linkTag.arcTag != null)
            {
                double radiusSI = linkTag.arcTag.radius.getSI();
                ArcDirection direction = linkTag.arcTag.direction;
                Point3d coordinate =
                    new Point3d(linkTag.nodeFrom.getLocation().getX(), linkTag.nodeFrom.getLocation().getY(),
                        linkTag.nodeFrom.getLocation().getZ());
                double startAngle = linkTag.nodeFrom.getDirection().getSI();
                if (direction.equals(ArcDirection.LEFT))
                {
                    linkTag.arcTag.center =
                        new Point3d(coordinate.x + radiusSI * Math.cos(startAngle + Math.PI / 2.0), coordinate.y + radiusSI
                            * Math.sin(startAngle + Math.PI / 2.0), 0.0);
                    linkTag.arcTag.startAngle = startAngle - Math.PI / 2.0;
                }
                else
                {
                    linkTag.arcTag.center =
                        new Point3d(coordinate.x + radiusSI * Math.cos(startAngle - Math.PI / 2.0), coordinate.y + radiusSI
                            * Math.sin(startAngle - Math.PI / 2.0), 0.0);
                    linkTag.arcTag.startAngle = startAngle + Math.PI / 2.0;
                }
            }
            return;
        }

        if (linkTag.nodeFrom == null && linkTag.nodeTo == null)
        {
            throw new NetworkException("Parsing network. Link: " + linkTag.name + ", both From-node and To-node are null");
        }

        if (linkTag.straightTag != null)
        {
            double lengthSI = linkTag.straightTag.length.getSI();
            if (linkTag.nodeTo == null)
            {
                Point3d coordinate =
                    new Point3d(linkTag.nodeFrom.getLocation().getX(), linkTag.nodeFrom.getLocation().getY(),
                        linkTag.nodeFrom.getLocation().getZ());
                double angle = linkTag.nodeFrom.getDirection().getSI();
                double slope = linkTag.nodeFrom.getSlope().getSI();
                coordinate.x += lengthSI * Math.cos(angle);
                coordinate.y += lengthSI * Math.sin(angle);
                coordinate.z += lengthSI * Math.sin(slope);
                NodeTag nodeTag = this.nodeTags.get(linkTag.nodeToName);
                nodeTag.angle = new DoubleScalar.Abs<AnglePlaneUnit>(angle, AnglePlaneUnit.SI);
                nodeTag.coordinate = coordinate;
                nodeTag.slope = new DoubleScalar.Abs<AngleSlopeUnit>(slope, AngleSlopeUnit.SI);
                @SuppressWarnings("rawtypes")
                Node node = makeNode(nodeTag);
                linkTag.nodeTo = node;
            }
            else if (linkTag.nodeFrom == null)
            {
                Point3d coordinate =
                    new Point3d(linkTag.nodeTo.getLocation().getX(), linkTag.nodeTo.getLocation().getY(), linkTag.nodeTo
                        .getLocation().getZ());
                double angle = linkTag.nodeTo.getDirection().getSI();
                double slope = linkTag.nodeTo.getSlope().getSI();
                coordinate.x -= lengthSI * Math.cos(angle);
                coordinate.y -= lengthSI * Math.sin(angle);
                coordinate.z -= lengthSI * Math.sin(slope);
                NodeTag nodeTag = this.nodeTags.get(linkTag.nodeFromName);
                nodeTag.angle = new DoubleScalar.Abs<AnglePlaneUnit>(angle, AnglePlaneUnit.SI);
                nodeTag.coordinate = coordinate;
                nodeTag.slope = new DoubleScalar.Abs<AngleSlopeUnit>(slope, AngleSlopeUnit.SI);
                @SuppressWarnings("rawtypes")
                Node node = makeNode(nodeTag);
                linkTag.nodeFrom = node;
            }
        }
        else if (linkTag.arcTag != null)
        {
            double radiusSI = linkTag.arcTag.radius.getSI();
            double angle = linkTag.arcTag.angle.getSI();
            ArcDirection direction = linkTag.arcTag.direction;
            if (linkTag.nodeTo == null)
            {
                Point3d coordinate = new Point3d();
                double startAngle = linkTag.nodeFrom.getDirection().getSI();
                double slope = linkTag.nodeFrom.getSlope().getSI();
                double lengthSI = radiusSI * angle;
                NodeTag nodeTag = this.nodeTags.get(linkTag.nodeToName);
                if (direction.equals(ArcDirection.LEFT))
                {
                    linkTag.arcTag.center =
                        new Point3d(linkTag.nodeFrom.getLocation().getX() + radiusSI * Math.cos(startAngle + Math.PI / 2.0),
                            linkTag.nodeFrom.getLocation().getY() + radiusSI * Math.sin(startAngle + Math.PI / 2.0), 0.0);
                    linkTag.arcTag.startAngle = startAngle - Math.PI / 2.0;
                    coordinate.x = linkTag.arcTag.center.x + radiusSI * Math.cos(linkTag.arcTag.startAngle + angle);
                    coordinate.y = linkTag.arcTag.center.y + radiusSI * Math.sin(linkTag.arcTag.startAngle + angle);
                    nodeTag.angle =
                        new DoubleScalar.Abs<AnglePlaneUnit>(AnglePlaneUnit.normalize(startAngle + angle), AnglePlaneUnit.SI);
                }
                else
                {
                    linkTag.arcTag.center =
                        new Point3d(coordinate.x + radiusSI * Math.cos(startAngle - Math.PI / 2.0), coordinate.y + radiusSI
                            * Math.sin(startAngle - Math.PI / 2.0), 0.0);
                    linkTag.arcTag.startAngle = startAngle;
                    coordinate.x = linkTag.arcTag.center.x + radiusSI * Math.cos(linkTag.arcTag.startAngle - angle);
                    coordinate.y = linkTag.arcTag.center.y + radiusSI * Math.sin(linkTag.arcTag.startAngle - angle);
                    nodeTag.angle =
                        new DoubleScalar.Abs<AnglePlaneUnit>(AnglePlaneUnit.normalize(startAngle - angle), AnglePlaneUnit.SI);
                }
                coordinate.z = linkTag.nodeFrom.getLocation().getZ() + lengthSI * Math.sin(slope);
                nodeTag.slope = new DoubleScalar.Abs<AngleSlopeUnit>(slope, AngleSlopeUnit.SI);
                nodeTag.coordinate = coordinate;
                @SuppressWarnings("rawtypes")
                Node node = makeNode(nodeTag);
                linkTag.nodeTo = node;
            }

            else if (linkTag.nodeFrom == null)
            {
                Point3d coordinate =
                    new Point3d(linkTag.nodeTo.getLocation().getX(), linkTag.nodeTo.getLocation().getY(), linkTag.nodeTo
                        .getLocation().getZ());
                double endAngle = linkTag.nodeTo.getDirection().getSI();
                double slope = linkTag.nodeTo.getSlope().getSI();
                double lengthSI = radiusSI * angle;
                NodeTag nodeTag = this.nodeTags.get(linkTag.nodeFromName);
                if (direction.equals(ArcDirection.LEFT))
                {
                    linkTag.arcTag.center =
                        new Point3d(coordinate.x + radiusSI + Math.cos(endAngle + Math.PI / 2.0), coordinate.y + radiusSI
                            * Math.sin(endAngle + Math.PI / 2.0), 0.0);
                    linkTag.arcTag.startAngle = endAngle - Math.PI / 2.0 - angle;
                    coordinate.x = linkTag.arcTag.center.x + radiusSI * Math.cos(linkTag.arcTag.startAngle);
                    coordinate.y = linkTag.arcTag.center.y + radiusSI * Math.sin(linkTag.arcTag.startAngle);
                    nodeTag.angle =
                        new DoubleScalar.Abs<AnglePlaneUnit>(AnglePlaneUnit.normalize(linkTag.arcTag.startAngle + Math.PI
                            / 2.0), AnglePlaneUnit.SI);
                }
                else
                {
                    linkTag.arcTag.center =
                        new Point3d(coordinate.x + radiusSI * Math.cos(endAngle - Math.PI / 2.0), coordinate.y + radiusSI
                            * Math.sin(endAngle - Math.PI / 2.0), 0.0);
                    linkTag.arcTag.startAngle = endAngle + Math.PI / 2.0 + angle;
                    coordinate.x = linkTag.arcTag.center.x + radiusSI * Math.cos(linkTag.arcTag.startAngle);
                    coordinate.y = linkTag.arcTag.center.y + radiusSI * Math.sin(linkTag.arcTag.startAngle);
                    nodeTag.angle =
                        new DoubleScalar.Abs<AnglePlaneUnit>(AnglePlaneUnit.normalize(linkTag.arcTag.startAngle - Math.PI
                            / 2.0), AnglePlaneUnit.SI);
                }
                coordinate.z -= lengthSI * Math.sin(slope);
                nodeTag.coordinate = coordinate;
                nodeTag.slope = new DoubleScalar.Abs<AngleSlopeUnit>(slope, AngleSlopeUnit.SI);
                @SuppressWarnings("rawtypes")
                Node node = makeNode(nodeTag);
                linkTag.nodeFrom = node;
            }
        }

    }

    /**
     * @param linkTag the link information from XML.
     * @return a constructed link
     * @throws SAXException when point cannot be instantiated
     * @throws NamingException when animation context cannot be found.
     * @throws RemoteException when communication error occurs when reaching animation context.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected final CrossSectionLink makeLink(final LinkTag linkTag) throws SAXException, RemoteException, NamingException
    {
        try
        {
            Object id = makeId(this.linkIdClass, linkTag.name);
            int points = 2;
            if (linkTag.arcTag != null)
            {
                points = (Math.abs(linkTag.arcTag.angle.getSI()) <= Math.PI / 2.0) ? 32 : 64;
            }
            NodeTag from = this.nodeTags.get(linkTag.nodeFromName);
            NodeTag to = this.nodeTags.get(linkTag.nodeToName);
            Coordinate[] coordinates = new Coordinate[points];
            coordinates[0] = new Coordinate(from.coordinate.x, from.coordinate.y, from.coordinate.z);
            coordinates[coordinates.length - 1] = new Coordinate(to.coordinate.x, to.coordinate.y, to.coordinate.z);
            if (linkTag.arcTag != null)
            {
                double angleStep = linkTag.arcTag.angle.getSI() / points;
                double slopeStep = (to.coordinate.z - from.coordinate.z) / points;
                double radiusSI = linkTag.arcTag.radius.getSI();
                if (linkTag.arcTag.direction.equals(ArcDirection.RIGHT))
                {
                    for (int p = 1; p < points - 1; p++)
                    {
                        coordinates[p] =
                            new Coordinate(linkTag.arcTag.center.x + radiusSI
                                * Math.cos(linkTag.arcTag.startAngle - angleStep * p), linkTag.arcTag.center.y + radiusSI
                                * Math.sin(linkTag.arcTag.startAngle - angleStep * p), from.coordinate.z + slopeStep * p);
                    }
                }
                else
                {
                    for (int p = 1; p < points - 1; p++)
                    {
                        coordinates[p] =
                            new Coordinate(linkTag.arcTag.center.x + radiusSI
                                * Math.cos(linkTag.arcTag.startAngle + angleStep * p), linkTag.arcTag.center.y + radiusSI
                                * Math.sin(linkTag.arcTag.startAngle + angleStep * p), from.coordinate.z + slopeStep * p);
                    }
                }
            }
            OTSLine3D designLine = new OTSLine3D(coordinates);
            CrossSectionLink link = new CrossSectionLink(id, linkTag.nodeFrom, linkTag.nodeTo, designLine);
            return link;
        }
        catch (NetworkException ne)
        {
            throw new SAXException("Error building Link", ne);
        }
    }

    /**
     * Make a generator.
     * @param generatorTag XML tag for the generator to build
     * @param lane the lane of the generator
     * @param name the name of the generator
     * @throws SimRuntimeException in case of simulation problems building the car generator
     * @throws RemoteException in case of network problems building the car generator
     * @throws NetworkException when route generator cannot be instantiated
     */
    protected final void makeGenerator(final GeneratorTag generatorTag, final Lane<?, ?> lane, final String name)
        throws SimRuntimeException, RemoteException, NetworkException
    {
        Class<?> gtuClass = LaneBasedIndividualCar.class;
        List<Node<?>> nodeList = new ArrayList<>();
        for (NodeTag nodeTag : generatorTag.routeTag.routeNodeTags)
        {
            nodeList.add(this.nodes.get(nodeTag.name));
        }
        DoubleScalar.Abs<TimeUnit> startTime =
            generatorTag.startTime != null ? generatorTag.startTime : new DoubleScalar.Abs<TimeUnit>(0.0, TimeUnit.SI);
        DoubleScalar.Abs<TimeUnit> endTime =
            generatorTag.endTime != null ? generatorTag.endTime : new DoubleScalar.Abs<TimeUnit>(Double.MAX_VALUE,
                TimeUnit.SI);
        LaneBasedRouteGenerator rg = new FixedLaneBasedRouteGenerator(new CompleteRoute("fixed route", nodeList));
        new GTUGeneratorIndividual<String>(name, this.simulator, generatorTag.gtuTag.gtuType, gtuClass,
            generatorTag.gtuTag.followingModel, generatorTag.gtuTag.laneChangeModel, generatorTag.initialSpeedDist,
            generatorTag.iatDist, generatorTag.gtuTag.lengthDist, generatorTag.gtuTag.widthDist,
            generatorTag.gtuTag.maxSpeedDist, generatorTag.maxGTUs, startTime, endTime, lane, generatorTag.position, rg,
            this.gtuColorer);
    }

    /**
     * Calculates the offsets and widths of the lanes and stripes in a RoadTypeTag by a forward pass from the first found
     * offset, and then a backward pass from the end to the beginning. Offsets are always in the middle of a lane or stripe.
     * Also give each lane a speed.
     * @param roadTypeTag the road type tag to calculate the offsets for.
     * @param globalTag to provide the global length.
     * @throws SAXException when the width of a lane cannot be calculated.
     */
    @SuppressWarnings("checkstyle:needbraces")
    protected final void calculateRoadTypeOffsets(final RoadTypeTag roadTypeTag, final GlobalTag globalTag)
        throws SAXException
    {
        // cseTags in a LinkedHashMap to guarantee the order.
        DoubleScalar.Rel<LengthUnit> lastOffset = null;
        DoubleScalar.Rel<LengthUnit> lastEdge = null;
        List<String> cseNames = new ArrayList<>();

        // forward pass
        for (String cseName : roadTypeTag.cseTags.keySet())
        {
            cseNames.add(cseName);
            CrossSectionElementTag cseTag = roadTypeTag.cseTags.get(cseName);

            // set the width
            double widthSI = Double.NaN;
            if (cseTag.width != null)
                widthSI = cseTag.width.getSI();
            else
            {
                if (roadTypeTag.width != null)
                    widthSI = roadTypeTag.width.getSI();
                else if (globalTag.width != null)
                    widthSI = globalTag.width.getSI();
                else
                    throw new SAXException("calculateRoadTypeOffsets for road type " + roadTypeTag.name
                        + ", no width for CSE " + cseName);
                cseTag.width = new DoubleScalar.Rel<LengthUnit>(widthSI, LengthUnit.SI);
            }

            // set the speed
            if (cseTag.elementType.equals(ElementType.LANE) && cseTag.speed == null)
            {
                if (roadTypeTag.speed != null)
                    cseTag.speed = roadTypeTag.speed.copy();
                else if (globalTag.speed != null)
                    cseTag.speed = globalTag.speed;
                else
                    throw new SAXException("calculateRoadTypeOffsets for road type " + roadTypeTag.name
                        + ", no speed for CSE " + cseName);
            }

            // set the offsets
            if (cseTag.offset != null)
            {
                lastOffset = cseTag.offset.copy();
                if (cseTag.elementType.equals(ElementType.STRIPE))
                    lastEdge = lastOffset.copy();
                else
                    lastEdge = new DoubleScalar.Rel<LengthUnit>(lastOffset.getSI() + widthSI / 2.0, LengthUnit.SI);
            }
            else
            {
                if (lastOffset != null)
                {
                    if (cseTag.elementType.equals(ElementType.STRIPE))
                        cseTag.offset = lastEdge.copy();
                    else
                    {
                        cseTag.offset = new DoubleScalar.Rel<LengthUnit>(lastEdge.getSI() + widthSI / 2.0, LengthUnit.SI);
                        lastEdge = new DoubleScalar.Rel<LengthUnit>(lastEdge.getSI() + widthSI, LengthUnit.SI);
                    }
                }
            }
        }

        // backward pass for the offsets
        lastOffset = null;
        lastEdge = null;
        for (int i = cseNames.size() - 1; i >= 0; i--)
        {
            String cseName = cseNames.get(i);
            CrossSectionElementTag cseTag = roadTypeTag.cseTags.get(cseName);
            double widthSI = cseTag.width.getSI();
            if (cseTag.offset != null)
            {
                lastOffset = cseTag.offset.copy();
                if (cseTag.elementType.equals(ElementType.STRIPE))
                    lastEdge = lastOffset.copy();
                else
                    lastEdge = new DoubleScalar.Rel<LengthUnit>(lastOffset.getSI() - widthSI / 2.0, LengthUnit.SI);
            }
            else
            {
                if (lastOffset != null)
                {
                    if (cseTag.elementType.equals(ElementType.STRIPE))
                        cseTag.offset = lastEdge.copy();
                    else
                    {
                        cseTag.offset = new DoubleScalar.Rel<LengthUnit>(lastEdge.getSI() - widthSI / 2.0, LengthUnit.SI);
                        lastEdge = new DoubleScalar.Rel<LengthUnit>(lastEdge.getSI() - widthSI, LengthUnit.SI);
                    }
                }
            }
        }
    }

    /**
     * At the end of a link tag, make sure the road type information is applied to the link.
     * @param roadTypeTag the road type information
     * @param csl the cross section link on which the data will be applied
     * @param linkTag the link information from XML
     * @param globalTag the global information for data not specified at the link
     * @throws NetworkException when the stripe cannot be instantiated
     * @throws RemoteException when the (remote) animator cannot be reached to create the animation
     * @throws NamingException when the /animation/2D tree cannot be found in the context
     * @throws SAXException when the stripe type cannot be parsed correctly
     * @throws GTUException when lane block cannot be created
     * @throws SimRuntimeException when generator cannot be created
     */
    @SuppressWarnings({"checkstyle:needbraces", "rawtypes", "unchecked"})
    protected final void applyRoadTypeToLink(final RoadTypeTag roadTypeTag, final CrossSectionLink csl,
        final LinkTag linkTag, final GlobalTag globalTag) throws NetworkException, RemoteException, NamingException,
        SAXException, GTUException, SimRuntimeException
    {
        List<CrossSectionElement> cseList = new ArrayList<>();
        List<Lane> lanes = new ArrayList<>();
        for (CrossSectionElementTag cseTag : roadTypeTag.cseTags.values())
        {
            switch (cseTag.elementType)
            {
                case STRIPE:
                    switch (cseTag.stripeType)
                    {
                        case BLOCKED:
                        case DASHED:
                            Stripe dashedLine = new Stripe(csl, cseTag.offset, cseTag.width);
                            dashedLine.addPermeability(GTUType.ALL, Permeable.BOTH);
                            if (this.simulator != null)
                            {
                                new StripeAnimation(dashedLine, this.simulator, StripeAnimation.TYPE.DASHED);
                            }
                            cseList.add(dashedLine);
                            break;

                        case DOUBLE:
                            Stripe doubleLine = new Stripe(csl, cseTag.offset, cseTag.width);
                            if (this.simulator != null)
                            {
                                new StripeAnimation(doubleLine, this.simulator, StripeAnimation.TYPE.DOUBLE);
                            }
                            cseList.add(doubleLine);
                            break;

                        case LEFTONLY:
                            Stripe leftOnlyLine = new Stripe(csl, cseTag.offset, cseTag.width);
                            leftOnlyLine.addPermeability(GTUType.ALL, Permeable.LEFT); // TODO: correct?
                            if (this.simulator != null)
                            {
                                new StripeAnimation(leftOnlyLine, this.simulator, StripeAnimation.TYPE.LEFTONLY);
                            }
                            cseList.add(leftOnlyLine);
                            break;

                        case RIGHTONLY:
                            Stripe rightOnlyLine = new Stripe(csl, cseTag.offset, cseTag.width);
                            rightOnlyLine.addPermeability(GTUType.ALL, Permeable.RIGHT); // TODO: correct?
                            if (this.simulator != null)
                            {
                                new StripeAnimation(rightOnlyLine, this.simulator, StripeAnimation.TYPE.RIGHTONLY);
                            }
                            cseList.add(rightOnlyLine);
                            break;

                        case SOLID:
                            Stripe solidLine = new Stripe(csl, cseTag.offset, cseTag.width);
                            if (this.simulator != null)
                            {
                                new StripeAnimation(solidLine, this.simulator, StripeAnimation.TYPE.SOLID);
                            }
                            cseList.add(solidLine);
                            break;

                        default:
                            throw new SAXException("Unknown Stripe type: " + cseTag.stripeType.toString());
                    }
                    break;

                case LANE:
                {
                    if (linkTag.sinkLanes.contains(cseTag.name))
                    {
                        // SINKLANE
                        SinkLane sinkLane =
                            new SinkLane(csl, cseTag.offset, cseTag.width, cseTag.laneType, cseTag.direction, cseTag.speed);
                        cseList.add(sinkLane);
                        lanes.add(sinkLane);
                        linkTag.lanes.put(cseTag.name, sinkLane);
                        if (this.simulator != null)
                        {
                            new LaneAnimation(sinkLane, this.simulator, cseTag.color);
                        }
                    }

                    else

                    {
                        // TODO LANEOVERRIDE
                        Lane<?, ?> lane =
                            new Lane.STR(csl, cseTag.offset, cseTag.offset, cseTag.width, cseTag.width, cseTag.laneType,
                                cseTag.direction, new DoubleScalar.Abs<FrequencyUnit>(Double.MAX_VALUE,
                                    FrequencyUnit.PER_HOUR), cseTag.speed);
                        cseList.add(lane);
                        lanes.add(lane);
                        linkTag.lanes.put(cseTag.name, lane);
                        if (this.simulator != null)
                        {
                            new LaneAnimation(lane, this.simulator, cseTag.color);
                        }

                        // BLOCK
                        if (linkTag.blockTags.containsKey(cseTag.name))
                        {
                            BlockTag blockTag = linkTag.blockTags.get(cseTag.name);
                            new LaneBlock(lane, blockTag.position, this.simulator, null);
                        }

                        // GENERATOR
                        if (linkTag.generatorTags.containsKey(cseTag.name))
                        {
                            GeneratorTag generatorTag = linkTag.generatorTags.get(cseTag.name);
                            makeGenerator(generatorTag, lane, cseTag.name);
                        }

                        // TODO FILL

                    }
                    break;
                }

                case NOTRAFFICLANE:
                {
                    // TODO Override
                    Lane<?, ?> lane = new NoTrafficLane.STR(csl, cseTag.offset, cseTag.offset, cseTag.width, cseTag.width);
                    cseList.add(lane);
                    if (this.simulator != null)
                    {
                        new LaneAnimation(lane, this.simulator, cseTag.color);
                    }
                    break;
                }

                case SHOULDER:
                {
                    // TODO Override
                    Shoulder shoulder = new Shoulder(csl, cseTag.offset, cseTag.width, cseTag.width);
                    cseList.add(shoulder);
                    if (this.simulator != null)
                    {
                        new ShoulderAnimation(shoulder, this.simulator);
                        // TODO color
                    }
                    break;
                }

                default:
                    throw new SAXException("Unknown Element type: " + cseTag.elementType.toString());
            }
        } // for (CrossSectionElementTag cseTag : roadTypeTag.cseTags.values())

        // make adjacent lanes
        for (int laneIndex = 1; laneIndex < lanes.size(); laneIndex++)
        {
            lanes.get(laneIndex - 1).addAccessibleAdjacentLane(lanes.get(laneIndex), LateralDirectionality.RIGHT);
            lanes.get(laneIndex).addAccessibleAdjacentLane(lanes.get(laneIndex - 1), LateralDirectionality.LEFT);
        }
    }

    /**
     * @param nodeNames the node names as a space-separated String
     * @return a list of NodeTags
     * @throws SAXException when node could not be found
     */
    protected final List<NodeTag> parseNodeList(final String nodeNames) throws SAXException
    {
        List<NodeTag> nodeList = new ArrayList<>();
        String[] ns = nodeNames.split("\\s");
        for (String s : ns)
        {
            if (!this.nodeTags.containsKey(s))
            {
                throw new SAXException("Node " + s + " from node list [" + nodeNames + "] was not defined");
            }
            nodeList.add(this.nodeTags.get(s));
        }
        return nodeList;
    }

    /**
     * This method parses a length string that can have values such as: BEGIN, END, 10m, END-10m, 98%.
     * @param posStr the position string to parse. Lengths are relative to the design line.
     * @param linkTag the link to retrieve the design line
     * @return the corresponding position as a length on the design line
     * @throws NetworkException when parsing fails
     */
    protected final DoubleScalar.Rel<LengthUnit> parseBeginEndPosition(final String posStr, final LinkTag linkTag)
        throws NetworkException
    {
        if (posStr.trim().equals("BEGIN"))
        {
            return new DoubleScalar.Rel<LengthUnit>(0.0, LengthUnit.METER);
        }

        double length;
        if (linkTag.arcTag != null)
        {
            length = linkTag.arcTag.radius.getSI() * linkTag.arcTag.angle.getSI();
        }
        else if (linkTag.straightTag != null)
        {
            length = linkTag.straightTag.length.getSI();
        }
        else
        {
            throw new NetworkException("parseBeginEndPosition - attribute POSITION with value " + posStr
                + " invalid for link " + linkTag.name + ": link is neither arc nor straight");
        }

        if (posStr.trim().equals("END"))
        {
            return new DoubleScalar.Rel<LengthUnit>(length, LengthUnit.METER);
        }

        if (posStr.endsWith("%"))
        {
            String s = posStr.substring(0, posStr.length() - 1).trim();
            try
            {
                double fraction = Double.parseDouble(s) / 100.0;
                if (fraction < 0.0 || fraction > 1.0)
                {
                    throw new NetworkException("parseBeginEndPosition: attribute POSITION with value " + posStr
                        + " invalid for link " + linkTag.name + ", should be a percentage between 0 and 100%");
                }
                return new DoubleScalar.Rel<LengthUnit>(length * fraction, LengthUnit.METER);
            }
            catch (NumberFormatException nfe)
            {
                throw new NetworkException("parseBeginEndPosition: attribute POSITION with value " + posStr
                    + " invalid for link " + linkTag.name + ", should be a percentage between 0 and 100%", nfe);
            }
        }

        if (posStr.trim().startsWith("END-"))
        {
            String s = posStr.substring(4).trim();
            double offset = LengthUnits.parseLengthRel(s).getSI();
            if (offset > length)
            {
                throw new NetworkException("parseBeginEndPosition - attribute POSITION with value " + posStr
                    + " invalid for link " + linkTag.name + ": provided negative offset greater than than link length");
            }
            return new DoubleScalar.Rel<LengthUnit>(length - offset, LengthUnit.METER);
        }

        DoubleScalar.Rel<LengthUnit> offset = LengthUnits.parseLengthRel(posStr);
        if (offset.getSI() > length)
        {
            throw new NetworkException("parseBeginEndPosition - attribute POSITION with value " + posStr
                + " invalid for link " + linkTag.name + ": provided offset greater than than link length");
        }
        return offset;
    }

    /*************************************************************************************************/
    /****************************** TAG CLASSES TO KEEP THE XML INFORMATION **************************/
    /*************************************************************************************************/

    /** LANEOVERRIDE element. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    class LaneOverrideTag
    {
        /** speed limit. */
        DoubleScalar.Abs<SpeedUnit> speed = null;

        /** direction. */
        LongitudinalDirectionality direction;

        /** animation color. */
        Color color;
    }

    /** LINK element. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    class LinkTag
    {
        /** name. */
        String name = null;

        /** from node. */
        @SuppressWarnings("rawtypes")
        Node nodeFrom = null;

        /** to node. */
        @SuppressWarnings("rawtypes")
        Node nodeTo = null;

        /** from node name. */
        String nodeFromName = null;

        /** to node name. */
        String nodeToName = null;

        /** road type. */
        RoadTypeTag roadTypeTag = null;

        /** straight. */
        StraightTag straightTag = null;

        /** arc. */
        ArcTag arcTag = null;

        /** map of lane name to lane override. */
        Map<String, LaneOverrideTag> laneOverrideTags = new HashMap<>();

        /** map of lane name to generators. */
        Map<String, GeneratorTag> generatorTags = new HashMap<>();

        /** map of lane name to blocks. */
        Map<String, BlockTag> blockTags = new HashMap<>();

        /** map of lane name to fill at t=0. */
        Map<String, FillTag> fillTags = new HashMap<>();

        /** map of lane name to generated lanes. */
        Map<String, Lane> lanes = new HashMap<>();

        /** sink lane names. */
        Set<String> sinkLanes = new HashSet<>();
    }

    /** ARC element. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    class ArcTag
    {
        /** angle. */
        DoubleScalar.Abs<AnglePlaneUnit> angle = null;

        /** radius. */
        DoubleScalar.Rel<LengthUnit> radius = null;

        /** direction. */
        ArcDirection direction = null;

        /** the center coordinate of the arc. Will be filled after parsing. */
        Point3d center;

        /** the startAngle in radians compared to the center coordinate. Will be filled after parsing. */
        double startAngle;
    }

    /** STRAIGHT element. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    class StraightTag
    {
        /** length. */
        DoubleScalar.Rel<LengthUnit> length = null;
    }

    /** direction of the arc; LEFT or RIGHT. */
    enum ArcDirection
    {
        /** Left = counter-clockwise. */
        LEFT,
        /** Right = clockwise. */
        RIGHT;
    }

    /** Generator element. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    class GeneratorTag
    {
        /** position of the generator on the link, relative to the design line. */
        DoubleScalar.Rel<LengthUnit> position = null;

        /** GTU tag. */
        GTUTag gtuTag = null;

        /** GTU mix tag. */
        GTUMixTag gtuMixTag = null;

        /** interarrival time. */
        DistContinuousDoubleScalar.Rel<TimeUnit> iatDist = null;

        /** initial speed. */
        DistContinuousDoubleScalar.Abs<SpeedUnit> initialSpeedDist = null;

        /** max number of generated GTUs. */
        int maxGTUs = Integer.MAX_VALUE;

        /** start time of generation. */
        DoubleScalar.Abs<TimeUnit> startTime = null;

        /** end time of generation. */
        DoubleScalar.Abs<TimeUnit> endTime = null;

        /** Route tag. */
        RouteTag routeTag = null;

        /** Route mix tag. */
        RouteMixTag routeMixTag = null;

        /** Shortest route tag. */
        ShortestRouteTag shortestRouteTag = null;

        /** Shortest route mix tag. */
        ShortestRouteMixTag shortestRouteMixTag = null;

        /** GTU colorer. */
        String gtuColorer;
    }

    /** ListGenerator element. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    class ListGeneratorTag
    {
        /** URI of the list. */
        URI uri = null;

        /** position of the generator on the link, relative to the design line. */
        DoubleScalar.Rel<LengthUnit> position = null;

        /** GTU tag. */
        GTUTag gtuTag = null;

        /** GTU mix tag. */
        GTUMixTag gtuMixTag = null;

        /** initial speed. */
        DistContinuousDoubleScalar.Abs<SpeedUnit> initialSpeedDist = null;

        /** GTU colorer. */
        String gtuColorer;
    }

    /** Fill element. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    class FillTag
    {
        /** GTU tag. */
        GTUTag gtuTag = null;

        /** GTU mix tag. */
        GTUMixTag gtuMixTag = null;

        /** inter-vehicle distance. */
        DistContinuousDoubleScalar.Rel<LengthUnit> distanceDist = null;

        /** initial speed. */
        DistContinuousDoubleScalar.Abs<SpeedUnit> initialSpeedDist = null;

        /** max number of generated GTUs. */
        int maxGTUs = Integer.MAX_VALUE;

        /** Route tag. */
        RouteTag routeTag = null;

        /** Route mix tag. */
        RouteMixTag routeMixTag = null;

        /** Shortest route tag. */
        ShortestRouteTag shortestRouteTag = null;

        /** Shortest route mix tag. */
        ShortestRouteMixTag shortestRouteMixTag = null;
    }

    /** BLOCK element. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    class BlockTag
    {
        /** position of the block. */
        DoubleScalar.Rel<LengthUnit> position = null;
    }

    /** ROUTE element. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    class RouteTag
    {
        /** name. */
        String name = null;

        /** Nodes. */
        List<NodeTag> routeNodeTags = new ArrayList<NodeTag>();
    }

    /** SHORTESTROUTE element. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    class ShortestRouteTag
    {
        /** name. */
        String name = null;

        /** From Node. */
        NodeTag from = null;

        /** Via Nodes. */
        List<NodeTag> via = new ArrayList<NodeTag>();

        /** To Node. */
        NodeTag to = null;

        /** time unit for the "cost" per time. */
        DoubleScalar.Abs<FrequencyUnit> costPerTime = null;

        /** distance unit for the "cost" per time. */
        DoubleScalar.Abs<LinearDensityUnit> costPerDistance = null;
    }

    /** ROUTEMIX element. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    class RouteMixTag
    {
        /** name. */
        String name = null;

        /** routes. */
        List<RouteTag> routes = new ArrayList<RouteTag>();

        /** weights. */
        List<Double> weights = new ArrayList<Double>();
    }

    /** SHORTESTROUTEMIX element. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    class ShortestRouteMixTag
    {
        /** name. */
        String name = null;

        /** shortest routes. */
        List<ShortestRouteTag> shortestRoutes = new ArrayList<ShortestRouteTag>();

        /** weights. */
        List<Double> weights = new ArrayList<Double>();
    }

}
