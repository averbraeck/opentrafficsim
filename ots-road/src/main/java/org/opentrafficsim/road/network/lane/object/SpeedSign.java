package org.opentrafficsim.road.network.lane.object;

import org.djunits.unit.DurationUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.Lane;

import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

/**
 * Speed sign.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 20 apr. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class SpeedSign extends AbstractLaneBasedObject
{

    /** */
    private static final long serialVersionUID = 20170420L;

    /** End of day. */
    private static final Duration ENDOFDAY = new Duration(24, DurationUnit.HOUR);

    /** Speed limit. */
    private final Speed speed;

    /** GTU type. */
    private final GTUType gtuType;

    /** Start time-of-day. */
    private final Duration startTimeOfDay;

    /** End time-of-day. */
    private final Duration endTimeOfDay;

    /**
     * Construct a new SpeedSign.
     * @param id String; the id of the new SpeedSign
     * @param lane Lane; Lane on/over which the SpeedSign is positioned
     * @param direction LongitudinalDirectionality; driving direction for which the new SpeedSign applies
     * @param longitudinalPosition Length; the longitudinal position along the lane of the new SpeedSign
     * @param simulator SimulatorInterface.TimeDoubleUnit; the simulator
     * @param speed Speed; the speed limit shown by the new SpeedSign
     * @param gtuType GTUType; GTU type that should obey the speed sign
     * @param startTimeOfDay Duration; start time-of-day
     * @param endTimeOfDay Duration; end time-of-day
     * @throws NetworkException when the position on the lane is out of bounds
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public SpeedSign(final String id, final Lane lane, final LongitudinalDirectionality direction,
            final Length longitudinalPosition, final SimulatorInterface.TimeDoubleUnit simulator, final Speed speed,
            final GTUType gtuType, final Duration startTimeOfDay, final Duration endTimeOfDay) throws NetworkException
    {
        super(id, lane, direction, longitudinalPosition, LaneBasedObject.makeGeometry(lane, longitudinalPosition));
        this.speed = speed;
        this.gtuType = gtuType;
        this.startTimeOfDay = startTimeOfDay;
        this.endTimeOfDay = endTimeOfDay;
    }

    /**
     * Speed sign active all day.
     * @param id String; id
     * @param lane Lane; lane
     * @param direction LongitudinalDirectionality; direction
     * @param longitudinalPosition Length; longitudinal position
     * @param simulator SimulatorInterface.TimeDoubleUnit; simulator
     * @param speed Speed; speed
     * @param gtuType GTUType; GTU type
     * @throws NetworkException when the position on the lane is out of bounds
     */
    public SpeedSign(final String id, final Lane lane, final LongitudinalDirectionality direction,
            final Length longitudinalPosition, final SimulatorInterface.TimeDoubleUnit simulator, final Speed speed,
            final GTUType gtuType) throws NetworkException
    {
        this(id, lane, direction, longitudinalPosition, simulator, speed, gtuType, Duration.ZERO, ENDOFDAY);
    }

    /**
     * Speed sign for all GTU types.
     * @param id String; id
     * @param lane Lane; lane
     * @param direction LongitudinalDirectionality; direction
     * @param longitudinalPosition Length; longitudinal position
     * @param simulator SimulatorInterface.TimeDoubleUnit; simulator
     * @param speed Speed; speed
     * @param startTimeOfDay Duration; start time-of-day
     * @param endTimeOfDay Duration; end time-of-day
     * @throws NetworkException when the position on the lane is out of bounds
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public SpeedSign(final String id, final Lane lane, final LongitudinalDirectionality direction,
            final Length longitudinalPosition, final SimulatorInterface.TimeDoubleUnit simulator, final Speed speed,
            final Duration startTimeOfDay, final Duration endTimeOfDay) throws NetworkException
    {
        this(id, lane, direction, longitudinalPosition, simulator, speed,
                lane.getNetwork().getGtuType(GTUType.DEFAULTS.VEHICLE), startTimeOfDay, endTimeOfDay);
    }

    /**
     * Speed sign active all day for all GTU types.
     * @param id String; id
     * @param lane Lane; lane
     * @param direction LongitudinalDirectionality; direction
     * @param longitudinalPosition Length; longitudinal position
     * @param simulator SimulatorInterface.TimeDoubleUnit; simulator
     * @param speed Speed; speed
     * @throws NetworkException when the position on the lane is out of bounds
     */
    public SpeedSign(final String id, final Lane lane, final LongitudinalDirectionality direction,
            final Length longitudinalPosition, final SimulatorInterface.TimeDoubleUnit simulator, final Speed speed)
            throws NetworkException
    {
        this(id, lane, direction, longitudinalPosition, simulator, speed,
                lane.getNetwork().getGtuType(GTUType.DEFAULTS.VEHICLE), Duration.ZERO, ENDOFDAY);
    }

    /**
     * Return whether this speed limit is currently active.
     * @param gtuTypeIn GTUType; GTU type
     * @param time Duration; current time-of-day
     * @return whether this speed limit is currently active
     */
    public final boolean isActive(final GTUType gtuTypeIn, final Duration time)
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
    public final AbstractLaneBasedObject clone(final CrossSectionElement newCSE,
            final SimulatorInterface.TimeDoubleUnit newSimulator) throws NetworkException
    {
        return new SpeedSign(getId(), (Lane) newCSE, getDirection(), getLongitudinalPosition(), newSimulator, this.speed,
                this.gtuType, this.startTimeOfDay, this.endTimeOfDay);
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
