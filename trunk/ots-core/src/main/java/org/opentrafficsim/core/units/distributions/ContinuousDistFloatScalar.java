package org.opentrafficsim.core.units.distributions;

import java.io.Serializable;

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
import org.djunits.value.Absolute;
import org.djunits.value.Relative;
import org.djunits.value.vfloat.scalar.AbstractFloatScalarAbs;
import org.djunits.value.vfloat.scalar.AbstractFloatScalarRel;
import org.djunits.value.vfloat.scalar.FloatAbsoluteTemperature;
import org.djunits.value.vfloat.scalar.FloatAcceleration;
import org.djunits.value.vfloat.scalar.FloatAngle;
import org.djunits.value.vfloat.scalar.FloatAngleSolid;
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
import org.djunits.value.vfloat.scalar.FloatScalar;
import org.djunits.value.vfloat.scalar.FloatSpeed;
import org.djunits.value.vfloat.scalar.FloatTemperature;
import org.djunits.value.vfloat.scalar.FloatTime;
import org.djunits.value.vfloat.scalar.FloatTorque;
import org.djunits.value.vfloat.scalar.FloatVolume;

import nl.tudelft.simulation.jstats.distributions.DistContinuous;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-26 01:01:13 +0200 (Sun, 26 Jul 2015) $, @version $Revision: 1155 $, by $Author: averbraeck $,
 * initial version Feb 2, 2015 <br>
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
        public Abs(final float constant, final AU unit)
        {
            super(constant, unit);
        }

        /**
         * @return a drawn number from the distribution in the given unit.
         */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public final T draw()
        {
            switch (getUnit().getClass().getSimpleName())
            {
                case "DirectionUnit":
                    return (T) new FloatDirection((float) getDistribution().draw(), (DirectionUnit) getUnit());

                case "PositionUnit":
                    return (T) new FloatPosition((float) getDistribution().draw(), (PositionUnit) getUnit());

                case "AbsoluteTemperatureUnit":
                    return (T) new FloatAbsoluteTemperature((float) getDistribution().draw(),
                            (AbsoluteTemperatureUnit) getUnit());

                case "TimeUnit":
                    return (T) new FloatTime((float) getDistribution().draw(), (TimeUnit) getUnit());

                default:
                    return (T) new FloatScalar.Abs((float) getDistribution().draw(), (AU) getUnit());
            }
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "ContinuousDistFloatScalar.Abs [T=" + getUnit().getClass().getSimpleName() + "]";
        }

    }

    /**
     * Relative value.
     * @param <T> The absolute FloatScalar type
     * @param <U> The unit type used
     */
    class Rel<T extends AbstractFloatScalarRel<U, T>, U extends Unit<U>> extends AbstractContinuousDistScalar
            implements Relative, Serializable
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
        public Rel(final float constant, final U unit)
        {
            super(constant, unit);
        }

        /**
         * @return a drawn number from the distribution in the given unit.
         */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public final T draw()
        {
            switch (getUnit().getClass().getSimpleName())
            {
                case "AccelerationUnit":
                    return (T) new FloatAcceleration((float) getDistribution().draw(), (AccelerationUnit) getUnit());

                case "AngleUnit":
                    return (T) new FloatAngle((float) getDistribution().draw(), (AngleUnit) getUnit());

                case "AngleSolidUnit":
                    return (T) new FloatAngleSolid((float) getDistribution().draw(), (AngleSolidUnit) getUnit());

                case "AreaUnit":
                    return (T) new FloatArea((float) getDistribution().draw(), (AreaUnit) getUnit());

                case "DensityUnit":
                    return (T) new FloatDensity((float) getDistribution().draw(), (DensityUnit) getUnit());

                case "DimensionlessUnit":
                    return (T) new FloatDimensionless((float) getDistribution().draw(), (DimensionlessUnit) getUnit());

                case "DurationUnit":
                    return (T) new FloatDuration((float) getDistribution().draw(), (DurationUnit) getUnit());

                case "ElectricalChargeUnit":
                    return (T) new FloatElectricalCharge((float) getDistribution().draw(), (ElectricalChargeUnit) getUnit());

                case "ElectricalCurrentUnit":
                    return (T) new FloatElectricalCurrent((float) getDistribution().draw(), (ElectricalCurrentUnit) getUnit());

                case "ElectricalPotentialUnit":
                    return (T) new FloatElectricalPotential((float) getDistribution().draw(),
                            (ElectricalPotentialUnit) getUnit());

                case "ElectricalResistanceUnit":
                    return (T) new FloatElectricalResistance((float) getDistribution().draw(),
                            (ElectricalResistanceUnit) getUnit());

                case "EnergyUnit":
                    return (T) new FloatEnergy((float) getDistribution().draw(), (EnergyUnit) getUnit());

                case "FlowMassUnit":
                    return (T) new FloatFlowMass((float) getDistribution().draw(), (FlowMassUnit) getUnit());

                case "FlowVolumeUnit":
                    return (T) new FloatFlowVolume((float) getDistribution().draw(), (FlowVolumeUnit) getUnit());

                case "ForceUnit":
                    return (T) new FloatForce((float) getDistribution().draw(), (ForceUnit) getUnit());

                case "FrequencyUnit":
                    return (T) new FloatFrequency((float) getDistribution().draw(), (FrequencyUnit) getUnit());

                case "LengthUnit":
                    return (T) new FloatLength((float) getDistribution().draw(), (LengthUnit) getUnit());

                case "LinearDensityUnit":
                    return (T) new FloatLinearDensity((float) getDistribution().draw(), (LinearDensityUnit) getUnit());

                case "MassUnit":
                    return (T) new FloatMass((float) getDistribution().draw(), (MassUnit) getUnit());

                case "PowerUnit":
                    return (T) new FloatPower((float) getDistribution().draw(), (PowerUnit) getUnit());

                case "PressureUnit":
                    return (T) new FloatPressure((float) getDistribution().draw(), (PressureUnit) getUnit());

                case "SpeedUnit":
                    return (T) new FloatSpeed((float) getDistribution().draw(), (SpeedUnit) getUnit());

                case "TemperatureUnit":
                    return (T) new FloatTemperature((float) getDistribution().draw(), (TemperatureUnit) getUnit());

                case "TorqueUnit":
                    return (T) new FloatTorque((float) getDistribution().draw(), (TorqueUnit) getUnit());

                case "VolumeUnit":
                    return (T) new FloatVolume((float) getDistribution().draw(), (VolumeUnit) getUnit());

                default:
                    return (T) new FloatScalar.Rel((float) getDistribution().draw(), getUnit());
            }
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "ContinuousDistFloatScalar.Rel [T=" + getUnit().getClass().getSimpleName() + "]";
        }

    }

}
