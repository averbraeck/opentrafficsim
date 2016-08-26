package org.opentrafficsim.core.gis;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Transform geographical coordinates between WGS84 and a local coordinate system.
 * <p>
 * Copyright (c) 2016-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Aug 4, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public interface WGS84Transformations
{
    /**
     * Convert a coordinate pair in the local system to WGS84 coordinates.
     * @param local Point2D; coordinates in the local system.
     * @return Point2D; the equivalent location in degrees in the WGS84 coordinate system
     * @throws IllegalArgumentException when <cite>local</cite> is not valid in the local system
     */
    Point2D toWGS84(final Point2D local) throws IllegalArgumentException;

    /**
     * Convert a coordinate pair in the local system to WGS84 coordinates.
     * @param localX double; X-coordinate in the local system
     * @param localY double; Y-coordinate in the local system
     * @return Point2D; the equivalent location in degrees in the WGS84 coordinate system
     * @throws IllegalArgumentException when <cite>localX</cite>, <cite>localY</cite> is not valid in the local system
     */
    Point2D toWGS84(final double localX, final double localY) throws IllegalArgumentException;

    /**
     * Convert a coordinate pair in WGS84 coordinates to local coordinates.
     * @param wgs84 Point2D; coordinates in degrees in the WGS84 coordinate system
     * @return Point2D; the equivalent location in the local coordinate system
     * @throws IllegalArgumentException when <cite>wgs84</cite> is not valid in the local system
     */
    Point2D fromWGS84(final Point2D wgs84) throws IllegalArgumentException;

    /**
     * Convert a coordinate pair in WGS84 coordinates to local coordinates.
     * @param wgs84East double; East coordinate in degrees in the WGS84 system (negative value indicates West)
     * @param wgs84North double; North coordinate in degrees in the WGS84 system (negative value indicates South)
     * @return Point2D; the equivalent location in the local coordinate system
     * @throws IllegalArgumentException when <cite>wgs84</cite> is not valid in the local system
     */
    Point2D fromWGS84(final double wgs84East, final double wgs84North) throws IllegalArgumentException;

    /**
     * Report the bounding box for conversion to the local coordinate system.<br>
     * Conversions from WGS84 to the local coordinate system should fail for locations outside this bounding box. If the valid
     * range is not adequately described by a rectangular bounding box, conversions for some areas within this bounding box may
     * also fail (with an IllegalArgumentException). There is no guarantee that the result of a conversion lies within the
     * bounding box for the reverse conversion.
     * @return Rectangle2D; bounding box in WGS84 degrees
     */
    Rectangle2D fromWGS84Bounds();

    /**
     * Report the bounding box for conversions from the local coordinate system. <br>
     * Conversions from the local coordinate system to WGS84 should fail for locations outside this bounding box. If the valid
     * range is not adequately described by a rectangular bounding box, conversions for some areas within this bounding box may
     * also fail (with an IllegalArgumentException). There is no guarantee that the result of a conversion lies within the bounding box for the reverse conversion.
     * @return Rectangle2D; bounding box of the local coordinate system
     */
    Rectangle2D toWGS84Bounds();
    
}
