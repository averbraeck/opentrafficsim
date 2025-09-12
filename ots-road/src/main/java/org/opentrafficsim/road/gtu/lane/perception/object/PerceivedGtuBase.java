package org.opentrafficsim.road.gtu.lane.perception.object;

import java.util.Objects;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.gtu.GtuType;

/**
 * Base class for perceived GTU's which stores the information.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class PerceivedGtuBase extends PerceivedGtuSimple
{

    /** */
    private static final long serialVersionUID = 20250907L;

    /** Behavior. */
    private final Behavior behavior;

    /**
     * Constructor.
     * @param id GTU id
     * @param gtuType GTU type
     * @param length length of the GTU
     * @param width width of the GTU
     * @param kinematics kinematics
     * @param signals signals
     * @param maneuver maneuver
     * @param behavior behavior
     * @throws NullPointerException when any input argument is {@code null}
     */
    @SuppressWarnings("parameternumber")
    public PerceivedGtuBase(final String id, final GtuType gtuType, final Length length, final Length width,
            final Kinematics kinematics, final Signals signals, final Maneuver maneuver, final Behavior behavior)
    {
        super(id, gtuType, length, width, kinematics, signals, maneuver);
        this.behavior = Throw.whenNull(behavior, "behavior");
    }

    @Override
    public Behavior getBehavior()
    {
        return this.behavior;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(this.behavior);
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
        PerceivedGtuBase other = (PerceivedGtuBase) obj;
        return Objects.equals(this.behavior, other.behavior);
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "PerceivedGtuBase [id=" + getId() + ", gtuType=" + getGtuType() + "]";
    }

}
