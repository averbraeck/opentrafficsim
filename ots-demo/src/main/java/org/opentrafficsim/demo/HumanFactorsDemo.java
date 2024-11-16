package org.opentrafficsim.demo;

import java.awt.Color;
import java.awt.Dimension;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import javax.naming.NamingException;

import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djunits.value.vdouble.vector.FrequencyVector;
import org.djunits.value.vdouble.vector.TimeVector;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.OrientedPoint2d;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.animation.colorer.FixedColor;
import org.opentrafficsim.animation.colorer.IncentiveColorer;
import org.opentrafficsim.animation.colorer.SocialPressureColorer;
import org.opentrafficsim.animation.colorer.TaskSaturationColorer;
import org.opentrafficsim.animation.gtu.colorer.AccelerationGtuColorer;
import org.opentrafficsim.animation.gtu.colorer.GtuColorer;
import org.opentrafficsim.animation.gtu.colorer.SpeedGtuColorer;
import org.opentrafficsim.animation.gtu.colorer.SwitchableGtuColorer;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterSet;
import org.opentrafficsim.base.parameters.ParameterTypeDouble;
import org.opentrafficsim.base.parameters.ParameterTypeDuration;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.base.parameters.constraint.DualBound;
import org.opentrafficsim.base.parameters.constraint.NumericConstraint;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.dsol.AbstractOtsModel;
import org.opentrafficsim.core.dsol.OtsAnimator;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.geometry.FractionalLengthData;
import org.opentrafficsim.core.geometry.OtsLine2d;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.gtu.perception.DirectEgoPerception;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.parameters.ParameterFactoryByType;
import org.opentrafficsim.core.units.distributions.ContinuousDistSpeed;
import org.opentrafficsim.demo.HumanFactorsDemo.HumanFactorsModel;
import org.opentrafficsim.road.definitions.DefaultsRoadNl;
import org.opentrafficsim.road.gtu.generator.characteristics.DefaultLaneBasedGtuCharacteristicsGeneratorOd;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGtuCharacteristicsGeneratorOd;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
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
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.HeadwayGtuType;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.HeadwayGtuType.PerceivedHeadwayGtuType;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.TaskHeadwayCollector;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtu;
import org.opentrafficsim.road.gtu.lane.perception.mental.AbstractTask;
import org.opentrafficsim.road.gtu.lane.perception.mental.AdaptationHeadway;
import org.opentrafficsim.road.gtu.lane.perception.mental.AdaptationSituationalAwareness;
import org.opentrafficsim.road.gtu.lane.perception.mental.AdaptationSpeed;
import org.opentrafficsim.road.gtu.lane.perception.mental.Fuller;
import org.opentrafficsim.road.gtu.lane.perception.mental.Fuller.BehavioralAdaptation;
import org.opentrafficsim.road.gtu.lane.perception.mental.Task;
import org.opentrafficsim.road.gtu.lane.perception.mental.TaskCarFollowing;
import org.opentrafficsim.road.gtu.lane.perception.mental.TaskManager;
import org.opentrafficsim.road.gtu.lane.perception.mental.TaskRoadSideDistraction;
import org.opentrafficsim.road.gtu.lane.tactical.following.AbstractIdm;
import org.opentrafficsim.road.gtu.lane.tactical.following.AbstractIdmFactory;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModelFactory;
import org.opentrafficsim.road.gtu.lane.tactical.following.IdmPlus;
import org.opentrafficsim.road.gtu.lane.tactical.following.IdmPlusFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.DefaultLmrsPerceptionFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveKeep;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveRoute;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveSocioSpeed;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveSpeedWithCourtesy;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.LmrsFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.SocioDesiredSpeed;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Cooperation;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.GapAcceptance;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsParameters;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.MandatoryIncentive;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Synchronization;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Tailgating;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.VoluntaryIncentive;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalRoutePlannerFactory;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.CrossSectionSlice;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneGeometryUtil;
import org.opentrafficsim.road.network.lane.Stripe;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;
import org.opentrafficsim.road.network.lane.object.Distraction;
import org.opentrafficsim.road.network.lane.object.Distraction.TrapezoidProfile;
import org.opentrafficsim.road.od.Categorization;
import org.opentrafficsim.road.od.Category;
import org.opentrafficsim.road.od.Interpolation;
import org.opentrafficsim.road.od.OdApplier;
import org.opentrafficsim.road.od.OdMatrix;
import org.opentrafficsim.road.od.OdOptions;
import org.opentrafficsim.swing.gui.OtsAnimationPanel;
import org.opentrafficsim.swing.gui.OtsSimulationApplication;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.jstats.distributions.DistLogNormal;
import nl.tudelft.simulation.jstats.distributions.DistTriangular;
import nl.tudelft.simulation.jstats.streams.StreamInterface;
import nl.tudelft.simulation.language.DsolException;

