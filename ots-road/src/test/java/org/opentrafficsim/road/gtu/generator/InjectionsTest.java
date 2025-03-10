package org.opentrafficsim.road.gtu.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.naming.NamingException;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.data.Column;
import org.djutils.data.ListTable;
import org.djutils.data.Table;
import org.djutils.draw.point.Point2d;
import org.djutils.exceptions.Try;
import org.djutils.immutablecollections.ImmutableLinkedHashMap;
import org.djutils.immutablecollections.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opentrafficsim.base.geometry.OtsGeometryException;
import org.opentrafficsim.base.geometry.OtsLine2d;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.definitions.Defaults;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.dsol.OtsModelInterface;
import org.opentrafficsim.core.dsol.OtsSimulator;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.perception.HistoryManagerDevs;
import org.opentrafficsim.road.definitions.DefaultsRoadNl;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions.GeneratorLanePosition;
import org.opentrafficsim.road.gtu.generator.LaneBasedGtuGenerator.Placement;
import org.opentrafficsim.road.gtu.generator.characteristics.DefaultLaneBasedGtuCharacteristicsGeneratorOd;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGtuCharacteristics;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtu;
import org.opentrafficsim.road.gtu.lane.tactical.following.IdmPlusFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.DefaultLmrsPerceptionFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.LmrsFactory;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalRoutePlannerFactory;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneGeometryUtil;
import org.opentrafficsim.road.network.lane.LanePosition;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * InjectionsTest.
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class InjectionsTest
{

    /** */
    private InjectionsTest()
    {
        // do not instantiate test class
    }

    /**
     * Test Injections.
     * @throws ParameterException on exception
     * @throws NetworkException on exception
     * @throws GtuException on exception
     * @throws OtsGeometryException on exception
     * @throws NamingException on exception
     * @throws SimRuntimeException on exception
     */
    @SuppressWarnings("checkstyle:methodlength") // don't want to define those columns as properties or again and again
    @Test
    public void testInjections() throws ParameterException, NetworkException, GtuException, OtsGeometryException,
            SimRuntimeException, NamingException
    {
        // columns with correct, and incorrect (xxx2) value type
        Column<Duration> time = new Column<>(Injections.TIME_COLUMN, "", Duration.class, "s");
        Column<Object> time2 = new Column<>(Injections.TIME_COLUMN, "", Object.class, "s");
        Column<String> id = new Column<>(Injections.ID_COLUMN, "", String.class);
        Column<Object> id2 = new Column<>(Injections.ID_COLUMN, "", Object.class);
        Column<String> gtu = new Column<>(Injections.GTU_TYPE_COLUMN, "", String.class);
        Column<Object> gtu2 = new Column<>(Injections.GTU_TYPE_COLUMN, "", Object.class);
        Column<Length> position = new Column<>(Injections.POSITION_COLUMN, "", Length.class, "m");
        Column<Object> position2 = new Column<>(Injections.POSITION_COLUMN, "", Object.class, "m");
        Column<String> lane = new Column<>(Injections.LANE_COLUMN, "", String.class);
        Column<Object> lane2 = new Column<>(Injections.LANE_COLUMN, "", Object.class);
        Column<String> link = new Column<>(Injections.LINK_COLUMN, "", String.class);
        Column<Object> link2 = new Column<>(Injections.LINK_COLUMN, "", Object.class);
        Column<Speed> speed = new Column<>(Injections.SPEED_COLUMN, "", Speed.class, "m/s");
        Column<Object> speed2 = new Column<>(Injections.SPEED_COLUMN, "", Object.class, "m/s");
        Column<String> origin = new Column<>(Injections.ORIGIN_COLUMN, "", String.class);
        Column<Object> origin2 = new Column<>(Injections.ORIGIN_COLUMN, "", Object.class);
        Column<String> destination = new Column<>(Injections.DESTINATION_COLUMN, "", String.class);
        Column<Object> destination2 = new Column<>(Injections.DESTINATION_COLUMN, "", Object.class);
        Column<String> route = new Column<>(Injections.ROUTE_COLUMN, "", String.class);
        Column<Object> route2 = new Column<>(Injections.ROUTE_COLUMN, "", Object.class);
        Column<Length> length = new Column<>(Injections.LENGTH_COLUMN, "", Length.class, "m");
        Column<Object> length2 = new Column<>(Injections.LENGTH_COLUMN, "", Object.class, "m");
        Column<Length> width = new Column<>(Injections.WIDTH_COLUMN, "", Length.class, "m");
        Column<Object> width2 = new Column<>(Injections.WIDTH_COLUMN, "", Object.class, "m");
        Column<Speed> maxspeed = new Column<>(Injections.MAX_SPEED_COLUMN, "", Speed.class, "m/s");
        Column<Object> maxspeed2 = new Column<>(Injections.MAX_SPEED_COLUMN, "", Object.class, "m/s");
        Column<Acceleration> maxacceleration = new Column<>(Injections.MAX_ACCELERATION_COLUMN, "", Acceleration.class, "m/s2");
        Column<Object> maxacceleration2 = new Column<>(Injections.MAX_ACCELERATION_COLUMN, "", Object.class, "m/s2");
        Column<Acceleration> maxdeceleration = new Column<>(Injections.MAX_DECELERATION_COLUMN, "", Acceleration.class, "m/s2");
        Column<Object> maxdeceleration2 = new Column<>(Injections.MAX_DECELERATION_COLUMN, "", Object.class, "m/s2");
        Column<Length> front = new Column<>(Injections.FRONT_COLUMN, "", Length.class, "m");
        Column<Object> front2 = new Column<>(Injections.FRONT_COLUMN, "", Object.class, "m");

        // tests on Injections with only table as input
        Try.testFail(() -> baseInjections(), IllegalArgumentException.class); // need time always
        baseInjections(time);
        Try.testFail(() -> baseInjections(time2), IllegalArgumentException.class); // xxx2 = wrong value type
        baseInjections(time, id);
        Try.testFail(() -> baseInjections(time, id2), IllegalArgumentException.class);
        Try.testFail(() -> baseInjections(time, position).asGeneratorPositions().getAllPositions(),
                IllegalArgumentException.class); // pos, lane, link?
        Try.testFail(() -> baseInjections(time, position2), IllegalArgumentException.class);
        Try.testFail(() -> baseInjections(time, lane).asGeneratorPositions().getAllPositions(), IllegalArgumentException.class);
        Try.testFail(() -> baseInjections(time, lane2), IllegalArgumentException.class);
        Try.testFail(() -> baseInjections(time, link).asGeneratorPositions().getAllPositions(), IllegalArgumentException.class);
        Try.testFail(() -> baseInjections(time, link2), IllegalArgumentException.class);
        // position types are defined
        assertTrue(baseInjections(time, id, position, lane, link).asGeneratorPositions().getAllPositions().isEmpty());
        // no speed
        Try.testFail(() -> baseInjections(time).asRoomChecker().canPlace(null, null, null, null), IllegalStateException.class);
        Try.testFail(() -> baseInjections(time, speed).asRoomChecker().canPlace(null, null, null, null),
                IllegalStateException.class); // no ttc
        Try.testFail(() -> baseInjections(time, speed2), IllegalArgumentException.class);
        Try.testFail(() -> baseInjections(time, gtu), IllegalArgumentException.class); // need full Injections constructor input
        Try.testFail(() -> baseInjections(time, origin), IllegalArgumentException.class);
        Try.testFail(() -> baseInjections(time, destination), IllegalArgumentException.class);
        Try.testFail(() -> baseInjections(time, route), IllegalArgumentException.class);
        Try.testFail(() -> baseInjections(time, length), IllegalArgumentException.class);
        Try.testFail(() -> baseInjections(time, width), IllegalArgumentException.class);
        Try.testFail(() -> baseInjections(time, maxspeed), IllegalArgumentException.class);
        Try.testFail(() -> baseInjections(time, maxacceleration), IllegalArgumentException.class);
        Try.testFail(() -> baseInjections(time, maxdeceleration), IllegalArgumentException.class);
        Try.testFail(() -> baseInjections(time, front), IllegalArgumentException.class);

        // tests on Injections with table as input, and all other input mocked
        mockInjections(time, gtu);
        mockInjections(time, speed);
        // data?
        Try.testFail(() -> mockInjections(time, speed).asRoomChecker().canPlace(null, null, null, null),
                NoSuchElementException.class);
        Try.testFail(() -> mockInjections(time, gtu2), IllegalArgumentException.class);
        Try.testFail(() -> mockInjections(time, origin2), IllegalArgumentException.class);
        Try.testFail(() -> mockInjections(time, destination2), IllegalArgumentException.class);
        Try.testFail(() -> mockInjections(time, route2), IllegalArgumentException.class);
        Try.testFail(() -> mockInjections(time, length2), IllegalArgumentException.class);
        Try.testFail(() -> mockInjections(time, width2), IllegalArgumentException.class);
        Try.testFail(() -> mockInjections(time, maxspeed2), IllegalArgumentException.class);
        Try.testFail(() -> mockInjections(time, maxacceleration2), IllegalArgumentException.class);
        Try.testFail(() -> mockInjections(time, maxdeceleration2), IllegalArgumentException.class);
        Try.testFail(() -> mockInjections(time, front2), IllegalArgumentException.class);

        // tests on arrival times, ids and placement
        ListTable arrivals = new ListTable("id", "", Set.of(id, time, speed));
        arrivals.addRow(Map.of(time, Duration.instantiateSI(1.0), id, "1", speed, Speed.instantiateSI(10.0)));
        arrivals.addRow(Map.of(time, Duration.instantiateSI(3.0), id, "2", speed, Speed.instantiateSI(11.0)));
        arrivals.addRow(Map.of(time, Duration.instantiateSI(6.0), id, "3", speed, Speed.instantiateSI(12.0)));
        arrivals.addRow(Map.of(time, Duration.instantiateSI(10.0), id, "4", speed, Speed.instantiateSI(13.0)));
        arrivals.addRow(Map.of(time, Duration.instantiateSI(15.0), id, "5", speed, Speed.instantiateSI(14.0)));
        arrivals.addRow(Map.of(time, Duration.instantiateSI(21.0), id, "6", speed, Speed.instantiateSI(15.0)));
        Injections arrivalsInjection = new Injections(arrivals, null, null, null, null, null, Duration.ONE);
        LanePosition generationLane = Mockito.mock(LanePosition.class);
        for (int i = 0; i < 6; i++)
        {
            // inter-arrival time
            assertEquals(i + 1, arrivalsInjection.asArrivalsSupplier().get().si, 1e-9);
            // with leader
            SortedSet<HeadwayGtu> leaders = new TreeSet<>();
            HeadwayGtu mockLeader = Mockito.mock(HeadwayGtu.class);
            Mockito.when(mockLeader.getDistance()).thenReturn(Length.ONE);
            Mockito.when(mockLeader.getSpeed()).thenReturn(Speed.ONE);
            leaders.add(mockLeader);
            assertFalse(arrivalsInjection.asRoomChecker().canPlace(leaders, null, Duration.ZERO, generationLane).canPlace());
            // without leader (note that these calls do not increase the row iterator, only inter-arrival time does that)
            Placement p = arrivalsInjection.asRoomChecker().canPlace(Collections.emptySortedSet(), null, Duration.ZERO,
                    generationLane);
            assertEquals(i + 10, p.getSpeed().si, 1e-9);
        }
        assertNull(arrivalsInjection.asArrivalsSupplier().get()); // stop headway generator at end of rows
        // separate for-loop for asynchronous access (i.e. of different rows) for inter-arrival time and id
        for (int i = 0; i < 6; i++)
        {
            assertEquals(String.valueOf(i + 1), arrivalsInjection.asIdSupplier().get());
        }

        // test GTU characteristics and generator positions
        // -- network
        OtsSimulatorInterface simulator = new OtsSimulator("net");
        simulator.initialize(Time.ZERO, Duration.ZERO, Duration.ONE, Mockito.mock(OtsModelInterface.class),
                HistoryManagerDevs.noHistory(simulator));
        RoadNetwork network = new RoadNetwork("network", simulator);
        Node nodeA = new Node(network, "A", new Point2d(0.0, 0.0), Direction.ZERO);
        Node nodeB = new Node(network, "B", new Point2d(100.0, 0.0), Direction.ZERO);
        CrossSectionLink linkAB = new CrossSectionLink(network, "AB", nodeA, nodeB, DefaultsNl.FREEWAY,
                new OtsLine2d(nodeA.getPoint(), nodeB.getPoint()), null, LaneKeepingPolicy.KEEPRIGHT);
        Length laneWidth = Length.instantiateSI(3.5);
        LaneGeometryUtil.createStraightLane(linkAB, "Lane1", Length.ZERO, laneWidth, DefaultsRoadNl.FREEWAY,
                Collections.emptyMap());
        LaneGeometryUtil.createStraightLane(linkAB, "Lane2", laneWidth, laneWidth, DefaultsRoadNl.FREEWAY,
                Collections.emptyMap());
        // -- table
        Table arrivals2 = new ListTable("id", "", Set.of(time, length)); // no GTU type column
        Try.testFail(() -> fullInjections(arrivals2, network).asLaneBasedGtuCharacteristicsGenerator(),
                IllegalStateException.class);
        arrivals = new ListTable("id", "", Set.of(time, gtu, length, position, lane, link));
        arrivals.addRow(Map.of(time, Duration.instantiateSI(1.0), gtu, "NL.CAR", length, Length.instantiateSI(1.0), position,
                Length.instantiateSI(10.0), lane, "Lane1", link, "AB"));
        arrivals.addRow(Map.of(time, Duration.instantiateSI(2.0), gtu, "NL.CAR", length, Length.instantiateSI(2.0), position,
                Length.instantiateSI(20.0), lane, "Lane2", link, "AB"));
        arrivals.addRow(Map.of(time, Duration.instantiateSI(3.0), gtu, "NL.CAR", length, Length.instantiateSI(3.0), position,
                Length.instantiateSI(30.0), lane, "Lane1", link, "AB"));
        arrivals.addRow(Map.of(time, Duration.instantiateSI(4.0), gtu, "NL.CAR", length, Length.instantiateSI(4.0), position,
                Length.instantiateSI(40.0), lane, "Lane2", link, "AB"));
        arrivals.addRow(Map.of(time, Duration.instantiateSI(5.0), gtu, "NL.TRUCK", length, Length.instantiateSI(5.0), position,
                Length.instantiateSI(50.0), lane, "Lane1", link, "AB"));
        arrivals.addRow(Map.of(time, Duration.instantiateSI(6.0), gtu, "NL.TRUCK", length, Length.instantiateSI(6.0), position,
                Length.instantiateSI(60.0), lane, "Lane2", link, "AB"));
        // -- the test
        Injections full = fullInjections(arrivals, network);
        assertEquals(6, full.asGeneratorPositions().getAllPositions().size());
        Try.testFail(() -> full.asLaneBasedGtuCharacteristicsGenerator().draw(), IllegalStateException.class); // first headway
        String[] lanes = new String[] {"Lane1", "Lane2"};
        int laneIndex = 0;
        for (int i = 0; i < 5; i++)
        {
            full.asArrivalsSupplier().get(); // headway
            LaneBasedGtuCharacteristics characteristics = full.asLaneBasedGtuCharacteristicsGenerator().draw();
            assertEquals(i + 1.0, characteristics.getLength().si, 1e-9);
            GeneratorLanePosition p =
                    full.asGeneratorPositions().draw(characteristics.getGtuType(), characteristics, Collections.emptyMap());
            assertEquals(lanes[laneIndex], p.getPosition().lane().getId());
            laneIndex = 1 - laneIndex;
            assertEquals((i + 1) * 10.0, p.getPosition().position().si, 1e-9);
        }
        Try.testFail(() -> full.asLaneBasedGtuCharacteristicsGenerator().draw(), IllegalStateException.class); // consec. draw
    }

    /**
     * Create Injections with only Table input.
     * @param columns columns.
     * @return Injections with only Table input.
     */
    private Injections baseInjections(final Column<?>... columns)
    {
        return new Injections(new ListTable("id", "", Set.of(columns)), null, null, null, null, null, null);
    }

    /**
     * Create Injections with Table input and with other input mocked.
     * @param columns columns.
     * @return Injections with Table input and with other input mocked.
     */
    @SuppressWarnings("unchecked")
    private Injections mockInjections(final Column<?>... columns)
    {
        return new Injections(new ListTable("id", "", Set.of(columns)), Mockito.mock(Network.class),
                Mockito.mock(ImmutableMap.class), Defaults.NL, Mockito.mock(LaneBasedStrategicalPlannerFactory.class),
                Mockito.mock(StreamInterface.class), Duration.ONE);
    }

    /**
     * Create Injections with Table and default other input.
     * @param table table.
     * @param network network.
     * @return Injections with Table input and with other input mocked.
     */
    private Injections fullInjections(final Table table, final Network network)
    {
        StreamInterface stream = new MersenneTwister();
        ImmutableMap<String, GtuType> gtuTypes = new ImmutableLinkedHashMap<>(
                Map.of(DefaultsNl.CAR.getId(), DefaultsNl.CAR, DefaultsNl.TRUCK.getId(), DefaultsNl.TRUCK));
        return new Injections(table, network, gtuTypes, Defaults.NL,
                DefaultLaneBasedGtuCharacteristicsGeneratorOd.defaultLmrs(stream), stream, Duration.ONE);
    }

    /**
     * Tests whether Injection ids are used even when a GTU is delayed due to a lack of space.
     * @throws NetworkException exception
     * @throws ParameterException exception
     * @throws SimRuntimeException exception
     * @throws NamingException exception
     */
    @Test
    public void testIdorder() throws NetworkException, SimRuntimeException, ParameterException, NamingException
    {
        // A small test network with two completely separated lanes on different links
        OtsSimulatorInterface simulator = new OtsSimulator("simulator");
        simulator.initialize(Time.ZERO, Duration.ZERO, Duration.instantiateSI(3600.0), Mockito.mock(OtsModelInterface.class),
                HistoryManagerDevs.noHistory(simulator));
        RoadNetwork network = new RoadNetwork("network", simulator);
        Point2d pointA = new Point2d(0.0, 0.0);
        Point2d pointB = new Point2d(0.0, 1000.0);
        Point2d pointC = new Point2d(10.0, 0.0);
        Point2d pointD = new Point2d(10.0, 1000.0);
        Node nodeA = new Node(network, "A", pointA);
        Node nodeB = new Node(network, "B", pointB);
        Node nodeC = new Node(network, "C", pointC);
        Node nodeD = new Node(network, "D", pointD);
        CrossSectionLink linkAB = new CrossSectionLink(network, "AB", nodeA, nodeB, DefaultsNl.FREEWAY,
                new OtsLine2d(pointA, pointB), null, LaneKeepingPolicy.KEEPRIGHT);
        CrossSectionLink linkCD = new CrossSectionLink(network, "CD", nodeC, nodeD, DefaultsNl.FREEWAY,
                new OtsLine2d(pointC, pointD), null, LaneKeepingPolicy.KEEPRIGHT);
        Map<GtuType, Speed> speedLimit = Map.of(DefaultsNl.CAR, Speed.instantiateSI(25.0));
        Lane lane1 = LaneGeometryUtil.createStraightLane(linkAB, "lane1", Length.ZERO, Length.instantiateSI(3.5),
                DefaultsRoadNl.FREEWAY, speedLimit);
        Lane lane2 = LaneGeometryUtil.createStraightLane(linkCD, "lane2", Length.ZERO, Length.instantiateSI(3.5),
                DefaultsRoadNl.FREEWAY, speedLimit);

        // Columns
        Column<Duration> time = new Column<>(Injections.TIME_COLUMN, "", Duration.class, "s");
        Column<String> id = new Column<>(Injections.ID_COLUMN, "", String.class);
        Column<String> gtu = new Column<>(Injections.GTU_TYPE_COLUMN, "", String.class);
        Column<Length> position = new Column<>(Injections.POSITION_COLUMN, "", Length.class, "m");
        Column<String> lane = new Column<>(Injections.LANE_COLUMN, "", String.class);
        Column<String> link = new Column<>(Injections.LINK_COLUMN, "", String.class);
        Column<Speed> speed = new Column<>(Injections.SPEED_COLUMN, "", Speed.class, "m/s");

        // Create arrivals, with 2 GTUs on lane 1 that are too close for direct generation
        ListTable arrivals = new ListTable("id", "", Set.of(id, time, gtu, position, lane, link, speed));
        arrivals.addRow(Map.of(time, Duration.instantiateSI(1.0), id, "1", gtu, "NL.CAR", position, Length.instantiateSI(10.0),
                lane, "lane1", link, "AB", speed, Speed.instantiateSI(5.0)));
        arrivals.addRow(Map.of(time, Duration.instantiateSI(1.5), id, "2", gtu, "NL.CAR", position, Length.instantiateSI(10.0),
                lane, "lane1", link, "AB", speed, Speed.instantiateSI(25.0)));
        arrivals.addRow(Map.of(time, Duration.instantiateSI(1.6), id, "3", gtu, "NL.CAR", position, Length.instantiateSI(10.0),
                lane, "lane2", link, "CD", speed, Speed.instantiateSI(25.0)));

        // Create the generator and its components
        ImmutableMap<String, GtuType> gtuTypes = new ImmutableLinkedHashMap<>(Map.of("NL.CAR", DefaultsNl.CAR));
        StreamInterface stream = new MersenneTwister();
        LmrsFactory tacticalFactory = new LmrsFactory(new IdmPlusFactory(stream), new DefaultLmrsPerceptionFactory());
        LaneBasedStrategicalRoutePlannerFactory strategicalPlannerFactory =
                new LaneBasedStrategicalRoutePlannerFactory(tacticalFactory);
        Injections injections = new Injections(arrivals, network, gtuTypes, Defaults.NL, strategicalPlannerFactory, stream,
                Duration.instantiateSI(60.0));
        new LaneBasedGtuGenerator("id", injections.asArrivalsSupplier(), injections.asLaneBasedGtuCharacteristicsGenerator(),
                injections.asGeneratorPositions(), network, simulator, injections.asRoomChecker(), injections.asIdSupplier());

        // Simulate till 1.7s and check that GTU 2 was not yet generated
        while (simulator.getSimulatorTime().si < 1.7)
        {
            simulator.step();
        }
        assertEquals(1, lane1.getGtuList().size(), "Lane1 should have 1 GTU as the second is too close.");
        assertEquals(1, lane2.getGtuList().size(), "Lane2 should have 1 GTU.");
        assertEquals("1", lane1.getGtuList().get(0).getId(), "GTU on lane 1 should have id \"1\".");
        assertEquals("3", lane2.getGtuList().get(0).getId(), "GTU on lane 1 should have id \"3\".");
        // Simulate till 20.0s and check that GTU 2 was generated
        while (simulator.getSimulatorTime().si < 20.0)
        {
            simulator.step();
        }
        assertEquals(2, lane1.getGtuList().size(), "Lane1 should have 2 GTUs after some simulation time.");
        assertTrue(lane1.getGtuList().get(0).getId().equals("2") || lane1.getGtuList().get(1).getId().equals("2"),
                "Lane1 does not have GTU \"2\" after some simulation time.");
    }

}
