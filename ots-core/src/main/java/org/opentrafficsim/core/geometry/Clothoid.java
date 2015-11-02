package org.opentrafficsim.core.geometry;

import org.djunits.value.vdouble.scalar.Angle;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.network.NetworkException;

/**
 * Generate an OTSLine3D for a clothoid. <br>
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
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Nov 2, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class Clothoid
{
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

    /* ====== LOCAL VARIABLES ====== */
    //@formatter:off
    /** S(x) for small x. */
    static double[] sn = {
        -2.99181919401019853726E3,
        7.08840045257738576863E5,
        -6.29741486205862506537E7,
        2.54890880573376359104E9,
        -4.42979518059697779103E10,
        3.18016297876567817986E11 
    };

    /** */
    static double[] sd = {
        2.81376268889994315696E2, 
        4.55847810806532581675E4, 
        5.17343888770096400730E6, 
        4.19320245898111231129E8,
        2.24411795645340920940E10, 
        6.07366389490084639049E11
    };

    /** C(x) for small x */
    static double[] cn = { 
        -4.98843114573573548651E-8, 
        9.50428062829859605134E-6, 
        -6.45191435683965050962E-4,
        1.88843319396703850064E-2, 
        -2.05525900955013891793E-1, 
        9.99999999999999998822E-1
    };

    /** */
    static double[] cd = { 
        3.99982968972495980367E-12, 
        9.15439215774657478799E-10, 
        1.25001862479598821474E-7,
        1.22262789024179030997E-5, 
        8.68029542941784300606E-4, 
        4.12142090722199792936E-2, 
        1.00000000000000000118E0 
    };

    /** Auxiliary function f(x). */
    static double[] fn = { 
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

    /** */
    static double[] fd = {
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

    /** Auxiliary function g(x). */
    static double[] gn = { 
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

    /** */
    static double[] gd = {
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
     * Compute polynomial in x.
     * @param x double; x
     * @param coef double[]; coefficients
     * @return
     */
    private static double polevl(double x, double[] coef)
    {
        double result = coef[0];
        for (int i = 0; i < coef.length; i++)
        {
            result *= x;
            result += coef[i];
        }
        return result;
    }

    /**
     * Compute polynomial in x.
     * @param x double; x
     * @param coef double[]; coefficients
     * @return
     */
    private static double p1evl(double x, double[] coef)
    {
        double result = x + coef[0];
        for (int i = 0; i < coef.length; i++)
        {
            result = result * x + coef[i];
        }
        return result;
    }

    /**
     * Approximate the fresnel function.
     * @param xxa
     * @return
     */
    private static double[] fresnel(double xxa)
    {
        double cc, ss;
        double x, x2;

        x = Math.abs(xxa);
        x2 = x * x;

        if (x2 < 2.5625)
        {
            double t;
            t = x2 * x2;
            ss = x * x2 * polevl(t, sn) / p1evl(t, sd);
            cc = x * polevl(t, cn) / polevl(t, cd);
        }
        else if (x > 36974.0)
        {
            cc = 0.5;
            ss = 0.5;
        }
        else
        {
            double f, g, c, s, t, u;
            x2 = x * x;
            t = Math.PI * x2;
            u = 1.0 / (t * t);
            t = 1.0 / t;
            f = 1.0 - u * polevl(u, fn) / p1evl(u, fd);
            g = t * polevl(u, gn) / p1evl(u, gd);

            t = Math.PI * 0.5 * x2;
            c = Math.cos(t);
            s = Math.sin(t);
            t = Math.PI * x;
            cc = 0.5 + (f * s - g * c) / t;
            ss = 0.5 - (f * c + g * s) / t;
        }

        if (xxa < 0.0)
        {
            cc = -cc;
            ss = -ss;
        }
        return new double[] { cc, ss };
    }

    /**
     * compute the actual "standard" spiral, starting with curvature 0
     * @param s run-length along spiral
     * @param cDot first derivative of curvature [1/m2]
     * @param initialCurvature double; curvature at start
     * @param x resulting x-coordinate in spirals local co-ordinate system [m]
     * @param y resulting y-coordinate in spirals local co-ordinate system [m]
     * @param t tangent direction at s [rad]
     */
    private static double[] odrSpiral(double s, double cDot, double initialCurvature)
    {
        double a;
        double[] result = new double[3];

        a = 1.0 / Math.sqrt(Math.abs(cDot));
        a *= Math.sqrt(Math.PI);

        double[] xy = fresnel(initialCurvature + s / a);
        result[0] = xy[0] * a;
        result[1] = xy[1] * a;

        if (cDot < 0.0)
            result[1] *= -1.0;

        result[2] = s * s * cDot * 0.5;
        return result;
    }

    /**
     * Approximate a clothoid that starts in the x-direction.
     * @param count int; number of line segments to generate
     * @param initialCurvature double; curvature at start
     * @param curvatureDerivative double; rate of curvature change along the clothoid
     * @param length double; total length of the clothoid
     * @return OTSLine3D
     * @throws NetworkException if the number of segments is too low
     */
    private static OTSLine3D clothoid(int count, double initialCurvature, double curvatureDerivative, double length)
            throws NetworkException
    {
        OTSPoint3D[] points = new OTSPoint3D[count + 1];
        double[] offset = odrSpiral(initialCurvature / curvatureDerivative, curvatureDerivative, initialCurvature);
        double sinRot = Math.sin(offset[2]);
        double cosRot = Math.cos(offset[2]);
        for (int i = 0; i <= count; i++)
        {
            double[] xyd =
                    odrSpiral(i * length / count + initialCurvature / curvatureDerivative, curvatureDerivative,
                            initialCurvature);
            double dx = xyd[0] - offset[0];
            double dy = xyd[1] - offset[1];
            points[i] = new OTSPoint3D(dx * cosRot + dy * sinRot, dy * cosRot - dx * sinRot, xyd[2]);
        }
        return new OTSLine3D(points);
    }

    /**
     * Approximate a clothoid that starts in the x-direction at a given point.
     * @param x1 double; x-coordinate of the start point
     * @param y1 double; y-coordinate of the start point
     * @param startDirection double; rotation in radians at the start of the curve
     * @param startCurvature double; curvature at the start of the clothoid
     * @param endCurvature double; curvature at the end of the clothoid
     * @param length double; length of the clothoid
     * @param numSegments int; number of segments to generate (the number of points is one higher than this)
     * @return OTSLine3D; the clothoid
     * @throws NetworkException if the number of segments is too low
     * @throws OTSGeometryException never
     */
    private static OTSLine3D clothoid(double x1, double y1, double startDirection, double startCurvature, double endCurvature,
            double length, int numSegments) throws NetworkException
    {
        OTSLine3D result = clothoid(numSegments, startCurvature, (endCurvature - startCurvature) / length, length);
        double sinRot = Math.sin(startDirection);
        double cosRot = Math.cos(startDirection);
        OTSPoint3D[] list = new OTSPoint3D[result.size()];
        for (int i = 0; i < result.size(); i++)
        {
            try
            {
                OTSPoint3D p = result.get(i);
                list[i] = new OTSPoint3D(x1 + cosRot * p.x + sinRot * p.y, y1 + cosRot * p.y - sinRot * p.x, 0);
            }
            catch (OTSGeometryException ge)
            {
                // cannot happen
            }
        }
        return new OTSLine3D(list);
    }

    /**
     * Approximate a clothoid.
     * @param start OTSPoint3D; starting point of the clothoid
     * @param startDirection Angle.Abs; start direction of the clothoid
     * @param endCurvature double; curvature at the end of the clothoid [1/m]
     * @param length Length.Rel; length of the clothoid
     * @param numSegments int; number of segments of the clothoid
     * @return OTSLine3D; the clothoid
     * @throws NetworkException if the number of segments is too low
     */
    public static OTSLine3D clothoid(OTSPoint3D start, Angle.Abs startDirection, double endCurvature, Length.Rel length,
            int numSegments) throws NetworkException
    {
        return clothoid(start.x, start.y, startDirection.si, 0, endCurvature, length.si, numSegments);
    }

    /**
     * Approximate a clothoid.
     * @param start OTSPoint3D; starting point of the clothoid
     * @param startDirection Angle.Abs; start direction of the clothoid
     * @param startCurvature double; curvature at the start of the clothoid [1/m]
     * @param endCurvature double; curvature at the end of the clothoid [1/m]
     * @param length Length.Rel; length of the clothoid
     * @param numSegments int; number of segments of the clothoid
     * @return OTSLine3D; the clothoid
     * @throws NetworkException if the number of segments is too low
     */
    public static OTSLine3D clothoid(OTSPoint3D start, Angle.Abs startDirection, double startCurvature, double endCurvature,
            Length.Rel length, int numSegments) throws NetworkException
    {
        return clothoid(start.x, start.y, startDirection.si, startCurvature, endCurvature, length.si, numSegments);
    }

    /**
     * @param args
     * @throws NetworkException
     * @throws OTSGeometryException
     */
    public static void main(final String[] args) throws NetworkException, OTSGeometryException
    {
        // OTSLine3D line = clothoid(104.1485, 89.037488, 0, 0, -0.04841457, 3.2, 100);
        // System.out.println(line.toPlotterFormat());
        OTSLine3D line = clothoid(10, 10, Math.PI / 8, 0*-0.03, 0.04, 100, 100);
        System.out.println(line.toPlotterFormat());
    }

}
