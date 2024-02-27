package org.opentrafficsim.kpi.sampling;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.kpi.interfaces.LaneData;

/**
 * Defines a rectangular region over space and time on a lane.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <L> lane data type
 */
public class SpaceTimeRegion<L extends LaneData<L>>
{

    /** Lane. */
    private final L lane;

    /** Start position. */
    private final Length startPosition;

    /** End position. */
    private final Length endPosition;

    /** Start time. */
    private final Time startTime;

    /** End time. */
    private final Time endTime;

    /**
     * @param lane L; lane
     * @param startPosition Length; start position
     * @param endPosition Length; end position
     * @param startTime Time; start time
     * @param endTime Time; end time
     * @throws IllegalArgumentException if start time is larger than end time
     */
    public SpaceTimeRegion(final L lane, final Length startPosition, final Length endPosition, final Time startTime,
            final Time endTime)
    {
        Throw.whenNull(startPosition, "Start position may not be null.");
        Throw.whenNull(endPosition, "End position may not be null.");
        Throw.whenNull(startTime, "Start time may not be null.");
        Throw.whenNull(endTime, "End time may not be null.");
        Throw.when(endPosition.lt(startPosition), IllegalArgumentException.class,
                "End position should be greater than start position.");
        Throw.when(endTime.lt(startTime), IllegalArgumentException.class, "End time should be greater than start time.");
        this.lane = lane;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * Returns the lane.
     * @return L; lane.
     */
    public final L getLane()
    {
        return this.lane;
    }

    /**
     * Returns the start position.
     * @return Length; start position.
     */
    public final Length getStartPosition()
    {
        return this.startPosition;
    }

    /**
     * Returns the end position.
     * @return Length; end position.
     */
    public final Length getEndPosition()
    {
        return this.endPosition;
    }

    /**
     * Returns the start time.
     * @return Time; start time.
     */
    public final Time getStartTime()
    {
        return this.startTime;
    }

    /**
     * Returns the end time.
     * @return Time end; time.
     */
    public final Time getEndTime()
    {
        return this.endTime;
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.endPosition == null) ? 0 : this.endPosition.hashCode());
        result = prime * result + ((this.endTime == null) ? 0 : this.endTime.hashCode());
        result = prime * result + ((this.lane == null) ? 0 : this.lane.hashCode());
        result = prime * result + ((this.startPosition == null) ? 0 : this.startPosition.hashCode());
        result = prime * result + ((this.startTime == null) ? 0 : this.startTime.hashCode());
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
        SpaceTimeRegion<?> other = (SpaceTimeRegion<?>) obj;
        if (this.endPosition == null)
        {
            if (other.endPosition != null)
            {
                return false;
            }
        }
        else if (!this.endPosition.equals(other.endPosition))
        {
            return false;
        }
        if (this.endTime == null)
        {
            if (other.endTime != null)
            {
                return false;
            }
        }
        else if (!this.endTime.equals(other.endTime))
        {
            return false;
        }
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
        if (this.startPosition == null)
        {
            if (other.startPosition != null)
            {
                return false;
            }
        }
        else if (!this.startPosition.equals(other.startPosition))
        {
            return false;
        }
        if (this.startTime == null)
        {
            if (other.startTime != null)
            {
                return false;
            }
        }
        else if (!this.startTime.equals(other.startTime))
        {
            return false;
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "SpaceTimeRegion [laneDirection=" + this.lane + ", startPosition=" + this.startPosition + ", endPosition="
                + this.endPosition + ", startTime=" + this.startTime + ", endTime=" + this.endTime + "]";
    }

}
