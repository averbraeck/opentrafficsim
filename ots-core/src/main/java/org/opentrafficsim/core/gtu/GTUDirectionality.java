package org.opentrafficsim.core.gtu;

/**
 * Longitudinal driving directions for a GTU. The driving directions can be used on a link or a lane. The two possible values
 * are <code>DIR_PLUS</code> and <code>DIR_MINUS</code>.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public enum GTUDirectionality
{
    /**
     * Driving direction matches the direction of the graph, increasing fractional position when driving in this direction.
     */
    DIR_PLUS,
    /**
     * Driving direction opposite to the direction of the graph, decreasing fractional position when driving in this direction.
     */
    DIR_MINUS;

    /**
     * @return whether the gtu drives in the design direction on the link
     */
    public boolean isPlus()
    {
        return this.equals(DIR_PLUS);
    }

    /**
     * @return whether the gtu drives against the design direction on the link
     */
    public boolean isMinus()
    {
        return this.equals(DIR_MINUS);
    }

    /**
     * Returns the flipped direction.
     * @return GTUDirectionality; flipped direction.
     */
    public GTUDirectionality flip()
    {
        return isPlus() ? DIR_MINUS : DIR_PLUS;
    }
}
