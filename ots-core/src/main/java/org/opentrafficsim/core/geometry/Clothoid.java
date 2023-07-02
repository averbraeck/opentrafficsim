package org.opentrafficsim.core.geometry;

import java.util.ArrayList;
import java.util.List;

import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.LinearDensity;
import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;

/**
 * Utility class to create clothoid lines. In particular for the cases:
 * <ul>
 * <li>A clothoid between two directed <i>points</i>.</li>
 * <li>A clothoid originating from a directed point with start curvature, end curvature, and <i>length</i> specified.</li>
 * <li>A clothoid originating from a directed point with start curvature, end curvature, and <i>A-value</i> specified.</li>
 * </ul>
 * This class is based on various sources:
 * <ul>
 * <li>W.J. Cody (1968) Chebyshev approximations for the Fresnel integrals. Mathematics of Computation, Vol. 22, Issue 102, pp.
 * 450–453.</li>
 * <li>Dale Connor and Lilia Krivodonova (2014) "Interpolation of two-dimensional curves with Euler spirals", Journal of
 * Computational and Applied Mathematics, Volume 261, 1 May 2014, pp. 320-332.</li>
 * <li>D.J. Waltona and D.S. Meek (2009) "G<sup>1</sup> interpolation with a single Cornu spiral segment", Journal of
 * Computational and Applied Mathematics, Volume 223, Issue 1, 1 January 2009, pp. 86-96.</li>
 * </ul>
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 * @see <a href="https://www.ams.org/journals/mcom/1985-44-170/S0025-5718-1985-0777277-6/S0025-5718-1985-0777277-6.pdf">Cody
 *      (1968)</a>
 * @see <a href="https://www.sciencedirect.com/science/article/pii/S0377042713006286">Connor and Krivodonova (2014)</a>
 * @see <a href="https://www.sciencedirect.com/science/article/pii/S0377042704000925">Waltona and Meek (2009)</a>
 */
public class Clothoid
{

    // {@formatter:off}
    /** Numerator coefficients to calculate C(t) in region 1. */
    private static double[] CN1 = new double[] {
            9.999999999999999421E-01,
            -1.994608988261842706E-01, 
            1.761939525434914045E-02,
            -5.280796513726226960E-04, 
            5.477113856826871660E-06
    };

    /** Denominator coefficients to calculate C(t) in region 1. */
    private static double[] CD1 = new double[] {
            1.000000000000000000E+00,
            4.727921120104532689E-02,
            1.099572150256418851E-03,
            1.552378852769941331E-05,
            1.189389014228757184E-07
    };

    /** Numerator coefficients to calculate C(t) in region 2. */
    private static double[] CN2 = new double[] {
            1.00000000000111043640E+00,
            -2.07073360335323894245E-01,
            1.91870279431746926505E-02,
            -6.71376034694922109230E-04,
            1.02365435056105864908E-05,
            -5.68293310121870728343E-08
    };

    /** Denominator coefficients to calculate C(t) in region 3. */
    private static double[] CD2 = new double[] {
            1.00000000000000000000E+00,
            3.96667496952323433510E-02,
            7.88905245052359907842E-04,
            1.01344630866749406081E-05,
            8.77945377892369265356E-08,
            4.41701374065009620393E-10
    };

    /** Numerator coefficients to calculate S(t) in region 1. */
    private static double[] SN1 = new double[] {
            5.2359877559829887021E-01,
            -7.0748991514452302596E-02,
            3.8778212346368287939E-03,
            -8.4555728435277680591E-05,
            6.7174846662514086196E-07
    };

    /** Denominator coefficients to calculate S(t) in region 1. */
    private static double[] SD1 = new double[] {
            1.0000000000000000000E+00,
            4.1122315114238422205E-02,
            8.1709194215213447204E-04,
            9.6269087593903403370E-06,
            5.9528122767840998345E-08
    };

    /** Numerator coefficients to calculate S(t) in region 2. */
    private static double[] SN2 = new double[] {
            5.23598775598344165913E-01,
            -7.37766914010191323867E-02,
            4.30730526504366510217E-03,
            -1.09540023911434994566E-04,
            1.28531043742724820610E-06,
            -5.76765815593088804567E-09
    };

    /** Denominator coefficients to calculate S(t) in region 2. */
    private static double[] SD2 = new double[] {
            1.00000000000000000000E+00,
            3.53398342167472162540E-02,
            6.18224620195473216538E-04,
            6.87086265718620117905E-06,
            5.03090581246612375866E-08,
            2.05539124458579596075E-10
    };

