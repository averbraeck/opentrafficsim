package org.opentrafficsim.core.network.factory.xml.units;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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
import org.junit.Test;
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
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jan 17, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
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
        assertEquals("unit is length", LengthUnit.METER, lengthDist.getDisplayUnit());
        assertEquals("distribution is uniform", DistUniform.class, lengthDist.getDistribution().getClass());
        ContinuousDistDoubleScalar.Abs<Position, PositionUnit, LengthUnit> positionDist =
                Distributions.parsePositionDist("UNIFORM(1, 3) m");
        assertEquals("unit is position", PositionUnit.METER, positionDist.getDisplayUnit());
        assertEquals("distribution is uniform", DistUniform.class, positionDist.getDistribution().getClass());
        ContinuousDistDoubleScalar.Rel<Duration, DurationUnit> durationDist =
                Distributions.parseDurationDist("UNIFORM(1, 3) s");
        assertEquals("unit is duration", DurationUnit.SI, durationDist.getDisplayUnit());
        assertEquals("distribution is uniform", DistUniform.class, durationDist.getDistribution().getClass());
        ContinuousDistDoubleScalar.Abs<Time, TimeUnit, DurationUnit> timeDist = Distributions.parseTimeDist("UNIFORM(1, 3) s");
        assertEquals("unit is time", TimeUnit.DEFAULT, timeDist.getDisplayUnit());
        assertEquals("distribution is uniform", DistUniform.class, positionDist.getDistribution().getClass());
        ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> speedDist = Distributions.parseSpeedDist("UNIFORM(1, 3) m/s");
        assertEquals("unit is speed", SpeedUnit.METER_PER_SECOND, speedDist.getDisplayUnit());
        assertEquals("distribution is uniform", DistUniform.class, speedDist.getDistribution().getClass());
        // Test the various distributions
        ContinuousDistDoubleScalar.Rel<Length, LengthUnit> dist = Distributions.parseLengthDist("CONST(123) m");
        assertEquals("distribution is constant", DistConstant.class, dist.getDistribution().getClass());
        dist = Distributions.parseLengthDist("CONSTANT(456) km");
        assertEquals("distribution is constant", DistConstant.class, dist.getDistribution().getClass());
        assertEquals("Const value in SI", 456000, dist.draw().si, 0);
        dist = Distributions.parseLengthDist("EXPO(456) km");
        assertEquals("distribution is exponential", DistExponential.class, dist.getDistribution().getClass());
        dist = Distributions.parseLengthDist("EXPONENTIAL(456) km");
        assertEquals("distribution is exponential", DistExponential.class, dist.getDistribution().getClass());
        dist = Distributions.parseLengthDist("TRIA(1, 3, 4) km");
        assertEquals("distribution is triangular", DistTriangular.class, dist.getDistribution().getClass());
        dist = Distributions.parseLengthDist("TRIANGULAR( 1 , 3 , 4) km"); // also play a bit with extra spaces
        assertEquals("distribution is triangular", DistTriangular.class, dist.getDistribution().getClass());
        dist = Distributions.parseLengthDist("NORM(1,3) km"); // also play a bit with no spaces
        assertEquals("distribution is normal", DistNormal.class, dist.getDistribution().getClass());
        dist = Distributions.parseLengthDist("NORMAL(1,3) km");
        assertEquals("distribution is normal", DistNormal.class, dist.getDistribution().getClass());
        dist = Distributions.parseLengthDist("BETA(1,3) km");
        assertEquals("distribution is Beta", DistBeta.class, dist.getDistribution().getClass());
        dist = Distributions.parseLengthDist("ERLANG(1,3) km");
        assertEquals("distribution is Erlang", DistErlang.class, dist.getDistribution().getClass());
        dist = Distributions.parseLengthDist("GAMMA(1,3) km");
        assertEquals("distribution is Gamma", DistGamma.class, dist.getDistribution().getClass());
        dist = Distributions.parseLengthDist("LOGNORMAL(1,3) km");
        assertEquals("distribution is lognormal", DistLogNormal.class, dist.getDistribution().getClass());
        dist = Distributions.parseLengthDist("PEARSON5(1,3) km");
        assertEquals("distribution is Pearson5", DistPearson5.class, dist.getDistribution().getClass());
        dist = Distributions.parseLengthDist("PEARSON6(1,3, 5) km");
        assertEquals("distribution is Pearson6", DistPearson6.class, dist.getDistribution().getClass());
        dist = Distributions.parseLengthDist("WEIB(1,3, 5) km");
        assertEquals("distribution is Weibull", DistWeibull.class, dist.getDistribution().getClass());
        dist = Distributions.parseLengthDist("WEIBULL(1,3, 5) km");
        assertEquals("distribution is Weibull", DistWeibull.class, dist.getDistribution().getClass());
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
