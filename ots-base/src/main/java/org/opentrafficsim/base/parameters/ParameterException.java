package org.opentrafficsim.base.parameters;

import org.opentrafficsim.base.OtsException;

/**
 * Throwable for exceptions regarding parameters.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class ParameterException extends OtsException
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
     * @param message Message.
     */
    public ParameterException(final String message)
    {
        super(message);
    }

    /**
     * Constructor with cause.
     * @param cause Cause.
     */
    public ParameterException(final Throwable cause)
    {
        super(cause);
    }

    /**
     * Constructor with message and cause.
     * @param message Message.
     * @param cause Cause.
     */
    public ParameterException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

}
