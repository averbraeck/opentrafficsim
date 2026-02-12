package org.opentrafficsim.demo;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.DurationUnit;
import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.exceptions.Try;
import org.opentrafficsim.animation.GraphLaneUtil;
import org.opentrafficsim.animation.gtu.colorer.IncentiveGtuColorer;
import org.opentrafficsim.animation.gtu.colorer.SynchronizationGtuColorer;
import org.opentrafficsim.base.OtsRuntimeException;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.distributions.ConstantSupplier;
import org.opentrafficsim.core.distributions.FrequencyAndObject;
import org.opentrafficsim.core.distributions.ObjectDistribution;
import org.opentrafficsim.core.dsol.OtsAnimator;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.idgenerator.IdSupplier;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.ProbabilisticRouteGenerator;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.parameters.ParameterFactory;
import org.opentrafficsim.core.parameters.ParameterFactoryByType;
import org.opentrafficsim.core.units.distributions.ContinuousDistDoubleScalar;
import org.opentrafficsim.demo.ShortMerge.ShortMergeModel;
import org.opentrafficsim.draw.colorer.Colorer;
import org.opentrafficsim.draw.colorer.trajectory.SynchronizationTrajectoryColorer;
import org.opentrafficsim.draw.graphs.GraphPath;
import org.opentrafficsim.draw.graphs.PlotScheduler;
import org.opentrafficsim.draw.graphs.TrajectoryPlot;
import org.opentrafficsim.draw.gtu.DefaultCarAnimation.GtuData.GtuMarker;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataString;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions;
import org.opentrafficsim.road.gtu.generator.LaneBasedGtuGenerator;
import org.opentrafficsim.road.gtu.generator.LaneBasedGtuGenerator.RoomChecker;
import org.opentrafficsim.road.gtu.generator.TtcRoomChecker;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGtuTemplate;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGtuTemplateDistribution;
import org.opentrafficsim.road.gtu.generator.headway.HeadwayGenerator;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedTacticalPlannerFactory;
import org.opentrafficsim.road.gtu.lane.tactical.Synchronizable;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveCourtesy;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.Lmrs;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.LmrsFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.LmrsFactory.Setting;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Cooperation;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsParameters;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Synchronization;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Tailgating;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalRoutePlannerFactory;
import org.opentrafficsim.road.network.factory.xml.OtsXmlModel;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LanePosition;
import org.opentrafficsim.road.network.lane.object.SpeedSign;
import org.opentrafficsim.road.network.sampling.GtuDataRoad;
import org.opentrafficsim.road.network.sampling.LaneDataRoad;
import org.opentrafficsim.road.network.sampling.RoadSampler;
import org.opentrafficsim.swing.graphs.OtsPlotScheduler;
import org.opentrafficsim.swing.graphs.SwingTrajectoryPlot;
import org.opentrafficsim.swing.gui.AnimationToggles;
import org.opentrafficsim.swing.gui.OtsAnimationPanel;
import org.opentrafficsim.swing.gui.OtsSimulationApplication;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.jstats.distributions.DistNormal;
import nl.tudelft.simulation.jstats.distributions.DistUniform;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;
import nl.tudelft.simulation.language.DsolException;

