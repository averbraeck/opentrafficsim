package org.opentrafficsim.core.gtu.perception;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.Gtu;

/**
 * Direct perception of ego values such as speed and length.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <G> GTU type
 * @param <P> perception type
 */
public class DirectEgoPerception<G extends Gtu, P extends Perception<G>> extends AbstractPerceptionCategory<G, P>
        implements EgoPerception<G, P>
{

    /**
     * Constructor.
     * @param perception perception
     */
    public DirectEgoPerception(final P perception)
    {
        super(perception);
    }

    @Override
    public Acceleration getAcceleration()
    {
        return getGtu().getAcceleration();
    }

    @Override
    public Speed getSpeed()
    {
        return getGtu().getSpeed();
    }

    @Override
    public Speed getMaximumSpeed()
    {
        return getGtu().getMaximumSpeed();
    }

    @Override
    public Length getLength()
    {
        return getGtu().getLength();
    }

    @Override
    public Length getWidth()
    {
        return getGtu().getWidth();
    }

    @Override
    public String toString()
    {
        return "DirectEgoPerception " + cacheAsString();
    }

}
