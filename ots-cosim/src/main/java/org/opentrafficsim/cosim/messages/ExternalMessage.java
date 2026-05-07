package org.opentrafficsim.cosim.messages;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.base.Identifiable;
import org.djutils.exceptions.Throw;

/**
 * Message from ExternalSim for vehicles in Hybrid or External mode. This message is typically sent at a relatively high
 * frequency, e.g. every 20ms.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 * @param time duration since start of simulation
 * @param vehicleId vehicle ID
 * @param xCoordinate x-coordinate
 * @param yCoordinate y-coordinate
 * @param direction vehicle direction
 * @param speed vehicle speed
 * @param acceleration vehicle acceleration
 */
public record ExternalMessage(Duration time, String vehicleId, Length xCoordinate, Length yCoordinate, Direction direction,
        Speed speed, Acceleration acceleration) implements Identifiable
{

    /** Message type ID. */
    public static final String ID = "EXTERNAL";

    /**
     * Constructor.
     */
    public ExternalMessage
    {
        Throw.whenNull(time, "time");
        Throw.when(time.lt0(), IllegalArgumentException.class, "Time should not be negative.");
        Throw.whenNull(vehicleId, "vehicleId");
        Throw.whenNull(xCoordinate, "xCoordinate");
        Throw.whenNull(yCoordinate, "yCoordinate");
        Throw.whenNull(direction, "direction");
        Throw.whenNull(speed, "speed");
        Throw.whenNull(acceleration, "acceleration");
    }

    @Override
    public String getId()
    {
        return ID;
    }

}
