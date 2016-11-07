package org.opentrafficsim.core.gtu.behavioralcharacteristics;


/**
 * Throwable for exceptions regarding parameters.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 13, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class ParameterException extends Exception
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

    /**
     * Constructor with message and cause.
     * @param message Message.
     * @param cause Cause.
     * @param enableSuppression Whether to enable suppression.
     * @param writableStackTrace Whether or not the stack trace should be writable.
     */
    public ParameterException(final String message, final Throwable cause, final boolean enableSuppression,
            final boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
