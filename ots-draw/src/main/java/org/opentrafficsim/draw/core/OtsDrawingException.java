package org.opentrafficsim.draw.core;

/**
 * OtsDrawingException.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class OtsDrawingException extends Exception
{
    /** */
    private static final long serialVersionUID = 1L;

    /** */
    public OtsDrawingException()
    {
        //
    }

    /**
     * @param message String; the error message
     */
    public OtsDrawingException(final String message)
    {
        super(message);
    }

    /**
     * @param cause Throwable; the cause of the exception to be included
     */
    public OtsDrawingException(final Throwable cause)
    {
        super(cause);
    }

    /**
     * @param message String; the error message
     * @param cause Throwable; the cause of the exception to be included
     */
    public OtsDrawingException(final String message, final Throwable cause)
    {
        super(message, cause);
    }
}
