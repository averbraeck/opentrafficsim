package org.sim0mq.message;

import java.util.HashMap;
import java.util.Map;

import nl.tudelft.simulation.immutablecollections.Immutable;
import nl.tudelft.simulation.immutablecollections.ImmutableHashMap;

/**
 * Message status names and corresponding values when serialized.
 * <p>
 * Copyright (c) 2016-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://sim0mq.org/docs/current/license.html">Sim0MQ License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Mar 3, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public enum MessageStatus
{
    /** message status NEW. */
    NEW(1),

    /** message status CHANGE. */
    CHANGE(2),

    /** message status DELETE. */
    DELETE(3);

    /** the status code, 1, 2, or 3. */
    protected final byte status;

    /** the types for retrieval. */
    private static ImmutableHashMap<Integer, MessageStatus> types = null;

    /**
     * @param status the status code, 1, 2, or 3.
     */
    private MessageStatus(final int status)
    {
        this.status = (byte) status;
    }

    /**
     * @return status code
     */
    public final byte getStatus()
    {
        return this.status;
    }

    /**
     * make the types after initialization.
     * @return the map of message status codes
     */
    public static ImmutableHashMap<Integer, MessageStatus> getTypes()
    {
        Map<Integer, MessageStatus> t = new HashMap<>();
        if (types == null)
        {
            for (int i = 0; i < values().length; i++)
            {
                MessageStatus ms = values()[i];
                t.put((int) ms.getStatus(), ms);
            }
            types = new ImmutableHashMap<>(t, Immutable.WRAP);
        }
        return types;
    }

}
