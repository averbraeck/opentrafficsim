package org.opentrafficsim.core.gtu;

/**
 * Thrown when a model component is missing, or when the correct implementation is missing. These situations cannot always be
 * checked beforehand, as certain model assume surrounding GTUs to have model components, and possibly of a certain
 * implementation. 
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class MissingComponentException extends GtuException
{

    /** */
    private static final long serialVersionUID = 20241025L;

    /** */
    public MissingComponentException()
    {
        super();
    }

    /**
     * Constructor with message.
     * @param message message
     */
    public MissingComponentException(final String message)
    {
        super(message);
    }

    /**
     * Constructor with throwable cause.
     * @param cause cause
     */
    public MissingComponentException(final Throwable cause)
    {
        super(cause);
    }

    /**
     * Constructor with message and throwable cause.
     * @param message message
     * @param cause cause
     */
    public MissingComponentException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

}
