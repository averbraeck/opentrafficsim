package org.opentrafficsim.core.value.vdouble.scalar;

import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.AnglePlaneUnit;
import org.opentrafficsim.core.unit.AngleSlopeUnit;
import org.opentrafficsim.core.unit.AngleSolidUnit;
import org.opentrafficsim.core.unit.AreaUnit;
import org.opentrafficsim.core.unit.DensityUnit;
import org.opentrafficsim.core.unit.ElectricalChargeUnit;
import org.opentrafficsim.core.unit.ElectricalCurrentUnit;
import org.opentrafficsim.core.unit.ElectricalPotentialUnit;
import org.opentrafficsim.core.unit.ElectricalResistanceUnit;
import org.opentrafficsim.core.unit.EnergyUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;

/**
 * Easy access
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Aug 28, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
@SuppressWarnings({"checkstyle:interfaceistype", "checkstyle:javadocmethod", "checkstyle:javadoctype",
    "checkstyle:javadocvariable", "checkstyle:linelength", "checkstyle:leftcurly", "checkstyle:rightcurly", "javadoc",
    "serial"})
public interface DOUBLE_SCALAR
{
    // @formatter:off
    
    /****************************************************************************************************************/
    /**************************************************** LENGTH ****************************************************/ 
    /****************************************************************************************************************/

    LengthUnit METER         = LengthUnit.METER;
    LengthUnit CENTIMETER    = LengthUnit.CENTIMETER;
    LengthUnit DECIMETER     = LengthUnit.DECIMETER;
    LengthUnit DEKAMETER     = LengthUnit.DEKAMETER;
    LengthUnit FOOT          = LengthUnit.FOOT;
    LengthUnit HECTOMETER    = LengthUnit.HECTOMETER;
    LengthUnit INCH          = LengthUnit.INCH;
    LengthUnit KILOMETER     = LengthUnit.KILOMETER;
    LengthUnit MILE          = LengthUnit.MILE;
    LengthUnit MILLIMETER    = LengthUnit.MILLIMETER;
    LengthUnit NAUTICAL_MILE = LengthUnit.NAUTICAL_MILE;
    LengthUnit YARD          = LengthUnit.YARD;
    LengthUnit M             = LengthUnit.METER;
    LengthUnit CM            = LengthUnit.CENTIMETER;
    LengthUnit DM            = LengthUnit.DECIMETER;
    LengthUnit DAM           = LengthUnit.DEKAMETER;
    LengthUnit FT            = LengthUnit.FOOT;
    LengthUnit HM            = LengthUnit.HECTOMETER;
    LengthUnit IN            = LengthUnit.INCH;
    LengthUnit KM            = LengthUnit.KILOMETER;
    LengthUnit MI            = LengthUnit.MILE;
    LengthUnit MM            = LengthUnit.MILLIMETER;
    LengthUnit NM            = LengthUnit.NAUTICAL_MILE;
    LengthUnit YD            = LengthUnit.YARD;
    
    abstract class Length extends DoubleScalar<LengthUnit>
    {
        protected Length(final LengthUnit unit) { super(unit); }
        
        public static class Rel extends DoubleScalar.Rel<LengthUnit>
        {
            public Rel(final double value, final LengthUnit unit) { super(value, unit); }
            public Rel(final org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel<LengthUnit> value) { super(value); }
            public Rel(final org.opentrafficsim.core.value.vdouble.scalar.MutableDoubleScalar.Rel<LengthUnit> value) { super(value); }
        }
        
        public static class Abs extends DoubleScalar.Abs<LengthUnit>
        {
            public Abs(final double value, final LengthUnit unit) { super(value, unit); }
            public Abs(final org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs<LengthUnit> value) { super(value); }
            public Abs(final org.opentrafficsim.core.value.vdouble.scalar.MutableDoubleScalar.Abs<LengthUnit> value) { super(value); }
        }
    }
    
    /****************************************************************************************************************/
    /***************************************************** TIME *****************************************************/ 
    /****************************************************************************************************************/

    TimeUnit SECOND      = TimeUnit.SECOND;
    TimeUnit DAY         = TimeUnit.DAY;
    TimeUnit HOUR        = TimeUnit.HOUR;
    TimeUnit MILLISECOND = TimeUnit.MILLISECOND;
    TimeUnit MINUTE      = TimeUnit.MINUTE;
    TimeUnit WEEK        = TimeUnit.WEEK;
    TimeUnit S           = TimeUnit.SECOND;
    TimeUnit HR          = TimeUnit.HOUR;
    TimeUnit MS          = TimeUnit.MILLISECOND;
    TimeUnit MIN         = TimeUnit.MINUTE;
    TimeUnit WK          = TimeUnit.WEEK;
    
    abstract class Time extends DoubleScalar<TimeUnit>
    {
        protected Time(final TimeUnit unit) { super(unit); }
        
        public static class Rel extends DoubleScalar.Rel<TimeUnit>
        {
            public Rel(final double value, final TimeUnit unit) { super(value, unit); }
            public Rel(final org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel<TimeUnit> value) { super(value); }
            public Rel(final org.opentrafficsim.core.value.vdouble.scalar.MutableDoubleScalar.Rel<TimeUnit> value) { super(value); }
        }
        
        public static class Abs extends DoubleScalar.Abs<TimeUnit>
        {
            public Abs(final double value, final TimeUnit unit) { super(value, unit); }
            public Abs(final org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs<TimeUnit> value) { super(value); }
            public Abs(final org.opentrafficsim.core.value.vdouble.scalar.MutableDoubleScalar.Abs<TimeUnit> value) { super(value); }
        }
    }
    
    /****************************************************************************************************************/
    /***************************************************** SPEED ****************************************************/ 
    /****************************************************************************************************************/

    SpeedUnit METER_PER_SECOND = SpeedUnit.METER_PER_SECOND;
    SpeedUnit FOOT_PER_SECOND  = SpeedUnit.FOOT_PER_SECOND;
    SpeedUnit KM_PER_HOUR      = SpeedUnit.KM_PER_HOUR;
    SpeedUnit KNOT             = SpeedUnit.KNOT;
    SpeedUnit MILE_PER_HOUR    = SpeedUnit.MILE_PER_HOUR;
    SpeedUnit MPS              = SpeedUnit.METER_PER_SECOND;
    SpeedUnit FTPS             = SpeedUnit.FOOT_PER_SECOND;
    SpeedUnit KMPH             = SpeedUnit.KM_PER_HOUR;
    SpeedUnit MPH              = SpeedUnit.MILE_PER_HOUR;
    
    abstract class Speed extends DoubleScalar<SpeedUnit>
    {
        protected Speed(final SpeedUnit unit) { super(unit); }
        
        public static class Rel extends DoubleScalar.Rel<SpeedUnit>
        {
            public Rel(final double value, final SpeedUnit unit) { super(value, unit); }
            public Rel(final org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel<SpeedUnit> value) { super(value); }
            public Rel(final org.opentrafficsim.core.value.vdouble.scalar.MutableDoubleScalar.Rel<SpeedUnit> value) { super(value); }
        }
        
        public static class Abs extends DoubleScalar.Abs<SpeedUnit>
        {
            public Abs(final double value, final SpeedUnit unit) { super(value, unit); }
            public Abs(final org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs<SpeedUnit> value) { super(value); }
            public Abs(final org.opentrafficsim.core.value.vdouble.scalar.MutableDoubleScalar.Abs<SpeedUnit> value) { super(value); }
        }
    }

    /****************************************************************************************************************/
    /************************************************** ACCELERATION ************************************************/ 
    /****************************************************************************************************************/

    AccelerationUnit METER_PER_SECOND_2       = AccelerationUnit.METER_PER_SECOND_2;
    AccelerationUnit FOOT_PER_SECOND_2        = AccelerationUnit.FOOT_PER_SECOND_2;
    AccelerationUnit GAL                      = AccelerationUnit.GAL;
    AccelerationUnit INCH_PER_SECOND_2        = AccelerationUnit.INCH_PER_SECOND_2;
    AccelerationUnit KM_PER_HOUR_2            = AccelerationUnit.KM_PER_HOUR_2;
    AccelerationUnit KNOT_PER_SECOND          = AccelerationUnit.KNOT_PER_SECOND;
    AccelerationUnit MILE_PER_HOUR_2          = AccelerationUnit.MILE_PER_HOUR_2;
    AccelerationUnit MILE_PER_HOUR_PER_SECOND = AccelerationUnit.MILE_PER_HOUR_PER_SECOND;
    AccelerationUnit STANDARD_GRAVITY         = AccelerationUnit.STANDARD_GRAVITY;
    AccelerationUnit MPS_2                    = AccelerationUnit.METER_PER_SECOND_2;
    AccelerationUnit FTPS_2                   = AccelerationUnit.FOOT_PER_SECOND_2;
    AccelerationUnit INPS_2                   = AccelerationUnit.INCH_PER_SECOND_2;
    AccelerationUnit KMPH_2                   = AccelerationUnit.KM_PER_HOUR_2;
    AccelerationUnit MPH_2                    = AccelerationUnit.MILE_PER_HOUR_2;
    AccelerationUnit MPHPS                    = AccelerationUnit.MILE_PER_HOUR_PER_SECOND;
    AccelerationUnit G                        = AccelerationUnit.STANDARD_GRAVITY;
    
    abstract class Acceleration extends DoubleScalar<AccelerationUnit>
    {
        protected Acceleration(final AccelerationUnit unit) { super(unit); }
        
        public static class Rel extends DoubleScalar.Rel<AccelerationUnit>
        {
            public Rel(final double value, final AccelerationUnit unit) { super(value, unit); }
            public Rel(final org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel<AccelerationUnit> value) { super(value); }
            public Rel(final org.opentrafficsim.core.value.vdouble.scalar.MutableDoubleScalar.Rel<AccelerationUnit> value) { super(value); }
        }
        
        public static class Abs extends DoubleScalar.Abs<AccelerationUnit>
        {
            public Abs(final double value, final AccelerationUnit unit) { super(value, unit); }
            public Abs(final org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs<AccelerationUnit> value) { super(value); }
            public Abs(final org.opentrafficsim.core.value.vdouble.scalar.MutableDoubleScalar.Abs<AccelerationUnit> value) { super(value); }
        }
    }
    
    /****************************************************************************************************************/
    /************************************************** ANGLE_PLANE *************************************************/ 
    /****************************************************************************************************************/

    AnglePlaneUnit RADIAN               = AnglePlaneUnit.RADIAN;
    AnglePlaneUnit ARCMINUTE            = AnglePlaneUnit.ARCMINUTE;
    AnglePlaneUnit ARCSECOND            = AnglePlaneUnit.ARCSECOND;
    AnglePlaneUnit CENTESIMAL_ARCMINUTE = AnglePlaneUnit.CENTESIMAL_ARCMINUTE;
    AnglePlaneUnit CENTESIMAL_ARCSECOND = AnglePlaneUnit.CENTESIMAL_ARCSECOND;
    AnglePlaneUnit DEGREE               = AnglePlaneUnit.DEGREE;
    AnglePlaneUnit GRAD                 = AnglePlaneUnit.GRAD;
    AnglePlaneUnit RAD                  = AnglePlaneUnit.RADIAN;
    AnglePlaneUnit DEG                  = AnglePlaneUnit.DEGREE;
    
    abstract class AnglePlane extends DoubleScalar<AnglePlaneUnit>
    {
        protected AnglePlane(final AnglePlaneUnit unit) { super(unit); }
        
        public static class Rel extends DoubleScalar.Rel<AnglePlaneUnit>
        {
            public Rel(final double value, final AnglePlaneUnit unit) { super(value, unit); }
            public Rel(final org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel<AnglePlaneUnit> value) { super(value); }
            public Rel(final org.opentrafficsim.core.value.vdouble.scalar.MutableDoubleScalar.Rel<AnglePlaneUnit> value) { super(value); }
        }
        
        public static class Abs extends DoubleScalar.Abs<AnglePlaneUnit>
        {
            public Abs(final double value, final AnglePlaneUnit unit) { super(value, unit); }
            public Abs(final org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs<AnglePlaneUnit> value) { super(value); }
            public Abs(final org.opentrafficsim.core.value.vdouble.scalar.MutableDoubleScalar.Abs<AnglePlaneUnit> value) { super(value); }
        }
    }

    /****************************************************************************************************************/
    /************************************************** ANGLE_SLOPE *************************************************/ 
    /****************************************************************************************************************/

    AngleSlopeUnit SLOPE_RADIAN               = AngleSlopeUnit.RADIAN;
    AngleSlopeUnit SLOPE_ARCMINUTE            = AngleSlopeUnit.ARCMINUTE;
    AngleSlopeUnit SLOPE_ARCSECOND            = AngleSlopeUnit.ARCSECOND;
    AngleSlopeUnit SLOPE_CENTESIMAL_ARCMINUTE = AngleSlopeUnit.CENTESIMAL_ARCMINUTE;
    AngleSlopeUnit SLOPE_CENTESIMAL_ARCSECOND = AngleSlopeUnit.CENTESIMAL_ARCSECOND;
    AngleSlopeUnit SLOPE_DEGREE               = AngleSlopeUnit.DEGREE;
    AngleSlopeUnit SLOPE_GRAD                 = AngleSlopeUnit.GRAD;
    
    abstract class AngleSlope extends DoubleScalar<AngleSlopeUnit>
    {
        protected AngleSlope(final AngleSlopeUnit unit) { super(unit); }
        
        public static class Rel extends DoubleScalar.Rel<AngleSlopeUnit>
        {
            public Rel(final double value, final AngleSlopeUnit unit) { super(value, unit); }
            public Rel(final org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel<AngleSlopeUnit> value) { super(value); }
            public Rel(final org.opentrafficsim.core.value.vdouble.scalar.MutableDoubleScalar.Rel<AngleSlopeUnit> value) { super(value); }
        }
        
        public static class Abs extends DoubleScalar.Abs<AngleSlopeUnit>
        {
            public Abs(final double value, final AngleSlopeUnit unit) { super(value, unit); }
            public Abs(final org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs<AngleSlopeUnit> value) { super(value); }
            public Abs(final org.opentrafficsim.core.value.vdouble.scalar.MutableDoubleScalar.Abs<AngleSlopeUnit> value) { super(value); }
        }
    }

    /****************************************************************************************************************/
    /************************************************** ANGLE_SOLID *************************************************/ 
    /****************************************************************************************************************/

    AngleSolidUnit STERADIAN     = AngleSolidUnit.STERADIAN;
    AngleSolidUnit SQUARE_DEGREE = AngleSolidUnit.SQUARE_DEGREE;
    
    abstract class AngleSolid extends DoubleScalar<AngleSolidUnit>
    {
        protected AngleSolid(final AngleSolidUnit unit) { super(unit); }
        
        public static class Rel extends DoubleScalar.Rel<AngleSolidUnit>
        {
            public Rel(final double value, final AngleSolidUnit unit) { super(value, unit); }
            public Rel(final org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel<AngleSolidUnit> value) { super(value); }
            public Rel(final org.opentrafficsim.core.value.vdouble.scalar.MutableDoubleScalar.Rel<AngleSolidUnit> value) { super(value); }
        }
        
        public static class Abs extends DoubleScalar.Abs<AngleSolidUnit>
        {
            public Abs(final double value, final AngleSolidUnit unit) { super(value, unit); }
            public Abs(final org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs<AngleSolidUnit> value) { super(value); }
            public Abs(final org.opentrafficsim.core.value.vdouble.scalar.MutableDoubleScalar.Abs<AngleSolidUnit> value) { super(value); }
        }
    }

    /****************************************************************************************************************/
    /***************************************************** AREA *****************************************************/ 
    /****************************************************************************************************************/

    AreaUnit SQUARE_METER         = AreaUnit.SQUARE_METER;
    AreaUnit SQUARE_CENTIMETER    = AreaUnit.SQUARE_CENTIMETER;
    AreaUnit SQUARE_FOOT          = AreaUnit.SQUARE_FOOT;
    AreaUnit SQUARE_INCH          = AreaUnit.SQUARE_INCH;
    AreaUnit SQUARE_MILE          = AreaUnit.SQUARE_MILE;
    AreaUnit SQUARE_MILLIMETER    = AreaUnit.SQUARE_MILLIMETER;
    AreaUnit SQUARE_YARD          = AreaUnit.SQUARE_YARD;
    AreaUnit M_2                  = AreaUnit.SQUARE_METER;
    AreaUnit CM_2                 = AreaUnit.SQUARE_CENTIMETER;
    AreaUnit FT_2                 = AreaUnit.SQUARE_FOOT;
    AreaUnit IN_2                 = AreaUnit.SQUARE_INCH;
    AreaUnit MI_2                 = AreaUnit.SQUARE_MILE;
    AreaUnit MM_2                 = AreaUnit.SQUARE_MILLIMETER;
    AreaUnit YD_2                 = AreaUnit.SQUARE_YARD;
    AreaUnit ACRE                 = AreaUnit.ACRE;
    AreaUnit ARE                  = AreaUnit.ARE;
    AreaUnit HECTARE              = AreaUnit.HECTARE;
    
    abstract class Area extends DoubleScalar<AreaUnit>
    {
        protected Area(final AreaUnit unit) { super(unit); }
        
        public static class Rel extends DoubleScalar.Rel<AreaUnit>
        {
            public Rel(final double value, final AreaUnit unit) { super(value, unit); }
            public Rel(final org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel<AreaUnit> value) { super(value); }
            public Rel(final org.opentrafficsim.core.value.vdouble.scalar.MutableDoubleScalar.Rel<AreaUnit> value) { super(value); }
        }
        
        public static class Abs extends DoubleScalar.Abs<AreaUnit>
        {
            public Abs(final double value, final AreaUnit unit) { super(value, unit); }
            public Abs(final org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs<AreaUnit> value) { super(value); }
            public Abs(final org.opentrafficsim.core.value.vdouble.scalar.MutableDoubleScalar.Abs<AreaUnit> value) { super(value); }
        }
    }
    
    /****************************************************************************************************************/
    /**************************************************** DENSITY ***************************************************/ 
    /****************************************************************************************************************/

    DensityUnit GRAM_PER_CENTIMETER_3 = DensityUnit.GRAM_PER_CENTIMETER_3;
    DensityUnit KG_PER_METER_3        = DensityUnit.KG_PER_METER_3;
    
    abstract class Density extends DoubleScalar<DensityUnit>
    {
        protected Density(final DensityUnit unit) { super(unit); }
        
        public static class Rel extends DoubleScalar.Rel<DensityUnit>
        {
            public Rel(final double value, final DensityUnit unit) { super(value, unit); }
            public Rel(final org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel<DensityUnit> value) { super(value); }
            public Rel(final org.opentrafficsim.core.value.vdouble.scalar.MutableDoubleScalar.Rel<DensityUnit> value) { super(value); }
        }
        
        public static class Abs extends DoubleScalar.Abs<DensityUnit>
        {
            public Abs(final double value, final DensityUnit unit) { super(value, unit); }
            public Abs(final org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs<DensityUnit> value) { super(value); }
            public Abs(final org.opentrafficsim.core.value.vdouble.scalar.MutableDoubleScalar.Abs<DensityUnit> value) { super(value); }
        }
    }

    /****************************************************************************************************************/
    /*********************************************** ELECTRICAL_CHARGE **********************************************/ 
    /****************************************************************************************************************/

    ElectricalChargeUnit COULOMB          = ElectricalChargeUnit.COULOMB;
    ElectricalChargeUnit ABCOULOMB        = ElectricalChargeUnit.ABCOULOMB;
    ElectricalChargeUnit ATOMIC_UNIT      = ElectricalChargeUnit.ATOMIC_UNIT;
    ElectricalChargeUnit EMU              = ElectricalChargeUnit.EMU;
    ElectricalChargeUnit ESU              = ElectricalChargeUnit.ESU;
    ElectricalChargeUnit FARADAY          = ElectricalChargeUnit.FARADAY;
    ElectricalChargeUnit FRANKLIN         = ElectricalChargeUnit.FRANKLIN;
    ElectricalChargeUnit MILLIAMPERE_HOUR = ElectricalChargeUnit.MILLIAMPERE_HOUR;
    ElectricalChargeUnit STATCOULOMB      = ElectricalChargeUnit.STATCOULOMB;
    
    abstract class ElectricalCharge extends DoubleScalar<ElectricalChargeUnit>
    {
        protected ElectricalCharge(final ElectricalChargeUnit unit) { super(unit); }
        
        public static class Rel extends DoubleScalar.Rel<ElectricalChargeUnit>
        {
            public Rel(final double value, final ElectricalChargeUnit unit) { super(value, unit); }
            public Rel(final org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel<ElectricalChargeUnit> value) { super(value); }
            public Rel(final org.opentrafficsim.core.value.vdouble.scalar.MutableDoubleScalar.Rel<ElectricalChargeUnit> value) { super(value); }
        }
        
        public static class Abs extends DoubleScalar.Abs<ElectricalChargeUnit>
        {
            public Abs(final double value, final ElectricalChargeUnit unit) { super(value, unit); }
            public Abs(final org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs<ElectricalChargeUnit> value) { super(value); }
            public Abs(final org.opentrafficsim.core.value.vdouble.scalar.MutableDoubleScalar.Abs<ElectricalChargeUnit> value) { super(value); }
        }
    }

    /****************************************************************************************************************/
    /********************************************** ELECTRICAL_CURRENT **********************************************/ 
    /****************************************************************************************************************/

    ElectricalCurrentUnit AMPERE      = ElectricalCurrentUnit.AMPERE;
    ElectricalCurrentUnit ABAMPERE    = ElectricalCurrentUnit.ABAMPERE;
    ElectricalCurrentUnit KILOAMPERE  = ElectricalCurrentUnit.KILOAMPERE;
    ElectricalCurrentUnit MICROAMPERE = ElectricalCurrentUnit.MICROAMPERE;
    ElectricalCurrentUnit MILLIAMPERE = ElectricalCurrentUnit.MILLIAMPERE;
    ElectricalCurrentUnit NANOAMPERE  = ElectricalCurrentUnit.NANOAMPERE;
    ElectricalCurrentUnit STATAMPERE  = ElectricalCurrentUnit.STATAMPERE;
    
    abstract class ElectricalCurrent extends DoubleScalar<ElectricalCurrentUnit>
    {
        protected ElectricalCurrent(final ElectricalCurrentUnit unit) { super(unit); }
        
        public static class Rel extends DoubleScalar.Rel<ElectricalCurrentUnit>
        {
            public Rel(final double value, final ElectricalCurrentUnit unit) { super(value, unit); }
            public Rel(final org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel<ElectricalCurrentUnit> value) { super(value); }
            public Rel(final org.opentrafficsim.core.value.vdouble.scalar.MutableDoubleScalar.Rel<ElectricalCurrentUnit> value) { super(value); }
        }
        
        public static class Abs extends DoubleScalar.Abs<ElectricalCurrentUnit>
        {
            public Abs(final double value, final ElectricalCurrentUnit unit) { super(value, unit); }
            public Abs(final org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs<ElectricalCurrentUnit> value) { super(value); }
            public Abs(final org.opentrafficsim.core.value.vdouble.scalar.MutableDoubleScalar.Abs<ElectricalCurrentUnit> value) { super(value); }
        }
    }

    /****************************************************************************************************************/
    /********************************************* ELECTRICAL_POTENTIAL *********************************************/ 
    /****************************************************************************************************************/

    ElectricalPotentialUnit VOLT      = ElectricalPotentialUnit.VOLT;
    ElectricalPotentialUnit ABVOLT    = ElectricalPotentialUnit.ABVOLT;
    ElectricalPotentialUnit KILOVOLT  = ElectricalPotentialUnit.KILOVOLT;
    ElectricalPotentialUnit MEGAVOLT  = ElectricalPotentialUnit.MEGAVOLT;
    ElectricalPotentialUnit MICROVOLT = ElectricalPotentialUnit.MICROVOLT;
    ElectricalPotentialUnit MILLIVOLT = ElectricalPotentialUnit.MILLIVOLT;
    ElectricalPotentialUnit STATVOLT  = ElectricalPotentialUnit.STATVOLT;
    
    abstract class ElectricalPotential extends DoubleScalar<ElectricalPotentialUnit>
    {
        protected ElectricalPotential(final ElectricalPotentialUnit unit) { super(unit); }
        
        public static class Rel extends DoubleScalar.Rel<ElectricalPotentialUnit>
        {
            public Rel(final double value, final ElectricalPotentialUnit unit) { super(value, unit); }
            public Rel(final org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel<ElectricalPotentialUnit> value) { super(value); }
            public Rel(final org.opentrafficsim.core.value.vdouble.scalar.MutableDoubleScalar.Rel<ElectricalPotentialUnit> value) { super(value); }
        }
        
        public static class Abs extends DoubleScalar.Abs<ElectricalPotentialUnit>
        {
            public Abs(final double value, final ElectricalPotentialUnit unit) { super(value, unit); }
            public Abs(final org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs<ElectricalPotentialUnit> value) { super(value); }
            public Abs(final org.opentrafficsim.core.value.vdouble.scalar.MutableDoubleScalar.Abs<ElectricalPotentialUnit> value) { super(value); }
        }
    }

    /****************************************************************************************************************/
    /******************************************** ELECTRICAL_RESISTANCE *********************************************/ 
    /****************************************************************************************************************/

    ElectricalResistanceUnit OHM      = ElectricalResistanceUnit.OHM;
    ElectricalResistanceUnit KILOOHM  = ElectricalResistanceUnit.KILOOHM;
    ElectricalResistanceUnit MEGAOHM  = ElectricalResistanceUnit.MEGAOHM;
    ElectricalResistanceUnit MILLIOHM = ElectricalResistanceUnit.MILLIOHM;
    
    abstract class ElectricalResistance extends DoubleScalar<ElectricalResistanceUnit>
    {
        protected ElectricalResistance(final ElectricalResistanceUnit unit) { super(unit); }
        
        public static class Rel extends DoubleScalar.Rel<ElectricalResistanceUnit>
        {
            public Rel(final double value, final ElectricalResistanceUnit unit) { super(value, unit); }
            public Rel(final org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel<ElectricalResistanceUnit> value) { super(value); }
            public Rel(final org.opentrafficsim.core.value.vdouble.scalar.MutableDoubleScalar.Rel<ElectricalResistanceUnit> value) { super(value); }
        }
        
        public static class Abs extends DoubleScalar.Abs<ElectricalResistanceUnit>
        {
            public Abs(final double value, final ElectricalResistanceUnit unit) { super(value, unit); }
            public Abs(final org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs<ElectricalResistanceUnit> value) { super(value); }
            public Abs(final org.opentrafficsim.core.value.vdouble.scalar.MutableDoubleScalar.Abs<ElectricalResistanceUnit> value) { super(value); }
        }
    }

    /****************************************************************************************************************/
    /*************************************************** ENERGY *****************************************************/ 
    /****************************************************************************************************************/

    EnergyUnit JOULE      = EnergyUnit.JOULE;
    EnergyUnit BTU_ISO            = EnergyUnit.BTU_ISO;
    EnergyUnit BTU_IT             = EnergyUnit.BTU_IT;
    EnergyUnit CALORIE_IT         = EnergyUnit.CALORIE_IT;
    EnergyUnit ELECTRONVOLT       = EnergyUnit.ELECTRONVOLT;
    EnergyUnit ERG                = EnergyUnit.ERG;
    EnergyUnit EXA_ELECTRONVOLT   = EnergyUnit.EXA_ELECTRONVOLT;
    EnergyUnit FOOT_POUND_FORCE   = EnergyUnit.FOOT_POUND_FORCE;
    EnergyUnit GIGA_ELECTRONVOLT  = EnergyUnit.GIGA_ELECTRONVOLT;
    EnergyUnit GIGAWATT_HOUR      = EnergyUnit.GIGAWATT_HOUR;
    EnergyUnit INCH_POUND_FORCE   = EnergyUnit.INCH_POUND_FORCE;
    EnergyUnit KILO_ELECTRONVOLT  = EnergyUnit.KILO_ELECTRONVOLT;
    EnergyUnit KILOCALORIE        = EnergyUnit.KILOCALORIE;
    EnergyUnit KILOWATT_HOUR      = EnergyUnit.KILOWATT_HOUR;
    EnergyUnit MEGA_ELECTRONVOLT  = EnergyUnit.MEGA_ELECTRONVOLT;
    EnergyUnit MEGAWATT_HOUR      = EnergyUnit.MEGAWATT_HOUR;
    EnergyUnit MICROWATT_HOUR     = EnergyUnit.MICROWATT_HOUR;
    EnergyUnit MILLI_ELECTRONVOLT = EnergyUnit.MILLI_ELECTRONVOLT;
    EnergyUnit MILLIWATT_HOUR     = EnergyUnit.MILLIWATT_HOUR;
    EnergyUnit PETA_ELECTRONVOLT  = EnergyUnit.PETA_ELECTRONVOLT;
    EnergyUnit STHENE_METER       = EnergyUnit.STHENE_METER;
    EnergyUnit TERA_ELECTRONVOLT  = EnergyUnit.TERA_ELECTRONVOLT;
    EnergyUnit TERAWATT_HOUR      = EnergyUnit.TERAWATT_HOUR;
    EnergyUnit WATT_HOUR          = EnergyUnit.WATT_HOUR;
    
    abstract class Energy extends DoubleScalar<EnergyUnit>
    {
        protected Energy(final EnergyUnit unit) { super(unit); }
        
        public static class Rel extends DoubleScalar.Rel<EnergyUnit>
        {
            public Rel(final double value, final EnergyUnit unit) { super(value, unit); }
            public Rel(final org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel<EnergyUnit> value) { super(value); }
            public Rel(final org.opentrafficsim.core.value.vdouble.scalar.MutableDoubleScalar.Rel<EnergyUnit> value) { super(value); }
        }
        
        public static class Abs extends DoubleScalar.Abs<EnergyUnit>
        {
            public Abs(final double value, final EnergyUnit unit) { super(value, unit); }
            public Abs(final org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs<EnergyUnit> value) { super(value); }
            public Abs(final org.opentrafficsim.core.value.vdouble.scalar.MutableDoubleScalar.Abs<EnergyUnit> value) { super(value); }
        }
    }

}

