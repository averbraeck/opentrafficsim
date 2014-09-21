package org.opentrafficsim.core.location;

import nl.tudelft.simulation.dsol.animation.LocatableInterface;

import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * <p>
 * Copyright (c) 2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version May 15, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://opentrafficsim.org/yufei">Yufei Yuan</a>
 * @param <E> the edge type.
 */
public interface LocationRelative<E extends Edge<?>> extends Location, LocatableInterface
{
    /**
     * @return the edge.
     */
    E getEdge();

    /**
     * @return the position on the line as a fraction between 0 and 1.
     */
    double getFractionalPosition();

    /**
     * @return the fractional position as a length.
     */
    DoubleScalar<LengthUnit> getPosition();
}
