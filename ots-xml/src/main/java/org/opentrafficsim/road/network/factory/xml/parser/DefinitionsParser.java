package org.opentrafficsim.road.network.factory.xml.parser;

import java.lang.reflect.Constructor;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.eval.Eval;
import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;
import org.djutils.reflection.ClassUtil;
import org.opentrafficsim.base.HierarchicalType;
import org.opentrafficsim.base.parameters.ParameterType;
import org.opentrafficsim.core.compatibility.GtuCompatibleInfraType;
import org.opentrafficsim.core.definitions.Defaults;
import org.opentrafficsim.core.definitions.Definitions;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.object.DetectorType;
import org.opentrafficsim.road.definitions.DefaultsRoad;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions.LaneBias;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions.RoadPosition;
import org.opentrafficsim.road.network.factory.xml.XmlParserException;
import org.opentrafficsim.road.network.factory.xml.utils.ParseUtil;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.xml.bindings.types.StringType;
import org.opentrafficsim.xml.generated.Compatibility;
import org.opentrafficsim.xml.generated.GtuTemplate;
import org.opentrafficsim.xml.generated.RoadLayout;
import org.opentrafficsim.xml.generated.SpeedLimit;
import org.opentrafficsim.xml.generated.StripeType;

/**
 * DefinitionParser parses the XML nodes of the Definitions tag: org.opentrafficsim.xml.generated.GtuType, GtuTemplate,
 * LinkType, LaneType and RoadLayout.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class DefinitionsParser
{

    /** */
    private DefinitionsParser()
    {
        // utility class
    }

    /**
     * Parse the Definitions tag in the OTS XML file.
     * @param definitions the Definitions tag
     * @param roadLayoutMap temporary storage for the road layouts
     * @param gtuTemplates map of GTU templates for the OD and/or Generators
     * @param laneBiases map of lane biases for the OD parser
     * @param linkTypeSpeedLimitMap map with speed limit information per link type
     * @param stripes stripes
     * @param eval expression evaluator.
     * @return the parsed definitions
     * @throws XmlParserException on parsing error
     */
    public static Definitions parseDefinitions(final org.opentrafficsim.xml.generated.Definitions definitions,
            final Map<String, RoadLayout> roadLayoutMap, final Map<String, GtuTemplate> gtuTemplates,
            final Map<String, LaneBias> laneBiases, final Map<LinkType, Map<GtuType, Speed>> linkTypeSpeedLimitMap,
            final Map<String, StripeType> stripes, final Eval eval) throws XmlParserException
    {
        Definitions parsedDefinitions = new Definitions();

        // Consumers specify specific checks and how tags or type instances should be stored in the maps
        BiConsumerThrows<org.opentrafficsim.xml.generated.LinkType, LinkType> linkTypeConsumer = new BiConsumerThrows<>()
        {
            @Override
            public void accept(final org.opentrafficsim.xml.generated.LinkType linkTag, final LinkType linkType)
                    throws XmlParserException
            {
                Map<GtuType, Speed> map = new LinkedHashMap<>();
                linkTypeSpeedLimitMap.put(linkType, map);
                for (SpeedLimit speedLimitTag : linkTag.getSpeedLimit())
                {
                    GtuType gtuType = getDefinition(GtuType.class, parsedDefinitions, speedLimitTag.getGtuType(),
                            "LinkType(.SpeedLimit)", linkTag.getId(), "GtuType", eval);
                    map.put(gtuType, speedLimitTag.getLegalSpeedLimit().get(eval));
                }
            }
        };
        BiConsumerThrows<org.opentrafficsim.xml.generated.GtuTemplate, Object> gtuTemplateConsumer = new BiConsumerThrows<>()
        {
            @Override
            public void accept(final org.opentrafficsim.xml.generated.GtuTemplate templateTag, final Object dummy)
                    throws XmlParserException
            {
                getDefinition(GtuType.class, parsedDefinitions, templateTag.getGtuType(), "GtuTemplate", templateTag.getId(),
                        "GtuType", eval);
                gtuTemplates.put(templateTag.getId(), templateTag);
            }
        };
        BiConsumerThrows<StripeType, Object> stripeConsumer = new BiConsumerThrows<>()
        {
            @Override
            public void accept(final StripeType stripeTag, final Object dummy)
            {
                stripes.put(stripeTag.getId(), stripeTag);
            }
        };
        BiConsumerThrows<org.opentrafficsim.xml.generated.RoadLayout, Object> roadLayoutConsumer = new BiConsumerThrows<>()
        {
            @Override
            public void accept(final org.opentrafficsim.xml.generated.RoadLayout layoutTag, final Object dummy)
            {
                roadLayoutMap.put(layoutTag.getId(), layoutTag);
            }
        };
        BiConsumerThrows<org.opentrafficsim.xml.generated.LaneBias, Object> biasConsumer = new BiConsumerThrows<>()
        {
            @Override
            public void accept(final org.opentrafficsim.xml.generated.LaneBias biasTag, final Object dummy)
                    throws XmlParserException
            {
                GtuType gtuType =
                        getDefinition(GtuType.class, parsedDefinitions, biasTag.getGtuType(), "LaneBias", "", "gtuType", eval);
                laneBiases.put(gtuType.getId(), parseLaneBias(biasTag, eval));
            }
        };

        parseDefinitionType(definitions, parsedDefinitions, org.opentrafficsim.xml.generated.GtuTypes.class,
                org.opentrafficsim.xml.generated.GtuType.class, GtuType.class, null, eval);
        parseDefinitionType(definitions, parsedDefinitions, org.opentrafficsim.xml.generated.LinkTypes.class,
                org.opentrafficsim.xml.generated.LinkType.class, LinkType.class, linkTypeConsumer, eval);
        parseDefinitionType(definitions, parsedDefinitions, org.opentrafficsim.xml.generated.LaneTypes.class,
                org.opentrafficsim.xml.generated.LaneType.class, LaneType.class, null, eval);
        parseDefinitionType(definitions, parsedDefinitions, org.opentrafficsim.xml.generated.DetectorTypes.class,
                org.opentrafficsim.xml.generated.DetectorType.class, DetectorType.class, null, eval);

        parseDefinitionType(definitions, parsedDefinitions, org.opentrafficsim.xml.generated.GtuTemplates.class,
                org.opentrafficsim.xml.generated.GtuTemplate.class, GtuType.class, gtuTemplateConsumer, eval);
        parseDefinitionType(definitions, parsedDefinitions, org.opentrafficsim.xml.generated.StripeTypes.class,
                org.opentrafficsim.xml.generated.StripeType.class, GtuType.class, stripeConsumer, eval);
        parseDefinitionType(definitions, parsedDefinitions, org.opentrafficsim.xml.generated.RoadLayouts.class,
                org.opentrafficsim.xml.generated.RoadLayout.class, GtuType.class, roadLayoutConsumer, eval);
        parseDefinitionType(definitions, parsedDefinitions, org.opentrafficsim.xml.generated.LaneBiases.class,
                org.opentrafficsim.xml.generated.LaneBias.class, GtuType.class, biasConsumer, eval);

        // The latter three use GtuType.class as a dummy to comply to T extends HierarchicalType<T, ?>

        return parsedDefinitions;
    }

    /**
     * Generic method to parse definition types. This class will find and loop the relevant type tags. If {@code G} is a
     * <i>generated</i> {@code HierarchicalType}, this method will:
     * <ul>
     * <li>When the type is default, get it from defaults {@code DefaultsRoad} (if {@code T} is within
     * {@code org.opentrafficsim.road}) or {@code Defaults} (otherwise).</li>
     * <li>When the type has a parent, use constructor {@code T(String, T)} to create a new type instance.</li>
     * <li>Otherwise use constructor {@code T(String)} to create a new type instance.</li>
     * </ul>
     * If {@code G} is a <i>generated</i> {@code GtuCompatibleInfraType}, {@code T} must be an <i>OTS</i>
     * GtuCompatibleInfraType. In this case compatibility will be parsed for non-default types. When a consumer is provided, it
     * is called for each tag, with the generated type instance if {@code G} is a <i>generated</i> {@code HierarchicalType}, or
     * {@code null} otherwise.
     * @param definitions org.opentrafficsim.xml.generated.Definitions; definitions tag.
     * @param parsedDefinitions parsed definitions, to get definitions from and store {@code HierarchicalType}s in.
     * @param typesTagClass generated class of XML tag containing type tags, e.g. LaneTypes (generated).
     * @param typeTagClass generated class of XML tag defining type instance, e.g. LaneType (generated).
     * @param typeClass OTS class of type, e.g. LaneType (from ots-road).
     * @param consumer consumer for specific parsing of the type, may be {@code null}.
     * @param eval expression evaluator.
     * @param <L> generated class type of XML tag containing type tags, e.g. LaneTypes (generated).
     * @param <G> generated class type of XML tag defining type instance, e.g. LaneType (generated).
     * @param <T> OTS class type of type, e.g. LaneType (from ots-road).
     * @throws XmlParserException when anything is not or badly defined
     */
    @SuppressWarnings("unchecked")
    private static <L, G, T extends HierarchicalType<T, ?>> void parseDefinitionType(
            final org.opentrafficsim.xml.generated.Definitions definitions, final Definitions parsedDefinitions,
            final Class<L> typesTagClass, final Class<G> typeTagClass, final Class<T> typeClass,
            final BiConsumerThrows<G, ? super T> consumer, final Eval eval) throws XmlParserException
    {
        for (L typesTag : ParseUtil.getObjectsOfType(definitions.getIncludeAndGtuTypesAndGtuTemplates(), typesTagClass))
        {
            for (G g : Try.assign(
                    () -> (List<G>) ClassUtil.resolveMethod(typesTagClass, "get" + typeTagClass.getSimpleName(), null)
                            .invoke(typesTag),
                    XmlParserException.class, "Unable to obtain %s from %s", typeTagClass.getSimpleName(),
                    typesTagClass.getSimpleName()))
            {
                T t = null;
                if (g instanceof org.opentrafficsim.xml.generated.HierarchicalType)
                {
                    // HierarchicalType: GtuType, LinkType, LaneType, DetectorType
                    org.opentrafficsim.xml.generated.HierarchicalType h = (org.opentrafficsim.xml.generated.HierarchicalType) g;
                    if (h.isDefault())
                    {
                        t = getDefault(typeClass, h.getId(), typeClass.getName().startsWith("org.opentrafficsim.road"));
                    }
                    else if (h.getParent() != null)
                    {
                        // Create new type with id and existing parent
                        T parent = getDefinition(typeClass, parsedDefinitions, h.getParent(), typeClass.getSimpleName(),
                                h.getId(), "parent", eval);
                        t = Try.assign(
                                () -> ((Constructor<T>) ClassUtil.resolveConstructor(typeClass,
                                        new Class[] {String.class, typeClass})).newInstance(h.getId(), parent),
                                XmlParserException.class, "No accessible constructor with (String, %s) in %s",
                                typeClass.getSimpleName(), typeClass.getSimpleName());
                        // CategoryLogger.with(Cat.PARSER).trace("Added {} {}", typeClass.getSimpleName(), t);
                    }
                    else
                    {
                        // Create new type with just the id
                        t = Try.assign(
                                () -> ((Constructor<T>) ClassUtil.resolveConstructor(typeClass, new Class[] {String.class}))
                                        .newInstance(h.getId()),
                                XmlParserException.class, "No accessible constructor with (String) in %s",
                                typeClass.getSimpleName());
                        // CategoryLogger.with(Cat.PARSER).trace("Added {} {}", typeClass.getSimpleName(), t);
                    }
                    parsedDefinitions.add(typeClass, t);

                    if (!h.isDefault() && g instanceof org.opentrafficsim.xml.generated.GtuCompatibleInfraType)
                    {
                        // GtuCompatibleInfraType: LinkType, LaneType, DetectorType
                        org.opentrafficsim.xml.generated.GtuCompatibleInfraType c =
                                (org.opentrafficsim.xml.generated.GtuCompatibleInfraType) g;
                        GtuCompatibleInfraType<?, ?> compatibleType = (GtuCompatibleInfraType<?, ?>) t;
                        for (Compatibility compTag : c.getCompatibility())
                        {
                            GtuType gtuType = getDefinition(GtuType.class, parsedDefinitions, compTag.getGtuType(),
                                    typeClass.getSimpleName() + "(.Compatibility)", c.getId(), "GtuType", eval);
                            if (compTag.getCompatible().get(eval))
                            {
                                compatibleType.addCompatibleGtuType(gtuType);
                            }
                            else
                            {
                                compatibleType.addIncompatibleGtuType(gtuType);
                            }
                        }
                    }
                }
                // Anything specific to the tag type
                if (consumer != null)
                {
                    consumer.accept(g, t);
                }
            }
        }
    }

    /**
     * Parse a single lane bias from XML.
     * @param laneBias org.opentrafficsim.xml.generated.LaneBias; lane bias to parse.
     * @param eval expression evaluator.
     * @return parsed lane bias.
     */
    public static LaneBias parseLaneBias(final org.opentrafficsim.xml.generated.LaneBias laneBias, final Eval eval)
    {
        double bias = laneBias.getBias().get(eval);
        int stickyLanes;
        if (laneBias.getStickyLanes() == null)
        {
            stickyLanes = Integer.MAX_VALUE;
        }
        else
        {
            if (laneBias.getStickyLanes().get(eval).compareTo(Integer.MAX_VALUE) > 0)
            {
                stickyLanes = Integer.MAX_VALUE;
            }
            else
            {
                stickyLanes = laneBias.getStickyLanes().get(eval);
            }
        }
        RoadPosition roadPosition;
        if (laneBias.getFromRight() != null)
        {
            roadPosition = new RoadPosition.ByValue(laneBias.getFromRight().get(eval));
        }
        else if (laneBias.getFromLeft() != null)
        {
            roadPosition = new RoadPosition.ByValue(1.0 - laneBias.getFromLeft().get(eval));
        }
        else
        {
            roadPosition = new RoadPosition.BySpeed(laneBias.getLeftSpeed().get(eval), laneBias.getRightSpeed().get(eval));
        }
        return new LaneBias(roadPosition, bias, stickyLanes);
    }

    /**
     * Return parsed definition specified by a field in an element.
     * @param clazz class of element type.
     * @param parsedDefinitions parsed definitions.
     * @param stringType string type containing value to obtain.
     * @param type definition type being parsed, e.g. LaneType.
     * @param elementId id of element being parsed.
     * @param field field in element being obtained, e.g. GtuType or Parent.
     * @param eval expression evaluator.
     * @param <T> element type
     * @return parsed element.
     * @throws XmlParserException when the desired element is not in the parsed definitions.
     */
    private static <T extends HierarchicalType<T, ?>> T getDefinition(final Class<T> clazz, final Definitions parsedDefinitions,
            final StringType stringType, final String type, final String elementId, final String field, final Eval eval)
            throws XmlParserException
    {
        Throw.when(stringType == null, XmlParserException.class, "%s %s %s not defined", type, elementId, field);
        T t = parsedDefinitions.get(clazz, stringType.get(eval));
        Throw.when(t == null, XmlParserException.class, "%s %s %s not found", type, elementId, field);
        return t;
    }

    /**
     * Return parsed definition.
     * @param clazz class of element type.
     * @param definitionId id of definition to obtain.
     * @param road {@code true} to use {@code DefaultsRoad}, otherwise {@code Defaults}.
     * @param <T> element type
     * @return parsed element.
     * @throws XmlParserException when the desired element is not in the parsed definitions.
     */
    private static <T extends HierarchicalType<T, ?>> T getDefault(final Class<T> clazz, final String definitionId,
            final boolean road) throws XmlParserException
    {
        Throw.when(definitionId == null, XmlParserException.class, "%s default has no id.", clazz.getSimpleName());
        T t = road ? DefaultsRoad.getByName(clazz, definitionId) : Defaults.getByName(clazz, definitionId);
        Throw.when(t == null, XmlParserException.class, "%s %s could not be found as default.", clazz.getSimpleName(),
                definitionId);
        return t;
    }

    /**
     * Parse the ParameterType tags in the OTS XML file.
     * @param definitions the Definitions tag
     * @param parameterMap map to store parameter type by id
     * @param eval expression evaluator.
     * @throws XmlParserException if the field in a ParameterType does not refer to a ParameterType&lt;?&gt;
     */
    public static void parseParameterTypes(final org.opentrafficsim.xml.generated.Definitions definitions,
            final Map<String, ParameterType<?>> parameterMap, final Eval eval) throws XmlParserException
    {
        for (org.opentrafficsim.xml.generated.ParameterTypes parameterTypes : ParseUtil.getObjectsOfType(
                definitions.getIncludeAndGtuTypesAndGtuTemplates(), org.opentrafficsim.xml.generated.ParameterTypes.class))
        {
            for (org.opentrafficsim.xml.generated.ParameterType parameterType : parameterTypes.getDurationOrLengthOrSpeed())
            {
                try
                {
                    parameterMap.put(parameterType.getId(), (ParameterType<?>) parameterType.getField().get(eval).get(null));
                }
                catch (ClassCastException exception)
                {
                    throw new XmlParserException("Parameter type with id " + parameterType.getId()
                            + " refers to a static field that is not a ParameterType<?>.");
                }
                catch (IllegalAccessException exception)
                {
                    throw new XmlParserException("Parameter type with id " + parameterType.getId()
                            + " refers to a static field that is not accessible.");
                }
            }
        }
    }

    /**
     * BiConsumer with throws.
     * <p>
     * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     * @param <G> tag type
     * @param <T> type type
     */
    @FunctionalInterface
    private interface BiConsumerThrows<G, T>
    {
        /**
         * Accept input.
         * @param g tag type.
         * @param t type type.
         * @throws XmlParserException when tag refers to non existent type
         */
        void accept(G g, T t) throws XmlParserException;
    }

}
