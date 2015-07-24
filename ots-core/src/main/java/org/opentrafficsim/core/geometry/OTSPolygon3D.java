package org.opentrafficsim.core.geometry;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Jul 22, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class OTSPolygon3D extends OTSLine3D
{

    /**
     * @param points the array of points to construct this OTSLine3D from.
     */
    public OTSPolygon3D(final OTSPoint3D[] points)
    {
        super(points);
    }

    /**
     * @param lineString the lineString to construct this OTSLine3D from.
     */
    public OTSPolygon3D(final LineString lineString)
    {
        super(lineString);
    }

    /**
     * @param coordinates the array of coordinates to construct this OTSLine3D from.
     */
    public OTSPolygon3D(final Coordinate[] coordinates)
    {
        super(coordinates);
    }

    /**
     * @param geometry the geometry to construct this OTSLine3D from.
     */
    public OTSPolygon3D(final Geometry geometry)
    {
        super(geometry);
    }


}
