package org.opentrafficsim.road.gtu.lane.perception.headway;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;

/**
 * Container for a reference to information about a headway with just a distance, without any further information about the
 * object; it assumes a speed of 0 at the headway, so it also good to store information about a lane drop. The reason for
 * storing a speed of zero at the end of a maximum headway is that we did not check the conditions beyond that point. A GTU or
 * lane drop could be right behind the last point we checked.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public class HeadwayDistance implements Headway
{
    /** */
    private static final long serialVersionUID = 20160410L;

    /** the distance of the headway. */
    private final Length distance;

    /**
     * Construct a new Headway information object with just a distance, without any further information about the object; it
     * assumes a speed of 0 at the headway, so it also good to store information about a lane drop.
     * @param distance the distance that needs to be stored.
     */
    public HeadwayDistance(final double distance)
    {
        this(new Length(distance, LengthUnit.SI));
    }

    /**
     * Construct a new Headway information object with just a distance, without any further information about the object; it
     * assumes a speed of 0 at the headway, so it also good to store information about a lane drop.
     * @param distance the distance that needs to be stored.
     */
    public HeadwayDistance(final Length distance)
    {
        this.distance = distance;
    }

    @Override
    public final String getId()
    {
        return "DISTANCE";
    }

    @Override
    public final Length getLength()
    {
        return null;
    }

    @Override
    public final Speed getSpeed()
    {
        return Speed.ZERO;
    }

    @Override
    public final Length getDistance()
    {
        return this.distance;
    }

    @Override
    public final ObjectType getObjectType()
    {
        return ObjectType.DISTANCEONLY;
    }

    @Override
    public final Acceleration getAcceleration()
    {
        return Acceleration.ZERO;
    }

    @Override
    public final Length getOverlapFront()
    {
        return null;
    }

    @Override
    public final Length getOverlapRear()
    {
        return null;
    }

    @Override
    public final Length getOverlap()
    {
        return null;
    }

    @Override
    public final boolean isAhead()
    {
        return this.distance.ge0();
    }

    @Override
    public final boolean isBehind()
    {
        return this.distance.lt0();
    }

    @Override
    public final boolean isParallel()
    {
        return false;
    }

    @Override
    public final String toString()
    {
        return "HeadwayDistance [distance=" + this.distance + "]";
    }

}
