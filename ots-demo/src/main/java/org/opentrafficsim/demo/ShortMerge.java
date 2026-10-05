package org.opentrafficsim.demo;

import java.awt.BasicStroke;
import java.awt.Color;
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
import org.djutils.draw.point.Point2d;
import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.event.reference.ReferenceType;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.chart.ui.TextAnchor;
import org.opentrafficsim.animation.colorer.Colorer;
import org.opentrafficsim.animation.colorer.trajectory.SynchronizationTrajectoryColorer;
import org.opentrafficsim.animation.data.gtu.IncentiveGtuColorer;
import org.opentrafficsim.animation.data.gtu.SynchronizationGtuColorer;
import org.opentrafficsim.animation.data.util.GraphLaneUtil;
import org.opentrafficsim.animation.graphs.BarPlot;
import org.opentrafficsim.animation.graphs.BarPlot.BarDataSource;
import org.opentrafficsim.animation.graphs.BarPlot.BarPlotData;
import org.opentrafficsim.animation.graphs.GraphPath;
import org.opentrafficsim.animation.graphs.PlotScheduler;
import org.opentrafficsim.animation.graphs.TrajectoryPlot;
import org.opentrafficsim.base.OtsRuntimeException;
import org.opentrafficsim.base.logger.Logger;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.distributions.ConstantSupplier;
import org.opentrafficsim.core.distributions.FrequencyAndObject;
import org.opentrafficsim.core.distributions.ObjectDistribution;
import org.opentrafficsim.core.dsol.OtsAnimator;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.idgenerator.IdSupplier;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.route.ProbabilisticRouteGenerator;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.parameters.ParameterFactory;
import org.opentrafficsim.core.parameters.ParameterFactoryByType;
import org.opentrafficsim.core.units.distributions.ContinuousDistDoubleScalar;
import org.opentrafficsim.demo.ShortMerge.ShortMergeModel;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataString;
import org.opentrafficsim.road.gtu.LaneBasedGtu;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions;
import org.opentrafficsim.road.gtu.generator.LaneBasedGtuGenerator;
import org.opentrafficsim.road.gtu.generator.LaneBasedGtuGenerator.RoomChecker;
import org.opentrafficsim.road.gtu.generator.TtcRoomChecker;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGtuTemplate;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGtuTemplateDistribution;
import org.opentrafficsim.road.gtu.generator.headway.HeadwayGenerator;
import org.opentrafficsim.road.gtu.perception.RelativeLane;
import org.opentrafficsim.road.gtu.perception.structure.NavigatingIterable.Entry;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalRoutePlannerFactory;
import org.opentrafficsim.road.gtu.tactical.LaneBasedTacticalPlannerFactory;
import org.opentrafficsim.road.gtu.tactical.Synchronizable;
import org.opentrafficsim.road.gtu.tactical.lmrs.IncentiveCourtesy;
import org.opentrafficsim.road.gtu.tactical.lmrs.Lmrs;
import org.opentrafficsim.road.gtu.tactical.lmrs.LmrsFactory;
import org.opentrafficsim.road.gtu.tactical.lmrs.LmrsFactory.Setting;
import org.opentrafficsim.road.gtu.tactical.util.lmrs.Cooperation;
import org.opentrafficsim.road.gtu.tactical.util.lmrs.LmrsParameters;
import org.opentrafficsim.road.gtu.tactical.util.lmrs.Synchronization;
import org.opentrafficsim.road.gtu.tactical.util.lmrs.Tailgating;
import org.opentrafficsim.road.network.CrossSectionLink;
import org.opentrafficsim.road.network.Lane;
import org.opentrafficsim.road.network.LanePosition;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.factory.xml.OtsXmlModel;
import org.opentrafficsim.road.network.sampling.GtuDataRoad;
import org.opentrafficsim.road.network.sampling.LaneDataRoad;
import org.opentrafficsim.road.network.sampling.RoadSampler;
import org.opentrafficsim.swing.graphs.OtsPlotScheduler;
import org.opentrafficsim.swing.graphs.SwingPlot;
import org.opentrafficsim.swing.graphs.SwingTrajectoryPlot;
import org.opentrafficsim.swing.gui.AnimationToggles;
import org.opentrafficsim.swing.gui.OtsSimulationApplication;
import org.opentrafficsim.swing.gui.OtsSimulationPanel;
import org.opentrafficsim.swing.gui.OtsSimulationPanelDecorator;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.swing.gui.TablePanel;
import nl.tudelft.simulation.jstats.distributions.DistNormal;
import nl.tudelft.simulation.jstats.distributions.DistUniform;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;
import nl.tudelft.simulation.language.DsolException;

