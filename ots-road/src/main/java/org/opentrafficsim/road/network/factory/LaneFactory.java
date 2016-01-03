package org.opentrafficsim.road.network.factory;

import java.awt.Color;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.dsol.OTSAnimatorInterface;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.geometry.Bezier;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.road.network.animation.LaneAnimation;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;
import org.opentrafficsim.road.network.lane.changing.OvertakingConditions;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-09-16 19:20:07 +0200 (Wed, 16 Sep 2015) $, @version $Revision: 1405 $, by $Author: averbraeck $,
 * initial version 30 okt. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public final class LaneFactory
{
    /** Do not instantiate this class. */
    private LaneFactory()
    {
        // Cannot be instantiated.
    }

    /**
     * Create a Link along intermediate coordinates from one Node to another.
     * @param name String; name of the new Link
     * @param from Node; start Node of the new Link
     * @param to Node; end Node of the new Link
     * @param intermediatePoints OTSPoint3D[]; array of intermediate coordinates (may be null); the intermediate points may
     *            contain the coordinates of the from node and to node
     * @return Link; the newly constructed Link
     * @throws OTSGeometryException when the design line is degenerate (only one point or duplicate point)
     */
    public static CrossSectionLink makeLink(final String name, final OTSNode from, final OTSNode to,
        final OTSPoint3D[] intermediatePoints) throws OTSGeometryException
    {
        List<OTSPoint3D> pointList =
            intermediatePoints == null ? new ArrayList<OTSPoint3D>() : new ArrayList<OTSPoint3D>(
                Arrays.asList(intermediatePoints));
        if (pointList.size() == 0 || !from.getPoint().equals(pointList.get(0)))
        {
            pointList.add(0, from.getPoint());
        }
        if (pointList.size() == 0 || !to.getPoint().equals(pointList.get(pointList.size() - 1)))
        {
            pointList.add(to.getPoint());
        }

        /*-
        // see if an intermediate point needs to be created to the start of the link in the right direction
        OTSPoint3D s1 = pointList.get(0);
        OTSPoint3D s2 = pointList.get(1);
        double dy = s2.y - s1.y;
        double dx = s2.x - s1.x;
        double a = from.getLocation().getRotZ();
        if (Math.abs(a - Math.atan2(dy, dx)) > 1E-6)
        {
            double r = Math.min(1.0, Math.sqrt(dy * dy + dx * dx) / 4.0); 
            OTSPoint3D extra = new OTSPoint3D(s1.x + r * Math.cos(a), s1.y + r * Math.sin(a), s1.z);
            pointList.add(1, extra);
        }
        
        // see if an intermediate point needs to be created to the end of the link in the right direction
        s1 = pointList.get(pointList.size() - 2);
        s2 = pointList.get(pointList.size() - 1);
        dy = s2.y - s1.y;
        dx = s2.x - s1.x;
        a = to.getLocation().getRotZ() - Math.PI;
        if (Math.abs(a - Math.atan2(dy, dx)) > 1E-6)
        {
            double r = Math.min(1.0, Math.sqrt(dy * dy + dx * dx) / 4.0); 
            OTSPoint3D extra = new OTSPoint3D(s2.x + r * Math.cos(a), s2.y + r * Math.sin(a), s2.z);
            pointList.add(pointList.size() - 2, extra);
        }
        */
        
        OTSLine3D designLine = new OTSLine3D(pointList);
        CrossSectionLink link =
            new CrossSectionLink(name, from, to, LinkType.ALL, designLine, LongitudinalDirectionality.DIR_PLUS,
                LaneKeepingPolicy.KEEP_RIGHT);
        return link;
    }

    /**
     * Create one Lane.
     * @param link Link; the link that owns the new Lane
     * @param id String; the id of this lane, should be unique within the link
     * @param laneType LaneType&lt;String&gt;; the type of the new Lane
     * @param latPosAtStart Length.Rel; the lateral position of the new Lane with respect to the design line of the link at the
     *            start of the link
     * @param latPosAtEnd Length.Rel; the lateral position of the new Lane with respect to the design line of the link at the
     *            end of the link
     * @param width Length.Rel; the width of the new Lane
     * @param speedLimit Speed; the speed limit on the new Lane
     * @param simulator OTSDEVSSimulatorInterface; the simulator
     * @return Lane
     * @throws NamingException when names cannot be registered for animation
     * @throws NetworkException on network inconsistency
     * @throws OTSGeometryException when creation of center line or contour fails
     */
    @SuppressWarnings("checkstyle:parameternumber")
    private static Lane makeLane(final CrossSectionLink link, final String id, final LaneType laneType,
        final Length.Rel latPosAtStart, final Length.Rel latPosAtEnd, final Length.Rel width, final Speed speedLimit,
        final OTSDEVSSimulatorInterface simulator) throws NamingException, NetworkException, OTSGeometryException
    {
        Map<GTUType, LongitudinalDirectionality> directionalityMap = new LinkedHashMap<>();
        directionalityMap.put(GTUType.ALL, LongitudinalDirectionality.DIR_PLUS);
        Map<GTUType, Speed> speedMap = new LinkedHashMap<>();
        speedMap.put(GTUType.ALL, speedLimit);
        Lane result =
            new Lane(link, id, latPosAtStart, latPosAtEnd, width, width, laneType, directionalityMap, speedMap,
                new OvertakingConditions.LeftAndRight());
        if (simulator instanceof OTSAnimatorInterface)
        {
            try
            {
                new LaneAnimation(result, simulator, Color.LIGHT_GRAY);
            }
            catch (RemoteException exception)
            {
                exception.printStackTrace();
            }
        }
        return result;
    }

    /**
     * Create a simple Lane.
     * @param name String; name of the Lane (and also of the Link that owns it)
     * @param from Node; starting node of the new Lane
     * @param to Node; ending node of the new Lane
     * @param intermediatePoints OTSPoint3D[]; intermediate coordinates or null to create a straight road; the intermediate
     *            points may contain the coordinates of the from node and to node
     * @param laneType LaneType; type of the new Lane
     * @param speedLimit Speed; the speed limit on the new Lane
     * @param simulator OTSDEVSSimulatorInterface; the simulator
     * @return Lane; the new Lane
     * @throws NamingException when names cannot be registered for animation
     * @throws NetworkException on network inconsistency
     * @throws OTSGeometryException when creation of center line or contour fails
     */
    public static Lane makeLane(final String name, final OTSNode from, final OTSNode to,
        final OTSPoint3D[] intermediatePoints, final LaneType laneType, final Speed speedLimit,
        final OTSDEVSSimulatorInterface simulator) throws NamingException, NetworkException, OTSGeometryException
    {
        Length.Rel width = new Length.Rel(4.0, LengthUnit.METER);
        final CrossSectionLink link = makeLink(name, from, to, intermediatePoints);
        Length.Rel latPos = new Length.Rel(0.0, LengthUnit.METER);
        return makeLane(link, "lane", laneType, latPos, latPos, width, speedLimit, simulator);
    }

    /**
     * Create a simple road with the specified number of Lanes.<br>
     * This method returns an array of Lane. These lanes are embedded in a Link that can be accessed through the getParentLink
     * method of the Lane.
     * @param name String; name of the Link
     * @param from Node; starting node of the new Lane
     * @param to Node; ending node of the new Lane
     * @param intermediatePoints OTSPoint3D[]; intermediate coordinates or null to create a straight road; the intermediate
     *            points may contain the coordinates of the from node and to node
     * @param laneCount int; number of lanes in the road
     * @param laneOffsetAtStart int; extra offset from design line in lane widths at start of link
     * @param laneOffsetAtEnd int; extra offset from design line in lane widths at end of link
     * @param laneType LaneType; type of the new Lanes
     * @param speedLimit Speed; the speed limit on all lanes
     * @param simulator OTSDEVSSimulatorInterface; the simulator
     * @return Lane&lt;String, String&gt;[]; array containing the new Lanes
     * @throws NamingException when names cannot be registered for animation
     * @throws NetworkException on topological problems
     * @throws OTSGeometryException when creation of center line or contour fails
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public static Lane[] makeMultiLane(final String name, final OTSNode from, final OTSNode to,
        final OTSPoint3D[] intermediatePoints, final int laneCount, final int laneOffsetAtStart,
        final int laneOffsetAtEnd, final LaneType laneType, final Speed speedLimit,
        final OTSDEVSSimulatorInterface simulator) throws NamingException, NetworkException, OTSGeometryException
    {
        final CrossSectionLink link = makeLink(name, from, to, intermediatePoints);
        Lane[] result = new Lane[laneCount];
        Length.Rel width = new Length.Rel(4.0, LengthUnit.METER);
        for (int laneIndex = 0; laneIndex < laneCount; laneIndex++)
        {
            // Be ware! LEFT is lateral positive, RIGHT is lateral negative.
            Length.Rel latPosAtStart =
                new Length.Rel((-0.5 - laneIndex - laneOffsetAtStart) * width.getSI(), LengthUnit.SI);
            Length.Rel latPosAtEnd =
                new Length.Rel((-0.5 - laneIndex - laneOffsetAtEnd) * width.getSI(), LengthUnit.SI);
            result[laneIndex] =
                makeLane(link, "lane." + laneIndex, laneType, latPosAtStart, latPosAtEnd, width, speedLimit, simulator);
        }
        return result;
    }

    /**
     * Create a simple road with the specified number of Lanes.<br>
     * This method returns an array of Lane. These lanes are embedded in a Link that can be accessed through the getParentLink
     * method of the Lane.
     * @param name String; name of the Link
     * @param from Node; starting node of the new Lane
     * @param to Node; ending node of the new Lane
     * @param intermediatePoints OTSPoint3D[]; intermediate coordinates or null to create a straight road; the intermediate
     *            points may contain the coordinates of the from node and to node
     * @param laneCount int; number of lanes in the road
     * @param laneType LaneType; type of the new Lanes
     * @param speedLimit Speed the speed limit (applies to all generated lanes)
     * @param simulator OTSDEVSSimulatorInterface; the simulator
     * @return Lane&lt;String, String&gt;[]; array containing the new Lanes
     * @throws NamingException when names cannot be registered for animation
     * @throws NetworkException on topological problems
     * @throws OTSGeometryException when creation of center line or contour fails
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public static Lane[] makeMultiLane(final String name, final OTSNode from, final OTSNode to,
        final OTSPoint3D[] intermediatePoints, final int laneCount, final LaneType laneType, final Speed speedLimit,
        final OTSDEVSSimulatorInterface simulator) throws NamingException, NetworkException, OTSGeometryException
    {
        return makeMultiLane(name, from, to, intermediatePoints, laneCount, 0, 0, laneType, speedLimit, simulator);
    }
    
    /**
     * Create a simple road with the specified number of Lanes, based on a Bezier curve.<br>
     * This method returns an array of Lane. These lanes are embedded in a Link that can be accessed through the getParentLink
     * method of the Lane.
     * @param name String; name of the Link
     * @param n1 Node; control node for the start direction
     * @param n2 Node; starting node of the new Lane
     * @param n3 Node; ending node of the new Lane
     * @param n4 Node; control node for the end direction
     * @param laneCount int; number of lanes in the road
     * @param laneOffsetAtStart int; extra offset from design line in lane widths at start of link
     * @param laneOffsetAtEnd int; extra offset from design line in lane widths at end of link
     * @param laneType LaneType; type of the new Lanes
     * @param speedLimit Speed; the speed limit on all lanes
     * @param simulator OTSDEVSSimulatorInterface; the simulator
     * @return Lane&lt;String, String&gt;[]; array containing the new Lanes
     * @throws NamingException when names cannot be registered for animation
     * @throws NetworkException on topological problems
     * @throws OTSGeometryException when creation of center line or contour fails
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public static Lane[] makeMultiLaneBezier(final String name, final OTSNode n1, final OTSNode n2, final OTSNode n3, final OTSNode n4,
        final int laneCount, final int laneOffsetAtStart,
        final int laneOffsetAtEnd, final LaneType laneType, final Speed speedLimit,
        final OTSDEVSSimulatorInterface simulator) throws NamingException, NetworkException, OTSGeometryException
    {
        OTSLine3D bezier = makeBezier(n1, n2, n3, n4);
        final CrossSectionLink link = makeLink(name, n2, n3, bezier.getPoints());
        Lane[] result = new Lane[laneCount];
        Length.Rel width = new Length.Rel(4.0, LengthUnit.METER);
        for (int laneIndex = 0; laneIndex < laneCount; laneIndex++)
        {
            // Be ware! LEFT is lateral positive, RIGHT is lateral negative.
            Length.Rel latPosAtStart =
                new Length.Rel((-0.5 - laneIndex - laneOffsetAtStart) * width.getSI(), LengthUnit.SI);
            Length.Rel latPosAtEnd =
                new Length.Rel((-0.5 - laneIndex - laneOffsetAtEnd) * width.getSI(), LengthUnit.SI);
            result[laneIndex] =
                makeLane(link, "lane." + laneIndex, laneType, latPosAtStart, latPosAtEnd, width, speedLimit, simulator);
        }
        return result;
    }

    /**
     * @param n1 node 1
     * @param n2 node 2
     * @param n3 node 3
     * @param n4 node 4
     * @return line between n2 and n3 with start-direction n1--&gt;n2 and end-direction n3--&gt;n4
     * @throws OTSGeometryException on failure of Bezier curve creation
     */
    public static OTSLine3D makeBezier(final OTSNode n1, final OTSNode n2, final OTSNode n3, final OTSNode n4)
        throws OTSGeometryException
    {
        OTSPoint3D p1 = n1.getPoint();
        OTSPoint3D p2 = n2.getPoint();
        OTSPoint3D p3 = n3.getPoint();
        OTSPoint3D p4 = n4.getPoint();
        DirectedPoint dp1 = new DirectedPoint(p2.x, p2.y, p2.z, 0.0, 0.0, Math.atan2(p2.y - p1.y, p2.x - p1.x));
        DirectedPoint dp2 = new DirectedPoint(p3.x, p3.y, p3.z, 0.0, 0.0, Math.atan2(p4.y - p3.y, p4.x - p3.x));
        return Bezier.cubic(dp1, dp2);
    }

}
