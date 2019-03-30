package org.opentrafficsim.road.network.factory.xml.parser;

import java.util.Map;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.logger.CategoryLogger;
import org.opentrafficsim.base.logger.Cat;
import org.opentrafficsim.core.compatibility.GTUCompatibility;
import org.opentrafficsim.core.distributions.Generator;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.TemplateGTUType;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.factory.xml.XmlParserException;
import org.opentrafficsim.road.network.factory.xml.utils.Generators;
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
import org.opentrafficsim.xml.generated.ROADLAYOUT;
import org.opentrafficsim.xml.generated.ROADLAYOUTS;
import org.opentrafficsim.xml.generated.RUN;

import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * RunParser parses the XML nodes of the RUN tag, including the RANDOMSTREAM tags. <br>
 * <br>
 * Copyright (c) 2003-2018 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public final class RunParser
{
    /** */
    private RunParser()
    {
        // utility class
    }

    /**
     * Parse the RUN tag in the OTS XML file.
     * @param run the RUN tag
     * @param otsNetwork the network
     * @param streamMap the map with the random streams to be filled
     * @throws XmlParserException on parsing error
     */
    public static void parseRun(final OTSRoadNetwork otsNetwork, final RUN run, final Map<String, StreamInformation> streamMap)
            throws XmlParserException
    {
    }

    /**
     * Parse the GTUTYPES tag in the OTS XML file.
     * @param definitions the DEFINTIONS tag
     * @param otsNetwork the network
     * @param overwriteDefaults overwrite default definitions in otsNetwork or not
     * @throws XmlParserException on parsing error
     */
    public static void parseGtuTypes(final DEFINITIONS definitions, final OTSRoadNetwork otsNetwork,
            final boolean overwriteDefaults) throws XmlParserException
    {
        for (GTUTYPES gtuTypes : ParseUtil.getObjectsOfType(definitions.getIncludeAndGTUTYPESAndGTUTEMPLATES(), GTUTYPES.class))
        {
            for (GTUTYPE gtuTag : gtuTypes.getGTUTYPE())
            {
                GTUType networkGtuType = otsNetwork.getGtuTypes().get(gtuTag.getID());
                if (networkGtuType == null || (networkGtuType != null && !gtuTag.isDEFAULT())
                        || (networkGtuType != null && gtuTag.isDEFAULT() && overwriteDefaults))
                {
                    if (gtuTag.getPARENT() != null)
                    {
                        GTUType parent = otsNetwork.getGtuType(gtuTag.getPARENT());
                        if (parent == null)
                        {
                            throw new XmlParserException(
                                    "GTUType " + gtuTag.getID() + " parent " + gtuTag.getPARENT() + " not found");
                        }
                        GTUType gtuType = new GTUType(gtuTag.getID(), parent);
                        CategoryLogger.filter(Cat.PARSER).trace("Added GTUType {}", gtuType);
                    }
                    else
                    {
                        GTUType gtuType = new GTUType(gtuTag.getID(), otsNetwork);
                        CategoryLogger.filter(Cat.PARSER).trace("Added GTUType {}", gtuType);
                    }
                }
                else
                    CategoryLogger.filter(Cat.PARSER).trace("Did NOT add GTUType {}", gtuTag.getID());
            }
        }
    }

    /**
     * Parse the LINKTYPES tag in the OTS XML file.
     * @param definitions the DEFINTIONS tag
     * @param otsNetwork the network
     * @param overwriteDefaults overwrite default definitions in otsNetwork or not
     * @throws XmlParserException on parsing error
     */
    public static void parseLinkTypes(final DEFINITIONS definitions, final OTSRoadNetwork otsNetwork,
            final boolean overwriteDefaults) throws XmlParserException
    {
        for (LINKTYPES linkTypes : ParseUtil.getObjectsOfType(definitions.getIncludeAndGTUTYPESAndGTUTEMPLATES(), LINKTYPES.class))
        {
            for (LINKTYPE linkTag : linkTypes.getLINKTYPE())
            {
                LinkType networkLinkType = otsNetwork.getLinkTypes().get(linkTag.getID());
                if (networkLinkType == null || (networkLinkType != null && !linkTag.isDEFAULT())
                        || (networkLinkType != null && linkTag.isDEFAULT() && overwriteDefaults))
                {
                    GTUCompatibility<LinkType> compatibility = new GTUCompatibility<>((LinkType) null);
                    for (COMPATIBILITY compTag : linkTag.getCOMPATIBILITY())
                    {
                        GTUType gtuType = otsNetwork.getGtuType(compTag.getGTUTYPE());
                        if (gtuType == null)
                        {
                            throw new XmlParserException("LinkType " + linkTag.getID() + ".compatibility: GTUType "
                                    + compTag.getGTUTYPE() + " not found");
                        }
                        compatibility.addAllowedGTUType(gtuType,
                                LongitudinalDirectionality.valueOf(compTag.getDIRECTION().toString()));
                    }
                    LinkType parent = otsNetwork.getLinkType(linkTag.getPARENT());
                    LinkType linkType = new LinkType(linkTag.getID(), parent, compatibility, otsNetwork);
                    CategoryLogger.filter(Cat.PARSER).trace("Added LinkType {}", linkType);
                }
                else
                    CategoryLogger.filter(Cat.PARSER).trace("Did NOT add LinkType {}", linkTag.getID());
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
    public static void parseLaneTypes(final DEFINITIONS definitions, final OTSRoadNetwork otsNetwork,
            final boolean overwriteDefaults) throws XmlParserException
    {
        for (LANETYPES laneTypes : ParseUtil.getObjectsOfType(definitions.getIncludeAndGTUTYPESAndGTUTEMPLATES(), LANETYPES.class))
        {
            for (LANETYPE laneTag : laneTypes.getLANETYPE())
            {
                LaneType networkLaneType = otsNetwork.getLaneTypes().get(laneTag.getID());
                if (networkLaneType == null || (networkLaneType != null && !laneTag.isDEFAULT())
                        || (networkLaneType != null && laneTag.isDEFAULT() && overwriteDefaults))
                {
                    GTUCompatibility<LaneType> compatibility = new GTUCompatibility<>((LaneType) null);
                    for (COMPATIBILITY compTag : laneTag.getCOMPATIBILITY())
                    {
                        GTUType gtuType = otsNetwork.getGtuType(compTag.getGTUTYPE());
                        if (gtuType == null)
                        {
                            throw new XmlParserException("LaneType " + laneTag.getID() + ".compatibility: GTUType "
                                    + compTag.getGTUTYPE() + " not found");
                        }
                        compatibility.addAllowedGTUType(gtuType,
                                LongitudinalDirectionality.valueOf(compTag.getDIRECTION().toString()));
                    }
                    if (laneTag.getPARENT() != null)
                    {
                        LaneType parent = otsNetwork.getLaneType(laneTag.getPARENT());
                        if (parent == null)
                        {
                            throw new XmlParserException(
                                    "LaneType " + laneTag.getID() + " parent " + laneTag.getPARENT() + " not found");
                        }
                        LaneType laneType = new LaneType(laneTag.getID(), parent, compatibility, otsNetwork);
                        CategoryLogger.filter(Cat.PARSER).trace("Added LaneType {}", laneType);
                    }
                    else
                    {
                        LaneType laneType = new LaneType(laneTag.getID(), compatibility, otsNetwork);
                        CategoryLogger.filter(Cat.PARSER).trace("Added LaneType {}", laneType);
                    }
                }
                else
                    CategoryLogger.filter(Cat.PARSER).trace("Did NOT add LaneType {}", laneTag.getID());
            }
        }
    }

    /**
     * Parse the GTUTEMPLATES tag in the OTS XML file.
     * @param definitions the DEFINTIONS tag
     * @param otsNetwork the network
     * @param overwriteDefaults overwrite default definitions in otsNetwork or not
     * @param gtuTemplates the templates to be used in the OD/Generators
     * @throws XmlParserException on parsing error
     */
    public static void parseGtuTemplates(final DEFINITIONS definitions, final OTSRoadNetwork otsNetwork,
            final boolean overwriteDefaults, Map<GTUType, TemplateGTUType> gtuTemplates) throws XmlParserException
    {
        // TODO: use random streams from the RUN tag, and access and parse them in the distributions
        StreamInterface stream = new MersenneTwister(2L);
        for (GTUTEMPLATES templateTypes : ParseUtil.getObjectsOfType(definitions.getIncludeAndGTUTYPESAndGTUTEMPLATES(),
                GTUTEMPLATES.class))
        {
            for (GTUTEMPLATE templateTag : templateTypes.getGTUTEMPLATE())
            {
                GTUType gtuType = otsNetwork.getGtuType(templateTag.getGTUTYPE());
                if (gtuType == null)
                {
                    throw new XmlParserException(
                            "GTUTemplate " + templateTag.getID() + " GTUType " + templateTag.getGTUTYPE() + " not found");
                }
                TemplateGTUType existingTemplate = gtuTemplates.get(gtuType);
                if (existingTemplate == null || (existingTemplate != null && !templateTag.isDEFAULT())
                        || (existingTemplate != null && templateTag.isDEFAULT() && overwriteDefaults))
                {
                    Generator<Length> lengthGenerator = Generators.makeLengthGenerator(stream, templateTag.getLENGTHDIST());
                    Generator<Length> widthGenerator = Generators.makeLengthGenerator(stream, templateTag.getWIDTHDIST());
                    Generator<Speed> maximumSpeedGenerator =
                            Generators.makeSpeedGenerator(stream, templateTag.getMAXSPEEDDIST());
                    Generator<Acceleration> maximumAccelerationGenerator =
                            Generators.makeAccelerationGenerator(stream, templateTag.getMAXACCELERATIONDIST());
                    Generator<Acceleration> maximumDecelerationGenerator =
                            Generators.makeDecelerationGenerator(stream, templateTag.getMAXDECELERATIONDIST());
                    TemplateGTUType templateGTUType = new TemplateGTUType(gtuType, lengthGenerator, widthGenerator,
                            maximumSpeedGenerator, maximumAccelerationGenerator, maximumDecelerationGenerator);
                    gtuTemplates.put(gtuType, templateGTUType);
                    CategoryLogger.filter(Cat.PARSER).trace("Added TemplateGTUType {}", templateGTUType);
                }
                else
                    CategoryLogger.filter(Cat.PARSER).trace("Did NOT add TemplateGTUType {}", templateTag.getID());
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
    public static void parseRoadLayouts(final DEFINITIONS definitions, final OTSRoadNetwork otsNetwork,
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

}
