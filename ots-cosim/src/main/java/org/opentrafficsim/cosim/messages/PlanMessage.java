package org.opentrafficsim.cosim.messages;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vfloat.vector.FloatAccelerationVector;
import org.djunits.value.vfloat.vector.FloatDurationVector;
import org.djunits.value.vfloat.vector.FloatLengthVector;
import org.djutils.base.Identifiable;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.gtu.TurnIndicatorStatus;

/**
 * Message from OTS to ExternalSim for vehicles with OTS or Hybrid control.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param time duration since start of simulation
 * @param vehicleId vehicle ID
 * @param speed initial speed
 * @param xCoordinates x-coordinates
 * @param yCoordinates y-coordinates
 * @param steps acceleration duration steps
 * @param acceleration acceleration values
 * @param turnIndicator turn indicator status
 */
public record PlanMessage(Duration time, String vehicleId, Speed speed, FloatLengthVector xCoordinates,
        FloatLengthVector yCoordinates, FloatDurationVector steps, FloatAccelerationVector acceleration,
        TurnIndicatorStatus turnIndicator) implements Identifiable
{

    /** Message type ID. */
    public static final String ID = "PLAN";

    /**
     * Constructor.
     */
    public PlanMessage
    {
        Throw.whenNull(time, "time");
        Throw.when(time.lt0(), IllegalArgumentException.class, "Time should not be negative.");
        Throw.whenNull(vehicleId, "vehicleId");
        Throw.whenNull(speed, "speed");
        Throw.whenNull(xCoordinates, "xCoordinates");
        Throw.whenNull(yCoordinates, "yCoordinates");
        Throw.whenNull(steps, "steps");
        Throw.whenNull(acceleration, "acceleration");
        Throw.whenNull(turnIndicator, "turnIndicator");
    }

    @Override
    public String getId()
    {
        return ID;
    }

}
