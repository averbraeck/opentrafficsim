package org.opentrafficsim.core.network;

import org.opentrafficsim.base.OtsException;

/**
 * Exception thrown when network topology is inconsistent.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$, initial version Aug 22, 2014 <br>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public class NetworkException extends OtsException
{

    /** */
    private static final long serialVersionUID = 20140822L;

    /**
     * 
     */
    public NetworkException()
    {
    }

    /**
     * @param message String
     */
    public NetworkException(final String message)
    {
        super(message);
    }

    /**
     * @param cause Throwable
     */
    public NetworkException(final Throwable cause)
    {
        super(cause);
    }

    /**
     * @param message String
     * @param cause Throwable
     */
    public NetworkException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

}
