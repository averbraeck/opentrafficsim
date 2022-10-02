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
import org.opentrafficsim.core.distributions.Generator;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.gtu.TemplateGtuType;
import org.opentrafficsim.core.idgenerator.IdGenerator;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions.LaneBias;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions.LaneBiases;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions.RoadPosition;
import org.opentrafficsim.road.gtu.generator.LaneBasedGtuGenerator;
import org.opentrafficsim.road.gtu.generator.MarkovCorrelation;
import org.opentrafficsim.road.gtu.generator.od.DefaultGtuCharacteristicsGeneratorOD.Factory;
import org.opentrafficsim.road.gtu.generator.od.ODApplier;
import org.opentrafficsim.road.gtu.generator.od.ODApplier.GeneratorObjects;
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
import org.opentrafficsim.road.network.factory.xml.utils.ParseDistribution;
import org.opentrafficsim.road.network.factory.xml.utils.Transformer;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.xml.generated.CATEGORYTYPE;
import org.opentrafficsim.xml.generated.GLOBALTIMETYPE.TIME;
import org.opentrafficsim.xml.generated.GTUTEMPLATE;
import org.opentrafficsim.xml.generated.LEVELTIMETYPE;
import org.opentrafficsim.xml.generated.NETWORKDEMAND;
import org.opentrafficsim.xml.generated.OD;
import org.opentrafficsim.xml.generated.OD.DEMAND;
import org.opentrafficsim.xml.generated.ODOPTIONS;
import org.opentrafficsim.xml.generated.ODOPTIONS.ODOPTIONSITEM;
import org.opentrafficsim.xml.generated.ODOPTIONS.ODOPTIONSITEM.DEFAULTMODEL;
import org.opentrafficsim.xml.generated.ODOPTIONS.ODOPTIONSITEM.LANEBIASES.LANEBIAS;
import org.opentrafficsim.xml.generated.ODOPTIONS.ODOPTIONSITEM.MARKOV.STATE;
import org.opentrafficsim.xml.generated.ODOPTIONS.ODOPTIONSITEM.MODEL;

