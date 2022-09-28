package org.opentrafficsim.core.units.distributions;

import java.io.Serializable;

import org.djunits.unit.AbsoluteLinearUnit;
import org.djunits.unit.AbsoluteTemperatureUnit;
import org.djunits.unit.AccelerationUnit;
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
import org.djunits.value.Absolute;
import org.djunits.value.vdouble.scalar.AbsoluteTemperature;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Angle;
import org.djunits.value.vdouble.scalar.Area;
import org.djunits.value.vdouble.scalar.Density;
import org.djunits.value.vdouble.scalar.Dimensionless;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.ElectricalCharge;
import org.djunits.value.vdouble.scalar.ElectricalCurrent;
import org.djunits.value.vdouble.scalar.ElectricalPotential;
import org.djunits.value.vdouble.scalar.ElectricalResistance;
import org.djunits.value.vdouble.scalar.Energy;
import org.djunits.value.vdouble.scalar.FlowMass;
import org.djunits.value.vdouble.scalar.FlowVolume;
import org.djunits.value.vdouble.scalar.Force;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.LinearDensity;
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
import org.djunits.value.vdouble.scalar.base.AbstractDoubleScalarAbs;
import org.djunits.value.vdouble.scalar.base.AbstractDoubleScalarRel;
import org.djunits.value.vdouble.scalar.base.DoubleScalar;
import org.opentrafficsim.core.distributions.Generator;

import nl.tudelft.simulation.jstats.distributions.DistContinuous;

/**
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
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
    class Abs<T extends AbstractDoubleScalarAbs<AU, T, RU, ?>, AU extends AbsoluteLinearUnit<AU, RU>, RU extends Unit<RU>>
            extends AbstractContinuousDistScalar implements Absolute, Serializable, Generator<T>
    {
        /** */
        private static final long serialVersionUID = 20150000L;

        /**
         * @param distribution DistContinuous; the wrapped distribution function.
         * @param unit AU; the unit.
         */
        public Abs(final DistContinuous distribution, final AU unit)
        {
            super(distribution, unit);
        }

        /**
         * @param constant double; the constant value.
         * @param unit AU; the unit.
         */
        public Abs(final double constant, final AU unit)
        {
            super(constant, unit);
        }

        /**
         * @return a drawn number from the distribution in the given unit.
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
                    return (T) DoubleScalar.instantiate(getDistribution().draw(), (AU) getDisplayUnit());
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
    class Rel<T extends AbstractDoubleScalarRel<U, T>, U extends Unit<U>> extends AbstractContinuousDistScalar
            implements Serializable, Generator<T>
    {
        /** */
        private static final long serialVersionUID = 20150000L;

        /**
         * @param distribution DistContinuous; the wrapped distribution function.
         * @param unit U; the unit.
         */
        public Rel(final DistContinuous distribution, final U unit)
        {
            super(distribution, unit);
        }

        /**
         * @param constant double; the constant value.
         * @param unit U; the unit.
         */
        public Rel(final double constant, final U unit)
        {
            super(constant, unit);
        }

        /**
         * @return a drawn number from the distribution in the given unit.
         */
        @Override
        @SuppressWarnings("unchecked")
        public T draw()
        {
            switch (getDisplayUnit().getClass().getSimpleName())
            {
                case "AccelerationUnit":
                    return (T) new Acceleration(getDistribution().draw(), (AccelerationUnit) getDisplayUnit());

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

                case "EnergyUnit":
                    return (T) new Energy(getDistribution().draw(), (EnergyUnit) getDisplayUnit());

                case "FlowMassUnit":
                    return (T) new FlowMass(getDistribution().draw(), (FlowMassUnit) getDisplayUnit());

                case "FlowVolumeUnit":
                    return (T) new FlowVolume(getDistribution().draw(), (FlowVolumeUnit) getDisplayUnit());

                case "ForceUnit":
                    return (T) new Force(getDistribution().draw(), (ForceUnit) getDisplayUnit());

                case "FrequencyUnit":
                    return (T) new Frequency(getDistribution().draw(), (FrequencyUnit) getDisplayUnit());

                case "LengthUnit":
                    return (T) new Length(getDistribution().draw(), (LengthUnit) getDisplayUnit());

                case "LinearDensityUnit":
                    return (T) new LinearDensity(getDistribution().draw(), (LinearDensityUnit) getDisplayUnit());

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
                    return (T) DoubleScalar.instantiate(getDistribution().draw(), (U) getDisplayUnit());
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
