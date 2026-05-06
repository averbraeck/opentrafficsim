package org.opentrafficsim.cosim.messages;

import org.djutils.base.Identifiable;
import org.djutils.exceptions.Throw;

/**
 * Reset message from ExternalSim to OTS.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param responseId response ID for in the ready message
 */
public record ResetMessage(Object responseId) implements Identifiable
{

    /** Message type ID. */
    public static final String ID = "RESET";

    /**
     * Constructor.
     */
    public ResetMessage
    {
        Throw.whenNull(responseId, "responseId");
    }

    @Override
    public String getId()
    {
        return ID;
    }

}
