package org.opentrafficsim.core.gtu;

/**
 * Turn indicator status.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public enum TurnIndicatorStatus
{
    /** None. */
    NONE,

    /** Left. */
    LEFT,

    /** Right. */
    RIGHT,

    /** Hazard warning. */
    HAZARD,

    /** Does not have a turn indicator. */
    NOTPRESENT;

    /**
     * Returns whether no indicator is on.
     * @return whether no indicator is on
     */
    public boolean isNone()
    {
        return this.equals(NONE);
    }

    /**
     * Returns whether left turn indicator is on.
     * @return whether left turn indicator is on
     */
    public boolean isLeft()
    {
        return this.equals(LEFT);
    }

    /**
     * Returns whether left turn indicator or hazard light is on.
     * @return whether left turn indicator or hazard light is on
     */
    public boolean isLeftOrBoth()
    {
        return this.equals(LEFT) || this.equals(HAZARD);
    }

    /**
     * Returns whether right turn indicator is on.
     * @return whether right turn indicator is on
     */
    public boolean isRight()
    {
        return this.equals(RIGHT);
    }

    /**
     * Returns whether right turn indicator or hazard light is on.
     * @return whether right turn indicator or hazard light is on
     */
    public boolean isRightOrBoth()
    {
        return this.equals(RIGHT) || this.equals(HAZARD);
    }

    /**
     * Returns whether hazard lights are on.
     * @return whether hazard lights are on
     */
    public boolean isHazard()
    {
        return this.equals(HAZARD);
    }

}
