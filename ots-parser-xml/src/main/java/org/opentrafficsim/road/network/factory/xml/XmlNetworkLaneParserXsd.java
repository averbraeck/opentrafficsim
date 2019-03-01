package org.opentrafficsim.road.network.factory.xml;

import java.io.File;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.djunits.unit.DirectionUnit;
import org.djunits.value.AngleUtil;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Duration;
import org.djutils.io.URLResource;
import org.djutils.logger.CategoryLogger;
import org.opentrafficsim.base.logger.Cat;
import org.opentrafficsim.core.dsol.OTSSimulator;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.Bezier;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.CrossSectionLink.Priority;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;
import org.opentrafficsim.trafficcontrol.FixedTimeController;
import org.opentrafficsim.trafficcontrol.FixedTimeController.SignalGroup;
import org.opentrafficsim.xml.bindings.types.ArcDirection;
import org.opentrafficsim.xml.generated.CONTROL;
import org.opentrafficsim.xml.generated.CONTROL.FIXEDTIME;
import org.opentrafficsim.xml.generated.LINK;
import org.opentrafficsim.xml.generated.NETWORK;
import org.opentrafficsim.xml.generated.NODE;
import org.opentrafficsim.xml.generated.SIGNALGROUP;
import org.opentrafficsim.xml.generated.TRAFFICLIGHTTYPE;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * Parse an XML file for an OTS network, based on the ots-network.xsd definition.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class XmlNetworkLaneParserXsd implements Serializable
{
    /** */
    private static final long serialVersionUID = 2019022L;

    /**
     * Parse the XML file and build the network.
     * @param filename the name of the file to parse
     * @param otsNetwork the network to insert the parsed objects in
     * @param simulator the simulator
     * @return the network that contains the parsed objects
     * @throws JAXBException when the parsing wails
     * @throws URISyntaxException when the filename is not valid
     * @throws NetworkException when the objects cannot be inserted into the network due to inconsistencies
     * @throws OTSGeometryException when the design line of a link is invalid
     */
    public static OTSNetwork build(final String filename, final OTSNetwork otsNetwork, final OTSSimulatorInterface simulator)
            throws JAXBException, URISyntaxException, NetworkException, OTSGeometryException
    {
        JAXBContext jc = JAXBContext.newInstance(NETWORK.class);

        Unmarshaller unmarshaller = jc.createUnmarshaller();
        File xml = new File(URLResource.getResource(filename).toURI().getPath());
        NETWORK jaxbNetwork = (NETWORK) unmarshaller.unmarshal(xml);
        List<Object> networkObjects = jaxbNetwork.getDEFINITIONSOrIncludeOrNODE();

        makeNodes(otsNetwork, networkObjects);
        Map<String, Direction> nodeDirections = calculateNodeAngles(otsNetwork, networkObjects);
        makeLinks(otsNetwork, networkObjects, nodeDirections, simulator);
        // applyRoadTypes(otsNetwork, networkObjects, simulator);
        makeControl(otsNetwork, simulator, networkObjects);

        return otsNetwork;
    }

    /**
     * Parse the Nodes.
     * @param otsNetwork the network to insert the parsed objects in
     * @param networkObjects the objects in the NETWORK tag
     * @throws NetworkException when the objects cannot be inserted into the network due to inconsistencies
     */
    private static void makeNodes(final OTSNetwork otsNetwork, final List<Object> networkObjects) throws NetworkException
    {
        for (Object networkObject : networkObjects)
        {
            if (networkObject instanceof NODE)
            {
                NODE xmlNode = (NODE) networkObject;
                new OTSNode(otsNetwork, xmlNode.getNAME(), new OTSPoint3D(xmlNode.getCOORDINATE()));
            }
        }
    }

    /**
     * Calculate the default angles of the Nodes, in case they have not been set. This is based on the STRAIGHT LINK elements in
     * the XML file.
     * @param otsNetwork the network to insert the parsed objects in
     * @param networkObjects the objects in the NETWORK tag
     * @return a map of nodes and their default direction
     */
    private static Map<String, Direction> calculateNodeAngles(final OTSNetwork otsNetwork, final List<Object> networkObjects)
    {
        Map<String, Direction> nodeDirections = new HashMap<>();
        for (NODE xmlNode : getObjectsOfType(networkObjects, NODE.class))
        {
            if (xmlNode.getDIRECTION() != null)
            {
                nodeDirections.put(xmlNode.getNAME(), xmlNode.getDIRECTION());
            }
        }

        for (LINK xmlLink : getObjectsOfType(networkObjects, LINK.class))
        {
            if (xmlLink.getSTRAIGHT() != null)
            {
                Node startNode = otsNetwork.getNode(xmlLink.getNODESTART().getNAME());
                Node endNode = otsNetwork.getNode(xmlLink.getNODEEND().getNAME());
                double direction = Math.atan2(endNode.getPoint().y - startNode.getPoint().y,
                        endNode.getPoint().x - startNode.getPoint().x);
                if (!nodeDirections.containsKey(startNode.getId()))
                {
                    nodeDirections.put(startNode.getId(), new Direction(direction, DirectionUnit.EAST_RADIAN));
                }
                if (!nodeDirections.containsKey(endNode.getId()))
                {
                    nodeDirections.put(endNode.getId(), new Direction(direction, DirectionUnit.EAST_RADIAN));
                }
            }
        }
        return nodeDirections;
    }

    /**
     * Build the links with the correct design line.
     * @param otsNetwork the network to insert the parsed objects in
     * @param networkObjects the objects in the NETWORK tag
     * @param nodeDirections a map of the node ids and their default directions
     * @param simulator the simulator
     * @throws NetworkException when the objects cannot be inserted into the network due to inconsistencies
     * @throws OTSGeometryException when the design line is invalid
     */
    static void makeLinks(final OTSNetwork otsNetwork, final List<Object> networkObjects, Map<String, Direction> nodeDirections,
            OTSSimulatorInterface simulator) throws NetworkException, OTSGeometryException
    {
        for (LINK xmlLink : getObjectsOfType(networkObjects, LINK.class))
        {
            Node startNode = otsNetwork.getNode(xmlLink.getNODESTART().getNAME());
            Node endNode = otsNetwork.getNode(xmlLink.getNODEEND().getNAME());
            double startDirection = nodeDirections.get(startNode.getId()).getInUnit();
            double endDirection = nodeDirections.get(endNode.getId()).getInUnit();
            OTSPoint3D startPoint = new OTSPoint3D(startNode.getPoint());
            OTSPoint3D endPoint = new OTSPoint3D(endNode.getPoint());

            if (!xmlLink.getOFFSETSTART().eq0())
            {
                // shift the start point perpendicular to the node direction or read from tag
                double offset = xmlLink.getOFFSETSTART().si;
                startPoint = new OTSPoint3D(startPoint.x + offset * Math.cos(startDirection + Math.PI / 2.0),
                        startPoint.y + offset * Math.sin(startDirection + Math.PI / 2.0), startPoint.z);
                CategoryLogger.filter(Cat.PARSER).debug("fc = " + startNode.getPoint() + ", sa = " + startDirection + ", so = "
                        + offset + ", sp = " + startPoint);
            }

            if (!xmlLink.getOFFSETEND().eq0())
            {
                // shift the end point perpendicular to the node direction or read from tag
                double offset = xmlLink.getOFFSETEND().si;
                endPoint = new OTSPoint3D(endPoint.x + offset * Math.cos(endDirection + Math.PI / 2.0),
                        endPoint.y + offset * Math.sin(endDirection + Math.PI / 2.0), endPoint.z);
                CategoryLogger.filter(Cat.PARSER).debug(
                        "tc = " + endNode.getPoint() + ", ea = " + endDirection + ", eo = " + offset + ", ep = " + endPoint);
            }

            OTSPoint3D[] coordinates = null;

            if (xmlLink.getSTRAIGHT() != null)
            {
                coordinates = new OTSPoint3D[2];
                coordinates[0] = startPoint;
                coordinates[1] = endPoint;
            }

            else if (xmlLink.getPOLYLINE() != null)
            {
                int intermediatePoints = xmlLink.getPOLYLINE().getINTERMEDIATEPOINTS().size();
                coordinates = new OTSPoint3D[intermediatePoints + 2];
                coordinates[0] = startPoint;
                coordinates[intermediatePoints + 1] = endPoint;
                for (int p = 0; p < intermediatePoints; p++)
                {
                    coordinates[p + 1] = new OTSPoint3D(xmlLink.getPOLYLINE().getINTERMEDIATEPOINTS().get(p));
                }

            }
            else if (xmlLink.getARC() != null)
            {
                // calculate the center position
                double radiusSI = xmlLink.getARC().getRADIUS().getSI();
                double offsetStart = 0.0;
                if (xmlLink.getOFFSETSTART() != null)
                {
                    offsetStart = xmlLink.getOFFSETSTART().si;
                }
                double offsetEnd = 0.0;
                if (xmlLink.getOFFSETEND() != null)
                {
                    offsetEnd = xmlLink.getOFFSETEND().si;
                }
                List<OTSPoint3D> centerList = OTSPoint3D.circleIntersections(startNode.getPoint(), radiusSI + offsetStart,
                        endNode.getPoint(), radiusSI + offsetEnd);
                OTSPoint3D center =
                        (xmlLink.getARC().getDIRECTION().equals(ArcDirection.RIGHT)) ? centerList.get(0) : centerList.get(1);

                // calculate start angle and end angle
                double sa = Math.atan2(startNode.getPoint().y - center.y, startNode.getPoint().x - center.x);
                double ea = Math.atan2(endNode.getPoint().y - center.y, endNode.getPoint().x - center.x);
                if (xmlLink.getARC().getDIRECTION().equals(ArcDirection.RIGHT))
                {
                    // right -> negative direction, ea should be less than sa
                    ea = (sa < ea) ? ea + Math.PI * 2.0 : ea;
                }
                else
                {
                    // left -> positive direction, sa should be less than ea
                    ea = (ea < sa) ? ea + Math.PI * 2.0 : ea;
                }

                // TODO: user defined #points
                int points = (AngleUtil.normalize(ea - sa) <= Math.PI / 2.0) ? 64 : 128;
                coordinates = new OTSPoint3D[points];
                coordinates[0] = new OTSPoint3D(startNode.getPoint().x + Math.cos(sa) * offsetStart,
                        startNode.getPoint().y + Math.sin(sa) * offsetStart, startNode.getPoint().z);
                coordinates[coordinates.length - 1] = new OTSPoint3D(endNode.getPoint().x + Math.cos(ea) * offsetEnd,
                        endNode.getPoint().y + Math.sin(ea) * offsetEnd, endNode.getPoint().z);
                double angleStep = Math.abs((ea - sa)) / points;
                double slopeStep = (endNode.getPoint().z - startNode.getPoint().z) / points;

                if (xmlLink.getARC().getDIRECTION().equals(ArcDirection.RIGHT))
                {
                    for (int p = 1; p < points - 1; p++)
                    {
                        double dRad = offsetStart + (offsetEnd - offsetStart) * p / points;
                        coordinates[p] = new OTSPoint3D(center.x + (radiusSI + dRad) * Math.cos(sa - angleStep * p),
                                center.y + (radiusSI + dRad) * Math.sin(sa - angleStep * p),
                                startNode.getPoint().z + slopeStep * p);
                    }
                }
                else
                {
                    for (int p = 1; p < points - 1; p++)
                    {
                        double dRad = offsetStart + (offsetEnd - offsetStart) * p / points;
                        coordinates[p] = new OTSPoint3D(center.x + (radiusSI + dRad) * Math.cos(sa + angleStep * p),
                                center.y + (radiusSI + dRad) * Math.sin(sa + angleStep * p),
                                startNode.getPoint().z + slopeStep * p);
                    }
                }
            }

            else if (xmlLink.getBEZIER() != null)
            {
                // TODO: user defined #points

                coordinates = Bezier
                        .cubic(128, new DirectedPoint(startPoint.x, startPoint.y, startPoint.z, 0, 0, startDirection),
                                new DirectedPoint(endPoint.x, endPoint.y, endPoint.z, 0, 0, endDirection), 1.0, false)
                        .getPoints();

                // TODO: Bezier shape factor and weighted factor
            }

            else if (xmlLink.getCLOTHOID() != null)
            {
                // TODO: user defined #points
                // TODO: Clothoid parsing
            }

            else
            {
                throw new NetworkException("Making link, but link " + xmlLink.getNAME()
                        + " has no filled straight, arc, bezier, polyline, or clothoid definition");
            }

            OTSLine3D designLine = OTSLine3D.createAndCleanOTSLine3D(coordinates);

            // TODO: Directionality has to be added later when the lanes and their direction are known.
            LaneKeepingPolicy laneKeepingPolicy = LaneKeepingPolicy.valueOf(xmlLink.getLANEKEEPING());
            CrossSectionLink link = new CrossSectionLink(otsNetwork, xmlLink.getNAME(), startNode, endNode, LinkType.FREEWAY,
                    designLine, simulator, laneKeepingPolicy);

            if (xmlLink.getPRIORITY() != null)
            {
                Priority priority = Priority.valueOf(xmlLink.getPRIORITY());
                link.setPriority(priority);
            }

            // TODO: rotationstart and rotationend of a link, or leave out of xsd?
            // TODO: networkAnimation.addDrawingInfoBase(link, new DrawingInfoLine<CrossSectionLink>(Color.BLACK, 0.5f));
        }
    }

    /**
     * Creates control objects.
     * @param otsNetwork OTSNetwork; network
     * @param simulator OTSSimulatorInterface; simulator
     * @param networkObjects List&lt;Object&gt;; XSD objects
     */
    public static void makeControl(final OTSNetwork otsNetwork, final OTSSimulatorInterface simulator,
            final List<Object> networkObjects)
    {
        for (CONTROL control : getObjectsOfType(networkObjects, CONTROL.class))
        {
            // Fixed time controllers
            for (FIXEDTIME fixedTime : control.getFIXEDTIME())
            {
                String id = fixedTime.getID();
                Duration cycleTime = fixedTime.getCYCLETIME();
                Duration offset = fixedTime.getOFFSET();
                Set<SignalGroup> signalGroups = new LinkedHashSet<>();
                for (CONTROL.FIXEDTIME.SIGNALGROUP signalGroup : fixedTime.getSIGNALGROUP())
                {
                    String signalGroupId = signalGroup.getID();
                    Duration signalGroupOffset = signalGroup.getOFFSET();
                    Duration preGreen = signalGroup.getPREGREEN() == null ? Duration.ZERO : signalGroup.getPREGREEN();
                    Duration green = signalGroup.getGREEN();
                    Duration yellow = signalGroup.getYELLOW();

                    // TODO: is there a better way to obtain a referenced object?
                    SIGNALGROUP referencedSignalGroup =
                            findObject(networkObjects, SIGNALGROUP.class, new Predicate<SIGNALGROUP>()
                            {
                                /** {@inheritDoc} */
                                @Override
                                public boolean test(final SIGNALGROUP t)
                                {
                                    return t.getID().equals(signalGroupId);
                                }
                            });

                    Set<String> trafficLightIds = new LinkedHashSet<>();
                    for (TRAFFICLIGHTTYPE trafficLight : referencedSignalGroup.getTRAFFICLIGHT())
                    {
                        trafficLightIds.add(trafficLight.getNAME());
                    }
                    signalGroups
                            .add(new SignalGroup(signalGroupId, trafficLightIds, signalGroupOffset, preGreen, green, yellow));
                }
                try
                {
                    new FixedTimeController(id, simulator, otsNetwork, cycleTime, offset, signalGroups);
                }
                catch (SimRuntimeException exception)
                {
                    // cannot happen; parsing happens with a new simulator at time = 0
                    throw new RuntimeException(exception);
                }
            }
        }
    }

    /**
     * Returns all objects of given type from the list of all objects.
     * @param networkObjects List&lt;Object&gt;; list of objects
     * @param clazz Class&lt;T&gt;; class of type of objects to return
     * @param <T> type
     * @return list of all objects of given type from the list of all objects
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> getObjectsOfType(final List<Object> networkObjects, final Class<T> clazz)
    {
        List<T> list = new ArrayList<>();
        for (Object networkObject : networkObjects)
        {
            if (clazz.isAssignableFrom(networkObject.getClass()))
            {
                list.add((T) networkObject);
            }
        }
        return list;
    }

    /**
     * Select object of given type by predicate.
     * @param networkObjects List&lt;Object&gt;; list of objects
     * @param clazz Class&lt;T&gt;; class of type of objects to return
     * @param predicate Predicate&lt;T&gt;; predicate
     * @param <T> type
     * @return (first) object of given type that matches the predicate
     */
    public static <T> T findObject(final List<Object> networkObjects, final Class<T> clazz, final Predicate<T> predicate)
    {
        for (Object networkObject : networkObjects)
        {
            if (clazz.isAssignableFrom(networkObject.getClass()))
            {
                @SuppressWarnings("unchecked")
                T t = (T) networkObject;
                if (predicate.test(t))
                {
                    return t;
                }
            }
        }
        throw new RuntimeException(String.format("Object of type %s could not be found.", clazz));
    }

    /**
     * @param args not used
     * @throws Exception on parsing error
     */
    public static void main(final String[] args) throws Exception
    {
        OTSSimulatorInterface simulator = new OTSSimulator();
        build("/N201v8.xml", new OTSNetwork(""), simulator);
        System.exit(0);
    }
}
