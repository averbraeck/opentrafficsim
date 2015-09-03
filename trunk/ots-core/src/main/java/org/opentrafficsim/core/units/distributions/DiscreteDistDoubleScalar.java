package org.opentrafficsim.core.units.distributions;

import nl.tudelft.simulation.jstats.distributions.DistDiscrete;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.AnglePlaneUnit;
import org.djunits.unit.AngleSlopeUnit;
import org.djunits.unit.AngleSolidUnit;
import org.djunits.unit.AreaUnit;
import org.djunits.unit.DensityUnit;
import org.djunits.unit.DimensionlessUnit;
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
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.djunits.value.vdouble.scalar.DOUBLE_SCALAR.Acceleration;
import org.djunits.value.vdouble.scalar.DOUBLE_SCALAR.AnglePlane;
import org.djunits.value.vdouble.scalar.DOUBLE_SCALAR.AngleSlope;
import org.djunits.value.vdouble.scalar.DOUBLE_SCALAR.AngleSolid;
import org.djunits.value.vdouble.scalar.DOUBLE_SCALAR.Area;
import org.djunits.value.vdouble.scalar.DOUBLE_SCALAR.Density;
import org.djunits.value.vdouble.scalar.DOUBLE_SCALAR.Dimensionless;
import org.djunits.value.vdouble.scalar.DOUBLE_SCALAR.ElectricalCharge;
import org.djunits.value.vdouble.scalar.DOUBLE_SCALAR.ElectricalCurrent;
import org.djunits.value.vdouble.scalar.DOUBLE_SCALAR.ElectricalPotential;
import org.djunits.value.vdouble.scalar.DOUBLE_SCALAR.ElectricalResistance;
import org.djunits.value.vdouble.scalar.DOUBLE_SCALAR.Energy;
import org.djunits.value.vdouble.scalar.DOUBLE_SCALAR.FlowMass;
import org.djunits.value.vdouble.scalar.DOUBLE_SCALAR.FlowVolume;
import org.djunits.value.vdouble.scalar.DOUBLE_SCALAR.Force;
import org.djunits.value.vdouble.scalar.DOUBLE_SCALAR.Frequency;
import org.djunits.value.vdouble.scalar.DOUBLE_SCALAR.Length;
import org.djunits.value.vdouble.scalar.DOUBLE_SCALAR.LinearDensity;
import org.djunits.value.vdouble.scalar.DOUBLE_SCALAR.Mass;
import org.djunits.value.vdouble.scalar.DOUBLE_SCALAR.Power;
import org.djunits.value.vdouble.scalar.DOUBLE_SCALAR.Pressure;
import org.djunits.value.vdouble.scalar.DOUBLE_SCALAR.Speed;
import org.djunits.value.vdouble.scalar.DOUBLE_SCALAR.Temperature;
import org.djunits.value.vdouble.scalar.DOUBLE_SCALAR.Time;
import org.djunits.value.vdouble.scalar.DOUBLE_SCALAR.Torque;
import org.djunits.value.vdouble.scalar.DOUBLE_SCALAR.Volume;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-26 01:01:13 +0200 (Sun, 26 Jul 2015) $, @version $Revision: 1155 $, by $Author: averbraeck $,
 * initial version Feb 2, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public interface DiscreteDistDoubleScalar
{
    /**
     * Absolute value.
     * @param <T> The absolute doublescalar type
     * @param <U> The unit type used
     */
    public static class Abs<T extends DoubleScalar.Abs<U>, U extends Unit<U>> extends AbstractDiscreteDistScalar implements
        Absolute
    {
        /**
         * @param distribution the wrapped distribution function.
         * @param unit the unit.
         */
        public Abs(final DistDiscrete distribution, final U unit)
        {
            super(distribution, unit);
        }

        /**
         * @param constant the constant value.
         * @param unit the unit.
         */
        public Abs(final long constant, final U unit)
        {
            super(constant, unit);
        }

        /**
         * @return a drawn number from the distribution in the given unit.
         */
        @SuppressWarnings({"unchecked", "rawtypes"})
        public final T draw()
        {
            switch (getUnit().getClass().getSimpleName())
            {
                case "AccelerationUnit":
                    return (T) new Acceleration.Abs(getDistribution().draw(), (AccelerationUnit) getUnit());

                case "AnglePlaneUnit":
                    return (T) new AnglePlane.Abs(getDistribution().draw(), (AnglePlaneUnit) getUnit());

                case "AngleSlopeUnit":
                    return (T) new AngleSlope.Abs(getDistribution().draw(), (AngleSlopeUnit) getUnit());

                case "AngleSolidUnit":
                    return (T) new AngleSolid.Abs(getDistribution().draw(), (AngleSolidUnit) getUnit());

                case "AreaUnit":
                    return (T) new Area.Abs(getDistribution().draw(), (AreaUnit) getUnit());

                case "DensityUnit":
                    return (T) new Density.Abs(getDistribution().draw(), (DensityUnit) getUnit());

                case "DimensionlessUnit":
                    return (T) new Dimensionless.Abs(getDistribution().draw(), (DimensionlessUnit) getUnit());

                case "ElectricalChargeUnit":
                    return (T) new ElectricalCharge.Abs(getDistribution().draw(), (ElectricalChargeUnit) getUnit());

                case "ElectricalCurrentUnit":
                    return (T) new ElectricalCurrent.Abs(getDistribution().draw(), (ElectricalCurrentUnit) getUnit());

                case "ElectricalPotentialUnit":
                    return (T) new ElectricalPotential.Abs(getDistribution().draw(), (ElectricalPotentialUnit) getUnit());

                case "ElectricalResistanceUnit":
                    return (T) new ElectricalResistance.Abs(getDistribution().draw(), (ElectricalResistanceUnit) getUnit());

                case "EnergyUnit":
                    return (T) new Energy.Abs(getDistribution().draw(), (EnergyUnit) getUnit());

                case "FlowMassUnit":
                    return (T) new FlowMass.Abs(getDistribution().draw(), (FlowMassUnit) getUnit());

                case "FlowVolumeUnit":
                    return (T) new FlowVolume.Abs(getDistribution().draw(), (FlowVolumeUnit) getUnit());

                case "ForceUnit":
                    return (T) new Force.Abs(getDistribution().draw(), (ForceUnit) getUnit());

                case "FrequencyUnit":
                    return (T) new Frequency.Abs(getDistribution().draw(), (FrequencyUnit) getUnit());

                case "LengthUnit":
                    return (T) new Length.Abs(getDistribution().draw(), (LengthUnit) getUnit());

                case "LinearDensityUnit":
                    return (T) new LinearDensity.Abs(getDistribution().draw(), (LinearDensityUnit) getUnit());

                case "MassUnit":
                    return (T) new Mass.Abs(getDistribution().draw(), (MassUnit) getUnit());

                case "PowerUnit":
                    return (T) new Power.Abs(getDistribution().draw(), (PowerUnit) getUnit());

                case "PressureUnit":
                    return (T) new Pressure.Abs(getDistribution().draw(), (PressureUnit) getUnit());

                case "SpeedUnit":
                    return (T) new Speed.Abs(getDistribution().draw(), (SpeedUnit) getUnit());

                case "TemperatureUnit":
                    return (T) new Temperature.Abs(getDistribution().draw(), (TemperatureUnit) getUnit());

                case "TimeUnit":
                    return (T) new Time.Abs(getDistribution().draw(), (TimeUnit) getUnit());

                case "TorqueUnit":
                    return (T) new Torque.Abs(getDistribution().draw(), (TorqueUnit) getUnit());

                case "VolumeUnit":
                    return (T) new Volume.Abs(getDistribution().draw(), (VolumeUnit) getUnit());

                default:
                    return (T) new DoubleScalar.Abs(getDistribution().draw(), getUnit());
            }
        }
    }

    /**
     * Relative value.
     * @param <T> The absolute doublescalar type
     * @param <U> The unit type used
     */
    public static class Rel<T extends DoubleScalar.Rel<U>, U extends Unit<U>> extends AbstractDiscreteDistScalar implements
        Relative
    {
        /**
         * @param distribution the wrapped distribution function.
         * @param unit the unit.
         */
        public Rel(final DistDiscrete distribution, final U unit)
        {
            super(distribution, unit);
        }

        /**
         * @param constant the constant value.
         * @param unit the unit.
         */
        public Rel(final long constant, final U unit)
        {
            super(constant, unit);
        }

        /**
         * @return a drawn number from the distribution in the given unit.
         */
        @SuppressWarnings({"unchecked", "rawtypes"})
        public final T draw()
        {
            switch (getUnit().getClass().getSimpleName())
            {
                case "AccelerationUnit":
                    return (T) new Acceleration.Rel(getDistribution().draw(), (AccelerationUnit) getUnit());

                case "AnglePlaneUnit":
                    return (T) new AnglePlane.Rel(getDistribution().draw(), (AnglePlaneUnit) getUnit());

                case "AngleSlopeUnit":
                    return (T) new AngleSlope.Rel(getDistribution().draw(), (AngleSlopeUnit) getUnit());

                case "AngleSolidUnit":
                    return (T) new AngleSolid.Rel(getDistribution().draw(), (AngleSolidUnit) getUnit());

                case "AreaUnit":
                    return (T) new Area.Rel(getDistribution().draw(), (AreaUnit) getUnit());

                case "DensityUnit":
                    return (T) new Density.Rel(getDistribution().draw(), (DensityUnit) getUnit());

                case "DimensionlessUnit":
                    return (T) new Dimensionless.Rel(getDistribution().draw(), (DimensionlessUnit) getUnit());

                case "ElectricalChargeUnit":
                    return (T) new ElectricalCharge.Rel(getDistribution().draw(), (ElectricalChargeUnit) getUnit());

                case "ElectricalCurrentUnit":
                    return (T) new ElectricalCurrent.Rel(getDistribution().draw(), (ElectricalCurrentUnit) getUnit());

                case "ElectricalPotentialUnit":
                    return (T) new ElectricalPotential.Rel(getDistribution().draw(), (ElectricalPotentialUnit) getUnit());

                case "ElectricalResistanceUnit":
                    return (T) new ElectricalResistance.Rel(getDistribution().draw(), (ElectricalResistanceUnit) getUnit());

                case "EnergyUnit":
                    return (T) new Energy.Rel(getDistribution().draw(), (EnergyUnit) getUnit());

                case "FlowMassUnit":
                    return (T) new FlowMass.Rel(getDistribution().draw(), (FlowMassUnit) getUnit());

                case "FlowVolumeUnit":
                    return (T) new FlowVolume.Rel(getDistribution().draw(), (FlowVolumeUnit) getUnit());

                case "ForceUnit":
                    return (T) new Force.Rel(getDistribution().draw(), (ForceUnit) getUnit());

                case "FrequencyUnit":
                    return (T) new Frequency.Rel(getDistribution().draw(), (FrequencyUnit) getUnit());

                case "LengthUnit":
                    return (T) new Length.Rel(getDistribution().draw(), (LengthUnit) getUnit());

                case "LinearDensityUnit":
                    return (T) new LinearDensity.Rel(getDistribution().draw(), (LinearDensityUnit) getUnit());

                case "MassUnit":
                    return (T) new Mass.Rel(getDistribution().draw(), (MassUnit) getUnit());

                case "PowerUnit":
                    return (T) new Power.Rel(getDistribution().draw(), (PowerUnit) getUnit());

                case "PressureUnit":
                    return (T) new Pressure.Rel(getDistribution().draw(), (PressureUnit) getUnit());

                case "SpeedUnit":
                    return (T) new Speed.Rel(getDistribution().draw(), (SpeedUnit) getUnit());

                case "TemperatureUnit":
                    return (T) new Temperature.Rel(getDistribution().draw(), (TemperatureUnit) getUnit());

                case "TimeUnit":
                    return (T) new Time.Rel(getDistribution().draw(), (TimeUnit) getUnit());

                case "TorqueUnit":
                    return (T) new Torque.Rel(getDistribution().draw(), (TorqueUnit) getUnit());

                case "VolumeUnit":
                    return (T) new Volume.Rel(getDistribution().draw(), (VolumeUnit) getUnit());

                default:
                    return (T) new DoubleScalar.Rel(getDistribution().draw(), getUnit());
            }
        }
    }

}
