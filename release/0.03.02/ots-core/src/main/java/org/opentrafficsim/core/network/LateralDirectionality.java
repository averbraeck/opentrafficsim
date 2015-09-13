package org.opentrafficsim.core.network;

/**
 * Directionality in lateral direction. LEFT corresponds to a positive offset, RIGHT to a negative offset.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Oct 15, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public enum LateralDirectionality
{
    /** Direction to the left of our current driving direction. */
    LEFT,
    /** Direction to the right of our current driving direction. */
    RIGHT;

    /**
     * @param offset the offset to calculate the lateral directionality for
     * @return LEFT for a positive offset, RIGHT for negative offset
     * @throws NetworkException when offset == 0.0 or offset == NaN
     */
    public static LateralDirectionality offsetDirection(final double offset) throws NetworkException
    {
        if (offset == 0.0 || Double.isNaN(offset))
        {
            throw new NetworkException("Cannot calculate Lateral Directionality of offset " + offset);
        }
        return offset < 0 ? RIGHT : LEFT;
    }
}
