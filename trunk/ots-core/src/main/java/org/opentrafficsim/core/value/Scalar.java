package org.opentrafficsim.core.value;

import java.io.Serializable;

import org.opentrafficsim.core.unit.Unit;

/**
 * Basics of the Scalar type.
 * <p>
 * Copyright (c) 2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Jun 13, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <U> the unit of the values in the constructor and for display
 */
public abstract class Scalar<U extends Unit<U>> extends Number implements Value<U>, Serializable
{
    /** */
    private static final long serialVersionUID = 20140615L;

    /** The unit of the Scalar. */
    private final U unit;

    /**
     * Construct a new Scalar.
     * @param unit U; the unit of the new Scalar
     */
    public Scalar(final U unit)
    {
        this.unit = unit;
    }

    /** {@inheritDoc} */
    @Override
    public final U getUnit()
    {
        return this.unit;
    }

    /** {@inheritDoc} */
    @Override
    public final double expressAsSIUnit(final double value)
    {
        return ValueUtil.expressAsSIUnit(value, this.unit);
    }

    /**
     * Convert a value from the standard SI unit into the unit of this Scalar.
     * @param value double; the value to convert
     * @return double; the value in the unit of this Scalar
     */
    protected final double expressAsSpecifiedUnit(final double value)
    {
        return ValueUtil.expressAsUnit(value, this.unit);
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isAbsolute()
    {
        return this instanceof Absolute;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isRelative()
    {
        return this instanceof Relative;
    }

}
