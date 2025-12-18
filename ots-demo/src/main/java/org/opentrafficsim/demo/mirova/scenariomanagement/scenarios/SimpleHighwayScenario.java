package org.opentrafficsim.demo.mirova.scenariomanagement.scenarios;

import java.util.*;
import java.util.function.Supplier;

import org.djunits.unit.*;
import org.djunits.value.storage.StorageType;
import org.djunits.value.vdouble.scalar.*;
import org.djunits.value.vdouble.vector.FrequencyVector;
import org.djunits.value.vdouble.vector.TimeVector;
import org.djunits.value.vdouble.vector.data.DoubleVectorData;
import org.djutils.immutablecollections.ImmutableMap;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.FixedRouteGenerator;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.object.DetectorType;
import org.opentrafficsim.road.network.*;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.GtuCharacteristics;
import org.opentrafficsim.core.gtu.GtuErrorHandler;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.idgenerator.IdSupplier;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.distributions.ConstantSupplier;
import org.opentrafficsim.core.distributions.FrequencyAndObject;
import org.opentrafficsim.core.distributions.ObjectDistribution;
import org.opentrafficsim.road.network.lane.*;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;
import org.opentrafficsim.road.network.lane.object.detector.LoopDetector;
import org.opentrafficsim.road.network.lane.object.detector.SinkDetector;
import org.opentrafficsim.animation.GraphLaneUtil;
import org.opentrafficsim.base.geometry.OtsLine2d;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterSet;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.road.definitions.DefaultsRoadNl;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions;
import org.opentrafficsim.road.gtu.generator.LaneBasedGtuGenerator;
import org.opentrafficsim.road.gtu.generator.TtcRoomChecker;
import org.opentrafficsim.road.gtu.generator.headway.HeadwayGenerator;

import org.opentrafficsim.road.gtu.generator.characteristics.*;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.*;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.MirovaParameters;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.following.AbstractWiedemannModel;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.following.W99ParameterTypes;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.following.Wiedemann99;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.following.Wiedemann99Factory;
import org.opentrafficsim.road.gtu.lane.VehicleModel;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModelFactory;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.util.logging.extendeddata.*;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalRoutePlannerFactory;
import org.opentrafficsim.road.network.sampling.*;
import org.opentrafficsim.road.od.Categorization;
import org.opentrafficsim.road.od.Category;
import org.opentrafficsim.road.od.Interpolation;
import org.opentrafficsim.road.od.OdApplier;
import org.opentrafficsim.road.od.OdMatrix;
import org.opentrafficsim.road.od.OdOptions;

import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;
import nl.tudelft.simulation.jstats.distributions.DistContinuous;
import nl.tudelft.simulation.jstats.distributions.DistNormal;


import org.opentrafficsim.demo.mirova.scenariomanagement.*;
import org.opentrafficsim.demo.mirova.scenariomanagement.libraries.DesiredSpeedLibrary;
import org.opentrafficsim.draw.graphs.GraphPath;

public class SimpleHighwayScenario extends ScenarioGenerator {

    public SimpleHighwayScenario() {
        super("SimpleHighway");
    }

