package org.opentrafficsim.imb.demo;

import java.rmi.RemoteException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.DurationUnit;
import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.ValueRuntimeException;
import org.djunits.value.storage.StorageType;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djunits.value.vdouble.vector.FrequencyVector;
import org.djunits.value.vdouble.vector.TimeVector;
import org.djunits.value.vdouble.vector.base.DoubleVector;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.animation.gtu.colorer.GTUColorer;
import org.opentrafficsim.core.compatibility.Compatible;
import org.opentrafficsim.core.distributions.ConstantGenerator;
import org.opentrafficsim.core.distributions.Distribution;
import org.opentrafficsim.core.distributions.Distribution.FrequencyAndObject;
import org.opentrafficsim.core.distributions.Generator;
import org.opentrafficsim.core.distributions.ProbabilityException;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.idgenerator.IdGenerator;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.core.parameters.ParameterFactory;
import org.opentrafficsim.core.parameters.ParameterFactoryByType;
import org.opentrafficsim.core.units.distributions.ContinuousDistDoubleScalar;
import org.opentrafficsim.imb.demo.generators.IDMPlusOldFactory;
import org.opentrafficsim.imb.demo.generators.RouteGeneratorProbability;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions;
import org.opentrafficsim.road.gtu.generator.LaneBasedGTUGenerator;
import org.opentrafficsim.road.gtu.generator.LaneBasedGTUGenerator.RoomChecker;
import org.opentrafficsim.road.gtu.generator.TTCRoomChecker;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedTemplateGTUType;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedTemplateGTUTypeDistribution;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedGTUFollowingDirectedChangeTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedGTUFollowingDirectedChangeTacticalPlannerFactory;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedTacticalPlannerFactory;
import org.opentrafficsim.road.gtu.strategical.od.Interpolation;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlannerFactory;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.object.sensor.SinkSensor;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.jstats.distributions.DistNormal;
import nl.tudelft.simulation.jstats.distributions.DistUniform;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 18 nov. 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class A58OdUtil
{

    /** Truck fraction. */
    private final static double truckFraction = 0.15;

    /** Simulation period. */
    private final static Time simPeriod = new Time(1.0, TimeUnit.BASE_HOUR);

    /** Time vector. */
    private static TimeVector timeVector;

    static
    {
        double[] t = new double[61];
        for (int i = 0; i < 61; i++)
        {
            t[i] = i;
        }
        try
        {
            timeVector = DoubleVector.instantiate(t, TimeUnit.BASE_MINUTE, StorageType.DENSE);
        }
        catch (ValueRuntimeException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * @param network OTSNetwork; the network
     * @param gtuColorer GTUColorer; the GTU colorer
     * @param simulator OTSSimulatorInterface; the simulator
     * @param penetrationRate double; the penetration rate parameter
     * @throws ProbabilityException on error with the lane-based distributions
     */
    public static void createDemand(final OTSRoadNetwork network, final GTUColorer gtuColorer,
            final OTSSimulatorInterface simulator, double penetrationRate) throws ProbabilityException
    {

        Map<String, StreamInterface> streams = new LinkedHashMap<>();
        long j = 3;
        streams.put("headwayGeneration", new MersenneTwister(100L + j));
        streams.put("gtuClass", new MersenneTwister(101L + j));
        streams.put("gtuRoute", new MersenneTwister(102L + j));
        simulator.getReplication().setStreams(streams);

        TTCRoomChecker roomChecker = new TTCRoomChecker(new Duration(10.0, DurationUnit.SI));
        IdGenerator idGenerator = new IdGenerator("");
        LaneBasedTacticalPlannerFactory<LaneBasedGTUFollowingDirectedChangeTacticalPlanner> tacticalFactory =
                new LaneBasedGTUFollowingDirectedChangeTacticalPlannerFactory(new IDMPlusOldFactory());
        // LaneBasedTacticalPlannerFactory<LMRS> tacticalFactory;
        // try
        // {
        // BehavioralCharacteristics bc = new BehavioralCharacteristics();
        // bc.setDefaultParameter(AbstractIDM.DELTA);
        // tacticalFactory = new LMRSFactory(new IDMPlusFactory(), bc);
        // }
        // catch (GTUException | ParameterException exception1)
        // {
        // throw new RuntimeException(exception1);
        // }

        ParameterFactoryByType bcFactory = new ParameterFactoryByType();
        Length lookAhead = new Length(1000.0, LengthUnit.SI);
        Length lookAheadStdev = new Length(250.0, LengthUnit.SI);
        Length perception = new Length(1.0, LengthUnit.KILOMETER);
        Acceleration b = new Acceleration(2.09, AccelerationUnit.SI);
        GTUType gtuType = new GTUType("car", network.getGtuType(GTUType.DEFAULTS.CAR));
        bcFactory.addParameter(gtuType, ParameterTypes.FSPEED,
                new DistNormal(streams.get("gtuClass"), 123.7 / 120, 12.0 / 120));
        bcFactory.addParameter(gtuType, ParameterTypes.B, b);
        bcFactory.addParameter(gtuType, ParameterTypes.LOOKAHEAD, new ContinuousDistDoubleScalar.Rel<>(
                new DistNormal(streams.get("gtuClass"), lookAhead.si, lookAheadStdev.si), LengthUnit.SI));
        bcFactory.addParameter(gtuType, ParameterTypes.PERCEPTION, perception);
        gtuType = new GTUType("car_equipped", network.getGtuType(GTUType.DEFAULTS.CAR));
        bcFactory.addParameter(gtuType, ParameterTypes.FSPEED,
                new DistNormal(streams.get("gtuClass"), 123.7 / 120, 12.0 / 120));
        bcFactory.addParameter(gtuType, ParameterTypes.B, b);
        bcFactory.addParameter(gtuType, ParameterTypes.T, new Duration(0.6, DurationUnit.SI));
        bcFactory.addParameter(gtuType, ParameterTypes.TMAX, new Duration(0.6, DurationUnit.SI));
        bcFactory.addParameter(gtuType, ParameterTypes.A, new Acceleration(2.0, AccelerationUnit.SI));
        bcFactory.addParameter(gtuType, ParameterTypes.LOOKAHEAD, new ContinuousDistDoubleScalar.Rel<>(
                new DistNormal(streams.get("gtuClass"), lookAhead.si, lookAheadStdev.si), LengthUnit.SI));
        bcFactory.addParameter(gtuType, ParameterTypes.PERCEPTION, perception);
        gtuType = new GTUType("truck", network.getGtuType(GTUType.DEFAULTS.TRUCK));
        bcFactory.addParameter(gtuType, ParameterTypes.A, new Acceleration(0.4, AccelerationUnit.SI));
        bcFactory.addParameter(gtuType, ParameterTypes.B, b);
        bcFactory.addParameter(gtuType, ParameterTypes.LOOKAHEAD, new ContinuousDistDoubleScalar.Rel<>(
                new DistNormal(streams.get("gtuClass"), lookAhead.si, lookAheadStdev.si), LengthUnit.SI));
        bcFactory.addParameter(gtuType, ParameterTypes.PERCEPTION, perception);
        bcFactory.addParameter(gtuType, ParameterTypes.FSPEED, 2.0);
        gtuType = new GTUType("truck_equipped", network.getGtuType(GTUType.DEFAULTS.TRUCK));
        bcFactory.addParameter(gtuType, ParameterTypes.A, new Acceleration(0.4, AccelerationUnit.SI));
        bcFactory.addParameter(gtuType, ParameterTypes.B, b);
        bcFactory.addParameter(gtuType, ParameterTypes.T, new Duration(0.6, DurationUnit.SI));
        bcFactory.addParameter(gtuType, ParameterTypes.TMAX, new Duration(0.6, DurationUnit.SI));
        bcFactory.addParameter(gtuType, ParameterTypes.LOOKAHEAD, new ContinuousDistDoubleScalar.Rel<>(
                new DistNormal(streams.get("gtuClass"), lookAhead.si, lookAheadStdev.si), LengthUnit.SI));
        bcFactory.addParameter(gtuType, ParameterTypes.PERCEPTION, perception);
        bcFactory.addParameter(gtuType, ParameterTypes.FSPEED, 2.0);

        LaneBasedStrategicalRoutePlannerFactory strategicalFactory =
                new LaneBasedStrategicalRoutePlannerFactory(tacticalFactory, bcFactory);
        for (String source : demandMap.keySet())
        {
            OTSNode from = (OTSNode) network.getNode(nodeMap.get(source + " source"));
            Throw.whenNull(from, "Node %s of source %s could not be found.", nodeMap.get(source + " source"), from);
            Throw.when(from.getLinks().size() != 1, RuntimeException.class,
                    "Node %s of source %s connects to multiple or no links.", nodeMap.get(source + " source"), from);
            CrossSectionLink link = (CrossSectionLink) from.getLinks().iterator().next();

            // sum demand
            int nLanes = link.getLanes().size();
            double[] demandArray = null;
            for (String dest : demandMap.get(source).keySet())
            {
                OTSNode to = (OTSNode) network.getNode(nodeMap.get(dest + " dest"));
                Throw.whenNull(to, "Node %s of destination %s could not be found.", nodeMap.get(dest + " dest"), to);
                Throw.when(to.getLinks().size() != 1, RuntimeException.class,
                        "Node %s of destination %s connects to multiple or no links.", nodeMap.get(dest + " dest"), to);

                // TODO create sinks in separate method, remember which links have been coupled to a sink to skip
                addSink(to, simulator);
                if (demandArray == null)
                {
                    demandArray = (double[]) demandMap.get(source).get(dest);
                }
                else
                {
                    demandArray = arraySum(demandArray, (double[]) demandMap.get(source).get(dest));
                }
            }
            sinks.clear(); // for next run, since this is all static
            demandArray = factorCopy(demandArray, 1.0 / nLanes);
            FrequencyVector demandVector;
            try
            {
                demandVector = DoubleVector.instantiate(demandArray, FrequencyUnit.PER_HOUR, StorageType.DENSE);
            }
            catch (ValueRuntimeException exception)
            {
                throw new RuntimeException(exception);
            }

            // truck fraction
            double truckFrac = truckFraction * nLanes; // fraction for all trucks on right-hand lane only
            Throw.when(truckFrac > 1, UnsupportedOperationException.class,
                    "Number of lanes and truck fraction are such that trucks are present on more than only the right-hand lane."
                            + " This is not supported.");

            RouteGeneratorProbability routeGenerator = new RouteGeneratorProbability(network, timeVector, from, simulator);
            for (String dest : demandMap.get(source).keySet())
            {
                OTSNode to = (OTSNode) network.getNode(nodeMap.get(dest + " dest"));
                routeGenerator.addDemand(to, (double[]) demandMap.get(source).get(dest));
            }

            HeadwayGeneratorDemand headwayGenerator = new HeadwayGeneratorDemand(timeVector, demandVector, simulator);

            ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> speedCar = new ContinuousDistDoubleScalar.Rel<>(
                    new DistUniform(streams.get("gtuClass"), 160, 200), SpeedUnit.KM_PER_HOUR);
            ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> speedTruck = new ContinuousDistDoubleScalar.Rel<>(
                    new DistUniform(streams.get("gtuClass"), 80, 95), SpeedUnit.KM_PER_HOUR);

            LaneBasedTemplateGTUType car =
                    new LaneBasedTemplateGTUType(new GTUType("car", network.getGtuType(GTUType.DEFAULTS.CAR)),
                            new ConstantGenerator<>(Length.instantiateSI(4.0)),
                            new ConstantGenerator<>(Length.instantiateSI(2.0)), speedCar, strategicalFactory, routeGenerator);
            LaneBasedTemplateGTUType carEquipped =
                    new LaneBasedTemplateGTUType(new GTUType("car_equipped", network.getGtuType(GTUType.DEFAULTS.CAR)),
                            new ConstantGenerator<>(Length.instantiateSI(4.0)),
                            new ConstantGenerator<>(Length.instantiateSI(2.0)), speedCar, strategicalFactory, routeGenerator);
            LaneBasedTemplateGTUType truck =
                    new LaneBasedTemplateGTUType(new GTUType("truck", network.getGtuType(GTUType.DEFAULTS.TRUCK)),
                            new ConstantGenerator<>(Length.instantiateSI(15.0)),
                            new ConstantGenerator<>(Length.instantiateSI(2.5)), speedTruck, strategicalFactory, routeGenerator);
            LaneBasedTemplateGTUType truckEquipped =
                    new LaneBasedTemplateGTUType(new GTUType("truck_equipped", network.getGtuType(GTUType.DEFAULTS.TRUCK)),
                            new ConstantGenerator<>(Length.instantiateSI(15.0)),
                            new ConstantGenerator<>(Length.instantiateSI(2.5)), speedTruck, strategicalFactory, routeGenerator);

            Distribution<LaneBasedTemplateGTUType> gtuTypeLeft = new Distribution<>(streams.get("gtuClass"));
            gtuTypeLeft.add(new FrequencyAndObject<>(1.0 - penetrationRate, car));
            gtuTypeLeft.add(new FrequencyAndObject<>(penetrationRate, carEquipped));
            Distribution<LaneBasedTemplateGTUType> gtuTypeRight = new Distribution<>(streams.get("gtuClass"));
            gtuTypeRight.add(new FrequencyAndObject<>((1.0 - penetrationRate) * (1 - truckFrac), car));
            gtuTypeRight.add(new FrequencyAndObject<>(penetrationRate * (1 - truckFrac), carEquipped));
            gtuTypeRight.add(new FrequencyAndObject<>((1.0 - penetrationRate) * truckFrac, truck));
            gtuTypeRight.add(new FrequencyAndObject<>(penetrationRate * truckFrac, truckEquipped));

            Map<String, Lane> lanesById = new LinkedHashMap<>();
            for (Lane lane : link.getLanes())
            {
                lanesById.put(lane.getId(), lane);
            }
            try
            {
                for (int i = 1; i < nLanes; i++)
                {
                    Lane lane = lanesById.get("A" + i);
                    Speed generationSpeed = new Speed(120, SpeedUnit.KM_PER_HOUR); // by definition > 1 lane on this loop
                    String id = link.getId() + ":A" + i;
                    makeGenerator(lane, generationSpeed, id, idGenerator, simulator, network, gtuTypeLeft, headwayGenerator,
                            gtuColorer, roomChecker, bcFactory, tacticalFactory, streams.get("gtuClass"));
                }
                Lane lane = lanesById.get("A" + nLanes);
                Speed generationSpeed = new Speed(nLanes == 1 ? 60.0 : 120, SpeedUnit.KM_PER_HOUR);
                String id = link.getId() + ":A" + nLanes;
                makeGenerator(lane, generationSpeed, id, idGenerator, simulator, network, gtuTypeRight, headwayGenerator,
                        gtuColorer, roomChecker, bcFactory, tacticalFactory, streams.get("gtuClass"));
            }
            catch (SimRuntimeException | ProbabilityException | GTUException | ParameterException exception)
            {
                exception.printStackTrace();
            }

        }
    }

    /**
     * Nodes that have received sinks on the incoming link.
     */
    private static Set<OTSNode> sinks = new LinkedHashSet<>();

    /**
     * Add sinks to network.
     * @param endNode OTSNode; node to add the sink to
     * @param simulator DEVSSimulatorInterface.TimeDoubleUnit; the simulator
     */
    private static void addSink(final OTSNode endNode, final DEVSSimulatorInterface.TimeDoubleUnit simulator)
    {
        if (sinks.contains(endNode))
        {
            return;
        }
        sinks.add(endNode);
        for (Lane lane : ((CrossSectionLink) endNode.getLinks().iterator().next()).getLanes())
        {
            System.out.println("Adding sink on lane " + lane + " to node " + endNode);
            try
            {
                new SinkSensor(lane, lane.getLength().minus(new Length(30, LengthUnit.SI)), Compatible.EVERYTHING, simulator);
            }
            catch (NetworkException exception)
            {
                // throw new RuntimeException(
                // "Length of lane " + lane + " incompatible with sink location, or sensor id already exists.", exception);
            }
        }
    }

    /**
     * @param lane Lane; the reference lane for this generator
     * @param generationSpeed Speed; the speed of the GTU
     * @param id String; the id of the generator itself
     * @param idGenerator IdGenerator; the generator for the ID
     * @param simulator OTSSimulatorInterface; the simulator
     * @param network OTSNetwork; the network
     * @param distribution Distribution&lt;LaneBasedTemplateGTUType&gt;; the type generator for the GTU
     * @param headwayGenerator HeadwayGeneratorDemand; the headway generator for the GTU
     * @param gtuColorer GTUColorer; the GTU colorer for animation
     * @param roomChecker RoomChecker; the checker to see if there is room for the GTU
     * @param bcFactory ParameterFactory; the factory to generate parameters for the GTU
     * @param tacticalFactory LaneBasedTacticalPlannerFactory&lt;?&gt;; the generator for the tactical planner
     * @param stream StreamInterface; randum number stream
     * @throws SimRuntimeException in case of scheduling problems
     * @throws ProbabilityException in case of an illegal probability distribution
     * @throws GTUException in case the GTU is inconsistent
     * @throws ParameterException in case a parameter for the perception is missing
     */
    private static void makeGenerator(final Lane lane, final Speed generationSpeed, final String id,
            final IdGenerator idGenerator, final OTSSimulatorInterface simulator, final OTSRoadNetwork network,
            final Distribution<LaneBasedTemplateGTUType> distribution, final HeadwayGeneratorDemand headwayGenerator,
            final GTUColorer gtuColorer, final RoomChecker roomChecker, final ParameterFactory bcFactory,
            final LaneBasedTacticalPlannerFactory<?> tacticalFactory, final StreamInterface stream)
            throws SimRuntimeException, ProbabilityException, GTUException, ParameterException
    {
        Set<DirectedLanePosition> initialLongitudinalPositions = new LinkedHashSet<>();
        // TODO DIR_MINUS
        initialLongitudinalPositions
                .add(new DirectedLanePosition(lane, new Length(10.0, LengthUnit.SI), GTUDirectionality.DIR_PLUS));
        LaneBasedTemplateGTUTypeDistribution characteristicsGenerator = new LaneBasedTemplateGTUTypeDistribution(distribution);
        new LaneBasedGTUGenerator(id, headwayGenerator, characteristicsGenerator,
                GeneratorPositions.create(initialLongitudinalPositions, stream), network, simulator, roomChecker, idGenerator);
    }

    /**
     * @param in double[]; the input vector
     * @param factor double; the multiplication factor
     * @return array with values multiplied by factor
     */
    private static double[] factorCopy(final double[] in, final double factor)
    {
        double out[] = new double[in.length];
        for (int i = 0; i < in.length; i++)
        {
            out[i] = in[i] * factor;
        }
        return out;
    }

    /**
     * @param in1 double[]; the first vector
     * @param in2 double[]; the second vector
     * @return sum per element of arrays
     */
    private static double[] arraySum(final double[] in1, final double[] in2)
    {
        double out[] = new double[in1.length];
        for (int i = 0; i < in1.length; i++)
        {
            out[i] = in1[i] + in2[i];
        }
        return out;
    }

    /** Map of demand. */
    private static LinkedHashMap<String, LinkedHashMap<String, Object>> demandMap = new LinkedHashMap<>();

    /** Conversion table of origin and destination names and node ids. */
    private static LinkedHashMap<String, String> nodeMap = new LinkedHashMap<>();

    /** Overall demand factor. */
    private static double demandFactor;

    static
    {
        demandMap.put("Junction De Baars South E", new LinkedHashMap<>());
        demandMap.get("Junction De Baars South E").put("Restplace Kerkeind",
                new double[] {50.0, 49.0, 54.0, 56.0, 43.0, 53.0, 32.0, 34.0, 44.0, 47.0, 55.0, 43.0, 35.0, 68.0, 42.0, 42.0,
                        42.0, 30.0, 38.0, 47.0, 41.0, 32.0, 35.0, 43.0, 52.0, 37.0, 49.0, 31.0, 40.0, 59.0, 66.0, 29.0, 40.0,
                        48.0, 43.0, 36.0, 34.0, 50.0, 48.0, 34.0, 47.0, 58.0, 42.0, 38.0, 44.0, 47.0, 35.0, 61.0, 43.0, 44.0,
                        43.0, 35.0, 40.0, 26.0, 43.0, 31.0, 38.0, 31.0, 42.0, 35.0, 0.0});
        demandMap.get("Junction De Baars South E").put("Petrol station",
                new double[] {49.0, 48.0, 53.0, 55.0, 42.0, 52.0, 32.0, 33.0, 44.0, 46.0, 54.0, 42.0, 34.0, 67.0, 41.0, 41.0,
                        41.0, 29.0, 38.0, 46.0, 40.0, 32.0, 34.0, 42.0, 51.0, 36.0, 48.0, 31.0, 39.0, 58.0, 65.0, 28.0, 39.0,
                        47.0, 42.0, 35.0, 33.0, 49.0, 47.0, 33.0, 46.0, 56.0, 41.0, 38.0, 44.0, 46.0, 34.0, 60.0, 42.0, 44.0,
                        42.0, 34.0, 39.0, 26.0, 42.0, 31.0, 38.0, 31.0, 41.0, 34.0, 0.0});
        demandMap.get("Junction De Baars South E").put("Connection Moergestel E",
                new double[] {484.0, 473.0, 519.0, 542.0, 415.0, 507.0, 311.0, 323.0, 426.0, 449.0, 530.0, 415.0, 334.0, 657.0,
                        403.0, 403.0, 403.0, 288.0, 369.0, 449.0, 392.0, 311.0, 334.0, 415.0, 496.0, 357.0, 473.0, 300.0, 380.0,
                        565.0, 634.0, 277.0, 380.0, 461.0, 415.0, 346.0, 323.0, 484.0, 461.0, 323.0, 449.0, 553.0, 403.0, 369.0,
                        426.0, 449.0, 334.0, 588.0, 415.0, 426.0, 415.0, 334.0, 380.0, 254.0, 415.0, 300.0, 369.0, 300.0, 403.0,
                        334.0, 0.0});
        demandMap.get("Junction De Baars South E").put("Connection Oirschot E",
                new double[] {387.0, 378.0, 415.0, 433.0, 332.0, 406.0, 249.0, 258.0, 341.0, 360.0, 424.0, 332.0, 267.0, 526.0,
                        323.0, 323.0, 323.0, 230.0, 295.0, 360.0, 313.0, 249.0, 267.0, 332.0, 396.0, 286.0, 378.0, 240.0, 304.0,
                        452.0, 507.0, 221.0, 304.0, 369.0, 332.0, 277.0, 258.0, 387.0, 369.0, 258.0, 360.0, 443.0, 323.0, 295.0,
                        341.0, 360.0, 267.0, 470.0, 332.0, 341.0, 332.0, 267.0, 304.0, 203.0, 332.0, 240.0, 295.0, 240.0, 323.0,
                        267.0, 0.0});
        demandMap.get("Junction De Baars South E").put("Restplace Kloosters",
                new double[] {2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 1.0, 1.0, 2.0, 2.0, 2.0, 2.0, 1.0, 3.0, 2.0, 2.0, 2.0, 1.0, 1.0,
                        2.0, 2.0, 1.0, 1.0, 2.0, 2.0, 1.0, 2.0, 1.0, 2.0, 2.0, 3.0, 1.0, 2.0, 2.0, 2.0, 1.0, 1.0, 2.0, 2.0, 1.0,
                        2.0, 2.0, 2.0, 1.0, 2.0, 2.0, 1.0, 2.0, 2.0, 2.0, 2.0, 1.0, 2.0, 1.0, 2.0, 1.0, 1.0, 1.0, 2.0, 1.0,
                        0.0});
        demandMap.get("Junction De Baars South E").put("Connection Best E",
                new double[] {464.0, 453.0, 497.0, 519.0, 398.0, 486.0, 298.0, 309.0, 409.0, 431.0, 508.0, 398.0, 320.0, 630.0,
                        387.0, 387.0, 387.0, 276.0, 354.0, 431.0, 376.0, 298.0, 320.0, 398.0, 475.0, 343.0, 453.0, 287.0, 365.0,
                        541.0, 608.0, 265.0, 365.0, 442.0, 398.0, 332.0, 309.0, 464.0, 442.0, 309.0, 431.0, 530.0, 387.0, 354.0,
                        409.0, 431.0, 320.0, 564.0, 398.0, 409.0, 398.0, 320.0, 365.0, 243.0, 398.0, 287.0, 354.0, 287.0, 387.0,
                        320.0, 0.0});
        demandMap.get("Junction De Baars South E").put("Junction Batadorp E",
                new double[] {1083.0, 1057.0, 1160.0, 1212.0, 928.0, 1135.0, 697.0, 722.0, 954.0, 1005.0, 1186.0, 928.0, 748.0,
                        1469.0, 903.0, 903.0, 903.0, 644.0, 825.0, 1005.0, 876.0, 697.0, 748.0, 928.0, 1109.0, 799.0, 1057.0,
                        670.0, 851.0, 1263.0, 1418.0, 619.0, 851.0, 1031.0, 928.0, 774.0, 722.0, 1083.0, 1031.0, 722.0, 1005.0,
                        1237.0, 903.0, 825.0, 954.0, 1005.0, 748.0, 1315.0, 928.0, 954.0, 928.0, 748.0, 851.0, 567.0, 928.0,
                        670.0, 825.0, 670.0, 903.0, 748.0, 0.0});
        demandMap.put("Junction De Baars North E", new LinkedHashMap<>());
        demandMap.get("Junction De Baars North E").put("Restplace Kerkeind",
                new double[] {22.0, 10.0, 2.0, 13.0, 14.0, 17.0, 17.0, 12.0, 7.0, 6.0, 16.0, 12.0, 13.0, 0.0, 2.0, 7.0, 25.0,
                        0.0, 16.0, 0.0, 17.0, 14.0, 12.0, 0.0, 18.0, 25.0, 0.0, 18.0, 0.0, 17.0, 0.0, 14.0, 7.0, 0.0, 0.0, 0.0,
                        0.0, 14.0, 12.0, 36.0, 13.0, 1.0, 18.0, 13.0, 5.0, 6.0, 19.0, 0.0, 7.0, 14.0, 0.0, 18.0, 4.0, 26.0, 0.0,
                        26.0, 4.0, 16.0, 0.0, 10.0, 0.0});
        demandMap.get("Junction De Baars North E").put("Petrol station",
                new double[] {21.0, 9.0, 2.0, 13.0, 14.0, 16.0, 16.0, 12.0, 7.0, 6.0, 15.0, 12.0, 13.0, 0.0, 2.0, 7.0, 25.0,
                        0.0, 15.0, 0.0, 16.0, 14.0, 12.0, 0.0, 18.0, 25.0, 0.0, 18.0, 0.0, 16.0, 0.0, 14.0, 7.0, 0.0, 0.0, 0.0,
                        0.0, 14.0, 12.0, 35.0, 13.0, 1.0, 18.0, 13.0, 5.0, 6.0, 19.0, 0.0, 7.0, 14.0, 0.0, 18.0, 4.0, 26.0, 0.0,
                        26.0, 4.0, 15.0, 0.0, 9.0, 0.0});
        demandMap.get("Junction De Baars North E").put("Connection Moergestel E",
                new double[] {207.0, 92.0, 23.0, 127.0, 138.0, 161.0, 161.0, 115.0, 69.0, 58.0, 150.0, 115.0, 127.0, 0.0, 23.0,
                        69.0, 242.0, 0.0, 150.0, 0.0, 161.0, 138.0, 115.0, 0.0, 173.0, 242.0, 0.0, 173.0, 0.0, 161.0, 0.0,
                        138.0, 69.0, 0.0, 0.0, 0.0, 0.0, 138.0, 115.0, 346.0, 127.0, 12.0, 173.0, 127.0, 46.0, 58.0, 184.0, 0.0,
                        69.0, 138.0, 0.0, 173.0, 35.0, 254.0, 0.0, 254.0, 35.0, 150.0, 0.0, 92.0, 0.0});
        demandMap.get("Junction De Baars North E").put("Connection Oirschot E",
                new double[] {166.0, 74.0, 18.0, 101.0, 111.0, 129.0, 129.0, 92.0, 55.0, 46.0, 120.0, 92.0, 101.0, 0.0, 18.0,
                        55.0, 194.0, 0.0, 120.0, 0.0, 129.0, 111.0, 92.0, 0.0, 138.0, 194.0, 0.0, 138.0, 0.0, 129.0, 0.0, 111.0,
                        55.0, 0.0, 0.0, 0.0, 0.0, 111.0, 92.0, 277.0, 101.0, 9.0, 138.0, 101.0, 37.0, 46.0, 148.0, 0.0, 55.0,
                        111.0, 0.0, 138.0, 28.0, 203.0, 0.0, 203.0, 28.0, 120.0, 0.0, 74.0, 0.0});
        demandMap.get("Junction De Baars North E").put("Restplace Kloosters",
                new double[] {1.0, 0.0, 0.0, 1.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1.0,
                        0.0, 1.0, 1.0, 0.0, 0.0, 1.0, 1.0, 0.0, 1.0, 0.0, 1.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1.0,
                        1.0, 0.0, 1.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 1.0, 0.0, 1.0, 0.0, 1.0, 0.0, 1.0, 0.0, 0.0,
                        0.0});
        demandMap.get("Junction De Baars North E").put("Connection Best E",
                new double[] {199.0, 88.0, 22.0, 122.0, 133.0, 155.0, 155.0, 111.0, 66.0, 55.0, 144.0, 111.0, 122.0, 0.0, 22.0,
                        66.0, 232.0, 0.0, 144.0, 0.0, 155.0, 133.0, 111.0, 0.0, 166.0, 232.0, 0.0, 166.0, 0.0, 155.0, 0.0,
                        133.0, 66.0, 0.0, 0.0, 0.0, 0.0, 133.0, 111.0, 332.0, 122.0, 11.0, 166.0, 122.0, 44.0, 55.0, 177.0, 0.0,
                        66.0, 133.0, 0.0, 166.0, 33.0, 243.0, 0.0, 243.0, 33.0, 144.0, 0.0, 88.0, 0.0});
        demandMap.get("Junction De Baars North E").put("Junction Batadorp E",
                new double[] {464.0, 206.0, 51.0, 283.0, 310.0, 361.0, 361.0, 258.0, 155.0, 129.0, 335.0, 258.0, 283.0, 0.0,
                        51.0, 155.0, 542.0, 0.0, 335.0, 0.0, 361.0, 310.0, 258.0, 0.0, 387.0, 542.0, 0.0, 387.0, 0.0, 361.0,
                        0.0, 310.0, 155.0, 0.0, 0.0, 0.0, 0.0, 310.0, 258.0, 774.0, 283.0, 26.0, 387.0, 283.0, 104.0, 129.0,
                        412.0, 0.0, 155.0, 310.0, 0.0, 387.0, 77.0, 567.0, 0.0, 567.0, 77.0, 335.0, 0.0, 206.0, 0.0});
        demandMap.put("Restplace Kerkeind", new LinkedHashMap<>());
        demandMap.get("Restplace Kerkeind").put("Petrol station",
                new double[] {4.0, 4.0, 0.0, 0.0, 0.0, 0.0, 7.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 2.0, 0.0,
                        0.0, 0.0, 0.0, 0.0, 2.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 7.0, 0.0, 0.0, 0.0, 0.0,
                        0.0, 0.0, 5.0, 0.0, 0.0, 0.0, 0.0, 0.0, 2.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 3.0, 0.0, 0.0, 0.0,
                        0.0});
        demandMap.get("Restplace Kerkeind").put("Connection Moergestel E",
                new double[] {38.0, 34.0, 0.0, 0.0, 1.0, 0.0, 71.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 22.0, 0.0,
                        0.0, 0.0, 0.0, 0.0, 21.0, 0.0, 0.0, 0.0, 0.0, 5.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 64.0, 0.0, 0.0, 0.0,
                        0.0, 0.0, 0.0, 51.0, 0.0, 0.0, 0.0, 0.0, 0.0, 16.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 30.0, 0.0, 0.0,
                        1.0, 0.0});
        demandMap.get("Restplace Kerkeind").put("Connection Oirschot E",
                new double[] {31.0, 27.0, 0.0, 0.0, 1.0, 0.0, 57.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 18.0, 0.0,
                        0.0, 0.0, 0.0, 0.0, 17.0, 0.0, 0.0, 0.0, 0.0, 4.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 51.0, 0.0, 0.0, 0.0,
                        0.0, 0.0, 0.0, 40.0, 0.0, 0.0, 0.0, 0.0, 0.0, 13.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 24.0, 0.0, 0.0,
                        1.0, 0.0});
        demandMap.get("Restplace Kerkeind").put("Connection Best E",
                new double[] {37.0, 33.0, 0.0, 0.0, 1.0, 0.0, 68.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 21.0, 0.0,
                        0.0, 0.0, 0.0, 0.0, 21.0, 0.0, 0.0, 0.0, 0.0, 5.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 61.0, 0.0, 0.0, 0.0,
                        0.0, 0.0, 0.0, 48.0, 0.0, 0.0, 0.0, 0.0, 0.0, 16.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 29.0, 0.0, 0.0,
                        1.0, 0.0});
        demandMap.get("Restplace Kerkeind").put("Junction Batadorp E",
                new double[] {85.0, 77.0, 0.0, 0.0, 2.0, 0.0, 159.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 50.0,
                        0.0, 0.0, 0.0, 0.0, 0.0, 48.0, 0.0, 0.0, 0.0, 0.0, 11.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 143.0, 0.0, 0.0,
                        0.0, 0.0, 0.0, 0.0, 114.0, 0.0, 0.0, 0.0, 0.0, 0.0, 37.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 67.0, 0.0,
                        0.0, 3.0, 0.0});
        demandMap.put("Petrol station", new LinkedHashMap<>());
        demandMap.get("Petrol station").put("Connection Moergestel E",
                new double[] {0.0, 87.0, 165.0, 0.0, 0.0, 39.0, 0.0, 71.0, 0.0, 0.0, 18.0, 72.0, 24.0, 8.0, 0.0, 0.0, 0.0, 45.0,
                        0.0, 0.0, 0.0, 51.0, 0.0, 0.0, 3.0, 0.0, 0.0, 0.0, 0.0, 0.0, 16.0, 10.0, 48.0, 22.0, 0.0, 48.0, 46.0,
                        23.0, 0.0, 0.0, 51.0, 0.0, 15.0, 23.0, 130.0, 22.0, 94.0, 0.0, 0.0, 56.0, 0.0, 0.0, 90.0, 0.0, 0.0, 0.0,
                        15.0, 0.0, 99.0, 0.0, 0.0});
        demandMap.get("Petrol station").put("Connection Oirschot E",
                new double[] {0.0, 70.0, 132.0, 0.0, 0.0, 31.0, 0.0, 56.0, 0.0, 0.0, 15.0, 58.0, 19.0, 7.0, 0.0, 0.0, 0.0, 36.0,
                        0.0, 0.0, 0.0, 41.0, 0.0, 0.0, 3.0, 0.0, 0.0, 0.0, 0.0, 0.0, 13.0, 8.0, 39.0, 18.0, 0.0, 38.0, 37.0,
                        18.0, 0.0, 0.0, 41.0, 0.0, 12.0, 18.0, 104.0, 18.0, 75.0, 0.0, 0.0, 45.0, 0.0, 0.0, 72.0, 0.0, 0.0, 0.0,
                        12.0, 0.0, 79.0, 0.0, 0.0});
        demandMap.get("Petrol station").put("Restplace Kloosters",
                new double[] {0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                        0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                        0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                        0.0});
        demandMap.get("Petrol station").put("Connection Best E",
                new double[] {0.0, 84.0, 158.0, 0.0, 0.0, 38.0, 0.0, 68.0, 0.0, 0.0, 17.0, 69.0, 23.0, 8.0, 0.0, 0.0, 0.0, 43.0,
                        0.0, 0.0, 0.0, 49.0, 0.0, 0.0, 3.0, 0.0, 0.0, 0.0, 0.0, 0.0, 15.0, 9.0, 46.0, 21.0, 0.0, 46.0, 44.0,
                        22.0, 0.0, 0.0, 49.0, 0.0, 14.0, 22.0, 124.0, 21.0, 90.0, 0.0, 0.0, 54.0, 0.0, 0.0, 86.0, 0.0, 0.0, 0.0,
                        15.0, 0.0, 95.0, 0.0, 0.0});
        demandMap.get("Petrol station").put("Junction Batadorp E",
                new double[] {0.0, 195.0, 370.0, 0.0, 0.0, 88.0, 0.0, 158.0, 0.0, 0.0, 41.0, 162.0, 53.0, 19.0, 0.0, 0.0, 0.0,
                        101.0, 0.0, 0.0, 0.0, 114.0, 0.0, 0.0, 7.0, 0.0, 0.0, 0.0, 0.0, 0.0, 36.0, 22.0, 108.0, 50.0, 0.0,
                        107.0, 103.0, 51.0, 0.0, 0.0, 114.0, 0.0, 33.0, 51.0, 290.0, 50.0, 210.0, 0.0, 0.0, 125.0, 0.0, 0.0,
                        200.0, 0.0, 0.0, 0.0, 34.0, 0.0, 222.0, 0.0, 0.0});
        demandMap.put("Connection Moergestel E", new LinkedHashMap<>());
        demandMap.get("Connection Moergestel E").put("Connection Oirschot E",
                new double[] {178.0, 204.0, 175.0, 206.0, 0.0, 221.0, 238.0, 132.0, 89.0, 130.0, 67.0, 130.0, 154.0, 197.0,
                        86.0, 53.0, 156.0, 96.0, 86.0, 173.0, 0.0, 0.0, 0.0, 0.0, 156.0, 170.0, 211.0, 120.0, 182.0, 94.0,
                        125.0, 156.0, 132.0, 118.0, 194.0, 91.0, 151.0, 10.0, 151.0, 134.0, 139.0, 187.0, 132.0, 192.0, 118.0,
                        173.0, 161.0, 146.0, 82.0, 41.0, 250.0, 29.0, 149.0, 161.0, 106.0, 0.0, 154.0, 122.0, 55.0, 122.0,
                        0.0});
        demandMap.get("Connection Moergestel E").put("Restplace Kloosters",
                new double[] {1.0, 1.0, 1.0, 1.0, 0.0, 1.0, 1.0, 1.0, 0.0, 1.0, 0.0, 1.0, 1.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0,
                        1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0, 1.0, 0.0, 1.0, 1.0,
                        1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0, 0.0, 1.0, 0.0, 1.0, 1.0, 1.0, 0.0, 1.0, 1.0, 0.0, 1.0,
                        0.0});
        demandMap.get("Connection Moergestel E").put("Connection Best E",
                new double[] {213.0, 244.0, 210.0, 247.0, 0.0, 265.0, 285.0, 158.0, 106.0, 155.0, 81.0, 155.0, 184.0, 236.0,
                        104.0, 63.0, 187.0, 115.0, 104.0, 207.0, 0.0, 0.0, 0.0, 0.0, 187.0, 204.0, 253.0, 144.0, 219.0, 112.0,
                        150.0, 187.0, 158.0, 141.0, 233.0, 109.0, 181.0, 12.0, 181.0, 161.0, 167.0, 224.0, 158.0, 230.0, 141.0,
                        207.0, 193.0, 175.0, 98.0, 49.0, 299.0, 35.0, 178.0, 193.0, 127.0, 0.0, 184.0, 147.0, 66.0, 147.0,
                        0.0});
        demandMap.get("Connection Moergestel E").put("Junction Batadorp E",
                new double[] {496.0, 570.0, 490.0, 577.0, 0.0, 617.0, 664.0, 370.0, 249.0, 363.0, 188.0, 363.0, 429.0, 550.0,
                        242.0, 148.0, 436.0, 269.0, 242.0, 483.0, 0.0, 0.0, 0.0, 0.0, 436.0, 476.0, 590.0, 336.0, 510.0, 262.0,
                        349.0, 436.0, 370.0, 329.0, 543.0, 255.0, 422.0, 27.0, 422.0, 376.0, 390.0, 523.0, 370.0, 537.0, 329.0,
                        483.0, 449.0, 410.0, 228.0, 114.0, 698.0, 81.0, 417.0, 449.0, 296.0, 0.0, 429.0, 343.0, 155.0, 343.0,
                        0.0});
        demandMap.put("Connection Oirschot E", new LinkedHashMap<>());
        demandMap.get("Connection Oirschot E").put("Restplace Kloosters",
                new double[] {3.0, 1.0, 1.0, 3.0, 2.0, 0.0, 1.0, 2.0, 2.0, 2.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 2.0, 3.0, 1.0,
                        0.0, 2.0, 2.0, 1.0, 1.0, 2.0, 1.0, 1.0, 1.0, 2.0, 1.0, 1.0, 0.0, 2.0, 0.0, 2.0, 1.0, 0.0, 2.0, 1.0, 2.0,
                        0.0, 2.0, 1.0, 2.0, 0.0, 1.0, 1.0, 2.0, 1.0, 1.0, 1.0, 2.0, 0.0, 1.0, 2.0, 1.0, 1.0, 1.0, 1.0, 2.0,
                        0.0});
        demandMap.get("Connection Oirschot E").put("Connection Best E",
                new double[] {611.0, 122.0, 230.0, 755.0, 453.0, 50.0, 360.0, 406.0, 410.0, 421.0, 313.0, 349.0, 298.0, 262.0,
                        241.0, 273.0, 467.0, 615.0, 201.0, 115.0, 421.0, 406.0, 252.0, 137.0, 496.0, 180.0, 349.0, 223.0, 403.0,
                        280.0, 334.0, 36.0, 464.0, 0.0, 568.0, 309.0, 25.0, 467.0, 309.0, 374.0, 111.0, 442.0, 212.0, 424.0,
                        22.0, 324.0, 266.0, 385.0, 252.0, 356.0, 219.0, 388.0, 0.0, 331.0, 424.0, 205.0, 324.0, 342.0, 162.0,
                        367.0, 0.0});
        demandMap.get("Connection Oirschot E").put("Junction Batadorp E",
                new double[] {1426.0, 286.0, 537.0, 1762.0, 1057.0, 118.0, 839.0, 948.0, 957.0, 981.0, 730.0, 814.0, 697.0,
                        613.0, 562.0, 637.0, 1091.0, 1435.0, 469.0, 269.0, 981.0, 948.0, 587.0, 319.0, 1158.0, 419.0, 814.0,
                        520.0, 940.0, 654.0, 781.0, 84.0, 1082.0, 0.0, 1325.0, 721.0, 58.0, 1091.0, 721.0, 873.0, 260.0, 1032.0,
                        495.0, 990.0, 50.0, 755.0, 621.0, 897.0, 587.0, 831.0, 512.0, 906.0, 0.0, 772.0, 990.0, 478.0, 755.0,
                        797.0, 377.0, 856.0, 0.0});
        demandMap.put("Restplace Kloosters", new LinkedHashMap<>());
        demandMap.get("Restplace Kloosters").put("Connection Best E",
                new double[] {127.0, 128.0, 289.0, 20.0, 0.0, 0.0, 346.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 299.0, 0.0,
                        0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 244.0, 217.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                        0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 23.0, 181.0, 37.0, 0.0, 0.0, 0.0, 419.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                        0.0, 0.0, 154.0, 55.0, 0.0});
        demandMap.get("Restplace Kloosters").put("Junction Batadorp E",
                new double[] {297.0, 297.0, 674.0, 46.0, 0.0, 0.0, 806.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 697.0, 0.0,
                        0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 569.0, 506.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                        0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 53.0, 421.0, 87.0, 0.0, 0.0, 0.0, 977.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                        0.0, 0.0, 358.0, 128.0, 0.0});
        demandMap.put("Connection Best E", new LinkedHashMap<>());
        demandMap.get("Connection Best E").put("Junction Batadorp E",
                new double[] {1002.0, 1092.0, 1560.0, 1356.0, 2136.0, 588.0, 2244.0, 2328.0, 864.0, 786.0, 1656.0, 1674.0, 0.0,
                        2154.0, 1056.0, 1152.0, 1002.0, 1314.0, 1674.0, 594.0, 2358.0, 1020.0, 1824.0, 726.0, 1404.0, 1314.0,
                        396.0, 1986.0, 840.0, 1422.0, 1434.0, 702.0, 1530.0, 912.0, 1764.0, 1350.0, 1224.0, 660.0, 1650.0,
                        714.0, 2202.0, 0.0, 1416.0, 2070.0, 18.0, 1902.0, 1290.0, 486.0, 1548.0, 978.0, 1530.0, 1836.0, 0.0,
                        672.0, 1998.0, 1644.0, 1746.0, 1518.0, 0.0, 1686.0, 0.0});
        demandMap.put("Junction Batadorp South W", new LinkedHashMap<>());
        demandMap.get("Junction Batadorp South W").put("Connection Best W",
                new double[] {645.0, 570.0, 564.0, 570.0, 465.0, 558.0, 507.0, 603.0, 624.0, 525.0, 597.0, 504.0, 462.0, 447.0,
                        525.0, 483.0, 600.0, 552.0, 453.0, 543.0, 522.0, 486.0, 456.0, 423.0, 537.0, 489.0, 582.0, 600.0, 525.0,
                        411.0, 513.0, 426.0, 480.0, 432.0, 423.0, 330.0, 429.0, 555.0, 408.0, 468.0, 534.0, 465.0, 447.0, 588.0,
                        444.0, 528.0, 453.0, 447.0, 543.0, 480.0, 372.0, 435.0, 477.0, 519.0, 546.0, 411.0, 504.0, 372.0, 333.0,
                        492.0, 0.0});
        demandMap.get("Junction Batadorp South W").put("Restplace Brehees",
                new double[] {52.0, 46.0, 45.0, 46.0, 37.0, 45.0, 41.0, 48.0, 50.0, 42.0, 48.0, 40.0, 37.0, 36.0, 42.0, 39.0,
                        48.0, 44.0, 36.0, 43.0, 42.0, 39.0, 36.0, 34.0, 43.0, 39.0, 47.0, 48.0, 42.0, 33.0, 41.0, 34.0, 38.0,
                        35.0, 34.0, 26.0, 34.0, 44.0, 33.0, 37.0, 43.0, 37.0, 36.0, 47.0, 36.0, 42.0, 36.0, 36.0, 43.0, 38.0,
                        30.0, 35.0, 38.0, 42.0, 44.0, 33.0, 40.0, 30.0, 27.0, 39.0, 0.0});
        demandMap.get("Junction Batadorp South W").put("Connection Oirschot W",
                new double[] {275.0, 243.0, 241.0, 243.0, 198.0, 238.0, 216.0, 257.0, 266.0, 224.0, 255.0, 215.0, 197.0, 191.0,
                        224.0, 206.0, 256.0, 236.0, 193.0, 232.0, 223.0, 207.0, 195.0, 181.0, 229.0, 209.0, 248.0, 256.0, 224.0,
                        175.0, 219.0, 182.0, 205.0, 184.0, 181.0, 141.0, 183.0, 237.0, 174.0, 200.0, 228.0, 198.0, 191.0, 251.0,
                        189.0, 225.0, 193.0, 191.0, 232.0, 205.0, 159.0, 186.0, 204.0, 221.0, 233.0, 175.0, 215.0, 159.0, 142.0,
                        210.0, 0.0});
        demandMap.get("Junction Batadorp South W").put("Connection Moergestel W",
                new double[] {37.0, 32.0, 32.0, 32.0, 26.0, 32.0, 29.0, 34.0, 36.0, 30.0, 34.0, 29.0, 26.0, 25.0, 30.0, 28.0,
                        34.0, 31.0, 26.0, 31.0, 30.0, 28.0, 26.0, 24.0, 31.0, 28.0, 33.0, 34.0, 30.0, 23.0, 29.0, 24.0, 27.0,
                        25.0, 24.0, 19.0, 24.0, 32.0, 23.0, 27.0, 30.0, 26.0, 25.0, 33.0, 25.0, 30.0, 26.0, 25.0, 31.0, 27.0,
                        21.0, 25.0, 27.0, 30.0, 31.0, 23.0, 29.0, 21.0, 19.0, 28.0, 0.0});
        demandMap.get("Junction Batadorp South W").put("Restplace Kriekampen",
                new double[] {44.0, 39.0, 39.0, 39.0, 32.0, 38.0, 35.0, 41.0, 43.0, 36.0, 41.0, 35.0, 32.0, 31.0, 36.0, 33.0,
                        41.0, 38.0, 31.0, 37.0, 36.0, 33.0, 31.0, 29.0, 37.0, 34.0, 40.0, 41.0, 36.0, 28.0, 35.0, 29.0, 33.0,
                        30.0, 29.0, 23.0, 29.0, 38.0, 28.0, 32.0, 37.0, 32.0, 31.0, 40.0, 31.0, 36.0, 31.0, 31.0, 37.0, 33.0,
                        26.0, 30.0, 33.0, 36.0, 38.0, 28.0, 35.0, 26.0, 23.0, 34.0, 0.0});
        demandMap.get("Junction Batadorp South W").put("Junction De Baars South W",
                new double[] {830.0, 733.0, 726.0, 733.0, 598.0, 718.0, 652.0, 776.0, 803.0, 675.0, 768.0, 648.0, 594.0, 575.0,
                        675.0, 621.0, 772.0, 710.0, 583.0, 699.0, 672.0, 625.0, 587.0, 544.0, 691.0, 629.0, 749.0, 772.0, 675.0,
                        529.0, 660.0, 548.0, 617.0, 556.0, 544.0, 425.0, 552.0, 714.0, 525.0, 602.0, 687.0, 598.0, 575.0, 756.0,
                        571.0, 679.0, 583.0, 575.0, 699.0, 617.0, 479.0, 560.0, 614.0, 668.0, 702.0, 529.0, 648.0, 479.0, 428.0,
                        633.0, 0.0});
        demandMap.get("Junction Batadorp South W").put("Junction De Baars North W",
                new double[] {1342.0, 1186.0, 1174.0, 1186.0, 968.0, 1161.0, 1055.0, 1255.0, 1299.0, 1093.0, 1242.0, 1049.0,
                        961.0, 930.0, 1093.0, 1005.0, 1249.0, 1149.0, 943.0, 1130.0, 1086.0, 1011.0, 949.0, 880.0, 1118.0,
                        1018.0, 1211.0, 1249.0, 1093.0, 855.0, 1068.0, 887.0, 999.0, 899.0, 880.0, 687.0, 893.0, 1155.0, 849.0,
                        974.0, 1111.0, 968.0, 930.0, 1224.0, 924.0, 1099.0, 943.0, 930.0, 1130.0, 999.0, 774.0, 905.0, 993.0,
                        1080.0, 1136.0, 855.0, 1049.0, 774.0, 693.0, 1024.0, 0.0});
        demandMap.put("Junction Batadorp North W", new LinkedHashMap<>());
        demandMap.get("Junction Batadorp North W").put("Connection Best W",
                new double[] {48.0, 60.0, 57.0, 42.0, 21.0, 72.0, 33.0, 72.0, 60.0, 33.0, 42.0, 27.0, 42.0, 30.0, 24.0, 39.0,
                        39.0, 42.0, 15.0, 24.0, 45.0, 45.0, 30.0, 27.0, 48.0, 33.0, 39.0, 39.0, 24.0, 39.0, 27.0, 33.0, 15.0,
                        36.0, 27.0, 12.0, 21.0, 30.0, 51.0, 36.0, 42.0, 57.0, 39.0, 60.0, 24.0, 48.0, 42.0, 30.0, 42.0, 42.0,
                        24.0, 33.0, 45.0, 57.0, 39.0, 39.0, 45.0, 15.0, 18.0, 57.0, 0.0});
        demandMap.get("Junction Batadorp North W").put("Restplace Brehees",
                new double[] {4.0, 5.0, 5.0, 3.0, 2.0, 6.0, 3.0, 6.0, 5.0, 3.0, 3.0, 2.0, 3.0, 2.0, 2.0, 3.0, 3.0, 3.0, 1.0,
                        2.0, 4.0, 4.0, 2.0, 2.0, 4.0, 3.0, 3.0, 3.0, 2.0, 3.0, 2.0, 3.0, 1.0, 3.0, 2.0, 1.0, 2.0, 2.0, 4.0, 3.0,
                        3.0, 5.0, 3.0, 5.0, 2.0, 4.0, 3.0, 2.0, 3.0, 3.0, 2.0, 3.0, 4.0, 5.0, 3.0, 3.0, 4.0, 1.0, 1.0, 5.0,
                        0.0});
        demandMap.get("Junction Batadorp North W").put("Connection Oirschot W",
                new double[] {20.0, 26.0, 24.0, 18.0, 9.0, 31.0, 14.0, 31.0, 26.0, 14.0, 18.0, 12.0, 18.0, 13.0, 10.0, 17.0,
                        17.0, 18.0, 6.0, 10.0, 19.0, 19.0, 13.0, 12.0, 20.0, 14.0, 17.0, 17.0, 10.0, 17.0, 12.0, 14.0, 6.0,
                        15.0, 12.0, 5.0, 9.0, 13.0, 22.0, 15.0, 18.0, 24.0, 17.0, 26.0, 10.0, 20.0, 18.0, 13.0, 18.0, 18.0,
                        10.0, 14.0, 19.0, 24.0, 17.0, 17.0, 19.0, 6.0, 8.0, 24.0, 0.0});
        demandMap.get("Junction Batadorp North W").put("Connection Moergestel W",
                new double[] {3.0, 3.0, 3.0, 2.0, 1.0, 4.0, 2.0, 4.0, 3.0, 2.0, 2.0, 2.0, 2.0, 2.0, 1.0, 2.0, 2.0, 2.0, 1.0,
                        1.0, 3.0, 3.0, 2.0, 2.0, 3.0, 2.0, 2.0, 2.0, 1.0, 2.0, 2.0, 2.0, 1.0, 2.0, 2.0, 1.0, 1.0, 2.0, 3.0, 2.0,
                        2.0, 3.0, 2.0, 3.0, 1.0, 3.0, 2.0, 2.0, 2.0, 2.0, 1.0, 2.0, 3.0, 3.0, 2.0, 2.0, 3.0, 1.0, 1.0, 3.0,
                        0.0});
        demandMap.get("Junction Batadorp North W").put("Restplace Kriekampen",
                new double[] {3.0, 4.0, 4.0, 3.0, 1.0, 5.0, 2.0, 5.0, 4.0, 2.0, 3.0, 2.0, 3.0, 2.0, 2.0, 3.0, 3.0, 3.0, 1.0,
                        2.0, 3.0, 3.0, 2.0, 2.0, 3.0, 2.0, 3.0, 3.0, 2.0, 3.0, 2.0, 2.0, 1.0, 2.0, 2.0, 1.0, 1.0, 2.0, 4.0, 2.0,
                        3.0, 4.0, 3.0, 4.0, 2.0, 3.0, 3.0, 2.0, 3.0, 3.0, 2.0, 2.0, 3.0, 4.0, 3.0, 3.0, 3.0, 1.0, 1.0, 4.0,
                        0.0});
        demandMap.get("Junction Batadorp North W").put("Junction De Baars South W",
                new double[] {62.0, 77.0, 73.0, 54.0, 27.0, 93.0, 42.0, 93.0, 77.0, 42.0, 54.0, 35.0, 54.0, 39.0, 31.0, 50.0,
                        50.0, 54.0, 19.0, 31.0, 58.0, 58.0, 39.0, 35.0, 62.0, 42.0, 50.0, 50.0, 31.0, 50.0, 35.0, 42.0, 19.0,
                        46.0, 35.0, 15.0, 27.0, 39.0, 66.0, 46.0, 54.0, 73.0, 50.0, 77.0, 31.0, 62.0, 54.0, 39.0, 54.0, 54.0,
                        31.0, 42.0, 58.0, 73.0, 50.0, 50.0, 58.0, 19.0, 23.0, 73.0, 0.0});
        demandMap.get("Junction Batadorp North W").put("Junction De Baars North W",
                new double[] {100.0, 125.0, 119.0, 87.0, 44.0, 150.0, 69.0, 150.0, 125.0, 69.0, 87.0, 56.0, 87.0, 62.0, 50.0,
                        81.0, 81.0, 87.0, 31.0, 50.0, 94.0, 94.0, 62.0, 56.0, 100.0, 69.0, 81.0, 81.0, 50.0, 81.0, 56.0, 69.0,
                        31.0, 75.0, 56.0, 25.0, 44.0, 62.0, 106.0, 75.0, 87.0, 119.0, 81.0, 125.0, 50.0, 100.0, 87.0, 62.0,
                        87.0, 87.0, 50.0, 69.0, 94.0, 119.0, 81.0, 81.0, 94.0, 31.0, 37.0, 119.0, 0.0});
        demandMap.put("Connection Best W", new LinkedHashMap<>());
        demandMap.get("Connection Best W").put("Restplace Brehees",
                new double[] {8.0, 18.0, 27.0, 18.0, 0.0, 15.0, 0.0, 32.0, 10.0, 0.0, 16.0, 13.0, 16.0, 18.0, 0.0, 10.0, 9.0,
                        10.0, 7.0, 6.0, 10.0, 18.0, 24.0, 10.0, 13.0, 21.0, 13.0, 0.0, 17.0, 19.0, 14.0, 10.0, 17.0, 17.0, 29.0,
                        9.0, 10.0, 10.0, 21.0, 15.0, 14.0, 30.0, 13.0, 27.0, 20.0, 8.0, 32.0, 13.0, 8.0, 24.0, 36.0, 0.0, 28.0,
                        19.0, 10.0, 32.0, 30.0, 15.0, 27.0, 9.0, 0.0});
        demandMap.get("Connection Best W").put("Connection Oirschot W",
                new double[] {44.0, 96.0, 145.0, 97.0, 0.0, 81.0, 0.0, 173.0, 54.0, 0.0, 84.0, 68.0, 86.0, 97.0, 0.0, 55.0,
                        47.0, 54.0, 37.0, 33.0, 52.0, 93.0, 125.0, 51.0, 70.0, 110.0, 68.0, 0.0, 88.0, 102.0, 77.0, 54.0, 90.0,
                        88.0, 154.0, 46.0, 51.0, 51.0, 111.0, 79.0, 74.0, 161.0, 68.0, 146.0, 108.0, 42.0, 173.0, 72.0, 45.0,
                        129.0, 189.0, 0.0, 147.0, 100.0, 51.0, 173.0, 163.0, 78.0, 145.0, 47.0, 0.0});
        demandMap.get("Connection Best W").put("Connection Moergestel W",
                new double[] {6.0, 13.0, 19.0, 13.0, 0.0, 11.0, 0.0, 23.0, 7.0, 0.0, 11.0, 9.0, 11.0, 13.0, 0.0, 7.0, 6.0, 7.0,
                        5.0, 4.0, 7.0, 12.0, 17.0, 7.0, 9.0, 15.0, 9.0, 0.0, 12.0, 14.0, 10.0, 7.0, 12.0, 12.0, 21.0, 6.0, 7.0,
                        7.0, 15.0, 11.0, 10.0, 22.0, 9.0, 19.0, 14.0, 6.0, 23.0, 10.0, 6.0, 17.0, 25.0, 0.0, 20.0, 13.0, 7.0,
                        23.0, 22.0, 10.0, 19.0, 6.0, 0.0});
        demandMap.get("Connection Best W").put("Restplace Kriekampen",
                new double[] {7.0, 15.0, 23.0, 16.0, 0.0, 13.0, 0.0, 28.0, 9.0, 0.0, 14.0, 11.0, 14.0, 16.0, 0.0, 9.0, 8.0, 9.0,
                        6.0, 5.0, 8.0, 15.0, 20.0, 8.0, 11.0, 18.0, 11.0, 0.0, 14.0, 16.0, 12.0, 9.0, 14.0, 14.0, 25.0, 7.0,
                        8.0, 8.0, 18.0, 13.0, 12.0, 26.0, 11.0, 24.0, 17.0, 7.0, 28.0, 12.0, 7.0, 21.0, 31.0, 0.0, 24.0, 16.0,
                        8.0, 28.0, 26.0, 13.0, 23.0, 8.0, 0.0});
        demandMap.get("Connection Best W").put("Junction De Baars South W",
                new double[] {131.0, 289.0, 436.0, 293.0, 0.0, 243.0, 0.0, 521.0, 162.0, 0.0, 255.0, 205.0, 259.0, 293.0, 0.0,
                        166.0, 143.0, 162.0, 112.0, 100.0, 158.0, 282.0, 378.0, 154.0, 212.0, 332.0, 205.0, 0.0, 266.0, 309.0,
                        232.0, 162.0, 270.0, 266.0, 463.0, 139.0, 154.0, 154.0, 336.0, 239.0, 224.0, 486.0, 205.0, 440.0, 324.0,
                        127.0, 521.0, 216.0, 135.0, 390.0, 571.0, 0.0, 444.0, 301.0, 154.0, 521.0, 490.0, 235.0, 436.0, 143.0,
                        0.0});
        demandMap.get("Connection Best W").put("Junction De Baars North W",
                new double[] {212.0, 468.0, 706.0, 474.0, 0.0, 393.0, 0.0, 843.0, 262.0, 0.0, 412.0, 331.0, 418.0, 474.0, 0.0,
                        268.0, 231.0, 262.0, 181.0, 162.0, 256.0, 456.0, 612.0, 250.0, 343.0, 537.0, 331.0, 0.0, 431.0, 499.0,
                        375.0, 262.0, 437.0, 431.0, 749.0, 225.0, 250.0, 250.0, 543.0, 387.0, 362.0, 787.0, 331.0, 712.0, 524.0,
                        206.0, 843.0, 350.0, 219.0, 631.0, 924.0, 0.0, 718.0, 487.0, 250.0, 843.0, 793.0, 381.0, 706.0, 231.0,
                        0.0});
        demandMap.put("Restplace Brehees", new LinkedHashMap<>());
        demandMap.get("Restplace Brehees").put("Connection Oirschot W",
                new double[] {19.0, 32.0, 0.0, 21.0, 40.0, 0.0, 30.0, 0.0, 85.0, 21.0, 0.0, 24.0, 65.0, 0.0, 11.0, 0.0, 43.0,
                        0.0, 0.0, 5.0, 31.0, 12.0, 51.0, 26.0, 26.0, 51.0, 0.0, 51.0, 13.0, 0.0, 31.0, 25.0, 0.0, 44.0, 25.0,
                        24.0, 0.0, 0.0, 23.0, 0.0, 23.0, 65.0, 13.0, 0.0, 31.0, 15.0, 38.0, 32.0, 13.0, 50.0, 19.0, 72.0, 0.0,
                        37.0, 52.0, 0.0, 0.0, 14.0, 0.0, 19.0, 0.0});
        demandMap.get("Restplace Brehees").put("Connection Moergestel W",
                new double[] {3.0, 4.0, 0.0, 3.0, 5.0, 0.0, 4.0, 0.0, 11.0, 3.0, 0.0, 3.0, 9.0, 0.0, 1.0, 0.0, 6.0, 0.0, 0.0,
                        1.0, 4.0, 2.0, 7.0, 3.0, 3.0, 7.0, 0.0, 7.0, 2.0, 0.0, 4.0, 3.0, 0.0, 6.0, 3.0, 3.0, 0.0, 0.0, 3.0, 0.0,
                        3.0, 9.0, 2.0, 0.0, 4.0, 2.0, 5.0, 4.0, 2.0, 7.0, 3.0, 10.0, 0.0, 5.0, 7.0, 0.0, 0.0, 2.0, 0.0, 2.0,
                        0.0});
        demandMap.get("Restplace Brehees").put("Restplace Kriekampen",
                new double[] {3.0, 5.0, 0.0, 3.0, 6.0, 0.0, 5.0, 0.0, 14.0, 3.0, 0.0, 4.0, 10.0, 0.0, 2.0, 0.0, 7.0, 0.0, 0.0,
                        1.0, 5.0, 2.0, 8.0, 4.0, 4.0, 8.0, 0.0, 8.0, 2.0, 0.0, 5.0, 4.0, 0.0, 7.0, 4.0, 4.0, 0.0, 0.0, 4.0, 0.0,
                        4.0, 10.0, 2.0, 0.0, 5.0, 2.0, 6.0, 5.0, 2.0, 8.0, 3.0, 12.0, 0.0, 6.0, 8.0, 0.0, 0.0, 2.0, 0.0, 3.0,
                        0.0});
        demandMap.get("Restplace Brehees").put("Junction De Baars South W",
                new double[] {59.0, 96.0, 0.0, 62.0, 120.0, 0.0, 91.0, 0.0, 256.0, 63.0, 0.0, 72.0, 196.0, 0.0, 32.0, 0.0,
                        129.0, 0.0, 0.0, 14.0, 93.0, 35.0, 154.0, 77.0, 79.0, 154.0, 0.0, 154.0, 40.0, 0.0, 93.0, 75.0, 0.0,
                        132.0, 77.0, 73.0, 0.0, 0.0, 68.0, 0.0, 71.0, 195.0, 40.0, 0.0, 93.0, 47.0, 115.0, 96.0, 38.0, 151.0,
                        59.0, 217.0, 0.0, 112.0, 158.0, 0.0, 0.0, 43.0, 0.0, 56.0, 0.0});
        demandMap.get("Restplace Brehees").put("Junction De Baars North W",
                new double[] {95.0, 156.0, 0.0, 101.0, 195.0, 0.0, 147.0, 0.0, 414.0, 102.0, 0.0, 116.0, 316.0, 0.0, 53.0, 0.0,
                        208.0, 0.0, 0.0, 23.0, 150.0, 57.0, 248.0, 125.0, 127.0, 250.0, 0.0, 248.0, 64.0, 0.0, 150.0, 122.0,
                        0.0, 213.0, 124.0, 118.0, 0.0, 0.0, 110.0, 0.0, 114.0, 315.0, 65.0, 0.0, 150.0, 75.0, 185.0, 156.0,
                        62.0, 245.0, 95.0, 351.0, 0.0, 181.0, 255.0, 0.0, 0.0, 70.0, 0.0, 91.0, 0.0});
        demandMap.put("Connection Oirschot W", new LinkedHashMap<>());
        demandMap.get("Connection Oirschot W").put("Connection Moergestel W",
                new double[] {0.0, 20.0, 4.0, 0.0, 12.0, 4.0, 13.0, 10.0, 0.0, 3.0, 6.0, 20.0, 6.0, 0.0, 8.0, 13.0, 8.0, 4.0,
                        9.0, 0.0, 7.0, 3.0, 16.0, 0.0, 3.0, 14.0, 1.0, 11.0, 5.0, 11.0, 11.0, 6.0, 7.0, 3.0, 5.0, 6.0, 14.0,
                        9.0, 14.0, 0.0, 13.0, 1.0, 0.0, 3.0, 18.0, 7.0, 0.0, 6.0, 8.0, 7.0, 13.0, 0.0, 4.0, 11.0, 20.0, 0.0,
                        1.0, 11.0, 2.0, 18.0, 0.0});
        demandMap.get("Connection Oirschot W").put("Restplace Kriekampen",
                new double[] {0.0, 24.0, 5.0, 0.0, 14.0, 5.0, 16.0, 12.0, 0.0, 4.0, 7.0, 25.0, 7.0, 0.0, 10.0, 16.0, 9.0, 5.0,
                        10.0, 0.0, 9.0, 4.0, 20.0, 0.0, 4.0, 17.0, 1.0, 13.0, 6.0, 13.0, 13.0, 7.0, 9.0, 3.0, 6.0, 7.0, 16.0,
                        11.0, 16.0, 0.0, 16.0, 2.0, 0.0, 3.0, 22.0, 9.0, 0.0, 7.0, 9.0, 8.0, 16.0, 0.0, 5.0, 14.0, 24.0, 0.0,
                        1.0, 13.0, 2.0, 22.0, 0.0});
        demandMap.get("Connection Oirschot W").put("Junction De Baars South W",
                new double[] {0.0, 452.0, 101.0, 0.0, 260.0, 98.0, 298.0, 229.0, 0.0, 78.0, 130.0, 460.0, 133.0, 0.0, 192.0,
                        295.0, 170.0, 84.0, 195.0, 0.0, 165.0, 74.0, 372.0, 0.0, 77.0, 312.0, 20.0, 241.0, 116.0, 243.0, 239.0,
                        130.0, 168.0, 64.0, 118.0, 130.0, 305.0, 202.0, 306.0, 0.0, 291.0, 30.0, 0.0, 64.0, 408.0, 160.0, 0.0,
                        135.0, 175.0, 148.0, 300.0, 3.0, 95.0, 258.0, 455.0, 0.0, 17.0, 241.0, 37.0, 405.0, 0.0});
        demandMap.get("Connection Oirschot W").put("Junction De Baars North W",
                new double[] {0.0, 731.0, 163.0, 0.0, 421.0, 158.0, 481.0, 371.0, 0.0, 126.0, 210.0, 744.0, 216.0, 0.0, 310.0,
                        478.0, 275.0, 136.0, 315.0, 0.0, 267.0, 120.0, 601.0, 0.0, 125.0, 505.0, 32.0, 390.0, 187.0, 393.0,
                        387.0, 211.0, 272.0, 104.0, 191.0, 211.0, 494.0, 326.0, 494.0, 0.0, 471.0, 48.0, 0.0, 104.0, 660.0,
                        259.0, 0.0, 218.0, 282.0, 239.0, 486.0, 4.0, 154.0, 418.0, 736.0, 0.0, 28.0, 390.0, 60.0, 656.0, 0.0});
        demandMap.put("Connection Moergestel W", new LinkedHashMap<>());
        demandMap.get("Connection Moergestel W").put("Restplace Kriekampen",
                new double[] {14.0, 0.0, 0.0, 0.0, 1.0, 0.0, 2.0, 6.0, 0.0, 2.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 9.0, 0.0,
                        0.0, 0.0, 0.0, 5.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 8.0, 4.0, 0.0, 2.0, 0.0, 0.0, 0.0, 0.0, 6.0, 0.0, 0.0,
                        2.0, 0.0, 8.0, 0.0, 2.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1.0, 2.0, 0.0,
                        0.0});
        demandMap.get("Connection Moergestel W").put("Junction De Baars South W",
                new double[] {263.0, 0.0, 0.0, 0.0, 20.0, 0.0, 38.0, 105.0, 0.0, 38.0, 0.0, 0.0, 0.0, 24.0, 0.0, 0.0, 0.0,
                        161.0, 0.0, 0.0, 0.0, 0.0, 97.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 150.0, 78.0, 0.0, 32.0, 0.0, 0.0, 0.0,
                        0.0, 109.0, 0.0, 0.0, 39.0, 0.0, 152.0, 0.0, 30.0, 0.0, 0.0, 18.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                        25.0, 0.0, 13.0, 36.0, 0.0, 0.0});
        demandMap.get("Connection Moergestel W").put("Junction De Baars North W",
                new double[] {426.0, 0.0, 0.0, 0.0, 32.0, 0.0, 62.0, 170.0, 0.0, 62.0, 0.0, 0.0, 0.0, 39.0, 0.0, 0.0, 0.0,
                        260.0, 0.0, 0.0, 0.0, 0.0, 157.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 243.0, 127.0, 0.0, 52.0, 0.0, 0.0, 0.0,
                        0.0, 176.0, 0.0, 0.0, 62.0, 0.0, 246.0, 0.0, 48.0, 0.0, 0.0, 30.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                        41.0, 0.0, 21.0, 59.0, 0.0, 0.0});
        demandMap.put("Restplace Kriekampen", new LinkedHashMap<>());
        demandMap.get("Restplace Kriekampen").put("Junction De Baars South W",
                new double[] {30.0, 231.0, 332.0, 0.0, 0.0, 0.0, 0.0, 0.0, 4.0, 0.0, 0.0, 47.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                        50.0, 0.0, 0.0, 0.0, 0.0, 74.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 155.0, 0.0, 0.0, 0.0, 0.0, 191.0,
                        0.0, 0.0, 0.0, 0.0, 100.0, 0.0, 0.0, 117.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                        0.0, 46.0, 0.0, 0.0});
        demandMap.get("Restplace Kriekampen").put("Junction De Baars North W",
                new double[] {49.0, 373.0, 536.0, 0.0, 0.0, 0.0, 0.0, 0.0, 7.0, 0.0, 0.0, 75.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                        82.0, 0.0, 0.0, 0.0, 0.0, 120.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 251.0, 0.0, 0.0, 0.0, 0.0, 310.0,
                        0.0, 0.0, 0.0, 0.0, 161.0, 0.0, 0.0, 190.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                        0.0, 74.0, 0.0, 0.0});

        // demand factor
        demandFactor = 1.0;
        for (String from : demandMap.keySet())
        {
            for (String to : demandMap.get(from).keySet())
            {
                double[] arr = (double[]) demandMap.get(from).get(to);
                for (int i = 0; i < arr.length; i++)
                {
                    arr[i] = arr[i] * demandFactor;
                }
            }
        }

        // bit of smoothing
        double smoothing = 0.5; // 1.0 = all mean, 0.0 = original
        for (String from : demandMap.keySet())
        {
            for (String to : demandMap.get(from).keySet())
            {
                double mean = 0.0;
                double[] arr = (double[]) demandMap.get(from).get(to);
                for (int i = 0; i < arr.length; i++)
                {
                    mean += arr[i];
                }
                mean /= arr.length; // 61st value is 0 and meaningless, marks the end of the last period
                for (int i = 0; i < arr.length; i++)
                {
                    arr[i] = (1.0 - smoothing) * arr[i] + smoothing * mean;
                }
            }
        }
        double[] a = (double[]) demandMap.get("Connection Oirschot W").get("Junction De Baars North W");

        nodeMap.put("Junction De Baars South E source", "N1EB");
        nodeMap.put("Junction De Baars North E source", "N4EB");
        nodeMap.put("Restplace Kerkeind dest", "N6EB");
        nodeMap.put("Restplace Kerkeind source", "N9EB");
        nodeMap.put("Petrol station dest", "N10EB");
        nodeMap.put("Petrol station source", "N13EB");
        nodeMap.put("Connection Moergestel E dest", "N16EB");
        nodeMap.put("Connection Moergestel E source", "N19EB");
        nodeMap.put("Connection Oirschot E dest", "N22EB");
        nodeMap.put("Connection Oirschot E source", "N25EB");
        nodeMap.put("Restplace Kloosters dest", "N28EB");
        nodeMap.put("Restplace Kloosters source", "N31EB");
        nodeMap.put("Connection Best E dest", "N35EB");
        nodeMap.put("Connection Best E source", "N38EB");
        nodeMap.put("Junction Batadorp E dest", "N37EB");
        nodeMap.put("Junction Batadorp South W source", "N1WB");
        nodeMap.put("Junction Batadorp North W source", "N4WB");
        nodeMap.put("Connection Best W dest", "N5WB");
        nodeMap.put("Connection Best W source", "N8WB");
        nodeMap.put("Restplace Brehees dest", "N11WB");
        nodeMap.put("Restplace Brehees source", "N14WB");
        nodeMap.put("Connection Oirschot W dest", "N17WB");
        nodeMap.put("Connection Oirschot W source", "N20WB");
        nodeMap.put("Connection Moergestel W dest", "N24WB");
        nodeMap.put("Connection Moergestel W source", "N27WB");
        nodeMap.put("Restplace Kriekampen dest", "N30WB");
        nodeMap.put("Restplace Kriekampen source", "N33WB");
        nodeMap.put("Junction De Baars North W dest", "N36WB");
        nodeMap.put("Junction De Baars South W dest", "N37WB");
    }

    /**
     * Generates headways based on demand.
     * <p>
     * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 17 nov. 2016 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private static class HeadwayGeneratorDemand implements Generator<Duration>
    {

        /** Interpolation of demand. */
        private final Interpolation interpolation;

        /** Vector of time. */
        private final TimeVector timeVector;

        /** Vector of flow values. */
        private final FrequencyVector demandVector;

        /** Simulator. */
        private final SimulatorInterface.TimeDoubleUnit simulator;

        /** Stream name of headway generation. */
        private static final String HEADWAY_STREAM = "headwayGeneration";

        /**
         * @param timeVector TimeVector; a time vector
         * @param demandVector FrequencyVector; the corresponding demand vector
         * @param simulator SimulatorInterface.TimeDoubleUnit; the simulator
         */
        public HeadwayGeneratorDemand(final TimeVector timeVector, final FrequencyVector demandVector,
                final SimulatorInterface.TimeDoubleUnit simulator)
        {
            this(timeVector, demandVector, simulator, Interpolation.STEPWISE);
        }

        /**
         * @param timeVector TimeVector; a time vector
         * @param demandVector FrequencyVector; the corresponding demand vector
         * @param simulator SimulatorInterface.TimeDoubleUnit; the simulator
         * @param interpolation Interpolation; interpolation type
         */
        public HeadwayGeneratorDemand(final TimeVector timeVector, final FrequencyVector demandVector,
                final SimulatorInterface.TimeDoubleUnit simulator, final Interpolation interpolation)
        {
            Throw.whenNull(timeVector, "Time vector may not be null.");
            Throw.whenNull(demandVector, "Demand vector may not be null.");
            Throw.whenNull(simulator, "Simulator may not be null.");
            Throw.whenNull(interpolation, "Interpolation may not be null.");
            Throw.whenNull(simulator.getReplication().getStream(HEADWAY_STREAM),
                    "Could not obtain random stream '" + HEADWAY_STREAM + "'.");
            for (int i = 0; i < timeVector.size() - 1; i++)
            {
                try
                {
                    Throw.when(timeVector.get(i).ge(timeVector.get(i + 1)), IllegalArgumentException.class,
                            "Time vector is not increasing.");
                }
                catch (ValueRuntimeException exception)
                {
                    throw new RuntimeException(
                            "Value out of range of time vector. Note that HeadwayGenerator does not create a safe copy.",
                            exception);
                }
            }
            Throw.when(timeVector.size() != demandVector.size(), IllegalArgumentException.class,
                    "Time and flow vector should be of the same size.");
            Throw.when(timeVector.size() < 2, IllegalArgumentException.class,
                    "Time and flow vector should be at least of size 2.");
            this.timeVector = timeVector;
            this.demandVector = demandVector;
            this.simulator = simulator;
            this.interpolation = interpolation;
        }

        /** {@inheritDoc} */
        @Override
        public final Duration draw() throws ProbabilityException, ParameterException
        {
            Time time = this.simulator.getSimulatorTime();
            try
            {
                Throw.when(time.lt(this.timeVector.get(0)), IllegalArgumentException.class,
                        "Cannot return a headway at time before first time in vector.");

                // get time period of current time
                int i = 0;
                while (this.timeVector.get(i + 1).lt(time) && i < this.timeVector.size() - 1)
                {
                    i++;
                }
                try
                {
                    return nextArrival(i, time.minus(this.timeVector.get(i)), 1.0).minus(time);
                }
                catch (RemoteException exception)
                {
                    throw new RuntimeException("Could not obtain replication.", exception);
                }
            }
            catch (ValueRuntimeException exception)
            {
                throw new RuntimeException(
                        "Value out of range of time or demand vector. Note that HeadwayGenerator does not create safe copies.",
                        exception);
            }
        }

        /**
         * Recursive determination of the next arrival time. Each recursion moves to the next time period. This occurs if a
         * randomly determined arrival falls outside of a time period, or when demand in a time period is 0.
         * @param i int; index of time period
         * @param start Duration; reference time from start of period i, pertains to previous arrival, or zero during recursion
         * @param fractionRemaining double; remaining fraction of headway to apply due to time in earlier time periods
         * @return time of next arrival
         * @throws ValueRuntimeException in case of an illegal time vector
         * @throws RemoteException in case of not being able to retrieve the replication
         */
        private Time nextArrival(final int i, final Duration start, final double fractionRemaining)
                throws ValueRuntimeException, RemoteException
        {

            // escape if beyond specified time by infinite next arrival (= no traffic)
            if (i == this.timeVector.size() - 1)
            {
                return new Time(Double.POSITIVE_INFINITY, TimeUnit.DEFAULT);
            }

            // skip zero-demand periods
            if (this.demandVector.get(i).equals(Frequency.ZERO))
            {
                // after zero-demand, the next headway is a random fraction of a random headway as there is no previous arrival
                return nextArrival(i + 1, Duration.ZERO,
                        this.simulator.getReplication().getStream(HEADWAY_STREAM).nextDouble());
            }

            // calculate headway from demand
            Frequency demand;
            if (this.interpolation.isStepWise())
            {
                demand = this.demandVector.get(i);
            }
            else
            {
                double f = start.si / (this.timeVector.get(i + 1).si - this.timeVector.get(i).si);
                demand = Frequency.interpolate(this.demandVector.get(i), this.demandVector.get(i + 1), f);
            }
            double t = -Math.log(this.simulator.getReplication().getStream(HEADWAY_STREAM).nextDouble()) / demand.si;

            // calculate arrival
            Time arrival = new Time(this.timeVector.get(i).si + start.si + t * fractionRemaining, TimeUnit.DEFAULT);

            // go to next period if arrival is beyond current period
            if (arrival.gt(this.timeVector.get(i + 1)))
            {
                double inStep = this.timeVector.get(i + 1).si - (this.timeVector.get(i).si + start.si);
                return nextArrival(i + 1, Duration.ZERO, fractionRemaining - inStep / t);
            }

            return arrival;

        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "HeadwayGeneratorDemand [interpolation=" + this.interpolation + ", timeVector=" + this.timeVector
                    + ", demandVector=" + this.demandVector + ", simulator=" + this.simulator + "]";
        }

    }

}
