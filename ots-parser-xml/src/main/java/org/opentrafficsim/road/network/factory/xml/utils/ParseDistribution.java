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
import org.opentrafficsim.xml.generated.ACCELERATIONDISTTYPE;
import org.opentrafficsim.xml.generated.CONTDISTTYPE;
import org.opentrafficsim.xml.generated.DISCRETEDISTTYPE;
import org.opentrafficsim.xml.generated.DURATIONDISTTYPE;
import org.opentrafficsim.xml.generated.FREQUENCYDISTTYPE;
import org.opentrafficsim.xml.generated.LENGTHDISTTYPE;
import org.opentrafficsim.xml.generated.LINEARDENSITYDISTTYPE;
import org.opentrafficsim.xml.generated.POSITIONDISTTYPE;
import org.opentrafficsim.xml.generated.SPEEDDISTTYPE;
import org.opentrafficsim.xml.generated.TIMEDISTTYPE;

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
    public static DistDiscrete makeDistDiscrete(final StreamInformation streamMap, final DISCRETEDISTTYPE distType)
            throws XmlParserException
    {
        if (distType.getBERNOULLI() != null)
        {
            StreamInterface stream = findStream(streamMap, distType.getBERNOULLI().getRANDOMSTREAM());
            return new DistBernoulli(stream, distType.getBERNOULLI().getP());
        }
        else if (distType.getBINOMIAL() != null)
        {
            StreamInterface stream = findStream(streamMap, distType.getBINOMIAL().getRANDOMSTREAM());
            return new DistBinomial(stream, (int) distType.getBINOMIAL().getN().longValue(), distType.getBINOMIAL().getP());
        }
        else if (distType.getCONSTANT() != null)
        {
            StreamInterface stream = findStream(streamMap, distType.getCONSTANT().getRANDOMSTREAM());
            return new DistDiscreteConstant(stream, distType.getCONSTANT().getC());
        }
        else if (distType.getGEOMETRIC() != null)
        {
            StreamInterface stream = findStream(streamMap, distType.getGEOMETRIC().getRANDOMSTREAM());
            return new DistGeometric(stream, distType.getGEOMETRIC().getP());
        }
        else if (distType.getNEGBINOMIAL() != null)
        {
            StreamInterface stream = findStream(streamMap, distType.getNEGBINOMIAL().getRANDOMSTREAM());
            return new DistNegBinomial(stream, (int) distType.getNEGBINOMIAL().getN().longValue(),
                    distType.getGEOMETRIC().getP());
        }
        else if (distType.getPOISSON() != null)
        {
            StreamInterface stream = findStream(streamMap, distType.getPOISSON().getRANDOMSTREAM());
            return new DistPoisson(stream, distType.getPOISSON().getLAMBDA());
        }
        else if (distType.getUNIFORM() != null)
        {
            StreamInterface stream = findStream(streamMap, distType.getUNIFORM().getRANDOMSTREAM());
            return new DistDiscreteUniform(stream, distType.getUNIFORM().getMIN(), distType.getUNIFORM().getMAX());
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
    public static DistContinuous makeDistContinuous(final StreamInformation streamMap, final CONTDISTTYPE distType)
            throws XmlParserException
    {
        if (distType.getCONSTANT() != null)
        {
            StreamInterface stream = findStream(streamMap, distType.getCONSTANT().getRANDOMSTREAM());
            return new DistConstant(stream, distType.getCONSTANT().getC());
        }
        else if (distType.getEXPONENTIAL() != null)
        {
            StreamInterface stream = findStream(streamMap, distType.getEXPONENTIAL().getRANDOMSTREAM());
            return new DistExponential(stream, distType.getEXPONENTIAL().getLAMBDA());
        }
        else if (distType.getTRIANGULAR() != null)
        {
            StreamInterface stream = findStream(streamMap, distType.getTRIANGULAR().getRANDOMSTREAM());
            return new DistTriangular(stream, distType.getTRIANGULAR().getMIN(), distType.getTRIANGULAR().getMODE(),
                    distType.getTRIANGULAR().getMAX());
        }
        else if (distType.getNORMAL() != null)
        {
            StreamInterface stream = findStream(streamMap, distType.getNORMAL().getRANDOMSTREAM());
            return new DistNormal(stream, distType.getNORMAL().getMU(), distType.getNORMAL().getSIGMA());
        }
        // TODO: NORMALTRUNC
        else if (distType.getBETA() != null)
        {
            StreamInterface stream = findStream(streamMap, distType.getBETA().getRANDOMSTREAM());
            return new DistBeta(stream, distType.getBETA().getALPHA1(), distType.getBETA().getALPHA2());
        }
        else if (distType.getERLANG() != null)
        {
            StreamInterface stream = findStream(streamMap, distType.getERLANG().getRANDOMSTREAM());
            return new DistErlang(stream, distType.getERLANG().getK().intValue(), (int) distType.getERLANG().getMEAN());
        }
        else if (distType.getGAMMA() != null)
        {
            StreamInterface stream = findStream(streamMap, distType.getGAMMA().getRANDOMSTREAM());
            return new DistGamma(stream, distType.getGAMMA().getALPHA(), distType.getGAMMA().getBETA());
        }
        else if (distType.getLOGNORMAL() != null)
        {
            StreamInterface stream = findStream(streamMap, distType.getLOGNORMAL().getRANDOMSTREAM());
            return new DistLogNormal(stream, distType.getLOGNORMAL().getMU(), distType.getLOGNORMAL().getSIGMA());
        }
        else if (distType.getPEARSON5() != null)
        {
            StreamInterface stream = findStream(streamMap, distType.getPEARSON5().getRANDOMSTREAM());
            return new DistPearson5(stream, distType.getPEARSON5().getALPHA(), distType.getPEARSON5().getBETA());
        }
        else if (distType.getPEARSON6() != null)
        {
            StreamInterface stream = findStream(streamMap, distType.getPEARSON6().getRANDOMSTREAM());
            return new DistPearson6(stream, distType.getPEARSON6().getALPHA1(), distType.getPEARSON6().getALPHA2(),
                    distType.getPEARSON6().getBETA());
        }
        else if (distType.getUNIFORM() != null)
        {
            StreamInterface stream = findStream(streamMap, distType.getUNIFORM().getRANDOMSTREAM());
            return new DistUniform(stream, distType.getUNIFORM().getMIN(), distType.getUNIFORM().getMAX());
        }
        else if (distType.getWEIBULL() != null)
        {
            StreamInterface stream = findStream(streamMap, distType.getWEIBULL().getRANDOMSTREAM());
            return new DistWeibull(stream, distType.getWEIBULL().getALPHA(), distType.getWEIBULL().getBETA());
        }
        throw new XmlParserException("makeDistContinuous - unknown distribution function " + distType);
    }

    /**
     * Find and return the stream belonging to te streamId.
     * @param streamInformation the map with streams from the RUN tag
     * @param streamId the id to search for
     * @return the stream belonging to te streamId
     * @throws XmlParserException when the stream could not be found
     */
    private static StreamInterface findStream(final StreamInformation streamInformation, final String streamId)
            throws XmlParserException
    {
        if (streamInformation.getStream(streamId) == null)
        {
            throw new XmlParserException("Could not find stream with ID=" + streamId);
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
            final LENGTHDISTTYPE lengthDist) throws XmlParserException
    {
        DistContinuous dist = makeDistContinuous(streamMap, lengthDist);
        for (LengthUnit unit : LengthUnit.BASE.getUnitsById().values())
        {
            if (unit.getDefaultAbbreviations().contains(lengthDist.getLENGTHUNIT()))
            {
                return new ContinuousDistDoubleScalar.Rel<Length, LengthUnit>(dist, unit);
            }
        }
        throw new XmlParserException(
                "Could not find LengthUnit " + lengthDist.getLENGTHUNIT() + " in tag of type LENGTHDISTTYPE");
    }

    /**
     * Parse an absolute position distribution, e.g. <code>UNIFORM(1, 3) m</code>.
     * @param streamMap the map with streams from the RUN tag
     * @param positionDist the tag to parse
     * @return a typed continuous random distribution.
     * @throws XmlParserException in case of a parse error.
     */
    public static ContinuousDistDoubleScalar.Abs<Position, PositionUnit, LengthUnit> parsePositionDist(
            final StreamInformation streamMap, final POSITIONDISTTYPE positionDist) throws XmlParserException
    {
        DistContinuous dist = makeDistContinuous(streamMap, positionDist);
        for (PositionUnit unit : PositionUnit.BASE.getUnitsById().values())
        {
            if (unit.getDefaultAbbreviations().contains(positionDist.getPOSITIONUNIT()))
            {
                return new ContinuousDistDoubleScalar.Abs<Position, PositionUnit, LengthUnit>(dist, unit);
            }
        }
        throw new XmlParserException(
                "Could not find PositionUnit " + positionDist.getPOSITIONUNIT() + " in tag of type POSITIONDISTTYPE");
    }

    /**
     * Parse a relative duration distribution, e.g. <code>UNIFORM(1, 3) s</code>.
     * @param streamMap the map with streams from the RUN tag
     * @param durationDist the tag to parse
     * @return a typed continuous random distribution.
     * @throws XmlParserException in case of a parse error.
     */
    public static ContinuousDistDoubleScalar.Rel<Duration, DurationUnit> parseDurationDist(final StreamInformation streamMap,
            final DURATIONDISTTYPE durationDist) throws XmlParserException
    {
        DistContinuous dist = makeDistContinuous(streamMap, durationDist);
        for (DurationUnit unit : DurationUnit.BASE.getUnitsById().values())
        {
            if (unit.getDefaultAbbreviations().contains(durationDist.getDURATIONUNIT()))
            {
                return new ContinuousDistDoubleScalar.Rel<Duration, DurationUnit>(dist, unit);
            }
        }
        throw new XmlParserException(
                "Could not find DurationUnit " + durationDist.getDURATIONUNIT() + " in tag of type DURATIONDISTTYPE");
    }

    /**
     * Parse an absolute time distribution, e.g. <code>UNIFORM(1, 3) s</code>.
     * @param streamMap the map with streams from the RUN tag
     * @param timeDist the tag to parse
     * @return a typed continuous random distribution.
     * @throws XmlParserException in case of a parse error.
     */
    public static ContinuousDistDoubleScalar.Abs<Time, TimeUnit, DurationUnit> parseTimeDist(final StreamInformation streamMap,
            final TIMEDISTTYPE timeDist) throws XmlParserException
    {
        DistContinuous dist = makeDistContinuous(streamMap, timeDist);
        for (TimeUnit unit : TimeUnit.BASE.getUnitsById().values())
        {
            if (unit.getDefaultAbbreviations().contains(timeDist.getTIMEUNIT()))
            {
                return new ContinuousDistDoubleScalar.Abs<Time, TimeUnit, DurationUnit>(dist, unit);
            }
        }
        throw new XmlParserException("Could not find TimeUnit " + timeDist.getTIMEUNIT() + " in tag of type TIMEDISTTYPE");
    }

    /**
     * Parse a relative speed distribution, e.g. <code>UNIFORM(1, 3) m/s</code>.
     * @param streamMap the map with streams from the RUN tag
     * @param speedDist the tag to parse
     * @return a typed continuous random distribution.
     * @throws XmlParserException in case of a parse error.
     */
    public static ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> parseSpeedDist(final StreamInformation streamMap,
            final SPEEDDISTTYPE speedDist) throws XmlParserException
    {
        DistContinuous dist = makeDistContinuous(streamMap, speedDist);
        for (SpeedUnit unit : SpeedUnit.BASE.getUnitsById().values())
        {
            if (unit.getDefaultAbbreviations().contains(speedDist.getSPEEDUNIT()))
            {
                return new ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit>(dist, unit);
            }
        }
        throw new XmlParserException("Could not find SpeedUnit " + speedDist.getSPEEDUNIT() + " in tag of type SPEEDDISTTYPE");
    }

    /**
     * Parse a relative acceleration distribution, e.g. <code>UNIFORM(1, 3) s</code>.
     * @param streamMap the map with streams from the RUN tag
     * @param accelerationDist the tag to parse
     * @return a typed continuous random distribution.
     * @throws XmlParserException in case of a parse error.
     */
    public static ContinuousDistDoubleScalar.Rel<Acceleration, AccelerationUnit> parseAccelerationDist(
            final StreamInformation streamMap, final ACCELERATIONDISTTYPE accelerationDist) throws XmlParserException
    {
        DistContinuous dist = makeDistContinuous(streamMap, accelerationDist);
        for (AccelerationUnit unit : AccelerationUnit.BASE.getUnitsById().values())
        {
            if (unit.getDefaultAbbreviations().contains(accelerationDist.getACCELERATIONUNIT()))
            {
                return new ContinuousDistDoubleScalar.Rel<Acceleration, AccelerationUnit>(dist, unit);
            }
        }
        throw new XmlParserException("Could not find AccelerationUnit " + accelerationDist.getACCELERATIONUNIT()
                + " in tag of type ACCELERATIONDISTTYPE");
    }

    /**
     * Parse a relative frequency distribution, e.g. <code>UNIFORM(1, 3) s</code>.
     * @param streamMap the map with streams from the RUN tag
     * @param frequencyDist the tag to parse
     * @return a typed continuous random distribution.
     * @throws XmlParserException in case of a parse error.
     */
    public static ContinuousDistDoubleScalar.Rel<Frequency, FrequencyUnit> parseFrequencyDist(final StreamInformation streamMap,
            final FREQUENCYDISTTYPE frequencyDist) throws XmlParserException
    {
        DistContinuous dist = makeDistContinuous(streamMap, frequencyDist);
        for (FrequencyUnit unit : FrequencyUnit.BASE.getUnitsById().values())
        {
            if (unit.getDefaultAbbreviations().contains(frequencyDist.getFREQUENCYUNIT()))
            {
                return new ContinuousDistDoubleScalar.Rel<Frequency, FrequencyUnit>(dist, unit);
            }
        }
        throw new XmlParserException(
                "Could not find FrequencyUnit " + frequencyDist.getFREQUENCYUNIT() + " in tag of type FREQUENCYDISTTYPE");
    }

    /**
     * Parse a relative linear density distribution, e.g. <code>UNIFORM(1, 3) s</code>.
     * @param streamMap the map with streams from the RUN tag
     * @param linearDensityDist the tag to parse
     * @return a typed continuous random distribution.
     * @throws XmlParserException in case of a parse error.
     */
    public static ContinuousDistDoubleScalar.Rel<LinearDensity, LinearDensityUnit> parseLinearDensityDist(
            final StreamInformation streamMap, final LINEARDENSITYDISTTYPE linearDensityDist) throws XmlParserException
    {
        DistContinuous dist = makeDistContinuous(streamMap, linearDensityDist);
        for (LinearDensityUnit unit : LinearDensityUnit.BASE.getUnitsById().values())
        {
            if (unit.getDefaultAbbreviations().contains(linearDensityDist.getLINEARDENSITYUNIT()))
            {
                return new ContinuousDistDoubleScalar.Rel<LinearDensity, LinearDensityUnit>(dist, unit);
            }
        }
        throw new XmlParserException("Could not find FrequencyUnit " + linearDensityDist.getLINEARDENSITYUNIT()
                + " in tag of type FREQUENCYDISTTYPE");
    }

}
