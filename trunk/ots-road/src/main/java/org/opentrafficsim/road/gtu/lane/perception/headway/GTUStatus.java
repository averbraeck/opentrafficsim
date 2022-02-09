package org.opentrafficsim.road.gtu.lane.perception.headway;

/**
 * Observable characteristics of a GTU.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 27 mrt. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public enum GTUStatus
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
