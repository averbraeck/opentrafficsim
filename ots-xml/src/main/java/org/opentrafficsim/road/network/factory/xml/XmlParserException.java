package org.opentrafficsim.road.network.factory.xml;

import org.opentrafficsim.base.OtsException;

/**
 * Exception during parsing.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class XmlParserException extends OtsException
{

    /** */
    private static final long serialVersionUID = 20180525L;

    /**
     * Constructor.
     */
    public XmlParserException()
    {
    }

    /**
     * Constructor with message and cause.
     * @param message message
     * @param cause cause
     */
    public XmlParserException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Constructor with message.
     * @param message message
     */
    public XmlParserException(final String message)
    {
        super(message);
    }

    /**
     * Constructor with cause.
     * @param cause cause
     */
    public XmlParserException(final Throwable cause)
    {
        super(cause);
    }

}
