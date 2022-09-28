package org.opentrafficsim.road.network.factory.xml;

/**
 * Exception during parsing.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
// TODO this class is not yet used in the network parser
public class XmlParserException extends Exception
{

    /** */
    private static final long serialVersionUID = 20180525L;

    /**
     * 
     */
    public XmlParserException()
    {
    }

    /**
     * @param message String; message
     * @param cause Throwable; cause
     */
    public XmlParserException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    /**
     * @param message String; messge
     */
    public XmlParserException(final String message)
    {
        super(message);
    }

    /**
     * @param cause Throwable; cause
     */
    public XmlParserException(final Throwable cause)
    {
        super(cause);
    }

}