    /** Numerator coefficients to calculate f(t) in region 3. */
    private static double[] FN3 = new double[] {
            3.1830975293580985290E-01,
            1.2226000551672961219E+01,
            1.2924886131901657025E+02,
            4.3886367156695547655E+02,
            4.1466722177958961672E+02,
            5.6771463664185116454E+01
    };

    /** Denominator coefficients to calculate f(t) in region 3. */
    private static double[] FD3 = new double[] {
            1.0000000000000000000E+00,
            3.8713003365583442831E+01,
            4.1674359830705629745E+02,
            1.4740030733966610568E+03,
            1.5371675584895759916E+03,
            2.9113088788847831515E+02
    };

    /** Numerator coefficients to calculate f(t) in region 4. */
    private static double[] FN4 = new double[] {
            3.183098818220169217E-01,
            1.958839410219691002E+01,
            3.398371349269842400E+02,
            1.930076407867157531E+03,
            3.091451615744296552E+03,
            7.177032493651399590E+02
    };

    /** Denominator coefficients to calculate f(t) in region 4. */
    private static double[] FD4 = new double[] {
            1.000000000000000000E+00,
            6.184271381728873709E+01,
            1.085350675006501251E+03,
            6.337471558511437898E+03,
            1.093342489888087888E+04,
            3.361216991805511494E+03
    };

    /** Numerator coefficients to calculate f(t) in region 5. */
    private static double[] FN5 = new double[] {
            -9.675460329952532343E-02,
            -2.431275407194161683E+01,
            -1.947621998306889176E+03,
            -6.059852197160773639E+04,
            -7.076806952837779823E+05,
            -2.417656749061154155E+06,
            -7.834914590078311336E+05
    };

    /** Denominator coefficients to calculate f(t) in region 5. */
    private static double[] FD5 = new double[] {
            1.000000000000000000E+00,
            2.548289012949732752E+02,
            2.099761536857815105E+04,
            6.924122509827708985E+05,
            9.178823229918143780E+06,
            4.292733255630186679E+07,
            4.803294184260528342E+07
    };

    /** Numerator coefficients to calculate g(t) in region 3. */
    private static double[] GN3 = new double[] {
            1.013206188102747985E-01,
            4.445338275505123778E+00,
            5.311228134809894481E+01,
            1.991828186789025318E+02,
            1.962320379716626191E+02,
            2.054214324985006303E+01
    };

    /** Denominator coefficients to calculate g(t) in region 3. */
    private static double[] GD3 = new double[] {
            1.000000000000000000E+00,
            4.539250196736893605E+01,
            5.835905757164290666E+02,
            2.544731331818221034E+03,
            3.481121478565452837E+03,
            1.013794833960028555E+03
    };

    /** Numerator coefficients to calculate g(t) in region 4. */
    private static double[] GN4 = new double[] {
            1.01321161761804586E-01,
            7.11205001789782823E+00,
            1.40959617911315524E+02,
            9.08311749529593938E+02,
            1.59268006085353864E+03,
            3.13330163068755950E+02
    };

    /** Denominator coefficients to calculate g(t) in region 4. */
    private static double[] GD4 = new double[] {
            1.00000000000000000E+00,
            7.17128596939302198E+01,
            1.49051922797329229E+03,
            1.06729678030583897E+04,
            2.41315567213369742E+04,
            1.15149832376260604E+04
    };

    /** Numerator coefficients to calculate g(t) in region 5. */
    private static double[] GN5 = new double[] {
            -1.53989733819769316E-01,
            -4.31710157823357568E+01,
            -3.87754141746378493E+03,
            -1.35678867813756347E+05,
            -1.77758950838029676E+06,
            -6.66907061668636416E+06,
            -1.72590224654836845E+06
    };
    
    /** Denominator coefficients to calculate g(t) in region 5. */
    private static double[] GD5 = new double[] {
            1.00000000000000000E+00,
            2.86733194975899483E+02,
            2.69183180396242536E+04,
            1.02878693056687506E+06,
            1.62095600500231646E+07,
            9.38695862531635179E+07,
            1.40622441123580005E+08
    };
    // {@formatter:on}

    /** Threshold to consider input to be a trivial straight or circle arc. The value is half a degree. */
    private static final double ANGLE_TOLERANCE = 2.0 * Math.PI / 720.0;

    /** Stopping tolerance for the Secant method to find optimal theta values. */
    private static final double SECANT_TOLERANCE = 1e-8;

    /** Utility class. */
    private Clothoid()
    {
        // do not instantiate
    }

