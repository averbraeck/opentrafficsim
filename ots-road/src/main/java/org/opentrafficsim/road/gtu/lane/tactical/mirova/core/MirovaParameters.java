package org.opentrafficsim.road.gtu.lane.tactical.mirova.core;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterTypeAcceleration;
import org.opentrafficsim.base.parameters.ParameterTypeBoolean;
import org.opentrafficsim.base.parameters.ParameterTypeDouble;
import org.opentrafficsim.base.parameters.ParameterTypeDuration;
import org.opentrafficsim.base.parameters.ParameterTypeLength;
import org.opentrafficsim.base.parameters.ParameterTypeSpeed;
import org.opentrafficsim.base.parameters.constraint.ConstraintInterface;
import org.opentrafficsim.base.parameters.constraint.NumericConstraint;

/**
 * Defines the specific parameters used within the MiRoVA tactical planner framework.
 * <p>
 * These parameters govern various aspects of the cognitive and tactical layers, including lane change desires (LMRS based),
 * social interactions, cooperation thresholds, and safety margins.
 * </p>
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public final class MirovaParameters implements ConstraintInterface
{

        /**
         * Private constructor to prevent instantiation of this parameter utility class.
         */
        private MirovaParameters()
        {
                // prevent instantiation
        }

        // ----------------------------------------------------------------------
        // Tactical lane changing parameters
        // ----------------------------------------------------------------------

        /** Desire threshold for free lane change. */
        public static final ParameterTypeDouble DFREE =
                        new ParameterTypeDouble("DFREE", "Desire threshold for free lane change", 0.365, POSITIVE);

        /** Desire threshold for mandatory lane change. */
        public static final ParameterTypeDouble DMAND =
                        new ParameterTypeDouble("DMAND", "Desire threshold for mandatory lane change", 0.577, POSITIVE);

        /** Desire threshold for active gap search. */
        public static final ParameterTypeDouble DSEARCH =
                        new ParameterTypeDouble("DSEARCH", "Desire threshold for active gap search", 0.788, POSITIVE);

        /** Additional distance required for emergency stopping maneuvers. */
        public static final ParameterTypeLength emergencyStoppingDistance = new ParameterTypeLength(
                        "EMERGENCY_STOPPING_DISTANCE", "Additional distance required for emergency stopping maneuvers",
                        Length.instantiateSI(5.0), POSITIVE);

        /** Look-ahead distance to check for mandatory lane changes. */
        public static final ParameterTypeLength mandatoryLaneChangeLookAheadDistance = new ParameterTypeLength(
                        "MANDATORY_LANE_CHANGE_LOOK_AHEAD_DISTANCE", "Look-ahead distance to check for mandatory lane changes",
                        Length.instantiateSI(500.0), POSITIVE);

        /** Extended look-ahead distance for lane change decisions. */
        public static final ParameterTypeLength extendedLookAheadDistance = new ParameterTypeLength(
                        "EXTENDED_LOOK_AHEAD_DISTANCE", "Extended look-ahead distance for lane change decisions",
                        Length.instantiateSI(400.0), POSITIVE);

        /** Lane change duration in low speed, congested situations. */
        public static final ParameterTypeDuration congestedLaneChangeDuration = new ParameterTypeDuration(
                        "CONGESTED_LANE_CHANGE_DURATION", "Lane change duration in low speed, congested situations",
                        Duration.instantiateSI(1.5), POSITIVE);

        /**
         * Critical deceleration limit for the MiRoVA framework.
         * <p>
         * This parameter represents a comfortable but strong braking limit (e.g., stopping for a yellow light). It is strictly
         * defined as a negative acceleration to align directly with kinematic equations.
         * </p>
         */
        public static final ParameterTypeAcceleration B_CRIT =
                        new ParameterTypeAcceleration("bCritMirova", "Critical deceleration (strictly negative acceleration)",
                                        Acceleration.instantiateSI(-3.5), NumericConstraint.NEGATIVE);

        /**
         * Maximum physical deceleration limit for the MiRoVA framework.
         * <p>
         * This parameter represents the absolute emergency braking capability of the vehicle. It is strictly defined as a
         * negative acceleration to align directly with kinematic equations.
         * </p>
         */
        public static final ParameterTypeAcceleration B_MAX =
                        new ParameterTypeAcceleration("bMaxMirova", "Maximum deceleration (strictly negative acceleration)",
                                        Acceleration.instantiateSI(-6.0), NumericConstraint.NEGATIVE);

        // ----------------------------------------------------------------------
        // Social interaction parameters
        // ----------------------------------------------------------------------

        /** Speed gain threshold for lane change desire. */
        public static final ParameterTypeSpeed vGain = new ParameterTypeSpeed("VGAIN",
                        "Speed gain threshold for lane change desire", new Speed(69.6, SpeedUnit.KM_PER_HOUR), POSITIVE);

        /** Critical speed for social interaction. */
        public static final ParameterTypeSpeed vCrit = new ParameterTypeSpeed("VCRIT", "Critical speed for social interaction",
                        new Speed(60.0, SpeedUnit.KM_PER_HOUR), POSITIVE);

        /** Sensitivity to speed-related social pressure. */
        public static final ParameterTypeDouble socioSpeedSensitivity = new ParameterTypeDouble("SOCIO_SPEED_SENSITIVITY",
                        "Sensitivity to speed-related social pressure", 0.25, POSITIVE);

        /** Waiting time for a next lane change in opposite direction. */
        public static final ParameterTypeDuration socialInteractionCooldown = new ParameterTypeDuration(
                        "SOCIAL_INTERACTION_COOLDOWN", "waiting time for a next lane change in opposite direction",
                        Duration.instantiateSI(6.0), POSITIVE);

        // ----------------------------------------------------------------------
        // Lane change safety parameters
        // ----------------------------------------------------------------------

        /** Factor to reduce safety distance during lane change. */
        public static final ParameterTypeDouble safetyDistanceReductionFactorLaneChange =
                        new ParameterTypeDouble("SAFETY_DISTANCE_REDUCTION_FACTOR_LANE_CHANGE",
                                        "Factor to reduce safety distance during lane change", 0.5, POSITIVE);

        /** Deceleration threshold for follower vehicles in lane change maneuvers. */
        public static final ParameterTypeAcceleration followerDecelerationThreshold = new ParameterTypeAcceleration(
                        "FOLLOWER_DECELERATION_THRESHOLD", "Deceleration threshold for follower vehicles in lc maneuvers",
                        Acceleration.instantiateSI(-1.5), NEGATIVE);

        /** Minimum deceleration for follower vehicles in lane change maneuvers. */
        public static final ParameterTypeAcceleration minFollowerDecelerationThreshold = new ParameterTypeAcceleration(
                        "MIN_FOLLOWER_DECELERATION_THRESHOLD", "Minimum deceleration for follower vehicles in lc maneuvers",
                        Acceleration.instantiateSI(-1.5), NEGATIVE);

        /** Maximum deceleration for follower vehicles in lane change maneuvers. */
        public static final ParameterTypeAcceleration maxFollowerDecelerationThreshold = new ParameterTypeAcceleration(
                        "MAX_FOLLOWER_DECELERATION_THRESHOLD", "Maximum deceleration for follower vehicles in lc maneuvers",
                        Acceleration.instantiateSI(-3.0), NEGATIVE);

        /** Deceleration threshold for ego vehicle in lane change maneuvers. */
        public static final ParameterTypeAcceleration egoDecelerationThreshold = new ParameterTypeAcceleration(
                        "EGO_DECELERATION_THRESHOLD", "Deceleration threshold for ego vehicle in lc maneuvers",
                        Acceleration.instantiateSI(-2.0), NEGATIVE);

        /** Minimum deceleration for ego vehicle in lane change maneuvers. */
        public static final ParameterTypeAcceleration minEgoDecelerationThreshold = new ParameterTypeAcceleration(
                        "MIN_EGO_DECELERATION_THRESHOLD", "Minimum deceleration for ego vehicle in lc maneuvers",
                        Acceleration.instantiateSI(-2.0), NEGATIVE);

        /** Maximum deceleration for ego vehicle in lane change maneuvers. */
        public static final ParameterTypeAcceleration maxEgoDecelerationThreshold = new ParameterTypeAcceleration(
                        "MAX_EGO_DECELERATION_THRESHOLD", "Maximum deceleration for ego vehicle in lc maneuvers",
                        Acceleration.instantiateSI(-4.0), NEGATIVE);

        // ----------------------------------------------------------------------
        // Cooperation parameters
        // ----------------------------------------------------------------------

        /** Deceleration threshold for cooperative maneuvers. */
        public static final ParameterTypeAcceleration cooperativeDecelerationThreshold = new ParameterTypeAcceleration(
                        "COOPERATIVE_DECELERATION_THRESHOLD", "Deceleration threshold for cooperative maneuvers",
                        Acceleration.instantiateSI(-4.0), NEGATIVE);

        /** Deceleration for preemptive cooperative maneuvers. */
        public static final ParameterTypeAcceleration preemptiveCooperativeDeceleration = new ParameterTypeAcceleration(
                        "PREEMPTIVE_COOPERATIVE_DECELERATION", "Deceleration for preemptive cooperative maneuvers",
                        Acceleration.instantiateSI(-1.0), NEGATIVE);

        /** Enable cooperative lane changes. */
        public static final ParameterTypeBoolean cooperativeLaneChangesEnabled =
                        new ParameterTypeBoolean("COOPERATIVE_LANE_CHANGES_ENABLED", "Enable cooperative lane changes", true);

        // ----------------------------------------------------------------------
        // Prevent undercutting parameters
        // ----------------------------------------------------------------------

        /** Time To Collision (TTC) threshold to prevent undercutting. */
        public static final ParameterTypeDuration undercuttingTTCThreshold = new ParameterTypeDuration(
                        "UNDERCUTTING_TIME_HEADWAY", "TTC to prevent undercutting", Duration.instantiateSI(5.0), POSITIVE);

        /**
         * Spatial relaxation time constant (Tau_s) for the Keane and Gao (2021) phenomenon.
         */
        public static final ParameterTypeDuration RELAXATION_TAU_SPACE = new ParameterTypeDuration("tau_relax_s",
                        "Spatial relaxation time constant", Duration.instantiateSI(20.0), ConstraintInterface.POSITIVE);

        /**
         * Speed relaxation time constant (Tau_v) for the Keane and Gao (2021) phenomenon.
         */
        public static final ParameterTypeDuration RELAXATION_TAU_SPEED = new ParameterTypeDuration("tau_relax_v",
                        "Speed relaxation time constant", Duration.instantiateSI(8.0), ConstraintInterface.POSITIVE);

        /** Time To Collision (TTC) threshold for emergency braking. */
        public static final ParameterTypeDuration ttc_emergency_braking = new ParameterTypeDuration("TTC_EMERGENCY_BRAKING",
                        "Time To Collision (TTC) threshold for emergency braking", Duration.instantiateSI(2.0), POSITIVE);

        public static final ParameterTypeDouble CF_MAX_LEADERS = new ParameterTypeDouble("CF_MAX_LEADERS",
                        "Maximum number of leaders considered in car-following", 2, POSITIVE);

        /**
         * Scaling factor for the maximum physical acceleration.
         * <p>
         * This dimensionless parameter serves as a multiplier to stochastically vary the vehicle's acceleration capabilities. A
         * value of 1.0 represents the baseline performance, while values below 1.0 represent degraded performance (e.g., due to
         * vehicle age or heavy load), and values above 1.0 represent higher performance (e.g., sports cars).
         * </p>
         */
        public static final ParameterTypeDouble ACCELERATION_SCALING_FACTOR = new ParameterTypeDouble("aScale",
                        "Scaling factor for the maximum physical acceleration.", 1.0, ConstraintInterface.POSITIVE);
}
