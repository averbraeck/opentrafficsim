package org.opentrafficsim.kpi.sampling;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.exceptions.Throw;

/**
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
    private final KpiLaneDirection laneDirection;

    /** Start position. */
    private final Length startPosition;

    /** End position. */
    private final Length endPosition;

    /** Start time. */
    private final Time startTime;

    /** End time. */
    private final Time endTime;

    /**
     * @param laneDirection KpiLaneDirection; lane direction
     * @param startPosition Length; start position
     * @param endPosition Length; end position
     * @param startTime Time; start time
     * @param endTime Time; end time
     * @throws IllegalArgumentException if start time is larger than end time
     */
    public SpaceTimeRegion(final KpiLaneDirection laneDirection, final Length startPosition, final Length endPosition,
            final Time startTime, final Time endTime)
    {
        Throw.whenNull(startPosition, "Start position may not be null.");
        Throw.whenNull(endPosition, "End position may not be null.");
        Throw.whenNull(startTime, "Start time may not be null.");
        Throw.whenNull(endTime, "End time may not be null.");
        Throw.when(endPosition.lt(startPosition), IllegalArgumentException.class,
                "End position should be greater than start position.");
        Throw.when(endTime.lt(startTime), IllegalArgumentException.class, "End time should be greater than start time.");
        this.laneDirection = laneDirection;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * @return laneDirection.
     */
    public final KpiLaneDirection getLaneDirection()
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
    public final Time getStartTime()
    {
        return this.startTime;
    }

    /**
     * @return endTime.
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
        result = prime * result + ((this.laneDirection == null) ? 0 : this.laneDirection.hashCode());
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
        SpaceTimeRegion other = (SpaceTimeRegion) obj;
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
        if (this.laneDirection == null)
        {
            if (other.laneDirection != null)
            {
                return false;
            }
        }
        else if (!this.laneDirection.equals(other.laneDirection))
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
        return "SpaceTimeRegion [laneDirection=" + this.laneDirection + ", startPosition=" + this.startPosition
                + ", endPosition=" + this.endPosition + ", startTime=" + this.startTime + ", endTime=" + this.endTime + "]";
    }

}
