package org.opentrafficsim.core.geometry;

import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;

/**
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Mar 31, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class OTSShape extends OTSLine3D
{
    /** */
    private static final long serialVersionUID = 20160331;

    /** The underlying shape (only constructed if needed). */
    private Path2D shape = null;

    /**
     * Construct a new OTSShape (closed shape).
     * @param points OTSPoint3D...; the array of points to construct this OTSLine3D from.
     * @throws OTSGeometryException when the provided points do not constitute a valid line (too few points or identical
     *             adjacent points)
     */
    public OTSShape(final OTSPoint3D... points) throws OTSGeometryException
    {
        super(points);
    }

    /**
     * Construct a new OTSShape (closed shape) from an array of Coordinate.
     * @param coordinates Coordinate[]; the array of coordinates to construct this OTSLine3D from
     * @throws OTSGeometryException when the provided points do not constitute a valid line (too few points or identical
     *             adjacent points)
     */
    public OTSShape(final Coordinate[] coordinates) throws OTSGeometryException
    {
        super(coordinates);
    }

    /**
     * Construct a new OTSShape (closed shape) from a LineString.
     * @param lineString LineString; the lineString to construct this OTSLine3D from.
     * @throws OTSGeometryException when the provided LineString does not constitute a valid line (too few points or identical
     *             adjacent points)
     */
    public OTSShape(final LineString lineString) throws OTSGeometryException
    {
        super(lineString);
    }

    /**
     * Construct a new OTSShape (closed shape) from a Geometry.
     * @param geometry Geometry; the geometry to construct this OTSLine3D from
     * @throws OTSGeometryException when the provided Geometry do not constitute a valid line (too few points or identical
     *             adjacent points)
     */
    public OTSShape(final Geometry geometry) throws OTSGeometryException
    {
        super(geometry);
    }

    /**
     * Construct a new OTSShape (closed shape) from a List&lt;OTSPoint3D&gt;.
     * @param pointList List&lt;OTSPoint3D&gt;; the list of points to construct this OTSLine3D from.
     * @throws OTSGeometryException when the provided points do not constitute a valid line (too few points or identical
     *             adjacent points)
     */
    public OTSShape(final List<OTSPoint3D> pointList) throws OTSGeometryException
    {
        super(pointList);
    }

    /**
     * Construct a new OTSShape (closed shape) from a Path2D.
     * @param path Path2D; the Path2D to construct this OTSLine3D from.
     * @throws OTSGeometryException when the provided points do not constitute a valid line (too few points or identical
     *             adjacent points)
     */
    public OTSShape(final Path2D path) throws OTSGeometryException
    {
        super(path);
    }

    /**
     * @return shape
     */
    public final Path2D getShape()
    {
        if (this.shape == null)
        {
            calculateShape();
        }
        return this.shape;
    }

    /**
     * calculate the java.awt.Shape, and close it if needed.
     */
    private void calculateShape()
    {
        this.shape = new Path2D.Double();
        this.shape.moveTo(getPoints()[0].x, getPoints()[0].y);
        for (int i = 1; i < getPoints().length; i++)
        {
            this.shape.lineTo(getPoints()[i].x, getPoints()[i].y);
        }
        this.shape.closePath();
    }

    /**
     * @param point OTSPoint3D; the point to check if it is inside the shape
     * @return whether the point is inside the shape
     */
    public final boolean contains(final OTSPoint3D point)
    {
        return getShape().contains(point.x, point.y);
    }

    /**
     * Check if this OTSShape completely covers a rectangular region.
     * @param rectangle Rectangle2D; the rectangular region
     * @return boolean; true if this OTSShape completely covers the region; false otherwise (or when the implementation of
     *         java.awt.geom.Path2D.contains found it prohibitively expensive to decide. Let us hope that this cannot happen
     *         with OTSShape objects. Peter has been unable to find out when this might happen.
     */
    public final boolean contains(final Rectangle2D rectangle)
    {
        return getShape().contains(rectangle);
    }

    /**
     * @param otsShape OTSShape; the shape to test the intersection with
     * @return whether the shapes intersect or whether one shape contains the other
     */
    public final boolean intersects(final OTSShape otsShape)
    {
        // step 1: quick check to see if the bounds intersect
        if (!getEnvelope().intersects(otsShape.getEnvelope()))
        {
            return false;
        }

        // step 2: quick check to see if any of the points of shape 1 is in shape 2
        for (OTSPoint3D p : getPoints())
        {
            if (otsShape.contains(p))
            {
                return true;
            }
        }

        // step 3: quick check to see if any of the points of shape 2 is in shape 1
        for (OTSPoint3D p : otsShape.getPoints())
        {
            if (contains(p))
            {
                return true;
            }
        }

        // step 4: see if any of the lines of shape 1 and shape 2 intersect (expensive!)
        Point2D prevPoint = getPoints()[this.size() - 1].getPoint2D();
        // for (int i = 0; i < getPoints().length - 1; i++)
        // {
        for (OTSPoint3D point : this.getPoints())
        {
            Point2D nextPoint = point.getPoint2D();
            Line2D.Double line1 = new Line2D.Double(prevPoint, nextPoint);
            // Line2D.Double line1 = new Line2D.Double(this.getPoints()[i].getPoint2D(), this.getPoints()[i + 1].getPoint2D());
            // for (int j = 0; j < otsShape.getPoints().length - 1; j++)
            // {
            Point2D otherPrevPoint = otsShape.getPoints()[otsShape.size() - 1].getPoint2D();
            for (OTSPoint3D otherPoint : otsShape.getPoints())
            {
                Point2D otherNextPoint = otherPoint.getPoint2D();
                // Line2D.Double line2 =
                // new Line2D.Double(otsShape.getPoints()[j].getPoint2D(), otsShape.getPoints()[j + 1].getPoint2D());
                Line2D.Double line2 = new Line2D.Double(otherPrevPoint, otherNextPoint);
                if (line1.intersectsLine(line2))
                {
                    double p1x = line1.getX1(), p1y = line1.getY1(), d1x = line1.getX2() - p1x, d1y = line1.getY2() - p1y;
                    double p2x = line2.getX1(), p2y = line2.getY1(), d2x = line2.getX2() - p2x, d2y = line2.getY2() - p2y;

                    double det = d2x * d1y - d2y * d1x;
                    if (det == 0)
                    {
                        /*- lines (partially) overlap, indicate 0, 1 or 2 (!) cross points
                         situations:
                         X============X        X============X        X============X        X=======X      X====X  
                                X---------X       X------X           X----X                X-------X           X----X
                         a. 2 intersections    b. 2 intersections    c. 1 intersection     d. 0 inters.   e. 0 inters.
                         */
                        Point2D p1s = line1.getP1(), p1e = line1.getP2(), p2s = line2.getP1(), p2e = line2.getP2();
                        if ((p1s.equals(p2s) && p1e.equals(p2e)) || (p1s.equals(p2e) && p1e.equals(p2s)))
                        {
                            return true; // situation d.
                        }
                        if (p1s.equals(p2s) && line1.ptLineDist(p2e) > 0 && line2.ptLineDist(p1e) > 0)
                        {
                            return true; // situation e.
                        }
                        if (p1e.equals(p2e) && line1.ptLineDist(p2s) > 0 && line2.ptLineDist(p1s) > 0)
                        {
                            return true; // situation e.
                        }
                        if (p1s.equals(p2e) && line1.ptLineDist(p2s) > 0 && line2.ptLineDist(p1e) > 0)
                        {
                            return true; // situation e.
                        }
                        if (p1e.equals(p2s) && line1.ptLineDist(p2e) > 0 && line2.ptLineDist(p1s) > 0)
                        {
                            return true; // situation e.
                        }
                    }
                    else
                    {
                        double z = (d2x * (p2y - p1y) + d2y * (p1x - p2x)) / det;
                        if (Math.abs(z) < 10.0 * Math.ulp(1.0) || Math.abs(z - 1.0) > 10.0 * Math.ulp(1.0))
                        {
                            return true; // intersection at end point
                        }
                    }

                }
                otherPrevPoint = otherNextPoint;
            }
            prevPoint = nextPoint;
        }
        return false;
    }

    /**
     * Create an OTSLine3D, while cleaning repeating successive points.
     * @param points OTSPoint3D[]; the coordinates of the line as OTSPoint3D
     * @return the line
     * @throws OTSGeometryException when number of points &lt; 2
     */
    public static OTSShape createAndCleanOTSShape(final OTSPoint3D[] points) throws OTSGeometryException
    {
        if (points.length < 2)
        {
            throw new OTSGeometryException(
                    "Degenerate OTSLine3D; has " + points.length + " point" + (points.length != 1 ? "s" : ""));
        }
        return createAndCleanOTSShape(new ArrayList<>(Arrays.asList(points)));
    }

    /**
     * Create an OTSLine3D, while cleaning repeating successive points.
     * @param pointList List&lt;OTSPoint3D&gt;; list of the coordinates of the line as OTSPoint3D; any duplicate points in this
     *            list are removed (this method may modify the provided list)
     * @return OTSLine3D; the line
     * @throws OTSGeometryException when number of non-equal points &lt; 2
     */
    public static OTSShape createAndCleanOTSShape(final List<OTSPoint3D> pointList) throws OTSGeometryException
    {
        // clean successive equal points
        int i = 1;
        while (i < pointList.size())
        {
            if (pointList.get(i - 1).equals(pointList.get(i)))
            {
                pointList.remove(i);
            }
            else
            {
                i++;
            }
        }
        return new OTSShape(pointList);
    }

    /**
     * Small test.
     * @param args String[]; empty
     * @throws OTSGeometryException when construction fails
     */
    public static void main(final String[] args) throws OTSGeometryException
    {
        OTSShape s1 = new OTSShape(new OTSPoint3D(0, 0), new OTSPoint3D(10, 0), new OTSPoint3D(10, 10), new OTSPoint3D(0, 10));
        OTSShape s2 = new OTSShape(new OTSPoint3D(5, 5), new OTSPoint3D(15, 5), new OTSPoint3D(15, 15), new OTSPoint3D(5, 15));
        System.out.println("s1.intersect(s2): " + s1.intersects(s2));
        System.out.println("s1.intersect(s1): " + s1.intersects(s1));
        OTSShape s3 =
                new OTSShape(new OTSPoint3D(25, 25), new OTSPoint3D(35, 25), new OTSPoint3D(35, 35), new OTSPoint3D(25, 35));
        System.out.println("s1.intersect(s3): " + s1.intersects(s3));
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "OTSShape [shape=" + this.shape + "]";
    }
}
