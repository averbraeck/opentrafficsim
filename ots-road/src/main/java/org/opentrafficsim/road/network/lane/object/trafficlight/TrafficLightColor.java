package org.opentrafficsim.road.network.lane.object.trafficlight;

/**
 * The colors for a normal traffic light.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
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
    PREGREEN, // not really a color, is it...

    /** Traffic light is not working. */
    BLACK;

    /**
     * Returns whether the light is red or yellow.
     * @return whether the light is red or yellow.
     */
    public final boolean isRedOrYellow()
    {
        return this.equals(RED) | this.equals(YELLOW);
    }

    /**
     * Returns whether the light is red.
     * @return whether the light is red.
     */
    public final boolean isRed()
    {
        return this.equals(RED);
    }

    /**
     * Returns whether the light is yellow.
     * @return whether the light is yellow.
     */
    public final boolean isYellow()
    {
        return this.equals(YELLOW);
    }

    /**
     * Returns whether the light is green.
     * @return whether the light is green.
     */
    public final boolean isGreen()
    {
        return this.equals(GREEN);
    }

    /**
     * Returns whether the light is pre-green.
     * @return whether the light is pre-green.
     */
    public final boolean isPreGreen()
    {
        return this.equals(PREGREEN);
    }

    /**
     * Returns whether the light is black (off).
     * @return whether the light is black (off).
     */
    public final boolean isBlack()
    {
        return this.equals(BLACK);
    }

}
