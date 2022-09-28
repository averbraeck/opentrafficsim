package org.opentrafficsim.ahfe;

import java.rmi.RemoteException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.DurationUnit;
import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.ValueRuntimeException;
import org.djunits.value.storage.StorageType;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djunits.value.vdouble.vector.FrequencyVector;
import org.djunits.value.vdouble.vector.TimeVector;
import org.djunits.value.vdouble.vector.base.DoubleVector;
import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterSet;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.animation.gtu.colorer.GTUColorer;
import org.opentrafficsim.core.distributions.ConstantGenerator;
import org.opentrafficsim.core.distributions.Distribution;
import org.opentrafficsim.core.distributions.Distribution.FrequencyAndObject;
import org.opentrafficsim.core.distributions.Generator;
import org.opentrafficsim.core.distributions.ProbabilityException;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.perception.DirectEgoPerception;
import org.opentrafficsim.core.gtu.perception.EgoPerception;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.idgenerator.IdGenerator;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.route.FixedRouteGenerator;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.parameters.ParameterFactory;
import org.opentrafficsim.core.parameters.ParameterFactoryByType;
import org.opentrafficsim.core.units.distributions.ContinuousDistDoubleScalar;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions;
import org.opentrafficsim.road.gtu.generator.LaneBasedGTUGenerator;
import org.opentrafficsim.road.gtu.generator.LaneBasedGTUGenerator.RoomChecker;
import org.opentrafficsim.road.gtu.generator.TTCRoomChecker;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedTemplateGTUType;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedTemplateGTUTypeDistribution;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.CategoricalLanePerception;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionFactory;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.AnticipationTrafficPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.DirectInfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.Anticipation;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedTacticalPlannerFactory;
import org.opentrafficsim.road.gtu.lane.tactical.following.AbstractIDM;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModelFactory;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlus;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlusFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveKeep;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveRoute;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveSpeedWithCourtesy;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.LMRS;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Cooperation;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Desire;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.GapAcceptance;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsParameters;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Synchronization;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Tailgating;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.VoluntaryIncentive;
import org.opentrafficsim.road.gtu.strategical.od.Interpolation;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlannerFactory;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.jstats.distributions.DistNormal;
import nl.tudelft.simulation.jstats.distributions.DistUniform;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */

public final class AHFEUtil
{

    /**
     * 
     */
    private AHFEUtil()
    {
        //
    }

