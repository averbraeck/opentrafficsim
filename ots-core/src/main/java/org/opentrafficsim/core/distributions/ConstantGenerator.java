package org.opentrafficsim.core.distributions;

/**
 * Generator implementation for a constant value.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 9 dec. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
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
