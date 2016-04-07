package org.opentrafficsim.core.gtu;

/**
 * Exception thrown when GTU encounters a problem.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$,
 *          initial version Aug 22, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class GTUException extends Exception
{

    /** */
    private static final long serialVersionUID = 20150217L;

    /**
     * 
     */
    public GTUException()
    {
    }

    /**
     * @param message String
     */
    public GTUException(final String message)
    {
        super(message);
    }

    /**
     * @param cause Throwable
     */
    public GTUException(final Throwable cause)
    {
        super(cause);
    }

    /**
     * @param message String
     * @param cause Throwable
     */
    public GTUException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    /**
     * @param message String; description of the problem
     * @param cause Throwable; the cause of this Exception
     * @param enableSuppression boolean; whether or not suppression is enabled or disabled
     * @param writableStackTrace boolean; whether or not the stack trace should be writable
     */
    public GTUException(final String message, final Throwable cause, final boolean enableSuppression,
        final boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * Throw an Exception if a condition is met, e.g. for pre- and postcondition checking.
     * @param condition the condition to check; an exception will be thrown if this is <b>true</b>
     * @param message the message to use in the exception
     * @throws GTUException the exception to throw on true condition
     */
    public static void failIf(final boolean condition, final String message) throws GTUException
    {
        if (condition)
        {
            StackTraceElement[] ste = new Exception().getStackTrace();
            String where =
                ste[1].getClassName() + "." + ste[1].getMethodName() + " (" + ste[1].getLineNumber()
                    + "): ";
            throw new GTUException(where + message);
        }
    }
}
