package org.opentrafficsim.road.network.factory.opendrive.parser;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.naming.NamingException;

import org.djunits.unit.DirectionUnit;
import org.djunits.value.vdouble.scalar.Direction;
import org.djutils.logger.CategoryLogger;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.draw.network.LinkAnimation;
import org.opentrafficsim.draw.network.NodeAnimation;
import org.opentrafficsim.opendrive.xml.generated.OpenDRIVE;
import org.opentrafficsim.opendrive.xml.generated.OpenDRIVE.Junction;
import org.opentrafficsim.opendrive.xml.generated.OpenDRIVE.Road;
import org.opentrafficsim.opendrive.xml.generated.OpenDRIVE.Road.PlanView.Geometry;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.factory.opendrive.OpenDriveParserException;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.OTSRoadNode;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;

import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * RoadParser.java.
 * <p>
 * Copyright (c) 2019-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://djunits.org/docs/license.html">DJUNITS License</a>.
 * <p>
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class RoadParser
{
    /** the XML structure for the xodr file. */
    private final OpenDRIVE openDrive;

    /** the network in which to write the information. */
    private final OTSRoadNetwork otsNetwork;

    /** the simulator. */
    private final OTSSimulatorInterface simulator;

    /** List of roads by id. */
    private Map<String, Road> roadMap = new LinkedHashMap<>();

    /** List of junctions by id. */
    private Map<String, Junction> junctionMap = new LinkedHashMap<>();

    /** OTSNodeMap. */
    private Map<DirectedPoint, OTSRoadNode> otsNodeMap = new LinkedHashMap<>();

    /**
     * @param openDrive the XML structure for the xodr file.
     * @param otsNetwork the network in which to write the information
     * @param simulator the simulator
     */
    public RoadParser(final OpenDRIVE openDrive, final OTSRoadNetwork otsNetwork, final OTSSimulatorInterface simulator)
    {
        this.openDrive = openDrive;
        this.otsNetwork = otsNetwork;
        this.simulator = simulator;
    }

    /**
     * Parse the roads in the OpenDrive XML structure.
     * @throws OpenDriveParserException on parser error
     * @throws NetworkException on network build error
     * @throws OTSGeometryException on invalid line
     */
    public void parseRoads() throws OpenDriveParserException, NetworkException, OTSGeometryException
    {
        for (Road road : this.openDrive.getRoad())
        {
            this.roadMap.put(road.getId(), road);
        }
        for (Junction junction : this.openDrive.getJunction())
        {
            this.junctionMap.put(junction.getId(), junction);
        }

        buildNodesLinks();

    }

    /**
     * Build the nodes and links in the network.
     * @throws NetworkException on error
     * @throws OTSGeometryException on error
     */
    private void buildNodesLinks() throws NetworkException, OTSGeometryException
    {
        for (Entry<String, Road> roadEntry : this.roadMap.entrySet())
        {
            Road road = roadEntry.getValue();
            List<Geometry> geometryList = road.getPlanView().getGeometry();
            OTSRoadNode startNode =
                    addNode(geometryList.get(0).getX(), geometryList.get(0).getY(), geometryList.get(0).getHdg());
            OTSRoadNode endNode = addNode(geometryList.get(geometryList.size() - 1).getX(),
                    geometryList.get(geometryList.size() - 1).getY(), geometryList.get(geometryList.size() - 1).getHdg());
            List<OTSPoint3D> pointList = new ArrayList<>();
            for (Geometry geometry : geometryList)
            {
                pointList.add(new OTSPoint3D(geometry.getX(), geometry.getY()));
            }
            try
            {
                OTSLine3D designLine = new OTSLine3D(pointList);
                CrossSectionLink otsLink = new CrossSectionLink(this.otsNetwork, roadEntry.getKey(), startNode, endNode,
                        this.otsNetwork.getLinkType(LinkType.DEFAULTS.ROAD), designLine, this.simulator,
                        LaneKeepingPolicy.KEEPLANE);
                this.otsNetwork.addLink(otsLink);
                new LinkAnimation(otsLink, this.simulator, 0.01f);
            }
            catch (NetworkException | RemoteException | NamingException | OTSGeometryException e)
            {
                CategoryLogger.always().warn(e.getMessage());
            }
        }
    }

    /**
     * Add or retrieve a node.
     * @param x the x coordinate
     * @param y the y coordinate
     * @param hdg the direction (heading)
     * @return an earlier stored node or a newly generated node
     * @throws NetworkException on error
     */
    private OTSRoadNode addNode(final double x, final double y, final double hdg) throws NetworkException
    {
        DirectedPoint id = new DirectedPoint(x, y, 0.0, 0.0, 0.0, hdg);
        OTSRoadNode node = this.otsNodeMap.get(id);
        if (node == null)
        {
            node = new OTSRoadNode(this.otsNetwork, id.toString(), new OTSPoint3D(x, y),
                    new Direction(hdg, DirectionUnit.EAST_DEGREE));
            this.otsNodeMap.put(id, node);
            try
            {
                this.otsNetwork.addNode(node);
                new NodeAnimation(node, this.simulator);
            }
            catch (NetworkException | RemoteException | NamingException e)
            {
                CategoryLogger.always().warn(e.getMessage());
            }
        }
        return node;
    }
}
