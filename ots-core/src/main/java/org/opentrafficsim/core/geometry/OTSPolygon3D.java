package org.opentrafficsim.core.geometry;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version Jul 22, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class OTSPolygon3D extends OTSLine3D
{

    /** */
    private static final long serialVersionUID = 20151002L;

    /**
     * @param points the array of points to construct this OTSLine3D from.
     * @throws OTSGeometryException when the points has fewer than 2 entries or two successive points coincide
     */
    public OTSPolygon3D(final OTSPoint3D[] points) throws OTSGeometryException
    {
        super(points);
    }

    /**
     * @param lineString the lineString to construct this OTSLine3D from.
     * @throws OTSGeometryException when the lineString has fewer than 2 entries or two successive points coincide
     */
    public OTSPolygon3D(final LineString lineString) throws OTSGeometryException
    {
        super(lineString);
    }

    /**
     * @param coordinates the array of coordinates to construct this OTSLine3D from.
     * @throws OTSGeometryException when coordinates has fewer than 2 entries or two successive coordinates coincide
     */
    public OTSPolygon3D(final Coordinate[] coordinates) throws OTSGeometryException
    {
        super(coordinates);
    }

    /**
     * @param geometry the geometry to construct this OTSLine3D from.
     * @throws OTSGeometryException when geometry has fewer than 2 points or two successive points coincide
     */
    public OTSPolygon3D(final Geometry geometry) throws OTSGeometryException
    {
        super(geometry);
    }

    /**
     * Determine if a point is horizontally (ignoring Z-coordinates) contained within this OTSPolygon3D. <br>
     * Derived from <a href=
     * "http://bbs.dartmouth.edu/~fangq/MATH/download/source/Determining%20if%20a%20point%20lies%20on%20the%20interior%20of%20a%20polygon.htm"
     * > Paul Bourke's Determining if a point lies on the interior of a polygon</a>.
     * @param point OTSPoint3D; the point
     * @return boolean; true if the <cite>point</cite> lies within this OTSPolygon3D; false if the <cite>point</cite> lies
     *         outside this OTSPolygon3D; true if the <cite>point</cite> coincides exactly with one of the points defining this
     *         OTSPolygon3D, unpredictable (but reproducible) if the <cite>point</cite> lies on the boundary of this
     *         OTSPolygon3D
     */
    public final boolean contains(final OTSPoint3D point)
    {
        // First take care of the case where point coincides with one of the points on the polygon
        for (OTSPoint3D p : getPoints())
        {
            if ((p.x == point.x) && (p.y == point.y))
            {
                return true; // we'll consider that a hit
            }
        }
        // http://paulbourke.net/geometry/insidepoly/ (Solution 2; 2D)
        double sumAngle = 0;
        OTSPoint3D prevPoint = getLast();
        // TODO: using atan twice in a loop is expensive; should look for a faster algorithm.
        for (OTSPoint3D p : getPoints())
        {
            double theta1 = Math.atan2(prevPoint.y - point.y, prevPoint.x - point.x);
            double theta2 = Math.atan2(p.y - point.y, p.x - point.x);
            double diffTheta = theta2 - theta1;
            while (diffTheta > Math.PI)
            {
                diffTheta -= 2 * Math.PI;
            }
            while (diffTheta < -Math.PI)
            {
                diffTheta += 2 * Math.PI;
            }
            sumAngle += diffTheta;
            // System.out.println(String.format("theta1=%.3f, theta2=%.3f diff=%.3f sum=%.3f", Math.toDegrees(theta1),
            // Math.toDegrees(theta2), Math.toDegrees(diffTheta), Math.toDegrees(sumAngle)));
            prevPoint = p;
        }
        return (sumAngle > Math.PI) || (sumAngle < -Math.PI);
    }

}
