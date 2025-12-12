package org.opentrafficsim.demo.mirova;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.storage.StorageType;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djunits.value.vdouble.vector.DurationVector;
import org.djunits.value.vdouble.vector.FrequencyVector;
import org.djunits.value.vdouble.vector.TimeVector;
import org.djunits.value.vdouble.vector.data.DoubleVectorData;
import org.djutils.cli.CliUtil;
import org.djutils.draw.point.Point2d;
import org.opentrafficsim.animation.GraphLaneUtil;
import org.opentrafficsim.base.geometry.OtsLine2d;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.definitions.Defaults;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.distributions.ConstantSupplier;
import org.opentrafficsim.core.distributions.FrequencyAndObject;
import org.opentrafficsim.core.distributions.ObjectDistribution;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.gtu.GtuCharacteristics;
import org.opentrafficsim.core.gtu.GtuErrorHandler;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.idgenerator.IdSupplier;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.definitions.DefaultsRoadNl;
import org.opentrafficsim.road.gtu.generator.CfRoomChecker;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions;
import org.opentrafficsim.road.gtu.generator.LaneBasedGtuGenerator;
import org.opentrafficsim.road.gtu.generator.TtcRoomChecker;
import org.opentrafficsim.road.gtu.generator.LaneBasedGtuGenerator.RoomChecker;
import org.opentrafficsim.road.gtu.generator.characteristics.DefaultLaneBasedGtuCharacteristicsGeneratorOd;
import org.opentrafficsim.road.gtu.generator.characteristics.DefaultLaneBasedGtuCharacteristicsGeneratorOd.Factory;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGtuCharacteristics;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGtuCharacteristicsGeneratorOd;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGtuTemplate;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGtuTemplateDistribution;
import org.opentrafficsim.road.gtu.generator.headway.ArrivalsHeadwayGenerator;
import org.opentrafficsim.road.gtu.generator.headway.HeadwayGenerator;
import org.opentrafficsim.road.gtu.lane.VehicleModel;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedTacticalPlannerFactory;
import org.opentrafficsim.road.gtu.lane.tactical.following.IdmPlusFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.DefaultLmrsPerceptionFactory;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.DefaultMirovaPerceptionFactory;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlannerFactory;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.util.logging.extendeddata.ExtendedDataActionState;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.util.logging.extendeddata.ExtendedDataCurrentCFAcceleration;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.util.logging.extendeddata.ExtendedDataCurrentDesiredSpeed;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.util.logging.extendeddata.ExtendedDataEgoDecelLeft;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.util.logging.extendeddata.ExtendedDataEgoDecelRight;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.util.logging.extendeddata.ExtendedDataFollowerDecelLeft;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.util.logging.extendeddata.ExtendedDataFollowerDecelRight;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.util.logging.extendeddata.ExtendedDataFrontGapDeltaSpeed;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.util.logging.extendeddata.ExtendedDataFrontGapDistance;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.util.logging.extendeddata.ExtendedDataFrontGapTimeHeadway;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.util.logging.extendeddata.ExtendedDataHeadwayRelaxationProgress;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.util.logging.extendeddata.ExtendedDataIsChangingLane;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.util.logging.extendeddata.ExtendedDataLaneChangeDesireLeft;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.util.logging.extendeddata.ExtendedDataLaneChangeDesireRight;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.util.logging.extendeddata.ExtendedDataLaneChangePlan;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.util.logging.extendeddata.ExtendedDataLaneChangePlanDirection;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.util.logging.extendeddata.ExtendedDataRelaxationTargetHeadway;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.util.logging.extendeddata.ExtendedDataRelaxedHeadway;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalRoutePlannerFactory;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneGeometryUtil;
import org.opentrafficsim.road.network.lane.LanePosition;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;
import org.opentrafficsim.road.network.lane.object.detector.SinkDetector;
import org.opentrafficsim.road.network.sampling.LaneDataRoad;
import org.opentrafficsim.road.network.sampling.RoadSampler;
import org.opentrafficsim.road.od.Categorization;
import org.opentrafficsim.road.od.Category;
import org.opentrafficsim.road.od.Interpolation;
import org.opentrafficsim.road.od.OdApplier;
import org.opentrafficsim.road.od.OdMatrix;
import org.opentrafficsim.road.od.OdOptions;
import org.opentrafficsim.swing.script.AbstractSimulationScript;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.LmrsFactory;


