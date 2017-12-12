package ahfe;

import static org.opentrafficsim.core.gtu.GTUType.CAR;
import static org.opentrafficsim.core.gtu.GTUType.TRUCK;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.DurationUnit;
import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.LengthUnit;
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
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.distributions.ConstantGenerator;
import org.opentrafficsim.core.distributions.ProbabilityException;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterFactory;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterFactoryByType;
import org.opentrafficsim.core.gtu.perception.DirectEgoPerception;
import org.opentrafficsim.core.gtu.perception.EgoPerception;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.idgenerator.IdGenerator;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.route.FixedRouteGenerator;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.network.route.RouteGenerator;
import org.opentrafficsim.road.gtu.generator.CharacteristicsGenerator;
import org.opentrafficsim.road.gtu.generator.GTUTypeGenerator;
import org.opentrafficsim.road.gtu.generator.HeadwayGeneratorDemand;
import org.opentrafficsim.road.gtu.generator.LaneBasedGTUGenerator;
import org.opentrafficsim.road.gtu.generator.LaneBasedGTUGenerator.RoomChecker;
import org.opentrafficsim.road.gtu.generator.SpeedGenerator;
import org.opentrafficsim.road.gtu.generator.TTCRoomChecker;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.CategorialLanePerception;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionFactory;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.DelayedNeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.DelayedNeighborsPerception.Anticipation;
import org.opentrafficsim.road.gtu.lane.perception.categories.DirectInfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedTacticalPlannerFactory;
import org.opentrafficsim.road.gtu.lane.tactical.following.AbstractIDM;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModelFactory;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlus;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlusFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.GapAcceptanceModels;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveKeep;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveRoute;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveSpeedWithCourtesy;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.LMRS;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Desire;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsParameters;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Synchronization;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.VoluntaryIncentive;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlannerFactory;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 2 mrt. 2017 <br>
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
     * @param network the network
     * @param gtuColorer the GTU colorer
     * @param simulator the simulator
     * @param replication replication number
     * @param anticipationStrategy anticipation strategy
     * @param reactionTime reaction time
     * @param anticipationTime anticipation time
     * @param truckFraction truck fraction
     * @param simulationTime simulation time
     * @param leftDemand demand on left highway
     * @param rightDemand demand on right highway
     * @param leftFraction fraction of traffic generated on left lane
     * @param distanceError distance error
     * @param speedError speed error
     * @param accelerationError acceleration error
     * @throws ValueException on value error
     * @throws ParameterException on parameter error
     * @throws GTUException on gtu error
     * @throws ProbabilityException on probability error
     * @throws SimRuntimeException on sim runtime error
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public static void createDemand(final OTSNetwork network, final GTUColorer gtuColorer,
            final OTSDEVSSimulatorInterface simulator, final int replication, final String anticipationStrategy,
            final Duration reactionTime, final Duration anticipationTime, final double truckFraction, final Time simulationTime,
            final Frequency leftDemand, final Frequency rightDemand, final double leftFraction, final double distanceError,
            final double speedError, final double accelerationError)
            throws ValueException, ParameterException, GTUException, SimRuntimeException, ProbabilityException
    {

        Random seedGenerator = new Random(replication);
        Map<String, StreamInterface> streams = new HashMap<>();
        streams.put("headwayGeneration", new MersenneTwister(Math.abs(seedGenerator.nextLong()) + 1));
        streams.put("gtuClass", new MersenneTwister(Math.abs(seedGenerator.nextLong()) + 1));
        streams.put("perception", new MersenneTwister(Math.abs(seedGenerator.nextLong()) + 1));
        simulator.getReplication().setStreams(streams);

        TTCRoomChecker roomChecker = new TTCRoomChecker(new Duration(10.0, DurationUnit.SI));
        IdGenerator idGenerator = new IdGenerator("");

        CarFollowingModelFactory<IDMPlus> idmPlusFactory = new IDMPlusFactory();
        PerceptionFactory delayedPerceptionFactory =
                new DelayedPerceptionFactory(Anticipation.valueOf(anticipationStrategy.toUpperCase()));
        Parameters params = new Parameters();
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
        GTUType gtuType = new GTUType("car", CAR);
        bcFactory.addGaussianParameter(gtuType, ParameterTypes.FSPEED, 123.7 / 120, 12.0 / 120, streams.get("gtuClass"));
        bcFactory.addParameter(gtuType, ParameterTypes.B, b);
        // bcFactory.addGaussianParameter(gtuType, ParameterTypes.LOOKAHEAD, lookAhead, lookAheadStdev,
        // streams.get("gtuClass"));
        bcFactory.addParameter(gtuType, ParameterTypes.PERCEPTION, perception);
        gtuType = new GTUType("truck", TRUCK);
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
        RouteGenerator fixedRouteGeneratorLeft = new FixedRouteGenerator(leftRoute);
        RouteGenerator fixedRouteGeneratorRight = new FixedRouteGenerator(rightRoute);

        SpeedGenerator speedCar = new SpeedGenerator(new Speed(160.0, SpeedUnit.KM_PER_HOUR),
                new Speed(200.0, SpeedUnit.KM_PER_HOUR), streams.get("gtuClass"));
        // TODO gaussian 85 +/- 2.5 for match with default LMRS
        SpeedGenerator speedTruck = new SpeedGenerator(new Speed(80.0, SpeedUnit.KM_PER_HOUR),
                new Speed(95.0, SpeedUnit.KM_PER_HOUR), streams.get("gtuClass"));
        GTUTypeGenerator gtuTypeGeneratorLeft = new GTUTypeGenerator(simulator, streams.get("gtuClass"));
        GTUTypeGenerator gtuTypeGeneratorRight = new GTUTypeGenerator(simulator, streams.get("gtuClass"));
        if (truckFraction < 1 - leftFraction)
        {
            gtuTypeGeneratorLeft.addType(new ConstantGenerator<>(Length.createSI(4.0)),
                    new ConstantGenerator<>(Length.createSI(2.0)), new GTUType("car", CAR), speedCar, 1.0);
            double p = truckFraction / (1 - leftFraction);
            gtuTypeGeneratorRight.addType(new ConstantGenerator<>(Length.createSI(4.0)),
                    new ConstantGenerator<>(Length.createSI(2.0)), new GTUType("car", CAR), speedCar, 1.0 - p);
            gtuTypeGeneratorRight.addType(new ConstantGenerator<>(Length.createSI(15.0)),
                    new ConstantGenerator<>(Length.createSI(2.5)), new GTUType("truck", TRUCK), speedTruck, p);
        }
        else
        {
            double p = (truckFraction - (1 - leftFraction)) / leftFraction;
            gtuTypeGeneratorLeft.addType(new ConstantGenerator<>(Length.createSI(4.0)),
                    new ConstantGenerator<>(Length.createSI(2.0)), new GTUType("car", CAR), speedCar, 1.0 - p);
            gtuTypeGeneratorLeft.addType(new ConstantGenerator<>(Length.createSI(15.0)),
                    new ConstantGenerator<>(Length.createSI(2.5)), new GTUType("truck", TRUCK), speedTruck, p);
            gtuTypeGeneratorRight.addType(new ConstantGenerator<>(Length.createSI(15.0)),
                    new ConstantGenerator<>(Length.createSI(2.5)), new GTUType("truck", TRUCK), speedTruck, 1.0);
        }

        TimeVector timeVector =
                new TimeVector(new double[] { 0, 360, 1560, 2160, 3960 }, TimeUnit.BASE_SECOND, StorageType.DENSE);
        double leftLeft = leftDemand.si * leftFraction;
        FrequencyVector leftLeftDemandPattern = new FrequencyVector(
                new double[] { leftLeft * 0.5, leftLeft * 0.5, leftLeft, leftLeft, 0.0 }, FrequencyUnit.SI, StorageType.DENSE);
        double leftRight = leftDemand.si * (1 - leftFraction);
        FrequencyVector leftRightDemandPattern =
                new FrequencyVector(new double[] { leftRight * 0.5, leftRight * 0.5, leftRight, leftRight, 0.0 },
                        FrequencyUnit.SI, StorageType.DENSE);
        double rightLeft = rightDemand.si * leftFraction;
        FrequencyVector rightLeftDemandPattern =
                new FrequencyVector(new double[] { rightLeft * 0.5, rightLeft * 0.5, rightLeft, rightLeft, 0.0 },
                        FrequencyUnit.SI, StorageType.DENSE);
        double rightRight = rightDemand.si * (1 - leftFraction);
        FrequencyVector rightRightDemandPattern =
                new FrequencyVector(new double[] { rightRight * 0.5, rightRight * 0.5, rightRight, rightRight, 0.0 },
                        FrequencyUnit.SI, StorageType.DENSE);
        // This defaults to stepwise interpolation, should have been linear.
        HeadwayGeneratorDemand leftLeftHeadways = new HeadwayGeneratorDemand(timeVector, leftLeftDemandPattern, simulator);
        HeadwayGeneratorDemand leftRightHeadways = new HeadwayGeneratorDemand(timeVector, leftRightDemandPattern, simulator);
        HeadwayGeneratorDemand rightLeftHeadways = new HeadwayGeneratorDemand(timeVector, rightLeftDemandPattern, simulator);
        HeadwayGeneratorDemand rightRightHeadways = new HeadwayGeneratorDemand(timeVector, rightRightDemandPattern, simulator);

        Speed genSpeed = new Speed(120.0, SpeedUnit.KM_PER_HOUR);
        CrossSectionLink leftLink = (CrossSectionLink) network.getLink("LEFTINPRE");
        CrossSectionLink rightLink = (CrossSectionLink) network.getLink("RIGHTINPRE");
        makeGenerator(getLane(leftLink, "FORWARD1"), genSpeed, "LEFTLEFT", fixedRouteGeneratorLeft, idGenerator, simulator,
                network, gtuTypeGeneratorLeft, leftLeftHeadways, gtuColorer, roomChecker, bcFactory, tacticalFactory,
                simulationTime);
        makeGenerator(getLane(leftLink, "FORWARD2"), genSpeed, "LEFTRIGHT", fixedRouteGeneratorLeft, idGenerator, simulator,
                network, gtuTypeGeneratorRight, leftRightHeadways, gtuColorer, roomChecker, bcFactory, tacticalFactory,
                simulationTime);
        makeGenerator(getLane(rightLink, "FORWARD1"), genSpeed, "RIGHTLEFT", fixedRouteGeneratorRight, idGenerator, simulator,
                network, gtuTypeGeneratorLeft, rightLeftHeadways, gtuColorer, roomChecker, bcFactory, tacticalFactory,
                simulationTime);
        makeGenerator(getLane(rightLink, "FORWARD2"), genSpeed, "RIGHTRIGHT", fixedRouteGeneratorRight, idGenerator, simulator,
                network, gtuTypeGeneratorRight, rightRightHeadways, gtuColorer, roomChecker, bcFactory, tacticalFactory,
                simulationTime);

    }

    /**
     * Get lane from link by id.
     * @param link link
     * @param id id
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
     * @param lane the reference lane for this generator
     * @param generationSpeed the speed of the GTU
     * @param id the id of the generator itself
     * @param routeGenerator the generator for the route
     * @param idGenerator the generator for the ID
     * @param simulator the simulator
     * @param network the network
     * @param gtuTypeGenerator the type generator for the GTU
     * @param headwayGenerator the headway generator for the GTU
     * @param gtuColorer the GTU colorer for animation
     * @param roomChecker the checker to see if there is room for the GTU
     * @param bcFactory the factory to generate parameters for the GTU
     * @param tacticalFactory the generator for the tactical planner
     * @param simulationTime simulation time
     * @throws SimRuntimeException in case of scheduling problems
     * @throws ProbabilityException in case of an illegal probability distribution
     * @throws GTUException in case the GTU is inconsistent
     * @throws ParameterException in case a parameter for the perception is missing
     */
    private static void makeGenerator(final Lane lane, final Speed generationSpeed, final String id,
            final RouteGenerator routeGenerator, final IdGenerator idGenerator, final OTSDEVSSimulatorInterface simulator,
            final OTSNetwork network, final GTUTypeGenerator gtuTypeGenerator, final HeadwayGeneratorDemand headwayGenerator,
            final GTUColorer gtuColorer, final RoomChecker roomChecker, final ParameterFactory bcFactory,
            final LaneBasedTacticalPlannerFactory<?> tacticalFactory, final Time simulationTime)
            throws SimRuntimeException, ProbabilityException, GTUException, ParameterException
    {
        Set<DirectedLanePosition> initialLongitudinalPositions = new HashSet<>();
        // TODO DIR_MINUS
        initialLongitudinalPositions
                .add(new DirectedLanePosition(lane, new Length(10.0, LengthUnit.SI), GTUDirectionality.DIR_PLUS));

        LaneBasedStrategicalRoutePlannerFactory strategicalFactory =
                new LaneBasedStrategicalRoutePlannerFactory(tacticalFactory, bcFactory);

        CharacteristicsGenerator characteristicsGenerator = new CharacteristicsGenerator(strategicalFactory, routeGenerator,
                simulator, gtuTypeGenerator, generationSpeed, initialLongitudinalPositions);

        new LaneBasedGTUGenerator(id, headwayGenerator, Long.MAX_VALUE, Time.ZERO, simulationTime, gtuColorer,
                characteristicsGenerator, initialLongitudinalPositions, network, simulator, roomChecker, idGenerator);
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
         * @param carFollowingModelFactory factory of the car-following model
         * @param defaultCarFollowingParameters default set of parameters for the car-following model
         * @param perceptionFactory perception factory
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
            Parameters parameters = new Parameters();
            parameters.setDefaultParameters(ParameterTypes.class);
            parameters.setDefaultParameters(LmrsParameters.class);
            parameters.setAll(this.defaultCarFollowingParameters);
            return parameters;
        }

        /** {@inheritDoc} */
        @Override
        public final LMRS create(final LaneBasedGTU gtu) throws GTUException
        {
            LMRS lmrs = new LMRS(this.carFollowingModelFactory.generateCarFollowingModel(), gtu,
                    this.perceptionFactory.generatePerception(gtu), Synchronization.PASSIVE, GapAcceptanceModels.INFORMED);
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
        public final String toString()
        {
            return "LMRSFactory [car-following=" + this.carFollowingModelFactory + "]";
        }

    }

    /**
     * Perception factory with delay and anticipation for neighbors.
     * <p>
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 2 mrt. 2017 <br>
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
         * @param anticipation anticipation form.
         */
        DelayedPerceptionFactory(final Anticipation anticipation)
        {
            this.anticipation = anticipation;
        }

        /** {@inheritDoc} */
        @Override
        public LanePerception generatePerception(final LaneBasedGTU gtu)
        {
            LanePerception perception = new CategorialLanePerception(gtu);
            perception.addPerceptionCategory(new DirectEgoPerception(perception));
            // perception.addPerceptionCategory(new DirectDefaultSimplePerception(perception));
            perception.addPerceptionCategory(new DirectInfrastructurePerception(perception));
            // perception.addPerceptionCategory(new DirectNeighborsPerception(perception));
            perception.addPerceptionCategory(new DelayedNeighborsPerception(perception, this.anticipation));
            // perception.addPerceptionCategory(new DirectIntersectionPerception(perception));
            return perception;
        }

        /** {@inheritDoc} */
        @Override
        public Parameters getParameters()
        {
            return new Parameters();
        }

    }

    /**
     * <p>
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 3 mrt. 2017 <br>
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
            if (perception.getLaneStructure().getRootLSR().getRight() != null
                    && perception.getLaneStructure().getRootLSR().getRight().getRight() != null
                    && perception.getPerceptionCategory(EgoPerception.class).getSpeed()
                            .gt(parameters.getParameter(ParameterTypes.VCONG)))
            {
                // may not be on this lane
                return new Desire(0, 1);
            }
            if (mandatoryDesire.getRight() < 0 || voluntaryDesire.getRight() < 0
                    || !perception.getLaneStructure().getCrossSection().contains(RelativeLane.RIGHT))
            {
                // no desire to go right if more dominant incentives provide a negative desire to go right
                return new Desire(0, 0);
            }
            // keep right with dFree
            return new Desire(0, 1.0);
        }

    }

}
