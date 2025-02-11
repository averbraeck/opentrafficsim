package org.opentrafficsim.road.network.factory.xml.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djunits.value.vdouble.vector.FrequencyVector;
import org.djunits.value.vdouble.vector.TimeVector;
import org.djutils.eval.Eval;
import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;
import org.djutils.multikeymap.MultiKeyMap;
import org.opentrafficsim.core.definitions.Definitions;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuTemplate;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.idgenerator.IdSupplier;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.object.DetectorType;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions.LaneBias;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions.LaneBiases;
import org.opentrafficsim.road.gtu.generator.LaneBasedGtuGenerator;
import org.opentrafficsim.road.gtu.generator.MarkovCorrelation;
import org.opentrafficsim.road.gtu.generator.characteristics.DefaultLaneBasedGtuCharacteristicsGeneratorOd;
import org.opentrafficsim.road.gtu.generator.characteristics.DefaultLaneBasedGtuCharacteristicsGeneratorOd.Factory;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalRoutePlannerFactory;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.factory.xml.XmlParserException;
import org.opentrafficsim.road.network.factory.xml.utils.ParseDistribution;
import org.opentrafficsim.road.network.factory.xml.utils.ParseUtil;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.od.Categorization;
import org.opentrafficsim.road.od.Category;
import org.opentrafficsim.road.od.Interpolation;
import org.opentrafficsim.road.od.OdApplier;
import org.opentrafficsim.road.od.OdApplier.GeneratorObjects;
import org.opentrafficsim.road.od.OdMatrix;
import org.opentrafficsim.road.od.OdOptions;
import org.opentrafficsim.road.od.OdOptions.Option;
import org.opentrafficsim.xml.bindings.PositiveFactorAdapter;
import org.opentrafficsim.xml.generated.CategoryType;
import org.opentrafficsim.xml.generated.Demand;
import org.opentrafficsim.xml.generated.LevelTimeType;
import org.opentrafficsim.xml.generated.Od;
import org.opentrafficsim.xml.generated.Od.Cell;
import org.opentrafficsim.xml.generated.OdOptions.OdOptionsItem;
import org.opentrafficsim.xml.generated.OdOptions.OdOptionsItem.DefaultModel;
import org.opentrafficsim.xml.generated.OdOptions.OdOptionsItem.LaneBiases.DefinedLaneBias;
import org.opentrafficsim.xml.generated.OdOptions.OdOptionsItem.Markov.State;
import org.opentrafficsim.xml.generated.OdOptions.OdOptionsItem.Model;

