package org.opentrafficsim.road.network.lane;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.djunits.unit.DirectionUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.junit.Test;
import org.mockito.Mockito;
import org.opentrafficsim.core.dsol.OTSReplication;
import org.opentrafficsim.core.dsol.OTSSimulator;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.perception.HistoryManagerDEVS;
import org.opentrafficsim.road.mock.MockDEVSSimulator;
import org.opentrafficsim.road.mock.MockSimulator;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;
import org.opentrafficsim.road.network.lane.conflict.Conflict;
import org.opentrafficsim.road.network.lane.conflict.ConflictType;
import org.opentrafficsim.road.network.lane.conflict.DefaultConflictRule;

/**
 * Test the Conflict class.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class ConflictTest
{

    /**
     * Test the Conflict class.
     * @throws NetworkException
     * @throws OTSGeometryException
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
        Mockito.when(simulator.getSimulatorTime()).thenReturn(Time.ZERO);
        OTSRoadNetwork network = new OTSRoadNetwork("Network for conflict test", true, simulator);
        LinkType linkType = network.getLinkType(LinkType.DEFAULTS.ROAD);
        LaneType laneType = network.getLaneType(LaneType.DEFAULTS.ONE_WAY_LANE);
        OTSPoint3D pointAFrom = new OTSPoint3D(0, 0, 0);
        OTSRoadNode nodeAFrom = new OTSRoadNode(network, "A from", pointAFrom, Direction.ZERO);
        OTSPoint3D pointATo = new OTSPoint3D(100, 0, 0);
        OTSRoadNode nodeATo = new OTSRoadNode(network, "A to", pointATo, Direction.ZERO);
        CrossSectionLink linkA = new CrossSectionLink(network, "Link A", nodeAFrom, nodeATo, linkType,
                new OTSLine3D(pointAFrom, pointATo), LaneKeepingPolicy.KEEPRIGHT);
        Lane laneA = new Lane(linkA, "lane A", Length.ZERO, new Length(3, LengthUnit.METER), laneType,
                new Speed(50, SpeedUnit.KM_PER_HOUR));

        OTSPoint3D pointBFrom = new OTSPoint3D(30, -15, 0);
        OTSRoadNode nodeBFrom = new OTSRoadNode(network, "B from", pointBFrom, new Direction(90, DirectionUnit.EAST_DEGREE));
        OTSPoint3D pointBTo = new OTSPoint3D(30, 50, 0);
        OTSRoadNode nodeBTo = new OTSRoadNode(network, "B to", pointBTo, new Direction(90, DirectionUnit.EAST_DEGREE));
        CrossSectionLink linkB = new CrossSectionLink(network, "Link B", nodeBFrom, nodeBTo, linkType,
                new OTSLine3D(pointBFrom, pointBTo), LaneKeepingPolicy.KEEPRIGHT);
        Lane laneB = new Lane(linkB, "lane B", Length.ZERO, new Length(3.25, LengthUnit.METER), laneType,
                new Speed(50, SpeedUnit.KM_PER_HOUR));

        OTSLine3D geometry1 = new OTSLine3D(new OTSPoint3D(30, 0, 0), new OTSPoint3D(33, 0, 0), new OTSPoint3D(33, 3, 0),
                new OTSPoint3D(30, 3, 0));
        OTSLine3D geometry2 = geometry1;
        GTUType gtuType = network.getGtuType("CAR");
        // That was a lot of code - just to prepare things to create a Conflict ...
        Conflict.generateConflictPair(ConflictType.CROSSING, new DefaultConflictRule(), false, laneA,
                new Length(30, LengthUnit.METER), new Length(3.25, LengthUnit.METER), GTUDirectionality.DIR_PLUS, geometry1,
                gtuType, laneB, new Length(15, LengthUnit.METER), new Length(3, LengthUnit.METER), GTUDirectionality.DIR_PLUS,
                geometry2, gtuType, simulator);
        
        // Find the Conflicts
        Conflict conflictA = (Conflict) laneA.getLaneBasedObjects().get(0);
        System.out.println("Conflict A: " + conflictA);
        Conflict conflictB = (Conflict) laneB.getLaneBasedObjects().get(0);
        System.out.println("Conflict B: " + conflictB);
        
        assertEquals("the conflicts are each others counter part", conflictA, conflictB.getOtherConflict());
        assertEquals("the conflicts are each others counter part", conflictB, conflictA.getOtherConflict());
        assertEquals("longitudinal position", new Length(30, LengthUnit.METER), conflictA.getLongitudinalPosition());
        assertEquals("longitudinal position", new Length(15, LengthUnit.METER), conflictB.getLongitudinalPosition());
        assertEquals("length", new Length(3.25, LengthUnit.METER), conflictA.getLength());
        assertEquals("length", new Length(3, LengthUnit.METER), conflictB.getLength());
        assertEquals("geometry", geometry1, conflictA.getGeometry());
        assertEquals("geometry", geometry2, conflictB.getGeometry());
        assertTrue("conflict rule", conflictA.getConflictRule() instanceof DefaultConflictRule);
        assertTrue("conflict rule", conflictB.getConflictRule() instanceof DefaultConflictRule);
    }

}
