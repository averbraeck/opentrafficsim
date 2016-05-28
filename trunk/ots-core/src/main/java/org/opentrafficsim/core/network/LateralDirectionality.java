package org.opentrafficsim.core.network;

/**
 * Directionality in lateral direction. LEFT is the direction to the left of the longitudinal orientation of the GTU, relative
 * to the forward driving direction. RIGHT is the direction to the right of the longitudinal orientation of the GTU, relative to
 * the forward driving direction.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Oct 15, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public enum LateralDirectionality
{
    /** Direction to the left of the longitudinal orientation of the GTU, relative to the forward driving direction. */
    LEFT,
    /** Direction to the right of the longitudinal orientation of the GTU, relative to the forward driving direction. */
    RIGHT;

    /**
     * Determine whether the direction is the left direction.
     * @return whether the direction is the left direction
     */
    public boolean isLeft()
    {
        return this.equals(LEFT);
    }

    /**
     * Determine whether the direction is the right direction.
     * @return whether the direction is the right direction
     */
    public boolean isRight()
    {
        return this.equals(LEFT);
    }
}