/**
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class ShortMerge extends OtsSimulationApplication<ShortMergeModel>
{
    /** */
    private static final long serialVersionUID = 20170407L;

    /** Network. */
    static final String NETWORK = "shortMerge";

    /** Truck fraction. */
    static final double TRUCK_FRACTION = 0.15;

    /** Left traffic fraction. */
    static final double LEFT_FRACTION = 0.3;

    /** Main demand per lane. */
    static final Frequency MAIN_DEMAND = new Frequency(1000, FrequencyUnit.PER_HOUR);

    /** Ramp demand. */
    static final Frequency RAMP_DEMAND = new Frequency(500, FrequencyUnit.PER_HOUR);

    /** Synchronization. */
    static final Synchronization SYNCHRONIZATION = Synchronization.ALIGN_GAP;

    /** Cooperation. */
    static final Cooperation COOPERATION = Cooperation.PASSIVE_MOVING;

    /** Use additional incentives. */
    static final boolean ADDITIONAL_INCENTIVES = true;

    /** Simulation time. */
    public static final Time SIMTIME = Time.ofSI(3600);

    /**
     * Create a ShortMerge Swing application.
     * @param title the title of the Frame
     * @param panel the tabbed panel to display
     * @param model the model
     */
    public ShortMerge(final String title, final OtsAnimationPanel panel, final ShortMergeModel model)
    {
        super(model, panel, Map.of(DefaultsNl.TRUCK, GtuMarker.SQUARE));
    }

    @Override
    protected void setAnimationToggles()
    {
        AnimationToggles.setTextAnimationTogglesStandard(getAnimationPanel());
        getAnimationPanel().getAnimationPanel().toggleClass(Link.class);
        getAnimationPanel().getAnimationPanel().toggleClass(Node.class);
        getAnimationPanel().getAnimationPanel().showClass(SpeedSign.class);
    }

    @Override
    protected void addTabs()
    {
        GraphPath<LaneDataRoad> path;
        try
        {
            Lane start = ((CrossSectionLink) getModel().getNetwork().getLink("AB").get()).getLanes().get(1);
            path = GraphLaneUtil.createPath("Right lane", start);
        }
        catch (NetworkException exception)
        {
            throw new OtsRuntimeException("Could not create a path as a lane has no set speed limit.", exception);
        }
        ExtendedDataSync<GtuDataRoad> syncData = new ExtendedDataSync<GtuDataRoad>();
        RoadSampler sampler = new RoadSampler(Set.of(syncData), Collections.emptySet(), getModel().getNetwork());
        GraphPath.initRecording(sampler, path);
        PlotScheduler scheduler = new OtsPlotScheduler(getModel().getSimulator());
        Duration updateInterval = Duration.ofSI(10.0);
        SwingTrajectoryPlot plot = new SwingTrajectoryPlot(
                new TrajectoryPlot("Trajectory right lane", updateInterval, scheduler, sampler.getSamplerData(), path), true);
        plot.addColorer(new SynchronizationTrajectoryColorer(syncData), false);
        getAnimationPanel().getTabbedPane().addTab(getAnimationPanel().getTabbedPane().getTabCount(), "trajectories",
                plot.getContentPane());
    }

    /**
     * Main program.
     * @param args the command line arguments (not used)
     */
    public static void main(final String[] args)
    {
        demo(true);
    }

    /**
     * Start the demo.
     * @param exitOnClose when running stand-alone: true; when running as part of a demo: false
     */
    public static void demo(final boolean exitOnClose)
    {
        try
        {
            OtsAnimator simulator = new OtsAnimator("ShortMerge");
            final ShortMergeModel otsModel = new ShortMergeModel(simulator);
            List<Colorer<? super Gtu>> colorers = new ArrayList<>(DEFAULT_GTU_COLORERS);
            colorers.add(new SynchronizationGtuColorer());
            colorers.add(new IncentiveGtuColorer(IncentiveCourtesy.class, "Courtesy incentive"));
            OtsAnimationPanel animationPanel = new OtsAnimationPanel(otsModel.getNetwork().getExtent(), simulator, otsModel,
                    colorers, otsModel.getNetwork());
            ShortMerge app = new ShortMerge("ShortMerge", animationPanel, otsModel);
            app.setExitOnClose(exitOnClose);
            animationPanel.enableSimulationControlButtons();
        }
        catch (SimRuntimeException | RemoteException | IndexOutOfBoundsException | DsolException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * <p>
     * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    public static class ShortMergeModel extends OtsXmlModel
    {
        /**
         * Constructor.
         * @param simulator the simulator
         */
        public ShortMergeModel(final OtsSimulatorInterface simulator)
        {
            super(simulator, "/resources/lmrs/" + NETWORK + ".xml");
        }

        @Override
        public void constructModel() throws SimRuntimeException
        {
            super.constructModel();
            Try.execute(() -> addGenerator(), OtsRuntimeException.class, "Unable to add generator to network.");
        }

        /**
         * Create generators.
         * @throws ParameterException on parameter exception
         * @throws GtuException on GTU exception
         * @throws NetworkException if not does not exist
         * @throws SimRuntimeException in case of sim run time exception
         */
        private void addGenerator() throws ParameterException, GtuException, NetworkException, SimRuntimeException
        {

            Random seedGenerator = new Random(1L);
            Map<String, StreamInterface> streams = new LinkedHashMap<>();
            StreamInterface stream = new MersenneTwister(Math.abs(seedGenerator.nextLong()) + 1);
            streams.put("headwayGeneration", stream);
            streams.put("gtuClass", new MersenneTwister(Math.abs(seedGenerator.nextLong()) + 1));
            getStreamInformation().addStream("headwayGeneration", stream);
            getStreamInformation().addStream("gtuClass", streams.get("gtuClass"));

            TtcRoomChecker roomChecker = new TtcRoomChecker(new Duration(10.0, DurationUnit.SI));
            IdSupplier idGenerator = new IdSupplier("");

            LmrsFactory<Lmrs> tacticalFactory = new LmrsFactory<Lmrs>(Lmrs::new).setStream(stream)
                    .set(Setting.SYNCHRONIZATION, SYNCHRONIZATION).set(Setting.COOPERATION, COOPERATION);
            if (ADDITIONAL_INCENTIVES)
            {
                tacticalFactory.set(Setting.INCENTIVE_COURTESY, true);
            }

            GtuType car = DefaultsNl.CAR;
            GtuType truck = DefaultsNl.TRUCK;
            Route routeAE =
                    getNetwork().getShortestRouteBetween(car, getNetwork().getNode("A").get(), getNetwork().getNode("E").get());
            Route routeAG = !NETWORK.equals("shortWeave") ? null : getNetwork().getShortestRouteBetween(car,
                    getNetwork().getNode("A").get(), getNetwork().getNode("G").get());
            Route routeFE =
                    getNetwork().getShortestRouteBetween(car, getNetwork().getNode("F").get(), getNetwork().getNode("E").get());
            Route routeFG = !NETWORK.equals("shortWeave") ? null : getNetwork().getShortestRouteBetween(car,
                    getNetwork().getNode("F").get(), getNetwork().getNode("G").get());

            double leftFraction = NETWORK.equals("shortWeave") ? LEFT_FRACTION : 0.0;
            List<FrequencyAndObject<Route>> routesA = new ArrayList<>();
            routesA.add(new FrequencyAndObject<>(1.0 - leftFraction, routeAE));
            routesA.add(new FrequencyAndObject<>(leftFraction, routeAG));
            List<FrequencyAndObject<Route>> routesF = new ArrayList<>();
            routesF.add(new FrequencyAndObject<>(1.0 - leftFraction, routeFE));
            routesF.add(new FrequencyAndObject<>(leftFraction, routeFG));
            Supplier<Route> routeGeneratorA = new ProbabilisticRouteGenerator(routesA, stream);
            Supplier<Route> routeGeneratorF = new ProbabilisticRouteGenerator(routesF, stream);

            Speed speedA = new Speed(120.0, SpeedUnit.KM_PER_HOUR);
            Speed speedF = new Speed(20.0, SpeedUnit.KM_PER_HOUR);

            CrossSectionLink linkA = (CrossSectionLink) getNetwork().getLink("AB").get();
            CrossSectionLink linkF = (CrossSectionLink) getNetwork().getLink("FF2").get();

            ParameterFactoryByType bcFactory = new ParameterFactoryByType();
            bcFactory.addParameter(car, ParameterTypes.FSPEED, new DistNormal(stream, 123.7 / 120, 12.0 / 120));
            bcFactory.addParameter(car, LmrsParameters.SOCIO, new DistNormal(stream, 0.5, 0.1));
            bcFactory.addParameter(truck, ParameterTypes.A, new Acceleration(0.8, AccelerationUnit.SI));
            bcFactory.addParameter(truck, LmrsParameters.SOCIO, new DistNormal(stream, 0.5, 0.1));
            bcFactory.addParameter(Tailgating.RHO, Tailgating.RHO.getDefaultValue());

            Supplier<Duration> headwaysA1 = new HeadwayGenerator(MAIN_DEMAND, stream);
            Supplier<Duration> headwaysA2 = new HeadwayGenerator(MAIN_DEMAND, stream);
            Supplier<Duration> headwaysA3 = new HeadwayGenerator(MAIN_DEMAND, stream);
            Supplier<Duration> headwaysF = new HeadwayGenerator(RAMP_DEMAND, stream);

            // speed generators
            ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> speedCar =
                    new ContinuousDistDoubleScalar.Rel<>(new DistUniform(stream, 160, 200), SpeedUnit.KM_PER_HOUR);
            ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> speedTruck =
                    new ContinuousDistDoubleScalar.Rel<>(new DistUniform(stream, 80, 95), SpeedUnit.KM_PER_HOUR);
            // strategical planner factory
            LaneBasedStrategicalRoutePlannerFactory strategicalFactory =
                    new LaneBasedStrategicalRoutePlannerFactory(tacticalFactory, bcFactory);
            // vehicle templates, with routes
            LaneBasedGtuTemplate carA = new LaneBasedGtuTemplate(car, new ConstantSupplier<>(Length.ofSI(4.0)),
                    new ConstantSupplier<>(Length.ofSI(2.0)), speedCar, strategicalFactory, routeGeneratorA);
            LaneBasedGtuTemplate carF = new LaneBasedGtuTemplate(car, new ConstantSupplier<>(Length.ofSI(4.0)),
                    new ConstantSupplier<>(Length.ofSI(2.0)), speedCar, strategicalFactory, routeGeneratorF);
            LaneBasedGtuTemplate truckA = new LaneBasedGtuTemplate(truck, new ConstantSupplier<>(Length.ofSI(15.0)),
                    new ConstantSupplier<>(Length.ofSI(2.5)), speedTruck, strategicalFactory, routeGeneratorA);
            LaneBasedGtuTemplate truckF = new LaneBasedGtuTemplate(truck, new ConstantSupplier<>(Length.ofSI(15.0)),
                    new ConstantSupplier<>(Length.ofSI(2.5)), speedTruck, strategicalFactory, routeGeneratorF);
            //
            ObjectDistribution<LaneBasedGtuTemplate> gtuTypeAllCarA = new ObjectDistribution<>(streams.get("gtuClass"));
            gtuTypeAllCarA.add(new FrequencyAndObject<>(1.0, carA));

            ObjectDistribution<LaneBasedGtuTemplate> gtuType1LaneF = new ObjectDistribution<>(streams.get("gtuClass"));
            gtuType1LaneF.add(new FrequencyAndObject<>(1.0 - 2 * TRUCK_FRACTION, carF));
            gtuType1LaneF.add(new FrequencyAndObject<>(2 * TRUCK_FRACTION, truckF));

            ObjectDistribution<LaneBasedGtuTemplate> gtuType2ndLaneA = new ObjectDistribution<>(streams.get("gtuClass"));
            gtuType2ndLaneA.add(new FrequencyAndObject<>(1.0 - 2 * TRUCK_FRACTION, carA));
            gtuType2ndLaneA.add(new FrequencyAndObject<>(2 * TRUCK_FRACTION, truckA));

            ObjectDistribution<LaneBasedGtuTemplate> gtuType3rdLaneA = new ObjectDistribution<>(streams.get("gtuClass"));
            gtuType3rdLaneA.add(new FrequencyAndObject<>(1.0 - 3 * TRUCK_FRACTION, carA));
            gtuType3rdLaneA.add(new FrequencyAndObject<>(3 * TRUCK_FRACTION, truckA));

            makeGenerator(getLane(linkA, "FORWARD1"), speedA, "gen1", idGenerator, gtuTypeAllCarA, headwaysA1, roomChecker,
                    bcFactory, tacticalFactory, SIMTIME, streams.get("gtuClass"));
            if (NETWORK.equals("shortWeave"))
            {
                makeGenerator(getLane(linkA, "FORWARD2"), speedA, "gen2", idGenerator, gtuTypeAllCarA, headwaysA2, roomChecker,
                        bcFactory, tacticalFactory, SIMTIME, streams.get("gtuClass"));
                makeGenerator(getLane(linkA, "FORWARD3"), speedA, "gen3", idGenerator, gtuType3rdLaneA, headwaysA3, roomChecker,
                        bcFactory, tacticalFactory, SIMTIME, streams.get("gtuClass"));
            }
            else
            {
                makeGenerator(getLane(linkA, "FORWARD2"), speedA, "gen2", idGenerator, gtuType2ndLaneA, headwaysA2, roomChecker,
                        bcFactory, tacticalFactory, SIMTIME, streams.get("gtuClass"));
            }
            makeGenerator(getLane(linkF, "FORWARD1"), speedF, "gen4", idGenerator, gtuType1LaneF, headwaysF, roomChecker,
                    bcFactory, tacticalFactory, SIMTIME, streams.get("gtuClass"));

            new SpeedSign("sign1", getLane(linkA, "FORWARD1"), Length.ofSI(10), new Speed(130.0, SpeedUnit.KM_PER_HOUR),
                    DefaultsNl.VEHICLE, Duration.ZERO, new Duration(24, DurationUnit.HOUR));

        }

        /**
         * Get lane from link by id.
         * @param link link
         * @param id id
         * @return lane
         */
        private Lane getLane(final CrossSectionLink link, final String id)
        {
            return (Lane) link.getCrossSectionElement(id).orElseThrow();
        }

        /**
         * @param lane the reference lane for this generator
         * @param generationSpeed the speed of the GTU
         * @param id the id of the supplier itself
         * @param idSupplier the supplier for the ID
         * @param distribution the type generator for the GTU
         * @param headwaySupplier the headway generator for the GTU
         * @param roomChecker the checker to see if there is room for the GTU
         * @param bcFactory the factory to generate parameters for the GTU
         * @param tacticalFactory the generator for the tactical planner
         * @param simulationTime simulation time
         * @param stream random numbers stream
         * @throws SimRuntimeException in case of scheduling problems
         * @throws GtuException in case the GTU is inconsistent
         * @throws ParameterException in case a parameter for the perception is missing
         * @throws NetworkException if the object could not be added to the network
         */
        private void makeGenerator(final Lane lane, final Speed generationSpeed, final String id, final IdSupplier idSupplier,
                final ObjectDistribution<LaneBasedGtuTemplate> distribution, final Supplier<Duration> headwaySupplier,
                final RoomChecker roomChecker, final ParameterFactory bcFactory,
                final LaneBasedTacticalPlannerFactory<?> tacticalFactory, final Time simulationTime,
                final StreamInterface stream) throws SimRuntimeException, GtuException, ParameterException, NetworkException
        {

            Set<LanePosition> initialLongitudinalPositions = new LinkedHashSet<>();
            initialLongitudinalPositions.add(new LanePosition(lane, new Length(5.0, LengthUnit.SI)));
            LaneBasedGtuTemplateDistribution characteristicsGenerator = new LaneBasedGtuTemplateDistribution(distribution);
            LaneBasedGtuGenerator generator = new LaneBasedGtuGenerator(id, headwaySupplier, characteristicsGenerator,
                    GeneratorPositions.create(initialLongitudinalPositions, stream), getNetwork(), getSimulator(), roomChecker,
                    idSupplier);
            generator.setNoLaneChangeDistance(Length.ofSI(100.0));
        }

    }

    /**
     * Extended data of synchronization phase.
     * @param <G> GTU data type
     */
    public static class ExtendedDataSync<G extends GtuDataRoad> extends ExtendedDataString<G>
    {

        /**
         * Constructor.
         */
        public ExtendedDataSync()
        {
            super("sync", "Synchronization status");
        }

        @Override
        public Optional<String> getValue(final GtuDataRoad gtu)
        {
            if (gtu.getGtu().getTacticalPlanner() instanceof Synchronizable sync)
            {
                return Optional.ofNullable(sync.getSynchronizationState().toString());
            }
            return Optional.of("N/A");
        }

    }

}
