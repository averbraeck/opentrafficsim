package org.opentrafficsim.core.gtu.plan.operational;

import org.opentrafficsim.core.gtu.GtuException;

/**
 * Exception for the operational plan, e.g. when a request is given outside the plan's validity.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public class OperationalPlanException extends GtuException
{

    /** */
    private static final long serialVersionUID = 20151223L;

    /**
     * Construct an operational plan exception.
     */
    public OperationalPlanException()
    {
    }

    /**
     * Constructor.
     * @param message exception message
     */
    public OperationalPlanException(final String message)
    {
        super(message);
    }

    /**
     * Constructor.
     * @param cause exception that triggered this exception
     */
    public OperationalPlanException(final Throwable cause)
    {
        super(cause);
    }

    /**
     * Constructor.
     * @param message exception message
     * @param cause exception that triggered this exception
     */
    public OperationalPlanException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

}
