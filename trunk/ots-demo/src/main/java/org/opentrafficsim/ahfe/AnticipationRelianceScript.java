package org.opentrafficsim.ahfe;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.ValueRuntimeException;
import org.djunits.value.storage.StorageType;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.vector.FrequencyVector;
import org.djunits.value.vdouble.vector.TimeVector;
import org.djunits.value.vdouble.vector.base.DoubleVector;
import org.djutils.cli.CliException;
import org.djutils.cli.CliUtil;
import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;
import org.djutils.io.URLResource;
import org.opentrafficsim.base.CompressedFileWriter;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterSet;
import org.opentrafficsim.base.parameters.ParameterTypeDouble;
import org.opentrafficsim.base.parameters.ParameterTypeDuration;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.base.parameters.constraint.DualBound;
import org.opentrafficsim.base.parameters.constraint.NumericConstraint;
import org.opentrafficsim.core.animation.gtu.colorer.AccelerationGTUColorer;
import org.opentrafficsim.core.animation.gtu.colorer.SpeedGTUColorer;
import org.opentrafficsim.core.animation.gtu.colorer.SwitchableGTUColorer;
import org.opentrafficsim.core.distributions.Distribution;
import org.opentrafficsim.core.distributions.Distribution.FrequencyAndObject;
import org.opentrafficsim.core.distributions.Generator;
import org.opentrafficsim.core.distributions.ProbabilityException;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.AbstractGTU;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.perception.DirectEgoPerception;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.parameters.ParameterFactoryByType;
import org.opentrafficsim.core.perception.HistoryManagerDEVS;
import org.opentrafficsim.core.units.distributions.ContinuousDistSpeed;
import org.opentrafficsim.draw.core.OTSDrawingException;
import org.opentrafficsim.draw.factory.DefaultAnimationFactory;
import org.opentrafficsim.kpi.interfaces.LaneDataInterface;
import org.opentrafficsim.kpi.sampling.KpiGtuDirectionality;
import org.opentrafficsim.kpi.sampling.KpiLaneDirection;
import org.opentrafficsim.kpi.sampling.SpaceTimeRegion;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataTypeNumber;
import org.opentrafficsim.road.gtu.colorer.DesiredHeadwayColorer;
import org.opentrafficsim.road.gtu.colorer.DesiredSpeedColorer;
import org.opentrafficsim.road.gtu.colorer.FixedColor;
import org.opentrafficsim.road.gtu.colorer.GTUTypeColorer;
import org.opentrafficsim.road.gtu.colorer.IncentiveColorer;
import org.opentrafficsim.road.gtu.colorer.ReactionTimeColorer;
import org.opentrafficsim.road.gtu.colorer.SynchronizationColorer;
import org.opentrafficsim.road.gtu.colorer.TaskColorer;
import org.opentrafficsim.road.gtu.colorer.TaskSaturationColorer;
import org.opentrafficsim.road.gtu.colorer.TotalDesireColorer;
import org.opentrafficsim.road.gtu.generator.od.DefaultGTUCharacteristicsGeneratorOD;
import org.opentrafficsim.road.gtu.generator.od.ODApplier;
import org.opentrafficsim.road.gtu.generator.od.ODOptions;
import org.opentrafficsim.road.gtu.generator.od.StrategicalPlannerFactorySupplierOD;
import org.opentrafficsim.road.gtu.lane.CollisionDetector;
import org.opentrafficsim.road.gtu.lane.CollisionException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.CategoricalLanePerception;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionFactory;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.AnticipationTrafficPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.DirectInfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.Anticipation;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.DirectNeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.Estimation;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.HeadwayGtuType.PerceivedHeadwayGtuType;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.TaskHeadwayCollector;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTU;
import org.opentrafficsim.road.gtu.lane.perception.mental.AbstractTask;
import org.opentrafficsim.road.gtu.lane.perception.mental.AdaptationHeadway;
import org.opentrafficsim.road.gtu.lane.perception.mental.AdaptationSituationalAwareness;
import org.opentrafficsim.road.gtu.lane.perception.mental.Fuller;
import org.opentrafficsim.road.gtu.lane.perception.mental.Fuller.BehavioralAdaptation;
import org.opentrafficsim.road.gtu.lane.perception.mental.Task;
import org.opentrafficsim.road.gtu.lane.perception.mental.TaskManager;
import org.opentrafficsim.road.gtu.lane.tactical.following.AbstractIDM;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModelFactory;
import org.opentrafficsim.road.gtu.lane.tactical.following.DesiredSpeedModel;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlus;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.AccelerationIncentive;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.DefaultLMRSPerceptionFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveCourtesy;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveGetInLane;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveKeep;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveRoute;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveSocioSpeed;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveSpeed;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveSpeedWithCourtesy;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveStayRight;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.LMRSFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.SocioDesiredSpeed;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Cooperation;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.GapAcceptance;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsParameters;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.MandatoryIncentive;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Synchronization;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Tailgating;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.VoluntaryIncentive;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;
import org.opentrafficsim.road.gtu.strategical.od.Categorization;
import org.opentrafficsim.road.gtu.strategical.od.Category;
import org.opentrafficsim.road.gtu.strategical.od.Interpolation;
import org.opentrafficsim.road.gtu.strategical.od.ODMatrix;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlannerFactory;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.factory.xml.parser.XmlNetworkLaneParser;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.sampling.GtuData;
import org.opentrafficsim.road.network.sampling.LinkData;
import org.opentrafficsim.road.network.sampling.RoadSampler;
import org.opentrafficsim.road.network.sampling.data.LeaderId;
import org.opentrafficsim.road.network.sampling.data.ReactionTime;
import org.opentrafficsim.road.network.sampling.data.TimeToCollision;
import org.opentrafficsim.swing.script.AbstractSimulationScript;

