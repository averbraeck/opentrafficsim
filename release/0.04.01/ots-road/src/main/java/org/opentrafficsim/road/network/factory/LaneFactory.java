package org.opentrafficsim.road.network.factory;

import java.awt.Color;
import java.rmi.RemoteException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.naming.NamingException;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.dsol.OTSAnimatorInterface;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
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
     * @param intermediatePoints OTSPoint3D[]; array of intermediate coordinates (may be null)
     * @return Link; the newly constructed Link
     * @throws NetworkException when the design line is degenerate (only one point or duplicate point)
     */
    public static CrossSectionLink makeLink(final String name, final OTSNode from, final OTSNode to,
        final OTSPoint3D[] intermediatePoints) throws NetworkException
    {
        int coordinateCount = 2 + (null == intermediatePoints ? 0 : intermediatePoints.length);
        OTSPoint3D[] points = new OTSPoint3D[coordinateCount];
        points[0] = new OTSPoint3D(from.getPoint().x, from.getPoint().y, 0);
        points[points.length - 1] = new OTSPoint3D(to.getPoint().x, to.getPoint().y, 0);
        if (null != intermediatePoints)
        {
            for (int i = 0; i < intermediatePoints.length; i++)
            {
                points[i + 1] = new OTSPoint3D(intermediatePoints[i]);
            }
        }
        OTSLine3D designLine = new OTSLine3D(points);
        CrossSectionLink link = new CrossSectionLink(name, from, to, LinkType.ALL, designLine, LaneKeepingPolicy.KEEP_RIGHT);
        return link;
    }

    /**
     * Create one Lane.
     * @param link Link; the link that owns the new Lane
     * @param id String; the id of this lane, should be unique within the link
     * @param laneType LaneType&lt;String&gt;; the type of the new Lane
     * @param latPosAtStart DoubleScalar.Rel&lt;LengthUnit&gt;; the lateral position of the new Lane with respect to the design
     *            line of the link at the start of the link
     * @param latPosAtEnd DoubleScalar.Rel&lt;LengthUnit&gt;; the lateral position of the new Lane with respect to the design
     *            line of the link at the end of the link
     * @param width DoubleScalar.Rel&lt;LengthUnit&gt;; the width of the new Lane
     * @param speedLimit DoubleScalar.Abs&lt;SpeedUnit&gt;; the speed limit on the new Lane
     * @param simulator OTSDEVSSimulatorInterface; the simulator
     * @return Lane
     * @throws NamingException when names cannot be registered for animation
     * @throws NetworkException on network inconsistency
     * @throws OTSGeometryException when creation of center line or contour fails
     */
    @SuppressWarnings("checkstyle:parameternumber")
    private static Lane makeLane(final CrossSectionLink link, final String id, final LaneType laneType,
        final Length.Rel latPosAtStart, final Length.Rel latPosAtEnd, final Length.Rel width, final Speed speedLimit,
        final OTSDEVSSimulatorInterface simulator) throws NamingException, NetworkException,
        OTSGeometryException
    {
        Map<GTUType, LongitudinalDirectionality> directionalityMap = new LinkedHashMap<>();
        directionalityMap.put(GTUType.ALL, LongitudinalDirectionality.FORWARD);
        Map<GTUType, Speed> speedMap = new LinkedHashMap<>();
        speedMap.put(GTUType.ALL, speedLimit);
        Lane result = new Lane(link, id, latPosAtStart, latPosAtEnd, width, width, laneType, directionalityMap, speedMap,
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
     * @param intermediatePoints OTSPoint3D[]; intermediate coordinates or null to create a straight road
     * @param laneType LaneType; type of the new Lane
     * @param speedLimit DoubleScalar.Abs&lt;SpeedUnit&gt;; the speed limit on the new Lane
     * @param simulator OTSDEVSSimulatorInterface; the simulator
     * @return Lane; the new Lane
     * @throws NamingException when names cannot be registered for animation
     * @throws NetworkException on network inconsistency
     * @throws OTSGeometryException when creation of center line or contour fails
     */
    public static Lane makeLane(final String name, final OTSNode from, final OTSNode to,
        final OTSPoint3D[] intermediatePoints, final LaneType laneType, final Speed speedLimit,
        final OTSDEVSSimulatorInterface simulator) throws NamingException, NetworkException,
        OTSGeometryException
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
     * @param intermediatePoints OTSPoint3D[]; intermediate coordinates or null to create a straight road
     * @param laneCount int; number of lanes in the road
     * @param laneOffsetAtStart int; extra offset from design line in lane widths at start of link
     * @param laneOffsetAtEnd int; extra offset from design line in lane widths at end of link
     * @param laneType LaneType; type of the new Lanes
     * @param speedLimit DoubleScalar.Abs&lt;SpeedUnit&gt;; the speed limit on all lanes
     * @param simulator OTSDEVSSimulatorInterface; the simulator
     * @return Lane&lt;String, String&gt;[]; array containing the new Lanes
     * @throws NamingException when names cannot be registered for animation
     * @throws NetworkException on topological problems
     * @throws OTSGeometryException when creation of center line or contour fails
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public static Lane[] makeMultiLane(final String name, final OTSNode from, final OTSNode to,
        final OTSPoint3D[] intermediatePoints, final int laneCount, final int laneOffsetAtStart, final int laneOffsetAtEnd,
        final LaneType laneType, final Speed speedLimit, final OTSDEVSSimulatorInterface simulator)
        throws NamingException, NetworkException, OTSGeometryException
    {
        final CrossSectionLink link = makeLink(name, from, to, intermediatePoints);
        Lane[] result = new Lane[laneCount];
        Length.Rel width = new Length.Rel(4.0, LengthUnit.METER);
        for (int laneIndex = 0; laneIndex < laneCount; laneIndex++)
        {
            // Be ware! LEFT is lateral positive, RIGHT is lateral negative.
            Length.Rel latPosAtStart = new Length.Rel((-0.5 - laneIndex - laneOffsetAtStart) * width.getSI(), LengthUnit.SI);
            Length.Rel latPosAtEnd = new Length.Rel((-0.5 - laneIndex - laneOffsetAtEnd) * width.getSI(), LengthUnit.SI);
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
     * @param intermediatePoints OTSPoint3D[]; intermediate coordinates or null to create a straight road
     * @param laneCount int; number of lanes in the road
     * @param laneType LaneType; type of the new Lanes
     * @param speedLimit DoubleScalar.Abs&lt;SpeedUnit&gt; the speed limit (applies to all generated lanes)
     * @param simulator OTSDEVSSimulatorInterface; the simulator
     * @return Lane&lt;String, String&gt;[]; array containing the new Lanes
     * @throws NamingException when names cannot be registered for animation
     * @throws NetworkException on topological problems
     * @throws OTSGeometryException when creation of center line or contour fails
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public static Lane[] makeMultiLane(final String name, final OTSNode from, final OTSNode to,
        final OTSPoint3D[] intermediatePoints, final int laneCount, final LaneType laneType, final Speed speedLimit,
        final OTSDEVSSimulatorInterface simulator) throws NamingException, NetworkException,
        OTSGeometryException
    {
        return makeMultiLane(name, from, to, intermediatePoints, laneCount, 0, 0, laneType, speedLimit, simulator);
    }

}
