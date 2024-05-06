package org.opentrafficsim.demo;

import java.io.BufferedWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djunits.value.vdouble.vector.FrequencyVector;
import org.djunits.value.vdouble.vector.TimeVector;
import org.djutils.cli.CliUtil;
import org.djutils.data.csv.CsvData;
import org.djutils.data.serialization.TextSerializationException;
import org.djutils.draw.point.OrientedPoint2d;
import org.djutils.draw.point.Point2d;
import org.djutils.event.Event;
import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;
import org.djutils.io.CompressedFileWriter;
import org.opentrafficsim.animation.colorer.GtuTypeColorer;
import org.opentrafficsim.animation.gtu.colorer.AccelerationGtuColorer;
import org.opentrafficsim.animation.gtu.colorer.GtuColorer;
import org.opentrafficsim.animation.gtu.colorer.IdGtuColorer;
import org.opentrafficsim.animation.gtu.colorer.SpeedGtuColorer;
import org.opentrafficsim.animation.gtu.colorer.SwitchableGtuColorer;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterSet;
import org.opentrafficsim.base.parameters.ParameterTypeDuration;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.base.parameters.constraint.NumericConstraint;
import org.opentrafficsim.core.definitions.Defaults;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.definitions.Definitions;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.gtu.GtuCharacteristics;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.gtu.perception.DirectEgoPerception;
import org.opentrafficsim.core.gtu.perception.EgoPerception;
import org.opentrafficsim.core.gtu.perception.Perception;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.parameters.ParameterFactoryByType;
import org.opentrafficsim.road.definitions.DefaultsRoadNl;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions.LaneBias;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions.LaneBiases;
import org.opentrafficsim.road.gtu.generator.characteristics.DefaultLaneBasedGtuCharacteristicsGeneratorOd;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGtuCharacteristics;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGtuCharacteristicsGeneratorOd;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.VehicleModel;
import org.opentrafficsim.road.gtu.lane.perception.CategoricalLanePerception;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.AnticipationTrafficPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.DirectInfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.DirectIntersectionPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.DirectNeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.HeadwayGtuType;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtu;
import org.opentrafficsim.road.gtu.lane.plan.operational.LaneChange;
import org.opentrafficsim.road.gtu.lane.plan.operational.LaneOperationalPlanBuilder;
import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedTacticalPlannerFactory;
import org.opentrafficsim.road.gtu.lane.tactical.following.AbstractIdm;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.following.IdmPlus;
import org.opentrafficsim.road.gtu.lane.tactical.following.IdmPlusFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.AbstractIncentivesTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.AccelerationIncentive;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.DefaultLmrsPerceptionFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.LmrsFactory;
import org.opentrafficsim.road.gtu.lane.tactical.util.CarFollowingUtil;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Cooperation;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Desire;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Incentive;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsParameters;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsUtil;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalRoutePlannerFactory;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.control.rampmetering.CycleTimeLightController;
import org.opentrafficsim.road.network.control.rampmetering.RampMetering;
import org.opentrafficsim.road.network.control.rampmetering.RampMeteringLightController;
import org.opentrafficsim.road.network.control.rampmetering.RampMeteringSwitch;
import org.opentrafficsim.road.network.control.rampmetering.RwsSwitch;
import org.opentrafficsim.road.network.factory.LaneFactory;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.Stripe;
import org.opentrafficsim.road.network.lane.Stripe.Type;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;
import org.opentrafficsim.road.network.lane.object.detector.LoopDetector;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLight;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;
import org.opentrafficsim.road.network.speed.SpeedLimitProspect;
import org.opentrafficsim.road.od.Categorization;
import org.opentrafficsim.road.od.Category;
import org.opentrafficsim.road.od.Interpolation;
import org.opentrafficsim.road.od.OdApplier;
import org.opentrafficsim.road.od.OdMatrix;
import org.opentrafficsim.road.od.OdOptions;
import org.opentrafficsim.swing.script.AbstractSimulationScript;

import nl.tudelft.simulation.jstats.distributions.DistNormal;
import nl.tudelft.simulation.jstats.streams.StreamInterface;
import picocli.CommandLine.Option;

