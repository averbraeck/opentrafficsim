package org.opentrafficsim.road.gtu.lane.tactical.mirova.following;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterTypeAcceleration;
import org.opentrafficsim.base.parameters.ParameterTypeDouble;
import org.opentrafficsim.base.parameters.ParameterTypeDuration;
import org.opentrafficsim.base.parameters.ParameterTypeLength;
import org.opentrafficsim.base.parameters.ParameterTypeSpeed;
import org.opentrafficsim.base.parameters.ParameterTypeString;
import org.opentrafficsim.base.parameters.constraint.ConstraintInterface;

/**
 * Parameter types for the Wiedemann 99 car-following model (CC0–CC9).
 * <p>
 * These parameters follow the naming convention used in VISSIM/Wiedemann99.
 * Values represent perception thresholds, time headways, acceleration limits
 * and oscillation characteristics of the model.
 * No definitions for CC0 and CC1 are provided here, as the W99 model is linked to
 * the standard OTS parameters S0 (minimum spacing) and T (desired time headway).
 * </p>
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 *
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public final class W99ParameterTypes implements ConstraintInterface
{
    /** Prevent instantiation of utility class. */
    private W99ParameterTypes()
    {
        // Utility class
    }

    // CC0 and CC1 parameters are mapped to ParameterTypes.S0 and ParameterTypes.T

    /** CC2 — Following variation ("max drift") [m]. */
    public static final ParameterTypeLength CC2;

    /** CC3 — Threshold for entering following [s]. */
    public static final ParameterTypeDuration CC3;

    /** CC4 — Negative following threshold [m/s]. */
    public static final ParameterTypeSpeed CC4;

    /** CC5 — Positive following threshold [m/s]. */
    public static final ParameterTypeSpeed CC5;

    /** CC6 — Speed dependency of oscillation [10^-4 rad/s]. */
    public static final ParameterTypeDouble CC6;

    /** CC7 — Oscillation acceleration [m/s²]. */
    public static final ParameterTypeAcceleration CC7;

    /** CC8 — Standstill acceleration [m/s²]. */
    public static final ParameterTypeAcceleration CC8;

    /** CC9 — Acceleration at 80 km/h [m/s²]. */
    public static final ParameterTypeAcceleration CC9;

    /** * Current driving mode of the vehicle. Dynamic during the simulation and technically
     * a vehicle attribute, but easier to implement and track as a parameter.
     */
    public static final ParameterTypeString CURRENT_DRIVING_MODE;

    // ----------------------------------------------------------------------
    // Static initializer: defines all parameters including default values
    // and optional constraints.
    // ----------------------------------------------------------------------

    static
    {
        CC2 = new ParameterTypeLength(
            "W99.cc2",
            "Following variation (max drift)",
            Length.instantiateSI(4.00),
            POSITIVE
        );

        CC3 = new ParameterTypeDuration(
            "W99.cc3",
            "Threshold for entering following",
            Duration.instantiateSI(-12.0),
            NEGATIVE
        );

        CC4 = new ParameterTypeSpeed(
            "W99.cc4",
            "Negative following threshold",
            Speed.instantiateSI(-0.35),
            NEGATIVE
        );

        CC5 = new ParameterTypeSpeed(
            "W99.cc5",
            "Positive following threshold",
            Speed.instantiateSI(0.35),
            POSITIVE
        );

        CC6 = new ParameterTypeDouble(
            "W99.cc6",
            "Speed dependency of oscillation (1e-4 rad/s)",
            6.0,
            POSITIVE
        );

        CC7 = new ParameterTypeAcceleration(
            "W99.cc7",
            "Oscillation acceleration",
            Acceleration.instantiateSI(0.25),
            POSITIVE
        );

        CC8 = new ParameterTypeAcceleration(
            "W99.cc8",
            "Standstill acceleration",
            Acceleration.instantiateSI(2.0),
            POSITIVE
        );

        CC9 = new ParameterTypeAcceleration(
            "W99.cc9",
            "Acceleration at 80 km/h",
            Acceleration.instantiateSI(1.5),
            POSITIVE
        );

        CURRENT_DRIVING_MODE = new ParameterTypeString(
            "W99.currentDrivingMode",
            "Current driving mode of the vehicle",
            "undefined"
        );
    }
}