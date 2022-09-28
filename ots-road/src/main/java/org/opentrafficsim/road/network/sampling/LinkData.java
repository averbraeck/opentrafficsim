package org.opentrafficsim.road.network.sampling;

import java.util.ArrayList;
import java.util.List;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.kpi.interfaces.LinkDataInterface;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * Link representation in road sampler.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 13 okt. 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class LinkData implements LinkDataInterface
{

    /** Wrapped link. */
    private final CrossSectionLink link;

    /**
     * @param link CrossSectionLink; wrapped link
     */
    public LinkData(final CrossSectionLink link)
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
    public final List<LaneData> getLaneDatas()
    {
        List<LaneData> lanes = new ArrayList<>();
        for (Lane lane : this.link.getLanes())
        {
            lanes.add(new LaneData(lane));
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
        LinkData other = (LinkData) obj;
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
