package org.opentrafficsim.core.network;

import java.util.List;

/**
 * Directionality in lateral direction. LEFT is the direction to the left of the longitudinal orientation of the GTU, relative
 * to the forward driving direction. RIGHT is the direction to the right of the longitudinal orientation of the GTU, relative to
 * the forward driving direction.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$, initial version Oct 15, 2014 <br>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public enum LateralDirectionality
{
    /** Direction to the left of the longitudinal orientation of the GTU, relative to the forward driving direction. */
    LEFT,

    /** Direction to the right of the longitudinal orientation of the GTU, relative to the forward driving direction. */
    RIGHT,

    /** Absence of a lateral direction. */
    NONE;

    /** List of LEFT and RIGHT that can be looped over. */
    public static final List<LateralDirectionality> LEFT_AND_RIGHT = List.of(LEFT, RIGHT);

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
        return this.equals(RIGHT);
    }

    /**
     * Determine whether the lateral direction is not present.
     * @return whether the lateral direction is not present
     */
    public boolean isNone()
    {
        return this.equals(NONE);
    }

    /**
     * Returns the other direction.
     * @return other direction
     */
    public LateralDirectionality flip()
    {
        return this.equals(LEFT) ? RIGHT : this.equals(RIGHT) ? LEFT : NONE;
    }

}
