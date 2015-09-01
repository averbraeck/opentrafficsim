package org.opentrafficsim.core.units.distributions;

import nl.tudelft.simulation.jstats.distributions.DistContinuous;
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
import org.djunits.unit.VolumeUnit;
import org.djunits.value.Absolute;
import org.djunits.value.Relative;
import org.djunits.value.vdouble.scalar.DOUBLE_SCALAR;

@SuppressWarnings({"checkstyle:interfaceistype", "checkstyle:javadocmethod", "checkstyle:javadoctype",
    "checkstyle:filelength", "checkstyle:javadocvariable", "checkstyle:linelength", "checkstyle:leftcurly",
    "checkstyle:rightcurly", "javadoc"})
public interface OTS_DOUBLE_DIST extends DOUBLE_SCALAR
{
    // @formatter:off
    
    /****************************************************************************************************************/
    /************************************************** ACCELERATION ************************************************/ 
    /****************************************************************************************************************/

    abstract class AccelerationContinuousDist extends DistContinuousDoubleScalar<AccelerationUnit>
    {
        protected AccelerationContinuousDist(final DistContinuous dist, final AccelerationUnit unit) { super(dist, unit); }
        protected AccelerationContinuousDist(final double value, final AccelerationUnit unit) { super(value, unit); }
        
        public static class Rel extends DistContinuousDoubleScalar<AccelerationUnit> implements Relative
        {
            public Rel(final DistContinuous dist, final AccelerationUnit unit) { super(dist, unit); }
            public Rel(final double value, final AccelerationUnit unit) { super(value, unit); }
            public Acceleration.Rel draw() { return new Acceleration.Rel(getDistribution().draw(), getUnit()); }
        }
        
        public static class Abs extends DistContinuousDoubleScalar<AccelerationUnit> implements Absolute
        {
            public Abs(final DistContinuous dist, final AccelerationUnit unit) { super(dist, unit); }
            public Abs(final double value, final AccelerationUnit unit) { super(value, unit); }
            public Acceleration.Abs draw() { return new Acceleration.Abs(getDistribution().draw(), getUnit()); }
        }
    }

    abstract class AccelerationDiscreteDist extends DistDiscreteDoubleScalar<AccelerationUnit>
    {
        protected AccelerationDiscreteDist(final DistDiscrete dist, final AccelerationUnit unit) { super(dist, unit); }
        protected AccelerationDiscreteDist(final long value, final AccelerationUnit unit) { super(value, unit); }
        
        public static class Rel extends DistDiscreteDoubleScalar<AccelerationUnit> implements Relative
        {
            public Rel(final DistDiscrete dist, final AccelerationUnit unit) { super(dist, unit); }
            public Rel(final long value, final AccelerationUnit unit) { super(value, unit); }
            public Acceleration.Rel draw() { return new Acceleration.Rel(getDistribution().draw(), getUnit()); }
        }
        
        public static class Abs extends DistDiscreteDoubleScalar<AccelerationUnit> implements Absolute
        {
            public Abs(final DistDiscrete dist, final AccelerationUnit unit) { super(dist, unit); }
            public Abs(final long value, final AccelerationUnit unit) { super(value, unit); }
            public Acceleration.Abs draw() { return new Acceleration.Abs(getDistribution().draw(), getUnit()); }
        }
    }

    /****************************************************************************************************************/
    /************************************************** ANGLE_PLANE *************************************************/ 
    /****************************************************************************************************************/

    abstract class AnglePlaneContinuousDist extends DistContinuousDoubleScalar<AnglePlaneUnit>
    {
        protected AnglePlaneContinuousDist(final DistContinuous dist, final AnglePlaneUnit unit) { super(dist, unit); }
        protected AnglePlaneContinuousDist(final double value, final AnglePlaneUnit unit) { super(value, unit); }
        
        public static class Rel extends DistContinuousDoubleScalar<AnglePlaneUnit> implements Relative
        {
            public Rel(final DistContinuous dist, final AnglePlaneUnit unit) { super(dist, unit); }
            public Rel(final double value, final AnglePlaneUnit unit) { super(value, unit); }
            public AnglePlane.Rel draw() { return new AnglePlane.Rel(getDistribution().draw(), getUnit()); }
        }
        
        public static class Abs extends DistContinuousDoubleScalar<AnglePlaneUnit> implements Absolute
        {
            public Abs(final DistContinuous dist, final AnglePlaneUnit unit) { super(dist, unit); }
            public Abs(final double value, final AnglePlaneUnit unit) { super(value, unit); }
            public AnglePlane.Abs draw() { return new AnglePlane.Abs(getDistribution().draw(), getUnit()); }
        }
    }

    abstract class AnglePlaneDiscreteDist extends DistDiscreteDoubleScalar<AnglePlaneUnit>
    {
        protected AnglePlaneDiscreteDist(final DistDiscrete dist, final AnglePlaneUnit unit) { super(dist, unit); }
        protected AnglePlaneDiscreteDist(final long value, final AnglePlaneUnit unit) { super(value, unit); }
        
        public static class Rel extends DistDiscreteDoubleScalar<AnglePlaneUnit> implements Relative
        {
            public Rel(final DistDiscrete dist, final AnglePlaneUnit unit) { super(dist, unit); }
            public Rel(final long value, final AnglePlaneUnit unit) { super(value, unit); }
            public AnglePlane.Rel draw() { return new AnglePlane.Rel(getDistribution().draw(), getUnit()); }
        }
        
        public static class Abs extends DistDiscreteDoubleScalar<AnglePlaneUnit> implements Absolute
        {
            public Abs(final DistDiscrete dist, final AnglePlaneUnit unit) { super(dist, unit); }
            public Abs(final long value, final AnglePlaneUnit unit) { super(value, unit); }
            public AnglePlane.Abs draw() { return new AnglePlane.Abs(getDistribution().draw(), getUnit()); }
        }
    }

    /****************************************************************************************************************/
    /************************************************** ANGLE_SLOPE *************************************************/ 
    /****************************************************************************************************************/

    abstract class AngleSlopeContinuousDist extends DistContinuousDoubleScalar<AngleSlopeUnit>
    {
        protected AngleSlopeContinuousDist(final DistContinuous dist, final AngleSlopeUnit unit) { super(dist, unit); }
        protected AngleSlopeContinuousDist(final double value, final AngleSlopeUnit unit) { super(value, unit); }
        
        public static class Rel extends DistContinuousDoubleScalar<AngleSlopeUnit> implements Relative
        {
            public Rel(final DistContinuous dist, final AngleSlopeUnit unit) { super(dist, unit); }
            public Rel(final double value, final AngleSlopeUnit unit) { super(value, unit); }
            public AngleSlope.Rel draw() { return new AngleSlope.Rel(getDistribution().draw(), getUnit()); }
        }
        
        public static class Abs extends DistContinuousDoubleScalar<AngleSlopeUnit> implements Absolute
        {
            public Abs(final DistContinuous dist, final AngleSlopeUnit unit) { super(dist, unit); }
            public Abs(final double value, final AngleSlopeUnit unit) { super(value, unit); }
            public AngleSlope.Abs draw() { return new AngleSlope.Abs(getDistribution().draw(), getUnit()); }
        }
    }

    abstract class AngleSlopeDiscreteDist extends DistDiscreteDoubleScalar<AngleSlopeUnit>
    {
        protected AngleSlopeDiscreteDist(final DistDiscrete dist, final AngleSlopeUnit unit) { super(dist, unit); }
        protected AngleSlopeDiscreteDist(final long value, final AngleSlopeUnit unit) { super(value, unit); }
        
        public static class Rel extends DistDiscreteDoubleScalar<AngleSlopeUnit> implements Relative
        {
            public Rel(final DistDiscrete dist, final AngleSlopeUnit unit) { super(dist, unit); }
            public Rel(final long value, final AngleSlopeUnit unit) { super(value, unit); }
            public AngleSlope.Rel draw() { return new AngleSlope.Rel(getDistribution().draw(), getUnit()); }
        }
        
