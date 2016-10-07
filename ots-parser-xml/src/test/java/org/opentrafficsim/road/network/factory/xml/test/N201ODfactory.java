package org.opentrafficsim.road.network.factory.xml.test;

import java.util.ArrayList;
import java.util.List;

import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.StorageType;
import org.djunits.value.ValueException;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djunits.value.vdouble.vector.DurationVector;
import org.opentrafficsim.core.distributions.Distribution;
import org.opentrafficsim.core.distributions.ProbabilityException;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.route.ProbabilisticRouteGenerator;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.units.distributions.ContinuousDistDoubleScalar;
import org.opentrafficsim.road.gtu.animation.DefaultSwitchableGTUColorer;
import org.opentrafficsim.road.gtu.generator.GTUGeneratorIndividual;
import org.opentrafficsim.road.gtu.lane.LaneBasedIndividualGTU;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedGTUFollowingTacticalPlannerFactory;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlusOld;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;
import org.opentrafficsim.road.gtu.strategical.od.Categorization;
import org.opentrafficsim.road.gtu.strategical.od.Category;
import org.opentrafficsim.road.gtu.strategical.od.Interpolation;
import org.opentrafficsim.road.gtu.strategical.od.ODMatrixTrips;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlannerFactory;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.sampling.Query;
import org.opentrafficsim.road.network.sampling.Sampling;
import org.opentrafficsim.road.network.sampling.meta.MetaDataSet;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 6 okt. 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */

public class N201ODfactory
{

    /**
     * Creates origin-destination matrix
     * @param network network
     * @return origin-destination matrix
     */
    public static ODMatrixTrips get(final Network network)
    {
        List<Node> origins = new ArrayList<>();
        origins.add(network.getNode("N1a")); // A, maar dan een stuk verder, tussenliggende kruispunten genegeerd
        origins.add(network.getNode("N234b_in2")); // B
        origins.add(network.getNode("N239a_in2")); // C
        origins.add(network.getNode("N245a_in2")); // D
        origins.add(network.getNode("N245b_in2")); // E
        origins.add(network.getNode("N249a_in2")); // F // tegenoverliggende weg heeft geen data dus niet meegenomen
        // G & H: Hoofdweg langs kanaal zit er niet in, op- en afritten zitten niet in het netwerk
        origins.add(network.getNode("N291a_in2")); // I // tegenoverliggende weg heeft geen data dus niet meegenomen
        origins.add(network.getNode("N50b")); // J

        List<Node> destinations = new ArrayList<>();
        destinations.add(network.getNode("N1b")); // A
        destinations.add(network.getNode("N234b_uit2")); // B
        destinations.add(network.getNode("N239a_uit2")); // C
        destinations.add(network.getNode("N245a_uit2")); // D
        destinations.add(network.getNode("N249a_uit2")); // F
        destinations.add(network.getNode("N291a_uit2")); // I
        destinations.add(network.getNode("N50a")); // J

        ODMatrixTrips matrix;
        try
        {
            matrix = new ODMatrixTrips("N201demo", origins, destinations, Categorization.UNCATEGORIZED,
                    new DurationVector(new double[] { 0, 3600 }, TimeUnit.SI, StorageType.DENSE), Interpolation.STEPWISE);
        }
        catch (ValueException exception)
        {
            throw new RuntimeException(exception);
        }

        // loop matrix
        // 2*0 because the through movement on the IJweg is not incorporated
        int[][] od = new int[][] { { 0, 502, 309, 35, 285, 33, 218 }, { 331, 0, 229, 26, 212, 25, 162 },
                { 150, 89, 0, 12, 98, 11, 75 }, { 29, 17, 14, 0, 30, 4, 23 }, { 30, 18, 14, 2 * 0, 32, 4, 25 },
                { 296, 175, 143, 18, 0, 21, 136 }, { 67, 40, 32, 4, 63, 0, 787 }, { 373, 221, 180, 22, 350, 815, 0 } };
        for (int o = 0; o < origins.size(); o++)
        {
            for (int d = 0; d < destinations.size(); d++)
            {
                if (od[o][d] > 0)
                {
                    matrix.putTripsVector(origins.get(o), destinations.get(d), Category.UNCATEGORIZED, new int[] { od[o][d] });
                }
            }
        }

        return matrix;
    }

