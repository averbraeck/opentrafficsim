package org.opentrafficsim.core.gtu.drivercharacteristics;

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
    public static final ParameterType<Length.Rel> S0 = new ParameterTypePositive<Length.Rel>("s0",
        "Car-following stopping distance.", Length.Rel.class, new Length.Rel(3.0, LengthUnit.SI));

    /** Maximum (desired) car-following acceleration. */
    public static final ParameterType<Acceleration> A = new ParameterTypePositive<Acceleration>("a",
        "Maximum (desired) car-following acceleration.", Acceleration.class, new Acceleration(1.25, AccelerationUnit.SI));

    /** Maximum comfortable car-following deceleration. */
    public static final ParameterType<Acceleration> B = new ParameterTypePositive<Acceleration>("b",
        "Maximum comfortable car-following deceleration.", Acceleration.class, new Acceleration(2.09, AccelerationUnit.SI));

    /** Maximum critical deceleration, e.g. stop/go at traffic light. */
    public static final ParameterType<Acceleration> BCRIT = new ParameterTypePositive<Acceleration>("bCrit",
        "Maximum critical deceleration, e.g. stop/go at traffic light.", Acceleration.class, new Acceleration(3.5,
            AccelerationUnit.SI));

    /** Maximum adjustment deceleration, e.g. when speed limit drops. */
    public static final ParameterType<Acceleration> B0 = new ParameterTypePositive<Acceleration>("b0",
        "Maximum adjustment deceleration, e.g. when speed limit drops.", Acceleration.class, new Acceleration(0.5,
            AccelerationUnit.SI));

    /** Current car-following headway. */
    public static final ParameterType<Time.Rel> T = new ParameterTypePositive<Time.Rel>("T",
        "Current car-following headway.", Time.Rel.class, new Time.Rel(1.2, TimeUnit.SI));

    /** Minimum car-following headway. */
    public static final ParameterType<Time.Rel> TMIN = new ParameterTypePositive<Time.Rel>("Tmin",
        "Minimum car-following headway.", Time.Rel.class, new Time.Rel(0.56, TimeUnit.SI))
    {
        public void check(final Time.Rel value, final BehavioralCharacteristics bc) throws ParameterException
        {
            super.check(value, bc);
            if (bc.contains(ParameterTypes.TMAX))
            {
                ParameterException.failIf(value.si >= bc.getTimeParameter(ParameterTypes.TMAX).si, 
                        "Value of Tmin is above or equal to Tmax.");
            }
        }
    };

    /** Maximum car-following headway. */
    public static final ParameterType<Time.Rel> TMAX = new ParameterTypePositive<Time.Rel>("Tmax",
        "Minimum car-following headway.", Time.Rel.class, new Time.Rel(1.2, TimeUnit.SI))
    {
        public void check(final Time.Rel value, final BehavioralCharacteristics bc) throws ParameterException
        {
            super.check(value, bc);
            if (bc.contains(ParameterTypes.TMIN))
            {
                ParameterException.failIf(value.si <= bc.getTimeParameter(ParameterTypes.TMIN).si, 
                        "Value of Tmax is below or equal to Tmin.");
            }
        }
    };

    /** Headway relaxation time. */
    public static final ParameterType<Time.Rel> TAU = new ParameterTypePositive<Time.Rel>("tau",
        "Headway relaxation time.", Time.Rel.class, new Time.Rel(25.0, TimeUnit.SI));

    /** Look-ahead time for mandatory lane changes. */
    public static final ParameterType<Time.Rel> T0 = new ParameterTypePositive<Time.Rel>("t0",
        "Look-ahead time for mandatory lane changes.", Time.Rel.class, new Time.Rel(43.0, TimeUnit.SI));

    /** Look-ahead distance. */
    public static final ParameterType<Length.Rel> LOOKAHEAD = new ParameterTypePositive<Length.Rel>("Look-ahead",
        "Look-ahead distance.", Length.Rel.class, new Length.Rel(295.0, LengthUnit.SI));
    
    /** Look-back distance. */
    public static final ParameterType<Length.Rel> LOOKBACK = new ParameterTypePositive<Length.Rel>("Look-back",
        "Look-back distance.", Length.Rel.class, new Length.Rel(100, LengthUnit.SI));
    
    // TODO: remove LOOKBACKOLD
    /** Look-back distance, for old MOBIL code only. */
    public static final ParameterType<Length.Rel> LOOKBACKOLD = new ParameterType<Length.Rel>("Look-back",
        "Look-back distance (old version for MOBIL code).", Length.Rel.class, new Length.Rel(-100, LengthUnit.SI));

    /** Speed limit adherence factor. */
    public static final ParameterTypeDouble FSPEED = new ParameterTypeDouble("fSpeed", "Speed limit adherence factor.", -1.0)
    {
        public void check(final double value) throws ParameterException
        {
            ParameterException.failIf(value <= 0, "Parameter of type fSpeed may not have a negative or zero value.");
        }
    };

    /** Speed threshold below which traffic is considered congested. */
    public static final ParameterType<Speed> VCONG = new ParameterTypePositive<Speed>("vCong",
        "Speed threshold below which traffic is considered congested.", Speed.class, new Speed(60, SpeedUnit.KM_PER_HOUR));

    /** Regular lane change duration. */
    public static final ParameterType<Time.Rel> LCDUR = new ParameterTypePositive<Time.Rel>("lcDur",
        "Regular lane change duration.", Time.Rel.class, new Time.Rel(3, TimeUnit.SI));

}
