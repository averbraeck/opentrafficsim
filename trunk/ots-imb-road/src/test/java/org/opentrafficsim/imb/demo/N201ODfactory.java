package org.opentrafficsim.imb.demo;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.ValueRuntimeException;
import org.djunits.value.storage.StorageType;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djunits.value.vdouble.vector.base.DoubleVector;
import org.opentrafficsim.core.distributions.Distribution;
import org.opentrafficsim.core.distributions.ProbabilityException;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.route.ProbabilisticRouteGenerator;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.units.distributions.ContinuousDistDoubleScalar;
import org.opentrafficsim.kpi.interfaces.GtuTypeDataInterface;
import org.opentrafficsim.kpi.sampling.KpiGtuDirectionality;
import org.opentrafficsim.kpi.sampling.Query;
import org.opentrafficsim.kpi.sampling.Sampler;
import org.opentrafficsim.kpi.sampling.meta.MetaDataGtuType;
import org.opentrafficsim.kpi.sampling.meta.MetaDataSet;
import org.opentrafficsim.road.gtu.generator.GTUGeneratorIndividualOld;
import org.opentrafficsim.road.gtu.lane.LaneBasedIndividualGTU;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedGTUFollowingTacticalPlannerFactory;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlusOld;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;
import org.opentrafficsim.road.gtu.strategical.od.Categorization;
import org.opentrafficsim.road.gtu.strategical.od.Category;
import org.opentrafficsim.road.gtu.strategical.od.Interpolation;
import org.opentrafficsim.road.gtu.strategical.od.ODMatrix;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlannerFactory;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.sampling.GtuTypeData;
import org.opentrafficsim.road.network.sampling.LinkData;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;

