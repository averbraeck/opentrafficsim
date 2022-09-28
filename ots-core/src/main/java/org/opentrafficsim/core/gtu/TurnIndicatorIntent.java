package org.opentrafficsim.core.gtu;

/**
 * Intent for turn indicator.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Mar 1, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
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
     * @return whether no indicator intent
     */
    public boolean isNone()
    {
        return this.equals(NONE);
    }

    /**
     * @return whether left turn indicator intent
     */
    public boolean isLeft()
    {
        return this.equals(LEFT);
    }

    /**
     * @return whether right turn indicator intent
     */
    public boolean isRight()
    {
        return this.equals(RIGHT);
    }

    /**
     * @return whether conflicting indicator intent
     */
    public boolean isConflicting()
    {
        return this.equals(CONFLICTING);
    }

}
