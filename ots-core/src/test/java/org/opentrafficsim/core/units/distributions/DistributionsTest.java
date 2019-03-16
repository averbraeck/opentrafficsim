package org.opentrafficsim.core.units.distributions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.djunits.unit.AbsoluteLinearUnit;
import org.djunits.unit.AbsoluteTemperatureUnit;
import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.AngleSolidUnit;
import org.djunits.unit.AngleUnit;
import org.djunits.unit.AreaUnit;
import org.djunits.unit.DensityUnit;
import org.djunits.unit.DimensionlessUnit;
import org.djunits.unit.DirectionUnit;
import org.djunits.unit.DurationUnit;
import org.djunits.unit.ElectricalChargeUnit;
import org.djunits.unit.ElectricalCurrentUnit;
import org.djunits.unit.ElectricalPotentialUnit;
import org.djunits.unit.ElectricalResistanceUnit;
import org.djunits.unit.EnergyUnit;
import org.djunits.unit.FlowMassUnit;
import org.djunits.unit.FlowVolumeUnit;
import org.djunits.unit.ForceUnit;
import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.LinearDensityUnit;
import org.djunits.unit.LinearUnit;
import org.djunits.unit.MassUnit;
import org.djunits.unit.PositionUnit;
import org.djunits.unit.PowerUnit;
import org.djunits.unit.PressureUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TemperatureUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.unit.TorqueUnit;
import org.djunits.unit.Unit;
import org.djunits.unit.VolumeUnit;
import org.djunits.unit.unitsystem.UnitSystem;
import org.djunits.value.vdouble.scalar.AbstractDoubleScalarAbs;
import org.djunits.value.vdouble.scalar.AbstractDoubleScalarRel;
import org.djunits.value.vfloat.scalar.AbstractFloatScalarAbs;
import org.djunits.value.vfloat.scalar.AbstractFloatScalarRel;
import org.junit.Test;

import nl.tudelft.simulation.jstats.distributions.DistContinuous;
import nl.tudelft.simulation.jstats.distributions.DistDiscrete;

