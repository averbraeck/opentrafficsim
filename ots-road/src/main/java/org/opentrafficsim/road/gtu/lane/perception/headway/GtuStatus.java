package org.opentrafficsim.road.gtu.lane.perception.headway;

/**
 * Observable characteristics of a GTU.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public enum GtuStatus
{
    /** Braking lights are on when observing the headway. */
    BRAKING_LIGHTS,

    /** Left turn indicator was on when observing the headway. */
    LEFT_TURNINDICATOR,

    /** Right turn indicator was on when observing the headway. */
    RIGHT_TURNINDICATOR,

    /** Alarm lights are on. */
    EMERGENCY_LIGHTS,

    /** GTU was honking (car) or ringing a bell (cyclist) when observing the headway. */
    HONK;
}
