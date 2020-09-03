package org.opentrafficsim.demo.cacc;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.djunits.Throw;
import org.djunits.unit.DurationUnit;
import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.ValueRuntimeException;
import org.djunits.value.storage.StorageType;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djunits.value.vdouble.vector.FrequencyVector;
import org.djunits.value.vdouble.vector.TimeVector;
import org.djunits.value.vdouble.vector.base.DoubleVector;
import org.djunits.value.vfloat.scalar.FloatDuration;
import org.djutils.cli.CliException;
import org.djutils.cli.CliUtil;
import org.djutils.event.EventInterface;
import org.djutils.event.EventListenerInterface;
import org.djutils.exceptions.Try;
import org.opentrafficsim.base.compressedfiles.CompressionType;
import org.opentrafficsim.base.compressedfiles.Writer;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.animation.gtu.colorer.AccelerationGTUColorer;
import org.opentrafficsim.core.animation.gtu.colorer.IDGTUColorer;
import org.opentrafficsim.core.animation.gtu.colorer.SpeedGTUColorer;
import org.opentrafficsim.core.animation.gtu.colorer.SwitchableGTUColorer;
import org.opentrafficsim.core.distributions.ConstantGenerator;
import org.opentrafficsim.core.distributions.Distribution;
import org.opentrafficsim.core.distributions.Distribution.FrequencyAndObject;
import org.opentrafficsim.core.distributions.ProbabilityException;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.Bezier;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.TemplateGTUType;
import org.opentrafficsim.core.gtu.perception.DirectEgoPerception;
import org.opentrafficsim.core.gtu.perception.EgoPerception;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.parameters.ParameterFactory;
import org.opentrafficsim.core.parameters.ParameterFactoryByType;
import org.opentrafficsim.draw.factory.DefaultAnimationFactory;
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
import org.opentrafficsim.kpi.sampling.meta.FilterDataGtuType;
import org.opentrafficsim.road.gtu.colorer.DesiredSpeedColorer;
import org.opentrafficsim.road.gtu.colorer.FixedColor;
import org.opentrafficsim.road.gtu.colorer.GTUTypeColorer;
import org.opentrafficsim.road.gtu.colorer.ReactionTimeColorer;
import org.opentrafficsim.road.gtu.colorer.SplitColorer;
import org.opentrafficsim.road.gtu.colorer.SynchronizationColorer;
import org.opentrafficsim.road.gtu.colorer.TaskColorer;
import org.opentrafficsim.road.gtu.colorer.TaskSaturationColorer;
import org.opentrafficsim.road.gtu.generator.LaneBasedGTUGenerator;
import org.opentrafficsim.road.gtu.generator.Platoons;
import org.opentrafficsim.road.gtu.generator.od.DefaultGTUCharacteristicsGeneratorOD;
import org.opentrafficsim.road.gtu.generator.od.ODApplier;
import org.opentrafficsim.road.gtu.generator.od.ODApplier.GeneratorObjects;
import org.opentrafficsim.road.gtu.generator.od.ODOptions;
import org.opentrafficsim.road.gtu.generator.od.StrategicalPlannerFactorySupplierOD;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.CategoricalLanePerception;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionFactory;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.AnticipationTrafficPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.DirectInfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.DirectIntersectionPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.Anticipation;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.DirectNeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.Estimation;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.HeadwayGtuType;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.HeadwayGtuType.PerceivedHeadwayGtuType;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTU;
import org.opentrafficsim.road.gtu.lane.perception.mental.AbstractTask;
import org.opentrafficsim.road.gtu.lane.perception.mental.AdaptationHeadway;
import org.opentrafficsim.road.gtu.lane.perception.mental.AdaptationSituationalAwareness;
import org.opentrafficsim.road.gtu.lane.perception.mental.Fuller;
import org.opentrafficsim.road.gtu.lane.perception.mental.Fuller.BehavioralAdaptation;
import org.opentrafficsim.road.gtu.lane.perception.mental.Task;
import org.opentrafficsim.road.gtu.lane.perception.mental.TaskManager;
import org.opentrafficsim.road.gtu.lane.tactical.cacc.CaccController;
import org.opentrafficsim.road.gtu.lane.tactical.cacc.CaccControllerFactory;
import org.opentrafficsim.road.gtu.lane.tactical.cacc.CaccParameters;
import org.opentrafficsim.road.gtu.lane.tactical.cacc.CaccTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.cacc.CaccTacticalPlannerFactory;
import org.opentrafficsim.road.gtu.lane.tactical.cacc.LongitudinalControllerFactory;
import org.opentrafficsim.road.gtu.lane.tactical.cacc.Platoon;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlusFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.DefaultLMRSPerceptionFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.LMRSFactory;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsParameters;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;
import org.opentrafficsim.road.gtu.strategical.od.Categorization;
import org.opentrafficsim.road.gtu.strategical.od.Category;
import org.opentrafficsim.road.gtu.strategical.od.Interpolation;
import org.opentrafficsim.road.gtu.strategical.od.ODMatrix;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlannerFactory;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneDirection;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.OTSRoadNode;
import org.opentrafficsim.road.network.lane.Stripe;
import org.opentrafficsim.road.network.lane.Stripe.Permeable;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;
import org.opentrafficsim.road.network.lane.object.sensor.Detector;
import org.opentrafficsim.road.network.lane.object.sensor.SinkSensor;
import org.opentrafficsim.road.network.sampling.LaneData;
import org.opentrafficsim.road.network.sampling.RoadSampler;
import org.opentrafficsim.road.network.sampling.data.TimeToCollision;
import org.opentrafficsim.swing.graphs.SwingContourPlot;
import org.opentrafficsim.swing.gui.OTSSimulationApplication;
import org.opentrafficsim.swing.script.AbstractSimulationScript;

