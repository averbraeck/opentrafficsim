package org.opentrafficsim.road.network.factory.opendrive.old;

import java.io.Serializable;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.network.NetworkException;
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
class SuperElevationTag implements Serializable
{

    /** */
    private static final long serialVersionUID = 20150723L;

    /** Sequence of the geometry. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    int id = 0;

    /** Start position (s-coordinate). */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length s = null;

    /** The a position (s-coordinate). */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length a = null;

    /** The b position (s-coordinate). */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length b = null;

    /** The c position (s-coordinate). */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length c = null;

    /** The d position (s-coordinate) */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length d = null;

    /**
     * Parse the attributes of the road tag. The sub-elements are parsed in separate classes.
     * @param node Node; the top-level road node
     * @param parser OpenDriveNetworkLaneParser; the parser with the lists of information
     * @return the generated RoadTag for further reference
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static SuperElevationTag parseSuperElevation(final Node node, final OpenDriveNetworkLaneParserOld parser)
            throws SAXException, NetworkException
    {
        NamedNodeMap attributes = node.getAttributes();
        SuperElevationTag elevationTag = new SuperElevationTag();

        Node s = attributes.getNamedItem("s");
        if (s == null)
            throw new SAXException("Geometry: missing attribute s");
        elevationTag.s = new Length(Double.parseDouble(s.getNodeValue().trim()), LengthUnit.METER);

        Node a = attributes.getNamedItem("a");
        if (a == null)
            throw new SAXException("Geometry: missing attribute a");
        elevationTag.a = new Length(Double.parseDouble(a.getNodeValue().trim()), LengthUnit.METER);

        Node b = attributes.getNamedItem("b");
        if (b == null)
            throw new SAXException("Geometry: missing attribute b");
        elevationTag.b = new Length(Double.parseDouble(b.getNodeValue().trim()), LengthUnit.METER);

        Node c = attributes.getNamedItem("c");
        if (c == null)
            throw new SAXException("Geometry: missing attribute c");
        elevationTag.c = new Length(Double.parseDouble(c.getNodeValue().trim()), LengthUnit.METER);

        Node d = attributes.getNamedItem("d");
        if (d == null)
            throw new SAXException("Geometry: missing attribute d");
        elevationTag.d = new Length(Double.parseDouble(d.getNodeValue().trim()), LengthUnit.METER);

        return elevationTag;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "SuperElevationTag [id=" + this.id + ", s=" + this.s + ", a=" + this.a + ", b=" + this.b + ", c=" + this.c
                + ", d=" + this.d + "]";
    }
}
