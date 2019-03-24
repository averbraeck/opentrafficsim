package org.opentrafficsim.road.network.factory.xml.units;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.PositionUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Position;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.dsol.DistNormalTrunc;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.factory.xml.units.AccelerationUnits;
import org.opentrafficsim.core.network.factory.xml.units.DurationUnits;
import org.opentrafficsim.core.network.factory.xml.units.LengthUnits;
import org.opentrafficsim.core.network.factory.xml.units.PositionUnits;
import org.opentrafficsim.core.network.factory.xml.units.SpeedUnits;
import org.opentrafficsim.core.network.factory.xml.units.TimeUnits;
import org.opentrafficsim.core.units.distributions.ContinuousDistDoubleScalar;

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
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Parse a distribution from text to a distribution.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public final class ParseDistribution
{
    /** Utility class. */
    private ParseDistribution()
    {
        // do not instantiate
    }

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
            d[i] = Double.parseDouble(ss[i].trim());
        }
        return d;
    }

    /**
     * Parse a continuous distribution.
     * @param stream StreamInterface; the stream to use
     * @param args double[]; the parameters of the distribution, e.g. {1.0, 2.0}.
     * @param ds String; the name of the distribution, e.g. UNIF.
     * @return the generated distribution.
     * @throws NetworkException in case distribution unknown or parameter number does not match.
     */
    private static DistContinuous makeDistContinuous(final StreamInterface stream, final String ds, final double[] args)
            throws NetworkException
    {
        try
        {
            switch (ds)
            {
                case "CONST":
                case "CONSTANT":
                    return new DistConstant(stream, args[0]);

                case "EXPO":
                case "EXPONENTIAL":
                    return new DistExponential(stream, args[0]);

                case "TRIA":
                case "TRIANGULAR":
                    return new DistTriangular(stream, args[0], args[1], args[2]);

                case "NORM":
                case "NORMAL":
                    return new DistNormal(stream, args[0], args[1]);

                case "NORMTRUNC":
                case "NORMALTRUNCATED":
                    return new DistNormalTrunc(stream, args[0], args[1], args[2], args[3]);

                case "BETA":
                    return new DistBeta(stream, args[0], args[1]);

                case "ERLANG":
                    return new DistErlang(stream, (int) args[0], args[1]);

                case "GAMMA":
                    return new DistGamma(stream, args[0], args[1]);

                case "LOGN":
                case "LOGNORMAL":
                    return new DistLogNormal(stream, args[0], args[1]);

                case "PEARSON5":
                    return new DistPearson5(stream, args[0], args[1]);

                case "PEARSON6":
                    return new DistPearson6(stream, args[0], args[1], args[2]);

                case "UNIF":
                case "UNIFORM":
                    return new DistUniform(stream, args[0], args[1]);

                case "WEIB":
                case "WEIBULL":
                    return new DistWeibull(stream, args[0], args[1]);

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
     * @param stream StreamInterface; the stream to use
     * @param s String; the string to be parsed.
     * @return a typed continuous random distribution.
     * @throws NetworkException in case of a parse error.
     */
    public static ContinuousDistDoubleScalar.Rel<Length, LengthUnit> parseLengthDist(final StreamInterface stream,
            final String s) throws NetworkException
    {
        String[] s1 = s.split("\\(");
        String ds = s1[0];
        String[] s2 = s1[1].split("\\)");
        String unit = LengthUnits.parseLengthUnit(s2[1]);
        double[] args = parseDoubleArgs(s2[0]);
        DistContinuous dist = makeDistContinuous(stream, ds, args);
        return new ContinuousDistDoubleScalar.Rel<Length, LengthUnit>(dist, LengthUnits.LENGTH_UNITS.get(unit));
    }

    /**
     * Parse an absolute length distribution, e.g. <code>UNIFORM(1, 3) m</code>.
     * @param stream StreamInterface; the stream to use
     * @param s String; the string to be parsed.
     * @return a typed continuous random distribution.
     * @throws NetworkException in case of a parse error.
     */
    public static ContinuousDistDoubleScalar.Abs<Position, PositionUnit, LengthUnit> parsePositionDist(
            final StreamInterface stream, final String s) throws NetworkException
    {
        String[] s1 = s.split("\\(");
        String ds = s1[0];
        String[] s2 = s1[1].split("\\)");
        String unit = LengthUnits.parseLengthUnit(s2[1]);
        double[] args = parseDoubleArgs(s2[0]);
        DistContinuous dist = makeDistContinuous(stream, ds, args);
        return new ContinuousDistDoubleScalar.Abs<Position, PositionUnit, LengthUnit>(dist,
                PositionUnits.POSITION_UNITS.get(unit));
    }

    /**
     * Parse a relative time distribution, e.g. <code>UNIFORM(1, 3) s</code>.
     * @param stream StreamInterface; the stream to use
     * @param s String; the string to be parsed.
     * @return a typed continuous random distribution.
     * @throws NetworkException in case of a parse error.
     */
    public static ContinuousDistDoubleScalar.Rel<Duration, DurationUnit> parseDurationDist(final StreamInterface stream,
            final String s) throws NetworkException
    {
        String[] s1 = s.split("\\(");
        String ds = s1[0];
        String[] s2 = s1[1].split("\\)");
        String unit = DurationUnits.parseDurationUnit(s2[1]);
        double[] args = parseDoubleArgs(s2[0]);
        DistContinuous dist = makeDistContinuous(stream, ds, args);
        return new ContinuousDistDoubleScalar.Rel<Duration, DurationUnit>(dist, DurationUnits.DURATION_UNITS.get(unit));
    }

    /**
     * Parse an absolute time distribution, e.g. <code>UNIFORM(1, 3) s</code>.
     * @param stream StreamInterface; the stream to use
     * @param s String; the string to be parsed.
     * @return a typed continuous random distribution.
     * @throws NetworkException in case of a parse error.
     */
    public static ContinuousDistDoubleScalar.Abs<Time, TimeUnit, DurationUnit> parseTimeDist(final StreamInterface stream,
            final String s) throws NetworkException
    {
        String[] s1 = s.split("\\(");
        String ds = s1[0];
        String[] s2 = s1[1].split("\\)");
        String unit = TimeUnits.parseTimeUnit(s2[1]);
        double[] args = parseDoubleArgs(s2[0]);
        DistContinuous dist = makeDistContinuous(stream, ds, args);
        return new ContinuousDistDoubleScalar.Abs<Time, TimeUnit, DurationUnit>(dist, TimeUnits.TIME_UNITS.get(unit));
    }

    /**
     * Parse a relative speed distribution, e.g. <code>TRIANGULAR(80, 90, 110) km/h</code>.
     * @param stream StreamInterface; the stream to use
     * @param s String; the string to be parsed.
     * @return a typed continuous random distribution.
     * @throws NetworkException in case of a parse error.
     */
    public static ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> parseSpeedDist(final StreamInterface stream, final String s)
            throws NetworkException
    {
        String[] s1 = s.split("\\(");
        String ds = s1[0];
        String[] s2 = s1[1].split("\\)");
        String unit = SpeedUnits.parseSpeedUnit(s2[1]);
        double[] args = parseDoubleArgs(s2[0]);
        DistContinuous dist = makeDistContinuous(stream, ds, args);
        return new ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit>(dist, SpeedUnits.SPEED_UNITS.get(unit));
    }

    /**
     * Parse a relative acceleration distribution, e.g. <code>TRIANGULAR(80, 90, 110) km/h^2</code>.
     * @param stream StreamInterface; the stream to use
     * @param s String; the string to be parsed.
     * @return a typed continuous random distribution.
     * @throws NetworkException in case of a parse error.
     */
    public static ContinuousDistDoubleScalar.Rel<Acceleration, AccelerationUnit> parseAccelerationDist(
            final StreamInterface stream, final String s) throws NetworkException
    {
        String[] s1 = s.split("\\(");
        String ds = s1[0];
        String[] s2 = s1[1].split("\\)");
        String unit = AccelerationUnits.parseAccelerationUnit(s2[1]);
        double[] args = parseDoubleArgs(s2[0]);
        DistContinuous dist = makeDistContinuous(stream, ds, args);
        return new ContinuousDistDoubleScalar.Rel<Acceleration, AccelerationUnit>(dist,
                AccelerationUnits.ACCELERATION_UNITS.get(unit));
    }

}
