package org.opentrafficsim.road.network.factory.opendrive;

import org.djunits.unit.AngleUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Angle;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.network.NetworkException;
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
public class HeaderTag
{
    /** name of the map. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String name = null;

    /** revMajor. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    int revMajor = 0;
    
    /** revMinor. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    int revMinor = 0;
    
    /** version of the map. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String version = null;
    
    /** date of the map. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String date = null;
    
    /** range of the map in the north */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length.Rel north = null;
    
    /** range of the map in the south */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length.Rel south = null;
    
    /** range of the map in the east */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length.Rel east = null;
    
    /** range of the map in the west */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length.Rel west = null;

    /** vendor of the map. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String vendor = null;
    
    /** origin latitude of the map */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length.Rel originLat = null;
    
    /** origin longitude of the map */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length.Rel originLong = null;
    
    /** origin Hdg of the map */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Angle.Rel originHdg = null;

    /**
     * Parse the attributes of the junction tag. The sub-elements are parsed in separate classes.
     * @param node the junction node to parse
     * @param parser the parser with the lists of information
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseHeader(final Node node, final OpenDriveNetworkLaneParser parser) throws SAXException,
        NetworkException
    {
        NamedNodeMap attributes = node.getAttributes();
        HeaderTag headerTag = new HeaderTag();

        Node name = attributes.getNamedItem("name");
        if (name == null)
            throw new SAXException("Header: missing attribute name");
        headerTag.name = name.getNodeValue().trim();

        Node revMajor = attributes.getNamedItem("revMajor");
        if (revMajor == null)
            throw new SAXException("Header: missing attribute revMajor");
        headerTag.revMajor = Integer.parseInt(revMajor.getNodeValue().trim());
        
        Node revMinor = attributes.getNamedItem("revMinor");
        if (revMinor == null)
            throw new SAXException("Header: missing attribute revMinor");
        headerTag.revMinor = Integer.parseInt(revMinor.getNodeValue().trim());
        
        Node version = attributes.getNamedItem("version");
        if (version == null)
            throw new SAXException("Header: missing attribute version");
        headerTag.version = version.getNodeValue().trim();
        
        Node date = attributes.getNamedItem("date");
        if (date == null)
            throw new SAXException("Header: missing attribute date");
        headerTag.date = date.getNodeValue().trim();
        
        Node vendor = attributes.getNamedItem("vendor");
        if (vendor == null)
            throw new SAXException("Header: missing attribute vendor");
        headerTag.vendor = vendor.getNodeValue().trim();
        
        Node north = attributes.getNamedItem("north");
        if (north == null)
            throw new SAXException("Header: missing attribute north");
        headerTag.north = new Length.Rel(Double.parseDouble(north.getNodeValue().trim()), LengthUnit.METER);
        
        Node south = attributes.getNamedItem("south");
        if (south == null)
            throw new SAXException("Header: missing attribute south");
        headerTag.south = new Length.Rel(Double.parseDouble(south.getNodeValue().trim()), LengthUnit.METER);
        
        Node east = attributes.getNamedItem("east");
        if (east == null)
            throw new SAXException("Header: missing attribute east");
        headerTag.east = new Length.Rel(Double.parseDouble(east.getNodeValue().trim()), LengthUnit.METER);
        
        Node west = attributes.getNamedItem("west");
        if (west == null)
            throw new SAXException("Header: missing attribute west");
        headerTag.west = new Length.Rel(Double.parseDouble(west.getNodeValue().trim()), LengthUnit.METER);
        
        Node originLat = attributes.getNamedItem("originLat");
        if (originLat == null)
            throw new SAXException("Header: missing attribute originLat");
        headerTag.originLat = new Length.Rel(Double.parseDouble(originLat.getNodeValue().trim()), LengthUnit.METER);

        Node originLong = attributes.getNamedItem("originLong");
        if (originLong == null)
            throw new SAXException("Header: missing attribute originLong");
        headerTag.originLong = new Length.Rel(Double.parseDouble(originLong.getNodeValue().trim()), LengthUnit.METER);

        Node originHdg = attributes.getNamedItem("originHdg");
        if (originHdg == null)
            throw new SAXException("Header: missing attribute originHdg");
        headerTag.originHdg = new Angle.Rel(Double.parseDouble(originHdg.getNodeValue().trim()), AngleUnit.DEGREE);
        
        parser.headerTag = headerTag;
    }

    /**
     * @return originLat
     */
    public Length.Rel getOriginLat()
    {
        return this.originLat;
    }

    /**
     * @return originLong
     */
    public Length.Rel getOriginLong()
    {
        return this.originLong;
    }
}
