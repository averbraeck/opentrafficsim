package org.opentrafficsim.core.units.distributions;

import nl.tudelft.simulation.jstats.distributions.DistContinuous;

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
import org.djunits.value.vfloat.scalar.FloatScalar;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-26 01:01:13 +0200 (Sun, 26 Jul 2015) $, @version $Revision: 1155 $, by $Author: averbraeck $,
 * initial version Feb 2, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public interface ContinuousDistFloatScalar extends OTS_FLOAT_DIST
{
    /**
     * Absolute value.
     * @param <T> The absolute FloatScalar type
     * @param <U> The unit type used
     */
    public static class Abs<T extends FloatScalar.Abs<U>, U extends Unit<U>> extends AbstractContinuousDistScalar implements
        Absolute
    {
        /**
         * @param distribution the wrapped distribution function.
         * @param unit the unit.
         */
        public Abs(final DistContinuous distribution, final U unit)
        {
            super(distribution, unit);
        }

        /**
         * @param constant the constant value.
         * @param unit the unit.
         */
        public Abs(final float constant, final U unit)
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
                    return (T) new Acceleration.Abs((float) getDistribution().draw(), (AccelerationUnit) getUnit());

                case "AnglePlaneUnit":
                    return (T) new AnglePlane.Abs((float) getDistribution().draw(), (AnglePlaneUnit) getUnit());

                case "AngleSlopeUnit":
                    return (T) new AngleSlope.Abs((float) getDistribution().draw(), (AngleSlopeUnit) getUnit());

                case "AngleSolidUnit":
                    return (T) new AngleSolid.Abs((float) getDistribution().draw(), (AngleSolidUnit) getUnit());

                case "AreaUnit":
                    return (T) new Area.Abs((float) getDistribution().draw(), (AreaUnit) getUnit());

                case "DensityUnit":
                    return (T) new Density.Abs((float) getDistribution().draw(), (DensityUnit) getUnit());

                case "DimensionlessUnit":
                    return (T) new Dimensionless.Abs((float) getDistribution().draw(), (DimensionlessUnit) getUnit());

                case "ElectricalChargeUnit":
                    return (T) new ElectricalCharge.Abs((float) getDistribution().draw(), (ElectricalChargeUnit) getUnit());

                case "ElectricalCurrentUnit":
                    return (T) new ElectricalCurrent.Abs((float) getDistribution().draw(), (ElectricalCurrentUnit) getUnit());

                case "ElectricalPotentialUnit":
                    return (T) new ElectricalPotential.Abs((float) getDistribution().draw(),
                        (ElectricalPotentialUnit) getUnit());

                case "ElectricalResistanceUnit":
                    return (T) new ElectricalResistance.Abs((float) getDistribution().draw(),
                        (ElectricalResistanceUnit) getUnit());

                case "EnergyUnit":
                    return (T) new Energy.Abs((float) getDistribution().draw(), (EnergyUnit) getUnit());

                case "FlowMassUnit":
                    return (T) new FlowMass.Abs((float) getDistribution().draw(), (FlowMassUnit) getUnit());

                case "FlowVolumeUnit":
                    return (T) new FlowVolume.Abs((float) getDistribution().draw(), (FlowVolumeUnit) getUnit());

                case "ForceUnit":
                    return (T) new Force.Abs((float) getDistribution().draw(), (ForceUnit) getUnit());

                case "FrequencyUnit":
                    return (T) new Frequency.Abs((float) getDistribution().draw(), (FrequencyUnit) getUnit());

                case "LengthUnit":
                    return (T) new Length.Abs((float) getDistribution().draw(), (LengthUnit) getUnit());

                case "LinearDensityUnit":
                    return (T) new LinearDensity.Abs((float) getDistribution().draw(), (LinearDensityUnit) getUnit());

                case "MassUnit":
                    return (T) new Mass.Abs((float) getDistribution().draw(), (MassUnit) getUnit());

                case "PowerUnit":
                    return (T) new Power.Abs((float) getDistribution().draw(), (PowerUnit) getUnit());

                case "PressureUnit":
                    return (T) new Pressure.Abs((float) getDistribution().draw(), (PressureUnit) getUnit());

                case "SpeedUnit":
                    return (T) new Speed.Abs((float) getDistribution().draw(), (SpeedUnit) getUnit());

                case "TemperatureUnit":
                    return (T) new Temperature.Abs((float) getDistribution().draw(), (TemperatureUnit) getUnit());

                case "TimeUnit":
                    return (T) new Time.Abs((float) getDistribution().draw(), (TimeUnit) getUnit());

                case "TorqueUnit":
                    return (T) new Torque.Abs((float) getDistribution().draw(), (TorqueUnit) getUnit());

                case "VolumeUnit":
                    return (T) new Volume.Abs((float) getDistribution().draw(), (VolumeUnit) getUnit());

                default:
                    return (T) new FloatScalar.Abs((float) getDistribution().draw(), getUnit());
            }
        }
    }

    /**
     * Relative value.
     * @param <T> The absolute FloatScalar type
     * @param <U> The unit type used
     */
    public static class Rel<T extends FloatScalar.Rel<U>, U extends Unit<U>> extends AbstractContinuousDistScalar implements
        Relative
    {
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
        @SuppressWarnings({"unchecked", "rawtypes"})
        public final T draw()
        {
            switch (getUnit().getClass().getSimpleName())
            {
                case "AccelerationUnit":
                    return (T) new Acceleration.Rel((float) getDistribution().draw(), (AccelerationUnit) getUnit());

                case "AnglePlaneUnit":
                    return (T) new AnglePlane.Rel((float) getDistribution().draw(), (AnglePlaneUnit) getUnit());

                case "AngleSlopeUnit":
                    return (T) new AngleSlope.Rel((float) getDistribution().draw(), (AngleSlopeUnit) getUnit());

                case "AngleSolidUnit":
                    return (T) new AngleSolid.Rel((float) getDistribution().draw(), (AngleSolidUnit) getUnit());

                case "AreaUnit":
                    return (T) new Area.Rel((float) getDistribution().draw(), (AreaUnit) getUnit());

                case "DensityUnit":
                    return (T) new Density.Rel((float) getDistribution().draw(), (DensityUnit) getUnit());

                case "DimensionlessUnit":
                    return (T) new Dimensionless.Rel((float) getDistribution().draw(), (DimensionlessUnit) getUnit());

                case "ElectricalChargeUnit":
                    return (T) new ElectricalCharge.Rel((float) getDistribution().draw(), (ElectricalChargeUnit) getUnit());

                case "ElectricalCurrentUnit":
                    return (T) new ElectricalCurrent.Rel((float) getDistribution().draw(), (ElectricalCurrentUnit) getUnit());

                case "ElectricalPotentialUnit":
                    return (T) new ElectricalPotential.Rel((float) getDistribution().draw(),
                        (ElectricalPotentialUnit) getUnit());

                case "ElectricalResistanceUnit":
                    return (T) new ElectricalResistance.Rel((float) getDistribution().draw(),
                        (ElectricalResistanceUnit) getUnit());

                case "EnergyUnit":
                    return (T) new Energy.Rel((float) getDistribution().draw(), (EnergyUnit) getUnit());

                case "FlowMassUnit":
                    return (T) new FlowMass.Rel((float) getDistribution().draw(), (FlowMassUnit) getUnit());

                case "FlowVolumeUnit":
                    return (T) new FlowVolume.Rel((float) getDistribution().draw(), (FlowVolumeUnit) getUnit());

                case "ForceUnit":
                    return (T) new Force.Rel((float) getDistribution().draw(), (ForceUnit) getUnit());

                case "FrequencyUnit":
                    return (T) new Frequency.Rel((float) getDistribution().draw(), (FrequencyUnit) getUnit());

                case "LengthUnit":
                    return (T) new Length.Rel((float) getDistribution().draw(), (LengthUnit) getUnit());

                case "LinearDensityUnit":
                    return (T) new LinearDensity.Rel((float) getDistribution().draw(), (LinearDensityUnit) getUnit());

                case "MassUnit":
                    return (T) new Mass.Rel((float) getDistribution().draw(), (MassUnit) getUnit());

                case "PowerUnit":
                    return (T) new Power.Rel((float) getDistribution().draw(), (PowerUnit) getUnit());

                case "PressureUnit":
                    return (T) new Pressure.Rel((float) getDistribution().draw(), (PressureUnit) getUnit());

                case "SpeedUnit":
                    return (T) new Speed.Rel((float) getDistribution().draw(), (SpeedUnit) getUnit());

                case "TemperatureUnit":
                    return (T) new Temperature.Rel((float) getDistribution().draw(), (TemperatureUnit) getUnit());

                case "TimeUnit":
                    return (T) new Time.Rel((float) getDistribution().draw(), (TimeUnit) getUnit());

                case "TorqueUnit":
                    return (T) new Torque.Rel((float) getDistribution().draw(), (TorqueUnit) getUnit());

                case "VolumeUnit":
                    return (T) new Volume.Rel((float) getDistribution().draw(), (VolumeUnit) getUnit());

                default:
                    return (T) new FloatScalar.Rel((float) getDistribution().draw(), getUnit());
            }
        }
    }

}
