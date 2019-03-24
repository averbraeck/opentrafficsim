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
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.distributions.Generator;
import org.opentrafficsim.core.distributions.ProbabilityException;
import org.opentrafficsim.core.units.distributions.ContinuousDistDoubleScalar;
import org.opentrafficsim.road.network.factory.xml.XmlParserException;

import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Generators.java. <br>
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
     * @param stream the stream to use
     * @param s the string to parse
     * @return the generator
     * @throws XmlParserException on parse error
     */
    public static Generator<Length> makeLengthGenerator(final StreamInterface stream, final String s) throws XmlParserException
    {
        try
        {
            final ContinuousDistDoubleScalar.Rel<Length, LengthUnit> dist = ParseDistribution.parseLengthDist(stream, s);
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
     * @param stream the stream to use
     * @param s the string to parse
     * @return the generator
     * @throws XmlParserException on parse error
     */
    public static Generator<Position> makePositionGenerator(final StreamInterface stream, final String s)
            throws XmlParserException
    {
        try
        {
            final ContinuousDistDoubleScalar.Abs<Position, PositionUnit, LengthUnit> dist =
                    ParseDistribution.parsePositionDist(stream, s);
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
     * @param stream the stream to use
     * @param s the string to parse
     * @return the generator
     * @throws XmlParserException on parse error
     */
    public static Generator<Duration> makeDurationGenerator(final StreamInterface stream, final String s)
            throws XmlParserException
    {
        try
        {
            final ContinuousDistDoubleScalar.Rel<Duration, DurationUnit> dist = ParseDistribution.parseDurationDist(stream, s);
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
     * @param stream the stream to use
     * @param s the string to parse
     * @return the generator
     * @throws XmlParserException on parse error
     */
    public static Generator<Time> makeTimeGenerator(final StreamInterface stream, final String s) throws XmlParserException
    {
        try
        {
            final ContinuousDistDoubleScalar.Abs<Time, TimeUnit, DurationUnit> dist =
                    ParseDistribution.parseTimeDist(stream, s);
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
     * @param stream the stream to use
     * @param s the string to parse
     * @return the generator
     * @throws XmlParserException on parse error
     */
    public static Generator<Speed> makeSpeedGenerator(final StreamInterface stream, final String s) throws XmlParserException
    {
        try
        {
            final ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> dist = ParseDistribution.parseSpeedDist(stream, s);
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
     * @param stream the stream to use
     * @param s the string to parse
     * @return the generator
     * @throws XmlParserException on parse error
     */
    public static Generator<Acceleration> makeAccelerationGenerator(final StreamInterface stream, final String s)
            throws XmlParserException
    {
        try
        {
            final ContinuousDistDoubleScalar.Rel<Acceleration, AccelerationUnit> dist =
                    ParseDistribution.parseAccelerationDist(stream, s);
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
     * @param stream the stream to use
     * @param s the string to parse
     * @return the generator
     * @throws XmlParserException on parse error
     */
    public static Generator<Acceleration> makeDecelerationGenerator(final StreamInterface stream, final String s)
            throws XmlParserException
    {
        try
        {
            final ContinuousDistDoubleScalar.Rel<Acceleration, AccelerationUnit> dist =
                    ParseDistribution.parseAccelerationDist(stream, s);
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
