package org.opentrafficsim.core.gtu.perception;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.network.NetworkException;

/**
 * Ego perception interface.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 13 feb. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <G> GTU type
 * @param <P> perception type
 */
public interface EgoPerception<G extends GTU, P extends Perception<G>> extends PerceptionCategory<G, P>
{

    /**
     * Update speed.
     * @throws GTUException if the GTU has not been initialized
     */
    void updateSpeed() throws GTUException;

    /**
     * Update acceleration.
     * @throws GTUException if the GTU has not been initialized
     */
    void updateAcceleration() throws GTUException;

    /**
     * Update length.
     * @throws GTUException if the GTU has not been initialized
     */
    void updateLength() throws GTUException;

    /**
     * Update width.
     * @throws GTUException if the GTU has not been initialized
     */
    void updateWidth() throws GTUException;

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
    default void updateAll() throws GTUException, NetworkException, ParameterException
    {
        updateSpeed();
        updateAcceleration();
        updateLength();
        updateWidth();
    }

}
