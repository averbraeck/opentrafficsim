package org.opentrafficsim.core.gtu;

/**
 * Intent for turn indicator.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public enum TurnIndicatorIntent
{

    /** None. */
    NONE,

    /** Left. */
    LEFT,

    /** Right. */
    RIGHT,

    /** Conflicting intents. */
    CONFLICTING;

    /**
     * Return whether there is no indicator intent.
     * @return whether no indicator intent
     */
    public boolean isNone()
    {
        return this.equals(NONE);
    }

    /**
     * Return whether the intent is left.
     * @return whether left turn indicator intent
     */
    public boolean isLeft()
    {
        return this.equals(LEFT);
    }

    /**
     * Return whether the intent is right.
     * @return whether right turn indicator intent
     */
    public boolean isRight()
    {
        return this.equals(RIGHT);
    }

    /**
     * Return whether indicator intent is conflicting.
     * @return whether conflicting indicator intent
     */
    public boolean isConflicting()
    {
        return this.equals(CONFLICTING);
    }

}
