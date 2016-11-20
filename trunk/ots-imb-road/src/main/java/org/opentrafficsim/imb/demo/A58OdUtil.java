package org.opentrafficsim.imb.demo;

import static org.djunits.value.StorageType.DENSE;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.ValueException;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djunits.value.vdouble.vector.FrequencyVector;
import org.djunits.value.vdouble.vector.TimeVector;
import org.opentrafficsim.core.distributions.ProbabilityException;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristicsFactory;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypes;
import org.opentrafficsim.core.idgenerator.IdGenerator;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.core.network.route.RouteGenerator;
import org.opentrafficsim.imb.demo.generators.BehavioralCharacteristicsFactoryByType;
import org.opentrafficsim.imb.demo.generators.CharacteristicsGenerator;
import org.opentrafficsim.imb.demo.generators.GTUTypeGenerator;
import org.opentrafficsim.imb.demo.generators.HeadwayGeneratorDemand;
import org.opentrafficsim.imb.demo.generators.RouteGeneratorProbability;
import org.opentrafficsim.road.gtu.generator.LaneBasedGTUGenerator;
import org.opentrafficsim.road.gtu.generator.LaneBasedGTUGenerator.RoomChecker;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedGTUFollowingDirectedChangeTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedGTUFollowingDirectedChangeTacticalPlannerFactory;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedTacticalPlannerFactory;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlusOld;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlannerFactory;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.language.Throw;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 18 nov. 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class A58OdUtil
{

    /** Truck fraction. */
    private final static double truckFraction = 0.15;

    /** Simulation period. */
    private final static Time simPeriod = new Time(1.0, TimeUnit.HOUR);

    /** Time vector. */
    private static TimeVector timeVector;

    static
    {
        double[] t = new double[61];
        for (int i = 0; i < 61; i++)
        {
            t[i] = i;
        }
        try
        {
            timeVector = new TimeVector(t, TimeUnit.MINUTE, DENSE);
        }
        catch (ValueException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * @param network the network
     * @param gtuColorer the GTU colorer
     * @param simulator the simulator
     * @param penetrationRate the penetration rate parameter
     */
    public static void createDemand(final OTSNetwork network, final GTUColorer gtuColorer,
            final OTSDEVSSimulatorInterface simulator, double penetrationRate)
    {

        A58RoomChecker roomChecker = new A58RoomChecker();
        IdGenerator idGenerator = new IdGenerator("");
        LaneBasedTacticalPlannerFactory<LaneBasedGTUFollowingDirectedChangeTacticalPlanner> tacticalFactory =
                new LaneBasedGTUFollowingDirectedChangeTacticalPlannerFactory(new IDMPlusOld());

        BehavioralCharacteristicsFactoryByType bcFactory = new BehavioralCharacteristicsFactoryByType();
        GTUType gtuType = new GTUType("car");
        bcFactory.addGaussianParameter(gtuType, ParameterTypes.FSPEED, 123.7 / 120, 12 / 120);
        gtuType = new GTUType("car_equipped");
        bcFactory.addGaussianParameter(gtuType, ParameterTypes.FSPEED, 123.7 / 120, 12 / 120);
        bcFactory.addParameter(gtuType, ParameterTypes.T, new Duration(0.6, TimeUnit.SI));
        bcFactory.addParameter(gtuType, ParameterTypes.A, new Acceleration(2.0, AccelerationUnit.SI));
        gtuType = new GTUType("truck");
        bcFactory.addParameter(gtuType, ParameterTypes.A, new Acceleration(0.6, AccelerationUnit.SI));
        gtuType = new GTUType("truck_equipped");
        bcFactory.addParameter(gtuType, ParameterTypes.A, new Acceleration(0.6, AccelerationUnit.SI));
        bcFactory.addParameter(gtuType, ParameterTypes.T, new Duration(0.6, TimeUnit.SI));

        for (String source : demandMap.keySet())
        {
            OTSNode from = (OTSNode) network.getNode(nodeMap.get(source + " source"));
            Throw.whenNull(from, "Node %s of source %s could not be found.", nodeMap.get(source + " source"), from);
            Throw.when(from.getLinks().size() != 1, RuntimeException.class,
                    "Node %s of source %s connects to multiple or no links.", nodeMap.get(source + " source"), from);
            CrossSectionLink link = (CrossSectionLink) from.getLinks().iterator().next();

            // sum demand
            int nLanes = link.getLanes().size();
            double[] demandArray = null;
            for (String dest : demandMap.get(source).keySet())
            {
                OTSNode to = (OTSNode) network.getNode(nodeMap.get(dest + " dest"));
                Throw.whenNull(to, "Node %s of destination %s could not be found.", nodeMap.get(dest + " dest"), to);
                Throw.when(to.getLinks().size() != 1, RuntimeException.class,
                        "Node %s of destination %s connects to multiple or no links.", nodeMap.get(dest + " dest"), to);

                // TODO create sinks in separate method, remember which links have been coupled to a sink to skip

                if (demandArray == null)
                {
                    demandArray = (double[]) demandMap.get(source).get(dest);
                }
                else
                {
                    demandArray = arraySum(demandArray, (double[]) demandMap.get(source).get(dest));
                }
            }
            demandArray = factorCopy(demandArray, 1.0 / nLanes);
            FrequencyVector demandVector;
            try
            {
                demandVector = new FrequencyVector(demandArray, FrequencyUnit.PER_HOUR, DENSE);
            }
            catch (ValueException exception)
            {
                throw new RuntimeException(exception);
            }

            // truck fraction
            double truckFrac = truckFraction * nLanes; // fraction for all trucks on right-hand lane only
            Throw.when(truckFrac > 1, UnsupportedOperationException.class,
                    "Number of lanes and truck fraction are such that trucks are present on more than only the right-hand lane."
                            + " This is not supported.");

            RouteGeneratorProbability routeGenerator = new RouteGeneratorProbability(network, timeVector, from, simulator);
            for (String dest : demandMap.get(source).keySet())
            {
                OTSNode to = (OTSNode) network.getNode(nodeMap.get(dest + " dest"));
                routeGenerator.addDemand(to, (double[]) demandMap.get(source).get(dest));
            }

            HeadwayGeneratorDemand headwayGenerator = new HeadwayGeneratorDemand(timeVector, demandVector, simulator);

            GTUTypeGenerator gtuTypeGenerator = new GTUTypeGenerator();
            gtuTypeGenerator.addType(new Length(4.0, LengthUnit.SI), new Length(2.0, LengthUnit.SI), new GTUType("car"),
                    new Speed(200.0, SpeedUnit.KM_PER_HOUR), (1.0 - penetrationRate));
            gtuTypeGenerator.addType(new Length(4.0, LengthUnit.SI), new Length(2.0, LengthUnit.SI),
                    new GTUType("car_equipped"), new Speed(200.0, SpeedUnit.KM_PER_HOUR), penetrationRate);

            Map<String, Lane> lanesById = new HashMap<>();
            for (Lane lane : link.getLanes())
            {
                lanesById.put(lane.getId(), lane);
            }
            try
            {
                for (int i = 1; i < nLanes; i++)
                {
                    Lane lane = lanesById.get("A" + i);
                    Speed generationSpeed = new Speed(120, SpeedUnit.KM_PER_HOUR); // by definition > 1 lane on this loop
                    String id = link.getId() + ":A" + i;
                    makeGenerator(lane, generationSpeed, id, routeGenerator, idGenerator, simulator, network, gtuTypeGenerator,
                            headwayGenerator, gtuColorer, roomChecker, bcFactory, tacticalFactory);
                }
                // add trucks
                gtuTypeGenerator = new GTUTypeGenerator();
                gtuTypeGenerator.addType(new Length(4.0, LengthUnit.SI), new Length(2.0, LengthUnit.SI), new GTUType("car"),
                        new Speed(200.0, SpeedUnit.KM_PER_HOUR), (1.0 - penetrationRate) * (1 - truckFrac));
                gtuTypeGenerator.addType(new Length(4.0, LengthUnit.SI), new Length(2.0, LengthUnit.SI),
                        new GTUType("car_equipped"), new Speed(200.0, SpeedUnit.KM_PER_HOUR),
                        penetrationRate * (1 - truckFrac));
                gtuTypeGenerator.addType(new Length(15.0, LengthUnit.SI), new Length(2.5, LengthUnit.SI), new GTUType("truck"),
                        new Speed(85.0, SpeedUnit.KM_PER_HOUR), (1.0 - penetrationRate) * truckFrac);
                gtuTypeGenerator.addType(new Length(15.0, LengthUnit.SI), new Length(2.5, LengthUnit.SI),
                        new GTUType("truck_equipped"), new Speed(85.0, SpeedUnit.KM_PER_HOUR), penetrationRate * truckFrac);
                Lane lane = lanesById.get("A" + nLanes);
                Speed generationSpeed = new Speed(nLanes == 1 ? 60.0 : 120, SpeedUnit.KM_PER_HOUR);
                String id = link.getId() + ":A" + nLanes;
                makeGenerator(lane, generationSpeed, id, routeGenerator, idGenerator, simulator, network, gtuTypeGenerator,
                        headwayGenerator, gtuColorer, roomChecker, bcFactory, tacticalFactory);
            }
            catch (SimRuntimeException | ProbabilityException | GTUException | ParameterException exception)
            {
                exception.printStackTrace();
            }

        }
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
     * @param bcFactory the factory to generate behavioral characteristics for the GTU
     * @param tacticalFactory the generator for the tactical planner
     * @throws SimRuntimeException in case of scheduling problems
     * @throws ProbabilityException in case of an illegal probability distribution
     * @throws GTUException in case the GTU is inconsistent
     * @throws ParameterException in case a parameter for the perception is missing
     */
    private static void makeGenerator(final Lane lane, final Speed generationSpeed, final String id,
            final RouteGenerator routeGenerator, final IdGenerator idGenerator, final OTSDEVSSimulatorInterface simulator,
            final OTSNetwork network, final GTUTypeGenerator gtuTypeGenerator, final HeadwayGeneratorDemand headwayGenerator,
            final GTUColorer gtuColorer, final RoomChecker roomChecker, final BehavioralCharacteristicsFactory bcFactory,
            final LaneBasedTacticalPlannerFactory<?> tacticalFactory)
            throws SimRuntimeException, ProbabilityException, GTUException, ParameterException
    {
        Set<DirectedLanePosition> initialLongitudinalPositions = new HashSet<>();
        // TODO DIR_MINUS
        initialLongitudinalPositions
                .add(new DirectedLanePosition(lane, new Length(10.0, LengthUnit.SI), GTUDirectionality.DIR_PLUS));

        LaneBasedStrategicalRoutePlannerFactory strategicalFactory =
                new LaneBasedStrategicalRoutePlannerFactory(tacticalFactory, routeGenerator, bcFactory);

        CharacteristicsGenerator characteristicsGenerator = new CharacteristicsGenerator(strategicalFactory, idGenerator,
                simulator, network, gtuTypeGenerator, generationSpeed, initialLongitudinalPositions);

        new LaneBasedGTUGenerator(id, headwayGenerator, Long.MAX_VALUE, Time.ZERO, simPeriod, gtuColorer,
                characteristicsGenerator, initialLongitudinalPositions, network, roomChecker);
    }

    /**
     * @param in the input vector
     * @param factor the multiplication factor
     * @return array with values multiplied by factor
     */
    private static double[] factorCopy(final double[] in, final double factor)
    {
        double out[] = new double[in.length];
        for (int i = 0; i < in.length; i++)
        {
            out[i] = in[i] * factor;
        }
        return out;
    }

    /**
     * @param in1 the first vector
     * @param in2 the second vector
     * @return sum per element of arrays
     */
    private static double[] arraySum(final double[] in1, final double[] in2)
    {
        double out[] = new double[in1.length];
        for (int i = 0; i < in1.length; i++)
        {
            out[i] = in1[i] + in2[i];
        }
        return out;
    }

    /** Map of demand. */
    private static HashMap<String, HashMap<String, Object>> demandMap = new HashMap<>();

    /** Conversion table of origin and destination names and node ids. */
    private static HashMap<String, String> nodeMap = new HashMap<>();

    static
    {
        demandMap.put("Junction De Baars South E", new HashMap<>());
        demandMap.get("Junction De Baars South E").put("Restplace Kerkeind",
                new double[] { 25.0, 25.0, 27.0, 28.0, 22.0, 26.0, 16.0, 17.0, 22.0, 23.0, 28.0, 22.0, 17.0, 34.0, 21.0, 21.0,
                        21.0, 15.0, 19.0, 23.0, 20.0, 16.0, 17.0, 22.0, 26.0, 19.0, 25.0, 16.0, 20.0, 29.0, 33.0, 14.0, 20.0,
                        24.0, 22.0, 18.0, 17.0, 25.0, 24.0, 17.0, 23.0, 29.0, 21.0, 19.0, 22.0, 23.0, 17.0, 31.0, 22.0, 22.0,
                        22.0, 17.0, 20.0, 13.0, 22.0, 16.0, 19.0, 16.0, 21.0, 17.0, 0.0 });
        demandMap.get("Junction De Baars South E").put("Petrol station",
                new double[] { 25.0, 24.0, 26.0, 28.0, 21.0, 26.0, 16.0, 16.0, 22.0, 23.0, 27.0, 21.0, 17.0, 34.0, 21.0, 21.0,
                        21.0, 15.0, 19.0, 23.0, 20.0, 16.0, 17.0, 21.0, 25.0, 18.0, 24.0, 15.0, 19.0, 29.0, 32.0, 14.0, 19.0,
                        24.0, 21.0, 18.0, 16.0, 25.0, 24.0, 16.0, 23.0, 28.0, 21.0, 19.0, 22.0, 23.0, 17.0, 30.0, 21.0, 22.0,
                        21.0, 17.0, 19.0, 13.0, 21.0, 15.0, 19.0, 15.0, 21.0, 17.0, 0.0 });
        demandMap.get("Junction De Baars South E").put("Connection Moergestel E",
                new double[] { 242.0, 236.0, 259.0, 271.0, 207.0, 254.0, 156.0, 161.0, 213.0, 225.0, 265.0, 207.0, 167.0, 328.0,
                        202.0, 202.0, 202.0, 144.0, 184.0, 225.0, 196.0, 156.0, 167.0, 207.0, 248.0, 179.0, 236.0, 150.0, 190.0,
                        282.0, 317.0, 138.0, 190.0, 230.0, 207.0, 173.0, 161.0, 242.0, 230.0, 161.0, 225.0, 277.0, 202.0, 184.0,
                        213.0, 225.0, 167.0, 294.0, 207.0, 213.0, 207.0, 167.0, 190.0, 127.0, 207.0, 150.0, 184.0, 150.0, 202.0,
                        167.0, 0.0 });
        demandMap.get("Junction De Baars South E").put("Connection Oirschot E",
                new double[] { 194.0, 189.0, 207.0, 217.0, 166.0, 203.0, 124.0, 129.0, 171.0, 180.0, 212.0, 166.0, 134.0, 263.0,
                        161.0, 161.0, 161.0, 115.0, 148.0, 180.0, 157.0, 124.0, 134.0, 166.0, 198.0, 143.0, 189.0, 120.0, 152.0,
                        226.0, 254.0, 111.0, 152.0, 184.0, 166.0, 138.0, 129.0, 194.0, 184.0, 129.0, 180.0, 221.0, 161.0, 148.0,
                        171.0, 180.0, 134.0, 235.0, 166.0, 171.0, 166.0, 134.0, 152.0, 101.0, 166.0, 120.0, 148.0, 120.0, 161.0,
                        134.0, 0.0 });
        demandMap.get("Junction De Baars South E").put("Restplace Kloosters",
                new double[] { 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                        1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                        1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                        0.0 });
        demandMap.get("Junction De Baars South E").put("Connection Best E",
                new double[] { 232.0, 227.0, 249.0, 260.0, 199.0, 243.0, 149.0, 155.0, 204.0, 215.0, 254.0, 199.0, 160.0, 315.0,
                        193.0, 193.0, 193.0, 138.0, 177.0, 215.0, 188.0, 149.0, 160.0, 199.0, 238.0, 171.0, 227.0, 144.0, 182.0,
                        271.0, 304.0, 133.0, 182.0, 221.0, 199.0, 166.0, 155.0, 232.0, 221.0, 155.0, 215.0, 265.0, 193.0, 177.0,
                        204.0, 215.0, 160.0, 282.0, 199.0, 204.0, 199.0, 160.0, 182.0, 122.0, 199.0, 144.0, 177.0, 144.0, 193.0,
                        160.0, 0.0 });
        demandMap.get("Junction De Baars South E").put("Junction Batadorp E",
                new double[] { 542.0, 529.0, 581.0, 606.0, 464.0, 568.0, 348.0, 361.0, 477.0, 503.0, 593.0, 464.0, 374.0, 734.0,
                        452.0, 452.0, 452.0, 323.0, 413.0, 503.0, 439.0, 348.0, 374.0, 464.0, 555.0, 400.0, 529.0, 335.0, 426.0,
                        632.0, 709.0, 310.0, 426.0, 516.0, 464.0, 387.0, 361.0, 542.0, 516.0, 361.0, 503.0, 619.0, 452.0, 413.0,
                        477.0, 503.0, 374.0, 658.0, 464.0, 477.0, 464.0, 374.0, 426.0, 284.0, 464.0, 335.0, 413.0, 335.0, 452.0,
                        374.0, 0.0 });
        demandMap.put("Junction De Baars North E", new HashMap<>());
        demandMap.get("Junction De Baars North E").put("Restplace Kerkeind",
                new double[] { 11.0, 5.0, 1.0, 7.0, 7.0, 8.0, 8.0, 6.0, 4.0, 3.0, 8.0, 6.0, 7.0, 0.0, 1.0, 4.0, 13.0, 0.0, 8.0,
                        0.0, 8.0, 7.0, 6.0, 0.0, 9.0, 13.0, 0.0, 9.0, 0.0, 8.0, 0.0, 7.0, 4.0, 0.0, 0.0, 0.0, 0.0, 7.0, 6.0,
                        18.0, 7.0, 1.0, 9.0, 7.0, 2.0, 3.0, 10.0, 0.0, 4.0, 7.0, 0.0, 9.0, 2.0, 13.0, 0.0, 13.0, 2.0, 8.0, 0.0,
                        5.0, 0.0 });
        demandMap.get("Junction De Baars North E").put("Petrol station",
                new double[] { 11.0, 5.0, 1.0, 6.0, 7.0, 8.0, 8.0, 6.0, 4.0, 3.0, 8.0, 6.0, 6.0, 0.0, 1.0, 4.0, 12.0, 0.0, 8.0,
                        0.0, 8.0, 7.0, 6.0, 0.0, 9.0, 12.0, 0.0, 9.0, 0.0, 8.0, 0.0, 7.0, 4.0, 0.0, 0.0, 0.0, 0.0, 7.0, 6.0,
                        18.0, 6.0, 1.0, 9.0, 6.0, 2.0, 3.0, 9.0, 0.0, 4.0, 7.0, 0.0, 9.0, 2.0, 13.0, 0.0, 13.0, 2.0, 8.0, 0.0,
                        5.0, 0.0 });
        demandMap.get("Junction De Baars North E").put("Connection Moergestel E",
                new double[] { 104.0, 46.0, 12.0, 63.0, 69.0, 81.0, 81.0, 58.0, 35.0, 29.0, 75.0, 58.0, 63.0, 0.0, 12.0, 35.0,
                        121.0, 0.0, 75.0, 0.0, 81.0, 69.0, 58.0, 0.0, 86.0, 121.0, 0.0, 86.0, 0.0, 81.0, 0.0, 69.0, 35.0, 0.0,
                        0.0, 0.0, 0.0, 69.0, 58.0, 173.0, 63.0, 6.0, 86.0, 63.0, 23.0, 29.0, 92.0, 0.0, 35.0, 69.0, 0.0, 86.0,
                        17.0, 127.0, 0.0, 127.0, 17.0, 75.0, 0.0, 46.0, 0.0 });
        demandMap.get("Junction De Baars North E").put("Connection Oirschot E",
                new double[] { 83.0, 37.0, 9.0, 51.0, 55.0, 65.0, 65.0, 46.0, 28.0, 23.0, 60.0, 46.0, 51.0, 0.0, 9.0, 28.0,
                        97.0, 0.0, 60.0, 0.0, 65.0, 55.0, 46.0, 0.0, 69.0, 97.0, 0.0, 69.0, 0.0, 65.0, 0.0, 55.0, 28.0, 0.0,
                        0.0, 0.0, 0.0, 55.0, 46.0, 138.0, 51.0, 5.0, 69.0, 51.0, 18.0, 23.0, 74.0, 0.0, 28.0, 55.0, 0.0, 69.0,
                        14.0, 101.0, 0.0, 101.0, 14.0, 60.0, 0.0, 37.0, 0.0 });
        demandMap.get("Junction De Baars North E").put("Restplace Kloosters",
                new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                        0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0,
                        0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0,
                        0.0 });
        demandMap.get("Junction De Baars North E").put("Connection Best E",
                new double[] { 99.0, 44.0, 11.0, 61.0, 66.0, 77.0, 77.0, 55.0, 33.0, 28.0, 72.0, 55.0, 61.0, 0.0, 11.0, 33.0,
                        116.0, 0.0, 72.0, 0.0, 77.0, 66.0, 55.0, 0.0, 83.0, 116.0, 0.0, 83.0, 0.0, 77.0, 0.0, 66.0, 33.0, 0.0,
                        0.0, 0.0, 0.0, 66.0, 55.0, 166.0, 61.0, 6.0, 83.0, 61.0, 22.0, 28.0, 88.0, 0.0, 33.0, 66.0, 0.0, 83.0,
                        17.0, 122.0, 0.0, 122.0, 17.0, 72.0, 0.0, 44.0, 0.0 });
        demandMap.get("Junction De Baars North E").put("Junction Batadorp E",
                new double[] { 232.0, 103.0, 26.0, 142.0, 155.0, 181.0, 181.0, 129.0, 77.0, 65.0, 168.0, 129.0, 142.0, 0.0,
                        26.0, 77.0, 271.0, 0.0, 168.0, 0.0, 181.0, 155.0, 129.0, 0.0, 194.0, 271.0, 0.0, 194.0, 0.0, 181.0, 0.0,
                        155.0, 77.0, 0.0, 0.0, 0.0, 0.0, 155.0, 129.0, 387.0, 142.0, 13.0, 194.0, 142.0, 52.0, 65.0, 206.0, 0.0,
                        77.0, 155.0, 0.0, 194.0, 39.0, 284.0, 0.0, 284.0, 39.0, 168.0, 0.0, 103.0, 0.0 });
        demandMap.put("Restplace Kerkeind", new HashMap<>());
        demandMap.get("Restplace Kerkeind").put("Petrol station",
                new double[] { 2.0, 2.0, 0.0, 0.0, 0.0, 0.0, 4.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0,
                        0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 3.0, 0.0, 0.0, 0.0, 0.0,
                        0.0, 0.0, 3.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 2.0, 0.0, 0.0, 0.0,
                        0.0 });
        demandMap.get("Restplace Kerkeind").put("Connection Moergestel E",
                new double[] { 19.0, 17.0, 0.0, 0.0, 0.0, 0.0, 36.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 11.0,
                        0.0, 0.0, 0.0, 0.0, 0.0, 11.0, 0.0, 0.0, 0.0, 0.0, 2.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 32.0, 0.0, 0.0,
                        0.0, 0.0, 0.0, 0.0, 25.0, 0.0, 0.0, 0.0, 0.0, 0.0, 8.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 15.0, 0.0,
                        0.0, 1.0, 0.0 });
        demandMap.get("Restplace Kerkeind").put("Connection Oirschot E",
                new double[] { 15.0, 14.0, 0.0, 0.0, 0.0, 0.0, 29.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 9.0, 0.0,
                        0.0, 0.0, 0.0, 0.0, 9.0, 0.0, 0.0, 0.0, 0.0, 2.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 26.0, 0.0, 0.0, 0.0,
                        0.0, 0.0, 0.0, 20.0, 0.0, 0.0, 0.0, 0.0, 0.0, 7.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 12.0, 0.0, 0.0,
                        1.0, 0.0 });
        demandMap.get("Restplace Kerkeind").put("Connection Best E",
                new double[] { 18.0, 16.0, 0.0, 0.0, 0.0, 0.0, 34.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 11.0,
                        0.0, 0.0, 0.0, 0.0, 0.0, 10.0, 0.0, 0.0, 0.0, 0.0, 2.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 31.0, 0.0, 0.0,
                        0.0, 0.0, 0.0, 0.0, 24.0, 0.0, 0.0, 0.0, 0.0, 0.0, 8.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 14.0, 0.0,
                        0.0, 1.0, 0.0 });
        demandMap.get("Restplace Kerkeind").put("Junction Batadorp E",
                new double[] { 43.0, 39.0, 0.0, 0.0, 1.0, 0.0, 79.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 24.0,
                        0.0, 0.0, 0.0, 0.0, 0.0, 24.0, 0.0, 0.0, 0.0, 0.0, 6.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 72.0, 0.0, 0.0,
                        0.0, 0.0, 0.0, 0.0, 57.0, 0.0, 0.0, 0.0, 0.0, 0.0, 18.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 33.0, 0.0,
                        0.0, 2.0, 0.0 });
        demandMap.put("Petrol station", new HashMap<>());
        demandMap.get("Petrol station").put("Connection Moergestel E",
                new double[] { 0.0, 44.0, 83.0, 0.0, 0.0, 20.0, 0.0, 35.0, 0.0, 0.0, 9.0, 36.0, 12.0, 4.0, 0.0, 0.0, 0.0, 23.0,
                        0.0, 0.0, 0.0, 25.0, 0.0, 0.0, 2.0, 0.0, 0.0, 0.0, 0.0, 0.0, 8.0, 5.0, 24.0, 11.0, 0.0, 24.0, 23.0,
                        12.0, 0.0, 0.0, 26.0, 0.0, 7.0, 12.0, 65.0, 11.0, 47.0, 0.0, 0.0, 28.0, 0.0, 0.0, 45.0, 0.0, 0.0, 0.0,
                        8.0, 0.0, 50.0, 0.0, 0.0 });
        demandMap.get("Petrol station").put("Connection Oirschot E",
                new double[] { 0.0, 35.0, 66.0, 0.0, 0.0, 16.0, 0.0, 28.0, 0.0, 0.0, 7.0, 29.0, 9.0, 3.0, 0.0, 0.0, 0.0, 18.0,
                        0.0, 0.0, 0.0, 20.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 6.0, 4.0, 19.0, 9.0, 0.0, 19.0, 18.0, 9.0,
                        0.0, 0.0, 20.0, 0.0, 6.0, 9.0, 52.0, 9.0, 38.0, 0.0, 0.0, 22.0, 0.0, 0.0, 36.0, 0.0, 0.0, 0.0, 6.0, 0.0,
                        40.0, 0.0, 0.0 });
        demandMap.get("Petrol station").put("Connection Best E",
                new double[] { 0.0, 42.0, 79.0, 0.0, 0.0, 19.0, 0.0, 34.0, 0.0, 0.0, 9.0, 35.0, 11.0, 4.0, 0.0, 0.0, 0.0, 22.0,
                        0.0, 0.0, 0.0, 24.0, 0.0, 0.0, 2.0, 0.0, 0.0, 0.0, 0.0, 0.0, 8.0, 5.0, 23.0, 11.0, 0.0, 23.0, 22.0,
                        11.0, 0.0, 0.0, 25.0, 0.0, 7.0, 11.0, 62.0, 11.0, 45.0, 0.0, 0.0, 27.0, 0.0, 0.0, 43.0, 0.0, 0.0, 0.0,
                        7.0, 0.0, 48.0, 0.0, 0.0 });
        demandMap.get("Petrol station").put("Junction Batadorp E",
                new double[] { 0.0, 98.0, 185.0, 0.0, 0.0, 44.0, 0.0, 79.0, 0.0, 0.0, 20.0, 81.0, 26.0, 9.0, 0.0, 0.0, 0.0,
                        51.0, 0.0, 0.0, 0.0, 57.0, 0.0, 0.0, 4.0, 0.0, 0.0, 0.0, 0.0, 0.0, 18.0, 11.0, 54.0, 25.0, 0.0, 53.0,
                        52.0, 26.0, 0.0, 0.0, 57.0, 0.0, 16.0, 26.0, 145.0, 24.0, 105.0, 0.0, 0.0, 63.0, 0.0, 0.0, 100.0, 0.0,
                        0.0, 0.0, 17.0, 0.0, 111.0, 0.0, 0.0 });
        demandMap.put("Connection Moergestel E", new HashMap<>());
        demandMap.get("Connection Moergestel E").put("Connection Oirschot E",
                new double[] { 89.0, 102.0, 88.0, 103.0, 0.0, 110.0, 119.0, 66.0, 44.0, 65.0, 34.0, 65.0, 77.0, 98.0, 43.0,
                        26.0, 78.0, 48.0, 43.0, 86.0, 0.0, 0.0, 0.0, 0.0, 78.0, 85.0, 106.0, 60.0, 91.0, 47.0, 62.0, 78.0, 66.0,
                        59.0, 97.0, 46.0, 76.0, 5.0, 76.0, 67.0, 70.0, 94.0, 66.0, 96.0, 59.0, 86.0, 80.0, 73.0, 41.0, 20.0,
                        125.0, 14.0, 74.0, 80.0, 53.0, 0.0, 77.0, 61.0, 28.0, 61.0, 0.0 });
        demandMap.get("Connection Moergestel E").put("Restplace Kloosters",
                new double[] { 0.0, 1.0, 0.0, 1.0, 0.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                        0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                        0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                        0.0 });
        demandMap.get("Connection Moergestel E").put("Connection Best E",
                new double[] { 106.0, 122.0, 105.0, 124.0, 0.0, 132.0, 142.0, 79.0, 53.0, 78.0, 40.0, 78.0, 92.0, 118.0, 52.0,
                        32.0, 93.0, 58.0, 52.0, 104.0, 0.0, 0.0, 0.0, 0.0, 93.0, 102.0, 127.0, 72.0, 109.0, 56.0, 75.0, 93.0,
                        79.0, 70.0, 116.0, 55.0, 91.0, 6.0, 91.0, 81.0, 83.0, 112.0, 79.0, 115.0, 70.0, 104.0, 96.0, 88.0, 49.0,
                        24.0, 150.0, 17.0, 89.0, 96.0, 63.0, 0.0, 92.0, 73.0, 33.0, 73.0, 0.0 });
        demandMap.get("Connection Moergestel E").put("Junction Batadorp E",
                new double[] { 249.0, 286.0, 245.0, 288.0, 0.0, 308.0, 332.0, 184.0, 124.0, 181.0, 94.0, 181.0, 214.0, 275.0,
                        121.0, 74.0, 218.0, 134.0, 121.0, 242.0, 0.0, 0.0, 0.0, 0.0, 218.0, 238.0, 295.0, 168.0, 255.0, 131.0,
                        175.0, 218.0, 184.0, 164.0, 271.0, 127.0, 212.0, 13.0, 212.0, 188.0, 195.0, 262.0, 184.0, 269.0, 164.0,
                        242.0, 225.0, 205.0, 114.0, 57.0, 349.0, 40.0, 208.0, 225.0, 148.0, 0.0, 214.0, 171.0, 77.0, 171.0,
                        0.0 });
        demandMap.put("Connection Oirschot E", new HashMap<>());
        demandMap.get("Connection Oirschot E").put("Restplace Kloosters",
                new double[] { 1.0, 0.0, 0.0, 2.0, 1.0, 0.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0,
                        0.0, 1.0, 1.0, 1.0, 0.0, 1.0, 0.0, 1.0, 0.0, 1.0, 1.0, 1.0, 0.0, 1.0, 0.0, 1.0, 1.0, 0.0, 1.0, 1.0, 1.0,
                        0.0, 1.0, 0.0, 1.0, 0.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0, 1.0, 0.0, 1.0, 1.0, 0.0, 1.0, 1.0, 0.0, 1.0,
                        0.0 });
        demandMap.get("Connection Oirschot E").put("Connection Best E",
                new double[] { 306.0, 61.0, 115.0, 378.0, 227.0, 25.0, 180.0, 203.0, 205.0, 210.0, 156.0, 174.0, 149.0, 131.0,
                        120.0, 137.0, 234.0, 307.0, 101.0, 58.0, 210.0, 203.0, 126.0, 68.0, 248.0, 90.0, 174.0, 111.0, 201.0,
                        140.0, 167.0, 18.0, 232.0, 0.0, 284.0, 155.0, 13.0, 234.0, 155.0, 187.0, 56.0, 221.0, 106.0, 212.0,
                        11.0, 162.0, 133.0, 192.0, 126.0, 178.0, 110.0, 194.0, 0.0, 165.0, 212.0, 102.0, 162.0, 171.0, 81.0,
                        183.0, 0.0 });
        demandMap.get("Connection Oirschot E").put("Junction Batadorp E",
                new double[] { 713.0, 142.0, 269.0, 881.0, 529.0, 59.0, 419.0, 474.0, 478.0, 491.0, 365.0, 407.0, 348.0, 306.0,
                        281.0, 319.0, 546.0, 717.0, 235.0, 134.0, 491.0, 474.0, 293.0, 159.0, 579.0, 210.0, 407.0, 260.0, 470.0,
                        327.0, 391.0, 42.0, 541.0, 0.0, 662.0, 361.0, 29.0, 546.0, 361.0, 437.0, 131.0, 516.0, 247.0, 495.0,
                        26.0, 378.0, 310.0, 448.0, 293.0, 415.0, 256.0, 453.0, 0.0, 386.0, 495.0, 239.0, 378.0, 398.0, 188.0,
                        428.0, 0.0 });
        demandMap.put("Restplace Kloosters", new HashMap<>());
        demandMap.get("Restplace Kloosters").put("Connection Best E",
                new double[] { 64.0, 64.0, 145.0, 10.0, 0.0, 0.0, 173.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 149.0, 0.0,
                        0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 122.0, 109.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                        0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 12.0, 90.0, 19.0, 0.0, 0.0, 0.0, 209.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                        0.0, 77.0, 27.0, 0.0 });
        demandMap.get("Restplace Kloosters").put("Junction Batadorp E",
                new double[] { 148.0, 148.0, 337.0, 22.0, 0.0, 0.0, 402.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 349.0, 0.0,
                        0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 284.0, 253.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                        0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 29.0, 210.0, 44.0, 0.0, 0.0, 0.0, 487.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                        0.0, 0.0, 180.0, 64.0, 0.0 });
        demandMap.put("Connection Best E", new HashMap<>());
        demandMap.get("Connection Best E").put("Junction Batadorp E",
                new double[] { 0.0, 0.0, 0.0, 0.0, 233.0, 0.0, 0.0, 320.0, 22.0, 0.0, 0.0, 98.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                        26.0, 0.0, 0.0, 0.0, 168.0, 0.0, 0.0, 182.0, 0.0, 0.0, 0.0, 92.0, 107.0, 0.0, 0.0, 0.0, 54.0, 65.0,
                        72.0, 0.0, 35.0, 0.0, 188.0, 0.0, 0.0, 63.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 144.0, 0.0, 0.0, 0.0,
                        0.0, 0.0, 69.0, 0.0, 0.0, 0.0 });
        demandMap.put("Junction Batadorp South W", new HashMap<>());
        demandMap.get("Junction Batadorp South W").put("Connection Best W",
                new double[] { 231.0, 210.0, 207.0, 204.0, 162.0, 210.0, 180.0, 225.0, 228.0, 186.0, 213.0, 177.0, 168.0, 159.0,
                        183.0, 174.0, 213.0, 198.0, 156.0, 189.0, 189.0, 177.0, 162.0, 150.0, 195.0, 174.0, 207.0, 213.0, 183.0,
                        150.0, 180.0, 153.0, 165.0, 156.0, 150.0, 114.0, 150.0, 195.0, 153.0, 168.0, 192.0, 174.0, 162.0, 216.0,
                        156.0, 192.0, 165.0, 159.0, 195.0, 174.0, 132.0, 156.0, 174.0, 192.0, 195.0, 150.0, 183.0, 129.0, 117.0,
                        183.0, 0.0 });
        demandMap.get("Junction Batadorp South W").put("Restplace Brehees",
                new double[] { 18.0, 17.0, 17.0, 16.0, 13.0, 17.0, 14.0, 18.0, 18.0, 15.0, 17.0, 14.0, 13.0, 13.0, 15.0, 14.0,
                        17.0, 16.0, 12.0, 15.0, 15.0, 14.0, 13.0, 12.0, 16.0, 14.0, 17.0, 17.0, 15.0, 12.0, 14.0, 12.0, 13.0,
                        12.0, 12.0, 9.0, 12.0, 16.0, 12.0, 13.0, 15.0, 14.0, 13.0, 17.0, 12.0, 15.0, 13.0, 13.0, 16.0, 14.0,
                        11.0, 12.0, 14.0, 15.0, 16.0, 12.0, 15.0, 10.0, 9.0, 15.0, 0.0 });
        demandMap.get("Junction Batadorp South W").put("Connection Oirschot W",
                new double[] { 99.0, 90.0, 88.0, 87.0, 69.0, 90.0, 77.0, 96.0, 97.0, 79.0, 91.0, 76.0, 72.0, 68.0, 78.0, 74.0,
                        91.0, 84.0, 67.0, 81.0, 81.0, 76.0, 69.0, 64.0, 83.0, 74.0, 88.0, 91.0, 78.0, 64.0, 77.0, 65.0, 70.0,
                        67.0, 64.0, 49.0, 64.0, 83.0, 65.0, 72.0, 82.0, 74.0, 69.0, 92.0, 67.0, 82.0, 70.0, 68.0, 83.0, 74.0,
                        56.0, 67.0, 74.0, 82.0, 83.0, 64.0, 78.0, 55.0, 50.0, 78.0, 0.0 });
        demandMap.get("Junction Batadorp South W").put("Connection Moergestel W",
                new double[] { 13.0, 12.0, 12.0, 12.0, 9.0, 12.0, 10.0, 13.0, 13.0, 11.0, 12.0, 10.0, 10.0, 9.0, 10.0, 10.0,
                        12.0, 11.0, 9.0, 11.0, 11.0, 10.0, 9.0, 9.0, 11.0, 10.0, 12.0, 12.0, 10.0, 9.0, 10.0, 9.0, 9.0, 9.0,
                        9.0, 6.0, 9.0, 11.0, 9.0, 10.0, 11.0, 10.0, 9.0, 12.0, 9.0, 11.0, 9.0, 9.0, 11.0, 10.0, 8.0, 9.0, 10.0,
                        11.0, 11.0, 9.0, 10.0, 7.0, 7.0, 10.0, 0.0 });
        demandMap.get("Junction Batadorp South W").put("Restplace Kriekampen",
                new double[] { 16.0, 14.0, 14.0, 14.0, 11.0, 14.0, 12.0, 15.0, 16.0, 13.0, 15.0, 12.0, 12.0, 11.0, 13.0, 12.0,
                        15.0, 14.0, 11.0, 13.0, 13.0, 12.0, 11.0, 10.0, 13.0, 12.0, 14.0, 15.0, 13.0, 10.0, 12.0, 11.0, 11.0,
                        11.0, 10.0, 8.0, 10.0, 13.0, 11.0, 12.0, 13.0, 12.0, 11.0, 15.0, 11.0, 13.0, 11.0, 11.0, 13.0, 12.0,
                        9.0, 11.0, 12.0, 13.0, 13.0, 10.0, 13.0, 9.0, 8.0, 13.0, 0.0 });
        demandMap.get("Junction Batadorp South W").put("Junction De Baars North W",
                new double[] { 594.0, 540.0, 533.0, 525.0, 417.0, 540.0, 463.0, 579.0, 587.0, 479.0, 548.0, 455.0, 432.0, 409.0,
                        471.0, 448.0, 548.0, 509.0, 401.0, 486.0, 486.0, 455.0, 417.0, 386.0, 502.0, 448.0, 533.0, 548.0, 471.0,
                        386.0, 463.0, 394.0, 425.0, 401.0, 386.0, 293.0, 386.0, 502.0, 394.0, 432.0, 494.0, 448.0, 417.0, 556.0,
                        401.0, 494.0, 425.0, 409.0, 502.0, 448.0, 340.0, 401.0, 448.0, 494.0, 502.0, 386.0, 471.0, 332.0, 301.0,
                        471.0, 0.0 });
        demandMap.get("Junction Batadorp South W").put("Junction De Baars South W",
                new double[] { 184.0, 167.0, 165.0, 162.0, 129.0, 167.0, 143.0, 179.0, 181.0, 148.0, 169.0, 141.0, 134.0, 126.0,
                        145.0, 138.0, 169.0, 157.0, 124.0, 150.0, 150.0, 141.0, 129.0, 119.0, 155.0, 138.0, 165.0, 169.0, 145.0,
                        119.0, 143.0, 122.0, 131.0, 124.0, 119.0, 91.0, 119.0, 155.0, 122.0, 134.0, 153.0, 138.0, 129.0, 172.0,
                        124.0, 153.0, 131.0, 126.0, 155.0, 138.0, 105.0, 124.0, 138.0, 153.0, 155.0, 119.0, 145.0, 103.0, 93.0,
                        145.0, 0.0 });
        demandMap.put("Junction Batadorp North W", new HashMap<>());
        demandMap.get("Junction Batadorp North W").put("Connection Best W",
                new double[] { 231.0, 210.0, 207.0, 204.0, 162.0, 210.0, 180.0, 225.0, 228.0, 186.0, 213.0, 177.0, 168.0, 159.0,
                        183.0, 174.0, 213.0, 198.0, 156.0, 189.0, 189.0, 177.0, 162.0, 150.0, 195.0, 174.0, 207.0, 213.0, 183.0,
                        150.0, 180.0, 153.0, 165.0, 156.0, 150.0, 114.0, 150.0, 195.0, 153.0, 168.0, 192.0, 174.0, 162.0, 216.0,
                        156.0, 192.0, 165.0, 159.0, 195.0, 174.0, 132.0, 156.0, 174.0, 192.0, 195.0, 150.0, 183.0, 129.0, 117.0,
                        183.0, 0.0 });
        demandMap.get("Junction Batadorp North W").put("Restplace Brehees",
                new double[] { 18.0, 17.0, 17.0, 16.0, 13.0, 17.0, 14.0, 18.0, 18.0, 15.0, 17.0, 14.0, 13.0, 13.0, 15.0, 14.0,
                        17.0, 16.0, 12.0, 15.0, 15.0, 14.0, 13.0, 12.0, 16.0, 14.0, 17.0, 17.0, 15.0, 12.0, 14.0, 12.0, 13.0,
                        12.0, 12.0, 9.0, 12.0, 16.0, 12.0, 13.0, 15.0, 14.0, 13.0, 17.0, 12.0, 15.0, 13.0, 13.0, 16.0, 14.0,
                        11.0, 12.0, 14.0, 15.0, 16.0, 12.0, 15.0, 10.0, 9.0, 15.0, 0.0 });
        demandMap.get("Junction Batadorp North W").put("Connection Oirschot W",
                new double[] { 99.0, 90.0, 88.0, 87.0, 69.0, 90.0, 77.0, 96.0, 97.0, 79.0, 91.0, 76.0, 72.0, 68.0, 78.0, 74.0,
                        91.0, 84.0, 67.0, 81.0, 81.0, 76.0, 69.0, 64.0, 83.0, 74.0, 88.0, 91.0, 78.0, 64.0, 77.0, 65.0, 70.0,
                        67.0, 64.0, 49.0, 64.0, 83.0, 65.0, 72.0, 82.0, 74.0, 69.0, 92.0, 67.0, 82.0, 70.0, 68.0, 83.0, 74.0,
                        56.0, 67.0, 74.0, 82.0, 83.0, 64.0, 78.0, 55.0, 50.0, 78.0, 0.0 });
        demandMap.get("Junction Batadorp North W").put("Connection Moergestel W",
                new double[] { 13.0, 12.0, 12.0, 12.0, 9.0, 12.0, 10.0, 13.0, 13.0, 11.0, 12.0, 10.0, 10.0, 9.0, 10.0, 10.0,
                        12.0, 11.0, 9.0, 11.0, 11.0, 10.0, 9.0, 9.0, 11.0, 10.0, 12.0, 12.0, 10.0, 9.0, 10.0, 9.0, 9.0, 9.0,
                        9.0, 6.0, 9.0, 11.0, 9.0, 10.0, 11.0, 10.0, 9.0, 12.0, 9.0, 11.0, 9.0, 9.0, 11.0, 10.0, 8.0, 9.0, 10.0,
                        11.0, 11.0, 9.0, 10.0, 7.0, 7.0, 10.0, 0.0 });
        demandMap.get("Junction Batadorp North W").put("Restplace Kriekampen",
                new double[] { 16.0, 14.0, 14.0, 14.0, 11.0, 14.0, 12.0, 15.0, 16.0, 13.0, 15.0, 12.0, 12.0, 11.0, 13.0, 12.0,
                        15.0, 14.0, 11.0, 13.0, 13.0, 12.0, 11.0, 10.0, 13.0, 12.0, 14.0, 15.0, 13.0, 10.0, 12.0, 11.0, 11.0,
                        11.0, 10.0, 8.0, 10.0, 13.0, 11.0, 12.0, 13.0, 12.0, 11.0, 15.0, 11.0, 13.0, 11.0, 11.0, 13.0, 12.0,
                        9.0, 11.0, 12.0, 13.0, 13.0, 10.0, 13.0, 9.0, 8.0, 13.0, 0.0 });
        demandMap.get("Junction Batadorp North W").put("Junction De Baars North W",
                new double[] { 594.0, 540.0, 533.0, 525.0, 417.0, 540.0, 463.0, 579.0, 587.0, 479.0, 548.0, 455.0, 432.0, 409.0,
                        471.0, 448.0, 548.0, 509.0, 401.0, 486.0, 486.0, 455.0, 417.0, 386.0, 502.0, 448.0, 533.0, 548.0, 471.0,
                        386.0, 463.0, 394.0, 425.0, 401.0, 386.0, 293.0, 386.0, 502.0, 394.0, 432.0, 494.0, 448.0, 417.0, 556.0,
                        401.0, 494.0, 425.0, 409.0, 502.0, 448.0, 340.0, 401.0, 448.0, 494.0, 502.0, 386.0, 471.0, 332.0, 301.0,
                        471.0, 0.0 });
        demandMap.get("Junction Batadorp North W").put("Junction De Baars South W",
                new double[] { 184.0, 167.0, 165.0, 162.0, 129.0, 167.0, 143.0, 179.0, 181.0, 148.0, 169.0, 141.0, 134.0, 126.0,
                        145.0, 138.0, 169.0, 157.0, 124.0, 150.0, 150.0, 141.0, 129.0, 119.0, 155.0, 138.0, 165.0, 169.0, 145.0,
                        119.0, 143.0, 122.0, 131.0, 124.0, 119.0, 91.0, 119.0, 155.0, 122.0, 134.0, 153.0, 138.0, 129.0, 172.0,
                        124.0, 153.0, 131.0, 126.0, 155.0, 138.0, 105.0, 124.0, 138.0, 153.0, 155.0, 119.0, 145.0, 103.0, 93.0,
                        145.0, 0.0 });
        demandMap.put("Connection Best W", new HashMap<>());
        demandMap.get("Connection Best W").put("Restplace Brehees",
                new double[] { 17.0, 18.0, 21.0, 19.0, 10.0, 17.0, 11.0, 23.0, 17.0, 12.0, 19.0, 16.0, 15.0, 16.0, 11.0, 16.0,
                        17.0, 16.0, 14.0, 15.0, 15.0, 16.0, 18.0, 13.0, 16.0, 18.0, 18.0, 14.0, 18.0, 15.0, 17.0, 13.0, 18.0,
                        15.0, 19.0, 11.0, 14.0, 16.0, 15.0, 16.0, 17.0, 19.0, 14.0, 22.0, 17.0, 14.0, 21.0, 15.0, 15.0, 19.0,
                        20.0, 10.0, 20.0, 17.0, 16.0, 20.0, 21.0, 14.0, 17.0, 13.0, 0.0 });
        demandMap.get("Connection Best W").put("Connection Oirschot W",
                new double[] { 93.0, 96.0, 112.0, 102.0, 53.0, 93.0, 61.0, 125.0, 90.0, 65.0, 102.0, 87.0, 82.0, 87.0, 60.0,
                        84.0, 90.0, 84.0, 73.0, 82.0, 79.0, 87.0, 98.0, 70.0, 86.0, 97.0, 94.0, 73.0, 99.0, 82.0, 91.0, 69.0,
                        94.0, 81.0, 104.0, 59.0, 72.0, 87.0, 81.0, 83.0, 89.0, 104.0, 75.0, 115.0, 92.0, 76.0, 110.0, 79.0,
                        80.0, 99.0, 109.0, 52.0, 105.0, 91.0, 84.0, 105.0, 113.0, 75.0, 90.0, 70.0, 0.0 });
        demandMap.get("Connection Best W").put("Connection Moergestel W",
                new double[] { 12.0, 13.0, 15.0, 14.0, 7.0, 12.0, 8.0, 17.0, 12.0, 9.0, 14.0, 12.0, 11.0, 12.0, 8.0, 11.0, 12.0,
                        11.0, 10.0, 11.0, 11.0, 12.0, 13.0, 9.0, 12.0, 13.0, 13.0, 10.0, 13.0, 11.0, 12.0, 9.0, 13.0, 11.0,
                        14.0, 8.0, 10.0, 12.0, 11.0, 11.0, 12.0, 14.0, 10.0, 15.0, 12.0, 10.0, 15.0, 11.0, 11.0, 13.0, 15.0,
                        7.0, 14.0, 12.0, 11.0, 14.0, 15.0, 10.0, 12.0, 9.0, 0.0 });
        demandMap.get("Connection Best W").put("Restplace Kriekampen",
                new double[] { 15.0, 15.0, 18.0, 16.0, 9.0, 15.0, 10.0, 20.0, 14.0, 10.0, 16.0, 14.0, 13.0, 14.0, 10.0, 13.0,
                        15.0, 14.0, 12.0, 13.0, 13.0, 14.0, 16.0, 11.0, 14.0, 16.0, 15.0, 12.0, 16.0, 13.0, 15.0, 11.0, 15.0,
                        13.0, 17.0, 9.0, 12.0, 14.0, 13.0, 13.0, 14.0, 17.0, 12.0, 19.0, 15.0, 12.0, 18.0, 13.0, 13.0, 16.0,
                        18.0, 8.0, 17.0, 15.0, 13.0, 17.0, 18.0, 12.0, 15.0, 11.0, 0.0 });
        demandMap.get("Connection Best W").put("Junction De Baars North W",
                new double[] { 558.0, 579.0, 677.0, 612.0, 319.0, 561.0, 365.0, 754.0, 540.0, 391.0, 612.0, 522.0, 497.0, 527.0,
                        363.0, 504.0, 543.0, 509.0, 437.0, 491.0, 476.0, 527.0, 592.0, 419.0, 520.0, 584.0, 569.0, 440.0, 594.0,
                        491.0, 548.0, 417.0, 566.0, 486.0, 625.0, 355.0, 435.0, 527.0, 486.0, 499.0, 535.0, 625.0, 453.0, 695.0,
                        556.0, 455.0, 664.0, 476.0, 484.0, 599.0, 659.0, 314.0, 630.0, 548.0, 504.0, 633.0, 682.0, 450.0, 545.0,
                        419.0, 0.0 });
        demandMap.get("Connection Best W").put("Junction De Baars South W",
                new double[] { 172.0, 179.0, 209.0, 189.0, 99.0, 173.0, 113.0, 233.0, 167.0, 121.0, 189.0, 161.0, 153.0, 163.0,
                        112.0, 156.0, 168.0, 157.0, 135.0, 152.0, 147.0, 163.0, 183.0, 130.0, 161.0, 180.0, 176.0, 136.0, 184.0,
                        152.0, 169.0, 129.0, 175.0, 150.0, 193.0, 110.0, 134.0, 163.0, 150.0, 154.0, 165.0, 193.0, 140.0, 215.0,
                        172.0, 141.0, 205.0, 147.0, 149.0, 185.0, 203.0, 97.0, 195.0, 169.0, 156.0, 196.0, 211.0, 139.0, 168.0,
                        130.0, 0.0 });
        demandMap.put("Restplace Brehees", new HashMap<>());
        demandMap.get("Restplace Brehees").put("Connection Oirschot W",
                new double[] { 7.0, 11.0, 0.0, 7.0, 13.0, 0.0, 10.0, 0.0, 28.0, 7.0, 0.0, 8.0, 22.0, 0.0, 4.0, 0.0, 14.0, 0.0,
                        0.0, 2.0, 10.0, 4.0, 17.0, 9.0, 9.0, 17.0, 0.0, 17.0, 4.0, 0.0, 10.0, 8.0, 0.0, 15.0, 8.0, 8.0, 0.0,
                        0.0, 8.0, 0.0, 8.0, 22.0, 4.0, 0.0, 10.0, 5.0, 13.0, 11.0, 4.0, 17.0, 7.0, 24.0, 0.0, 12.0, 17.0, 0.0,
                        0.0, 5.0, 0.0, 6.0, 0.0 });
        demandMap.get("Restplace Brehees").put("Connection Moergestel W",
                new double[] { 1.0, 1.0, 0.0, 1.0, 2.0, 0.0, 1.0, 0.0, 4.0, 1.0, 0.0, 1.0, 3.0, 0.0, 0.0, 0.0, 2.0, 0.0, 0.0,
                        0.0, 1.0, 1.0, 2.0, 1.0, 1.0, 2.0, 0.0, 2.0, 1.0, 0.0, 1.0, 1.0, 0.0, 2.0, 1.0, 1.0, 0.0, 0.0, 1.0, 0.0,
                        1.0, 3.0, 1.0, 0.0, 1.0, 1.0, 2.0, 1.0, 1.0, 2.0, 1.0, 3.0, 0.0, 2.0, 2.0, 0.0, 0.0, 1.0, 0.0, 1.0,
                        0.0 });
        demandMap.get("Restplace Brehees").put("Restplace Kriekampen",
                new double[] { 1.0, 2.0, 0.0, 1.0, 2.0, 0.0, 2.0, 0.0, 5.0, 1.0, 0.0, 1.0, 3.0, 0.0, 1.0, 0.0, 2.0, 0.0, 0.0,
                        0.0, 2.0, 1.0, 3.0, 1.0, 1.0, 3.0, 0.0, 3.0, 1.0, 0.0, 2.0, 1.0, 0.0, 2.0, 1.0, 1.0, 0.0, 0.0, 1.0, 0.0,
                        1.0, 3.0, 1.0, 0.0, 2.0, 1.0, 2.0, 2.0, 1.0, 3.0, 1.0, 4.0, 0.0, 2.0, 3.0, 0.0, 0.0, 1.0, 0.0, 1.0,
                        0.0 });
        demandMap.get("Restplace Brehees").put("Junction De Baars North W",
                new double[] { 39.0, 64.0, 0.0, 42.0, 80.0, 0.0, 60.0, 0.0, 170.0, 42.0, 0.0, 48.0, 131.0, 0.0, 22.0, 0.0, 85.0,
                        0.0, 0.0, 10.0, 62.0, 24.0, 102.0, 52.0, 53.0, 103.0, 0.0, 102.0, 26.0, 0.0, 62.0, 50.0, 0.0, 88.0,
                        51.0, 49.0, 0.0, 0.0, 45.0, 0.0, 47.0, 130.0, 27.0, 0.0, 62.0, 31.0, 76.0, 64.0, 26.0, 101.0, 39.0,
                        144.0, 0.0, 75.0, 105.0, 0.0, 0.0, 29.0, 0.0, 37.0, 0.0 });
        demandMap.get("Restplace Brehees").put("Junction De Baars South W",
                new double[] { 12.0, 20.0, 0.0, 13.0, 25.0, 0.0, 19.0, 0.0, 53.0, 13.0, 0.0, 15.0, 40.0, 0.0, 7.0, 0.0, 26.0,
                        0.0, 0.0, 3.0, 19.0, 7.0, 32.0, 16.0, 16.0, 32.0, 0.0, 32.0, 8.0, 0.0, 19.0, 15.0, 0.0, 27.0, 16.0,
                        15.0, 0.0, 0.0, 14.0, 0.0, 15.0, 40.0, 8.0, 0.0, 19.0, 10.0, 24.0, 20.0, 8.0, 31.0, 12.0, 45.0, 0.0,
                        23.0, 32.0, 0.0, 0.0, 9.0, 0.0, 12.0, 0.0 });
        demandMap.put("Connection Oirschot W", new HashMap<>());
        demandMap.get("Connection Oirschot W").put("Connection Moergestel W",
                new double[] { 4.0, 20.0, 8.0, 7.0, 13.0, 11.0, 13.0, 10.0, 3.0, 14.0, 11.0, 15.0, 8.0, 7.0, 11.0, 11.0, 9.0,
                        7.0, 9.0, 6.0, 10.0, 7.0, 13.0, 5.0, 9.0, 12.0, 7.0, 12.0, 10.0, 12.0, 10.0, 9.0, 9.0, 8.0, 8.0, 9.0,
                        12.0, 10.0, 11.0, 4.0, 14.0, 6.0, 6.0, 10.0, 15.0, 9.0, 7.0, 11.0, 10.0, 10.0, 11.0, 7.0, 11.0, 12.0,
                        15.0, 4.0, 11.0, 12.0, 8.0, 15.0, 0.0 });
        demandMap.get("Connection Oirschot W").put("Restplace Kriekampen",
                new double[] { 5.0, 24.0, 10.0, 9.0, 16.0, 13.0, 15.0, 12.0, 4.0, 17.0, 13.0, 18.0, 10.0, 8.0, 13.0, 13.0, 11.0,
                        8.0, 11.0, 7.0, 13.0, 9.0, 16.0, 5.0, 10.0, 15.0, 8.0, 14.0, 12.0, 15.0, 13.0, 11.0, 10.0, 10.0, 10.0,
                        11.0, 14.0, 12.0, 13.0, 5.0, 16.0, 8.0, 7.0, 13.0, 18.0, 11.0, 8.0, 13.0, 13.0, 11.0, 14.0, 8.0, 13.0,
                        14.0, 18.0, 4.0, 13.0, 14.0, 10.0, 18.0, 0.0 });
        demandMap.get("Connection Oirschot W").put("Junction De Baars North W",
                new double[] { 193.0, 889.0, 376.0, 330.0, 580.0, 485.0, 566.0, 432.0, 144.0, 630.0, 485.0, 689.0, 376.0, 302.0,
                        474.0, 499.0, 393.0, 314.0, 424.0, 263.0, 474.0, 337.0, 601.0, 203.0, 386.0, 562.0, 315.0, 536.0, 431.0,
                        545.0, 468.0, 426.0, 384.0, 359.0, 381.0, 426.0, 535.0, 451.0, 477.0, 173.0, 610.0, 292.0, 250.0, 471.0,
                        677.0, 416.0, 299.0, 505.0, 470.0, 430.0, 517.0, 311.0, 476.0, 541.0, 672.0, 165.0, 502.0, 536.0, 378.0,
                        668.0, 0.0 });
        demandMap.get("Connection Oirschot W").put("Junction De Baars South W",
                new double[] { 60.0, 275.0, 116.0, 102.0, 179.0, 150.0, 175.0, 134.0, 44.0, 195.0, 150.0, 213.0, 116.0, 93.0,
                        147.0, 154.0, 121.0, 97.0, 131.0, 81.0, 146.0, 104.0, 186.0, 63.0, 119.0, 174.0, 97.0, 166.0, 133.0,
                        168.0, 145.0, 131.0, 119.0, 111.0, 118.0, 131.0, 165.0, 139.0, 147.0, 53.0, 188.0, 90.0, 77.0, 145.0,
                        209.0, 129.0, 92.0, 156.0, 145.0, 133.0, 160.0, 96.0, 147.0, 167.0, 207.0, 51.0, 155.0, 166.0, 117.0,
                        206.0, 0.0 });
        demandMap.put("Connection Moergestel W", new HashMap<>());
        demandMap.get("Connection Moergestel W").put("Restplace Kriekampen",
                new double[] { 7.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1.0, 3.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 4.0, 0.0,
                        0.0, 0.0, 0.0, 3.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 4.0, 2.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 3.0, 0.0, 0.0,
                        1.0, 0.0, 4.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0,
                        0.0 });
        demandMap.get("Connection Moergestel W").put("Junction De Baars North W",
                new double[] { 264.0, 0.0, 0.0, 0.0, 19.0, 0.0, 38.0, 106.0, 0.0, 38.0, 0.0, 0.0, 0.0, 25.0, 0.0, 0.0, 0.0,
                        160.0, 0.0, 0.0, 0.0, 0.0, 97.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 150.0, 79.0, 0.0, 32.0, 0.0, 0.0, 0.0,
                        0.0, 109.0, 0.0, 0.0, 40.0, 0.0, 153.0, 0.0, 30.0, 0.0, 0.0, 19.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                        26.0, 0.0, 13.0, 37.0, 0.0, 0.0 });
        demandMap.get("Connection Moergestel W").put("Junction De Baars South W",
                new double[] { 81.0, 0.0, 0.0, 0.0, 6.0, 0.0, 12.0, 33.0, 0.0, 12.0, 0.0, 0.0, 0.0, 8.0, 0.0, 0.0, 0.0, 49.0,
                        0.0, 0.0, 0.0, 0.0, 30.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 46.0, 24.0, 0.0, 10.0, 0.0, 0.0, 0.0, 0.0, 34.0,
                        0.0, 0.0, 12.0, 0.0, 47.0, 0.0, 9.0, 0.0, 0.0, 6.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 8.0, 0.0, 4.0,
                        11.0, 0.0, 0.0 });
        demandMap.put("Restplace Kriekampen", new HashMap<>());
        demandMap.get("Restplace Kriekampen").put("Junction De Baars North W",
                new double[] { 31.0, 231.0, 332.0, 0.0, 0.0, 0.0, 0.0, 0.0, 4.0, 0.0, 0.0, 47.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                        50.0, 0.0, 0.0, 0.0, 0.0, 74.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 155.0, 0.0, 0.0, 0.0, 0.0, 192.0,
                        0.0, 0.0, 0.0, 0.0, 99.0, 0.0, 0.0, 118.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                        0.0, 45.0, 0.0, 0.0 });
        demandMap.get("Restplace Kriekampen").put("Junction De Baars South W",
                new double[] { 9.0, 71.0, 102.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 15.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                        16.0, 0.0, 0.0, 0.0, 0.0, 23.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 48.0, 0.0, 0.0, 0.0, 0.0, 59.0, 0.0,
                        0.0, 0.0, 0.0, 31.0, 0.0, 0.0, 36.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                        14.0, 0.0, 0.0 });

        nodeMap.put("Junction De Baars South E source", "N1EB");
        nodeMap.put("Junction De Baars North E source", "N4EB");
        nodeMap.put("Restplace Kerkeind dest", "N6EB");
        nodeMap.put("Restplace Kerkeind source", "N9EB");
        nodeMap.put("Petrol station dest", "N10EB");
        nodeMap.put("Petrol station source", "N13EB");
        nodeMap.put("Connection Moergestel E dest", "N16EB");
        nodeMap.put("Connection Moergestel E source", "N19EB");
        nodeMap.put("Connection Oirschot E dest", "N22EB");
        nodeMap.put("Connection Oirschot E source", "N25EB");
        nodeMap.put("Restplace Kloosters dest", "N28EB");
        nodeMap.put("Restplace Kloosters source", "N31EB");
        nodeMap.put("Connection Best E dest", "N35EB");
        nodeMap.put("Connection Best E source", "N38EB");
        nodeMap.put("Junction Batadorp E dest", "N37EB");
        nodeMap.put("Junction Batadorp South W source", "N1WB");
        nodeMap.put("Junction Batadorp North W source", "N4WB");
        nodeMap.put("Connection Best W dest", "N5WB");
        nodeMap.put("Connection Best W source", "N8WB");
        nodeMap.put("Restplace Brehees dest", "N11WB");
        nodeMap.put("Restplace Brehees source", "N14WB");
        nodeMap.put("Connection Oirschot W dest", "N17WB");
        nodeMap.put("Connection Oirschot W source", "N20WB");
        nodeMap.put("Connection Moergestel W dest", "N24WB");
        nodeMap.put("Connection Moergestel W source", "N27WB");
        nodeMap.put("Restplace Kriekampen dest", "N30WB");
        nodeMap.put("Restplace Kriekampen source", "N33WB");
        nodeMap.put("Junction De Baars North W dest", "N36WB");
        nodeMap.put("Junction De Baars South W dest", "N37WB");
    }

}