        public static class Abs extends DistDiscreteDoubleScalar<AngleSlopeUnit> implements Absolute
        {
            public Abs(final DistDiscrete dist, final AngleSlopeUnit unit) { super(dist, unit); }
            public Abs(final long value, final AngleSlopeUnit unit) { super(value, unit); }
            public AngleSlope.Abs draw() { return new AngleSlope.Abs(getDistribution().draw(), getUnit()); }
        }
    }
    
    /****************************************************************************************************************/
    /************************************************** ANGLE_SOLID *************************************************/ 
    /****************************************************************************************************************/

    abstract class AngleSolidContinuousDist extends DistContinuousDoubleScalar<AngleSolidUnit>
    {
        protected AngleSolidContinuousDist(final DistContinuous dist, final AngleSolidUnit unit) { super(dist, unit); }
        protected AngleSolidContinuousDist(final double value, final AngleSolidUnit unit) { super(value, unit); }
        
        public static class Rel extends DistContinuousDoubleScalar<AngleSolidUnit> implements Relative
        {
            public Rel(final DistContinuous dist, final AngleSolidUnit unit) { super(dist, unit); }
            public Rel(final double value, final AngleSolidUnit unit) { super(value, unit); }
            public AngleSolid.Rel draw() { return new AngleSolid.Rel(getDistribution().draw(), getUnit()); }
        }
        
        public static class Abs extends DistContinuousDoubleScalar<AngleSolidUnit> implements Absolute
        {
            public Abs(final DistContinuous dist, final AngleSolidUnit unit) { super(dist, unit); }
            public Abs(final double value, final AngleSolidUnit unit) { super(value, unit); }
            public AngleSolid.Abs draw() { return new AngleSolid.Abs(getDistribution().draw(), getUnit()); }
        }
    }

    abstract class AngleSolidDiscreteDist extends DistDiscreteDoubleScalar<AngleSolidUnit>
    {
        protected AngleSolidDiscreteDist(final DistDiscrete dist, final AngleSolidUnit unit) { super(dist, unit); }
        protected AngleSolidDiscreteDist(final long value, final AngleSolidUnit unit) { super(value, unit); }
        
        public static class Rel extends DistDiscreteDoubleScalar<AngleSolidUnit> implements Relative
        {
            public Rel(final DistDiscrete dist, final AngleSolidUnit unit) { super(dist, unit); }
            public Rel(final long value, final AngleSolidUnit unit) { super(value, unit); }
            public AngleSolid.Rel draw() { return new AngleSolid.Rel(getDistribution().draw(), getUnit()); }
        }
        
        public static class Abs extends DistDiscreteDoubleScalar<AngleSolidUnit> implements Absolute
        {
            public Abs(final DistDiscrete dist, final AngleSolidUnit unit) { super(dist, unit); }
            public Abs(final long value, final AngleSolidUnit unit) { super(value, unit); }
            public AngleSolid.Abs draw() { return new AngleSolid.Abs(getDistribution().draw(), getUnit()); }
        }
    }

    /****************************************************************************************************************/
    /***************************************************** AREA *****************************************************/ 
    /****************************************************************************************************************/

    abstract class AreaContinuousDist extends DistContinuousDoubleScalar<AreaUnit>
    {
        protected AreaContinuousDist(final DistContinuous dist, final AreaUnit unit) { super(dist, unit); }
        protected AreaContinuousDist(final double value, final AreaUnit unit) { super(value, unit); }
        
        public static class Rel extends DistContinuousDoubleScalar<AreaUnit> implements Relative
        {
            public Rel(final DistContinuous dist, final AreaUnit unit) { super(dist, unit); }
            public Rel(final double value, final AreaUnit unit) { super(value, unit); }
            public Area.Rel draw() { return new Area.Rel(getDistribution().draw(), getUnit()); }
        }
        
        public static class Abs extends DistContinuousDoubleScalar<AreaUnit> implements Absolute
        {
            public Abs(final DistContinuous dist, final AreaUnit unit) { super(dist, unit); }
            public Abs(final double value, final AreaUnit unit) { super(value, unit); }
            public Area.Abs draw() { return new Area.Abs(getDistribution().draw(), getUnit()); }
        }
    }

    abstract class AreaDiscreteDist extends DistDiscreteDoubleScalar<AreaUnit>
    {
        protected AreaDiscreteDist(final DistDiscrete dist, final AreaUnit unit) { super(dist, unit); }
        protected AreaDiscreteDist(final long value, final AreaUnit unit) { super(value, unit); }
        
        public static class Rel extends DistDiscreteDoubleScalar<AreaUnit> implements Relative
        {
            public Rel(final DistDiscrete dist, final AreaUnit unit) { super(dist, unit); }
            public Rel(final long value, final AreaUnit unit) { super(value, unit); }
            public Area.Rel draw() { return new Area.Rel(getDistribution().draw(), getUnit()); }
        }
        
        public static class Abs extends DistDiscreteDoubleScalar<AreaUnit> implements Absolute
        {
            public Abs(final DistDiscrete dist, final AreaUnit unit) { super(dist, unit); }
            public Abs(final long value, final AreaUnit unit) { super(value, unit); }
            public Area.Abs draw() { return new Area.Abs(getDistribution().draw(), getUnit()); }
        }
    }

    /****************************************************************************************************************/
    /**************************************************** DENSITY ***************************************************/ 
    /****************************************************************************************************************/

    abstract class DensityContinuousDist extends DistContinuousDoubleScalar<DensityUnit>
    {
        protected DensityContinuousDist(final DistContinuous dist, final DensityUnit unit) { super(dist, unit); }
        protected DensityContinuousDist(final double value, final DensityUnit unit) { super(value, unit); }
        
        public static class Rel extends DistContinuousDoubleScalar<DensityUnit> implements Relative
        {
            public Rel(final DistContinuous dist, final DensityUnit unit) { super(dist, unit); }
            public Rel(final double value, final DensityUnit unit) { super(value, unit); }
            public Density.Rel draw() { return new Density.Rel(getDistribution().draw(), getUnit()); }
        }
        
        public static class Abs extends DistContinuousDoubleScalar<DensityUnit> implements Absolute
        {
            public Abs(final DistContinuous dist, final DensityUnit unit) { super(dist, unit); }
            public Abs(final double value, final DensityUnit unit) { super(value, unit); }
            public Density.Abs draw() { return new Density.Abs(getDistribution().draw(), getUnit()); }
        }
    }

    abstract class DensityDiscreteDist extends DistDiscreteDoubleScalar<DensityUnit>
    {
        protected DensityDiscreteDist(final DistDiscrete dist, final DensityUnit unit) { super(dist, unit); }
        protected DensityDiscreteDist(final long value, final DensityUnit unit) { super(value, unit); }
        
        public static class Rel extends DistDiscreteDoubleScalar<DensityUnit> implements Relative
        {
            public Rel(final DistDiscrete dist, final DensityUnit unit) { super(dist, unit); }
            public Rel(final long value, final DensityUnit unit) { super(value, unit); }
            public Density.Rel draw() { return new Density.Rel(getDistribution().draw(), getUnit()); }
        }
        
        public static class Abs extends DistDiscreteDoubleScalar<DensityUnit> implements Absolute
        {
            public Abs(final DistDiscrete dist, final DensityUnit unit) { super(dist, unit); }
            public Abs(final long value, final DensityUnit unit) { super(value, unit); }
            public Density.Abs draw() { return new Density.Abs(getDistribution().draw(), getUnit()); }
        }
    }

    /****************************************************************************************************************/
    /*********************************************** ELECTRICAL_CHARGE **********************************************/ 
    /****************************************************************************************************************/

    abstract class ElectricalChargeContinuousDist extends DistContinuousDoubleScalar<ElectricalChargeUnit>
    {
        protected ElectricalChargeContinuousDist(final DistContinuous dist, final ElectricalChargeUnit unit) { super(dist, unit); }
        protected ElectricalChargeContinuousDist(final double value, final ElectricalChargeUnit unit) { super(value, unit); }
        
