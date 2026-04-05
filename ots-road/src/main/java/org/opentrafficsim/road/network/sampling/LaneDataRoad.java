package org.opentrafficsim.road.network.sampling;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.kpi.interfaces.LaneData;
import org.opentrafficsim.road.network.Lane;

/**
 * Lane representation in road sampler.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class LaneDataRoad implements LaneData<LaneDataRoad>
{

    /** Wrapped lane. */
    private final Lane lane;

    /**
     * Constructor.
     * @param lane wrapped lane
     */
    public LaneDataRoad(final Lane lane)
    {
        this.lane = lane;
    }

    /**
     * Returns the lane.
     * @return lane.
     */
    public Lane getLane()
    {
        return this.lane;
    }

    @Override
    public Length getLength()
    {
        return this.lane.getLength();
    }

    @Override
    public LinkDataRoad getLinkData()
    {
        return new LinkDataRoad(this.lane.getLink());
    }

    @Override
    public String getId()
    {
        return this.lane.getId();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.lane == null) ? 0 : this.lane.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        LaneDataRoad other = (LaneDataRoad) obj;
        if (this.lane == null)
        {
            if (other.lane != null)
            {
                return false;
            }
        }
        else if (!this.lane.equals(other.lane))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "LaneData [lane=" + this.lane + "]";
    }

}