import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.jstats.distributions.DistEmpiricalInterpolated;
import nl.tudelft.simulation.jstats.distributions.DistUniform;
import nl.tudelft.simulation.jstats.distributions.empirical.InterpolatedEmpiricalDistribution;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.FixedRouteGenerator;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.object.DetectorType;
import org.opentrafficsim.core.parameters.ParameterFactoryByType;
import org.opentrafficsim.core.units.distributions.ContinuousDistDoubleScalar;
import org.opentrafficsim.demo.mirova.scenariomanagement.libraries.DesiredSpeedLibrary;
import org.opentrafficsim.draw.graphs.GraphPath;
import org.opentrafficsim.kpi.sampling.SamplerData;

import picocli.CommandLine.Option;

public class SimpleSimulation extends AbstractSimulationScript
{


    //@Option(names = "--output", description = "Generate output.", negatable = true, defaultValue = "false")
    private boolean output = true;

    RoadSampler samplerExtended;

    RoadNetwork network;

    /** The random number generator used to decide what kind of GTU to generate. */
    private StreamInterface stream = new MersenneTwister(12345);

    protected SimpleSimulation()
    {
        super("Simple simulation", "Example simple simulation");
    }

    public static void main(final String[] args) throws Exception
    {
        SimpleSimulation simpleSimulation = new SimpleSimulation();
        CliUtil.execute(simpleSimulation, args);
        simpleSimulation.start();
    }