/**
 * Demo of a short on-ramp where merging vehicles only have ~225m to accelerate from ~20km/h to merge with an acceleration lane
 * of 65m. This showcases the LMRS synchronization model.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Alexander Verbraeck
 * @author Peter Knoppers
 * @author Wouter Schakel
 */
public class ShortMerge extends OtsSimulationApplication<ShortMergeModel>
{
    /** Serialization version UID. */
    private static final long serialVersionUID = 20170407L;

    /** Network. */
    static final String NETWORK = "shortMerge";

    /** Truck fraction. */
    static final double TRUCK_FRACTION = 0.15;

    /** Left traffic fraction. */
    static final double LEFT_FRACTION = 0.3;

    /** Main demand per lane. */
    static final Frequency MAIN_DEMAND = new Frequency(1500, FrequencyUnit.PER_HOUR);

    /** Ramp demand. */
    static final Frequency RAMP_DEMAND = new Frequency(500, FrequencyUnit.PER_HOUR);

    /** Synchronization. */
    static final Synchronization SYNCHRONIZATION = Synchronization.ALIGN_GAP_MOVING;

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
    public ShortMerge(final String title, final OtsSimulationPanel panel, final ShortMergeModel model)
    {
        super(model, panel);
    }

    /**
     * Adds tabs. The default does nothing.
     * @param animationPanel animation panel
     * @param network network
     */
    private static void addTabs(final OtsSimulationPanel animationPanel, final Network network)
    {
        // trajectories
        GraphPath<LaneDataRoad> path;
        try
        {
            Lane start = ((CrossSectionLink) network.getLink("AB").get()).getLanes().get(1);
            path = GraphLaneUtil.createPath("Right lane", start);
        }
        catch (NetworkException exception)
        {
            throw new OtsRuntimeException("Could not create a path as a lane has no set speed limit.", exception);
        }
        ExtendedDataSync<GtuDataRoad> syncData = new ExtendedDataSync<GtuDataRoad>();
        RoadSampler sampler = new RoadSampler(Set.of(syncData), Collections.emptySet(), (RoadNetwork) network);
        GraphPath.initRecording(sampler, path);
        PlotScheduler scheduler = new OtsPlotScheduler(network.getSimulator());
        Duration updateInterval = Duration.ofSI(10.0);
        SwingTrajectoryPlot plot = new SwingTrajectoryPlot(
                new TrajectoryPlot("Trajectory right lane", updateInterval, scheduler, sampler.getSamplerData(), path), true);
        plot.addColorer(new SynchronizationTrajectoryColorer(syncData), false);
        animationPanel.getTabbedPane().addTab(animationPanel.getTabbedPane().getTabCount(), "trajectories",
                plot.getContentPane());

        // lane change statistics
        LcGapDataSource source = new LcGapDataSource(network);
        BarPlot lcGapPlot = new BarPlot(scheduler, "Gap distribution", updateInterval, Duration.ZERO,
                new BarPlotData("Gap [s]", LcGapDataSource.GAP, source));
        BarPlot lcTtcPlot = new BarPlot(scheduler, "TTC distribution", updateInterval, Duration.ZERO,
                new BarPlotData("TTC [s]", LcGapDataSource.TTC, source));
        BarPlot lcAccPlot = new BarPlot(scheduler, "Acceleration distribution", updateInterval, Duration.ZERO,
                new BarPlotData("dv/dt [m/s\u00B2]", LcGapDataSource.ACCELERATION, source));
        BarPlot lcLocPlot = new BarPlot(scheduler, "Merge location", updateInterval, Duration.ZERO,
                new BarPlotData("location [m]", LcGapDataSource.MERGE_LOCATION, source));

        ValueMarker marker = new ValueMarker(network.getLink("BC").get().getLength().si);
        marker.setPaint(Color.BLACK);
        marker.setStroke(new BasicStroke(1.5f));
        marker.setLabel(" End of ramp ");
        marker.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
        marker.setLabelTextAnchor(TextAnchor.TOP_LEFT);
        lcLocPlot.getChart().getXYPlot().addDomainMarker(marker);

        SwingPlot lcGapSwingPlot = new SwingPlot(lcGapPlot);
        SwingPlot lcTtcSwingPlot = new SwingPlot(lcTtcPlot);
        SwingPlot lcAccSwingPlot = new SwingPlot(lcAccPlot);
        SwingPlot lcLocSwingPlot = new SwingPlot(lcLocPlot);
        TablePanel charts = new TablePanel(2, 2);
        charts.setCell(lcGapSwingPlot.getContentPane(), 0, 0);
        charts.setCell(lcTtcSwingPlot.getContentPane(), 0, 1);
        charts.setCell(lcAccSwingPlot.getContentPane(), 1, 0);
        charts.setCell(lcLocSwingPlot.getContentPane(), 1, 1);
        animationPanel.getTabbedPane().addTab(animationPanel.getTabbedPane().getTabCount(), "lane change stats", charts);
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
            OtsSimulationPanel simulationPanel = new OtsSimulationPanel(otsModel.getNetwork(), new OtsSimulationPanelDecorator()
            {
                @Override
                public void setAnimationToggles(final OtsSimulationPanel simulationPanel)
                {
                    AnimationToggles.setIconAnimationTogglesStandard(simulationPanel);
                }

                @Override
                public void addTabs(final OtsSimulationPanel simulationPanel, final Network network)
                {
                    ShortMerge.addTabs(simulationPanel, network);
                }

                @Override
                public List<Colorer<? super Gtu>> getGtuColorers()
                {
                    List<Colorer<? super Gtu>> colorers = new ArrayList<>(DEFAULT_GTU_COLORERS);
                    colorers.add(new SynchronizationGtuColorer());
                    colorers.add(new IncentiveGtuColorer(IncentiveCourtesy.class, "Courtesy incentive"));
                    return colorers;
                }
            });
            ShortMerge app = new ShortMerge("ShortMerge", simulationPanel, otsModel);
            app.setExitOnClose(exitOnClose);
            simulationPanel.enableSimulationControlButtons();
        }
        catch (SimRuntimeException | RemoteException | IndexOutOfBoundsException | DsolException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * Short merge model.
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
            try
            {
                addGenerator();
            }
            catch (ParameterException | NetworkException | SimRuntimeException exception)
            {
                throw new OtsRuntimeException("Unable to create generator.");
            }
        }

