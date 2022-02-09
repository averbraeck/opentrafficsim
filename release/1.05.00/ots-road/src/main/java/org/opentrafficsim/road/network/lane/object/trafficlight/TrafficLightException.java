package org.opentrafficsim.road.network.lane.object.trafficlight;

/**
 * Exception for traffic lights and traffic light controllers.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Oct 4, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class TrafficLightException extends Exception
{
    /** */
    private static final long serialVersionUID = 20161004L;

    /**
     * Construct a TrafficLightException.
     */
    public TrafficLightException()
    {
    }

    /**
     * Construct a TrafficLightException.
     * @param message String; the explanation of the exception
     */
    public TrafficLightException(final String message)
    {
        super(message);
    }

    /**
     * Construct a TrafficLightException.
     * @param cause Throwable; the Throwable causing this exception
     */
    public TrafficLightException(final Throwable cause)
    {
        super(cause);
    }

    /**
     * @param message String; the explanation of the exception
     * @param cause Throwable; the Throwable causing this exception
     */
    public TrafficLightException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    /**
     * @param message String; the explanation of the exception
     * @param cause Throwable; the Throwable causing this exception
     * @param enableSuppression boolean; whether or not suppression is enabled or disabled
     * @param writableStackTrace boolean; whether or not the stack trace should be writable
     */
    public TrafficLightException(final String message, final Throwable cause, final boolean enableSuppression,
            final boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
