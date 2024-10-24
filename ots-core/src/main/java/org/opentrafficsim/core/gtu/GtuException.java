package org.opentrafficsim.core.gtu;

import org.opentrafficsim.base.OtsException;

/**
 * Exception thrown when GTU encounters a problem.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public class GtuException extends OtsException
{

    /** */
    private static final long serialVersionUID = 20150217L;

    /**
     * 
     */
    public GtuException()
    {
    }

    /**
     * @param message String
     */
    public GtuException(final String message)
    {
        super(message);
    }

    /**
     * @param cause Throwable
     */
    public GtuException(final Throwable cause)
    {
        super(cause);
    }

    /**
     * @param message String
     * @param cause Throwable
     */
    public GtuException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

}
