package org.opentrafficsim.core.gtu;

/**
 * Exception to throw by move checks.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Aug 6, 2019 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class MoveCheckerException extends RuntimeException
{

    /** */
    private static final long serialVersionUID = 20190806L;

    /** 
     * @param cause Throwable; original cause
     */
    public MoveCheckerException(final Throwable cause)
    {
        super(cause);
    }
    
}
