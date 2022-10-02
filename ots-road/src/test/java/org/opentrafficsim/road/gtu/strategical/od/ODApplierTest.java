package org.opentrafficsim.road.gtu.strategical.od;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;

import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.ValueRuntimeException;
import org.djunits.value.storage.StorageType;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djunits.value.vdouble.vector.FrequencyVector;
import org.djunits.value.vdouble.vector.TimeVector;
import org.djunits.value.vdouble.vector.base.DoubleVector;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.distributions.Generator;
import org.opentrafficsim.core.distributions.ProbabilityException;
import org.opentrafficsim.core.dsol.OtsReplication;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.perception.HistoryManager;
import org.opentrafficsim.core.perception.HistoryManagerDEVS;
import org.opentrafficsim.road.gtu.generator.headway.ArrivalsHeadwayGenerator.HeadwayDistribution;
import org.opentrafficsim.road.gtu.generator.od.ODApplier;
import org.opentrafficsim.road.gtu.generator.od.ODApplier.GeneratorObjects;
import org.opentrafficsim.road.gtu.generator.od.ODOptions;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.OTSRoadNode;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.experiment.SingleReplication;
import nl.tudelft.simulation.dsol.model.DSOLModel;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class ODApplierTest
{

    /** Local time object used in simulator MockUp. Can be set for testing at different simulation times. */
    private Time time;

    /** Random number stream. */
    private StreamInterface stream = new MersenneTwister(1L);

    /** MockUp replication. */
    private OtsReplication replication;

    /** MockUp simulator. */
    private OtsSimulatorInterface simulator;

    /** Mockup model. */
    private DSOLModel model;

    /** Network. */
    private OTSRoadNetwork network;

    /** History manager. */
    private HistoryManager historyManager;

    /** Lanes. */
    private final Map<String, Lane> lanes = new LinkedHashMap<>();

    /**
     * @return a mock of the simulator that uses this.time as the time for getSimulatorTime()
     */
    private OtsSimulatorInterface createSimulatorMock()
    {
        OtsSimulatorInterface simulatorMock = Mockito.mock(OtsSimulatorInterface.class);
        Answer<Time> answerTime = new Answer<Time>()
        {
            @Override
            public Time answer(final InvocationOnMock invocation) throws Throwable
            {
                return ODApplierTest.this.time;
            }
        };
        Mockito.when(simulatorMock.getSimulatorAbsTime()).then(answerTime);
        Answer<Duration> answerDuration = new Answer<Duration>()
        {
            @Override
            public Duration answer(final InvocationOnMock invocation) throws Throwable
            {
                return ODApplierTest.this.time.minus(Time.ZERO);
            }
        };
        Mockito.when(simulatorMock.getSimulatorTime()).then(answerDuration);
        Mockito.when(simulatorMock.getReplication()).thenReturn(this.replication);
        Mockito.when(simulatorMock.getModel()).thenReturn(this.model);
        return simulatorMock;
    }

    /**
     * @return a mock of the model
     */
    private DSOLModel createModelMock()
    {
        DSOLModel modelMock = Mockito.mock(DSOLModel.class);
        Mockito.when(modelMock.getStream("generation")).thenReturn(new MersenneTwister(1L));
        Mockito.when(modelMock.getStream("default")).thenReturn(new MersenneTwister(2L));
        return modelMock;
    }

    /**
     * Constructor.
     * @throws OTSGeometryException on exception
     * @throws NetworkException on exception
     * @throws NamingException on ...
     */
    public ODApplierTest() throws NetworkException, OTSGeometryException, NamingException
    {
        this.replication =
                new OtsReplication("replication for ODApplierTest", Time.ZERO, Duration.ZERO, Duration.instantiateSI(10.0));
        this.model = createModelMock();
        System.out.println(this.model);
        this.simulator = createSimulatorMock();
        System.out.println(this.simulator);
        System.out.println(this.simulator.getModel());
        this.historyManager = new HistoryManagerDEVS(this.simulator, Duration.instantiateSI(10.0), Duration.instantiateSI(1.0));
        this.time = Time.ZERO;
        makeNetwork();
    }

    /**
     * @throws NetworkException on network exception
     * @throws OTSGeometryException on geometry exception
     */
    private void makeNetwork() throws NetworkException, OTSGeometryException
    {
        this.network = new OTSRoadNetwork("ODApplierExample", true, this.simulator);
        OTSPoint3D pointA = new OTSPoint3D(0, 0, 0);
        OTSPoint3D pointB = new OTSPoint3D(1000, 0, 0);
        OTSRoadNode nodeA = new OTSRoadNode(this.network, "A", pointA, Direction.ZERO);
        OTSRoadNode nodeB = new OTSRoadNode(this.network, "B", pointB, Direction.ZERO);
        CrossSectionLink linkAB = new CrossSectionLink(this.network, "AB", nodeA, nodeB,
                this.network.getLinkType(LinkType.DEFAULTS.ROAD), new OTSLine3D(pointA, pointB), LaneKeepingPolicy.KEEPRIGHT);
        this.lanes.put("lane1", new Lane(linkAB, "lane1", Length.instantiateSI(1.75), Length.instantiateSI(3.5),
                this.network.getLaneType(LaneType.DEFAULTS.HIGHWAY), new Speed(120, SpeedUnit.KM_PER_HOUR)));
        this.lanes.put("lane2", new Lane(linkAB, "lane2", Length.instantiateSI(-1.75), Length.instantiateSI(3.5),
                this.network.getLaneType(LaneType.DEFAULTS.HIGHWAY), new Speed(120, SpeedUnit.KM_PER_HOUR)));
        Set<GtuType> gtuTypes = new LinkedHashSet<>();
        gtuTypes.add(this.network.getGtuType(GtuType.DEFAULTS.VEHICLE));
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
     * @throws ProbabilityException on exception
     * @throws IllegalAccessException on exception
     * @throws IllegalArgumentException on exception
     * @throws OTSGeometryException on exception
     */
    @Test
    public void headwayGeneratorTest() throws ValueRuntimeException, NetworkException, ParameterException, SimRuntimeException,
            ProbabilityException, IllegalArgumentException, IllegalAccessException, OTSGeometryException
    {

        this.time = Time.ZERO;
        Node nodeA = this.network.getNode("A");
        Node nodeB = this.network.getNode("B");
        Lane lane1 = this.lanes.get("lane1");
        Lane lane2 = this.lanes.get("lane2");

        // options
        ODOptions odOptions = new ODOptions().set(ODOptions.HEADWAY_DIST, HeadwayDistribution.CONSTANT);

        // Stepwise interpolation with constant headways tests
        ODMatrix od = getOD(new double[] {100, 200, 300, 400, 500, 600}, new double[] {1000, 2000, 0, 0, 2000, 0},
                Interpolation.STEPWISE, nodeA, nodeB, lane1, lane2);
        Map<String, GeneratorObjects> generatorObjects = ODApplier.applyOD(this.network, od, odOptions);
        assertEquals("Incorrect number of generator created or returned.", generatorObjects.size(), 2);
        for (String id : generatorObjects.keySet())
        {
            Generator<Duration> headwayGenerator = generatorObjects.get(id).getHeadwayGenerator();
            double factor = id.equals("A1") ? 0.4 : 0.6;
            // now check various points in time
            this.time = Time.instantiateSI(0); // spanning initial 0-demand period
            assertAboutEqual(headwayGenerator.draw(), 100 + 1 / (factor * 1000 / 3600));
            this.time = Time.instantiateSI(30); // spanning 0-demand period partially
            assertAboutEqual(headwayGenerator.draw(), 70 + 1 / (factor * 1000 / 3600));
            this.time = Time.instantiateSI(100); // start of demand period
            assertAboutEqual(headwayGenerator.draw(), 1 / (factor * 1000 / 3600));
            this.time = Time.instantiateSI(130); // middle of demand period
            assertAboutEqual(headwayGenerator.draw(), 1 / (factor * 1000 / 3600));
            this.time = Time.instantiateSI(199); // over slice edge
            double preSlice = factor * 1000 / 3600;
            assertAboutEqual(headwayGenerator.draw(), 1 + (1 - preSlice) / (factor * 2000 / 3600));
            this.time = Time.instantiateSI(299); // spanning 0-demand period in the middle
            preSlice = factor * 2000 / 3600;
            assertAboutEqual(headwayGenerator.draw(), 201 + (1 - preSlice) / (factor * 2000 / 3600));
            this.time = Time.instantiateSI(599); // just before end
            assertEquals(headwayGenerator.draw(), null);
            this.time = Time.instantiateSI(600); // on end
            assertEquals(headwayGenerator.draw(), null);
            this.time = Time.instantiateSI(700); // beyond end
            assertEquals(headwayGenerator.draw(), null);
        }

        // new network to avoid placing double sinks...
        makeNetwork();

        // Linear interpolation with constant headways tests
        this.time = Time.ZERO;
        od = getOD(new double[] {100, 200, 300, 400, 500, 600}, new double[] {1000, 2000, 0, 0, 2000, 0}, Interpolation.LINEAR,
                nodeA, nodeB, lane1, lane2);
        generatorObjects = ODApplier.applyOD(this.network, od, odOptions);
        assertEquals("Incorrect number of generator created or returned.", generatorObjects.size(), 2);
        for (String id : generatorObjects.keySet())
        {
            Generator<Duration> headwayGenerator = generatorObjects.get(id).getHeadwayGenerator();
            double factor = id.equals("A1") ? 0.4 : 0.6;
            // now check various points in time
            this.time = Time.instantiateSI(0); // spanning initial 0-demand period
            double inv = inverseTrapezoidal(1.0, 100, 1000, 200, 2000, 100, factor);
            assertAboutEqual(headwayGenerator.draw(), 100 + inv);
            this.time = Time.instantiateSI(30); // spanning 0-demand period partially
            assertAboutEqual(headwayGenerator.draw(), 70 + inv);
            this.time = Time.instantiateSI(100); // start of demand period
            assertAboutEqual(headwayGenerator.draw(), inv);
            this.time = Time.instantiateSI(130); // middle of demand period
            assertAboutEqual(headwayGenerator.draw(), inverseTrapezoidal(1.0, 100, 1000, 200, 2000, 130, factor));
            this.time = Time.instantiateSI(199); // over slice edge
            double preSlice = trapezoidal(100, 1000, 200, 2000, 199, 200, factor);
            assertAboutEqual(headwayGenerator.draw(), 1 + inverseTrapezoidal(1.0 - preSlice, 200, 2000, 300, 0, 200, factor));
            this.time = Time.instantiateSI(299); // spanning 0-demand period in the middle
            preSlice = trapezoidal(200, 2000, 300, 0, 299, 300, factor);
            assertAboutEqual(headwayGenerator.draw(), 101 + inverseTrapezoidal(1.0 - preSlice, 400, 0, 500, 2000, 400, factor));
            this.time = Time.instantiateSI(599); // just before end
            assertEquals(headwayGenerator.draw(), null);
            this.time = Time.instantiateSI(600); // on end
            assertEquals(headwayGenerator.draw(), null);
            this.time = Time.instantiateSI(700); // beyond end
            assertEquals(headwayGenerator.draw(), null);
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
                    odOptions = new ODOptions().set(ODOptions.HEADWAY_DIST, headwayRandomization);
                    od = getOD(new double[] {1200, 2400, 3600, 4800, 6000, 7200}, new double[] {1000, 2000, 0, 0, 2000, 0},
                            interpolation, nodeA, nodeB, lane1, lane2);
                    generatorObjects = ODApplier.applyOD(this.network, od, odOptions);
                    assertEquals("Incorrect number of generators created or returned.", generatorObjects.size(), 2);
                    for (String id : generatorObjects.keySet())
                    {
                        Generator<Duration> headwayGenerator = generatorObjects.get(id).getHeadwayGenerator();
                        double factor = id.equals("A1") ? 0.4 : 0.6;
                        double n = 0;
                        int nSims = 10;
                        for (int i = 0; i < nSims; i++) // 10 simulations
                        {
                            // simulate entire demand period and check total number of vehicles
                            while (this.time.si < 7200)
                            {
                                Duration headway = headwayGenerator.draw();
                                if (headway != null)
                                {
                                    n++;
                                    this.time = this.time.plus(headway);
                                }
                                else
                                {
                                    this.time = Time.instantiateSI(7200);
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
                        System.out.println(String.format(
                                "A demand of %.2f resulted in %.0f vehicles (%s%.2f%%) as mean over %d simulations (%s demand, %s headways).",
                                nDemand, n, p > 0 ? "+" : "", p, nSims, interpolation.name(), headwayRandomization.getName()));
                        assertTrue(String.format("Demand generated with exponential headways was more than 5%% off (%s%.2f%%).",
                                p > 0 ? "+" : "", p), Math.abs(p) < 5);
                    }
                }
            }
        }

    }

    /**
     * Creates an OD from input.
     * @param timeVec double[]; time vector [s]
     * @param demandVec double[]; demand vector [veh/h]
     * @param interpolation Interpolation; interpolation
     * @param nodeA Node; from node
     * @param nodeB Node; to node
     * @param lane1 Lane; lane 1 (60% of all traffic, all cars)
     * @param lane2 Lane; lane 2 (40% of all traffic, half of which is trucks)
     * @return OD
     * @throws ValueRuntimeException on exception
     * @throws NetworkException on exception
     */
    private ODMatrix getOD(final double[] timeVec, final double[] demandVec, final Interpolation interpolation,
            final Node nodeA, final Node nodeB, final Lane lane1, final Lane lane2)
            throws ValueRuntimeException, NetworkException
    {
        Categorization categorization = new Categorization("ODExample", Lane.class, GtuType.class, Route.class);
        List<Node> origins = new ArrayList<>();
        origins.add(nodeA);
        List<Node> destinations = new ArrayList<>();
        destinations.add(nodeB);
        TimeVector timeVector = DoubleVector.instantiate(timeVec, TimeUnit.DEFAULT, StorageType.DENSE);
        ODMatrix od = new ODMatrix("ODExample", origins, destinations, categorization, timeVector, interpolation);
        FrequencyVector demand = DoubleVector.instantiate(demandVec, FrequencyUnit.PER_HOUR, StorageType.DENSE);
        Route route = new Route("AB").addNode(nodeA).addNode(nodeB);
        Category category = new Category(categorization, lane1, this.network.getGtuType(GtuType.DEFAULTS.CAR), route);
        od.putDemandVector(nodeA, nodeB, category, demand, timeVector, interpolation, .6);
        category = new Category(categorization, lane2, this.network.getGtuType(GtuType.DEFAULTS.CAR), route);
        od.putDemandVector(nodeA, nodeB, category, demand, timeVector, interpolation, .2);
        category = new Category(categorization, lane2, this.network.getGtuType(GtuType.DEFAULTS.TRUCK), route);
        od.putDemandVector(nodeA, nodeB, category, demand, timeVector, interpolation, .2);
        return od;
    }

    /**
     * Trapezoidal integration for linearly interpolated demand.
     * @param t1 double; start time of demand period [s]
     * @param f1 double; start frequency of demand period [veh/h]
     * @param t2 double; end time of demand period [s]
     * @param f2 double; end frequency of demand period [veh/h]
     * @param tFrom double; start time of integrated period [s]
     * @param tTo double; end time of integrated period [s]
     * @param factor double; demand factor [-]
     * @return double; integrated demand [veh]
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
     * @param rem double; remainder to integrate to [veh]
     * @param t1 double; start time of demand period [s]
     * @param f1 double; start frequency of demand period [veh/h]
     * @param t2 double; end time of demand period [s]
     * @param f2 double; end frequency of demand period [veh/h]
     * @param tFrom double; start time of integrated period [s]
     * @param factor double; demand factor [-]
     * @return double; time [s]
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
        assertTrue(message, Math.abs(num1.doubleValue() - num2.doubleValue()) < 0.1);
    }

    /**
     * @throws ValueRuntimeException on exception
     * @throws NetworkException on exception
     * @throws SimRuntimeException on exception
     * @throws ParameterException on exception
     * @throws GtuException on exception
     * @throws ProbabilityException on exception
     */
    @Test
    public void gtuFractionTest() throws ValueRuntimeException, NetworkException, ParameterException, SimRuntimeException,
            ProbabilityException, GtuException
    {
        this.time = Time.ZERO;
        Node nodeA = this.network.getNode("A");
        Node nodeB = this.network.getNode("B");
        Lane lane1 = this.lanes.get("lane1");
        Lane lane2 = this.lanes.get("lane2");
        ODOptions odOptions = new ODOptions().set(ODOptions.HEADWAY_DIST, HeadwayDistribution.CONSTANT);
        ODMatrix od = getOD(new double[] {0, 100, 200}, new double[] {1000, 1500, 0}, Interpolation.LINEAR, nodeA, nodeB, lane1,
                lane2);
        Map<String, GeneratorObjects> generatorObjects = ODApplier.applyOD(this.network, od, odOptions);
        int nTot = 1000;
        int nCar = nTot / 2;
        int nTruck = nTot / 2;
        for (int i = 1; i < 3; i++)
        {
            for (double t = 40; t < 200; t += 100)
            {
                Map<GtuType, Integer> counts = new LinkedHashMap<>();
                counts.put(this.network.getGtuType(GtuType.DEFAULTS.CAR), 0);
                counts.put(this.network.getGtuType(GtuType.DEFAULTS.TRUCK), 0);
                for (int j = 0; j < nTot; j++)
                {
                    GtuType type = generatorObjects.get("A" + i).getCharachteristicsGenerator().draw().getGtuType();
                    if (counts.containsKey(type))
                    {
                        counts.put(type, counts.get(type) + 1);
                    }
                    else
                    {
                        fail("Vehicle generated from OD is of GtuType (" + type.getId() + ") that is not in the OD.");
                    }
                }
                assertTrue(
                        String.format("Generated number of CARs (%d) deviates too much from the expected value (%d).",
                                counts.get(this.network.getGtuType(GtuType.DEFAULTS.CAR)), nCar),
                        Math.abs(counts.get(this.network.getGtuType(GtuType.DEFAULTS.CAR)) - nCar) < nTot * .05);
                assertTrue(
                        String.format("Generated number of TRUCKs (%d) deviates too much from the expected value (%d).",
                                counts.get(this.network.getGtuType(GtuType.DEFAULTS.TRUCK)), nTruck),
                        Math.abs(counts.get(this.network.getGtuType(GtuType.DEFAULTS.TRUCK)) - nTruck) < nTot * .05);
                System.out.println(String.format("Generated %d CARs for expected value %d.",
                        counts.get(this.network.getGtuType(GtuType.DEFAULTS.CAR)), nCar));
                System.out.println(String.format("Generated %d TRUCKs for expected value %d.",
                        counts.get(this.network.getGtuType(GtuType.DEFAULTS.TRUCK)), nTruck));
            }
            nCar = 1000;
            nTruck = 0;
        }
    }

}
