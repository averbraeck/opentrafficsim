package org.opentrafficsim.road.network.factory.xml.parser;

import java.util.LinkedHashMap;
import java.util.Map;

import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.exceptions.Throw;
import org.djutils.logger.CategoryLogger;
import org.opentrafficsim.base.logger.Cat;
import org.opentrafficsim.base.parameters.ParameterType;
import org.opentrafficsim.core.definitions.Defaults;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.definitions.Definitions;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.road.definitions.DefaultsRoad;
import org.opentrafficsim.road.network.factory.xml.XmlParserException;
import org.opentrafficsim.road.network.factory.xml.utils.ParseUtil;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.object.detector.DetectorType;
import org.opentrafficsim.xml.generated.Compatibility;
import org.opentrafficsim.xml.generated.DetectorTypes;
import org.opentrafficsim.xml.generated.GtuTemplate;
import org.opentrafficsim.xml.generated.GtuTemplates;
import org.opentrafficsim.xml.generated.GtuTypes;
import org.opentrafficsim.xml.generated.LaneTypes;
import org.opentrafficsim.xml.generated.LinkTypes;
import org.opentrafficsim.xml.generated.RoadLayout;
import org.opentrafficsim.xml.generated.RoadLayouts;
import org.opentrafficsim.xml.generated.SpeedLimit;

import nl.tudelft.simulation.dsol.experiment.StreamInformation;

