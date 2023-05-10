package org.opentrafficsim.road.network.lane.object.trafficlight;

/**
 * The colors for a normal traffic light.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
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
    PreGreen,

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
        return this.equals(PreGreen);
    }

    /** @return whether the light is black (off). */
    public final boolean isBlack()
    {
        return this.equals(BLACK);
    }

}