    /**
     * Create clothoid between two directed points. This method is based on the procedure in:<br>
     * <br>
     * Dale Connor and Lilia Krivodonova (2014) "Interpolation of two-dimensional curves with Euler spirals", Journal of
     * Computational and Applied Mathematics, Volume 261, 1 May 2014, pp. 320-332.<br>
     * <br>
     * Which applies the theory proven in:<br>
     * <br>
     * D.J. Waltona and D.S. Meek (2009) "G<sup>1</sup> interpolation with a single Cornu spiral segment", Journal of
     * Computational and Applied Mathematics, Volume 223, Issue 1, 1 January 2009, pp. 86-96.<br>
     * <br>
     * This procedure guarantees that the resulting line has the minimal angle rotation that is required to connect the points.
     * If the points approximate a straight line or circle, with a tolerance of up to half a degree, those respective lines are
     * created. The numerical approximation of the underlying Fresnal integral is different from the paper. See
     * {@code fresnal()}.
     * @param start DirectedPoint; start point.
     * @param end DirectedPoint; end point.
     * @param numSegments int; number of segments.
     * @return ClothoidInfo; clothoid information including line through the directed points.
     * @see <a href="https://www.sciencedirect.com/science/article/pii/S0377042713006286">Connor and Krivodonova (2014)</a>
     * @see <a href="https://www.sciencedirect.com/science/article/pii/S0377042704000925">Waltona and Meek (2009)</a>
     */
    public static ClothoidInfo clothoidPoints(final DirectedPoint start, final DirectedPoint end, final int numSegments)
    {
        Throw.whenNull(start, "Start may not be null.");
        Throw.whenNull(end, "End may not be null.");
        Throw.when(numSegments < 2, IllegalArgumentException.class, "Number of segments must be at least 2.");

        double dx = end.x - start.x;
        double dy = end.y - start.y;
        double d2 = Math.hypot(dx, dy); // length of straight line from start to end
        double d = Math.atan2(dy, dx); // angle of line through start and end points

        double phi1 = normalizeAngle(d - start.dirZ);
        double phi2 = normalizeAngle(end.dirZ - d);
        double phi1Abs = Math.abs(phi1);
        double phi2Abs = Math.abs(phi2);

        if (phi1Abs < ANGLE_TOLERANCE && phi2Abs < ANGLE_TOLERANCE)
        {
            // Straight
            OtsPoint3d startPoint = new OtsPoint3d(start.x, start.y, start.z);
            OtsPoint3d endPoint = new OtsPoint3d(end.x, end.y, end.z);
            OtsLine3d line = Try.assign(() -> new OtsLine3d(startPoint, endPoint),
                    "Unable to create straight line from start to end point"
                            + " (which is done as the points and their directions are on a line).");
            return new ClothoidInfo(line, LinearDensity.ZERO, end, LinearDensity.ZERO,
                    Length.instantiateSI(start.distance(end)), Length.POSITIVE_INFINITY);
        }
        else if (Math.abs(phi2 - phi1) < ANGLE_TOLERANCE)
        {
            // Circle, as points are (nearly) on a circle arc
            return makeCircle(start, end, d2, phi1, numSegments);
        }

        // The algorithm assumes |phi2| to be larger than |phi1|. If this is not the case, the clothoid is created in the
        // opposite direction.
        DirectedPoint p1;
        DirectedPoint p2;
        boolean opposite;
        if (phi2Abs < phi1Abs)
        {
            opposite = true;
            double phi3 = phi1;
            phi1 = -phi2;
            phi2 = -phi3;
            p1 = end;
            p2 = start;
            dx = -dx;
            dy = -dy;
        }
        else
        {
            opposite = false;
            p1 = start;
            p2 = end;
        }

        // The algorithm assumes 0 < phi2 < pi. If this is not the case, the input and output are reflected on 'd'.
        boolean reflect = false;
        if (phi2 < 0 || phi2 > Math.PI)
        {
            reflect = true;
            phi1 = -phi1;
            phi2 = -phi2;
        }

        // h(phi1, phi2) guarantees for negative values along with 0 < phi1 < phi2 < pi, that a C-shaped clothoid exists.
        // In our numerical case, this is an indication rather than a guarantee, hence getTheta() may shift shape.
        double[] cs = fresnel(alphaToT(phi1 + phi2));
        double h = cs[1] * Math.cos(phi1) - cs[0] * Math.sin(phi1);
        boolean cShape = 0 < phi1 && phi1 < phi2 && phi2 < Math.PI && h < 0; // otherwise, S-shape
        double theta = getTheta(phi1, phi2, cShape);
        double aSign = cShape ? -1.0 : 1.0;
        double thetaSign = -aSign;

        double v1 = theta + phi1 + phi2;
        double v2 = theta + phi1;
        double[] cs0 = fresnel(alphaToT(theta));
        double[] cs1 = fresnel(alphaToT(v1));
        double a = d2 / ((cs1[1] + aSign * cs0[1]) * Math.sin(v2) + (cs1[0] + aSign * cs0[0]) * Math.cos(v2));

        double[] t0; // unit vector from the origin of the clothoid, towards the positive side
        double[] n0; // normal unit vector to t0
        dx /= d2; // normalized
        dy /= d2;
        if (reflect)
        {
            // reflect t0 and n0 on 'd' so that the created output clothoid is reflected back after input was reflected
            t0 = new double[] {Math.cos(-v2) * dx + Math.sin(-v2) * dy, -Math.sin(-v2) * dx + Math.cos(-v2) * dy};
            n0 = new double[] {-t0[1], t0[0]};
        }
        else
        {
            t0 = new double[] {Math.cos(v2) * dx + Math.sin(v2) * dy, -Math.sin(v2) * dx + Math.cos(v2) * dy};
            n0 = new double[] {t0[1], -t0[0]};
        }

        double alphaMin = thetaSign * theta;
        // alphaMax = theta + phi1 + phi2, which is v1

        OtsLine3d line = makeLine(alphaMin, v1, a, p1, p2, t0, n0, numSegments);
        line = opposite ? line.reverse() : line;
        LinearDensity startCurvature = LinearDensity.instantiateSI(Math.PI * alphaToT(alphaMin) / a);
        LinearDensity endCurvature = LinearDensity.instantiateSI(Math.PI * alphaToT(v1) / a);
        Length length = Length.instantiateSI(a * (alphaToT(v1) - alphaToT(alphaMin)));
        return new ClothoidInfo(line, startCurvature, end, endCurvature, length, Length.instantiateSI(a));
    }

