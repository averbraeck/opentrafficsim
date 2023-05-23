package org.opentrafficsim.road.network.factory.xml.utils;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.DurationUnit;
import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.LinearDensityUnit;
import org.djunits.unit.PositionUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.LinearDensity;
import org.djunits.value.vdouble.scalar.Position;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.units.distributions.ContinuousDistDoubleScalar;
import org.opentrafficsim.road.network.factory.xml.XmlParserException;
import org.opentrafficsim.xml.generated.AccelerationDistType;
import org.opentrafficsim.xml.generated.ConstantDistType;
import org.opentrafficsim.xml.generated.DiscreteDistType;
import org.opentrafficsim.xml.generated.DurationDistType;
import org.opentrafficsim.xml.generated.FrequencyDistType;
import org.opentrafficsim.xml.generated.LengthDistType;
import org.opentrafficsim.xml.generated.LinearDensityDistType;
import org.opentrafficsim.xml.generated.PositionDistType;
import org.opentrafficsim.xml.generated.RandomStreamSource;
import org.opentrafficsim.xml.generated.SpeedDistType;
import org.opentrafficsim.xml.generated.TimeDistType;

import nl.tudelft.simulation.dsol.experiment.StreamInformation;
import nl.tudelft.simulation.jstats.distributions.DistBernoulli;
import nl.tudelft.simulation.jstats.distributions.DistBeta;
import nl.tudelft.simulation.jstats.distributions.DistBinomial;
import nl.tudelft.simulation.jstats.distributions.DistConstant;
import nl.tudelft.simulation.jstats.distributions.DistContinuous;
import nl.tudelft.simulation.jstats.distributions.DistDiscrete;
import nl.tudelft.simulation.jstats.distributions.DistDiscreteConstant;
import nl.tudelft.simulation.jstats.distributions.DistDiscreteUniform;
import nl.tudelft.simulation.jstats.distributions.DistErlang;
import nl.tudelft.simulation.jstats.distributions.DistExponential;
import nl.tudelft.simulation.jstats.distributions.DistGamma;
import nl.tudelft.simulation.jstats.distributions.DistGeometric;
import nl.tudelft.simulation.jstats.distributions.DistLogNormal;
import nl.tudelft.simulation.jstats.distributions.DistNegBinomial;
import nl.tudelft.simulation.jstats.distributions.DistNormal;
import nl.tudelft.simulation.jstats.distributions.DistNormalTrunc;
import nl.tudelft.simulation.jstats.distributions.DistPearson5;
import nl.tudelft.simulation.jstats.distributions.DistPearson6;
import nl.tudelft.simulation.jstats.distributions.DistPoisson;
import nl.tudelft.simulation.jstats.distributions.DistTriangular;
import nl.tudelft.simulation.jstats.distributions.DistUniform;
import nl.tudelft.simulation.jstats.distributions.DistWeibull;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Parse a distribution from text to a distribution.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 */
public final class ParseDistribution
{
    /** Utility class. */
    private ParseDistribution()
    {
        // do not instantiate
    }

    /**
     * Parse a discrete distribution.
     * @param streamMap map with stream information
     * @param distType the distribution to parse
     * @return the generated distribution.
     * @throws XmlParserException in case distribution unknown or parameter number does not match.
     */
    public static DistDiscrete makeDistDiscrete(final StreamInformation streamMap, final DiscreteDistType distType)
            throws XmlParserException
    {
        StreamInterface stream = findStream(streamMap, distType.getRandomStream());
        if (distType.getBernoulliI() != null)
        {
            return new DistBernoulli(stream, distType.getBernoulliI().getP());
        }
        else if (distType.getBinomial() != null)
        {
            return new DistBinomial(stream, (int) distType.getBinomial().getN().longValue(), distType.getBinomial().getP());
        }
        else if (distType.getConstant() != null)
        {
            return new DistDiscreteConstant(stream, distType.getConstant().getC());
        }
        else if (distType.getGeometric() != null)
        {
            return new DistGeometric(stream, distType.getGeometric().getP());
        }
        else if (distType.getNegBinomial() != null)
        {
            return new DistNegBinomial(stream, (int) distType.getNegBinomial().getN().longValue(),
                    distType.getGeometric().getP());
        }
        else if (distType.getPoisson() != null)
        {
            return new DistPoisson(stream, distType.getPoisson().getLambda());
        }
        else if (distType.getUniform() != null)
        {
            return new DistDiscreteUniform(stream, distType.getUniform().getMin(), distType.getUniform().getMax());
        }
        throw new XmlParserException("makeDistDiscrete - unknown distribution function " + distType);
    }

