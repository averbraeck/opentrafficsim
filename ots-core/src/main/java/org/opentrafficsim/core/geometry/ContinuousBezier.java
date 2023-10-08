package org.opentrafficsim.core.geometry;

import java.util.Arrays;

import org.djutils.draw.point.Point2d;
import org.djutils.exceptions.Throw;

/**
 * Continuous definition of a Bezier. Note that this class does not implement {@code ContinuousLine}. This class is simply a
 * helper class for (and a super of) {@code ContinuousBezierCubic}, which uses this class to determine curvature, offset lines,
 * etc.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 * @see <a href="https://pomax.github.io/bezierinfo/">Bezier info</a>
 */
public class ContinuousBezier
{

    /** T values of numerical approach of Legendre-Gauss to determine Bezier length. */
    private static final double[] T =
            new double[] {-0.0640568928626056260850430826247450385909, 0.0640568928626056260850430826247450385909,
                    -0.1911188674736163091586398207570696318404, 0.1911188674736163091586398207570696318404,
                    -0.3150426796961633743867932913198102407864, 0.3150426796961633743867932913198102407864,
                    -0.4337935076260451384870842319133497124524, 0.4337935076260451384870842319133497124524,
                    -0.5454214713888395356583756172183723700107, 0.5454214713888395356583756172183723700107,
                    -0.6480936519369755692524957869107476266696, 0.6480936519369755692524957869107476266696,
                    -0.7401241915785543642438281030999784255232, 0.7401241915785543642438281030999784255232,
                    -0.8200019859739029219539498726697452080761, 0.8200019859739029219539498726697452080761,
                    -0.8864155270044010342131543419821967550873, 0.8864155270044010342131543419821967550873,
                    -0.9382745520027327585236490017087214496548, 0.9382745520027327585236490017087214496548,
                    -0.9747285559713094981983919930081690617411, 0.9747285559713094981983919930081690617411,
                    -0.9951872199970213601799974097007368118745, 0.9951872199970213601799974097007368118745};

    /** C values of numerical approach of Legendre-Gauss to determine Bezier length. */
    private static final double[] C =
            new double[] {0.1279381953467521569740561652246953718517, 0.1279381953467521569740561652246953718517,
                    0.1258374563468282961213753825111836887264, 0.1258374563468282961213753825111836887264,
                    0.121670472927803391204463153476262425607, 0.121670472927803391204463153476262425607,
                    0.1155056680537256013533444839067835598622, 0.1155056680537256013533444839067835598622,
                    0.1074442701159656347825773424466062227946, 0.1074442701159656347825773424466062227946,
                    0.0976186521041138882698806644642471544279, 0.0976186521041138882698806644642471544279,
                    0.086190161531953275917185202983742667185, 0.086190161531953275917185202983742667185,
                    0.0733464814110803057340336152531165181193, 0.0733464814110803057340336152531165181193,
                    0.0592985849154367807463677585001085845412, 0.0592985849154367807463677585001085845412,
                    0.0442774388174198061686027482113382288593, 0.0442774388174198061686027482113382288593,
                    0.0285313886289336631813078159518782864491, 0.0285313886289336631813078159518782864491,
                    0.0123412297999871995468056670700372915759, 0.0123412297999871995468056670700372915759};

    /** The shape points. */
    protected Point2d[] points;

    /**
     * Create a Bezier of any order.
     * @param points Point2d... shape points.
     */
    public ContinuousBezier(final Point2d... points)
    {
        Throw.whenNull(points, "Points may not be null.");
        Throw.when(points.length < 2, IllegalArgumentException.class, "Minimum number of points is 2.");
        for (Point2d point : points)
        {
            Throw.whenNull(point, "One of the points is null.");
        }
        this.points = points;
    }

    /**
     * Returns the derivative for a Bezier, which is a Bezier of 1 order lower.
     * @return ContinuousBezier derivative Bezier.
     * @throws IllegalStateException if the Bezier has less than two points, in which case no derivative can be calculated.
     */
    public ContinuousBezier derivative()
    {
        Throw.when(this.points.length < 2, IllegalStateException.class,
                "Requesting derivative on Bezier with less than 2 points");
        int n = this.points.length - 1;
        Point2d[] derivativePoints = new Point2d[n];
        for (int i = 0; i < n; i++)
        {
            derivativePoints[i] =
                    new Point2d(n * (this.points[i + 1].x - this.points[i].x), n * (this.points[i + 1].y - this.points[i].y));
        }
        return new ContinuousBezier(derivativePoints);
    }

    /**
     * Returns the estimated length using the method of numerical approach of Legendre-Gauss, which is quite accurate.
     * @return double; estimated length.
     */
    public double length()
    {
        double len = 0.0;
        for (int i = 0; i < T.length; i++)
        {
            double t = 0.5 * T[i] + 0.5;
            Point2d p = derivative().at(t);
            len += C[i] * Math.hypot(p.x, p.y);
        }
        len *= 0.5;
        return len;
    }

    /**
     * Return the point for the given t value.
     * @param t double; t value, moving from 0 to 1 along the Bezier.
     * @return Point2d; point of the Bezier at t.
     */
    public Point2d at(final double t)
    {
        double[] x = new double[this.points.length];
        double[] y = new double[this.points.length];
        for (int j = 0; j < this.points.length; j++)
        {
            x[j] = this.points[j].x;
            y[j] = this.points[j].y;
        }
        return new Point2d(Bezier.Bn(t, x), Bezier.Bn(t, y));
    }

    /**
     * Returns the curvature at the given t value.
     * @param t double; t value, moving from 0 to 1 along the Bezier.
     * @return double curvature at the given t value.
     */
    public double curvature(final double t)
    {
        ContinuousBezier der = derivative();
        Point2d d = der.at(t);
        double denom = Math.pow(d.x * d.x + d.y * d.y, 3.0 / 2.0);
        if (denom == 0.0)
        {
            return Double.POSITIVE_INFINITY;
        }
        Point2d dd = der.derivative().at(t);
        double numer = d.x * dd.y - dd.x * d.y;
        return numer / denom;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "ContinuousBezier [points=" + Arrays.toString(this.points) + "]";
    }

}
