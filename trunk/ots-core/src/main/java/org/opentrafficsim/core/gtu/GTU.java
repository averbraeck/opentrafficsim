package org.opentrafficsim.core.gtu;

import java.io.Serializable;

import nl.tudelft.simulation.dsol.animation.LocatableInterface;

import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * <p>
 * Copyright (c) 2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version May 15, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @param <ID> The type of ID, e.g., String or Integer
 */
public interface GTU<ID> extends LocatableInterface, Serializable
{
    /** @return the id of the GTU, could be String or Integer */
    ID getId();

    /** @return the maximum length of the GTU (parallel with driving direction). */
    DoubleScalar.Rel<LengthUnit> getLength();

    /** @return the maximum width of the GTU (perpendicular to driving direction). */
    DoubleScalar.Rel<LengthUnit> getWidth();

    /** @return the maximum velocity of the GTU, in the linear direction */
    DoubleScalar.Abs<SpeedUnit> getMaximumVelocity();

    /** @return the type of GTU, e.g. TruckType, CarType, BusType */
    GTUType<?> getGTUType();

    /** @return the simulator of the GTU. */
    OTSDEVSSimulatorInterface getSimulator();

    /** @return the front position of the GTU, relative to its reference point. */
    RelativePosition getFront();

    /** @return the rear position of the GTU, relative to its reference point. */
    RelativePosition getRear();
}
