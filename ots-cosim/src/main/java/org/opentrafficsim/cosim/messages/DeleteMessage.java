package org.opentrafficsim.cosim.messages;

import org.djunits.value.vdouble.scalar.Duration;
import org.djutils.base.Identifiable;
import org.djutils.exceptions.Throw;

/**
 * Vehicle delete message sent from ExternalSim to OTS or from OTS to ExternalSim.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param time duration since start of simulation
 * @param vehicleId vehicle ID
 */
public record DeleteMessage(Duration time, String vehicleId) implements Identifiable
{

    /** Message type ID. */
    public static final String ID = "DELETE";

    /**
     * Constructor.
     */
    public DeleteMessage
    {
        Throw.whenNull(time, "time");
        Throw.when(time.lt0(), IllegalArgumentException.class, "Time should not be negative.");
        Throw.whenNull(vehicleId, "vehicleId");
    }

    @Override
    public String getId()
    {
        return ID;
    }

}
