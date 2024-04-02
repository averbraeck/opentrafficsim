package org.opentrafficsim.road.network.factory.xml;

/**
 * Exception when XML elements, and their attributes, are in a circular dependency relation.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class CircularDependencyException extends RuntimeException
{

    /** */
    private static final long serialVersionUID = 20180525L;

    /**
     * 
     */
    public CircularDependencyException()
    {
    }

    /**
     * Constructor with message and cause.
     * @param message String; message
     * @param cause Throwable; cause
     */
    public CircularDependencyException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Constructor with message.
     * @param message String; message
     */
    public CircularDependencyException(final String message)
    {
        super(message);
    }

    /**
     * Constructor with cause.
     * @param cause Throwable; cause
     */
    public CircularDependencyException(final Throwable cause)
    {
        super(cause);
    }

}
