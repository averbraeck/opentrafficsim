package org.opentrafficsim.core.gtu;

/**
 * Exception to throw by move checks.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class MoveCheckerException extends RuntimeException
{

    /** */
    private static final long serialVersionUID = 20190806L;

    /**
     * @param cause Throwable; original cause
     */
    public MoveCheckerException(final Throwable cause)
    {
        super(cause);
    }

}
