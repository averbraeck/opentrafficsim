package org.opentrafficsim.sim0mq.kpi;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.kpi.interfaces.LaneDataInterface;
import org.opentrafficsim.kpi.interfaces.LinkDataInterface;

/**
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class LaneData implements LaneDataInterface
{
    /** Corresponding Link. */
    private LinkData linkData;

    /** Wrapped lane. */
    private final String laneName;

    /** the link length. */
    final Length length;

    /**
     * @param linkData LinkData; data about the link
     * @param laneName String; name of the lane
     * @param length Length; length of the lane
     */
    public LaneData(final LinkData linkData, final String laneName, final Length length)
    {
        this.linkData = linkData;
        this.laneName = laneName;
        this.length = length;
        this.linkData.addLaneData(this);
    }

    /** {@inheritDoc} */
    @Override
    public final Length getLength()
    {
        return this.length;
    }

    /** {@inheritDoc} */
    @Override
    public final LinkDataInterface getLinkData()
    {
        return this.linkData;
    }

    /**
     * @return laneName
     */
    public final String getLaneName()
    {
        return this.laneName;
    }

    /** {@inheritDoc} */
    @Override
    public String getId()
    {
        return this.laneName;
    }

    /**
     * @param linkData LinkData; set linkData
     */
    public final void setLinkData(LinkData linkData)
    {
        this.linkData = linkData;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.laneName == null) ? 0 : this.laneName.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LaneData other = (LaneData) obj;
        if (this.laneName == null)
        {
            if (other.laneName != null)
                return false;
        }
        else if (!this.laneName.equals(other.laneName))
            return false;
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "LaneData [linkData=" + this.linkData + ", laneName=" + this.laneName + ", length=" + this.length + "]";
    }

}
