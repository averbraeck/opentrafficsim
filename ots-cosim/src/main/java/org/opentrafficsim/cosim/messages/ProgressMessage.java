package org.opentrafficsim.cosim.messages;

import org.djunits.value.vdouble.scalar.Duration;
import org.djutils.base.Identifiable;
import org.djutils.exceptions.Throw;

/**
 * Progress message from ExternalSim to OTS.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param untilTime time until which to simulate
 * @param responseId response ID for in the ready message
 */
public record ProgressMessage(Duration untilTime, Object responseId) implements Identifiable
{

    /** Message type ID. */
    public static final String ID = "PROGRESS";

    /**
     * Constructor.
     */
    public ProgressMessage
    {
        Throw.whenNull(untilTime, "untilTime");
        Throw.whenNull(responseId, "responseId");
    }

    @Override
    public String getId()
    {
        return ID;
    }

}
