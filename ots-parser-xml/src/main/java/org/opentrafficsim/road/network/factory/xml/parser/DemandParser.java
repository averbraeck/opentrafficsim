package org.opentrafficsim.road.network.factory.xml.parser;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.djunits.value.StorageType;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Time;
import org.djunits.value.vdouble.vector.FrequencyVector;
import org.djunits.value.vdouble.vector.TimeVector;
import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;
import org.opentrafficsim.core.distributions.Generator;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.NestedCache;
import org.opentrafficsim.core.gtu.TemplateGTUType;
import org.opentrafficsim.core.idgenerator.IdGenerator;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.parameters.ParameterFactory;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions.LaneBias;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions.LaneBiases;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions.RoadPosition;
import org.opentrafficsim.road.gtu.generator.LaneBasedGTUGenerator.RoomChecker;
import org.opentrafficsim.road.gtu.generator.MarkovCorrelation;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGTUCharacteristicsGenerator;
import org.opentrafficsim.road.gtu.generator.od.DefaultGTUCharacteristicsGeneratorOD.Factory;
import org.opentrafficsim.road.gtu.generator.od.ODApplier;
import org.opentrafficsim.road.gtu.generator.od.ODOptions;
import org.opentrafficsim.road.gtu.generator.od.ODOptions.Option;
import org.opentrafficsim.road.gtu.generator.od.StrategicalPlannerFactorySupplierOD;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlusFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.DefaultLMRSPerceptionFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.LMRSFactory;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;
import org.opentrafficsim.road.gtu.strategical.od.Categorization;
import org.opentrafficsim.road.gtu.strategical.od.Category;
import org.opentrafficsim.road.gtu.strategical.od.Interpolation;
import org.opentrafficsim.road.gtu.strategical.od.ODMatrix;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlannerFactory;
import org.opentrafficsim.road.gtu.strategical.route.RouteGeneratorOD;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.factory.xml.XmlParserException;
import org.opentrafficsim.road.network.factory.xml.utils.Transformer;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.xml.generated.CATEGORYTYPE;
import org.opentrafficsim.xml.generated.GENERATOR;
import org.opentrafficsim.xml.generated.GLOBALTIMETYPE.TIME;
import org.opentrafficsim.xml.generated.GTUTEMPLATEMIX;
import org.opentrafficsim.xml.generated.LEVELTIMETYPE;
import org.opentrafficsim.xml.generated.LISTGENERATOR;
import org.opentrafficsim.xml.generated.NETWORKDEMAND;
import org.opentrafficsim.xml.generated.OD;
import org.opentrafficsim.xml.generated.OD.DEMAND;
import org.opentrafficsim.xml.generated.ODOPTIONS;
import org.opentrafficsim.xml.generated.ODOPTIONS.ODOPTIONSITEM;
import org.opentrafficsim.xml.generated.ODOPTIONS.ODOPTIONSITEM.LANEBIASES.LANEBIAS;
import org.opentrafficsim.xml.generated.ODOPTIONS.ODOPTIONSITEM.MARKOV.STATE;
import org.opentrafficsim.xml.generated.ODOPTIONS.ODOPTIONSITEM.MODEL;
import org.opentrafficsim.xml.generated.SINK;

import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Mar 29, 2019 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */

public class DemandParser
{
    /** */
    private DemandParser()
    {
        // static class
    }

