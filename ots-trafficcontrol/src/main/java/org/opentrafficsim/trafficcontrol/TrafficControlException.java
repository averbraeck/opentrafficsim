package org.opentrafficsim.trafficcontrol;

/**
 * Exceptions thrown by traffic control programs.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class TrafficControlException extends Exception
{

    /** */
    private static final long serialVersionUID = 20161116;

    /**
     * 
     */
    public TrafficControlException()
    {
    }

    /**
     * @param message String
     */
    public TrafficControlException(final String message)
    {
        super(message);
    }

    /**
     * @param cause Throwable
     */
    public TrafficControlException(final Throwable cause)
    {
        super(cause);
    }

    /**
     * @param message String
     * @param cause Throwable
     */
    public TrafficControlException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    /**
     * @param message description of the problem
     * @param cause the cause of this Exception
     * @param enableSuppression whether or not suppression is enabled or disabled
     * @param writableStackTrace whether or not the stack trace should be writable
     */
    public TrafficControlException(final String message, final Throwable cause, final boolean enableSuppression,
            final boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
