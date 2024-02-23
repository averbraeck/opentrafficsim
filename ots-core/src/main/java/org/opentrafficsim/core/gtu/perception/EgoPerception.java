package org.opentrafficsim.core.gtu.perception;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.network.NetworkException;

/**
 * Ego perception interface.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 * @param <G> GTU type
 * @param <P> perception type
 */
public interface EgoPerception<G extends Gtu, P extends Perception<G>> extends PerceptionCategory<G, P>
{

    /**
     * Update speed.
     * @throws GtuException if the GTU has not been initialized
     */
    void updateSpeed() throws GtuException;

    /**
     * Update acceleration.
     * @throws GtuException if the GTU has not been initialized
     */
    void updateAcceleration() throws GtuException;

    /**
     * Update length.
     * @throws GtuException if the GTU has not been initialized
     */
    void updateLength() throws GtuException;

    /**
     * Update width.
     * @throws GtuException if the GTU has not been initialized
     */
    void updateWidth() throws GtuException;

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

    /** {@inheritDoc} */
    @Override
    default void updateAll() throws GtuException, NetworkException, ParameterException
    {
        updateSpeed();
        updateAcceleration();
        updateLength();
        updateWidth();
    }

}
