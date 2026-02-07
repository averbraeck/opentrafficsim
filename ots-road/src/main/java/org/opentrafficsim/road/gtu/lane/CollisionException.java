package org.opentrafficsim.road.gtu.lane;

import org.opentrafficsim.base.OtsRuntimeException;

/**
 * Throw when a collision is detected.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class CollisionException extends OtsRuntimeException
{

    /** */
    private static final long serialVersionUID = 20150217L;

    /**
     * Constructor.
     */
    public CollisionException()
    {
    }

    /**
     * Constructor.
     * @param message String
     */
    public CollisionException(final String message)
    {
        super(message);
    }

    /**
     * Constructor.
     * @param cause Throwable
     */
    public CollisionException(final Throwable cause)
    {
        super(cause);
    }

    /**
     * Constructor.
     * @param message String
     * @param cause Throwable
     */
    public CollisionException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

}