    /**
     * Parse a continuous distribution.
     * @param streamMap map with stream information
     * @param distType the distribution to parse
     * @return the generated distribution.
     * @throws XmlParserException in case distribution unknown or parameter number does not match.
     */
    public static DistContinuous makeDistContinuous(final StreamInformation streamMap, final ConstantDistType distType)
            throws XmlParserException
    {
        StreamInterface stream = findStream(streamMap, distType.getRandomStream());
        if (distType.getConstant() != null)
        {
            return new DistConstant(stream, distType.getConstant().getC());
        }
        else if (distType.getExponential() != null)
        {
            return new DistExponential(stream, distType.getExponential().getLambda());
        }
        else if (distType.getTriangular() != null)
        {
            return new DistTriangular(stream, distType.getTriangular().getMin(), distType.getTriangular().getMode(),
                    distType.getTriangular().getMax());
        }
        else if (distType.getNormal() != null)
        {
            return new DistNormal(stream, distType.getNormal().getMu(), distType.getNormal().getSigma());
        }
        else if (distType.getNormal() != null)
        {
            return new DistNormalTrunc(stream, distType.getNormalTrunc().getMu(), distType.getNormalTrunc().getSigma(),
                    distType.getNormalTrunc().getMin(), distType.getNormalTrunc().getMax());
        }
        else if (distType.getBeta() != null)
        {
            return new DistBeta(stream, distType.getBeta().getAlpha1(), distType.getBeta().getAlpha2());
        }
        else if (distType.getErlang() != null)
        {
            return new DistErlang(stream, distType.getErlang().getK().intValue(), (int) distType.getErlang().getMean());
        }
        else if (distType.getGamma() != null)
        {
            return new DistGamma(stream, distType.getGamma().getAlpha(), distType.getGamma().getBeta());
        }
        else if (distType.getLogNormal() != null)
        {
            return new DistLogNormal(stream, distType.getLogNormal().getMu(), distType.getLogNormal().getSigma());
        }
        else if (distType.getPearson5() != null)
        {
            return new DistPearson5(stream, distType.getPearson5().getAlpha(), distType.getPearson5().getBeta());
        }
        else if (distType.getPearson6() != null)
        {
            return new DistPearson6(stream, distType.getPearson6().getAlpha1(), distType.getPearson6().getAlpha2(),
                    distType.getPearson6().getBeta());
        }
        else if (distType.getUniform() != null)
        {
            return new DistUniform(stream, distType.getUniform().getMin(), distType.getUniform().getMax());
        }
        else if (distType.getWeibull() != null)
        {
            return new DistWeibull(stream, distType.getWeibull().getAlpha(), distType.getWeibull().getBeta());
        }
        throw new XmlParserException("makeDistContinuous - unknown distribution function " + distType);
    }

    /**
     * Find and return the stream belonging to te streamId.
     * @param streamInformation the map with streams from the RUN tag
     * @param streamSource the stream source
     * @return the stream belonging to te streamId
     * @throws XmlParserException when the stream could not be found
     */
    private static StreamInterface findStream(final StreamInformation streamInformation, final RandomStreamSource streamSource)
            throws XmlParserException
    {
        String streamId;
        if (streamSource == null || streamSource.getDefault() == null)
        {
            streamId = "default";
        }
        else if (streamSource.getGeneration() == null)
        {
            streamId = "generation";
        }
        else
        {
            streamId = streamSource.getDefined();
        }
        if (streamInformation.getStream(streamId) == null)
        {
            throw new XmlParserException("Could not find stream with Id=" + streamId);
        }
        return streamInformation.getStream(streamId);
    }

