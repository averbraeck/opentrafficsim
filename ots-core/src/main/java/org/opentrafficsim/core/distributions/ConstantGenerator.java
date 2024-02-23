package org.opentrafficsim.core.distributions;

/**
 * Generator implementation for a constant value.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 * @param <O> type of the object returned by the draw method
 */
public class ConstantGenerator<O> implements Generator<O>
{

    /** Value. */
    private final O value;

    /**
     * Constructor.
     * @param value O; value
     */
    public ConstantGenerator(final O value)
    {
        this.value = value;
    }

    /** {@inheritDoc} */
    @Override
    public O draw()
    {
        return this.value;
    }

    /**
     * Returns the value.
     * @return O; value
     */
    public O getValue()
    {
        return this.value;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "ConstantGenerator [value=" + this.value + "]";
    }

}