    /**
     * Create clothoid from one point based on curvature and length. This method calculates the A-value as
     * <i>sqrt(L/|k2-k1|)</i>, where <i>L</i> is the length of the resulting clothoid, and <i>k2</i> and <i>k1</i> are the end
     * and start curvature.
     * @param start DirectedPoint; start point.
     * @param length Length; Length of the resulting clothoid.
     * @param startCurvature LinearDensity; start curvature.
     * @param endCurvature LinearDensity; end curvature;
     * @param numSegments int; number of segments.
     * @return ClothoidInfo; clothoid information including line through the start point obeying the curvature and A-value.
     */
    public static ClothoidInfo clothoidLength(final DirectedPoint start, final Length length,
            final LinearDensity startCurvature, final LinearDensity endCurvature, final int numSegments)
    {
        Length a = Length.instantiateSI(Math.sqrt(length.si / Math.abs(endCurvature.si - startCurvature.si)));
        return clothoidA(start, a, startCurvature, endCurvature, numSegments);
    }

    /**
     * Create clothoid from one point based on curvature and A-value.
     * @param start DirectedPoint; start point.
     * @param a Length; A-value.
     * @param startCurvature LinearDensity; start curvature.
     * @param endCurvature LinearDensity; end curvature;
     * @param numSegments int; number of segments.
     * @return ClothoidInfo; clothoid information including line through the start point obeying the curvature and A-value.
     */
    public static ClothoidInfo clothoidA(final DirectedPoint start, final Length a, final LinearDensity startCurvature,
            final LinearDensity endCurvature, final int numSegments)
    {
        Throw.whenNull(start, "Start may not be null.");
        Throw.whenNull(a, "\"A\"-value may not be null.");
        Throw.whenNull(startCurvature, "Start curvature may not be null.");
        Throw.whenNull(endCurvature, "End curvature may not be null.");
        Throw.when(startCurvature.eq(endCurvature), IllegalArgumentException.class, "Start and and curvature are equal, "
                + "which creates a clothoid of length 0. Please create a circle arc instead.");
        Throw.when(numSegments < 2, IllegalArgumentException.class, "Number of segments must be at least 2.");

        double l1 = a.si * a.si * startCurvature.si;
        double l2 = a.si * a.si * endCurvature.si;
        double alphaMin = Math.abs(l1) * startCurvature.si / 2.0;
        double alphaMax = Math.abs(l2) * endCurvature.si / 2.0;

        double ang = normalizeAngle(start.dirZ) - Math.abs(alphaMin);
        double[] t0 = new double[] {Math.cos(ang), Math.sin(ang)};
        double[] n0 = new double[] {t0[1], -t0[0]};
        Direction endDirection = Direction.instantiateSI(ang + Math.abs(alphaMax));
        if (startCurvature.si > endCurvature.si)
        {
            // In these cases the algorithm works in the negative direction. We need to flip over the line through the start
            // point that runs perpendicular to the start direction.
            double m = Math.tan(start.dirZ + Math.PI / 2.0);

            // Linear algebra flipping, see: https://math.stackexchange.com/questions/525082/reflection-across-a-line
            double onePlusMm = 1.0 + m * m;
            double oneMinusMm = 1.0 - m * m;
            double mmMinusOne = m * m - 1.0;
            double twoM = 2.0 * m;
            t0 = new double[] {(oneMinusMm * t0[0] + 2 * m * t0[1]) / onePlusMm,
                    (twoM * t0[0] + mmMinusOne * t0[1]) / onePlusMm};
            n0 = new double[] {(oneMinusMm * n0[0] + 2 * m * n0[1]) / onePlusMm,
                    (twoM * n0[0] + mmMinusOne * n0[1]) / onePlusMm};

            double ang2 = Math.atan2(t0[1], t0[0]);
            endDirection = Direction.instantiateSI(ang2 - Math.abs(alphaMax) + Math.PI);
        }

        // Scale 'a', due to parameter conversion between C(alpha)/S(alpha) and C(t)/S(t); t = sqrt(2*alpha/pi).
        double aa = a.si * Math.sqrt(Math.PI);

        OtsLine3d line = makeLine(alphaMin, alphaMax, aa, start, null, t0, n0, numSegments);
        Length length = Length.instantiateSI(a.si * a.si * Math.abs(endCurvature.si - startCurvature.si));
        OtsPoint3d endPoint = Try.assign(() -> line.get(line.size() - 1), "Line does not have an end point.");
        DirectedPoint end = new DirectedPoint(endPoint.x, endPoint.y, endPoint.z, 0.0, 0.0, endDirection.si);

        return new ClothoidInfo(line, startCurvature, end, endCurvature, length, a);
    }

