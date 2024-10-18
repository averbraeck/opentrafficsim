package org.opentrafficsim.base.geometry;

/**
 * Exception when geometry is incorrectly specified or an operation is requested that is not possible.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class OtsGeometryException extends Exception
{
    /** */
    private static final long serialVersionUID = 20150722L;

    /**
     * Empty constructor.
     */
    public OtsGeometryException()
    {
    }

    /**
     * Constructor with message.
     * @param message message to display for this exception.
     */
    public OtsGeometryException(final String message)
    {
        super(message);
    }

    /**
     * Constructor with cause.
     * @param cause the exception that triggered this exception.
     */
    public OtsGeometryException(final Throwable cause)
    {
        super(cause);
    }

    /**
     * Constructor with message and cause.
     * @param message message to display for this exception.
     * @param cause the exception that triggered this exception.
     */
    public OtsGeometryException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Complete constructor.
     * @param message message to display for this exception.
     * @param cause the exception that triggered this exception.
     * @param enableSuppression whether or not suppression is enabled or disabled
     * @param writableStackTrace whether or not the stack trace should be writable
     */
    public OtsGeometryException(final String message, final Throwable cause, final boolean enableSuppression,
            final boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
