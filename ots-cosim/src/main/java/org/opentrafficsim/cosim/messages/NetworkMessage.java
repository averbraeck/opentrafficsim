package org.opentrafficsim.cosim.messages;

import org.djutils.base.Identifiable;
import org.djutils.exceptions.Throw;

/**
 * Network message from ExternalSim to OTS. This message is a trigger for OTS to setup a simulation. Prior to this message a
 * {@link RoutesMessage} and {@link OdMatrixMessage} or required if this information is not contained within this message.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 * @param type network type
 * @param network network contents
 * @param responseId response ID for in the ready message
 */
public record NetworkMessage(NetworkType type, String network, Object responseId) implements Identifiable
{

    /** Message type ID. */
    public static final String ID = "NETWORK";

    /**
     * Constructor.
     */
    public NetworkMessage
    {
        Throw.whenNull(type, "type");
        Throw.whenNull(network, "network");
        Throw.whenNull(responseId, "responseId");
    }

    @Override
    public String getId()
    {
        return ID;
    }

    /**
     * Network type.
     */
    public enum NetworkType
    {
        /**
         * OTS network. Routes and OD can be defined within the OTS XML or in separate {@link RoutesMessage} and
         * {@link OdMatrixMessage} messages.
         */
        OTS,

        /** OpenDRIVE network. Requires {@link RoutesMessage} and {@link OdMatrixMessage} sent prior. */
        OPENDRIVE;
    }

}