/**
 * Test the various distribution classes.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jan 13, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class DistributionsTest
{

    /** Next value returned by the draw method of the dummy DistContinuous. */
    private double nextDoubleResult = 1d;

    /**
     * Retrieve the current value of the nextDoubleResult field.
     * @return double; the current value of the nextDoubleResult field
     */
    final double getDoubleNextResult()
    {
        return this.nextDoubleResult;
    }

    /** Next value returned by the draw method of the dummy DistDiscrete. */
    private long nextLongResult = 1L;

    /**
     * Retrieve the current value of the nextLongResult field.
     * @return long; the current value of the nextLongResult field
     */
    final long getLongNextResult()
    {
        return this.nextLongResult;
    }

    /**
     * Test the various constructors.
     * @throws ClassNotFoundException if that happens uncaught; this test has failed
     */
    @Test
    public final void testConstructors() throws ClassNotFoundException
    {
        AbsoluteLinearUnit<?, ?>[] absoluteUnits =
                {DirectionUnit.BASE, PositionUnit.BASE, AbsoluteTemperatureUnit.KELVIN, TimeUnit.BASE, AbsJunkUnit.BASE};
        Unit<?>[] relativeUnits = {AccelerationUnit.SI, AngleUnit.SI, AngleSolidUnit.SI, AreaUnit.SI, DensityUnit.SI,
                DimensionlessUnit.SI, ElectricalChargeUnit.SI, ElectricalCurrentUnit.SI, ElectricalPotentialUnit.SI,
                ElectricalResistanceUnit.SI, EnergyUnit.SI, FlowMassUnit.SI, FlowVolumeUnit.SI, ForceUnit.SI, FrequencyUnit.SI,
                LengthUnit.SI, LinearDensityUnit.SI, MassUnit.SI, PowerUnit.SI, PressureUnit.SI, SpeedUnit.SI,
                TemperatureUnit.SI, DurationUnit.SI, TorqueUnit.SI, VolumeUnit.SI, JunkUnit.SI};

        DistContinuous distCont = new DistContinuous(null)
        {

            /** */
            private static final long serialVersionUID = 1L;

            @Override
            public double draw()
            {
                return getDoubleNextResult();
            }

            @Override
            public double probDensity(final double observation)
            {
                return 0;
            }
        };
        for (AbsoluteLinearUnit<?, ?> unit : absoluteUnits)
        {
            checkDoubleAbsContUnit(distCont, unit);
            checkFloatAbsContUnit(distCont, unit);
        }
        for (Unit<?> unit : relativeUnits)
        {
            checkDoubleRelContUnit(distCont, unit);
            checkFloatRelContUnit(distCont, unit);
        }

        DistDiscrete distDisc = new DistDiscrete(null)
        {

            /** */
            private static final long serialVersionUID = 1L;

            @Override
            public long draw()
            {
                return getLongNextResult();
            }

            @Override
            public double probability(final int observation)
            {
                return 0;
            }
        };
        for (AbsoluteLinearUnit<?, ?> unit : absoluteUnits)
        {
            checkDoubleAbsDiscUnit(distDisc, unit);
            checkFloatAbsDiscUnit(distDisc, unit);
        }
        for (Unit<?> unit : relativeUnits)
        {
            checkDoubleRelDiscUnit(distDisc, unit);
            checkFloatRelDiscUnit(distDisc, unit);
        }

    }

    /**
     * Exercise continuous distribution of an Absolute unit.
     * @param distribution DistContinuous; the random source
     * @param unit Unit
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void checkDoubleAbsContUnit(final DistContinuous distribution, final AbsoluteLinearUnit unit)
    {
        ContinuousDistDoubleScalar.Abs dist = new ContinuousDistDoubleScalar.Abs(distribution, unit);
        this.nextDoubleResult = 123.456;
        AbstractDoubleScalarAbs result = dist.draw();
        assertEquals("Unit matches", unit, result.getUnit());
        assertEquals("Value matches", this.nextDoubleResult, result.si, 0.0001);
        this.nextDoubleResult = 23.456;
        result = dist.draw();
        assertEquals("Value matches", this.nextDoubleResult, result.si, 0.0001);
        dist = new ContinuousDistDoubleScalar.Abs(1.234, unit);
        result = dist.draw();
        assertEquals("Unit matches", unit, result.getUnit());
        assertEquals("Value matches", 1.234, result.si, 0.0001);
        assertTrue("toString result contains ContinuousDistDoubleScalar.Abs",
                dist.toString().contains("ContinuousDistDoubleScalar.Abs"));
        assertTrue("toString method contains unit name " + unit.getClass().getSimpleName(),
                dist.toString().contains(unit.getClass().getSimpleName()));
    }

    /**
     * Exercise continuous distribution of a Relative unit.
     * @param distribution DistContinuous; the random source
     * @param unit Unit
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void checkDoubleRelContUnit(final DistContinuous distribution, final Unit unit)
    {
        ContinuousDistDoubleScalar.Rel dist = new ContinuousDistDoubleScalar.Rel(distribution, unit);
        this.nextDoubleResult = 123.456;
        AbstractDoubleScalarRel result = dist.draw();
        assertEquals("Unit matches", unit, result.getUnit());
        assertEquals("Value matches", this.nextDoubleResult, result.si, 0.0001);
        this.nextDoubleResult = 23.456;
        result = dist.draw();
        assertEquals("Value matches", this.nextDoubleResult, result.si, 0.0001);
        dist = new ContinuousDistDoubleScalar.Rel(1.234, unit);
        result = dist.draw();
        assertEquals("Unit matches", unit, result.getUnit());
        assertEquals("Value matches", 1.234, result.si, 0.0001);
        assertTrue("toString result contains ContinuousDistDoubleScalar.Rel",
                dist.toString().contains("ContinuousDistDoubleScalar.Rel"));
        assertTrue("toString method contains unit name " + unit.getClass().getSimpleName(),
                dist.toString().contains(unit.getClass().getSimpleName()));
    }

    /**
     * Exercise discrete distribution of an Absolute unit.
     * @param distribution DistDiscrete; the random source
     * @param unit Unit
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void checkDoubleAbsDiscUnit(final DistDiscrete distribution, final AbsoluteLinearUnit unit)
    {
        DiscreteDistDoubleScalar.Abs dist = new DiscreteDistDoubleScalar.Abs(distribution, unit);
        this.nextDoubleResult = 123.456;
        AbstractDoubleScalarAbs result = dist.draw();
        assertEquals("Unit matches", unit, result.getUnit());
        assertEquals("Value matches", this.nextLongResult, result.si, 0.0001);
        this.nextDoubleResult = 23.456;
        result = dist.draw();
        assertEquals("Value matches", this.getLongNextResult(), result.si, 0.0001);
        dist = new DiscreteDistDoubleScalar.Abs(1234, unit);
        result = dist.draw();
        assertEquals("Unit matches", unit, result.getUnit());
        assertEquals("Value matches", 1234, result.si, 0.0001);
        assertTrue("toString result contains DiscreteDistDoubleScalar.Abs",
                dist.toString().contains("DiscreteDistDoubleScalar.Abs"));
        assertTrue("toString method contains unit name " + unit.getClass().getSimpleName(),
                dist.toString().contains(unit.getClass().getSimpleName()));
    }

    /**
     * Exercise discrete distribution of a Relative unit.
     * @param distribution DistContinuous; the random source
     * @param unit Unit
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void checkDoubleRelDiscUnit(final DistDiscrete distribution, final Unit unit)
    {
        DiscreteDistDoubleScalar.Rel dist = new DiscreteDistDoubleScalar.Rel(distribution, unit);
        this.nextDoubleResult = 123.456;
        AbstractDoubleScalarRel result = dist.draw();
        assertEquals("Unit matches", unit, result.getUnit());
        assertEquals("Value matches", this.nextLongResult, result.si, 0.0001);
        this.nextDoubleResult = 23.456;
        result = dist.draw();
        assertEquals("Value matches", this.nextLongResult, result.si, 0.0001);
        dist = new DiscreteDistDoubleScalar.Rel(1234, unit);
        result = dist.draw();
        assertEquals("Unit matches", unit, result.getUnit());
        assertEquals("Value matches", 1234, result.si, 0.0001);
        assertTrue("toString result contains DiscreteDistDoubleScalar.Rel",
                dist.toString().contains("DiscreteDistDoubleScalar.Rel"));
        assertTrue("toString method contains unit name " + unit.getClass().getSimpleName(),
                dist.toString().contains(unit.getClass().getSimpleName()));
    }

    /**
     * Exercise continuous distribution of an Absolute unit.
     * @param distribution DistContinuous; the random source
     * @param unit Unit
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void checkFloatAbsContUnit(final DistContinuous distribution, final AbsoluteLinearUnit unit)
    {
        ContinuousDistFloatScalar.Abs dist = new ContinuousDistFloatScalar.Abs(distribution, unit);
        this.nextDoubleResult = 123.456;
        AbstractFloatScalarAbs result = dist.draw();
        assertEquals("Unit matches", unit, result.getUnit());
        assertEquals("Value matches", this.nextDoubleResult, result.si, 0.0001);
        this.nextDoubleResult = 23.456;
        result = dist.draw();
        assertEquals("Value matches", this.nextDoubleResult, result.si, 0.0001);
        dist = new ContinuousDistFloatScalar.Abs(1.234f, unit);
        result = dist.draw();
        assertEquals("Unit matches", unit, result.getUnit());
        assertEquals("Value matches", 1.234, result.si, 0.0001);
        assertTrue("toString result contains ContinuousDistFloatScalar.Abs",
                dist.toString().contains("ContinuousDistFloatScalar.Abs"));
        assertTrue("toString method contains unit name " + unit.getClass().getSimpleName(),
                dist.toString().contains(unit.getClass().getSimpleName()));
    }

    /**
     * Exercise continuous distribution of a Relative unit.
     * @param distribution DistContinuous; the random source
     * @param unit Unit
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void checkFloatRelContUnit(final DistContinuous distribution, final Unit unit)
    {
        ContinuousDistFloatScalar.Rel dist = new ContinuousDistFloatScalar.Rel(distribution, unit);
        this.nextDoubleResult = 123.456;
        AbstractFloatScalarRel result = dist.draw();
        assertEquals("Unit matches", unit, result.getUnit());
        assertEquals("Value matches", this.nextDoubleResult, result.si, 0.0001);
        this.nextDoubleResult = 23.456;
        result = dist.draw();
        assertEquals("Value matches", this.nextDoubleResult, result.si, 0.0001);
        dist = new ContinuousDistFloatScalar.Rel(1.234f, unit);
        result = dist.draw();
        assertEquals("Unit matches", unit, result.getUnit());
        assertEquals("Value matches", 1.234, result.si, 0.0001);
        assertTrue("toString result contains ContinuousDistFloatScalar.Rel",
                dist.toString().contains("ContinuousDistFloatScalar.Rel"));
        assertTrue("toString method contains unit name " + unit.getClass().getSimpleName(),
                dist.toString().contains(unit.getClass().getSimpleName()));
    }

    /**
     * Exercise discrete distribution of an Absolute unit.
     * @param distribution DistDiscrete; the random source
     * @param unit Unit
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void checkFloatAbsDiscUnit(final DistDiscrete distribution, final AbsoluteLinearUnit unit)
    {
        DiscreteDistFloatScalar.Abs dist = new DiscreteDistFloatScalar.Abs(distribution, unit);
        this.nextDoubleResult = 123.456;
        AbstractFloatScalarAbs result = dist.draw();
        assertEquals("Unit matches", unit, result.getUnit());
        assertEquals("Value matches", this.nextLongResult, result.si, 0.0001);
        this.nextDoubleResult = 23.456;
        result = dist.draw();
        assertEquals("Value matches", this.getLongNextResult(), result.si, 0.0001);
        dist = new DiscreteDistFloatScalar.Abs(1234, unit);
        result = dist.draw();
        assertEquals("Unit matches", unit, result.getUnit());
        assertEquals("Value matches", 1234, result.si, 0.0001);
        assertTrue("toString result contains DiscreteDistFloatScalar.Abs",
                dist.toString().contains("DiscreteDistFloatScalar.Abs"));
        assertTrue("toString method contains unit name " + unit.getClass().getSimpleName(),
                dist.toString().contains(unit.getClass().getSimpleName()));
    }

    /**
     * Exercise discrete distribution of a Relative unit.
     * @param distribution DistContinuous; the random source
     * @param unit Unit
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void checkFloatRelDiscUnit(final DistDiscrete distribution, final Unit unit)
    {
        DiscreteDistFloatScalar.Rel dist = new DiscreteDistFloatScalar.Rel(distribution, unit);
        this.nextDoubleResult = 123.456;
        AbstractFloatScalarRel result = dist.draw();
        assertEquals("Unit matches", unit, result.getUnit());
        assertEquals("Value matches", this.nextLongResult, result.si, 0.0001);
        this.nextDoubleResult = 23.456;
        result = dist.draw();
        assertEquals("Value matches", this.nextLongResult, result.si, 0.0001);
        dist = new DiscreteDistFloatScalar.Rel(1234, unit);
        result = dist.draw();
        assertEquals("Unit matches", unit, result.getUnit());
        assertEquals("Value matches", 1234, result.si, 0.0001);
        assertTrue("toString result contains DiscreteDistFloatScalar.Rel",
                dist.toString().contains("DiscreteDistFloatScalar.Rel"));
        assertTrue("toString method contains unit name " + unit.getClass().getSimpleName(),
                dist.toString().contains(unit.getClass().getSimpleName()));
    }

    /**
     * Relative Unit used for testing. Based on a LengthUnit.
     */
    static class JunkUnit extends LinearUnit<JunkUnit>
    {
        /** */
        private static final long serialVersionUID = 1L;

        /** The SI unit. */
        public static final JunkUnit SI;

        /**
         * @param lengthUnit LengthUnit ...
         * @param name String ...
         * @param abbreviation String ...
         * @param unitSystem UnitSystem ...
         */
        JunkUnit(final LengthUnit lengthUnit, final String name, final String abbreviation, final UnitSystem unitSystem)
        {
            super(name, abbreviation, unitSystem);
        }

        static
        {
            SI = new JunkUnit(LengthUnit.METER, "Junk", "JNK", null);
        }

        /** {@inheritDoc} */
        @Override
        public JunkUnit getStandardUnit()
        {
            return SI;
        }

        /** {@inheritDoc} */
        @Override
        public String getSICoefficientsString()
        {
            return "m5";
        }
    }

    /**
     * Absolute Unit used for testing. Based on a PositionUnit.
     */
    static class AbsJunkUnit extends AbsoluteLinearUnit<AbsJunkUnit, JunkUnit>
    {
        /** */
        private static final long serialVersionUID = 1L;

        /** The BASE unit. */
        public static final AbsJunkUnit BASE;

        /**
         * @param positionUnit PositionUnit ...
         * @param name String ...
         * @param abbreviation String ...
         * @param unitSystem UnitSystem ...
         */
        AbsJunkUnit(final PositionUnit positionUnit, final String name, final String abbreviation, final UnitSystem unitSystem)
        {
            super(name, abbreviation, unitSystem, 1.0, 0.0, JunkUnit.SI);
        }

        static
        {
            BASE = new AbsJunkUnit(PositionUnit.METER, "Junk", "JNK", null);
        }

        /** {@inheritDoc} */
        @Override
        public AbsJunkUnit getStandardUnit()
        {
            return BASE;
        }

        /** {@inheritDoc} */
        @Override
        public String getSICoefficientsString()
        {
            return "m5";
        }
    }
}
