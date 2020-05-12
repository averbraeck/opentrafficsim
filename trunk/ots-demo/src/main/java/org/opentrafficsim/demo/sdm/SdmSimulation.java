package org.opentrafficsim.demo.sdm;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.ValueRuntimeException;
import org.djunits.value.storage.StorageType;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djunits.value.vdouble.vector.FrequencyVector;
import org.djunits.value.vdouble.vector.TimeVector;
import org.djunits.value.vdouble.vector.base.DoubleVector;
import org.djunits.value.vfloat.scalar.FloatDuration;
import org.djutils.cli.CliException;
import org.djutils.cli.CliUtil;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.compressedfiles.CompressionType;
import org.opentrafficsim.base.compressedfiles.Writer;
import org.opentrafficsim.core.animation.gtu.colorer.AccelerationGTUColorer;
import org.opentrafficsim.core.animation.gtu.colorer.SpeedGTUColorer;
import org.opentrafficsim.core.animation.gtu.colorer.SwitchableGTUColorer;
import org.opentrafficsim.core.compatibility.Compatible;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.core.perception.HistoryManagerDEVS;
import org.opentrafficsim.draw.graphs.ContourDataSource;
import org.opentrafficsim.draw.graphs.ContourPlotSpeed;
import org.opentrafficsim.draw.graphs.GraphPath;
import org.opentrafficsim.draw.graphs.road.GraphLaneUtil;
import org.opentrafficsim.kpi.sampling.KpiGtuDirectionality;
import org.opentrafficsim.kpi.sampling.KpiLaneDirection;
import org.opentrafficsim.kpi.sampling.SamplingException;
import org.opentrafficsim.kpi.sampling.SpaceTimeRegion;
import org.opentrafficsim.kpi.sampling.Trajectory;
import org.opentrafficsim.kpi.sampling.TrajectoryGroup;
import org.opentrafficsim.road.gtu.colorer.DesiredHeadwayColorer;
import org.opentrafficsim.road.gtu.colorer.DistractionColorer;
import org.opentrafficsim.road.gtu.colorer.FixedColor;
import org.opentrafficsim.road.gtu.colorer.SynchronizationColorer;
import org.opentrafficsim.road.gtu.colorer.TaskSaturationColorer;
import org.opentrafficsim.road.gtu.generator.od.DefaultGTUCharacteristicsGeneratorOD;
import org.opentrafficsim.road.gtu.generator.od.ODApplier;
import org.opentrafficsim.road.gtu.generator.od.ODOptions;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.mental.AdaptationSituationalAwareness;
import org.opentrafficsim.road.gtu.lane.perception.mental.ExponentialTask;
import org.opentrafficsim.road.gtu.lane.perception.mental.Task;
import org.opentrafficsim.road.gtu.lane.perception.mental.sdm.DefaultDistraction;
import org.opentrafficsim.road.gtu.lane.perception.mental.sdm.DistractionFactory;
import org.opentrafficsim.road.gtu.lane.perception.mental.sdm.StochasticDistractionModel;
import org.opentrafficsim.road.gtu.lane.perception.mental.sdm.TaskSupplier;
import org.opentrafficsim.road.gtu.strategical.od.Categorization;
import org.opentrafficsim.road.gtu.strategical.od.Category;
import org.opentrafficsim.road.gtu.strategical.od.Interpolation;
import org.opentrafficsim.road.gtu.strategical.od.ODMatrix;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.factory.LaneFactory;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneDirection;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.OTSRoadNode;
import org.opentrafficsim.road.network.lane.Stripe.Permeable;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;
import org.opentrafficsim.road.network.lane.object.sensor.SinkSensor;
import org.opentrafficsim.road.network.sampling.LaneData;
import org.opentrafficsim.road.network.sampling.RoadSampler;
import org.opentrafficsim.road.network.sampling.data.TimeToCollision;
import org.opentrafficsim.swing.graphs.SwingContourPlot;
import org.opentrafficsim.swing.graphs.SwingPlot;
import org.opentrafficsim.swing.gui.OTSSimulationApplication;
import org.opentrafficsim.swing.script.AbstractSimulationScript;
import org.opentrafficsim.swing.script.IdmOptions;

