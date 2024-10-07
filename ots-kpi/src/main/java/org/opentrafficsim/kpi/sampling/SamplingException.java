package org.opentrafficsim.kpi.sampling;

/**
 * Exception thrown when sampling encounters an error.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */

public class SamplingException extends Exception
{

    /** */
    private static final long serialVersionUID = 20160929L;

    /**
     * Constructor.
     */
    public SamplingException()
    {
    }

    /**
     * Constructor.
     * @param message String
     */
    public SamplingException(final String message)
    {
        super(message);
    }

    /**
     * Constructor.
     * @param cause Throwable
     */
    public SamplingException(final Throwable cause)
    {
        super(cause);
    }

    /**
     * Constructor.
     * @param message String
     * @param cause Throwable
     */
    public SamplingException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Constructor.
     * @param message description of the problem
     * @param cause the cause of this Exception
     * @param enableSuppression whether or not suppression is enabled or disabled
     * @param writableStackTrace whether or not the stack trace should be writable
     */
    public SamplingException(final String message, final Throwable cause, final boolean enableSuppression,
            final boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
