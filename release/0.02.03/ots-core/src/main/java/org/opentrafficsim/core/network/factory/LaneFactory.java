package org.opentrafficsim.core.network.factory;

import java.awt.Color;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.opentrafficsim.core.dsol.OTSAnimatorInterface;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.core.network.animation.LaneAnimation;
import org.opentrafficsim.core.network.lane.CrossSectionLink;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.network.lane.LaneType;
import org.opentrafficsim.core.unit.FrequencyUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
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
     */
    public static CrossSectionLink<String, String> makeLink(final String name, final OTSNode<String> from,
        final OTSNode<String> to, final OTSPoint3D[] intermediatePoints)
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
        CrossSectionLink<String, String> link = new CrossSectionLink<String, String>(name, from, to, designLine);
        // XXX: new LinearGeometry(link, lineString, null);
        return link;
    }

    /**
     * Create one Lane.
     * @param link Link; the link that owns the new Lane
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
     * @throws RemoteException on communications failure
     * @throws NetworkException on network inconsistency
     * @throws OTSGeometryException when creation of center line or contour fails
     */
    @SuppressWarnings("checkstyle:parameternumber")
    private static Lane<String, String> makeLane(final CrossSectionLink<String, String> link,
        final LaneType<String> laneType, final DoubleScalar.Rel<LengthUnit> latPosAtStart,
        final DoubleScalar.Rel<LengthUnit> latPosAtEnd, final DoubleScalar.Rel<LengthUnit> width,
        final DoubleScalar.Abs<SpeedUnit> speedLimit, final OTSDEVSSimulatorInterface simulator) throws RemoteException,
        NamingException, NetworkException, OTSGeometryException
    {
        DoubleScalar.Abs<FrequencyUnit> f2000 = new DoubleScalar.Abs<FrequencyUnit>(2000.0, FrequencyUnit.PER_HOUR);
        Lane<String, String> result =
            new Lane<String, String>(link, latPosAtStart, latPosAtEnd, width, width, laneType,
                LongitudinalDirectionality.FORWARD, f2000, speedLimit);
        if (simulator instanceof OTSAnimatorInterface)
        {
            new LaneAnimation(result, simulator, Color.LIGHT_GRAY);
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
     * @throws RemoteException on communications failure
     * @throws NetworkException on network inconsistency
     * @throws OTSGeometryException when creation of center line or contour fails
     */
    public static Lane<String, String> makeLane(final String name, final OTSNode<String> from, final OTSNode<String> to,
        final OTSPoint3D[] intermediatePoints, final LaneType<String> laneType,
        final DoubleScalar.Abs<SpeedUnit> speedLimit, final OTSDEVSSimulatorInterface simulator) throws RemoteException,
        NamingException, NetworkException, OTSGeometryException
    {
        DoubleScalar.Rel<LengthUnit> width = new DoubleScalar.Rel<LengthUnit>(4.0, LengthUnit.METER);
        final CrossSectionLink<String, String> link = makeLink(name, from, to, intermediatePoints);
        DoubleScalar.Rel<LengthUnit> latPos = new DoubleScalar.Rel<LengthUnit>(0.0, LengthUnit.METER);
        return makeLane(link, laneType, latPos, latPos, width, speedLimit, simulator);
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
     * @throws RemoteException on communications failure
     * @throws NetworkException on topological problems
     * @throws OTSGeometryException when creation of center line or contour fails
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public static Lane<String, String>[] makeMultiLane(final String name, final OTSNode<String> from,
        final OTSNode<String> to, final OTSPoint3D[] intermediatePoints, final int laneCount, final int laneOffsetAtStart,
        final int laneOffsetAtEnd, final LaneType<String> laneType, final DoubleScalar.Abs<SpeedUnit> speedLimit,
        final OTSDEVSSimulatorInterface simulator) throws RemoteException, NamingException, NetworkException,
        OTSGeometryException
    {
        final CrossSectionLink<String, String> link = makeLink(name, from, to, intermediatePoints);
        Lane<String, String>[] result = new Lane[laneCount];
        DoubleScalar.Rel<LengthUnit> width = new DoubleScalar.Rel<LengthUnit>(4.0, LengthUnit.METER);
        for (int laneIndex = 0; laneIndex < laneCount; laneIndex++)
        {
            // Be ware! LEFT is lateral positive, RIGHT is lateral negative.
            DoubleScalar.Rel<LengthUnit> latPosAtStart =
                new DoubleScalar.Rel<LengthUnit>((-0.5 - laneIndex - laneOffsetAtStart) * width.getSI(), LengthUnit.METER);
            DoubleScalar.Rel<LengthUnit> latPosAtEnd =
                new DoubleScalar.Rel<LengthUnit>((-0.5 - laneIndex - laneOffsetAtEnd) * width.getSI(), LengthUnit.METER);
            result[laneIndex] = makeLane(link, laneType, latPosAtStart, latPosAtEnd, width, speedLimit, simulator);
        }
        // Make lanes adjacent in their natural order
        for (int laneIndex = 1; laneIndex < laneCount; laneIndex++)
        {
            result[laneIndex - 1].addAccessibleAdjacentLane(result[laneIndex], LateralDirectionality.RIGHT);
            result[laneIndex].addAccessibleAdjacentLane(result[laneIndex - 1], LateralDirectionality.LEFT);
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
     * @throws RemoteException on communications failure
     * @throws NetworkException on topological problems
     * @throws OTSGeometryException when creation of center line or contour fails
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public static Lane<String, String>[] makeMultiLane(final String name, final OTSNode<String> from,
        final OTSNode<String> to, final OTSPoint3D[] intermediatePoints, final int laneCount,
        final LaneType<String> laneType, final DoubleScalar.Abs<SpeedUnit> speedLimit,
        final OTSDEVSSimulatorInterface simulator) throws RemoteException, NamingException, NetworkException,
        OTSGeometryException
    {
        return makeMultiLane(name, from, to, intermediatePoints, laneCount, 0, 0, laneType, speedLimit, simulator);
    }

}