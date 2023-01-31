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
import org.opentrafficsim.xml.generated.COMPATIBILITY;
import org.opentrafficsim.xml.generated.DEFINITIONS;
import org.opentrafficsim.xml.generated.GTUTEMPLATE;
import org.opentrafficsim.xml.generated.GTUTEMPLATES;
import org.opentrafficsim.xml.generated.GTUTYPE;
import org.opentrafficsim.xml.generated.GTUTYPES;
import org.opentrafficsim.xml.generated.LANETYPE;
import org.opentrafficsim.xml.generated.LANETYPES;
import org.opentrafficsim.xml.generated.LINKTYPE;
import org.opentrafficsim.xml.generated.LINKTYPES;
import org.opentrafficsim.xml.generated.PARAMETERTYPE;
import org.opentrafficsim.xml.generated.ROADLAYOUT;
import org.opentrafficsim.xml.generated.ROADLAYOUTS;
import org.opentrafficsim.xml.generated.SPEEDLIMIT;

import nl.tudelft.simulation.dsol.experiment.StreamInformation;

/**
 * DefinitionParser parses the XML nodes of the DEFINITIONS tag: GTUTYPE, GTUTEMPLATE, LINKTYPE, LANETYPE and ROADLAYOUT.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
     * Parse the DEFINITIONS tag in the OTS XML file.
     * @param definitions the DEFINTIONS tag
     * @param overwriteDefaults overwrite default definitions in otsNetwork or not
     * @param roadLayoutMap temporary storage for the road layouts
     * @param gtuTemplates map of GTU templates for the OD and/or Generators
     * @param streamInformation map with stream information
     * @param linkTypeSpeedLimitMap map with speed limit information per link type
     * @return the parsed definitions
     * @throws XmlParserException on parsing error
     */
    public static Definitions parseDefinitions(final DEFINITIONS definitions, final boolean overwriteDefaults,
            final Map<String, ROADLAYOUT> roadLayoutMap, final Map<String, GTUTEMPLATE> gtuTemplates,
            final StreamInformation streamInformation, final Map<LinkType, Map<GtuType, Speed>> linkTypeSpeedLimitMap)
            throws XmlParserException
    {
        Definitions parsedDefinitions = new Definitions();
        parseGtuTypes(definitions, parsedDefinitions);
        parseLinkTypes(definitions, parsedDefinitions, overwriteDefaults, linkTypeSpeedLimitMap);
        parseLaneTypes(definitions, parsedDefinitions, overwriteDefaults);
        parseGtuTemplates(definitions, parsedDefinitions, overwriteDefaults, gtuTemplates, streamInformation);
        parseRoadLayouts(definitions, parsedDefinitions, roadLayoutMap);
        return parsedDefinitions;
    }

    /**
     * Parse the GTUTYPES tag in the OTS XML file.
     * @param definitions the DEFINTIONS tag
     * @param parsedDefinitions Definitions; parsed definitions (definitions are stored in this)
     * @throws XmlParserException on parsing error
     */
    public static void parseGtuTypes(final DEFINITIONS definitions, final Definitions parsedDefinitions)
            throws XmlParserException
    {
        for (GTUTYPES gtuTypes : ParseUtil.getObjectsOfType(definitions.getIncludeAndGTUTYPESAndGTUTEMPLATES(), GTUTYPES.class))
        {
            for (GTUTYPE gtuTag : gtuTypes.getGTUTYPE())
            {
                GtuType gtuType;
                if (gtuTag.isDEFAULT())
                {
                    // TODO: remove addition of "NL." once the xml standard has been updated
                    String id = gtuTag.getID().contains(".") ? gtuTag.getID() : "NL." + gtuTag.getID();
                    gtuType = Defaults.getByName(GtuType.class, id);
                    Throw.when(gtuType == null, XmlParserException.class, "GtuType %s could not be found as default.",
                            gtuTag.getID());
                }
                else if (gtuTag.getPARENT() != null)
                {
                    GtuType parent = parsedDefinitions.get(GtuType.class, gtuTag.getPARENT());
                    Throw.when(parent == null, XmlParserException.class, "GtuType %s parent %s not found", gtuTag.getID(),
                            gtuTag.getPARENT());
                    gtuType = new GtuType(gtuTag.getID(), parent);
                    CategoryLogger.filter(Cat.PARSER).trace("Added GtuType {}", gtuType);
                }
                else
                {
                    gtuType = new GtuType(gtuTag.getID());
                    CategoryLogger.filter(Cat.PARSER).trace("Added GtuType {}", gtuType);
                }
                parsedDefinitions.add(GtuType.class, gtuType);
            }
        }
    }

    /**
     * Parse the LINKTYPES tag in the OTS XML file.
     * @param definitions the DEFINTIONS tag
     * @param parsedDefinitions Definitions; parsed definitions (definitions are stored in this)
     * @param overwriteDefaults overwrite default definitions in otsNetwork or not
     * @param linkTypeSpeedLimitMap map with speed limit information per link type
     * @throws XmlParserException on parsing error
     */
    public static void parseLinkTypes(final DEFINITIONS definitions, final Definitions parsedDefinitions,
            final boolean overwriteDefaults, final Map<LinkType, Map<GtuType, Speed>> linkTypeSpeedLimitMap)
            throws XmlParserException
    {
        for (LINKTYPES linkTypes : ParseUtil.getObjectsOfType(definitions.getIncludeAndGTUTYPESAndGTUTEMPLATES(),
                LINKTYPES.class))
        {
            for (LINKTYPE linkTag : linkTypes.getLINKTYPE())
            {
                LinkType linkType;
                if (linkTag.isDEFAULT())
                {
                    // TODO: remove if-statement (keep else-part) once the xml standard has been updated
                    if (linkTag.getID().equals("NONE"))
                    {
                        linkType = DefaultsNl.NONE_LINK;
                    }
                    else
                    {
                        // TODO: remove addition of "NL." once the xml standard has been updated
                        String id = linkTag.getID().contains(".") ? linkTag.getID() : "NL." + linkTag.getID();
                        linkType = Defaults.getByName(LinkType.class, id);
                        Throw.when(linkType == null, XmlParserException.class, "LinkType %s could not be found as default.",
                                linkTag.getID());
                    }
                }
                else if (linkTag.getPARENT() != null)
                {
                    LinkType parent = parsedDefinitions.get(LinkType.class, linkTag.getPARENT());
                    Throw.when(parent == null, XmlParserException.class, "LinkType %s parent %s not found", linkTag.getID(),
                            linkTag.getPARENT());
                    linkType = new LinkType(linkTag.getID(), parent);
                    CategoryLogger.filter(Cat.PARSER).trace("Added LinkType {}", linkType);
                }
                else
                {
                    linkType = new LinkType(linkTag.getID());
                    CategoryLogger.filter(Cat.PARSER).trace("Added LinkType {}", linkType);
                }
                parsedDefinitions.add(LinkType.class, linkType);

                for (COMPATIBILITY compTag : linkTag.getCOMPATIBILITY())
                {
                    // TODO: direction is ignored, NONE value erroneously results in accessibility
                    GtuType gtuType = parsedDefinitions.get(GtuType.class, compTag.getGTUTYPE());
                    Throw.when(gtuType == null, XmlParserException.class, "LinkType %s.compatibility: GtuType %s not found",
                            linkTag.getID(), compTag.getGTUTYPE());
                    linkType.addCompatibleGtuType(gtuType);
                }

                linkTypeSpeedLimitMap.put(linkType, new LinkedHashMap<>());
                for (SPEEDLIMIT speedLimitTag : linkTag.getSPEEDLIMIT())
                {
                    GtuType gtuType = parsedDefinitions.get(GtuType.class, speedLimitTag.getGTUTYPE());
                    linkTypeSpeedLimitMap.get(linkType).put(gtuType, speedLimitTag.getLEGALSPEEDLIMIT());
                }
            }
        }
    }

    /**
     * Parse the LANETYPES tag in the OTS XML file.
     * @param definitions the DEFINTIONS tag
     * @param parsedDefinitions Definitions; parsed definitions (definitions are stored in this)
     * @param overwriteDefaults overwrite default definitions in otsNetwork or not
     * @throws XmlParserException on parsing error
     */
    public static void parseLaneTypes(final DEFINITIONS definitions, final Definitions parsedDefinitions,
            final boolean overwriteDefaults) throws XmlParserException
    {
        for (LANETYPES laneTypes : ParseUtil.getObjectsOfType(definitions.getIncludeAndGTUTYPESAndGTUTEMPLATES(),
                LANETYPES.class))
        {
            for (LANETYPE laneTag : laneTypes.getLANETYPE())
            {
                LaneType laneType;
                if (laneTag.isDEFAULT())
                {
                    // TODO: remove addition of "NL." once the xml standard has been updated
                    String id = laneTag.getID().contains(".") ? laneTag.getID() : "NL." + laneTag.getID();
                    laneType = DefaultsRoad.getByName(LaneType.class, id);
                    Throw.when(laneType == null, XmlParserException.class, "LaneType %s could not be found as default.",
                            laneTag.getID());
                }
                else if (laneTag.getPARENT() != null)
                {
                    LaneType parent = parsedDefinitions.get(LaneType.class, laneTag.getPARENT());
                    Throw.when(parent == null, XmlParserException.class, "LaneType %s parent %s not found", laneTag.getID(),
                            laneTag.getPARENT());
                    laneType = new LaneType(laneTag.getID(), parent);
                    CategoryLogger.filter(Cat.PARSER).trace("Added LaneType {}", laneType);
                }
                else
                {
                    laneType = new LaneType(laneTag.getID());
                    CategoryLogger.filter(Cat.PARSER).trace("Added LaneType {}", laneType);
                }
                parsedDefinitions.add(LaneType.class, laneType);

                for (COMPATIBILITY compTag : laneTag.getCOMPATIBILITY())
                {
                    // TODO: direction is ignored, NONE value erroneously results in accessibility
                    GtuType gtuType = parsedDefinitions.get(GtuType.class, compTag.getGTUTYPE());
                    Throw.when(gtuType == null, XmlParserException.class, "LaneType %s.compatibility: GtuType %s not found",
                            laneTag.getID(), compTag.getGTUTYPE());
                    laneType.addCompatibleGtuType(gtuType);
                }
            }
        }
    }

    /**
     * Store the GTUTEMPLATE tags in the OTS XML file.
     * @param definitions the DEFINTIONS tag
     * @param parsedDefinitions Definitions; parsed definitions (definitions are stored in this)
     * @param overwriteDefaults overwrite default definitions in otsNetwork or not
     * @param gtuTemplates the templates to be used in the OD/Generators
     * @param streamInformation map with stream information
     * @throws XmlParserException on parsing error
     */
    public static void parseGtuTemplates(final DEFINITIONS definitions, final Definitions parsedDefinitions,
            final boolean overwriteDefaults, final Map<String, GTUTEMPLATE> gtuTemplates,
            final StreamInformation streamInformation) throws XmlParserException
    {
        for (GTUTEMPLATES templateTypes : ParseUtil.getObjectsOfType(definitions.getIncludeAndGTUTYPESAndGTUTEMPLATES(),
                GTUTEMPLATES.class))
        {
            for (GTUTEMPLATE templateTag : templateTypes.getGTUTEMPLATE())
            {
                GtuType gtuType = parsedDefinitions.get(GtuType.class, templateTag.getGTUTYPE());
                if (gtuType == null)
                {
                    throw new XmlParserException(
                            "GTUTemplate " + templateTag.getID() + " GtuType " + templateTag.getGTUTYPE() + " not found");
                }
                gtuTemplates.put(templateTag.getID(), templateTag);
            }
        }
    }

    /**
     * Parse the ROADLAYOUTS tag in the OTS XML file.
     * @param definitions the DEFINTIONS tag
     * @param parsedDefinitions Definitions; parsed definitions (definitions are stored in this)
     * @param roadLayoutMap temporary storage for the road layouts
     * @throws XmlParserException on parsing error
     */
    public static void parseRoadLayouts(final DEFINITIONS definitions, final Definitions parsedDefinitions,
            final Map<String, ROADLAYOUT> roadLayoutMap) throws XmlParserException
    {
        for (ROADLAYOUTS roadLayoutTypes : ParseUtil.getObjectsOfType(definitions.getIncludeAndGTUTYPESAndGTUTEMPLATES(),
                ROADLAYOUTS.class))
        {
            for (ROADLAYOUT layoutTag : roadLayoutTypes.getROADLAYOUT())
            {
                roadLayoutMap.put(layoutTag.getID(), layoutTag);
            }
        }
    }

    /**
     * Parse the PARAMETERTYPE tags in the OTS XML file.
     * @param definitions the DEFINTIONS tag
     * @param parameterMap map to store parameter type by id
     * @throws XmlParserException if the field in a PARAMETERTYPE does not refer to a ParameterType&lt;?&gt;
     */
    public static void parseParameterTypes(final DEFINITIONS definitions, final Map<String, ParameterType<?>> parameterMap)
            throws XmlParserException
    {
        for (PARAMETERTYPE parameterType : ParseUtil.getObjectsOfType(definitions.getIncludeAndGTUTYPESAndGTUTEMPLATES(),
                PARAMETERTYPE.class))
        {
            try
            {
                parameterMap.put(parameterType.getID(), (ParameterType<?>) parameterType.getFIELD());
            }
            catch (ClassCastException exception)
            {
                throw new XmlParserException("Parameter type with id " + parameterType.getID()
                        + " refers to a static field that is not a ParameterType<?>.");
            }
        }
    }

}
