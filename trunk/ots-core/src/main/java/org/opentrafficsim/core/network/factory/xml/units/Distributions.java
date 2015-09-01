package org.opentrafficsim.core.network.factory.xml.units;

import nl.tudelft.simulation.jstats.distributions.DistBeta;
import nl.tudelft.simulation.jstats.distributions.DistConstant;
import nl.tudelft.simulation.jstats.distributions.DistContinuous;
import nl.tudelft.simulation.jstats.distributions.DistErlang;
import nl.tudelft.simulation.jstats.distributions.DistExponential;
import nl.tudelft.simulation.jstats.distributions.DistGamma;
import nl.tudelft.simulation.jstats.distributions.DistLogNormal;
import nl.tudelft.simulation.jstats.distributions.DistNormal;
import nl.tudelft.simulation.jstats.distributions.DistPearson5;
import nl.tudelft.simulation.jstats.distributions.DistPearson6;
import nl.tudelft.simulation.jstats.distributions.DistTriangular;
import nl.tudelft.simulation.jstats.distributions.DistUniform;
import nl.tudelft.simulation.jstats.distributions.DistWeibull;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

import org.opentrafficsim.core.OTS_DIST;
import org.opentrafficsim.core.OTS_SCALAR;
import org.opentrafficsim.core.network.NetworkException;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public final class Distributions implements OTS_SCALAR, OTS_DIST
{
    /** Utility class. */
    private Distributions()
    {
        // do not instantiate
    }

    /** TODO include in GLOBAL tag. */
    private static final StreamInterface STREAM = new MersenneTwister();

    /**
     * parse a set of comma-separated values, e.g., <code>10.0, 4, 5.23</code>.
     * @param s the string to parse.
     * @return array of double values.
     */
    private static double[] parseDoubleArgs(final String s)
    {
        String[] ss = s.split(",");
        double[] d = new double[ss.length];
        for (int i = 0; i < ss.length; i++)
        {
            d[i] = Double.parseDouble(ss[i]);
        }
        return d;
    }

    /**
     * Parse a continuous distribution.
     * @param ds the name of the distribution, e.g. UNIF.
     * @param args the parameters of the distribution, e.g. {1.0, 2.0}.
     * @return the generated distribution.
     * @throws NetworkException in case distribution unknown or parameter number does not match.
     */
    private static DistContinuous makeDistContinuous(final String ds, final double[] args) throws NetworkException
    {
        try
        {
            switch (ds)
            {
                case "CONST":
                case "CONSTANT":
                    return new DistConstant(STREAM, args[0]);

                case "EXPO":
                case "EXPONENTIAL":
                    return new DistExponential(STREAM, args[0]);

                case "TRIA":
                case "TRIANGULAR":
                    return new DistTriangular(STREAM, args[0], args[1], args[2]);

                case "NORM":
                case "NORMAL":
                    return new DistNormal(STREAM, args[0], args[1]);

                case "BETA":
                    return new DistBeta(STREAM, args[0], args[1]);

                case "ERLANG":
                    return new DistErlang(STREAM, (int) args[0], args[1]);

                case "GAMMA":
                    return new DistGamma(STREAM, args[0], args[1]);

                case "LOGN":
                case "LOGNORMAL":
                    return new DistLogNormal(STREAM, args[0], args[1]);

                case "PEARSON5":
                    return new DistPearson5(STREAM, args[0], args[1]);

                case "PEARSON6":
                    return new DistPearson6(STREAM, args[0], args[1], args[2]);

                case "UNIF":
                case "UNIFORM":
                    return new DistUniform(STREAM, args[0], args[1]);

                case "WEIB":
                case "WEIBULL":
                    return new DistWeibull(STREAM, args[0], args[1]);

                default:
                    throw new NetworkException("makeDistContinuous - unknown distribution function " + ds);
            }
        }
        catch (IndexOutOfBoundsException e)
        {
            throw new NetworkException("makeDistContinuous - wrong number of parameters for distribution function " + ds);
        }
    }

    /**
     * Parse a relative length distribution, e.g. <code>UNIFORM(1, 3) m</code>.
     * @param s the string to be parsed.
     * @return a typed continuous random distribution.
     * @throws NetworkException in case of a parse error.
     */
    public static LengthContinuousDist.Rel parseLengthDistRel(final String s) throws NetworkException
    {
        String[] s1 = s.split("\\(");
        String ds = s1[0];
        String[] s2 = s1[1].split("\\)");
        String unit = LengthUnits.parseLengthUnit(s2[1]);
        double[] args = parseDoubleArgs(s2[0]);
        DistContinuous dist = makeDistContinuous(ds, args);
        return new LengthContinuousDist.Rel(dist, LengthUnits.LENGTH_UNITS.get(unit));
    }

    /**
     * Parse an absolute length distribution, e.g. <code>UNIFORM(1, 3) m</code>.
     * @param s the string to be parsed.
     * @return a typed continuous random distribution.
     * @throws NetworkException in case of a parse error.
     */
    public static LengthContinuousDist.Abs parseLengthDistAbs(final String s) throws NetworkException
    {
        String[] s1 = s.split("\\(");
        String ds = s1[0];
        String[] s2 = s1[1].split("\\)");
        String unit = LengthUnits.parseLengthUnit(s2[1]);
        double[] args = parseDoubleArgs(s2[0]);
        DistContinuous dist = makeDistContinuous(ds, args);
        return new LengthContinuousDist.Abs(dist, LengthUnits.LENGTH_UNITS.get(unit));
    }

    /**
     * Parse a relative time distribution, e.g. <code>UNIFORM(1, 3) s</code>.
     * @param s the string to be parsed.
     * @return a typed continuous random distribution.
     * @throws NetworkException in case of a parse error.
     */
    public static TimeContinuousDist.Rel parseTimeDistRel(final String s) throws NetworkException
    {
        String[] s1 = s.split("\\(");
        String ds = s1[0];
        String[] s2 = s1[1].split("\\)");
        String unit = TimeUnits.parseTimeUnit(s2[1]);
        double[] args = parseDoubleArgs(s2[0]);
        DistContinuous dist = makeDistContinuous(ds, args);
        return new TimeContinuousDist.Rel(dist, TimeUnits.TIME_UNITS.get(unit));
    }

    /**
     * Parse an absolute time distribution, e.g. <code>UNIFORM(1, 3) s</code>.
     * @param s the string to be parsed.
     * @return a typed continuous random distribution.
     * @throws NetworkException in case of a parse error.
     */
    public static TimeContinuousDist.Abs parseTimeDistAbs(final String s) throws NetworkException
    {
        String[] s1 = s.split("\\(");
        String ds = s1[0];
        String[] s2 = s1[1].split("\\)");
        String unit = TimeUnits.parseTimeUnit(s2[1]);
        double[] args = parseDoubleArgs(s2[0]);
        DistContinuous dist = makeDistContinuous(ds, args);
        return new TimeContinuousDist.Abs(dist, TimeUnits.TIME_UNITS.get(unit));
    }

    /**
     * Parse a relative speed distribution, e.g. <code>TRIANGULAR(80, 90, 110) km/h</code>.
     * @param s the string to be parsed.
     * @return a typed continuous random distribution.
     * @throws NetworkException in case of a parse error.
     */
    public static SpeedContinuousDist.Rel parseSpeedDistRel(final String s) throws NetworkException
    {
        String[] s1 = s.split("\\(");
        String ds = s1[0];
        String[] s2 = s1[1].split("\\)");
        String unit = SpeedUnits.parseSpeedUnit(s2[1]);
        double[] args = parseDoubleArgs(s2[0]);
        DistContinuous dist = makeDistContinuous(ds, args);
        return new SpeedContinuousDist.Rel(dist, SpeedUnits.SPEED_UNITS.get(unit));
    }

    /**
     * Parse an absolute speed distribution, e.g. <code>TRIANGULAR(80, 90, 110) km/h</code>.
     * @param s the string to be parsed.
     * @return a typed continuous random distribution.
     * @throws NetworkException in case of a parse error.
     */
    public static SpeedContinuousDist.Abs parseSpeedDistAbs(final String s) throws NetworkException
    {
        String[] s1 = s.split("\\(");
        String ds = s1[0];
        String[] s2 = s1[1].split("\\)");
        String unit = SpeedUnits.parseSpeedUnit(s2[1]);
        double[] args = parseDoubleArgs(s2[0]);
        DistContinuous dist = makeDistContinuous(ds, args);
        return new SpeedContinuousDist.Abs(dist, SpeedUnits.SPEED_UNITS.get(unit));
    }

}
