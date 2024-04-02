package org.opentrafficsim.base.parameters;

/**
 * Throwable for exceptions regarding parameters.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class ParameterException extends Exception
{

    /** Serialization id. */
    private static final long serialVersionUID = 20160325L;

    /**
     * Empty constructor.
     */
    public ParameterException()
    {
    }

    /**
     * Constructor with message.
     * @param message String; Message.
     */
    public ParameterException(final String message)
    {
        super(message);
    }

    /**
     * Constructor with cause.
     * @param cause Throwable; Cause.
     */
    public ParameterException(final Throwable cause)
    {
        super(cause);
    }

    /**
     * Constructor with message and cause.
     * @param message String; Message.
     * @param cause Throwable; Cause.
     */
    public ParameterException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Constructor with message and cause.
     * @param message String; Message.
     * @param cause Throwable; Cause.
     * @param enableSuppression boolean; Whether to enable suppression.
     * @param writableStackTrace boolean; Whether or not the stack trace should be writable.
     */
    public ParameterException(final String message, final Throwable cause, final boolean enableSuppression,
            final boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
