package org.opentrafficsim.base;

/**
 * OtsRuntimeException is a generic runtime exception for the OTS project. Runtime exceptions do ot have to be declared in the
 * header of the method or constructor. 
 * <p>
 * Copyright (c) 2022-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 */
public class OtsRuntimeException extends RuntimeException
{
    /** */
    private static final long serialVersionUID = 20220915L;

    /**
     * Create an exception without a message.
     */
    public OtsRuntimeException()
    {
        super();
    }

    /**
     * Create an exception with a message.
     * @param message String; the message to include in the exception
     */
    public OtsRuntimeException(final String message)
    {
        super(message);
    }

    /**
     * Create an exception with an underlying cause.
     * @param cause Throwable; the underlying cause of the exception
     */
    public OtsRuntimeException(final Throwable cause)
    {
        super(cause);
    }

    /**
     * Create an exception with an underlying cause and a message.
     * @param message String; the message to include in the exception
     * @param cause Throwable; the underlying cause of the exception
     */
    public OtsRuntimeException(final String message, final Throwable cause)
    {
        super(message, cause);
    }
}
