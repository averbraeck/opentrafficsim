package org.opentrafficsim.core.units.distributions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.djunits.quantity.Quantity;
import org.djunits.unit.AbsoluteLinearUnit;
import org.djunits.unit.AbsoluteTemperatureUnit;
import org.djunits.unit.AbsorbedDoseUnit;
import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.AmountOfSubstanceUnit;
import org.djunits.unit.AngleUnit;
import org.djunits.unit.AreaUnit;
import org.djunits.unit.CatalyticActivityUnit;
import org.djunits.unit.DensityUnit;
import org.djunits.unit.DimensionlessUnit;
import org.djunits.unit.DirectionUnit;
import org.djunits.unit.DurationUnit;
import org.djunits.unit.ElectricalCapacitanceUnit;
import org.djunits.unit.ElectricalChargeUnit;
import org.djunits.unit.ElectricalConductanceUnit;
import org.djunits.unit.ElectricalCurrentUnit;
import org.djunits.unit.ElectricalInductanceUnit;
import org.djunits.unit.ElectricalPotentialUnit;
import org.djunits.unit.ElectricalResistanceUnit;
import org.djunits.unit.EnergyUnit;
import org.djunits.unit.EquivalentDoseUnit;
import org.djunits.unit.FlowMassUnit;
import org.djunits.unit.FlowVolumeUnit;
import org.djunits.unit.ForceUnit;
import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.IlluminanceUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.LinearDensityUnit;
import org.djunits.unit.LuminousFluxUnit;
import org.djunits.unit.LuminousIntensityUnit;
import org.djunits.unit.MagneticFluxDensityUnit;
import org.djunits.unit.MagneticFluxUnit;
import org.djunits.unit.MassUnit;
import org.djunits.unit.PositionUnit;
import org.djunits.unit.PowerUnit;
import org.djunits.unit.PressureUnit;
import org.djunits.unit.SolidAngleUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TemperatureUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.unit.TorqueUnit;
import org.djunits.unit.Unit;
import org.djunits.unit.VolumeUnit;
import org.djunits.unit.scale.IdentityScale;
import org.djunits.unit.scale.OffsetLinearScale;
import org.djunits.unit.si.SIPrefixes;
import org.djunits.unit.unitsystem.UnitSystem;
import org.djunits.value.vdouble.scalar.base.DoubleScalarAbs;
import org.djunits.value.vdouble.scalar.base.DoubleScalarRel;
import org.djunits.value.vfloat.scalar.base.FloatScalarAbs;
import org.djunits.value.vfloat.scalar.base.FloatScalarRel;
import org.junit.jupiter.api.Test;

