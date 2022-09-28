package org.opentrafficsim.road.network.lane.object.trafficlight;

/**
 * The colors for a normal traffic light.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Oct 6, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public enum TrafficLightColor
{
    /** GTU needs to stop. */
    RED,

    /** GTU is allowed to continue if it cannot stop anymore. */
    YELLOW,

    /** GTU is allowed to drive. */
    GREEN,

    /** Pre-green indication. */
    PREGREEN,

    /** Traffic light is not working. */
    BLACK;

    /** @return whether the light is red or yellow. */
    public final boolean isRedOrYellow()
    {
        return this.equals(RED) | this.equals(YELLOW);
    }

    /** @return whether the light is red. */
    public final boolean isRed()
    {
        return this.equals(RED);
    }

    /** @return whether the light is yellow. */
    public final boolean isYellow()
    {
        return this.equals(YELLOW);
    }

    /** @return whether the light is green. */
    public final boolean isGreen()
    {
        return this.equals(GREEN);
    }

    /** @return whether the light is pre-green. */
    public final boolean isPreGreen()
    {
        return this.equals(PREGREEN);
    }

    /** @return whether the light is black (off). */
    public final boolean isBlack()
    {
        return this.equals(BLACK);
    }

}
