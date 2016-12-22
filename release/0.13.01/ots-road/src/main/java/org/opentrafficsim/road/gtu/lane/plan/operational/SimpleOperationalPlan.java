package org.opentrafficsim.road.gtu.lane.plan.operational;

import java.io.Serializable;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.opentrafficsim.core.network.LateralDirectionality;

import nl.tudelft.simulation.language.Throw;

/**
 * Simplified plan containing only an acceleration value and possible lane change direction.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jul 26, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */

public class SimpleOperationalPlan implements Serializable
{

    /** */
    private static final long serialVersionUID = 20160811L;

    /** Acceleration. */
    private Acceleration acceleration;

    /** Lane change direction. */
    private final LateralDirectionality laneChangeDirection;

    /**
     * @param acceleration acceleration
     */
    public SimpleOperationalPlan(final Acceleration acceleration)
    {
        this(acceleration, LateralDirectionality.NONE);
    }

    /**
     * @param acceleration acceleration
     * @param laneChangeDirection lane change direction, may be {@code null}.
     */
    public SimpleOperationalPlan(final Acceleration acceleration, final LateralDirectionality laneChangeDirection)
    {
        Throw.whenNull(acceleration, "Acceleration may not be null.");
        Throw.whenNull(laneChangeDirection, "Lane change direction may not be null.");
        this.acceleration = acceleration;
        this.laneChangeDirection = laneChangeDirection;
    }

    /**
     * @return acceleration.
     */
    public final Acceleration getAcceleration()
    {
        return this.acceleration;
    }
    
    /**
     * @return if lane change.
     */
    public final boolean isLaneChange()
    {
        return this.laneChangeDirection != LateralDirectionality.NONE;
    }

    /**
     * @return laneChangeDirection, may be NONE if no lane change.
     */
    public final LateralDirectionality getLaneChangeDirection()
    {
        return this.laneChangeDirection;
    }
    
    /**
     * Set minimum of current and given acceleration.
     * @param a acceleration to set if lower than current acceleration
     */
    public final void minimumAcceleration(final Acceleration a)
    {
        this.acceleration = Acceleration.min(this.acceleration, a);
    }
    
    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "SimpleOperationalPlan [Acceleration=" + this.acceleration + ", change=" + this.laneChangeDirection + "]";
    }

}
