package org.opentrafficsim.road.network.lane;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.djunits.unit.DirectionUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.Point2d;
import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.junit.Test;
import org.mockito.Mockito;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.dsol.OtsReplication;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.geometry.OtsLine2d;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.perception.HistoryManagerDevs;
import org.opentrafficsim.road.definitions.DefaultsRoadNl;
import org.opentrafficsim.road.mock.MockDevsSimulator;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;
import org.opentrafficsim.road.network.lane.conflict.Conflict;
import org.opentrafficsim.road.network.lane.conflict.ConflictType;
import org.opentrafficsim.road.network.lane.conflict.DefaultConflictRule;

/**
 * Test the Conflict class.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class ConflictTest implements EventListener
{
    /** ... */
    private static final long serialVersionUID = 20200708L;

    /** Storage for received events. */
    private List<Event> collectedEvents = new ArrayList<>();

    /**
     * Test the Conflict class.
     * @throws NetworkException on error
     * @throws OtsGeometryException on error
     */
    @Test
    public void testConstructor() throws NetworkException, OtsGeometryException
    {
        OtsSimulatorInterface simulator = MockDevsSimulator.createMock();
        OtsReplication replication = Mockito.mock(OtsReplication.class);
        HistoryManagerDevs hmd = Mockito.mock(HistoryManagerDevs.class);
        Mockito.when(hmd.now()).thenReturn(Time.ZERO);
        Mockito.when(replication.getHistoryManager(simulator)).thenReturn(hmd);
        Mockito.when(simulator.getReplication()).thenReturn(replication);
        Mockito.when(simulator.getSimulatorAbsTime()).thenReturn(Time.ZERO);
        Mockito.when(simulator.getSimulatorTime()).thenReturn(Duration.ZERO);
        RoadNetwork network = new RoadNetwork("Network for conflict test", simulator);
        LinkType linkType = DefaultsNl.ROAD;
        LaneType laneType = DefaultsRoadNl.ONE_WAY_LANE;
        Point2d pointAFrom = new Point2d(0, 0);
        Node nodeAFrom = new Node(network, "A from", pointAFrom, Direction.ZERO);
        Point2d pointATo = new Point2d(100, 0);
        Node nodeATo = new Node(network, "A to", pointATo, Direction.ZERO);
        CrossSectionLink linkA = new CrossSectionLink(network, "Link A", nodeAFrom, nodeATo, linkType,
                new OtsLine2d(pointAFrom, pointATo), null, LaneKeepingPolicy.KEEPRIGHT);
        Lane laneA = LaneGeometryUtil.createStraightLane(linkA, "lane A", Length.ZERO, new Length(2, LengthUnit.METER),
                laneType, Map.of(DefaultsNl.VEHICLE, new Speed(50, SpeedUnit.KM_PER_HOUR)));
        laneA.addListener(this, Lane.OBJECT_ADD_EVENT);

        Point2d pointBFrom = new Point2d(30, -15);
        Point2d pointBTo = new Point2d(60, 60);
        Direction bDirection =
                new Direction(Math.atan2(pointBTo.y - pointBFrom.y, pointBTo.x - pointBFrom.x), DirectionUnit.EAST_RADIAN);
        Node nodeBFrom = new Node(network, "B from", pointBFrom, bDirection);
        Node nodeBTo = new Node(network, "B to", pointBTo, bDirection);
        CrossSectionLink linkB = new CrossSectionLink(network, "Link B", nodeBFrom, nodeBTo, linkType,
                new OtsLine2d(pointBFrom, pointBTo), null, LaneKeepingPolicy.KEEPRIGHT);
        Lane laneB = LaneGeometryUtil.createStraightLane(linkB, "lane B", Length.ZERO, new Length(4, LengthUnit.METER),
                laneType, Map.of(DefaultsNl.VEHICLE, new Speed(50, SpeedUnit.KM_PER_HOUR)));
        laneB.addListener(this, Lane.OBJECT_ADD_EVENT);
        // The intersection of the link design lines is at 50, 0
        System.out.print(laneA.getContour().toPlot());
        System.out.print(laneB.getContour().toPlot());
        System.out.println("c0,1,0");
        System.out.print(laneA.getCenterLine().toPlot());
        System.out.print(laneB.getCenterLine().toPlot());
        System.out.println("c1,0,0");

        // Find out where the conflict area starts. With acute angles this is the point closest to pointAFrom among the
        // intersections of the lane contours. Similar for conflict area end.
        Point2d conflictStart = null;
        double closestDistance = Double.MAX_VALUE;
        Point2d conflictEnd = null;
        double furthestDistance = 0.0;
        for (Point2d intersection : intersections(laneA.getContour(), laneB.getContour()))
        {
            double distance = pointAFrom.distance(intersection);
            if (distance < closestDistance)
            {
                conflictStart = intersection;
                closestDistance = distance;
            }
            if (distance > furthestDistance)
            {
                conflictEnd = intersection;
                furthestDistance = distance;
            }
        }
        // System.out.println(conflictStart);
        // System.out.println(conflictEnd);

        // Next statements pretend that vehicle width equals lane width.
        PolyLine2d geometry1 =
                new PolyLine2d(conflictStart, conflictEnd, new Point2d(conflictStart.x, conflictEnd.y), conflictStart);
        System.out.print(geometry1.toPlot());
        PolyLine2d geometry2 = new PolyLine2d(conflictStart,
                new Point2d(conflictStart.x + laneB.getWidth(0).si * Math.sin(bDirection.si),
                        conflictStart.y - laneB.getWidth(0).si * Math.cos(bDirection.si)),
                conflictEnd, new Point2d(conflictEnd.x - laneB.getWidth(0).si * Math.sin(bDirection.si),
                        conflictEnd.y + laneB.getWidth(0).si * Math.cos(bDirection.si)),
                conflictStart);
        System.out.print(geometry2.toPlot());

        System.out.println("#angle B:           " + bDirection.toString(DirectionUnit.EAST_DEGREE));
        Length conflictBStart =
                new Length(pointBFrom.distance(new Point2d(conflictStart.x + laneB.getWidth(0).si / 2 * Math.sin(bDirection.si),
                        conflictStart.y - laneB.getWidth(0).si / 2 * Math.cos(bDirection.si))), LengthUnit.SI);
        System.out.println("#conflict B start:  " + conflictBStart);
        Length conflictBLength = new Length(
                laneA.getWidth(0).si / Math.sin(bDirection.si) + laneB.getWidth(0).si / Math.tan(bDirection.si), LengthUnit.SI);
        System.out.println("#conflict B length: " + conflictBLength);
        System.out.println("c0,0,1");
        System.out.println(String.format("M%.3f,%.3f <%f l%f,0", pointBFrom.x, pointBFrom.y, Math.toDegrees(bDirection.si),
                conflictBStart.si));
        System.out.println(String.format("c0,0,0 l%f,0", conflictBLength.si));

        assertEquals("not events received yet", 0, this.collectedEvents.size());

        // That was a lot of code - just to prepare things to call generateConflictPair ...
        Conflict.generateConflictPair(ConflictType.CROSSING, new DefaultConflictRule(), false, laneA,
                new Length(conflictStart.x, LengthUnit.SI), new Length(conflictEnd.x - conflictStart.x, LengthUnit.SI),
                geometry1, laneB, conflictBStart, conflictBLength, geometry2, simulator);

        // Check that two conflicts have been created
        assertEquals("one conflict on lane A", 1, laneA.getLaneBasedObjects().size());
        assertEquals("one conflict on lane B", 1, laneB.getLaneBasedObjects().size());
        // Get the Conflicts
        Conflict conflictA = (Conflict) laneA.getLaneBasedObjects().get(0);
        System.out.println("Conflict A: " + conflictA);
        Conflict conflictB = (Conflict) laneB.getLaneBasedObjects().get(0);
        System.out.println("Conflict B: " + conflictB);

        assertEquals("the conflicts are each others counter part", conflictA, conflictB.getOtherConflict());
        assertEquals("the conflicts are each others counter part", conflictB, conflictA.getOtherConflict());
        assertEquals("longitudinal position", new Length(conflictStart.x, LengthUnit.SI), conflictA.getLongitudinalPosition());
        assertEquals("longitudinal position", conflictBStart, conflictB.getLongitudinalPosition());
        assertEquals("length", new Length(conflictEnd.x - conflictStart.x, LengthUnit.SI), conflictA.getLength());
        assertEquals("length", conflictBLength, conflictB.getLength());
        assertEquals("geometry", geometry1, conflictA.getGeometry());
        assertEquals("geometry", geometry2, conflictB.getGeometry());
        assertTrue("conflict rule", conflictA.getConflictRule() instanceof DefaultConflictRule);
        assertTrue("conflict rule", conflictB.getConflictRule() instanceof DefaultConflictRule);
        assertFalse("conflict A is not permitted", conflictA.isPermitted());
        assertFalse("conflict B is not permitted", conflictB.isPermitted());
        assertEquals("construction of two conflicts has generated two events", 2, this.collectedEvents.size());
        // Not checking the contents of those events; these are subject to change; as they indirectly link to the Network

    }

    /**
     * Find all 2D (ignoring Z) intersections between two OtsLine2d objects.
     * @param a Polygon2d; the first polyline
     * @param b Polygon2d; the second polyline
     * @return Set&lt;Point2d&gt;; the intersections
     */
    public Set<Point2d> intersections(final Polygon2d a, final Polygon2d b)
    {
        // TODO discuss if this method should be moved into the OtsLine2d class
        Set<Point2d> result = new LinkedHashSet<>();
        Point2d prevA = null;
        for (Point2d nextA : a.getPointList())
        {
            if (null != prevA)
            {
                Point2d prevB = null;
                for (Point2d nextB : b.getPointList())
                {
                    if (null != prevB)
                    {
                        Point2d intersection = Point2d.intersectionOfLineSegments(prevA, nextA, prevB, nextB);
                        if (null != intersection)
                        {
                            result.add(intersection);
                        }
                    }
                    prevB = nextB;
                }
            }
            prevA = nextA;
        }
        return result;
    }

    @Override
    public final void notify(final Event event) throws RemoteException
    {
        // System.out.println("received event " + event);
        this.collectedEvents.add(event);
    }

}
