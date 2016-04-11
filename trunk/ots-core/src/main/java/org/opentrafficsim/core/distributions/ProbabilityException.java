package org.opentrafficsim.core.distributions;

import java.util.Arrays;
import java.util.IllegalFormatException;

/**
 * Exception thrown when provided probabilities or frequencies are invalid. Negative probabilities or frequencies are invalid. A
 * set of probabilities or frequencies that adds up to 0 causes this exception when the draw method is called.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Mar 1, 2016 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class ProbabilityException extends Exception
{
    /** */
    private static final long serialVersionUID = 20160301L;

    /**
     * 
     */
    public ProbabilityException()
    {
    }

    /**
     * @param message String
     */
    public ProbabilityException(final String message)
    {
        super(message);
    }

    /**
     * @param cause Throwable
     */
    public ProbabilityException(final Throwable cause)
    {
        super(cause);
    }

    /**
     * @param message String
     * @param cause Throwable
     */
    public ProbabilityException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    /**
     * @param message String; description of the problem
     * @param cause Throwable; the cause of this ValueException
     * @param enableSuppression boolean; whether or not suppression is enabled or disabled
     * @param writableStackTrace boolean; whether or not the stack trace should be writable
     */
    public ProbabilityException(final String message, final Throwable cause, final boolean enableSuppression,
            final boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * Throw an Exception if a condition is met, e.g. for pre- and postcondition checking. Use e.g. as follows:<br>
     * <code>ProbabilityException.throwIf(value == null, "value cannot be null for id = %s", id);</code>
     * @param condition the condition to check; an exception will be thrown if this is <b>true</b>
     * @param message the message to use in the exception, with potential formatting identifiers
     * @param args potential values to use for the formatting identifiers
     * @throws ProbabilityException the exception to throw on true condition
     */
    public static void throwIf(final boolean condition, final String message, final Object... args) throws ProbabilityException
    {
        if (condition)
        {
            StackTraceElement[] ste = new Exception().getStackTrace();
            String where = ste[1].getClassName() + "." + ste[1].getMethodName() + " (" + ste[1].getLineNumber() + "): ";
            try
            {
                throw new ProbabilityException(where + String.format(message, args));
            }
            catch (IllegalFormatException exception)
            {
                throw new ProbabilityException(where + message + "[FormatException; args=" + Arrays.asList(args) + "]");
            }
        }
    }

}