    /**
     * @param network OTSRoadNetwork; the network
     * @param gtuColorer GTUColorer; the GTU colorer
     * @param simulator OTSSimulatorInterface; the simulator
     * @param replication int; replication number
     * @param anticipationStrategy String; anticipation strategy
     * @param reactionTime Duration; reaction time
     * @param anticipationTime Duration; anticipation time
     * @param truckFraction double; truck fraction
     * @param simulationTime Time; simulation time
     * @param leftDemand Frequency; demand on left highway
     * @param rightDemand Frequency; demand on right highway
     * @param leftFraction double; fraction of traffic generated on left lane
     * @param distanceError double; distance error
     * @param speedError double; speed error
     * @param accelerationError double; acceleration error
     * @throws ValueRuntimeException on value error
     * @throws ParameterException on parameter error
     * @throws GTUException on gtu error
     * @throws ProbabilityException on probability error
     * @throws SimRuntimeException on sim runtime error
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public static void createDemand(final OTSRoadNetwork network, final GTUColorer gtuColorer,
            final OTSSimulatorInterface simulator, final int replication, final String anticipationStrategy,
            final Duration reactionTime, final Duration anticipationTime, final double truckFraction, final Time simulationTime,
            final Frequency leftDemand, final Frequency rightDemand, final double leftFraction, final double distanceError,
            final double speedError, final double accelerationError)
            throws ValueRuntimeException, ParameterException, GTUException, SimRuntimeException, ProbabilityException
    {

        Random seedGenerator = new Random(replication);
        Map<String, StreamInterface> streams = new LinkedHashMap<>();
        streams.put("headwayGeneration", new MersenneTwister(Math.abs(seedGenerator.nextLong()) + 1));
        streams.put("gtuClass", new MersenneTwister(Math.abs(seedGenerator.nextLong()) + 1));
        streams.put("perception", new MersenneTwister(Math.abs(seedGenerator.nextLong()) + 1));
        simulator.getModel().getStreamInformation().addStream("headwayGeneration", streams.get("headwayGeneration"));
        simulator.getModel().getStreamInformation().addStream("gtuClass", streams.get("gtuClass"));
        simulator.getModel().getStreamInformation().addStream("perception", streams.get("perception"));

        TTCRoomChecker roomChecker = new TTCRoomChecker(new Duration(10.0, DurationUnit.SI));
        IdGenerator idGenerator = new IdGenerator("");

        CarFollowingModelFactory<IDMPlus> idmPlusFactory = new IDMPlusFactory(streams.get("gtuClass"));
        PerceptionFactory delayedPerceptionFactory = Try.assign(
                () -> new DelayedPerceptionFactory(
                        (Anticipation) Anticipation.class.getDeclaredField(anticipationStrategy.toUpperCase()).get(null)),
                "Exception while obtaining anticipation value %s", anticipationStrategy);
        ParameterSet params = new ParameterSet();
        params.setDefaultParameter(AbstractIDM.DELTA);
        params.setParameter(ParameterTypes.TR, reactionTime);
        params.setParameter(DelayedNeighborsPerception.TA, anticipationTime);
        params.setDefaultParameter(DelayedNeighborsPerception.TAUE);
        params.setParameter(DelayedNeighborsPerception.SERROR, distanceError);
        params.setParameter(DelayedNeighborsPerception.VERROR, speedError);
        params.setParameter(DelayedNeighborsPerception.AERROR, accelerationError);
        LaneBasedTacticalPlannerFactory<LMRS> tacticalFactory =
                new LMRSFactoryAHFE(idmPlusFactory, params, delayedPerceptionFactory);

        ParameterFactoryByType bcFactory = new ParameterFactoryByType();
        // Length lookAhead = new Length(1000.0, LengthUnit.SI);
        // Length lookAheadStdev = new Length(250.0, LengthUnit.SI);
        Length perception = new Length(1.0, LengthUnit.KILOMETER);
        Acceleration b = new Acceleration(2.09, AccelerationUnit.SI);
        GTUType gtuType = new GTUType("car", network.getGtuType(GTUType.DEFAULTS.CAR));
        bcFactory.addParameter(gtuType, ParameterTypes.FSPEED,
                new DistNormal(streams.get("gtuClass"), 123.7 / 120, 12.0 / 120));
        bcFactory.addParameter(gtuType, ParameterTypes.B, b);
        // bcFactory.addGaussianParameter(gtuType, ParameterTypes.LOOKAHEAD, lookAhead, lookAheadStdev,
        // streams.get("gtuClass"));
        bcFactory.addParameter(gtuType, ParameterTypes.PERCEPTION, perception);
        gtuType = new GTUType("truck", network.getGtuType(GTUType.DEFAULTS.TRUCK));
        bcFactory.addParameter(gtuType, ParameterTypes.A, new Acceleration(0.8, AccelerationUnit.SI));
        bcFactory.addParameter(gtuType, ParameterTypes.B, b);
        // bcFactory.addGaussianParameter(gtuType, ParameterTypes.LOOKAHEAD, lookAhead, lookAheadStdev,
        // streams.get("gtuClass"));
        bcFactory.addParameter(gtuType, ParameterTypes.PERCEPTION, perception);
        bcFactory.addParameter(gtuType, ParameterTypes.FSPEED, 2.0);

        Route leftRoute = new Route("left");
        Route rightRoute = new Route("right");
        try
        {
            leftRoute.addNode(network.getNode("LEFTINPRE"));
            leftRoute.addNode(network.getNode("LEFTIN"));
            leftRoute.addNode(network.getNode("STARTCONVERGE"));
            leftRoute.addNode(network.getNode("STARTWEAVING"));
            leftRoute.addNode(network.getNode("NARROWING"));
            leftRoute.addNode(network.getNode("EXIT"));
            rightRoute.addNode(network.getNode("RIGHTINPRE"));
            rightRoute.addNode(network.getNode("RIGHTIN"));
            rightRoute.addNode(network.getNode("STARTWEAVING"));
            rightRoute.addNode(network.getNode("NARROWING"));
            rightRoute.addNode(network.getNode("EXIT"));
        }
        catch (NetworkException exception)
        {
            exception.printStackTrace();
        }
        Generator<Route> fixedRouteGeneratorLeft = new FixedRouteGenerator(leftRoute);
        Generator<Route> fixedRouteGeneratorRight = new FixedRouteGenerator(rightRoute);

        LaneBasedStrategicalRoutePlannerFactory strategicalFactory =
                new LaneBasedStrategicalRoutePlannerFactory(tacticalFactory, bcFactory);
        ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> speedCar =
                new ContinuousDistDoubleScalar.Rel<>(new DistUniform(streams.get("gtuClass"), 160, 200), SpeedUnit.KM_PER_HOUR);
        ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> speedTruck =
                new ContinuousDistDoubleScalar.Rel<>(new DistNormal(streams.get("gtuClass"), 80, 2.5), SpeedUnit.KM_PER_HOUR);

        LaneBasedTemplateGTUType carLeft =
                new LaneBasedTemplateGTUType(new GTUType("car", network.getGtuType(GTUType.DEFAULTS.CAR)),
                        new ConstantGenerator<>(Length.instantiateSI(4.0)), new ConstantGenerator<>(Length.instantiateSI(2.0)),
                        speedCar, strategicalFactory, fixedRouteGeneratorLeft);
        LaneBasedTemplateGTUType truckLeft =
                new LaneBasedTemplateGTUType(new GTUType("truck", network.getGtuType(GTUType.DEFAULTS.TRUCK)),
                        new ConstantGenerator<>(Length.instantiateSI(15.0)), new ConstantGenerator<>(Length.instantiateSI(2.5)),
                        speedTruck, strategicalFactory, fixedRouteGeneratorLeft);
        LaneBasedTemplateGTUType carRight =
                new LaneBasedTemplateGTUType(new GTUType("car", network.getGtuType(GTUType.DEFAULTS.CAR)),
                        new ConstantGenerator<>(Length.instantiateSI(4.0)), new ConstantGenerator<>(Length.instantiateSI(2.0)),
                        speedCar, strategicalFactory, fixedRouteGeneratorRight);
        LaneBasedTemplateGTUType truckRight =
                new LaneBasedTemplateGTUType(new GTUType("truck", network.getGtuType(GTUType.DEFAULTS.TRUCK)),
                        new ConstantGenerator<>(Length.instantiateSI(15.0)), new ConstantGenerator<>(Length.instantiateSI(2.5)),
                        speedTruck, strategicalFactory, fixedRouteGeneratorRight);

        // GTUTypeGenerator gtuTypeGeneratorLeft = new GTUTypeGenerator(simulator, streams.get("gtuClass"));
        // GTUTypeGenerator gtuTypeGeneratorRight = new GTUTypeGenerator(simulator, streams.get("gtuClass"));

        Distribution<LaneBasedTemplateGTUType> gtuTypeGeneratorLeftLeft = new Distribution<>(streams.get("gtuClass"));
        Distribution<LaneBasedTemplateGTUType> gtuTypeGeneratorLeftRight = new Distribution<>(streams.get("gtuClass"));
        Distribution<LaneBasedTemplateGTUType> gtuTypeGeneratorRightLeft = new Distribution<>(streams.get("gtuClass"));
        Distribution<LaneBasedTemplateGTUType> gtuTypeGeneratorRightRight = new Distribution<>(streams.get("gtuClass"));
        if (truckFraction < 1 - leftFraction)
        {
            double p = truckFraction / (1 - leftFraction);

            gtuTypeGeneratorLeftLeft.add(new FrequencyAndObject<>(1.0, carLeft));
            gtuTypeGeneratorLeftRight.add(new FrequencyAndObject<>(1.0 - p, carLeft));
            gtuTypeGeneratorLeftRight.add(new FrequencyAndObject<>(p, truckLeft));

            gtuTypeGeneratorRightLeft.add(new FrequencyAndObject<>(1.0, carRight));
            gtuTypeGeneratorRightRight.add(new FrequencyAndObject<>(1.0 - p, carRight));
            gtuTypeGeneratorRightRight.add(new FrequencyAndObject<>(p, truckRight));
        }
        else
        {
            double p = (truckFraction - (1 - leftFraction)) / leftFraction;
            gtuTypeGeneratorLeftLeft.add(new FrequencyAndObject<>(1.0 - p, carLeft));
            gtuTypeGeneratorLeftLeft.add(new FrequencyAndObject<>(p, truckLeft));
            gtuTypeGeneratorLeftRight.add(new FrequencyAndObject<>(1.0, truckLeft));

            gtuTypeGeneratorRightLeft.add(new FrequencyAndObject<>(1.0 - p, carRight));
            gtuTypeGeneratorRightLeft.add(new FrequencyAndObject<>(p, truckRight));
            gtuTypeGeneratorRightRight.add(new FrequencyAndObject<>(1.0, truckRight));
        }

        TimeVector timeVector =
                DoubleVector.instantiate(new double[] {0, 360, 1560, 2160, 3960}, TimeUnit.BASE_SECOND, StorageType.DENSE);
        double leftLeft = leftDemand.si * leftFraction;
        FrequencyVector leftLeftDemandPattern = DoubleVector.instantiate(
                new double[] {leftLeft * 0.5, leftLeft * 0.5, leftLeft, leftLeft, 0.0}, FrequencyUnit.SI, StorageType.DENSE);
        double leftRight = leftDemand.si * (1 - leftFraction);
        FrequencyVector leftRightDemandPattern =
                DoubleVector.instantiate(new double[] {leftRight * 0.5, leftRight * 0.5, leftRight, leftRight, 0.0},
                        FrequencyUnit.SI, StorageType.DENSE);
        double rightLeft = rightDemand.si * leftFraction;
        FrequencyVector rightLeftDemandPattern =
                DoubleVector.instantiate(new double[] {rightLeft * 0.5, rightLeft * 0.5, rightLeft, rightLeft, 0.0},
                        FrequencyUnit.SI, StorageType.DENSE);
        double rightRight = rightDemand.si * (1 - leftFraction);
        FrequencyVector rightRightDemandPattern =
                DoubleVector.instantiate(new double[] {rightRight * 0.5, rightRight * 0.5, rightRight, rightRight, 0.0},
                        FrequencyUnit.SI, StorageType.DENSE);
        // This defaults to stepwise interpolation, should have been linear.
        HeadwayGeneratorDemand leftLeftHeadways = new HeadwayGeneratorDemand(timeVector, leftLeftDemandPattern, simulator);
        HeadwayGeneratorDemand leftRightHeadways = new HeadwayGeneratorDemand(timeVector, leftRightDemandPattern, simulator);
        HeadwayGeneratorDemand rightLeftHeadways = new HeadwayGeneratorDemand(timeVector, rightLeftDemandPattern, simulator);
        HeadwayGeneratorDemand rightRightHeadways = new HeadwayGeneratorDemand(timeVector, rightRightDemandPattern, simulator);

        Speed genSpeed = new Speed(120.0, SpeedUnit.KM_PER_HOUR);
        CrossSectionLink leftLink = (CrossSectionLink) network.getLink("LEFTINPRE");
        CrossSectionLink rightLink = (CrossSectionLink) network.getLink("RIGHTINPRE");
        makeGenerator(getLane(leftLink, "FORWARD1"), genSpeed, "LEFTLEFT", idGenerator, simulator, network,
                gtuTypeGeneratorLeftLeft, leftLeftHeadways, gtuColorer, roomChecker, bcFactory, tacticalFactory, simulationTime,
                streams.get("gtuClass"));
        makeGenerator(getLane(leftLink, "FORWARD2"), genSpeed, "LEFTRIGHT", idGenerator, simulator, network,
                gtuTypeGeneratorLeftRight, leftRightHeadways, gtuColorer, roomChecker, bcFactory, tacticalFactory,
                simulationTime, streams.get("gtuClass"));
        makeGenerator(getLane(rightLink, "FORWARD1"), genSpeed, "RIGHTLEFT", idGenerator, simulator, network,
                gtuTypeGeneratorRightLeft, rightLeftHeadways, gtuColorer, roomChecker, bcFactory, tacticalFactory,
                simulationTime, streams.get("gtuClass"));
        makeGenerator(getLane(rightLink, "FORWARD2"), genSpeed, "RIGHTRIGHT", idGenerator, simulator, network,
                gtuTypeGeneratorRightRight, rightRightHeadways, gtuColorer, roomChecker, bcFactory, tacticalFactory,
                simulationTime, streams.get("gtuClass"));

    }

    /**
     * Get lane from link by id.
     * @param link CrossSectionLink; link
     * @param id String; id
     * @return lane
     */
    private static Lane getLane(final CrossSectionLink link, final String id)
    {
        for (Lane lane : link.getLanes())
        {
            if (lane.getId().equals(id))
            {
                return lane;
            }
        }
        throw new RuntimeException("Could not find lane " + id + " on link " + link.getId());
    }

