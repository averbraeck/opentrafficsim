package org.opentrafficsim.core.units.distributions;

import java.io.Serializable;

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
import org.opentrafficsim.core.distributions.Generator;

import nl.tudelft.simulation.jstats.distributions.DistContinuous;

/**
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 */
public interface ContinuousDistDoubleScalar
{
    /**
     * Absolute value.
     * @param <T> The absolute DoubleScalar type
     * @param <AU> The absolute unit type used
     * @param <RU> The relative unit type belonging to AU
     */
    class Abs<T extends DoubleScalarAbs<AU, T, RU, ?>, AU extends AbsoluteLinearUnit<AU, RU>, RU extends Unit<RU>>
            extends AbstractContinuousDistScalar implements Serializable, Generator<T>
    {
        /** */
        private static final long serialVersionUID = 20150000L;

        /**
         * @param distribution the wrapped distribution function.
         * @param unit the unit.
         */
        public Abs(final DistContinuous distribution, final AU unit)
        {
            super(distribution, unit);
        }

        /**
         * @param constant the constant value.
         * @param unit the unit.
         */
        public Abs(final double constant, final AU unit)
        {
            super(constant, unit);
        }

        /**
         * {@inheritDoc}
         * @throws IllegalStateException when the unit is not of a known type
         */
        @Override
        @SuppressWarnings("unchecked")
        public T draw()
        {
            switch (getDisplayUnit().getClass().getSimpleName())
            {
                case "DirectionUnit":
                    return (T) new Direction(getDistribution().draw(), (DirectionUnit) getDisplayUnit());

                case "PositionUnit":
                    return (T) new Position(getDistribution().draw(), (PositionUnit) getDisplayUnit());

                case "AbsoluteTemperatureUnit":
                    return (T) new AbsoluteTemperature(getDistribution().draw(), (AbsoluteTemperatureUnit) getDisplayUnit());

                case "TimeUnit":
                    return (T) new Time(getDistribution().draw(), (TimeUnit) getDisplayUnit());

                default:
                    throw new IllegalStateException("Unable to draw value for absolute scalar with unit " + getDisplayUnit());
            }
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "ContinuousDistDoubleScalar.Abs [T=" + getDisplayUnit().getClass().getSimpleName() + "]";
        }

    }