    // ------------------------------------------------------------
    // Basic road network
    // ------------------------------------------------------------
    @Override
    public void buildNetwork(final OtsSimulatorInterface sim) throws Exception {
        this.network = new RoadNetwork("SimpleHighwayNet", sim);
        Node A = new Node(this.network, "A", new org.djutils.draw.point.Point2d(0, 0), Direction.ZERO);
        Node B = new Node(this.network, "B", new org.djutils.draw.point.Point2d(2000, 0), Direction.ZERO);

        CrossSectionLink link = new CrossSectionLink(
            this.network,
            "AB",
            A,
            B,
            DefaultsNl.FREEWAY,
            new OtsLine2d(A.getPoint(), B.getPoint()),
            null,
            LaneKeepingPolicy.KEEPRIGHT
        );

        List<Lane> listLanes = new ArrayList<>();

        listLanes.add(LaneGeometryUtil.createStraightLane(link, "L1", Length.instantiateSI(-3.5), Length.instantiateSI(3.5),
                DefaultsRoadNl.FREEWAY,
                Map.of(DefaultsNl.VEHICLE, new Speed(250, SpeedUnit.KM_PER_HOUR))));

        listLanes.add(LaneGeometryUtil.createStraightLane(link, "L2", Length.instantiateSI(0), Length.instantiateSI(3.5),
                DefaultsRoadNl.FREEWAY,
                Map.of(DefaultsNl.VEHICLE, new Speed(250, SpeedUnit.KM_PER_HOUR))));
        listLanes.add(LaneGeometryUtil.createStraightLane(link, "L3", Length.instantiateSI(3.5), Length.instantiateSI(3.5),
                DefaultsRoadNl.FREEWAY,
                Map.of(DefaultsNl.VEHICLE, new Speed(250, SpeedUnit.KM_PER_HOUR))));
        LaneGeometryUtil.createStraightStripe(DefaultsRoadNl.SOLID, "1", link, Length.instantiateSI(5.25),
                Length.instantiateSI(0.2));
        LaneGeometryUtil.createStraightStripe(DefaultsRoadNl.DASHED, "2", link, Length.instantiateSI(1.75),
                Length.instantiateSI(0.2));
        LaneGeometryUtil.createStraightStripe(DefaultsRoadNl.DASHED, "3", link, Length.instantiateSI(-1.75),
                Length.instantiateSI(0.2));
        LaneGeometryUtil.createStraightStripe(DefaultsRoadNl.SOLID, "4", link, Length.instantiateSI(-5.25),
                Length.instantiateSI(0.2));

        for (Lane lane : listLanes)
        {
            new SinkDetector(lane, lane.getLength(), DefaultsNl.ROAD_USERS);
            this.initialLongitudinalPositions.add(new LanePosition(lane, new Length(5.0, LengthUnit.SI)));
            addLoopDetector(new LoopDetector("det_"+lane.getId(), new LanePosition(lane, lane.getLength().times(0.5)), Length.ZERO, DefaultsNl.LOOP_DETECTOR, Time.instantiateSI(60.0),
                    Duration.instantiateSI(60.0), LoopDetector.HARMONIC_MEAN_SPEED));
        }

        // get all lanes for later use
        this.listAllLanes = new ArrayList<>();
        ImmutableMap<String, Link> linkMap = this.network.getLinkMap();
        for (Link linkIter : linkMap.values()) {
            CrossSectionLink csLink = (CrossSectionLink) linkIter;
            this.listAllLanes.addAll(csLink.getLanes());
        }
    }

    // The simulation setup uses network + params + output config
    @Override
    public RoadNetwork setupSimulation(
            final OtsSimulatorInterface sim,
            final ScenarioParameters params
            ) throws Exception {

        this.stream = new MersenneTwister(params.getSeed());

        buildNetwork(sim);
        getOutputConfiguration().setRoadNetwork(this.network);
        // ---------------------------------------------------------
        // ROUTE
        // ---------------------------------------------------------
        buildRoutes();

        // ---------------------------------------------------------
        // GTU templates & speed distributions
        // ---------------------------------------------------------
        buildGtuTemplates(sim);

        // ---------------------------------------------------------
        // output road samplers

        buildRoadSamplers();
        buildOutputConfiguration();

        // ---------------------------------------------------------
        //createVehiclesFromODMatrix(params, sim);
        createVehiclesFromGenerator(params, sim);

        return this.network;
    }

    // ------------------------------------------------------------
    // GTU templates
    // ------------------------------------------------------------
    @Override
    public void buildGtuTemplates(final OtsSimulatorInterface sim) throws Exception {

        LaneBasedStrategicalPlannerFactory<?> strategicalPlannerFactoryCars = buildStrategicalPlannerFactoryCar();
        LaneBasedStrategicalPlannerFactory<?> strategicalPlannerFactoryTrucks = buildStrategicalPlannerFactoryTruck();

        Supplier<Route> routeGenerator = new FixedRouteGenerator(this.routes.get("A-B"));

        LaneBasedGtuTemplate car = new LaneBasedGtuTemplate(
                DefaultsNl.CAR,
                new ConstantSupplier<>(Length.instantiateSI(4.0)),
                new ConstantSupplier<>(Length.instantiateSI(2.0)),
                DesiredSpeedLibrary.hoogendoornCars(this.stream),
                strategicalPlannerFactoryCars,
                routeGenerator
               );

        this.gtuTemplates.put(DefaultsNl.CAR, car);

        LaneBasedGtuTemplate truck = new LaneBasedGtuTemplate(
                DefaultsNl.TRUCK,
                   new ConstantSupplier<>(Length.instantiateSI(12.0)),
                   new ConstantSupplier<>(Length.instantiateSI(2.5)),
                   DesiredSpeedLibrary.hoogendoornTrucks(this.stream),
                   strategicalPlannerFactoryTrucks,
                   routeGenerator
        );

        this.gtuTemplates.put(DefaultsNl.TRUCK, truck);
    }

