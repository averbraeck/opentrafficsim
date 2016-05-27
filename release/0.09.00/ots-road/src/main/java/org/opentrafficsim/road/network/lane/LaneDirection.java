package org.opentrafficsim.road.network.lane;

import java.io.Serializable;

import org.opentrafficsim.core.gtu.GTUDirectionality;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Mar 30, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class LaneDirection implements Serializable
{
    /** */
    private static final long serialVersionUID = 20160330L;

    /** The lane. */
    private final Lane lane;

    /** The GTU direction to drive on this lane. */
    private final GTUDirectionality direction;

    /**
     * @param lane the lane
     * @param direction the direction to drive on this lane
     */
    public LaneDirection(final Lane lane, final GTUDirectionality direction)
    {
        super();
        this.lane = lane;
        this.direction = direction;
    }

    /**
     * @return the lane
     */
    public final Lane getLane()
    {
        return this.lane;
    }

    /**
     * @return the direction to drive on this lane
     */
    public final GTUDirectionality getDirection()
    {
        return this.direction;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "[" + this.lane + (this.direction.isPlus() ? " +]" : " -]");
    }

}
