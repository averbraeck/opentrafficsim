package org.opentrafficsim.core.geometry;

/**
 * Utility class to create clothoid lines, in particular the Fresnel integral based on:
 * <ul>
 * <li>W.J. Cody (1968) Chebyshev approximations for the Fresnel integrals. Mathematics of Computation, Vol. 22, Issue 102, pp.
 * 450–453.</li>
 * </ul>
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @see <a href="https://www.ams.org/journals/mcom/1985-44-170/S0025-5718-1985-0777277-6/S0025-5718-1985-0777277-6.pdf">Cody
 *      (1968)</a>
 * @param c C value of Fresnel integral
 * @param s S value of Fresnel integral
 */
public record Fresnel(double c, double s)
{

    // {@formatter:off}
    /** Numerator coefficients to calculate C(t) in region 1. */
    private static final double[] CN1 = new double[] {
            9.999999999999999421E-01,
            -1.994608988261842706E-01, 
            1.761939525434914045E-02,
            -5.280796513726226960E-04, 
            5.477113856826871660E-06
    };

    /** Denominator coefficients to calculate C(t) in region 1. */
    private static final double[] CD1 = new double[] {
            1.000000000000000000E+00,
            4.727921120104532689E-02,
            1.099572150256418851E-03,
            1.552378852769941331E-05,
            1.189389014228757184E-07
    };

    /** Numerator coefficients to calculate C(t) in region 2. */
    private static final double[] CN2 = new double[] {
            1.00000000000111043640E+00,
            -2.07073360335323894245E-01,
            1.91870279431746926505E-02,
            -6.71376034694922109230E-04,
            1.02365435056105864908E-05,
            -5.68293310121870728343E-08
    };

    /** Denominator coefficients to calculate C(t) in region 3. */
    private static final double[] CD2 = new double[] {
            1.00000000000000000000E+00,
            3.96667496952323433510E-02,
            7.88905245052359907842E-04,
            1.01344630866749406081E-05,
            8.77945377892369265356E-08,
            4.41701374065009620393E-10
    };

    /** Numerator coefficients to calculate S(t) in region 1. */
    private static final double[] SN1 = new double[] {
            5.2359877559829887021E-01,
            -7.0748991514452302596E-02,
            3.8778212346368287939E-03,
            -8.4555728435277680591E-05,
            6.7174846662514086196E-07
    };

    /** Denominator coefficients to calculate S(t) in region 1. */
    private static final double[] SD1 = new double[] {
            1.0000000000000000000E+00,
            4.1122315114238422205E-02,
            8.1709194215213447204E-04,
            9.6269087593903403370E-06,
            5.9528122767840998345E-08
    };

    /** Numerator coefficients to calculate S(t) in region 2. */
    private static final double[] SN2 = new double[] {
            5.23598775598344165913E-01,
            -7.37766914010191323867E-02,
            4.30730526504366510217E-03,
            -1.09540023911434994566E-04,
            1.28531043742724820610E-06,
            -5.76765815593088804567E-09
    };

    /** Denominator coefficients to calculate S(t) in region 2. */
    private static final double[] SD2 = new double[] {
            1.00000000000000000000E+00,
            3.53398342167472162540E-02,
            6.18224620195473216538E-04,
            6.87086265718620117905E-06,
            5.03090581246612375866E-08,
            2.05539124458579596075E-10
    };

    /** Numerator coefficients to calculate f(t) in region 3. */
    private static final double[] FN3 = new double[] {
            3.1830975293580985290E-01,
            1.2226000551672961219E+01,
            1.2924886131901657025E+02,
            4.3886367156695547655E+02,
            4.1466722177958961672E+02,
            5.6771463664185116454E+01
    };

    /** Denominator coefficients to calculate f(t) in region 3. */
    private static final double[] FD3 = new double[] {
            1.0000000000000000000E+00,
            3.8713003365583442831E+01,
            4.1674359830705629745E+02,
            1.4740030733966610568E+03,
            1.5371675584895759916E+03,
            2.9113088788847831515E+02
    };

    /** Numerator coefficients to calculate f(t) in region 4. */
    private static final double[] FN4 = new double[] {
            3.183098818220169217E-01,
            1.958839410219691002E+01,
            3.398371349269842400E+02,
            1.930076407867157531E+03,
            3.091451615744296552E+03,
            7.177032493651399590E+02
    };

