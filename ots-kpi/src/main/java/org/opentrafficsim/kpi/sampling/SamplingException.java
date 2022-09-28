package org.opentrafficsim.kpi.sampling;

/**
 * Exception thrown when sampling encounters an error.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Sep 29, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */

public class SamplingException extends Exception
{

    /** */
    private static final long serialVersionUID = 20160929L;

    /**
     * 
     */
    public SamplingException()
    {
    }

    /**
     * @param message String; String
     */
    public SamplingException(final String message)
    {
        super(message);
    }

    /**
     * @param cause Throwable; Throwable
     */
    public SamplingException(final Throwable cause)
    {
        super(cause);
    }

    /**
     * @param message String; String
     * @param cause Throwable; Throwable
     */
    public SamplingException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    /**
     * @param message String; description of the problem
     * @param cause Throwable; the cause of this Exception
     * @param enableSuppression boolean; whether or not suppression is enabled or disabled
     * @param writableStackTrace boolean; whether or not the stack trace should be writable
     */
    public SamplingException(final String message, final Throwable cause, final boolean enableSuppression,
            final boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
