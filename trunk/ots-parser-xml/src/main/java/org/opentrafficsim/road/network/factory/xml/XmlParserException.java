package org.opentrafficsim.road.network.factory.xml;

/**
 * Exception during parsing.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 25 mei 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
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
        super();
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