    /**
     * @param lane Lane; the reference lane for this generator
     * @param generationSpeed Speed; the speed of the GTU
     * @param id String; the id of the generator itself
     * @param idGenerator IdGenerator; the generator for the ID
     * @param simulator OTSSimulatorInterface; the simulator
     * @param network OTSRoadNetwork; the network
     * @param distribution Distribution&lt;LaneBasedTemplateGTUType&gt;; the type generator for the GTU
     * @param headwayGenerator HeadwayGeneratorDemand; the headway generator for the GTU
     * @param gtuColorer GTUColorer; the GTU colorer for animation
     * @param roomChecker RoomChecker; the checker to see if there is room for the GTU
     * @param bcFactory ParameterFactory; the factory to generate parameters for the GTU
     * @param tacticalFactory LaneBasedTacticalPlannerFactory&lt;?&gt;; the generator for the tactical planner
     * @param simulationTime Time; simulation time
     * @param stream StreamInterface; random number stream
     * @throws SimRuntimeException in case of scheduling problems
     * @throws ProbabilityException in case of an illegal probability distribution
     * @throws GTUException in case the GTU is inconsistent
     * @throws ParameterException in case a parameter for the perception is missing
     */
    private static void makeGenerator(final Lane lane, final Speed generationSpeed, final String id,
            final IdGenerator idGenerator, final OTSSimulatorInterface simulator, final OTSRoadNetwork network,
            final Distribution<LaneBasedTemplateGTUType> distribution, final HeadwayGeneratorDemand headwayGenerator,
            final GTUColorer gtuColorer, final RoomChecker roomChecker, final ParameterFactory bcFactory,
            final LaneBasedTacticalPlannerFactory<?> tacticalFactory, final Time simulationTime, final StreamInterface stream)
            throws SimRuntimeException, ProbabilityException, GTUException, ParameterException
    {
        Set<DirectedLanePosition> initialLongitudinalPositions = new LinkedHashSet<>();
        // TODO DIR_MINUS
        initialLongitudinalPositions
                .add(new DirectedLanePosition(lane, new Length(10.0, LengthUnit.SI), GTUDirectionality.DIR_PLUS));
        LaneBasedTemplateGTUTypeDistribution characteristicsGenerator = new LaneBasedTemplateGTUTypeDistribution(distribution);
        new LaneBasedGTUGenerator(id, headwayGenerator, characteristicsGenerator,
                GeneratorPositions.create(initialLongitudinalPositions, stream), network, simulator, roomChecker, idGenerator);
    }

