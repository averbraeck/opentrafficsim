package org.opentrafficsim.road.gtu.lane.perception;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * A light-weight wrapper for LaneRecord search tools (PerceptionIterator). This is suitable for situations where parts of the
 * network not in the LaneStructure need to be perceived, such as conflicting lanes at intersection conflicts. Searches can only
 * be simple upstream or downstream searches, without lateral movement and without regard of a route. This class should not be
 * used whenever the LaneStructure can be used, as this class builds up a new tree each time step.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 19 feb. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class LaneDirectionRecord implements LaneRecord<LaneDirectionRecord>
{

    /** Lane. */
    private final Lane lane;

    /** Direction of travel. */
    private final GTUDirectionality dir;

    /** Distance to start. */
    private final Length startDistance;

    /** GTU type. */
    private final GTUType gtuType;

    /** Stored next lanes. */
    private List<LaneDirectionRecord> next;

    /** Stored prev lanes. */
    private List<LaneDirectionRecord> prev;

    /**
     * Constructor.
     * @param lane Lane; lane
     * @param dir GTUDirectionality; direction of travel
     * @param startDistance Length; distance to start
     * @param gtuType GTUType; GTU type
     */
    public LaneDirectionRecord(final Lane lane, final GTUDirectionality dir, final Length startDistance, final GTUType gtuType)
    {
        this.lane = lane;
        this.dir = dir;
        this.startDistance = startDistance;
        this.gtuType = gtuType;
    }

    /** {@inheritDoc} */
    @Override
    public List<LaneDirectionRecord> getNext()
    {
        if (this.next == null)
        {
            Map<Lane, GTUDirectionality> map = this.lane.downstreamLanes(this.dir, this.gtuType);
            this.next = new ArrayList<>();
            Length distance = this.startDistance.plus(getLength());
            for (Lane down : map.keySet())
            {
                this.next.add(new LaneDirectionRecord(down, map.get(down), distance, this.gtuType));
            }
        }
        return this.next;
    }

    /** {@inheritDoc} */
    @Override
    public List<LaneDirectionRecord> getPrev()
    {
        if (this.prev == null)
        {
            Map<Lane, GTUDirectionality> map = this.lane.upstreamLanes(this.dir, this.gtuType);
            this.prev = new ArrayList<>();
            for (Lane up : map.keySet())
            {
                this.prev.add(new LaneDirectionRecord(up, map.get(up), this.startDistance.minus(up.getLength()), this.gtuType));
            }
        }
        return this.prev;
    }

    /** {@inheritDoc} */
    @Override
    public Length getStartDistance()
    {
        return this.startDistance;
    }

    /** {@inheritDoc} */
    @Override
    public Length getLength()
    {
        return this.lane.getLength();
    }

    /** {@inheritDoc} */
    @Override
    public GTUDirectionality getDirection()
    {
        return this.dir;
    }

    /** {@inheritDoc} */
    @Override
    public Lane getLane()
    {
        return this.lane;
    }

}
