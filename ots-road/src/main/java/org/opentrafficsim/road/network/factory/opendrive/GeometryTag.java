package org.opentrafficsim.road.network.factory.opendrive;

import org.djunits.unit.AnglePlaneUnit;
import org.djunits.unit.AngleSlopeUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.AnglePlane;
import org.djunits.value.vdouble.scalar.AngleSlope;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNode;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
class GeometryTag 
{

    /** sequence of the geometry. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String id = null;
    
    /** start position (s-coordinate). */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length.Rel s = null;
    
    /** x position (s-coordinate). */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length.Rel x = null;
    
    /** y position (s-coordinate). */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length.Rel y = null;
    
    /** hdg position (s-coordinate). */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length.Rel hdg = null;

    /** total length of the reference line in the xy-plane, as indicated in the XML document. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length.Rel length = null;
    
    /** spiralTag */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    SpiralTag spiralTag = null;
    
    /** arcTag */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    ArcTag arcTag = null;

    /** the calculated Node, either through a coordinate or after calculation. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    OTSNode node = null;

    /**
     * Parse the attributes of the road tag. The sub-elements are parsed in separate classes.
     * @param node the top-level road node
     * @param parser the parser with the lists of information
     * @return the generated RoadTag for further reference
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static GeometryTag parseGeometry(final Node node, final OpenDriveNetworkLaneParser parser) throws SAXException, NetworkException
    {
        NamedNodeMap attributes = node.getAttributes();
        GeometryTag geometryTag = new GeometryTag();

        Node s = attributes.getNamedItem("s");
        if (s == null)
            throw new SAXException("Geometry: missing attribute s");
        geometryTag.s = new Length.Rel(Double.parseDouble(s.getNodeValue().trim()), LengthUnit.METER);

        Node x = attributes.getNamedItem("x");
        if (x == null)
            throw new SAXException("Geometry: missing attribute x");
        geometryTag.x = new Length.Rel(Double.parseDouble(x.getNodeValue().trim()), LengthUnit.METER);
        
        Node y = attributes.getNamedItem("y");
        if (y == null)
            throw new SAXException("Geometry: missing attribute y");
        geometryTag.y = new Length.Rel(Double.parseDouble(y.getNodeValue().trim()), LengthUnit.METER);
        
        Node hdg = attributes.getNamedItem("hdg");
        if (hdg == null)
            throw new SAXException("Geometry: missing attribute hdg");
        geometryTag.hdg = new Length.Rel(Double.parseDouble(hdg.getNodeValue().trim()), LengthUnit.METER);
        
        Node length = attributes.getNamedItem("length");
        if (length == null)
            throw new SAXException("Geometry: missing attribute length");
        geometryTag.length = new Length.Rel(Double.parseDouble(length.getNodeValue().trim()), LengthUnit.METER);
        
        SpiralTag.parseSpiral(node.getChildNodes(), parser, geometryTag);
        
        ArcTag.parseArc(node.getChildNodes(), parser, geometryTag);
        
        makeOTSNode(geometryTag);

        return geometryTag;
    }
    
    /**
     * @param geometryTag the tag with the info for the node.
     * @return a constructed node
     */
    static OTSNode makeOTSNode(final GeometryTag geometryTag)
    {
        AnglePlane.Abs angle = new AnglePlane.Abs(0.0, AnglePlaneUnit.SI);
        AngleSlope.Abs slope = new AngleSlope.Abs(0.0, AngleSlopeUnit.SI);
        OTSPoint3D coordinate = new OTSPoint3D(geometryTag.x.doubleValue(),geometryTag.y.doubleValue(),geometryTag.hdg.doubleValue());
        
        OTSNode node = new OTSNode(geometryTag.id, coordinate, angle, slope);
        geometryTag.node = node;
        return node;
    }
}
