package org.opentrafficsim.road.network.lane;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.djutils.draw.Export;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.Point2d;
import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opentrafficsim.base.geometry.OtsLine2d;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.dsol.OtsReplication;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.perception.HistoryManagerDevs;
import org.opentrafficsim.road.definitions.DefaultsRoadNl;
import org.opentrafficsim.road.mock.MockDevsSimulator;
import org.opentrafficsim.road.network.LaneKeepingPolicy;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.lane.conflict.Conflict;
import org.opentrafficsim.road.network.lane.conflict.ConflictType;
import org.opentrafficsim.road.network.lane.conflict.DefaultConflictRule;

/**
 * Test the Conflict class.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public final class ConflictTest implements EventListener
{
    /** Verbose test. */
    private static final boolean VERBOSE = false;

    /** Storage for received events. */
    private List<Event> collectedEvents = new ArrayList<>();

    /** */
    private ConflictTest()
    {
        // do not instantiate test class
    }

    /**
     * Test the Conflict class.
     * @throws NetworkException on error
     */
    @Test
    public void testConstructor() throws NetworkException
    {
        OtsSimulatorInterface simulator = MockDevsSimulator.createMock();
        OtsReplication replication = Mockito.mock(OtsReplication.class);
        HistoryManagerDevs hmd = Mockito.mock(HistoryManagerDevs.class);
        Mockito.when(hmd.now()).thenReturn(Duration.ZERO);
        Mockito.when(replication.getHistoryManager(simulator)).thenReturn(hmd);
        Mockito.when(simulator.getReplication()).thenReturn(replication);
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
        if (VERBOSE)
        {
            System.out.print(Export.toPlot(laneA.getAbsoluteContour()));
            System.out.print(Export.toPlot(laneB.getAbsoluteContour()));
            System.out.println("c0,1,0");
            System.out.print(Export.toPlot(laneA.getCenterLine()));
            System.out.print(Export.toPlot(laneB.getCenterLine()));
            System.out.println("c1,0,0");
        }

        // Find out where the conflict area starts. With acute angles this is the point closest to pointAFrom among the
        // intersections of the lane contours. Similar for conflict area end.
        Point2d conflictStart = null;
        double closestDistance = Double.MAX_VALUE;
        Point2d conflictEnd = null;
        double furthestDistance = 0.0;
        for (Point2d intersection : intersections(laneA.getAbsoluteContour(), laneB.getAbsoluteContour()))
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
        Polygon2d geometry1 =
                new Polygon2d(conflictStart, conflictEnd, new Point2d(conflictStart.x, conflictEnd.y), conflictStart);

        Polygon2d geometry2 = new Polygon2d(conflictStart,
                new Point2d(conflictStart.x + laneB.getWidth(0).si * Math.sin(bDirection.si),
                        conflictStart.y - laneB.getWidth(0).si * Math.cos(bDirection.si)),
                conflictEnd, new Point2d(conflictEnd.x - laneB.getWidth(0).si * Math.sin(bDirection.si),
                        conflictEnd.y + laneB.getWidth(0).si * Math.cos(bDirection.si)),
                conflictStart);

        Length conflictBStart =
                new Length(pointBFrom.distance(new Point2d(conflictStart.x + laneB.getWidth(0).si / 2 * Math.sin(bDirection.si),
                        conflictStart.y - laneB.getWidth(0).si / 2 * Math.cos(bDirection.si))), LengthUnit.SI);

        Length conflictBLength = new Length(
                laneA.getWidth(0).si / Math.sin(bDirection.si) + laneB.getWidth(0).si / Math.tan(bDirection.si), LengthUnit.SI);

        if (VERBOSE)
        {
            System.out.print(Export.toPlot(geometry1));
            System.out.print(Export.toPlot(geometry2));
            System.out.println("#angle B:           " + bDirection.toString(DirectionUnit.EAST_DEGREE));
            System.out.println("#conflict B start:  " + conflictBStart);
            System.out.println("#conflict B length: " + conflictBLength);
            System.out.println("c0,0,1");
            System.out.println(String.format("M%.3f,%.3f <%f l%f,0", pointBFrom.x, pointBFrom.y, Math.toDegrees(bDirection.si),
                    conflictBStart.si));
            System.out.println(String.format("c0,0,0 l%f,0", conflictBLength.si));
        }

        assertEquals(0, this.collectedEvents.size(), "not events received yet");

        // That was a lot of code - just to prepare things to call generateConflictPair ...
        Conflict.generateConflictPair(ConflictType.CROSSING, new DefaultConflictRule(), false, laneA,
                new Length(conflictStart.x, LengthUnit.SI), new Length(conflictEnd.x - conflictStart.x, LengthUnit.SI),
                geometry1, laneB, conflictBStart, conflictBLength, geometry2, simulator);

        // Check that two conflicts have been created
        assertEquals(1, laneA.getLaneBasedObjects().size(), "one conflict on lane A");
        assertEquals(1, laneB.getLaneBasedObjects().size(), "one conflict on lane B");
        // Get the Conflicts
        Conflict conflictA = (Conflict) laneA.getLaneBasedObjects().get(0);
        Conflict conflictB = (Conflict) laneB.getLaneBasedObjects().get(0);
        if (VERBOSE)
        {
            System.out.println("Conflict A: " + conflictA);
            System.out.println("Conflict B: " + conflictB);
        }

        assertEquals(conflictA, conflictB.getOtherConflict(), "the conflicts are each others counter part");
        assertEquals(conflictB, conflictA.getOtherConflict(), "the conflicts are each others counter part");
        assertEquals(new Length(conflictStart.x, LengthUnit.SI), conflictA.getLongitudinalPosition(), "longitudinal position");
        assertEquals(conflictBStart, conflictB.getLongitudinalPosition(), "longitudinal position");
        assertEquals(new Length(conflictEnd.x - conflictStart.x, LengthUnit.SI), conflictA.getLength(), "length");
        assertEquals(conflictBLength, conflictB.getLength(), "length");
        assertEquals(geometry1, conflictA.getAbsoluteContour(), "contour");
        assertEquals(geometry2, conflictB.getAbsoluteContour(), "contour");
        assertTrue(conflictA.getConflictRule() instanceof DefaultConflictRule, "conflict rule");
        assertTrue(conflictB.getConflictRule() instanceof DefaultConflictRule, "conflict rule");
        assertFalse(conflictA.isPermitted(), "conflict A is not permitted");
        assertFalse(conflictB.isPermitted(), "conflict B is not permitted");
        assertEquals(2, this.collectedEvents.size(), "construction of two conflicts has generated two events");
        // Not checking the contents of those events; these are subject to change; as they indirectly link to the Network

    }

    /**
     * Find all 2D (ignoring Z) intersections between two OtsLine2d objects.
     * @param a the first polyline
     * @param b the second polyline
     * @return the intersections
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
    public void notify(final Event event)
    {
        // System.out.println("received event " + event);
        this.collectedEvents.add(event);
    }

}