    /** Denominator coefficients to calculate f(t) in region 4. */
    private static final double[] FD4 = new double[] {
            1.000000000000000000E+00,
            6.184271381728873709E+01,
            1.085350675006501251E+03,
            6.337471558511437898E+03,
            1.093342489888087888E+04,
            3.361216991805511494E+03
    };

    /** Numerator coefficients to calculate f(t) in region 5. */
    private static final double[] FN5 = new double[] {
            -9.675460329952532343E-02,
            -2.431275407194161683E+01,
            -1.947621998306889176E+03,
            -6.059852197160773639E+04,
            -7.076806952837779823E+05,
            -2.417656749061154155E+06,
            -7.834914590078311336E+05
    };

    /** Denominator coefficients to calculate f(t) in region 5. */
    private static final double[] FD5 = new double[] {
            1.000000000000000000E+00,
            2.548289012949732752E+02,
            2.099761536857815105E+04,
            6.924122509827708985E+05,
            9.178823229918143780E+06,
            4.292733255630186679E+07,
            4.803294184260528342E+07
    };

    /** Numerator coefficients to calculate g(t) in region 3. */
    private static final double[] GN3 = new double[] {
            1.013206188102747985E-01,
            4.445338275505123778E+00,
            5.311228134809894481E+01,
            1.991828186789025318E+02,
            1.962320379716626191E+02,
            2.054214324985006303E+01
    };

    /** Denominator coefficients to calculate g(t) in region 3. */
    private static final double[] GD3 = new double[] {
            1.000000000000000000E+00,
            4.539250196736893605E+01,
            5.835905757164290666E+02,
            2.544731331818221034E+03,
            3.481121478565452837E+03,
            1.013794833960028555E+03
    };

    /** Numerator coefficients to calculate g(t) in region 4. */
    private static final double[] GN4 = new double[] {
            1.01321161761804586E-01,
            7.11205001789782823E+00,
            1.40959617911315524E+02,
            9.08311749529593938E+02,
            1.59268006085353864E+03,
            3.13330163068755950E+02
    };

    /** Denominator coefficients to calculate g(t) in region 4. */
    private static final double[] GD4 = new double[] {
            1.00000000000000000E+00,
            7.17128596939302198E+01,
            1.49051922797329229E+03,
            1.06729678030583897E+04,
            2.41315567213369742E+04,
            1.15149832376260604E+04
    };

    /** Numerator coefficients to calculate g(t) in region 5. */
    private static final double[] GN5 = new double[] {
            -1.53989733819769316E-01,
            -4.31710157823357568E+01,
            -3.87754141746378493E+03,
            -1.35678867813756347E+05,
            -1.77758950838029676E+06,
            -6.66907061668636416E+06,
            -1.72590224654836845E+06
    };
    
    /** Denominator coefficients to calculate g(t) in region 5. */
    private static final double[] GD5 = new double[] {
            1.00000000000000000E+00,
            2.86733194975899483E+02,
            2.69183180396242536E+04,
            1.02878693056687506E+06,
            1.62095600500231646E+07,
            9.38695862531635179E+07,
            1.40622441123580005E+08
    };
    // {@formatter:on}

    /**
     * Approximate the Fresnel integral. The method used is based on Cody (1968). This method applies rational approximation to
     * approximate the clothoid. For clothoid rotation beyond 1.6 rad, this occurs in polar form. The polar form is robust for
     * arbitrary large numbers, unlike polynomial expansion, and will at a large threshold converge to (0.5, 0.5). There are 5
     * regions with different fitted values for the rational approximations, in Cartesian or polar form.<br>
     * <br>
     * W.J. Cody (1968) Chebyshev approximations for the Fresnel integrals. Mathematics of Computation, Vol. 22, Issue 102, pp.
     * 450–453.
     * @param x length along the standard Fresnel integral (no scaling).
     * @return array with two double values c and s
     * @see <a href="https://www.ams.org/journals/mcom/1968-22-102/S0025-5718-68-99871-2/S0025-5718-68-99871-2.pdf">Cody
     *      (1968)</a>
     */
    public static Fresnel integral(final double x)
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

        return new Fresnel(cc, ss);
    }

    /**
     * Evaluate numerator or denominator of rational approximation.
     * @param t value along the clothoid.
     * @param coef rational approximation coefficients.
     * @param sign sign of exponent, +1 for Cartesian rational approximation, -1 for polar approximation.
     * @return numerator or denominator of rational approximation.
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

}