/**
 * DefinitionParser parses the XML nodes of the Definitions tag: org.opentrafficsim.xml.generated.GtuType, GtuTemplate,
 * LinkType, LaneType and RoadLayout.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
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
     * @param definitions the DEFINTIONS tag
     * @param roadLayoutMap temporary storage for the road layouts
     * @param gtuTemplates map of GTU templates for the OD and/or Generators
     * @param streamInformation map with stream information
     * @param linkTypeSpeedLimitMap map with speed limit information per link type
     * @return the parsed definitions
     * @throws XmlParserException on parsing error
     */
    public static Definitions parseDefinitions(final org.opentrafficsim.xml.generated.Definitions definitions,
            final Map<String, RoadLayout> roadLayoutMap, final Map<String, GtuTemplate> gtuTemplates,
            final StreamInformation streamInformation, final Map<LinkType, Map<GtuType, Speed>> linkTypeSpeedLimitMap)
            throws XmlParserException
    {
        Definitions parsedDefinitions = new Definitions();
        parseGtuTypes(definitions, parsedDefinitions);
        parseLinkTypes(definitions, parsedDefinitions, linkTypeSpeedLimitMap);
        parseLaneTypes(definitions, parsedDefinitions);
        parseDetectorTypes(definitions, parsedDefinitions);
        parseGtuTemplates(definitions, parsedDefinitions, gtuTemplates, streamInformation);
        parseRoadLayouts(definitions, parsedDefinitions, roadLayoutMap);
        return parsedDefinitions;
    }

    /**
     * Parse the org.opentrafficsim.xml.generated.GtuTypes tag in the OTS XML file.
     * @param definitions the DEFINTIONS tag
     * @param parsedDefinitions Definitions; parsed definitions (definitions are stored in this)
     * @throws XmlParserException on parsing error
     */
    public static void parseGtuTypes(final org.opentrafficsim.xml.generated.Definitions definitions,
            final Definitions parsedDefinitions) throws XmlParserException
    {
        for (GtuTypes gtuTypes : ParseUtil.getObjectsOfType(definitions.getIncludeAndGtuTypesAndGtuTemplates(),
                org.opentrafficsim.xml.generated.GtuTypes.class))
        {
            for (org.opentrafficsim.xml.generated.GtuType gtuTag : gtuTypes.getGtuType())
            {
                GtuType gtuType;
                if (gtuTag.isDefault())
                {
                    // TODO: remove addition of "NL." once the xml standard has been updated
                    String id = gtuTag.getId().contains(".") ? gtuTag.getId() : "NL." + gtuTag.getId();
                    gtuType = Defaults.getByName(GtuType.class, id);
                    Throw.when(gtuType == null, XmlParserException.class, "GtuType %s could not be found as default.",
                            gtuTag.getId());
                }
                else if (gtuTag.getParent() != null)
                {
                    GtuType parent = parsedDefinitions.get(GtuType.class, gtuTag.getParent());
                    Throw.when(parent == null, XmlParserException.class, "GtuType %s parent %s not found", gtuTag.getId(),
                            gtuTag.getParent());
                    gtuType = new GtuType(gtuTag.getId(), parent);
                    CategoryLogger.filter(Cat.PARSER).trace("Added GtuType {}", gtuType);
                }
                else
                {
                    gtuType = new GtuType(gtuTag.getId());
                    CategoryLogger.filter(Cat.PARSER).trace("Added GtuType {}", gtuType);
                }
                parsedDefinitions.add(GtuType.class, gtuType);
            }
        }
    }

    /**
     * Parse the LinkTypes tag in the OTS XML file.
     * @param definitions the DEFINTIONS tag
     * @param parsedDefinitions Definitions; parsed definitions (definitions are stored in this)
     * @param linkTypeSpeedLimitMap map with speed limit information per link type
     * @throws XmlParserException on parsing error
     */
    public static void parseLinkTypes(final org.opentrafficsim.xml.generated.Definitions definitions,
            final Definitions parsedDefinitions, final Map<LinkType, Map<GtuType, Speed>> linkTypeSpeedLimitMap)
            throws XmlParserException
    {
        for (LinkTypes linkTypes : ParseUtil.getObjectsOfType(definitions.getIncludeAndGtuTypesAndGtuTemplates(),
                LinkTypes.class))
        {
            for (org.opentrafficsim.xml.generated.LinkType linkTag : linkTypes.getLinkType())
            {
                LinkType linkType;
                if (linkTag.isDefault())
                {
                    linkType = Defaults.getByName(LinkType.class, linkTag.getId());
                    Throw.when(linkType == null, XmlParserException.class, "LinkType %s could not be found as default.",
                            linkTag.getId());
                }
                else if (linkTag.getParent() != null)
                {
                    LinkType parent = parsedDefinitions.get(LinkType.class, linkTag.getParent());
                    Throw.when(parent == null, XmlParserException.class, "LinkType %s parent %s not found", linkTag.getId(),
                            linkTag.getParent());
                    linkType = new LinkType(linkTag.getId(), parent);
                    CategoryLogger.filter(Cat.PARSER).trace("Added LinkType {}", linkType);
                }
                else
                {
                    linkType = new LinkType(linkTag.getId());
                    CategoryLogger.filter(Cat.PARSER).trace("Added LinkType {}", linkType);
                }
                parsedDefinitions.add(LinkType.class, linkType);

                for (Compatibility compTag : linkTag.getCompatibility())
                {
                    // TODO: direction is ignored, NONE value erroneously results in accessibility
                    GtuType gtuType = parsedDefinitions.get(GtuType.class, compTag.getGtuType());
                    Throw.when(gtuType == null, XmlParserException.class, "LinkType %s.compatibility: GtuType %s not found",
                            linkTag.getId(), compTag.getGtuType());
                    linkType.addCompatibleGtuType(gtuType);
                }

                linkTypeSpeedLimitMap.put(linkType, new LinkedHashMap<>());
                for (SpeedLimit speedLimitTag : linkTag.getSpeedLimit())
                {
                    GtuType gtuType = parsedDefinitions.get(GtuType.class, speedLimitTag.getGtuType());
                    linkTypeSpeedLimitMap.get(linkType).put(gtuType, speedLimitTag.getLegalSpeedLimit());
                }
            }
        }
    }

    /**
     * Parse the LaneTypes tag in the OTS XML file.
     * @param definitions the DEFINTIONS tag
     * @param parsedDefinitions Definitions; parsed definitions (definitions are stored in this)
     * @throws XmlParserException on parsing error
     */
    public static void parseLaneTypes(final org.opentrafficsim.xml.generated.Definitions definitions,
            final Definitions parsedDefinitions) throws XmlParserException
    {
        for (LaneTypes laneTypes : ParseUtil.getObjectsOfType(definitions.getIncludeAndGtuTypesAndGtuTemplates(),
                LaneTypes.class))
        {
            for (org.opentrafficsim.xml.generated.LaneType laneTag : laneTypes.getLaneType())
            {
                LaneType laneType;
                if (laneTag.isDefault())
                {
                    // TODO: remove addition of "NL." once the xml standard has been updated
                    String id = laneTag.getId().contains(".") ? laneTag.getId() : "NL." + laneTag.getId();
                    laneType = DefaultsRoad.getByName(LaneType.class, id);
                    Throw.when(laneType == null, XmlParserException.class, "LaneType %s could not be found as default.",
                            laneTag.getId());
                }
                else if (laneTag.getParent() != null)
                {
                    LaneType parent = parsedDefinitions.get(LaneType.class, laneTag.getParent());
                    Throw.when(parent == null, XmlParserException.class, "LaneType %s parent %s not found", laneTag.getId(),
                            laneTag.getParent());
                    laneType = new LaneType(laneTag.getId(), parent);
                    CategoryLogger.filter(Cat.PARSER).trace("Added LaneType {}", laneType);
                }
                else
                {
                    laneType = new LaneType(laneTag.getId());
                    CategoryLogger.filter(Cat.PARSER).trace("Added LaneType {}", laneType);
                }
                parsedDefinitions.add(LaneType.class, laneType);

                for (Compatibility compTag : laneTag.getCompatibility())
                {
                    // TODO: direction is ignored, NONE value erroneously results in accessibility
                    GtuType gtuType = parsedDefinitions.get(GtuType.class, compTag.getGtuType());
                    Throw.when(gtuType == null, XmlParserException.class, "LaneType %s.compatibility: GtuType %s not found",
                            laneTag.getId(), compTag.getGtuType());
                    laneType.addCompatibleGtuType(gtuType);
                }
            }
        }
    }

    /**
     * Parse the DetectorTypes tag in the OTS XML file.
     * @param definitions the DEFINTIONS tag
     * @param parsedDefinitions Definitions; parsed definitions (definitions are stored in this)
     * @throws XmlParserException on parsing error
     */
    public static void parseDetectorTypes(final org.opentrafficsim.xml.generated.Definitions definitions,
            final Definitions parsedDefinitions) throws XmlParserException
    {
        for (DetectorTypes detectorTypes : ParseUtil.getObjectsOfType(definitions.getIncludeAndGtuTypesAndGtuTemplates(),
                DetectorTypes.class))
        {
            for (org.opentrafficsim.xml.generated.DetectorType detectorTag : detectorTypes.getDetectorType())
            {
                DetectorType detectorType;
                if (detectorTag.isDefault())
                {
                    // TODO: remove addition of "NL." once the xml standard has been updated
                    String id = detectorTag.getId().contains(".") ? detectorTag.getId() : "NL." + detectorTag.getId();
                    detectorType = DefaultsRoad.getByName(DetectorType.class, id);
                    Throw.when(detectorType == null, XmlParserException.class, "DetectorType %s could not be found as default.",
                            detectorTag.getId());
                }
                else if (detectorTag.getParent() != null)
                {
                    DetectorType parent = parsedDefinitions.get(DetectorType.class, detectorTag.getParent());
                    Throw.when(parent == null, XmlParserException.class, "DetectorType %s parent %s not found",
                            detectorTag.getId(), detectorTag.getParent());
                    detectorType = new DetectorType(detectorTag.getId(), parent);
                    CategoryLogger.filter(Cat.PARSER).trace("Added DetectorType {}", detectorType);
                }
                else
                {
                    detectorType = new DetectorType(detectorTag.getId());
                    CategoryLogger.filter(Cat.PARSER).trace("Added DetectorType {}", detectorType);
                }
                parsedDefinitions.add(DetectorType.class, detectorType);

                for (Compatibility compTag : detectorTag.getCompatibility())
                {
                    // TODO: direction is ignored, NONE value erroneously results in accessibility
                    GtuType gtuType = parsedDefinitions.get(GtuType.class, compTag.getGtuType());
                    Throw.when(gtuType == null, XmlParserException.class, "LaneType %s.compatibility: GtuType %s not found",
                            detectorTag.getId(), compTag.getGtuType());
                    detectorType.addCompatibleGtuType(gtuType);
                }
            }
        }
    }

    /**
     * Store the GtuTemplate tags in the OTS XML file.
     * @param definitions the DEFINTIONS tag
     * @param parsedDefinitions Definitions; parsed definitions (definitions are stored in this)
     * @param gtuTemplates the templates to be used in the OD/Generators
     * @param streamInformation map with stream information
     * @throws XmlParserException on parsing error
     */
    public static void parseGtuTemplates(final org.opentrafficsim.xml.generated.Definitions definitions,
            final Definitions parsedDefinitions, final Map<String, GtuTemplate> gtuTemplates,
            final StreamInformation streamInformation) throws XmlParserException
    {
        for (GtuTemplates templateTypes : ParseUtil.getObjectsOfType(definitions.getIncludeAndGtuTypesAndGtuTemplates(),
                GtuTemplates.class))
        {
            for (GtuTemplate templateTag : templateTypes.getGtuTemplate())
            {
                GtuType gtuType = parsedDefinitions.get(GtuType.class, templateTag.getGtuType());
                if (gtuType == null)
                {
                    throw new XmlParserException(
                            "GTUTemplate " + templateTag.getId() + " GtuType " + templateTag.getGtuType() + " not found");
                }
                gtuTemplates.put(templateTag.getId(), templateTag);
            }
        }
    }

    /**
     * Parse the RoadLayouts tag in the OTS XML file.
     * @param definitions the DEFINTIONS tag
     * @param parsedDefinitions Definitions; parsed definitions (definitions are stored in this)
     * @param roadLayoutMap temporary storage for the road layouts
     * @throws XmlParserException on parsing error
     */
    public static void parseRoadLayouts(final org.opentrafficsim.xml.generated.Definitions definitions,
            final Definitions parsedDefinitions, final Map<String, RoadLayout> roadLayoutMap) throws XmlParserException
    {
        for (RoadLayouts roadLayoutTypes : ParseUtil.getObjectsOfType(definitions.getIncludeAndGtuTypesAndGtuTemplates(),
                RoadLayouts.class))
        {
            for (RoadLayout layoutTag : roadLayoutTypes.getRoadLayout())
            {
                roadLayoutMap.put(layoutTag.getId(), layoutTag);
            }
        }
    }

    /**
     * Parse the ParameterType tags in the OTS XML file.
     * @param definitions the DEFINTIONS tag
     * @param parameterMap map to store parameter type by id
     * @throws XmlParserException if the field in a ParameterType does not refer to a ParameterType&lt;?&gt;
     */
    public static void parseParameterTypes(final org.opentrafficsim.xml.generated.Definitions definitions,
            final Map<String, ParameterType<?>> parameterMap) throws XmlParserException
    {
        for (org.opentrafficsim.xml.generated.ParameterType parameterType : ParseUtil.getObjectsOfType(
                definitions.getIncludeAndGtuTypesAndGtuTemplates(), org.opentrafficsim.xml.generated.ParameterType.class))
        {
            try
            {
                parameterMap.put(parameterType.getId(), (ParameterType<?>) parameterType.getField());
            }
            catch (ClassCastException exception)
            {
                throw new XmlParserException("Parameter type with id " + parameterType.getId()
                        + " refers to a static field that is not a ParameterType<?>.");
            }
        }
    }

}
