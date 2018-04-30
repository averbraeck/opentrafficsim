package strategies;

import java.awt.Color;
import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;

import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.StorageType;
import org.djunits.value.ValueException;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djunits.value.vdouble.vector.FrequencyVector;
import org.djunits.value.vdouble.vector.TimeVector;
import org.opentrafficsim.base.modelproperties.Property;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterSet;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUCharacteristics;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.Try;
import org.opentrafficsim.core.gtu.animation.AccelerationGTUColorer;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.gtu.animation.IDGTUColorer;
import org.opentrafficsim.core.gtu.animation.SpeedGTUColorer;
import org.opentrafficsim.core.gtu.animation.SwitchableGTUColorer;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterFactoryByType;
import org.opentrafficsim.core.gtu.perception.DirectEgoPerception;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.OTSLink;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.core.network.animation.LinkAnimation;
import org.opentrafficsim.core.network.animation.NodeAnimation;
import org.opentrafficsim.core.perception.HistoryManager;
import org.opentrafficsim.core.units.distributions.ContinuousDistDoubleScalar;
import org.opentrafficsim.road.animation.AnimationToggles;
import org.opentrafficsim.road.gtu.animation.DesiredHeadwayColorer;
import org.opentrafficsim.road.gtu.animation.DesiredSpeedColorer;
import org.opentrafficsim.road.gtu.animation.FixedColor;
import org.opentrafficsim.road.gtu.animation.GTUTypeColorer;
import org.opentrafficsim.road.gtu.animation.IncentiveColorer;
import org.opentrafficsim.road.gtu.animation.SocialPressureColorer;
import org.opentrafficsim.road.gtu.animation.SynchronizationColorer;
import org.opentrafficsim.road.gtu.animation.TotalDesireColorer;
import org.opentrafficsim.road.gtu.generator.GTUGenerator;
import org.opentrafficsim.road.gtu.generator.GTUGeneratorAnimation;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions.LaneBias;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions.LaneBiases;
import org.opentrafficsim.road.gtu.generator.MarkovCorrelation;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGTUCharacteristics;
import org.opentrafficsim.road.gtu.generator.headway.ArrivalsHeadwayGenerator.HeadwayDistribution;
import org.opentrafficsim.road.gtu.generator.od.GTUCharacteristicsGeneratorOD;
import org.opentrafficsim.road.gtu.generator.od.ODApplier;
import org.opentrafficsim.road.gtu.generator.od.ODApplier.GeneratorObjects;
import org.opentrafficsim.road.gtu.generator.od.ODOptions;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.CategoricalLanePerception;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionFactory;
import org.opentrafficsim.road.gtu.lane.perception.categories.AnticipationTrafficPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.DirectInfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.DirectNeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.HeadwayGtuType;
import org.opentrafficsim.road.gtu.lane.tactical.following.AbstractIDM;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModelFactory;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlus;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlusFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.AccelerationIncentive;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveKeep;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveRoute;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveSocioSpeed;
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
import org.opentrafficsim.road.network.animation.LaneAnimation;
import org.opentrafficsim.road.network.animation.StripeAnimation;
import org.opentrafficsim.road.network.animation.StripeAnimation.TYPE;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.Stripe;
import org.opentrafficsim.road.network.lane.Stripe.Permeable;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;
import org.opentrafficsim.road.network.lane.changing.OvertakingConditions;
import org.opentrafficsim.road.network.lane.object.SpeedSign;
import org.opentrafficsim.road.network.lane.object.sensor.Detector;
import org.opentrafficsim.road.network.lane.object.sensor.Detector.CompressionMethod;
import org.opentrafficsim.road.network.lane.object.sensor.Detector.DetectorMeasurement;
import org.opentrafficsim.road.network.lane.object.sensor.SinkSensor;
import org.opentrafficsim.simulationengine.AbstractWrappableAnimation;
import org.opentrafficsim.simulationengine.AbstractWrappableSimulation;
import org.opentrafficsim.simulationengine.OTSSimulationException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.event.EventInterface;
import nl.tudelft.simulation.event.EventListenerInterface;
import nl.tudelft.simulation.jstats.distributions.DistNormal;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Simulations regarding LMRS lane change strategies. This entails the base LMRS with:
 * <ul>
 * <li>Distributed Tmax</li>
 * <li>Distributed vGain</li>
 * <li>Distributed socio-speed sensitivity parameter (LmrsParameters.SOCIO)</li>
 * <li>Altered gap-acceptance: use own Tmax (GapAcceptance.EGO_HEADWAY)</li>
 * <li>Altered desired speed: increase during overtaking (SocioDesiredSpeed)</li>
 * <li>Lane change incentive to get out of the way (IncentiveSocioSpeed)</li>
 * </ul>
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 2 mrt. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class LmrsStrategies implements EventListenerInterface
{

    // TODO find Tmax for which capacity is about the same as the base LMRS, also regard saturation flow

    /** Simulation time. */
    private final static Time simTime = Time.createSI(3600);

    /** Truck fraction. */
    private final static double fTruck = 0.10;

    /** Synchronization. */
    final static Synchronization synchronization = Synchronization.PASSIVE;

    /** Cooperation. */
    final static Cooperation cooperation = Cooperation.PASSIVE;

    /** Gap-acceptance. */
    final static GapAcceptance gapAcceptance = GapAcceptance.INFORMED;

    /** Use base LMRS. */
    static boolean baseLMRS = false;

    /** Seed. */
    long seed = 6L;

    /** Sigma. */
    double sigma = 0.6; // 0.4

    /** vGain [km/h]. */
    double vGain = 30; // 69.7;

    /** Suffix for file name. */
    private String suffix = "";

    /** Distributed maximum speed for trucks. */
    ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> vTruck;

    /** Strategical planner factories per GTU type. */
    final Map<GTUType, LaneBasedStrategicalPlannerFactory<?>> factories = new HashMap<>();

    /** The simulator. */
    OTSDEVSSimulatorInterface simulator;

    /** The network. */
    OTSNetwork network;

    /** GTU colorer. */
    final GTUColorer colorer = SwitchableGTUColorer.builder().addActiveColorer(new FixedColor(Color.BLUE, "Blue"))
            .addColorer(GTUTypeColorer.DEFAULT).addColorer(new IDGTUColorer())
            .addColorer(new SpeedGTUColorer(new Speed(150, SpeedUnit.KM_PER_HOUR)))
            .addColorer(new DesiredSpeedColorer(new Speed(80, SpeedUnit.KM_PER_HOUR), new Speed(150, SpeedUnit.KM_PER_HOUR)))
            .addColorer(new AccelerationGTUColorer(Acceleration.createSI(-6.0), Acceleration.createSI(2)))
            .addColorer(new SynchronizationColorer())
            .addColorer(new DesiredHeadwayColorer(Duration.createSI(0.5), Duration.createSI(2.0)))
            .addColorer(new TotalDesireColorer()).addColorer(new IncentiveColorer(IncentiveRoute.class))
            .addColorer(new IncentiveColorer(IncentiveStayRight.class))
            .addColorer(new IncentiveColorer(IncentiveSpeedWithCourtesy.class))
            .addColorer(new IncentiveColorer(IncentiveKeep.class)).addColorer(new IncentiveColorer(IncentiveSocioSpeed.class))
            .addColorer(new SocialPressureColorer()).build();

    /**
     * Main method with command line arguments.
     * @param args String[] command line arguments
     */
    public static void main(String[] args)
    {

        // default properties
        boolean autorun = false;

        // parse args
        for (String arg : args)
        {
            int equalsPos = arg.indexOf("=");
            if (equalsPos >= 0)
            {
                // set something
                String key = arg.substring(0, equalsPos);
                String value = arg.substring(equalsPos + 1);
                if ("autorun".equalsIgnoreCase(key))
                {
                    if ("true".equalsIgnoreCase(value))
                    {
                        autorun = true;
                    }
                    else if ("false".equalsIgnoreCase(value))
                    {
                        autorun = false;
                    }
                    else
                    {
                        System.err.println("bad autorun value " + value + " (ignored)");
                    }
                }
            }
        }

        // run
        if (autorun)
        {
            double[] sigmaArray = new double[] { .6 };// { .2, .4, .6, .8 };
            double[] vGainArray = new double[] { 30 };// { 15, 30, 45, 60 };
            int seeds = 1;// 10;
            int n = 1;
            int nDone = 0;
            int nTot = sigmaArray.length * vGainArray.length * seeds;
            LocalDateTime start = LocalDateTime.now();
            for (double sigmaLoop : sigmaArray)
            {
                for (double vGainLoop : vGainArray)
                {
                    // for (long seedLoop = 1; seedLoop <= seeds; seedLoop++)
                    // {
                    int seedLoop = 6;
                    LmrsStrategies lmrsStrategies = new LmrsStrategies();
                    lmrsStrategies.sigma = sigmaLoop;
                    lmrsStrategies.vGain = vGainLoop;
                    lmrsStrategies.seed = seedLoop;
                    lmrsStrategies.suffix = String.format("_%.2f_%.2f_%02d", sigmaLoop, vGainLoop, seedLoop);
                    LocalDateTime now = LocalDateTime.now();
                    if (n > nDone)
                    {
                        System.out.println("[" + now.format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "] Running simulation "
                                + n + " of " + nTot + " with suffix " + lmrsStrategies.suffix);
                    }
                    if (n > nDone + 1)
                    {
                        long tRemain = (nTot - n - nDone - 1)
                                * (now.toEpochSecond(ZoneOffset.UTC) - start.toEpochSecond(ZoneOffset.UTC)) / (n - nDone - 1);
                        LocalDateTime eft = now.plusSeconds(tRemain);
                        System.out.println("           Estimated finish time: "
                                + eft.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    }
                    if (n > nDone)
                    {
                        LmrsStrategiesSimulation lmrsStrategiesSimulation = lmrsStrategies.new LmrsStrategiesSimulation();
                        try
                        {
                            OTSDEVSSimulatorInterface sim = lmrsStrategiesSimulation.buildSimulator(Time.ZERO, Duration.ZERO,
                                    Duration.createSI(simTime.si), new ArrayList<Property<?>>());
                            double tReport = 60.0;
                            Time t = sim.getSimulatorTime().getTime();
                            while (t.lt(simTime))
                            {
                                sim.step();
                                t = sim.getSimulatorTime().getTime();
                                if (t.si >= tReport)
                                {
                                    System.out.println("Simulation time is " + t);
                                    tReport += 60.0;
                                }
                            }
                            sim.stop(); // end of simulation event
                            HistoryManager.clear(sim);
                        }
                        catch (Exception exception)
                        {
                            exception.printStackTrace();
                        }
                    }
                    n++;
                }
                // }
            }
        }
        else
        {
            LmrsStrategiesAnimation lmrsStrategiesAnimation = new LmrsStrategies().new LmrsStrategiesAnimation();
            try
            {
                lmrsStrategiesAnimation.buildAnimator(Time.ZERO, Duration.ZERO, Duration.createSI(simTime.si),
                        new ArrayList<Property<?>>(), null, true);
            }
            catch (Exception exception)
            {
                exception.printStackTrace();
            }
        }
    }

    /**
     * Simulation without visualization.
     * <p>
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 21 mrt. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    class LmrsStrategiesSimulation extends AbstractWrappableSimulation
    {

        /** */
        private static final long serialVersionUID = 20180321L;

        /** {@inheritDoc} */
        @Override
        public String shortName()
        {
            return "LMRS Strategies";
        }

        /** {@inheritDoc} */
        @Override
        public String description()
        {
            return "Simulation to test the effects of lane change strategies using the LMRS.";
        }

        /** {@inheritDoc} */
        @Override
        protected OTSModelInterface makeModel() throws OTSSimulationException
        {
            return new LmrsStrategiesModel();
        }

    }

    /**
     * Animator.
     * <p>
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 3 mrt. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    class LmrsStrategiesAnimation extends AbstractWrappableAnimation
    {

        /** */
        private static final long serialVersionUID = 20180303L;

        /** {@inheritDoc} */
        @Override
        public String shortName()
        {
            return "LMRS Strategies";
        }

        /** {@inheritDoc} */
        @Override
        public String description()
        {
            return "Simulation to test the effects of lane change strategies using the LMRS.";
        }

        /** {@inheritDoc} */
        @Override
        protected OTSModelInterface makeModel() throws OTSSimulationException
        {
            return new LmrsStrategiesModel();
        }

        /** {@inheritDoc} */
        @Override
        public GTUColorer getColorer()
        {
            return LmrsStrategies.this.colorer;
        }

        /** {@inheritDoc} */
        @Override
        protected final void addAnimationToggles()
        {
            AnimationToggles.setIconAnimationTogglesFull(this);
            toggleAnimationClass(OTSLink.class);
            toggleAnimationClass(OTSNode.class);
            toggleAnimationClass(GTUGenerator.class);
            showAnimationClass(SpeedSign.class);
        }

    }

    /**
     * LMRS model.
     * <p>
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 3 mrt. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    class LmrsStrategiesModel implements OTSModelInterface
    {

        /** */
        private static final long serialVersionUID = 20180303L;

        /** {@inheritDoc} */
        @Override
        public void constructModel(SimulatorInterface<Time, Duration, OTSSimTimeDouble> simul)
                throws SimRuntimeException, RemoteException
        {
            OTSDEVSSimulatorInterface sim = (OTSDEVSSimulatorInterface) simul;
            LmrsStrategies.this.simulator = sim;
            OTSNetwork net = new OTSNetwork("LMRS strategies");
            LmrsStrategies.this.simulator.addListener(LmrsStrategies.this, SimulatorInterface.END_OF_REPLICATION_EVENT);
            LmrsStrategies.this.network = net;
            Map<String, StreamInterface> streams = new HashMap<>();
            StreamInterface stream = new MersenneTwister(LmrsStrategies.this.seed);
            streams.put("generation", stream);
            sim.getReplication().setStreams(streams);

            // Vehicle-driver classes
            LmrsStrategies.this.vTruck =
                    new ContinuousDistDoubleScalar.Rel<>(new DistNormal(stream, 85.0, 2.5), SpeedUnit.KM_PER_HOUR);
            // characteristics generator using the input available in this context
            class LmrsStrategyCharacteristicsGenerator implements GTUCharacteristicsGeneratorOD
            {
                /** {@inheritDoc} */
                @Override
                public LaneBasedGTUCharacteristics draw(final Node origin, final Node destination, final Category category,
                        final StreamInterface randomStream) throws GTUException
                {
                    GTUType gtuType = category.get(GTUType.class);
                    GTUCharacteristics gtuCharacteristics =
                            Try.assign(() -> GTUType.defaultCharacteristics(gtuType, randomStream),
                                    "Exception while applying default GTU characteristics.");
                    if (gtuType.equals(GTUType.TRUCK))
                    {
                        gtuCharacteristics = new GTUCharacteristics(GTUType.TRUCK, gtuCharacteristics.getLength(),
                                gtuCharacteristics.getWidth(), LmrsStrategies.this.vTruck.draw(),
                                gtuCharacteristics.getMaximumAcceleration(), gtuCharacteristics.getMaximumDeceleration(),
                                gtuCharacteristics.getFront());
                    }
                    return new LaneBasedGTUCharacteristics(gtuCharacteristics, LmrsStrategies.this.factories.get(gtuType), null,
                            origin, destination);
                }
            }
            // perception factory with ego, infra and neighbors only
            class LmrsStrategiesPerceptionFactory implements PerceptionFactory
            {
                /** {@inheritDoc} */
                @Override
                public LanePerception generatePerception(final LaneBasedGTU gtu)
                {
                    LanePerception perception = new CategoricalLanePerception(gtu);
                    perception.addPerceptionCategory(new DirectEgoPerception(perception));
                    perception.addPerceptionCategory(new DirectInfrastructurePerception(perception));
                    perception.addPerceptionCategory(new DirectNeighborsPerception(perception, HeadwayGtuType.WRAP));
                    perception.addPerceptionCategory(new AnticipationTrafficPerception(perception));
                    return perception;
                }

                /** {@inheritDoc} */
                @Override
                public Parameters getParameters() throws ParameterException
                {
                    return new ParameterSet().setDefaultParameter(ParameterTypes.LOOKAHEAD)
                            .setDefaultParameter(ParameterTypes.LOOKBACKOLD).setDefaultParameter(ParameterTypes.PERCEPTION)
                            .setDefaultParameter(ParameterTypes.LOOKBACK);
                }
            }
            PerceptionFactory perceptionFactory = new LmrsStrategiesPerceptionFactory();
            // IDM factory with socio desired speed
            class SocioIDMFactory implements CarFollowingModelFactory<IDMPlus>
            {
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
                    return new IDMPlus(AbstractIDM.HEADWAY, new SocioDesiredSpeed(AbstractIDM.DESIRED_SPEED));
                }
            }
            // random parameters
            ParameterFactoryByType parameterFactory = new ParameterFactoryByType();
            if (!baseLMRS)
            {
                parameterFactory.addParameter(Tailgating.RHO, 0.0);
                // 0.15 -> -1.6471
                // 0.25 -> -1.1363
                // 0.35 -> -0.79982
                // 0.50 -> -0.44315
                parameterFactory.addParameter(GTUType.CAR, LmrsParameters.SOCIO, LmrsStrategies.this.sigma);
                // new DistLogNormal(stream, -0.79982, 0.5));
                parameterFactory.addCorrelation(GTUType.CAR, null, LmrsParameters.SOCIO,
                        (first, then) -> then <= 1.0 ? then : 1.0);
                parameterFactory.addParameter(GTUType.TRUCK, LmrsParameters.SOCIO, 1.0); // WATCH IT
                parameterFactory.addParameter(GTUType.CAR, LmrsParameters.VGAIN,
                        // 25km/h -> 3.3789
                        // 35km/h -> 3.7153
                        // 70km/h -> 4.4085
                        Speed.createSI(LmrsStrategies.this.vGain / 3.6));
                // new ContinuousDistDoubleScalar.Rel<>(new DistLogNormal(stream, 3.7153, 0.4), SpeedUnit.KM_PER_HOUR));
                parameterFactory.addParameter(ParameterTypes.TMAX, Duration.createSI(1.5)); // WATCH IT
            }
            // parameterFactory.addParameter(ParameterTypes.TMAX,
            // new ContinuousDistDoubleScalar.Rel<>(new DistLogNormal(stream, 0.25, 0.5), DurationUnit.SECOND));
            // parameterFactory.addCorrelation(ParameterTypes.TMIN, ParameterTypes.TMAX,
            // (first, then) -> Duration.max(first.plus(Duration.createSI(0.000000001)), then));
            parameterFactory.addParameter(GTUType.CAR, ParameterTypes.FSPEED,
                    new DistNormal(stream, 123.7 / 120.0, 12.0 / 120.0));
            parameterFactory.addParameter(GTUType.TRUCK, ParameterTypes.A, Acceleration.createSI(0.4));
            parameterFactory.addParameter(GTUType.TRUCK, ParameterTypes.FSPEED, 1.0);

            // parameterFactory.addParameter(ParameterTypes.TMIN, Duration.createSI(0.8));

            try
            {
                // Strategical factories
                for (GTUType gtuType : new GTUType[] { GTUType.CAR, GTUType.TRUCK })
                {
                    // incentives
                    Set<MandatoryIncentive> mandatoryIncentives = new LinkedHashSet<>();
                    Set<VoluntaryIncentive> voluntaryIncentives = new LinkedHashSet<>();
                    Set<AccelerationIncentive> accelerationIncentives = new LinkedHashSet<>();
                    mandatoryIncentives.add(new IncentiveRoute());
                    voluntaryIncentives.add(new IncentiveSpeedWithCourtesy());
                    voluntaryIncentives.add(new IncentiveKeep());
                    if (!baseLMRS)
                    {
                        voluntaryIncentives.add(new IncentiveSocioSpeed());
                    }
                    // accelerationIncentives.add(new AccelerationNoRightOvertake());
                    if (gtuType.equals(GTUType.TRUCK))
                    {
                        voluntaryIncentives.add(new IncentiveStayRight());
                    }
                    // car-following factory
                    CarFollowingModelFactory<?> cfFactory = // trucks don't change their desired speed
                            gtuType.equals(GTUType.CAR) && !baseLMRS ? new SocioIDMFactory() : new IDMPlusFactory(stream);
                    // tailgating
                    Tailgating tailgating = baseLMRS ? Tailgating.NONE : Tailgating.PRESSURE; // WATCH IT
                    // strategical and tactical factory
                    LaneBasedStrategicalPlannerFactory<?> laneBasedStrategicalPlannerFactory =
                            new LaneBasedStrategicalRoutePlannerFactory(
                                    new LMRSFactory(cfFactory, perceptionFactory, synchronization, cooperation, gapAcceptance,
                                            tailgating, mandatoryIncentives, voluntaryIncentives, accelerationIncentives),
                                    parameterFactory);
                    LmrsStrategies.this.factories.put(gtuType, laneBasedStrategicalPlannerFactory);
                }

                // Network
                OTSPoint3D pointA = new OTSPoint3D(0, 0, 0);
                OTSPoint3D pointB = new OTSPoint3D(4000, 0, 0);
                OTSPoint3D pointC = new OTSPoint3D(6400, 0, 0);
                OTSNode nodeA = new OTSNode(net, "A", pointA);
                OTSNode nodeB = new OTSNode(net, "B", pointB);
                OTSNode nodeC = new OTSNode(net, "C", pointC);
                CrossSectionLink linkAB = new CrossSectionLink(net, "AB", nodeA, nodeB, LinkType.FREEWAY,
                        new OTSLine3D(pointA, pointB), sim, LaneKeepingPolicy.KEEP_RIGHT);
                CrossSectionLink linkBC = new CrossSectionLink(net, "BC", nodeB, nodeC, LinkType.FREEWAY,
                        new OTSLine3D(pointB, pointC), sim, LaneKeepingPolicy.KEEP_RIGHT);
                Lane laneAB1 = new Lane(linkAB, "laneAB1", Length.createSI(0.0), Length.createSI(3.5), LaneType.HIGHWAY,
                        new Speed(120, SpeedUnit.KM_PER_HOUR), new OvertakingConditions.LeftOnly());
                Lane laneAB2 = new Lane(linkAB, "laneAB2", Length.createSI(3.5), Length.createSI(3.5), LaneType.HIGHWAY,
                        new Speed(120, SpeedUnit.KM_PER_HOUR), new OvertakingConditions.LeftOnly());
                Lane laneAB3 = new Lane(linkAB, "laneAB3", Length.createSI(7.0), Length.createSI(3.5), LaneType.HIGHWAY,
                        new Speed(120, SpeedUnit.KM_PER_HOUR), new OvertakingConditions.LeftOnly());
                Lane laneBC1 = new Lane(linkBC, "laneBC1", Length.createSI(0.0), Length.createSI(3.5), LaneType.HIGHWAY,
                        new Speed(120, SpeedUnit.KM_PER_HOUR), new OvertakingConditions.LeftOnly());
                Lane laneBC2 = new Lane(linkBC, "laneBC2", Length.createSI(3.5), Length.createSI(3.5), LaneType.HIGHWAY,
                        new Speed(120, SpeedUnit.KM_PER_HOUR), new OvertakingConditions.LeftOnly());
                Set<GTUType> gtuTypes = new HashSet<>();
                gtuTypes.add(GTUType.VEHICLE);
                Stripe stripeAB1 = new Stripe(linkAB, Length.createSI(-1.75), Length.createSI(0.2));
                Stripe stripeAB2 = new Stripe(linkAB, Length.createSI(1.75), Length.createSI(0.2), gtuTypes, Permeable.BOTH);
                Stripe stripeAB3 = new Stripe(linkAB, Length.createSI(5.25), Length.createSI(0.2), gtuTypes, Permeable.BOTH);
                Stripe stripeAB4 = new Stripe(linkAB, Length.createSI(8.75), Length.createSI(0.2), gtuTypes, Permeable.BOTH);
                Stripe stripeBC1 = new Stripe(linkBC, Length.createSI(-1.75), Length.createSI(0.2), gtuTypes, Permeable.BOTH);
                Stripe stripeBC2 = new Stripe(linkBC, Length.createSI(1.75), Length.createSI(0.2), gtuTypes, Permeable.BOTH);
                Stripe stripeBC3 = new Stripe(linkBC, Length.createSI(5.25), Length.createSI(0.2), gtuTypes, Permeable.BOTH);
                new NodeAnimation(nodeA, sim);
                new NodeAnimation(nodeB, sim);
                new NodeAnimation(nodeC, sim);
                new LinkAnimation(linkAB, sim, 0.5f);
                new LinkAnimation(linkBC, sim, 0.5f);
                new LaneAnimation(laneAB1, sim, Color.GRAY.brighter(), false);
                new LaneAnimation(laneAB2, sim, Color.GRAY.brighter(), false);
                new LaneAnimation(laneAB3, sim, Color.GRAY.brighter(), false);
                new LaneAnimation(laneBC1, sim, Color.GRAY.brighter(), false);
                new LaneAnimation(laneBC2, sim, Color.GRAY.brighter(), false);
                new StripeAnimation(stripeAB1, sim, TYPE.SOLID);
                new StripeAnimation(stripeAB2, sim, TYPE.DASHED);
                new StripeAnimation(stripeAB3, sim, TYPE.DASHED);
                new StripeAnimation(stripeAB4, sim, TYPE.SOLID);
                new StripeAnimation(stripeBC1, sim, TYPE.SOLID);
                new StripeAnimation(stripeBC2, sim, TYPE.DASHED);
                new StripeAnimation(stripeBC3, sim, TYPE.SOLID);
                // sensors
                new SinkSensor(laneBC1, laneBC1.getLength().minus(Length.createSI(100.0)), sim);
                new SinkSensor(laneBC2, laneBC2.getLength().minus(Length.createSI(100.0)), sim);

                // detectors
                Lane[][] grid = new Lane[][] { new Lane[] { laneAB3 }, new Lane[] { laneAB2, laneBC2 },
                        new Lane[] { laneAB1, laneBC1 } };
                Duration aggregationPeriod = Duration.createSI(60.0);
                DetectorMeasurement<?, ?>[] measurements = new DetectorMeasurement[] { Detector.MEAN_SPEED, Detector.PASSAGES,
                        new Detector.PlatoonSizes(Duration.createSI(3.0)) };
                String[] prefix = { "A", "B", "C" };
                for (int i = 0; i < grid.length; i++)
                {
                    int num = 1;
                    Length pos = Length.createSI(100.0);
                    for (int j = 0; j < grid[i].length; j++)
                    {
                        while (pos.lt(grid[i][j].getLength()))
                        {
                            new Detector(String.format("%s%02d", prefix[i], num), grid[i][j], pos, Length.ZERO,
                                    LmrsStrategies.this.simulator, aggregationPeriod, measurements);
                            num++;
                            pos = pos.plus(Length.createSI(100.0));
                        }
                        pos = pos.minus(grid[i][j].getLength());
                    }
                }

                // OD
                Categorization categorization = new Categorization("ODExample", GTUType.class);
                List<Node> origins = new ArrayList<>();
                origins.add(nodeA);
                List<Node> destinations = new ArrayList<>();
                destinations.add(nodeC);
                TimeVector timeVector = new TimeVector(new double[] { 0.0, 2400.0, 3600.0 }, TimeUnit.BASE, StorageType.DENSE);
                ODMatrix od = new ODMatrix("LMRS strategies", origins, destinations, categorization, timeVector,
                        Interpolation.LINEAR);
                FrequencyVector demand =
                        new FrequencyVector(new double[] { 1000.0, 5000.0, 1000.0 }, FrequencyUnit.PER_HOUR, StorageType.DENSE);
                Category category = new Category(categorization, GTUType.CAR);
                od.putDemandVector(nodeA, nodeC, category, demand, timeVector, Interpolation.LINEAR, 1.0 - fTruck);
                category = new Category(categorization, GTUType.TRUCK);
                od.putDemandVector(nodeA, nodeC, category, demand, timeVector, Interpolation.LINEAR, fTruck);
                // options
                MarkovCorrelation<GTUType, Frequency> markov = new MarkovCorrelation<>();
                markov.addState(GTUType.TRUCK, 0.4);
                LaneBiases biases = new LaneBiases().addBias(GTUType.VEHICLE, LaneBias.bySpeed(140, 100)).addBias(GTUType.TRUCK,
                        LaneBias.TRUCK_RIGHT);
                ODOptions odOptions =
                        new ODOptions().set(ODOptions.GTU_COLORER, LmrsStrategies.this.colorer).set(ODOptions.MARKOV, markov)
                                .set(ODOptions.LANE_BIAS, biases).set(ODOptions.NO_LC_DIST, Length.createSI(100.0))
                                .set(ODOptions.GTU_TYPE, new LmrsStrategyCharacteristicsGenerator())
                                .set(ODOptions.HEADWAY_DIST, HeadwayDistribution.CONSTANT);
                Map<String, GeneratorObjects> generatedObjects = ODApplier.applyOD(net, od, sim, odOptions);
                for (String str : generatedObjects.keySet())
                {
                    new GTUGeneratorAnimation(generatedObjects.get(str).getGenerator(), sim);
                }
            }
            catch (NetworkException | OTSGeometryException | NamingException | ValueException | ParameterException
                    | GTUException exception)
            {
                exception.printStackTrace();
            }

        }

        /** {@inheritDoc} */
        @Override
        public SimulatorInterface<Time, Duration, OTSSimTimeDouble> getSimulator() throws RemoteException
        {
            return LmrsStrategies.this.simulator;
        }

        /** {@inheritDoc} */
        @Override
        public OTSNetwork getNetwork()
        {
            return LmrsStrategies.this.network;
        }

    }

    /** {@inheritDoc} */
    @Override
    public void notify(final EventInterface event) throws RemoteException
    {
        if (event.getType().equals(SimulatorInterface.END_OF_REPLICATION_EVENT))
        {
            Detector.writeToFile(this.network, "D:/TU Delft/Post-doc/Projects/LMRS-strategies/pre-tests/detsAggrData"
                    + LmrsStrategies.this.suffix + ".txt", true, "%.3f", CompressionMethod.NONE);
            Detector.writeToFile(this.network, "D:/TU Delft/Post-doc/Projects/LMRS-strategies/pre-tests/detsMesoData"
                    + LmrsStrategies.this.suffix + ".txt", false, "%.3f", CompressionMethod.NONE);
            // solve bug that event is fired twice
            LmrsStrategies.this.simulator.removeListener(LmrsStrategies.this, SimulatorInterface.END_OF_REPLICATION_EVENT);
        }
    }

}
