package org.opentrafficsim.road.network.sampling;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.road.network.lane.LaneDirection;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Sep 22, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class SpaceTimeRegion
{

    /** Lane direction. */
    private final LaneDirection laneDirection;

    /** Start position. */
    private final Length startPosition;

    /** End position. */
    private final Length endPosition;

    /** Start time. */
    private final Duration startTime;

    /** End time. */
    private final Duration endTime;

    /**
     * @param laneDirection lane direction
     * @param startPosition start position
     * @param endPosition end position
     * @param startTime start time
     * @param endTime end time
     */
    SpaceTimeRegion(final LaneDirection laneDirection, final Length startPosition, final Length endPosition,
        final Duration startTime, final Duration endTime)
    {
        this.laneDirection = laneDirection;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * @return laneDirection.
     */
    public final LaneDirection getLaneDirection()
    {
        return this.laneDirection;
    }

    /**
     * @return startPosition.
     */
    public final Length getStartPosition()
    {
        return this.startPosition;
    }

    /**
     * @return endPosition.
     */
    public final Length getEndPosition()
    {
        return this.endPosition;
    }

    /**
     * @return startTime.
     */
    public final Duration getStartTime()
    {
        return this.startTime;
    }

    /**
     * @return endTime.
     */
    public final Duration getEndTime()
    {
        return this.endTime;
    }

}