/**
 * This demo exists to show how the human factor models can be used in code. In particular see the
 * {@code HumanFactorsModel.constructModel()} method. The included human factors are 1) social interactions regarding lane
 * changes, tailgating and changes in speed, and 2) Anticipation Reliance in a mental task load framework of imperfect
 * perception. The scenario includes a distraction halfway on the network.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @see HumanFactorsModel#buildHumanFactorsModel()
 * @see <a href="https://www.preprints.org/manuscript/202305.0193/v1">Schakel et al. (2023) Social Interactions on Multi-Lane
 *      Motorways: Towards a Theory of Impacts</a>
 * @see <a href="https://www.sciencedirect.com/science/article/pii/S0191261520303714">Calvert et al. (2020) A generic
 *      multi-scale framework for microscopic traffic simulation part II – Anticipation Reliance as compensation mechanism for
 *      potential task overload</a>
 */
public final class HumanFactorsDemo extends OtsSimulationApplication<HumanFactorsModel>
{

    /** */
    private static final long serialVersionUID = 20241012L;

    /**
     * Constructor.
     * @param model model
     * @param panel panel
     */
    private HumanFactorsDemo(final HumanFactorsModel model, final OtsAnimationPanel panel)
    {
        super(model, panel);
    }

    /**
     * Main program.
     * @param args the command line arguments (not used)
     */
    public static void main(final String[] args)
    {
        try
        {
            OtsAnimator simulator = new OtsAnimator("HFDemo");
            final HumanFactorsModel junctionModel = new HumanFactorsModel(simulator);
            simulator.initialize(Time.ZERO, Duration.ZERO, Duration.instantiateSI(3600.0), junctionModel);
            // Note some relevant colorers for social interactions and task saturation
            GtuColorer colorer = new SwitchableGtuColorer(0, new FixedColor(Color.BLUE, "Blue"),
                    new SpeedGtuColorer(new Speed(150.0, SpeedUnit.KM_PER_HOUR)),
                    new AccelerationGtuColorer(Acceleration.instantiateSI(-4.0), Acceleration.instantiateSI(2.0)),
                    new SocialPressureColorer(), new IncentiveColorer(IncentiveSocioSpeed.class), new TaskSaturationColorer());
            OtsAnimationPanel animationPanel = new OtsAnimationPanel(junctionModel.getNetwork().getExtent(),
                    new Dimension(800, 600), simulator, junctionModel, colorer, junctionModel.getNetwork());
            new HumanFactorsDemo(junctionModel, animationPanel);
            animationPanel.enableSimulationControlButtons();
        }
        catch (SimRuntimeException | NamingException | RemoteException | DsolException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * The simulation model object.
     */
    public static class HumanFactorsModel extends AbstractOtsModel
    {

        /** */
        private static final long serialVersionUID = 20241012L;

        /** The network. */
        private RoadNetwork network;

        /** Characteristics generator. */
        private LaneBasedGtuCharacteristicsGeneratorOd characteristics;

        /**
         * Constructor.
         * @param simulator simulator
         */
        public HumanFactorsModel(final OtsSimulatorInterface simulator)
        {
            super(simulator);
        }

        @Override
        public Network getNetwork()
        {
            return this.network;
        }

        @Override
        public void constructModel() throws SimRuntimeException
        {
            try
            {
                buildNetwork();
                buildHumanFactorsModel();
                setDemand();
            }
            catch (NetworkException | ParameterException exception)
            {
                throw new SimRuntimeException(exception);
            }
        }

        /**
         * Builds the human factors model.
         * @throws ParameterException if parameter has no default value
         */
        private void buildHumanFactorsModel() throws ParameterException
        {
            StreamInterface stream = getSimulator().getModel().getStream("generation");

            // social = social interactions, perception = imperfect perception
            boolean social = true;
            boolean perception = true;

            ParameterFactoryByType parameterFactory = new ParameterFactoryByType();
            parameterFactory.addParameter(ParameterTypes.LOOKBACK, ParameterTypes.LOOKBACK.getDefaultValue());
            parameterFactory.addParameter(ParameterTypes.LOOKAHEAD, ParameterTypes.LOOKAHEAD.getDefaultValue());
            parameterFactory.addParameter(ParameterTypes.PERCEPTION, ParameterTypes.PERCEPTION.getDefaultValue());
            if (social)
            {
                /*
                 * In case of social interactions we 1) introduce the RHO status variable of social pressure, value only
                 * important at vehicle generation as the model sets this, 2) increase the TMAX value from a normal 1.2s to
                 * 1.6s, as the tailgating phenomenon will reduce this, leading to an average at around 1.2s, but now dynamic
                 * depending on social pressure, 3) we introduce a distributed SOCIO parameter to represent sensitivity to
                 * social pressure, 4) we lower VGAIN to increase ego-speed sensitivity, and distribute it (we now have a
                 * population of drivers distributed on a plain of socio and ego speed sensitivity, and 5) something similar but
                 * simple for trucks.
                 */
                parameterFactory.addParameter(Tailgating.RHO, 0.0);
                parameterFactory.addParameter(ParameterTypes.TMAX, Duration.instantiateSI(1.6));
                parameterFactory.addParameter(DefaultsNl.CAR, LmrsParameters.SOCIO, new DistTriangular(stream, 0.0, 0.25, 1.0));
                parameterFactory.addParameter(DefaultsNl.CAR, LmrsParameters.VGAIN, // mu =~ 3.3789, sigma = 0.4, mode = 25.0
                        new ContinuousDistSpeed(new DistLogNormal(stream, Math.log(25.0) + 0.4 * 0.4, 0.4),
                                SpeedUnit.KM_PER_HOUR));
                parameterFactory.addParameter(DefaultsNl.TRUCK, LmrsParameters.SOCIO, 1.0);
                parameterFactory.addParameter(DefaultsNl.TRUCK, LmrsParameters.VGAIN, new Speed(50.0, SpeedUnit.KM_PER_HOUR));
            }

            /*
             * Car-following: In case of social interactions, the normal desired headway model is wrapped in a model that
             * adjusts it as a response to social pressure from the following vehicle.
             */
            CarFollowingModelFactory<IdmPlus> cfModelFactory = social
                    ? new AbstractIdmFactory<>(
                            () -> new IdmPlus(AbstractIdm.HEADWAY, new SocioDesiredSpeed(AbstractIdm.DESIRED_SPEED)), stream)
                    : new IdmPlusFactory(stream);

            // In case of imperfect perception we use the below, otherwise DefaultLmrsPerceptionFactory (see at bottom)
            PerceptionFactory perceptionFactory = perception ? new PerceptionFactory()
            {
                @Override
                public Parameters getParameters() throws ParameterException
                {
                    /*
                     * We need to include (default) values for 1) the Fuller task demand model of task demand, capacity and
                     * saturation, 2) the anticipation reliance (AR) task manager based on a notion of a primary task and
                     * auxiliary task, 3) situational awareness as it depends on task saturation, 4) car-following task
                     * parameter of exponential relationship with task demand, 5) over or underestimation, and 6) sensitivity of
                     * adapting the headway and desired speed based on high task saturation.
                     */
                    ParameterSet perceptionParams = new ParameterSet();
                    perceptionParams.setDefaultParameters(Fuller.class);
                    perceptionParams.setDefaultParameters(TaskManagerAr.class);
                    perceptionParams.setDefaultParameters(AdaptationSituationalAwareness.class);
                    perceptionParams.setDefaultParameter(CarFollowingTask.HEXP);
                    perceptionParams.setDefaultParameter(Estimation.OVER_EST);
                    perceptionParams.setDefaultParameter(AdaptationHeadway.BETA_T);
                    perceptionParams.setDefaultParameter(AdaptationSpeed.BETA_V0);
                    return perceptionParams;
                }

                @Override
                public LanePerception generatePerception(final LaneBasedGtu gtu)
                {
                    // Tasks that determine task demand
                    Set<Task> tasks = new LinkedHashSet<>();
                    tasks.add(new TaskCarFollowing());
                    tasks.add(new LaneChangeTask());
                    tasks.add(new TaskRoadSideDistraction()); // Level of distraction is defined in Distraction network object
                    // Behavioral adaptations (AdaptationSituationalAwareness only sets situational awareness and reaction time)
                    Set<BehavioralAdaptation> behavioralAdapatations = new LinkedHashSet<>();
                    behavioralAdapatations.add(new AdaptationSituationalAwareness());
                    behavioralAdapatations.add(new AdaptationHeadway());
                    behavioralAdapatations.add(new AdaptationSpeed());
                    // AR task manager, assuming lane changing to be primary
                    TaskManager taskManager = new TaskManagerAr("lane-changing");
                    // Fuller framework based on components
                    CategoricalLanePerception perception =
                            new CategoricalLanePerception(gtu, new Fuller(tasks, behavioralAdapatations, taskManager));
                    // Imperfect estimation of distance and speed difference, with reaction time, and compensatory anticipation
                    HeadwayGtuType headwayGtuType =
                            new PerceivedHeadwayGtuType(Estimation.FACTOR_ESTIMATION, Anticipation.CONSTANT_SPEED);
                    // Standard perception categories, using imperfect perception regarding neighbors with the HeadwayGtuType
                    perception.addPerceptionCategory(new DirectEgoPerception<>(perception));
                    perception.addPerceptionCategory(new DirectInfrastructurePerception(perception));
                    perception.addPerceptionCategory(new DirectNeighborsPerception(perception, headwayGtuType));
                    perception.addPerceptionCategory(new AnticipationTrafficPerception(perception));
                    return perception;
                }
            } : new DefaultLmrsPerceptionFactory();

            // Tailgating, in case of social interactions, reduces the headway based on exerted social pressure on the leader
            Tailgating tailgating = social ? Tailgating.PRESSURE : Tailgating.NONE;

            // incentives: mandatory, voluntary
            Supplier<Set<MandatoryIncentive>> mandatorySupplier = () ->
            {
                Set<MandatoryIncentive> mandatoryIncentives = new LinkedHashSet<>();
                mandatoryIncentives.add(new IncentiveRoute());
                return mandatoryIncentives;
            };
            Supplier<Set<VoluntaryIncentive>> voluntarySupplier = () ->
            {
                Set<VoluntaryIncentive> voluntaryIncentives = new LinkedHashSet<>();
                voluntaryIncentives.add(new IncentiveSpeedWithCourtesy());
                voluntaryIncentives.add(new IncentiveKeep());
                /*
                 * Next to increasing speed, social interactions include a change in lane change desire to get or stay out of
                 * the way of faster (potential) followers.
                 */
                if (social)
                {
                    voluntaryIncentives.add(new IncentiveSocioSpeed());
                }
                return voluntaryIncentives;
            };

            // Layered factories (tactical, strategical, strategical in an OD context)
            LmrsFactory lmrsFactory = new LmrsFactory(cfModelFactory, perceptionFactory, Synchronization.PASSIVE,
                    Cooperation.PASSIVE, GapAcceptance.INFORMED, tailgating, mandatorySupplier, voluntarySupplier,
                    () -> new LinkedHashSet<>());
            LaneBasedStrategicalRoutePlannerFactory strategicalPlannerFactory =
                    new LaneBasedStrategicalRoutePlannerFactory(lmrsFactory, parameterFactory);
            this.characteristics =
                    new DefaultLaneBasedGtuCharacteristicsGeneratorOd.Factory(strategicalPlannerFactory).create();
        }

        /**
         * Builds the network, a 3km 2-lane highway section.
         * @throws NetworkException when the network is ill defined
         */
        private void buildNetwork() throws NetworkException
        {
            this.network = new RoadNetwork("HF network", getSimulator());

            OrientedPoint2d p1 = new OrientedPoint2d(0.0, 0.0, 0.0);
            OrientedPoint2d p2 = new OrientedPoint2d(3000.0, 0.0, 0.0);

            Node nodeA = new Node(this.network, "A", p1);
            Node nodeB = new Node(this.network, "B", p2);

            Map<GtuType, Speed> speedLimit = Map.of(DefaultsNl.VEHICLE, new Speed(130.0, SpeedUnit.KM_PER_HOUR));

            OtsLine2d centerLine = new OtsLine2d(p1, p2);
            CrossSectionLink link = new CrossSectionLink(this.network, "AB", nodeA, nodeB, DefaultsNl.HIGHWAY, centerLine,
                    FractionalLengthData.of(0.0, 0.0), LaneKeepingPolicy.KEEPRIGHT);

            CrossSectionSlice slice =
                    new CrossSectionSlice(Length.instantiateSI(0.0), Length.instantiateSI(3.5), Length.instantiateSI(0.2));
            OtsLine2d offset = centerLine.offsetLine(slice.getOffset().si);
            new Stripe(Stripe.StripeType.SOLID, link, offset, getContour(offset, slice.getWidth().si), List.of(slice));

            slice = new CrossSectionSlice(Length.instantiateSI(0.0), Length.instantiateSI(1.75), Length.instantiateSI(3.5));
            offset = centerLine.offsetLine(slice.getOffset().si);
            Lane left = new Lane(link, "LEFT", offset, getContour(offset, slice.getWidth().si), List.of(slice),
                    DefaultsRoadNl.HIGHWAY, speedLimit);

            slice = new CrossSectionSlice(Length.instantiateSI(0.0), Length.instantiateSI(0.0), Length.instantiateSI(0.2));
            offset = centerLine.offsetLine(slice.getOffset().si);
            new Stripe(Stripe.StripeType.DASHED, link, offset, getContour(offset, slice.getWidth().si), List.of(slice));

            slice = new CrossSectionSlice(Length.instantiateSI(0.0), Length.instantiateSI(-1.75), Length.instantiateSI(3.5));
            offset = centerLine.offsetLine(slice.getOffset().si);
            Lane right = new Lane(link, "RIGHT", offset, getContour(offset, slice.getWidth().si), List.of(slice),
                    DefaultsRoadNl.HIGHWAY, speedLimit);

            slice = new CrossSectionSlice(Length.instantiateSI(0.0), Length.instantiateSI(-3.5), Length.instantiateSI(0.2));
            offset = centerLine.offsetLine(slice.getOffset().si);
            new Stripe(Stripe.StripeType.SOLID, link, offset, getContour(offset, slice.getWidth().si), List.of(slice));

            // Add distraction halfway on the network, 0.3 on left lane, 0.2 on right lane, with distance profile
            new Distraction("distractionLeft", left, Length.instantiateSI(1500.0), new TrapezoidProfile(0.3,
                    Length.instantiateSI(-100.0), Length.instantiateSI(50.0), Length.instantiateSI(150.0)));
            new Distraction("distractionRight", right, Length.instantiateSI(1500.0), new TrapezoidProfile(0.2,
                    Length.instantiateSI(-100.0), Length.instantiateSI(50.0), Length.instantiateSI(150.0)));
        }

        /**
         * Creates contour from line based on width.
         * @param line line
         * @param width width
         * @return contour from line based on width
         */
        private Polygon2d getContour(final OtsLine2d line, final double width)
        {
            return LaneGeometryUtil.getContour(line.offsetLine(width / 2, width / 2), line.offsetLine(-width / 2, -width / 2));
        }

        /**
         * Set demand in network.
         * @throws SimRuntimeException sim exception
         * @throws ParameterException parameter exception
         */
        private void setDemand() throws SimRuntimeException, ParameterException
        {
            Node nodeA = this.network.getNode("A");
            Node nodeB = this.network.getNode("B");
            Categorization categorization = new Categorization("GTU type", GtuType.class);
            GtuType.registerTemplateSupplier(DefaultsNl.CAR, DefaultsNl.NL);
            GtuType.registerTemplateSupplier(DefaultsNl.TRUCK, DefaultsNl.NL);
            List<Node> origins = new ArrayList<>();
            origins.add(nodeA);
            List<Node> destinations = new ArrayList<>();
            destinations.add(nodeB);
            OdMatrix od = new OdMatrix("OD", origins, destinations, categorization,
                    new TimeVector(new double[] {0.0, 1800.0, 3600.0}), Interpolation.LINEAR);
            FrequencyVector demand = new FrequencyVector(new double[] {2000.0, 4000.0, 1000.0}, FrequencyUnit.PER_HOUR);
            double truckFraction = 0.1;
            od.putDemandVector(nodeA, nodeB, new Category(categorization, DefaultsNl.CAR), demand, 1.0 - truckFraction);
            od.putDemandVector(nodeA, nodeB, new Category(categorization, DefaultsNl.TRUCK), demand, truckFraction);
            OdOptions odOptions = new OdOptions();
            odOptions.set(OdOptions.NO_LC_DIST, Length.instantiateSI(150.0));
            odOptions.set(OdOptions.GTU_TYPE, this.characteristics);
            odOptions.set(OdOptions.LANE_BIAS, DefaultsRoadNl.LANE_BIAS_CAR_TRUCK);
            OdApplier.applyOd(this.network, od, odOptions, DefaultsNl.VEHICLES);
        }
    }

    /**
     * Task manager implementation of anticipation reliance.
     */
    public static class TaskManagerAr implements TaskManager
    {
        /** Fraction of primary task that can be reduced by anticipation reliance. */
        public static final ParameterTypeDouble ALPHA = new ParameterTypeDouble("alpha",
                "Fraction of primary task that can be reduced by anticipation reliance.", 0.8, DualBound.UNITINTERVAL);

        /** Fraction of auxiliary tasks that can be reduced by anticipation reliance. */
        public static final ParameterTypeDouble BETA = new ParameterTypeDouble("beta",
                "Fraction of auxiliary tasks that can be reduced by anticipation reliance.", 0.6, DualBound.UNITINTERVAL);

        /** Primary task id. */
        private final String primaryTaskId;

        /**
         * Constructor.
         * @param primaryTaskId String; primary task id.
         */
        public TaskManagerAr(final String primaryTaskId)
        {
            Throw.whenNull(primaryTaskId, "Primary task id may not be null.");
            this.primaryTaskId = primaryTaskId;
        }

        @Override
        public void manage(final Set<Task> tasks, final LanePerception perception, final LaneBasedGtu gtu,
                final Parameters parameters) throws ParameterException, GtuException
        {
            Task primary = null;
            Set<Task> auxiliaryTasks = new LinkedHashSet<>();
            for (Task task : tasks)
            {
                if (task.getId().equals(this.primaryTaskId))
                {
                    primary = task;
                }
                else
                {
                    auxiliaryTasks.add(task);
                }
            }
            Throw.whenNull(primary, "There is no task with id '%s'.", this.primaryTaskId);
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

    /**
     * Car-following task demand based on headway.
     */
    public class CarFollowingTask extends AbstractTask
    {

        /** Car-following task parameter. */
        public static final ParameterTypeDuration HEXP = new ParameterTypeDuration("Hexp",
                "Exponential decay of car-following task by headway.", Duration.instantiateSI(4.0), NumericConstraint.POSITIVE);

        /**
         * Constructor.
         */
        public CarFollowingTask()
        {
            super("car-following");
        }

        @Override
        public double calculateTaskDemand(final LanePerception perception, final LaneBasedGtu gtu, final Parameters parameters)
                throws ParameterException, GtuException
        {
            try
            {
                NeighborsPerception neighbors = perception.getPerceptionCategory(NeighborsPerception.class);
                PerceptionCollectable<HeadwayGtu, LaneBasedGtu> leaders = neighbors.getLeaders(RelativeLane.CURRENT);
                Duration headway = leaders.collect(new TaskHeadwayCollector(gtu.getSpeed()));
                return headway == null ? 0.0 : Math.exp(-headway.si / parameters.getParameter(HEXP).si);
            }
            catch (OperationalPlanException ex)
            {
                throw new GtuException(ex);
            }
        }
    }

    /**
     * Lane change task demand depending on lane change desire.
     */
    public static class LaneChangeTask extends AbstractTask
    {
        /**
         * Constructor.
         */
        public LaneChangeTask()
        {
            super("lane-changing");
        }

        @Override
        public double calculateTaskDemand(final LanePerception perception, final LaneBasedGtu gtu, final Parameters parameters)
                throws ParameterException, GtuException
        {
            return Math.max(0.0,
                    Math.max(parameters.getParameter(LmrsParameters.DLEFT), parameters.getParameter(LmrsParameters.DRIGHT)));
        }
    }

}