    /**
     * Create clothoid line. Note that alphaMin &gt; alphaMax is allowed, as the shape is then simply created in reverse. The
     * rotation and flipping of the clothoid is determined by t0 and n0. The first point is provided to shift the clothoid. An
     * optional last point is used to linearly shift over progression of alpha the points with some numerical error correction
     * such that the end of the line is exactly at the last point. The scaling of the clothoid is given by value a. Finally, the
     * range in direction angle is given by alphaMin and alphaMax.
     * @param alphaMin double; minimum number of radians that is moved on to a side of the full clothoid.
     * @param alphaMax double; maximum number of radians that is moved on to a side of the full clothoid.
     * @param a double; shape or scaling factor for the clothoid
     * @param p1 DirectedPoint; from point.
     * @param p2 DirectedPoint; to point, may be {@code null} as it is only used for corrective shift.
     * @param t0 double[]; unit vector from the origin of the clothoid, towards the positive side.
     * @param n0 double[]; normal unit vector to t0.
     * @param numSegments int; number of segments.
     * @return OtsLine3d; clothoid line.
     */
    private static OtsLine3d makeLine(final double alphaMin, final double alphaMax, final double a, final DirectedPoint p1,
            final DirectedPoint p2, final double[] t0, final double[] n0, final int numSegments)
    {
        double step = (alphaMax - alphaMin) / numSegments;
        List<OtsPoint3d> points = new ArrayList<>(numSegments + 1);

        // Create first point to figure out the required overall shift
        double[] csMin = fresnel(alphaToT(alphaMin));
        double xMin = a * (csMin[0] * t0[0] - csMin[1] * n0[0]);
        double yMin = a * (csMin[0] * t0[1] - csMin[1] * n0[1]);
        double dx = p1.x - xMin;
        double dy = p1.y - yMin;
        points.add(new OtsPoint3d(xMin + dx, yMin + dy, 0.0));

        // Due to numerical precision, we linearly scale over alpha such that the final point is exactly on p2
        double xShift = 0.0;
        double yShift = 0.0;
        if (p2 != null)
        {
            // Create last point to figure out linear shift over alpha such that last point ends up at p2 (if any)
            double[] csMax = fresnel(alphaToT(alphaMax));
            double xMax = a * (csMax[0] * t0[0] - csMax[1] * n0[0]);
            double yMax = a * (csMax[0] * t0[1] - csMax[1] * n0[1]);
            xShift = p2.x - (xMax + dx);
            yShift = p2.y - (yMax + dy);
        }

        double dAlpha = alphaMax - alphaMin;
        for (int i = 1; i <= numSegments; i++)
        {
            double alpha = alphaMin + i * step;
            double r = (alpha - alphaMin) / dAlpha;
            double[] cs = fresnel(alphaToT(alpha));
            points.add(new OtsPoint3d(dx + a * (cs[0] * t0[0] - cs[1] * n0[0]) + r * xShift,
                    dy + a * (cs[0] * t0[1] - cs[1] * n0[1]) + r * yShift, 0.0));
        }

        return Try.assign(() -> new OtsLine3d(points), "Unable to create OtsLine3d.");
    }