    /**
     * Parse a relative length distribution, e.g. <code>UNIFORM(1, 3) m</code>.
     * @param streamMap the map with streams from the RUN tag
     * @param lengthDist the tag to parse
     * @return a typed continuous random distribution.
     * @throws XmlParserException in case of a parse error.
     */
    public static ContinuousDistDoubleScalar.Rel<Length, LengthUnit> parseLengthDist(final StreamInformation streamMap,
            final LengthDistType lengthDist) throws XmlParserException
    {
        DistContinuous dist = makeDistContinuous(streamMap, lengthDist);
        for (LengthUnit unit : LengthUnit.BASE.getUnitsById().values())
        {
            if (unit.getDefaultAbbreviations().contains(lengthDist.getLengthUnit()))
            {
                return new ContinuousDistDoubleScalar.Rel<Length, LengthUnit>(dist, unit);
            }
        }
        throw new XmlParserException(
                "Could not find LengthUnit " + lengthDist.getLengthUnit() + " in tag of type LengthDistType");
    }

    /**
     * Parse an absolute position distribution, e.g. <code>UNIFORM(1, 3) m</code>.
     * @param streamMap the map with streams from the RUN tag
     * @param positionDist the tag to parse
     * @return a typed continuous random distribution.
     * @throws XmlParserException in case of a parse error.
     */
    public static ContinuousDistDoubleScalar.Abs<Position, PositionUnit, LengthUnit> parsePositionDist(
            final StreamInformation streamMap, final PositionDistType positionDist) throws XmlParserException
    {
        DistContinuous dist = makeDistContinuous(streamMap, positionDist);
        for (PositionUnit unit : PositionUnit.BASE.getUnitsById().values())
        {
            if (unit.getDefaultAbbreviations().contains(positionDist.getPositionUnit()))
            {
                return new ContinuousDistDoubleScalar.Abs<Position, PositionUnit, LengthUnit>(dist, unit);
            }
        }
        throw new XmlParserException(
                "Could not find PositionUnit " + positionDist.getPositionUnit() + " in tag of type PositionDistType");
    }

    /**
     * Parse a relative duration distribution, e.g. <code>UNIFORM(1, 3) s</code>.
     * @param streamMap the map with streams from the RUN tag
     * @param durationDist the tag to parse
     * @return a typed continuous random distribution.
     * @throws XmlParserException in case of a parse error.
     */
    public static ContinuousDistDoubleScalar.Rel<Duration, DurationUnit> parseDurationDist(final StreamInformation streamMap,
            final DurationDistType durationDist) throws XmlParserException
    {
        DistContinuous dist = makeDistContinuous(streamMap, durationDist);
        for (DurationUnit unit : DurationUnit.BASE.getUnitsById().values())
        {
            if (unit.getDefaultAbbreviations().contains(durationDist.getDurationUnit()))
            {
                return new ContinuousDistDoubleScalar.Rel<Duration, DurationUnit>(dist, unit);
            }
        }
        throw new XmlParserException(
                "Could not find DurationUnit " + durationDist.getDurationUnit() + " in tag of type DurationDistType");
    }

    /**
     * Parse an absolute time distribution, e.g. <code>UNIFORM(1, 3) s</code>.
     * @param streamMap the map with streams from the RUN tag
     * @param timeDist the tag to parse
     * @return a typed continuous random distribution.
     * @throws XmlParserException in case of a parse error.
     */
    public static ContinuousDistDoubleScalar.Abs<Time, TimeUnit, DurationUnit> parseTimeDist(final StreamInformation streamMap,
            final TimeDistType timeDist) throws XmlParserException
    {
        DistContinuous dist = makeDistContinuous(streamMap, timeDist);
        for (TimeUnit unit : TimeUnit.BASE.getUnitsById().values())
        {
            if (unit.getDefaultAbbreviations().contains(timeDist.getTimeUnit()))
            {
                return new ContinuousDistDoubleScalar.Abs<Time, TimeUnit, DurationUnit>(dist, unit);
            }
        }
        throw new XmlParserException("Could not find TimeUnit " + timeDist.getTimeUnit() + " in tag of type TimeDistType");
    }