    /**
     * Creates generators and returns OD matrices.
     * @param otsNetwork OTSRoadNetwork; network
     * @param simulator OTSSimulatorInterface; simulator
     * @param demands List&lt;NETWORKDEMAND&gt;; demand
     * @param parameterFactory ParameterFactory; parameter factory, as parsed by {@code ModelParser}
     * @param gtuTemplates Map&lt;GTUType, TemplateGTUType&gt;; GTU templates
     * @param factories Map&lt;String, LaneBasedStrategicalPlannerFactory&lt;?&gt;&gt;; factories from model parser
     * @throws XmlParserException; if the OD contains an inconsistency or error
     */
    public static void parseDemand(final OTSRoadNetwork otsNetwork, final OTSSimulatorInterface simulator,
            final List<NETWORKDEMAND> demands, final ParameterFactory parameterFactory,
            final Map<GTUType, TemplateGTUType> gtuTemplates, Map<String, LaneBasedStrategicalPlannerFactory<?>> factories)
            throws XmlParserException
    {
        IdGenerator idGenerator = new IdGenerator("");

        int idCounter = 1;
        for (NETWORKDEMAND subDemand : demands)
        {

            // Map<String>
            for (GTUTEMPLATEMIX gtuMix : subDemand.getGTUTEMPLATEMIX())
            {

            }

            for (GENERATOR generator : subDemand.getGENERATOR())
            {
                String linkId = generator.getLINK();
                String laneId = null;

                String id = linkId + "." + laneId + "." + idCounter;
                Generator<Duration> interarrivelTimeGenerator;
                RoomChecker roomChecker;
                LaneBasedGTUCharacteristicsGenerator laneBasedGTUCharacteristicsGenerator;
                // Location

                Link link = otsNetwork.getLink(linkId);
                Throw.when(!(link instanceof CrossSectionLink), XmlParserException.class,
                        "Generator on link %s can not be added as the link is not a CrossSectionLink.", linkId);
                // Lane lane = link.
                Set<DirectedLanePosition> positions = new LinkedHashSet<>();
                // GeneratorPositions generatorPositions = GeneratorPositions.create(positions, stream);

                // LaneBasedGTUGenerator gen = new LaneBasedGTUGenerator(id, interarrivelTimeGenerator,
                // laneBasedGTUCharacteristicsGenerator, generatorPositions, otsNetwork, simulator, roomChecker, idGenerator);
            }

            for (LISTGENERATOR generator : subDemand.getLISTGENERATOR())
            {

            }

            for (SINK sink : subDemand.getSINK())
            {

            }

            // Collect options
            Map<String, ODOPTIONS> odOptionsMap = new LinkedHashMap<>();
            for (ODOPTIONS odOptions : subDemand.getODOPTIONS())
            {
                odOptionsMap.put(odOptions.getID(), odOptions);
            }
            List<OD> ods = subDemand.getOD();
            for (OD od : ods)
            {

                // ID
                String id = od.getID();

                // Origins and destinations, retrieve them from demand items
                List<Node> origins = new ArrayList<>();
                List<Node> destinations = new ArrayList<>();
                for (DEMAND demand : od.getDEMAND())
                {
                    origins.add(otsNetwork.getNode(demand.getORIGIN()));
                    destinations.add(otsNetwork.getNode(demand.getDESTINATION()));
                }

                // Create categorization
                Categorization categorization;
                Map<String, Category> categories = new LinkedHashMap<>();
                Map<String, Double> categoryFactors = new LinkedHashMap<>();
                if (od.getCATEGORY() == null || od.getCATEGORY().isEmpty())
                {
                    categorization = Categorization.UNCATEGORIZED;
                }
                else
                {
                    List<Class<?>> categoryClasses = new ArrayList<>();
                    if (od.getCATEGORY().get(0).getGTUTYPE() != null)
                    {
                        categoryClasses.add(GTUType.class);
                    }
                    if (od.getCATEGORY().get(0).getROUTE() != null)
                    {
                        categoryClasses.add(Route.class);
                    }
                    if (od.getCATEGORY().get(0).getLANE() != null)
                    {
                        categoryClasses.add(Lane.class);
                    }
                    if (categoryClasses.isEmpty())
                    {
                        // XML uses categories, but these define nothing
                        categorization = Categorization.UNCATEGORIZED;
                    }
                    else
                    {
                        Class<?> clazz = categoryClasses.get(0);
                        categoryClasses.remove(0);
                        categorization = new Categorization("", clazz, categoryClasses.toArray(new Class[0]));
                    }
                    // create categories and check that all categories comply with the categorization
                    for (CATEGORYTYPE category : od.getCATEGORY())
                    {
                        Throw.when(
                                (categorization.entails(GTUType.class) && category.getGTUTYPE() == null)
                                        || (!categorization.entails(GTUType.class) && category.getGTUTYPE() != null),
                                XmlParserException.class, "Categories are inconsistent concerning GTUType.");
                        Throw.when(
                                (categorization.entails(Route.class) && category.getROUTE() == null)
                                        || (!categorization.entails(Route.class) && category.getROUTE() != null),
                                XmlParserException.class, "Categories are inconsistent concerning Route.");
                        Throw.when(
                                (categorization.entails(Lane.class) && category.getLANE() == null)
                                        || (!categorization.entails(Lane.class) && category.getLANE() != null),
                                XmlParserException.class, "Categories are inconsistent concerning Lane.");
                        List<Object> objects = new ArrayList<>();
                        if (categorization.entails(GTUType.class))
                        {
                            objects.add(otsNetwork.getGtuType(category.getGTUTYPE()));
                        }
                        if (categorization.entails(Route.class))
                        {
                            objects.add(otsNetwork.getRoute(category.getROUTE()));
                        }
                        if (categorization.entails(Lane.class))
                        {
                            CrossSectionLink link = (CrossSectionLink) otsNetwork.getLink(category.getLANE().getLINK());
                            Lane lane = (Lane) link.getCrossSectionElement(category.getLANE().getID());
                            objects.add(lane);
                        }
                        categories.put(category.getID(), new Category(categorization, objects.get(0),
                                objects.subList(1, objects.size()).toArray(new Object[objects.size() - 1])));
                        categoryFactors.put(category.getID(), parsePositiveFactor(category.getFACTOR()));
                    }
                }

                // Global time vector
                List<Time> timeList = new ArrayList<>();
                for (TIME time : od.getGLOBALTIME().getTIME())
                {
                    timeList.add(time.getVALUE());
                }
                Collections.sort(timeList);
                TimeVector globalTimeVector = Try.assign(() -> new TimeVector(timeList, StorageType.DENSE),
                        XmlParserException.class, "Global time has no values.");

                // Global interpolation
                Interpolation globalInterpolation =
                        od.getGLOBALINTERPOLATION().equals("LINEAR") ? Interpolation.LINEAR : Interpolation.STEPWISE;

                // Global factor
                double globalFactor = parsePositiveFactor(od.getGLOBALFACTOR());

                // Create the OD matrix
                ODMatrix odMatrix =
                        new ODMatrix(id, origins, destinations, categorization, globalTimeVector, globalInterpolation);

                // Add demand
                NestedCache<Set<DEMAND>> demandPerOD = new NestedCache<>(Node.class, Node.class);
                for (DEMAND demand : od.getDEMAND())
                {
                    Node origin = otsNetwork.getNode(demand.getORIGIN());
                    Node destination = otsNetwork.getNode(demand.getDESTINATION());
                    demandPerOD.getValue(() -> new LinkedHashSet<>(), origin, destination).add(demand);
                }
                for (Object o : demandPerOD.getKeys())
                {
                    NestedCache<Set<DEMAND>> demandPerD = demandPerOD.getChild(o);
                    for (Object d : demandPerD.getKeys())
                    {
                        Set<DEMAND> set = demandPerD.getValue(d);
                        Node origin = (Node) o;
                        Node destination = (Node) d;
                        Throw.when(categorization.equals(Categorization.UNCATEGORIZED) && set.size() > 1,
                                XmlParserException.class,
                                "Multiple DEMAND tags define demand from %s to %s in uncategorized demand.", origin.getId(),
                                destination.getId());

                        // Find main demand, that may be split among other DEMAND tags between the same origin and destination
                        DEMAND main = null;
                        if (!categorization.equals(Categorization.UNCATEGORIZED))
                        {
                            for (DEMAND demand : set)
                            {
                                if (demand.getCATEGORY() == null)
                                {
                                    Throw.when(main != null, XmlParserException.class,
                                            "Multiple DEMAND tags define main demand from %s to %s.", origin.getId(),
                                            destination.getId());
                                    Throw.when(set.size() == 1, XmlParserException.class,
                                            "Categorized demand from %s to %s has single DEMAND, and without category.",
                                            origin.getId(), destination.getId());
                                    main = demand;
                                }
                            }
                        }

                        // Add demand per tag
                        for (DEMAND demand : set)
                        {
                            // Skip main demand, it is split among other tags
                            if (demand.equals(main))
                            {
                                continue;
                            }

                            // TimeVector: demand > main demand > global
                            List<LEVELTIMETYPE> timeTags =
                                    demand.getLEVEL() == null || demand.getLEVEL().get(0).getTIME() == null
                                            ? (main == null || main.getLEVEL() == null
                                                    || main.getLEVEL().get(0).getTIME() == null ? null : main.getLEVEL())
                                            : demand.getLEVEL();
                            TimeVector timeVector = timeTags == null ? globalTimeVector : parseTimeVector(timeTags);

                            // Interpolation: demand > main demand > global
                            String interpolationString = demand.getINTERPOLATION() == null
                                    ? (main == null || main.getINTERPOLATION() == null ? null : main.getINTERPOLATION())
                                    : demand.getINTERPOLATION();
                            Interpolation interpolation = interpolationString == null ? globalInterpolation
                                    : interpolationString.equals("LINEAR") ? Interpolation.LINEAR : Interpolation.STEPWISE;

                            // Category
                            Category category = categorization.equals(Categorization.UNCATEGORIZED) ? Category.UNCATEGORIZED
                                    : categories.get(demand.getCATEGORY());

                            // Factor
                            double factor = globalFactor;
                            factor = main == null ? factor : factor * parsePositiveFactor(main.getFACTOR());
                            factor *= parsePositiveFactor(demand.getFACTOR());

                            // Figure out where the base demand, and optional factors are
                            Frequency[] demandRaw = new Frequency[timeVector.size()];
                            List<LEVELTIMETYPE> baseDemand;
                            List<LEVELTIMETYPE> factors = null;
                            if (demand.getLEVEL() == null)
                            {
                                // this demand specified no levels, use main demand
                                baseDemand = main.getLEVEL();
                            }
                            else if (demand.getLEVEL().get(0).getValue().contains("veh"))
                            {
                                // this demand specifies levels
                                baseDemand = demand.getLEVEL();
                            }
                            else
                            {
                                // this demand specifies factors on the main demand
                                baseDemand = main.getLEVEL();
                                factors = demand.getLEVEL();
                            }
                            // sort
                            sortLevelTime(baseDemand);
                            if (factors != null)
                            {
                                sortLevelTime(factors);
                            }
                            // fill array, include factors
                            for (int i = 0; i < baseDemand.size(); i++)
                            {
                                Throw.when(
                                        baseDemand.get(i).getTIME() != null && factors != null
                                                && factors.get(i).getTIME() != null
                                                && !baseDemand.get(i).getTIME().eq(factors.get(i).getTIME()),
                                        XmlParserException.class,
                                        "Demand from %s to %s is specified with factors that have different time from the base demand.",
                                        origin, destination);
                                demandRaw[i] = parseLevel(baseDemand.get(i).getValue(),
                                        factor * (factors == null ? 1.0 : parsePositiveFactor(factors.get(i).getValue())));
                            }
                            FrequencyVector demandVector = Try.assign(() -> new FrequencyVector(demandRaw, StorageType.DENSE),
                                    XmlParserException.class, "Unexpected empty demand.");

                            // Finally, add the demand
                            odMatrix.putDemandVector(origin, destination, category, demandVector, timeVector, interpolation);
                        }

                    }
                }

                // OD Options
                ODOptions odOptions = new ODOptions().set(ODOptions.GTU_ID, idGenerator);
                for (ODOPTIONSITEM options : odOptionsMap.get(od.getOPTIONS()).getODOPTIONSITEM())
                {
                    /*
                     * The current 'options' is valid within a single context, i.e. global, link type, origin or lane. All
                     * option values are set in odOptions for that context, in the current loop. For the model factories an
                     * implementation of StrategicalPlannerFactorySupplierOD is created that responds to the GTU type, and
                     * selects a factory assigned to that GTU type within the context. Or, the default factory in the context is
                     * used. Or finally, a default LMRS. If no model factory is specified in the context (nor a higher context),
                     * no option value is set and ODOptions itself returns a default LMRS factory.
                     */

                    // GTU type (model)
                    if (options.getDEFAULTMODEL() != null || (options.getMODEL() != null && !options.getMODEL().isEmpty()))
                    {
                        LaneBasedStrategicalPlannerFactory<?> defaultFactory;
                        if (options.getDEFAULTMODEL() != null)
                        {
                            String modelId = options.getDEFAULTMODEL().getID();
                            Throw.when(!factories.containsKey(modelId), XmlParserException.class,
                                    "OD option DEFAULTMODEL refers to a non-existent model with ID %s.", modelId);
                            defaultFactory = factories.get(modelId);
                        }
                        else
                        {
                            defaultFactory = null;
                        }
                        // compose map that couples GTU types to factories through MODEL ID's
                        final Map<GTUType, LaneBasedStrategicalPlannerFactory<?>> gtuTypeFactoryMap = new LinkedHashMap<>();
                        if (options.getMODEL() != null)
                        {
                            for (MODEL model : options.getMODEL())
                            {
                                GTUType gtuType = otsNetwork.getGtuType(model.getGTUTYPE());
                                Throw.when(!factories.containsKey(model.getID()), XmlParserException.class,
                                        "OD option MODEL refers to a non existent-model with ID %s.", model.getID());
                                gtuTypeFactoryMap.put(gtuType, factories.get(model.getID()));
                            }
                        }
                        Factory factory = new Factory(); // DefaultGTUCharacteristicsGeneratorOD factory
                        factory.setTemplates(new LinkedHashSet<>(gtuTemplates.values()));
                        factory.setFactorySupplier(new StrategicalPlannerFactorySupplierOD()
                        {
                            /** {@inheritDoc} */
                            @Override
                            public LaneBasedStrategicalPlannerFactory<?> getFactory(final Node origin, final Node destination,
                                    final Category category, final StreamInterface randomStream) throws GTUException
                            {
                                if (category.getCategorization().entails(GTUType.class))
                                {
                                    LaneBasedStrategicalPlannerFactory<?> strategicalPlannerFactory =
                                            gtuTypeFactoryMap.get(category.get(GTUType.class));
                                    if (strategicalPlannerFactory != null)
                                    {
                                        // a model factory for this GTU type is specified
                                        return strategicalPlannerFactory;
                                    }
                                }
                                if (defaultFactory != null)
                                {
                                    // a default model factory is specified
                                    return defaultFactory;
                                }
                                // no model factory specified, return a default LMRS factory
                                return new LaneBasedStrategicalRoutePlannerFactory(
                                        new LMRSFactory(new IDMPlusFactory(randomStream), new DefaultLMRSPerceptionFactory()),
                                        parameterFactory, RouteGeneratorOD.getDefaultRouteSupplier(randomStream));
                            }
                        });
                        setOption(odOptions, ODOptions.GTU_TYPE, factory.create(), options, otsNetwork);
                    }
                    // no lc
                    setOption(odOptions, ODOptions.NO_LC_DIST, options.getNOLANECHANGE(), options, otsNetwork);
                    // room checker
                    setOption(odOptions, ODOptions.ROOM_CHECKER, Transformer.parseRoomChecker(options.getROOMCHECKER()),
                            options, otsNetwork);
                    // headway distribution
                    try
                    {
                        setOption(odOptions, ODOptions.HEADWAY_DIST,
                                Transformer.parseHeadwayDistribution(options.getHEADWAYDIST()), options, otsNetwork);
                    }
                    catch (NoSuchFieldException | IllegalAccessException exception)
                    {
                        throw new XmlParserException(exception);
                    }
                    // markov
                    if (options.getMARKOV() != null)
                    {
                        Throw.when(!categorization.entails(GTUType.class), XmlParserException.class,
                                "The OD option MARKOV can only be used if GTUType is in the CATEGORY's.");
                        Throw.when(!categorization.entails(Lane.class) && options.getLANE() != null, XmlParserException.class,
                                "Markov chains at lane level are not used if Lane's are not in the CATEGORY's.");
                        MarkovCorrelation<GTUType, Frequency> markov = new MarkovCorrelation<>();
                        for (STATE state : options.getMARKOV().getSTATE())
                        {
                            GTUType gtuType = otsNetwork.getGtuType(state.getGTUTYPE());
                            double correlation = state.getCORRELATION();
                            if (state.getPARENT() == null)
                            {
                                markov.addState(gtuType, correlation);
                            }
                            else
                            {
                                GTUType parentType = otsNetwork.getGtuType(state.getPARENT());
                                markov.addState(parentType, gtuType, correlation);
                            }
                        }
                        setOption(odOptions, ODOptions.MARKOV, markov, options, otsNetwork);
                    }
                    // lane biases
                    if (options.getLANEBIASES() != null)
                    {
                        LaneBiases laneBiases = new LaneBiases();
                        for (LANEBIAS laneBias : options.getLANEBIASES().getLANEBIAS())
                        {
                            GTUType gtuType = otsNetwork.getGtuType(laneBias.getGTUTYPE());
                            double bias = laneBias.getBIAS();
                            int stickyLanes;
                            if (laneBias.getSTICKYLANES() == null)
                            {
                                stickyLanes = Integer.MAX_VALUE;
                            }
                            else
                            {
                                if (laneBias.getSTICKYLANES().compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0)
                                {
                                    stickyLanes = Integer.MAX_VALUE;
                                }
                                else
                                {
                                    stickyLanes = laneBias.getSTICKYLANES().intValue();
                                }
                            }
                            RoadPosition roadPosition;
                            if (laneBias.getFROMRIGHT() != null)
                            {
                                roadPosition = new RoadPosition.ByValue(laneBias.getFROMRIGHT());
                            }
                            else if (laneBias.getFROMLEFT() != null)
                            {
                                roadPosition = new RoadPosition.ByValue(1.0 - laneBias.getFROMLEFT());
                            }
                            else
                            {
                                roadPosition = new RoadPosition.BySpeed(laneBias.getLEFTSPEED(), laneBias.getRIGHTSPEED());
                            }
                            laneBiases.addBias(gtuType, new LaneBias(roadPosition, bias, stickyLanes));
                        }
                        setOption(odOptions, ODOptions.getLaneBiasOption(otsNetwork), laneBiases, options, otsNetwork);
                    }
                }

                // Invoke ODApplier
                Try.execute(() -> ODApplier.applyOD(otsNetwork, odMatrix, simulator, odOptions), XmlParserException.class,
                        "Simulator time should be zero when parsing an OD.");
            }
        }
    }