    /**
     * Normalizes the angle to be in the range [-pi pi].
     * @param angle double; angle.
     * @return double; angle in the range [-pi pi].
     */
    private static double normalizeAngle(final double angle)
    {
        double out = angle;
        while (out > Math.PI)
        {
            out -= 2 * Math.PI;
        }
        while (out < -Math.PI)
        {
            out += 2 * Math.PI;
        }
        return out;
    }

    /**
     * Create a circle arc. This is done instead of a clothoid when the points and their directions are on a circle arc.
     * @param start DirectedPoint; start point for the circle arc.
     * @param end DirectedPoint; end point for the circle arc.
     * @param d2 double; length of straight line from start to end.
     * @param phi1 double; angle between start direction and straight line from start to end 'd'.
     * @param numSegments int; number of segments.
     * @return OtsLine3d; line.
     */
    private static ClothoidInfo makeCircle(final DirectedPoint start, final DirectedPoint end, final double d2,
            final double phi1, final int numSegments)
    {
        double r = .5 * d2 / Math.sin(phi1);
        double cosStartDirection = Math.cos(start.dirZ);
        double sinStartDirection = Math.sin(start.dirZ);
        double ang = Math.PI / 2.0;
        double cosAng = Math.cos(ang);
        double sinAng = Math.sin(ang);
        double x0 = start.x - r * (cosStartDirection * cosAng + sinStartDirection * sinAng);
        double y0 = start.y - r * (cosStartDirection * -sinAng + sinStartDirection * cosAng);
        double from = Math.atan2(start.y - y0, start.x - x0);
        double to = Math.atan2(end.y - y0, end.x - x0);
        if (r < 0 && to > from)
        {
            to = to - 2.0 * Math.PI;
        }
        else if (r > 0 && to < from)
        {
            to = to + 2.0 * Math.PI;
        }
        double step = (to - from) / numSegments;
        double absR = Math.abs(r);
        List<OtsPoint3d> points = new ArrayList<>();
        for (int i = 0; i < numSegments + 1; i++)
        {
            double s = from + i * step;
            points.add(new OtsPoint3d(x0 + Math.cos(s) * absR, y0 + Math.sin(s) * absR, 0.0));
        }
        OtsLine3d line = Try.assign(() -> new OtsLine3d(points), "Unable to create OtsLine3d.");
        LinearDensity curvature = LinearDensity.instantiateSI(1.0 / r);
        Length length = Length.instantiateSI(Math.abs((to - from) * r));
        return new ClothoidInfo(line, curvature, end, curvature, length, Length.ZERO);
    }

    /**
     * Returns theta value given shape to use. If no such value is found, the other shape may be attempted.
     * @param phi1 double; phi1.
     * @param phi2 double; phi2.
     * @param cShape boolean; C-shaped, or S-shaped otherwise.
     * @return double; theta value; the number of radians that is moved on to a side of the full clothoid.
     */
    private static double getTheta(final double phi1, final double phi2, final boolean cShape)
    {
        double sign, phiMin, phiMax;
        if (cShape)
        {
            double lambda = (1 - Math.cos(phi1)) / (1 - Math.cos(phi2));
            phiMin = 0.0;
            phiMax = (lambda * lambda * (phi1 + phi2)) / (1 - (lambda * lambda));
            sign = -1.0;
        }
        else
        {
            phiMin = Math.max(0, -phi1);
            phiMax = Math.PI / 2 - phi1;
            sign = 1;
        }

        double fMin = fTheta(phiMin, phi1, phi2, sign);
        double fMax = fTheta(phiMax, phi1, phi2, sign);
        if (fMin * fMax > 0)
        {
            throw new RuntimeException("f(phiMin) and f(phiMax) have the same sign, we cant find f(theta) = 0 between them.");
        }

        // Find optimum using Secant method, see https://en.wikipedia.org/wiki/Secant_method
        double x0 = phiMin;
        double x1 = phiMax;
        double x2 = 0;
        for (int i = 0; i < 100; i++) // max 100 iterations, otherwise use latest x2 value
        {
            double f1 = fTheta(x1, phi1, phi2, sign);
            x2 = x1 - f1 * (x1 - x0) / (f1 - fTheta(x0, phi1, phi2, sign));
            x2 = Math.max(Math.min(x2, phiMax), phiMin); // this line is an essential addition to keep the algorithm at bay
            x0 = x1;
            x1 = x2;
            if (Math.abs(x0 - x1) < SECANT_TOLERANCE || Math.abs(x0 / x1 - 1) < SECANT_TOLERANCE
                    || Math.abs(f1) < SECANT_TOLERANCE)
            {
                return x2;
            }
        }

        return x2;
    }