        public static class Rel extends DistContinuousDoubleScalar<ElectricalChargeUnit> implements Relative
        {
            public Rel(final DistContinuous dist, final ElectricalChargeUnit unit) { super(dist, unit); }
            public Rel(final double value, final ElectricalChargeUnit unit) { super(value, unit); }
            public ElectricalCharge.Rel draw() { return new ElectricalCharge.Rel(getDistribution().draw(), getUnit()); }
        }
        
        public static class Abs extends DistContinuousDoubleScalar<ElectricalChargeUnit> implements Absolute
        {
            public Abs(final DistContinuous dist, final ElectricalChargeUnit unit) { super(dist, unit); }
            public Abs(final double value, final ElectricalChargeUnit unit) { super(value, unit); }
            public ElectricalCharge.Abs draw() { return new ElectricalCharge.Abs(getDistribution().draw(), getUnit()); }
        }
    }

    abstract class ElectricalChargeDiscreteDist extends DistDiscreteDoubleScalar<ElectricalChargeUnit>
    {
        protected ElectricalChargeDiscreteDist(final DistDiscrete dist, final ElectricalChargeUnit unit) { super(dist, unit); }
        protected ElectricalChargeDiscreteDist(final long value, final ElectricalChargeUnit unit) { super(value, unit); }
        
        public static class Rel extends DistDiscreteDoubleScalar<ElectricalChargeUnit> implements Relative
        {
            public Rel(final DistDiscrete dist, final ElectricalChargeUnit unit) { super(dist, unit); }
            public Rel(final long value, final ElectricalChargeUnit unit) { super(value, unit); }
            public ElectricalCharge.Rel draw() { return new ElectricalCharge.Rel(getDistribution().draw(), getUnit()); }
        }
        
        public static class Abs extends DistDiscreteDoubleScalar<ElectricalChargeUnit> implements Absolute
        {
            public Abs(final DistDiscrete dist, final ElectricalChargeUnit unit) { super(dist, unit); }
            public Abs(final long value, final ElectricalChargeUnit unit) { super(value, unit); }
            public ElectricalCharge.Abs draw() { return new ElectricalCharge.Abs(getDistribution().draw(), getUnit()); }
        }
    }

    /****************************************************************************************************************/
    /********************************************** ELECTRICAL_CURRENT **********************************************/ 
    /****************************************************************************************************************/

    abstract class ElectricalCurrentContinuousDist extends DistContinuousDoubleScalar<ElectricalCurrentUnit>
    {
        protected ElectricalCurrentContinuousDist(final DistContinuous dist, final ElectricalCurrentUnit unit) { super(dist, unit); }
        protected ElectricalCurrentContinuousDist(final double value, final ElectricalCurrentUnit unit) { super(value, unit); }
        
        public static class Rel extends DistContinuousDoubleScalar<ElectricalCurrentUnit> implements Relative
        {
            public Rel(final DistContinuous dist, final ElectricalCurrentUnit unit) { super(dist, unit); }
            public Rel(final double value, final ElectricalCurrentUnit unit) { super(value, unit); }
            public ElectricalCurrent.Rel draw() { return new ElectricalCurrent.Rel(getDistribution().draw(), getUnit()); }
        }
        
        public static class Abs extends DistContinuousDoubleScalar<ElectricalCurrentUnit> implements Absolute
        {
            public Abs(final DistContinuous dist, final ElectricalCurrentUnit unit) { super(dist, unit); }
            public Abs(final double value, final ElectricalCurrentUnit unit) { super(value, unit); }
            public ElectricalCurrent.Abs draw() { return new ElectricalCurrent.Abs(getDistribution().draw(), getUnit()); }
        }
    }

    abstract class ElectricalCurrentDiscreteDist extends DistDiscreteDoubleScalar<ElectricalCurrentUnit>
    {
        protected ElectricalCurrentDiscreteDist(final DistDiscrete dist, final ElectricalCurrentUnit unit) { super(dist, unit); }
        protected ElectricalCurrentDiscreteDist(final long value, final ElectricalCurrentUnit unit) { super(value, unit); }
        
        public static class Rel extends DistDiscreteDoubleScalar<ElectricalCurrentUnit> implements Relative
        {
            public Rel(final DistDiscrete dist, final ElectricalCurrentUnit unit) { super(dist, unit); }
            public Rel(final long value, final ElectricalCurrentUnit unit) { super(value, unit); }
            public ElectricalCurrent.Rel draw() { return new ElectricalCurrent.Rel(getDistribution().draw(), getUnit()); }
        }
        
        public static class Abs extends DistDiscreteDoubleScalar<ElectricalCurrentUnit> implements Absolute
        {
            public Abs(final DistDiscrete dist, final ElectricalCurrentUnit unit) { super(dist, unit); }
            public Abs(final long value, final ElectricalCurrentUnit unit) { super(value, unit); }
            public ElectricalCurrent.Abs draw() { return new ElectricalCurrent.Abs(getDistribution().draw(), getUnit()); }
        }
    }

    /****************************************************************************************************************/
    /********************************************* ELECTRICAL_POTENTIAL *********************************************/ 
    /****************************************************************************************************************/

    abstract class ElectricalPotentialContinuousDist extends DistContinuousDoubleScalar<ElectricalPotentialUnit>
    {
        protected ElectricalPotentialContinuousDist(final DistContinuous dist, final ElectricalPotentialUnit unit) { super(dist, unit); }
        protected ElectricalPotentialContinuousDist(final double value, final ElectricalPotentialUnit unit) { super(value, unit); }
        
        public static class Rel extends DistContinuousDoubleScalar<ElectricalPotentialUnit> implements Relative
        {
            public Rel(final DistContinuous dist, final ElectricalPotentialUnit unit) { super(dist, unit); }
            public Rel(final double value, final ElectricalPotentialUnit unit) { super(value, unit); }
            public ElectricalPotential.Rel draw() { return new ElectricalPotential.Rel(getDistribution().draw(), getUnit()); }
        }
        
        public static class Abs extends DistContinuousDoubleScalar<ElectricalPotentialUnit> implements Absolute
        {
            public Abs(final DistContinuous dist, final ElectricalPotentialUnit unit) { super(dist, unit); }
            public Abs(final double value, final ElectricalPotentialUnit unit) { super(value, unit); }
            public ElectricalPotential.Abs draw() { return new ElectricalPotential.Abs(getDistribution().draw(), getUnit()); }
        }
    }

    abstract class ElectricalPotentialDiscreteDist extends DistDiscreteDoubleScalar<ElectricalPotentialUnit>
    {
        protected ElectricalPotentialDiscreteDist(final DistDiscrete dist, final ElectricalPotentialUnit unit) { super(dist, unit); }
        protected ElectricalPotentialDiscreteDist(final long value, final ElectricalPotentialUnit unit) { super(value, unit); }
        
        public static class Rel extends DistDiscreteDoubleScalar<ElectricalPotentialUnit> implements Relative
        {
            public Rel(final DistDiscrete dist, final ElectricalPotentialUnit unit) { super(dist, unit); }
            public Rel(final long value, final ElectricalPotentialUnit unit) { super(value, unit); }
            public ElectricalPotential.Rel draw() { return new ElectricalPotential.Rel(getDistribution().draw(), getUnit()); }
        }
        
        public static class Abs extends DistDiscreteDoubleScalar<ElectricalPotentialUnit> implements Absolute
        {
            public Abs(final DistDiscrete dist, final ElectricalPotentialUnit unit) { super(dist, unit); }
            public Abs(final long value, final ElectricalPotentialUnit unit) { super(value, unit); }
            public ElectricalPotential.Abs draw() { return new ElectricalPotential.Abs(getDistribution().draw(), getUnit()); }
        }
    }

    /****************************************************************************************************************/
    /******************************************** ELECTRICAL_RESISTANCE *********************************************/ 
    /****************************************************************************************************************/

    abstract class ElectricalResistanceContinuousDist extends DistContinuousDoubleScalar<ElectricalResistanceUnit>
    {
        protected ElectricalResistanceContinuousDist(final DistContinuous dist, final ElectricalResistanceUnit unit) { super(dist, unit); }
        protected ElectricalResistanceContinuousDist(final double value, final ElectricalResistanceUnit unit) { super(value, unit); }
        
