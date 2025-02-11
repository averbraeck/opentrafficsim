package org.opentrafficsim.trafficcontrol;

import org.opentrafficsim.base.OtsException;

/**
 * Exceptions thrown by traffic control programs.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class TrafficControlException extends OtsException
{

    /** */
    private static final long serialVersionUID = 20161116;

    /**
     * Constructor.
     */
    public TrafficControlException()
    {
    }

    /**
     * Constructor.
     * @param message String
     */
    public TrafficControlException(final String message)
    {
        super(message);
    }

    /**
     * Constructor.
     * @param cause Throwable
     */
    public TrafficControlException(final Throwable cause)
    {
        super(cause);
    }

    /**
     * Constructor.
     * @param message String
     * @param cause Throwable
     */
    public TrafficControlException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

}
