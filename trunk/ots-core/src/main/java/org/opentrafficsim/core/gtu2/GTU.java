package org.opentrafficsim.core.gtu2;

import java.io.Serializable;
import java.util.Map;

import nl.tudelft.simulation.dsol.animation.LocatableInterface;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;

/**
 * Generalized Travel Unit. <br>
 * A GTU is an object (person, car, ship) that can travel over the infrastructure.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision: 1165 $, $LastChangedDate: 2015-07-28 17:11:47 +0200 (Tue, 28 Jul 2015) $, by $Author: averbraeck $,
 *          initial version May 15, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public interface GTU extends LocatableInterface, Serializable
{
    /** @return the id of the GTU */
    String getId();

    /** @return the maximum length of the GTU (parallel with driving / moving direction). */
    Length.Rel getLength();

    /** @return the maximum width of the GTU (perpendicular to driving / moving direction). */
    Length.Rel getWidth();

    /** @return the maximum velocity of the GTU, in the driving / moving direction */
    Speed getMaximumVelocity();

    /** @return the type of GTU, e.g. TruckType, CarType, BusType */
    GTUType getGTUType();

    /** @return the simulator of the GTU. */
    OTSDEVSSimulatorInterface getSimulator();

    /** @return the reference position of the GTU, by definition (0, 0, 0). */
    RelativePosition getReference();

    /** @return the front position of the GTU, relative to its reference point. */
    RelativePosition getFront();

    /** @return the rear position of the GTU, relative to its reference point. */
    RelativePosition getRear();

    /**
     * @return the current velocity of the GTU in its moving direction, combining longitudinal, lateral and vertical speed
     *         components.
     */
    Speed getVelocity();

    /** @return the positions for this GTU. */
    Map<RelativePosition.TYPE, RelativePosition> getRelativePositions();

    /** destroy the vehicle from the simulation and animation. */
    void destroy();

    /**
     * @return the current acceleration of the GTU in its moving direction, combining longitudinal, lateral and vertical
     *         acceleration components.
     */
    Acceleration getAcceleration();

    /** @return Length.Abs; the current odometer value */
    Length.Abs getOdometer();

}
