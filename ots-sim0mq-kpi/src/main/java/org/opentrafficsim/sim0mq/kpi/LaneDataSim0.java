package org.opentrafficsim.sim0mq.kpi;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.kpi.interfaces.LaneData;

/**
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class LaneDataSim0 implements LaneData<LaneDataSim0>
{
    /** Corresponding Link. */
    private LinkDataSim0 linkData;

    /** Wrapped lane. */
    private final String laneName;

    /** the link length. */
    final Length length;

    /**
     * @param linkData data about the link
     * @param laneName name of the lane
     * @param length length of the lane
     */
    public LaneDataSim0(final LinkDataSim0 linkData, final String laneName, final Length length)
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
    public final LinkDataSim0 getLinkData()
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
     * @param linkData set linkData
     */
    public final void setLinkData(final LinkDataSim0 linkData)
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
    public boolean equals(final Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LaneDataSim0 other = (LaneDataSim0) obj;
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
