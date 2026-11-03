package org.opentrafficsim.road.gtu.tactical.util.lmrs;

/**
 * Lane change urgency. This is conceptually equal to "lane change desire" with a notion of the level of mandatory desired
 * contained within it. This distinction is relevant to separate behaviors that drivers perform, versus the levels of risk they
 * are willing to accept within these behaviors. For example, drivers may be willing to adjust their speed and position to
 * traffic in an adjacent lane, but the level of maximum deceleration to achieve this may vary.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.<br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * @author Wouter Schakel
 * @param desireLeft level of left lane change desire in the range [-1 1]
 * @param desireRight level of right lane change desire in the range [-1 1]
 * @param urgencyLeft urgency of left lane change in the range [0 desireLeft] if desireLeft &gt; 0, or 0 otherwise
 * @param urgencyRight urgency of right lane change in the range [0 desireLeft] if desireRight &gt; 0, or 0 otherwise
 */
public record Urgency(double desireLeft, double desireRight, double urgencyLeft, double urgencyRight)
{

    /**
     * Constructor. Values will be bounded by the constructor. For urgency the mandatory lane change desire can be given, even
     * if negative.
     * @param desireLeft level of left lane change desire in the range [-1 1]
     * @param desireRight level of right lane change desire in the range [-1 1]
     * @param urgencyLeft urgency of left lane change in the range [0 desireLeft] if desireLeft &gt; 0, or 0 otherwise
     * @param urgencyRight urgency of right lane change in the range [0 desireLeft] if desireRight &gt; 0, or 0 otherwise
     */
    public Urgency(final double desireLeft, final double desireRight, final double urgencyLeft, final double urgencyRight)
    {
        this.desireLeft = Math.max(-1.0, Math.min(desireLeft, 1.0));
        this.desireRight = Math.max(-1.0, Math.min(desireRight, 1.0));
        this.urgencyLeft = this.desireLeft < 0.0 ? 0.0 : Math.max(0.0, Math.min(urgencyLeft, this.desireLeft));
        this.urgencyRight = this.desireRight < 0.0 ? 0.0 : Math.max(0.0, Math.min(urgencyRight, this.desireRight));
    }

    /**
     * Returns whether the left desire is larger than (or equal to) the right.
     * @return Returns whether the left desire is larger than (or equal to) the right.
     */
    public boolean leftIsLargerOrEqual()
    {
        return this.desireLeft >= this.desireRight;
    }

}
