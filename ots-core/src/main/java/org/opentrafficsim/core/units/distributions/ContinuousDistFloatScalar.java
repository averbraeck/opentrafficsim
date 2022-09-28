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
import org.djunits.value.vfloat.scalar.FloatAbsoluteTemperature;
import org.djunits.value.vfloat.scalar.FloatAcceleration;
import org.djunits.value.vfloat.scalar.FloatAngle;
import org.djunits.value.vfloat.scalar.FloatArea;
import org.djunits.value.vfloat.scalar.FloatDensity;
import org.djunits.value.vfloat.scalar.FloatDimensionless;
import org.djunits.value.vfloat.scalar.FloatDirection;
import org.djunits.value.vfloat.scalar.FloatDuration;
import org.djunits.value.vfloat.scalar.FloatElectricalCharge;
import org.djunits.value.vfloat.scalar.FloatElectricalCurrent;
import org.djunits.value.vfloat.scalar.FloatElectricalPotential;
import org.djunits.value.vfloat.scalar.FloatElectricalResistance;
import org.djunits.value.vfloat.scalar.FloatEnergy;
import org.djunits.value.vfloat.scalar.FloatFlowMass;
import org.djunits.value.vfloat.scalar.FloatFlowVolume;
import org.djunits.value.vfloat.scalar.FloatForce;
import org.djunits.value.vfloat.scalar.FloatFrequency;
import org.djunits.value.vfloat.scalar.FloatLength;
import org.djunits.value.vfloat.scalar.FloatLinearDensity;
import org.djunits.value.vfloat.scalar.FloatMass;
import org.djunits.value.vfloat.scalar.FloatPosition;
import org.djunits.value.vfloat.scalar.FloatPower;
import org.djunits.value.vfloat.scalar.FloatPressure;
import org.djunits.value.vfloat.scalar.FloatSolidAngle;
import org.djunits.value.vfloat.scalar.FloatSpeed;
import org.djunits.value.vfloat.scalar.FloatTemperature;
import org.djunits.value.vfloat.scalar.FloatTime;
import org.djunits.value.vfloat.scalar.FloatTorque;
import org.djunits.value.vfloat.scalar.FloatVolume;
import org.djunits.value.vfloat.scalar.base.AbstractFloatScalarAbs;
import org.djunits.value.vfloat.scalar.base.AbstractFloatScalarRel;
import org.djunits.value.vfloat.scalar.base.FloatScalar;

import nl.tudelft.simulation.jstats.distributions.DistContinuous;