        public static class Rel extends DistContinuousDoubleScalar<ElectricalResistanceUnit> implements Relative
        {
            public Rel(final DistContinuous dist, final ElectricalResistanceUnit unit) { super(dist, unit); }
            public Rel(final double value, final ElectricalResistanceUnit unit) { super(value, unit); }
            public ElectricalResistance.Rel draw() { return new ElectricalResistance.Rel(getDistribution().draw(), getUnit()); }
        }
        
        public static class Abs extends DistContinuousDoubleScalar<ElectricalResistanceUnit> implements Absolute
        {
            public Abs(final DistContinuous dist, final ElectricalResistanceUnit unit) { super(dist, unit); }
            public Abs(final double value, final ElectricalResistanceUnit unit) { super(value, unit); }
            public ElectricalResistance.Abs draw() { return new ElectricalResistance.Abs(getDistribution().draw(), getUnit()); }
        }
    }

    abstract class ElectricalResistanceDiscreteDist extends DistDiscreteDoubleScalar<ElectricalResistanceUnit>
    {
        protected ElectricalResistanceDiscreteDist(final DistDiscrete dist, final ElectricalResistanceUnit unit) { super(dist, unit); }
        protected ElectricalResistanceDiscreteDist(final long value, final ElectricalResistanceUnit unit) { super(value, unit); }
        
        public static class Rel extends DistDiscreteDoubleScalar<ElectricalResistanceUnit> implements Relative
        {
            public Rel(final DistDiscrete dist, final ElectricalResistanceUnit unit) { super(dist, unit); }
            public Rel(final long value, final ElectricalResistanceUnit unit) { super(value, unit); }
            public ElectricalResistance.Rel draw() { return new ElectricalResistance.Rel(getDistribution().draw(), getUnit()); }
        }
        
        public static class Abs extends DistDiscreteDoubleScalar<ElectricalResistanceUnit> implements Absolute
        {
            public Abs(final DistDiscrete dist, final ElectricalResistanceUnit unit) { super(dist, unit); }
            public Abs(final long value, final ElectricalResistanceUnit unit) { super(value, unit); }
            public ElectricalResistance.Abs draw() { return new ElectricalResistance.Abs(getDistribution().draw(), getUnit()); }
        }
    }

    /****************************************************************************************************************/
    /*************************************************** ENERGY *****************************************************/ 
    /****************************************************************************************************************/

    abstract class EnergyContinuousDist extends DistContinuousDoubleScalar<EnergyUnit>
    {
        protected EnergyContinuousDist(final DistContinuous dist, final EnergyUnit unit) { super(dist, unit); }
        protected EnergyContinuousDist(final double value, final EnergyUnit unit) { super(value, unit); }
        
        public static class Rel extends DistContinuousDoubleScalar<EnergyUnit> implements Relative
        {
            public Rel(final DistContinuous dist, final EnergyUnit unit) { super(dist, unit); }
            public Rel(final double value, final EnergyUnit unit) { super(value, unit); }
            public Energy.Rel draw() { return new Energy.Rel(getDistribution().draw(), getUnit()); }
        }
        
        public static class Abs extends DistContinuousDoubleScalar<EnergyUnit> implements Absolute
        {
            public Abs(final DistContinuous dist, final EnergyUnit unit) { super(dist, unit); }
            public Abs(final double value, final EnergyUnit unit) { super(value, unit); }
            public Energy.Abs draw() { return new Energy.Abs(getDistribution().draw(), getUnit()); }
        }
    }

    abstract class EnergyDiscreteDist extends DistDiscreteDoubleScalar<EnergyUnit>
    {
        protected EnergyDiscreteDist(final DistDiscrete dist, final EnergyUnit unit) { super(dist, unit); }
        protected EnergyDiscreteDist(final long value, final EnergyUnit unit) { super(value, unit); }
        
        public static class Rel extends DistDiscreteDoubleScalar<EnergyUnit> implements Relative
        {
            public Rel(final DistDiscrete dist, final EnergyUnit unit) { super(dist, unit); }
            public Rel(final long value, final EnergyUnit unit) { super(value, unit); }
            public Energy.Rel draw() { return new Energy.Rel(getDistribution().draw(), getUnit()); }
        }
        
        public static class Abs extends DistDiscreteDoubleScalar<EnergyUnit> implements Absolute
        {
            public Abs(final DistDiscrete dist, final EnergyUnit unit) { super(dist, unit); }
            public Abs(final long value, final EnergyUnit unit) { super(value, unit); }
            public Energy.Abs draw() { return new Energy.Abs(getDistribution().draw(), getUnit()); }
        }
    }

    /****************************************************************************************************************/
    /************************************************** FLOW_MASS ***************************************************/ 
    /****************************************************************************************************************/

    abstract class FlowMassContinuousDist extends DistContinuousDoubleScalar<FlowMassUnit>
    {
        protected FlowMassContinuousDist(final DistContinuous dist, final FlowMassUnit unit) { super(dist, unit); }
        protected FlowMassContinuousDist(final double value, final FlowMassUnit unit) { super(value, unit); }
        
        public static class Rel extends DistContinuousDoubleScalar<FlowMassUnit> implements Relative
        {
            public Rel(final DistContinuous dist, final FlowMassUnit unit) { super(dist, unit); }
            public Rel(final double value, final FlowMassUnit unit) { super(value, unit); }
            public FlowMass.Rel draw() { return new FlowMass.Rel(getDistribution().draw(), getUnit()); }
        }
        
        public static class Abs extends DistContinuousDoubleScalar<FlowMassUnit> implements Absolute
        {
            public Abs(final DistContinuous dist, final FlowMassUnit unit) { super(dist, unit); }
            public Abs(final double value, final FlowMassUnit unit) { super(value, unit); }
            public FlowMass.Abs draw() { return new FlowMass.Abs(getDistribution().draw(), getUnit()); }
        }
    }

    abstract class FlowMassDiscreteDist extends DistDiscreteDoubleScalar<FlowMassUnit>
    {
        protected FlowMassDiscreteDist(final DistDiscrete dist, final FlowMassUnit unit) { super(dist, unit); }
        protected FlowMassDiscreteDist(final long value, final FlowMassUnit unit) { super(value, unit); }
        
        public static class Rel extends DistDiscreteDoubleScalar<FlowMassUnit> implements Relative
        {
            public Rel(final DistDiscrete dist, final FlowMassUnit unit) { super(dist, unit); }
            public Rel(final long value, final FlowMassUnit unit) { super(value, unit); }
            public FlowMass.Rel draw() { return new FlowMass.Rel(getDistribution().draw(), getUnit()); }
        }
        
        public static class Abs extends DistDiscreteDoubleScalar<FlowMassUnit> implements Absolute
        {
            public Abs(final DistDiscrete dist, final FlowMassUnit unit) { super(dist, unit); }
            public Abs(final long value, final FlowMassUnit unit) { super(value, unit); }
            public FlowMass.Abs draw() { return new FlowMass.Abs(getDistribution().draw(), getUnit()); }
        }
    }

    /****************************************************************************************************************/
    /************************************************* FLOW_VOLUME **************************************************/ 
    /****************************************************************************************************************/

    abstract class FlowVolumeContinuousDist extends DistContinuousDoubleScalar<FlowVolumeUnit>
    {
        protected FlowVolumeContinuousDist(final DistContinuous dist, final FlowVolumeUnit unit) { super(dist, unit); }
        protected FlowVolumeContinuousDist(final double value, final FlowVolumeUnit unit) { super(value, unit); }
        
        public static class Rel extends DistContinuousDoubleScalar<FlowVolumeUnit> implements Relative
        {
            public Rel(final DistContinuous dist, final FlowVolumeUnit unit) { super(dist, unit); }
            public Rel(final double value, final FlowVolumeUnit unit) { super(value, unit); }
            public FlowVolume.Rel draw() { return new FlowVolume.Rel(getDistribution().draw(), getUnit()); }
        }
        
