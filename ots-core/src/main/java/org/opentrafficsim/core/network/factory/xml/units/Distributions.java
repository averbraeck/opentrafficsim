package org.opentrafficsim.core.network.factory.xml.units;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.PositionUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Position;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.units.distributions.ContinuousDistDoubleScalar;

import nl.tudelft.simulation.jstats.distributions.DistBeta;
import nl.tudelft.simulation.jstats.distributions.DistConstant;
import nl.tudelft.simulation.jstats.distributions.DistContinuous;
import nl.tudelft.simulation.jstats.distributions.DistErlang;
import nl.tudelft.simulation.jstats.distributions.DistExponential;
import nl.tudelft.simulation.jstats.distributions.DistGamma;
import nl.tudelft.simulation.jstats.distributions.DistLogNormal;
import nl.tudelft.simulation.jstats.distributions.DistNormal;
import nl.tudelft.simulation.jstats.distributions.DistNormalTrunc;
import nl.tudelft.simulation.jstats.distributions.DistPearson5;
import nl.tudelft.simulation.jstats.distributions.DistPearson6;
import nl.tudelft.simulation.jstats.distributions.DistTriangular;
import nl.tudelft.simulation.jstats.distributions.DistUniform;
import nl.tudelft.simulation.jstats.distributions.DistWeibull;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 */
public final class Distributions
{
    /** Utility class. */
    private Distributions()
    {
        // do not instantiate
    }

    /** TODO include in GLOBAL tag. */
    private static final StreamInterface STREAM = new MersenneTwister(2L);

    /**
     * parse a set of comma-separated values, e.g., <code>10.0, 4, 5.23</code>.
     * @param s String; the string to parse.
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
     * @param ds String; the name of the distribution, e.g. UNIF.
     * @param args double[]; the parameters of the distribution, e.g. {1.0, 2.0}.
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

                case "NORMTRUNC":
                case "NORMALTRUNCATED":
                    return new DistNormalTrunc(STREAM, args[0], args[1], args[2], args[3]);

                case "BETA":
                    return new DistBeta(STREAM, args[0], args[1]);

                case "ERLANG":
                    return new DistErlang(STREAM, (int) args[0], (int) args[1]);

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
     * @param s String; the string to be parsed.
     * @return a typed continuous random distribution.
     * @throws NetworkException in case of a parse error.
     */
    public static ContinuousDistDoubleScalar.Rel<Length, LengthUnit> parseLengthDist(final String s) throws NetworkException
    {
        String[] s1 = s.split("\\(");
        String ds = s1[0];
        String[] s2 = s1[1].split("\\)");
        String unit = s2[1].trim();
        double[] args = parseDoubleArgs(s2[0]);
        DistContinuous dist = makeDistContinuous(ds, args);
        return new ContinuousDistDoubleScalar.Rel<Length, LengthUnit>(dist, LengthUnit.BASE.getUnitByAbbreviation(unit));
    }

    /**
     * Parse an absolute length distribution, e.g. <code>UNIFORM(1, 3) m</code>.
     * @param s String; the string to be parsed.
     * @return a typed continuous random distribution.
     * @throws NetworkException in case of a parse error.
     */
    public static ContinuousDistDoubleScalar.Abs<Position, PositionUnit, LengthUnit> parsePositionDist(final String s)
            throws NetworkException
    {
        String[] s1 = s.split("\\(");
        String ds = s1[0];
        String[] s2 = s1[1].split("\\)");
        String unit = s2[1].trim();
        double[] args = parseDoubleArgs(s2[0]);
        DistContinuous dist = makeDistContinuous(ds, args);
        return new ContinuousDistDoubleScalar.Abs<Position, PositionUnit, LengthUnit>(dist,
                PositionUnit.BASE.getUnitByAbbreviation(unit));
    }

    /**
     * Parse a relative time distribution, e.g. <code>UNIFORM(1, 3) s</code>.
     * @param s String; the string to be parsed.
     * @return a typed continuous random distribution.
     * @throws NetworkException in case of a parse error.
     */
    public static ContinuousDistDoubleScalar.Rel<Duration, DurationUnit> parseDurationDist(final String s)
            throws NetworkException
    {
        String[] s1 = s.split("\\(");
        String ds = s1[0];
        String[] s2 = s1[1].split("\\)");
        String unit = s2[1].trim();
        double[] args = parseDoubleArgs(s2[0]);
        DistContinuous dist = makeDistContinuous(ds, args);
        return new ContinuousDistDoubleScalar.Rel<Duration, DurationUnit>(dist, DurationUnit.BASE.getUnitByAbbreviation(unit));
    }

    /**
     * Parse an absolute time distribution, e.g. <code>UNIFORM(1, 3) s</code>.
     * @param s String; the string to be parsed.
     * @return a typed continuous random distribution.
     * @throws NetworkException in case of a parse error.
     */
    public static ContinuousDistDoubleScalar.Abs<Time, TimeUnit, DurationUnit> parseTimeDist(final String s)
            throws NetworkException
    {
        String[] s1 = s.split("\\(");
        String ds = s1[0];
        String[] s2 = s1[1].split("\\)");
        String unit = s2[1].trim();
        double[] args = parseDoubleArgs(s2[0]);
        DistContinuous dist = makeDistContinuous(ds, args);
        return new ContinuousDistDoubleScalar.Abs<Time, TimeUnit, DurationUnit>(dist,
                TimeUnit.BASE.getUnitByAbbreviation(unit));
    }

    /**
     * Parse a relative speed distribution, e.g. <code>TRIANGULAR(80, 90, 110) km/h</code>.
     * @param s String; the string to be parsed.
     * @return a typed continuous random distribution.
     * @throws NetworkException in case of a parse error.
     */
    public static ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> parseSpeedDist(final String s) throws NetworkException
    {
        String[] s1 = s.split("\\(");
        String ds = s1[0];
        String[] s2 = s1[1].split("\\)");
        String unit = s2[1].trim();
        double[] args = parseDoubleArgs(s2[0]);
        DistContinuous dist = makeDistContinuous(ds, args);
        return new ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit>(dist, SpeedUnit.BASE.getUnitByAbbreviation(unit));
    }

}
