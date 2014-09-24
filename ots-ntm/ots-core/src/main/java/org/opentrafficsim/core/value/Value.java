package org.opentrafficsim.core.value;

import org.opentrafficsim.core.unit.Unit;

/**
 * Value is a static interface that implements a couple of unit-related static methods.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Aug 18, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <U> the unit type.
 */
public interface Value<U extends Unit<U>>
{
    /**
     * @return unit
     */
    U getUnit();

    /**
     * @param value the value to convert in SI units
     * @return the value in SI units
     */
    double expressAsSIUnit(final double value);

    /**
     * @return whether the value is absolute.
     */
    boolean isAbsolute();

    /**
     * @return whether the value is relative.
     */
    boolean isRelative();

    /**
     * @return a copy of the object
     */
    Value<U> copy();
}
