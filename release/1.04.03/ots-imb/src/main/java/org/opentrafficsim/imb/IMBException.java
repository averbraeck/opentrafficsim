package org.opentrafficsim.imb;

/**
 * The IMBException specifies problems with the connection to the IMB bus.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Sep 9, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class IMBException extends Exception
{
    /** */
    private static final long serialVersionUID = 20160909L;

    /**
     * Standard constructor for the IMBException specifying problems with the connection to the IMB bus.
     */
    public IMBException()
    {
    }

    /**
     * Standard constructor for the IMBException specifying problems with the connection to the IMB bus.
     * @param message String; the message to display as part of the exception.
     */
    public IMBException(final String message)
    {
        super(message);
    }

    /**
     * Standard constructor for the IMBException specifying problems with the connection to the IMB bus.
     * @param cause Throwable; the event that caused this event to happen (A null value is permitted, and indicates that the
     *            cause is nonexistent or unknown.)
     */
    public IMBException(final Throwable cause)
    {
        super(cause);
    }

    /**
     * Standard constructor for the IMBException specifying problems with the connection to the IMB bus.
     * @param message String; the message to display as part of the exception.
     * @param cause Throwable; the event that caused this event to happen (A null value is permitted, and indicates that the
     *            cause is nonexistent or unknown.)
     */
    public IMBException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Standard constructor for the IMBException specifying problems with the connection to the IMB bus.
     * @param message String; the message to display as part of the exception.
     * @param cause Throwable; the event that caused this event to happen (A null value is permitted, and indicates that the
     *            cause is nonexistent or unknown.)
     * @param enableSuppression boolean; whether or not suppression is enabled or disabled
     * @param writableStackTrace boolean; whether or not the stack trace should be writable
     */
    public IMBException(final String message, final Throwable cause, final boolean enableSuppression,
            final boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