    /**
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private static class LMRSFactoryAHFE implements LaneBasedTacticalPlannerFactory<LMRS>
    {

        /** Constructor for the car-following model. */
        private final CarFollowingModelFactory<? extends CarFollowingModel> carFollowingModelFactory;

        /** Default set of parameters for the car-following model. */
        private final Parameters defaultCarFollowingParameters;

        /** Factory for perception. */
        private final PerceptionFactory perceptionFactory;

        /**
         * Constructor with car-following model class. The class should have an accessible empty constructor.
         * @param carFollowingModelFactory CarFollowingModelFactory&lt;? extends CarFollowingModel&gt;; factory of the
         *            car-following model
         * @param defaultCarFollowingParameters Parameters; default set of parameters for the car-following model
         * @param perceptionFactory PerceptionFactory; perception factory
         * @throws GTUException if the supplied car-following model does not have an accessible empty constructor
         */
        LMRSFactoryAHFE(final CarFollowingModelFactory<? extends CarFollowingModel> carFollowingModelFactory,
                final Parameters defaultCarFollowingParameters, final PerceptionFactory perceptionFactory) throws GTUException
        {
            this.carFollowingModelFactory = carFollowingModelFactory;
            this.defaultCarFollowingParameters = defaultCarFollowingParameters;
            this.perceptionFactory = perceptionFactory;
        }

