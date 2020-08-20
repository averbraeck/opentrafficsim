package org.opentrafficsim.road.gtu.lane.tactical.cacc;

import static org.opentrafficsim.base.parameters.constraint.NumericConstraint.NEGATIVE;
import static org.opentrafficsim.base.parameters.constraint.NumericConstraint.POSITIVE;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterTypeAcceleration;
import org.opentrafficsim.base.parameters.ParameterTypeDouble;
import org.opentrafficsim.base.parameters.ParameterTypeDuration;
import org.opentrafficsim.base.parameters.ParameterTypeLength;
import org.opentrafficsim.base.parameters.ParameterTypeSpeed;

public class CaccParameters
{

    public static ParameterTypeDuration T_SYSTEM_CACC;

    public static final ParameterTypeDuration T_SYSTEM_ACC;

    public static final ParameterTypeDouble K;

    public static final ParameterTypeDouble K_A;

    public static final ParameterTypeDouble K_V;

    public static final ParameterTypeDouble K_D;

    public static final ParameterTypeAcceleration A_MIN;

    public static final ParameterTypeAcceleration A_MAX;

    public static final ParameterTypeDouble R_MIN;

    public static final ParameterTypeLength STANDSTILL;

    public static final ParameterTypeDuration T_GAP;

    public static ParameterTypeAcceleration A_REDUCED;

    public static ParameterTypeSpeed SET_SPEED;

    static
    {
        T_SYSTEM_CACC = new ParameterTypeDuration("TsystemCacc", "Constant time gap spacing",
                new Duration(0.5, DurationUnit.SI), POSITIVE);
        T_SYSTEM_ACC = new ParameterTypeDuration("TsystemAcc", "Constant time gap spacing", new Duration(1.5, DurationUnit.SI),
                POSITIVE);
        T_GAP = new ParameterTypeDuration("Tgap", "Gap acceptance headway", new Duration(0.56, DurationUnit.SI), POSITIVE);
        K = new ParameterTypeDouble("k", "General gain", 0.3, POSITIVE);
        K_A = new ParameterTypeDouble("ka", "Acceleration gain", 1.0, POSITIVE);
        K_D = new ParameterTypeDouble("kd", "Spacing gain", 0.1, POSITIVE); // 0.1
        K_V = new ParameterTypeDouble("kv", "Speed gain", 0.58, POSITIVE); // 0.58
        A_MIN = new ParameterTypeAcceleration("Amin", "Maximum (comfortable) deceleration",
                new Acceleration(-5, AccelerationUnit.SI), NEGATIVE);
        A_MAX = new ParameterTypeAcceleration("Amax", "Maximum (comfortable) accceleration",
                new Acceleration(1.25, AccelerationUnit.SI), POSITIVE); // was 2 m/s?
        R_MIN = new ParameterTypeDouble("rmin", "Minimum spacing", 2, POSITIVE);
        STANDSTILL = new ParameterTypeLength("Standstill", "Standstill distance", new Length(3.0, LengthUnit.METER), POSITIVE);
        A_REDUCED = new ParameterTypeAcceleration("Areduced", "Reduction in acceleration to create gaps",
                new Acceleration(0, AccelerationUnit.SI));
        SET_SPEED = new ParameterTypeSpeed("SetSpeed", "Free flow speed for leading platoon vehicle",
                new Speed(80, SpeedUnit.KM_PER_HOUR), POSITIVE);
    }
}