/**
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class RampMeteringDemo extends AbstractSimulationScript
{

    /** Controlled car GTU type id. */
    private static final String CONTROLLED_CAR_ID = "controlledCar";

    /** Parameter factory. */
    private ParameterFactoryByType parameterFactory = new ParameterFactoryByType();

    /** Ramp metering. */
    @Option(names = {"-r", "--rampMetering"}, description = "Ramp metering on or off", defaultValue = "true")
    private boolean rampMetering;

    /** Whether to generate output. */
    @Option(names = "--output", description = "Generate output.", negatable = true, defaultValue = "false")
    private boolean output;

    /** Accepted gap. */
    @Option(names = "--acceptedGap", description = "Accepted gap.") // , defaultValue = "0.5s")
    private Duration acceptedGap = Duration.instantiateSI(0.5);

    /** Main demand. */
    private FrequencyVector mainDemand;

    /** Main demand string. */
    @Option(names = "--mainDemand", description = "Main demand in veh/h.", defaultValue = "2000,3000,3900,3900,3000")
    private String mainDemandString;

    /** Ramp demand. */
    private FrequencyVector rampDemand;

    /** Ramp demand string. */
    @Option(names = "--rampDemand", description = "Ramp demand in veh/h.", defaultValue = "500,500,500,500,500")
    private String rampDemandString;

    /** Demand time. */
    private TimeVector demandTime;

    /** Demand time string. */
    @Option(names = "--demandTime", description = "Demand time in min.", defaultValue = "0,10,40,50,70")
    private String demandTimeString;

    /** Scenario. */
    @Option(names = "--scenario", description = "Scenario name.", defaultValue = "test")
    private String scenario;

    /** GTUs in simulation. */
    private Map<String, Double> gtusInSimulation = new LinkedHashMap<>();

    /** Total travel time, accumulated. */
    private double totalTravelTime = 0.0;

    /** Total travel time delay, accumulated. */
    private double totalTravelTimeDelay = 0.0;

    /** Stores defintions such as GtuTypes. */
    private Definitions definitions = new Definitions();

    /**
     * Constructor.
     */
    protected RampMeteringDemo()
    {
        super("Ramp metering 1", "Ramp metering 2");
    }

    /**
     * @param args String[] command line arguments
     * @throws Exception any exception
     */
    public static void main(final String[] args) throws Exception
    {
        RampMeteringDemo demo = new RampMeteringDemo();
        CliUtil.changeOptionDefault(demo, "simulationTime", "4200s");
        CliUtil.execute(demo, args);
        demo.mainDemand = new FrequencyVector(arrayFromString(demo.mainDemandString), FrequencyUnit.PER_HOUR);
        demo.rampDemand = new FrequencyVector(arrayFromString(demo.rampDemandString), FrequencyUnit.PER_HOUR);
        demo.demandTime = new TimeVector(arrayFromString(demo.demandTimeString), TimeUnit.BASE_MINUTE);
        demo.start();
    }

    /**
     * Returns an array from a String.
     * @param str String; string
     * @return double[] array
     */
    private static double[] arrayFromString(final String str)
    {
        int n = 0;
        for (String part : str.split(","))
        {
            n++;
        }
        double[] out = new double[n];
        int i = 0;
        for (String part : str.split(","))
        {
            out[i] = Double.valueOf(part);
            i++;
        }
        return out;
    }

    /** {@inheritDoc} */
    @Override
    protected RoadNetwork setupSimulation(final OtsSimulatorInterface sim) throws Exception
    {
        RoadNetwork network = new RoadNetwork("RampMetering", sim);
        if (this.output)
        {
            network.addListener(this, Network.GTU_ADD_EVENT);
            network.addListener(this, Network.GTU_REMOVE_EVENT);
        }
        GtuType car = DefaultsNl.CAR;
        GtuType.registerTemplateSupplier(car, Defaults.NL);
        GtuType controlledCar = new GtuType(CONTROLLED_CAR_ID, car);
        this.definitions.add(GtuType.class, car);
        this.definitions.add(GtuType.class, controlledCar);

        GtuColorer[] colorers =
                new GtuColorer[] {new IdGtuColorer(), new SpeedGtuColorer(new Speed(150, SpeedUnit.KM_PER_HOUR)),
                        new AccelerationGtuColorer(Acceleration.instantiateSI(-6.0), Acceleration.instantiateSI(2)),
                        new GtuTypeColorer().add(car).add(controlledCar)};
        SwitchableGtuColorer colorer = new SwitchableGtuColorer(0, colorers);
        setGtuColorer(colorer);

        // parameters
        StreamInterface stream = sim.getModel().getStream("generation");
        this.parameterFactory.addParameter(ParameterTypes.FSPEED, new DistNormal(stream, 123.7 / 120.0, 12.0 / 1200));

        Node nodeA = new Node(network, "A", new Point2d(0, 0), Direction.ZERO);
        Node nodeB = new Node(network, "B", new Point2d(3000, 0), Direction.ZERO);
        Node nodeC = new Node(network, "C", new Point2d(3250, 0), Direction.ZERO);
        Node nodeD = new Node(network, "D", new Point2d(6000, 0), Direction.ZERO);
        Node nodeE = new Node(network, "E", new Point2d(2000, -25), Direction.ZERO);
        Node nodeF = new Node(network, "F", new Point2d(2750, 0.0), Direction.ZERO);

        LinkType freeway = DefaultsNl.FREEWAY;
        LaneKeepingPolicy policy = LaneKeepingPolicy.KEEPRIGHT;
        Length laneWidth = Length.instantiateSI(3.6);
        LaneType freewayLane = DefaultsRoadNl.FREEWAY;
        Speed speedLimit = new Speed(120, SpeedUnit.KM_PER_HOUR);
        Speed rampSpeedLimit = new Speed(70, SpeedUnit.KM_PER_HOUR);
        List<Lane> lanesAB = new LaneFactory(network, nodeA, nodeB, freeway, sim, policy, DefaultsNl.VEHICLE)
                .leftToRight(1.0, laneWidth, freewayLane, speedLimit).addLanes(Type.DASHED).getLanes();
        List<Stripe> stripes = new ArrayList<>();
        List<Lane> lanesBC = new LaneFactory(network, nodeB, nodeC, freeway, sim, policy, DefaultsNl.VEHICLE)
                .leftToRight(1.0, laneWidth, freewayLane, speedLimit).addLanes(stripes, Type.DASHED, Type.BLOCK).getLanes();
        stripes.get(2).addPermeability(car, LateralDirectionality.LEFT); // prevent right lane changes over block stripe
        List<Lane> lanesCD = new LaneFactory(network, nodeC, nodeD, freeway, sim, policy, DefaultsNl.VEHICLE)
                .leftToRight(1.0, laneWidth, freewayLane, speedLimit).addLanes(Type.DASHED).getLanes();
        List<Lane> lanesEF = new LaneFactory(network, nodeE, nodeF, freeway, sim, policy, DefaultsNl.VEHICLE)
                .setOffsetEnd(laneWidth.times(1.5).neg()).leftToRight(0.5, laneWidth, freewayLane, rampSpeedLimit).addLanes()
                .getLanes();
        List<Lane> lanesFB = new LaneFactory(network, nodeF, nodeB, freeway, sim, policy, DefaultsNl.VEHICLE)
                .setOffsetStart(laneWidth.times(1.5).neg()).setOffsetEnd(laneWidth.times(1.5).neg())
                .leftToRight(0.5, laneWidth, freewayLane, speedLimit).addLanes().getLanes();
        // detectors
        Time first = Time.instantiateSI(60.0);
        Duration agg = Duration.instantiateSI(60.0);
        // TODO: detector length affects occupancy, which length to use?
        Length detectorLength = Length.ZERO;
        LoopDetector det1 = new LoopDetector("1", lanesAB.get(0), Length.instantiateSI(2900), detectorLength,
                DefaultsRoadNl.LOOP_DETECTOR, sim, first, agg, LoopDetector.MEAN_SPEED, LoopDetector.OCCUPANCY);
        LoopDetector det2 = new LoopDetector("2", lanesAB.get(1), Length.instantiateSI(2900), detectorLength,
                DefaultsRoadNl.LOOP_DETECTOR, sim, first, agg, LoopDetector.MEAN_SPEED, LoopDetector.OCCUPANCY);
        LoopDetector det3 = new LoopDetector("3", lanesCD.get(0), Length.instantiateSI(100), detectorLength,
                DefaultsRoadNl.LOOP_DETECTOR, sim, first, agg, LoopDetector.MEAN_SPEED, LoopDetector.OCCUPANCY);
        LoopDetector det4 = new LoopDetector("4", lanesCD.get(1), Length.instantiateSI(100), detectorLength,
                DefaultsRoadNl.LOOP_DETECTOR, sim, first, agg, LoopDetector.MEAN_SPEED, LoopDetector.OCCUPANCY);
        List<LoopDetector> detectors12 = new ArrayList<>();
        detectors12.add(det1);
        detectors12.add(det2);
        List<LoopDetector> detectors34 = new ArrayList<>();
        detectors34.add(det3);
        detectors34.add(det4);
        if (this.rampMetering)
        {
            // traffic light
            TrafficLight light = new TrafficLight("light", lanesEF.get(0), lanesEF.get(0).getLength(), sim);
            List<TrafficLight> lightList = new ArrayList<>();
            lightList.add(light);
            // ramp metering
            RampMeteringSwitch rampSwitch = new RwsSwitch(detectors12);
            RampMeteringLightController rampLightController =
                    new CycleTimeLightController(sim, lightList, DefaultsRoadNl.LOOP_DETECTOR);
            new RampMetering(sim, rampSwitch, rampLightController);
        }

        // OD
        List<Node> origins = new ArrayList<>();
        origins.add(nodeA);
        origins.add(nodeE);
        List<Node> destinations = new ArrayList<>();
        destinations.add(nodeD);
        Categorization categorization = new Categorization("cat", GtuType.class);// , Lane.class);
        Interpolation globalInterpolation = Interpolation.LINEAR;
        OdMatrix od = new OdMatrix("rampMetering", origins, destinations, categorization, this.demandTime, globalInterpolation);
        // Category carCatMainLeft = new Category(categorization, car, lanesAB.get(0));
        // Category carCatMainRight = new Category(categorization, car, lanesAB.get(1));
        Category carCatRamp = new Category(categorization, car);// , lanesEB.get(0));
        Category controlledCarCat = new Category(categorization, controlledCar);
        // double fLeft = 0.6;
        od.putDemandVector(nodeA, nodeD, carCatRamp, this.mainDemand, 0.6);
        od.putDemandVector(nodeA, nodeD, controlledCarCat, this.mainDemand, 0.4);
        // od.putDemandVector(nodeA, nodeD, carCatMainLeft, mainDemand, fLeft);
        // od.putDemandVector(nodeA, nodeD, carCatMainRight, mainDemand, 1.0 - fLeft);
        od.putDemandVector(nodeE, nodeD, carCatRamp, this.rampDemand, 0.6);
        od.putDemandVector(nodeE, nodeD, controlledCarCat, this.rampDemand, 0.4);
        OdOptions odOptions = new OdOptions();
        DefaultLaneBasedGtuCharacteristicsGeneratorOd.Factory factory =
                new DefaultLaneBasedGtuCharacteristicsGeneratorOd.Factory(new LaneBasedStrategicalRoutePlannerFactory(
                        new LmrsFactory(new IdmPlusFactory(stream), new DefaultLmrsPerceptionFactory())));
        odOptions.set(OdOptions.GTU_TYPE, new ControlledStrategicalPlannerGenerator(factory.create()));
        odOptions.set(OdOptions.INSTANT_LC, true);
        odOptions.set(OdOptions.LANE_BIAS, new LaneBiases().addBias(car, LaneBias.WEAK_LEFT));
        odOptions.set(OdOptions.NO_LC_DIST, Length.instantiateSI(300));
        OdApplier.applyOd(network, od, odOptions, DefaultsRoadNl.ROAD_USERS);

        return network;
    }

    /**
     * Returns the parameter factory.
     * @return ParameterFactoryByType; parameter factory
     */
    final ParameterFactoryByType getParameterFactory()
    {
        return this.parameterFactory;
    }

    /** {@inheritDoc} */
    @Override
    public void notify(final Event event) throws RemoteException
    {
        if (event.getType().equals(Network.GTU_ADD_EVENT))
        {
            this.gtusInSimulation.put((String) event.getContent(), getSimulator().getSimulatorTime().si);
        }
        else if (event.getType().equals(Network.GTU_REMOVE_EVENT))
        {
            measureTravelTime((String) event.getContent());
        }
        else
        {
            super.notify(event);
        }
    }

    /**
     * Adds travel time and delay for a single GTU.
     * @param id String; id of the GTU
     */
    private void measureTravelTime(final String id)
    {
        double tt = getSimulator().getSimulatorTime().si - this.gtusInSimulation.get(id);
        double x = getNetwork().getGTU(id).getOdometer().si;
        // TODO: we assume 120km/h everywhere, including the slower ramps
        double ttd = tt - (x / (120 / 3.6));
        this.totalTravelTime += tt;
        this.totalTravelTimeDelay += ttd;
        this.gtusInSimulation.remove(id);
    }

    /** {@inheritDoc} */
    @Override
    protected void onSimulationEnd()
    {
        if (this.output)
        {
            // detector data
            String file = String.format("%s_%02d_detectors.csv", this.scenario, getSeed());
            try
            {
                CsvData.writeData(file, file + ".header", LoopDetector.asTablePeriodicData(getNetwork()));
            }
            catch (IOException | TextSerializationException exception)
            {
                throw new RuntimeException(exception);
            }

            // travel time data
            for (Gtu gtu : getNetwork().getGTUs())
            {
                measureTravelTime(gtu.getId());
            }
            Throw.when(!this.gtusInSimulation.isEmpty(), RuntimeException.class,
                    "GTUs remain in simulation that are not measured.");
            file = String.format("%s_%02d_time.txt", this.scenario, getSeed());
            BufferedWriter bw = null;
            try
            {
                bw = CompressedFileWriter.create(file, false);
                bw.write(String.format("Total travel time: %.3fs", this.totalTravelTime));
                bw.newLine();
                bw.write(String.format("Total travel time delay: %.3fs", this.totalTravelTimeDelay));
                bw.close();
            }
            catch (IOException exception)
            {
                throw new RuntimeException(exception);
            }
            finally
            {
                try
                {
                    if (bw != null)
                    {
                        bw.close();
                    }
                }
                catch (IOException ex)
                {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    /**
     * Strategical planner generator. This class can be used as input in {@code OdOptions} to generate the right models with
     * different GTU types.
     */
    private class ControlledStrategicalPlannerGenerator implements LaneBasedGtuCharacteristicsGeneratorOd
    {

        /** Default generator. */
        private final DefaultLaneBasedGtuCharacteristicsGeneratorOd defaultGenerator;

        /** Controlled planner factory. */
        private LaneBasedStrategicalPlannerFactory<?> controlledPlannerFactory;

        /**
         * Constructor.
         * @param defaultGenerator LaneBasedGtuCharacteristicsGenerator; generator for non-controlled GTU's
         */
        ControlledStrategicalPlannerGenerator(final DefaultLaneBasedGtuCharacteristicsGeneratorOd defaultGenerator)
        {
            this.defaultGenerator = defaultGenerator;
            // anonymous factory to create tactical planners for controlled GTU's
            LaneBasedTacticalPlannerFactory<?> tacticalPlannerFactory =
                    new LaneBasedTacticalPlannerFactory<LaneBasedTacticalPlanner>()
                    {
                        @Override
                        public Parameters getParameters() throws ParameterException
                        {
                            ParameterSet set = new ParameterSet();
                            set.setDefaultParameter(ParameterTypes.PERCEPTION);
                            set.setDefaultParameter(ParameterTypes.LOOKBACK);
                            set.setDefaultParameter(ParameterTypes.LOOKAHEAD);
                            set.setDefaultParameter(ParameterTypes.S0);
                            set.setDefaultParameter(ParameterTypes.TMIN);
                            set.setDefaultParameter(ParameterTypes.TMAX);
                            set.setDefaultParameter(ParameterTypes.DT);
                            set.setDefaultParameter(ParameterTypes.VCONG);
                            set.setDefaultParameter(ParameterTypes.T0);
                            set.setDefaultParameter(ParameterTypes.BCRIT);
                            set.setDefaultParameters(LmrsParameters.class);
                            set.setDefaultParameters(AbstractIdm.class);
                            return set;
                        }

                        @SuppressWarnings("synthetic-access")
                        @Override
                        public LaneBasedTacticalPlanner create(final LaneBasedGtu gtu) throws GtuException
                        {
                            // here the lateral control system is initiated
                            ParameterSet settings = new ParameterSet();
                            try
                            {
                                // system operation settings
                                settings.setParameter(SyncAndAccept.SYNCTIME, Duration.instantiateSI(1.0));
                                settings.setParameter(SyncAndAccept.COOPTIME, Duration.instantiateSI(2.0));
                                // parameters used in car-following model for gap-acceptance
                                settings.setParameter(AbstractIdm.DELTA, 1.0);
                                settings.setParameter(ParameterTypes.S0, Length.instantiateSI(3.0));
                                settings.setParameter(ParameterTypes.A, Acceleration.instantiateSI(2.0));
                                settings.setParameter(ParameterTypes.B, Acceleration.instantiateSI(2.0));
                                settings.setParameter(ParameterTypes.T, RampMeteringDemo.this.acceptedGap);
                                settings.setParameter(ParameterTypes.FSPEED, 1.0);
                                settings.setParameter(ParameterTypes.B0, Acceleration.instantiateSI(0.5));
                                settings.setParameter(ParameterTypes.VCONG, new Speed(60, SpeedUnit.KM_PER_HOUR));
                            }
                            catch (ParameterException exception)
                            {
                                throw new GtuException(exception);
                            }
                            return new ControlledTacticalPlanner(gtu, new SyncAndAccept(gtu, new IdmPlus(), settings));
                        }
                    };
            // standard strategical planner factory using the tactical factory and the simulation-wide parameter factory
            this.controlledPlannerFactory = new LaneBasedStrategicalRoutePlannerFactory(tacticalPlannerFactory,
                    RampMeteringDemo.this.getParameterFactory());
        }

        /** {@inheritDoc} */
        @Override
        public LaneBasedGtuCharacteristics draw(final Node origin, final Node destination, final Category category,
                final StreamInterface randomStream) throws GtuException
        {
            GtuType gtuType = category.get(GtuType.class);
            // if GTU type is a controlled car, create characteristics for a controlled car
            if (gtuType.equals(RampMeteringDemo.this.definitions.get(GtuType.class, CONTROLLED_CAR_ID)))
            {
                Route route = null;
                VehicleModel vehicleModel = VehicleModel.MINMAX;
                GtuCharacteristics gtuCharacteristics =
                        GtuType.defaultCharacteristics(gtuType, origin.getNetwork(), randomStream);
                return new LaneBasedGtuCharacteristics(gtuCharacteristics, this.controlledPlannerFactory, route, origin,
                        destination, vehicleModel);
            }
            // otherwise generate default characteristics
            return this.defaultGenerator.draw(origin, destination, category, randomStream);
        }

    }

    /** Tactical planner. */
    private static class ControlledTacticalPlanner extends AbstractIncentivesTacticalPlanner
    {
        /** */
        private static final long serialVersionUID = 20190731L;

        /** Lane change system. */
        private AutomaticLaneChangeSystem laneChangeSystem;

        /** Lane change status. */
        private final LaneChange laneChange;

        /** Map that {@code getLaneChangeDesire} writes current desires in. This is not used here. */
        private Map<Class<? extends Incentive>, Desire> dummyMap = new LinkedHashMap<>();

        /**
         * Constructor.
         * @param gtu LaneBasedGtu; gtu
         * @param laneChangeSystem AutomaticLaneChangeSystem; lane change system
         */
        ControlledTacticalPlanner(final LaneBasedGtu gtu, final AutomaticLaneChangeSystem laneChangeSystem)
        {
            super(new IdmPlus(), gtu, generatePerception(gtu));
            setDefaultIncentives();
            this.laneChangeSystem = laneChangeSystem;
            this.laneChange = Try.assign(() -> new LaneChange(gtu), "Parameter LCDUR is required.", GtuException.class);
        }

        /**
         * Helper method to create perception.
         * @param gtu LaneBasedGtu; gtu
         * @return LanePerception lane perception
         */
        private static LanePerception generatePerception(final LaneBasedGtu gtu)
        {
            CategoricalLanePerception perception = new CategoricalLanePerception(gtu);
            perception.addPerceptionCategory(new DirectEgoPerception<LaneBasedGtu, Perception<LaneBasedGtu>>(perception));
            perception.addPerceptionCategory(new DirectInfrastructurePerception(perception));
            // TODO: perceived GTUs as first type
            perception.addPerceptionCategory(new DirectNeighborsPerception(perception, HeadwayGtuType.WRAP));
            perception.addPerceptionCategory(new AnticipationTrafficPerception(perception));
            perception.addPerceptionCategory(new DirectIntersectionPerception(perception, HeadwayGtuType.WRAP));
            return perception;
        }

        /** {@inheritDoc} */
        @Override
        public OperationalPlan generateOperationalPlan(final Time startTime, final OrientedPoint2d locationAtStartTime)
                throws OperationalPlanException, GtuException, NetworkException, ParameterException
        {
            // get some general input
            Speed speed = getPerception().getPerceptionCategory(EgoPerception.class).getSpeed();
            SpeedLimitProspect slp = getPerception().getPerceptionCategory(InfrastructurePerception.class)
                    .getSpeedLimitProspect(RelativeLane.CURRENT);
            SpeedLimitInfo sli = slp.getSpeedLimitInfo(Length.ZERO);

            // LMRS desire
            Desire desire = LmrsUtil.getLaneChangeDesire(getGtu().getParameters(), getPerception(), getCarFollowingModel(),
                    getMandatoryIncentives(), getVoluntaryIncentives(), this.dummyMap);

            // other vehicles respond to these 'interpreted' levels of lane change desire
            getGtu().getParameters().setParameter(LmrsParameters.DLEFT, desire.left());
            getGtu().getParameters().setParameter(LmrsParameters.DRIGHT, desire.right());

            // car-following
            Acceleration a = getGtu().getCarFollowingAcceleration();

            // cooperation
            Acceleration aCoop = Cooperation.PASSIVE.cooperate(getPerception(), getGtu().getParameters(), sli,
                    getCarFollowingModel(), LateralDirectionality.LEFT, desire);
            a = Acceleration.min(a, aCoop);
            aCoop = Cooperation.PASSIVE.cooperate(getPerception(), getGtu().getParameters(), sli, getCarFollowingModel(),
                    LateralDirectionality.RIGHT, desire);
            a = Acceleration.min(a, aCoop);

            // compose human plan
            SimpleOperationalPlan simplePlan =
                    new SimpleOperationalPlan(a, getGtu().getParameters().getParameter(ParameterTypes.DT));
            for (AccelerationIncentive incentive : getAccelerationIncentives())
            {
                incentive.accelerate(simplePlan, RelativeLane.CURRENT, Length.ZERO, getGtu(), getPerception(),
                        getCarFollowingModel(), speed, getGtu().getParameters(), sli);
            }

            // add lane change control
            if (!this.laneChange.isChangingLane())
            {
                double dFree = getGtu().getParameters().getParameter(LmrsParameters.DFREE);
                if (this.laneChangeSystem.initiatedLaneChange().isNone())
                {
                    if (desire.leftIsLargerOrEqual() && desire.left() > dFree)
                    {
                        this.laneChangeSystem.initiateLaneChange(LateralDirectionality.LEFT);
                    }
                    else if (desire.right() > dFree)
                    {
                        this.laneChangeSystem.initiateLaneChange(LateralDirectionality.RIGHT);
                    }
                }
                else
                {
                    if ((this.laneChangeSystem.initiatedLaneChange().isLeft() && desire.left() < dFree)
                            || (this.laneChangeSystem.initiatedLaneChange().isRight() && desire.right() < dFree))
                    {
                        this.laneChangeSystem.initiateLaneChange(LateralDirectionality.NONE);
                    }
                }
            }
            simplePlan = this.laneChangeSystem.operate(simplePlan, getGtu().getParameters());
            simplePlan.setTurnIndicator(getGtu());

            // create plan
            return LaneOperationalPlanBuilder.buildPlanFromSimplePlan(getGtu(), startTime, simplePlan, this.laneChange);
        }
    }

    /** Interface allowing tactical planners to use an automatic lane change system. */
    private interface AutomaticLaneChangeSystem
    {

        /**
         * Update operational plan with actions to change lane. This method should be called by the tactical planner always.
         * @param simplePlan SimpleOperationalPlan; plan
         * @param parameters Parameters; parameters
         * @return SimpleOperationalPlan; adapted plan
         * @throws OperationalPlanException if the system runs in to an error
         * @throws ParameterException if a parameter is missing
         */
        SimpleOperationalPlan operate(SimpleOperationalPlan simplePlan, Parameters parameters)
                throws OperationalPlanException, ParameterException;

        /**
         * Returns the direction in which the system was initiated to perform a lane change.
         * @return LateralDirectionality; direction in which the system was initiated to perform a lane change, {@code NONE} if
         *         none
         */
        LateralDirectionality initiatedLaneChange();

        /**
         * Initiate a lane change.
         * @param dir LateralDirectionality; direction, use {@code NONE} to cancel
         */
        void initiateLaneChange(LateralDirectionality dir);

    }

    /** Implementation of an automatic lane change system. */
    private static class SyncAndAccept implements AutomaticLaneChangeSystem
    {
        /** Parameter of time after lane change command when the system will start synchronization. */
        public static final ParameterTypeDuration SYNCTIME = new ParameterTypeDuration("tSync",
                "Time after which synchronization starts.", Duration.instantiateSI(1.0), NumericConstraint.POSITIVE);

        /** Parameter of time after lane change command when the system will start cooperation (indicator). */
        public static final ParameterTypeDuration COOPTIME = new ParameterTypeDuration("tCoop",
                "Time after which cooperation starts (indicator).", Duration.instantiateSI(2.0), NumericConstraint.POSITIVE);

        /** GTU. */
        private final LaneBasedGtu gtu;

        /** Car-following model for gap-acceptance. */
        private final CarFollowingModel carFollowingModel;

        /** Parameters containing the system settings. */
        private final Parameters settings;

        /** Initiated lane change direction. */
        private LateralDirectionality direction = LateralDirectionality.NONE;

        /** Time when the lane change was initiated. */
        private Time initiationTime;

        /**
         * Constructor.
         * @param gtu LaneBasedGtu; GTU
         * @param carFollowingModel CarFollowingModel; car-following model
         * @param settings Parameters; system settings
         */
        SyncAndAccept(final LaneBasedGtu gtu, final CarFollowingModel carFollowingModel, final Parameters settings)
        {
            this.gtu = gtu;
            this.carFollowingModel = carFollowingModel;
            this.settings = settings;
        }

        /** {@inheritDoc} */
        @Override
        public SimpleOperationalPlan operate(final SimpleOperationalPlan simplePlan, final Parameters parameters)
                throws OperationalPlanException, ParameterException
        {
            // active?
            if (this.direction.isNone())
            {
                return simplePlan;
            }

            // check gap
            InfrastructurePerception infra =
                    this.gtu.getTacticalPlanner().getPerception().getPerceptionCategory(InfrastructurePerception.class);
            SpeedLimitInfo sli = infra.getSpeedLimitProspect(RelativeLane.CURRENT).getSpeedLimitInfo(Length.ZERO);
            NeighborsPerception neighbors =
                    this.gtu.getTacticalPlanner().getPerception().getPerceptionCategory(NeighborsPerception.class);
            if (infra.getLegalLaneChangePossibility(RelativeLane.CURRENT, this.direction).gt0()
                    && !neighbors.isGtuAlongside(this.direction)
                    && acceptGap(neighbors.getFirstFollowers(this.direction), sli, false)
                    && acceptGap(neighbors.getFirstLeaders(this.direction), sli, true))
            {
                // gaps accepted, start lane change
                SimpleOperationalPlan plan =
                        new SimpleOperationalPlan(simplePlan.getAcceleration(), simplePlan.getDuration(), this.direction);
                this.direction = LateralDirectionality.NONE;
                this.initiationTime = null;
                return plan;
            }

            // synchronization
            Duration since = this.gtu.getSimulator().getSimulatorAbsTime().minus(this.initiationTime);
            if (since.gt(this.settings.getParameter(SYNCTIME))
                    || this.gtu.getSpeed().lt(this.settings.getParameter(ParameterTypes.VCONG)))
            {
                PerceptionCollectable<HeadwayGtu, LaneBasedGtu> leaders =
                        neighbors.getLeaders(new RelativeLane(this.direction, 1));
                if (!leaders.isEmpty())
                {
                    HeadwayGtu leader = leaders.first();
                    Acceleration a = CarFollowingUtil.followSingleLeader(this.carFollowingModel, this.settings,
                            this.gtu.getSpeed(), sli, leader);
                    a = Acceleration.max(a, this.settings.getParameter(ParameterTypes.B).neg());
                    simplePlan.minimizeAcceleration(a);
                }
            }

            // cooperation
            if (since.gt(this.settings.getParameter(COOPTIME))
                    || this.gtu.getSpeed().lt(this.settings.getParameter(ParameterTypes.VCONG)))
            {
                if (this.direction.isLeft())
                {
                    simplePlan.setIndicatorIntentLeft();
                }
                else
                {
                    simplePlan.setIndicatorIntentRight();
                }
            }

            // return
            return simplePlan;
        }

        /**
         * Checks whether a gap can be accepted.
         * @param neighbors Set&lt;HeadwayGtu&gt;; neighbors
         * @param sli SpeedLimitInfo; speed limit info
         * @param leaders boolean; whether we are dealing with leaders, or followers
         * @return boolean; whether the gap is accepted
         * @throws ParameterException if a parameter is not defined
         */
        private boolean acceptGap(final Set<HeadwayGtu> neighbors, final SpeedLimitInfo sli, final boolean leaders)
                throws ParameterException
        {
            for (HeadwayGtu neighbor : neighbors)
            {
                Acceleration a = CarFollowingUtil.followSingleLeader(this.carFollowingModel, this.settings,
                        leaders ? this.gtu.getSpeed() : neighbor.getSpeed(), sli, neighbor.getDistance(),
                        leaders ? neighbor.getSpeed() : this.gtu.getSpeed());
                if (a.lt(this.settings.getParameter(ParameterTypes.B).neg()))
                {
                    return false;
                }
            }
            return true;
        }

        /** {@inheritDoc} */
        @Override
        public LateralDirectionality initiatedLaneChange()
        {
            return this.direction;
        }

        /** {@inheritDoc} */
        @Override
        public void initiateLaneChange(final LateralDirectionality dir)
        {
            this.direction = dir;
            if (!dir.isNone())
            {
                this.initiationTime = this.gtu.getSimulator().getSimulatorAbsTime();
            }
            else
            {
                this.initiationTime = null;
            }
        }
    }

}