    /**
     * Function who's solution <i>f</i>(<i>theta</i>) = 0 for the given value of <i>phi1</i> and <i>phi2</i> gives the angle
     * that solves fitting a C-shaped clothoid through two points. This assumes that <i>sign</i> = -1. If <i>sign</i> = 1, this
     * changes to <i>g</i>(<i>theta</i>) = 0 being a solution for an S-shaped clothoid.
     * @param theta double; angle defining the curvature of the resulting clothoid.
     * @param phi1 double; angle between the line through both end points, and the direction of the first point.
     * @param phi2 double; angle between the line through both end points, and the direction of the last point.
     * @param sign double; 1 for C-shaped, -1 for S-shaped.
     * @return double; <i>f</i>(<i>theta</i>) for <i>sign</i> = -1, or <i>g</i>(<i>theta</i>) for <i>sign</i> = 1.
     */
    private static double fTheta(final double theta, final double phi1, final double phi2, final double sign)
    {
        double thetaPhi1 = theta + phi1;
        double[] cs0 = fresnel(alphaToT(theta));
        double[] cs1 = fresnel(alphaToT(thetaPhi1 + phi2));
        return (cs1[1] + sign * cs0[1]) * Math.cos(thetaPhi1) - (cs1[0] + sign * cs0[0]) * Math.sin(thetaPhi1);
    }

    /**
     * Approximate the Fresnel integral. The method used is based on Cody (1968). This method applies rational approximation to
     * approximate the clothoid. For clothoid rotation beyond 1.6 rad, this occurs in polar form. The polar form is robust for
     * arbitrary large numbers, unlike polynomial expansion, and will at a large threshold converge to (0.5, 0.5). There are 5
     * regions with different fitted values for the rational approximations, in Cartesian or polar form.<br>
     * <br>
     * W.J. Cody (1968) Chebyshev approximations for the Fresnel integrals. Mathematics of Computation, Vol. 22, Issue 102, pp.
     * 450–453.
     * @param x double; length along the standard Fresnel integral (no scaling).
     * @return double[]; array with two double values c and s
     * @see <a href="https://www.ams.org/journals/mcom/1968-22-102/S0025-5718-68-99871-2/S0025-5718-68-99871-2.pdf">Cody
     *      (1968)</a>
     */
    private static double[] fresnel(final double x)
    {
        final double t = Math.abs(x);
        double cc, ss;
        if (t < 1.2)
        {
            cc = t * ratioEval(t, CN1, +1) / ratioEval(t, CD1, +1);
            ss = t * t * t * ratioEval(t, SN1, +1) / ratioEval(t, SD1, +1);
        }
        else if (t < 1.6)
        {
            cc = t * ratioEval(t, CN2, +1) / ratioEval(t, CD2, +1);
            ss = t * t * t * ratioEval(t, SN2, +1) / ratioEval(t, SD2, +1);
        }
        else if (t < 1.9)
        {
            double pitt2 = Math.PI * t * t / 2;
            double sinpitt2 = Math.sin(pitt2);
            double cospitt2 = Math.cos(pitt2);
            double ft = (1 / t) * ratioEval(t, FN3, -1) / ratioEval(t, FD3, -1);
            double gt = (1 / (t * t * t)) * ratioEval(t, GN3, -1) / ratioEval(t, GD3, -1);
            cc = .5 + ft * sinpitt2 - gt * cospitt2;
            ss = .5 - ft * cospitt2 - gt * sinpitt2;
        }
        else if (t < 2.4)
        {
            double pitt2 = Math.PI * t * t / 2;
            double sinpitt2 = Math.sin(pitt2);
            double cospitt2 = Math.cos(pitt2);
            double tinv = 1 / t;
            double tttinv = tinv * tinv * tinv;
            double ft = tinv * ratioEval(t, FN4, -1) / ratioEval(t, FD4, -1);
            double gt = tttinv * ratioEval(t, GN4, -1) / ratioEval(t, GD4, -1);
            cc = .5 + ft * sinpitt2 - gt * cospitt2;
            ss = .5 - ft * cospitt2 - gt * sinpitt2;
        }
        else
        {
            double pitt2 = Math.PI * t * t / 2;
            double sinpitt2 = Math.sin(pitt2);
            double cospitt2 = Math.cos(pitt2);
            double piinv = 1 / Math.PI;
            double tinv = 1 / t;
            double tttinv = tinv * tinv * tinv;
            double ttttinv = tttinv * tinv;
            double ft = tinv * (piinv + (ttttinv * ratioEval(t, FN5, -1) / ratioEval(t, FD5, -1)));
            double gt = tttinv * ((piinv * piinv) + (ttttinv * ratioEval(t, GN5, -1) / ratioEval(t, GD5, -1)));
            cc = .5 + ft * sinpitt2 - gt * cospitt2;
            ss = .5 - ft * cospitt2 - gt * sinpitt2;
        }
        if (x < 0)
        {
            cc = -cc;
            ss = -ss;
        }

        return new double[] {cc, ss};
    }

