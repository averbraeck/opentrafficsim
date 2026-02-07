package org.opentrafficsim.road.gtu.lane.perception.object;

import java.util.Objects;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Throw;

/**
 * Base class for perceived objects which stores the information.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class PerceivedObjectBase implements PerceivedObject
{

    /** Object id. */
    private final String id;

    /** Object type. */
    private final ObjectType objectType;

    /** Length of the object. */
    private final Length length;

    /** Kinematics of the object. */
    private final Kinematics kinematics;

    /**
     * Constructor.
     * @param id object id
     * @param objectType object type
     * @param length length of the object
     * @param kinematics kinematics of the object
     * @throws NullPointerException when any input argument is {@code null}
     */
    public PerceivedObjectBase(final String id, final ObjectType objectType, final Length length, final Kinematics kinematics)
    {
        this.id = Throw.whenNull(id, "id");
        this.objectType = Throw.whenNull(objectType, "objectType");
        this.length = Throw.whenNull(length, "length");
        this.kinematics = Throw.whenNull(kinematics, "kinematics");
    }

    @Override
    public String getId()
    {
        return this.id;
    }

    @Override
    public ObjectType getObjectType()
    {
        return this.objectType;
    }

    @Override
    public Length getLength()
    {
        return this.length;
    }

    @Override
    public Kinematics getKinematics()
    {
        return this.kinematics;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
        return Objects.hash(this.id, this.kinematics, this.length, this.objectType);
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
        PerceivedObjectBase other = (PerceivedObjectBase) obj;
        return Objects.equals(this.id, other.id) && Objects.equals(this.kinematics, other.kinematics)
                && Objects.equals(this.length, other.length) && this.objectType == other.objectType;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "PerceivedObjectBase [id=" + this.id + ", objectType=" + this.objectType + "]";
    }

}
