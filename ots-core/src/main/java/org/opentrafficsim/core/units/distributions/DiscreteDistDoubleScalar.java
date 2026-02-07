package org.opentrafficsim.core.units.distributions;

import java.util.function.Supplier;

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
import org.djunits.value.vdouble.scalar.AbsoluteTemperature;
import org.djunits.value.vdouble.scalar.AbsorbedDose;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.AmountOfSubstance;
import org.djunits.value.vdouble.scalar.Angle;
import org.djunits.value.vdouble.scalar.Area;
import org.djunits.value.vdouble.scalar.CatalyticActivity;
import org.djunits.value.vdouble.scalar.Density;
import org.djunits.value.vdouble.scalar.Dimensionless;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.ElectricalCapacitance;
import org.djunits.value.vdouble.scalar.ElectricalCharge;
import org.djunits.value.vdouble.scalar.ElectricalConductance;
import org.djunits.value.vdouble.scalar.ElectricalCurrent;
import org.djunits.value.vdouble.scalar.ElectricalInductance;
import org.djunits.value.vdouble.scalar.ElectricalPotential;
import org.djunits.value.vdouble.scalar.ElectricalResistance;
import org.djunits.value.vdouble.scalar.Energy;
import org.djunits.value.vdouble.scalar.EquivalentDose;
import org.djunits.value.vdouble.scalar.FlowMass;
import org.djunits.value.vdouble.scalar.FlowVolume;
import org.djunits.value.vdouble.scalar.Force;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Illuminance;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.LinearDensity;
import org.djunits.value.vdouble.scalar.LuminousFlux;
import org.djunits.value.vdouble.scalar.LuminousIntensity;
import org.djunits.value.vdouble.scalar.MagneticFlux;
import org.djunits.value.vdouble.scalar.MagneticFluxDensity;
import org.djunits.value.vdouble.scalar.Mass;
import org.djunits.value.vdouble.scalar.Position;
import org.djunits.value.vdouble.scalar.Power;
import org.djunits.value.vdouble.scalar.Pressure;
import org.djunits.value.vdouble.scalar.SolidAngle;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Temperature;
import org.djunits.value.vdouble.scalar.Time;
import org.djunits.value.vdouble.scalar.Torque;
import org.djunits.value.vdouble.scalar.Volume;
import org.djunits.value.vdouble.scalar.base.DoubleScalarAbs;
import org.djunits.value.vdouble.scalar.base.DoubleScalarRel;

import nl.tudelft.simulation.jstats.distributions.DistDiscrete;

/**
 * Interface for discretely distributed double scalars.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 */
public interface DiscreteDistDoubleScalar
{
    /**
     * Absolute value.
     * @param <T> The absolute doublescalar type
     * @param <AU> The absolute unit type used
     * @param <RU> The relative unit type belonging to AU
     */
    class Abs<T extends DoubleScalarAbs<AU, T, RU, ?>, AU extends AbsoluteLinearUnit<AU, RU>, RU extends Unit<RU>>
            extends AbstractDiscreteDistScalar implements Supplier<T>
    {
        /**
         * Constructor.
         * @param distribution the wrapped distribution function.
         * @param unit the unit.
         */
        public Abs(final DistDiscrete distribution, final AU unit)
        {
            super(distribution, unit);
        }

        /**
         * Constructor.
         * @param constant the constant value.
         * @param unit the unit.
         */
        public Abs(final long constant, final AU unit)
        {
            super(constant, unit);
        }

        /**
         * {@inheritDoc}
         * @throws IllegalStateException when the unit is not of a known type
         */
        @Override
        @SuppressWarnings("unchecked")
        public T get()
        {
            switch (getUnit().getClass().getSimpleName())
            {
                case "DirectionUnit":
                    return (T) new Direction(getDistribution().draw(), (DirectionUnit) getUnit());

                case "PositionUnit":
                    return (T) new Position(getDistribution().draw(), (PositionUnit) getUnit());

                case "AbsoluteTemperatureUnit":
                    return (T) new AbsoluteTemperature(getDistribution().draw(), (AbsoluteTemperatureUnit) getUnit());

                case "TimeUnit":
                    return (T) new Time(getDistribution().draw(), (TimeUnit) getUnit());

                default:
                    throw new IllegalStateException("Unable to draw value for absolute scalar with unit " + getUnit());
            }
        }

        @Override
        public String toString()
        {
            return "DiscreteDistDoubleScalar.Abs [T=" + getUnit().getClass().getSimpleName() + "]";
        }

    }

    /**
     * Relative value.
     * @param <T> The absolute doublescalar type
     * @param <U> The unit type used
     */
    class Rel<T extends DoubleScalarRel<U, T>, U extends Unit<U>> extends AbstractDiscreteDistScalar implements Supplier<T>
    {
        /**
         * Constructor.
         * @param distribution the wrapped distribution function.
         * @param unit the unit.
         */
        public Rel(final DistDiscrete distribution, final U unit)
        {
            super(distribution, unit);
        }

        /**
         * Constructor.
         * @param constant the constant value.
         * @param unit the unit.
         */
        public Rel(final long constant, final U unit)
        {
            super(constant, unit);
        }