    /**
     * Parse the value of a LEVELTIMETYPE that specifies flow (i.e. with 'veh' per time unit).
     * @param string String; value of LEVELTIMETYPE
     * @param factor double; total applicable factor on this level
     * @return Frequency; resulting frequency
     */
    private static Frequency parseLevel(final String string, double factor)
    {
        return Frequency.valueOf(string.replace("veh", "")).multiplyBy(factor);
    }

    /**
     * Sorts LEVELTIMETYPE in a list by the time value, if any.
     * @param levelTime List&lt;LEVELTIMETYPE&gt;; sorted list
     */
    private static void sortLevelTime(final List<LEVELTIMETYPE> levelTime)
    {
        Collections.sort(levelTime, new Comparator<LEVELTIMETYPE>()
        {
            /** {@inheritDoc} */
            @Override
            public int compare(final LEVELTIMETYPE o1, final LEVELTIMETYPE o2)
            {
                if (o1.getTIME() == null && o2.getTIME() == null)
                {
                    return 0;
                }
                if (o1.getTIME() == null)
                {
                    return -1;
                }
                if (o2.getTIME() == null)
                {
                    return 1;
                }
                return o1.getTIME().compareTo(o2.getTIME());
            }
        });
    }

    /**
     * Parse a list of {@code LEVELTIMETYPE} to a {@code TimeVector}.
     * @param list List&lt;LEVELTIMETYPE&gt;; list of time information
     * @return TimeVector; time vector
     * @throws XmlParserException
     */
    private final static TimeVector parseTimeVector(final List<LEVELTIMETYPE> list) throws XmlParserException
    {
        List<Time> timeList = new ArrayList<>();
        for (LEVELTIMETYPE time : list)
        {
            timeList.add(time.getTIME());
        }
        Collections.sort(timeList);
        return Try.assign(() -> new TimeVector(timeList, StorageType.DENSE), XmlParserException.class,
                "Global time has no values.");
    }

