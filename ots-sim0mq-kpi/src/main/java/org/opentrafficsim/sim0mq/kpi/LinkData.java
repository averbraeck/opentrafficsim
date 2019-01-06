package org.opentrafficsim.sim0mq.kpi;

import java.util.ArrayList;
import java.util.List;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.kpi.interfaces.LaneDataInterface;
import org.opentrafficsim.kpi.interfaces.LinkDataInterface;

/**
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 13 okt. 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class LinkData implements LinkDataInterface
{

    /** Wrapped link. */
    private final String linkName;

    /** start node. */
    final NodeData startNode;

    /** end node. */
    final NodeData endNode;

    /** Lanes on this link. */
    private final List<LaneDataInterface> laneDataList = new ArrayList<>();

    /** the link length. */
    private final Length length;

    /**
     * @param linkName String; wrapped link name
     * @param startNode NodeData; data of start node
     * @param endNode NodeData; data of end node
     * @param length Length; the length
     */
    public LinkData(final String linkName, final NodeData startNode, final NodeData endNode, final Length length)
    {
        this.linkName = linkName;
        this.length = length;
        this.startNode = startNode;
        this.endNode = endNode;
    }

    /**
     * Add the lane to the list of lanes for this link.
     * @param laneData LaneData; the lane to add
     */
    public void addLaneData(final LaneData laneData)
    {
        this.laneDataList.add(laneData);
    }

    /** {@inheritDoc} */
    @Override
    public final List<LaneDataInterface> getLaneDatas()
    {
        return this.laneDataList;
    }

    /** {@inheritDoc} */
    @Override
    public final Length getLength()
    {
        return this.length;
    }

    /**
     * @return startNode
     */
    public final NodeData getStartNode()
    {
        return this.startNode;
    }

    /**
     * @return endNode
     */
    public final NodeData getEndNode()
    {
        return this.endNode;
    }

    /**
     * @return linkName
     */
    public final String getLinkName()
    {
        return this.linkName;
    }

    /** {@inheritDoc} */
    @Override
    public String getId()
    {
        return this.linkName;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.endNode == null) ? 0 : this.endNode.hashCode());
        result = prime * result + ((this.length == null) ? 0 : this.length.hashCode());
        result = prime * result + ((this.linkName == null) ? 0 : this.linkName.hashCode());
        result = prime * result + ((this.startNode == null) ? 0 : this.startNode.hashCode());
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
        LinkData other = (LinkData) obj;
        if (this.linkName == null)
        {
            if (other.linkName != null)
                return false;
        }
        else if (!this.linkName.equals(other.linkName))
            return false;
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "LinkData [linkName=" + this.linkName + ", startNode=" + this.startNode + ", endNode=" + this.endNode
                + ", length=" + this.length + ", laneDataList.size()=" + this.laneDataList.size() + "]";
    }

}
