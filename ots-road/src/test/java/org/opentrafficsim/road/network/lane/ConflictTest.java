package org.opentrafficsim.road.network.lane;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.djunits.unit.DirectionUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.event.EventInterface;
import org.djutils.event.EventListenerInterface;
import org.junit.Test;
import org.mockito.Mockito;
import org.opentrafficsim.core.dsol.OTSReplication;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.perception.HistoryManagerDEVS;
import org.opentrafficsim.road.mock.MockDEVSSimulator;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;
import org.opentrafficsim.road.network.lane.conflict.Conflict;
import org.opentrafficsim.road.network.lane.conflict.ConflictType;
import org.opentrafficsim.road.network.lane.conflict.DefaultConflictRule;

/**
 * Test the Conflict class.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class ConflictTest implements EventListenerInterface
{
    /** ... */
    private static final long serialVersionUID = 20200708L;

    /** Storage for received events. */
    private List<EventInterface> collectedEvents = new ArrayList<>();

    /**
     * Test the Conflict class.
     * @throws NetworkException on error
     * @throws OTSGeometryException on error
     */
    @Test
    public void testConstructor() throws NetworkException, OTSGeometryException
    {
        OTSSimulatorInterface simulator = MockDEVSSimulator.createMock();
        OTSReplication replication = Mockito.mock(OTSReplication.class);
        HistoryManagerDEVS hmd = Mockito.mock(HistoryManagerDEVS.class);
        Mockito.when(hmd.now()).thenReturn(Time.ZERO);
        Mockito.when(replication.getHistoryManager(simulator)).thenReturn(hmd);
        Mockito.when(simulator.getReplication()).thenReturn(replication);
        Mockito.when(simulator.getSimulatorAbsTime()).thenReturn(Time.ZERO);
        Mockito.when(simulator.getSimulatorTime()).thenReturn(Duration.ZERO);
        OTSRoadNetwork network = new OTSRoadNetwork("Network for conflict test", true, simulator);
        LinkType linkType = network.getLinkType(LinkType.DEFAULTS.ROAD);
        LaneType laneType = network.getLaneType(LaneType.DEFAULTS.ONE_WAY_LANE);
        OTSPoint3D pointAFrom = new OTSPoint3D(0, 0, 0);
        OTSRoadNode nodeAFrom = new OTSRoadNode(network, "A from", pointAFrom, Direction.ZERO);
        OTSPoint3D pointATo = new OTSPoint3D(100, 0, 0);
        OTSRoadNode nodeATo = new OTSRoadNode(network, "A to", pointATo, Direction.ZERO);
        CrossSectionLink linkA = new CrossSectionLink(network, "Link A", nodeAFrom, nodeATo, linkType,
                new OTSLine3D(pointAFrom, pointATo), LaneKeepingPolicy.KEEPRIGHT);
        Lane laneA = new Lane(linkA, "lane A", Length.ZERO, new Length(2, LengthUnit.METER), laneType,
                new Speed(50, SpeedUnit.KM_PER_HOUR));
        laneA.addListener(this, Lane.OBJECT_ADD_EVENT);

        OTSPoint3D pointBFrom = new OTSPoint3D(30, -15, 0);
        OTSPoint3D pointBTo = new OTSPoint3D(60, 60, 0);
        Direction bDirection =
                new Direction(Math.atan2(pointBTo.y - pointBFrom.y, pointBTo.x - pointBFrom.x), DirectionUnit.EAST_RADIAN);
        OTSRoadNode nodeBFrom = new OTSRoadNode(network, "B from", pointBFrom, bDirection);
        OTSRoadNode nodeBTo = new OTSRoadNode(network, "B to", pointBTo, bDirection);
        CrossSectionLink linkB = new CrossSectionLink(network, "Link B", nodeBFrom, nodeBTo, linkType,
                new OTSLine3D(pointBFrom, pointBTo), LaneKeepingPolicy.KEEPRIGHT);
        Lane laneB = new Lane(linkB, "lane B", Length.ZERO, new Length(4, LengthUnit.METER), laneType,
                new Speed(50, SpeedUnit.KM_PER_HOUR));
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
        OTSPoint3D conflictStart = null;
        double closestDistance = Double.MAX_VALUE;
        OTSPoint3D conflictEnd = null;
        double furthestDistance = 0.0;
        for (OTSPoint3D intersection : intersections(laneA.getContour(), laneB.getContour()))
        {
            double distance = pointAFrom.distanceSI(intersection);
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
        OTSLine3D geometry1 = new OTSLine3D(conflictStart, new OTSPoint3D(conflictEnd.x, conflictStart.y, 0), conflictEnd,
                new OTSPoint3D(conflictStart.x, conflictEnd.y, 0), conflictStart);
        System.out.print(geometry1.toPlot());
        OTSLine3D geometry2 = new OTSLine3D(conflictStart,
                new OTSPoint3D(conflictStart.x + laneB.getWidth(0).si * Math.sin(bDirection.si),
                        conflictStart.y - laneB.getWidth(0).si * Math.cos(bDirection.si), 0),
                conflictEnd, new OTSPoint3D(conflictEnd.x - laneB.getWidth(0).si * Math.sin(bDirection.si),
                        conflictEnd.y + laneB.getWidth(0).si * Math.cos(bDirection.si), 0),
                conflictStart);
        System.out.print(geometry2.toPlot());

        System.out.println("#angle B:           " + bDirection.toString(DirectionUnit.EAST_DEGREE));
        Length conflictBStart = new Length(
                pointBFrom.distance(new OTSPoint3D(conflictStart.x + laneB.getWidth(0).si / 2 * Math.sin(bDirection.si),
                        conflictStart.y - laneB.getWidth(0).si / 2 * Math.cos(bDirection.si), 0)).si,
                LengthUnit.SI);
        System.out.println("#conflict B start:  " + conflictBStart);
        Length conflictBLength = new Length(
                laneA.getWidth(0).si / Math.sin(bDirection.si) + laneB.getWidth(0).si / Math.tan(bDirection.si), LengthUnit.SI);
        System.out.println("#conflict B length: " + conflictBLength);
        System.out.println("c0,0,1");
        System.out.println(String.format("M%.3f,%.3f <%f l%f,0", pointBFrom.x, pointBFrom.y, Math.toDegrees(bDirection.si),
                conflictBStart.si));
        System.out.println(String.format("c0,0,0 l%f,0", conflictBLength.si));

        GtuType bicycles = network.getGtuType("BICYCLE");
        GtuType cars = network.getGtuType("CAR");

        assertEquals("not events received yet", 0, this.collectedEvents.size());

        // That was a lot of code - just to prepare things to call generateConflictPair ...
        Conflict.generateConflictPair(ConflictType.CROSSING, new DefaultConflictRule(), false, laneA,
                new Length(conflictStart.x, LengthUnit.SI), new Length(conflictEnd.x - conflictStart.x, LengthUnit.SI),
                geometry1, bicycles, laneB, conflictBStart, conflictBLength, geometry2,
                cars, simulator);

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
        assertEquals("conflict A is about bicycles", bicycles, conflictA.getGtuType());
        assertEquals("conflict B is about cars", cars, conflictB.getGtuType());
        assertEquals("construction of two conflicts has generated two events", 2, this.collectedEvents.size());
        // Not checking the contents of those events; these are subject to change; as they indirectly link to the Network

    }

    /**
     * Find all 2D (ignoring Z) intersections between two OTSLine3D objects.
     * @param a OTSLine3D; the first polyline
     * @param b OTSLine3D; the second polyline
     * @return Set&lt;OTSPoint3D&gt;; the intersections
     */
    public Set<OTSPoint3D> intersections(final OTSLine3D a, final OTSLine3D b)
    {
        // TODO discuss if this method should be moved into the OTSLine3D class
        Set<OTSPoint3D> result = new LinkedHashSet<>();
        OTSPoint3D prevA = null;
        for (OTSPoint3D nextA : a.getPoints())
        {
            if (null != prevA)
            {
                OTSPoint3D prevB = null;
                for (OTSPoint3D nextB : b.getPoints())
                {
                    if (null != prevB)
                    {
                        OTSPoint3D intersection = OTSPoint3D.intersectionOfLineSegments(prevA, nextA, prevB, nextB);
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
    public final void notify(final EventInterface event) throws RemoteException
    {
        // System.out.println("received event " + event);
        this.collectedEvents.add(event);
    }

}
