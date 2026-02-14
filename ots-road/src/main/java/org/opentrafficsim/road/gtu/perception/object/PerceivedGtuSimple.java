package org.opentrafficsim.road.gtu.perception.object;

import java.util.Objects;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.road.gtu.LaneBasedGtu;

/**
 * Simple implementation of perceived GTU's which stores the information. This class does not support the behavioral component.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class PerceivedGtuSimple extends PerceivedObjectBase implements PerceivedGtu
{

    /** GTU type. */
    private final GtuType gtuType;

    /** GTU width. */
    private final Length width;

    /** Signals. */
    private final Signals signals;

    /** Maneuver. */
    private final Maneuver maneuver;

    /**
     * Constructor.
     * @param id GTU id
     * @param gtuType GTU type
     * @param length length of the GTU
     * @param width width of the GTU
     * @param kinematics kinematics
     * @param signals signals
     * @param maneuver maneuver
     * @throws NullPointerException when any input argument is {@code null}
     */
    public PerceivedGtuSimple(final String id, final GtuType gtuType, final Length length, final Length width,
            final Kinematics kinematics, final Signals signals, final Maneuver maneuver)
    {
        super(id, ObjectType.GTU, length, kinematics);
        this.gtuType = Throw.whenNull(gtuType, "gtuType");
        this.width = Throw.whenNull(width, "width");
        this.signals = Throw.whenNull(signals, "signals");
        this.maneuver = Throw.whenNull(maneuver, "maneuver");
    }

    @Override
    public Length getWidth()
    {
        return this.width;
    }

    @Override
    public GtuType getGtuType()
    {
        return this.gtuType;
    }

    @Override
    public Signals getSignals()
    {
        return this.signals;
    }

    @Override
    public Maneuver getManeuver()
    {
        return this.maneuver;
    }

    @Override
    public Behavior getBehavior()
    {
        throw new UnsupportedOperationException("HeadwayGtuSimple does not support behavior in HeadwyaGtu.");
    }

    /**
     * Returns perceived GTU with given kinematics, but without {@code Behavior}.
     * @param gtu GTU that is perceived
     * @param kinematics kinematics for the vehicle
     * @return perceived view of the GTU
     */
    public static PerceivedGtuSimple of(final LaneBasedGtu gtu, final Kinematics kinematics)
    {
        return new PerceivedGtuSimple(gtu.getId(), gtu.getType(), gtu.getLength(), gtu.getWidth(), kinematics, Signals.of(gtu),
                Maneuver.of(gtu));
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(this.gtuType, this.maneuver, this.signals, this.width);
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
        PerceivedGtuSimple other = (PerceivedGtuSimple) obj;
        return Objects.equals(this.gtuType, other.gtuType) && Objects.equals(this.maneuver, other.maneuver)
                && Objects.equals(this.signals, other.signals) && Objects.equals(this.width, other.width);
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "PerceivedGtuSimple [id=" + getId() + ", gtuType=" + this.gtuType + "]";
    }

}
