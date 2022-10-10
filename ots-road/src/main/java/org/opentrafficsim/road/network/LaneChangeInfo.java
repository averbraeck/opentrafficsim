package org.opentrafficsim.road.network;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.network.LateralDirectionality;

/**
 * Lane change info.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class LaneChangeInfo implements Comparable<LaneChangeInfo>
{

    /** Required number of lane changes. */
    private final int n;

    /** Remaining distance. */
    private final Length remainingDistance;

    /** Whether the need to change lane comes from a dead-end. */
    private final boolean deadEnd;

    /** Lateral directionality of required lane changes. */
    private final LateralDirectionality lat;

    /**
     * Constructor.
     * @param n int; required number of lane changes
     * @param remainingDistance Length; remaining distance
     * @param deadEnd boolean; whether the need to change lane comes from a dead-end
     * @param lat LateralDirectionality; lateral directionality of required lane changes
     */
    public LaneChangeInfo(final int n, final Length remainingDistance, final boolean deadEnd, final LateralDirectionality lat)
    {
        Throw.whenNull(remainingDistance, "remainingDistance may not be null");
        Throw.whenNull(lat, "lat may not be null");
        this.n = n;
        this.remainingDistance = remainingDistance;
        this.deadEnd = deadEnd;
        this.lat = lat;
    }

    /**
     * Returns the required number of lane changes.
     * @return int; required number of lane changes
     */
    public int getNumberOfLaneChanges()
    {
        return this.n;
    }

    /**
     * Return the remaining distance.
     * @return Length; remaining distance
     */
    public Length getRemainingDistance()
    {
        return this.remainingDistance;
    }

    /**
     * Returns whether the need to change lane comes from a dead-end.
     * @return boolean; whether the need to change lane comes from a dead-end
     */
    public boolean deadEnd()
    {
        return this.deadEnd;
    }

    /**
     * Returns the lateral directionality of the required lane changes.
     * @return LateralDirectionality; lateral directionality of the required lane changes
     */
    public final LateralDirectionality getLateralDirectionality()
    {
        return this.lat;
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
            return Integer.compare(this.n, o.n);
        }
        return this.remainingDistance.compareTo(o.remainingDistance);
    }

}
