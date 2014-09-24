package org.opentrafficsim.core.value;

import java.io.Serializable;

import org.opentrafficsim.core.unit.Unit;

/**
 * AbstractValue is a class to help construct Matrix, Complex, and Vector but it does not extend java.lang.Number. The Scalar
 * class <i>does</i> extend Number, and implements the same interfaces from Value.
 * <p>
 * Copyright (c) 2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Jun 13, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <U> the unit of the values in the constructor and for display
 */
public abstract class AbstractValue<U extends Unit<U>> implements Value<U>, Serializable
{
    /** */
    private static final long serialVersionUID = 20140615L;

    /** the unit of the value. */
    private final U unit;

    /**
     * @param unit the unit of the value
     */
    public AbstractValue(final U unit)
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
     * @param value the value to convert in the specified unit for this scalar
     * @return the value in the unit as specified for this scalar
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