        /** {@inheritDoc} */
        @Override
        public final Parameters getParameters()
        {
            ParameterSet parameters = new ParameterSet();
            parameters.setDefaultParameters(ParameterTypes.class);
            parameters.setDefaultParameters(LmrsParameters.class);
            this.defaultCarFollowingParameters.setAllIn(parameters);
            return parameters;
        }

        /** {@inheritDoc} */
        @Override
        public final LMRS create(final LaneBasedGTU gtu) throws GTUException
        {
            LMRS lmrs = new LMRS(this.carFollowingModelFactory.generateCarFollowingModel(), gtu,
                    this.perceptionFactory.generatePerception(gtu), Synchronization.PASSIVE, Cooperation.PASSIVE,
                    GapAcceptance.INFORMED, Tailgating.NONE);
            lmrs.addMandatoryIncentive(new IncentiveRoute());
            lmrs.addVoluntaryIncentive(new IncentiveSpeedWithCourtesy());
            if (gtu.getGTUType().getId().equals("car"))
            {
                lmrs.addVoluntaryIncentive(new IncentiveKeep());
            }
            else
            {
                lmrs.addVoluntaryIncentive(new KeepRightTruck());
            }
            return lmrs;
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "LMRSFactory [car-following=" + this.carFollowingModelFactory + "]";
        }

    }

    /**
     * Perception factory with delay and anticipation for neighbors.
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private static class DelayedPerceptionFactory implements PerceptionFactory
    {

        /** Anticipation form. */
        private final Anticipation anticipation;

        /**
         * Constructor.
         * @param anticipation Anticipation; anticipation form.
         */
        DelayedPerceptionFactory(final Anticipation anticipation)
        {
            this.anticipation = anticipation;
        }

        /** {@inheritDoc} */
        @Override
        public LanePerception generatePerception(final LaneBasedGTU gtu)
        {
            LanePerception perception = new CategoricalLanePerception(gtu);
            perception.addPerceptionCategory(new DirectEgoPerception(perception));
            // perception.addPerceptionCategory(new DirectDefaultSimplePerception(perception));
            perception.addPerceptionCategory(new DirectInfrastructurePerception(perception));
            // perception.addPerceptionCategory(new DirectNeighborsPerception(perception));
            perception.addPerceptionCategory(new DelayedNeighborsPerception(perception, this.anticipation));
            // perception.addPerceptionCategory(new DirectIntersectionPerception(perception));
            perception.addPerceptionCategory(new AnticipationTrafficPerception(perception));
            return perception;
        }

        /** {@inheritDoc} */
        @Override
        public Parameters getParameters()
        {
            return new ParameterSet();
        }

    }

    /**
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private static class KeepRightTruck implements VoluntaryIncentive
    {

        /**
         * 
         */
        KeepRightTruck()
        {
        }

        /** {@inheritDoc} */
        @Override
        public Desire determineDesire(final Parameters parameters, final LanePerception perception,
                final CarFollowingModel carFollowingModel, final Desire mandatoryDesire, final Desire voluntaryDesire)
                throws ParameterException, OperationalPlanException
        {
            if (perception.getLaneStructure().getRootRecord().getRight() != null
                    && perception.getLaneStructure().getRootRecord().getRight().getRight() != null
                    && perception.getPerceptionCategory(EgoPerception.class).getSpeed()
                            .gt(parameters.getParameter(ParameterTypes.VCONG)))
            {
                // may not be on this lane
                return new Desire(0, 1);
            }
            if (mandatoryDesire.getRight() < 0 || voluntaryDesire.getRight() < 0
                    || !perception.getLaneStructure().getExtendedCrossSection().contains(RelativeLane.RIGHT))
            {
                // no desire to go right if more dominant incentives provide a negative desire to go right
                return new Desire(0, 0);
            }
            // keep right with dFree
            return new Desire(0, 1.0);
        }

    }

    /**
     * Generates headways based on demand.
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    // TODO replace with ArrivalsHeadwayGenerator and Arrivals
    private static class HeadwayGeneratorDemand implements Generator<Duration>
    {

        /** Interpolation of demand. */
        private final Interpolation interpolation;

        /** Vector of time. */
        private final TimeVector timeVector;

        /** Vector of flow values. */
        private final FrequencyVector demandVector;

        /** Simulator. */
        private final OTSSimulatorInterface simulator;

        /** Stream name of headway generation. */
        private static final String HEADWAY_STREAM = "headwayGeneration";

        /**
         * @param timeVector TimeVector; a time vector
         * @param demandVector FrequencyVector; the corresponding demand vector
         * @param simulator OTSSimulatorInterface; the simulator
         */
        HeadwayGeneratorDemand(final TimeVector timeVector, final FrequencyVector demandVector,
                final OTSSimulatorInterface simulator)
        {
            this(timeVector, demandVector, simulator, Interpolation.STEPWISE);
        }

        /**
         * @param timeVector TimeVector; a time vector
         * @param demandVector FrequencyVector; the corresponding demand vector
         * @param simulator OTSSimulatorInterface; the simulator
         * @param interpolation Interpolation; interpolation type
         */
        HeadwayGeneratorDemand(final TimeVector timeVector, final FrequencyVector demandVector,
                final OTSSimulatorInterface simulator, final Interpolation interpolation)
        {
            Throw.whenNull(timeVector, "Time vector may not be null.");
            Throw.whenNull(demandVector, "Demand vector may not be null.");
            Throw.whenNull(simulator, "Simulator may not be null.");
            Throw.whenNull(interpolation, "Interpolation may not be null.");
            Throw.whenNull(simulator.getModel().getStream(HEADWAY_STREAM),
                    "Could not obtain random stream '" + HEADWAY_STREAM + "'.");
            for (int i = 0; i < timeVector.size() - 1; i++)
            {
                try
                {
                    Throw.when(timeVector.get(i).ge(timeVector.get(i + 1)), IllegalArgumentException.class,
                            "Time vector is not increasing.");
                }
                catch (ValueRuntimeException exception)
                {
                    throw new RuntimeException(
                            "Value out of range of time vector. Note that HeadwayGenerator does not create a safe copy.",
                            exception);
                }
            }
            Throw.when(timeVector.size() != demandVector.size(), IllegalArgumentException.class,
                    "Time and flow vector should be of the same size.");
            Throw.when(timeVector.size() < 2, IllegalArgumentException.class,
                    "Time and flow vector should be at least of size 2.");
            this.timeVector = timeVector;
            this.demandVector = demandVector;
            this.simulator = simulator;
            this.interpolation = interpolation;
        }

        /** {@inheritDoc} */
        @Override
        public final Duration draw() throws ProbabilityException, ParameterException
        {
            Time time = this.simulator.getSimulatorAbsTime();
            try
            {
                Throw.when(time.lt(this.timeVector.get(0)), IllegalArgumentException.class,
                        "Cannot return a headway at time before first time in vector.");

                // get time period of current time
                int i = 0;
                while (this.timeVector.get(i + 1).lt(time) && i < this.timeVector.size() - 1)
                {
                    i++;
                }
                try
                {
                    return nextArrival(i, time.minus(this.timeVector.get(i)), 1.0).minus(time);
                }
                catch (RemoteException exception)
                {
                    throw new RuntimeException("Could not obtain replication.", exception);
                }
            }
            catch (ValueRuntimeException exception)
            {
                throw new RuntimeException(
                        "Value out of range of time or demand vector. Note that HeadwayGenerator does not create safe copies.",
                        exception);
            }
        }

        /**
         * Recursive determination of the next arrival time. Each recursion moves to the next time period. This occurs if a
         * randomly determined arrival falls outside of a time period, or when demand in a time period is 0.
         * @param i int; index of time period
         * @param start Duration; reference time from start of period i, pertains to previous arrival, or zero during recursion
         * @param fractionRemaining double; remaining fraction of headway to apply due to time in earlier time periods
         * @return time of next arrival
         * @throws ValueRuntimeException in case of an illegal time vector
         * @throws RemoteException in case of not being able to retrieve the replication
         */
        private Time nextArrival(final int i, final Duration start, final double fractionRemaining)
                throws ValueRuntimeException, RemoteException
        {

            // escape if beyond specified time by infinite next arrival (= no traffic)
            if (i == this.timeVector.size() - 1)
            {
                return new Time(Double.POSITIVE_INFINITY, TimeUnit.DEFAULT);
            }

            // skip zero-demand periods
            if (this.demandVector.get(i).equals(Frequency.ZERO))
            {
                // after zero-demand, the next headway is a random fraction of a random headway as there is no previous arrival
                return nextArrival(i + 1, Duration.ZERO, this.simulator.getModel().getStream(HEADWAY_STREAM).nextDouble());
            }

            // calculate headway from demand
            Frequency demand;
            if (this.interpolation.isStepWise())
            {
                demand = this.demandVector.get(i);
            }
            else
            {
                double f = start.si / (this.timeVector.get(i + 1).si - this.timeVector.get(i).si);
                demand = Frequency.interpolate(this.demandVector.get(i), this.demandVector.get(i + 1), f);
            }
            double t = -Math.log(this.simulator.getModel().getStream(HEADWAY_STREAM).nextDouble()) / demand.si;

            // calculate arrival
            Time arrival = new Time(this.timeVector.get(i).si + start.si + t * fractionRemaining, TimeUnit.DEFAULT);

            // go to next period if arrival is beyond current period
            if (arrival.gt(this.timeVector.get(i + 1)))
            {
                double inStep = this.timeVector.get(i + 1).si - (this.timeVector.get(i).si + start.si);
                return nextArrival(i + 1, Duration.ZERO, fractionRemaining - inStep / t);
            }

            return arrival;

        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "HeadwayGeneratorDemand [interpolation=" + this.interpolation + ", timeVector=" + this.timeVector
                    + ", demandVector=" + this.demandVector + ", simulator=" + this.simulator + "]";
        }

    }

}
