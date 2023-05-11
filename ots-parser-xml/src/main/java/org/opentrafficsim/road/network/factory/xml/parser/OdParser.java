package org.opentrafficsim.road.network.factory.xml.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.storage.StorageType;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djunits.value.vdouble.vector.FrequencyVector;
import org.djunits.value.vdouble.vector.TimeVector;
import org.djunits.value.vdouble.vector.base.DoubleVector;
import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;
import org.djutils.logger.CategoryLogger;
import org.djutils.multikeymap.MultiKeyMap;
import org.opentrafficsim.base.logger.Cat;
import org.opentrafficsim.core.definitions.Definitions;
import org.opentrafficsim.core.distributions.Generator;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuTemplate;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.idgenerator.IdGenerator;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;
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
import org.opentrafficsim.road.network.factory.xml.utils.Transformer;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.object.detector.DetectorType;
import org.opentrafficsim.road.od.Categorization;
import org.opentrafficsim.road.od.Category;
import org.opentrafficsim.road.od.Interpolation;
import org.opentrafficsim.road.od.OdApplier;
import org.opentrafficsim.road.od.OdApplier.GeneratorObjects;
import org.opentrafficsim.road.od.OdMatrix;
import org.opentrafficsim.road.od.OdOptions;
import org.opentrafficsim.road.od.OdOptions.Option;
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
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
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
     * @param otsNetwork RoadNetwork; network
     * @param definitions Definitions; parsed definitions.
     * @param demands List&lt;Demand&gt;; demand
     * @param gtuTemplates Map&lt;String, org.opentrafficsim.xml.generated.GtuTemplate&gt;; GTU templates
     * @param definedLaneBiases Map&lt;String, LaneBias&lt;?&gt;&gt;; defined lane biases
     * @param factories Map&lt;String, LaneBasedStrategicalPlannerFactory&lt;?&gt;&gt;; factories from model parser
     * @param modelIdReferrals Map&lt;String, String&gt;; model id referrals
     * @param streamMap Map&lt;String, StreamInformation&gt;; stream map
     * @return List&lt;LaneBasedGtuGenerator&gt;; generators
     * @throws XmlParserException if the OD contains an inconsistency or error
     */
    @SuppressWarnings("checkstyle:methodlength")
    public static List<LaneBasedGtuGenerator> parseDemand(final RoadNetwork otsNetwork, final Definitions definitions,
            final List<Demand> demands, final Map<String, org.opentrafficsim.xml.generated.GtuTemplate> gtuTemplates,
            final Map<String, LaneBias> definedLaneBiases, final Map<String, LaneBasedStrategicalPlannerFactory<?>> factories,
            final Map<String, String> modelIdReferrals, final StreamInformation streamMap) throws XmlParserException
    {
        List<LaneBasedGtuGenerator> generators = new ArrayList<>();

        IdGenerator idGenerator = new IdGenerator("");

        for (Demand subDemand : demands)
        {
            // Collect options
            Map<String, org.opentrafficsim.xml.generated.OdOptions> odOptionsMap = new LinkedHashMap<>();
            for (org.opentrafficsim.xml.generated.OdOptions odOptions : subDemand.getOdOptions())
            {
                odOptionsMap.put(odOptions.getId(), odOptions);
            }
            List<Od> ods = subDemand.getOd();
            for (Od od : ods)
            {

                // ID
                String id = od.getId();

                // Origins and destinations, retrieve them from demand items
                List<Node> origins = new ArrayList<>();
                List<Node> destinations = new ArrayList<>();
                for (Cell demand : od.getCell())
                {
                    if (!origins.contains(otsNetwork.getNode(demand.getOrigin())))
                    {
                        Node originNode = otsNetwork.getNode(demand.getOrigin());
                        if (null == originNode)
                        {
                            CategoryLogger.filter(Cat.PARSER).trace("Parse demand: cannot find origin {}", demand.getOrigin());
                        }
                        else
                        {
                            // TODO: will skipping origins that are not in the network cause problems later on?
                            origins.add(originNode);
                        }
                    }
                    if (!destinations.contains(otsNetwork.getNode(demand.getDestination())))
                    {
                        Node destinationNode = otsNetwork.getNode(demand.getDestination());
                        if (null == destinationNode)
                        {
                            CategoryLogger.filter(Cat.PARSER).trace("Parse demand: cannot find destination {}",
                                    demand.getDestination());
                        }
                        else
                        {
                            // TODO: will skipping origins that are not in the network cause problems later on?
                            destinations.add(destinationNode);
                        }
                    }
                }

                // Create categorization
                Categorization categorization;
                Map<String, Category> categories = new LinkedHashMap<>();
                Map<String, Double> categoryFactors = new LinkedHashMap<>();
                if (od.getCategory() == null || od.getCategory().isEmpty())
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
                        Class<?> clazz = categoryClasses.get(0);
                        categoryClasses.remove(0);
                        categorization = new Categorization("", clazz, categoryClasses.toArray(new Class[0]));
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
                            objects.add(definitions.get(GtuType.class, category.getGtuType()));
                        }
                        if (categorization.entails(Route.class))
                        {
                            objects.add(otsNetwork.getRoute(category.getRoute()));
                        }
                        if (categorization.entails(Lane.class))
                        {
                            CrossSectionLink link = (CrossSectionLink) otsNetwork.getLink(category.getLane().getLink());
                            Lane lane = (Lane) link.getCrossSectionElement(category.getLane().getId());
                            objects.add(lane);
                        }
                        categories.put(category.getId(), new Category(categorization, objects.get(0),
                                objects.subList(1, objects.size()).toArray(new Object[objects.size() - 1])));
                        categoryFactors.put(category.getId(), parsePositiveFactor(category.getFactor()));
                    }
                }

                // Global time vector
                TimeVector globalTimeVector = null;
                if (od.getGlobalTime() != null)
                {
                    List<Time> timeList = new ArrayList<>();
                    for (org.opentrafficsim.xml.generated.GlobalTimeType.Time time : od.getGlobalTime().getTime())
                    {
                        timeList.add(time.getValue());
                    }
                    Collections.sort(timeList);
                    globalTimeVector =
                            Try.assign(() -> DoubleVector.instantiateList(timeList, TimeUnit.DEFAULT, StorageType.DENSE),
                                    XmlParserException.class, "Global time has no values.");
                }

                // Global interpolation
                Interpolation globalInterpolation =
                        od.getGlobalInterpolation().equals("LINEAR") ? Interpolation.LINEAR : Interpolation.STEPWISE;

                // Global factor
                double globalFactor = parsePositiveFactor(od.getGlobalFactor());

                // Create the OD matrix
                OdMatrix odMatrix =
                        new OdMatrix(id, origins, destinations, categorization, globalTimeVector, globalInterpolation);

                // Add demand
                MultiKeyMap<Set<Cell>> demandPerOD = new MultiKeyMap<>(Node.class, Node.class);
                for (Cell demand : od.getCell())
                {
                    Node origin = otsNetwork.getNode(demand.getOrigin());
                    Node destination = otsNetwork.getNode(demand.getDestination());
                    demandPerOD.get(() -> new LinkedHashSet<>(), origin, destination).add(demand);
                }
                for (Object o : demandPerOD.getKeys())
                {
                    MultiKeyMap<Set<Cell>> demandPerD = demandPerOD.getSubMap(o);
                    for (Object d : demandPerD.getKeys())
                    {
                        Set<Cell> set = demandPerD.get(d);
                        Node origin = (Node) o;
                        Node destination = (Node) d;
                        Throw.when(categorization.equals(Categorization.UNCATEGORIZED) && set.size() > 1,
                                XmlParserException.class,
                                "Multiple DEMAND tags define demand from %s to %s in uncategorized demand.", origin.getId(),
                                destination.getId());

                        // Find main demand, that may be split among other DEMAND tags between the same origin and destination
                        Cell main = null;
                        if (!categorization.equals(Categorization.UNCATEGORIZED))
                        {
                            for (Cell cell : set)
                            {
                                if (cell.getCategory() == null)
                                {
                                    Throw.when(main != null, XmlParserException.class,
                                            "Multiple DEMAND tags define main demand from %s to %s.", origin.getId(),
                                            destination.getId());
                                    Throw.when(set.size() == 1, XmlParserException.class,
                                            "Categorized demand from %s to %s has single DEMAND, and without category.",
                                            origin.getId(), destination.getId());
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

                            // TimeVector: demand > main demand > global
                            List<LevelTimeType> timeTags =
                                    cell.getLevel() == null || cell.getLevel().get(0).getTIME() == null
                                            ? (main == null || main.getLevel() == null
                                                    || main.getLevel().get(0).getTIME() == null ? null : main.getLevel())
                                            : cell.getLevel();
                            TimeVector timeVector = timeTags == null ? globalTimeVector : parseTimeVector(timeTags);

                            // Interpolation: demand > main demand > global
                            // TODO: LINEAR follows when only global STEPWISE is defined
                            String interpolationString = cell.getInterpolation() == null
                                    ? (main == null || main.getInterpolation() == null ? null : main.getInterpolation())
                                    : cell.getInterpolation();
                            Interpolation interpolation = interpolationString == null ? globalInterpolation
                                    : interpolationString.equals("LINEAR") ? Interpolation.LINEAR : Interpolation.STEPWISE;

                            // Category
                            Category category = categorization.equals(Categorization.UNCATEGORIZED) ? Category.UNCATEGORIZED
                                    : categories.get(cell.getCategory());

                            // Factor
                            double factor = globalFactor;
                            factor = main == null ? factor : factor * parsePositiveFactor(main.getFactor());
                            factor *= parsePositiveFactor(cell.getFactor());

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
                                        XmlParserException.class, "Demand from %s to %s is specified with factors that have "
                                                + "different time from the base demand.",
                                        origin, destination);
                                demandRaw[i] = parseLevel(baseDemand.get(i).getValue(),
                                        factor * (factors == null ? 1.0 : parsePositiveFactor(factors.get(i).getValue())));
                            }
                            FrequencyVector demandVector =
                                    Try.assign(() -> DoubleVector.instantiate(demandRaw, FrequencyUnit.SI, StorageType.DENSE),
                                            XmlParserException.class, "Unexpected empty demand.");

                            // Finally, add the demand
                            odMatrix.putDemandVector(origin, destination, category, demandVector, timeVector, interpolation);
                        }

                    }
                }

                // OD Options
                OdOptions odOptions =
                        new OdOptions().set(OdOptions.GTU_ID, idGenerator).set(OdOptions.NO_LC_DIST, Length.instantiateSI(1.0));
                // templates
                Set<GtuTemplate> templates = new LinkedHashSet<>();
                for (org.opentrafficsim.xml.generated.GtuTemplate template : gtuTemplates.values())
                {
                    GtuType gtuType = definitions.get(GtuType.class, template.getGtuType());
                    Generator<Length> lengthGenerator = ParseDistribution.parseLengthDist(streamMap, template.getLengthDist());
                    Generator<Length> widthGenerator = ParseDistribution.parseLengthDist(streamMap, template.getWidthDist());
                    Generator<Speed> maximumSpeedGenerator =
                            ParseDistribution.parseSpeedDist(streamMap, template.getMaxSpeedDist());
                    if (template.getMaxAccelerationDist() == null || template.getMaxDecelerationDist() == null)
                    {
                        templates.add(new GtuTemplate(gtuType, lengthGenerator, widthGenerator, maximumSpeedGenerator));
                    }
                    else
                    {
                        Generator<Acceleration> maxAccelerationGenerator =
                                ParseDistribution.parseAccelerationDist(streamMap, template.getMaxAccelerationDist());
                        Generator<Acceleration> maxDecelerationGenerator =
                                ParseDistribution.parseAccelerationDist(streamMap, template.getMaxDecelerationDist());
                        templates.add(new GtuTemplate(gtuType, lengthGenerator, widthGenerator, maximumSpeedGenerator,
                                maxAccelerationGenerator, maxDecelerationGenerator));
                    }
                }
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
                    Throw.when(!odOptionsMap.containsKey(od.getOptions()), XmlParserException.class,
                            "OD options of id od.getOPTIONS() not defined.");
                    for (OdOptionsItem options : odOptionsMap.get(od.getOptions()).getOdOptionsItem())
                    {
                        /*
                         * The current 'options' is valid within a single context, i.e. global, link type, origin or lane. All
                         * option values are set in odOptions for that context, in the current loop. For the model factories an
                         * implementation of DefaultLaneBasedGtuCharacteristicsGeneratorOd is created that responds to the GTU
                         * type, and selects a factory assigned to that GTU type within the context. Or, the default factory in
                         * the context is used. Or finally, a default LMRS. If no model factory is specified in the context (nor
                         * a higher context), no option value is set and OdOptions itself returns a default LMRS factory.
                         */

                        // GTU type (model)
                        if (options.getDefaultModel() != null || (options.getModel() != null && !options.getModel().isEmpty()))
                        {
                            LaneBasedStrategicalPlannerFactory<?> defaultFactory;
                            if (options.getDefaultModel() != null)
                            {
                                // TODO: model id referral
                                String modelId = OdParser.getModelId(options.getDefaultModel(), modelIdReferrals);
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
                            if (options.getModel() != null)
                            {
                                for (Model model : options.getModel())
                                {
                                    GtuType gtuType = definitions.get(GtuType.class, model.getGtuType());
                                    Throw.when(!factories.containsKey(model.getId()), XmlParserException.class,
                                            "OD option Model refers to a non existent-model with ID %s.", model.getId());
                                    gtuTypeFactoryMap.put(gtuType, factories.get(getModelId(model, modelIdReferrals)));
                                }
                            }

                            LaneBasedStrategicalPlannerFactory<LaneBasedStrategicalPlanner> factoryByGtuType =
                                    new LaneBasedStrategicalPlannerFactory<LaneBasedStrategicalPlanner>()
                                    {
                                        /** {@inheritDoc} */
                                        @Override
                                        public LaneBasedStrategicalPlanner create(final LaneBasedGtu gtu, final Route route,
                                                final Node origin, final Node destination) throws GtuException
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
                            setOption(odOptions, OdOptions.GTU_TYPE, characteristicsGeneratorFactory.create(), options,
                                    otsNetwork, definitions);
                        }
                        // no lc
                        setOption(odOptions, OdOptions.NO_LC_DIST, options.getNoLaneChange(), options, otsNetwork, definitions);
                        // room checker
                        setOption(odOptions, OdOptions.ROOM_CHECKER, Transformer.parseRoomChecker(options.getRoomChecker()),
                                options, otsNetwork, definitions);
                        // headway distribution
                        try
                        {
                            setOption(odOptions, OdOptions.HEADWAY_DIST,
                                    Transformer.parseHeadwayDistribution(options.getHeadwayDist()), options, otsNetwork,
                                    definitions);
                        }
                        catch (NoSuchFieldException | IllegalAccessException exception)
                        {
                            throw new XmlParserException(exception);
                        }
                        // markov
                        if (options.getMarkov() != null)
                        {
                            Throw.when(!categorization.entails(GtuType.class), XmlParserException.class,
                                    "The OD option Markov can only be used if GtuType is in the CATEGORY's.");
                            Throw.when(!categorization.entails(Lane.class) && options.getLane() != null,
                                    XmlParserException.class,
                                    "Markov chains at lane level are not used if Lane's are not in the CATEGORY's.");
                            MarkovCorrelation<GtuType, Frequency> markov = new MarkovCorrelation<>();
                            for (State state : options.getMarkov().getState())
                            {
                                GtuType gtuType = definitions.get(GtuType.class, state.getGtuType());
                                double correlation = state.getCorrelation();
                                if (state.getParent() == null)
                                {
                                    markov.addState(gtuType, correlation);
                                }
                                else
                                {
                                    GtuType parentType = definitions.get(GtuType.class, state.getParent());
                                    markov.addState(parentType, gtuType, correlation);
                                }
                            }
                            setOption(odOptions, OdOptions.MARKOV, markov, options, otsNetwork, definitions);
                        }
                        // lane biases
                        if (options.getLaneBiases() != null)
                        {
                            LaneBiases laneBiases = new LaneBiases();
                            for (org.opentrafficsim.xml.generated.LaneBias laneBiasType : ParseUtil.getObjectsOfType(
                                    options.getLaneBiases().getLaneBiasOrDefinedLaneBias(),
                                    org.opentrafficsim.xml.generated.LaneBias.class))
                            {
                                GtuType gtuType = definitions.get(GtuType.class, laneBiasType.getGtuType());
                                Throw.whenNull(gtuType, "GTU type %s in lane bias does not exist.", laneBiasType.getGtuType());
                                laneBiases.addBias(gtuType, DefinitionsParser.parseLaneBias(laneBiasType));
                            }
                            for (DefinedLaneBias definedLaneBias : ParseUtil.getObjectsOfType(
                                    options.getLaneBiases().getLaneBiasOrDefinedLaneBias(), DefinedLaneBias.class))
                            {
                                GtuType gtuType = definitions.get(GtuType.class, definedLaneBias.getGtuType());
                                Throw.whenNull(gtuType, "GTU type %s in defined lane bias does not exist.",
                                        definedLaneBias.getGtuType());
                                laneBiases.addBias(gtuType, definedLaneBiases.get(definedLaneBias.getGtuType()));
                            }
                            setOption(odOptions, OdOptions.LANE_BIAS, laneBiases, options, otsNetwork, definitions);
                        }

                    }
                }

                // Invoke ODApplier
                DetectorType detectorType = definitions.get(DetectorType.class, od.getSinkType());
                Map<String, GeneratorObjects> output =
                        Try.assign(() -> OdApplier.applyOd(otsNetwork, odMatrix, odOptions, detectorType),
                                XmlParserException.class, "Simulator time should be zero when parsing an OD.");

                // Collect generators in output
                for (GeneratorObjects generatorObject : output.values())
                {
                    generators.add(generatorObject.getGenerator());
                }
            }
        }

        return generators;
    }

    /**
     * Parse the value of a LevelTimeType that specifies flow (i.e. with 'veh' per time unit).
     * @param string String; value of LevelTimeType
     * @param factor double; total applicable factor on this level
     * @return Frequency; resulting frequency
     */
    private static Frequency parseLevel(final String string, final double factor)
    {
        return Frequency.valueOf(string.replace("veh", "")).times(factor);
    }

    /**
     * Sorts LevelTimeType in a list by the time value, if any.
     * @param levelTime List&lt;LevelTimeType&gt;; sorted list
     */
    private static void sortLevelTime(final List<LevelTimeType> levelTime)
    {
        Collections.sort(levelTime, new Comparator<LevelTimeType>()
        {
            /** {@inheritDoc} */
            @Override
            public int compare(final LevelTimeType o1, final LevelTimeType o2)
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
     * Parse a list of {@code LevelTimeType} to a {@code TimeVector}.
     * @param list List&lt;LevelTimeType&gt;; list of time information
     * @return TimeVector; time vector
     * @throws XmlParserException if global time has no values
     */
    private static TimeVector parseTimeVector(final List<LevelTimeType> list) throws XmlParserException
    {
        List<Time> timeList = new ArrayList<>();
        for (LevelTimeType time : list)
        {
            timeList.add(time.getTIME());
        }
        Collections.sort(timeList);
        return Try.assign(() -> DoubleVector.instantiateList(timeList, TimeUnit.DEFAULT, StorageType.DENSE),
                XmlParserException.class, "Global time has no values.");
    }

    /**
     * Parses a positive factor.
     * @param factor String; factor in {@code String} format
     * @return double; factor in {@code double} format
     * @throws XmlParserException if the factor is not positive
     */
    private static double parsePositiveFactor(final String factor) throws XmlParserException
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
     * @param odOptions OdOptions; OD options to set the option in
     * @param option Option&lt;T&gt;; option to set
     * @param value T; value to set the option to
     * @param options OdOptionsItem; used to set the option on the right level (Link type, origin node, lane
     * @param otsNetwork RoadNetwork; to get the link type, origin node or lane from
     * @param definitions Definitions; parsed definitions.
     * @param <T> option value type
     */
    private static <T> void setOption(final OdOptions odOptions, final Option<T> option, final T value,
            final OdOptionsItem options, final RoadNetwork otsNetwork, final Definitions definitions)
    {
        if (value != null)
        {
            if (options.getLinkType() != null)
            {
                odOptions.set(definitions.get(LinkType.class, options.getLinkType().getValue()), option, value);
            }
            else if (options.getOrigin() != null)
            {
                odOptions.set(otsNetwork.getNode(options.getOrigin().getValue()), option, value);
            }
            else if (options.getLane() != null)
            {
                CrossSectionLink link = (CrossSectionLink) otsNetwork.getLink(options.getLane().getLink());
                odOptions.set((Lane) link.getCrossSectionElement(options.getLane().getId()), option, value);
            }
            else
            {
                odOptions.set(option, value);
            }
        }
    }

    /**
     * Returns the ID of a default model, referred if there is a referral specified.
     * @param model String; model
     * @param modelIdReferrals String; model ID
     * @return ID of a model, referred if there is a referral specified
     */
    private static String getModelId(final DefaultModel model, final Map<String, String> modelIdReferrals)
    {
        if (model.getModelIdReferral() != null)
        {
            return modelIdReferrals.get(model.getModelIdReferral());
        }
        return model.getId();
    }

    /**
     * Returns the ID of a model, referred if there is a referral specified.
     * @param model String; model
     * @param modelIdReferrals String; model ID
     * @return ID of a model, referred if there is a referral specified
     */
    private static String getModelId(final Model model, final Map<String, String> modelIdReferrals)
    {
        if (model.getModelIdReferral() != null)
        {
            return modelIdReferrals.get(model.getModelIdReferral());
        }
        return model.getId();
    }

}