        /**
         * Create generators.
         * @throws ParameterException on parameter exception
         * @throws NetworkException if not does not exist
         * @throws SimRuntimeException in case of sim run time exception
         */
        private void addGenerator() throws ParameterException, NetworkException, SimRuntimeException
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
            bcFactory.addParameter(truck, ParameterTypes.FSPEED_GTU, new DistNormal(stream, 85.0 / 80.0, 2.5 / 80.0));
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
            ConstantSupplier<Speed> speedTruck = new ConstantSupplier<>(new Speed(95.0, SpeedUnit.KM_PER_HOUR));
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
         * @throws NetworkException if the object could not be added to the network
         */
        private void makeGenerator(final Lane lane, final Speed generationSpeed, final String id, final IdSupplier idSupplier,
                final ObjectDistribution<LaneBasedGtuTemplate> distribution, final Supplier<Duration> headwaySupplier,
                final RoomChecker roomChecker, final ParameterFactory bcFactory,
                final LaneBasedTacticalPlannerFactory<?> tacticalFactory, final Time simulationTime,
                final StreamInterface stream) throws SimRuntimeException, NetworkException
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

    /**
     * Bar data source that collects lane change information.
     */
    private static class LcGapDataSource implements BarDataSource, EventListener
    {

        /** Gap data key. */
        public static final String GAP = "Gap";

