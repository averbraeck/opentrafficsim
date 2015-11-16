package org.opentrafficsim.core.geometry;

import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.opentrafficsim.core.network.NetworkException;

/**
 * Generation of B&eacute;zier curves. <br>
 * The class implements the cubic(...) method to generate a cubic B&eacute;zier curve using the following formula: B(t) = (1 -
 * t)<sup>3</sup>P<sub>0</sub> + 3t(1 - t)<sup>2</sup> P<sub>1</sub> + 3t<sup>2</sup> (1 - t) P<sub>2</sub> + t<sup>3</sup>
 * P<sub>3</sub> where P<sub>0</sub> and P<sub>3</sub> are the end points, and P<sub>1</sub> and P<sub>2</sub> the control
 * points. <br>
 * For a smooth movement, one of the standard implementations if the cubic(...) function offered is the case where P<sub>1</sub>
 * is positioned halfway between P<sub>0</sub> and P<sub>3</sub> starting from P<sub>0</sub> in the direction of P<sub>3</sub>,
 * and P<sub>2</sub> is positioned halfway between P<sub>3</sub> and P<sub>0</sub> starting from P<sub>3</sub> in the direction
 * of P<sub>0</sub>.<br>
 * Finally, an n-point generalization of the B&eacute;zier curve is implemented with the bezier(...) function.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 14, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public final class Bezier
{
    /** the default number of points to use to construct a B&eacute;zier curve. */
    private static final int DEFAULT_NUM_POINTS = 64;

    /** cached factorial values. */
    private static long[] fact = new long[]{1L, 1L, 2L, 6L, 24L, 120L, 720L, 5040L, 40320L, 362880L, 3628800L,
        39916800L, 479001600L, 6227020800L, 87178291200L, 1307674368000L, 20922789888000L, 355687428096000L,
        6402373705728000L, 121645100408832000L, 2432902008176640000L};

    /** Utility class. */
    private Bezier()
    {
        // do not instantiate
    }

    /**
     * Construct a cubic B&eacute;zier curve from start to end with two control points.
     * @param numPoints the number of points for the B&eacute;zier curve
     * @param start the start point of the B&eacute;zier curve
     * @param control1 the first control point
     * @param control2 the second control point
     * @param end the end point of the B&eacute;zier curve
     * @return a cubic B&eacute;zier curve between start and end, with the two provided control points
     * @throws NetworkException in case the number of points is less than 2 or the B&eacute;zier curve could not be constructed
     */
    public static OTSLine3D cubic(final int numPoints, final OTSPoint3D start, final OTSPoint3D control1,
        final OTSPoint3D control2, final OTSPoint3D end) throws NetworkException
    {
        OTSPoint3D[] points = new OTSPoint3D[numPoints];
        for (int n = 0; n < numPoints; n++)
        {
            double t = n / (numPoints - 1.0);
            double x = B3(t, start.x, control1.x, control2.x, end.x);
            double y = B3(t, start.y, control1.y, control2.y, end.y);
            double z = B3(t, start.z, control1.z, control2.z, end.z);
            points[n] = new OTSPoint3D(x, y, z);
        }
        return new OTSLine3D(points);
    }

    /**
     * Construct a cubic B&eacute;zier curve from start to end with two generated control points at half the distance between
     * start and end. The z-value is interpolated in a linear way.
     * @param numPoints the number of points for the B&eacute;zier curve
     * @param start the directed start point of the B&eacute;zier curve
     * @param end the directed end point of the B&eacute;zier curve
     * @return a cubic B&eacute;zier curve between start and end, with the two provided control points
     * @throws NetworkException in case the number of points is less than 2 or the B&eacute;zier curve could not be constructed
     */
    public static OTSLine3D cubic(final int numPoints, final DirectedPoint start, final DirectedPoint end)
        throws NetworkException
    {
        double distance2 =
            Math.sqrt((end.x - start.x) * (end.x - start.x) + (end.y - start.y) * (end.y - start.y)) / 2.0;
        OTSPoint3D control1 =
            new OTSPoint3D(start.x + distance2 * Math.cos(start.getRotZ()), start.y + distance2
                * Math.sin(start.getRotZ()), start.z);
        OTSPoint3D control2 =
            new OTSPoint3D(end.x - distance2 * Math.cos(end.getRotZ()), end.y - distance2 * Math.sin(end.getRotZ()),
                end.z);
        // return cubic(numPoints, new OTSPoint3D(start), control1, control2, new OTSPoint3D(end));
        return bezier(numPoints, new OTSPoint3D(start), control1, control2, new OTSPoint3D(end));
    }

    /**
     * Construct a cubic B&eacute;zier curve from start to end with two generated control points at half the distance between
     * start and end. The z-value is interpolated in a linear way.
     * @param start the directed start point of the B&eacute;zier curve
     * @param end the directed end point of the B&eacute;zier curve
     * @return a cubic B&eacute;zier curve between start and end, with the two provided control points
     * @throws NetworkException in case the number of points is less than 2 or the B&eacute;zier curve could not be constructed
     */
    public static OTSLine3D cubic(final DirectedPoint start, final DirectedPoint end) throws NetworkException
    {
        return cubic(DEFAULT_NUM_POINTS, start, end);
    }

    /**
     * Calculate the cubic B&eacute;zier point with B(t) = (1 - t)<sup>3</sup>P<sub>0</sub> + 3t(1 - t)<sup>2</sup>
     * P<sub>1</sub> + 3t<sup>2</sup> (1 - t) P<sub>2</sub> + t<sup>3</sup> P<sub>3</sub>.
     * @param t the fraction
     * @param p0 the first point of the curve
     * @param p1 the first control point
     * @param p2 the second control point
     * @param p3 the end point of the curve
     * @return the cubic bezier value B(t)
     */
    @SuppressWarnings("checkstyle:methodname")
    private static double B3(final double t, final double p0, final double p1, final double p2, final double p3)
    {
        double t2 = t * t;
        double t3 = t2 * t;
        double m = (1.0 - t);
        double m2 = m * m;
        double m3 = m2 * m;
        return m3 * p0 + 3.0 * t * m2 * p1 + 3.0 * t2 * m * p2 + t3 * p3;
    }

    /**
     * Construct a B&eacute;zier curve of degree n.
     * @param numPoints the number of points for the B&eacute;zier curve to be constructed
     * @param points the points of the curve, where the first and last are begin and end point, and the intermediate ones are
     *            control points. There should be at least two points.
     * @return the B&eacute;zier value B(t) of degree n, where n is the number of points in the array
     * @throws NetworkException in case the number of points is less than 2 or the B&eacute;zier curve could not be constructed
     */
    public static OTSLine3D bezier(final int numPoints, final OTSPoint3D... points) throws NetworkException
    {
        OTSPoint3D[] result = new OTSPoint3D[numPoints];
        double[] px = new double[points.length];
        double[] py = new double[points.length];
        double[] pz = new double[points.length];
        for (int i = 0; i < points.length; i++)
        {
            px[i] = points[i].x;
            py[i] = points[i].y;
            pz[i] = points[i].z;
        }
        for (int n = 0; n < numPoints; n++)
        {
            double t = n / (numPoints - 1.0);
            double x = Bn(t, px);
            double y = Bn(t, py);
            double z = Bn(t, pz);
            result[n] = new OTSPoint3D(x, y, z);
        }
        return new OTSLine3D(result);
    }

    /**
     * Construct a B&eacute;zier curve of degree n.
     * @param points the points of the curve, where the first and last are begin and end point, and the intermediate ones are
     *            control points. There should be at least two points.
     * @return the B&eacute;zier value B(t) of degree n, where n is the number of points in the array
     * @throws NetworkException in case the number of points is less than 2 or the B&eacute;zier curve could not be constructed
     */
    public static OTSLine3D bezier(final OTSPoint3D... points) throws NetworkException
    {
        return bezier(DEFAULT_NUM_POINTS, points);
    }

    /**
     * Calculate the B&eacute;zier point of degree n, with B(t) = Sum(i = 0..n) [C(n, i) * (1 - t)<sup>n-i</sup> t<sup>i</sup>
     * P<sub>i</sub>], where C(n, k) is the binomial coefficient defined by n! / ( k! (n-k)! ), ! being the factorial operator.
     * @param t the fraction
     * @param p the points of the curve, where the first and last are begin and end point, and the intermediate ones are control
     *            points
     * @return the B&eacute;zier value B(t) of degree n, where n is the number of points in the array
     */
    @SuppressWarnings("checkstyle:methodname")
    private static double Bn(final double t, final double... p)
    {
        double b = 0.0;
        double m = (1.0 - t);
        int n = p.length - 1;
        double fn = factorial(n);
        for (int i = 0; i <= n; i++)
        {
            double c = fn / (factorial(i) * (factorial(n - i)));
            b += c * Math.pow(m, n - i) * Math.pow(t, i) * p[i];
        }
        return b;
    }

    /**
     * Calculate factorial(k), which is k * (k-1) * (k-2) * ... * 1. For factorials up to 20, a lookup table is used.
     * @param k the parameter
     * @return factorial(k)
     */
    private static double factorial(final int k)
    {
        if (k < fact.length)
        {
            return fact[k];
        }
        double f = 1;
        for (int i = 2; i <= k; i++)
        {
            f = f * i;
        }
        return f;
    }

    /**
     * @param args args
     * @throws NetworkException ne
     */
    public static void main(final String[] args) throws NetworkException
    {
//        DirectedPoint s = new DirectedPoint(0, 0, 0, 0, 0, -Math.PI/2.0);
//        DirectedPoint e = new DirectedPoint(10, 10, 20, 0, 0, Math.PI);
//        OTSLine3D b1 = Bezier.cubic(s, e);
//        for (OTSPoint3D p : b1.getPoints())
//        {
//            System.out.println(p.x + "\t" + p.y + "\t" + p.z);
//        }
        
        OTSPoint3D s = new OTSPoint3D(0, 0, 0);
        OTSPoint3D s1 = new OTSPoint3D(10, 0, 0);
        OTSPoint3D m1 = new OTSPoint3D(25, 5, 0);
        OTSPoint3D m2 = new OTSPoint3D(-15, 5, 0);
        OTSPoint3D e0 = new OTSPoint3D(0, 10, 20);
        OTSPoint3D e = new OTSPoint3D(10, 10, 20);
        OTSLine3D b1 = Bezier.bezier(s, s1, m1, m2, e0, e);
        for (OTSPoint3D p : b1.getPoints())
        {
            System.out.println(p.x + "\t" + p.y + "\t" + p.z);
        }

    }
}
