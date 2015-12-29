package org.opentrafficsim.road.network.factory.xml;

import org.djunits.value.vdouble.scalar.Angle;
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
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jul 24, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
class ArcTag
{
    /** angle. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Angle.Abs angle = null;

    /** radius. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length.Rel radius = null;

    /** direction. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    ArcDirection direction = null;

    /** the center coordinate of the arc. Will be filled after parsing. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    OTSPoint3D center;

    /** the startAngle in radians compared to the center coordinate. Will be filled after parsing. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    double startAngle;

    /** direction of the arc; LEFT or RIGHT. */
    enum ArcDirection
    {
        /** Left = counter-clockwise. */
        LEFT,
        /** Right = clockwise. */
        RIGHT;
    }

    /**
     * Parse the LINK.ARC tag.
     * @param arcNode the XML-node to parse
     * @param parser the parser with the lists of information
     * @param linkTag the parent link tag
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseArc(final Node arcNode, final XmlNetworkLaneParser parser, final LinkTag linkTag)
        throws SAXException, NetworkException
    {
        NamedNodeMap arcAttributes = arcNode.getAttributes();
        linkTag.arcTag = new ArcTag();

        Node radius = arcAttributes.getNamedItem("RADIUS");
        if (radius == null)
            throw new SAXException("ARC: missing attribute RADIUS");
        linkTag.arcTag.radius = LengthUnits.parseLengthRel(radius.getNodeValue());

        Node angle = arcAttributes.getNamedItem("ANGLE");
        if (angle == null)
            throw new SAXException("ARC: missing attribute ANGLE");
        linkTag.arcTag.angle = AngleUnits.parseAngleAbs(angle.getNodeValue());

        Node dirNode = arcAttributes.getNamedItem("DIRECTION");
        if (dirNode == null)
            throw new SAXException("ARC: missing attribute DIRECTION");
        String dir = dirNode.getNodeValue().trim();
        linkTag.arcTag.direction =
            (dir.equals("L") || dir.equals("LEFT") || dir.equals("COUNTERCLOCKWISE")) ? ArcDirection.LEFT
                : ArcDirection.RIGHT;

    }
}