    @Override
    protected RoadNetwork setupSimulation(final OtsSimulatorInterface sim) throws NullPointerException, NetworkException, ParameterException, SimRuntimeException
    {
        this.network = new RoadNetwork("Simple network", sim);
        Point2d pointA = new Point2d(0, 0);
        Point2d pointB = new Point2d(2500, 0);
        Node nodeA = new Node(this.network, "A", pointA, Direction.ZERO);
        Node nodeB = new Node(this.network, "B", pointB, Direction.ZERO);
        GtuType car = DefaultsNl.CAR;
        GtuType truck = DefaultsNl.TRUCK;
        GtuType vehicle = DefaultsNl.VEHICLE;

        LinkType freewayLink = DefaultsNl.FREEWAY;
        LaneType freewayLane = DefaultsRoadNl.FREEWAY;
        CrossSectionLink link = new CrossSectionLink(this.network, "AB", nodeA, nodeB, freewayLink, new OtsLine2d(pointA, pointB),
                null, LaneKeepingPolicy.KEEPRIGHT);
        LaneGeometryUtil.createStraightLane(link, "Left", Length.instantiateSI(1.75), Length.instantiateSI(3.5), freewayLane,
                Map.of(vehicle, new Speed(180, SpeedUnit.KM_PER_HOUR)));
        LaneGeometryUtil.createStraightLane(link, "Right", Length.instantiateSI(-1.75), Length.instantiateSI(3.5), freewayLane,
                Map.of(vehicle, new Speed(180, SpeedUnit.KM_PER_HOUR)));
        LaneGeometryUtil.createStraightStripe(DefaultsRoadNl.SOLID, "1", link, Length.instantiateSI(3.5),
                Length.instantiateSI(0.2));
        LaneGeometryUtil.createStraightStripe(DefaultsRoadNl.DASHED, "2", link, Length.instantiateSI(0.0),
                Length.instantiateSI(0.2));
        LaneGeometryUtil.createStraightStripe(DefaultsRoadNl.SOLID, "3", link, Length.instantiateSI(-3.5),
                Length.instantiateSI(0.2));

        List<String> names = new ArrayList<>();
        names.add("Left lane");
        names.add("Right lane");
        List<Lane> start = new ArrayList<>();
        start.add(link.getLanes().get(0));
        start.add(link.getLanes().get(1));


        for (Lane lane : start)
        {
            new SinkDetector(lane, lane.getLength(), DefaultsNl.ROAD_USERS);
        }

//        new SinkDetector(start.get(0), start.get(0).getLength(), new DetectorType("NL.VEHICLES"));
//        new SinkDetector(start.get(1), start.get(1).getLength(), new DetectorType("NL.VEHICLES"));



        this.samplerExtended = RoadSampler.build(this.network)
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
                .registerExtendedDataType(new ExtendedDataFollowerDecelRight())
                .registerExtendedDataType(new ExtendedDataFollowerDecelLeft())
                .registerExtendedDataType(new ExtendedDataEgoDecelRight())
                .registerExtendedDataType(new ExtendedDataEgoDecelLeft())
                .registerExtendedDataType(new ExtendedDataCurrentCFAcceleration())
                .registerExtendedDataType(new ExtendedDataCurrentDesiredSpeed())
                .create();

        SamplerData<?> samplerData = this.samplerExtended.getSamplerData();

        List<Node> origins = new ArrayList<>();
        origins.add(nodeA);
        List<Node> destinations = new ArrayList<>();
        destinations.add(nodeB);

        Categorization categorization = new Categorization("MyCategorization", GtuType.class);

        DoubleVectorData data =
                DoubleVectorData.instantiate(new double[] {0.0, 0.5, 1.0}, TimeUnit.BASE_HOUR.getScale(), StorageType.DENSE);
        TimeVector timeVector = new TimeVector(data, TimeUnit.BASE_HOUR);
        Interpolation interpolation = Interpolation.STEPWISE;

        OdMatrix odMatrix = new OdMatrix("MyOD", origins, destinations, categorization, timeVector, interpolation);

        Category carCategory = new Category(categorization, DefaultsNl.CAR);

        data = DoubleVectorData.instantiate(new double[] {1000.0, 2000.0, 0.0}, FrequencyUnit.PER_HOUR.getScale(),
                StorageType.DENSE);
        FrequencyVector demandABCar = new FrequencyVector(data, FrequencyUnit.PER_HOUR);
        odMatrix.putDemandVector(nodeA, nodeB, carCategory, demandABCar);


        List<Node> nodes = new ArrayList<>();
        nodes.add(nodeA);
        nodes.add(nodeB);


        TtcRoomChecker roomChecker = new TtcRoomChecker(new Duration(10.0, DurationUnit.SI));
        IdSupplier idGenerator = new IdSupplier("");
        Set<LanePosition> initialLongitudinalPositions = new LinkedHashSet<>();
        initialLongitudinalPositions.add(new LanePosition(link.getLanes().get(0), new Length(5.0, LengthUnit.SI)));
        initialLongitudinalPositions.add(new LanePosition(link.getLanes().get(1), new Length(5.0, LengthUnit.SI)));



        ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> speedCar = DesiredSpeedLibrary.carsUnrestricted(this.stream);
               // new ContinuousDistDoubleScalar.Rel<>(new DistEmpiricalInterpolated(this.stream, vWishDistribution), SpeedUnit.KM_PER_HOUR);

        ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> speedTrucks = DesiredSpeedLibrary.trucks(this.stream);
               // new ContinuousDistDoubleScalar.Rel<>(new DistUniform(this.stream, 80.0, 100.0), SpeedUnit.KM_PER_HOUR);
        Supplier<Route> routeGeneratorCar = new FixedRouteGenerator(new Route("RouteAB", car, nodes));
        Supplier<Route> routeGeneratorTruck = new FixedRouteGenerator(new Route("RouteAB", truck, nodes));

        LaneBasedStrategicalPlannerFactory<?> strategicalPlannerFactoryCars = new LaneBasedStrategicalRoutePlannerFactory(
                new MirovaTacticalPlannerFactory(new IdmPlusFactory(this.stream), new DefaultMirovaPerceptionFactory()));
        LaneBasedStrategicalPlannerFactory<?> strategicalPlannerFactoryTrucks = new LaneBasedStrategicalRoutePlannerFactory(
                new MirovaTacticalPlannerFactory(new IdmPlusFactory(this.stream), new DefaultMirovaPerceptionFactory()));

        LaneBasedGtuTemplate carTemplate = new LaneBasedGtuTemplate(car, new ConstantSupplier<>(Length.instantiateSI(4.0)),
                new ConstantSupplier<>(Length.instantiateSI(2.0)), speedCar, strategicalPlannerFactoryCars,
                routeGeneratorCar);
        LaneBasedGtuTemplate truckTemplate = new LaneBasedGtuTemplate(truck, new ConstantSupplier<>(Length.instantiateSI(12.0)),
                new ConstantSupplier<>(Length.instantiateSI(3.5)), speedTrucks, strategicalPlannerFactoryTrucks,
                routeGeneratorTruck);


        ObjectDistribution<LaneBasedGtuTemplate> gtuTypeDistribution = new ObjectDistribution<>(this.stream);
        gtuTypeDistribution.add(new FrequencyAndObject<>(0.85, carTemplate));
        gtuTypeDistribution.add(new FrequencyAndObject<>(0.15, truckTemplate));

        LaneBasedGtuTemplateDistribution characteristicsGenerator = new LaneBasedGtuTemplateDistribution(gtuTypeDistribution);

        Supplier<Duration> headwayGenerator =
                new HeadwayGenerator(new Frequency(2500.0, FrequencyUnit.PER_HOUR), new MersenneTwister(4L));

        new LaneBasedGtuGenerator("Generator", headwayGenerator, characteristicsGenerator,
                GeneratorPositions.create(initialLongitudinalPositions, this.stream), this.network, getSimulator(),
                roomChecker, idGenerator);


//        LaneBasedGtuCharacteristicsGeneratorOd characteristicsGenerator = new LaneBasedGtuCharacteristicsGeneratorOd()
//        {
//            @Override
//            public LaneBasedGtuCharacteristics draw(final Node origin, final Node destination, final Category category, final StreamInterface randomStream)
//                    throws GtuException
//            {
//                GtuType gtuType = category.get(GtuType.class);
////                Route route = category.get(Route.class);
//                List<Node> nodes = new ArrayList<>();
//                nodes.add(origin);
//                nodes.add(destination);
//                Route route = null;
//                try
//                {
//                    route = new Route("RouteAB", gtuType, nodes);
//                }
//                catch (NetworkException exception)
//                {
//                    exception.printStackTrace();
//                }
//                GtuCharacteristics gtuCharacteristics = Defaults.NL.apply(gtuType, randomStream).get();
//
//                // change to MINMAX after debugging
//                VehicleModel vehicleModel = VehicleModel.MINMAX;
//
//                LaneBasedTacticalPlannerFactory<?> tactical = new MirovaTacticalPlannerFactory(new IdmPlusFactory(randomStream), new DefaultMirovaPerceptionFactory());
//                //LaneBasedTacticalPlannerFactory<?> tactical = new LmrsFactory(new IdmPlusFactory(randomStream), new DefaultLmrsPerceptionFactory());
//                LaneBasedStrategicalPlannerFactory<?> strategical = new LaneBasedStrategicalRoutePlannerFactory(tactical);
//
//                return new LaneBasedGtuCharacteristics(gtuCharacteristics, strategical, route, origin, destination, vehicleModel);
//
//            }
//        };

//        OdOptions odOptions = new OdOptions();
//        odOptions.set(OdOptions.GTU_TYPE, characteristicsGenerator);
//        odOptions.set(OdOptions.ERROR_HANDLER, GtuErrorHandler.DELETE);
//
//        DetectorType detectorType = new DetectorType("Detector AB");
//
//        OdApplier.applyOd(this.network, odMatrix, odOptions, new DetectorType("NL.VEHICLES"));


        GraphPath<LaneDataRoad> pathLeft = GraphLaneUtil.createPath(names.get(0), start.get(0));
        GraphPath<LaneDataRoad> pathRight = GraphLaneUtil.createPath(names.get(1), start.get(1));
        this.samplerExtended.scheduleStartRecording(Time.instantiateSI(1), pathLeft.get(0).getSource(0));
        this.samplerExtended.scheduleStopRecording(Time.instantiateSI(599), pathLeft.get(0).getSource(0));
        this.samplerExtended.scheduleStartRecording(Time.instantiateSI(1), pathRight.get(0).getSource(0));
        this.samplerExtended.scheduleStopRecording(Time.instantiateSI(599), pathRight.get(0).getSource(0));

        return this.network;
    }


    protected void createNetwork(final OtsSimulatorInterface sim) throws NetworkException
    {

    }

    @Override
    protected void onSimulationEnd()
    {

        System.out.println("Simulation ended, generating output...");

        SamplerData<?> samplerData = this.samplerExtended.getSamplerData();

        samplerData.writeToFile("D:\\Mitarbeitende\\gw2128\\projects\\mirova\\SimpleSimulation\\trajectory_data.csv");

    }



}