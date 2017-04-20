package org.opentrafficsim.road.network.lane.object;

import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.animation.SpeedSignAnimation;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * Speed sign.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
    private static final Time ENDOFDAY = new Time(24, TimeUnit.HOUR);

    /** Speed limit. */
    private final Speed speed;

    /** GTU type. */
    private final GTUType gtuType;

    /** Start time. */
    private final Time startTime;

    /** End time. */
    private final Time endTime;

    /**
     * Speed sign.
     * @param id id
     * @param lane lane
     * @param direction direction
     * @param longitudinalPosition longitudinal position
     * @param simulator simulator
     * @param speed speed
     * @param gtuType GTU type
     * @param startTime start time of day
     * @param endTime end time of day
     * @throws NetworkException when the position on the lane is out of bounds
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public SpeedSign(final String id, final Lane lane, final LongitudinalDirectionality direction,
            final Length longitudinalPosition, final OTSSimulatorInterface simulator, final Speed speed, final GTUType gtuType,
            final Time startTime, final Time endTime) throws NetworkException
    {
        super(id, lane, direction, longitudinalPosition, LaneBasedObject.makeGeometry(lane, longitudinalPosition));
        this.speed = speed;
        this.gtuType = gtuType;
        this.startTime = startTime;
        this.endTime = endTime;
        
        try
        {
            new SpeedSignAnimation(this, simulator);
        }
        catch (RemoteException | NamingException exception)
        {
            throw new NetworkException(exception);
        }
    }

    /**
     * Speed sign active all day.
     * @param id id
     * @param lane lane
     * @param direction direction
     * @param longitudinalPosition longitudinal position
     * @param simulator simulator
     * @param speed speed
     * @param gtuType GTU type
     * @throws NetworkException when the position on the lane is out of bounds
     */
    public SpeedSign(final String id, final Lane lane, final LongitudinalDirectionality direction,
            final Length longitudinalPosition, final OTSSimulatorInterface simulator, final Speed speed, final GTUType gtuType)
            throws NetworkException
    {
        this(id, lane, direction, longitudinalPosition, simulator, speed, gtuType, Time.ZERO, ENDOFDAY);
    }

    /**
     * Speed sign for all GTU types.
     * @param id id
     * @param lane lane
     * @param direction direction
     * @param longitudinalPosition longitudinal position
     * @param simulator simulator
     * @param speed speed
     * @param startTime start time of day
     * @param endTime end time of day
     * @throws NetworkException when the position on the lane is out of bounds
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public SpeedSign(final String id, final Lane lane, final LongitudinalDirectionality direction,
            final Length longitudinalPosition, final OTSSimulatorInterface simulator, final Speed speed, final Time startTime,
            final Time endTime) throws NetworkException
    {
        this(id, lane, direction, longitudinalPosition, simulator, speed, GTUType.ALL, startTime, endTime);
    }

    /**
     * Speed sign active all day for all GTU types.
     * @param id id
     * @param lane lane
     * @param direction direction
     * @param longitudinalPosition longitudinal position
     * @param simulator simulator
     * @param speed speed
     * @throws NetworkException when the position on the lane is out of bounds
     */
    public SpeedSign(final String id, final Lane lane, final LongitudinalDirectionality direction,
            final Length longitudinalPosition, final OTSSimulatorInterface simulator, final Speed speed) throws NetworkException
    {
        this(id, lane, direction, longitudinalPosition, simulator, speed, GTUType.ALL, Time.ZERO, ENDOFDAY);
    }

    /**
     * Return whether this speed limit is currently active.
     * @param gtuTypeIn GTU type
     * @param time current time
     * @return whether this speed limit is currently active
     */
    public boolean isActive(final GTUType gtuTypeIn, final Time time)
    {
        return gtuTypeIn.isOfType(this.gtuType) && time.ge(this.startTime) && time.le(this.endTime);
    }

    /**
     * Returns the speed.
     * @return the speed
     */
    public Speed getSpeed()
    {
        return this.speed;
    }

    /** {@inheritDoc} */
    @Override
    public AbstractLaneBasedObject clone(final CrossSectionElement newCSE, final OTSSimulatorInterface newSimulator,
            final boolean animation) throws NetworkException
    {
        return new SpeedSign(getId(), (Lane) newCSE, getDirection(), getLongitudinalPosition(), newSimulator, this.speed,
                this.gtuType, this.startTime, this.endTime);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.endTime == null) ? 0 : this.endTime.hashCode());
        result = prime * result + ((this.gtuType == null) ? 0 : this.gtuType.hashCode());
        result = prime * result + ((this.speed == null) ? 0 : this.speed.hashCode());
        result = prime * result + ((this.startTime == null) ? 0 : this.startTime.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj)
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
    public String toString()
    {
        return "SpeedSign [speed=" + this.speed + ", gtuType=" + this.gtuType + ", startTime=" + this.startTime + ", endTime="
                + this.endTime + "]";
    }
    
}