/**
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
     * @param network Network; network
     * @return origin-destination matrix
     */
    public static ODMatrix get(final Network network)
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

        ODMatrix matrix;
        try
        {
            matrix = new ODMatrix("N201demo", origins, destinations, Categorization.UNCATEGORIZED,
                    DoubleVector.instantiate(new double[] {0, 3600}, TimeUnit.DEFAULT, StorageType.DENSE),
                    Interpolation.STEPWISE);
        }
        catch (ValueRuntimeException exception)
        {
            throw new RuntimeException(exception);
        }

        // loop matrix
        // 2*0 because the through movement on the IJweg is not incorporated
        int[][] od = new int[][] {{0, 502, 309, 35, 285, 33, 218}, {331, 0, 229, 26, 212, 25, 162},
                {150, 89, 0, 12, 98, 11, 75}, {29, 17, 14, 0, 30, 4, 23}, {30, 18, 14, 2 * 0, 32, 4, 25},
                {296, 175, 143, 18, 0, 21, 136}, {67, 40, 32, 4, 63, 0, 787}, {373, 221, 180, 22, 350, 815, 0}};
        for (int o = 0; o < origins.size(); o++)
        {
            for (int d = 0; d < destinations.size(); d++)
            {
                if (od[o][d] > 0)
                {
                    matrix.putTripsVector(origins.get(o), destinations.get(d), Category.UNCATEGORIZED, new int[] {od[o][d]});
                }
            }
        }

        return matrix;
    }

    /**
     * Makes generators at origin nodes of the OD.
     * @param network OTSNetwork; network
     * @param matrix ODMatrix; origin-destination matrix
     * @param simulator OTSSimulatorInterface; simulator
     */
    public static void makeGeneratorsFromOD(final OTSRoadNetwork network, final ODMatrix matrix,
            final OTSSimulatorInterface simulator)
    {

        // fixed generator input
        Class<?> gtuClass = LaneBasedIndividualGTU.class;
        Time startTime = Time.ZERO;
        Time endTime = new Time(Double.MAX_VALUE, TimeUnit.BASE_SECOND);
        Length position = new Length(1.0, LengthUnit.SI);
        GTUType gtuType = network.getGtuType(GTUType.DEFAULTS.CAR);
        ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> initSpeedDist =
                new ContinuousDistDoubleScalar.Rel<>(30, SpeedUnit.KM_PER_HOUR);
        ContinuousDistDoubleScalar.Rel<Length, LengthUnit> lengthDist =
                new ContinuousDistDoubleScalar.Rel<>(4, LengthUnit.METER);
        ContinuousDistDoubleScalar.Rel<Length, LengthUnit> widthDist =
                new ContinuousDistDoubleScalar.Rel<>(2, LengthUnit.METER);
        ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> maxSpeedDist =
                new ContinuousDistDoubleScalar.Rel<>(200, SpeedUnit.KM_PER_HOUR);
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
                        route = network.getShortestRouteBetween(network.getGtuType(GTUType.DEFAULTS.VEHICLE), origin,
                                destination);
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
                            new LaneBasedGTUFollowingTacticalPlannerFactory(new IDMPlusOld()));
            // time
            CrossSectionLink link = (CrossSectionLink) origin.getLinks().iterator().next(); // should be only 1 for origins
            int lanes = link.getLanes().size();
            double iat = 3600 / (dem / lanes);
            ContinuousDistDoubleScalar.Rel<Duration, DurationUnit> iatDist =
                    new ContinuousDistDoubleScalar.Rel<>(iat, DurationUnit.SECOND);
            GTUDirectionality dir =
                    link.getStartNode().equals(origin) ? GTUDirectionality.DIR_PLUS : GTUDirectionality.DIR_MINUS;
            // put generator on each lane
            for (Lane lane : link.getLanes())
            {
                try
                {
                    new GTUGeneratorIndividualOld(origin + "." + link.getLanes().indexOf(lane), simulator, gtuType, gtuClass,
                            initSpeedDist, iatDist, lengthDist, widthDist, maxSpeedDist, Integer.MAX_VALUE, startTime, endTime,
                            lane, position, dir, strategicalPlannerFactory, routeGenerator, network);
                }
                catch (SimRuntimeException exception)
                {
                    throw new RuntimeException(exception);
                }
            }
        }
    }

    /**
     * @param network OTSNetwork; network
     * @param sampler Sampler; sampling
     * @return query covering the entire N201
     */
    public static Query getQuery(final OTSNetwork network, final Sampler sampler)
    {
        // String[] southBound = new String[] { "L1a", "L2a", "L3a4a", "L5a", "L6a", "L7a", "L8a9a", "L10a11a", "L12a",
        // "L13a14a",
        // "L15a16a", "L17a", "L18a19a", "L20a21a", "L22a", "L23a24a", "L25a", "L26a", "L27a", "L28a29a", "L30a", "L31a",
        // "L32a", "L33a", "L34a", "L35a", "L36a", "L37a", "L38a", "L39a", "L40a", "L41a", "L42a", "L43a", "L44a", "L45a",
        // "L46a", "L47a48a", "L49a" };
        String[] southBound = new String[] {"L2a"};
        String[] northBound = new String[] {"L49b", "L48b47b", "L46b", "L45b", "L44b", "L43b", "L42b", "L41b", "L40b", "L39b",
                "L38b", "L37b", "L36b", "L35b", "L34b", "L33b", "L32b", "L31b", "L30b", "L29b28b", "L27b", "L26b", "L25b",
                "L24b23b", "L22b21b", "L20b", "L19b18b", "L17b16b", "L15b", "L14b13b", "L12b", "L11b", "L10b", "L9b8b", "L7b",
                "L6b", "L5b", "L4b3b", "L2b", "L1b"};
        MetaDataSet metaDataSet = new MetaDataSet();
        Set<GtuTypeDataInterface> gtuTypes = new LinkedHashSet<>();
        gtuTypes.add(new GtuTypeData(network.getGtuType(GTUType.DEFAULTS.CAR)));
        gtuTypes.add(new GtuTypeData(network.getGtuType(GTUType.DEFAULTS.BUS)));
        metaDataSet.put(new MetaDataGtuType(), gtuTypes);
        Query query = new Query(sampler, "N201 both directions", metaDataSet, new Frequency(2.0, FrequencyUnit.PER_MINUTE));
        // addSpaceTimeRegions(query, network, northBound);
        addSpaceTimeRegions(query, network, southBound);
        return query;
    }

    /**
     * @param query Query; query
     * @param network OTSNetwork; network
     * @param links String[]; link names
     */
    private static void addSpaceTimeRegions(final Query query, final OTSNetwork network, final String[] links)
    {
        for (String link : links)
        {
            query.addSpaceTimeRegionLink(new LinkData((CrossSectionLink) network.getLink(link)), KpiGtuDirectionality.DIR_PLUS,
                    Length.ZERO, network.getLink(link).getLength(), new Time(0, TimeUnit.BASE_HOUR),
                    new Time(1.0, TimeUnit.BASE_HOUR));
        }
    }

}