/**
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public interface ContinuousDistFloatScalar
{
    /**
     * Absolute value.
     * @param <T> The absolute FloatScalar type
     * @param <AU> The absolute unit type used
     * @param <RU> The relative unit type belonging to AU
     */
    class Abs<T extends AbstractFloatScalarAbs<AU, T, RU, ?>, AU extends AbsoluteLinearUnit<AU, RU>, RU extends Unit<RU>>
            extends AbstractContinuousDistScalar implements Absolute, Serializable
    {
        /** */
        private static final long serialVersionUID = 20150000;

        /**
         * @param distribution DistContinuous; the wrapped distribution function.
         * @param unit AU; the unit.
         */
        public Abs(final DistContinuous distribution, final AU unit)
        {
            super(distribution, unit);
        }

        /**
         * @param constant float; the constant value.
         * @param unit AU; the unit.
         */
        public Abs(final float constant, final AU unit)
        {
            super(constant, unit);
        }

        /**
         * @return a drawn number from the distribution in the given unit.
         */
        @SuppressWarnings("unchecked")
        public final T draw()
        {
            switch (getDisplayUnit().getClass().getSimpleName())
            {
                case "DirectionUnit":
                    return (T) new FloatDirection((float) getDistribution().draw(), (DirectionUnit) getDisplayUnit());

                case "PositionUnit":
                    return (T) new FloatPosition((float) getDistribution().draw(), (PositionUnit) getDisplayUnit());

                case "AbsoluteTemperatureUnit":
                    return (T) new FloatAbsoluteTemperature((float) getDistribution().draw(),
                            (AbsoluteTemperatureUnit) getDisplayUnit());

                case "TimeUnit":
                    return (T) new FloatTime((float) getDistribution().draw(), (TimeUnit) getDisplayUnit());

                default:
                    return (T) FloatScalar.instantiate((float) getDistribution().draw(), (AU) getDisplayUnit());
            }
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "ContinuousDistFloatScalar.Abs [T=" + getDisplayUnit().getClass().getSimpleName() + "]";
        }

    }

    /**
     * Relative value.
     * @param <T> The absolute FloatScalar type
     * @param <U> The unit type used
     */
    class Rel<T extends AbstractFloatScalarRel<U, T>, U extends Unit<U>> extends AbstractContinuousDistScalar
            implements Serializable
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
         * @param constant float; the constant value.
         * @param unit U; the unit.
         */
        public Rel(final float constant, final U unit)
        {
            super(constant, unit);
        }

        /**
         * @return a drawn number from the distribution in the given unit.
         */
        @SuppressWarnings("unchecked")
        public final T draw()
        {
            switch (getDisplayUnit().getClass().getSimpleName())
            {
                case "AccelerationUnit":
                    return (T) new FloatAcceleration((float) getDistribution().draw(), (AccelerationUnit) getDisplayUnit());

                case "AngleUnit":
                    return (T) new FloatAngle((float) getDistribution().draw(), (AngleUnit) getDisplayUnit());

                case "SolidAngleUnit":
                    return (T) new FloatSolidAngle((float) getDistribution().draw(), (SolidAngleUnit) getDisplayUnit());

                case "AreaUnit":
                    return (T) new FloatArea((float) getDistribution().draw(), (AreaUnit) getDisplayUnit());

                case "DensityUnit":
                    return (T) new FloatDensity((float) getDistribution().draw(), (DensityUnit) getDisplayUnit());

                case "DimensionlessUnit":
                    return (T) new FloatDimensionless((float) getDistribution().draw(), (DimensionlessUnit) getDisplayUnit());

                case "DurationUnit":
                    return (T) new FloatDuration((float) getDistribution().draw(), (DurationUnit) getDisplayUnit());

                case "ElectricalChargeUnit":
                    return (T) new FloatElectricalCharge((float) getDistribution().draw(),
                            (ElectricalChargeUnit) getDisplayUnit());

                case "ElectricalCurrentUnit":
                    return (T) new FloatElectricalCurrent((float) getDistribution().draw(),
                            (ElectricalCurrentUnit) getDisplayUnit());

                case "ElectricalPotentialUnit":
                    return (T) new FloatElectricalPotential((float) getDistribution().draw(),
                            (ElectricalPotentialUnit) getDisplayUnit());

                case "ElectricalResistanceUnit":
                    return (T) new FloatElectricalResistance((float) getDistribution().draw(),
                            (ElectricalResistanceUnit) getDisplayUnit());

                case "EnergyUnit":
                    return (T) new FloatEnergy((float) getDistribution().draw(), (EnergyUnit) getDisplayUnit());

                case "FlowMassUnit":
                    return (T) new FloatFlowMass((float) getDistribution().draw(), (FlowMassUnit) getDisplayUnit());

                case "FlowVolumeUnit":
                    return (T) new FloatFlowVolume((float) getDistribution().draw(), (FlowVolumeUnit) getDisplayUnit());

                case "ForceUnit":
                    return (T) new FloatForce((float) getDistribution().draw(), (ForceUnit) getDisplayUnit());

                case "FrequencyUnit":
                    return (T) new FloatFrequency((float) getDistribution().draw(), (FrequencyUnit) getDisplayUnit());

                case "LengthUnit":
                    return (T) new FloatLength((float) getDistribution().draw(), (LengthUnit) getDisplayUnit());

                case "LinearDensityUnit":
                    return (T) new FloatLinearDensity((float) getDistribution().draw(), (LinearDensityUnit) getDisplayUnit());

                case "MassUnit":
                    return (T) new FloatMass((float) getDistribution().draw(), (MassUnit) getDisplayUnit());

                case "PowerUnit":
                    return (T) new FloatPower((float) getDistribution().draw(), (PowerUnit) getDisplayUnit());

                case "PressureUnit":
                    return (T) new FloatPressure((float) getDistribution().draw(), (PressureUnit) getDisplayUnit());

                case "SpeedUnit":
                    return (T) new FloatSpeed((float) getDistribution().draw(), (SpeedUnit) getDisplayUnit());

                case "TemperatureUnit":
                    return (T) new FloatTemperature((float) getDistribution().draw(), (TemperatureUnit) getDisplayUnit());

                case "TorqueUnit":
                    return (T) new FloatTorque((float) getDistribution().draw(), (TorqueUnit) getDisplayUnit());

                case "VolumeUnit":
                    return (T) new FloatVolume((float) getDistribution().draw(), (VolumeUnit) getDisplayUnit());

                default:
                    return (T) FloatScalar.instantiate((float) getDistribution().draw(), (U) getDisplayUnit());
            }
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "ContinuousDistFloatScalar.Rel [T=" + getDisplayUnit().getClass().getSimpleName() + "]";
        }

    }

}
