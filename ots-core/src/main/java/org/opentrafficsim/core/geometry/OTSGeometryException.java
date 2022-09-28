package org.opentrafficsim.core.geometry;

/**
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class OTSGeometryException extends Exception
{
    /** */
    private static final long serialVersionUID = 20150722L;

    /**
     * construct empty OTSGeometryException.
     */
    public OTSGeometryException()
    {
    }

    /**
     * @param message String; message to display for this exception.
     */
    public OTSGeometryException(final String message)
    {
        super(message);
    }

    /**
     * @param cause Throwable; the exception that triggered this exception.
     */
    public OTSGeometryException(final Throwable cause)
    {
        super(cause);
    }

    /**
     * @param message String; message to display for this exception.
     * @param cause Throwable; the exception that triggered this exception.
     */
    public OTSGeometryException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    /**
     * @param message String; message to display for this exception.
     * @param cause Throwable; the exception that triggered this exception.
     * @param enableSuppression boolean; whether or not suppression is enabled or disabled
     * @param writableStackTrace boolean; whether or not the stack trace should be writable
     */
    public OTSGeometryException(final String message, final Throwable cause, final boolean enableSuppression,
            final boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
