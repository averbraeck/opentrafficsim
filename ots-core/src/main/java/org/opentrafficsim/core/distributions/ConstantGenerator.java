package org.opentrafficsim.core.distributions;

/**
 * Generator implementation for a constant value.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <O> type of the object returned by the draw method
 */
public class ConstantGenerator<O> implements Generator<O>
{

    /** Value. */
    private final O value;

    /**
     * Constructor.
     * @param value value
     */
    public ConstantGenerator(final O value)
    {
        this.value = value;
    }

    @Override
    public O draw()
    {
        return this.value;
    }

    /**
     * Returns the value.
     * @return value
     */
    public O getValue()
    {
        return this.value;
    }

    @Override
    public String toString()
    {
        return "ConstantGenerator [value=" + this.value + "]";
    }

}