        public static class Abs extends DistContinuousDoubleScalar<FlowVolumeUnit> implements Absolute
        {
            public Abs(final DistContinuous dist, final FlowVolumeUnit unit) { super(dist, unit); }
            public Abs(final double value, final FlowVolumeUnit unit) { super(value, unit); }
            public FlowVolume.Abs draw() { return new FlowVolume.Abs(getDistribution().draw(), getUnit()); }
        }
    }

    abstract class FlowVolumeDiscreteDist extends DistDiscreteDoubleScalar<FlowVolumeUnit>
    {
        protected FlowVolumeDiscreteDist(final DistDiscrete dist, final FlowVolumeUnit unit) { super(dist, unit); }
        protected FlowVolumeDiscreteDist(final long value, final FlowVolumeUnit unit) { super(value, unit); }
        
        public static class Rel extends DistDiscreteDoubleScalar<FlowVolumeUnit> implements Relative
        {
            public Rel(final DistDiscrete dist, final FlowVolumeUnit unit) { super(dist, unit); }
            public Rel(final long value, final FlowVolumeUnit unit) { super(value, unit); }
            public FlowVolume.Rel draw() { return new FlowVolume.Rel(getDistribution().draw(), getUnit()); }
        }
        
        public static class Abs extends DistDiscreteDoubleScalar<FlowVolumeUnit> implements Absolute
        {
            public Abs(final DistDiscrete dist, final FlowVolumeUnit unit) { super(dist, unit); }
            public Abs(final long value, final FlowVolumeUnit unit) { super(value, unit); }
            public FlowVolume.Abs draw() { return new FlowVolume.Abs(getDistribution().draw(), getUnit()); }
        }
    }

    /****************************************************************************************************************/
    /**************************************************** FORCE *****************************************************/ 
    /****************************************************************************************************************/

    abstract class ForceContinuousDist extends DistContinuousDoubleScalar<ForceUnit>
    {
        protected ForceContinuousDist(final DistContinuous dist, final ForceUnit unit) { super(dist, unit); }
        protected ForceContinuousDist(final double value, final ForceUnit unit) { super(value, unit); }
        
        public static class Rel extends DistContinuousDoubleScalar<ForceUnit> implements Relative
        {
            public Rel(final DistContinuous dist, final ForceUnit unit) { super(dist, unit); }
            public Rel(final double value, final ForceUnit unit) { super(value, unit); }
            public Force.Rel draw() { return new Force.Rel(getDistribution().draw(), getUnit()); }
        }
        
        public static class Abs extends DistContinuousDoubleScalar<ForceUnit> implements Absolute
        {
            public Abs(final DistContinuous dist, final ForceUnit unit) { super(dist, unit); }
            public Abs(final double value, final ForceUnit unit) { super(value, unit); }
            public Force.Abs draw() { return new Force.Abs(getDistribution().draw(), getUnit()); }
        }
    }

    abstract class ForceDiscreteDist extends DistDiscreteDoubleScalar<ForceUnit>
    {
        protected ForceDiscreteDist(final DistDiscrete dist, final ForceUnit unit) { super(dist, unit); }
        protected ForceDiscreteDist(final long value, final ForceUnit unit) { super(value, unit); }
        
        public static class Rel extends DistDiscreteDoubleScalar<ForceUnit> implements Relative
        {
            public Rel(final DistDiscrete dist, final ForceUnit unit) { super(dist, unit); }
            public Rel(final long value, final ForceUnit unit) { super(value, unit); }
            public Force.Rel draw() { return new Force.Rel(getDistribution().draw(), getUnit()); }
        }
        
        public static class Abs extends DistDiscreteDoubleScalar<ForceUnit> implements Absolute
        {
            public Abs(final DistDiscrete dist, final ForceUnit unit) { super(dist, unit); }
            public Abs(final long value, final ForceUnit unit) { super(value, unit); }
            public Force.Abs draw() { return new Force.Abs(getDistribution().draw(), getUnit()); }
        }
    }

    /****************************************************************************************************************/
    /************************************************** FREQUENCY ***************************************************/ 
    /****************************************************************************************************************/

    abstract class FrequencyContinuousDist extends DistContinuousDoubleScalar<FrequencyUnit>
    {
        protected FrequencyContinuousDist(final DistContinuous dist, final FrequencyUnit unit) { super(dist, unit); }
        protected FrequencyContinuousDist(final double value, final FrequencyUnit unit) { super(value, unit); }
        
        public static class Rel extends DistContinuousDoubleScalar<FrequencyUnit> implements Relative
        {
            public Rel(final DistContinuous dist, final FrequencyUnit unit) { super(dist, unit); }
            public Rel(final double value, final FrequencyUnit unit) { super(value, unit); }
            public Frequency.Rel draw() { return new Frequency.Rel(getDistribution().draw(), getUnit()); }
        }
        
        public static class Abs extends DistContinuousDoubleScalar<FrequencyUnit> implements Absolute
        {
            public Abs(final DistContinuous dist, final FrequencyUnit unit) { super(dist, unit); }
            public Abs(final double value, final FrequencyUnit unit) { super(value, unit); }
            public Frequency.Abs draw() { return new Frequency.Abs(getDistribution().draw(), getUnit()); }
        }
    }

    abstract class FrequencyDiscreteDist extends DistDiscreteDoubleScalar<FrequencyUnit>
    {
        protected FrequencyDiscreteDist(final DistDiscrete dist, final FrequencyUnit unit) { super(dist, unit); }
        protected FrequencyDiscreteDist(final long value, final FrequencyUnit unit) { super(value, unit); }
        
        public static class Rel extends DistDiscreteDoubleScalar<FrequencyUnit> implements Relative
        {
            public Rel(final DistDiscrete dist, final FrequencyUnit unit) { super(dist, unit); }
            public Rel(final long value, final FrequencyUnit unit) { super(value, unit); }
            public Frequency.Rel draw() { return new Frequency.Rel(getDistribution().draw(), getUnit()); }
        }
        
        public static class Abs extends DistDiscreteDoubleScalar<FrequencyUnit> implements Absolute
        {
            public Abs(final DistDiscrete dist, final FrequencyUnit unit) { super(dist, unit); }
            public Abs(final long value, final FrequencyUnit unit) { super(value, unit); }
            public Frequency.Abs draw() { return new Frequency.Abs(getDistribution().draw(), getUnit()); }
        }
    }

    /****************************************************************************************************************/
    /**************************************************** LENGTH ****************************************************/ 
    /****************************************************************************************************************/

    abstract class LengthContinuousDist extends DistContinuousDoubleScalar<LengthUnit>
    {
        protected LengthContinuousDist(final DistContinuous dist, final LengthUnit unit) { super(dist, unit); }
        protected LengthContinuousDist(final double value, final LengthUnit unit) { super(value, unit); }
        
        public static class Rel extends DistContinuousDoubleScalar<LengthUnit> implements Relative
        {
            public Rel(final DistContinuous dist, final LengthUnit unit) { super(dist, unit); }
            public Rel(final double value, final LengthUnit unit) { super(value, unit); }
            public Length.Rel draw() { return new Length.Rel(getDistribution().draw(), getUnit()); }
        }
        
        public static class Abs extends DistContinuousDoubleScalar<LengthUnit> implements Absolute
        {
            public Abs(final DistContinuous dist, final LengthUnit unit) { super(dist, unit); }
            public Abs(final double value, final LengthUnit unit) { super(value, unit); }
            public Length.Abs draw() { return new Length.Abs(getDistribution().draw(), getUnit()); }
        }
    }

    abstract class LengthDiscreteDist extends DistDiscreteDoubleScalar<LengthUnit>
    {
        protected LengthDiscreteDist(final DistDiscrete dist, final LengthUnit unit) { super(dist, unit); }
        protected LengthDiscreteDist(final long value, final LengthUnit unit) { super(value, unit); }
        
