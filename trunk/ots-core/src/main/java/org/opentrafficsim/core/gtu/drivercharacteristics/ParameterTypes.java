package org.opentrafficsim.core.gtu.drivercharacteristics;

import static org.opentrafficsim.core.gtu.drivercharacteristics.AbstractParameterType.Check.POSITIVE;
import static org.opentrafficsim.core.gtu.drivercharacteristics.AbstractParameterType.Check.NEGATIVE;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;

/**
 * Predefined list of common parameter types.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Mar 24, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author Wouter Schakel
 */
@SuppressWarnings("checkstyle:finalclass")
public class ParameterTypes
{
    /** Do not create instance. */
    private ParameterTypes()
    {
        //
    }

    /** Car-following stopping distance. */
    public static final ParameterTypeLength S0 = new ParameterTypeLength("s0", "Car-following stopping distance.",
        new Length.Rel(3.0, LengthUnit.SI), POSITIVE);

    /** Maximum (desired) car-following acceleration. */
    public static final ParameterTypeAcceleration A = new ParameterTypeAcceleration("a",
        "Maximum (desired) car-following acceleration.", new Acceleration(1.25, AccelerationUnit.SI), POSITIVE);

    /** Maximum comfortable car-following deceleration. */
    public static final ParameterTypeAcceleration B = new ParameterTypeAcceleration("b",
        "Maximum comfortable car-following deceleration.", new Acceleration(2.09, AccelerationUnit.SI), POSITIVE);

    /** Maximum critical deceleration, e.g. stop/go at traffic light. */
    public static final ParameterTypeAcceleration BCRIT = new ParameterTypeAcceleration("bCrit",
        "Maximum critical deceleration, e.g. stop/go at traffic light.", new Acceleration(3.5, AccelerationUnit.SI),
        POSITIVE);

    /** Maximum adjustment deceleration, e.g. when speed limit drops. */
    public static final ParameterTypeAcceleration B0 = new ParameterTypeAcceleration("b0",
        "Maximum adjustment deceleration, e.g. when speed limit drops.", new Acceleration(0.5, AccelerationUnit.SI),
        POSITIVE);

    /** Current car-following headway. */
    public static final ParameterTypeTime T = new ParameterTypeTime("T", "Current car-following headway.", new Time.Rel(1.2,
        TimeUnit.SI), POSITIVE);

    /** Minimum car-following headway. */
    public static final ParameterTypeTime TMIN = new ParameterTypeTime("Tmin", "Minimum car-following headway.",
        new Time.Rel(0.56, TimeUnit.SI), POSITIVE)
    {
        /** */
        private static final long serialVersionUID = 20160400L;

        @Override
        public void check(final Time.Rel value, final BehavioralCharacteristics bc) throws ParameterException
        {
            ParameterException.throwIf(bc.contains(ParameterTypes.TMAX) && value.si >= bc.getParameter(ParameterTypes.TMAX).si,
                    "Value of Tmin is above or equal to Tmax.");
        }
    };

    /** Maximum car-following headway. */
    public static final ParameterTypeTime TMAX = new ParameterTypeTime("Tmax", "Minimum car-following headway.",
        new Time.Rel(1.2, TimeUnit.SI), POSITIVE)
    {
        /** */
        private static final long serialVersionUID = 20160400L;

        @Override
        public void check(final Time.Rel value, final BehavioralCharacteristics bc) throws ParameterException
        {
            ParameterException.throwIf(bc.contains(ParameterTypes.TMIN)
                && value.si <= bc.getParameter(ParameterTypes.TMIN).si, "Value of Tmax is below or equal to Tmin.");
        }
    };

    /** Headway relaxation time. */
    public static final ParameterTypeTime TAU = new ParameterTypeTime("tau", "Headway relaxation time.", new Time.Rel(25.0,
        TimeUnit.SI), POSITIVE);

    /** Look-ahead time for mandatory lane changes. */
    public static final ParameterTypeTime T0 = new ParameterTypeTime("t0", "Look-ahead time for mandatory lane changes.",
        new Time.Rel(43.0, TimeUnit.SI), POSITIVE);

    /** Look-ahead distance. */
    public static final ParameterTypeLength LOOKAHEAD = new ParameterTypeLength("Look-ahead", "Look-ahead distance.",
        new Length.Rel(295.0, LengthUnit.SI), POSITIVE);

    /** Look-back distance. */
    public static final ParameterTypeLength LOOKBACK = new ParameterTypeLength("Look-back", "Look-back distance.",
        new Length.Rel(100, LengthUnit.SI), POSITIVE);

    // TODO: remove LOOKBACKOLD
    /** Look-back distance, for old MOBIL code only. */
    public static final ParameterTypeLength LOOKBACKOLD = new ParameterTypeLength("Look-back old",
        "Look-back distance (old version for MOBIL code).", new Length.Rel(-100, LengthUnit.SI), NEGATIVE);

    /** Speed limit adherence factor. */
    public static final ParameterTypeDouble FSPEED = new ParameterTypeDouble("fSpeed", "Speed limit adherence factor.", 1.0,
        POSITIVE);

    /** Speed threshold below which traffic is considered congested. */
    public static final ParameterTypeSpeed VCONG = new ParameterTypeSpeed("vCong",
        "Speed threshold below which traffic is considered congested.", new Speed(60, SpeedUnit.KM_PER_HOUR), POSITIVE);

    /** Regular lane change duration. */
    public static final ParameterTypeTime LCDUR = new ParameterTypeTime("lcDur", "Regular lane change duration.",
        new Time.Rel(3, TimeUnit.SI), POSITIVE);

}