import nl.tudelft.simulation.dsol.experiment.StreamInformation;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public final class ODParser
{
    /** */
    private ODParser()
    {
        // static class
    }

    /**
     * Creates generators and returns OD matrices.
     * @param otsNetwork OTSRoadNetwork; network
     * @param demands List&lt;NETWORKDEMAND&gt;; demand
     * @param gtuTemplates Map&lt;String, GTUTEMPLATE&gt;; GTU templates
     * @param factories Map&lt;String, LaneBasedStrategicalPlannerFactory&lt;?&gt;&gt;; factories from model parser
     * @param modelIdReferrals Map&lt;String, String&gt;; model id referrals
     * @param streamMap Map&lt;String, StreamInformation&gt;; stream map
     * @return List&lt;LaneBasedGtuGenerator&gt;; generators
     * @throws XmlParserException if the OD contains an inconsistency or error
     */
    @SuppressWarnings("checkstyle:methodlength")
    public static List<LaneBasedGtuGenerator> parseDemand(final OTSRoadNetwork otsNetwork, final List<NETWORKDEMAND> demands,
            final Map<String, GTUTEMPLATE> gtuTemplates, final Map<String, LaneBasedStrategicalPlannerFactory<?>> factories,
            final Map<String, String> modelIdReferrals, final StreamInformation streamMap) throws XmlParserException
    {
        List<LaneBasedGtuGenerator> generators = new ArrayList<>();

        IdGenerator idGenerator = new IdGenerator("");

        for (NETWORKDEMAND subDemand : demands)
        {
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
                    if (!origins.contains(otsNetwork.getNode(demand.getORIGIN())))
                    {
                        Node originNode = otsNetwork.getNode(demand.getORIGIN());
                        if (null == originNode)
                        {
                            CategoryLogger.filter(Cat.PARSER).trace("Parse demand: cannot find origin {}", demand.getORIGIN());
                        }
                        else
                        {
                            // TODO: will skipping origins that are not in the network cause problems later on?
                            origins.add(originNode);
                        }
                    }
                    if (!destinations.contains(otsNetwork.getNode(demand.getDESTINATION())))
                    {
                        Node destinationNode = otsNetwork.getNode(demand.getDESTINATION());
                        if (null == destinationNode)
                        {
                            CategoryLogger.filter(Cat.PARSER).trace("Parse demand: cannot find destination {}",
                                    demand.getDESTINATION());
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
                if (od.getCATEGORY() == null || od.getCATEGORY().isEmpty())
                {
                    categorization = Categorization.UNCATEGORIZED;
                }
                else
                {
                    List<Class<?>> categoryClasses = new ArrayList<>();
                    if (od.getCATEGORY().get(0).getGTUTYPE() != null)
                    {
                        categoryClasses.add(GtuType.class);
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
                                (categorization.entails(GtuType.class) && category.getGTUTYPE() == null)
                                        || (!categorization.entails(GtuType.class) && category.getGTUTYPE() != null),
                                XmlParserException.class, "Categories are inconsistent concerning GtuType.");
                        Throw.when(
                                (categorization.entails(Route.class) && category.getROUTE() == null)
                                        || (!categorization.entails(Route.class) && category.getROUTE() != null),
                                XmlParserException.class, "Categories are inconsistent concerning Route.");
                        Throw.when(
                                (categorization.entails(Lane.class) && category.getLANE() == null)
                                        || (!categorization.entails(Lane.class) && category.getLANE() != null),
                                XmlParserException.class, "Categories are inconsistent concerning Lane.");
                        List<Object> objects = new ArrayList<>();
                        if (categorization.entails(GtuType.class))
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
                TimeVector globalTimeVector = null;
                if (od.getGLOBALTIME() != null)
                {
                    List<Time> timeList = new ArrayList<>();
                    for (TIME time : od.getGLOBALTIME().getTIME())
                    {
                        timeList.add(time.getVALUE());
                    }
                    Collections.sort(timeList);
                    globalTimeVector =
                            Try.assign(() -> DoubleVector.instantiateList(timeList, TimeUnit.DEFAULT, StorageType.DENSE),
                                    XmlParserException.class, "Global time has no values.");
                }

                // Global interpolation
                Interpolation globalInterpolation =
                        od.getGLOBALINTERPOLATION().equals("LINEAR") ? Interpolation.LINEAR : Interpolation.STEPWISE;

                // Global factor
                double globalFactor = parsePositiveFactor(od.getGLOBALFACTOR());

                // Create the OD matrix
                ODMatrix odMatrix =
                        new ODMatrix(id, origins, destinations, categorization, globalTimeVector, globalInterpolation);

                // Add demand
                MultiKeyMap<Set<DEMAND>> demandPerOD = new MultiKeyMap<>(Node.class, Node.class);
                for (DEMAND demand : od.getDEMAND())
                {
                    Node origin = otsNetwork.getNode(demand.getORIGIN());
                    Node destination = otsNetwork.getNode(demand.getDESTINATION());
                    demandPerOD.get(() -> new LinkedHashSet<>(), origin, destination).add(demand);
                }
                for (Object o : demandPerOD.getKeys())
                {
                    MultiKeyMap<Set<DEMAND>> demandPerD = demandPerOD.getSubMap(o);
                    for (Object d : demandPerD.getKeys())
                    {
                        Set<DEMAND> set = demandPerD.get(d);
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
                            // TODO: LINEAR follows when only global STEPWISE is defined
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
                            FrequencyVector demandVector =
                                    Try.assign(() -> DoubleVector.instantiate(demandRaw, FrequencyUnit.SI, StorageType.DENSE),
                                            XmlParserException.class, "Unexpected empty demand.");

                            // Finally, add the demand
                            odMatrix.putDemandVector(origin, destination, category, demandVector, timeVector, interpolation);
                        }

                    }
                }

                // OD Options
                ODOptions odOptions =
                        new ODOptions().set(ODOptions.GTU_ID, idGenerator).set(ODOptions.NO_LC_DIST, Length.instantiateSI(1.0));
                // templates
                Set<TemplateGtuType> templates = new LinkedHashSet<>();
                for (GTUTEMPLATE template : gtuTemplates.values())
                {
                    GtuType gtuType = otsNetwork.getGtuType(template.getGTUTYPE());
                    Generator<Length> lengthGenerator = ParseDistribution.parseLengthDist(streamMap, template.getLENGTHDIST());
                    Generator<Length> widthGenerator = ParseDistribution.parseLengthDist(streamMap, template.getWIDTHDIST());
                    Generator<Speed> maximumSpeedGenerator =
                            ParseDistribution.parseSpeedDist(streamMap, template.getMAXSPEEDDIST());
                    if (template.getMAXACCELERATIONDIST() == null || template.getMAXDECELERATIONDIST() == null)
                    {
                        templates.add(new TemplateGtuType(gtuType, lengthGenerator, widthGenerator, maximumSpeedGenerator));
                    }
                    else
                    {
                        Generator<Acceleration> maxAccelerationGenerator =
                                ParseDistribution.parseAccelerationDist(streamMap, template.getMAXACCELERATIONDIST());
                        Generator<Acceleration> maxDecelerationGenerator =
                                ParseDistribution.parseAccelerationDist(streamMap, template.getMAXDECELERATIONDIST());
                        templates.add(new TemplateGtuType(gtuType, lengthGenerator, widthGenerator, maximumSpeedGenerator,
                                maxAccelerationGenerator, maxDecelerationGenerator));
                    }
                }
                // default global option to integrate defined templates
                Factory factory = new Factory(); // DefaultGtuCharacteristicsGeneratorOD factory
                factory.setTemplates(templates);
                odOptions.set(ODOptions.GTU_TYPE, factory.create());
                // other options
                if (od.getOPTIONS() != null)
                {
                    Throw.when(!odOptionsMap.containsKey(od.getOPTIONS()), XmlParserException.class,
                            "OD options of id od.getOPTIONS() not defined.");
                    for (ODOPTIONSITEM options : odOptionsMap.get(od.getOPTIONS()).getODOPTIONSITEM())
                    {
                        /*
                         * The current 'options' is valid within a single context, i.e. global, link type, origin or lane. All
                         * option values are set in odOptions for that context, in the current loop. For the model factories an
                         * implementation of StrategicalPlannerFactorySupplierOD is created that responds to the GTU type, and
                         * selects a factory assigned to that GTU type within the context. Or, the default factory in the
                         * context is used. Or finally, a default LMRS. If no model factory is specified in the context (nor a
                         * higher context), no option value is set and ODOptions itself returns a default LMRS factory.
                         */

                        // GTU type (model)
                        if (options.getDEFAULTMODEL() != null || (options.getMODEL() != null && !options.getMODEL().isEmpty()))
                        {
                            LaneBasedStrategicalPlannerFactory<?> defaultFactory;
                            if (options.getDEFAULTMODEL() != null)
                            {
                                // TODO: model id referral
                                String modelId = ODParser.getModelId(options.getDEFAULTMODEL(), modelIdReferrals);
                                Throw.when(!factories.containsKey(modelId), XmlParserException.class,
                                        "OD option DEFAULTMODEL refers to a non-existent model with ID %s.", modelId);
                                defaultFactory = factories.get(modelId);
                            }
                            else
                            {
                                defaultFactory = null;
                            }
                            // compose map that couples GTU types to factories through MODEL ID's
                            final Map<GtuType, LaneBasedStrategicalPlannerFactory<?>> gtuTypeFactoryMap = new LinkedHashMap<>();
                            if (options.getMODEL() != null)
                            {
                                for (MODEL model : options.getMODEL())
                                {
                                    GtuType gtuType = otsNetwork.getGtuType(model.getGTUTYPE());
                                    Throw.when(!factories.containsKey(model.getID()), XmlParserException.class,
                                            "OD option MODEL refers to a non existent-model with ID %s.", model.getID());
                                    gtuTypeFactoryMap.put(gtuType, factories.get(getModelId(model, modelIdReferrals)));
                                }
                            }
                            factory = new Factory(); // DefaultGtuCharacteristicsGeneratorOD factory
                            factory.setTemplates(templates);
                            factory.setFactorySupplier(new StrategicalPlannerFactorySupplierOD()
                            {
                                /** {@inheritDoc} */
                                @Override
                                public LaneBasedStrategicalPlannerFactory<?> getFactory(final Node origin,
                                        final Node destination, final Category category, final StreamInterface randomStream)
                                        throws GtuException
                                {
                                    if (category.getCategorization().entails(GtuType.class))
                                    {
                                        LaneBasedStrategicalPlannerFactory<?> strategicalPlannerFactory =
                                                gtuTypeFactoryMap.get(category.get(GtuType.class));
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
                                    // TODO: LMRSFactory can receive a parameter factory, but how to define those parameters in
                                    // XML?
                                    return new LaneBasedStrategicalRoutePlannerFactory(
                                            new LMRSFactory(new IDMPlusFactory(randomStream),
                                                    new DefaultLMRSPerceptionFactory()),
                                            RouteGeneratorOD.getDefaultRouteSupplier(randomStream));
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
                            Throw.when(!categorization.entails(GtuType.class), XmlParserException.class,
                                    "The OD option MARKOV can only be used if GtuType is in the CATEGORY's.");
                            Throw.when(!categorization.entails(Lane.class) && options.getLANE() != null,
                                    XmlParserException.class,
                                    "Markov chains at lane level are not used if Lane's are not in the CATEGORY's.");
                            MarkovCorrelation<GtuType, Frequency> markov = new MarkovCorrelation<>();
                            for (STATE state : options.getMARKOV().getSTATE())
                            {
                                GtuType gtuType = otsNetwork.getGtuType(state.getGTUTYPE());
                                double correlation = state.getCORRELATION();
                                if (state.getPARENT() == null)
                                {
                                    markov.addState(gtuType, correlation);
                                }
                                else
                                {
                                    GtuType parentType = otsNetwork.getGtuType(state.getPARENT());
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
                                GtuType gtuType = otsNetwork.getGtuType(laneBias.getGTUTYPE());
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
                }

                // Invoke ODApplier
                Map<String, GeneratorObjects> output = Try.assign(() -> ODApplier.applyOD(otsNetwork, odMatrix, odOptions),
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
     * Parse the value of a LEVELTIMETYPE that specifies flow (i.e. with 'veh' per time unit).
     * @param string String; value of LEVELTIMETYPE
     * @param factor double; total applicable factor on this level
     * @return Frequency; resulting frequency
     */
    private static Frequency parseLevel(final String string, final double factor)
    {
        return Frequency.valueOf(string.replace("veh", "")).times(factor);
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
     * @throws XmlParserException if global time has no values
     */
    private static TimeVector parseTimeVector(final List<LEVELTIMETYPE> list) throws XmlParserException
    {
        List<Time> timeList = new ArrayList<>();
        for (LEVELTIMETYPE time : list)
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

    /**
     * Returns the ID of a default model, referred if there is a referral specified.
     * @param model String; model
     * @param modelIdReferrals String; model ID
     * @return ID of a model, referred if there is a referral specified
     */
    private static String getModelId(final DEFAULTMODEL model, final Map<String, String> modelIdReferrals)
    {
        if (model.getMODELIDREFERRAL() != null)
        {
            return modelIdReferrals.get(model.getMODELIDREFERRAL());
        }
        return model.getID();
    }

    /**
     * Returns the ID of a model, referred if there is a referral specified.
     * @param model String; model
     * @param modelIdReferrals String; model ID
     * @return ID of a model, referred if there is a referral specified
     */
    private static String getModelId(final MODEL model, final Map<String, String> modelIdReferrals)
    {
        if (model.getMODELIDREFERRAL() != null)
        {
            return modelIdReferrals.get(model.getMODELIDREFERRAL());
        }
        return model.getID();
    }

}
