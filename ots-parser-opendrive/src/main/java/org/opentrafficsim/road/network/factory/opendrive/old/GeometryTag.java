package org.opentrafficsim.road.network.factory.opendrive.old;

import java.io.Serializable;
import java.util.UUID;

import org.djunits.unit.AngleUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Angle;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.road.network.lane.OTSRoadNode;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Parser for geometry tag.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
class GeometryTag implements Serializable
{

    /** */
    private static final long serialVersionUID = 20150723L;

    /** Sequence of the geometry. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String id = null;

    /** Start position (s-coordinate). */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length s = null;

    /** The x position (s-coordinate). */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length x = null;

    /** The y position (s-coordinate). */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length y = null;

    /** The z position (s-coordinate). */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length z = null;

    /** The hdg position (s-coordinate). */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Angle hdg = null;

    /** Total length of the reference line in the xy-plane, as indicated in the XML document. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length length = null;

    /** SpiralTag. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    SpiralTag spiralTag = null;

    /** ArcTag. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    ArcTag arcTag = null;

    /** The calculated Node, either through a coordinate or after calculation. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    OTSRoadNode node = null;

    OTSLine3D interLine = null;

    /**
     * Parse the attributes of the road tag. The sub-elements are parsed in separate classes.
     * @param node Node; the top-level road node
     * @param parser OpenDriveNetworkLaneParser; the parser with the lists of information
     * @return the generated RoadTag for further reference
     * @throws SAXException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static GeometryTag parseGeometry(final Node node, final OpenDriveNetworkLaneParserOld parser) throws SAXException
    {
        NamedNodeMap attributes = node.getAttributes();
        GeometryTag geometryTag = new GeometryTag();

        Node s = attributes.getNamedItem("s");
        if (s == null)
            throw new SAXException("Geometry: missing attribute s");
        geometryTag.s = new Length(Double.parseDouble(s.getNodeValue().trim()), LengthUnit.METER);

        Node x = attributes.getNamedItem("x");
        if (x == null)
            throw new SAXException("Geometry: missing attribute x");
        geometryTag.x = new Length(Double.parseDouble(x.getNodeValue().trim()), LengthUnit.METER);

        Node y = attributes.getNamedItem("y");
        if (y == null)
            throw new SAXException("Geometry: missing attribute y");
        geometryTag.y = new Length(Double.parseDouble(y.getNodeValue().trim()), LengthUnit.METER);

        Node hdg = attributes.getNamedItem("hdg");
        if (hdg == null)
            throw new SAXException("Geometry: missing attribute hdg");
        geometryTag.hdg = new Angle(Double.parseDouble(hdg.getNodeValue().trim()), AngleUnit.RADIAN);

        Node length = attributes.getNamedItem("length");
        if (length == null)
            throw new SAXException("Geometry: missing attribute length");
        geometryTag.length = new Length(Double.parseDouble(length.getNodeValue().trim()), LengthUnit.METER);

        SpiralTag.parseSpiral(node.getChildNodes(), parser, geometryTag);

        ArcTag.parseArc(node.getChildNodes(), parser, geometryTag);

        return geometryTag;
    }

    /**
     * @param network Network; the network
     * @param geometryTag GeometryTag; the tag with the info for the node.
     * @return a constructed node
     * @throws NetworkException if node already exists in the network, or if name of the node is not unique.
     */
    static OTSNode makeOTSNode(final Network network, final GeometryTag geometryTag) throws NetworkException
    {
        OTSPoint3D coordinate =
                new OTSPoint3D(geometryTag.x.doubleValue(), geometryTag.y.doubleValue(), geometryTag.z.doubleValue());

        if (geometryTag.id == null)
        {
            geometryTag.id = UUID.randomUUID().toString();
        }

        OTSRoadNode node = new OTSRoadNode(network, geometryTag.id, coordinate, Direction.instantiateSI(Double.NaN));
        geometryTag.node = node;
        return node;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "GeometryTag [id=" + this.id + ", s=" + this.s + ", x=" + this.x + ", y=" + this.y + ", z=" + this.z + ", hdg="
                + this.hdg + ", length=" + this.length + ", spiralTag=" + this.spiralTag + ", arcTag=" + this.arcTag + ", node="
                + this.node + ", interLine=" + this.interLine + "]";
    }
}
