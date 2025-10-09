package org.opentrafficsim.base.parameters;

import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.parameters.constraint.ConstraintInterface;

/**
 * Predefined list of common parameter types.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
@SuppressWarnings("checkstyle:finalclass")
public class ParameterTypes implements ConstraintInterface
{
    /** Do not create instance. */
    private ParameterTypes()
    {
        //
    }

    /** Fixed model time step. */
    public static final ParameterTypeDuration DT;

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

    /** Reaction time. */
    public static final ParameterTypeDuration TR;

    static
    {

        DT = new ParameterTypeDuration("dt", "Fixed model time step", Duration.ofSI(0.5), POSITIVE);

        S0 = new ParameterTypeLength("s0", "Car-following stopping distance", Length.ofSI(3.0), POSITIVE);

        A = new ParameterTypeAcceleration("a", "Maximum (desired) car-following acceleration", Acceleration.ofSI(1.25),
                POSITIVE);

        B = new ParameterTypeAcceleration("b", "Maximum comfortable car-following deceleration", Acceleration.ofSI(2.09),
                POSITIVE)
        {
            /** */
            private static final long serialVersionUID = 20170203L;

            @Override
            public void check(final Acceleration value, final Parameters params) throws ParameterException
            {
                Acceleration b0 = params.getParameterOrNull(B0);
                Throw.when(b0 != null && value.si <= b0.si, ParameterException.class, "Value of b is below or equal to b0");
                Acceleration bCrit = params.getParameterOrNull(BCRIT);
                Throw.when(bCrit != null && value.si >= bCrit.si, ParameterException.class,
                        "Value of b is above or equal to bCrit");
            }
        };

        BCRIT = new ParameterTypeAcceleration("bCrit", "Maximum critical deceleration, e.g. stop/go at traffic light",
                Acceleration.ofSI(3.5), POSITIVE)
        {
            /** */
            private static final long serialVersionUID = 20170203L;

            @Override
            public void check(final Acceleration value, final Parameters params) throws ParameterException
            {
                Acceleration b0 = params.getParameterOrNull(B0);
                Throw.when(b0 != null && value.si <= b0.si, ParameterException.class, "Value of bCrit is below or equal to b0");
                Acceleration b = params.getParameterOrNull(B);
                Throw.when(b != null && value.si <= b.si, ParameterException.class, "Value of bCrit is below or equal to b");
            }
        };

        B0 = new ParameterTypeAcceleration("b0", "Maximum adjustment deceleration, e.g. when speed limit drops",
                Acceleration.ofSI(0.5), POSITIVE)
        {
            /** */
            private static final long serialVersionUID = 20170203L;

            @Override
            public void check(final Acceleration value, final Parameters params) throws ParameterException
            {
                Acceleration b = params.getParameterOrNull(B);
                Throw.when(b != null && value.si >= b.si, ParameterException.class, "Value of b0 is above or equal to b");
                Acceleration bCrit = params.getParameterOrNull(BCRIT);
                Throw.when(bCrit != null && value.si >= bCrit.si, ParameterException.class,
                        "Value of b0 is above or equal to bCrit");
            }
        };

        T = new ParameterTypeDuration("T", "Current car-following headway", Duration.ofSI(1.2), POSITIVE);

        TMIN = new ParameterTypeDuration("Tmin", "Minimum car-following headway", Duration.ofSI(0.56), POSITIVE)
        {
            /** */
            private static final long serialVersionUID = 20160400L;

            @Override
            public void check(final Duration value, final Parameters params) throws ParameterException
            {
                Duration tMax = params.getParameterOrNull(TMAX);
                Throw.when(tMax != null && value.si >= tMax.si, ParameterException.class,
                        "Value of Tmin is above or equal to Tmax");
            }
        };

        TMAX = new ParameterTypeDuration("Tmax", "Maximum car-following headway", Duration.ofSI(1.2), POSITIVE)
        {
            /** */
            private static final long serialVersionUID = 20160400L;

            @Override
            public void check(final Duration value, final Parameters params) throws ParameterException
            {
                Duration tMin = params.getParameterOrNull(TMIN);
                Throw.when(tMin != null && value.si <= tMin.si, ParameterException.class,
                        "Value of Tmax is below or equal to Tmin");
            }
        };

        TAU = new ParameterTypeDuration("tau", "Headway relaxation time", Duration.ofSI(25.0), POSITIVE);

        T0 = new ParameterTypeDuration("t0", "Look-ahead time for mandatory lane changes", Duration.ofSI(43.0), POSITIVE);

        LOOKAHEAD = new ParameterTypeLength("Look-ahead", "Look-ahead distance", Length.ofSI(295.0), POSITIVE);

        LOOKBACK = new ParameterTypeLength("Look-back", "Look-back distance", Length.ofSI(200.0), POSITIVE);

        LOOKBACKOLD = new ParameterTypeLength("Look-back old", "Look-back distance (old version for MOBIL code)",
                Length.ofSI(-200.0), NEGATIVE);

        FSPEED = new ParameterTypeDouble("fSpeed", "Speed limit adherence factor", 1.0, POSITIVE);

        VCONG = new ParameterTypeSpeed("vCong", "Speed threshold below which traffic is considered congested",
                new Speed(60, SpeedUnit.KM_PER_HOUR), POSITIVE);

        LCDUR = new ParameterTypeDuration("lcDur", "Regular lane change duration", Duration.ofSI(3.0), POSITIVE);

        PERCEPTION =
                new ParameterTypeLength("perception", "Mental map length", new Length(2.0, LengthUnit.KILOMETER), POSITIVE);

        TR = new ParameterTypeDuration("Tr", "Reaction time", Duration.ofSI(0.5), POSITIVEZERO);

    }

}