        public static class Rel extends DistDiscreteDoubleScalar<LengthUnit> implements Relative
        {
            public Rel(final DistDiscrete dist, final LengthUnit unit) { super(dist, unit); }
            public Rel(final long value, final LengthUnit unit) { super(value, unit); }
            public Length.Rel draw() { return new Length.Rel(getDistribution().draw(), getUnit()); }
        }
        
        public static class Abs extends DistDiscreteDoubleScalar<LengthUnit> implements Absolute
        {
            public Abs(final DistDiscrete dist, final LengthUnit unit) { super(dist, unit); }
            public Abs(final long value, final LengthUnit unit) { super(value, unit); }
            public Length.Abs draw() { return new Length.Abs(getDistribution().draw(), getUnit()); }
        }
    }

    /****************************************************************************************************************/
    /************************************************ LINEAR_DENSITY ************************************************/ 
    /****************************************************************************************************************/

    abstract class LinearDensityContinuousDist extends DistContinuousDoubleScalar<LinearDensityUnit>
    {
        protected LinearDensityContinuousDist(final DistContinuous dist, final LinearDensityUnit unit) { super(dist, unit); }
        protected LinearDensityContinuousDist(final double value, final LinearDensityUnit unit) { super(value, unit); }
        
        public static class Rel extends DistContinuousDoubleScalar<LinearDensityUnit> implements Relative
        {
            public Rel(final DistContinuous dist, final LinearDensityUnit unit) { super(dist, unit); }
            public Rel(final double value, final LinearDensityUnit unit) { super(value, unit); }
            public LinearDensity.Rel draw() { return new LinearDensity.Rel(getDistribution().draw(), getUnit()); }
        }
        
        public static class Abs extends DistContinuousDoubleScalar<LinearDensityUnit> implements Absolute
        {
            public Abs(final DistContinuous dist, final LinearDensityUnit unit) { super(dist, unit); }
            public Abs(final double value, final LinearDensityUnit unit) { super(value, unit); }
            public LinearDensity.Abs draw() { return new LinearDensity.Abs(getDistribution().draw(), getUnit()); }
        }
    }

    abstract class LinearDensityDiscreteDist extends DistDiscreteDoubleScalar<LinearDensityUnit>
    {
        protected LinearDensityDiscreteDist(final DistDiscrete dist, final LinearDensityUnit unit) { super(dist, unit); }
        protected LinearDensityDiscreteDist(final long value, final LinearDensityUnit unit) { super(value, unit); }
        
        public static class Rel extends DistDiscreteDoubleScalar<LinearDensityUnit> implements Relative
        {
            public Rel(final DistDiscrete dist, final LinearDensityUnit unit) { super(dist, unit); }
            public Rel(final long value, final LinearDensityUnit unit) { super(value, unit); }
            public LinearDensity.Rel draw() { return new LinearDensity.Rel(getDistribution().draw(), getUnit()); }
        }
        
        public static class Abs extends DistDiscreteDoubleScalar<LinearDensityUnit> implements Absolute
        {
            public Abs(final DistDiscrete dist, final LinearDensityUnit unit) { super(dist, unit); }
            public Abs(final long value, final LinearDensityUnit unit) { super(value, unit); }
            public LinearDensity.Abs draw() { return new LinearDensity.Abs(getDistribution().draw(), getUnit()); }
        }
    }

    /****************************************************************************************************************/
    /***************************************************** MASS *****************************************************/ 
    /****************************************************************************************************************/

    abstract class MassContinuousDist extends DistContinuousDoubleScalar<MassUnit>
    {
        protected MassContinuousDist(final DistContinuous dist, final MassUnit unit) { super(dist, unit); }
        protected MassContinuousDist(final double value, final MassUnit unit) { super(value, unit); }
        
        public static class Rel extends DistContinuousDoubleScalar<MassUnit> implements Relative
        {
            public Rel(final DistContinuous dist, final MassUnit unit) { super(dist, unit); }
            public Rel(final double value, final MassUnit unit) { super(value, unit); }
            public Mass.Rel draw() { return new Mass.Rel(getDistribution().draw(), getUnit()); }
        }
        
        public static class Abs extends DistContinuousDoubleScalar<MassUnit> implements Absolute
        {
            public Abs(final DistContinuous dist, final MassUnit unit) { super(dist, unit); }
            public Abs(final double value, final MassUnit unit) { super(value, unit); }
            public Mass.Abs draw() { return new Mass.Abs(getDistribution().draw(), getUnit()); }
        }
    }

    abstract class MassDiscreteDist extends DistDiscreteDoubleScalar<MassUnit>
    {
        protected MassDiscreteDist(final DistDiscrete dist, final MassUnit unit) { super(dist, unit); }
        protected MassDiscreteDist(final long value, final MassUnit unit) { super(value, unit); }
        
        public static class Rel extends DistDiscreteDoubleScalar<MassUnit> implements Relative
        {
            public Rel(final DistDiscrete dist, final MassUnit unit) { super(dist, unit); }
            public Rel(final long value, final MassUnit unit) { super(value, unit); }
            public Mass.Rel draw() { return new Mass.Rel(getDistribution().draw(), getUnit()); }
        }
        
        public static class Abs extends DistDiscreteDoubleScalar<MassUnit> implements Absolute
        {
            public Abs(final DistDiscrete dist, final MassUnit unit) { super(dist, unit); }
            public Abs(final long value, final MassUnit unit) { super(value, unit); }
            public Mass.Abs draw() { return new Mass.Abs(getDistribution().draw(), getUnit()); }
        }
    }

    /****************************************************************************************************************/
    /**************************************************** POWER *****************************************************/ 
    /****************************************************************************************************************/

    abstract class PowerContinuousDist extends DistContinuousDoubleScalar<PowerUnit>
    {
        protected PowerContinuousDist(final DistContinuous dist, final PowerUnit unit) { super(dist, unit); }
        protected PowerContinuousDist(final double value, final PowerUnit unit) { super(value, unit); }
        
        public static class Rel extends DistContinuousDoubleScalar<PowerUnit> implements Relative
        {
            public Rel(final DistContinuous dist, final PowerUnit unit) { super(dist, unit); }
            public Rel(final double value, final PowerUnit unit) { super(value, unit); }
            public Power.Rel draw() { return new Power.Rel(getDistribution().draw(), getUnit()); }
        }
        
        public static class Abs extends DistContinuousDoubleScalar<PowerUnit> implements Absolute
        {
            public Abs(final DistContinuous dist, final PowerUnit unit) { super(dist, unit); }
            public Abs(final double value, final PowerUnit unit) { super(value, unit); }
            public Power.Abs draw() { return new Power.Abs(getDistribution().draw(), getUnit()); }
        }
    }

    abstract class PowerDiscreteDist extends DistDiscreteDoubleScalar<PowerUnit>
    {
        protected PowerDiscreteDist(final DistDiscrete dist, final PowerUnit unit) { super(dist, unit); }
        protected PowerDiscreteDist(final long value, final PowerUnit unit) { super(value, unit); }
        
        public static class Rel extends DistDiscreteDoubleScalar<PowerUnit> implements Relative
        {
            public Rel(final DistDiscrete dist, final PowerUnit unit) { super(dist, unit); }
            public Rel(final long value, final PowerUnit unit) { super(value, unit); }
            public Power.Rel draw() { return new Power.Rel(getDistribution().draw(), getUnit()); }
        }
        
        public static class Abs extends DistDiscreteDoubleScalar<PowerUnit> implements Absolute
        {
            public Abs(final DistDiscrete dist, final PowerUnit unit) { super(dist, unit); }
            public Abs(final long value, final PowerUnit unit) { super(value, unit); }
            public Power.Abs draw() { return new Power.Abs(getDistribution().draw(), getUnit()); }
        }
    }

    /****************************************************************************************************************/
    /************************************************** PRESSURE ****************************************************/ 
    /****************************************************************************************************************/

    abstract class PressureContinuousDist extends DistContinuousDoubleScalar<PressureUnit>
    {
        protected PressureContinuousDist(final DistContinuous dist, final PressureUnit unit) { super(dist, unit); }
        protected PressureContinuousDist(final double value, final PressureUnit unit) { super(value, unit); }
        
