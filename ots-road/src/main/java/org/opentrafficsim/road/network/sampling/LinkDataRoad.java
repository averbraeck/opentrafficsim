package org.opentrafficsim.road.network.sampling;

import java.util.ArrayList;
import java.util.List;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.kpi.interfaces.LinkData;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * Link representation in road sampler.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class LinkDataRoad implements LinkData<LaneDataRoad>
{

    /** Wrapped link. */
    private final CrossSectionLink link;

    /**
     * @param link CrossSectionLink; wrapped link
     */
    public LinkDataRoad(final CrossSectionLink link)
    {
        this.link = link;
    }

    /**
     * @return link.
     */
    public final CrossSectionLink getLink()
    {
        return this.link;
    }

    /** {@inheritDoc} */
    @Override
    public final List<LaneDataRoad> getLaneDatas()
    {
        List<LaneDataRoad> lanes = new ArrayList<>();
        for (Lane lane : this.link.getLanes())
        {
            lanes.add(new LaneDataRoad(lane));
        }
        return lanes;
    }

    /** {@inheritDoc} */
    @Override
    public final Length getLength()
    {
        return this.link.getLength();
    }

    /** {@inheritDoc} */
    @Override
    public final String getId()
    {
        return this.link.getId();
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.link == null) ? 0 : this.link.hashCode());
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
        LinkDataRoad other = (LinkDataRoad) obj;
        if (this.link == null)
        {
            if (other.link != null)
            {
                return false;
            }
        }
        else if (!this.link.equals(other.link))
        {
            return false;
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "LinkData [link=" + this.link + "]";
    }

}
