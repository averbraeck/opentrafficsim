package org.opentrafficsim.core.gtu.drivercharacteristics;

import org.opentrafficsim.core.gtu.GTUException;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Mar 24, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class ParameterException extends Exception
{

    /**
     * 
     */
    public ParameterException()
    {
    }

    /**
     * @param message
     */
    public ParameterException(String message)
    {
        super(message);
    }

    /**
     * @param cause
     */
    public ParameterException(Throwable cause)
    {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public ParameterException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public ParameterException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * Throw an Exception if a condition is met, e.g. for pre- and postcondition checking.
     * @param condition the condition to check; an exception will be thrown if this is <b>true</b>
     * @param message the message to use in the exception
     * @throws ParameterException the exception to throw on true condition
     */
    public static void failIf(final boolean condition, final String message) throws ParameterException
    {
        if (condition)
        {
            StackTraceElement[] ste = new Exception().getStackTrace();
            String where =
                ste[1].getClassName() + "." + ste[1].getMethodName() + " (" + ste[1].getLineNumber()
                    + "): ";
            throw new ParameterException(where + message);
        }
    }

}

