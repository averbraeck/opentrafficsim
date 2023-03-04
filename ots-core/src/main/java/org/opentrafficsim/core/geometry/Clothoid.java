package org.opentrafficsim.core.geometry;

import org.djunits.unit.DirectionUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.LinearDensityUnit;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.LinearDensity;
import org.djutils.logger.CategoryLogger;

/**
 * Generate an OtsLine3d for a clothoid. <br>
 * Derived from odrSpiral.c by M. Dupuis @ VIRES GmbH <br>
 * 
 * <pre>
 *        Licensed under the Apache License, Version 2.0 (the "License");
 *        you may not use this file except in compliance with the License.
 *        You may obtain a copy of the License at
 * 
 *            http://www.apache.org/licenses/LICENSE-2.0
 * 
 *        Unless required by applicable law or agreed to in writing, software
 *        distributed under the License is distributed on an "AS IS" BASIS,
 *        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *        See the License for the specific language governing permissions and
 *        limitations under the License.
 * </pre>
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author M. Dupuis @ VIRES GmbH
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public final class Clothoid
{
    /** Utility class. */
    private Clothoid()
    {
        // do not instantiate
    }

    /*- ===================================================
     *  file:       odrSpiral.c
     * ---------------------------------------------------
     *  purpose:    free method for computing spirals
     *              in OpenDRIVE applications 
     * ---------------------------------------------------
     *  using methods of CEPHES library
     * ---------------------------------------------------
     *  first edit: 09.03.2010 by M. Dupuis @ VIRES GmbH
     *  last mod.:  09.03.2010 by M. Dupuis @ VIRES GmbH
     * ===================================================
        Copyright 2010 VIRES Simulationstechnologie GmbH
    
        Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at
    
            http://www.apache.org/licenses/LICENSE-2.0
    
        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
        
        
        NOTE:
        The methods have been realized using the CEPHES library 
    
            http://www.netlib.org/cephes/
    
        and do neither constitute the only nor the exclusive way of implementing 
        spirals for OpenDRIVE applications. Their sole purpose is to facilitate 
        the interpretation of OpenDRIVE spiral data.
     */

    //@formatter:off
    /** S(x) for small x numerator. */
    static final double[] SN = {
        -2.99181919401019853726E3,
        7.08840045257738576863E5,
        -6.29741486205862506537E7,
        2.54890880573376359104E9,
        -4.42979518059697779103E10,
        3.18016297876567817986E11 
    };

    /** S(x) for small x denominator. */
    static final double[] SD = {
        2.81376268889994315696E2, 
        4.55847810806532581675E4, 
        5.17343888770096400730E6, 
        4.19320245898111231129E8,
        2.24411795645340920940E10, 
        6.07366389490084639049E11
    };

    /** C(x) for small x numerator. */
    static final double[] CN = {
        -4.98843114573573548651E-8, 
        9.50428062829859605134E-6, 
        -6.45191435683965050962E-4,
        1.88843319396703850064E-2, 
        -2.05525900955013891793E-1, 
        9.99999999999999998822E-1
    };

    /** C(x) for small x denominator. */
    static final double[] CD = {
        3.99982968972495980367E-12, 
        9.15439215774657478799E-10, 
        1.25001862479598821474E-7,
        1.22262789024179030997E-5, 
        8.68029542941784300606E-4, 
        4.12142090722199792936E-2, 
        1.00000000000000000118E0 
    };

    /** Auxiliary function f(x) numerator. */
    static final double[] FN = {
        4.21543555043677546506E-1, 
        1.43407919780758885261E-1, 
        1.15220955073585758835E-2,
        3.45017939782574027900E-4, 
        4.63613749287867322088E-6, 
        3.05568983790257605827E-8, 
        1.02304514164907233465E-10,
        1.72010743268161828879E-13, 
        1.34283276233062758925E-16, 
        3.76329711269987889006E-20
    };

    /** Auxiliary function f(x) denominator. */
    static final double[] FD = {
        7.51586398353378947175E-1, 
        1.16888925859191382142E-1, 
        6.44051526508858611005E-3, 
        1.55934409164153020873E-4,
        1.84627567348930545870E-6, 
        1.12699224763999035261E-8, 
        3.60140029589371370404E-11, 
        5.88754533621578410010E-14,
        4.52001434074129701496E-17, 
        1.25443237090011264384E-20 
    };

    /** Auxiliary function g(x) numerator. */
    static final double[] GN = {
        5.04442073643383265887E-1, 
        1.97102833525523411709E-1, 
        1.87648584092575249293E-2,
        6.84079380915393090172E-4, 
        1.15138826111884280931E-5, 
        9.82852443688422223854E-8, 
        4.45344415861750144738E-10,
        1.08268041139020870318E-12, 
        1.37555460633261799868E-15, 
        8.36354435630677421531E-19, 
        1.86958710162783235106E-22
    };

    /** Auxiliary function g(x) denominator. */
    static final double[] GD = {
        1.47495759925128324529E0, 
        3.37748989120019970451E-1, 
        2.53603741420338795122E-2, 
        8.14679107184306179049E-4,
        1.27545075667729118702E-5, 
        1.04314589657571990585E-7, 
        4.60680728146520428211E-10, 
        1.10273215066240270757E-12,
        1.38796531259578871258E-15, 
        8.39158816283118707363E-19, 
        1.86958710162783236342E-22
    };
    //@formatter:on

    /**
     * Compute a polynomial in x.
     * @param x double; x
     * @param coef double[]; coefficients
     * @return polynomial in x
     */
    private static double polevl(final double x, final double[] coef)
    {
        double result = coef[0];
        for (int i = 0; i < coef.length; i++)
        {
            result = result * x + coef[i];
        }
        return result;
    }

    /**
     * Compute a polynomial in x.
     * @param x double; x
     * @param coef double[]; coefficients
     * @return polynomial in x
     */
    private static double p1evl(final double x, final double[] coef)
    {
        double result = x + coef[0];
        for (int i = 0; i < coef.length; i++)
        {
            result = result * x + coef[i];
        }
        return result;
    }

    /**
     * Approximate the Fresnel function.
     * @param xxa double; the xxa parameter
     * @return double[]; array with two double values c and s
     */
    private static double[] fresnel(final double xxa)
    {
        final double x = Math.abs(xxa);
        final double x2 = x * x;
        double cc, ss;

        if (x2 < 2.5625)
        {
            final double t = x2 * x2;
            ss = x * x2 * polevl(t, SN) / p1evl(t, SD);
            cc = x * polevl(t, CN) / polevl(t, CD);
        }
        else if (x > 36974.0)
        {
            cc = 0.5;
            ss = 0.5;
        }
        else
        {
            double t = Math.PI * x2;
            final double u = 1.0 / (t * t);
            t = 1.0 / t;
            final double f = 1.0 - u * polevl(u, FN) / p1evl(u, FD);
            final double g = t * polevl(u, GN) / p1evl(u, GD);

            t = Math.PI * 0.5 * x2;
            final double c = Math.cos(t);
            final double s = Math.sin(t);
            t = Math.PI * x;
            cc = 0.5 + (f * s - g * c) / t;
            ss = 0.5 - (f * c + g * s) / t;
        }

        if (xxa < 0.0)
        {
            cc = -cc;
            ss = -ss;
        }
        return new double[] {cc, ss};
    }

    /**
     * Approximate one point of the "standard" spiral (curvature at start is 0).
     * @param s double; run-length along spiral
     * @param cDot double; first derivative of curvature [1/m2]
     * @param initialCurvature double; curvature at start
     * @return double[]; array of three double values containing x, y, and tangent direction
     */
    private static double[] odrSpiral(final double s, final double cDot, final double initialCurvature)
    {
        double a = Math.sqrt(Math.PI / Math.abs(cDot));

        double[] xy = fresnel(initialCurvature + s / a);
        return new double[] {xy[0] * a, xy[1] * a * Math.signum(cDot), s * s * cDot * 0.5};
    }

    /**
     * Approximate a clothoid that starts in the x-direction.
     * @param initialCurvature double; curvature at start
     * @param curvatureDerivative double; rate of curvature change along the clothoid
     * @param length double; total length of the clothoid
     * @param numSegments int; number of segments used to approximate (the number of points is one higher than this)
     * @return OtsLine3d; the line; the z-component of each point is set to 0
     * @throws OtsGeometryException if the number of segments is too low
     */
    private static OtsLine3d clothoid(final double initialCurvature, final double curvatureDerivative, final double length,
            final int numSegments) throws OtsGeometryException
    {
        OtsPoint3d[] points = new OtsPoint3d[numSegments + 1];
        double[] offset = odrSpiral(initialCurvature / curvatureDerivative, curvatureDerivative, initialCurvature);
        double sinRot = Math.sin(offset[2]);
        double cosRot = Math.cos(offset[2]);
        for (int i = 0; i <= numSegments; i++)
        {
            double[] xyd = odrSpiral(i * length / numSegments + initialCurvature / curvatureDerivative, curvatureDerivative,
                    initialCurvature);
            double dx = xyd[0] - offset[0];
            double dy = xyd[1] - offset[1];
            points[i] = new OtsPoint3d(dx * cosRot + dy * sinRot, dy * cosRot - dx * sinRot, 0);
        }
        return new OtsLine3d(points);
    }

    /**
     * Approximate a clothoid that starts at a given point in the given direction and curvature. Elevation is linearly
     * interpolated over the length of the clothoid.
     * @param x1 double; x-coordinate of the start point
     * @param y1 double; y-coordinate of the start point
     * @param startElevation double; z-component at start of the curve
     * @param startDirection double; rotation in radians at the start of the curve
     * @param startCurvature double; curvature at the start of the clothoid
     * @param endCurvature double; curvature at the end of the clothoid
     * @param length double; length of the clothoid
     * @param endElevation double; z-component at end of the curve
     * @param numSegments int; number of segments used to approximate (the number of points is one higher than this)
     * @return OtsLine3d; the clothoid
     * @throws OtsGeometryException if the number of segments is too low
     */
    @SuppressWarnings("checkstyle:parameternumber")
    private static OtsLine3d clothoid(final double x1, final double y1, final double startElevation,
            final double startDirection, final double startCurvature, final double endCurvature, final double length,
            final double endElevation, final int numSegments) throws OtsGeometryException
    {
        OtsLine3d result = clothoid(startCurvature, (endCurvature - startCurvature) / length, length, numSegments);
        double sinRot = Math.sin(startDirection);
        double cosRot = Math.cos(startDirection);
        OtsPoint3d[] list = new OtsPoint3d[result.size()];
        double elevationPerStep = (endElevation - startElevation) / (result.size() - 1);
        for (int i = 0; i < result.size(); i++)
        {
            try
            {
                OtsPoint3d p = result.get(i);
                list[i] = new OtsPoint3d(x1 + cosRot * p.x + sinRot * p.y, y1 + cosRot * p.y - sinRot * p.x,
                        startElevation + i * elevationPerStep);
            }
            catch (OtsGeometryException ge)
            {
                // cannot happen
                CategoryLogger.always().error(ge, "CANNOT HAPPEN; if you see this; let us know what you did.");
            }
        }
        return new OtsLine3d(list);
    }

    /**
     * Approximate a clothoid with curvature 0 at start.
     * @param start OtsPoint3d; starting point of the clothoid
     * @param startDirection Direction; start direction of the clothoid
     * @param endCurvature double; curvature at the end of the clothoid [1/m]
     * @param length Length; length of the clothoid
     * @param endElevation Length; elevation at end of the clothoid
     * @param numSegments int; number of segments used to approximate (the number of points is one higher than this)
     * @return OtsLine3d; the clothoid
     * @throws OtsGeometryException if the number of segments is too low
     */
    public static OtsLine3d clothoid(final OtsPoint3d start, final Direction startDirection, final double endCurvature,
            final Length length, final Length endElevation, final int numSegments) throws OtsGeometryException
    {
        return clothoid(start.x, start.y, start.z, startDirection.si, 0, endCurvature, length.si, endElevation.si, numSegments);
    }

    /**
     * Approximate a clothoid.
     * @param start OtsPoint3d; starting point of the clothoid
     * @param startDirection Direction; start direction of the clothoid
     * @param startCurvature double; curvature at the start of the clothoid [1/m]
     * @param endCurvature double; curvature at the end of the clothoid [1/m]
     * @param length Length; length of the clothoid
     * @param endElevation Length; elevation at end of the clothoid
     * @param numSegments int; number of segments used to approximate (the number of points is one higher than this)
     * @return OtsLine3d; the clothoid
     * @throws OtsGeometryException if the number of segments is too low
     */
    public static OtsLine3d clothoid(final OtsPoint3d start, final Direction startDirection, final double startCurvature,
            final double endCurvature, final Length length, final Length endElevation, final int numSegments)
            throws OtsGeometryException
    {
        return clothoid(start.x, start.y, start.x, startDirection.si, startCurvature, endCurvature, length.si, endElevation.si,
                numSegments);
    }

    /**
     * Approximate a clothoid with curvature 0 at start.
     * @param start OtsPoint3d; starting point of the clothoid
     * @param startDirection Direction; start direction of the clothoid
     * @param endCurvature LinearDensity; curvature at the end of the clothoid
     * @param length Length; length of the clothoid
     * @param endElevation Length; elevation at end of the clothoid
     * @param numSegments int; number of segments used to approximate (the number of points is one higher than this)
     * @return OtsLine3d; the clothoid
     * @throws OtsGeometryException if the number of segments is too low
     */
    public static OtsLine3d clothoid(final OtsPoint3d start, final Direction startDirection, final LinearDensity endCurvature,
            final Length length, final Length endElevation, final int numSegments) throws OtsGeometryException
    {
        return clothoid(start, startDirection, 0, endCurvature.si, length, endElevation, numSegments);
    }

    /**
     * Approximate a clothoid.
     * @param start OtsPoint3d; starting point of the clothoid
     * @param startDirection Direction; start direction of the clothoid
     * @param startCurvature LinearDensity; curvature at the start of the clothoid [1/m]
     * @param endCurvature LinearDensity; curvature at the end of the clothoid [1/m]
     * @param length Length; length of the clothoid
     * @param endElevation Length; elevation at end of the clothoid
     * @param numSegments int; number of segments used to approximate (the number of points is one higher than this)
     * @return OtsLine3d; the clothoid
     * @throws OtsGeometryException if the number of segments is too low
     */
    public static OtsLine3d clothoid(final OtsPoint3d start, final Direction startDirection, final LinearDensity startCurvature,
            final LinearDensity endCurvature, final Length length, final Length endElevation, final int numSegments)
            throws OtsGeometryException
    {
        return clothoid(start, startDirection, startCurvature.si, endCurvature.si, length, endElevation, numSegments);
    }

    /**
     * Demonstrate / test the clothoid methods.
     * @param args String[]; the command line arguments (not used)
     * @throws OtsGeometryException if the number of segments is too low
     */
    public static void main(final String[] args) throws OtsGeometryException
    {
        OtsLine3d line;
        // line = clothoid(104.1485, 89.037488, 0, 0, 0, -0.04841457, 0, 3.2, 100);
        // System.out.println(line.toPlotterFormat());
        // line = clothoid(10, 10, 5, Math.PI / 8, 0 * -0.03, 0.04, 100, 15, 100);
        line = clothoid(new OtsPoint3d(10, 10, 5), new Direction(Math.PI / 8, DirectionUnit.EAST_RADIAN),
                new LinearDensity(0 * -0.03, LinearDensityUnit.PER_METER), new LinearDensity(0.04, LinearDensityUnit.PER_METER),
                new Length(100, LengthUnit.METER), new Length(15, LengthUnit.METER), 100);
        System.out.println(OtsGeometryUtil.printCoordinates("#", line, "\n"));
    }

}
