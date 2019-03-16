package org.opentrafficsim.road.network.factory.xml.old;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.factory.xml.units.Distributions;
import org.opentrafficsim.core.units.distributions.ContinuousDistDoubleScalar;
import org.opentrafficsim.road.network.factory.xml.old.CrossSectionElementTag.ElementType;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
class ListGeneratorTag implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150723L;

    /** URI of the list. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    URI uri = null;

    /** Position of the sink on the link, relative to the design line, stored as a string to parse when the length is known. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String positionStr = null;

    /** GTU tag. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    GTUTag gtuTag = null;

    /** GTU mix tag. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    GTUMixTag gtuMixTag = null;

    /** Initial speed. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> initialSpeedDist = null;

    /** GTU colorer. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String gtuColorer;

    /**
     * Parse the LISTGENERATOR tag.
     * @param node Node; the LISTGENERATOR node to parse
     * @param parser XmlNetworkLaneParserOld; the parser with the lists of information
     * @param linkTag LinkTag; the parent LINK tag
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseListGenerator(final Node node, final XmlNetworkLaneParserOld parser, final LinkTag linkTag)
            throws SAXException, NetworkException
    {
        NamedNodeMap attributes = node.getAttributes();
        ListGeneratorTag listGeneratorTag = new ListGeneratorTag();

        if (attributes.getNamedItem("URI") == null)
            throw new SAXException("LISTGENERATOR: missing attribute URI" + " for link " + linkTag.name);
        String uriStr = attributes.getNamedItem("URI").getNodeValue().trim();
        try
        {
            listGeneratorTag.uri = new URI(uriStr);
        }
        catch (URISyntaxException exception)
        {
            throw new NetworkException("LISTGENERATOR: URI " + uriStr + " is not valid", exception);
        }

        if (attributes.getNamedItem("LANE") == null)
            throw new SAXException("LISTGENERATOR: missing attribute LANE" + " for link " + linkTag.name);
        String laneName = attributes.getNamedItem("LANE").getNodeValue().trim();
        if (linkTag.roadLayoutTag == null)
            throw new NetworkException("LISTGENERATOR: LANE " + laneName + " no ROADTYPE for link " + linkTag.name);
        CrossSectionElementTag cseTag = linkTag.roadLayoutTag.cseTags.get(laneName);
        if (cseTag == null)
            throw new NetworkException("LISTGENERATOR: LANE " + laneName + " not found in elements of link " + linkTag.name
                    + " - roadtype " + linkTag.roadLayoutTag.name);
        if (cseTag.elementType != ElementType.LANE)
            throw new NetworkException("LISTGENERATOR: LANE " + laneName + " not a real GTU lane for link " + linkTag.name
                    + " - roadtype " + linkTag.roadLayoutTag.name);
        if (linkTag.generatorTags.containsKey(laneName))
            throw new SAXException("LISTGENERATOR for LANE with NAME " + laneName + " defined twice");

        Node position = attributes.getNamedItem("POSITION");
        if (position == null)
            throw new NetworkException("LISTGENERATOR: POSITION element not found in elements of link " + linkTag.name
                    + " - roadtype " + linkTag.roadLayoutTag.name);
        listGeneratorTag.positionStr = position.getNodeValue().trim();

        if (attributes.getNamedItem("GTU") != null)
        {
            String gtuName = attributes.getNamedItem("GTU").getNodeValue().trim();
            if (!parser.gtuTags.containsKey(gtuName))
                throw new NetworkException(
                        "LISTGENERATOR: LANE " + laneName + " GTU " + gtuName + " in link " + linkTag.name + " not defined");
            listGeneratorTag.gtuTag = parser.gtuTags.get(gtuName);
        }

        if (attributes.getNamedItem("GTUMIX") != null)
        {
            String gtuMixName = attributes.getNamedItem("GTUMIX").getNodeValue().trim();
            if (!parser.gtuMixTags.containsKey(gtuMixName))
                throw new NetworkException("LISTGENERATOR: LANE " + laneName + " GTUMIX " + gtuMixName + " in link "
                        + linkTag.name + " not defined");
            listGeneratorTag.gtuMixTag = parser.gtuMixTags.get(gtuMixName);
        }

        if (listGeneratorTag.gtuTag == null && listGeneratorTag.gtuMixTag == null)
            throw new SAXException("LISTGENERATOR: missing attribute GTU or GTUMIX for Lane with NAME " + laneName + " of link "
                    + linkTag.name);

        if (listGeneratorTag.gtuTag != null && listGeneratorTag.gtuMixTag != null)
            throw new SAXException("LISTGENERATOR: both attribute GTU and GTUMIX defined for Lane with NAME " + laneName
                    + " of link " + linkTag.name);

        Node initialSpeed = attributes.getNamedItem("INITIALSPEED");
        if (initialSpeed == null)
            throw new SAXException("LISTGENERATOR: missing attribute INITIALSPEED");
        listGeneratorTag.initialSpeedDist = Distributions.parseSpeedDist(initialSpeed.getNodeValue());

        // TODO GTUColorer

        linkTag.listGeneratorTags.put(laneName, listGeneratorTag);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "ListGeneratorTag [uri=" + this.uri + ", positionStr=" + this.positionStr + ", gtuTag=" + this.gtuTag
                + ", gtuMixTag=" + this.gtuMixTag + ", initialSpeedDist=" + this.initialSpeedDist + ", gtuColorer="
                + this.gtuColorer + "]";
    }
}