    /**
     * Makes generators at origin nodes of the OD.
     * @param network network
     * @param matrix origin-destination matrix
     * @param simulator simulator
     */
    public static void makeGeneratorsFromOD(final OTSNetwork network, final ODMatrixTrips matrix,
            final OTSDEVSSimulatorInterface simulator)
    {

        // fixed generator input
        Class<?> gtuClass = LaneBasedIndividualGTU.class;
        Time startTime = Time.ZERO;
        Time endTime = new Time(Double.MAX_VALUE, TimeUnit.SI);
        Length position = new Length(1.0, LengthUnit.SI);
        GTUType gtuType = new GTUType("CAR");
        ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> initSpeedDist =
                new ContinuousDistDoubleScalar.Rel<>(30, SpeedUnit.KM_PER_HOUR);
        ContinuousDistDoubleScalar.Rel<Length, LengthUnit> lengthDist =
                new ContinuousDistDoubleScalar.Rel<>(4, LengthUnit.METER);
        ContinuousDistDoubleScalar.Rel<Length, LengthUnit> widthDist =
                new ContinuousDistDoubleScalar.Rel<>(2, LengthUnit.METER);
        ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> maxSpeedDist =
                new ContinuousDistDoubleScalar.Rel<>(200, SpeedUnit.KM_PER_HOUR);
        DefaultSwitchableGTUColorer colorer = new DefaultSwitchableGTUColorer();
        MersenneTwister rand = new MersenneTwister();
        // loop origins
        for (Node origin : matrix.getOrigins())
        {
            // route generator
            double dem = matrix.originTotal(origin);
            List<Distribution.FrequencyAndObject<Route>> routeList = new ArrayList<>();
            for (Node destination : matrix.getDestinations())
            {
                if (matrix.contains(origin, destination, Category.UNCATEGORIZED))
                {
                    Route route = new Route(origin + "->" + destination);
                    try
                    {
                        route = network.getShortestRouteBetween(GTUType.ALL, origin, destination);
                    }
                    catch (NetworkException exception)
                    {
                        throw new RuntimeException("Problem finding route from " + origin + " to " + destination, exception);
                    }
                    double prob = matrix.originDestinationTotal(origin, destination) / dem;
                    routeList.add(new Distribution.FrequencyAndObject<>(prob, route));
                }
            }
            ProbabilisticRouteGenerator routeGenerator;
            try
            {
                routeGenerator = new ProbabilisticRouteGenerator(routeList, rand);
            }
            catch (ProbabilityException exception)
            {
                throw new RuntimeException(exception);
            }
            // strategical planner factory using route generator (i.e. a strategical planner factory required per origin)
            LaneBasedStrategicalPlannerFactory<LaneBasedStrategicalPlanner> strategicalPlannerFactory =
                    new LaneBasedStrategicalRoutePlannerFactory(
                            new LaneBasedGTUFollowingTacticalPlannerFactory(new IDMPlusOld()), routeGenerator);
            // time
            CrossSectionLink link = (CrossSectionLink) origin.getLinks().iterator().next(); // should be only 1 for origins
            int lanes = link.getLanes().size();
            double iat = 3600 / (dem / lanes);
            ContinuousDistDoubleScalar.Rel<Duration, TimeUnit> iatDist =
                    new ContinuousDistDoubleScalar.Rel<>(iat, TimeUnit.SECOND);
            GTUDirectionality dir =
                    link.getStartNode().equals(origin) ? GTUDirectionality.DIR_PLUS : GTUDirectionality.DIR_MINUS;
            // put generator on each lane
            for (Lane lane : link.getLanes())
            {
                try
                {
                    new GTUGeneratorIndividual(origin + "." + link.getLanes().indexOf(lane), simulator, gtuType, gtuClass,
                            initSpeedDist, iatDist, lengthDist, widthDist, maxSpeedDist, Integer.MAX_VALUE, startTime, endTime,
                            lane, position, dir, colorer, strategicalPlannerFactory, network);
                }
                catch (SimRuntimeException exception)
                {
                    throw new RuntimeException(exception);
                }
            }
        }
    }

    /**
     * @param network network
     * @param sampling sampling
     * @param simulator simulator
     * @return query covering the entire N201
     */
    public static Query getQuery(final OTSNetwork network, final Sampling sampling, final OTSDEVSSimulatorInterface simulator)
    {
        String[] southBound = new String[] { "L1a", "L2a", "L3a4a", "L5a", "L6a", "L7a", "L8a9a", "L10a11a", "L12a", "L13a14a",
                "L15a16a", "L17a", "L18a19a", "L20a21a", "L22a", "L23a24a", "L25a", "L26a", "L27a", "L28a29a", "L30a", "L31a",
                "L32a", "L33a", "L34a", "L35a", "L36a", "L37a", "L38a", "L39a", "L40a", "L41a", "L42a", "L43a", "L44a", "L45a",
                "L46a", "L47a48a", "L49a" };
        String[] northBound = new String[] { "L49b", "L48b47b", "L46b", "L45b", "L44b", "L43b", "L42b", "L41b", "L40b", "L39b",
                "L38b", "L37b", "L36b", "L35b", "L34b", "L33b", "L32b", "L31b", "L30b", "L29b28b", "L27b", "L26b", "L25b",
                "L24b23b", "L22b21b", "L20b", "L19b18b", "L17b16b", "L15b", "L14b13b", "L12b", "L11b", "L10b", "L9b8b", "L7b",
                "L6b", "L5b", "L4b3b", "L2b", "L1b" };
        boolean connected = false;
        Query query = new Query(sampling, "N201 both directions", connected, new MetaDataSet(),
                new Frequency(2.0, FrequencyUnit.PER_MINUTE));
        addSpaceTimeRegions(query, network, northBound, simulator);
        addSpaceTimeRegions(query, network, southBound, simulator);
        return query;
    }

    /**
     * @param query query
     * @param network network
     * @param links link names
     * @param simulator simulator
     */
    private static void addSpaceTimeRegions(final Query query, final OTSNetwork network, final String[] links,
            final OTSDEVSSimulatorInterface simulator)
    {
        for (String link : links)
        {
            query.addSpaceTimeRegionLink(simulator, (CrossSectionLink) network.getLink(link), GTUDirectionality.DIR_PLUS,
                    Length.ZERO, network.getLink(link).getLength(), Duration.ZERO, new Duration(1.0, TimeUnit.HOUR));
        }
    }

}
