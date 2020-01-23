package org.opentrafficsim.road.network.factory.vissim;

import java.io.Serializable;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.factory.xml.units.Coordinates;
import org.xml.sax.SAXException;

/**
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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

    /** Points of the line. */
    OTSPoint3D[] vertices = null;

    /**
     * @param polyLineTag PolyLineTag; the parser with the lists of information
     */
    public PolyLineTag(PolyLineTag polyLineTag)
    {
        if (polyLineTag != null)
        {
            this.length = polyLineTag.length;
            this.vertices = polyLineTag.vertices;
        }
    }

    /**
     * @param length Length; length of the line
     * @param vertices OTSPoint3D[]; the points of the line
     */
    public PolyLineTag(Length length, OTSPoint3D[] vertices)
    {
        super();
        this.length = length;
        this.vertices = vertices;
    }

    /**
     *
     */
    public PolyLineTag()
    {
        // TODO Auto-generated constructor stub
    }

    /**
     * Parse the LINK.POLYLINE tag.
     * @param coords String; the XML-node to parse
     * @param parser VissimNetworkLaneParser; the parser with the lists of information
     * @param linkTag LinkTag; the parent link tag
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parsePolyLine(final String coords, final VissimNetworkLaneParser parser, final LinkTag linkTag)
            throws SAXException, NetworkException
    {
        linkTag.polyLineTag = new PolyLineTag();
        linkTag.polyLineTag.vertices = parseVertices(coords);
    }

    /**
     * Parse a coordinate with (x,y) or (x,y,z).
     * @param cs String; the string containing the coordinate.
     * @return a Point3d containing the x,y or x,y,z values.
     */
    public static OTSPoint3D[] parseVertices(final String cs)
    {
        String cs1 = cs.replaceAll("\\s+", "");
        String c = cs1.replace(")(", ")split(");
        String[] cc = c.split("split");
        OTSPoint3D[] coords = new OTSPoint3D[cc.length - 2];
        // only intermediate points: therefore the first and last are not included!!
        int i = 0;
        int vertexCount = 0;
        for (String coord : cc)
        {
            if (i > 0 && i < cc.length - 1)
            {
                coords[vertexCount] = Coordinates.parseCoordinate(coord);
                vertexCount++;
            }
            i++;
        }
        return coords;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "PolyLineTag [length=" + this.length + "]";
    }

}
