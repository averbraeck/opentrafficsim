package org.opentrafficsim.road.network;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.network.LateralDirectionality;

/**
 * Lane change info.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param numberOfLaneChanges int; required number of lane changes
 * @param remainingDistance Length; remaining distance
 * @param deadEnd boolean; whether the need to change lane comes from a dead-end
 * @param lateralDirectionality LateralDirectionality; lateral directionality of required lane changes
 */
public record LaneChangeInfo(int numberOfLaneChanges, Length remainingDistance, boolean deadEnd,
        LateralDirectionality lateralDirectionality) implements Comparable<LaneChangeInfo>
{

    /**
     * Constructor.
     * @param numberOfLaneChanges int; required number of lane changes
     * @param remainingDistance Length; remaining distance
     * @param deadEnd boolean; whether the need to change lane comes from a dead-end
     * @param lateralDirectionality LateralDirectionality; lateral directionality of required lane changes
     */
    public LaneChangeInfo
    {
        Throw.whenNull(remainingDistance, "remainingDistance may not be null");
        Throw.whenNull(lateralDirectionality, "lat may not be null");
    }

    /** {@inheritDoc} */
    @Override
    public int compareTo(final LaneChangeInfo o)
    {
        if (o == null)
        {
            return 1;
        }
        if (o.remainingDistance.equals(this.remainingDistance))
        {
            return Integer.compare(this.numberOfLaneChanges, o.numberOfLaneChanges);
        }
        return this.remainingDistance.compareTo(o.remainingDistance);
    }

}
