package org.opentrafficsim.core.gtu;

/**
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Mar 1, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
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
     * @return whether no indicator is on
     */
    public boolean isNone()
    {
        return this.equals(NONE);
    }

    /**
     * @return whether left turn indicator is on
     */
    public boolean isLeft()
    {
        return this.equals(LEFT);
    }

    /**
     * @return whether left turn indicator or hazard light is on
     */
    public boolean isLeftOrBoth()
    {
        return this.equals(LEFT) || this.equals(HAZARD);
    }

    /**
     * @return whether right turn indicator is on
     */
    public boolean isRight()
    {
        return this.equals(RIGHT);
    }

    /**
     * @return whether right turn indicator or hazard light is on
     */
    public boolean isRightOrBoth()
    {
        return this.equals(RIGHT) || this.equals(HAZARD);
    }

    /**
     * @return whether hazard lights are on
     */
    public boolean isHazard()
    {
        return this.equals(HAZARD);
    }

}