    /** ------------------------------------------------------------
     * Create vehicles from headway generator
     * @param params
     * @param sim
     * @throws Exception
     */
    public void createVehiclesFromGenerator(final ScenarioParameters params, final OtsSimulatorInterface sim) throws Exception {
        HeadwayGenerator headwayGenerator = new HeadwayGenerator(new Frequency(params.getDemand(), FrequencyUnit.PER_HOUR), this.stream);

        ObjectDistribution<LaneBasedGtuTemplate> gtuTypeDistribution = new ObjectDistribution<>(this.stream);
        gtuTypeDistribution.add(new FrequencyAndObject<>(1.0 - params.getTruckShare(), this.gtuTemplates.get(DefaultsNl.CAR)));
        gtuTypeDistribution.add(new FrequencyAndObject<>(params.getTruckShare(), this.gtuTemplates.get(DefaultsNl.TRUCK)));

        LaneBasedGtuTemplateDistribution characteristicsGenerator = new LaneBasedGtuTemplateDistribution(gtuTypeDistribution);

        // Create generator
        new LaneBasedGtuGenerator(
            "Gen",
            headwayGenerator,
            characteristicsGenerator,
            GeneratorPositions.create(this.initialLongitudinalPositions, this.stream, getLaneBiases()),
            this.network,
            sim,
            new TtcRoomChecker(new Duration(1.0, DurationUnit.SI)),
            new IdSupplier("")
        );

    }

    /** ------------------------------------------------------------
     * Create vehicles from OD matrix
     * @param params
     * @param sim
     * @throws Exception
     */
    public void createVehiclesFromODMatrix(final ScenarioParameters params, final OtsSimulatorInterface sim) throws Exception {
        OdMatrix odMatrix = build3LaneDemandOD(
                this.network.getNode("A"),
                this.network.getNode("B")
        );

        // Define GTU characteristics generator for OD
        LaneBasedGtuCharacteristicsGeneratorOd characteristicsGenerator =
                buildOdsCharacteristicsGenerator(sim);


        OdOptions odOptions = new OdOptions();
        odOptions.set(OdOptions.GTU_TYPE, characteristicsGenerator);
        odOptions.set(OdOptions.ERROR_HANDLER, GtuErrorHandler.DELETE);
        odOptions.set(OdOptions.LANE_BIAS, getLaneBiases());

        OdApplier.applyOd(this.network, odMatrix, odOptions, new DetectorType("NL.VEHICLES"));
    }

    /** ------------------------------------------------------------
     * Build strategical planner factory for cars
     * @return
     */
    public LaneBasedStrategicalPlannerFactory<?> buildStrategicalPlannerFactoryCar()
    {
        CarFollowingModelFactory<Wiedemann99> w99CarFactory = new Wiedemann99Factory(this.stream) {
            @Override
            public Parameters getParameters() throws ParameterException {
                ParameterSet parameters = new ParameterSet();
                parameters.setDefaultParameters(W99ParameterTypes.class);
                //parameters.setParameter(ParameterTypes.T, Duration.instantiateSI(1.0)); // desired time headway
                DistContinuous fSpeed = new DistNormal(SimpleHighwayScenario.this.stream, 123.7 / 120.0, 0.1);
                parameters.setParameter(AbstractWiedemannModel.FSPEED, fSpeed.draw());
                return parameters;
            }
        };

        MirovaTacticalPlannerFactory mirovaTacticalPlannerFactoryCars =
                new MirovaTacticalPlannerFactory(w99CarFactory, new DefaultMirovaPerceptionFactory())
        {
            @Override
            public Parameters getParameters() throws ParameterException {
                Parameters parameters = getDefaultParameters();
                //DistContinuous cooldown = new DistUniform(SimpleHighwayScenario.this.stream, 3.0, 6.0);
                //parameters.setParameter(MirovaParameters.socialInteractionCooldown, Duration.instantiateSI(cooldown.draw()));
                parameters.setParameter(ParameterTypes.TMAX, new Duration(1.0, DurationUnit.SI));
                parameters.setParameter(ParameterTypes.TMIN, new Duration(0.5, DurationUnit.SI));
                parameters.setParameter(MirovaParameters.socioSpeedSensitivity, 0.75);
                return parameters;
            }
        };

        LaneBasedStrategicalPlannerFactory<?> strategicalPlannerFactoryCars = new LaneBasedStrategicalRoutePlannerFactory(
                mirovaTacticalPlannerFactoryCars);

        return strategicalPlannerFactoryCars;
    }

