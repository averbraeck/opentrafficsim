package org.opentrafficsim.core.geometry;

import java.util.Arrays;
import java.util.IllegalFormatException;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-16 10:20:53 +0200 (Thu, 16 Jul 2015) $, @version $Revision: 1124 $, by $Author: pknoppers $,
 * initial version Jul 22, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class OTSGeometryException extends Exception
{
    /** */
    private static final long serialVersionUID = 20150722L;

    /**
     * construct empty OTSGeometryException.
     */
    public OTSGeometryException()
    {
        super();
    }

    /**
     * @param message message to display for this exception.
     */
    public OTSGeometryException(final String message)
    {
        super(message);
    }

    /**
     * @param cause the exception that triggered this exception.
     */
    public OTSGeometryException(final Throwable cause)
    {
        super(cause);
    }

    /**
     * @param message message to display for this exception.
     * @param cause the exception that triggered this exception.
     */
    public OTSGeometryException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    /**
     * @param message message to display for this exception.
     * @param cause the exception that triggered this exception.
     * @param enableSuppression whether or not suppression is enabled or disabled
     * @param writableStackTrace whether or not the stack trace should be writable
     */
    public OTSGeometryException(final String message, final Throwable cause, final boolean enableSuppression,
            final boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * Throw an Exception if a condition is met, e.g. for pre- and postcondition checking. Use e.g. as follows:<br>
     * <code>OTSGeometryException.throwIf(value == null, "value cannot be null for id = %s", id);</code>
     * @param condition the condition to check; an exception will be thrown if this is <b>true</b>
     * @param message the message to use in the exception, with potential formatting identifiers
     * @param args potential values to use for the formatting identifiers
     * @throws OTSGeometryException the exception to throw on true condition
     */
    public static void throwIf(final boolean condition, final String message, final Object... args) throws OTSGeometryException
    {
        if (condition)
        {
            StackTraceElement[] ste = new Exception().getStackTrace();
            String where = ste[1].getClassName() + "." + ste[1].getMethodName() + " (" + ste[1].getLineNumber() + "): ";
            try
            {
                throw new OTSGeometryException(where + String.format(message, args));
            }
            catch (IllegalFormatException exception)
            {
                throw new OTSGeometryException(where + message + "[FormatException; args=" + Arrays.asList(args) + "]");
            }
        }
    }
}
