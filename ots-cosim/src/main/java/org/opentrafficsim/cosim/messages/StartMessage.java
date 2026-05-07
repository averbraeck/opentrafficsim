package org.opentrafficsim.cosim.messages;

import org.djutils.base.Identifiable;

/**
 * Start message from ExternalSim to OTS.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 */
public record StartMessage() implements Identifiable
{

    /** Message type ID. */
    public static final String ID = "START";

    @Override
    public String getId()
    {
        return ID;
    }

}
