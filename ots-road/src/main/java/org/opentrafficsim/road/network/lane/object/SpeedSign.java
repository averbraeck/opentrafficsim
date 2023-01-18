package org.opentrafficsim.road.network.lane.object;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * Speed sign.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class SpeedSign extends AbstractLaneBasedObject
{

    /** */
    private static final long serialVersionUID = 20170420L;

    /** Speed limit. */
    private final Speed speed;

    /** GTU type. */
    private final GtuType gtuType;

    /** Start time-of-day. */
    private final Duration startTimeOfDay;

    /** End time-of-day. */
    private final Duration endTimeOfDay;

    /**
     * Construct a new SpeedSign.
     * @param id String; the id of the new SpeedSign
     * @param lane Lane; Lane on/over which the SpeedSign is positioned
     * @param longitudinalPosition Length; the longitudinal position along the lane of the new SpeedSign
     * @param simulator OTSSimulatorInterface; the simulator
     * @param speed Speed; the speed limit shown by the new SpeedSign
     * @param gtuType GtuType; GTU type that should obey the speed sign
     * @param startTimeOfDay Duration; start time-of-day
     * @param endTimeOfDay Duration; end time-of-day
     * @throws NetworkException when the position on the lane is out of bounds
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public SpeedSign(final String id, final Lane lane, final Length longitudinalPosition, final OtsSimulatorInterface simulator,
            final Speed speed, final GtuType gtuType, final Duration startTimeOfDay, final Duration endTimeOfDay)
            throws NetworkException
    {
        super(id, lane, longitudinalPosition, LaneBasedObject.makeGeometry(lane, longitudinalPosition));
        this.speed = speed;
        this.gtuType = gtuType;
        this.startTimeOfDay = startTimeOfDay;
        this.endTimeOfDay = endTimeOfDay;

        init();
    }

    /**
     * Return whether this speed limit is currently active.
     * @param gtuTypeIn GtuType; GTU type
     * @param time Duration; current time-of-day
     * @return whether this speed limit is currently active
     */
    public final boolean isActive(final GtuType gtuTypeIn, final Duration time)
    {
        return gtuTypeIn.isOfType(this.gtuType) && time.ge(this.startTimeOfDay) && time.le(this.endTimeOfDay);
    }

    /**
     * Returns the speed.
     * @return the speed
     */
    public final Speed getSpeed()
    {
        return this.speed;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.endTimeOfDay == null) ? 0 : this.endTimeOfDay.hashCode());
        result = prime * result + ((this.gtuType == null) ? 0 : this.gtuType.hashCode());
        result = prime * result + ((this.speed == null) ? 0 : this.speed.hashCode());
        result = prime * result + ((this.startTimeOfDay == null) ? 0 : this.startTimeOfDay.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj)
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
        SpeedSign other = (SpeedSign) obj;
        if (this.endTimeOfDay == null)
        {
            if (other.endTimeOfDay != null)
            {
                return false;
            }
        }
        else if (!this.endTimeOfDay.equals(other.endTimeOfDay))
        {
            return false;
        }
        if (this.gtuType == null)
        {
            if (other.gtuType != null)
            {
                return false;
            }
        }
        else if (!this.gtuType.equals(other.gtuType))
        {
            return false;
        }
        if (this.speed == null)
        {
            if (other.speed != null)
            {
                return false;
            }
        }
        else if (!this.speed.equals(other.speed))
        {
            return false;
        }
        if (this.startTimeOfDay == null)
        {
            if (other.startTimeOfDay != null)
            {
                return false;
            }
        }
        else if (!this.startTimeOfDay.equals(other.startTimeOfDay))
        {
            return false;
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "SpeedSign [speed=" + this.speed + ", gtuType=" + this.gtuType + ", startTime=" + this.startTimeOfDay
                + ", endTime=" + this.endTimeOfDay + "]";
    }

}