import nl.tudelft.simulation.dsol.swing.gui.TablePanel;
import nl.tudelft.simulation.jstats.streams.StreamInterface;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

/**
 * Script to run a simulation with the Stochastic Distraction Model.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 5 nov. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class SdmSimulation extends AbstractSimulationScript
{
    /** Network. */
    private OTSRoadNetwork network;

    /** Sampler for statistics. */
    private RoadSampler sampler;

    /** Time to collision data type. */
    private final TimeToCollision ttc = new TimeToCollision();

    /** Space time regions to sample traffic on. */
    private final List<SpaceTimeRegion> regions = new ArrayList<>();

    // Options

    /** IDM parameters. */
    @Mixin
    private IdmOptions idmOptions;

    /** Output file. */
    @Option(names = "--outputFile", description = "Output file", defaultValue = "output.txt")
    private String outputFile;

    /** Output. */
    @Option(names = {"-o", "--output"}, description = "Create output", negatable = true, defaultValue = "true")
    private boolean output;

    /** Plots. */
    @Option(names = {"-p", "--plots"}, description = "Create plots", negatable = true, defaultValue = "false")
    private boolean plots;

    /** Fraction of trucks. */
    @Option(names = "--truckFraction", description = "Fraction of trucks", defaultValue = "0.05")
    private double truckFraction;

    /** Demand left. */
    @Option(names = "--demandLeft", description = "Demand left", defaultValue = "3600/h")
    private Frequency demandLeft;

    /** Demand right. */
    @Option(names = "--demandRight", description = "Demand right", defaultValue = "3600/h")
    private Frequency demandRight;

    /** Start demand factor. */
    @Option(names = "--startDemandFctor", description = "Factor on demand at start", defaultValue = "0.45")
    private double startDemandFctor;

    /** Allow multitasking. */
    @Option(names = "--multitasking", description = "Multitasking", negatable = true, defaultValue = "false")
    private boolean multitasking;

    /** Allow multitasking. */
    @Option(names = "--distractions", split = ",",
            description = "Distraction, 1=TALKING_CELL_PHONE,12=CONVERSING,5=MANIPULATING_AUDIO_CONTROLS,16=EXTERNAL_DISTRACTION",
            defaultValue = "1,12,5,16")
    private String[] distractions;

    /** Initial task demand when talking on the phone. */
    @Option(names = "--phoneInit", description = "Initial task demand when talking on the phone", defaultValue = "1.0")
    private double phoneInit;

    /** Final task demand when talking on the phone. */
    @Option(names = "--phoneFinal", description = "Final task demand when talking on the phone", defaultValue = "0.3")
    private double phoneFinal;

    /** Exponential duration if task demand reduction when talking on the phone. */
    @Option(names = "--phoneTau", description = "Exponential duration if task demand reduction when talking on the phone",
            defaultValue = "10s")
    private Duration phoneTau;

    /** Task demand when conversing. */
    @Option(names = "--conversing", description = "Task demand when conversing", defaultValue = "0.3")
    private double conversing;

    /** Task demand when controlling audio. */
    @Option(names = "--audio", description = "Task demand when controlling audio", defaultValue = "0.3")
    private double audio;

    /** Fixed (minimum) task demand for external distraction. */
    @Option(names = "--externalBase", description = "Fixed (minimum) task demand for external distraction",
            defaultValue = "0.2")
    private double externalBase;

    /** Variable task demand for external distraction. */
    @Option(names = "--externalVar",
            description = "Variable task demand for external distraction (random fraction added externalBase)",
            defaultValue = "0.3")
    private double externalVar;

    /** Time step. */
    @Option(names = "--dt", description = "Time step", defaultValue = "0.5s")
    private Duration dt;

    /** Minimum situational awareness. */
    @Option(names = "--saMin", description = "Minimum situational awareness", defaultValue = "0.5")
    private double saMin;

    /** Maximum situational awareness. */
    @Option(names = "--saMax", description = "Maximum situational awareness", defaultValue = "1.0")
    private double saMax;

    /** Task capability. */
    @Option(names = "--tc", description = "Task capability", defaultValue = "1.0")
    private double tc;

    /** Critical task saturation. */
    @Option(names = "--tsCrit", description = "Critical task saturation", defaultValue = "0.8")
    private double tsCrit;

    /** Maximum task saturation. */
    @Option(names = "--tsMax", description = "Maximum task saturation", defaultValue = "2.0")
    private double tsMax;

    /** Maximum reaction time. */
    @Option(names = "--trMax", description = "Maximum reaction time", defaultValue = "2.0s")
    private Duration trMax;

    /** Maximum additional factor on headway for adaptation. */
    @Option(names = "--betaT", description = "Maximum additional factor on headway for adaptation", defaultValue = "1.0")
    private double betaT;

    /**
     * Constructor.
     */
    protected SdmSimulation()
    {
        super("SDM simulation", "Simulations using the Stochastic Distraction Model");
        // set GTU colorers to use
        setGtuColorer(SwitchableGTUColorer.builder().addActiveColorer(new FixedColor(Color.BLUE, "Blue"))
                .addColorer(new SynchronizationColorer())
                .addColorer(new DistractionColorer(DefaultDistraction.ANSWERING_CELL_PHONE, DefaultDistraction.CONVERSING,
                        DefaultDistraction.MANIPULATING_AUDIO_CONTROLS, DefaultDistraction.EXTERNAL_DISTRACTION))
                .addColorer(new SpeedGTUColorer(new Speed(150, SpeedUnit.KM_PER_HOUR)))
                .addColorer(new AccelerationGTUColorer(Acceleration.instantiateSI(-6.0), Acceleration.instantiateSI(2)))
                .addColorer(new DesiredHeadwayColorer(Duration.instantiateSI(0.56), Duration.instantiateSI(2.4)))
                .addColorer(new TaskSaturationColorer()).build());
        try
        {
            CliUtil.changeOptionDefault(this, "warmupTime", "300s");
            CliUtil.changeOptionDefault(this, "simulationTime", "3900s");
            CliUtil.changeOptionDefault(IdmOptions.class, "aTruck", "0.8m/s^2");
        }
        catch (NoSuchFieldException | IllegalStateException | IllegalArgumentException | CliException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * Start a simulation.
     * @param args String...; command line arguments
     */
    public static void main(final String... args)
    {
        try
        {
            SdmSimulation sim = new SdmSimulation();
            CliUtil.execute(sim, args);
            sim.start();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void check() throws Exception
    {
        super.check();
        Throw.when(this.truckFraction < 0.0 || this.truckFraction > 1.0, IllegalArgumentException.class,
                "Truck fraction %f is below 0.0 or above 1.0.");
    }

    /** {@inheritDoc} */
    @Override
    protected OTSRoadNetwork setupSimulation(final OTSSimulatorInterface sim) throws Exception
    {
        // manager of historic information to allow a reaction time
        sim.getReplication().setHistoryManager(new HistoryManagerDEVS(sim,
                AdaptationSituationalAwareness.TR_MAX.getDefaultValue(), Duration.instantiateSI(10.0)));

        // Network
        this.network = new OTSRoadNetwork("SDM", true, getSimulator());
        OTSPoint3D pointA = new OTSPoint3D(0.0, 0.0);
        OTSPoint3D pointB = new OTSPoint3D(0.0, -20.0);
        OTSPoint3D pointC = new OTSPoint3D(1600.0, -20.0);
        OTSPoint3D pointD = new OTSPoint3D(2000.0, 0.0);
        OTSPoint3D pointE = new OTSPoint3D(2500.0, 0.0);
        OTSPoint3D pointF = new OTSPoint3D(3500.0, 0.0);
        OTSRoadNode nodeA = new OTSRoadNode(this.network, "A", pointA, Direction.ZERO);
        OTSRoadNode nodeB = new OTSRoadNode(this.network, "B", pointB, Direction.ZERO);
        OTSRoadNode nodeC = new OTSRoadNode(this.network, "C", pointC, Direction.ZERO);
        OTSRoadNode nodeD = new OTSRoadNode(this.network, "D", pointD, Direction.ZERO);
        OTSRoadNode nodeE = new OTSRoadNode(this.network, "E", pointE, Direction.ZERO);
        OTSRoadNode nodeF = new OTSRoadNode(this.network, "F", pointF, Direction.ZERO);
        LinkType type = this.network.getLinkType(LinkType.DEFAULTS.FREEWAY);
        LaneKeepingPolicy policy = LaneKeepingPolicy.KEEPRIGHT;
        Length laneWidth = Length.instantiateSI(3.5);
        LaneType laneType = this.network.getLaneType(LaneType.DEFAULTS.FREEWAY);
        Speed speedLimit = new Speed(120.0, SpeedUnit.KM_PER_HOUR);
        List<Lane> allLanes = new ArrayList<>();
        allLanes.addAll(new LaneFactory(this.network, nodeA, nodeD, type, sim, policy)
                .leftToRight(2.0, laneWidth, laneType, speedLimit).addLanes(Permeable.BOTH).getLanes());
        allLanes.addAll(new LaneFactory(this.network, nodeB, nodeC, type, sim, policy)
                .leftToRight(0.0, laneWidth, laneType, speedLimit).addLanes(Permeable.BOTH).getLanes());
        allLanes.addAll(new LaneFactory(this.network, nodeC, nodeD, type, sim, policy)
                .leftToRight(0.0, laneWidth, laneType, speedLimit).addLanes(Permeable.BOTH).getLanes());
        allLanes.addAll(
                new LaneFactory(this.network, nodeD, nodeE, type, sim, policy).leftToRight(2.0, laneWidth, laneType, speedLimit)
                        .addLanes(Permeable.BOTH, Permeable.BOTH, Permeable.BOTH).getLanes());
        List<Lane> lanesEF = new LaneFactory(this.network, nodeE, nodeF, type, sim, policy)
                .leftToRight(1.0, laneWidth, laneType, speedLimit).addLanes(Permeable.BOTH, Permeable.BOTH).getLanes();
        allLanes.addAll(lanesEF);
        for (Lane lane : lanesEF)
        {
            new SinkSensor(lane, lane.getLength().minus(Length.instantiateSI(50)), Compatible.EVERYTHING, sim);
        }

        // OD
        List<OTSNode> origins = new ArrayList<>();
        origins.add(nodeA);
        origins.add(nodeB);
        List<OTSNode> destinations = new ArrayList<>();
        destinations.add(nodeF);
        double wut = sim.getReplication().getTreatment().getWarmupPeriod().si;
        double rl = sim.getReplication().getTreatment().getRunLength().si;
        TimeVector timeVector = DoubleVector.instantiate(new double[] {0.0, wut, wut + (rl - wut) * 0.5, rl}, TimeUnit.DEFAULT,
                StorageType.DENSE);
        Interpolation interpolation = Interpolation.LINEAR;
        Categorization categorization = new Categorization("GTU categorization", GTUType.class);
        ODMatrix odMatrix = new ODMatrix("OD", origins, destinations, categorization, timeVector, interpolation);
        Category carCategory = new Category(categorization, this.network.getGtuType(GTUType.DEFAULTS.CAR));
        Category truCategory = new Category(categorization, this.network.getGtuType(GTUType.DEFAULTS.TRUCK));
        double f1 = this.truckFraction;
        double f2 = 1.0 - f1;
        double left2 = this.demandLeft.getInUnit(FrequencyUnit.PER_HOUR);
        double right2 = this.demandRight.getInUnit(FrequencyUnit.PER_HOUR);
        double startDemandFactor = this.startDemandFctor;
        double left1 = left2 * startDemandFactor;
        double right1 = right2 * startDemandFactor;
        odMatrix.putDemandVector(nodeA, nodeF, carCategory, freq(new double[] {f2 * left1, f2 * left1, f2 * left2, 0.0}));
        odMatrix.putDemandVector(nodeA, nodeF, truCategory, freq(new double[] {f1 * left1, f1 * left1, f1 * left2, 0.0}));
        odMatrix.putDemandVector(nodeB, nodeF, carCategory, freq(new double[] {f2 * right1, f2 * right1, f2 * right2, 0.0}));
        odMatrix.putDemandVector(nodeB, nodeF, truCategory, freq(new double[] {f1 * right1, f1 * right1, f1 * right2, 0.0}));
        ODOptions odOptions = new ODOptions().set(ODOptions.NO_LC_DIST, Length.instantiateSI(200)).set(ODOptions.GTU_TYPE,
                new DefaultGTUCharacteristicsGeneratorOD(
                        new SdmStrategicalPlannerFactory(this.network, sim.getReplication().getStream("generation"), this)));
        ODApplier.applyOD(this.network, odMatrix, sim, odOptions);

        // setup the SDM
        DistractionFactory distFactory = new DistractionFactory(sim.getReplication().getStream("default"));
        for (String distraction : this.distractions)
        {
            DefaultDistraction dist = DefaultDistraction.values()[Integer.parseInt(distraction) - 1];
            distFactory.addDistraction(dist, getTaskSupplier(dist, sim.getReplication().getStream("default")));
        }
        new StochasticDistractionModel(this.multitasking, distFactory.build(), sim, this.network);

        // sampler
        if (this.output)
        {
            this.sampler = new RoadSampler(this.network);
            Time start = new Time(0.05, TimeUnit.BASE_HOUR);
            Time end = new Time(1.05, TimeUnit.BASE_HOUR);
            for (Lane lane : allLanes)
            {
                KpiLaneDirection kpiLane = new KpiLaneDirection(new LaneData(lane), KpiGtuDirectionality.DIR_PLUS);
                SpaceTimeRegion region = new SpaceTimeRegion(kpiLane, Length.ZERO, lane.getLength(), start, end);
                this.regions.add(region);
                this.sampler.registerSpaceTimeRegion(region);
            }
            this.sampler.registerExtendedDataType(this.ttc);
        }

        // return network
        return this.network;
    }

    /** {@inheritDoc} */
    @Override
    protected void addTabs(final OTSSimulatorInterface sim, final OTSSimulationApplication<?> animation)
    {
        if (!this.output || !this.plots)
        {
            return;
        }
        try
        {
            TablePanel charts = new TablePanel(2, 2);
            GraphPath<KpiLaneDirection> path1 = GraphLaneUtil.createPath("Left road, left lane",
                    new LaneDirection((Lane) ((CrossSectionLink) this.network.getLink("AD")).getCrossSectionElement("Lane 1"),
                            GTUDirectionality.DIR_PLUS));
            GraphPath<KpiLaneDirection> path2 = GraphLaneUtil.createPath("Left road, right lane",
                    new LaneDirection((Lane) ((CrossSectionLink) this.network.getLink("AD")).getCrossSectionElement("Lane 2"),
                            GTUDirectionality.DIR_PLUS));
            GraphPath<KpiLaneDirection> path3 = GraphLaneUtil.createPath("Right road, left lane",
                    new LaneDirection((Lane) ((CrossSectionLink) this.network.getLink("BC")).getCrossSectionElement("Lane 1"),
                            GTUDirectionality.DIR_PLUS));
            GraphPath<KpiLaneDirection> path4 = GraphLaneUtil.createPath("Right road, right lane",
                    new LaneDirection((Lane) ((CrossSectionLink) this.network.getLink("BC")).getCrossSectionElement("Lane 2"),
                            GTUDirectionality.DIR_PLUS));
            SwingPlot plot = null;
            plot = new SwingContourPlot(
                    new ContourPlotSpeed("Left road, left lane", sim, new ContourDataSource<>(this.sampler, path1)));
            charts.setCell(plot.getContentPane(), 0, 0);
            plot = new SwingContourPlot(
                    new ContourPlotSpeed("Left road, right lane", sim, new ContourDataSource<>(this.sampler, path2)));
            charts.setCell(plot.getContentPane(), 1, 0);
            plot = new SwingContourPlot(
                    new ContourPlotSpeed("Right road, left lane", sim, new ContourDataSource<>(this.sampler, path3)));
            charts.setCell(plot.getContentPane(), 0, 1);
            plot = new SwingContourPlot(
                    new ContourPlotSpeed("Right road, right lane", sim, new ContourDataSource<>(this.sampler, path4)));
            charts.setCell(plot.getContentPane(), 1, 1);
            animation.getAnimationPanel().getTabbedPane().addTab(animation.getAnimationPanel().getTabbedPane().getTabCount(),
                    "statistics ", charts);
        }
        catch (NetworkException exception)
        {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Creates a frequency vector.
     * @param array double[]; array in veh/h
     * @return FrequencyVector; frequency vector
     * @throws ValueRuntimeException on problem
     */
    private FrequencyVector freq(final double[] array) throws ValueRuntimeException
    {
        return DoubleVector.instantiate(array, FrequencyUnit.PER_HOUR, StorageType.DENSE);
    }

    /**
     * Returns a task supplier for a distraction. These are specific to the SDM simulations.
     * @param distraction DefaultDistraction; distraction
     * @param stream StreamInterface; random number stream for randomized aspects of the distraction
     * @return TaskSupplier; task supplier
     */
    private TaskSupplier getTaskSupplier(final DefaultDistraction distraction, final StreamInterface stream)
    {
        switch (distraction)
        {
            case TALKING_CELL_PHONE:
            {
                return new TaskSupplier()
                {
                    /** {@inheritDoc} */
                    @SuppressWarnings("synthetic-access")
                    @Override
                    public Task getTask(final LaneBasedGTU gtu)
                    {
                        return new ExponentialTask(distraction.getId(), SdmSimulation.this.phoneInit,
                                SdmSimulation.this.phoneFinal, SdmSimulation.this.phoneTau, gtu.getSimulator());
                    }
                };
            }
            case CONVERSING:
            {
                return new TaskSupplier.Constant(distraction.getId(), SdmSimulation.this.conversing);
            }
            case MANIPULATING_AUDIO_CONTROLS:
            {
                return new TaskSupplier.Constant(distraction.getId(), SdmSimulation.this.audio);
            }
            case EXTERNAL_DISTRACTION:
            {
                return new TaskSupplier.Constant(distraction.getId(),
                        SdmSimulation.this.externalBase + SdmSimulation.this.externalVar * stream.nextDouble());
            }
            default:
                throw new IllegalArgumentException("Distraction " + distraction + " is not recognized.");
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void onSimulationEnd()
    {
        if (this.output)
        {
            Length preDetectorPosition = Length.instantiateSI(400.0); // on link DE, upstream of lane drop
            Length postDetectorPosition = Length.instantiateSI(100.0); // on link EF, downstream of lane drop
            double tts = 0.0;
            List<Float> ttcList = new ArrayList<>();
            List<Float> decList = new ArrayList<>();
            int[] counts = new int[60];
            int[] speedCounts = new int[60];
            double[] speedSum = new double[60];
            for (SpaceTimeRegion region : this.regions)
            {
                TrajectoryGroup<?> trajectoryGroup =
                        this.sampler.getTrajectoryGroup(region.getLaneDirection()).getTrajectoryGroup(region.getStartPosition(),
                                region.getEndPosition(), region.getStartTime(), region.getEndTime());
                for (Trajectory<?> trajectory : trajectoryGroup)
                {
                    try
                    {
                        tts += trajectory.getTotalDuration().si;
                        for (FloatDuration ttcVal : trajectory.getExtendedData(this.ttc))
                        {
                            if (!Float.isNaN(ttcVal.si) && ttcVal.si < 20)
                            {
                                ttcList.add(ttcVal.si);
                            }
                        }
                        for (float decVal : trajectory.getA())
                        {
                            if (decVal < -2.0)
                            {
                                decList.add(decVal);
                            }
                        }
                        if (region.getLaneDirection().getLaneData().getLinkData().getId().equals("DE") && trajectory.size() > 1
                                && trajectory.getX(0) < preDetectorPosition.si
                                && trajectory.getX(trajectory.size() - 1) > preDetectorPosition.si)
                        {
                            double t = trajectory.getTimeAtPosition(postDetectorPosition).si - region.getStartTime().si;
                            double v = trajectory.getSpeedAtPosition(postDetectorPosition).si;
                            speedCounts[(int) (t / 60.0)]++;
                            speedSum[(int) (t / 60.0)] += v;
                        }
                        if (region.getLaneDirection().getLaneData().getLinkData().getId().equals("EF") && trajectory.size() > 1
                                && trajectory.getX(0) < postDetectorPosition.si
                                && trajectory.getX(trajectory.size() - 1) > postDetectorPosition.si)
                        {
                            double t = trajectory.getTimeAtPosition(postDetectorPosition).si - region.getStartTime().si;
                            counts[(int) (t / 60.0)]++;
                        }
                    }
                    catch (SamplingException exception)
                    {
                        throw new RuntimeException(
                                "Unexpected exception: TimeToCollission not available or index out of bounds.", exception);
                    }
                }
            }
            double qMax = 0;
            for (int i = 0; i < counts.length - 4; i++)
            {
                int q = 0;
                for (int j = i; j < i + 5; j++)
                {
                    q += counts[j];
                }
                qMax = qMax > q ? qMax : q;
            }
            qMax *= 12; // twelve periods of 5min in an hour
            int n = 0;
            int countSum = 0;
            for (int i = 0; i < counts.length; i++)
            {
                double v = speedSum[i] / speedCounts[i];
                if (v < 80 / 3.6)
                {
                    countSum += counts[i];
                    n++;
                }
            }
            double qSat = n == 0 ? Double.NaN : 60.0 * countSum / n; // per min -> per hour
            BufferedWriter bw;
            try
            {
                bw = new BufferedWriter(
                        new OutputStreamWriter(Writer.createOutputStream(this.outputFile, CompressionType.ZIP)));
                bw.write(String.format("total time spent [s]: %.0f", tts));
                bw.newLine();
                bw.write(String.format("maximum flow [veh/h]: %.3f", qMax));
                bw.newLine();
                bw.write(String.format("saturation flow [veh/h]: %.3f", qSat));
                bw.newLine();
                bw.write(String.format("time to collision [s]: %s", ttcList));
                bw.newLine();
                bw.write(String.format("strong decelerations [m/s^2]: %s", decList));
                bw.close();
            }
            catch (IOException exception)
            {
                throw new RuntimeException(exception);
            }
        }
    }

    /**
     * @return idmOptions.
     */
    public IdmOptions getIdmOptions()
    {
        return this.idmOptions;
    }

    /**
     * @return dt.
     */
    public Duration getDt()
    {
        return this.dt;
    }

    /**
     * @return saMin.
     */
    public double getSaMin()
    {
        return this.saMin;
    }

    /**
     * @return saMax.
     */
    public double getSaMax()
    {
        return this.saMax;
    }

    /**
     * @return tc.
     */
    public double getTc()
    {
        return this.tc;
    }

    /**
     * @return tsCrit.
     */
    public double getTsCrit()
    {
        return this.tsCrit;
    }

    /**
     * @return tsMax.
     */
    public double getTsMax()
    {
        return this.tsMax;
    }

    /**
     * @return trMax.
     */
    public Duration getTrMax()
    {
        return this.trMax;
    }

    /**
     * @return betaT.
     */
    public double getBetaT()
    {
        return this.betaT;
    }

}
