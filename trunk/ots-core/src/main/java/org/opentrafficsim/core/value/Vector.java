package org.opentrafficsim.core.value;

import java.io.Serializable;

import org.opentrafficsim.core.unit.Unit;

/**
 * <p>
 * Copyright (c) 2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Jun 13, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <U> the unit of the values in the constructor and for display
 */
public abstract class Vector<U extends Unit<U>> extends AbstractValue<U> implements Serializable, MathFunctions,
        VectorFunctions
{
    /** */
    private static final long serialVersionUID = 20140615L;

    /**
     * Create a new Vector.
     * @param unit Unit; the unit of the new Vector
     */
    public Vector(final U unit)
    {
        super(unit);
    }

}
