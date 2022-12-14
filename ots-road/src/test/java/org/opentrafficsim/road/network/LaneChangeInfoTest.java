package org.opentrafficsim.road.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.naming.NamingException;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.immutablecollections.ImmutableSortedSet;
import org.junit.Test;
import org.mockito.Mockito;
import org.opentrafficsim.core.dsol.OtsModelInterface;
import org.opentrafficsim.core.dsol.OtsSimulator;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.geometry.OtsPoint3D;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.gtu.GtuType.DEFAULTS;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.network.factory.LaneFactory;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.OtsRoadNode;
import org.opentrafficsim.road.network.lane.Stripe.Permeable;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * Test for lane change info. <br>
 * <br>
 * Copyright (c) 2022-2022 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://djutils.org" target="_blank"> https://djutils.org</a>. The DJUTILS project is
 * distributed under a three-clause BSD-style license, which can be found at
 * <a href="https://djutils.org/docs/license.html" target="_blank"> https://djutils.org/docs/license.html</a>. <br>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class LaneChangeInfoTest
{

    /**
     * Test to see whether lane change info is correctly delivered by the network.
     * @throws NetworkException on exception
     * @throws OtsGeometryException on exception
     * @throws SimRuntimeException on exception
     * @throws NamingException on exception
     */
    @Test
    public final void laneChangeInfoTest() throws NetworkException, OtsGeometryException, SimRuntimeException, NamingException
    {

        // Preparations
        OtsModelInterface model = Mockito.mock(OtsModelInterface.class);
        OtsSimulatorInterface simulator = new OtsSimulator("Lane change info test");
        simulator.initialize(Time.ZERO, Duration.ZERO, Duration.instantiateSI(3600), model);
        OtsRoadNetwork network = new OtsRoadNetwork("Lane change info test network", true, simulator);
        GtuType car = network.getGtuType(DEFAULTS.CAR);

        LinkType freeway = network.getLinkType(LinkType.DEFAULTS.FREEWAY);
        LaneKeepingPolicy policy = LaneKeepingPolicy.KEEPRIGHT;
        Length laneWidth = Length.instantiateSI(3.5);
        LaneType freewayLane = network.getLaneType(LaneType.DEFAULTS.FREEWAY);
        Speed speedLimit = new Speed(120, SpeedUnit.KM_PER_HOUR);

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
        OtsRoadNode nodeA = new OtsRoadNode(network, "A", new OtsPoint3D(0, 0, 0), Direction.ZERO);
        OtsRoadNode nodeB = new OtsRoadNode(network, "B", new OtsPoint3D(200, 0, 0), Direction.ZERO);
        OtsRoadNode nodeC = new OtsRoadNode(network, "C", new OtsPoint3D(500, 0, 0), Direction.ZERO);
        OtsRoadNode nodeD = new OtsRoadNode(network, "D", new OtsPoint3D(900, 0, 0), Direction.ZERO);
        OtsRoadNode nodeE = new OtsRoadNode(network, "E", new OtsPoint3D(1400, 0, 0), Direction.ZERO);
        OtsRoadNode nodeF = new OtsRoadNode(network, "F", new OtsPoint3D(2000, 0, 0), Direction.ZERO);
        OtsRoadNode nodeF2 = new OtsRoadNode(network, "F2", new OtsPoint3D(2000, -3.5, 0), Direction.ZERO);
        OtsRoadNode nodeG = new OtsRoadNode(network, "G", new OtsPoint3D(2700, 0, 0), Direction.ZERO);
        OtsRoadNode nodeH = new OtsRoadNode(network, "H", new OtsPoint3D(3500, 0, 0), Direction.ZERO);
        OtsRoadNode nodeH2 = new OtsRoadNode(network, "H2", new OtsPoint3D(3500, 3.5, 0), Direction.ZERO);

        // Lanes
        List<Lane> lanesAB = new LaneFactory(network, nodeA, nodeB, freeway, simulator, policy)
                .leftToRight(4.0, laneWidth, freewayLane, speedLimit).addLanes(Permeable.BOTH, Permeable.BOTH, Permeable.BOTH)
                .getLanes();
        List<Lane> lanesBC = new LaneFactory(network, nodeB, nodeC, freeway, simulator, policy)
                .leftToRight(3.0, laneWidth, freewayLane, speedLimit).addLanes(Permeable.BOTH, Permeable.BOTH).getLanes();
        List<Lane> lanesCD = new LaneFactory(network, nodeC, nodeD, freeway, simulator, policy)
                .leftToRight(3.0, laneWidth, freewayLane, speedLimit).addLanes(Permeable.LEFT, Permeable.BOTH).getLanes();
        List<Lane> lanesDE = new LaneFactory(network, nodeD, nodeE, freeway, simulator, policy)
                .leftToRight(3.0, laneWidth, freewayLane, speedLimit).addLanes(Permeable.LEFT, Permeable.BOTH, Permeable.RIGHT)
                .getLanes();
        List<Lane> lanesEF = new LaneFactory(network, nodeE, nodeF, freeway, simulator, policy)
                .leftToRight(3.0, laneWidth, freewayLane, speedLimit).addLanes(Permeable.BOTH, Permeable.BOTH).getLanes();
        List<Lane> lanesEF2 = new LaneFactory(network, nodeE, nodeF2, freeway, simulator, policy)
                .leftToRight(0.0, laneWidth, freewayLane, speedLimit).addLanes().getLanes();
        List<Lane> lanesFG = new LaneFactory(network, nodeF, nodeG, freeway, simulator, policy)
                .leftToRight(4.0, laneWidth, freewayLane, speedLimit).addLanes(Permeable.LEFT, Permeable.BOTH, Permeable.BOTH)
                .getLanes();
        List<Lane> lanesGH = new LaneFactory(network, nodeG, nodeH, freeway, simulator, policy)
                .leftToRight(3.0, laneWidth, freewayLane, speedLimit).addLanes(Permeable.BOTH, Permeable.BOTH).getLanes();
        List<Lane> lanesGH2 = new LaneFactory(network, nodeG, nodeH2, freeway, simulator, policy)
                .leftToRight(4.0, laneWidth, freewayLane, speedLimit).addLanes().getLanes();

        // Preparation
        LaneAccessLaw law = LaneAccessLaw.LEGAL;
        LateralDirectionality left = LateralDirectionality.LEFT;
        LateralDirectionality right = LateralDirectionality.RIGHT;
        Length range = Length.instantiateSI(10000);

        // Route A -> F2
        Route routeAF2 = network.getShortestRouteBetween(car, nodeA, nodeF2);
        // AB -> F2
        check(network.getLaneChangeInfo(lanesAB.get(0), routeAF2, car, range, law), right, new int[] {1, 2, 4},
                new double[] {200, 500, 1400});
        check(network.getLaneChangeInfo(lanesAB.get(1), routeAF2, car, range, law), right, new int[] {1, 3},
                new double[] {500, 1400});
        check(network.getLaneChangeInfo(lanesAB.get(2), routeAF2, car, range, law), right, new int[] {2}, new double[] {1400});
        check(network.getLaneChangeInfo(lanesAB.get(3), routeAF2, car, range, law), right, new int[] {1}, new double[] {1400});
        // BC -> F2
        check(network.getLaneChangeInfo(lanesBC.get(0), routeAF2, car, range, law), right, new int[] {1, 3},
                new double[] {300, 1200});
        check(network.getLaneChangeInfo(lanesBC.get(1), routeAF2, car, range, law), right, new int[] {2}, new double[] {1200});
        check(network.getLaneChangeInfo(lanesBC.get(2), routeAF2, car, range, law), right, new int[] {1}, new double[] {1200});
        // CD - F2
        assertNull(network.getLaneChangeInfo(lanesCD.get(0), routeAF2, car, range, law));
        check(network.getLaneChangeInfo(lanesCD.get(1), routeAF2, car, range, law), right, new int[] {2}, new double[] {900});
        check(network.getLaneChangeInfo(lanesCD.get(2), routeAF2, car, range, law), right, new int[] {1}, new double[] {900});
        // DE - F2
        assertNull(network.getLaneChangeInfo(lanesDE.get(0), routeAF2, car, range, law));
        check(network.getLaneChangeInfo(lanesDE.get(1), routeAF2, car, range, law), right, new int[] {2}, new double[] {500});
        check(network.getLaneChangeInfo(lanesDE.get(2), routeAF2, car, range, law), right, new int[] {1}, new double[] {500});
        // EF2
        check(network.getLaneChangeInfo(lanesEF2.get(0), routeAF2, car, range, law), null, new int[] {}, new double[] {});

        // Route A -> H2
        Route routeAH2 = network.getShortestRouteBetween(car, nodeA, nodeH2);
        // AB -> H2
        check(network.getLaneChangeInfo(lanesAB.get(0), routeAH2, car, range, law), right, new int[] {1}, new double[] {200});
        check(network.getLaneChangeInfo(lanesAB.get(1), routeAH2, car, range, law), left, new int[] {1}, new double[] {2700});
        check(network.getLaneChangeInfo(lanesAB.get(2), routeAH2, car, range, law), left, new int[] {2}, new double[] {2700});
        check(network.getLaneChangeInfo(lanesAB.get(3), routeAH2, car, range, law), left, new int[] {3}, new double[] {2700});
        // BC -> H2
        check(network.getLaneChangeInfo(lanesBC.get(0), routeAH2, car, range, law), left, new int[] {1}, new double[] {2500});
        check(network.getLaneChangeInfo(lanesBC.get(1), routeAH2, car, range, law), left, new int[] {2}, new double[] {2500});
        check(network.getLaneChangeInfo(lanesBC.get(2), routeAH2, car, range, law), left, new int[] {3}, new double[] {2500});
        // CD -> H2
        check(network.getLaneChangeInfo(lanesCD.get(0), routeAH2, car, range, law), left, new int[] {1}, new double[] {2200});
        check(network.getLaneChangeInfo(lanesCD.get(1), routeAH2, car, range, law), left, new int[] {2}, new double[] {2200});
        check(network.getLaneChangeInfo(lanesCD.get(2), routeAH2, car, range, law), left, new int[] {3}, new double[] {2200});
        // DE -> H2
        check(network.getLaneChangeInfo(lanesDE.get(0), routeAH2, car, range, law), left, new int[] {1}, new double[] {1800});
        check(network.getLaneChangeInfo(lanesDE.get(1), routeAH2, car, range, law), left, new int[] {2}, new double[] {1800});
        check(network.getLaneChangeInfo(lanesDE.get(2), routeAH2, car, range, law), left, new int[] {3}, new double[] {1800});
        assertNull(network.getLaneChangeInfo(lanesDE.get(3), routeAH2, car, range, law));
        // EF -> H2
        check(network.getLaneChangeInfo(lanesEF.get(0), routeAH2, car, range, law), left, new int[] {1}, new double[] {1300});
        check(network.getLaneChangeInfo(lanesEF.get(1), routeAH2, car, range, law), left, new int[] {2}, new double[] {1300});
        check(network.getLaneChangeInfo(lanesEF.get(2), routeAH2, car, range, law), left, new int[] {3}, new double[] {1300});
        // FG -> H2
        check(network.getLaneChangeInfo(lanesFG.get(0), routeAH2, car, range, law), null, new int[] {}, new double[] {});
        check(network.getLaneChangeInfo(lanesFG.get(1), routeAH2, car, range, law), left, new int[] {1}, new double[] {700});
        check(network.getLaneChangeInfo(lanesFG.get(2), routeAH2, car, range, law), left, new int[] {2}, new double[] {700});
        check(network.getLaneChangeInfo(lanesFG.get(3), routeAH2, car, range, law), left, new int[] {3}, new double[] {700});
        // GH2
        check(network.getLaneChangeInfo(lanesGH2.get(0), routeAH2, car, range, law), null, new int[] {}, new double[] {});

        // Route A -> H
        Route routeAH = network.getShortestRouteBetween(car, nodeA, nodeH);
        // AB -> H
        check(network.getLaneChangeInfo(lanesAB.get(0), routeAH, car, range, law), right, new int[] {1}, new double[] {200});
        check(network.getLaneChangeInfo(lanesAB.get(1), routeAH, car, range, law), null, new int[] {}, new double[] {});
        check(network.getLaneChangeInfo(lanesAB.get(2), routeAH, car, range, law), null, new int[] {}, new double[] {});
        check(network.getLaneChangeInfo(lanesAB.get(3), routeAH, car, range, law), null, new int[] {}, new double[] {});
        // BC -> H
        check(network.getLaneChangeInfo(lanesBC.get(0), routeAH, car, range, law), null, new int[] {}, new double[] {});
        check(network.getLaneChangeInfo(lanesBC.get(1), routeAH, car, range, law), null, new int[] {}, new double[] {});
        check(network.getLaneChangeInfo(lanesBC.get(2), routeAH, car, range, law), null, new int[] {}, new double[] {});
        // CD -> H
        check(network.getLaneChangeInfo(lanesCD.get(0), routeAH, car, range, law), null, new int[] {}, new double[] {});
        check(network.getLaneChangeInfo(lanesCD.get(1), routeAH, car, range, law), null, new int[] {}, new double[] {});
        check(network.getLaneChangeInfo(lanesCD.get(2), routeAH, car, range, law), null, new int[] {}, new double[] {});
        // DE -> H
        check(network.getLaneChangeInfo(lanesDE.get(0), routeAH, car, range, law), null, new int[] {}, new double[] {});
        check(network.getLaneChangeInfo(lanesDE.get(1), routeAH, car, range, law), null, new int[] {}, new double[] {});
        check(network.getLaneChangeInfo(lanesDE.get(2), routeAH, car, range, law), null, new int[] {}, new double[] {});
        assertNull(network.getLaneChangeInfo(lanesDE.get(3), routeAH, car, range, law));
        // EF -> H
        check(network.getLaneChangeInfo(lanesEF.get(0), routeAH, car, range, law), null, new int[] {}, new double[] {});
        check(network.getLaneChangeInfo(lanesEF.get(1), routeAH, car, range, law), null, new int[] {}, new double[] {});
        check(network.getLaneChangeInfo(lanesEF.get(2), routeAH, car, range, law), null, new int[] {}, new double[] {});
        // FG -> H
        assertNull(network.getLaneChangeInfo(lanesFG.get(0), routeAH, car, range, law));
        check(network.getLaneChangeInfo(lanesFG.get(1), routeAH, car, range, law), null, new int[] {}, new double[] {});
        check(network.getLaneChangeInfo(lanesFG.get(2), routeAH, car, range, law), null, new int[] {}, new double[] {});
        check(network.getLaneChangeInfo(lanesFG.get(3), routeAH, car, range, law), null, new int[] {}, new double[] {});
        // GH
        check(network.getLaneChangeInfo(lanesGH.get(0), routeAH, car, range, law), null, new int[] {}, new double[] {});
        check(network.getLaneChangeInfo(lanesGH.get(1), routeAH, car, range, law), null, new int[] {}, new double[] {});
        check(network.getLaneChangeInfo(lanesGH.get(2), routeAH, car, range, law), null, new int[] {}, new double[] {});

        // Shorter ranges for AB -> F2
        range = Length.instantiateSI(1000);
        check(network.getLaneChangeInfo(lanesAB.get(0), routeAF2, car, range, law), right, new int[] {1, 2},
                new double[] {200, 500});
        range = Length.instantiateSI(400);
        check(network.getLaneChangeInfo(lanesAB.get(0), routeAF2, car, range, law), right, new int[] {1}, new double[] {200});
        range = Length.instantiateSI(100);
        check(network.getLaneChangeInfo(lanesAB.get(0), routeAF2, car, range, law), null, new int[] {}, new double[] {});

    }

    /**
     * Checks whether lane change info meets the provided direction, number of changes, and distance.
     * @param laneChangeInfos ImmutableSortedSet&lt;LaneChangeInfo&gt;; lane change info to be tested
     * @param lat LateralDirectionality; lateral direction of lane changes
     * @param laneChanges int[]; number of lane changes for consecutive lane change infos
     * @param distances double[]; distance for lane changes for consecutive lane change infos
     */
    private void check(final ImmutableSortedSet<LaneChangeInfo> laneChangeInfos, final LateralDirectionality lat,
            final int[] laneChanges, final double[] distances)
    {
        assertEquals(laneChanges.length, laneChangeInfos.size());
        int i = 0;
        for (LaneChangeInfo laneChangeInfo : laneChangeInfos)
        {
            assertEquals(lat, laneChangeInfo.getLateralDirectionality());
            assertEquals(laneChanges[i], laneChangeInfo.getNumberOfLaneChanges());
            assertTrue(Math.abs(laneChangeInfo.getRemainingDistance().si - distances[i]) < 1e-3);
            i++;
        }
    }

}
