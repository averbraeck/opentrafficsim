package org.opentrafficsim.core.gtu.perception;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.Gtu;

/**
 * Direct perception of ego values such as speed and length.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <G> GTU type
 * @param <P> perception type
 */

public class DirectEgoPerception<G extends Gtu, P extends Perception<G>> extends AbstractPerceptionCategory<G, P>
        implements EgoPerception<G, P>
{

    /** */
    private static final long serialVersionUID = 20160811L;

    /**
     * Constructor.
     * @param perception perception
     */
    public DirectEgoPerception(final P perception)
    {
        super(perception);
    }

    /** {@inheritDoc} */
    @Override
    public final Acceleration getAcceleration()
    {
        return computeIfAbsent("acceleration", () -> getGtu().getAcceleration());
    }

    /** {@inheritDoc} */
    @Override
    public final Speed getSpeed()
    {
        return computeIfAbsent("speed", () -> getGtu().getSpeed());
    }

    /** {@inheritDoc} */
    @Override
    public final Length getLength()
    {
        return computeIfAbsent("length", () -> getGtu().getLength());
    }

    /** {@inheritDoc} */
    @Override
    public final Length getWidth()
    {
        return computeIfAbsent("width", () -> getGtu().getWidth());
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "DirectEgoPerception " + cacheAsString();
    }

}