    public LaneBasedStrategicalPlannerFactory<?> buildStrategicalPlannerFactoryTruck()
    {
        CarFollowingModelFactory<Wiedemann99> w99TruckFactory = new Wiedemann99Factory(this.stream) {
            @Override
            public Parameters getParameters() throws ParameterException {
                ParameterSet parameters = new ParameterSet();
                parameters.setDefaultParameters(W99ParameterTypes.class);
                DistContinuous fSpeed = new DistNormal(SimpleHighwayScenario.this.stream, 123.7 / 120.0, 0.1);
                parameters.setParameter(AbstractWiedemannModel.FSPEED, fSpeed.draw());
                return parameters;
            }
        };

        MirovaTacticalPlannerFactory mirovaTacticalPlannerFactoryTrucks =
                new MirovaTacticalPlannerFactory(w99TruckFactory, new DefaultMirovaPerceptionFactory())
        {
            @Override
            public Parameters getParameters() throws ParameterException {
                Parameters parameters = getDefaultParameters();
                parameters.setParameter(ParameterTypes.TMAX, new Duration(1.6, DurationUnit.SI));
                parameters.setParameter(ParameterTypes.TMIN, new Duration(1.2, DurationUnit.SI));
                //DistContinuous cooldown = new DistUniform(SimpleHighwayScenario.this.stream, 3.0, 6.0);
                //parameters.setParameter(MirovaParameters.socialInteractionCooldown, Duration.instantiateSI(cooldown.draw()));
                parameters.setParameter(MirovaParameters.vGain, new Speed(80.0, SpeedUnit.KM_PER_HOUR)); // higher vGain for trucks to reduce discretionary lane changes
                parameters.setParameter(MirovaParameters.socioSpeedSensitivity, 0.75); // more conservative lane changes for trucks
                return parameters;
            }
        };

        LaneBasedStrategicalPlannerFactory<?> strategicalPlannerFactoryTrucks = new LaneBasedStrategicalRoutePlannerFactory(
                mirovaTacticalPlannerFactoryTrucks);

        return strategicalPlannerFactoryTrucks;
    }

    public void buildODMatrixFactory() throws Exception {
        OdMatrix odMatrix = build3LaneDemandOD(
                this.network.getNode("A"),
                this.network.getNode("B")
        );

        LaneBasedStrategicalPlannerFactory<?> strategicalPlannerFactoryCars = buildStrategicalPlannerFactoryCar();
        LaneBasedStrategicalPlannerFactory<?> strategicalPlannerFactoryTrucks = buildStrategicalPlannerFactoryTruck();

    }

    @Override
    public void buildRoutes() {
        try
        {
            this.routes.put("A-B", new Route("RouteAB", DefaultsNl.VEHICLE,
                    List.of(this.network.getNode("A"), this.network.getNode("B"))));
        }
        catch (NetworkException exception)
        {
            exception.printStackTrace();
        }
    }

    /** ------------------------------------------------------------
     *Build road samplers
     * @param sim OtsSimulatorInterface
     * @throws NetworkException
     */
    @Override
    public void buildRoadSamplers() throws NetworkException {

        RoadSampler sampler = RoadSampler.build(this.network)
                .registerExtendedDataType(new ExtendedDataRelaxedHeadway())
                .registerExtendedDataType(new ExtendedDataHeadwayRelaxationProgress())
                .registerExtendedDataType(new ExtendedDataRelaxationTargetHeadway())
                .registerExtendedDataType(new ExtendedDataActionState())
                .registerExtendedDataType(new ExtendedDataLaneChangeDesireLeft())
                .registerExtendedDataType(new ExtendedDataLaneChangeDesireRight())
                .registerExtendedDataType(new ExtendedDataIsChangingLane())
                .registerExtendedDataType(new ExtendedDataLaneChangePlan())
                .registerExtendedDataType(new ExtendedDataLaneChangePlanDirection())
                .registerExtendedDataType(new ExtendedDataFrontGapTimeHeadway())
                .registerExtendedDataType(new ExtendedDataFrontGapDeltaSpeed())
                .registerExtendedDataType(new ExtendedDataFrontGapDistance())
                .registerExtendedDataType(new ExtendedDataW99DrivingMode())
                .registerExtendedDataType(new ExtendedDataFollowerDecelRight())
                .registerExtendedDataType(new ExtendedDataFollowerDecelLeft())
                .registerExtendedDataType(new ExtendedDataEgoDecelRight())
                .registerExtendedDataType(new ExtendedDataEgoDecelLeft())
                .registerExtendedDataType(new ExtendedDataCurrentCFAcceleration())
                .registerExtendedDataType(new ExtendedDataCurrentDesiredSpeed())
                .registerExtendedDataType(new ExtendedDataSocioSpeedPressure())
                .create();

        // activates sampling on all lanes for the entire simulation duration
        for (Lane lane : this.listAllLanes) {
            GraphPath<LaneDataRoad> path = GraphLaneUtil.createPath("path", lane);
            sampler.scheduleStartRecording(Time.instantiateSI(0), path.get(0).getSource(0));

        }

        this.listRoadSamplers.add(sampler);

    }


