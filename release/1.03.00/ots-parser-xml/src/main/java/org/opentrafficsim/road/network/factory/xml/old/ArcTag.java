package org.opentrafficsim.road.network.factory.xml.old;

import java.io.Serializable;

import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.factory.xml.units.AngleUnits;
import org.opentrafficsim.core.network.factory.xml.units.LengthUnits;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jul 24, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
class ArcTag implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150724L;

    /** Angle. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Direction angle = null;

    /** Radius. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length radius = null;

    /** Direction. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    ArcDirection direction = null;

    /** The center coordinate of the arc. Will be filled after parsing. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    OTSPoint3D center;

    /** The startAngle in radians compared to the center coordinate. Will be filled after parsing. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    double startAngle;

    /** Direction of the arc; LEFT or RIGHT. */
    enum ArcDirection
    {
        /** Left = counter-clockwise. */
        LEFT,
        /** Right = clockwise. */
        RIGHT;
    }

    /**
     * Parse the LINK.ARC tag.
     * @param arcNode Node; the XML-node to parse
     * @param parser XmlNetworkLaneParserOld; the parser with the lists of information
     * @param linkTag LinkTag; the parent link tag
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseArc(final Node arcNode, final XmlNetworkLaneParserOld parser, final LinkTag linkTag)
            throws SAXException, NetworkException
    {
        NamedNodeMap arcAttributes = arcNode.getAttributes();
        linkTag.arcTag = new ArcTag();

        Node radius = arcAttributes.getNamedItem("RADIUS");
        if (radius == null)
            throw new SAXException("ARC: missing attribute RADIUS");
        linkTag.arcTag.radius = LengthUnits.parseLength(radius.getNodeValue());

        Node angle = arcAttributes.getNamedItem("ANGLE");
        if (angle == null)
            throw new SAXException("ARC: missing attribute ANGLE");
        linkTag.arcTag.angle = AngleUnits.parseDirection(angle.getNodeValue());

        Node dirNode = arcAttributes.getNamedItem("DIRECTION");
        if (dirNode == null)
            throw new SAXException("ARC: missing attribute DIRECTION");
        String dir = dirNode.getNodeValue().trim();
        linkTag.arcTag.direction = (dir.equals("L") || dir.equals("LEFT") || dir.equals("COUNTERCLOCKWISE")) ? ArcDirection.LEFT
                : ArcDirection.RIGHT;

    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "ArcTag [angle=" + this.angle + ", radius=" + this.radius + ", direction=" + this.direction + ", center="
                + this.center + ", startAngle=" + this.startAngle + "]";
    }
}
