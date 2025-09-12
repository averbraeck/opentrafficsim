package org.opentrafficsim.road.gtu.lane.perception.object;

import java.util.Objects;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * Lane based perceived object.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class PerceivedLaneBasedObject extends PerceivedObjectBase
{

    /** */
    private static final long serialVersionUID = 20250908L;

    /** Lane. */
    private final Lane lane;

    /**
     * Constructor.
     * @param id object id
     * @param objectType object type
     * @param length length of the object
     * @param kinematics kinematics of the object
     * @param lane lane
     */
    public PerceivedLaneBasedObject(final String id, final ObjectType objectType, final Length length,
            final Kinematics kinematics, final Lane lane)
    {
        super(id, objectType, length, kinematics);
        this.lane = lane;
    }

    /**
     * Lane at which the object is located.
     * @return lane at which the object is located
     */
    public Lane getLane()
    {
        return this.lane;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(this.lane);
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
        if (!super.equals(obj))
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        PerceivedLaneBasedObject other = (PerceivedLaneBasedObject) obj;
        return Objects.equals(this.lane, other.lane);
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "PerceivedLaneBasedObject [id=" + getId() + ", type=" + getObjectType() + "]";
    }

}
