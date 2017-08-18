package org.opentrafficsim.road.network.sampling;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.kpi.interfaces.LaneDataInterface;
import org.opentrafficsim.kpi.interfaces.LinkDataInterface;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 13 okt. 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class LaneData implements LaneDataInterface
{

    /** Wrapped lane. */
    private final Lane lane;

    /**
     * @param lane wrapped lane
     */
    public LaneData(final Lane lane)
    {
        this.lane = lane;
    }

    /**
     * @return lane.
     */
    public final Lane getLane()
    {
        return this.lane;
    }

    /** {@inheritDoc} */
    @Override
    public final Length getLength()
    {
        return this.lane.getLength();
    }

    /** {@inheritDoc} */
    @Override
    public final LinkDataInterface getLinkData()
    {
        return new LinkData(this.lane.getParentLink());
    }

    /** {@inheritDoc} */
    @Override
    public final String getId()
    {
        return this.lane.getId();
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.lane == null) ? 0 : this.lane.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean equals(final Object obj)
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
        LaneData other = (LaneData) obj;
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

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "LaneData [lane=" + this.lane + "]";
    }

}
