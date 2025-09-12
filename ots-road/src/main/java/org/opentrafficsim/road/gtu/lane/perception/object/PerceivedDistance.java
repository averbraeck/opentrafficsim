package org.opentrafficsim.road.gtu.lane.perception.object;

import org.djunits.value.vdouble.scalar.Length;

import com.google.common.base.Objects;

/**
 * Generic class for any static object at a distance, without specifying what the object type is.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class PerceivedDistance extends PerceivedObjectBase
{

    /** */
    private static final long serialVersionUID = 20250908L;

    /**
     * Construct a new Headway information object with just a distance, without any further information about the object; it
     * assumes a speed of 0 at the headway, so it also good to store information about a lane drop.
     * @param distance the distance that needs to be stored.
     */
    public PerceivedDistance(final Length distance)
    {
        super("DISTANCE", ObjectType.DISTANCEONLY, Length.ZERO,
                distance.gt0() ? Kinematics.staticAhead(distance) : Kinematics.staticBehind(distance.neg()));
    }

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
        return Objects.equal(getKinematics().getDistance(), ((PerceivedDistance) obj).getKinematics().getDistance());
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(getKinematics().getDistance());
    }

    @Override
    public final String toString()
    {
        return "PerceivedDistance [distance=" + getKinematics().getDistance() + "]";
    }

}