    /**
     * Evaluate numerator or denominator of rational approximation.
     * @param t double; value along the clothoid.
     * @param coef double[]; rational approximation coefficients.
     * @param sign double; sign of exponent, +1 for Cartesian rational approximation, -1 for polar approximation.
     * @return double; numerator or denominator of rational approximation.
     */
    private static double ratioEval(final double t, final double[] coef, final double sign)
    {
        double value = 0;
        for (int s = 0; s < coef.length; s++)
        {
            value += coef[s] * Math.pow(t, sign * 4 * s);
        }
        return value;
    }

    /**
     * Performs alpha to t variable change.
     * @param alpha double; alpha value, must be positive.
     * @return double; t value (length along the Fresnel integral, also known as x).
     */
    private static double alphaToT(final double alpha)
    {
        return alpha >= 0 ? Math.sqrt(alpha * 2.0 / Math.PI) : -Math.sqrt(-alpha * 2.0 / Math.PI);
    }

    /**
     * Information on clothoid. All information, except for the polyline, are theoretical value. These values numerically
     * derived from the polyline will not be exact.
     * <p>
     * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    public static class ClothoidInfo
    {
        /** Line. */
        private final OtsLine3d line;

        /** Start curvature. */
        private final LinearDensity startCurvature;

        /** End point. */
        private final DirectedPoint endPoint;

        /** End curvature. */
        private final LinearDensity endCurvature;

        /** Length. */
        private final Length length;

        /** A-value; scaling factor for the clothoid. */
        private final Length a;

        /**
         * Constructor.
         * @param line OtsLine3d; start curvature.
         * @param startCurvature LinearDensity;
         * @param endPoint DirectedPoint; 3nd point.
         * @param endCurvature LinearDensity; end curvature.
         * @param length Length; length.
         * @param a Length; A-value; scaling factor for the clothoid.
         */
        public ClothoidInfo(final OtsLine3d line, final LinearDensity startCurvature, final DirectedPoint endPoint,
                final LinearDensity endCurvature, final Length length, final Length a)
        {
            this.line = line;
            this.startCurvature = startCurvature;
            this.endPoint = endPoint;
            this.endCurvature = endCurvature;
            this.length = length;
            this.a = a;
        }

        /**
         * Returns the polyline.
         * @return OtsLine3d; line.
         */
        public OtsLine3d getLine()
        {
            return this.line;
        }

        /**
         * Returns the theoretical start curvature.
         * @return LinearDensity; start curvature.
         */
        public LinearDensity getStartCurvature()
        {
            return this.startCurvature;
        }

        /**
         * Returns the end point.
         * @return DirectedPoint; end point.
         */
        public DirectedPoint getEndPoint()
        {
            return this.endPoint;
        }

        /**
         * Returns the theoretical end curvature.
         * @return LinearDensity; end curvature.
         */
        public LinearDensity getEndCurvature()
        {
            return this.endCurvature;
        }

        /**
         * Returns the theoretical length.
         * @return Length; length;
         */
        public Length getLength()
        {
            return this.length;
        }

        /**
         * Returns the theoretical A-value.
         * @return Length; A-value.
         */
        public Length getA()
        {
            return this.a;
        }

    }

}
