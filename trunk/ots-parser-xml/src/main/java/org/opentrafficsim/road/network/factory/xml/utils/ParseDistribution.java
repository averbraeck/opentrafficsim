package org.opentrafficsim.road.network.factory.xml.utils;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.PositionUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.unit.Unit;
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
import org.opentrafficsim.xml.generated.CONTDISTTYPE;
import org.opentrafficsim.xml.generated.LENGTHDISTTYPE;

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
     * Parse a continuous distribution.
     * @param distType the distribution to parse
     * @return the generated distribution.
     * @throws NetworkException in case distribution unknown or parameter number does not match.
     */
    private static DistContinuous makeDistContinuous(final CONTDISTTYPE distType)
            throws NetworkException
    {
        if (distType.getCONSTANT() != null)
        {
            StreamInterface stream = findStream(distType.getCONSTANT().getRANDOMSTREAM());
            return new DistConstant(stream, distType.getCONSTANT().getC());
        }
        else if (distType.getEXPONENTIAL() != null)
        {
            return new DistExponential(stream, distType.getEXPONENTIAL().getLAMBDA());
        }
        else if (distType.getTRIANGULAR() != null)
        {
            return new DistTriangular(stream, distType.getTRIANGULAR().getMIN(), distType.getTRIANGULAR().getMODE(),
                    distType.getTRIANGULAR().getMAX());
        }
        else if (distType.getNORMAL() != null)
        {
            return new DistNormal(stream, distType.getNORMAL().getMU(), distType.getNORMAL().getSIGMA());
        }
        // TODO: NORMALTRUNC
        else if (distType.getBETA() != null)
        {
            return new DistBeta(stream, distType.getBETA().getALPHA1(), distType.getBETA().getALPHA2());
        }
        else if (distType.getERLANG() != null)
        {
            return new DistErlang(stream, distType.getERLANG().getK().intValue(), distType.getERLANG().getMEAN());
        }
        else if (distType.getGAMMA() != null)
        {
            return new DistGamma(stream, distType.getGAMMA().getALPHA(), distType.getGAMMA().getBETA());
        }
        else if (distType.getLOGNORMAL() != null)
        {
            return new DistLogNormal(stream, distType.getLOGNORMAL().getMU(), distType.getLOGNORMAL().getSIGMA());
        }
        else if (distType.getPEARSON5() != null)
        {
            return new DistPearson5(stream, distType.getPEARSON5().getALPHA(), distType.getPEARSON5().getBETA());
        }
        else if (distType.getPEARSON6() != null)
        {
            return new DistPearson6(stream, distType.getPEARSON6().getALPHA1(), distType.getPEARSON6().getALPHA2(),
                    distType.getPEARSON6().getBETA());
        }
        else if (distType.getUNIFORM() != null)
        {
            return new DistUniform(stream, distType.getUNIFORM().getMIN(), distType.getUNIFORM().getMAX());
        }
        else if (distType.getWEIBULL() != null)
        {
            return new DistWeibull(stream, distType.getWEIBULL().getALPHA(), distType.getWEIBULL().getBETA());
        }
        else
        {
            throw new NetworkException("makeDistContinuous - unknown distribution function " + distType);
        }
    }

    private static StreamInterface findStream(final String stream)
    {
        StreamInterface stream = 
    }
    
    /**
     * Parse a relative length distribution, e.g. <code>UNIFORM(1, 3) m</code>.
     * @param stream StreamInterface; the stream to use
     * @param s String; the string to be parsed.
     * @return a typed continuous random distribution.
     * @throws NetworkException in case of a parse error.
     */
    public static ContinuousDistDoubleScalar.Rel<Length, LengthUnit> parseLengthDist(final StreamInterface stream,
            final LENGTHDISTTYPE lengthDist) throws NetworkException
    {
        DistContinuous dist = makeDistContinuous(stream, lengthDist);
        LengthUnit lengthUnit = null;
        for (LengthUnit unit : Unit.getUnits(LengthUnit.class))
        {
            if (unit.getDefaultLocaleTextualRepresentations().contains(lengthDist.getLENGTHUNIT()))
            {
                lengthUnit = unit;
                break;
            }
        }

        return new ContinuousDistDoubleScalar.Rel<Length, LengthUnit>(dist, lengthUnit);
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