    /**
     * Parse a relative speed distribution, e.g. <code>UNIFORM(1, 3) m/s</code>.
     * @param streamMap the map with streams from the RUN tag
     * @param speedDist the tag to parse
     * @return a typed continuous random distribution.
     * @throws XmlParserException in case of a parse error.
     */
    public static ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> parseSpeedDist(final StreamInformation streamMap,
            final SpeedDistType speedDist) throws XmlParserException
    {
        DistContinuous dist = makeDistContinuous(streamMap, speedDist);
        for (SpeedUnit unit : SpeedUnit.BASE.getUnitsById().values())
        {
            if (unit.getDefaultAbbreviations().contains(speedDist.getSpeedUnit()))
            {
                return new ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit>(dist, unit);
            }
        }
        throw new XmlParserException("Could not find SpeedUnit " + speedDist.getSpeedUnit() + " in tag of type SpeedDistType");
    }

    /**
     * Parse a relative acceleration distribution, e.g. <code>UNIFORM(1, 3) s</code>.
     * @param streamMap the map with streams from the RUN tag
     * @param accelerationDist the tag to parse
     * @return a typed continuous random distribution.
     * @throws XmlParserException in case of a parse error.
     */
    public static ContinuousDistDoubleScalar.Rel<Acceleration, AccelerationUnit> parseAccelerationDist(
            final StreamInformation streamMap, final AccelerationDistType accelerationDist) throws XmlParserException
    {
        DistContinuous dist = makeDistContinuous(streamMap, accelerationDist);
        for (AccelerationUnit unit : AccelerationUnit.BASE.getUnitsById().values())
        {
            if (unit.getDefaultAbbreviations().contains(accelerationDist.getAccelerationUnit()))
            {
                return new ContinuousDistDoubleScalar.Rel<Acceleration, AccelerationUnit>(dist, unit);
            }
        }
        throw new XmlParserException("Could not find AccelerationUnit " + accelerationDist.getAccelerationUnit()
                + " in tag of type AccelerationDistType");
    }

    /**
     * Parse a relative frequency distribution, e.g. <code>UNIFORM(1, 3) s</code>.
     * @param streamMap the map with streams from the RUN tag
     * @param frequencyDist the tag to parse
     * @return a typed continuous random distribution.
     * @throws XmlParserException in case of a parse error.
     */
    public static ContinuousDistDoubleScalar.Rel<Frequency, FrequencyUnit> parseFrequencyDist(final StreamInformation streamMap,
            final FrequencyDistType frequencyDist) throws XmlParserException
    {
        DistContinuous dist = makeDistContinuous(streamMap, frequencyDist);
        for (FrequencyUnit unit : FrequencyUnit.BASE.getUnitsById().values())
        {
            if (unit.getDefaultAbbreviations().contains(frequencyDist.getFrequencyUnit()))
            {
                return new ContinuousDistDoubleScalar.Rel<Frequency, FrequencyUnit>(dist, unit);
            }
        }
        throw new XmlParserException(
                "Could not find FrequencyUnit " + frequencyDist.getFrequencyUnit() + " in tag of type FrequencyDistType");
    }

    /**
     * Parse a relative linear density distribution, e.g. <code>UNIFORM(1, 3) s</code>.
     * @param streamMap the map with streams from the RUN tag
     * @param linearDensityDist the tag to parse
     * @return a typed continuous random distribution.
     * @throws XmlParserException in case of a parse error.
     */
    public static ContinuousDistDoubleScalar.Rel<LinearDensity, LinearDensityUnit> parseLinearDensityDist(
            final StreamInformation streamMap, final LinearDensityDistType linearDensityDist) throws XmlParserException
    {
        DistContinuous dist = makeDistContinuous(streamMap, linearDensityDist);
        for (LinearDensityUnit unit : LinearDensityUnit.BASE.getUnitsById().values())
        {
            if (unit.getDefaultAbbreviations().contains(linearDensityDist.getLinearDensityUnit()))
            {
                return new ContinuousDistDoubleScalar.Rel<LinearDensity, LinearDensityUnit>(dist, unit);
            }
        }
        throw new XmlParserException("Could not find FrequencyUnit " + linearDensityDist.getLinearDensityUnit()
                + " in tag of type FrequencyDistType");
    }

}
