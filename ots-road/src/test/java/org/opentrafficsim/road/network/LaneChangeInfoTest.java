package org.opentrafficsim.road.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.draw.point.Point2d;
import org.djutils.immutablecollections.ImmutableSortedSet;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.dsol.OtsModelInterface;
import org.opentrafficsim.core.dsol.OtsSimulator;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.perception.HistoryManagerDevs;
import org.opentrafficsim.road.definitions.DefaultsRoadNl;
import org.opentrafficsim.road.network.RoadNetwork.LaneChangeTiming;
import org.opentrafficsim.road.network.factory.LaneFactory;
import org.opentrafficsim.road.network.speed.LaneSpeedLimits;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * Test for lane change info.
 * <p>
 * Copyright (c) 2022-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.<br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * @author Wouter Schakel
 */
public final class LaneChangeInfoTest
{

    /** Constructor. */
    private LaneChangeInfoTest()
    {
        // do not instantiate test class
    }

    /**
     * Test to see whether lane change info is correctly delivered by the network.
     * @throws NetworkException on exception
     * @throws SimRuntimeException on exception
     * @throws NamingException on exception
     */
    @Test
    @SuppressWarnings("methodlength")
    public void laneChangeInfoTest() throws NetworkException, SimRuntimeException, NamingException
    {

        // Preparations
        OtsModelInterface model = Mockito.mock(OtsModelInterface.class);
        OtsSimulatorInterface simulator = new OtsSimulator("Lane change info test");
        simulator.initialize(Duration.ZERO, Duration.ZERO, Duration.ofSI(3600), model, HistoryManagerDevs.noHistory(simulator));
        RoadNetwork network = new RoadNetwork("Lane change info test network", simulator);
        GtuType car = DefaultsNl.CAR;

        LinkType freeway = DefaultsNl.FREEWAY;
        LaneKeepingPolicy policy = LaneKeepingPolicy.KEEPRIGHT;
        Length laneWidth = Length.ofSI(3.5);
        LaneType freewayLane = DefaultsRoadNl.FREEWAY;
        LaneSpeedLimits speedLimits = new LaneSpeedLimits(new Speed(120, SpeedUnit.KM_PER_HOUR),
                Map.of(DefaultsNl.TRUCK, new Speed(80, SpeedUnit.KM_PER_HOUR)));

        /*-
         * A   B   C   D   E   F   G   H        nodes
         * ----                ---->>>> H2
         * - - ----------------- - ----
         * - - - - ========- - - - - -          ===== only left lane changes
         * - - - - - - - - - - - - - -
         * ------------- - ------------
         *             ---->>>> F2              >>>>  ramp
         */

        // Nodes
        Node nodeA = new Node(network, "A", new Point2d(0, 0), Direction.ZERO);
        Node nodeB = new Node(network, "B", new Point2d(200, 0), Direction.ZERO);
        Node nodeC = new Node(network, "C", new Point2d(500, 0), Direction.ZERO);
        Node nodeD = new Node(network, "D", new Point2d(900, 0), Direction.ZERO);
        Node nodeE = new Node(network, "E", new Point2d(1400, 0), Direction.ZERO);
        Node nodeF = new Node(network, "F", new Point2d(2000, 0), Direction.ZERO);
        Node nodeF2 = new Node(network, "F2", new Point2d(2000, -3.5), Direction.ZERO);
        Node nodeG = new Node(network, "G", new Point2d(2700, 0), Direction.ZERO);
        Node nodeH = new Node(network, "H", new Point2d(3500, 0), Direction.ZERO);
        Node nodeH2 = new Node(network, "H2", new Point2d(3500, 3.5), Direction.ZERO);

        // Lanes
        List<Lane> lsAB = new LaneFactory(network, nodeA, nodeB, freeway, simulator, policy)
                .leftToRight(4.0, laneWidth, freewayLane, speedLimits)
                .addLanes(DefaultsRoadNl.DASHED, DefaultsRoadNl.DASHED, DefaultsRoadNl.DASHED).getLanes();
        List<Lane> lsBC = new LaneFactory(network, nodeB, nodeC, freeway, simulator, policy)
                .leftToRight(3.0, laneWidth, freewayLane, speedLimits).addLanes(DefaultsRoadNl.DASHED, DefaultsRoadNl.DASHED)
                .getLanes();
        List<Lane> lsCD = new LaneFactory(network, nodeC, nodeD, freeway, simulator, policy)
                .leftToRight(3.0, laneWidth, freewayLane, speedLimits).addLanes(DefaultsRoadNl.LEFT, DefaultsRoadNl.DASHED)
                .getLanes();
        List<Lane> lsDE = new LaneFactory(network, nodeD, nodeE, freeway, simulator, policy)
                .leftToRight(3.0, laneWidth, freewayLane, speedLimits)
                .addLanes(DefaultsRoadNl.LEFT, DefaultsRoadNl.DASHED, DefaultsRoadNl.RIGHT).getLanes();
        List<Lane> lsEF = new LaneFactory(network, nodeE, nodeF, freeway, simulator, policy)
                .leftToRight(3.0, laneWidth, freewayLane, speedLimits).addLanes(DefaultsRoadNl.DASHED, DefaultsRoadNl.DASHED)
                .getLanes();
        List<Lane> lsEF2 = new LaneFactory(network, nodeE, nodeF2, freeway, simulator, policy)
                .leftToRight(0.0, laneWidth, freewayLane, speedLimits).addLanes().getLanes();
        List<Lane> lsFG = new LaneFactory(network, nodeF, nodeG, freeway, simulator, policy)
                .leftToRight(4.0, laneWidth, freewayLane, speedLimits)
                .addLanes(DefaultsRoadNl.LEFT, DefaultsRoadNl.DASHED, DefaultsRoadNl.DASHED).getLanes();
        List<Lane> lsGH = new LaneFactory(network, nodeG, nodeH, freeway, simulator, policy)
                .leftToRight(3.0, laneWidth, freewayLane, speedLimits).addLanes(DefaultsRoadNl.DASHED, DefaultsRoadNl.DASHED)
                .getLanes();
        List<Lane> lsGH2 = new LaneFactory(network, nodeG, nodeH2, freeway, simulator, policy)
                .leftToRight(4.0, laneWidth, freewayLane, speedLimits).addLanes().getLanes();

        // Preparation
        LaneAccessLaw law = LaneAccessLaw.LEGAL;
        LateralDirectionality left = LateralDirectionality.LEFT;
        LateralDirectionality right = LateralDirectionality.RIGHT;
        Length r = Length.ofSI(10000);
        LaneChangeTiming te = LaneChangeTiming.EARLIEST_FEASIBLE;
        LaneChangeTiming tl = LaneChangeTiming.LATEST_FEASIBLE;

        // Route A -> F2
        Route rAF2 = network.getShortestRouteBetween(car, nodeA, nodeF2);
        // AB -> F2
        check(network.getLaneChangeInfo(lsAB.get(0), rAF2, tl, car, r, law), right, new int[] {1, 2, 4},
                new double[] {200, 500, 1400});
        check(network.getLaneChangeInfo(lsAB.get(1), rAF2, tl, car, r, law), right, new int[] {1, 3}, new double[] {500, 1400});
        check(network.getLaneChangeInfo(lsAB.get(2), rAF2, tl, car, r, law), right, new int[] {2}, new double[] {1400});
        check(network.getLaneChangeInfo(lsAB.get(3), rAF2, tl, car, r, law), right, new int[] {1}, new double[] {1400});
        check(network.getLaneChangeInfo(lsAB.get(0), rAF2, te, car, r, law), right, new int[] {3, 4}, new double[] {0, 900});
        check(network.getLaneChangeInfo(lsAB.get(1), rAF2, te, car, r, law), right, new int[] {2, 3}, new double[] {0, 900});
        check(network.getLaneChangeInfo(lsAB.get(2), rAF2, te, car, r, law), right, new int[] {1, 2}, new double[] {0, 900});
        check(network.getLaneChangeInfo(lsAB.get(3), rAF2, te, car, r, law), right, new int[] {1}, new double[] {900});
        // BC -> F2
        check(network.getLaneChangeInfo(lsBC.get(0), rAF2, tl, car, r, law), right, new int[] {1, 3}, new double[] {300, 1200});
        check(network.getLaneChangeInfo(lsBC.get(1), rAF2, tl, car, r, law), right, new int[] {2}, new double[] {1200});
        check(network.getLaneChangeInfo(lsBC.get(2), rAF2, tl, car, r, law), right, new int[] {1}, new double[] {1200});
        check(network.getLaneChangeInfo(lsBC.get(0), rAF2, te, car, r, law), right, new int[] {2, 3}, new double[] {0, 700});
        check(network.getLaneChangeInfo(lsBC.get(1), rAF2, te, car, r, law), right, new int[] {1, 2}, new double[] {0, 700});
        check(network.getLaneChangeInfo(lsBC.get(2), rAF2, te, car, r, law), right, new int[] {1}, new double[] {700});
        // CD - F2
        assertTrue(network.getLaneChangeInfo(lsCD.get(0), rAF2, tl, car, r, law).isEmpty());
        check(network.getLaneChangeInfo(lsCD.get(1), rAF2, tl, car, r, law), right, new int[] {2}, new double[] {900});
        check(network.getLaneChangeInfo(lsCD.get(2), rAF2, tl, car, r, law), right, new int[] {1}, new double[] {900});
        assertTrue(network.getLaneChangeInfo(lsCD.get(0), rAF2, te, car, r, law).isEmpty());
        check(network.getLaneChangeInfo(lsCD.get(1), rAF2, te, car, r, law), right, new int[] {1, 2}, new double[] {0, 400});
        check(network.getLaneChangeInfo(lsCD.get(2), rAF2, te, car, r, law), right, new int[] {1}, new double[] {400});
        // DE - F2
        assertTrue(network.getLaneChangeInfo(lsDE.get(0), rAF2, tl, car, r, law).isEmpty());
        check(network.getLaneChangeInfo(lsDE.get(1), rAF2, tl, car, r, law), right, new int[] {2}, new double[] {500});
        check(network.getLaneChangeInfo(lsDE.get(2), rAF2, tl, car, r, law), right, new int[] {1}, new double[] {500});
        assertTrue(network.getLaneChangeInfo(lsDE.get(0), rAF2, te, car, r, law).isEmpty());
        check(network.getLaneChangeInfo(lsDE.get(1), rAF2, te, car, r, law), right, new int[] {2}, new double[] {0});
        check(network.getLaneChangeInfo(lsDE.get(2), rAF2, te, car, r, law), right, new int[] {1}, new double[] {0});
        // EF2
        assertTrue(network.getLaneChangeInfo(lsEF2.get(0), rAF2, tl, car, r, law).isEmpty());
        assertTrue(network.getLaneChangeInfo(lsEF2.get(0), rAF2, te, car, r, law).isEmpty());

        // Route A -> H2
        Route rAH2 = network.getShortestRouteBetween(car, nodeA, nodeH2);
        // AB -> H2
        check(network.getLaneChangeInfo(lsAB.get(0), rAH2, tl, car, r, law), right, new int[] {1}, new double[] {200});
        check(network.getLaneChangeInfo(lsAB.get(1), rAH2, tl, car, r, law), left, new int[] {1}, new double[] {2700});
        check(network.getLaneChangeInfo(lsAB.get(2), rAH2, tl, car, r, law), left, new int[] {2}, new double[] {2700});
        check(network.getLaneChangeInfo(lsAB.get(3), rAH2, tl, car, r, law), left, new int[] {3}, new double[] {2700});
        check(network.getLaneChangeInfo(lsAB.get(0), rAH2, te, car, r, law), right, new int[] {1}, new double[] {0});
        check(network.getLaneChangeInfo(lsAB.get(1), rAH2, te, car, r, law), left, new int[] {1}, new double[] {2000});
        check(network.getLaneChangeInfo(lsAB.get(2), rAH2, te, car, r, law), left, new int[] {1, 2}, new double[] {0, 2000});
        check(network.getLaneChangeInfo(lsAB.get(3), rAH2, te, car, r, law), left, new int[] {2, 3}, new double[] {0, 2000});
        // BC -> H2
        check(network.getLaneChangeInfo(lsBC.get(0), rAH2, tl, car, r, law), left, new int[] {1}, new double[] {2500});
        check(network.getLaneChangeInfo(lsBC.get(1), rAH2, tl, car, r, law), left, new int[] {2}, new double[] {2500});
        check(network.getLaneChangeInfo(lsBC.get(2), rAH2, tl, car, r, law), left, new int[] {3}, new double[] {2500});
        check(network.getLaneChangeInfo(lsBC.get(0), rAH2, te, car, r, law), left, new int[] {1}, new double[] {1800});
        check(network.getLaneChangeInfo(lsBC.get(1), rAH2, te, car, r, law), left, new int[] {1, 2}, new double[] {0, 1800});
        check(network.getLaneChangeInfo(lsBC.get(2), rAH2, te, car, r, law), left, new int[] {2, 3}, new double[] {0, 1800});
        // CD -> H2
        check(network.getLaneChangeInfo(lsCD.get(0), rAH2, tl, car, r, law), left, new int[] {1}, new double[] {2200});
        check(network.getLaneChangeInfo(lsCD.get(1), rAH2, tl, car, r, law), left, new int[] {2}, new double[] {2200});
        check(network.getLaneChangeInfo(lsCD.get(2), rAH2, tl, car, r, law), left, new int[] {3}, new double[] {2200});
        check(network.getLaneChangeInfo(lsCD.get(0), rAH2, te, car, r, law), left, new int[] {1}, new double[] {1500});
        check(network.getLaneChangeInfo(lsCD.get(1), rAH2, te, car, r, law), left, new int[] {1, 2}, new double[] {0, 1500});
        check(network.getLaneChangeInfo(lsCD.get(2), rAH2, te, car, r, law), left, new int[] {2, 3}, new double[] {0, 1500});
        // DE -> H2
        check(network.getLaneChangeInfo(lsDE.get(0), rAH2, tl, car, r, law), left, new int[] {1}, new double[] {1800});
        check(network.getLaneChangeInfo(lsDE.get(1), rAH2, tl, car, r, law), left, new int[] {2}, new double[] {1800});
        check(network.getLaneChangeInfo(lsDE.get(2), rAH2, tl, car, r, law), left, new int[] {3}, new double[] {1800});
        assertTrue(network.getLaneChangeInfo(lsDE.get(3), rAH2, tl, car, r, law).isEmpty());
        check(network.getLaneChangeInfo(lsDE.get(0), rAH2, te, car, r, law), left, new int[] {1}, new double[] {1100});
        check(network.getLaneChangeInfo(lsDE.get(1), rAH2, te, car, r, law), left, new int[] {1, 2}, new double[] {0, 1100});
        check(network.getLaneChangeInfo(lsDE.get(2), rAH2, te, car, r, law), left, new int[] {2, 3}, new double[] {0, 1100});
        assertTrue(network.getLaneChangeInfo(lsDE.get(3), rAH2, te, car, r, law).isEmpty());
        // EF -> H2
        check(network.getLaneChangeInfo(lsEF.get(0), rAH2, tl, car, r, law), left, new int[] {1}, new double[] {1300});
        check(network.getLaneChangeInfo(lsEF.get(1), rAH2, tl, car, r, law), left, new int[] {2}, new double[] {1300});
        check(network.getLaneChangeInfo(lsEF.get(2), rAH2, tl, car, r, law), left, new int[] {3}, new double[] {1300});
        check(network.getLaneChangeInfo(lsEF.get(0), rAH2, te, car, r, law), left, new int[] {1}, new double[] {600});
        check(network.getLaneChangeInfo(lsEF.get(1), rAH2, te, car, r, law), left, new int[] {1, 2}, new double[] {0, 600});
        check(network.getLaneChangeInfo(lsEF.get(2), rAH2, te, car, r, law), left, new int[] {2, 3}, new double[] {0, 600});
        // FG -> H2
        assertTrue(network.getLaneChangeInfo(lsFG.get(0), rAH2, tl, car, r, law).isEmpty());
        check(network.getLaneChangeInfo(lsFG.get(1), rAH2, tl, car, r, law), left, new int[] {1}, new double[] {700});
        check(network.getLaneChangeInfo(lsFG.get(2), rAH2, tl, car, r, law), left, new int[] {2}, new double[] {700});
        check(network.getLaneChangeInfo(lsFG.get(3), rAH2, tl, car, r, law), left, new int[] {3}, new double[] {700});
        assertTrue(network.getLaneChangeInfo(lsFG.get(0), rAH2, te, car, r, law).isEmpty());
        check(network.getLaneChangeInfo(lsFG.get(1), rAH2, te, car, r, law), left, new int[] {1}, new double[] {0});
        check(network.getLaneChangeInfo(lsFG.get(2), rAH2, te, car, r, law), left, new int[] {2}, new double[] {0});
        check(network.getLaneChangeInfo(lsFG.get(3), rAH2, te, car, r, law), left, new int[] {3}, new double[] {0});
        // GH2
        assertTrue(network.getLaneChangeInfo(lsGH2.get(0), rAH2, tl, car, r, law).isEmpty());
        assertTrue(network.getLaneChangeInfo(lsGH2.get(0), rAH2, te, car, r, law).isEmpty());

        // Route A -> H
        Route rAH = network.getShortestRouteBetween(car, nodeA, nodeH);
        // AB -> H
        check(network.getLaneChangeInfo(lsAB.get(0), rAH, tl, car, r, law), right, new int[] {1}, new double[] {200});
        assertTrue(network.getLaneChangeInfo(lsAB.get(1), rAH, tl, car, r, law).isEmpty());
        assertTrue(network.getLaneChangeInfo(lsAB.get(2), rAH, tl, car, r, law).isEmpty());
        assertTrue(network.getLaneChangeInfo(lsAB.get(3), rAH, tl, car, r, law).isEmpty());
        check(network.getLaneChangeInfo(lsAB.get(0), rAH, te, car, r, law), right, new int[] {1}, new double[] {0});
        assertTrue(network.getLaneChangeInfo(lsAB.get(1), rAH, te, car, r, law).isEmpty());
        assertTrue(network.getLaneChangeInfo(lsAB.get(2), rAH, te, car, r, law).isEmpty());
        assertTrue(network.getLaneChangeInfo(lsAB.get(3), rAH, te, car, r, law).isEmpty());
        // BC -> H
        assertTrue(network.getLaneChangeInfo(lsBC.get(0), rAH, tl, car, r, law).isEmpty());
        assertTrue(network.getLaneChangeInfo(lsBC.get(1), rAH, tl, car, r, law).isEmpty());
        assertTrue(network.getLaneChangeInfo(lsBC.get(2), rAH, tl, car, r, law).isEmpty());
        assertTrue(network.getLaneChangeInfo(lsBC.get(0), rAH, te, car, r, law).isEmpty());
        assertTrue(network.getLaneChangeInfo(lsBC.get(1), rAH, te, car, r, law).isEmpty());
        assertTrue(network.getLaneChangeInfo(lsBC.get(2), rAH, te, car, r, law).isEmpty());
        // CD -> H
        assertTrue(network.getLaneChangeInfo(lsCD.get(0), rAH, tl, car, r, law).isEmpty());
        assertTrue(network.getLaneChangeInfo(lsCD.get(1), rAH, tl, car, r, law).isEmpty());
        assertTrue(network.getLaneChangeInfo(lsCD.get(2), rAH, tl, car, r, law).isEmpty());
        assertTrue(network.getLaneChangeInfo(lsCD.get(0), rAH, te, car, r, law).isEmpty());
        assertTrue(network.getLaneChangeInfo(lsCD.get(1), rAH, te, car, r, law).isEmpty());
        assertTrue(network.getLaneChangeInfo(lsCD.get(2), rAH, te, car, r, law).isEmpty());
        // DE -> H
        assertTrue(network.getLaneChangeInfo(lsDE.get(0), rAH, tl, car, r, law).isEmpty());
        assertTrue(network.getLaneChangeInfo(lsDE.get(1), rAH, tl, car, r, law).isEmpty());
        assertTrue(network.getLaneChangeInfo(lsDE.get(2), rAH, tl, car, r, law).isEmpty());
        assertTrue(network.getLaneChangeInfo(lsDE.get(3), rAH, tl, car, r, law).isEmpty());
        assertTrue(network.getLaneChangeInfo(lsDE.get(0), rAH, te, car, r, law).isEmpty());
        assertTrue(network.getLaneChangeInfo(lsDE.get(1), rAH, te, car, r, law).isEmpty());
        assertTrue(network.getLaneChangeInfo(lsDE.get(2), rAH, te, car, r, law).isEmpty());
        assertTrue(network.getLaneChangeInfo(lsDE.get(3), rAH, te, car, r, law).isEmpty());
        // EF -> H
        assertTrue(network.getLaneChangeInfo(lsEF.get(0), rAH, tl, car, r, law).isEmpty());
        assertTrue(network.getLaneChangeInfo(lsEF.get(1), rAH, tl, car, r, law).isEmpty());
        assertTrue(network.getLaneChangeInfo(lsEF.get(2), rAH, tl, car, r, law).isEmpty());
        assertTrue(network.getLaneChangeInfo(lsEF.get(0), rAH, te, car, r, law).isEmpty());
        assertTrue(network.getLaneChangeInfo(lsEF.get(1), rAH, te, car, r, law).isEmpty());
        assertTrue(network.getLaneChangeInfo(lsEF.get(2), rAH, te, car, r, law).isEmpty());
        // FG -> H
        assertTrue(network.getLaneChangeInfo(lsFG.get(0), rAH, tl, car, r, law).isEmpty());
        assertTrue(network.getLaneChangeInfo(lsFG.get(1), rAH, tl, car, r, law).isEmpty());
        assertTrue(network.getLaneChangeInfo(lsFG.get(2), rAH, tl, car, r, law).isEmpty());
        assertTrue(network.getLaneChangeInfo(lsFG.get(3), rAH, tl, car, r, law).isEmpty());
        assertTrue(network.getLaneChangeInfo(lsFG.get(0), rAH, te, car, r, law).isEmpty());
        assertTrue(network.getLaneChangeInfo(lsFG.get(1), rAH, te, car, r, law).isEmpty());
        assertTrue(network.getLaneChangeInfo(lsFG.get(2), rAH, te, car, r, law).isEmpty());
        assertTrue(network.getLaneChangeInfo(lsFG.get(3), rAH, te, car, r, law).isEmpty());
        // GH
        assertTrue(network.getLaneChangeInfo(lsGH.get(0), rAH, tl, car, r, law).isEmpty());
        assertTrue(network.getLaneChangeInfo(lsGH.get(1), rAH, tl, car, r, law).isEmpty());
        assertTrue(network.getLaneChangeInfo(lsGH.get(2), rAH, tl, car, r, law).isEmpty());
        assertTrue(network.getLaneChangeInfo(lsGH.get(0), rAH, te, car, r, law).isEmpty());
        assertTrue(network.getLaneChangeInfo(lsGH.get(1), rAH, te, car, r, law).isEmpty());
        assertTrue(network.getLaneChangeInfo(lsGH.get(2), rAH, te, car, r, law).isEmpty());

        // Shorter ranges for AB -> F2
        r = Length.ofSI(1000);
        check(network.getLaneChangeInfo(lsAB.get(0), rAF2, tl, car, r, law), right, new int[] {1, 2}, new double[] {200, 500});
        check(network.getLaneChangeInfo(lsAB.get(0), rAF2, te, car, r, law), right, new int[] {3, 4}, new double[] {0, 900});
        r = Length.ofSI(400);
        check(network.getLaneChangeInfo(lsAB.get(0), rAF2, tl, car, r, law), right, new int[] {1}, new double[] {200});
        check(network.getLaneChangeInfo(lsAB.get(0), rAF2, te, car, r, law), right, new int[] {3}, new double[] {0});
        r = Length.ofSI(100);
        assertTrue(network.getLaneChangeInfo(lsAB.get(0), rAF2, tl, car, r, law).isEmpty());
        check(network.getLaneChangeInfo(lsAB.get(0), rAF2, te, car, r, law), right, new int[] {3}, new double[] {0});

    }

    /**
     * Checks whether lane change info meets the provided direction, number of changes, and distance.
     * @param laneChangeInfos lane change info to be tested
     * @param lat lateral direction of lane changes
     * @param laneChanges number of lane changes for consecutive lane change infos
     * @param distances distance for lane changes for consecutive lane change infos
     */
    private void check(final ImmutableSortedSet<LaneChangeInfo> laneChangeInfos, final LateralDirectionality lat,
            final int[] laneChanges, final double[] distances)
    {
        assertEquals(laneChanges.length, laneChangeInfos.size());
        int i = 0;
        for (LaneChangeInfo laneChangeInfo : laneChangeInfos)
        {
            assertEquals(lat, laneChangeInfo.lateralDirectionality());
            assertEquals(laneChanges[i], laneChangeInfo.numberOfLaneChanges());
            assertTrue(Math.abs(laneChangeInfo.remainingDistance().si - distances[i]) < 1e-3);
            i++;
        }
    }

}
