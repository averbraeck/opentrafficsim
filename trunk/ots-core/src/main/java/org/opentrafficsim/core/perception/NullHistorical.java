package org.opentrafficsim.core.perception;

import org.djunits.value.vdouble.scalar.Time;

/**
 * Simple implementation without history that can be used inside a generic context where also implementations with history can
 * be used.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 1 feb. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <T> value type
 */
public class NullHistorical<T> implements Historical<T>
{

    /** Value. */
    private T val;

    /**
     * Constructor.
     * @param value T; value
     */
    public NullHistorical(final T value)
    {
        this.val = value;
    }

    /** {@inheritDoc} */
    @Override
    public void set(final T value)
    {
        this.val = value;
    }

    /** {@inheritDoc} */
    @Override
    public T get()
    {
        return this.val;
    }

    /** {@inheritDoc} */
    @Override
    public T get(final Time time)
    {
        return this.val;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "NullHistorical [val=" + this.val + "]";
    }

}
