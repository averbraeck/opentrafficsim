package org.opentrafficsim.road.gtu.lane.tactical.following;

import org.djunits.value.vdouble.scalar.Speed;

/**
 * Contains information for the desired or maximum speed for car-following.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 21, 2016 <br>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */

public class SpeedInfo
{

    /** Speed limit. */
    private final Speed speedLimit;
    
    /** Speed limit enforcement. */
    private final boolean enforcement;
    
    /** Maximum vehicle speed. */
    private final Speed maximumVehicleSpeed;
    
    /**
     * Constructor.
     * @param speedLimit Speed limit.
     * @param enforcement Whether the speed limit is enforced.
     * @param maximumVehicleSpeed Maximum vehicle speed.
     */
    public SpeedInfo(final Speed speedLimit, final boolean enforcement, final Speed maximumVehicleSpeed)
    {
        this.speedLimit = speedLimit;
        this.enforcement = enforcement;
        this.maximumVehicleSpeed = maximumVehicleSpeed;
    }

    /**
     * Returns the speed limit.
     * @return Speed limit.
     */
    public final Speed getSpeedLimit()
    {
        return this.speedLimit;
    }

    /**
     * Return whether the speed limit is enforced.
     * @return Whether the speed limit is enforced.
     */
    public final boolean isEnforcement()
    {
        return this.enforcement;
    }

    /**
     * Returns the maximum vehicle speed.
     * @return Maximum vehicle speed.
     */
    public final Speed getMaximumVehicleSpeed()
    {
        return this.maximumVehicleSpeed;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "SpeedInfo [speedLimit=" + this.speedLimit + ", enforcement=" + this.enforcement + ", maximumVehicleSpeed="
                + this.maximumVehicleSpeed + "]";
    }

}
