package org.opentrafficsim.cosim.messages;

import org.djutils.base.Identifiable;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.cosim.RoutesJson;

/**
 * Message from ExternalSim to OTS for route information. This message is required when the network message does not contain
 * route information, e.g. when using an OpenDRIVE network.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 * @param routes routes
 * @param responseId response ID for in the ready message
 */
public record RoutesMessage(RoutesJson routes, Object responseId) implements Identifiable
{

    /** Message type ID. */
    public static final String ID = "ROUTES";

    /**
     * Constructor.
     */
    public RoutesMessage
    {
        Throw.whenNull(routes, "routes");
        Throw.whenNull(responseId, "responseId");
    }

    @Override
    public String getId()
    {
        return ID;
    }

}