    public LaneBasedGtuCharacteristicsGeneratorOd buildOdsCharacteristicsGenerator(
            final OtsSimulatorInterface sim)
    {
        return new LaneBasedGtuCharacteristicsGeneratorOd()
        {
            @Override
            public LaneBasedGtuCharacteristics draw(
                    final Node origin, final Node destination, final Category category, final StreamInterface randomStream)
                    throws GtuException
            {
                GtuType gtuType = category.get(GtuType.class);
                LaneBasedGtuTemplate template = SimpleHighwayScenario.this.gtuTemplates.get(gtuType);
                Route route = null;
                try
                {
                    route = new Route("ODRoute", gtuType,
                            List.of(origin, destination));
                }
                catch (NetworkException exception)
                {
                    exception.printStackTrace();
                }
                GtuCharacteristics gtuCharacteristics =  getGtuTemplates().get(gtuType).get(); // Defaults.NL.apply(gtuType, randomStream).get() ;
                VehicleModel vehicleModel = VehicleModel.MINMAX;
                LaneBasedStrategicalPlannerFactory<?> strategical = SimpleHighwayScenario.this.gtuTemplates.get(gtuType).getStrategicalPlannerFactory();
                return new LaneBasedGtuCharacteristics(gtuCharacteristics, strategical, route, origin, destination, vehicleModel);
            }
        };
    }

    @Override
    public void setDefaultParameters() {
        this.defaultParameters.setDemand(4000.0); // vehicles per hour
        this.defaultParameters.setTruckShare(0.1); // 10% trucks
        this.defaultParameters.setSeed(42L); // random seed
    }

    @Override
    public List<Node> getOrigins(final RoadNetwork network) {
        List<Node> origins = new ArrayList<>();
        origins.add(network.getNode("A"));
        return origins;
    }

    @Override
    public List<Node> getDestinations(final RoadNetwork network) {

        List<Node> destinations = new ArrayList<>();
        destinations.add(network.getNode("B"));
        return destinations;
    }

    @Override
    public ScenarioOutputConfiguration buildOutputConfiguration() {
        this.outputConfiguration.addRoadSamplers(this.listRoadSamplers).addLoopDetectors(this.listLoopDetectors);
        return this.outputConfiguration;
    }


    /**
     * Builds an OD-matrix for a 3-lane motorway scenario following the demand pattern
     * described in Weyland et al.’s simulation setup. The generated demand covers a
     * complete loading and unloading cycle, starting with a warm-up period, ramping up
     * to peak traffic, and ramping down symmetrically.
     *
     * <p><b>Demand Profile</b>
     * <ul>
     *   <li>Heavy-duty traffic share: 10% trucks, 90% passenger cars</li>
     *   <li>Initial flow: 600 veh/h</li>
     *   <li>Warm-up: 30 minutes at constant 600 veh/h</li>
     *   <li>After warm-up: demand increases by +200 veh/h every 15 minutes</li>
     *   <li>Maximum flow (3-lane scenario): 6600 veh/h</li>
     *   <li>After reaching the maximum, demand decreases by –200 veh/h every 15 minutes</li>
     *   <li>Final flow returns to 600 veh/h</li>
     * </ul>
     *
     * <p><b>Time Vector Structure</b><br>
     * The time vector starts at t = 0.0 h.
     * <ul>
     *   <li>t₀ = 0.00 h</li>
     *   <li>t₁ = 0.50 h (warm-up interval)</li>
     *   <li>From t₂ onward: increments of 0.25 h (15-minute intervals)</li>
     * </ul>
     *
     * <p><b>Matrix Structure</b><br>
     * A single OD relation (origin → destination) is populated with two demand vectors:
     * one for cars and one for trucks. Values are interpreted as step-wise interpolation
     * (constant demand within each interval).
     *
     * @param origin The origin node of the OD relation.
     *
     * @param destination The destination node of the OD relation.
     *
     * @return A fully populated {@link OdMatrix} containing the complete 3-lane demand curve with
     *        warm-up, rise, peak, and decline phases.
     *
     * @throws Exception If the OD matrix cannot be created or demand vectors cannot be assigned.
     */

