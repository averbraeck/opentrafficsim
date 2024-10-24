package org.opentrafficsim.core.distributions;

import org.opentrafficsim.base.OtsException;

/**
 * Exception thrown when provided probabilities or frequencies are invalid. Negative probabilities or frequencies are invalid. A
 * set of probabilities or frequencies that adds up to 0 causes this exception when the draw method is called.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public class ProbabilityException extends OtsException
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

}