import nl.tudelft.simulation.dsol.experiment.StreamInformation;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * This utility creates GTU generators from an OD matrix.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class OdParser
{
    /** */
    private OdParser()
    {
        // static class
    }

    /**
     * Creates generators and returns OD matrices.
     * @param otsNetwork network
     * @param definitions parsed definitions.
     * @param demand demand
     * @param gtuTemplates Map&lt;String, org.opentrafficsim.xml.generated.GtuTemplate&gt;; GTU templates
     * @param definedLaneBiases defined lane biases
     * @param factories factories from model parser
     * @param modelIdReferrals model id referrals
     * @param streamMap stream map
     * @param eval expression evaluator.
     * @return generators
     * @throws XmlParserException if the OD contains an inconsistency or error
     */
    public static List<LaneBasedGtuGenerator> parseDemand(final RoadNetwork otsNetwork, final Definitions definitions,
            final Demand demand, final Map<String, org.opentrafficsim.xml.generated.GtuTemplate> gtuTemplates,
            final Map<String, LaneBias> definedLaneBiases, final Map<String, LaneBasedStrategicalPlannerFactory<?>> factories,
            final Map<String, String> modelIdReferrals, final StreamInformation streamMap, final Eval eval)
            throws XmlParserException
    {
        List<LaneBasedGtuGenerator> generators = new ArrayList<>();

        // Collect options
        Map<String, org.opentrafficsim.xml.generated.OdOptions> odOptionsMap = new LinkedHashMap<>();
        for (org.opentrafficsim.xml.generated.OdOptions odOptions : demand.getOdOptions())
        {
            odOptionsMap.put(odOptions.getId(), odOptions);
        }

        for (Od od : demand.getOd())
        {
            // Origins and destinations, retrieve them from demand items
            List<Node> origins = new ArrayList<>();
            List<Node> destinations = new ArrayList<>();
            for (Cell cell : od.getCell())
            {
                String originId = cell.getOrigin().get(eval);
                if (!origins.contains(otsNetwork.getNode(originId)))
                {
                    Node originNode = otsNetwork.getNode(originId);
                    Throw.whenNull(originNode, "Parse demand: cannot find origin %s", originId);
                    origins.add(originNode);
                }
                String destinationId = cell.getDestination().get(eval);
                if (!destinations.contains(otsNetwork.getNode(destinationId)))
                {
                    Node destinationNode = otsNetwork.getNode(destinationId);
                    Throw.whenNull(destinationNode, "Parse demand: cannot find destination %s", destinationId);
                    destinations.add(destinationNode);
                }
            }

            // Create categorization
            Map<String, Category> categories = new LinkedHashMap<>();
            Categorization categorization = parseCategories(otsNetwork, definitions, od, categories, eval);

            // Global time vector
            TimeVector globalTimeVector = null;
            if (od.getGlobalTime() != null)
            {
                List<Time> timeList = new ArrayList<>();
                for (org.opentrafficsim.xml.generated.GlobalTimeType.Time time : od.getGlobalTime().getTime())
                {
                    timeList.add(time.getValue().get(eval));
                }
                Collections.sort(timeList);
                globalTimeVector = Try.assign(() -> new TimeVector(timeList, TimeUnit.DEFAULT), XmlParserException.class,
                        "Global time has no values.");
            }

            Interpolation globalInterpolation = od.getGlobalInterpolation().get(eval);
            double globalFactor = od.getGlobalFactor().get(eval);

            // Create the OD matrix
            OdMatrix odMatrix =
                    new OdMatrix(od.getId(), origins, destinations, categorization, globalTimeVector, globalInterpolation);

            // Add demand
            MultiKeyMap<Set<Cell>> demandPerOD = new MultiKeyMap<>(Node.class, Node.class);
            for (Cell cell : od.getCell())
            {
                Node origin = otsNetwork.getNode(cell.getOrigin().get(eval));
                Node destination = otsNetwork.getNode(cell.getDestination().get(eval));
                demandPerOD.get(() -> new LinkedHashSet<>(), origin, destination).add(cell);
            }
            addDemand(categories, globalFactor, odMatrix, demandPerOD, eval);

            // OD options
            Set<GtuTemplate> templates = parseGtuTemplates(definitions, gtuTemplates, streamMap, eval);
            OdOptions odOptions = parseOdOptions(otsNetwork, definitions, templates, definedLaneBiases, factories,
                    modelIdReferrals, streamMap, odOptionsMap, od, categorization, eval);

            // Invoke OdApplier
            DetectorType detectorType = definitions.get(DetectorType.class, od.getSinkType().get(eval));
            Map<String, GeneratorObjects> output =
                    Try.assign(() -> OdApplier.applyOd(otsNetwork, odMatrix, odOptions, detectorType), XmlParserException.class,
                            "Simulator time should be zero when parsing an OD.");

            // Collect generators in output
            for (GeneratorObjects generatorObject : output.values())
            {
                generators.add(generatorObject.generator());
            }
        }

        return generators;
    }

    /**
     * Parse categories (save them in a map), and derive the categorization.
     * @param otsNetwork network to obtain routes and lanes in categories.
     * @param definitions definitions to get GTU types in categories.
     * @param od OD with categories.
     * @param categories map to store categories in.
     * @param eval expression evaluator.
     * @return Categorization
     * @throws XmlParserException when a category does not match the categorization.
     */
    private static Categorization parseCategories(final RoadNetwork otsNetwork, final Definitions definitions, final Od od,
            final Map<String, Category> categories, final Eval eval) throws XmlParserException
    {
        Categorization categorization;
        Map<String, Double> categoryFactors = new LinkedHashMap<>();
        if (od.getCategory().isEmpty())
        {
            categorization = Categorization.UNCATEGORIZED;
        }
        else
        {
            List<Class<?>> categoryClasses = new ArrayList<>();
            if (od.getCategory().get(0).getGtuType() != null)
            {
                categoryClasses.add(GtuType.class);
            }
            if (od.getCategory().get(0).getRoute() != null)
            {
                categoryClasses.add(Route.class);
            }
            if (od.getCategory().get(0).getLane() != null)
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
                categorization = new Categorization("", categoryClasses.get(0),
                        categoryClasses.subList(1, categoryClasses.size()).toArray(new Class<?>[categoryClasses.size() - 1]));
            }
            // create categories and check that all categories comply with the categorization
            for (CategoryType category : od.getCategory())
            {
                Throw.when(
                        (categorization.entails(GtuType.class) && category.getGtuType() == null)
                                || (!categorization.entails(GtuType.class) && category.getGtuType() != null),
                        XmlParserException.class, "Categories are inconsistent concerning GtuType.");
                Throw.when(
                        (categorization.entails(Route.class) && category.getRoute() == null)
                                || (!categorization.entails(Route.class) && category.getRoute() != null),
                        XmlParserException.class, "Categories are inconsistent concerning Route.");
                Throw.when(
                        (categorization.entails(Lane.class) && category.getLane() == null)
                                || (!categorization.entails(Lane.class) && category.getLane() != null),
                        XmlParserException.class, "Categories are inconsistent concerning Lane.");
                List<Object> objects = new ArrayList<>();
                if (categorization.entails(GtuType.class))
                {
                    objects.add(definitions.get(GtuType.class, category.getGtuType().get(eval)));
                }
                if (categorization.entails(Route.class))
                {
                    objects.add(otsNetwork.getRoute(category.getRoute().get(eval)));
                }
                if (categorization.entails(Lane.class))
                {
                    CrossSectionLink link = (CrossSectionLink) otsNetwork.getLink(category.getLane().getLink().get(eval));
                    Lane lane = (Lane) link.getCrossSectionElement(category.getLane().getLane().get(eval));
                    objects.add(lane);
                }
                categories.put(category.getId(), new Category(categorization, objects.get(0),
                        objects.subList(1, objects.size()).toArray(new Object[objects.size() - 1])));
                categoryFactors.put(category.getId(), category.getFactor().get(eval));
            }
        }
        return categorization;
    }

    /**
     * Add cell data to OD matrix.
     * @param categories map of parsed categories.
     * @param globalFactor factor on entire OD.
     * @param odMatrix OD matrix to set demand data in.
     * @param demandPerOD cell tags per origin and destination node.
     * @param eval expression evaluator.
     * @throws XmlParserException when data in inconsistently defined.
     */
    private static void addDemand(final Map<String, Category> categories, final double globalFactor, final OdMatrix odMatrix,
            final MultiKeyMap<Set<Cell>> demandPerOD, final Eval eval) throws XmlParserException
    {
        Categorization categorization = odMatrix.getCategorization();
        TimeVector globalTimeVector = odMatrix.getGlobalTimeVector();
        Interpolation globalInterpolation = odMatrix.getGlobalInterpolation();
        for (Object o : demandPerOD.getKeys())
        {
            MultiKeyMap<Set<Cell>> demandPerD = demandPerOD.getSubMap(o);
            for (Object d : demandPerD.getKeys())
            {
                Set<Cell> set = demandPerD.get(d);
                Node origin = (Node) o;
                Node destination = (Node) d;
                Throw.when(categorization.equals(Categorization.UNCATEGORIZED) && set.size() > 1, XmlParserException.class,
                        "Multiple Cell tags define demand from %s to %s in uncategorized demand.", origin.getId(),
                        destination.getId());

                // Find main cell, that may be split among other Cell tags between the same origin and destination
                Cell main = null;
                if (!categorization.equals(Categorization.UNCATEGORIZED))
                {
                    for (Cell cell : set)
                    {
                        if (cell.getCategory() == null)
                        {
                            Throw.when(main != null, XmlParserException.class,
                                    "Multiple Cell tags define main demand from %s to %s.", origin.getId(),
                                    destination.getId());
                            Throw.when(set.size() == 1, XmlParserException.class,
                                    "Categorized demand from %s to %s has single Cell, and without category.", origin.getId(),
                                    destination.getId());
                            main = cell;
                        }
                    }
                }

                // Add cell per tag
                for (Cell cell : set)
                {
                    // Skip main demand, it is split among other tags
                    if (cell.equals(main))
                    {
                        continue;
                    }

                    // TimeVector: cell > main > global
                    TimeVector timeVector = cell.getLevel() != null && cell.getLevel().get(0).getTime() != null
                            ? parseTimeVector(cell.getLevel(), eval)
                            : (main != null && main.getLevel() != null && main.getLevel().get(0).getTime() != null
                                    ? parseTimeVector(main.getLevel(), eval) : globalTimeVector);

                    // Interpolation: cell > main > global
                    Interpolation interpolation = cell.getInterpolation() != null ? cell.getInterpolation().get(eval)
                            : (main != null && main.getInterpolation() != null ? main.getInterpolation().get(eval)
                                    : globalInterpolation);

                    // Category
                    Category category = categorization.equals(Categorization.UNCATEGORIZED) ? Category.UNCATEGORIZED
                            : categories.get(cell.getCategory().get(eval));

                    // Factor: (global * main * cell)
                    double factor = globalFactor;
                    factor = main == null ? factor : factor * main.getFactor().get(eval);
                    factor = cell.getFactor() == null ? factor : factor * cell.getFactor().get(eval);

                    // Figure out where the base demand, and optional factors are
                    Frequency[] demandRaw = new Frequency[timeVector.size()];
                    List<LevelTimeType> baseDemand;
                    List<LevelTimeType> factors = null;
                    if (cell.getLevel() == null)
                    {
                        // this demand specified no levels, use main demand
                        baseDemand = main.getLevel();
                    }
                    else if (cell.getLevel().get(0).getValue().contains("veh"))
                    {
                        // this demand specifies levels
                        baseDemand = cell.getLevel();
                    }
                    else
                    {
                        // this demand specifies factors on the main demand
                        baseDemand = main.getLevel();
                        factors = cell.getLevel();
                    }
                    // sort
                    sortLevelTime(baseDemand, eval);
                    if (factors != null)
                    {
                        sortLevelTime(factors, eval);
                    }
                    // fill array, include factors
                    for (int i = 0; i < baseDemand.size(); i++)
                    {
                        Throw.when(
                                baseDemand.get(i).getTime() != null && factors != null && factors.get(i).getTime() != null
                                        && !baseDemand.get(i).getTime().get(eval).eq(factors.get(i).getTime().get(eval)),
                                XmlParserException.class, "Demand from %s to %s is specified with factors that have "
                                        + "different time from the base demand.",
                                origin, destination);
                        demandRaw[i] = parseLevel(baseDemand.get(i).getValue(), factor * (factors == null ? 1.0
                                : new PositiveFactorAdapter().unmarshal(factors.get(i).getValue()).get(eval)));
                    }
                    FrequencyVector demandVector = new FrequencyVector(demandRaw, FrequencyUnit.SI);

                    // Finally, add the demand
                    odMatrix.putDemandVector(origin, destination, category, demandVector, timeVector, interpolation);
                }
            }
        }
    }

    /**
     * Parse OD options.
     * @param otsNetwork network to obtain routes and lanes in categories.
     * @param definitions definitions to get GTU types in categories.
     * @param templates parsed GTU templates.
     * @param definedLaneBiases parsed lane biases.
     * @param factories parsed model factories.
     * @param modelIdReferrals model id referrals.
     * @param streamMap parsed random streams.
     * @param odOptionsMap Map&lt;String, org.opentrafficsim.xml.generated.OdOptions&gt;; gathered OdOptions tags.
     * @param od OD tag.
     * @param categorization categorization.
     * @param eval expression evaluator.
     * @return OdOptions.
     * @throws XmlParserException when options in OD are not defined, or Markov chain not well defined.
     */
    private static OdOptions parseOdOptions(final RoadNetwork otsNetwork, final Definitions definitions,
            final Set<GtuTemplate> templates, final Map<String, LaneBias> definedLaneBiases,
            final Map<String, LaneBasedStrategicalPlannerFactory<?>> factories, final Map<String, String> modelIdReferrals,
            final StreamInformation streamMap, final Map<String, org.opentrafficsim.xml.generated.OdOptions> odOptionsMap,
            final Od od, final Categorization categorization, final Eval eval) throws XmlParserException
    {
        OdOptions odOptions =
                new OdOptions().set(OdOptions.GTU_ID, new IdSupplier("")).set(OdOptions.NO_LC_DIST, Length.instantiateSI(1.0));

        // default global option to integrate defined templates
        StreamInterface stream = streamMap.getStream("generation");
        LaneBasedStrategicalRoutePlannerFactory defaultLmrsFactory =
                DefaultLaneBasedGtuCharacteristicsGeneratorOd.defaultLmrs(stream);
        Factory characteristicsGeneratorFactory = new Factory(defaultLmrsFactory);
        characteristicsGeneratorFactory.setTemplates(templates);
        odOptions.set(OdOptions.GTU_TYPE, characteristicsGeneratorFactory.create());
        // other options
        if (od.getOptions() != null)
        {
            Throw.when(!odOptionsMap.containsKey(od.getOptions().get(eval)), XmlParserException.class,
                    "OD options in OD %s not defined.", od.getId());
            for (OdOptionsItem option : odOptionsMap.get(od.getOptions().get(eval)).getOdOptionsItem())
            {
                /*
                 * The current 'options' is valid within a single context, i.e. global, link type, origin or lane. All option
                 * values are set in odOptions for that context, in the current loop. For the model factories an implementation
                 * of DefaultLaneBasedGtuCharacteristicsGeneratorOd is created that responds to the GTU type, and selects a
                 * factory assigned to that GTU type within the context. Or, the default factory in the context is used. Or
                 * finally, a default LMRS. If no model factory is specified in the context (nor a higher context), no option
                 * value is set and OdOptions itself returns a default LMRS factory.
                 */

                // GTU type (model)
                parseModelOption(otsNetwork, definitions, factories, modelIdReferrals, odOptions, templates, defaultLmrsFactory,
                        option, eval);

                // no lc
                if (option.getNoLaneChange() != null)
                {
                    setOption(odOptions, OdOptions.NO_LC_DIST, option.getNoLaneChange().get(eval), option, otsNetwork,
                            definitions, eval);
                }

                // room checker
                setOption(odOptions, OdOptions.ROOM_CHECKER, ParseUtil.parseRoomChecker(option.getRoomChecker(), eval), option,
                        otsNetwork, definitions, eval);

                // headway distribution
                if (option.getHeadwayDist() != null)
                {
                    setOption(odOptions, OdOptions.HEADWAY_DIST, option.getHeadwayDist().get(eval), option, otsNetwork,
                            definitions, eval);
                }

                // markov
                if (option.getMarkov() != null)
                {
                    Throw.when(!categorization.entails(GtuType.class), XmlParserException.class,
                            "The OD option Markov can only be used if GtuType is in the CATEGORY's.");
                    Throw.when(!categorization.entails(Lane.class) && option.getLane() != null, XmlParserException.class,
                            "Markov chains at lane level are not used if Lane's are not in the CATEGORY's.");
                    MarkovCorrelation<GtuType, Frequency> markov = new MarkovCorrelation<>();
                    for (State state : option.getMarkov().getState())
                    {
                        GtuType gtuType = definitions.get(GtuType.class, state.getGtuType().get(eval));
                        double correlation = state.getCorrelation().get(eval);
                        if (state.getParent() == null)
                        {
                            markov.addState(gtuType, correlation);
                        }
                        else
                        {
                            GtuType parentType = definitions.get(GtuType.class, state.getParent().get(eval));
                            markov.addState(parentType, gtuType, correlation);
                        }
                    }
                    setOption(odOptions, OdOptions.MARKOV, markov, option, otsNetwork, definitions, eval);
                }

                // lane biases
                if (option.getLaneBiases() != null)
                {
                    LaneBiases laneBiases = new LaneBiases();
                    for (org.opentrafficsim.xml.generated.LaneBias laneBiasType : option.getLaneBiases().getLaneBias())
                    {
                        String gtuTypeId = laneBiasType.getGtuType().get(eval);
                        GtuType gtuType = definitions.get(GtuType.class, gtuTypeId);
                        Throw.whenNull(gtuType, "GTU type %s in lane bias does not exist.", gtuTypeId);
                        laneBiases.addBias(gtuType, DefinitionsParser.parseLaneBias(laneBiasType, eval));
                    }
                    for (DefinedLaneBias definedLaneBias : option.getLaneBiases().getDefinedLaneBias())
                    {
                        String gtuTypeId = definedLaneBias.getGtuType().get(eval);
                        GtuType gtuType = definitions.get(GtuType.class, gtuTypeId);
                        Throw.whenNull(gtuType, "GTU type %s in defined lane bias does not exist.", gtuTypeId);
                        laneBiases.addBias(gtuType, definedLaneBiases.get(definedLaneBias.getGtuType().get(eval)));
                    }
                    setOption(odOptions, OdOptions.LANE_BIAS, laneBiases, option, otsNetwork, definitions, eval);
                }

            }
        }
        return odOptions;
    }

    /**
     * Parse OD model option.
     * @param otsNetwork network to obtain routes and lanes in categories.
     * @param definitions definitions to get GTU types in categories.
     * @param factories parsed model factories.
     * @param modelIdReferrals model id referrals.
     * @param odOptions OD options.
     * @param templates parsed GTU templates.
     * @param defaultLmrsFactory default LMRS factory.
     * @param option OD option item tag.
     * @param eval expression evaluator.
     * @throws XmlParserException when a non-existent model is referred.
     */
    private static void parseModelOption(final RoadNetwork otsNetwork, final Definitions definitions,
            final Map<String, LaneBasedStrategicalPlannerFactory<?>> factories, final Map<String, String> modelIdReferrals,
            final OdOptions odOptions, final Set<GtuTemplate> templates,
            final LaneBasedStrategicalRoutePlannerFactory defaultLmrsFactory, final OdOptionsItem option, final Eval eval)
            throws XmlParserException
    {
        Factory characteristicsGeneratorFactory;
        if (option.getDefaultModel() != null || (option.getModel() != null && !option.getModel().isEmpty()))
        {
            LaneBasedStrategicalPlannerFactory<?> defaultFactory;
            if (option.getDefaultModel() != null)
            {
                // TODO: model id referral
                String modelId = OdParser.getModelId(option.getDefaultModel(), modelIdReferrals, eval);
                Throw.when(!factories.containsKey(modelId), XmlParserException.class,
                        "OD option DefaultModel refers to a non-existent model with ID %s.", modelId);
                defaultFactory = factories.get(modelId);
            }
            else
            {
                defaultFactory = null;
            }
            // compose map that couples GTU types to factories through Model ID's
            final Map<GtuType, LaneBasedStrategicalPlannerFactory<?>> gtuTypeFactoryMap = new LinkedHashMap<>();
            if (option.getModel() != null)
            {
                for (Model model : option.getModel())
                {
                    GtuType gtuType = definitions.get(GtuType.class, model.getGtuType().get(eval));
                    Throw.when(!factories.containsKey(model.getId().get(eval)), XmlParserException.class,
                            "OD option Model refers to a non existent-model with ID %s.", model.getId());
                    gtuTypeFactoryMap.put(gtuType, factories.get(getModelId(model, modelIdReferrals, eval)));
                }
            }

            LaneBasedStrategicalPlannerFactory<LaneBasedStrategicalPlanner> factoryByGtuType =
                    new LaneBasedStrategicalPlannerFactory<LaneBasedStrategicalPlanner>()
                    {
                        @Override
                        public LaneBasedStrategicalPlanner create(final LaneBasedGtu gtu, final Route route, final Node origin,
                                final Node destination) throws GtuException
                        {
                            LaneBasedStrategicalPlannerFactory<?> strategicalPlannerFactory =
                                    gtuTypeFactoryMap.get(gtu.getType());
                            if (strategicalPlannerFactory != null)
                            {
                                // a model factory for this GTU type is specified
                                return strategicalPlannerFactory.create(gtu, route, origin, destination);
                            }
                            if (defaultFactory != null)
                            {
                                // a default model factory is specified
                                return defaultFactory.create(gtu, route, origin, destination);
                            }
                            return defaultLmrsFactory.create(gtu, route, origin, destination);
                        }
                    };
            characteristicsGeneratorFactory = new Factory(factoryByGtuType).setTemplates(templates);
            setOption(odOptions, OdOptions.GTU_TYPE, characteristicsGeneratorFactory.create(), option, otsNetwork, definitions,
                    eval);
        }
    }

    /**
     * @param definitions definitions to get GTU types in categories.
     * @param gtuTemplates Map&lt;String, org.opentrafficsim.xml.generated.GtuTemplate&gt;; GTU template tags.
     * @param streamMap random streams.
     * @param eval expression evaluator.
     * @return GTU templates.
     * @throws XmlParserException when a distribution cannot be parsed.
     */
    private static Set<GtuTemplate> parseGtuTemplates(final Definitions definitions,
            final Map<String, org.opentrafficsim.xml.generated.GtuTemplate> gtuTemplates, final StreamInformation streamMap,
            final Eval eval) throws XmlParserException
    {
        Set<GtuTemplate> templates = new LinkedHashSet<>();
        for (org.opentrafficsim.xml.generated.GtuTemplate template : gtuTemplates.values())
        {
            GtuType gtuType = definitions.get(GtuType.class, template.getGtuType().get(eval));
            Supplier<Length> lengthGenerator = ParseDistribution.parseContinuousDist(streamMap, template.getLengthDist(),
                    template.getLengthDist().getLengthUnit().get(eval), eval);
            Supplier<Length> widthGenerator = ParseDistribution.parseContinuousDist(streamMap, template.getWidthDist(),
                    template.getWidthDist().getLengthUnit().get(eval), eval);
            Supplier<Speed> maximumSpeedGenerator = ParseDistribution.parseContinuousDist(streamMap, template.getMaxSpeedDist(),
                    template.getMaxSpeedDist().getSpeedUnit().get(eval), eval);
            if (template.getMaxAccelerationDist() == null || template.getMaxDecelerationDist() == null)
            {
                templates.add(new GtuTemplate(gtuType, lengthGenerator, widthGenerator, maximumSpeedGenerator));
            }
            else
            {
                Supplier<Acceleration> maxAccelerationGenerator =
                        ParseDistribution.parseContinuousDist(streamMap, template.getMaxAccelerationDist(),
                                template.getMaxAccelerationDist().getAccelerationUnit().get(eval), eval);
                Supplier<Acceleration> maxDecelerationGenerator =
                        ParseDistribution.parseContinuousDist(streamMap, template.getMaxDecelerationDist(),
                                template.getMaxDecelerationDist().getAccelerationUnit().get(eval), eval);
                templates.add(new GtuTemplate(gtuType, lengthGenerator, widthGenerator, maximumSpeedGenerator,
                        maxAccelerationGenerator, maxDecelerationGenerator));
            }
        }
        return templates;
    }

    /**
     * Parse the value of a LevelTimeType that specifies flow (i.e. with 'veh' per time unit).
     * @param string value of LevelTimeType
     * @param factor total applicable factor on this level
     * @return resulting frequency
     */
    private static Frequency parseLevel(final String string, final double factor)
    {
        return Frequency.valueOf(string.replace("veh", "")).times(factor);
    }

    /**
     * Sorts LevelTimeType in a list by the time value, if any.
     * @param levelTime sorted list
     * @param eval expression evaluator.
     */
    private static void sortLevelTime(final List<LevelTimeType> levelTime, final Eval eval)
    {
        Collections.sort(levelTime, new Comparator<LevelTimeType>()
        {
            @Override
            public int compare(final LevelTimeType o1, final LevelTimeType o2)
            {
                if (o1.getTime() == null && o2.getTime() == null)
                {
                    return 0;
                }
                if (o1.getTime() == null)
                {
                    return -1;
                }
                if (o2.getTime() == null)
                {
                    return 1;
                }
                return o1.getTime().get(eval).compareTo(o2.getTime().get(eval));
            }
        });
    }

    /**
     * Parse a list of {@code LevelTimeType} to a {@code TimeVector}.
     * @param list list of time information
     * @param eval expression evaluator.
     * @return time vector
     * @throws XmlParserException if global time has no values
     */
    private static TimeVector parseTimeVector(final List<LevelTimeType> list, final Eval eval) throws XmlParserException
    {
        List<Time> timeList = new ArrayList<>();
        for (LevelTimeType time : list)
        {
            timeList.add(time.getTime().get(eval));
        }
        Collections.sort(timeList);
        return new TimeVector(timeList, TimeUnit.DEFAULT);
    }

    /**
     * Set option.
     * @param odOptions OD options to set the option in
     * @param option option to set
     * @param value value to set the option to
     * @param options used to set the option on the right level (Link type, origin node, lane
     * @param otsNetwork to get the link type, origin node or lane from
     * @param definitions parsed definitions.
     * @param eval expression evaluator.
     * @param <T> option value type
     */
    private static <T> void setOption(final OdOptions odOptions, final Option<T> option, final T value,
            final OdOptionsItem options, final RoadNetwork otsNetwork, final Definitions definitions, final Eval eval)
    {
        if (value != null)
        {
            if (options.getLinkType() != null)
            {
                odOptions.set(definitions.get(LinkType.class, options.getLinkType().get(eval)), option, value);
            }
            else if (options.getOrigin() != null)
            {
                odOptions.set(otsNetwork.getNode(options.getOrigin().get(eval)), option, value);
            }
            else if (options.getLane() != null)
            {
                CrossSectionLink link = (CrossSectionLink) otsNetwork.getLink(options.getLane().getLink().get(eval));
                odOptions.set((Lane) link.getCrossSectionElement(options.getLane().getLane().get(eval)), option, value);
            }
            else
            {
                odOptions.set(option, value);
            }
        }
    }

    /**
     * Returns the ID of a default model, referred if there is a referral specified.
     * @param model model
     * @param modelIdReferrals model ID
     * @param eval expression evaluator.
     * @return ID of a model, referred if there is a referral specified
     */
    private static String getModelId(final DefaultModel model, final Map<String, String> modelIdReferrals, final Eval eval)
    {
        if (model.getModelIdReferral() != null)
        {
            return modelIdReferrals.get(model.getModelIdReferral().get(eval));
        }
        return model.getId().get(eval);
    }

    /**
     * Returns the ID of a model, referred if there is a referral specified.
     * @param model model
     * @param modelIdReferrals model ID
     * @param eval expression evaluator.
     * @return ID of a model, referred if there is a referral specified
     */
    private static String getModelId(final Model model, final Map<String, String> modelIdReferrals, final Eval eval)
    {
        if (model.getModelIdReferral() != null)
        {
            return modelIdReferrals.get(model.getModelIdReferral().get(eval));
        }
        return model.getId().get(eval);
    }

}