    public OdMatrix build3LaneDemandOD(
            final Node origin,
            final Node destination
            ) throws Exception
    {
        // ---------------------------------------------------------------
        // TIME VECTOR — first interval 30 min, then 15 min increments
        // ---------------------------------------------------------------
        int steps = 62;                // 0..61 → 62 time points
        double[] time = new double[steps];

        time[0] = 0.0;                 // t0
        time[1] = 0.5;                 // t1 = warm-up (30 min)

        for (int i = 2; i < steps; i++)
        {
            time[i] = 0.5 + (i - 1) * 0.25;  // from 0.75 onward every 15 min
        }

        TimeVector timeVector = new TimeVector(
                DoubleVectorData.instantiate(time, TimeUnit.BASE_HOUR.getScale(), StorageType.DENSE),
                TimeUnit.BASE_HOUR
        );

        // ---------------------------------------------------------------
        // DEMAND CURVE — 3-lane (600 → 6600 → 600)
        // ---------------------------------------------------------------
        double[] totalDemand = new double[steps];

        int riseSteps = 30;

        // i=0 and i=1 = warm-up flat at 600
        totalDemand[0] = 600;
        totalDemand[1] = 600;

        // Rising phase starts at index 2
        for (int i = 2; i <= riseSteps + 1; i++)
        {
            totalDemand[i] = 600 + 200 * (i - 1);  // shift by 1 due to warm-up
        }

        // Falling phase
        for (int i = riseSteps + 2; i < steps; i++)
        {
            int k = i - (riseSteps + 1);
            totalDemand[i] = 6600 - 200 * k;
        }

        // ---------------------------------------------------------------
        // SPLIT INTO CAR / TRUCK DEMAND
        // ---------------------------------------------------------------
        double[] carDemand   = new double[steps];
        double[] truckDemand = new double[steps];

        for (int i = 0; i < steps; i++)
        {
            carDemand[i]   = totalDemand[i] * 0.90;
            truckDemand[i] = totalDemand[i] * 0.10;
        }

        FrequencyVector carFreq = new FrequencyVector(
                DoubleVectorData.instantiate(carDemand, FrequencyUnit.PER_HOUR.getScale(), StorageType.DENSE),
                FrequencyUnit.PER_HOUR
        );
        System.out.println("carFreq=" + carFreq.toString());

        FrequencyVector truckFreq = new FrequencyVector(
                DoubleVectorData.instantiate(truckDemand, FrequencyUnit.PER_HOUR.getScale(), StorageType.DENSE),
                FrequencyUnit.PER_HOUR
        );
        System.out.println("truckFreq=" + truckFreq.toString());

        // ---------------------------------------------------------------
        // OD MATRIX
        // ---------------------------------------------------------------
        Categorization categorization = new Categorization("MyCategorization", GtuType.class);


        OdMatrix odMatrix = new OdMatrix(
                "OD_3LaneScenario",
                getOrigins(this.network),
                getDestinations(this.network),
                categorization,
                timeVector,
                Interpolation.STEPWISE
        );

        Category carCat   = new Category(odMatrix.getCategorization(), DefaultsNl.CAR);
        Category truckCat = new Category(odMatrix.getCategorization(), DefaultsNl.TRUCK);

        odMatrix.putDemandVector(origin, destination, carCat,   carFreq);
        odMatrix.putDemandVector(origin, destination, truckCat, truckFreq);

        return odMatrix;
    }

    public GeneratorPositions.LaneBiases getLaneBiases() {
        GeneratorPositions.LaneBiases laneBiases = new GeneratorPositions.LaneBiases();
        laneBiases.addBias(DefaultsNl.VEHICLE, GeneratorPositions.LaneBias.bySpeed(150, 80)); // slow vehicles prefer right lane
        return laneBiases;
    }

    /**
     * @return
     */
    public Map<GtuType, LaneBasedGtuTemplate> getGtuTemplates() {
        return this.gtuTemplates;
    }

}