import nl.tudelft.simulation.dsol.swing.gui.TablePanel;
import nl.tudelft.simulation.jstats.streams.StreamInterface;
import picocli.CommandLine;
import picocli.CommandLine.Option;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 17 okt. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class CaccSimulationAdv extends AbstractSimulationScript
{

    /** ...  */
    private static final long serialVersionUID = 1L;

    /** Sampler for statistics. */
    private RoadSampler sampler;

    /** Time to collision data type. */
    private final TimeToCollision ttc = new TimeToCollision();

    /** Meta GTU type. */
    private final FilterDataGtuType metaGtu = new FilterDataGtuType();

    /** The CACC controller. */
    private CaccController caccController;
    
    /** The GTU type with CACC. */
    private GTUType caccGTUType;

    /** Space time regions to sample traffic on. */
    private final List<SpaceTimeRegion> regions = new ArrayList<>();

    /** List of lane for the graphs. */
    private List<Lane> graphLanes = new ArrayList<>();

    // Options
    
    /** Network name. */
//    @Option(names = "--nn", description = "Network name", defaultValue = "onramp")
//    private String networkName;

    /** Car-following task parameter. */
    @Option(names = "--hExp", description = "Exponential decay of car-following task by headway", defaultValue = "4.0 s")
    private Duration hExp;
    // TODO add check that hExp is positive

    /** Alpha. */
    @Option(names = "--alpha", description = "Fraction of primary task that can be reduced by anticipation reliance",
            defaultValue = "0.8")
    private double alpha;
    // TODO add check that alpha is in interval [-1 .. +1]

    /** Beta. */
    @Option(names = "--beta", description = "Fraction of auxiliary tasks that can be reduced by anticipation reliance",
            defaultValue = "0.6")
    private double beta;
    // TODO add check that beta is in interval [-1 .. +1]

    /** Network name. */
    @Option(names = "--nn", description = "Network name", defaultValue = "onramp")
    private String networkName;

    /** Intensity factor. */
    @Option(names = "--intensity", description = "Intensity factor", defaultValue = "1.00")
    private double intensity;

    /** Penetration. */
    @Option(names = "--penetration", description = "Fraction of vehicles equipped with CACC", defaultValue = "1.00")
    private double penetration;
    
    /** Platoon size. */
    @Option(names = "--platoon", description = "Platoon size", defaultValue = "3")
    private int platoonSize;
    
    /** Headway. */
    @Option(names = "--headway", description = "Platoon headway", defaultValue = "0.9")
    private double headway;
    
    /** Synchronization. */
    @Option(names = "--synchronization", description = "Synchronization", defaultValue = "0.0")
    private double synchronization;
    
    /** Free flow speed of platoon vehicles. */
    @Option(names = "--setspeed", description = "Free flow speed of platoon vehicles", defaultValue = "80 km/h")
    private Speed setspeed;
    
    /** Time step. */
    @Option(names = "--sd", description = "Simulation time", defaultValue = "4500s")
    private Duration simulationDuration;
    
    /** Show graphs. */
    @Option(names = "--graphs", description = "Show graphs", defaultValue = "true")
    private boolean graphs;
    
    /** Number of lanes. */
    @Option(names = "--nol", description = "Number of lanes", defaultValue = "3")
    private int numberOfLanes;
    
    /** Controller. */
    @Option(names = "--controller", description = "Controller", defaultValue = "CACC")
    private String controller;
    
    /** Output file for detections (?). */
    @Option(names = "--outputFileDets", description = "Output file for detections", 
            defaultValue = "C:\\\\Temp\\\\Journal-Platoon\\\\DetsDefault.csv")
    private String outputFileDets;
    
    /** Output file for trajectories (?). */
    @Option(names = "--outputFileTraj", description = "Output file for trajectories",
            defaultValue = "C:\\\\Temp\\\\Journal-Platoon\\\\GenDefault")
    private String outputFileTraj;
    
    /** Output file for Generators (?). */
    @Option(names = "--outputFileGen", description = "Output file for generators", 
            defaultValue = "C:\\Temp\\Journal-Platoon\\GenDefault")
    private String outputFileGen;
    
    /** Strategies. */
    @Option(names = "--strategies", description = "Strategies (boolean)", defaultValue = "false")
    private boolean strategies;

    /** Adaptation. */
    @Option(names = "--adaptation", description = "Adaptation (boolean)", defaultValue = "false")
    private boolean adaptation;

    /** Tasks. */
    @Option(names = "--tasks", description = "Tasks (boolean)", defaultValue = "true")
    private boolean tasks;

    /** Fraction underestimation. */
    @Option(names = "--fractionUnderestimation", description = "Fraction underestimation", defaultValue = "0.75")
    private double fractionUnderestimation;
    
    /** Equilibrium distance. */
    @Option(names = "--startSpacing", description = "Gross equilibrium distance at 25m/s with s0=3m, l=12m and T=0.3s",
            defaultValue = "0.5")
    private double startSpacing;

    /**
     * @param properties x
     * @throws CliException x 
     * @throws NoSuchFieldException x 
     */
    protected CaccSimulationAdv(final String[] properties) throws NoSuchFieldException, CliException
    {
        super("Truck CACC", "Simulation of CACC trucks");
        CommandLine cmd = new CommandLine(this);
        CliUtil.changeOptionDefault(AbstractSimulationScript.class, "seed", "5");
        CliUtil.execute(cmd, properties);
    }

    /**
     * Main method that creates and starts a simulation.
     * @param args String[]; command line arguments
     * @throws Exception x
     * @throws CliException x
     * @throws NoSuchFieldException x 
     */
    public static void main(final String[] args) throws NoSuchFieldException, CliException, Exception
    {
        new CaccSimulationAdv(args).start();
    }

    /** {@inheritDoc} */
    @Override
    protected OTSRoadNetwork setupSimulation(final OTSSimulatorInterface sim) throws Exception
    {
        OTSRoadNetwork network = new OTSRoadNetwork(this.networkName, true, sim);
        GTUType carGTUType = network.getGtuType(GTUType.DEFAULTS.CAR);
        GTUType truckGTUType = network.getGtuType(GTUType.DEFAULTS.TRUCK);
        this.caccGTUType = new GTUType("CACC", truckGTUType);
        LinkType freewayLinkType = network.getLinkType(LinkType.DEFAULTS.FREEWAY);
        LaneType freewayLaneType = network.getLaneType(LaneType.DEFAULTS.FREEWAY);
        GTUType vehicle = network.getGtuType(GTUType.DEFAULTS.VEHICLE);
        this.caccController = new CaccController();
        this.caccController.setCACCGTUType(this.caccGTUType);
        setGtuColorer(new SwitchableGTUColorer(0, new FixedColor(Color.BLUE, "Blue"),
                new GTUTypeColorer().add(carGTUType, Color.BLUE).add(truckGTUType, Color.RED).add(this.caccGTUType,
                        Color.GREEN),
                (new TaskColorer("car-following")), (new TaskColorer("lane-changing")), (new TaskSaturationColorer()),
                (new ReactionTimeColorer(Duration.instantiateSI(1.0))), new IDGTUColorer(),
                new SpeedGTUColorer(new Speed(150, SpeedUnit.KM_PER_HOUR)),
                new DesiredSpeedColorer(new Speed(50, SpeedUnit.KM_PER_HOUR), new Speed(150, SpeedUnit.KM_PER_HOUR)),
                new AccelerationGTUColorer(Acceleration.instantiateSI(-6.0), Acceleration.instantiateSI(2)), new SplitColorer(),
                new SynchronizationColorer(), new TaskColorer("car-following")));

        // Factory for settable parameters
        ParameterFactoryByType parameters = new ParameterFactoryByType();
        // Parameters
        parameters.addParameter(CaccParameters.T_SYSTEM_CACC, new Duration(this.headway, DurationUnit.SI));
        parameters.addParameter(CaccParameters.A_REDUCED, Acceleration.instantiateSI(this.synchronization));
        parameters.addParameter(CaccParameters.SET_SPEED, this.setspeed);

        CaccControllerFactory longitudinalControllerFactory;
        String controllerName = this.controller;
        if (controllerName.equals("CACC"))
        {
            longitudinalControllerFactory = new CaccControllerFactory();
        }
        else
        {
            throw new RuntimeException("Controller " + controllerName + " not supported.");
        }

        Length laneWidth = Length.instantiateSI(3.5);
        Length stripeWidth = Length.instantiateSI(0.2);
        Map<String, GeneratorObjects> odApplierOutput;

        String platoonDetector1;

        if (networkName.equals("onramp"))
        {
            // points
            OTSPoint3D pointA = new OTSPoint3D(0, 0);
            OTSPoint3D pointB = new OTSPoint3D(2000, 0); // 700 meters toerit
            OTSPoint3D pointC = new OTSPoint3D(2330, 0); // 330 meters onramp
            OTSPoint3D pointD = new OTSPoint3D(3330, 0);
            OTSPoint3D pointE = new OTSPoint3D(1300, -40);

            // nodes
            OTSRoadNode nodeA = new OTSRoadNode(network, "A", pointA, Direction.ZERO);
            OTSRoadNode nodeB = new OTSRoadNode(network, "B", pointB, Direction.ZERO);
            OTSRoadNode nodeC = new OTSRoadNode(network, "C", pointC, Direction.ZERO);
            OTSRoadNode nodeD = new OTSRoadNode(network, "D", pointD, Direction.ZERO);
            OTSRoadNode nodeE = new OTSRoadNode(network, "E", pointE, Direction.ZERO);

            // links
            CrossSectionLink linkAB = new CrossSectionLink(network, "AB", nodeA, nodeB, freewayLinkType,
                    new OTSLine3D(pointA, pointB), LaneKeepingPolicy.KEEPRIGHT);
            CrossSectionLink linkBC = new CrossSectionLink(network, "BC", nodeB, nodeC, freewayLinkType,
                    new OTSLine3D(pointB, pointC), LaneKeepingPolicy.KEEPRIGHT);
            CrossSectionLink linkCD = new CrossSectionLink(network, "CD", nodeC, nodeD, freewayLinkType,
                    new OTSLine3D(pointC, pointD), LaneKeepingPolicy.KEEPRIGHT);
            CrossSectionLink linkEB = new CrossSectionLink(network, "EB", nodeE, nodeB, freewayLinkType,
                    Bezier.cubic(nodeE.getLocation(), nodeB.getLocation()), LaneKeepingPolicy.KEEPRIGHT);

            // lanes and stripes
            int n = this.numberOfLanes;
            // List<Lane> originLanes = new ArrayList<>();
            for (int i = 0; i < n; i++)
            {
                for (CrossSectionLink link : new CrossSectionLink[] {linkAB, linkBC, linkCD})
                {
                    Lane lane = new Lane(link, "Lane " + (i + 1), laneWidth.times((0.5 + i)), laneWidth, freewayLaneType,
                            new Speed(100, SpeedUnit.KM_PER_HOUR));
                    Length offset = laneWidth.times(i + 1.0);
                    Stripe stripe = new Stripe(link, offset, offset, stripeWidth);
                    if (i < n - 1)
                    {
                        if (lane.getParentLink().getId().equals("BC"))
                        {
                            // stripe.addPermeability(GTUType.VEHICLE, Permeable.LEFT);
                            stripe.addPermeability(vehicle, Permeable.BOTH);
                        }
                        else
                        {
                            stripe.addPermeability(vehicle, Permeable.BOTH);
                        }
                    }
                    if (lane.getParentLink().getId().equals("BC"))
                    {
                        // new Detector(lane.getFullId(), lane, Length.instantiateSI(150.0), sim);
                        new Detector(lane.getFullId(), lane, Length.instantiateSI(330.0), sim);
                    }
                    // sink sensors
                    if (lane.getParentLink().getId().equals("CD"))
                    {
                        new SinkSensor(lane, lane.getLength().minus(Length.instantiateSI(100.0)), GTUDirectionality.DIR_PLUS,
                                sim);
                        new SinkSensor(lane, lane.getLength().minus(Length.instantiateSI(90.0)), GTUDirectionality.DIR_PLUS,
                                sim);
                        new SinkSensor(lane, lane.getLength().minus(Length.instantiateSI(80.0)), GTUDirectionality.DIR_PLUS,
                                sim);
                        // detectors 100m after on ramp
                        // new Detector(lane.getFullId(), lane, Length.instantiateSI(0.0), sim); // id equal to lane, may be
                        // different
                    }
                    if (lane.getParentLink().getId().equals("AB"))
                    {
                        // originLanes.add(lane);
                        this.graphLanes.add(lane);
                    }
                }
            }
            new Stripe(linkAB, Length.ZERO, Length.ZERO, stripeWidth);
            Stripe stripe = new Stripe(linkBC, Length.ZERO, Length.ZERO, stripeWidth);
            stripe.addPermeability(vehicle, Permeable.LEFT);
            new Stripe(linkCD, Length.ZERO, Length.ZERO, stripeWidth);
            new Lane(linkBC, "Acceleration lane", laneWidth.times(-0.5), laneWidth, freewayLaneType,
                    new Speed(100, SpeedUnit.KM_PER_HOUR));
            Lane onramp = new Lane(linkEB, "Onramp", laneWidth.times(-0.5), laneWidth, freewayLaneType,
                    new Speed(100, SpeedUnit.KM_PER_HOUR));
            new Stripe(linkEB, Length.ZERO, Length.ZERO, stripeWidth);
            new Stripe(linkEB, laneWidth.neg(), laneWidth.neg(), stripeWidth);
            new Stripe(linkBC, laneWidth.neg(), laneWidth.neg(), stripeWidth);

            // Detector on onramp
            // new Detector("Acceleration lane", accel, Length.createSI(200.0), sim);

            // OD without demand
            List<OTSRoadNode> origins = new ArrayList<>();
            origins.add(nodeA);
            origins.add(nodeE);
            List<OTSRoadNode> destinations = new ArrayList<>();
            destinations.add(nodeD);
            TimeVector timeVector = DoubleVector.instantiate(new double[] {0.0, 0.25, 0.50, 0.75, 1.0, 1.25, 1.50, 1.75, 2.0},
                    TimeUnit.BASE_HOUR, StorageType.DENSE);
            Interpolation interpolation = Interpolation.LINEAR; // or STEPWISE
            Categorization categorization = new Categorization("CACC", GTUType.class, Lane.class);
            // Category carCategory1 = new Category(categorization, GTUType.CAR, originLanes.get(1));
            Category carCategory2 = new Category(categorization, carGTUType, this.graphLanes.get(2));
            Category carCategory1 = new Category(categorization, carGTUType, this.graphLanes.get(1));
            Category carCategory0 = new Category(categorization, carGTUType, this.graphLanes.get(0));
            Category carCategoryR = new Category(categorization, carGTUType, onramp);
            Category truCategory0 = new Category(categorization, truckGTUType, this.graphLanes.get(0));
            Category truCategoryR = new Category(categorization, truckGTUType, onramp);
            Category caccCategory = new Category(categorization, caccGTUType, this.graphLanes.get(0));
            ODMatrix odMatrix = new ODMatrix("CACC OD", origins, destinations, categorization, timeVector, interpolation);

            double intensityIncrease = this.intensity;
            double platoonPenetration = this.penetration;

            float[] mainlineDemand = new float[] {2584, 3028, 3228, 3510, 3228, 0};
            // float[] mainlineDemand = new float[]{2584, 3028, 3528, 3900, 3428, 0};

            // float[] rampDemand = new float[]{2900, 2900, 3068, 3532, 3232, 0};

            // Demand of trucks departing from main road20
            List<Double> demandList = new ArrayList<Double>();
            demandList.add((double) mainlineDemand[0] * 0.15 * intensityIncrease);
            demandList.add((double) mainlineDemand[1] * 0.15 * intensityIncrease);
            demandList.add(mainlineDemand[2] * 0.15 * intensityIncrease);
            demandList.add(mainlineDemand[3] * 0.15 * intensityIncrease);
            demandList.add(mainlineDemand[4] * 0.15 * intensityIncrease);
            demandList.add(mainlineDemand[5] * 0.15 * intensityIncrease);
            demandList.add(mainlineDemand[5] * 0.15 * intensityIncrease);
            demandList.add(mainlineDemand[5] * 0.15 * intensityIncrease);
            demandList.add((double) 0);
            demandList.add((double) 0);
            List<Double> demandPlatoon = new ArrayList<Double>(); // List for number of platoons (not number of platoon
                                                                  // vehicles!)
            List<Double> demandPlatoonVehicles = new ArrayList<Double>(); // Demand for platoon vehicles

            for (int k = 0; k < demandList.size(); k++)
            {
                double platoonVehicles = (demandList.get(k) * platoonPenetration);
                double platoonPlatoons = (Math.round(platoonVehicles / platoonSize)) / 4; // Actual generated platoons -> divide
                                                                                          // by 4 to account for 15 minutes
                double newDemandVal = Math.max(demandList.get(k) - platoonVehicles, 0);
                demandList.set(k, newDemandVal);
                demandPlatoon.add(platoonPlatoons);
                platoonVehicles = platoonPlatoons * platoonSize * 4; // Based on actual generated platoons, but used to
                                                                     // compensate per hour
                demandPlatoonVehicles.add(platoonVehicles);
            }

            // Platoon definition (platoons = ... divide into multiple for multiple origins)
            Set<LaneDirection> position = new LinkedHashSet<>();
            position.add(new LaneDirection(this.graphLanes.get(0), GTUDirectionality.DIR_PLUS));
            DefaultGTUCharacteristicsGeneratorOD characteristicsGenerator =
                    getCharacteristicsGenerator(longitudinalControllerFactory, sim, parameters);
            Platoons<Category> platoons =
                    Platoons.ofCategory(characteristicsGenerator, sim, sim.getReplication().getStream("generation"), position);

            platoons.fixInfo(nodeA, nodeD, caccCategory);

            double dt = this.startSpacing;

            // Determine platoon generation times (of leading vehicle only) using pseudorandom generation (exponential
            // distribution)
            // Then check if interval times do not overlap with whole platoon generation, adjust accordingly
            List<Double> generationTimes = new ArrayList<Double>();
            double previous = 0;

            for (int k = 0; k < demandList.size(); k++)
            {
                for (int i = 0; i < demandPlatoon.get(k); i++)
                {
                    double lambda = (demandPlatoon.get(k) * 4); // Number of platoons to be generated (per hour -> times 4)
                    StreamInterface rand = sim.getReplication().getStream("generation");
                    // Random rand = new Random();
                    double arrival = (Math.log(1 - rand.nextDouble()) / (-lambda) * 3600); // Inter arrival time is in the unit
                                                                                           // of lambda (here per 15 minutes)

                    double startTime = (arrival + previous); // (k * 900) + (arrival + previous); // Interarrival plus 15
                                                             // minutes (900 seconds)
                    generationTimes.add(startTime);
                    previous = previous + arrival;
                }
            }

            // Sorting the generation time to check for overlap in generation
            Collections.sort(generationTimes);

            for (int i = 0; i < generationTimes.size() - 1; i++)
            {
                double diff = generationTimes.get(i + 1) - generationTimes.get(i);
                double generationDuration = 0.5;
                double maxDuration = (generationDuration * platoonSize) + 1.0;
                if (diff <= maxDuration)
                {
                    generationTimes.set(i + 1, generationTimes.get(i) + maxDuration);
                }

                // ((platoonSize-1)*((12.0 + 3.0)/22.22) + 0.3) + ( (12.0 + 3.0)/22.22 + 1.0);
                double endTime = generationTimes.get(i) + (generationDuration * platoonSize);
                platoons.addPlatoon(Time.instantiateSI(generationTimes.get(i)), Time.instantiateSI(endTime));

                for (double t = generationTimes.get(i); t < endTime; t += dt)
                {
                    platoons.addGtu(Time.instantiateSI(t));
                }
            }

            // OD demand
            // cars (without compensation we use fraction 0.5 in putDemandVector, otherwise we multiply by laneshare)
            odMatrix.putDemandVector(nodeA, nodeD, carCategory2,
                    freq(new double[] {mainlineDemand[0] * 0.85 * (intensityIncrease),
                            mainlineDemand[1] * 0.85 * (intensityIncrease), mainlineDemand[2] * 0.85 * (intensityIncrease),
                            mainlineDemand[3] * 0.85 * (intensityIncrease), mainlineDemand[4] * 0.85 * (intensityIncrease),
                            mainlineDemand[5] * 0.85 * (intensityIncrease), mainlineDemand[5] * 0.85 * (intensityIncrease), 0,
                            0}),
                    timeVector, interpolation, 0.26);
            odMatrix.putDemandVector(nodeA, nodeD, carCategory1,
                    freq(new double[] {mainlineDemand[0] * 0.85 * (intensityIncrease),
                            mainlineDemand[1] * 0.85 * (intensityIncrease), mainlineDemand[2] * 0.85 * (intensityIncrease),
                            mainlineDemand[3] * 0.85 * (intensityIncrease), mainlineDemand[4] * 0.85 * (intensityIncrease),
                            mainlineDemand[5] * 0.85 * (intensityIncrease), mainlineDemand[5] * 0.85 * (intensityIncrease), 0,
                            0}),
                    timeVector, interpolation, 0.32);
            odMatrix.putDemandVector(nodeA, nodeD, carCategory0,
                    platoons.compensate(carCategory0, freq(new double[] {mainlineDemand[0] * 0.85 * (intensityIncrease),
                            mainlineDemand[1] * 0.85 * (intensityIncrease), mainlineDemand[2] * 0.85 * (intensityIncrease),
                            mainlineDemand[3] * 0.85 * (intensityIncrease), mainlineDemand[4] * 0.85 * (intensityIncrease),
                            mainlineDemand[5] * 0.85 * (intensityIncrease), mainlineDemand[5] * 0.85 * (intensityIncrease), 0,
                            0}), timeVector, interpolation),
                    timeVector, interpolation, 0.32);
            odMatrix.putDemandVector(nodeE, nodeD, carCategoryR,
                    freq(new double[] {mainlineDemand[0] * 0.85 * 0.25 * (intensityIncrease),
                            mainlineDemand[1] * 0.85 * 0.25 * (intensityIncrease),
                            mainlineDemand[2] * 0.85 * 0.25 * (intensityIncrease),
                            mainlineDemand[3] * 0.85 * 0.25 * (intensityIncrease),
                            mainlineDemand[4] * 0.85 * 0.25 * (intensityIncrease),
                            mainlineDemand[5] * 0.85 * 0.25 * (intensityIncrease),
                            mainlineDemand[5] * 0.85 * 0.25 * (intensityIncrease), 0, 0}));
            // trucks
            odMatrix.putDemandVector(nodeE, nodeD, truCategoryR,
                    freq(new double[] {mainlineDemand[0] * 0.15 * 0.25 * (intensityIncrease),
                            mainlineDemand[1] * 0.15 * 0.25 * (intensityIncrease),
                            mainlineDemand[2] * 0.15 * 0.25 * (intensityIncrease),
                            mainlineDemand[3] * 0.15 * 0.25 * (intensityIncrease),
                            mainlineDemand[4] * 0.15 * 0.25 * (intensityIncrease),
                            mainlineDemand[5] * 0.15 * 0.25 * (intensityIncrease),
                            mainlineDemand[5] * 0.15 * 0.25 * (intensityIncrease), 0, 0}));
            // cacc trucks & compensated normal trucks - if there are no, or only, cacc vehicles -> remove demand
            if (this.penetration != 1.0)
            {
                odMatrix.putDemandVector(nodeA, nodeD, truCategory0, platoons.compensate(truCategory0,
                        freq(new double[] {demandList.get(0), demandList.get(1), demandList.get(2), demandList.get(3),
                                demandList.get(4), demandList.get(5), demandList.get(6), demandList.get(7), demandList.get(8)}),
                        timeVector, interpolation));
            }
            if (this.penetration != 0.0)
            {
                odMatrix.putDemandVector(nodeA, nodeD, caccCategory,
                        platoons.compensate(caccCategory, freq(new double[] {demandPlatoonVehicles.get(0),
                                demandPlatoonVehicles.get(1), demandPlatoonVehicles.get(2), demandPlatoonVehicles.get(3),
                                demandPlatoonVehicles.get(4), demandPlatoonVehicles.get(5), demandPlatoonVehicles.get(6),
                                demandPlatoonVehicles.get(7), demandPlatoonVehicles.get(8)}), timeVector, interpolation));
            }
            // options
            ODOptions odOptions = new ODOptions().set(ODOptions.NO_LC_DIST, Length.instantiateSI(300.0)).set(ODOptions.GTU_TYPE,
                    characteristicsGenerator);
            odApplierOutput = ODApplier.applyOD(network, odMatrix, odOptions);

            // start platoons
            platoonDetector1 = "A1";
            platoons.start(odApplierOutput.get(platoonDetector1).getGenerator());

            // Write platoon generation times to file
            BufferedWriter bw;
            bw = new BufferedWriter(
                    new OutputStreamWriter(Writer.createOutputStream(this.outputFileGen, CompressionType.NONE)));
            bw.write(String.format("Platoon generation times [s]: %s", generationTimes));
            bw.close();
        }
        else if (networkName.equals("onrampMERGE"))
        {
            // points
            OTSPoint3D pointA = new OTSPoint3D(0, 0);
            OTSPoint3D pointB = new OTSPoint3D(2000, 0); // 700 meters toerit (1300 - 2000)
            OTSPoint3D pointC = new OTSPoint3D(2330, 0); // 330 meters onramp
            OTSPoint3D pointD = new OTSPoint3D(3330, 0);
            OTSPoint3D pointE = new OTSPoint3D(1300, -40);

            // nodes
            OTSRoadNode nodeA = new OTSRoadNode(network, "A", pointA, Direction.ZERO);
            OTSRoadNode nodeB = new OTSRoadNode(network, "B", pointB, Direction.ZERO);
            OTSRoadNode nodeC = new OTSRoadNode(network, "C", pointC, Direction.ZERO);
            OTSRoadNode nodeD = new OTSRoadNode(network, "D", pointD, Direction.ZERO);
            OTSRoadNode nodeE = new OTSRoadNode(network, "E", pointE, Direction.ZERO);

            // links
            CrossSectionLink linkAB = new CrossSectionLink(network, "AB", nodeA, nodeB, freewayLinkType,
                    new OTSLine3D(pointA, pointB), LaneKeepingPolicy.KEEPRIGHT);
            CrossSectionLink linkBC = new CrossSectionLink(network, "BC", nodeB, nodeC, freewayLinkType,
                    new OTSLine3D(pointB, pointC), LaneKeepingPolicy.KEEPRIGHT);
            CrossSectionLink linkCD = new CrossSectionLink(network, "CD", nodeC, nodeD, freewayLinkType,
                    new OTSLine3D(pointC, pointD), LaneKeepingPolicy.KEEPRIGHT);
            CrossSectionLink linkEB = new CrossSectionLink(network, "EB", nodeE, nodeB, freewayLinkType,
                    Bezier.cubic(nodeE.getLocation(), nodeB.getLocation()), LaneKeepingPolicy.KEEPRIGHT);

            // lanes and stripes
            int n = this.numberOfLanes;
            // List<Lane> originLanes = new ArrayList<>();
            for (int i = 0; i < n; i++)
            {
                for (CrossSectionLink link : new CrossSectionLink[] {linkAB, linkBC, linkCD})
                {
                    Lane lane = new Lane(link, "Lane " + (i + 1), laneWidth.times((0.5 + i)), laneWidth, freewayLaneType,
                            new Speed(100, SpeedUnit.KM_PER_HOUR));
                    Length offset = laneWidth.times(i + 1.0);
                    Stripe stripe = new Stripe(link, offset, offset, stripeWidth);
                    if (i < n - 1)
                    {
                        if (lane.getParentLink().getId().equals("BC"))
                        {
                            // stripe.addPermeability(GTUType.VEHICLE, Permeable.LEFT);
                            stripe.addPermeability(vehicle, Permeable.BOTH);

                        }
                        else
                        {
                            stripe.addPermeability(vehicle, Permeable.BOTH);
                        }
                    }
                    if (lane.getParentLink().getId().equals("BC"))
                    {
                        new Detector(lane.getFullId(), lane, Length.instantiateSI(330.0), sim);
                    }
                    // sink sensors
                    if (lane.getParentLink().getId().equals("CD"))
                    {
                        new SinkSensor(lane, lane.getLength().minus(Length.instantiateSI(100.0)), GTUDirectionality.DIR_PLUS,
                                sim);
                        // detectors 0m after on ramp
                        // new Detector(lane.getFullId(), lane, Length.createSI(0.0), GTUDirectionality.DIR_PLUS, sim); 
                        // id equal to lane, may be different
                    }
                    if (lane.getParentLink().getId().equals("AB"))
                    {
                        // originLanes.add(lane);
                        this.graphLanes.add(lane);
                    }
                }
            }
            new Stripe(linkAB, Length.ZERO, Length.ZERO, stripeWidth);
            Stripe stripe = new Stripe(linkBC, Length.ZERO, Length.ZERO, stripeWidth);
            stripe.addPermeability(vehicle, Permeable.LEFT);
            new Stripe(linkCD, Length.ZERO, Length.ZERO, stripeWidth);
            new Lane(linkBC, "Acceleration lane", laneWidth.times(-0.5), laneWidth, freewayLaneType,
                    new Speed(100, SpeedUnit.KM_PER_HOUR));
            Lane onramp = new Lane(linkEB, "Onramp", laneWidth.times(-0.5), laneWidth, freewayLaneType,
                    new Speed(100, SpeedUnit.KM_PER_HOUR));
            new Stripe(linkEB, Length.ZERO, Length.ZERO, stripeWidth);
            new Stripe(linkEB, laneWidth.neg(), laneWidth.neg(), stripeWidth);
            new Stripe(linkBC, laneWidth.neg(), laneWidth.neg(), stripeWidth);

            // OD without demand
            List<OTSRoadNode> origins = new ArrayList<>();
            origins.add(nodeA);
            origins.add(nodeE);
            List<OTSRoadNode> destinations = new ArrayList<>();
            destinations.add(nodeD);
            TimeVector timeVector = DoubleVector.instantiate(new double[] {0.0, 0.25, 0.50, 0.75, 1.0, 1.25, 1.50, 1.75, 2.0},
                    TimeUnit.BASE_HOUR, StorageType.DENSE);
            Interpolation interpolation = Interpolation.LINEAR; // or STEPWISE
            Categorization categorization = new Categorization("CACC", GTUType.class, Lane.class);
            Category carCategory1 = new Category(categorization, carGTUType, this.graphLanes.get(1));
            Category carCategory0 = new Category(categorization, carGTUType, this.graphLanes.get(0));
            Category carCategoryR = new Category(categorization, carGTUType, onramp);
            Category truCategory0 = new Category(categorization, truckGTUType, this.graphLanes.get(0));
            Category truCategoryR = new Category(categorization, truckGTUType, onramp);
            Category caccCategory = new Category(categorization, caccGTUType, onramp);
            ODMatrix odMatrix = new ODMatrix("CACC OD", origins, destinations, categorization, timeVector, interpolation);

            double intensityIncrease = this.intensity;
            double platoonPenetration = this.penetration;

            // Demand of trucks departing from onramp
            List<Double> demandList = new ArrayList<Double>();
            demandList.add((double) 0);
            demandList.add((double) 0);
            demandList.add(45 * (1 + intensityIncrease));
            demandList.add(45 * (1 + intensityIncrease));
            demandList.add(45 * (1 + intensityIncrease));
            demandList.add(45 * (1 + intensityIncrease));
            demandList.add(45 * (1 + intensityIncrease));

            demandList.add((double) 0);
            demandList.add((double) 0);
            List<Double> demandPlatoon = new ArrayList<Double>(); // List for number of platoons (not number of platoon
                                                                  // vehicles!)
            List<Double> demandPlatoonVehicles = new ArrayList<Double>(); // Demand for platoon vehicles

            for (int k = 0; k < demandList.size(); k++)
            {
                double platoonVehicles = (demandList.get(k) * platoonPenetration);
                // double platoonPlatoons = Math.round(platoonVehicles/platoonSize)/4; // Actual generated platoons -> divide by
                // 4 to account for 15 minutes
                double platoonPlatoons = Math.ceil(Math.abs(platoonVehicles / platoonSize)) / 4; // Rounding up
                double newDemandVal = Math.max(demandList.get(k) - platoonVehicles, 0);
                demandList.set(k, newDemandVal);
                demandPlatoon.add(platoonPlatoons);
                platoonVehicles = platoonPlatoons * platoonSize * 4; // Based on actual generated platoons, but used to
                                                                     // compensate per hour
                demandPlatoonVehicles.add(platoonVehicles);
            }

            // Platoon definition (platoons = ... divide into multiple for multiple origins)
            Set<LaneDirection> position = new LinkedHashSet<>();
            position.add(new LaneDirection(onramp, GTUDirectionality.DIR_PLUS));
            DefaultGTUCharacteristicsGeneratorOD characteristicsGenerator =
                    getCharacteristicsGenerator(longitudinalControllerFactory, sim, parameters);
            Platoons<Category> platoons =
                    Platoons.ofCategory(characteristicsGenerator, sim, sim.getReplication().getStream("generation"), position);

            platoons.fixInfo(nodeE, nodeD, caccCategory);

            double dt = this.startSpacing;

            // Determine platoon generation times (of leading vehicle only) using pseudorandom generation (exponential
            // distribution)
            // Then check if interval times do not overlap with whole platoon generation, adjust accordingly
            List<Double> generationTimes = new ArrayList<Double>();
            double previous = 0;

            for (int k = 0; k < demandList.size(); k++)
            {
                for (int i = 0; i < demandPlatoon.get(k); i++)
                {
                    double lambda = (demandPlatoon.get(k) * 4); // Number of platoons to be generated (per hour -> times 4)
                    StreamInterface rand = sim.getReplication().getStream("generation");
                    // Random rand = new Random();
                    double arrival = (Math.log(1 - rand.nextDouble()) / (-lambda) * 3600); // Inter arrival time is in the unit
                                                                                           // of lambda (here per 15 minutes)

                    double startTime = (arrival + previous); // (k * 900) + (arrival + previous); // Interarrival plus 15
                                                             // minutes (900 seconds)
                    generationTimes.add(startTime);
                    previous = previous + arrival;
                }
            }

            // Sorting the generation time to check for overlap in generation
            Collections.sort(generationTimes);

            for (int i = 0; i < generationTimes.size() - 1; i++)
            {
                double diff = generationTimes.get(i + 1) - generationTimes.get(i);
                double generationDuration = 0.5;
                double maxDuration = (generationDuration * platoonSize) + 1.0;
                if (diff <= maxDuration)
                {
                    generationTimes.set(i + 1, generationTimes.get(i) + maxDuration);
                }

                // ((platoonSize-1)*((12.0 + 3.0)/22.22) + 0.3) + ( (12.0 + 3.0)/22.22 + 1.0);
                double endTime = generationTimes.get(i) + (generationDuration * platoonSize);
                platoons.addPlatoon(Time.instantiateSI(generationTimes.get(i)), Time.instantiateSI(endTime));

                for (double t = generationTimes.get(i); t < endTime; t += dt)
                {
                    platoons.addGtu(Time.instantiateSI(t));
                }
            }
            // OD demand
            // cars (without compensation we use fraction 0.5 in putDemandVector, otherwise we multiply by laneshare)
            odMatrix.putDemandVector(nodeA, nodeD, carCategory1,
                    freq(new double[] {0, 0, 1000 * (1 + intensityIncrease), 1000 * (1 + intensityIncrease),
                            1000 * (1 + intensityIncrease), 1000 * (1 + intensityIncrease), 1000 * (1 + intensityIncrease),

                            0, 0}),
                    timeVector, interpolation, 0.5);
            odMatrix.putDemandVector(nodeA, nodeD, carCategory0,
                    freq(new double[] {0, 0, 1000 * (1 + intensityIncrease), 1000 * (1 + intensityIncrease),
                            1000 * (1 + intensityIncrease), 1000 * (1 + intensityIncrease), 1000 * (1 + intensityIncrease),

                            0, 0}),
                    timeVector, interpolation, 0.5);
            odMatrix.putDemandVector(nodeE, nodeD, carCategoryR,
                    platoons.compensate(carCategoryR,
                            freq(new double[] {0, 0, 250 * (1 + intensityIncrease), 250 * (1 + intensityIncrease),
                                    250 * (1 + intensityIncrease), 250 * (1 + intensityIncrease), 250 * (1 + intensityIncrease),

                                    0, 0}),
                            timeVector, interpolation));
            // trucks
            odMatrix.putDemandVector(nodeA, nodeD, truCategory0,
                    platoons.compensate(truCategory0,
                            freq(new double[] {0, 0, 180 * (1 + intensityIncrease), 180 * (1 + intensityIncrease),
                                    180 * (1 + intensityIncrease), 180 * (1 + intensityIncrease), 180 * (1 + intensityIncrease),

                                    0, 0}),
                            timeVector, interpolation));
            // cacc trucks & compensated normal trucks - if there are no, or only, cacc vehicles -> remove demand
            if (this.penetration != 1.0)
            {
                odMatrix.putDemandVector(nodeE, nodeD, truCategoryR,
                        freq(new double[] {demandList.get(0), demandList.get(1), demandList.get(2), demandList.get(3),
                                demandList.get(4), demandList.get(5), demandList.get(6), demandList.get(7), demandList.get(8)}),
                        timeVector, interpolation);
            }
            if (this.penetration != 0.0)
            {
                odMatrix.putDemandVector(nodeE, nodeD, caccCategory,
                        platoons.compensate(caccCategory, freq(new double[] {demandPlatoonVehicles.get(0),
                                demandPlatoonVehicles.get(1), demandPlatoonVehicles.get(2), demandPlatoonVehicles.get(3),
                                demandPlatoonVehicles.get(4), demandPlatoonVehicles.get(5), demandPlatoonVehicles.get(6),
                                demandPlatoonVehicles.get(7), demandPlatoonVehicles.get(8)}), timeVector, interpolation));
            }

            // options
            ODOptions odOptions = new ODOptions().set(ODOptions.NO_LC_DIST, Length.instantiateSI(300.0)).set(ODOptions.GTU_TYPE,
                    characteristicsGenerator);
            odApplierOutput = ODApplier.applyOD(network, odMatrix, odOptions);

            // start platoons
            platoonDetector1 = "E1";
            platoons.start(odApplierOutput.get(platoonDetector1).getGenerator());

            // Write platoon generation times to file
            BufferedWriter bw;
            bw = new BufferedWriter(
                    new OutputStreamWriter(Writer.createOutputStream(this.outputFileGen, CompressionType.NONE)));
            bw.write(String.format("Platoon generation times [s]: %s", generationTimes));
            bw.close();

        }
        else
        {
            throw new RuntimeException("Network " + networkName + " not supported.");
        }

        // listen to generator so we can add platoon object to the tactical planner (for first direction)
        odApplierOutput.get(platoonDetector1).getGenerator().addListener(new EventListenerInterface()
        {
            /** Current platoon. */
            private Platoon platoon = null;

            /** Last generated CACC GTU. */
            private LaneBasedGTU lastGtu;

            /** {@inheritDoc} */
            @Override
            public void notify(final EventInterface event) throws RemoteException
            {
                if (event.getType().equals(LaneBasedGTUGenerator.GTU_GENERATED_EVENT))
                {
                    LaneBasedGTU gtu = (LaneBasedGTU) event.getContent();
                    if (gtu.getGTUType().equals(caccGTUType))
                    {
                        // If platoon is not within 2 seconds, or platoon is equal or larger than set platoon size -> new
                        // platoon
                        // || this.lastGtu.getLocation().distance(gtu.getLocation()) > 2.0 * gtu.getSpeed().si

                        if (this.lastGtu == null || this.lastGtu.isDestroyed()
                                || this.lastGtu.getLocation().distance(gtu.getLocation()) > 3.0 * gtu.getSpeed().si
                                || this.platoon.size() >= platoonSize)
                        {
                            this.platoon = new Platoon();
                        }
                        this.platoon.addGtu(gtu.getId());
                        ((CaccTacticalPlanner) gtu.getTacticalPlanner()).setPlatoon(this.platoon);
                        this.lastGtu = gtu;
                    }
                }
            }
        }, LaneBasedGTUGenerator.GTU_GENERATED_EVENT);

        // add animation to network objects
        DefaultAnimationFactory.animateNetwork(network, sim, getGtuColorer());

        /** Sampler for statistics. */
        this.sampler = RoadSampler.build(network).registerExtendedDataType(this.ttc)
                .registerFilterDataType(new FilterDataGtuType()).create();
        
        // Construct sample time and space window
        Time start = new Time(0.25, TimeUnit.BASE_HOUR); // TODO set times
        Time end = new Time(2.00, TimeUnit.BASE_HOUR);
        for (Link link : network.getLinkMap().values())
        {
            for (Lane lane : ((CrossSectionLink) link).getLanes())
            {
                KpiLaneDirection kpiLane = new KpiLaneDirection(new LaneData(lane), KpiGtuDirectionality.DIR_PLUS);
                SpaceTimeRegion region = new SpaceTimeRegion(kpiLane, Length.ZERO, lane.getLength(), start, end);
                this.regions.add(region);
                this.sampler.registerSpaceTimeRegion(region);
            }
        }

        // sampler
        for (Link link : network.getLinkMap().values())
        {
            for (Lane lane : ((CrossSectionLink) link).getLanes())
            {
                KpiLaneDirection kpiLane = new KpiLaneDirection(new LaneData(lane), KpiGtuDirectionality.DIR_PLUS);
                SpaceTimeRegion region = new SpaceTimeRegion(kpiLane, Length.ZERO, lane.getLength(), start, end);
                this.regions.add(region);
                this.sampler.registerSpaceTimeRegion(region);
            }
        }
        return network;
    }

    /** {@inheritDoc} */
    @Override
    protected void addTabs(final OTSSimulatorInterface sim, final OTSSimulationApplication<?> animation)
    {
        if (this.graphs)
        {
            // create tab
            int h = (int) Math.sqrt(this.graphLanes.size());
            int w = (int) Math.ceil(((double) this.graphLanes.size()) / h);
            TablePanel charts = new TablePanel(w, h);
            animation.getAnimationPanel().getTabbedPane().addTab(animation.getAnimationPanel().getTabbedPane().getTabCount(),
                    "statistics", charts);

            // create plots per lane
            int h2 = 0;
            int w2 = 0;
            for (int i = 0; i < this.graphLanes.size(); i++)
            {
                GraphPath<KpiLaneDirection> graphPath;
                try
                {
                    // starts at the lane and includes downstream lanes
                    graphPath = GraphLaneUtil.createPath("Lane" + (i + 1),
                            new LaneDirection(this.graphLanes.get(i), GTUDirectionality.DIR_PLUS));
                    GraphPath.initRecording(sampler, graphPath);
                }
                catch (NetworkException exception)
                {
                    throw new RuntimeException(exception);
                }
                ContourDataSource<?> dataPool = new ContourDataSource<>(this.sampler.getSamplerData(), graphPath);
                SwingContourPlot plot = new SwingContourPlot(new ContourPlotSpeed("Speed lane " + (i + 1), sim, dataPool));
                charts.setCell(plot.getContentPane(), w2, h2);
                w2++;
                if (w2 > w)
                {
                    w = 0;
                    h2++;
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void onSimulationEnd()
    {
        // periodic = true; periodic data is stored, if false other mesoscopic data would be stored, but that has not been setup
        Detector.writeToFile(getNetwork(), this.outputFileDets, true);

        // this.sampler.writeToFile(getProperty("outputFile"));
        double tts = 0.0;
        List<Float> ttcList = new ArrayList<>();
        List<String> ttcListGtuType = new ArrayList<>();
        List<Float> decList = new ArrayList<>();
        List<String> decListGtuType = new ArrayList<>();
        int[] counts = new int[120];
        Length detectorPosition = Length.instantiateSI(100.0);
        for (SpaceTimeRegion region : this.regions)
        {
            TrajectoryGroup<?> trajectoryGroup =
                    this.sampler.getSamplerData().getTrajectoryGroup(region.getLaneDirection()).getTrajectoryGroup(
                            region.getStartPosition(), region.getEndPosition(), region.getStartTime(), region.getEndTime());
            for (Trajectory<?> trajectory : trajectoryGroup)
            {
                try
                {
                    tts += trajectory.getTotalDuration().si;
                    String gtuTypeId = trajectory.getMetaData(metaGtu).getId();
                    for (FloatDuration ttcVal : trajectory.getExtendedData(this.ttc))
                    {
                        if (!Float.isNaN(ttcVal.si) && ttcVal.si < 20)
                        {
                            ttcList.add(ttcVal.si);
                            ttcListGtuType.add(gtuTypeId);
                        }
                    }
                    for (float decVal : trajectory.getA())
                    {
                        if (decVal < -2.0)
                        {
                            decList.add(decVal);
                            decListGtuType.add(gtuTypeId);
                        }
                    }
                    if (region.getLaneDirection().getLaneData().getLinkData().getId().equals("CD") && trajectory.size() > 1
                            && trajectory.getX(0) < detectorPosition.si
                            && trajectory.getX(trajectory.size() - 1) > detectorPosition.si)
                    {
                        double t = trajectory.getTimeAtPosition(detectorPosition).si - region.getStartTime().si;
                        counts[(int) (t / 60.0)]++;
                    }
                }
                catch (SamplingException exception)
                {
                    throw new RuntimeException("Unexpected exception: TimeToCollission not available or index out of bounds.",
                            exception);
                }
            }
        }
        int qMax = 0;
        for (int i = 0; i < counts.length - 4; i++)
        {
            int q = 0;
            for (int j = i; j < i + 5; j++)
            {
                q += counts[j];
            }
            qMax = qMax > q ? qMax : q;
        }
        BufferedWriter bw;
        try
        {
            bw = new BufferedWriter(
                    new OutputStreamWriter(Writer.createOutputStream(this.outputFileTraj, CompressionType.NONE)));
            bw.write(String.format("total time spent [s]: %.0f", tts));
            bw.newLine();
            bw.write(String.format("maximum flow [veh/5min]: %d", qMax));
            bw.newLine();
            bw.write(String.format("time to collision [s]: %s", ttcList));
            bw.newLine();
            bw.write(String.format("time to collision GTU type: %s", ttcListGtuType));
            bw.newLine();
            bw.write(String.format("strong decelerations [m/s^2]: %s", decList));
            bw.newLine();
            bw.write(String.format("strong decelerations GTU type: %s", decListGtuType));
            bw.close();
        }
        catch (IOException exception)
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
     * Returns an OD compatible characteristics generator that uses the GTUType to select either regular models or CACC specific
     * models.
     * @param longitudinalControllerFactory LongitudinalControllerFactory&lt;? extends CACC&gt;; longitudinal controller factory
     * @param simulator OTSSimulatorInterface; simulator
     * @param parameters ParameterFactory parameters
     * @return OD compatible characteristics generator
     */
    private DefaultGTUCharacteristicsGeneratorOD getCharacteristicsGenerator(
            final LongitudinalControllerFactory<? extends CaccController> longitudinalControllerFactory,
            final OTSSimulatorInterface simulator, final ParameterFactory parameters)
    {
        // create template for CACC truck properties (this is then used instead of GTUType.defaultCharacteristics(...))
        Set<TemplateGTUType> templates = new LinkedHashSet<>();
        templates.add(new TemplateGTUType(caccGTUType, new ConstantGenerator<>(Length.instantiateSI(12.0)),
                new ConstantGenerator<>(Length.instantiateSI(2.55)),
                new ConstantGenerator<>(Speed.instantiateSI(this.setspeed.si + 2.78))));
        // anonymous class for factory supplier that overwrites the getFactory() method to return one based on the GTU type
        return new DefaultGTUCharacteristicsGeneratorOD(templates, new StrategicalPlannerFactorySupplierOD()
        {
            /** Strategical planner for regular traffic. */
            private LaneBasedStrategicalPlannerFactory<?> lmrsFactory = null;

            /** Strategical planner for CACC trucks. */
            private LaneBasedStrategicalPlannerFactory<?> caccFactory = null;

            /** {@inheritDoc} */
            @Override
            public LaneBasedStrategicalPlannerFactory<?> getFactory(final Node origin, final Node destination,
                    final Category category, final StreamInterface randomStream) throws GTUException
            {
                GTUType gtuType = category.get(GTUType.class);
                if (!gtuType.equals(caccGTUType))
                {
                    if (this.lmrsFactory == null)
                    {
                        // perception factory with estimation distribution (i.e. over- vs. underestimation)
                        Distribution<Estimation> estimation;
                        try
                        {
                            estimation = new Distribution<>(randomStream);
                            estimation.add(new FrequencyAndObject<>(fractionUnderestimation, Estimation.UNDERESTIMATION));
                            estimation.add(new FrequencyAndObject<>(1.0 - fractionUnderestimation, Estimation.OVERESTIMATION));
                        }
                        catch (ProbabilityException ex)
                        {
                            throw new GTUException("Random stream is null.", ex);
                        }

                        PerceptionFactory perceptionFactory = new LmrsPerceptionFactory(estimation);

                        this.lmrsFactory = new LaneBasedStrategicalRoutePlannerFactory(
                                new LMRSFactory(new IDMPlusFactory(randomStream), perceptionFactory), parameters);
                    }
                    return this.lmrsFactory;
                }
                if (this.caccFactory == null)
                {
                    this.caccFactory = new LaneBasedStrategicalRoutePlannerFactory(
                            new CaccTacticalPlannerFactory(new IDMPlusFactory(randomStream), longitudinalControllerFactory,
                                    simulator, caccGTUType),
                            parameters);
                }
                return this.caccFactory;
            }
        });
    }

    /** ... */
    private class LmrsPerceptionFactory extends DefaultLMRSPerceptionFactory
    {
        /** Estimation distribution. */
        private final Distribution<Estimation> estimation;

        /**
         * Constructor.
         * @param estimation Distribution&lt;Estimation&gt;; estimation distribution
         */
        LmrsPerceptionFactory(final Distribution<Estimation> estimation)
        {
            this.estimation = estimation;
        }

        /** {@inheritDoc} */
        @Override
        public LanePerception generatePerception(final LaneBasedGTU gtu)
        {
            Set<Task> tasksSet = new LinkedHashSet<>();
            if (tasks)
            {
                tasksSet.add(new CarFollowingTaskAR());
                tasksSet.add(new LaneChangeTaskAR());
            }

            Set<BehavioralAdaptation> behavioralAdapatations = new LinkedHashSet<>();
            behavioralAdapatations.add(new AdaptationSituationalAwareness());
            if (adaptation)
            {
                behavioralAdapatations.add(new AdaptationHeadway());
            }
            LanePerception perception;
            if (tasks)
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
            perception.addPerceptionCategory(new DirectIntersectionPerception(perception, HeadwayGtuType.WRAP));
            return perception;
        }

        /** {@inheritDoc} */
        @Override
        public Parameters getParameters() throws ParameterException
        {
            Parameters params = super.getParameters();
            //params.setParameter(HEXP, Duration.instantiateSI(4.0));
            //params.setParameter(ALPHA, alpha);
            //params.setParameter(BETA, beta);
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

    /** ... */
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
            double a = alpha;
            double b = beta;
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
                CategoricalLanePerception clp = (CategoricalLanePerception) perception;
                if (null == ((DirectEgoPerception<?, ?>) clp.getPerceptionCategory(EgoPerception.class)).getTimeStampedSpeed())
                {
                    // System.out.println("CarFollowingTaskAR for " + perception.getGtu());
                    // System.out.println("perception is " + perception);
                    try
                    {
                        clp.getPerceptionCategory(EgoPerception.class).updateAll();
                        if (null == clp.getPerceptionCategory(EgoPerception.class).getSpeed())
                        {
                            System.out.println("fixed perception is " + perception);
                        }
                    }
                    catch (GTUException | NetworkException | ParameterException e)
                    {
                        e.printStackTrace();
                    }
                }
                // boolean state = leaders.isEmpty();
                // System.out.println("Leaders.isEmpty: " + state);
                return leaders.isEmpty() ? 0.0 : Math.exp(-leaders.first().getDistance().si
                        / (perception.getGtu().getSpeed().si * hExp.si));
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

}
