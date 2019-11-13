package org.opentrafficsim.simulationengine;


/**
 * Exception for the operational plan, e.g. when a request is given outside the plan's validity.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Dec 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class OTSSimulationException extends Exception
{
    /** */
    private static final long serialVersionUID = 20151223L;

    /**
     * Construct an operational plan exception.
     */
    public OTSSimulationException()
    {
        super();
    }

    /**
     * @param message exception message
     */
    public OTSSimulationException(final String message)
    {
        super(message);
    }

    /**
     * @param cause exception that triggered this exception
     */
    public OTSSimulationException(final Throwable cause)
    {
        super(cause);
    }

    /**
     * @param message exception message
     * @param cause exception that triggered this exception
     */
    public OTSSimulationException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    /**
     * @param message exception message
     * @param cause exception that triggered this exception
     * @param enableSuppression whether or not suppression is enabled or disabled
     * @param writableStackTrace whether or not the stack trace should be writable
     */
    public OTSSimulationException(final String message, final Throwable cause, final boolean enableSuppression,
        final boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * Throw an Exception if a condition is met, e.g. for pre- and postcondition checking.
     * @param condition the condition to check; an exception will be thrown if this is <b>true</b>
     * @param message the message to use in the exception
     * @throws OTSSimulationException the exception to throw on true condition
     */
    public static void failIf(final boolean condition, final String message) throws OTSSimulationException
    {
        if (condition)
        {
            StackTraceElement[] ste = new Exception().getStackTrace();
            String where = ste[1].getClassName() + "." + ste[1].getMethodName() + " (" + ste[1].getLineNumber() + "): ";
            throw new OTSSimulationException(where + message);
        }
    }
}