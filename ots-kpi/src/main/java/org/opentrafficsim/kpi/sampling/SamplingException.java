package org.opentrafficsim.kpi.sampling;

import org.opentrafficsim.base.OtsException;

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
public class SamplingException extends OtsException
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
     * @param message message
     */
    public SamplingException(final String message)
    {
        super(message);
    }

    /**
     * Constructor.
     * @param cause cause
     */
    public SamplingException(final Throwable cause)
    {
        super(cause);
    }

    /**
     * Constructor.
     * @param message message
     * @param cause cause
     */
    public SamplingException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

}
