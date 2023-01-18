package org.opentrafficsim.road.network.factory.xml.parser;

import java.util.LinkedHashMap;
import java.util.Map;

import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.logger.CategoryLogger;
import org.opentrafficsim.base.logger.Cat;
import org.opentrafficsim.base.parameters.ParameterType;
import org.opentrafficsim.core.definitions.Defaults;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.road.network.OtsRoadNetwork;
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
     * @param otsNetwork the network
     * @param definitions the DEFINTIONS tag
     * @param overwriteDefaults overwrite default definitions in otsNetwork or not
     * @param roadLayoutMap temporary storage for the road layouts
     * @param gtuTemplates map of GTU templates for the OD and/or Generators
     * @param streamInformation map with stream information
     * @param linkTypeSpeedLimitMap map with speed limit information per link type
     * @throws XmlParserException on parsing error
     */
    public static void parseDefinitions(final OtsRoadNetwork otsNetwork, final DEFINITIONS definitions,
            final boolean overwriteDefaults, final Map<String, ROADLAYOUT> roadLayoutMap,
            final Map<String, GTUTEMPLATE> gtuTemplates, final StreamInformation streamInformation,
            final Map<LinkType, Map<GtuType, Speed>> linkTypeSpeedLimitMap) throws XmlParserException
    {
        parseGtuTypes(definitions, otsNetwork);
        parseLinkTypes(definitions, otsNetwork, overwriteDefaults, linkTypeSpeedLimitMap);
        parseLaneTypes(definitions, otsNetwork, overwriteDefaults);
        parseGtuTemplates(definitions, otsNetwork, overwriteDefaults, gtuTemplates, streamInformation);
        parseRoadLayouts(definitions, otsNetwork, roadLayoutMap);
    }

    /**
     * Parse the GTUTYPES tag in the OTS XML file.
     * @param definitions the DEFINTIONS tag
     * @param otsNetwork the network
     * @throws XmlParserException on parsing error
     */
    public static void parseGtuTypes(final DEFINITIONS definitions, final OtsRoadNetwork otsNetwork) throws XmlParserException
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
                }
                else if (gtuTag.getPARENT() != null)
                {
                    GtuType parent = otsNetwork.getGtuType(gtuTag.getPARENT());
                    if (parent == null)
                    {
                        throw new XmlParserException(
                                "GtuType " + gtuTag.getID() + " parent " + gtuTag.getPARENT() + " not found");
                    }
                    gtuType = new GtuType(gtuTag.getID(), parent);
                    CategoryLogger.filter(Cat.PARSER).trace("Added GtuType {}", gtuType);
                }
                else
                {
                    gtuType = new GtuType(gtuTag.getID());
                    CategoryLogger.filter(Cat.PARSER).trace("Added GtuType {}", gtuType);
                }
                otsNetwork.addGtuType(gtuType);
            }
        }
    }

    /**
     * Parse the LINKTYPES tag in the OTS XML file.
     * @param definitions the DEFINTIONS tag
     * @param otsNetwork the network
     * @param overwriteDefaults overwrite default definitions in otsNetwork or not
     * @param linkTypeSpeedLimitMap map with speed limit information per link type
     * @throws XmlParserException on parsing error
     */
    public static void parseLinkTypes(final DEFINITIONS definitions, final OtsRoadNetwork otsNetwork,
            final boolean overwriteDefaults, final Map<LinkType, Map<GtuType, Speed>> linkTypeSpeedLimitMap)
            throws XmlParserException
    {
        for (LINKTYPES linkTypes : ParseUtil.getObjectsOfType(definitions.getIncludeAndGTUTYPESAndGTUTEMPLATES(),
                LINKTYPES.class))
        {
            for (LINKTYPE linkTag : linkTypes.getLINKTYPE())
            {
                LinkType networkLinkType = otsNetwork.getLinkTypes().get(linkTag.getID());
                if (networkLinkType == null || (networkLinkType != null && !linkTag.isDEFAULT())
                        || (networkLinkType != null && linkTag.isDEFAULT() && overwriteDefaults))
                {
                    LinkType parent = otsNetwork.getLinkType(linkTag.getPARENT());
                    LinkType linkType = new LinkType(linkTag.getID(), parent, otsNetwork);
                    for (COMPATIBILITY compTag : linkTag.getCOMPATIBILITY())
                    {
                        GtuType gtuType = otsNetwork.getGtuType(compTag.getGTUTYPE());
                        if (gtuType == null)
                        {
                            throw new XmlParserException("LinkType " + linkTag.getID() + ".compatibility: GtuType "
                                    + compTag.getGTUTYPE() + " not found");
                        }
                        linkType.addCompatibleGtuType(gtuType);
                    }
                    networkLinkType = linkType;
                    CategoryLogger.filter(Cat.PARSER).trace("Added LinkType {}", linkType);
                }
                else
                {
                    CategoryLogger.filter(Cat.PARSER).trace("Did NOT add LinkType {}", linkTag.getID());
                }

                linkTypeSpeedLimitMap.put(networkLinkType, new LinkedHashMap<>());
                for (SPEEDLIMIT speedLimitTag : linkTag.getSPEEDLIMIT())
                {
                    GtuType gtuType = otsNetwork.getGtuType(speedLimitTag.getGTUTYPE());
                    linkTypeSpeedLimitMap.get(networkLinkType).put(gtuType, speedLimitTag.getLEGALSPEEDLIMIT());
                }
            }
        }
    }

    /**
     * Parse the LANETYPES tag in the OTS XML file.
     * @param definitions the DEFINTIONS tag
     * @param otsNetwork the network
     * @param overwriteDefaults overwrite default definitions in otsNetwork or not
     * @throws XmlParserException on parsing error
     */
    public static void parseLaneTypes(final DEFINITIONS definitions, final OtsRoadNetwork otsNetwork,
            final boolean overwriteDefaults) throws XmlParserException
    {
        for (LANETYPES laneTypes : ParseUtil.getObjectsOfType(definitions.getIncludeAndGTUTYPESAndGTUTEMPLATES(),
                LANETYPES.class))
        {
            for (LANETYPE laneTag : laneTypes.getLANETYPE())
            {
                LaneType networkLaneType = otsNetwork.getLaneTypes().get(laneTag.getID());
                if (networkLaneType == null || (networkLaneType != null && !laneTag.isDEFAULT())
                        || (networkLaneType != null && laneTag.isDEFAULT() && overwriteDefaults))
                {
                    LaneType laneType;
                    if (laneTag.getPARENT() != null)
                    {
                        LaneType parent = otsNetwork.getLaneType(laneTag.getPARENT());
                        if (parent == null)
                        {
                            throw new XmlParserException(
                                    "LaneType " + laneTag.getID() + " parent " + laneTag.getPARENT() + " not found");
                        }
                        laneType = new LaneType(laneTag.getID(), parent, otsNetwork);
                        CategoryLogger.filter(Cat.PARSER).trace("Added LaneType {}", laneType);
                    }
                    else
                    {
                        laneType = new LaneType(laneTag.getID(), otsNetwork);
                        CategoryLogger.filter(Cat.PARSER).trace("Added LaneType {}", laneType);
                    }
                    for (COMPATIBILITY compTag : laneTag.getCOMPATIBILITY())
                    {
                        GtuType gtuType = otsNetwork.getGtuType(compTag.getGTUTYPE());
                        if (gtuType == null)
                        {
                            throw new XmlParserException("LaneType " + laneTag.getID() + ".compatibility: GtuType "
                                    + compTag.getGTUTYPE() + " not found");
                        }
                        laneType.addCompatibleGtuType(gtuType);
                    }
                }
                else
                {
                    CategoryLogger.filter(Cat.PARSER).trace("Did NOT add LaneType {}", laneTag.getID());
                }
            }
        }
    }

    /**
     * Store the GTUTEMPLATE tags in the OTS XML file.
     * @param definitions the DEFINTIONS tag
     * @param otsNetwork the network
     * @param overwriteDefaults overwrite default definitions in otsNetwork or not
     * @param gtuTemplates the templates to be used in the OD/Generators
     * @param streamInformation map with stream information
     * @throws XmlParserException on parsing error
     */
    public static void parseGtuTemplates(final DEFINITIONS definitions, final OtsRoadNetwork otsNetwork,
            final boolean overwriteDefaults, final Map<String, GTUTEMPLATE> gtuTemplates,
            final StreamInformation streamInformation) throws XmlParserException
    {
        for (GTUTEMPLATES templateTypes : ParseUtil.getObjectsOfType(definitions.getIncludeAndGTUTYPESAndGTUTEMPLATES(),
                GTUTEMPLATES.class))
        {
            for (GTUTEMPLATE templateTag : templateTypes.getGTUTEMPLATE())
            {
                GtuType gtuType = otsNetwork.getGtuType(templateTag.getGTUTYPE());
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
     * @param otsNetwork the network
     * @param roadLayoutMap temporary storage for the road layouts
     * @throws XmlParserException on parsing error
     */
    public static void parseRoadLayouts(final DEFINITIONS definitions, final OtsRoadNetwork otsNetwork,
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
     * @param otsNetwork the network
     * @param parameterMap map to store parameter type by id
     * @throws XmlParserException if the field in a PARAMETERTYPE does not refer to a ParameterType&lt;?&gt;
     */
    public static void parseParameterTypes(final DEFINITIONS definitions, final OtsRoadNetwork otsNetwork,
            final Map<String, ParameterType<?>> parameterMap) throws XmlParserException
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
