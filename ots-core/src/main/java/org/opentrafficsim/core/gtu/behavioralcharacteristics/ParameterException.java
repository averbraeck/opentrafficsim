package org.opentrafficsim.core.gtu.behavioralcharacteristics;

import java.util.Arrays;
import java.util.IllegalFormatException;

/**
 * Throwable for exceptions regarding parameters.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Mar 24, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author Wouter Schakel
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

    /**
     * Throw an Exception if a condition is met, e.g. for pre- and postcondition checking. Use e.g. as follows:<br>
     * <code>ParameterException.throwIf(value == null, "value cannot be null for id = %s", id);</code>
     * @param condition the condition to check; an exception will be thrown if this is <b>true</b>
     * @param message the message to use in the exception, with potential formatting identifiers
     * @param args potential values to use for the formatting identifiers
     * @throws ParameterException the exception to throw on true condition
     */
    public static void throwIf(final boolean condition, final String message, final Object... args) throws ParameterException
    {
        if (condition)
        {
            StackTraceElement[] ste = new Exception().getStackTrace();
            String where = ste[1].getClassName() + "." + ste[1].getMethodName() + " (" + ste[1].getLineNumber() + "): ";
            try
            {
                throw new ParameterException(where + String.format(message, args));
            }
            catch (IllegalFormatException exception)
            {
                throw new ParameterException(where + message + "[FormatException; args=" + Arrays.asList(args) + "]");
            }
        }
    }

}
