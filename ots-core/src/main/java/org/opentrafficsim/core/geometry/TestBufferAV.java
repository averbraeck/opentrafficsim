package org.opentrafficsim.core.geometry;

import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.opentrafficsim.core.network.NetworkException;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 9, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public final class TestBufferAV
{
    /** */
    private TestBufferAV()
    {
    }

    /**
     * @param args args
     * @throws NetworkException on error
     * @throws OTSGeometryException on error
     */
    public static void main(final String[] args) throws NetworkException, OTSGeometryException
    {
        OTSLine3D line0 =
            new OTSLine3D(new OTSPoint3D(10, 10, 0), new OTSPoint3D(10, 8, 0), new OTSPoint3D(0, 6, 0), new OTSPoint3D(
                10, 4, 0), new OTSPoint3D(10, 0, 0));
        OTSLine3D line1 =
            new OTSLine3D(new OTSPoint3D(10, 10, 0), new OTSPoint3D(9.999, 8, 0), new OTSPoint3D(9.996, 7.99, 0),
                new OTSPoint3D(9.999, 7.98, 0), new OTSPoint3D(10.03, 7.95, 0), new OTSPoint3D(10.01, 7.94, 0),
                new OTSPoint3D(10.0, 7.94, 0), new OTSPoint3D(10, 6, 0), new OTSPoint3D(10, 2, 0));
        OTSLine3D line2 =
            new OTSLine3D(new OTSPoint3D(10, 10, 0), new OTSPoint3D(9.999, 8, 0), new OTSPoint3D(9.996, 7.99, 0),
                new OTSPoint3D(9.999, 7.98, 0), new OTSPoint3D(10.03, 7.95, 0), new OTSPoint3D(10.01, 7.94, 0),
                new OTSPoint3D(10.0, 7.94, 0), new OTSPoint3D(10, 6, 0), new OTSPoint3D(9.999, 6, 0), new OTSPoint3D(
                    9.996, 5.99, 0), new OTSPoint3D(9.999, 5.98, 0), new OTSPoint3D(10.03, 5.95, 0), new OTSPoint3D(
                    10.01, 5.94, 0), new OTSPoint3D(10.0, 5.94, 0), new OTSPoint3D(10, 2, 0));
        OTSLine3D buf1 = offset(line0, 4.0);
        // OTSLine3D buf2 = offset(line0, -4.0);
        // printExcel(line2);
        // printExcel(buf1);
        // printExcel(buf2);
    }

    private static void printExcel(final OTSLine3D line)
    {
        for (OTSPoint3D p : line.getPoints())
        {
            System.out.println(p.x + "\t" + p.y);
        }
        System.out.println();
    }

    private static final int LINES2PI = 128;

    private static OTSLine3D offset(final OTSLine3D line, final double offset) throws OTSGeometryException,
        NetworkException
    {
        List<Point2D.Double> orig = new ArrayList<>();
        List<Line2D.Double> lines = new ArrayList<>();
        List<Path2D.Double> rects = new ArrayList<>();
        List<Path2D.Double> circs = new ArrayList<>();
        double sign = (offset < 0) ? -1.0 : 1.0;
        boolean left = offset > 0;
        double pi2 = Math.PI / 2.0;

        // for each line segment, define a line segment at the offset; also define a rectangle
        for (int i = 0; i < line.size() - 1; i++)
        {
            Point2D.Double p1 = new Point2D.Double(line.get(i).x, line.get(i).y);
            Point2D.Double p2 = new Point2D.Double(line.get(i + 1).x, line.get(i + 1).y);
            orig.add(p1);
            double p1x = p1.x;
            double p1y = p1.y;
            double p2x = p2.x;
            double p2y = p2.y;
            double angle = Math.atan2(p2y - p1y, p2x - p1x);
            double osina = sign * offset * Math.sin(angle + sign * pi2);
            double ocosa = sign * offset * Math.cos(angle + sign * pi2);
            Point2D.Double o1 = new Point2D.Double(p1x + ocosa, p1y + osina);
            Point2D.Double o2 = new Point2D.Double(p2x + ocosa, p2y + osina);
            Point2D.Double q1 = new Point2D.Double(p1x + 0.999 * ocosa, p1y + 0.999 * osina);
            Point2D.Double q2 = new Point2D.Double(p2x + 0.999 * ocosa, p2y + 0.999 * osina);
            lines.add(new Line2D.Double(o1, o2));
            rects.add(makeRectangle(p1, p2, q2, q1));
        }
        orig.add(new Point2D.Double(line.get(line.size() - 1).x, line.get(line.size() - 1).y));

        // for each subsequent line segment, draw an arc as line segments based on the angles
        for (int i = 0; i < orig.size() - 2; i++)
        {
            Point2D.Double p1 = orig.get(i);
            Point2D.Double p2 = orig.get(i + 1);
            Point2D.Double p3 = orig.get(i + 2);

            // test if the line and its successor have an angle > pi towards each other
            double angle1 = norm(Math.atan2(p2.y - p1.y, p2.x - p1.x));
            double angle2 = norm(Math.atan2(p3.y - p2.y, p3.x - p2.x));
            if (angle1 != angle2 && norm(sign * (angle2 - angle1)) > Math.PI)
            {
                // make an arc between the points; O = p2; leave out first and last point!
                int numPoints = (int) Math.ceil(LINES2PI * norm(Math.abs(angle2 - angle1)) / (2.0 * Math.PI)) + 1;
                Point2D.Double[] arc = new Point2D.Double[numPoints + 1];
                arc[0] = new Point2D.Double(lines.get(i).x2, lines.get(i).y2);
                arc[arc.length - 1] = new Point2D.Double(lines.get(i + 1).x1, lines.get(i + 1).y1);
                for (int j = 1; j < numPoints; j++)
                {
                    double angle = angle1 + sign * pi2 + (angle2 - angle1) * (1.0 * j / numPoints);
                    arc[j] =
                        new Point2D.Double(p2.x + sign * offset * Math.cos(angle), p2.y + sign * offset
                            * Math.sin(angle));
                }

                for (int j = 0; j < arc.length - 1; j++)
                {
                    lines.add(new Line2D.Double(arc[j], arc[j + 1]));
                }

                Path2D.Double circ = new Path2D.Double();
                circ.moveTo(p2.x, p2.y);
                for (int j = 0; j <= numPoints; j++)
                {
                    double angle = angle1 + sign * pi2 + (angle2 - angle1) * (1.0 * j / numPoints);
                    circ.lineTo(p2.x + sign * offset * 0.999 * Math.cos(angle), p2.y + sign * offset * 0.999
                        * Math.sin(angle));
                }
                circ.closePath();
                circs.add(circ);
            }
        }

        // add the 'cube' or circle at both ends of the line as a no-go area.
        Point2D.Double p1 = orig.get(0);
        Point2D.Double p2 = orig.get(1);
        double angle1 = norm(Math.PI + Math.atan2(p2.y - p1.y, p2.x - p1.x));
        double so999 = sign * offset * 0.999;
        p1 =
            new Point2D.Double(p1.x + sign * offset * 0.001 * Math.cos(angle1), p1.y + sign * offset * 0.001
                * Math.sin(angle1));
        p2 = new Point2D.Double(p1.x + so999 * Math.cos(angle1), p1.y + so999 * Math.sin(angle1));
        Point2D.Double p3 =
            new Point2D.Double(p2.x + so999 * Math.cos(angle1 - sign * pi2), p2.y + so999
                * Math.sin(angle1 - sign * pi2));
        Point2D.Double p4 =
            new Point2D.Double(p1.x + so999 * Math.cos(angle1 - sign * pi2), p1.y - so999
                * Math.sin(angle1 + sign * pi2));
        rects.add(makeRectangle(p1, p2, p3, p4));

        p1 = orig.get(orig.size() - 1);
        p2 = orig.get(orig.size() - 2);
        double angle2 = norm(Math.PI + Math.atan2(p2.y - p1.y, p2.x - p1.x));
        p1 =
            new Point2D.Double(p1.x + sign * offset * 0.001 * Math.cos(angle1), p1.y + sign * offset * 0.001
                * Math.sin(angle1));
        p2 = new Point2D.Double(p1.x + so999 * Math.cos(angle2), p1.y + so999 * Math.sin(angle2));
        p3 =
            new Point2D.Double(p2.x + so999 * Math.cos(angle2 + sign * pi2), p2.y + so999
                * Math.sin(angle2 + sign * pi2));
        p4 =
            new Point2D.Double(p1.x + so999 * Math.cos(angle2 + sign * pi2), p1.y + so999
                * Math.sin(angle2 + sign * pi2));
        rects.add(makeRectangle(p1, p2, p3, p4));

        // determine all crossing lines and split both at the crossing point.
        // TODO a line can be cut multiple times -- new lines have to be fed back to the process
        // TODO therefore should be a while() loop instead of a for() loop.
        List<Line2D.Double> lines2 = new ArrayList<>();
        List<Line2D.Double> untouched = new ArrayList<>(lines);
        for (int i = 0; i < lines.size(); i++)
        {
            for (int j = i + 1; j < lines.size(); j++)
            {
                Line2D.Double line1 = lines.get(i);
                Line2D.Double line2 = lines.get(j);
                Point2D.Double cross = intersection(line1, line2);
                if (cross != null)
                {
                    untouched.remove(line1);
                    untouched.remove(line2);
                    // make 4 new lines
                    lines2.add(new Line2D.Double(line1.getP1(), cross));
                    lines2.add(new Line2D.Double(cross, line1.getP2()));
                    lines2.add(new Line2D.Double(line2.getP1(), cross));
                    lines2.add(new Line2D.Double(cross, line2.getP2()));
                }
            }
        }
        lines2.addAll(untouched);

        // throw out all lines that cross the center line
        List<Line2D.Double> remove = new ArrayList<>();
        for (int i = 0; i < orig.size() - 1; i++)
        {
            Line2D.Double o = new Line2D.Double(orig.get(i), orig.get(i + 1));
            for (Line2D.Double l : lines2)
            {
                if (o.intersectsLine(l))
                {
                    remove.add(l);
                }
            }
        }
        lines2.removeAll(remove);

        // throw out all lines that are 'inside' the rectangles with one of their points
        for (Path2D.Double rect : rects)
        {
            remove = new ArrayList<>();
            for (Line2D.Double l : lines2)
            {
                if (rect.contains(l.getP1()) || rect.contains(l.getP2()))
                {
                    remove.add(l);
                }
            }
            lines2.removeAll(remove);
        }

        // throw out all lines that are 'inside' the circles around each center line
        for (Path2D.Double circle : circs)
        {
            remove = new ArrayList<>();
            for (Line2D.Double l : lines2)
            {
                if (circle.contains(l.getP1()) || circle.contains(l.getP2()))
                {
                    remove.add(l);
                }
            }
            lines2.removeAll(remove);
        }

        // TODO walk through the line segments and string them together.

        print(lines2);

        // for (Path2D.Double rect : rects)
        // {
        // print(rect);
        // }
        // for (Path2D.Double circ : circs)
        // {
        // print(circ);
        // }

        // TODO no line yet.
        return new OTSLine3D(line.getPoints());
    }

    private static Path2D.Double makeRectangle(Point2D.Double p1, Point2D.Double p2, Point2D.Double p3,
        Point2D.Double p4)
    {
        Path2D.Double rect = new Path2D.Double();
        rect.moveTo(p1.x, p1.y);
        rect.lineTo(p2.x, p2.y);
        rect.lineTo(p3.x, p3.y);
        rect.lineTo(p4.x, p4.y);
        rect.closePath();
        return rect;
    }

    private static double norm(double angle)
    {
        while (angle < 0)
        {
            angle += 2.0 * Math.PI;
        }
        while (angle > 2.0 * Math.PI)
        {
            angle -= 2.0 * Math.PI;
        }
        return angle;
    }

    private static Point2D.Double intersection(Line2D.Double line1, Line2D.Double line2)
    {
        if (!line1.intersectsLine(line2))
        {
            return null;
        }
        double px = line1.getX1(), py = line1.getY1(), rx = line1.getX2() - px, ry = line1.getY2() - py;
        double qx = line2.getX1(), qy = line2.getY1(), sx = line2.getX2() - qx, sy = line2.getY2() - qy;

        double det = sx * ry - sy * rx;
        if (det == 0)
        {
            return null;
        }
        else
        {
            double z = (sx * (qy - py) + sy * (px - qx)) / det;
            if (z == 0 || z == 1)
            {
                return null; // intersection at end point
            }
            return new Point2D.Double(px + z * rx, py + z * ry);
        }
    }

    private static void print(Path2D.Double shape)
    {
        PathIterator pi = shape.getPathIterator(null);
        Point2D.Double pf = null;
        while (!pi.isDone())
        {
            double[] p = new double[6];
            int segtype = pi.currentSegment(p);
            if (segtype == PathIterator.SEG_MOVETO || segtype == PathIterator.SEG_LINETO)
            {
                System.out.println(p[0] + "\t" + p[1]);
                if (pf == null)
                {
                    pf = new Point2D.Double(p[0], p[1]);
                }
            }
            pi.next();
        }
        System.out.println(pf.x + "\t" + pf.y);
        System.out.println();
    }

    private static void print(List<Line2D.Double> lines)
    {
        if (lines.size() == 0)
        {
            System.out.println("<<none>>");
            return;
        }
        for (Line2D.Double line : lines)
        {
            System.out.println(line.x1 + "\t" + line.y1);
            System.out.println(line.x2 + "\t" + line.y2);
            System.out.println();
        }
    }
}