        /** Time to collision data key. */
        public static final String TTC = "TTC";

        /** Acceleration data key. */
        public static final String ACCELERATION = "ACCELERATION";

        /** Merge location data key. */
        public static final String MERGE_LOCATION = "MERGE_LOCATION";

        /** Maximum gap. */
        private static final double GAP_MAX = 3.0;

        /** Gap step size. */
        private static final double GAP_DX = 0.1;

        /** Maximum TTC. */
        private static final double TTC_MAX = 10.0;

        /** TTC step size. */
        private static final double TTC_DX = 0.5;

        /** Minimum acceleration. */
        private static final double ACC_MIN = -5.0;

        /** Maximum acceleration. */
        private static final double ACC_MAX = 1.0;

        /** Acceleration step size. */
        private static final double ACC_DX = 0.2;

        /** Merge location. */
        private static final double LOC_MAX_EXTRA = 100.0;

        /** Merge location step size. */
        private static final double LOC_DX = 5.0;

        /** Network. */
        private final Network network;

        /** Data map. */
        private final Map<String, float[][]> data = new LinkedHashMap<>();

        /**
         * Constructor.
         * @param network network
         */
        LcGapDataSource(final Network network)
        {
            this.data.put(GAP, new float[2][(int) (GAP_MAX / GAP_DX)]);
            this.data.put(TTC, new float[2][(int) (TTC_MAX / TTC_DX)]);
            this.data.put(ACCELERATION, new float[2][(int) ((ACC_MAX - ACC_MIN) / ACC_DX)]);
            this.data.put(MERGE_LOCATION,
                    new float[1][(int) ((network.getLink("BC").get().getLength().si + LOC_MAX_EXTRA) / LOC_DX)]);
            this.network = network;
            this.network.addListener(this, Network.GTU_ADD_EVENT, ReferenceType.WEAK);
            this.network.addListener(this, Network.GTU_REMOVE_EVENT, ReferenceType.WEAK);
        }

        @Override
        public double getMinX(final Object dataKey)
        {
            return switch ((String) dataKey)
            {
                case GAP, TTC, MERGE_LOCATION -> 0.0;
                case ACCELERATION -> ACC_MIN;
                default -> throw new RuntimeException();
            };
        }

        @Override
        public double getDx(final Object dataKey)
        {
            return switch ((String) dataKey)
            {
                case GAP -> GAP_DX;
                case TTC -> TTC_DX;
                case ACCELERATION -> ACC_DX;
                case MERGE_LOCATION -> LOC_DX;
                default -> throw new RuntimeException();
            };
        }

        @Override
        public float[][] getData(final Object dataKey)
        {
            return this.data.get(dataKey);
        }

        @Override
        public String[] seriesLabels(final Object dataKey)
        {
            return switch ((String) dataKey)
            {
                case GAP, TTC, ACCELERATION -> new String[] {"lag gap", "lead gap"};
                case MERGE_LOCATION -> new String[] {""};
                default -> throw new RuntimeException();
            };
        }

