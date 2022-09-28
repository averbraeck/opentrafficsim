package org.opentrafficsim.core.geometry;

/**
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
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
    }

    /**
     * @param message String; message to display for this exception.
     */
    public OTSGeometryException(final String message)
    {
        super(message);
    }

    /**
     * @param cause Throwable; the exception that triggered this exception.
     */
    public OTSGeometryException(final Throwable cause)
    {
        super(cause);
    }

    /**
     * @param message String; message to display for this exception.
     * @param cause Throwable; the exception that triggered this exception.
     */
    public OTSGeometryException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    /**
     * @param message String; message to display for this exception.
     * @param cause Throwable; the exception that triggered this exception.
     * @param enableSuppression boolean; whether or not suppression is enabled or disabled
     * @param writableStackTrace boolean; whether or not the stack trace should be writable
     */
    public OTSGeometryException(final String message, final Throwable cause, final boolean enableSuppression,
            final boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