        /**
         * Draw value.
         * @return a drawn number from the distribution in the given unit.
         */
        @Override
        @SuppressWarnings("unchecked")
        public T get()
        {
            switch (getUnit().getClass().getSimpleName())
            {
                case "AccelerationUnit":
                    return (T) new Acceleration(getDistribution().draw(), (AccelerationUnit) getUnit());

                case "AbsorbedDoseUnit":
                    return (T) new AbsorbedDose(getDistribution().draw(), (AbsorbedDoseUnit) getUnit());

                case "AmountOfSubstanceUnit":
                    return (T) new AmountOfSubstance(getDistribution().draw(), (AmountOfSubstanceUnit) getUnit());

                case "CatalyticActivityUnit":
                    return (T) new CatalyticActivity(getDistribution().draw(), (CatalyticActivityUnit) getUnit());

                case "AngleUnit":
                    return (T) new Angle(getDistribution().draw(), (AngleUnit) getUnit());

                case "SolidAngleUnit":
                    return (T) new SolidAngle(getDistribution().draw(), (SolidAngleUnit) getUnit());

                case "AreaUnit":
                    return (T) new Area(getDistribution().draw(), (AreaUnit) getUnit());

                case "DensityUnit":
                    return (T) new Density(getDistribution().draw(), (DensityUnit) getUnit());

                case "DimensionlessUnit":
                    return (T) new Dimensionless(getDistribution().draw(), (DimensionlessUnit) getUnit());

                case "DurationUnit":
                    return (T) new Duration(getDistribution().draw(), (DurationUnit) getUnit());

                case "ElectricalChargeUnit":
                    return (T) new ElectricalCharge(getDistribution().draw(), (ElectricalChargeUnit) getUnit());

                case "ElectricalCurrentUnit":
                    return (T) new ElectricalCurrent(getDistribution().draw(), (ElectricalCurrentUnit) getUnit());

                case "ElectricalPotentialUnit":
                    return (T) new ElectricalPotential(getDistribution().draw(), (ElectricalPotentialUnit) getUnit());

                case "ElectricalResistanceUnit":
                    return (T) new ElectricalResistance(getDistribution().draw(), (ElectricalResistanceUnit) getUnit());

                case "ElectricalCapacitanceUnit":
                    return (T) new ElectricalCapacitance(getDistribution().draw(), (ElectricalCapacitanceUnit) getUnit());

                case "ElectricalConductanceUnit":
                    return (T) new ElectricalConductance(getDistribution().draw(), (ElectricalConductanceUnit) getUnit());

                case "ElectricalInductanceUnit":
                    return (T) new ElectricalInductance(getDistribution().draw(), (ElectricalInductanceUnit) getUnit());

                case "EnergyUnit":
                    return (T) new Energy(getDistribution().draw(), (EnergyUnit) getUnit());

                case "EquivalentDoseUnit":
                    return (T) new EquivalentDose(getDistribution().draw(), (EquivalentDoseUnit) getUnit());

                case "FlowMassUnit":
                    return (T) new FlowMass(getDistribution().draw(), (FlowMassUnit) getUnit());

                case "FlowVolumeUnit":
                    return (T) new FlowVolume(getDistribution().draw(), (FlowVolumeUnit) getUnit());

                case "ForceUnit":
                    return (T) new Force(getDistribution().draw(), (ForceUnit) getUnit());

                case "FrequencyUnit":
                    return (T) new Frequency(getDistribution().draw(), (FrequencyUnit) getUnit());

                case "IlluminanceUnit":
                    return (T) new Illuminance(getDistribution().draw(), (IlluminanceUnit) getUnit());

                case "LengthUnit":
                    return (T) new Length(getDistribution().draw(), (LengthUnit) getUnit());

                case "LinearDensityUnit":
                    return (T) new LinearDensity(getDistribution().draw(), (LinearDensityUnit) getUnit());

                case "LuminousFluxUnit":
                    return (T) new LuminousFlux(getDistribution().draw(), (LuminousFluxUnit) getUnit());

                case "LuminousIntensityUnit":
                    return (T) new LuminousIntensity(getDistribution().draw(), (LuminousIntensityUnit) getUnit());

                case "MagneticFluxUnit":
                    return (T) new MagneticFlux(getDistribution().draw(), (MagneticFluxUnit) getUnit());

                case "MagneticFluxDensityUnit":
                    return (T) new MagneticFluxDensity(getDistribution().draw(), (MagneticFluxDensityUnit) getUnit());

                case "MassUnit":
                    return (T) new Mass(getDistribution().draw(), (MassUnit) getUnit());

                case "PowerUnit":
                    return (T) new Power(getDistribution().draw(), (PowerUnit) getUnit());

                case "PressureUnit":
                    return (T) new Pressure(getDistribution().draw(), (PressureUnit) getUnit());

                case "SpeedUnit":
                    return (T) new Speed(getDistribution().draw(), (SpeedUnit) getUnit());

                case "TemperatureUnit":
                    return (T) new Temperature(getDistribution().draw(), (TemperatureUnit) getUnit());

                case "TorqueUnit":
                    return (T) new Torque(getDistribution().draw(), (TorqueUnit) getUnit());

                case "VolumeUnit":
                    return (T) new Volume(getDistribution().draw(), (VolumeUnit) getUnit());

                default:
                    throw new IllegalStateException("Unable to draw value for relative scalar with unit " + getUnit());
            }
        }

        @Override
        public String toString()
        {
            return "DiscreteDistDoubleScalar.Rel [T=" + getUnit().getClass().getSimpleName() + "]";
        }

    }

}
