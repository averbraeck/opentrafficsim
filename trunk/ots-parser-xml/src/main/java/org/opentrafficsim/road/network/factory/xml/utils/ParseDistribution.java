package org.opentrafficsim.road.network.factory.xml.utils;

import java.util.Map;

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
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.units.distributions.ContinuousDistDoubleScalar;
import org.opentrafficsim.xml.generated.ACCELERATIONDISTTYPE;
import org.opentrafficsim.xml.generated.CONTDISTTYPE;
import org.opentrafficsim.xml.generated.DURATIONDISTTYPE;
import org.opentrafficsim.xml.generated.LENGTHDISTTYPE;
import org.opentrafficsim.xml.generated.POSITIONDISTTYPE;
import org.opentrafficsim.xml.generated.SPEEDDISTTYPE;
import org.opentrafficsim.xml.generated.TIMEDISTTYPE;

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
     * @param streamMap map with stream information
     * @param distType the distribution to parse
     * @return the generated distribution.
     * @throws NetworkException in case distribution unknown or parameter number does not match.
     */
    private static DistContinuous makeDistContinuous(final Map<String, StreamInformation> streamMap,
            final CONTDISTTYPE distType) throws NetworkException
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
            return new DistErlang(stream, distType.getERLANG().getK().intValue(), distType.getERLANG().getMEAN());
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
        else
        {
            throw new NetworkException("makeDistContinuous - unknown distribution function " + distType);
        }
    }

    /**
     * Find and return the stream belonging to te streamId.
     * @param streamMap the map with streams from the RUN tag
     * @param streamId the id to search for
     * @return the stream belonging to te streamId
     * @throws NetworkException when the stream could not be found
     */
    private static StreamInterface findStream(final Map<String, StreamInformation> streamMap, final String streamId)
            throws NetworkException
    {
        StreamInformation streamInformation = streamMap.get(streamId);
        if (streamInformation == null)
        {
            throw new NetworkException("Could not find stream with ID=" + streamId);
        }
        return streamInformation.getStream();
    }

    /**
     * Parse a relative length distribution, e.g. <code>UNIFORM(1, 3) m</code>.
     * @param streamMap the map with streams from the RUN tag
     * @param lengthDist the tag to parse
     * @return a typed continuous random distribution.
     * @throws NetworkException in case of a parse error.
     */
    public static ContinuousDistDoubleScalar.Rel<Length, LengthUnit> parseLengthDist(
            final Map<String, StreamInformation> streamMap, final LENGTHDISTTYPE lengthDist) throws NetworkException
    {
        DistContinuous dist = makeDistContinuous(streamMap, lengthDist);
        LengthUnit lengthUnit = null;
        for (LengthUnit unit : Unit.getUnits(LengthUnit.class))
        {
            if (unit.getDefaultLocaleTextualRepresentations().contains(lengthDist.getLENGTHUNIT()))
            {
                lengthUnit = unit;
                break;
            }
        }
        if (lengthUnit == null)
        {
            throw new NetworkException(
                    "Could not find LengthUnit " + lengthDist.getLENGTHUNIT() + " in tag of type LENGTHDISTTYPE");
        }
        return new ContinuousDistDoubleScalar.Rel<Length, LengthUnit>(dist, lengthUnit);
    }

    /**
     * Parse an absolute position distribution, e.g. <code>UNIFORM(1, 3) m</code>.
     * @param streamMap the map with streams from the RUN tag
     * @param positionDist the tag to parse
     * @return a typed continuous random distribution.
     * @throws NetworkException in case of a parse error.
     */
    public static ContinuousDistDoubleScalar.Abs<Position, PositionUnit, LengthUnit> parsePositionDist(
            final Map<String, StreamInformation> streamMap, final POSITIONDISTTYPE positionDist) throws NetworkException
    {
        DistContinuous dist = makeDistContinuous(streamMap, positionDist);
        PositionUnit positionUnit = null;
        for (PositionUnit unit : Unit.getUnits(PositionUnit.class))
        {
            if (unit.getDefaultLocaleTextualRepresentations().contains(positionDist.getPOSITIONUNIT()))
            {
                positionUnit = unit;
                break;
            }
        }
        if (positionUnit == null)
        {
            throw new NetworkException(
                    "Could not find PositionUnit " + positionDist.getPOSITIONUNIT() + " in tag of type POSITIONDISTTYPE");
        }
        return new ContinuousDistDoubleScalar.Abs<Position, PositionUnit, LengthUnit>(dist, positionUnit);
    }

    /**
     * Parse a relative duration distribution, e.g. <code>UNIFORM(1, 3) s</code>.
     * @param streamMap the map with streams from the RUN tag
     * @param durationDist the tag to parse
     * @return a typed continuous random distribution.
     * @throws NetworkException in case of a parse error.
     */
    public static ContinuousDistDoubleScalar.Rel<Duration, DurationUnit> parseDurationDist(
            final Map<String, StreamInformation> streamMap, final DURATIONDISTTYPE durationDist) throws NetworkException
    {
        DistContinuous dist = makeDistContinuous(streamMap, durationDist);
        DurationUnit durationUnit = null;
        for (DurationUnit unit : Unit.getUnits(DurationUnit.class))
        {
            if (unit.getDefaultLocaleTextualRepresentations().contains(durationDist.getDURATIONUNIT()))
            {
                durationUnit = unit;
                break;
            }
        }
        if (durationUnit == null)
        {
            throw new NetworkException(
                    "Could not find DurationUnit " + durationDist.getDURATIONUNIT() + " in tag of type DURATIONDISTTYPE");
        }
        return new ContinuousDistDoubleScalar.Rel<Duration, DurationUnit>(dist, durationUnit);
    }

    /**
     * Parse an absolute time distribution, e.g. <code>UNIFORM(1, 3) s</code>.
     * @param streamMap the map with streams from the RUN tag
     * @param timeDist the tag to parse
     * @return a typed continuous random distribution.
     * @throws NetworkException in case of a parse error.
     */
    public static ContinuousDistDoubleScalar.Abs<Time, TimeUnit, DurationUnit> parseTimeDist(
            final Map<String, StreamInformation> streamMap, final TIMEDISTTYPE timeDist) throws NetworkException
    {
        DistContinuous dist = makeDistContinuous(streamMap, timeDist);
        TimeUnit timeUnit = null;
        for (TimeUnit unit : Unit.getUnits(TimeUnit.class))
        {
            if (unit.getDefaultLocaleTextualRepresentations().contains(timeDist.getTIMEUNIT()))
            {
                timeUnit = unit;
                break;
            }
        }
        if (timeUnit == null)
        {
            throw new NetworkException("Could not find TimeUnit " + timeDist.getTIMEUNIT() + " in tag of type TIMEDISTTYPE");
        }
        return new ContinuousDistDoubleScalar.Abs<Time, TimeUnit, DurationUnit>(dist, timeUnit);
    }

    /**
     * Parse a relative speed distribution, e.g. <code>UNIFORM(1, 3) m/s</code>.
     * @param streamMap the map with streams from the RUN tag
     * @param speedDist the tag to parse
     * @return a typed continuous random distribution.
     * @throws NetworkException in case of a parse error.
     */
    public static ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> parseSpeedDist(
            final Map<String, StreamInformation> streamMap, final SPEEDDISTTYPE speedDist) throws NetworkException
    {
        DistContinuous dist = makeDistContinuous(streamMap, speedDist);
        SpeedUnit speedUnit = null;
        for (SpeedUnit unit : Unit.getUnits(SpeedUnit.class))
        {
            if (unit.getDefaultLocaleTextualRepresentations().contains(speedDist.getSPEEDUNIT()))
            {
                speedUnit = unit;
                break;
            }
        }
        if (speedUnit == null)
        {
            throw new NetworkException(
                    "Could not find SpeedUnit " + speedDist.getSPEEDUNIT() + " in tag of type SPEEDDISTTYPE");
        }
        return new ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit>(dist, speedUnit);
    }

    /**
     * Parse a relative acceleration distribution, e.g. <code>UNIFORM(1, 3) s</code>.
     * @param streamMap the map with streams from the RUN tag
     * @param accelerationDist the tag to parse
     * @return a typed continuous random distribution.
     * @throws NetworkException in case of a parse error.
     */
    public static ContinuousDistDoubleScalar.Rel<Acceleration, AccelerationUnit> parseAccelerationDist(
            final Map<String, StreamInformation> streamMap, final ACCELERATIONDISTTYPE accelerationDist) throws NetworkException
    {
        DistContinuous dist = makeDistContinuous(streamMap, accelerationDist);
        AccelerationUnit accelerationUnit = null;
        for (AccelerationUnit unit : Unit.getUnits(AccelerationUnit.class))
        {
            if (unit.getDefaultLocaleTextualRepresentations().contains(accelerationDist.getACCELERATIONUNIT()))
            {
                accelerationUnit = unit;
                break;
            }
        }
        if (accelerationUnit == null)
        {
            throw new NetworkException("Could not find AccelerationUnit " + accelerationDist.getACCELERATIONUNIT()
                    + " in tag of type ACCELERATIONDISTTYPE");
        }
        return new ContinuousDistDoubleScalar.Rel<Acceleration, AccelerationUnit>(dist, accelerationUnit);
    }

}
