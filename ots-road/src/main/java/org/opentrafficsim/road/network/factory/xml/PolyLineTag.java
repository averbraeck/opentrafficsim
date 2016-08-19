package org.opentrafficsim.road.network.factory.xml;

import java.io.Serializable;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.factory.xml.units.Coordinates;
import org.opentrafficsim.core.network.factory.xml.units.LengthUnits;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Jul 24, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
class PolyLineTag implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150724L;

    /** Length. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length length = null;

    OTSPoint3D[] coordinates = null;

    /**
     * Parse the LINK.POLYLINE tag.
     * @param polyLine the XML-node to parse
     * @param parser the parser with the lists of information
     * @param linkTag the parent link tag
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parsePolyLine(final Node polyLine, final XmlNetworkLaneParser parser, final LinkTag linkTag)
            throws SAXException, NetworkException
    {
        NamedNodeMap polyLineAttributes = polyLine.getAttributes();
        linkTag.polyLineTag = new PolyLineTag();

        if (polyLineAttributes.getNamedItem("INTERMEDIATEPOINTS") != null)
            linkTag.polyLineTag.coordinates =
                    Coordinates.parseCoordinates(polyLineAttributes.getNamedItem("INTERMEDIATEPOINTS").getNodeValue());
        if (polyLineAttributes.getNamedItem("LENGTH") != null)
            linkTag.polyLineTag.length = LengthUnits.parseLengthRel(polyLineAttributes.getNamedItem("LENGTH").getNodeValue());

    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "PolyLineTag [length=" + this.length + "]";
    }
}
