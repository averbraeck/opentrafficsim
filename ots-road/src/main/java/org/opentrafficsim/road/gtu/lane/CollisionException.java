package org.opentrafficsim.road.gtu.lane;

/**
 * Throw when a collision is detected.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class CollisionException extends RuntimeException
{

    /** */
    private static final long serialVersionUID = 20150217L;

    /**
     * 
     */
    public CollisionException()
    {
    }

    /**
     * @param message String
     */
    public CollisionException(final String message)
    {
        super(message);
    }

    /**
     * @param cause Throwable
     */
    public CollisionException(final Throwable cause)
    {
        super(cause);
    }

    /**
     * @param message String
     * @param cause Throwable
     */
    public CollisionException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    /**
     * @param message description of the problem
     * @param cause the cause of this Exception
     * @param enableSuppression whether or not suppression is enabled or disabled
     * @param writableStackTrace whether or not the stack trace should be writable
     */
    public CollisionException(final String message, final Throwable cause, final boolean enableSuppression,
            final boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
