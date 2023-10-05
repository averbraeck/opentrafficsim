package org.opentrafficsim.core.network.factory.xml.units;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

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
import org.junit.jupiter.api.Test;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.units.distributions.ContinuousDistDoubleScalar;

import nl.tudelft.simulation.jstats.distributions.DistBeta;
import nl.tudelft.simulation.jstats.distributions.DistConstant;
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

/**
 * Test the Distributions parser.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class DistributionsTest
{

    /**
     * Test the Distributions parser.
     * @throws NetworkException if that happens uncaught; this test has failed
     */
    @Test
    public final void testDistributions() throws NetworkException
    {
        // Test the various quantities
        ContinuousDistDoubleScalar.Rel<Length, LengthUnit> lengthDist = Distributions.parseLengthDist("UNIFORM(1, 3) m");
        assertEquals(LengthUnit.METER, lengthDist.getDisplayUnit(), "unit is length");
        assertEquals(DistUniform.class, lengthDist.getDistribution().getClass(), "distribution is uniform");
        ContinuousDistDoubleScalar.Abs<Position, PositionUnit, LengthUnit> positionDist =
                Distributions.parsePositionDist("UNIFORM(1, 3) m");
        assertEquals(PositionUnit.METER, positionDist.getDisplayUnit(), "unit is position");
        assertEquals(DistUniform.class, positionDist.getDistribution().getClass(), "distribution is uniform");
        ContinuousDistDoubleScalar.Rel<Duration, DurationUnit> durationDist =
                Distributions.parseDurationDist("UNIFORM(1, 3) s");
        assertEquals(DurationUnit.SI, durationDist.getDisplayUnit(), "unit is duration");
        assertEquals(DistUniform.class, durationDist.getDistribution().getClass(), "distribution is uniform");
        ContinuousDistDoubleScalar.Abs<Time, TimeUnit, DurationUnit> timeDist = Distributions.parseTimeDist("UNIFORM(1, 3) s");
        assertEquals(TimeUnit.DEFAULT, timeDist.getDisplayUnit(), "unit is time");
        assertEquals(DistUniform.class, positionDist.getDistribution().getClass(), "distribution is uniform");
        ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> speedDist = Distributions.parseSpeedDist("UNIFORM(1, 3) m/s");
        assertEquals(SpeedUnit.METER_PER_SECOND, speedDist.getDisplayUnit(), "unit is speed");
        assertEquals(DistUniform.class, speedDist.getDistribution().getClass(), "distribution is uniform");
        // Test the various distributions
        ContinuousDistDoubleScalar.Rel<Length, LengthUnit> dist = Distributions.parseLengthDist("CONST(123) m");
        assertEquals(DistConstant.class, dist.getDistribution().getClass(), "distribution is constant");
        dist = Distributions.parseLengthDist("CONSTANT(456) km");
        assertEquals(DistConstant.class, dist.getDistribution().getClass(), "distribution is constant");
        assertEquals(456000, dist.draw().si, 0, "Const value in SI");
        dist = Distributions.parseLengthDist("EXPO(456) km");
        assertEquals(DistExponential.class, dist.getDistribution().getClass(), "distribution is exponential");
        dist = Distributions.parseLengthDist("EXPONENTIAL(456) km");
        assertEquals(DistExponential.class, dist.getDistribution().getClass(), "distribution is exponential");
        dist = Distributions.parseLengthDist("TRIA(1, 3, 4) km");
        assertEquals(DistTriangular.class, dist.getDistribution().getClass(), "distribution is triangular");
        dist = Distributions.parseLengthDist("TRIANGULAR( 1 , 3 , 4) km"); // also play a bit with extra spaces
        assertEquals(DistTriangular.class, dist.getDistribution().getClass(), "distribution is triangular");
        dist = Distributions.parseLengthDist("NORM(1,3) km"); // also play a bit with no spaces
        assertEquals(DistNormal.class, dist.getDistribution().getClass(), "distribution is normal");
        dist = Distributions.parseLengthDist("NORMAL(1,3) km");
        assertEquals(DistNormal.class, dist.getDistribution().getClass(), "distribution is normal");
        dist = Distributions.parseLengthDist("BETA(1,3) km");
        assertEquals(DistBeta.class, dist.getDistribution().getClass(), "distribution is Beta");
        dist = Distributions.parseLengthDist("ERLANG(1,3) km");
        assertEquals(DistErlang.class, dist.getDistribution().getClass(), "distribution is Erlang");
        dist = Distributions.parseLengthDist("GAMMA(1,3) km");
        assertEquals(DistGamma.class, dist.getDistribution().getClass(), "distribution is Gamma");
        dist = Distributions.parseLengthDist("LOGNORMAL(1,3) km");
        assertEquals(DistLogNormal.class, dist.getDistribution().getClass(), "distribution is lognormal");
        dist = Distributions.parseLengthDist("PEARSON5(1,3) km");
        assertEquals(DistPearson5.class, dist.getDistribution().getClass(), "distribution is Pearson5");
        dist = Distributions.parseLengthDist("PEARSON6(1,3, 5) km");
        assertEquals(DistPearson6.class, dist.getDistribution().getClass(), "distribution is Pearson6");
        dist = Distributions.parseLengthDist("WEIB(1,3, 5) km");
        assertEquals(DistWeibull.class, dist.getDistribution().getClass(), "distribution is Weibull");
        dist = Distributions.parseLengthDist("WEIBULL(1,3, 5) km");
        assertEquals(DistWeibull.class, dist.getDistribution().getClass(), "distribution is Weibull");
        // FIXME: non-integer first argument for ERLANG is quietly rounded to nearest integer value
        // Test the various exceptions
        try
        {
            Distributions.parseLengthDist("UNIFORM(3, 1) km");
            fail("illegal interval should have thrown an IllegalArgumentException");
        }
        catch (IllegalArgumentException iae)
        {
            // Ignore expected exception
        }
        try
        {
            Distributions.parseLengthDist("NONEXISTENTDIST(1,2) km");
            fail("non existent distribution name should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        try
        {
            Distributions.parseLengthDist("CONST(2)");
            fail("distribution with missing unit should have thrown an ArrayIndexOutOfBoundsException");
        }
        catch (ArrayIndexOutOfBoundsException aioobe)
        {
            // Ignore expected exception
        }
    }
}
