package strategies;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;

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
import org.djunits.value.vfloat.scalar.FloatLength;
import org.djunits.value.vfloat.scalar.FloatSpeed;
import org.djutils.event.EventInterface;
import org.djutils.event.EventListenerInterface;
import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;
import org.opentrafficsim.base.CompressedFileWriter;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterSet;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.animation.gtu.colorer.AccelerationGTUColorer;
import org.opentrafficsim.core.animation.gtu.colorer.GTUColorer;
import org.opentrafficsim.core.animation.gtu.colorer.IDGTUColorer;
import org.opentrafficsim.core.animation.gtu.colorer.SpeedGTUColorer;
import org.opentrafficsim.core.animation.gtu.colorer.SwitchableGTUColorer;
import org.opentrafficsim.core.compatibility.Compatible;
import org.opentrafficsim.core.dsol.AbstractOTSModel;
import org.opentrafficsim.core.dsol.AbstractOTSSimulationApplication;
import org.opentrafficsim.core.dsol.OTSAnimator;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimulator;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.GTUCharacteristics;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.perception.DirectEgoPerception;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.OTSLink;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.core.parameters.ParameterFactoryByType;
import org.opentrafficsim.core.units.distributions.ContinuousDistDoubleScalar;
import org.opentrafficsim.core.units.distributions.ContinuousDistSpeed;
import org.opentrafficsim.draw.core.OTSDrawingException;
import org.opentrafficsim.draw.gtu.GtuGeneratorQueueAnimation;
import org.opentrafficsim.draw.network.LinkAnimation;
import org.opentrafficsim.draw.network.NodeAnimation;
import org.opentrafficsim.draw.road.LaneAnimation;
import org.opentrafficsim.draw.road.StripeAnimation;
import org.opentrafficsim.draw.road.StripeAnimation.TYPE;
import org.opentrafficsim.kpi.sampling.KpiGtuDirectionality;
import org.opentrafficsim.kpi.sampling.KpiLaneDirection;
import org.opentrafficsim.kpi.sampling.Sampler;
import org.opentrafficsim.kpi.sampling.SpaceTimeRegion;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataTypeDuration;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataTypeLength;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataTypeNumber;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataTypeSpeed;
import org.opentrafficsim.road.gtu.colorer.DesiredHeadwayColorer;
import org.opentrafficsim.road.gtu.colorer.DesiredSpeedColorer;
import org.opentrafficsim.road.gtu.colorer.FixedColor;
import org.opentrafficsim.road.gtu.colorer.GTUTypeColorer;
import org.opentrafficsim.road.gtu.colorer.IncentiveColorer;
import org.opentrafficsim.road.gtu.colorer.SocialPressureColorer;
import org.opentrafficsim.road.gtu.colorer.SynchronizationColorer;
import org.opentrafficsim.road.gtu.colorer.TotalDesireColorer;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions.LaneBias;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions.LaneBiases;
import org.opentrafficsim.road.gtu.generator.GtuGeneratorQueue;
import org.opentrafficsim.road.gtu.generator.MarkovCorrelation;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGTUCharacteristics;
import org.opentrafficsim.road.gtu.generator.headway.ArrivalsHeadwayGenerator.HeadwayDistribution;
import org.opentrafficsim.road.gtu.generator.od.GTUCharacteristicsGeneratorOD;
import org.opentrafficsim.road.gtu.generator.od.ODApplier;
import org.opentrafficsim.road.gtu.generator.od.ODApplier.GeneratorObjects;
import org.opentrafficsim.road.gtu.generator.od.ODOptions;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.VehicleModel;
import org.opentrafficsim.road.gtu.lane.perception.CategoricalLanePerception;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionFactory;
import org.opentrafficsim.road.gtu.lane.perception.categories.AnticipationTrafficPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.DirectInfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.DirectNeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.HeadwayGtuType;
import org.opentrafficsim.road.gtu.lane.plan.operational.LaneChange;
import org.opentrafficsim.road.gtu.lane.tactical.DesireBased;
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
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Incentive;
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
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.OTSRoadNode;
import org.opentrafficsim.road.network.lane.Stripe;
import org.opentrafficsim.road.network.lane.Stripe.Permeable;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;
import org.opentrafficsim.road.network.lane.object.SpeedSign;
import org.opentrafficsim.road.network.lane.object.sensor.Detector;
import org.opentrafficsim.road.network.lane.object.sensor.Detector.CompressionMethod;
import org.opentrafficsim.road.network.lane.object.sensor.Detector.DetectorMeasurement;
import org.opentrafficsim.road.network.lane.object.sensor.SinkSensor;
import org.opentrafficsim.road.network.sampling.GtuData;
import org.opentrafficsim.road.network.sampling.LaneData;
import org.opentrafficsim.road.network.sampling.RoadSampler;
import org.opentrafficsim.swing.gui.AnimationToggles;
import org.opentrafficsim.swing.gui.OTSAnimationPanel;
import org.opentrafficsim.swing.gui.OTSSimulationApplication;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.jstats.distributions.DistLogNormal;
import nl.tudelft.simulation.jstats.distributions.DistNormal;
import nl.tudelft.simulation.jstats.distributions.DistTriangular;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Simulations regarding LMRS lane change strategies. This entails the base LMRS with:
 * <ul>
 * <li>Distributed Tmax</li>
 * <li>Distributed vGain</li>
 * <li>Distributed socio-speed sensitivity parameter (LmrsParameters.SOCIO)</li>
 * <li>Altered gap-acceptance: use own Tmax (GapAcceptance.EGO_HEADWAY) [not required if Tmin/max not distributed]</li>
 * <li>Altered desired speed: increase during overtaking (SocioDesiredSpeed)</li>
 * <li>Lane change incentive to get out of the way (IncentiveSocioSpeed)</li>
 * </ul>
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 2 mrt. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class LmrsStrategies implements EventListenerInterface
{

    /** Simulation time. */
    static final Time SIMTIME = Time.instantiateSI(3900);

    /** Truck fraction. */
    private double fTruck;

    /** Synchronization. */
    static final Synchronization SYNCHRONIZATION = Synchronization.PASSIVE;

    /** Cooperation. */
    static final Cooperation COOPERATION = Cooperation.PASSIVE;

    /** Gap-acceptance. */
    static final GapAcceptance GAPACCEPTANCE = GapAcceptance.INFORMED;

    /** Use base LMRS. */
    private boolean baseLMRS;

    /** Form of tailgating. */
    private Tailgating tailgating;

    /** Seed. */
    private long seed;

    /** Sigma. */
    private double sigma;

    /** vGain [km/h] (after log-normal shift). */
    private double vGain;

    /** Maximum headway [s]. */
    private double tMax;

    /** Maximum flow [veh/h]. */
    private double qMax;

    /** Suffix for file name. */
    private String suffix;

    /** Folder to save files. */
    private String folder;

    /** Strategical planner factories per GTU type. */
    private final Map<GTUType, LaneBasedStrategicalPlannerFactory<?>> factories = new LinkedHashMap<>();

    /** The simulator. */
    private DEVSSimulatorInterface.TimeDoubleUnit simulator;

    /** The network. */
    private OTSRoadNetwork network;

    /** Autorun. */
    private boolean autorun;

    /** List of lane changes. */
    private List<String> laneChanges = new ArrayList<>();

    /** Sample data or not. */
    private boolean sampling;

    /** Sampler when sampling. */
    private Sampler<GtuData> sampler;

    /** GTU colorer. */
    private static final GTUColorer colorer = SwitchableGTUColorer.builder()
            .addActiveColorer(new FixedColor(Color.BLUE, "Blue")).addColorer(GTUTypeColorer.DEFAULT)
            .addColorer(new IDGTUColorer()).addColorer(new SpeedGTUColorer(new Speed(150, SpeedUnit.KM_PER_HOUR)))
            .addColorer(new DesiredSpeedColorer(new Speed(80, SpeedUnit.KM_PER_HOUR), new Speed(150, SpeedUnit.KM_PER_HOUR)))
            .addColorer(new AccelerationGTUColorer(Acceleration.instantiateSI(-6.0), Acceleration.instantiateSI(2)))
            .addColorer(new SynchronizationColorer())
            .addColorer(new DesiredHeadwayColorer(Duration.instantiateSI(0.5), Duration.instantiateSI(2.0)))
            .addColorer(new TotalDesireColorer()).addColorer(new IncentiveColorer(IncentiveRoute.class))
            .addColorer(new IncentiveColorer(IncentiveStayRight.class))
            .addColorer(new IncentiveColorer(IncentiveSpeedWithCourtesy.class))
            .addColorer(new IncentiveColorer(IncentiveKeep.class)).addColorer(new IncentiveColorer(IncentiveSocioSpeed.class))
            .addColorer(new SocialPressureColorer()).build();

    /**
     * Main method with command line arguments.
     * @param args String[]; String[] command line arguments
     */
    @SuppressWarnings("unchecked")
    public static void main(final String[] args)
    {
        LaneChange.MIN_LC_LENGTH_FACTOR = 1.0;

        // default properties
        boolean autorun = false;
        String suffix = "";
        long seed = 1L;
        double sigma = 0.1; // 0.25;
        double vGain = 3.3789;
        // 25km/h -> 3.3789
        // 35km/h -> 3.7153
        // 50km/h -> 4.072
        // 70km/h -> 4.4085
        // base: 69.6km/h
        boolean baseLMRS = false;
        double tMax = 1.6;
        double fTruck = 0.1;
        double qMax = 5500;
        String folder = "D:/";
        boolean sampling = false;
        Tailgating tailgating = Tailgating.PRESSURE;

        boolean vGainSet = false;

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
                    autorun = Boolean.parseBoolean(value);
                }
                else if ("suffix".equalsIgnoreCase(key))
                {
                    suffix = value;
                }
                else if ("seed".equalsIgnoreCase(key))
                {
                    seed = Long.parseLong(value);
                }
                else if ("sigma".equalsIgnoreCase(key))
                {
                    sigma = Double.parseDouble(value);
                }
                else if ("vgain".equalsIgnoreCase(key))
                {
                    vGain = Double.parseDouble(value);
                    vGainSet = true;
                }
                else if ("baselmrs".equalsIgnoreCase(key))
                {
                    baseLMRS = Boolean.parseBoolean(value);
                    if (baseLMRS && !vGainSet)
                    {
                        vGain = Try.assign(() -> LmrsParameters.VGAIN.getDefaultValue().getInUnit(SpeedUnit.KM_PER_HOUR), "");
                    }
                }
                else if ("tmax".equalsIgnoreCase(key))
                {
                    tMax = Double.parseDouble(value);
                }
                else if ("ftruck".equalsIgnoreCase(key))
                {
                    fTruck = Double.parseDouble(value);
                }
                else if ("qmax".equalsIgnoreCase(key))
                {
                    qMax = Double.parseDouble(value);
                }
                else if ("folder".equalsIgnoreCase(key))
                {
                    folder = value;
                }
                else if ("sampling".equalsIgnoreCase(key))
                {
                    sampling = Boolean.parseBoolean(value);
                }
                else if ("tailgating".equalsIgnoreCase(key))
                {
                    // overrule for sensitivity analysis
                    tailgating = value.equalsIgnoreCase("none") ? Tailgating.NONE
                            : (value.equalsIgnoreCase("pressure") ? Tailgating.PRESSURE : Tailgating.RHO_ONLY);
                }
                else
                {
                    throw new RuntimeException("Key " + key + " not supported.");
                }
            }
        }
        Throw.whenNull(folder, "Provide a folder to save files using a command line argument named 'folder'.");

        // setup arguments
        LmrsStrategies lmrsStrategies = new LmrsStrategies();
        lmrsStrategies.autorun = autorun;
        lmrsStrategies.suffix = suffix;
        lmrsStrategies.seed = seed;
        lmrsStrategies.sigma = sigma;
        lmrsStrategies.vGain = vGain;
        lmrsStrategies.baseLMRS = baseLMRS;
        lmrsStrategies.tailgating = tailgating;
        lmrsStrategies.tMax = tMax;
        lmrsStrategies.fTruck = fTruck;
        lmrsStrategies.qMax = qMax;
        lmrsStrategies.folder = folder;
        lmrsStrategies.sampling = sampling;
        if (baseLMRS)
        {
            lmrsStrategies.incentives =
                    new Class[] {IncentiveRoute.class, IncentiveSpeedWithCourtesy.class, IncentiveKeep.class};
        }
        else
        {
            lmrsStrategies.incentives = new Class[] {IncentiveRoute.class, IncentiveSpeedWithCourtesy.class,
                    IncentiveKeep.class, IncentiveSocioSpeed.class};
        }

        // run
        if (autorun)
        {
            try
            {
                OTSSimulator simulator = new OTSSimulator("LmrsStrategies");
                final LmrsStrategiesModel lmrsModel = lmrsStrategies.new LmrsStrategiesModel(simulator);
                // + 1e-9 is a hack to allow step() to perform detector aggregation of more than 1 detectors -at- the sim end
                simulator.initialize(Time.ZERO, Duration.ZERO, Duration.instantiateSI(SIMTIME.si + 1e-9), lmrsModel);
                lmrsStrategies.new LmrsStrategiesSimulation(lmrsModel);
                double tReport = 60.0;
                Time t = simulator.getSimulatorTime();
                while (t.le(SIMTIME))
                {
                    simulator.step();
                    t = simulator.getSimulatorTime();
                    if (t.si >= tReport)
                    {
                        System.out.println("Simulation time is " + t);
                        tReport += 60.0;
                    }
                }
                simulator.stop(); // end of simulation event
            }
            catch (Exception exception)
            {
                exception.printStackTrace();
                System.exit(-1);
            }
        }
        else
        {
            try
            {
                OTSAnimator simulator = new OTSAnimator("LmrsStrategies");
                final LmrsStrategiesModel lmrsModel = lmrsStrategies.new LmrsStrategiesModel(simulator);
                // + 1e-9 is a hack to allow step() to perform detector aggregation of more than 1 detectors -at- the sim end
                simulator.initialize(Time.ZERO, Duration.ZERO, Duration.instantiateSI(SIMTIME.si + 1e-9), lmrsModel);
                OTSAnimationPanel animationPanel = new OTSAnimationPanel(lmrsModel.getNetwork().getExtent(),
                        new Dimension(800, 600), simulator, lmrsModel, LmrsStrategies.colorer, lmrsModel.getNetwork());
                lmrsStrategies.new LmrsStrategiesAnimation(lmrsModel, animationPanel);
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
     * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 21 mrt. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    class LmrsStrategiesSimulation extends AbstractOTSSimulationApplication
    {
        /** */
        private static final long serialVersionUID = 1L;

        /**
         * @param model OTSModelInterface; model
         */
        LmrsStrategiesSimulation(final OTSModelInterface model)
        {
            super(model);
        }
    }

    /**
     * Animator.
     * <p>
     * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 3 mrt. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    class LmrsStrategiesAnimation extends OTSSimulationApplication<OTSModelInterface>
    {
        /** */
        private static final long serialVersionUID = 20180303L;

        /**
         * @param model OTSModelInterface; the model
         * @param panel OTSAnimationPanel; the animation panel
         * @throws OTSDrawingException on animation error
         */
        LmrsStrategiesAnimation(final OTSModelInterface model, final OTSAnimationPanel panel) throws OTSDrawingException
        {
            super(model, panel);
        }

        /** {@inheritDoc} */
        @Override
        protected void setAnimationToggles()
        {
            AnimationToggles.setIconAnimationTogglesFull(getAnimationPanel());
            getAnimationPanel().getAnimationPanel().toggleClass(OTSLink.class);
            getAnimationPanel().getAnimationPanel().toggleClass(OTSNode.class);
            getAnimationPanel().getAnimationPanel().toggleClass(GtuGeneratorQueue.class);
            getAnimationPanel().getAnimationPanel().showClass(SpeedSign.class);
        }

    }

    /**
     * LMRS model.
     * <p>
     * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 3 mrt. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    class LmrsStrategiesModel extends AbstractOTSModel
    {
        /**
         * @param simulator OTSSimulatorInterface; the simulator
         */
        LmrsStrategiesModel(final OTSSimulatorInterface simulator)
        {
            super(simulator);
        }

        /** */
        private static final long serialVersionUID = 20180303L;

        /** {@inheritDoc} */
        @Override
        @SuppressWarnings({"synthetic-access", "checkstyle:methodlength"})
        public void constructModel()
        {
            LmrsStrategies.this.simulator = getSimulator();
            OTSRoadNetwork net = new OTSRoadNetwork("LMRS strategies", true, getSimulator());
            try
            {
                LmrsStrategies.this.simulator.addListener(LmrsStrategies.this, SimulatorInterface.END_REPLICATION_EVENT);
            }
            catch (RemoteException exception1)
            {
                exception1.printStackTrace();
            }
            LmrsStrategies.this.network = net;
            net.addListener(LmrsStrategies.this, Network.GTU_ADD_EVENT);
            net.addListener(LmrsStrategies.this, Network.GTU_REMOVE_EVENT);
            Map<String, StreamInterface> streams = new LinkedHashMap<>();
            StreamInterface stream = new MersenneTwister(LmrsStrategies.this.seed);
            streams.put("generation", stream);
            getSimulator().getReplication().setStreams(streams);

            // Vehicle-driver classes
            // characteristics generator using the input available in this context
            /** Characteristics generator. */
            class LmrsStrategyCharacteristicsGenerator implements GTUCharacteristicsGeneratorOD
            {

                /** Distributed maximum speed for trucks. */
                private ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> vTruck;

                /**
                 * Constructor.
                 * @param strm StreamInterface;
                 */
                LmrsStrategyCharacteristicsGenerator(final StreamInterface strm)
                {
                    this.vTruck = new ContinuousDistDoubleScalar.Rel<>(new DistNormal(strm, 85.0, 2.5), SpeedUnit.KM_PER_HOUR);
                }

                /** {@inheritDoc} */
                @Override
                public LaneBasedGTUCharacteristics draw(final Node origin, final Node destination, final Category category,
                        final StreamInterface randomStream) throws GTUException
                {
                    GTUType gtuType = category.get(GTUType.class);
                    GTUCharacteristics gtuCharacteristics =
                            Try.assign(() -> GTUType.defaultCharacteristics(gtuType, LmrsStrategies.this.network, randomStream),
                                    "Exception while applying default GTU characteristics.");
                    if (gtuType.equals(LmrsStrategies.this.network.getGtuType(GTUType.DEFAULTS.TRUCK)))
                    {
                        gtuCharacteristics = new GTUCharacteristics(
                                LmrsStrategies.this.network.getGtuType(GTUType.DEFAULTS.TRUCK), gtuCharacteristics.getLength(),
                                gtuCharacteristics.getWidth(), this.vTruck.draw(), gtuCharacteristics.getMaximumAcceleration(),
                                gtuCharacteristics.getMaximumDeceleration(), gtuCharacteristics.getFront());
                    }
                    return new LaneBasedGTUCharacteristics(gtuCharacteristics, LmrsStrategies.this.factories.get(gtuType), null,
                            origin, destination, VehicleModel.NONE);
                }
            }
            /** Perception factory. */
            class LmrsStrategiesPerceptionFactory implements PerceptionFactory
            {
                /** {@inheritDoc} */
                @Override
                public LanePerception generatePerception(final LaneBasedGTU gtu)
                {
                    LanePerception perception = new CategoricalLanePerception(gtu);
                    perception.addPerceptionCategory(new DirectEgoPerception<>(perception));
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
            /** IDM factory with socio speed. */
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
            parameterFactory.addParameter(Tailgating.RHO, 0.0);
            if (!LmrsStrategies.this.baseLMRS)
            {
                parameterFactory.addParameter(LmrsStrategies.this.network.getGtuType(GTUType.DEFAULTS.CAR),
                        LmrsParameters.SOCIO, new DistTriangular(stream, 0.0, LmrsStrategies.this.sigma, 1.0));
                parameterFactory.addCorrelation(LmrsStrategies.this.network.getGtuType(GTUType.DEFAULTS.CAR), null,
                        LmrsParameters.SOCIO, (first, then) -> then <= 1.0 ? then : 1.0);
                parameterFactory.addParameter(LmrsStrategies.this.network.getGtuType(GTUType.DEFAULTS.TRUCK),
                        LmrsParameters.SOCIO, 1.0);
                parameterFactory.addParameter(LmrsStrategies.this.network.getGtuType(GTUType.DEFAULTS.CAR),
                        LmrsParameters.VGAIN, new ContinuousDistSpeed(new DistLogNormal(stream, LmrsStrategies.this.vGain, 0.4),
                                SpeedUnit.KM_PER_HOUR));
                parameterFactory.addParameter(LmrsStrategies.this.network.getGtuType(GTUType.DEFAULTS.TRUCK),
                        LmrsParameters.VGAIN, new Speed(50.0, SpeedUnit.KM_PER_HOUR));
                parameterFactory.addParameter(ParameterTypes.TMAX, Duration.instantiateSI(LmrsStrategies.this.tMax));
            }
            else
            {
                // overrule for sensitivity analysis
                parameterFactory.addParameter(LmrsStrategies.this.network.getGtuType(GTUType.DEFAULTS.CAR),
                        LmrsParameters.VGAIN, new Speed(LmrsStrategies.this.vGain, SpeedUnit.KM_PER_HOUR));
            }
            parameterFactory.addParameter(LmrsStrategies.this.network.getGtuType(GTUType.DEFAULTS.CAR), ParameterTypes.FSPEED,
                    new DistNormal(stream, 123.7 / 120.0, 12.0 / 120.0));
            parameterFactory.addParameter(LmrsStrategies.this.network.getGtuType(GTUType.DEFAULTS.TRUCK), ParameterTypes.A,
                    Acceleration.instantiateSI(0.4));
            parameterFactory.addParameter(LmrsStrategies.this.network.getGtuType(GTUType.DEFAULTS.TRUCK), ParameterTypes.FSPEED,
                    1.0);

            try
            {
                // Strategical factories
                for (GTUType gtuType : new GTUType[] {LmrsStrategies.this.network.getGtuType(GTUType.DEFAULTS.CAR),
                        LmrsStrategies.this.network.getGtuType(GTUType.DEFAULTS.TRUCK)})
                {
                    // incentives
                    Set<MandatoryIncentive> mandatoryIncentives = new LinkedHashSet<>();
                    Set<VoluntaryIncentive> voluntaryIncentives = new LinkedHashSet<>();
                    Set<AccelerationIncentive> accelerationIncentives = new LinkedHashSet<>();
                    mandatoryIncentives.add(new IncentiveRoute());
                    voluntaryIncentives.add(new IncentiveSpeedWithCourtesy());
                    voluntaryIncentives.add(new IncentiveKeep()); // before socio-speed and stay-right
                    if (!LmrsStrategies.this.baseLMRS)
                    {
                        voluntaryIncentives.add(new IncentiveSocioSpeed());
                    }
                    // accelerationIncentives.add(new AccelerationNoRightOvertake());
                    if (gtuType.equals(LmrsStrategies.this.network.getGtuType(GTUType.DEFAULTS.TRUCK)))
                    {
                        voluntaryIncentives.add(new IncentiveStayRight());
                    }
                    // car-following factory
                    CarFollowingModelFactory<?> cfFactory = // trucks don't change their desired speed
                            gtuType.equals(LmrsStrategies.this.network.getGtuType(GTUType.DEFAULTS.CAR))
                                    && !LmrsStrategies.this.baseLMRS ? new SocioIDMFactory() : new IDMPlusFactory(stream);
                    // tailgating
                    Tailgating tlgt = LmrsStrategies.this.baseLMRS ? Tailgating.NONE : LmrsStrategies.this.tailgating;
                    // strategical and tactical factory
                    LaneBasedStrategicalPlannerFactory<?> laneBasedStrategicalPlannerFactory =
                            new LaneBasedStrategicalRoutePlannerFactory(
                                    new LMRSFactory(cfFactory, perceptionFactory, SYNCHRONIZATION, COOPERATION, GAPACCEPTANCE,
                                            tlgt, mandatoryIncentives, voluntaryIncentives, accelerationIncentives),
                                    parameterFactory);
                    LmrsStrategies.this.factories.put(gtuType, laneBasedStrategicalPlannerFactory);
                }

                // Network
                OTSPoint3D pointA = new OTSPoint3D(0, 0, 0);
                OTSPoint3D pointB = new OTSPoint3D(4000, 0, 0);
                OTSPoint3D pointC = new OTSPoint3D(7400, 0, 0);
                OTSRoadNode nodeA = new OTSRoadNode(net, "A", pointA, Direction.ZERO);
                OTSRoadNode nodeB = new OTSRoadNode(net, "B", pointB, Direction.ZERO);
                OTSRoadNode nodeC = new OTSRoadNode(net, "C", pointC, Direction.ZERO);
                CrossSectionLink linkAB = new CrossSectionLink(net, "AB", nodeA, nodeB,
                        LmrsStrategies.this.network.getLinkType(LinkType.DEFAULTS.FREEWAY), new OTSLine3D(pointA, pointB),
                        getSimulator(), LaneKeepingPolicy.KEEPRIGHT);
                CrossSectionLink linkBC = new CrossSectionLink(net, "BC", nodeB, nodeC,
                        LmrsStrategies.this.network.getLinkType(LinkType.DEFAULTS.FREEWAY), new OTSLine3D(pointB, pointC),
                        getSimulator(), LaneKeepingPolicy.KEEPRIGHT);
                Lane laneAB1 = new Lane(linkAB, "laneAB1", Length.instantiateSI(0.0), Length.instantiateSI(3.5),
                        LmrsStrategies.this.network.getLaneType(LaneType.DEFAULTS.HIGHWAY),
                        new Speed(120, SpeedUnit.KM_PER_HOUR));
                Lane laneAB2 = new Lane(linkAB, "laneAB2", Length.instantiateSI(3.5), Length.instantiateSI(3.5),
                        LmrsStrategies.this.network.getLaneType(LaneType.DEFAULTS.HIGHWAY),
                        new Speed(120, SpeedUnit.KM_PER_HOUR));
                Lane laneAB3 = new Lane(linkAB, "laneAB3", Length.instantiateSI(7.0), Length.instantiateSI(3.5),
                        LmrsStrategies.this.network.getLaneType(LaneType.DEFAULTS.HIGHWAY),
                        new Speed(120, SpeedUnit.KM_PER_HOUR));
                Lane laneBC1 = new Lane(linkBC, "laneBC1", Length.instantiateSI(0.0), Length.instantiateSI(3.5),
                        LmrsStrategies.this.network.getLaneType(LaneType.DEFAULTS.HIGHWAY),
                        new Speed(120, SpeedUnit.KM_PER_HOUR));
                Lane laneBC2 = new Lane(linkBC, "laneBC2", Length.instantiateSI(3.5), Length.instantiateSI(3.5),
                        LmrsStrategies.this.network.getLaneType(LaneType.DEFAULTS.HIGHWAY),
                        new Speed(120, SpeedUnit.KM_PER_HOUR));
                Set<GTUType> gtuTypes = new LinkedHashSet<>();
                gtuTypes.add(LmrsStrategies.this.network.getGtuType(GTUType.DEFAULTS.VEHICLE));
                Stripe stripeAB1 =
                        new Stripe(linkAB, Length.instantiateSI(-1.75), Length.instantiateSI(-1.75), Length.instantiateSI(0.2));
                Stripe stripeAB2 = new Stripe(linkAB, Length.instantiateSI(1.75), Length.instantiateSI(1.75),
                        Length.instantiateSI(0.2), gtuTypes, Permeable.BOTH);
                Stripe stripeAB3 = new Stripe(linkAB, Length.instantiateSI(5.25), Length.instantiateSI(5.25),
                        Length.instantiateSI(0.2), gtuTypes, Permeable.BOTH);
                Stripe stripeAB4 = new Stripe(linkAB, Length.instantiateSI(8.75), Length.instantiateSI(8.75),
                        Length.instantiateSI(0.2), gtuTypes, Permeable.BOTH);
                Stripe stripeBC1 = new Stripe(linkBC, Length.instantiateSI(-1.75), Length.instantiateSI(-1.75),
                        Length.instantiateSI(0.2), gtuTypes, Permeable.BOTH);
                Stripe stripeBC2 = new Stripe(linkBC, Length.instantiateSI(1.75), Length.instantiateSI(1.75),
                        Length.instantiateSI(0.2), gtuTypes, Permeable.BOTH);
                Stripe stripeBC3 = new Stripe(linkBC, Length.instantiateSI(5.25), Length.instantiateSI(5.25),
                        Length.instantiateSI(0.2), gtuTypes, Permeable.BOTH);
                new NodeAnimation(nodeA, getSimulator());
                new NodeAnimation(nodeB, getSimulator());
                new NodeAnimation(nodeC, getSimulator());
                new LinkAnimation(linkAB, getSimulator(), 0.5f);
                new LinkAnimation(linkBC, getSimulator(), 0.5f);
                new LaneAnimation(laneAB1, getSimulator(), Color.GRAY.brighter());
                new LaneAnimation(laneAB2, getSimulator(), Color.GRAY.brighter());
                new LaneAnimation(laneAB3, getSimulator(), Color.GRAY.brighter());
                new LaneAnimation(laneBC1, getSimulator(), Color.GRAY.brighter());
                new LaneAnimation(laneBC2, getSimulator(), Color.GRAY.brighter());
                new StripeAnimation(stripeAB1, getSimulator(), TYPE.SOLID);
                new StripeAnimation(stripeAB2, getSimulator(), TYPE.DASHED);
                new StripeAnimation(stripeAB3, getSimulator(), TYPE.DASHED);
                new StripeAnimation(stripeAB4, getSimulator(), TYPE.SOLID);
                new StripeAnimation(stripeBC1, getSimulator(), TYPE.SOLID);
                new StripeAnimation(stripeBC2, getSimulator(), TYPE.DASHED);
                new StripeAnimation(stripeBC3, getSimulator(), TYPE.SOLID);
                // sensors
                new SinkSensor(laneBC1, laneBC1.getLength().minus(Length.instantiateSI(100.0)), Compatible.EVERYTHING,
                        getSimulator());
                new SinkSensor(laneBC2, laneBC2.getLength().minus(Length.instantiateSI(100.0)), Compatible.EVERYTHING,
                        getSimulator());

                // detectors
                Lane[][] grid =
                        new Lane[][] {new Lane[] {laneAB3}, new Lane[] {laneAB2, laneBC2}, new Lane[] {laneAB1, laneBC1}};
                Duration aggregationPeriod = Duration.instantiateSI(60.0);
                DetectorMeasurement<?, ?>[] measurements = new DetectorMeasurement[] {Detector.MEAN_SPEED, Detector.PASSAGES,
                        new VGainMeasurement(), new SigmaMeasurement(), new VDesMeasurement(), new VDes0Measurement()};
                String[] prefix = {"A", "B", "C"};
                for (int i = 0; i < grid.length; i++)
                {
                    int num = 1;
                    Length pos = Length.instantiateSI(100.0);
                    for (int j = 0; j < grid[i].length; j++)
                    {
                        while (pos.lt(grid[i][j].getLength()))
                        {
                            new Detector(String.format("%s%02d", prefix[i], num), grid[i][j], pos, Length.ZERO,
                                    LmrsStrategies.this.simulator, aggregationPeriod, measurements);
                            num++;
                            pos = pos.plus(Length.instantiateSI(100.0));
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
                TimeVector timeVector = DoubleVector.instantiate(new double[] {0.0, 300.0, 2700.0, SIMTIME.si},
                        TimeUnit.DEFAULT, StorageType.DENSE);
                ODMatrix od = new ODMatrix("LMRS strategies", origins, destinations, categorization, timeVector,
                        Interpolation.LINEAR);
                double q = LmrsStrategies.this.qMax;
                FrequencyVector demand = DoubleVector.instantiate(new double[] {q * .6, q * .6, q, 0.0}, FrequencyUnit.PER_HOUR,
                        StorageType.DENSE);
                Category category = new Category(categorization, LmrsStrategies.this.network.getGtuType(GTUType.DEFAULTS.CAR));
                od.putDemandVector(nodeA, nodeC, category, demand, timeVector, Interpolation.LINEAR,
                        1.0 - LmrsStrategies.this.fTruck);
                category = new Category(categorization, LmrsStrategies.this.network.getGtuType(GTUType.DEFAULTS.TRUCK));
                od.putDemandVector(nodeA, nodeC, category, demand, timeVector, Interpolation.LINEAR,
                        LmrsStrategies.this.fTruck);
                // options
                MarkovCorrelation<GTUType, Frequency> markov = new MarkovCorrelation<>();
                markov.addState(LmrsStrategies.this.network.getGtuType(GTUType.DEFAULTS.TRUCK), 0.4);
                LaneBiases biases = new LaneBiases()
                        .addBias(LmrsStrategies.this.network.getGtuType(GTUType.DEFAULTS.VEHICLE), LaneBias.bySpeed(140, 100))
                        .addBias(LmrsStrategies.this.network.getGtuType(GTUType.DEFAULTS.TRUCK), LaneBias.TRUCK_RIGHT);
                ODOptions odOptions = new ODOptions().set(ODOptions.MARKOV, markov)
                        .set(ODOptions.getLaneBiasOption(LmrsStrategies.this.network), biases)
                        .set(ODOptions.NO_LC_DIST, Length.instantiateSI(100.0)).set(ODOptions.INSTANT_LC, true)
                        .set(ODOptions.GTU_TYPE, new LmrsStrategyCharacteristicsGenerator(stream))
                        .set(ODOptions.HEADWAY_DIST, HeadwayDistribution.CONSTANT);
                Map<String, GeneratorObjects> generatedObjects = ODApplier.applyOD(net, od, getSimulator(), odOptions);
                for (String str : generatedObjects.keySet())
                {
                    new GtuGeneratorQueueAnimation(generatedObjects.get(str).getGenerator(), getSimulator());
                }

                // Sampler
                if (LmrsStrategies.this.sampling)
                {
                    LmrsStrategies.this.sampler = new RoadSampler(LmrsStrategies.this.simulator);
                    addLaneToSampler(laneAB1);
                    addLaneToSampler(laneAB2);
                    addLaneToSampler(laneAB3);
                    addLaneToSampler(laneBC1);
                    addLaneToSampler(laneBC2);
                    LmrsStrategies.this.sampler.registerExtendedDataType(new ExtendedDataTypeLength<GtuData>("Length")
                    {
                        @Override
                        public FloatLength getValue(final GtuData gtu)
                        {
                            return FloatLength.instantiateSI((float) gtu.getGtu().getLength().si);
                        }
                    });
                    LmrsStrategies.this.sampler.registerExtendedDataType(new ExtendedDataTypeNumber<GtuData>("Rho")
                    {
                        @Override
                        public Float getValue(final GtuData gtu)
                        {
                            try
                            {
                                return gtu.getGtu().getParameters().getParameter(Tailgating.RHO).floatValue();
                            }
                            catch (ParameterException exception)
                            {
                                throw new RuntimeException("Could not obtain rho for trajectory.", exception);
                            }
                        }
                    });
                    LmrsStrategies.this.sampler.registerExtendedDataType(new ExtendedDataTypeSpeed<GtuData>("V0")
                    {
                        @Override
                        public FloatSpeed getValue(final GtuData gtu)
                        {
                            try
                            {
                                return FloatSpeed.instantiateSI(gtu.getGtu().getDesiredSpeed().floatValue());
                            }
                            catch (NullPointerException ex)
                            {
                                return FloatSpeed.NaN;
                            }
                        }
                    });
                    LmrsStrategies.this.sampler.registerExtendedDataType(new ExtendedDataTypeDuration<GtuData>("T")
                    {
                        @Override
                        public FloatDuration getValue(final GtuData gtu)
                        {
                            try
                            {
                                return FloatDuration.instantiateSI(
                                        gtu.getGtu().getParameters().getParameter(ParameterTypes.T).floatValue());
                            }
                            catch (ParameterException exception)
                            {
                                throw new RuntimeException("Could not obtain T for trajectory.", exception);
                            }
                        }
                    });
                }
            }
            catch (NetworkException | OTSGeometryException | NamingException | ValueRuntimeException | ParameterException
                    | RemoteException | SimRuntimeException exception)
            {
                exception.printStackTrace();
            }
        }

        /**
         * Adds a lane to the sampler.
         * @param lane Lane; lane
         */
        @SuppressWarnings("synthetic-access")
        private void addLaneToSampler(final Lane lane)
        {
            LmrsStrategies.this.sampler.registerSpaceTimeRegion(
                    new SpaceTimeRegion(new KpiLaneDirection(new LaneData(lane), KpiGtuDirectionality.DIR_PLUS), Length.ZERO,
                            lane.getLength(), Time.instantiateSI(300), SIMTIME));
        }

        /** {@inheritDoc} */
        @SuppressWarnings("synthetic-access")
        @Override
        public OTSRoadNetwork getNetwork()
        {
            return LmrsStrategies.this.network;
        }

        /** {@inheritDoc} */
        @Override
        public Serializable getSourceId()
        {
            return "LmrsStrategiesModel";
        }

    }

    /** Incentives. */
    private Class<? extends Incentive>[] incentives;

    /** {@inheritDoc} */
    @Override
    public void notify(final EventInterface event) throws RemoteException
    {
        if (event.getType().equals(LaneBasedGTU.LANE_CHANGE_EVENT))
        {
            Object[] payload = (Object[]) event.getContent();
            GTU gtu = this.network.getGTU((String) payload[0]);
            LateralDirectionality dir = (LateralDirectionality) payload[1];
            DirectedLanePosition from = (DirectedLanePosition) payload[2];
            DesireBased desire = (DesireBased) gtu.getTacticalPlanner();
            Double dMax = Double.NEGATIVE_INFINITY;
            String cause = "Unknown";
            for (Class<? extends Incentive> incentive : this.incentives)
            {
                double d = desire.getLatestDesire(incentive).get(dir);
                if (d > dMax)
                {
                    cause = incentive.getSimpleName();
                    dMax = d;
                }
            }
            this.laneChanges.add(String.format("%.3f,%s,%.3f,%s,%s", this.simulator.getSimulatorTime().si,
                    from.getLane().getFullId(), from.getPosition().si, dir, cause));
        }
        else if (event.getType().equals(Network.GTU_ADD_EVENT))
        {
            this.network.getGTU((String) event.getContent()).addListener(this, LaneBasedGTU.LANE_CHANGE_EVENT);
        }
        else if (event.getType().equals(Network.GTU_REMOVE_EVENT))
        {
            this.network.getGTU((String) event.getContent()).removeListener(this, LaneBasedGTU.LANE_CHANGE_EVENT);
        }
        else if (event.getType().equals(SimulatorInterface.END_REPLICATION_EVENT))
        {
            CompressionMethod compression = this.autorun ? CompressionMethod.ZIP : CompressionMethod.NONE;
            // write detector data
            Detector.writeToFile(this.network, this.folder + "detsAggrData" + LmrsStrategies.this.suffix + ".txt", true, "%.3f",
                    compression);
            Detector.writeToFile(this.network, this.folder + "detsMesoData" + LmrsStrategies.this.suffix + ".txt", false,
                    "%.3f", compression);
            // write lane change data
            this.laneChanges.add(0, "t[s],lane,x[m],dir,cause");
            BufferedWriter bw = CompressedFileWriter.create(this.folder + "laneChanges" + LmrsStrategies.this.suffix + ".txt",
                    this.autorun);
            try
            {
                for (String str : this.laneChanges)
                {
                    bw.write(str);
                    bw.newLine();
                }
            }
            catch (IOException exception)
            {
                throw new RuntimeException("Could not write to file.", exception);
            }
            finally
            {
                try
                {
                    if (bw != null)
                    {
                        bw.close();
                    }
                }
                catch (IOException ex)
                {
                    ex.printStackTrace();
                }
            }
            // write sampler data
            if (LmrsStrategies.this.sampling)
            {
                LmrsStrategies.this.sampler.writeToFile(this.folder + "sampled" + LmrsStrategies.this.suffix + ".txt");
            }
            // solve bug that event is fired twice
            LmrsStrategies.this.simulator.removeListener(LmrsStrategies.this, SimulatorInterface.END_REPLICATION_EVENT);
            // beep
            if (!this.autorun)
            {
                Toolkit.getDefaultToolkit().beep();
            }
            else
            {
                System.exit(0);
            }
        }
    }

    /**
     * Class to store sigma value.
     * <p>
     * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 3 mei 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    class SigmaMeasurement implements DetectorMeasurement<List<Double>, List<Double>>
    {
        /** {@inheritDoc} */
        @Override
        public List<Double> identity()
        {
            return new ArrayList<>();
        }

        /** {@inheritDoc} */
        @Override
        public List<Double> accumulateEntry(final List<Double> cumulative, final LaneBasedGTU gtu, final Detector loopDetector)
        {
            Double sig = gtu.getParameters().getParameterOrNull(LmrsParameters.SOCIO);
            if (sig == null)
            {
                cumulative.add(Double.NaN);
            }
            else
            {
                cumulative.add(sig);
            }
            return cumulative;
        }

        /** {@inheritDoc} */
        @Override
        public List<Double> accumulateExit(final List<Double> cumulative, final LaneBasedGTU gtu, final Detector loopDetector)
        {
            return cumulative;
        }

        /** {@inheritDoc} */
        @Override
        public boolean isPeriodic()
        {
            return false;
        }

        /** {@inheritDoc} */
        @Override
        public List<Double> aggregate(final List<Double> cumulative, final int count, final Duration aggregation)
        {
            return cumulative;
        }

        /** {@inheritDoc} */
        @Override
        public String getName()
        {
            return "sigma";
        }

        /** {@inheritDoc} */
        @Override
        public String stringValue(final List<Double> aggregate, final String format)
        {
            return Detector.printListDouble(aggregate, format);
        }
    }

    /**
     * Class to store vGain value.
     * <p>
     * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 3 mei 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    class VGainMeasurement implements DetectorMeasurement<List<Double>, List<Double>>
    {
        /** {@inheritDoc} */
        @Override
        public List<Double> identity()
        {
            return new ArrayList<>();
        }

        /** {@inheritDoc} */
        @Override
        public List<Double> accumulateEntry(final List<Double> cumulative, final LaneBasedGTU gtu, final Detector loopDetector)
        {
            Speed vGn = gtu.getParameters().getParameterOrNull(LmrsParameters.VGAIN);
            if (vGn == null)
            {
                cumulative.add(Double.NaN);
            }
            else
            {
                cumulative.add(vGn.si);
            }
            return cumulative;
        }

        /** {@inheritDoc} */
        @Override
        public List<Double> accumulateExit(final List<Double> cumulative, final LaneBasedGTU gtu, final Detector loopDetector)
        {
            return cumulative;
        }

        /** {@inheritDoc} */
        @Override
        public boolean isPeriodic()
        {
            return false;
        }

        /** {@inheritDoc} */
        @Override
        public List<Double> aggregate(final List<Double> cumulative, final int count, final Duration aggregation)
        {
            return cumulative;
        }

        /** {@inheritDoc} */
        @Override
        public String getName()
        {
            return "vGain";
        }

        /** {@inheritDoc} */
        @Override
        public String stringValue(final List<Double> aggregate, final String format)
        {
            return Detector.printListDouble(aggregate, format);
        }
    }

    /**
     * Class to store vDes value.
     * <p>
     * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 3 mei 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    class VDesMeasurement implements DetectorMeasurement<List<Double>, List<Double>>
    {
        /** {@inheritDoc} */
        @Override
        public List<Double> identity()
        {
            return new ArrayList<>();
        }

        /** {@inheritDoc} */
        @Override
        public List<Double> accumulateEntry(final List<Double> cumulative, final LaneBasedGTU gtu, final Detector loopDetector)
        {
            Speed vDes = gtu.getDesiredSpeed();
            if (vDes == null)
            {
                cumulative.add(Double.NaN);
            }
            else
            {
                cumulative.add(vDes.si);
            }
            return cumulative;
        }

        /** {@inheritDoc} */
        @Override
        public List<Double> accumulateExit(final List<Double> cumulative, final LaneBasedGTU gtu, final Detector loopDetector)
        {
            return cumulative;
        }

        /** {@inheritDoc} */
        @Override
        public boolean isPeriodic()
        {
            return false;
        }

        /** {@inheritDoc} */
        @Override
        public List<Double> aggregate(final List<Double> cumulative, final int count, final Duration aggregation)
        {
            return cumulative;
        }

        /** {@inheritDoc} */
        @Override
        public String getName()
        {
            return "vDes";
        }

        /** {@inheritDoc} */
        @Override
        public String stringValue(final List<Double> aggregate, final String format)
        {
            return Detector.printListDouble(aggregate, format);
        }
    }

    /**
     * Class to store vDes value.
     * <p>
     * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 3 mei 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    class VDes0Measurement implements DetectorMeasurement<List<Double>, List<Double>>
    {
        /** {@inheritDoc} */
        @Override
        public List<Double> identity()
        {
            return new ArrayList<>();
        }

        /** {@inheritDoc} */
        @Override
        public List<Double> accumulateEntry(final List<Double> cumulative, final LaneBasedGTU gtu, final Detector loopDetector)
        {
            double vDes0;
            try
            {
                vDes0 = Math.min(gtu.getMaximumSpeed().si, gtu.getParameters().getParameter(ParameterTypes.FSPEED)
                        * loopDetector.getLane().getSpeedLimit(gtu.getGTUType()).si);
            }
            catch (ParameterException | NetworkException exception)
            {
                throw new RuntimeException(exception);
            }
            cumulative.add(vDes0);
            return cumulative;
        }

        /** {@inheritDoc} */
        @Override
        public List<Double> accumulateExit(final List<Double> cumulative, final LaneBasedGTU gtu, final Detector loopDetector)
        {
            return cumulative;
        }

        /** {@inheritDoc} */
        @Override
        public boolean isPeriodic()
        {
            return false;
        }

        /** {@inheritDoc} */
        @Override
        public List<Double> aggregate(final List<Double> cumulative, final int count, final Duration aggregation)
        {
            return cumulative;
        }

        /** {@inheritDoc} */
        @Override
        public String getName()
        {
            return "vDes0";
        }

        /** {@inheritDoc} */
        @Override
        public String stringValue(final List<Double> aggregate, final String format)
        {
            return Detector.printListDouble(aggregate, format);
        }
    }

}
