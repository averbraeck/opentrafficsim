package org.opentrafficsim.core.gtu.perception;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.Gtu;

/**
 * Ego perception interface.
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
public interface EgoPerception<G extends Gtu, P extends Perception<G>> extends PerceptionCategory<G, P>
{

    /**
     * Returns the acceleration.
     * @return acceleration
     */
    Acceleration getAcceleration();

    /**
     * Returns the speed.
     * @return speed
     */
    Speed getSpeed();

    /**
     * Returns the length.
     * @return length
     */
    Length getLength();

    /**
     * Returns the width.
     * @return width
     */
    Length getWidth();

}