        public static class Rel extends DistContinuousDoubleScalar<PressureUnit> implements Relative
        {
            public Rel(final DistContinuous dist, final PressureUnit unit) { super(dist, unit); }
            public Rel(final double value, final PressureUnit unit) { super(value, unit); }
            public Pressure.Rel draw() { return new Pressure.Rel(getDistribution().draw(), getUnit()); }
        }
        
        public static class Abs extends DistContinuousDoubleScalar<PressureUnit> implements Absolute
        {
            public Abs(final DistContinuous dist, final PressureUnit unit) { super(dist, unit); }
            public Abs(final double value, final PressureUnit unit) { super(value, unit); }
            public Pressure.Abs draw() { return new Pressure.Abs(getDistribution().draw(), getUnit()); }
        }
    }

    abstract class PressureDiscreteDist extends DistDiscreteDoubleScalar<PressureUnit>
    {
        protected PressureDiscreteDist(final DistDiscrete dist, final PressureUnit unit) { super(dist, unit); }
        protected PressureDiscreteDist(final long value, final PressureUnit unit) { super(value, unit); }
        
        public static class Rel extends DistDiscreteDoubleScalar<PressureUnit> implements Relative
        {
            public Rel(final DistDiscrete dist, final PressureUnit unit) { super(dist, unit); }
            public Rel(final long value, final PressureUnit unit) { super(value, unit); }
            public Pressure.Rel draw() { return new Pressure.Rel(getDistribution().draw(), getUnit()); }
        }
        
        public static class Abs extends DistDiscreteDoubleScalar<PressureUnit> implements Absolute
        {
            public Abs(final DistDiscrete dist, final PressureUnit unit) { super(dist, unit); }
            public Abs(final long value, final PressureUnit unit) { super(value, unit); }
            public Pressure.Abs draw() { return new Pressure.Abs(getDistribution().draw(), getUnit()); }
        }
    }

    /****************************************************************************************************************/
    /***************************************************** SPEED ****************************************************/ 
    /****************************************************************************************************************/

    abstract class SpeedContinuousDist extends DistContinuousDoubleScalar<SpeedUnit>
    {
        protected SpeedContinuousDist(final DistContinuous dist, final SpeedUnit unit) { super(dist, unit); }
        protected SpeedContinuousDist(final double value, final SpeedUnit unit) { super(value, unit); }
        
        public static class Rel extends DistContinuousDoubleScalar<SpeedUnit> implements Relative
        {
            public Rel(final DistContinuous dist, final SpeedUnit unit) { super(dist, unit); }
            public Rel(final double value, final SpeedUnit unit) { super(value, unit); }
            public Speed.Rel draw() { return new Speed.Rel(getDistribution().draw(), getUnit()); }
        }
        
        public static class Abs extends DistContinuousDoubleScalar<SpeedUnit> implements Absolute
        {
            public Abs(final DistContinuous dist, final SpeedUnit unit) { super(dist, unit); }
            public Abs(final double value, final SpeedUnit unit) { super(value, unit); }
            public Speed.Abs draw() { return new Speed.Abs(getDistribution().draw(), getUnit()); }
        }
    }

    abstract class SpeedDiscreteDist extends DistDiscreteDoubleScalar<SpeedUnit>
    {
        protected SpeedDiscreteDist(final DistDiscrete dist, final SpeedUnit unit) { super(dist, unit); }
        protected SpeedDiscreteDist(final long value, final SpeedUnit unit) { super(value, unit); }
        
        public static class Rel extends DistDiscreteDoubleScalar<SpeedUnit> implements Relative
        {
            public Rel(final DistDiscrete dist, final SpeedUnit unit) { super(dist, unit); }
            public Rel(final long value, final SpeedUnit unit) { super(value, unit); }
            public Speed.Rel draw() { return new Speed.Rel(getDistribution().draw(), getUnit()); }
        }
        
        public static class Abs extends DistDiscreteDoubleScalar<SpeedUnit> implements Absolute
        {
            public Abs(final DistDiscrete dist, final SpeedUnit unit) { super(dist, unit); }
            public Abs(final long value, final SpeedUnit unit) { super(value, unit); }
            public Speed.Abs draw() { return new Speed.Abs(getDistribution().draw(), getUnit()); }
        }
    }

    /****************************************************************************************************************/
    /************************************************* TEMPERATURE **************************************************/ 
    /****************************************************************************************************************/

    abstract class TemperatureContinuousDist extends DistContinuousDoubleScalar<TemperatureUnit>
    {
        protected TemperatureContinuousDist(final DistContinuous dist, final TemperatureUnit unit) { super(dist, unit); }
        protected TemperatureContinuousDist(final double value, final TemperatureUnit unit) { super(value, unit); }
        
        public static class Rel extends DistContinuousDoubleScalar<TemperatureUnit> implements Relative
        {
            public Rel(final DistContinuous dist, final TemperatureUnit unit) { super(dist, unit); }
            public Rel(final double value, final TemperatureUnit unit) { super(value, unit); }
            public Temperature.Rel draw() { return new Temperature.Rel(getDistribution().draw(), getUnit()); }
        }
        
        public static class Abs extends DistContinuousDoubleScalar<TemperatureUnit> implements Absolute
        {
            public Abs(final DistContinuous dist, final TemperatureUnit unit) { super(dist, unit); }
            public Abs(final double value, final TemperatureUnit unit) { super(value, unit); }
            public Temperature.Abs draw() { return new Temperature.Abs(getDistribution().draw(), getUnit()); }
        }
    }

    abstract class TemperatureDiscreteDist extends DistDiscreteDoubleScalar<TemperatureUnit>
    {
        protected TemperatureDiscreteDist(final DistDiscrete dist, final TemperatureUnit unit) { super(dist, unit); }
        protected TemperatureDiscreteDist(final long value, final TemperatureUnit unit) { super(value, unit); }
        
        public static class Rel extends DistDiscreteDoubleScalar<TemperatureUnit> implements Relative
        {
            public Rel(final DistDiscrete dist, final TemperatureUnit unit) { super(dist, unit); }
            public Rel(final long value, final TemperatureUnit unit) { super(value, unit); }
            public Temperature.Rel draw() { return new Temperature.Rel(getDistribution().draw(), getUnit()); }
        }
        
        public static class Abs extends DistDiscreteDoubleScalar<TemperatureUnit> implements Absolute
        {
            public Abs(final DistDiscrete dist, final TemperatureUnit unit) { super(dist, unit); }
            public Abs(final long value, final TemperatureUnit unit) { super(value, unit); }
            public Temperature.Abs draw() { return new Temperature.Abs(getDistribution().draw(), getUnit()); }
        }
    }

    /****************************************************************************************************************/
    /***************************************************** TIME *****************************************************/ 
    /****************************************************************************************************************/

    abstract class TimeContinuousDist extends DistContinuousDoubleScalar<TimeUnit>
    {
        protected TimeContinuousDist(final DistContinuous dist, final TimeUnit unit) { super(dist, unit); }
        protected TimeContinuousDist(final double value, final TimeUnit unit) { super(value, unit); }
        
        public static class Rel extends DistContinuousDoubleScalar<TimeUnit> implements Relative
        {
            public Rel(final DistContinuous dist, final TimeUnit unit) { super(dist, unit); }
            public Rel(final double value, final TimeUnit unit) { super(value, unit); }
            public Time.Rel draw() { return new Time.Rel(getDistribution().draw(), getUnit()); }
        }
        
        public static class Abs extends DistContinuousDoubleScalar<TimeUnit> implements Absolute
        {
            public Abs(final DistContinuous dist, final TimeUnit unit) { super(dist, unit); }
            public Abs(final double value, final TimeUnit unit) { super(value, unit); }
            public Time.Abs draw() { return new Time.Abs(getDistribution().draw(), getUnit()); }
        }
    }

    abstract class TimeDiscreteDist extends DistDiscreteDoubleScalar<TimeUnit>
    {
        protected TimeDiscreteDist(final DistDiscrete dist, final TimeUnit unit) { super(dist, unit); }
        protected TimeDiscreteDist(final long value, final TimeUnit unit) { super(value, unit); }
        
