package org.opentrafficsim.demo.carFollowing;

import static org.opentrafficsim.core.gtu.GTUType.CAR;
import static org.opentrafficsim.core.gtu.GTUType.TRUCK;

import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

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
import org.opentrafficsim.base.modelproperties.Property;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterSet;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.distributions.ConstantGenerator;
import org.opentrafficsim.core.distributions.Distribution;
import org.opentrafficsim.core.distributions.Distribution.FrequencyAndObject;
import org.opentrafficsim.core.distributions.Generator;
import org.opentrafficsim.core.distributions.ProbabilityException;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterFactory;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterFactoryByType;
import org.opentrafficsim.core.idgenerator.IdGenerator;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSLink;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.core.network.route.ProbabilisticRouteGenerator;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.network.route.RouteGenerator;
import org.opentrafficsim.core.units.distributions.ContinuousDistDoubleScalar;
import org.opentrafficsim.road.animation.AnimationToggles;
import org.opentrafficsim.road.gtu.animation.LmrsSwitchableColorer;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions;
import org.opentrafficsim.road.gtu.generator.LaneBasedGTUGenerator;
import org.opentrafficsim.road.gtu.generator.LaneBasedGTUGenerator.RoomChecker;
import org.opentrafficsim.road.gtu.generator.TTCRoomChecker;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedTemplateGTUType;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedTemplateGTUTypeDistribution;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedTacticalPlannerFactory;
import org.opentrafficsim.road.gtu.lane.tactical.following.AbstractIDM;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModelFactory;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlus;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlusFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.AccelerationConflicts;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.AccelerationIncentive;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.AccelerationSpeedLimitTransition;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.AccelerationTrafficLights;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.DefaultLMRSPerceptionFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.GapAcceptanceModels;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveCourtesy;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveGetInLane;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveKeep;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveRoute;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveSocioSpeed;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveSpeedWithCourtesy;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.LMRS;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.LMRSFactory;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsParameters;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.MandatoryIncentive;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Synchronization;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.VoluntaryIncentive;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlannerFactory;
import org.opentrafficsim.road.network.factory.xml.XmlNetworkLaneParser;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.object.SpeedSign;
import org.opentrafficsim.simulationengine.AbstractWrappableAnimation;
import org.opentrafficsim.simulationengine.OTSSimulationException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.jstats.distributions.DistUniform;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;
import nl.tudelft.simulation.language.io.URLResource;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 7 apr. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class ShortMerge extends AbstractWrappableAnimation
{

    /** Network. */
    static final String NETWORK = "shortWeave";

    /** Truck fraction. */
    static final double TRUCK_FRACTION = 0.15;

    /** Left traffic fraction. */
    static final double LEFT_FRACTION = 0.6;

    /** Main demand. */
    static final Frequency MAIN_DEMAND = new Frequency(1000, FrequencyUnit.PER_HOUR);

    /** Ramp demand. */
    static final Frequency RAMP_DEMAND = new Frequency(200, FrequencyUnit.PER_HOUR);

    /** Synchronization. */
    static final Synchronization SYNCHRONIZATION = Synchronization.ACTIVE;

    /** Use additional incentives. */
    static final boolean ADDITIONAL_INCENTIVES = true;

    /** Simulation time. */
    public static final Time SIMTIME = Time.createSI(3600);

    /** */
    private static final long serialVersionUID = 20170407L;

    /** The simulator. */
    private OTSDEVSSimulatorInterface simulator;

    /** {@inheritDoc} */
    @Override
    public final String shortName()
    {
        return "ShortMerge";
    }

    /** {@inheritDoc} */
    @Override
    public final String description()
    {
        return "Short merge to test lane change models.";
    }

    /** {@inheritDoc} */
    @Override
    protected final void addAnimationToggles()
    {
        AnimationToggles.setTextAnimationTogglesFull(this);
        this.toggleAnimationClass(OTSLink.class);
        this.toggleAnimationClass(OTSNode.class);
        showAnimationClass(SpeedSign.class);
    }

    /** {@inheritDoc} */
    @Override
    protected GTUColorer getColorer()
    {
        return new LmrsSwitchableColorer();
    }

    /** {@inheritDoc} */
    @Override
    protected OTSModelInterface makeModel(final GTUColorer colorer) throws OTSSimulationException
    {
        return new ShortMergeModel(colorer);
    }

    /**
     * @return simulator.
     */
    public final OTSDEVSSimulatorInterface getSimulator()
    {
        return this.simulator;
    }

    /**
     * @param simulator set simulator.
     */
    public final void setSimulator(final OTSDEVSSimulatorInterface simulator)
    {
        this.simulator = simulator;
    }

    /**
     * Main method.
     * @param args args for main program
     */
    public static void main(final String[] args)
    {

        ShortMerge shortMerge = new ShortMerge();
        try
        {
            shortMerge.buildAnimator(Time.ZERO, Duration.ZERO, Duration.createSI(SIMTIME.si), new ArrayList<Property<?>>(),
                    null, true);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }

    }

    /**
     * <p>
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 7 apr. 2017 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    class ShortMergeModel implements OTSModelInterface
    {

        /**
         * @param network set network.
         */
        public void setNetwork(final OTSNetwork network)
        {
            this.network = network;
        }

        /** */
        private static final long serialVersionUID = 20170407L;

        /** The network. */
        private OTSNetwork network;

        /** Colorer. */
        private final GTUColorer colorer;

        /**
         * @param colorer colorer
         */
        ShortMergeModel(final GTUColorer colorer)
        {
            this.colorer = colorer;
        }

        /** {@inheritDoc} */
        @Override
        public void constructModel(final SimulatorInterface<Time, Duration, OTSSimTimeDouble> sim)
                throws SimRuntimeException, RemoteException
        {
            ShortMerge.this.setSimulator((OTSDEVSSimulatorInterface) sim);

            try
            {
                InputStream stream = URLResource.getResourceAsStream("/lmrs/" + NETWORK + ".xml");
                XmlNetworkLaneParser nlp = new XmlNetworkLaneParser((OTSDEVSSimulatorInterface) sim);
                this.network = new OTSNetwork("ShortMerge");
                nlp.build(stream, this.network);

                addGenerator();

            }
            catch (Exception exception)
            {
                exception.printStackTrace();
            }
        }

        /** {@inheritDoc} */
        @Override
        public SimulatorInterface<Time, Duration, OTSSimTimeDouble> getSimulator() throws RemoteException
        {
            return ShortMerge.this.getSimulator();
        }

        /** {@inheritDoc} */
        @Override
        public OTSNetwork getNetwork()
        {
            return this.network;
        }

        /**
         * Create generators.
         * @throws ParameterException on parameter exception
         * @throws GTUException on GTU exception
         * @throws NetworkException if not does not exist
         * @throws ProbabilityException negative probability
         * @throws SimRuntimeException in case of sim run time exception
         * @throws RemoteException if no simulator
         */
        private void addGenerator() throws ParameterException, GTUException, NetworkException, ProbabilityException,
                SimRuntimeException, RemoteException
        {

            Random seedGenerator = new Random(1L);
            Map<String, StreamInterface> streams = new HashMap<>();
            StreamInterface stream = new MersenneTwister(Math.abs(seedGenerator.nextLong()) + 1);
            streams.put("headwayGeneration", stream);
            streams.put("gtuClass", new MersenneTwister(Math.abs(seedGenerator.nextLong()) + 1));
            this.getSimulator().getReplication().setStreams(streams);

            TTCRoomChecker roomChecker = new TTCRoomChecker(new Duration(10.0, DurationUnit.SI));
            IdGenerator idGenerator = new IdGenerator("");

            CarFollowingModelFactory<IDMPlus> idmPlusFactory = new IDMPlusFactory(streams.get("gtuClass"));
            ParameterSet params = new ParameterSet();
            params.setDefaultParameter(AbstractIDM.DELTA);

            Set<MandatoryIncentive> mandatoryIncentives = new LinkedHashSet<>();
            Set<VoluntaryIncentive> voluntaryIncentives = new LinkedHashSet<>();
            Set<AccelerationIncentive> accelerationIncentives = new LinkedHashSet<>();
            mandatoryIncentives.add(new IncentiveRoute());
            if (ADDITIONAL_INCENTIVES)
            {
                mandatoryIncentives.add(new IncentiveGetInLane());
            }
            voluntaryIncentives.add(new IncentiveSpeedWithCourtesy());
            voluntaryIncentives.add(new IncentiveKeep());
            if (ADDITIONAL_INCENTIVES)
            {
                voluntaryIncentives.add(new IncentiveCourtesy());
                voluntaryIncentives.add(new IncentiveSocioSpeed());
            }
            accelerationIncentives.add(new AccelerationSpeedLimitTransition());
            accelerationIncentives.add(new AccelerationTrafficLights());
            accelerationIncentives.add(new AccelerationConflicts());
            LaneBasedTacticalPlannerFactory<LMRS> tacticalFactory =
                    new LMRSFactory(idmPlusFactory, new DefaultLMRSPerceptionFactory(), SYNCHRONIZATION,
                            GapAcceptanceModels.INFORMED, mandatoryIncentives, voluntaryIncentives, accelerationIncentives);

            GTUType car = new GTUType("car", CAR);
            GTUType truck = new GTUType("truck", TRUCK);
            Route routeAE = this.network.getShortestRouteBetween(car, this.network.getNode("A"), this.network.getNode("E"));
            Route routeAG = !NETWORK.equals("shortWeave") ? null
                    : this.network.getShortestRouteBetween(car, this.network.getNode("A"), this.network.getNode("G"));
            Route routeFE = this.network.getShortestRouteBetween(car, this.network.getNode("F"), this.network.getNode("E"));
            Route routeFG = !NETWORK.equals("shortWeave") ? null
                    : this.network.getShortestRouteBetween(car, this.network.getNode("F"), this.network.getNode("G"));

            double leftFraction = NETWORK.equals("shortWeave") ? LEFT_FRACTION : 0.0;
            List<FrequencyAndObject<Route>> routesA = new ArrayList<>();
            routesA.add(new FrequencyAndObject<>(1.0 - leftFraction, routeAE));
            routesA.add(new FrequencyAndObject<>(leftFraction, routeAG));
            List<FrequencyAndObject<Route>> routesF = new ArrayList<>();
            routesF.add(new FrequencyAndObject<>(1.0 - leftFraction, routeFE));
            routesF.add(new FrequencyAndObject<>(leftFraction, routeFG));
            RouteGenerator routeGeneratorA = new ProbabilisticRouteGenerator(routesA, stream);
            RouteGenerator routeGeneratorF = new ProbabilisticRouteGenerator(routesF, stream);

            Speed speedA = new Speed(120.0, SpeedUnit.KM_PER_HOUR);
            Speed speedF = new Speed(20.0, SpeedUnit.KM_PER_HOUR);

            CrossSectionLink linkA = (CrossSectionLink) this.network.getLink("AB");
            CrossSectionLink linkF = (CrossSectionLink) this.network.getLink("FF2");

            ParameterFactoryByType bcFactory = new ParameterFactoryByType();
            bcFactory.addGaussianParameter(car, ParameterTypes.FSPEED, 123.7 / 120, 12.0 / 120, stream);
            bcFactory.addGaussianParameter(car, LmrsParameters.SOCIO, 0.5, 0.1, stream);
            bcFactory.addParameter(truck, ParameterTypes.A, new Acceleration(0.8, AccelerationUnit.SI));
            bcFactory.addGaussianParameter(truck, LmrsParameters.SOCIO, 0.5, 0.1, stream);

            Generator<Duration> headwaysA1 = new HeadwayGenerator(MAIN_DEMAND);
            Generator<Duration> headwaysA2 = new HeadwayGenerator(MAIN_DEMAND);
            Generator<Duration> headwaysA3 = new HeadwayGenerator(MAIN_DEMAND);
            Generator<Duration> headwaysF = new HeadwayGenerator(RAMP_DEMAND);

            // speed generators
            ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> speedCar =
                    new ContinuousDistDoubleScalar.Rel<>(new DistUniform(stream, 160, 200), SpeedUnit.KM_PER_HOUR);
            ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> speedTruck =
                    new ContinuousDistDoubleScalar.Rel<>(new DistUniform(stream, 80, 95), SpeedUnit.KM_PER_HOUR);
            // strategical planner factory
            LaneBasedStrategicalRoutePlannerFactory strategicalFactory =
                    new LaneBasedStrategicalRoutePlannerFactory(tacticalFactory, bcFactory);
            // vehicle templates, with routes
            LaneBasedTemplateGTUType carA =
                    new LaneBasedTemplateGTUType(new GTUType("car", CAR), new ConstantGenerator<>(Length.createSI(4.0)),
                            new ConstantGenerator<>(Length.createSI(2.0)), speedCar, strategicalFactory, routeGeneratorA);
            LaneBasedTemplateGTUType carF =
                    new LaneBasedTemplateGTUType(new GTUType("car", CAR), new ConstantGenerator<>(Length.createSI(4.0)),
                            new ConstantGenerator<>(Length.createSI(2.0)), speedCar, strategicalFactory, routeGeneratorF);
            LaneBasedTemplateGTUType truckA =
                    new LaneBasedTemplateGTUType(new GTUType("truck", TRUCK), new ConstantGenerator<>(Length.createSI(15.0)),
                            new ConstantGenerator<>(Length.createSI(2.5)), speedTruck, strategicalFactory, routeGeneratorA);
            LaneBasedTemplateGTUType truckF =
                    new LaneBasedTemplateGTUType(new GTUType("truck", TRUCK), new ConstantGenerator<>(Length.createSI(15.0)),
                            new ConstantGenerator<>(Length.createSI(2.5)), speedTruck, strategicalFactory, routeGeneratorF);
            //
            Distribution<LaneBasedTemplateGTUType> gtuTypeAllCarA = new Distribution<>(streams.get("gtuClass"));
            gtuTypeAllCarA.add(new FrequencyAndObject<>(1.0, carA));

            Distribution<LaneBasedTemplateGTUType> gtuType1LaneF = new Distribution<>(streams.get("gtuClass"));
            gtuType1LaneF.add(new FrequencyAndObject<>(1.0 - 2 * TRUCK_FRACTION, carF));
            gtuType1LaneF.add(new FrequencyAndObject<>(2 * TRUCK_FRACTION, truckF));

            Distribution<LaneBasedTemplateGTUType> gtuType2ndLaneA = new Distribution<>(streams.get("gtuClass"));
            gtuType2ndLaneA.add(new FrequencyAndObject<>(1.0 - 2 * TRUCK_FRACTION, carA));
            gtuType2ndLaneA.add(new FrequencyAndObject<>(2 * TRUCK_FRACTION, truckA));

            Distribution<LaneBasedTemplateGTUType> gtuType3rdLaneA = new Distribution<>(streams.get("gtuClass"));
            gtuType3rdLaneA.add(new FrequencyAndObject<>(1.0 - 3 * TRUCK_FRACTION, carA));
            gtuType3rdLaneA.add(new FrequencyAndObject<>(3 * TRUCK_FRACTION, truckA));

            makeGenerator(getLane(linkA, "FORWARD1"), speedA, "gen1", idGenerator, gtuTypeAllCarA, headwaysA1, this.colorer,
                    roomChecker, bcFactory, tacticalFactory, SIMTIME, streams.get("gtuClass"));
            if (NETWORK.equals("shortWeave"))
            {
                makeGenerator(getLane(linkA, "FORWARD2"), speedA, "gen2", idGenerator, gtuTypeAllCarA, headwaysA2, this.colorer,
                        roomChecker, bcFactory, tacticalFactory, SIMTIME, streams.get("gtuClass"));
                makeGenerator(getLane(linkA, "FORWARD3"), speedA, "gen3", idGenerator, gtuType3rdLaneA, headwaysA3,
                        this.colorer, roomChecker, bcFactory, tacticalFactory, SIMTIME, streams.get("gtuClass"));
            }
            else
            {
                makeGenerator(getLane(linkA, "FORWARD2"), speedA, "gen2", idGenerator, gtuType2ndLaneA, headwaysA2,
                        this.colorer, roomChecker, bcFactory, tacticalFactory, SIMTIME, streams.get("gtuClass"));
            }
            makeGenerator(getLane(linkF, "FORWARD1"), speedF, "gen4", idGenerator, gtuType1LaneF, headwaysF, this.colorer,
                    roomChecker, bcFactory, tacticalFactory, SIMTIME, streams.get("gtuClass"));

            new SpeedSign("sign1", getLane(linkA, "FORWARD1"), LongitudinalDirectionality.DIR_PLUS, Length.createSI(10),
                    (OTSSimulatorInterface) this.getSimulator(), new Speed(130.0, SpeedUnit.KM_PER_HOUR));

        }

        /**
         * Get lane from link by id.
         * @param link link
         * @param id id
         * @return lane
         */
        private Lane getLane(final CrossSectionLink link, final String id)
        {
            return (Lane) link.getCrossSectionElement(id);
        }

        /**
         * @param lane the reference lane for this generator
         * @param generationSpeed the speed of the GTU
         * @param id the id of the generator itself
         * @param idGenerator the generator for the ID
         * @param distribution the type generator for the GTU
         * @param headwayGenerator the headway generator for the GTU
         * @param gtuColorer the GTU colorer for animation
         * @param roomChecker the checker to see if there is room for the GTU
         * @param bcFactory the factory to generate parameters for the GTU
         * @param tacticalFactory the generator for the tactical planner
         * @param simulationTime simulation time
         * @param stream random numbers stream
         * @throws SimRuntimeException in case of scheduling problems
         * @throws ProbabilityException in case of an illegal probability distribution
         * @throws GTUException in case the GTU is inconsistent
         * @throws ParameterException in case a parameter for the perception is missing
         */
        private void makeGenerator(final Lane lane, final Speed generationSpeed, final String id, final IdGenerator idGenerator,
                final Distribution<LaneBasedTemplateGTUType> distribution, final Generator<Duration> headwayGenerator,
                final GTUColorer gtuColorer, final RoomChecker roomChecker, final ParameterFactory bcFactory,
                final LaneBasedTacticalPlannerFactory<?> tacticalFactory, final Time simulationTime,
                final StreamInterface stream) throws SimRuntimeException, ProbabilityException, GTUException, ParameterException
        {

            Set<DirectedLanePosition> initialLongitudinalPositions = new HashSet<>();
            // TODO DIR_MINUS
            initialLongitudinalPositions
                    .add(new DirectedLanePosition(lane, new Length(10.0, LengthUnit.SI), GTUDirectionality.DIR_PLUS));
            LaneBasedTemplateGTUTypeDistribution characteristicsGenerator =
                    new LaneBasedTemplateGTUTypeDistribution(distribution);
            new LaneBasedGTUGenerator(id, headwayGenerator, gtuColorer, characteristicsGenerator,
                    GeneratorPositions.create(initialLongitudinalPositions, stream), this.network,
                    ShortMerge.this.getSimulator(), roomChecker, idGenerator);
        }

    }

    /**
     * <p>
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 29 jan. 2017 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private class HeadwayGenerator implements Generator<Duration>
    {

        /** Demand level. */
        private final Frequency demand;

        /**
         * @param demand demand
         */
        HeadwayGenerator(final Frequency demand)
        {
            this.demand = demand;
        }

        /** {@inheritDoc} */
        @Override
        public Duration draw() throws ProbabilityException, ParameterException
        {
            return new Duration(
                    -Math.log(ShortMerge.this.getSimulator().getReplication().getStream("headwayGeneration").nextDouble())
                            / this.demand.si,
                    DurationUnit.SI);
        }

    }

}