    /**
     * Parses a positive factor.
     * @param factor String; factor in {@code String} format
     * @return double; factor in {@code double} format
     * @throws XmlParserException if the factor is not positive
     */
    private final static double parsePositiveFactor(final String factor) throws XmlParserException
    {
        double factorValue;
        if (factor.endsWith("%"))
        {
            factorValue = Double.parseDouble(factor.substring(0, factor.length() - 1)) / 100.0;
        }
        factorValue = Double.parseDouble(factor);
        Throw.when(factorValue < 0.0, XmlParserException.class, "Factor %d is not positive.", factorValue);
        return factorValue;
    }

    /**
     * Set option.
     * @param odOptions ODOptions; OD options to set the option in
     * @param option Option&lt;T&gt;; option to set
     * @param value T; value to set the option to
     * @param options ODOPTIONSITEM; used to set the option on the right level (Link type, origin node, lane
     * @param otsNetwork OTSRoadNetwork; to get the link type, origin node or lane from
     * @param <T> option value type
     */
    private static <T> void setOption(final ODOptions odOptions, final Option<T> option, final T value,
            final ODOPTIONSITEM options, final OTSRoadNetwork otsNetwork)
    {
        if (value != null)
        {
            if (options.getLINKTYPE() != null)
            {
                odOptions.set(otsNetwork.getLinkType(options.getLINKTYPE().getVALUE()), option, value);
            }
            else if (options.getORIGIN() != null)
            {
                odOptions.set(otsNetwork.getNode(options.getORIGIN().getVALUE()), option, value);
            }
            else if (options.getLANE() != null)
            {
                CrossSectionLink link = (CrossSectionLink) otsNetwork.getLink(options.getLANE().getLINK());
                odOptions.set((Lane) link.getCrossSectionElement(options.getLANE().getID()), option, value);
            }
            else
            {
                odOptions.set(option, value);
            }
        }
    }

}
