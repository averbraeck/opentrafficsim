package org.opentrafficsim.cosim.messages;

import org.djunits.value.vdouble.scalar.Duration;
import org.djutils.base.Identifiable;
import org.djutils.exceptions.Throw;

/**
 * Message from ExternalSim to OTS to change the vehicle control mode.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 * @param time duration since start of simulation
 * @param vehicleId vehicle ID
 * @param controlMode vehicle control mode
 */
public record ModeMessage(Duration time, String vehicleId, ControlMode controlMode) implements Identifiable
{

    /** Message type ID. */
    public static final String ID = "MODE";

    /**
     * Constructor.
     */
    public ModeMessage
    {
        Throw.whenNull(time, "time");
        Throw.when(time.lt0(), IllegalArgumentException.class, "Time should not be negative.");
        Throw.whenNull(vehicleId, "vehicleId");
        Throw.whenNull(controlMode, "mode");
    }

    @Override
    public String getId()
    {
        return ID;
    }

    /**
     * Vehicle control mode.
     */
    public enum ControlMode
    {
        /** Vehicle is controlled by OTS. */
        OTS,

        /** OTS determines operational plan, ExternalSim controls the vehicle based on this. */
        HYBRID,

        /** ExternalSim controls the vehicle. */
        EXTERNAL;
    }

}
