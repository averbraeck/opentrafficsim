package org.opentrafficsim.core.distributions;

/**
 * Exception thrown when provided probabilities or frequencies are invalid. Negative probabilities or frequencies are invalid. A
 * set of probabilities or frequencies that adds up to 0 causes this exception when the draw method is called.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class ProbabilityException extends Exception
{
    /** */
    private static final long serialVersionUID = 20160301L;

    /**
     * 
     */
    public ProbabilityException()
    {
    }

    /**
     * @param message String
     */
    public ProbabilityException(final String message)
    {
        super(message);
    }

    /**
     * @param cause Throwable
     */
    public ProbabilityException(final Throwable cause)
    {
        super(cause);
    }

    /**
     * @param message String
     * @param cause Throwable
     */
    public ProbabilityException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    /**
     * @param message description of the problem
     * @param cause the cause of this ValueRuntimeException
     * @param enableSuppression whether or not suppression is enabled or disabled
     * @param writableStackTrace whether or not the stack trace should be writable
     */
    public ProbabilityException(final String message, final Throwable cause, final boolean enableSuppression,
            final boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
