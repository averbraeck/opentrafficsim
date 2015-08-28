package org.opentrafficsim.core.value.vdouble.scalar;

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
    
}