import nl.tudelft.simulation.jstats.distributions.DistLogNormal;
import nl.tudelft.simulation.jstats.distributions.DistNormal;
import nl.tudelft.simulation.jstats.distributions.DistTriangular;
import nl.tudelft.simulation.jstats.streams.StreamInterface;
import picocli.CommandLine.Option;

/**
 * Distraction simulation.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 9 apr. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public final class AnticipationRelianceScript extends AbstractSimulationScript
{

    /** */
    private static final long serialVersionUID = 20200516L;

    /** Car-following task parameter. */
    static final ParameterTypeDuration HEXP = new ParameterTypeDuration("Hexp",
            "Exponential decay of car-following task by headway.", Duration.instantiateSI(4.0), NumericConstraint.POSITIVE);

    /** Fraction of primary task that can be reduced by anticipation reliance. */
    static final ParameterTypeDouble ALPHA = new ParameterTypeDouble("alpha",
            "Fraction of primary task that can be reduced by anticipation reliance.", 0.8, DualBound.UNITINTERVAL);

    /** Fraction of auxiliary tasks that can be reduced by anticipation reliance. */
    static final ParameterTypeDouble BETA = new ParameterTypeDouble("beta",
            "Fraction of auxiliary tasks that can be reduced by anticipation reliance.", 0.6, DualBound.UNITINTERVAL);

    /** Distance to not consider at start of the network. */
    private static Length ignoreStart = Length.instantiateSI(2900); // Not 100m on pre-link, so 3000 total

    /** Distance to not consider at end of the network. */
    private static Length ignoreEnd = Length.instantiateSI(1000);

    /** Sampler. */
    private RoadSampler sampler;

    /** Truck fraction. */
    @Option(names = {"-f", "--fTruck"}, description = "Truck fraction", defaultValue = "0.05")
    private double fTruck;

    /** Left demand. */
    @Option(names = "--leftDemand", description = "Left demand", defaultValue = "3500/h")
    private Frequency leftDemand;

    /** Right demand. */
    @Option(names = "--rightDemand", description = "Right demand", defaultValue = "3200/h")
    private Frequency rightDemand;

    /** Sampler. */
    @Option(names = "--sampler", description = "Sampler", negatable = true, defaultValue = "false")
    private boolean doSampler;

    /** Sampler. */
    @Option(names = "--scenario", description = "Scenario", defaultValue = "test")
    private String scenario;

    /** Output directory. */
    @Option(names = "--outputDir", description = "Output directory", defaultValue = "")
    private String outputDir;

    /** Tasks. */
    @Option(names = "--tasks", description = "Use tasks", negatable = true, defaultValue = "false")
    private boolean tasks;

    /** Strategies. */
    @Option(names = "--strategies", description = "Use strategies", negatable = true, defaultValue = "false")
    private boolean strategies;

    /** Adaptation. */
    @Option(names = "--adaptation", description = "Use adaptation", negatable = true, defaultValue = "false")
    private boolean adaptation;

    /** Alpha. */
    @Option(names = "--alpha", description = "Alpha: maximum lane-change reduction", defaultValue = "0.8")
    private double alpha;

    /** Beta. */
    @Option(names = "--beta", description = "Beta: maximum car-following reduction", defaultValue = "0.6")
    private double beta;

    /** Fraction of underestimation. */
    @Option(names = "--fUnderestimate", description = "Fraction underestimation", defaultValue = "0.75")
    private double fractionUnderestimation;

    /**
     * Main method.
     * @param args String[]; command line arguments
     */
    public static void main(final String... args)
    {
        // Long start = System.currentTimeMillis();
        AnticipationRelianceScript script = new AnticipationRelianceScript();
        if (script.isAutorun())
        {
            System.out.println("Running " + script.scenario + "_" + script.getSeed());
        }
        // script.setProperty("sampler", true);
        // script.setProperty("autorun", false);
        // script.setProperty("tasks", true);
        // script.setProperty("strategies", false);
        // script.setProperty("adaptation", false);
        // script.setProperty("alpha", 0.0);
        // script.setProperty("beta", 0.0);
        try
        {
            CliUtil.execute(script, args); // XX from old version
            script.start();
        }
        catch (Throwable throwable)
        {
            // Note: we only get here if we autorun
            Throwable original = throwable;
            while (throwable != null)
            {
                if (throwable instanceof CollisionException)
                {
                    double tCollision = script.getSimulator().getSimulatorTime().si;
                    script.onSimulationEnd();
                    String file = script.getOutputFileStart() + "_collision.txt";
                    BufferedWriter writer = CompressedFileWriter.create(file, false);
                    try
                    {
                        writer.write(String.format("Collision at: %.6f", tCollision));
                        writer.newLine();
                        writer.write(throwable.getMessage());
                        writer.close();
                    }
                    catch (IOException ex)
                    {
                        System.err.println("Unable to write to file.");
                    }
                    System.exit(0);
                }
                throwable = throwable.getCause();
            }
            throw new RuntimeException(original);
        }
    }

    /**
     * Returns the start for output files, which can be amended with some name of contained data and extension.
     * @return String; start for output files, which can be amended with some name of contained data and extension
     */
    private String getOutputFileStart()
    {
        return this.outputDir + this.scenario + "_" + getSeed();
    }

    /**
     * Constructor.
     */
    private AnticipationRelianceScript()
    {
        super("Distraction", "Distraction simulation");
        setGtuColorer(SwitchableGTUColorer.builder().addActiveColorer(new FixedColor(Color.BLUE, "Blue"))
                .addColorer(new TaskColorer("car-following")).addColorer(new TaskColorer("lane-changing"))
                .addColorer(new TaskSaturationColorer()).addColorer(new ReactionTimeColorer(Duration.instantiateSI(1.0)))
                .addColorer(GTUTypeColorer.DEFAULT).addColorer(new SpeedGTUColorer(new Speed(150, SpeedUnit.KM_PER_HOUR)))
                .addColorer(
                        new DesiredSpeedColorer(new Speed(50, SpeedUnit.KM_PER_HOUR), new Speed(150, SpeedUnit.KM_PER_HOUR)))
                .addColorer(new AccelerationGTUColorer(Acceleration.instantiateSI(-6.0), Acceleration.instantiateSI(2)))
                .addColorer(new SynchronizationColorer())
                .addColorer(new DesiredHeadwayColorer(Duration.instantiateSI(0.56), Duration.instantiateSI(2.4)))
                .addColorer(new TotalDesireColorer()).addColorer(new IncentiveColorer(IncentiveRoute.class))
                .addColorer(new IncentiveColorer(IncentiveSpeedWithCourtesy.class))
                .addColorer(new IncentiveColorer(IncentiveSpeed.class)).addColorer(new IncentiveColorer(IncentiveKeep.class))
                .addColorer(new IncentiveColorer(IncentiveGetInLane.class))
                .addColorer(new IncentiveColorer(IncentiveCourtesy.class))
                .addColorer(new IncentiveColorer(IncentiveSocioSpeed.class)).build());
        try
        {
            CliUtil.changeOptionDefault(this, "warmupTime", "360s");
            CliUtil.changeOptionDefault(this, "simulationTime", "3960s");
        }
        catch (NoSuchFieldException | IllegalStateException | IllegalArgumentException | CliException exception)
        {
            throw new RuntimeException(exception);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void onSimulationEnd()
    {
        if (this.sampler != null)
        {
            this.sampler.getSamplerData().writeToFile(getOutputFileStart() + ".csv");
        }
    }

    /** {@inheritDoc} */
    @Override
    protected OTSRoadNetwork setupSimulation(final OTSSimulatorInterface sim) throws Exception
    {
        AbstractGTU.ALIGNED = true;

        // Network
        URL xmlURL = URLResource.getResource("/AHFE/Network.xml");
        OTSRoadNetwork network = new OTSRoadNetwork("Distraction", true, sim);
        new CollisionDetector(network); // XXX: is this needed here? was in old version...
        XmlNetworkLaneParser.build(xmlURL, network, false);

        // new Distraction("distraction", ((CrossSectionLink) network.getLink("END")).getLanes().get(0),
        // Length.instantiateSI(1000),
        // sim, new TrapezoidProfile(0.2, Length.instantiateSI(-400), Length.instantiateSI(200), Length.instantiateSI(400)));

        // OD
        List<Node> origins = new ArrayList<>();
        origins.add(network.getNode("LEFTINPRE"));
        origins.add(network.getNode("RIGHTINPRE"));
        List<Node> destinations = new ArrayList<>();
        destinations.add(network.getNode("EXIT"));
        Categorization categorization = new Categorization("Distraction", GTUType.class);
        TimeVector globalTime =
                DoubleVector.instantiate(new double[] {0, 360, 1560, 2160, 3960}, TimeUnit.BASE_SECOND, StorageType.DENSE);
        ODMatrix od = new ODMatrix("Distraction", origins, destinations, categorization, globalTime, Interpolation.LINEAR);
        Category carCategory = new Category(categorization, network.getGtuType(GTUType.DEFAULTS.CAR));
        Category truckCategory = new Category(categorization, network.getGtuType(GTUType.DEFAULTS.TRUCK));
        FrequencyVector leftDemandPatternCar =
                getDemand(this.leftDemand.getInUnit(FrequencyUnit.PER_HOUR) * (1.0 - this.fTruck));
        FrequencyVector leftDemandPatternTruck = getDemand(this.leftDemand.getInUnit(FrequencyUnit.PER_HOUR) * this.fTruck);
        FrequencyVector rightDemandPatternCar =
                getDemand(this.rightDemand.getInUnit(FrequencyUnit.PER_HOUR) * (1.0 - this.fTruck));
        FrequencyVector rightDemandPatternTruck = getDemand(this.rightDemand.getInUnit(FrequencyUnit.PER_HOUR) * this.fTruck);
        od.putDemandVector(network.getNode("LEFTINPRE"), network.getNode("EXIT"), carCategory, leftDemandPatternCar);
        od.putDemandVector(network.getNode("LEFTINPRE"), network.getNode("EXIT"), truckCategory, leftDemandPatternTruck);
        od.putDemandVector(network.getNode("RIGHTINPRE"), network.getNode("EXIT"), carCategory, rightDemandPatternCar);
        od.putDemandVector(network.getNode("RIGHTINPRE"), network.getNode("EXIT"), truckCategory, rightDemandPatternTruck);
        ODOptions odOptions = new ODOptions()
                .set(ODOptions.GTU_TYPE, new DefaultGTUCharacteristicsGeneratorOD(new DistractionFactorySupplier()))
                .set(ODOptions.INSTANT_LC, true);
        ODApplier.applyOD(network, od, odOptions);

        // History
        sim.getReplication()
                .setHistoryManager(new HistoryManagerDEVS(sim, Duration.instantiateSI(2.0), Duration.instantiateSI(1.0)));

        // Sampler
        if (this.doSampler)
        {
            RoadSampler.Factory factory = RoadSampler.build(network);
            factory.registerExtendedDataType(new TimeToCollision());
            factory.registerExtendedDataType(new TaskSaturationDataType());
            factory.registerExtendedDataType(new LeaderId());
            factory.registerExtendedDataType(new ReactionTime());
            factory.registerExtendedDataType(new SituationalAwarenessDataType());
            if (this.tasks)
            {
                factory.registerExtendedDataType(new TaskAnticipationRelianceDataType("car-following"));
                factory.registerExtendedDataType(new TaskDemandDataType("car-following"));
                factory.registerExtendedDataType(new TaskAnticipationRelianceDataType("lane-changing"));
                factory.registerExtendedDataType(new TaskDemandDataType("lane-changing"));
            }
            this.sampler = factory.create();

            LinkData linkData = new LinkData((CrossSectionLink) network.getLink("LEFTIN"));
            registerLinkToSampler(linkData, ignoreStart, linkData.getLength());
            linkData = new LinkData((CrossSectionLink) network.getLink("RIGHTIN"));
            registerLinkToSampler(linkData, ignoreStart, linkData.getLength());
            linkData = new LinkData((CrossSectionLink) network.getLink("CONVERGE"));
            registerLinkToSampler(linkData, Length.ZERO, linkData.getLength());
            linkData = new LinkData((CrossSectionLink) network.getLink("WEAVING"));
            registerLinkToSampler(linkData, Length.ZERO, linkData.getLength());
            linkData = new LinkData((CrossSectionLink) network.getLink("END"));
            registerLinkToSampler(linkData, Length.ZERO, linkData.getLength().minus(ignoreEnd));
        }

        // return
        return network;
    }

    /**
     * Register a link to the sampler, so data is sampled there.
     * @param linkData LinkData; link data
     * @param startDistance Length; start distance on link
     * @param endDistance Length; end distance on link
     */
    private void registerLinkToSampler(final LinkData linkData, final Length startDistance, final Length endDistance)
    {
        for (LaneDataInterface laneData : linkData.getLaneDatas())
        {
            Length start = laneData.getLength().times(startDistance.si / linkData.getLength().si);
            Length end = laneData.getLength().times(endDistance.si / linkData.getLength().si);
            this.sampler
                    .registerSpaceTimeRegion(new SpaceTimeRegion(new KpiLaneDirection(laneData, KpiGtuDirectionality.DIR_PLUS),
                            start, end, getStartTime().plus(getWarmupTime()), getStartTime().plus(getSimulationTime())));
        }
    }

    /**
     * Compose demand vector.
     * @param demand double; maximum demand value
     * @return FrequencyVector demand vector
     * @throws ValueRuntimeException on value exception
     */
    private static FrequencyVector getDemand(final double demand) throws ValueRuntimeException
    {
        return DoubleVector.instantiate(new double[] {demand * 0.5, demand * 0.5, demand, demand, 0.0}, FrequencyUnit.PER_HOUR,
                StorageType.DENSE);
    }

    /** {@inheritDoc} */
    @Override
    protected void animateNetwork(final OTSNetwork net)
    {
        try
        {
            DefaultAnimationFactory.animateXmlNetwork(net, getGtuColorer());
        }
        catch (OTSDrawingException exception)
        {
            throw new RuntimeException("Exception while creating network animation.", exception);
        }
    }

    /////////////////////
    ////// FACTORY //////
    /////////////////////

    /**
     * Supplier of strategical factory.
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 9 apr. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private class DistractionFactorySupplier implements StrategicalPlannerFactorySupplierOD
    {

        /** Factory cars. */
        private LaneBasedStrategicalRoutePlannerFactory factoryCar = null;

        /** Factory trucks. */
        private LaneBasedStrategicalRoutePlannerFactory factoryTruck = null;

        /** */
        DistractionFactorySupplier()
        {
        }

        /** {@inheritDoc} */
        @SuppressWarnings("synthetic-access")
        @Override
        public LaneBasedStrategicalPlannerFactory<?> getFactory(final Node origin, final Node destination,
                final Category category, final StreamInterface randomStream) throws GTUException
        {
            OTSRoadNetwork network = (OTSRoadNetwork) origin.getNetwork();
            if (this.factoryCar == null)
            {
                // car-following model, with different desired speed models
                CarFollowingModelFactory<IDMPlus> cfFactoryCar =
                        new IdmPlusFactoryAR(() -> AnticipationRelianceScript.this.strategies
                                ? new SocioDesiredSpeed(AbstractIDM.DESIRED_SPEED) : AbstractIDM.DESIRED_SPEED);
                CarFollowingModelFactory<IDMPlus> cfFactoryTruck = new IdmPlusFactoryAR(() -> AbstractIDM.DESIRED_SPEED);

                // perception factory with estimation distribution (i.e. over- vs. underestimation)
                Distribution<Estimation> estimation;
                try
                {
                    estimation = new Distribution<>(randomStream);
                    estimation.add(new FrequencyAndObject<>(AnticipationRelianceScript.this.fractionUnderestimation,
                            Estimation.UNDERESTIMATION));
                    estimation.add(new FrequencyAndObject<>(1.0 - AnticipationRelianceScript.this.fractionUnderestimation,
                            Estimation.OVERESTIMATION));
                }
                catch (ProbabilityException ex)
                {
                    throw new GTUException("Random stream is null.", ex);
                }
                PerceptionFactory perceptionFactory = new LmrsPerceptionFactoryAR(estimation);

                // tailgating
                Tailgating tailgating = AnticipationRelianceScript.this.strategies ? Tailgating.PRESSURE : Tailgating.NONE;

                // incentives
                Set<MandatoryIncentive> mandatoryIncentives = new LinkedHashSet<>();
                mandatoryIncentives.add(new IncentiveRoute());
                Set<VoluntaryIncentive> voluntaryIncentivesCar = new LinkedHashSet<>();
                Set<VoluntaryIncentive> voluntaryIncentivesTruck = new LinkedHashSet<>();
                voluntaryIncentivesCar.add(new IncentiveSpeedWithCourtesy());
                voluntaryIncentivesCar.add(new IncentiveKeep());
                if (AnticipationRelianceScript.this.strategies)
                {
                    voluntaryIncentivesCar.add(new IncentiveSocioSpeed());
                }
                voluntaryIncentivesTruck.addAll(voluntaryIncentivesCar);
                voluntaryIncentivesTruck.add(new IncentiveStayRight());

                // acceleration incentives (none, nothing on the freeway)
                Set<AccelerationIncentive> accelerationIncentives = new LinkedHashSet<>();

                // parameter factory
                ParameterFactoryByType params = new ParameterFactoryByType();
                params.addParameter(network.getGtuType(GTUType.DEFAULTS.CAR), ParameterTypes.FSPEED,
                        new DistNormal(randomStream, 123.7 / 120.0, 12.0 / 120.0));
                params.addParameter(network.getGtuType(GTUType.DEFAULTS.TRUCK), ParameterTypes.A,
                        Acceleration.instantiateSI(0.4));
                if (AnticipationRelianceScript.this.strategies)
                {
                    params.addParameter(Tailgating.RHO, 0.0);
                    params.addParameter(network.getGtuType(GTUType.DEFAULTS.CAR), LmrsParameters.SOCIO,
                            new DistTriangular(randomStream, 0.0, 0.25, 1.0));
                    params.addParameter(network.getGtuType(GTUType.DEFAULTS.TRUCK), LmrsParameters.SOCIO, 1.0);
                    params.addParameter(network.getGtuType(GTUType.DEFAULTS.CAR), LmrsParameters.VGAIN,
                            new ContinuousDistSpeed(new DistLogNormal(randomStream, 3.3789, 0.4), SpeedUnit.KM_PER_HOUR));
                    params.addParameter(network.getGtuType(GTUType.DEFAULTS.TRUCK), LmrsParameters.VGAIN,
                            new Speed(50.0, SpeedUnit.KM_PER_HOUR));
                    params.addParameter(ParameterTypes.TMAX, Duration.instantiateSI(1.6));
                }
                if (AnticipationRelianceScript.this.adaptation)
                {
                    params.addParameter(AdaptationHeadway.BETA_T, 1.0); // T := T * (1.0 + this value)
                }

                // factories
                this.factoryCar = new LaneBasedStrategicalRoutePlannerFactory(new LMRSFactory(cfFactoryCar, perceptionFactory,
                        Synchronization.PASSIVE, Cooperation.PASSIVE, GapAcceptance.INFORMED, tailgating, mandatoryIncentives,
                        voluntaryIncentivesCar, accelerationIncentives), params);
                this.factoryTruck = new LaneBasedStrategicalRoutePlannerFactory(new LMRSFactory(cfFactoryTruck,
                        perceptionFactory, Synchronization.PASSIVE, Cooperation.PASSIVE, GapAcceptance.INFORMED, tailgating,
                        mandatoryIncentives, voluntaryIncentivesTruck, accelerationIncentives), params);
            }
            return category.get(GTUType.class).isOfType(network.getGtuType(GTUType.DEFAULTS.TRUCK)) ? this.factoryTruck
                    : this.factoryCar;
        }
    }

    ////////////////////////////////////
    ////// FACTORY HELPER CLASSES //////
    ////////////////////////////////////

    /** Car-following task. */
    private class CarFollowingTaskAR extends AbstractTask
    {
        /** Constructor. */
        CarFollowingTaskAR()
        {
            super("car-following");
        }

        /** {@inheritDoc} */
        @Override
        public double calculateTaskDemand(final LanePerception perception, final LaneBasedGTU gtuCF,
                final Parameters parameters) throws ParameterException, GTUException
        {
            try
            {
                NeighborsPerception neighbors = perception.getPerceptionCategory(NeighborsPerception.class);
                PerceptionCollectable<HeadwayGTU, LaneBasedGTU> leaders = neighbors.getLeaders(RelativeLane.CURRENT);
                Duration headway = leaders.collect(new TaskHeadwayCollector(gtuCF.getSpeed()));
                return headway == null ? 0.0 : Math.exp(-headway.si / parameters.getParameter(HEXP).si);
            }
            catch (OperationalPlanException ex)
            {
                throw new GTUException(ex);
            }
        }
    }

    /** Lane change task. */
    private class LaneChangeTaskAR extends AbstractTask
    {
        /** Constructor. */
        LaneChangeTaskAR()
        {
            super("lane-changing");
        }

        /** {@inheritDoc} */
        @Override
        public double calculateTaskDemand(final LanePerception perception, final LaneBasedGTU gtuLC,
                final Parameters parameters) throws ParameterException, GTUException
        {
            return Math.max(0.0,
                    Math.max(parameters.getParameter(LmrsParameters.DLEFT), parameters.getParameter(LmrsParameters.DRIGHT)));
        }
    }

    /** LMRS perception factory with AR. */
    private class LmrsPerceptionFactoryAR extends DefaultLMRSPerceptionFactory
    {
        /** Estimation distribution. */
        private final Distribution<Estimation> estimation;

        /**
         * Constructor.
         * @param estimation Distribution&lt;Estimation&gt;; estimation distribution
         */
        LmrsPerceptionFactoryAR(final Distribution<Estimation> estimation)
        {
            this.estimation = estimation;
        }

        /** {@inheritDoc} */
        @SuppressWarnings("synthetic-access")
        @Override
        public LanePerception generatePerception(final LaneBasedGTU gtu)
        {
            Set<Task> tasksSet = new LinkedHashSet<>();
            if (AnticipationRelianceScript.this.tasks)
            {
                tasksSet.add(new CarFollowingTaskAR());
                tasksSet.add(new LaneChangeTaskAR());
            }

            Set<BehavioralAdaptation> behavioralAdapatations = new LinkedHashSet<>();
            behavioralAdapatations.add(new AdaptationSituationalAwareness());
            if (AnticipationRelianceScript.this.adaptation)
            {
                behavioralAdapatations.add(new AdaptationHeadway());
            }
            LanePerception perception;
            if (AnticipationRelianceScript.this.tasks)
            {
                Fuller fuller = new Fuller(tasksSet, behavioralAdapatations, new TaskManagerAR());
                perception = new CategoricalLanePerception(gtu, fuller);
            }
            else
            {
                perception = new CategoricalLanePerception(gtu);
            }
            perception.addPerceptionCategory(new DirectEgoPerception<>(perception));
            perception.addPerceptionCategory(new DirectInfrastructurePerception(perception));
            Estimation est = Try.assign(() -> this.estimation.draw(), "Probability exception while drawing estimation.");
            perception.addPerceptionCategory(
                    new DirectNeighborsPerception(perception, new PerceivedHeadwayGtuType(est, Anticipation.CONSTANT_SPEED)));
            perception.addPerceptionCategory(new AnticipationTrafficPerception(perception));
            return perception;
        }

        /** {@inheritDoc} */
        @SuppressWarnings("synthetic-access")
        @Override
        public Parameters getParameters() throws ParameterException
        {
            Parameters params = super.getParameters();
            params.setParameter(HEXP, Duration.instantiateSI(4.0));
            params.setParameter(ALPHA, AnticipationRelianceScript.this.alpha);
            params.setParameter(BETA, AnticipationRelianceScript.this.beta);
            params.setParameter(Fuller.TC, 1.0);
            params.setParameter(Fuller.TS_CRIT, 1.0); // Was not changed!
            params.setParameter(Fuller.TS_MAX, 2.0);
            params.setParameter(AdaptationSituationalAwareness.SA, 1.0);
            params.setParameter(AdaptationSituationalAwareness.SA_MAX, 1.0);
            params.setParameter(AdaptationSituationalAwareness.SA_MIN, 0.5);
            params.setParameter(AdaptationSituationalAwareness.TR_MAX, Duration.instantiateSI(2.0));
            params.setParameter(ParameterTypes.TR, Duration.ZERO);
            params.setParameter(AdaptationHeadway.BETA_T, 1.0); // Increase?
            return params;
        }
    }

    /** Task manager for AR. */
    private class TaskManagerAR implements TaskManager
    {
        /** {@inheritDoc} */
        @Override
        public void manage(final Set<Task> tasksMan, final LanePerception perception, final LaneBasedGTU gtu,
                final Parameters parameters) throws ParameterException, GTUException
        {
            Task primary = null;
            Set<Task> auxiliaryTasks = new LinkedHashSet<>();
            for (Task task : tasksMan)
            {
                if (task.getId().equals("lane-changing"))
                {
                    primary = task;
                }
                else
                {
                    auxiliaryTasks.add(task);
                }
            }
            Throw.whenNull(primary, "There is no task with id 'lane-changing'.");
            double primaryTaskDemand = primary.calculateTaskDemand(perception, gtu, parameters);
            primary.setTaskDemand(primaryTaskDemand);
            // max AR is alpha of TD, actual AR approaches 0 for increasing TD
            double a = parameters.getParameter(ALPHA);
            double b = parameters.getParameter(BETA);
            primary.setAnticipationReliance(a * primaryTaskDemand * (1.0 - primaryTaskDemand));
            for (Task auxiliary : auxiliaryTasks)
            {
                double auxiliaryTaskLoad = auxiliary.calculateTaskDemand(perception, gtu, parameters);
                auxiliary.setTaskDemand(auxiliaryTaskLoad);
                // max AR is beta of TD, actual AR approaches 0 as primary TD approaches 0
                auxiliary.setAnticipationReliance(b * auxiliaryTaskLoad * primaryTaskDemand);
            }
        }
    }

    /** Car-following model factory. */
    private class IdmPlusFactoryAR implements CarFollowingModelFactory<IDMPlus>
    {
        /** Generator for desired speed model. */
        private final Generator<DesiredSpeedModel> desiredSpeedModelGenerator;

        /**
         * Constructor.
         * @param desiredSpeedModelGenerator Generator&lt;DesiredSpeedModel&gt;; generator for desired speed model
         */
        IdmPlusFactoryAR(final Generator<DesiredSpeedModel> desiredSpeedModelGenerator)
        {
            this.desiredSpeedModelGenerator = desiredSpeedModelGenerator;
        }

        /** {@inheritDoc} */
        @Override
        public Parameters getParameters() throws ParameterException
        {
            ParameterSet parameters = new ParameterSet();
            parameters.setDefaultParameters(AbstractIDM.class);
            return parameters;
        }

        /** {@inheritDoc} */
        @Override
        public IDMPlus generateCarFollowingModel()
        {
            return new IDMPlus(AbstractIDM.HEADWAY,
                    Try.assign(() -> this.desiredSpeedModelGenerator.draw(), "Unexpected exception."));
        }
    }

    /** Task saturation trajectory data. */
    private class TaskSaturationDataType extends ExtendedDataTypeNumber<GtuData>
    {

        /**
         * Constructor.
         */
        TaskSaturationDataType()
        {
            super("TS");
        }

        /** {@inheritDoc} */
        @Override
        public Float getValue(final GtuData gtu)
        {
            Double ts = gtu.getGtu().getParameters().getParameterOrNull(Fuller.TS);
            if (ts != null)
            {
                return (float) (double) ts;
            }
            return Float.NaN;
        }

    }

    /** Task adaptation reliance trajectory data. */
    private class TaskAnticipationRelianceDataType extends ExtendedDataTypeNumber<GtuData>
    {

        /** Task id. */
        private String taskId;

        /**
         * Constructor.
         * @param taskId String; task id
         */
        TaskAnticipationRelianceDataType(final String taskId)
        {
            super(taskId + "_AR");
            this.taskId = taskId;
        }

        /** {@inheritDoc} */
        @Override
        public Float getValue(final GtuData gtu)
        {
            return (float) ((Fuller) gtu.getGtu().getTacticalPlanner().getPerception().getMental())
                    .getAnticipationReliance(this.taskId);
        }

    }

    /** Task demand trajectory data. */
    private class TaskDemandDataType extends ExtendedDataTypeNumber<GtuData>
    {

        /** Task id. */
        private String taskId;

        /**
         * Constructor.
         * @param taskId String; task id
         */
        TaskDemandDataType(final String taskId)
        {
            super(taskId + "_TD");
            this.taskId = taskId;
        }

        /** {@inheritDoc} */
        @Override
        public Float getValue(final GtuData gtu)
        {
            return (float) ((Fuller) gtu.getGtu().getTacticalPlanner().getPerception().getMental()).getTaskDemand(this.taskId);
        }

    }

    /** Situational awareness trajectory data. */
    private class SituationalAwarenessDataType extends ExtendedDataTypeNumber<GtuData>
    {

        /**
         * Constructor.
         */
        SituationalAwarenessDataType()
        {
            super("SA");
        }

        /** {@inheritDoc} */
        @Override
        public Float getValue(final GtuData gtu)
        {
            Double ts = gtu.getGtu().getParameters().getParameterOrNull(AdaptationSituationalAwareness.SA);
            if (ts != null)
            {
                return (float) (double) ts;
            }
            return Float.NaN;
        }

    }

}
