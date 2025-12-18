package org.opentrafficsim.road.gtu.lane.tactical.mirova.core;

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
import org.opentrafficsim.base.parameters.constraint.ConstraintInterface;

public class MirovaParameters implements ConstraintInterface
{

    private MirovaParameters()
    {
        // prevent instantiation
    }

    // tactical lane changing parameters
    public static final ParameterTypeDouble DFREE =
            new ParameterTypeDouble("DFREE",
                    "Desire threshold for free lane change",
                    0.365,
                    POSITIVE);
    public static final ParameterTypeDouble DMAND =
            new ParameterTypeDouble("DMAND",
                    "Desire threshold for mandatory lane change",
                    0.577,
                    POSITIVE);
    public static final ParameterTypeDouble DSEARCH =
            new ParameterTypeDouble("DSEARCH",
                    "Desire threshold for active gap search",
                    0.788,
                    POSITIVE);
    public static final ParameterTypeLength emergencyStoppingDistance =
            new ParameterTypeLength("EMERGENCY_STOPPING_DISTANCE",
                    "Additional distance required for emergency stopping maneuvers",
                    Length.instantiateSI(5.0),
                    POSITIVE);
    public static final ParameterTypeLength mandatoryLaneChangeLookAheadDistance =
            new ParameterTypeLength("MANDATORY_LANE_CHANGE_LOOK_AHEAD_DISTANCE",
                    "Look-ahead distance to check for mandatory lane changes",
                    Length.instantiateSI(500.0),
                    POSITIVE);

    // social interaction parameters
    public static final ParameterTypeSpeed vGain =
            new ParameterTypeSpeed("VGAIN",
                    "Speed gain threshold for lane change desire",
                    new Speed(35.0, SpeedUnit.KM_PER_HOUR),
                    POSITIVE);
    public static final ParameterTypeSpeed vCrit =
            new ParameterTypeSpeed("VCRIT",
                    "Critical speed for social interaction",
                    new Speed(60.0, SpeedUnit.KM_PER_HOUR),
                    POSITIVE);
    public static final ParameterTypeDouble socioSpeedSensitivity =
            new ParameterTypeDouble("SOCIO_SPEED_SENSITIVITY",
                    "Sensitivity to speed-related social pressure",
                    0.25,
                    POSITIVE);
    public static final ParameterTypeDuration socialInteractionCooldown =
            new ParameterTypeDuration("SOCIAL_INTERACTION_COOLDOWN",
                    "waiting time for a next lane change in opposite direction",
                    Duration.instantiateSI(6.0),
                    POSITIVE);

    // lane change safety parameters
    public static final ParameterTypeDouble safetyDistanceReductionFactorLaneChange =
            new ParameterTypeDouble("SAFETY_DISTANCE_REDUCTION_FACTOR_LANE_CHANGE",
                    "Factor to reduce safety distance during lane change",
                    0.5,
                    POSITIVE);
    public static final ParameterTypeAcceleration followerDecelerationThreshold =
            new ParameterTypeAcceleration("FOLLOWER_DECELERATION_THRESHOLD",
                    "Deceleration threshold for follower vehicles in lc maneuvers",
                    Acceleration.instantiateSI(-1.5),
                    NEGATIVE);
    public static final ParameterTypeAcceleration minFollowerDecelerationThreshold =
            new ParameterTypeAcceleration("MIN_FOLLOWER_DECELERATION_THRESHOLD",
                    "Minimum deceleration for follower vehicles in lc maneuvers",
                    Acceleration.instantiateSI(-1.5),
                    NEGATIVE);
    public static final ParameterTypeAcceleration maxFollowerDecelerationThreshold =
            new ParameterTypeAcceleration("MAX_FOLLOWER_DECELERATION_THRESHOLD",
                    "Maximum deceleration for follower vehicles in lc maneuvers",
                    Acceleration.instantiateSI(-4.0),
                    NEGATIVE);
    public static final ParameterTypeAcceleration egoDecelerationThreshold =
            new ParameterTypeAcceleration("EGO_DECELERATION_THRESHOLD",
                    "Deceleration threshold for ego vehicle in lc maneuvers",
                    Acceleration.instantiateSI(-2.0),
                    NEGATIVE);
    public static final ParameterTypeAcceleration minEgoDecelerationThreshold =
            new ParameterTypeAcceleration("MIN_EGO_DECELERATION_THRESHOLD",
                    "Minimum deceleration for ego vehicle in lc maneuvers",
                    Acceleration.instantiateSI(-2.0),
                    NEGATIVE);
    public static final ParameterTypeAcceleration maxEgoDecelerationThreshold =
            new ParameterTypeAcceleration("MAX_EGO_DECELERATION_THRESHOLD",
                    "Maximum deceleration for ego vehicle in lc maneuvers",
                    Acceleration.instantiateSI(-5.0),
                    NEGATIVE);
}