    /**
     * Relative value.
     * @param <T> The absolute DoubleScalar type
     * @param <U> The unit type used
     */
    class Rel<T extends DoubleScalarRel<U, T>, U extends Unit<U>> extends AbstractContinuousDistScalar
            implements Serializable, Generator<T>
    {
        /** */
        private static final long serialVersionUID = 20150000L;

        /**
         * @param distribution the wrapped distribution function.
         * @param unit the unit.
         */
        public Rel(final DistContinuous distribution, final U unit)
        {
            super(distribution, unit);
        }

        /**
         * @param constant the constant value.
         * @param unit the unit.
         */
        public Rel(final double constant, final U unit)
        {
            super(constant, unit);
        }

        /**
         * {@inheritDoc}
         * @throws IllegalStateException when the unit is not of a known type
         */
        @Override
        @SuppressWarnings("unchecked")
        public T draw()
        {
            switch (getDisplayUnit().getClass().getSimpleName())
            {
                case "AccelerationUnit":
                    return (T) new Acceleration(getDistribution().draw(), (AccelerationUnit) getDisplayUnit());

                case "AbsorbedDoseUnit":
                    return (T) new AbsorbedDose(getDistribution().draw(), (AbsorbedDoseUnit) getDisplayUnit());

                case "AmountOfSubstanceUnit":
                    return (T) new AmountOfSubstance(getDistribution().draw(), (AmountOfSubstanceUnit) getDisplayUnit());

                case "CatalyticActivityUnit":
                    return (T) new CatalyticActivity(getDistribution().draw(), (CatalyticActivityUnit) getDisplayUnit());

                case "AngleUnit":
                    return (T) new Angle(getDistribution().draw(), (AngleUnit) getDisplayUnit());

                case "SolidAngleUnit":
                    return (T) new SolidAngle(getDistribution().draw(), (SolidAngleUnit) getDisplayUnit());

                case "AreaUnit":
                    return (T) new Area(getDistribution().draw(), (AreaUnit) getDisplayUnit());

                case "DensityUnit":
                    return (T) new Density(getDistribution().draw(), (DensityUnit) getDisplayUnit());

                case "DimensionlessUnit":
                    return (T) new Dimensionless(getDistribution().draw(), (DimensionlessUnit) getDisplayUnit());

                case "DurationUnit":
                    return (T) new Duration(getDistribution().draw(), (DurationUnit) getDisplayUnit());

                case "ElectricalChargeUnit":
                    return (T) new ElectricalCharge(getDistribution().draw(), (ElectricalChargeUnit) getDisplayUnit());

                case "ElectricalCurrentUnit":
                    return (T) new ElectricalCurrent(getDistribution().draw(), (ElectricalCurrentUnit) getDisplayUnit());

                case "ElectricalPotentialUnit":
                    return (T) new ElectricalPotential(getDistribution().draw(), (ElectricalPotentialUnit) getDisplayUnit());

                case "ElectricalResistanceUnit":
                    return (T) new ElectricalResistance(getDistribution().draw(), (ElectricalResistanceUnit) getDisplayUnit());

                case "ElectricalCapacitanceUnit":
                    return (T) new ElectricalCapacitance(getDistribution().draw(),
                            (ElectricalCapacitanceUnit) getDisplayUnit());

                case "ElectricalConductanceUnit":
                    return (T) new ElectricalConductance(getDistribution().draw(),
                            (ElectricalConductanceUnit) getDisplayUnit());

                case "ElectricalInductanceUnit":
                    return (T) new ElectricalInductance(getDistribution().draw(), (ElectricalInductanceUnit) getDisplayUnit());

                case "EnergyUnit":
                    return (T) new Energy(getDistribution().draw(), (EnergyUnit) getDisplayUnit());

                case "EquivalentDoseUnit":
                    return (T) new EquivalentDose(getDistribution().draw(), (EquivalentDoseUnit) getDisplayUnit());

                case "FlowMassUnit":
                    return (T) new FlowMass(getDistribution().draw(), (FlowMassUnit) getDisplayUnit());

                case "FlowVolumeUnit":
                    return (T) new FlowVolume(getDistribution().draw(), (FlowVolumeUnit) getDisplayUnit());

                case "ForceUnit":
                    return (T) new Force(getDistribution().draw(), (ForceUnit) getDisplayUnit());

                case "FrequencyUnit":
                    return (T) new Frequency(getDistribution().draw(), (FrequencyUnit) getDisplayUnit());

                case "IlluminanceUnit":
                    return (T) new Illuminance(getDistribution().draw(), (IlluminanceUnit) getDisplayUnit());

                case "LengthUnit":
                    return (T) new Length(getDistribution().draw(), (LengthUnit) getDisplayUnit());

                case "LinearDensityUnit":
                    return (T) new LinearDensity(getDistribution().draw(), (LinearDensityUnit) getDisplayUnit());

                case "LuminousFluxUnit":
                    return (T) new LuminousFlux(getDistribution().draw(), (LuminousFluxUnit) getDisplayUnit());

                case "LuminousIntensityUnit":
                    return (T) new LuminousIntensity(getDistribution().draw(), (LuminousIntensityUnit) getDisplayUnit());

                case "MagneticFluxUnit":
                    return (T) new MagneticFlux(getDistribution().draw(), (MagneticFluxUnit) getDisplayUnit());

                case "MagneticFluxDensityUnit":
                    return (T) new MagneticFluxDensity(getDistribution().draw(), (MagneticFluxDensityUnit) getDisplayUnit());

                case "MassUnit":
                    return (T) new Mass(getDistribution().draw(), (MassUnit) getDisplayUnit());

                case "PowerUnit":
                    return (T) new Power(getDistribution().draw(), (PowerUnit) getDisplayUnit());

                case "PressureUnit":
                    return (T) new Pressure(getDistribution().draw(), (PressureUnit) getDisplayUnit());

                case "SpeedUnit":
                    return (T) new Speed(getDistribution().draw(), (SpeedUnit) getDisplayUnit());

                case "TemperatureUnit":
                    return (T) new Temperature(getDistribution().draw(), (TemperatureUnit) getDisplayUnit());

                case "TorqueUnit":
                    return (T) new Torque(getDistribution().draw(), (TorqueUnit) getDisplayUnit());

                case "VolumeUnit":
                    return (T) new Volume(getDistribution().draw(), (VolumeUnit) getDisplayUnit());

                default:
                    throw new IllegalStateException("Unable to draw value for relative scalar with unit " + getDisplayUnit());
            }
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "ContinuousDistDoubleScalar.Rel [T=" + getDisplayUnit().getClass().getSimpleName() + "]";
        }
    }

}