import nl.tudelft.simulation.jstats.distributions.DistContinuous;
import nl.tudelft.simulation.jstats.distributions.DistDiscrete;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Test the various distribution classes.
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
        AbsoluteLinearUnit<?, ?>[] absoluteUnits = {DirectionUnit.DEFAULT, PositionUnit.DEFAULT, AbsoluteTemperatureUnit.KELVIN,
                TimeUnit.DEFAULT/* , AbsJunkUnit.DEFAULT */};
        Unit<?>[] relativeUnits = {AccelerationUnit.SI, AngleUnit.SI, SolidAngleUnit.SI, AreaUnit.SI, DensityUnit.SI,
                DimensionlessUnit.SI, ElectricalChargeUnit.SI, ElectricalCurrentUnit.SI, ElectricalPotentialUnit.SI,
                ElectricalResistanceUnit.SI, EnergyUnit.SI, FlowMassUnit.SI, FlowVolumeUnit.SI, ForceUnit.SI, FrequencyUnit.SI,
                LengthUnit.SI, LinearDensityUnit.SI, MassUnit.SI, PowerUnit.SI, PressureUnit.SI, SpeedUnit.SI,
                TemperatureUnit.SI, DurationUnit.SI, TorqueUnit.SI, VolumeUnit.SI, AbsorbedDoseUnit.SI,
                AmountOfSubstanceUnit.SI, CatalyticActivityUnit.SI, ElectricalCapacitanceUnit.SI, ElectricalConductanceUnit.SI,
                ElectricalInductanceUnit.SI, EquivalentDoseUnit.SI, IlluminanceUnit.SI, LuminousFluxUnit.SI,
                LuminousIntensityUnit.SI, MagneticFluxDensityUnit.SI, MagneticFluxUnit.SI /* , JunkUnit.SI */};

        StreamInterface stream = new MersenneTwister(1L);
        DistContinuous distCont = new DistContinuous(stream)
        {

            /** */
            private static final long serialVersionUID = 1L;

            @Override
            public double draw()
            {
                return getDoubleNextResult();
            }

            @Override
            public double getProbabilityDensity(final double observation)
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

        DistDiscrete distDisc = new DistDiscrete(stream)
        {

            /** */
            private static final long serialVersionUID = 1L;

            @Override
            public long draw()
            {
                return getLongNextResult();
            }

            @Override
            public double probability(final long observation)
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
        DoubleScalarAbs result = dist.draw();
        assertEquals(unit, result.getDisplayUnit(), "Unit matches");
        assertEquals(this.nextDoubleResult, result.si, 0.0001, "Value matches");
        this.nextDoubleResult = 23.456;
        result = dist.draw();
        assertEquals(this.nextDoubleResult, result.si, 0.0001, "Value matches");
        dist = new ContinuousDistDoubleScalar.Abs(1.234, unit);
        result = dist.draw();
        assertEquals(unit, result.getDisplayUnit(), "Unit matches");
        assertEquals(1.234, result.si, 0.0001, "Value matches");
        assertTrue(dist.toString().contains("ContinuousDistDoubleScalar.Abs"),
                "toString result contains ContinuousDistDoubleScalar.Abs");
        assertTrue(dist.toString().contains(unit.getClass().getSimpleName()),
                "toString method contains unit name " + unit.getClass().getSimpleName());
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
        DoubleScalarRel result = dist.draw();
        assertEquals(unit, result.getDisplayUnit(), "Unit matches");
        assertEquals(this.nextDoubleResult, result.si, 0.0001, "Value matches");
        this.nextDoubleResult = 23.456;
        result = dist.draw();
        assertEquals(this.nextDoubleResult, result.si, 0.0001, "Value matches");
        dist = new ContinuousDistDoubleScalar.Rel(1.234, unit);
        result = dist.draw();
        assertEquals(unit, result.getDisplayUnit(), "Unit matches");
        assertEquals(1.234, result.si, 0.0001, "Value matches");
        assertTrue(dist.toString().contains("ContinuousDistDoubleScalar.Rel"),
                "toString result contains ContinuousDistDoubleScalar.Rel");
        assertTrue(dist.toString().contains(unit.getClass().getSimpleName()),
                "toString method contains unit name " + unit.getClass().getSimpleName());
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
        DoubleScalarAbs result = dist.draw();
        assertEquals(unit, result.getDisplayUnit(), "Unit matches");
        assertEquals(this.nextLongResult, result.si, 0.0001, "Value matches");
        this.nextDoubleResult = 23.456;
        result = dist.draw();
        assertEquals(this.getLongNextResult(), result.si, 0.0001, "Value matches");
        dist = new DiscreteDistDoubleScalar.Abs(1234, unit);
        result = dist.draw();
        assertEquals(unit, result.getDisplayUnit(), "Unit matches");
        assertEquals(1234, result.si, 0.0001, "Value matches");
        assertTrue(dist.toString().contains("DiscreteDistDoubleScalar.Abs"),
                "toString result contains DiscreteDistDoubleScalar.Abs");
        assertTrue(dist.toString().contains(unit.getClass().getSimpleName()),
                "toString method contains unit name " + unit.getClass().getSimpleName());
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
        DoubleScalarRel result = dist.draw();
        assertEquals(unit, result.getDisplayUnit(), "Unit matches");
        assertEquals(this.nextLongResult, result.si, 0.0001, "Value matches");
        this.nextDoubleResult = 23.456;
        result = dist.draw();
        assertEquals(this.nextLongResult, result.si, 0.0001, "Value matches");
        dist = new DiscreteDistDoubleScalar.Rel(1234, unit);
        result = dist.draw();
        assertEquals(unit, result.getDisplayUnit(), "Unit matches");
        assertEquals(1234, result.si, 0.0001, "Value matches");
        assertTrue(dist.toString().contains("DiscreteDistDoubleScalar.Rel"),
                "toString result contains DiscreteDistDoubleScalar.Rel");
        assertTrue(dist.toString().contains(unit.getClass().getSimpleName()),
                "toString method contains unit name " + unit.getClass().getSimpleName());
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
        FloatScalarAbs result = dist.draw();
        assertEquals(unit, result.getDisplayUnit(), "Unit matches");
        assertEquals(this.nextDoubleResult, result.si, 0.0001, "Value matches");
        this.nextDoubleResult = 23.456;
        result = dist.draw();
        assertEquals(this.nextDoubleResult, result.si, 0.0001, "Value matches");
        dist = new ContinuousDistFloatScalar.Abs(1.234f, unit);
        result = dist.draw();
        assertEquals(unit, result.getDisplayUnit(), "Unit matches");
        assertEquals(1.234, result.si, 0.0001, "Value matches");
        assertTrue(dist.toString().contains("ContinuousDistFloatScalar.Abs"),
                "toString result contains ContinuousDistFloatScalar.Abs");
        assertTrue(dist.toString().contains(unit.getClass().getSimpleName()),
                "toString method contains unit name " + unit.getClass().getSimpleName());
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
        FloatScalarRel result = dist.draw();
        assertEquals(unit, result.getDisplayUnit(), "Unit matches");
        assertEquals(this.nextDoubleResult, result.si, 0.0001, "Value matches");
        this.nextDoubleResult = 23.456;
        result = dist.draw();
        assertEquals(this.nextDoubleResult, result.si, 0.0001, "Value matches");
        dist = new ContinuousDistFloatScalar.Rel(1.234f, unit);
        result = dist.draw();
        assertEquals(unit, result.getDisplayUnit(), "Unit matches");
        assertEquals(1.234, result.si, 0.0001, "Value matches");
        assertTrue(dist.toString().contains("ContinuousDistFloatScalar.Rel"),
                "toString result contains ContinuousDistFloatScalar.Rel");
        assertTrue(dist.toString().contains(unit.getClass().getSimpleName()),
                "toString method contains unit name " + unit.getClass().getSimpleName());
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
        FloatScalarAbs result = dist.draw();
        assertEquals(unit, result.getDisplayUnit(), "Unit matches");
        assertEquals(this.nextLongResult, result.si, 0.0001, "Value matches");
        this.nextDoubleResult = 23.456;
        result = dist.draw();
        assertEquals(this.getLongNextResult(), result.si, 0.0001, "Value matches");
        dist = new DiscreteDistFloatScalar.Abs(1234, unit);
        result = dist.draw();
        assertEquals(unit, result.getDisplayUnit(), "Unit matches");
        assertEquals(1234, result.si, 0.0001, "Value matches");
        assertTrue(dist.toString().contains("DiscreteDistFloatScalar.Abs"),
                "toString result contains DiscreteDistFloatScalar.Abs");
        assertTrue(dist.toString().contains(unit.getClass().getSimpleName()),
                "toString method contains unit name " + unit.getClass().getSimpleName());
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
        FloatScalarRel result = dist.draw();
        assertEquals(unit, result.getDisplayUnit(), "Unit matches");
        assertEquals(this.nextLongResult, result.si, 0.0001, "Value matches");
        this.nextDoubleResult = 23.456;
        result = dist.draw();
        assertEquals(this.nextLongResult, result.si, 0.0001, "Value matches");
        dist = new DiscreteDistFloatScalar.Rel(1234, unit);
        result = dist.draw();
        assertEquals(unit, result.getDisplayUnit(), "Unit matches");
        assertEquals(1234, result.si, 0.0001, "Value matches");
        assertTrue(dist.toString().contains("DiscreteDistFloatScalar.Rel"),
                "toString result contains DiscreteDistFloatScalar.Rel");
        assertTrue(dist.toString().contains(unit.getClass().getSimpleName()),
                "toString method contains unit name " + unit.getClass().getSimpleName());
    }

    /**
     * Relative Unit used for testing. Based on a LengthUnit.
     */
    static class JunkUnit extends Unit<JunkUnit>
    {
        /** */
        private static final long serialVersionUID = 1L;

        /** The base, with "m5" as the SI signature. */
        public static final Quantity<JunkUnit> BASE = new Quantity<>("Junk", "m5");

        /** The SI unit for junk is m5. */
        public static final JunkUnit SI = new JunkUnit().build(new Unit.Builder<JunkUnit>().setQuantity(BASE).setId("m5")
                .setName("meter^5").setUnitSystem(UnitSystem.SI_DERIVED).setSiPrefixes(SIPrefixes.NONE, 1.0)
                .setScale(IdentityScale.SCALE));

        /** m/s3. */
        public static final JunkUnit JUNK = SI;
    }

    /**
     * Absolute Unit used for testing. Based on a PositionUnit.
     */
    static class AbsJunkUnit extends AbsoluteLinearUnit<AbsJunkUnit, JunkUnit>
    {
        /** */
        private static final long serialVersionUID = 1L;

        /** The base, with "m" as the SI signature. */
        public static final Quantity<AbsJunkUnit> BASE = new Quantity<>("AbsJunk", "m5");

        /** The SI unit for position is meter. */
        public static final AbsJunkUnit DEFAULT =
                new AbsJunkUnit().build(new AbsoluteLinearUnit.Builder<AbsJunkUnit, JunkUnit>().setQuantity(BASE).setId("m5")
                        .setName("meter^5").setUnitSystem(UnitSystem.SI_BASE).setSiPrefixes(SIPrefixes.NONE, 1.0)
                        .setScale(new OffsetLinearScale(1.0, 0.0)).setRelativeUnit(JunkUnit.JUNK));

        /** meter. */
        public static final AbsJunkUnit ABSJUNK = DEFAULT;
    }
}