        public static class Rel extends DistDiscreteDoubleScalar<TimeUnit> implements Relative
        {
            public Rel(final DistDiscrete dist, final TimeUnit unit) { super(dist, unit); }
            public Rel(final long value, final TimeUnit unit) { super(value, unit); }
            public Time.Rel draw() { return new Time.Rel(getDistribution().draw(), getUnit()); }
        }
        
        public static class Abs extends DistDiscreteDoubleScalar<TimeUnit> implements Absolute
        {
            public Abs(final DistDiscrete dist, final TimeUnit unit) { super(dist, unit); }
            public Abs(final long value, final TimeUnit unit) { super(value, unit); }
            public Time.Abs draw() { return new Time.Abs(getDistribution().draw(), getUnit()); }
        }
    }

    /****************************************************************************************************************/
    /*************************************************** TORQUE *****************************************************/ 
    /****************************************************************************************************************/

    abstract class TorqueContinuousDist extends DistContinuousDoubleScalar<TorqueUnit>
    {
        protected TorqueContinuousDist(final DistContinuous dist, final TorqueUnit unit) { super(dist, unit); }
        protected TorqueContinuousDist(final double value, final TorqueUnit unit) { super(value, unit); }
        
        public static class Rel extends DistContinuousDoubleScalar<TorqueUnit> implements Relative
        {
            public Rel(final DistContinuous dist, final TorqueUnit unit) { super(dist, unit); }
            public Rel(final double value, final TorqueUnit unit) { super(value, unit); }
            public Torque.Rel draw() { return new Torque.Rel(getDistribution().draw(), getUnit()); }
        }
        
        public static class Abs extends DistContinuousDoubleScalar<TorqueUnit> implements Absolute
        {
            public Abs(final DistContinuous dist, final TorqueUnit unit) { super(dist, unit); }
            public Abs(final double value, final TorqueUnit unit) { super(value, unit); }
            public Torque.Abs draw() { return new Torque.Abs(getDistribution().draw(), getUnit()); }
        }
    }

    abstract class TorqueDiscreteDist extends DistDiscreteDoubleScalar<TorqueUnit>
    {
        protected TorqueDiscreteDist(final DistDiscrete dist, final TorqueUnit unit) { super(dist, unit); }
        protected TorqueDiscreteDist(final long value, final TorqueUnit unit) { super(value, unit); }
        
        public static class Rel extends DistDiscreteDoubleScalar<TorqueUnit> implements Relative
        {
            public Rel(final DistDiscrete dist, final TorqueUnit unit) { super(dist, unit); }
            public Rel(final long value, final TorqueUnit unit) { super(value, unit); }
            public Torque.Rel draw() { return new Torque.Rel(getDistribution().draw(), getUnit()); }
        }
        
        public static class Abs extends DistDiscreteDoubleScalar<TorqueUnit> implements Absolute
        {
            public Abs(final DistDiscrete dist, final TorqueUnit unit) { super(dist, unit); }
            public Abs(final long value, final TorqueUnit unit) { super(value, unit); }
            public Torque.Abs draw() { return new Torque.Abs(getDistribution().draw(), getUnit()); }
        }
    }

    /****************************************************************************************************************/
    /*************************************************** VOLUME *****************************************************/ 
    /****************************************************************************************************************/
 
    abstract class VolumeContinuousDist extends DistContinuousDoubleScalar<VolumeUnit>
    {
        protected VolumeContinuousDist(final DistContinuous dist, final VolumeUnit unit) { super(dist, unit); }
        protected VolumeContinuousDist(final double value, final VolumeUnit unit) { super(value, unit); }
        
        public static class Rel extends DistContinuousDoubleScalar<VolumeUnit> implements Relative
        {
            public Rel(final DistContinuous dist, final VolumeUnit unit) { super(dist, unit); }
            public Rel(final double value, final VolumeUnit unit) { super(value, unit); }
            public Volume.Rel draw() { return new Volume.Rel(getDistribution().draw(), getUnit()); }
        }
        
        public static class Abs extends DistContinuousDoubleScalar<VolumeUnit> implements Absolute
        {
            public Abs(final DistContinuous dist, final VolumeUnit unit) { super(dist, unit); }
            public Abs(final double value, final VolumeUnit unit) { super(value, unit); }
            public Volume.Abs draw() { return new Volume.Abs(getDistribution().draw(), getUnit()); }
        }
    }

    abstract class VolumeDiscreteDist extends DistDiscreteDoubleScalar<VolumeUnit>
    {
        protected VolumeDiscreteDist(final DistDiscrete dist, final VolumeUnit unit) { super(dist, unit); }
        protected VolumeDiscreteDist(final long value, final VolumeUnit unit) { super(value, unit); }
        
        public static class Rel extends DistDiscreteDoubleScalar<VolumeUnit> implements Relative
        {
            public Rel(final DistDiscrete dist, final VolumeUnit unit) { super(dist, unit); }
            public Rel(final long value, final VolumeUnit unit) { super(value, unit); }
            public Volume.Rel draw() { return new Volume.Rel(getDistribution().draw(), getUnit()); }
        }
        
        public static class Abs extends DistDiscreteDoubleScalar<VolumeUnit> implements Absolute
        {
            public Abs(final DistDiscrete dist, final VolumeUnit unit) { super(dist, unit); }
            public Abs(final long value, final VolumeUnit unit) { super(value, unit); }
            public Volume.Abs draw() { return new Volume.Abs(getDistribution().draw(), getUnit()); }
        }
    }

    /****************************************************************************************************************/
    /*********************************************** DIMENSIONLESS **************************************************/ 
    /****************************************************************************************************************/
    
    abstract class DimensionlessContinuousDist extends DistContinuousDoubleScalar<DimensionlessUnit>
    {
        protected DimensionlessContinuousDist(final DistContinuous dist, final DimensionlessUnit unit) { super(dist, unit); }
        protected DimensionlessContinuousDist(final double value, final DimensionlessUnit unit) { super(value, unit); }
        
        public static class Rel extends DistContinuousDoubleScalar<DimensionlessUnit> implements Relative
        {
            public Rel(final DistContinuous dist, final DimensionlessUnit unit) { super(dist, unit); }
            public Rel(final double value, final DimensionlessUnit unit) { super(value, unit); }
            public Dimensionless.Rel draw() { return new Dimensionless.Rel(getDistribution().draw(), getUnit()); }
        }
        
        public static class Abs extends DistContinuousDoubleScalar<DimensionlessUnit> implements Absolute
        {
            public Abs(final DistContinuous dist, final DimensionlessUnit unit) { super(dist, unit); }
            public Abs(final double value, final DimensionlessUnit unit) { super(value, unit); }
            public Dimensionless.Abs draw() { return new Dimensionless.Abs(getDistribution().draw(), getUnit()); }
        }
    }

    abstract class DimensionlessDiscreteDist extends DistDiscreteDoubleScalar<DimensionlessUnit>
    {
        protected DimensionlessDiscreteDist(final DistDiscrete dist, final DimensionlessUnit unit) { super(dist, unit); }
        protected DimensionlessDiscreteDist(final long value, final DimensionlessUnit unit) { super(value, unit); }
        
        public static class Rel extends DistDiscreteDoubleScalar<DimensionlessUnit> implements Relative
        {
            public Rel(final DistDiscrete dist, final DimensionlessUnit unit) { super(dist, unit); }
            public Rel(final long value, final DimensionlessUnit unit) { super(value, unit); }
            public Dimensionless.Rel draw() { return new Dimensionless.Rel(getDistribution().draw(), getUnit()); }
        }
        
        public static class Abs extends DistDiscreteDoubleScalar<DimensionlessUnit> implements Absolute
        {
            public Abs(final DistDiscrete dist, final DimensionlessUnit unit) { super(dist, unit); }
            public Abs(final long value, final DimensionlessUnit unit) { super(value, unit); }
            public Dimensionless.Abs draw() { return new Dimensionless.Abs(getDistribution().draw(), getUnit()); }
        }
    }

}