        @Override
        public void notify(final Event event)
        {
            if (event.getType().equals(LaneBasedGtu.LANE_CHANGE_EVENT))
            {
                Object[] content = (Object[]) event.getContent();
                Optional<Gtu> gtu = this.network.getGTU((String) content[0]);
                if (gtu.isPresent() && gtu.get() instanceof LaneBasedGtu lGtu)
                {
                    RelativeLane lane = new RelativeLane(LateralDirectionality.valueOf((String) content[1]), 1);

                    try
                    {
                        Iterable<Entry<LaneBasedGtu>> leaders = lGtu.getTacticalPlanner().getPerception().getLaneStructure()
                                .getFirstDownstreamGtus(lane, RelativePosition.FRONT, RelativePosition.REAR,
                                        RelativePosition.FRONT, RelativePosition.REAR);
                        double minLeadGap = Double.MAX_VALUE;
                        double minLeadTtc = Double.MAX_VALUE;
                        for (Entry<LaneBasedGtu> entry : leaders)
                        {
                            double v = gtu.get().getSpeed().si;
                            double dv = v - entry.object().getSpeed().si;
                            double gap = entry.distance().si / v;
                            double ttc = entry.distance().si / dv;
                            minLeadGap = gap < minLeadGap ? gap : minLeadGap;
                            minLeadTtc = 0.0 < ttc && ttc < minLeadTtc ? ttc : minLeadTtc;
                        }
                        addValue(minLeadGap, LcGapDataSource.GAP, 1);
                        addValue(minLeadTtc, LcGapDataSource.TTC, 1);

                        LaneBasedGtu nearestFollower = null;
                        Iterable<Entry<LaneBasedGtu>> followers = lGtu.getTacticalPlanner().getPerception().getLaneStructure()
                                .getFirstUpstreamGtus(lane, RelativePosition.REAR, RelativePosition.FRONT,
                                        RelativePosition.REAR, RelativePosition.FRONT);
                        double minLagGap = Double.MAX_VALUE;
                        double minLagTtc = Double.MAX_VALUE;
                        for (Entry<LaneBasedGtu> entry : followers)
                        {
                            double v = entry.object().getSpeed().si;
                            double dv = v - gtu.get().getSpeed().si;
                            double gap = entry.distance().si / v;
                            double ttc = entry.distance().si / dv;
                            if (gap < minLagGap)
                            {
                                nearestFollower = entry.object();
                                minLagGap = gap;
                            }
                            minLagTtc = 0.0 < ttc && ttc < minLagTtc ? ttc : minLagTtc;
                        }
                        addValue(minLagGap, LcGapDataSource.GAP, 0);
                        addValue(minLagTtc, LcGapDataSource.TTC, 0);

                        addValue(gtu.get().getAcceleration().si, LcGapDataSource.ACCELERATION, 1);
                        // get follower acceleration from its first move after the lane change
                        if (nearestFollower != null)
                        {
                            nearestFollower.addListener(this, LaneBasedGtu.LANEBASED_MOVE_EVENT, ReferenceType.WEAK);
                        }

                        Point2d point = lGtu.getLocation();
                        if (point.y < 0.0)
                        {
                            addValue(point.x - this.network.getNode("B").get().getLocation().x, LcGapDataSource.MERGE_LOCATION,
                                    0);
                        }
                    }
                    catch (ParameterException ex)
                    {
                        Logger.ots().error("Unable to obtain lane change gap for plot.");
                    }
                }
            }
            else if (event.getType().equals(LaneBasedGtu.LANEBASED_MOVE_EVENT))
            {
                Optional<Gtu> gtu = this.network.getGTU((String) ((Object[]) event.getContent())[0]);
                if (gtu.isPresent() && gtu.get() instanceof LaneBasedGtu lGtu)
                {
                    addValue(gtu.get().getAcceleration().si, LcGapDataSource.ACCELERATION, 0);
                    lGtu.removeListener(this, LaneBasedGtu.LANEBASED_MOVE_EVENT);
                }
            }
            else if (event.getType().equals(Network.GTU_ADD_EVENT))
            {
                Optional<Gtu> gtu = this.network.getGTU((String) event.getContent());
                if (gtu.isPresent() && gtu.get() instanceof LaneBasedGtu lGtu)
                {
                    lGtu.addListener(this, LaneBasedGtu.LANE_CHANGE_EVENT);
                }
            }
            else if (event.getType().equals(Network.GTU_REMOVE_EVENT))
            {
                Optional<Gtu> gtu = this.network.getGTU((String) event.getContent());
                if (gtu.isPresent())
                {
                    gtu.get().removeListener(this, LaneBasedGtu.LANE_CHANGE_EVENT);
                }
            }
        }

        /**
         * Add value to data.
         * @param value value
         * @param key data key
         * @param series series within data
         */
        private void addValue(final double value, final String key, final int series)
        {
            double dx = getDx(key);
            int item = (int) Math.floor(value / dx);
            if (ACCELERATION.equals(key))
            {
                item -= (int) Math.floor(ACC_MIN / dx);
            }
            if (item >= 0 && item < this.data.get(key)[series].length)
            {
                this.data.get(key)[series][item]++;
            }
        }

    }

}
