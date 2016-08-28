package org.opentrafficsim.core.gtu.behavioralcharacteristics;

import static org.opentrafficsim.core.gtu.behavioralcharacteristics.AbstractParameterType.Check.NEGATIVE;
import static org.opentrafficsim.core.gtu.behavioralcharacteristics.AbstractParameterType.Check.POSITIVE;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.Throw;

/**
 * Predefined list of common parameter types.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 13, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
@SuppressWarnings("checkstyle:finalclass")
public class ParameterTypes
{
    /** Do not create instance. */
    private ParameterTypes()
    {
        //
    }
    
    /** Fixed model time step. */
    public static final ParameterTypeDuration DT = new ParameterTypeDuration("dt", "Fixed model time step.", new Duration(
        0.5, TimeUnit.SI), POSITIVE);
    
    /** Car-following stopping distance. */
    public static final ParameterTypeLength S0;

    /** Maximum (desired) car-following acceleration. */
    public static final ParameterTypeAcceleration A;

    /** Maximum comfortable car-following deceleration. */
    public static final ParameterTypeAcceleration B;

    /** Maximum critical deceleration, e.g. stop/go at traffic light. */
    public static final ParameterTypeAcceleration BCRIT;

    /** Maximum adjustment deceleration, e.g. when speed limit drops. */
    public static final ParameterTypeAcceleration B0;

    /** Current car-following headway. */
    public static final ParameterTypeDuration T;

    /** Minimum car-following headway. */
    public static final ParameterTypeDuration TMIN;

    /** Maximum car-following headway. */
    public static final ParameterTypeDuration TMAX;

    /** Headway relaxation time. */
    public static final ParameterTypeDuration TAU;

    /** Look-ahead time for mandatory lane changes. */
    public static final ParameterTypeDuration T0;

    /** Look-ahead distance. */
    public static final ParameterTypeLength LOOKAHEAD;

    /** Look-back distance. */
    public static final ParameterTypeLength LOOKBACK;

    // TODO remove LOOKBACKOLD
    /** Look-back distance, for old MOBIL code only. */
    public static final ParameterTypeLength LOOKBACKOLD;

    /** Speed limit adherence factor. */
    public static final ParameterTypeDouble FSPEED;

    /** Speed threshold below which traffic is considered congested. */
    public static final ParameterTypeSpeed VCONG;

    /** Regular lane change duration. */
    public static final ParameterTypeDuration LCDUR;
    
    /** Length of mental map ahead. */
    public static final ParameterTypeLength PERCEPTION;

    static
    {

        /** {@formatter:off} */

        S0 = new ParameterTypeLength("s0", "Car-following stopping distance.", new Length(3.0, LengthUnit.SI), POSITIVE);

        A = new ParameterTypeAcceleration("a", "Maximum (desired) car-following acceleration.", 
            new Acceleration(1.25, AccelerationUnit.SI), POSITIVE);

        B = new ParameterTypeAcceleration("b", "Maximum comfortable car-following deceleration.", 
            new Acceleration(2.09, AccelerationUnit.SI), POSITIVE);

        BCRIT = new ParameterTypeAcceleration("bCrit", "Maximum critical deceleration, e.g. stop/go at traffic light.",
            new Acceleration(3.5, AccelerationUnit.SI), POSITIVE);

        B0 = new ParameterTypeAcceleration("b0", "Maximum adjustment deceleration, e.g. when speed limit drops.",
            new Acceleration(0.5, AccelerationUnit.SI), POSITIVE);

        T = new ParameterTypeDuration("T", "Current car-following headway.", new Duration(1.2, TimeUnit.SI), POSITIVE);

        TMIN = new ParameterTypeDuration("Tmin", "Minimum car-following headway.", new Duration(0.56, TimeUnit.SI), POSITIVE)
            {
                /** */
                private static final long serialVersionUID = 20160400L;

                @Override
                public void check(final Duration value, final BehavioralCharacteristics bc) throws ParameterException
                {
                    Throw.when(bc.contains(ParameterTypes.TMAX) && value.si >= bc.getParameter(ParameterTypes.TMAX).si,
                        ParameterException.class, "Value of Tmin is above or equal to Tmax.");
                }
            };

        TMAX = new ParameterTypeDuration("Tmax", "Maximum car-following headway.", new Duration(1.2, TimeUnit.SI), POSITIVE)
        {
            /** */
            private static final long serialVersionUID = 20160400L;

            @Override
            public void check(final Duration value, final BehavioralCharacteristics bc) throws ParameterException
            {
                Throw.when(bc.contains(ParameterTypes.TMIN) && value.si <= bc.getParameter(ParameterTypes.TMIN).si,
                    ParameterException.class, "Value of Tmax is below or equal to Tmin.");
            }
        };

        TAU = new ParameterTypeDuration("tau", "Headway relaxation time.", new Duration(25.0, TimeUnit.SI), POSITIVE);

        T0 = new ParameterTypeDuration("t0", "Look-ahead time for mandatory lane changes.", new Duration(43.0, TimeUnit.SI),
                POSITIVE);

        LOOKAHEAD = new ParameterTypeLength("Look-ahead", "Look-ahead distance.", new Length(295.0, LengthUnit.SI), POSITIVE);

        LOOKBACK = new ParameterTypeLength("Look-back", "Look-back distance.", new Length(100, LengthUnit.SI), POSITIVE);

        LOOKBACKOLD = new ParameterTypeLength("Look-back old", "Look-back distance (old version for MOBIL code).", 
            new Length(-100, LengthUnit.SI), NEGATIVE);
        
        FSPEED = new ParameterTypeDouble("fSpeed", "Speed limit adherence factor.", 1.0, POSITIVE);

        VCONG = new ParameterTypeSpeed("vCong", "Speed threshold below which traffic is considered congested.", 
            new Speed(60, SpeedUnit.KM_PER_HOUR), POSITIVE);

        LCDUR = new ParameterTypeDuration("lcDur", "Regular lane change duration.", new Duration(3, TimeUnit.SI), POSITIVE);
        
        PERCEPTION = new ParameterTypeLength("mapLength", "Mental map length", new Length(2.0, LengthUnit.KILOMETER), POSITIVE);
        
    }

}
