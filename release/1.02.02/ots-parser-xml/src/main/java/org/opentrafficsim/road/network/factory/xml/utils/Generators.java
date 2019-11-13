package org.opentrafficsim.road.network.factory.xml.utils;

import java.util.Map;

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
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.distributions.Generator;
import org.opentrafficsim.core.distributions.ProbabilityException;
import org.opentrafficsim.core.units.distributions.ContinuousDistDoubleScalar;
import org.opentrafficsim.road.network.factory.xml.XmlParserException;
import org.opentrafficsim.xml.generated.ACCELERATIONDISTTYPE;
import org.opentrafficsim.xml.generated.DURATIONDISTTYPE;
import org.opentrafficsim.xml.generated.LENGTHDISTTYPE;
import org.opentrafficsim.xml.generated.POSITIONDISTTYPE;
import org.opentrafficsim.xml.generated.SPEEDDISTTYPE;
import org.opentrafficsim.xml.generated.TIMEDISTTYPE;

/**
 * Generators based on distribution tags for typed scalars. <br>
 * <br>
 * Copyright (c) 2003-2018 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public final class Generators
{
    /**
     * 
     */
    private Generators()
    {
        // utility class
    }

    /**
     * Parse a Length distribution into a Generator for Lengths
     * @param streamMap the map with predefined streams
     * @param lengthDist the tag to parse
     * @return the generator
     * @throws XmlParserException on parse error
     */
    public static Generator<Length> makeLengthGenerator(Map<String, StreamInformation> streamMap,
            final LENGTHDISTTYPE lengthDist) throws XmlParserException
    {
        try
        {
            final ContinuousDistDoubleScalar.Rel<Length, LengthUnit> dist =
                    ParseDistribution.parseLengthDist(streamMap, lengthDist);
            Generator<Length> generator = new Generator<Length>()
            {
                @Override
                public Length draw() throws ProbabilityException, ParameterException
                {
                    return dist.draw();
                }

                /** {@inheritDoc} */
                @Override
                public String toString()
                {
                    return "Generator<Length>(" + dist.getDistribution().toString() + " " + dist.getUnit() + ")";
                }
            };
            return generator;
        }
        catch (Exception exception)
        {
            throw new XmlParserException(exception);
        }
    }

    /**
     * Parse a Position distribution into a Generator for Positions
     * @param streamMap the map with predefined streams
     * @param positionDist the tag to parse
     * @return the generator
     * @throws XmlParserException on parse error
     */
    public static Generator<Position> makePositionGenerator(Map<String, StreamInformation> streamMap,
            final POSITIONDISTTYPE positionDist) throws XmlParserException
    {
        try
        {
            final ContinuousDistDoubleScalar.Abs<Position, PositionUnit, LengthUnit> dist =
                    ParseDistribution.parsePositionDist(streamMap, positionDist);
            Generator<Position> generator = new Generator<Position>()
            {
                @Override
                public Position draw() throws ProbabilityException, ParameterException
                {
                    return dist.draw();
                }

                /** {@inheritDoc} */
                @Override
                public String toString()
                {
                    return "Generator<Position>(" + dist.getDistribution().toString() + " " + dist.getUnit() + ")";
                }
            };
            return generator;
        }
        catch (Exception exception)
        {
            throw new XmlParserException(exception);
        }
    }

    /**
     * Parse a Duration distribution into a Generator for Durations
     * @param streamMap the map with predefined streams
     * @param durationDist the tag to parse
     * @return the generator
     * @throws XmlParserException on parse error
     */
    public static Generator<Duration> makeDurationGenerator(Map<String, StreamInformation> streamMap,
            final DURATIONDISTTYPE durationDist) throws XmlParserException
    {
        try
        {
            final ContinuousDistDoubleScalar.Rel<Duration, DurationUnit> dist =
                    ParseDistribution.parseDurationDist(streamMap, durationDist);
            Generator<Duration> generator = new Generator<Duration>()
            {
                @Override
                public Duration draw() throws ProbabilityException, ParameterException
                {
                    return dist.draw();
                }

                /** {@inheritDoc} */
                @Override
                public String toString()
                {
                    return "Generator<Duration>(" + dist.getDistribution().toString() + " " + dist.getUnit() + ")";
                }
            };
            return generator;
        }
        catch (Exception exception)
        {
            throw new XmlParserException(exception);
        }
    }

    /**
     * Parse a Time distribution into a Generator for Times
     * @param streamMap the map with predefined streams
     * @param timeDist the tag to parse
     * @return the generator
     * @throws XmlParserException on parse error
     */
    public static Generator<Time> makeTimeGenerator(Map<String, StreamInformation> streamMap, final TIMEDISTTYPE timeDist)
            throws XmlParserException
    {
        try
        {
            final ContinuousDistDoubleScalar.Abs<Time, TimeUnit, DurationUnit> dist =
                    ParseDistribution.parseTimeDist(streamMap, timeDist);
            Generator<Time> generator = new Generator<Time>()
            {
                @Override
                public Time draw() throws ProbabilityException, ParameterException
                {
                    return dist.draw();
                }

                /** {@inheritDoc} */
                @Override
                public String toString()
                {
                    return "Generator<Time>(" + dist.getDistribution().toString() + " " + dist.getUnit() + ")";
                }
            };
            return generator;
        }
        catch (Exception exception)
        {
            throw new XmlParserException(exception);
        }
    }

    /**
     * Parse a Speed distribution into a Generator for Speeds
     * @param streamMap the map with predefined streams
     * @param speedDist the tag to parse
     * @return the generator
     * @throws XmlParserException on parse error
     */
    public static Generator<Speed> makeSpeedGenerator(Map<String, StreamInformation> streamMap, final SPEEDDISTTYPE speedDist)
            throws XmlParserException
    {
        try
        {
            final ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> dist =
                    ParseDistribution.parseSpeedDist(streamMap, speedDist);
            Generator<Speed> generator = new Generator<Speed>()
            {
                @Override
                public Speed draw() throws ProbabilityException, ParameterException
                {
                    return dist.draw();
                }

                /** {@inheritDoc} */
                @Override
                public String toString()
                {
                    return "Generator<Speed>(" + dist.getDistribution().toString() + " " + dist.getUnit() + ")";
                }
            };
            return generator;
        }
        catch (Exception exception)
        {
            throw new XmlParserException(exception);
        }
    }

    /**
     * Parse an Acceleration distribution into a Generator for Accelerations.
     * @param streamMap the map with predefined streams
     * @param accelerationDist the tag to parse
     * @return the generator
     * @throws XmlParserException on parse error
     */
    public static Generator<Acceleration> makeAccelerationGenerator(Map<String, StreamInformation> streamMap,
            final ACCELERATIONDISTTYPE accelerationDist) throws XmlParserException
    {
        try
        {
            final ContinuousDistDoubleScalar.Rel<Acceleration, AccelerationUnit> dist =
                    ParseDistribution.parseAccelerationDist(streamMap, accelerationDist);
            Generator<Acceleration> generator = new Generator<Acceleration>()
            {
                @Override
                public Acceleration draw() throws ProbabilityException, ParameterException
                {
                    return dist.draw();
                }

                /** {@inheritDoc} */
                @Override
                public String toString()
                {
                    return "Generator<Acceleration>(" + dist.getDistribution().toString() + " " + dist.getUnit() + ")";
                }
            };
            return generator;
        }
        catch (Exception exception)
        {
            throw new XmlParserException(exception);
        }
    }

    /**
     * Parse an Acceleration distribution into a Generator for Decelerations (accceleration with a minus sign).
     * @param streamMap the map with predefined streams
     * @param decelerationDist the tag to parse
     * @return the generator
     * @throws XmlParserException on parse error
     */
    public static Generator<Acceleration> makeDecelerationGenerator(Map<String, StreamInformation> streamMap,
            final ACCELERATIONDISTTYPE decelerationDist) throws XmlParserException
    {
        try
        {
            final ContinuousDistDoubleScalar.Rel<Acceleration, AccelerationUnit> dist =
                    ParseDistribution.parseAccelerationDist(streamMap, decelerationDist);
            Generator<Acceleration> generator = new Generator<Acceleration>()
            {
                @Override
                public Acceleration draw() throws ProbabilityException, ParameterException
                {
                    return dist.draw().multiplyBy(-1.0);
                }

                /** {@inheritDoc} */
                @Override
                public String toString()
                {
                    return "Generator<Deceleration>(" + dist.getDistribution().toString() + " " + dist.getUnit() + ")";
                }
            };
            return generator;
        }
        catch (Exception exception)
        {
            throw new XmlParserException(exception);
        }
    }

}
