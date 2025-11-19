package org.opentrafficsim.demo.mirova.scenariomanagement.scenarios;

import java.util.*;
import java.util.function.Supplier;

import org.djunits.unit.*;
import org.djunits.value.vdouble.scalar.*;
import org.djutils.immutablecollections.ImmutableMap;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.FixedRouteGenerator;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.network.*;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.idgenerator.IdSupplier;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.distributions.ConstantSupplier;
import org.opentrafficsim.core.distributions.FrequencyAndObject;
import org.opentrafficsim.core.distributions.ObjectDistribution;
import org.opentrafficsim.road.network.lane.*;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;
import org.opentrafficsim.animation.GraphLaneUtil;
import org.opentrafficsim.base.geometry.OtsLine2d;

import org.opentrafficsim.road.definitions.DefaultsRoadNl;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions;
import org.opentrafficsim.road.gtu.generator.LaneBasedGtuGenerator;
import org.opentrafficsim.road.gtu.generator.TtcRoomChecker;
import org.opentrafficsim.road.gtu.generator.LaneBasedGtuGenerator.RoomChecker;
import org.opentrafficsim.road.gtu.generator.headway.HeadwayGenerator;

import org.opentrafficsim.road.gtu.generator.characteristics.*;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.*;
import org.opentrafficsim.road.gtu.lane.tactical.following.IdmPlusFactory;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.util.logging.extendeddata.*;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalRoutePlannerFactory;
import org.opentrafficsim.road.network.sampling.*;
import org.opentrafficsim.road.network.sampling.RoadSampler.Factory;
import org.opentrafficsim.kpi.sampling.SamplerData;
import org.opentrafficsim.core.parameters.ParameterFactoryByType;
import org.opentrafficsim.core.units.distributions.ContinuousDistDoubleScalar;

import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.distributions.DistEmpiricalInterpolated;
import nl.tudelft.simulation.jstats.distributions.empirical.InterpolatedEmpiricalDistribution;

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

        this.initialLongitudinalPositions.add(new LanePosition(link.getLanes().get(0), new Length(5.0, LengthUnit.SI)));
        this.initialLongitudinalPositions.add(new LanePosition(link.getLanes().get(1), new Length(5.0, LengthUnit.SI)));

        LaneGeometryUtil.createStraightLane(link, "L1", Length.instantiateSI(1.5), Length.instantiateSI(3.5),
                DefaultsRoadNl.FREEWAY,
                Map.of(DefaultsNl.VEHICLE, new Speed(180, SpeedUnit.KM_PER_HOUR)));

        LaneGeometryUtil.createStraightLane(link, "L2", Length.instantiateSI(-1.5), Length.instantiateSI(3.5),
                DefaultsRoadNl.FREEWAY,
                Map.of(DefaultsNl.VEHICLE, new Speed(180, SpeedUnit.KM_PER_HOUR)));

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
            final ScenarioParameters params,
            final ScenarioOutputConfiguration output) throws Exception {

        this.stream = new MersenneTwister(params.getSeed());

        buildNetwork(sim);

        // ---------------------------------------------------------
        // ROUTE
        // ---------------------------------------------------------
        buildRoutes();

        // ---------------------------------------------------------
        // GTU templates & speed distributions
        // ---------------------------------------------------------
        buildGtuTemplates(sim);

        // ---------------------------------------------------------
        // Demand (simple headway generator)
        // ---------------------------------------------------------
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
            GeneratorPositions.create(this.initialLongitudinalPositions, this.stream),
            this.network,
            sim,
            new TtcRoomChecker(new Duration(10.0, DurationUnit.SI)),
            new IdSupplier("")
        );



        return this.network;
    }

    // ------------------------------------------------------------
    // GTU templates
    // ------------------------------------------------------------
    @Override
    public void buildGtuTemplates(final OtsSimulatorInterface sim) throws Exception {

        LaneBasedStrategicalPlannerFactory<?> strategicalPlannerFactoryCars = new LaneBasedStrategicalRoutePlannerFactory(
                new MirovaTacticalPlannerFactory(new IdmPlusFactory(this.stream), new DefaultMirovaPerceptionFactory()));
        LaneBasedStrategicalPlannerFactory<?> strategicalPlannerFactoryTrucks = new LaneBasedStrategicalRoutePlannerFactory(
                new MirovaTacticalPlannerFactory(new IdmPlusFactory(this.stream), new DefaultMirovaPerceptionFactory()));

        Supplier<Route> routeGenerator = new FixedRouteGenerator( this.routes.get("A-B"));


        LaneBasedGtuTemplate car = new LaneBasedGtuTemplate(
                DefaultsNl.CAR,
                new ConstantSupplier<>(Length.instantiateSI(4.0)),
                new ConstantSupplier<>(Length.instantiateSI(2.0)),
                DesiredSpeedLibrary.germanMotorwayCars(new MersenneTwister(1)),
                strategicalPlannerFactoryCars,
                routeGenerator
               );

        this.gtuTemplates.put(DefaultsNl.CAR, car);

        LaneBasedGtuTemplate truck = new LaneBasedGtuTemplate(
                DefaultsNl.TRUCK,
                   new ConstantSupplier<>(Length.instantiateSI(12.0)),
                   new ConstantSupplier<>(Length.instantiateSI(2.5)),
                   DesiredSpeedLibrary.trucks(new MersenneTwister(2)),
                   strategicalPlannerFactoryTrucks,
                   routeGenerator
        );

        this.gtuTemplates.put(DefaultsNl.TRUCK, truck);
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
                .registerExtendedDataType(new ExtendedDataFollowerDecelRight())
                .registerExtendedDataType(new ExtendedDataFollowerDecelLeft())
                .registerExtendedDataType(new ExtendedDataEgoDecelRight())
                .registerExtendedDataType(new ExtendedDataEgoDecelLeft())
                .registerExtendedDataType(new ExtendedDataCurrentCFAcceleration())
                .registerExtendedDataType(new ExtendedDataCurrentDesiredSpeed())
                .create();

        // activates sampling on all lanes for the entire simulation duration
        for (Lane lane : this.listAllLanes) {
            GraphPath<LaneDataRoad> path = GraphLaneUtil.createPath("path", lane);
            sampler.scheduleStartRecording(Time.instantiateSI(0), path.get(0).getSource(0));

        }

        this.listRoadSamplers.add(sampler);
    }

    @Override
    public List<Node> getOrigins(final RoadNetwork network) {
        return List.of(network.getNode("A"));
    }

    @Override
    public List<Node> getDestinations(final RoadNetwork network) {
        return List.of(network.getNode("B"));
    }

    @Override
    public ScenarioOutputConfiguration configureOutput() {
        return new ScenarioOutputConfiguration().addRoadSamplers(this.listRoadSamplers);
    }
}


