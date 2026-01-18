package org.opentrafficsim.road.gtu.strategical.od;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import javax.naming.NamingException;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.ValueRuntimeException;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djunits.value.vdouble.vector.DurationVector;
import org.djunits.value.vdouble.vector.FrequencyVector;
import org.djutils.draw.point.Point2d;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.opentrafficsim.base.geometry.OtsGeometryException;
import org.opentrafficsim.base.geometry.OtsLine2d;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.dsol.OtsReplication;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.perception.HistoryManagerDevs;
import org.opentrafficsim.road.definitions.DefaultsRoadNl;
import org.opentrafficsim.road.gtu.generator.characteristics.DefaultLaneBasedGtuCharacteristicsGeneratorOd;
import org.opentrafficsim.road.gtu.generator.characteristics.DefaultLaneBasedGtuCharacteristicsGeneratorOd.Factory;
import org.opentrafficsim.road.gtu.generator.headway.ArrivalsHeadwayGenerator.HeadwayDistribution;
import org.opentrafficsim.road.network.LaneKeepingPolicy;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneGeometryUtil;
import org.opentrafficsim.road.od.Categorization;
import org.opentrafficsim.road.od.Category;
import org.opentrafficsim.road.od.Interpolation;
import org.opentrafficsim.road.od.OdApplier;
import org.opentrafficsim.road.od.OdApplier.GeneratorObjects;
import org.opentrafficsim.road.od.OdMatrix;
import org.opentrafficsim.road.od.OdOptions;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.experiment.SingleReplication;
import nl.tudelft.simulation.dsol.model.DsolModel;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class OdApplierTest
{

    /** Verbose test. */
    private static final boolean VERBOSE = false;

    /** Local time object used in simulator MockUp. Can be set for testing at different simulation times. */
    private Time time;

    /** Random number stream. */
    private StreamInterface stream = new MersenneTwister(1L);

    /** MockUp replication. */
    private OtsReplication replication;

    /** MockUp simulator. */
    private OtsSimulatorInterface simulator;

    /** Mockup model. */
    private DsolModel model;

    /** Network. */
    private RoadNetwork network;

    /** Lanes. */
    private final Map<String, Lane> lanes = new LinkedHashMap<>();

    /**
     * Returns simulator.
     * @return a mock of the simulator that uses this.time as the time for getSimulatorTime()
     */
    private OtsSimulatorInterface createSimulatorMock()
    {
        OtsSimulatorInterface simulatorMock = Mockito.mock(OtsSimulatorInterface.class);
        Answer<Duration> answerDuration = new Answer<Duration>()
        {
            @Override
            public Duration answer(final InvocationOnMock invocation) throws Throwable
            {
                return OdApplierTest.this.time.minus(Time.ZERO);
            }
        };
        Mockito.when(simulatorMock.getSimulatorTime()).then(answerDuration);
        Mockito.when(simulatorMock.getReplication()).thenReturn(this.replication);
        Mockito.when(simulatorMock.getModel()).thenReturn(this.model);
        return simulatorMock;
    }

    /**
     * Returns model.
     * @return a mock of the model
     */
    private DsolModel createModelMock()
    {
        DsolModel modelMock = Mockito.mock(DsolModel.class);
        Mockito.when(modelMock.getStream("generation")).thenReturn(new MersenneTwister(1L));
        Mockito.when(modelMock.getStream("default")).thenReturn(new MersenneTwister(2L));
        return modelMock;
    }

    /**
     * Constructor.
     * @throws NetworkException on exception
     * @throws NamingException on ...
     */
    public OdApplierTest() throws NetworkException, NamingException
    {
        this.model = createModelMock();
        this.simulator = createSimulatorMock();
        HistoryManagerDevs historyManager = Mockito.mock(HistoryManagerDevs.class);
        this.replication = new OtsReplication("replication for ODApplierTest", Time.ZERO, Duration.ZERO, Duration.ofSI(10.0),
                historyManager);
        Mockito.when(this.simulator.getReplication()).thenReturn(this.replication);
        this.time = Time.ZERO;
        makeNetwork();
    }

    /**
     * Make network.
     * @throws NetworkException on network exception
     */
    private void makeNetwork() throws NetworkException
    {
        this.network = new RoadNetwork("ODApplierExample", this.simulator);
        Point2d pointA = new Point2d(0, 0);
        Point2d pointB = new Point2d(1000, 0);
        Node nodeA = new Node(this.network, "A", pointA, Direction.ZERO);
        Node nodeB = new Node(this.network, "B", pointB, Direction.ZERO);
        CrossSectionLink linkAB = new CrossSectionLink(this.network, "AB", nodeA, nodeB, DefaultsNl.ROAD,
                new OtsLine2d(pointA, pointB), null, LaneKeepingPolicy.KEEPRIGHT);
        this.lanes.put("lane1",
                LaneGeometryUtil.createStraightLane(linkAB, "lane1", Length.ofSI(1.75), Length.ofSI(1.75), Length.ofSI(3.5),
                        Length.ofSI(3.5), DefaultsRoadNl.HIGHWAY,
                        Map.of(DefaultsNl.VEHICLE, new Speed(120, SpeedUnit.KM_PER_HOUR))));
        this.lanes.put("lane2",
                LaneGeometryUtil.createStraightLane(linkAB, "lane2", Length.ofSI(-1.75), Length.ofSI(-1.75), Length.ofSI(3.5),
                        Length.ofSI(3.5), DefaultsRoadNl.HIGHWAY,
                        Map.of(DefaultsNl.VEHICLE, new Speed(120, SpeedUnit.KM_PER_HOUR))));
        Set<GtuType> gtuTypes = new LinkedHashSet<>();
        gtuTypes.add(DefaultsNl.VEHICLE);
    }

    /**
     * Returns the set simulation time.
     * @return set simulation time
     */
    final Time getTime()
    {
        return this.time;
    }

    /**
     * Returns the set random number stream.
     * @return set random number stream
     */
    final StreamInterface getStream()
    {
        return this.stream;
    }

    /**
     * Returns the replication.
     * @return replication
     */
    final SingleReplication<Duration> getReplication()
    {
        return this.replication;
    }

    /**
     * Returns the simulator.
     * @return simulator
     */
    final OtsSimulatorInterface getSimulator()
    {
        return this.simulator;
    }

    /**
     * Test whether correct headways and frequencies are calculated.
     * @throws NetworkException on exception
     * @throws ValueRuntimeException on exception
     * @throws SimRuntimeException on exception
     * @throws ParameterException on exception
     * @throws IllegalAccessException on exception
     * @throws IllegalArgumentException on exception
     * @throws OtsGeometryException on exception
     */
    @Test
    public void headwayGeneratorTest() throws ValueRuntimeException, NetworkException, ParameterException, SimRuntimeException,
            IllegalArgumentException, IllegalAccessException, OtsGeometryException
    {

        this.time = Time.ZERO;
        Node nodeA = this.network.getNode("A").get();
        Node nodeB = this.network.getNode("B").get();
        Lane lane1 = this.lanes.get("lane1");
        Lane lane2 = this.lanes.get("lane2");

        // options
        OdOptions odOptions = new OdOptions().set(OdOptions.HEADWAY_DIST, HeadwayDistribution.CONSTANT);

        // Stepwise interpolation with constant headways tests
        OdMatrix od = getOD(new double[] {100, 200, 300, 400, 500, 600}, new double[] {1000, 2000, 0, 0, 2000, 0},
                Interpolation.STEPWISE, nodeA, nodeB, lane1, lane2);
        Map<String, GeneratorObjects> generatorObjects = OdApplier.applyOd(this.network, od, odOptions, DefaultsNl.ROAD_USERS);
        assertEquals(generatorObjects.size(), 2, "Incorrect number of generator created or returned.");
        for (String id : generatorObjects.keySet())
        {
            Supplier<Duration> headwayGenerator = generatorObjects.get(id).headwayGenerator();
            double factor = id.equals("A1") ? 0.4 : 0.6;
            // now check various points in time
            this.time = Time.ofSI(0); // spanning initial 0-demand period
            assertAboutEqual(headwayGenerator.get(), 100 + 1 / (factor * 1000 / 3600));
            this.time = Time.ofSI(30); // spanning 0-demand period partially
            assertAboutEqual(headwayGenerator.get(), 70 + 1 / (factor * 1000 / 3600));
            this.time = Time.ofSI(100); // start of demand period
            assertAboutEqual(headwayGenerator.get(), 1 / (factor * 1000 / 3600));
            this.time = Time.ofSI(130); // middle of demand period
            assertAboutEqual(headwayGenerator.get(), 1 / (factor * 1000 / 3600));
            this.time = Time.ofSI(199); // over slice edge
            double preSlice = factor * 1000 / 3600;
            assertAboutEqual(headwayGenerator.get(), 1 + (1 - preSlice) / (factor * 2000 / 3600));
            this.time = Time.ofSI(299); // spanning 0-demand period in the middle
            preSlice = factor * 2000 / 3600;
            assertAboutEqual(headwayGenerator.get(), 201 + (1 - preSlice) / (factor * 2000 / 3600));
            this.time = Time.ofSI(599); // just before end
            assertEquals(headwayGenerator.get(), null);
            this.time = Time.ofSI(600); // on end
            assertEquals(headwayGenerator.get(), null);
            this.time = Time.ofSI(700); // beyond end
            assertEquals(headwayGenerator.get(), null);
        }

        // new network to avoid placing double sinks...
        makeNetwork();

        // Linear interpolation with constant headways tests
        this.time = Time.ZERO;
        od = getOD(new double[] {100, 200, 300, 400, 500, 600}, new double[] {1000, 2000, 0, 0, 2000, 0}, Interpolation.LINEAR,
                nodeA, nodeB, lane1, lane2);
        generatorObjects = OdApplier.applyOd(this.network, od, odOptions, DefaultsNl.ROAD_USERS);
        assertEquals(generatorObjects.size(), 2, "Incorrect number of generator created or returned.");
        for (String id : generatorObjects.keySet())
        {
            Supplier<Duration> headwayGenerator = generatorObjects.get(id).headwayGenerator();
            double factor = id.equals("A1") ? 0.4 : 0.6;
            // now check various points in time
            this.time = Time.ofSI(0); // spanning initial 0-demand period
            double inv = inverseTrapezoidal(1.0, 100, 1000, 200, 2000, 100, factor);
            assertAboutEqual(headwayGenerator.get(), 100 + inv);
            this.time = Time.ofSI(30); // spanning 0-demand period partially
            assertAboutEqual(headwayGenerator.get(), 70 + inv);
            this.time = Time.ofSI(100); // start of demand period
            assertAboutEqual(headwayGenerator.get(), inv);
            this.time = Time.ofSI(130); // middle of demand period
            assertAboutEqual(headwayGenerator.get(), inverseTrapezoidal(1.0, 100, 1000, 200, 2000, 130, factor));
            this.time = Time.ofSI(199); // over slice edge
            double preSlice = trapezoidal(100, 1000, 200, 2000, 199, 200, factor);
            assertAboutEqual(headwayGenerator.get(), 1 + inverseTrapezoidal(1.0 - preSlice, 200, 2000, 300, 0, 200, factor));
            this.time = Time.ofSI(299); // spanning 0-demand period in the middle
            preSlice = trapezoidal(200, 2000, 300, 0, 299, 300, factor);
            assertAboutEqual(headwayGenerator.get(), 101 + inverseTrapezoidal(1.0 - preSlice, 400, 0, 500, 2000, 400, factor));
            this.time = Time.ofSI(599); // just before end
            assertEquals(headwayGenerator.get(), null);
            this.time = Time.ofSI(600); // on end
            assertEquals(headwayGenerator.get(), null);
            this.time = Time.ofSI(700); // beyond end
            assertEquals(headwayGenerator.get(), null);
        }

        // All interpolations and randomizations tests (only total test)
        Field[] headwayFields = HeadwayDistribution.class.getDeclaredFields();
        for (Field headwayField : headwayFields)
        {
            if (headwayField.getType().equals(HeadwayDistribution.class))
            {
                HeadwayDistribution headwayRandomization = (HeadwayDistribution) headwayField.get(null);
                for (Interpolation interpolation : Interpolation.values())
                {
                    this.time = Time.ZERO;
                    odOptions = new OdOptions().set(OdOptions.HEADWAY_DIST, headwayRandomization);
                    od = getOD(new double[] {1200, 2400, 3600, 4800, 6000, 7200}, new double[] {1000, 2000, 0, 0, 2000, 0},
                            interpolation, nodeA, nodeB, lane1, lane2);
                    generatorObjects = OdApplier.applyOd(this.network, od, odOptions, DefaultsNl.ROAD_USERS);
                    assertEquals(generatorObjects.size(), 2, "Incorrect number of generators created or returned.");
                    for (String id : generatorObjects.keySet())
                    {
                        Supplier<Duration> headwayGenerator = generatorObjects.get(id).headwayGenerator();
                        double factor = id.equals("A1") ? 0.4 : 0.6;
                        double n = 0;
                        int nSims = 10;
                        for (int i = 0; i < nSims; i++) // 10 simulations
                        {
                            // simulate entire demand period and check total number of vehicles
                            while (this.time.si < 7200)
                            {
                                Duration headway = headwayGenerator.get();
                                if (headway != null)
                                {
                                    n++;
                                    this.time = this.time.plus(headway);
                                }
                                else
                                {
                                    this.time = Time.ofSI(7200);
                                }
                            }
                            this.time = Time.ZERO;
                        }
                        double nDemand;
                        if (interpolation.isStepWise())
                        {
                            nDemand = factor * 1200 * (1000 + 2000 + 2000) / 3600;
                        }
                        else
                        {
                            nDemand = factor * 1200 * (1500 + 1000 + 1000 + 1000) / 3600;
                        }
                        n /= nSims;
                        double p = 100 * (n / nDemand - 1);
                        if (VERBOSE)
                        {
                            System.out.println(String.format(
                                    "A demand of %.2f resulted in %.0f vehicles (%s%.2f%%) "
                                            + "as mean over %d simulations (%s demand, %s headways).",
                                    nDemand, n, p > 0 ? "+" : "", p, nSims, interpolation.name(),
                                    headwayRandomization.getName()));
                        }
                        assertTrue(Math.abs(p) < 5,
                                String.format("Demand generated with exponential headways was more than 5%% off (%s%.2f%%).",
                                        p > 0 ? "+" : "", p));
                    }
                }
            }
        }

    }

    /**
     * Creates an OD from input.
     * @param timeVec time vector [s]
     * @param demandVec demand vector [veh/h]
     * @param interpolation interpolation
     * @param nodeA from node
     * @param nodeB to node
     * @param lane1 lane 1 (60% of all traffic, all cars)
     * @param lane2 lane 2 (40% of all traffic, half of which is trucks)
     * @return OD
     * @throws ValueRuntimeException on exception
     * @throws NetworkException on exception
     */
    private OdMatrix getOD(final double[] timeVec, final double[] demandVec, final Interpolation interpolation,
            final Node nodeA, final Node nodeB, final Lane lane1, final Lane lane2)
            throws ValueRuntimeException, NetworkException
    {
        Categorization categorization = new Categorization("ODExample", Lane.class, GtuType.class, Route.class);
        List<Node> origins = new ArrayList<>();
        origins.add(nodeA);
        List<Node> destinations = new ArrayList<>();
        destinations.add(nodeB);
        DurationVector timeVector = new DurationVector(timeVec, DurationUnit.SECOND);
        OdMatrix od = new OdMatrix("ODExample", origins, destinations, categorization, timeVector, interpolation);
        FrequencyVector demand = new FrequencyVector(demandVec, FrequencyUnit.PER_HOUR);
        GtuType gtuType = DefaultsNl.CAR;
        Route route = new Route("AB", gtuType).addNode(nodeA).addNode(nodeB);
        Category category = new Category(categorization, lane1, DefaultsNl.CAR, route);
        od.putDemandVector(nodeA, nodeB, category, demand, timeVector, interpolation, .6);
        category = new Category(categorization, lane2, DefaultsNl.CAR, route);
        od.putDemandVector(nodeA, nodeB, category, demand, timeVector, interpolation, .2);
        category = new Category(categorization, lane2, DefaultsNl.TRUCK, route);
        od.putDemandVector(nodeA, nodeB, category, demand, timeVector, interpolation, .2);
        return od;
    }

    /**
     * Trapezoidal integration for linearly interpolated demand.
     * @param t1 start time of demand period [s]
     * @param f1 start frequency of demand period [veh/h]
     * @param t2 end time of demand period [s]
     * @param f2 end frequency of demand period [veh/h]
     * @param tFrom start time of integrated period [s]
     * @param tTo end time of integrated period [s]
     * @param factor demand factor [-]
     * @return integrated demand [veh]
     */
    private double trapezoidal(final double t1, final double f1, final double t2, final double f2, final double tFrom,
            final double tTo, final double factor)
    {
        double f1si = f1 / 3600;
        double f2si = f2 / 3600;
        f1si *= factor;
        f2si *= factor;
        double f = (tFrom - t1) / (t2 - t1);
        double ff1 = (1 - f) * f1si + f * f2si;
        f = (tTo - t1) / (t2 - t1);
        double ff2 = (1 - f) * f1si + f * f2si;
        return .5 * (ff1 + ff2) * (tTo - tFrom);
    }

    /**
     * Inverse trapezoidal rule. Finds the time such that demand integrates to a value of {@code rem}. This function is only
     * valid if demand {@code rem} is present before the demand line crosses 0.
     * @param rem remainder to integrate to [veh]
     * @param t1 start time of demand period [s]
     * @param f1 start frequency of demand period [veh/h]
     * @param t2 end time of demand period [s]
     * @param f2 end frequency of demand period [veh/h]
     * @param tFrom start time of integrated period [s]
     * @param factor demand factor [-]
     * @return time [s]
     */
    private double inverseTrapezoidal(final double rem, final double t1, final double f1, final double t2, final double f2,
            final double tFrom, final double factor)
    {
        double f1si = f1 / 3600;
        double f2si = f2 / 3600;
        f1si *= factor;
        f2si *= factor;
        double slope = (f2si - f1si) / (t2 - t1);
        double f = (tFrom - t1) / (t2 - t1);
        double ff1 = (1 - f) * f1si + f * f2si;
        return (-ff1 + Math.sqrt(2 * slope * rem + ff1 * ff1)) / slope;
    }

    /**
     * Asserts whether two numbers are about equal (within 0.1).
     * @param num1 number 1
     * @param num2 number 2
     */
    private void assertAboutEqual(final Number num1, final Number num2)
    {
        String message = "Values " + num1 + " and " + num2 + " are not equal.";
        assertAboutEqual(message, num1, num2);
    }

    /**
     * Asserts whether two numbers are about equal (within 0.1).
     * @param message message on failure
     * @param num1 number 1
     * @param num2 number 2
     */
    private void assertAboutEqual(final String message, final Number num1, final Number num2)
    {
        assertTrue(Math.abs(num1.doubleValue() - num2.doubleValue()) < 0.1, message);
    }

    /**
     * Test fraction.
     * @throws ValueRuntimeException on exception
     * @throws NetworkException on exception
     * @throws SimRuntimeException on exception
     * @throws ParameterException on exception
     * @throws GtuException on exception
     */
    @Test
    public void gtuFractionTest()
            throws ValueRuntimeException, NetworkException, ParameterException, SimRuntimeException, GtuException
    {
        this.time = Time.ZERO;
        Node nodeA = this.network.getNode("A").get();
        Node nodeB = this.network.getNode("B").get();
        Lane lane1 = this.lanes.get("lane1");
        Lane lane2 = this.lanes.get("lane2");
        OdOptions odOptions = new OdOptions().set(OdOptions.HEADWAY_DIST, HeadwayDistribution.CONSTANT);
        Factory factory = new Factory(DefaultLaneBasedGtuCharacteristicsGeneratorOd.defaultLmrs(new MersenneTwister()));
        odOptions.set(OdOptions.GTU_TYPE, factory.create());
        OdMatrix od = getOD(new double[] {0, 100, 200}, new double[] {1000, 1500, 0}, Interpolation.LINEAR, nodeA, nodeB, lane1,
                lane2);
        Map<String, GeneratorObjects> generatorObjects = OdApplier.applyOd(this.network, od, odOptions, DefaultsNl.ROAD_USERS);
        int nTot = 1000;
        int nCar = nTot / 2;
        int nTruck = nTot / 2;
        for (int i = 1; i < 3; i++)
        {
            for (double t = 40; t < 200; t += 100)
            {
                Map<GtuType, Integer> counts = new LinkedHashMap<>();
                counts.put(DefaultsNl.CAR, 0);
                counts.put(DefaultsNl.TRUCK, 0);
                for (int j = 0; j < nTot; j++)
                {
                    GtuType type = generatorObjects.get("A" + i).characteristicsGenerator().draw().getGtuType();
                    if (counts.containsKey(type))
                    {
                        counts.put(type, counts.get(type) + 1);
                    }
                    else
                    {
                        fail("Vehicle generated from OD is of GtuType (" + type.getId() + ") that is not in the OD.");
                    }
                }
                assertTrue(Math.abs(counts.get(DefaultsNl.CAR) - nCar) < nTot * .05,
                        String.format("Generated number of CARs (%d) deviates too much from the expected value (%d).",
                                counts.get(DefaultsNl.CAR), nCar));
                assertTrue(Math.abs(counts.get(DefaultsNl.TRUCK) - nTruck) < nTot * .05,
                        String.format("Generated number of TRUCKs (%d) deviates too much from the expected value (%d).",
                                counts.get(DefaultsNl.TRUCK), nTruck));
                if (VERBOSE)
                {
                    System.out.println(
                            String.format("Generated %d CARs for expected value %d.", counts.get(DefaultsNl.CAR), nCar));
                    System.out.println(
                            String.format("Generated %d TRUCKs for expected value %d.", counts.get(DefaultsNl.TRUCK), nTruck));
                }
            }
            nCar = 1000;
            nTruck = 0;
        }
    }

}
