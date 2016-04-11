package org.opentrafficsim.core.network;

import java.util.Arrays;
import java.util.IllegalFormatException;

/**
 * Exception thrown when network topology is inconsistent.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Aug 22, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class NetworkException extends Exception
{

    /** */
    private static final long serialVersionUID = 20140822L;

    /**
     * 
     */
    public NetworkException()
    {
    }

    /**
     * @param message String
     */
    public NetworkException(final String message)
    {
        super(message);
    }

    /**
     * @param cause Throwable
     */
    public NetworkException(final Throwable cause)
    {
        super(cause);
    }

    /**
     * @param message String
     * @param cause Throwable
     */
    public NetworkException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    /**
     * @param message String; description of the problem
     * @param cause Throwable; the cause of this ValueException
     * @param enableSuppression boolean; whether or not suppression is enabled or disabled
     * @param writableStackTrace boolean; whether or not the stack trace should be writable
     */
    public NetworkException(final String message, final Throwable cause, final boolean enableSuppression,
            final boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * Throw an Exception if a condition is met, e.g. for pre- and postcondition checking. Use e.g. as follows:<br>
     * <code>NetworkException.throwIf(value == null, "value cannot be null for id = %s", id);</code>
     * @param condition the condition to check; an exception will be thrown if this is <b>true</b>
     * @param message the message to use in the exception, with potential formatting identifiers
     * @param args potential values to use for the formatting identifiers
     * @throws NetworkException the exception to throw on true condition
     */
    public static void throwIf(final boolean condition, final String message, final Object... args) throws NetworkException
    {
        if (condition)
        {
            StackTraceElement[] ste = new Exception().getStackTrace();
            String where = ste[1].getClassName() + "." + ste[1].getMethodName() + " (" + ste[1].getLineNumber() + "): ";
            try
            {
                throw new NetworkException(where + String.format(message, args));
            }
            catch (IllegalFormatException exception)
            {
                throw new NetworkException(where + message + "[FormatException; args=" + Arrays.asList(args) + "]");
            }
        }
    }
}
