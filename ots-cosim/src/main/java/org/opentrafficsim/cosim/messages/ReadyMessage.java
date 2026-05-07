package org.opentrafficsim.cosim.messages;

import org.djutils.base.Identifiable;
import org.djutils.exceptions.Throw;

/**
 * Ready message from OTS to ExternalSim after a {@link RoutesMessage}, {@link OdMatrixMessage}, {@link NetworkMessage},
 * {@link ResetMessage}, {@link ProgressMessage} or {@link VehicleMessage}. For a {@link VehicleMessage} a ready message not
 * sent during simulation (i.e. after {@link StartMessage} or first {@link ProgressMessage} and before a {@link StopMessage} or
 * {@link ResetMessage}).
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 * @param responseId response ID in the received message of which the processing is ready
 */
public record ReadyMessage(Object responseId) implements Identifiable
{

    /** Message type ID. */
    public static final String ID = "READY";

    /**
     * Constructor.
     */
    public ReadyMessage
    {
        Throw.whenNull(responseId, "responseId");
    }

    @Override
    public String getId()
    {
        return ID;
    }

}
