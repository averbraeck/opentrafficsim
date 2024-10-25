package org.opentrafficsim.core.perception;

import org.djunits.value.vdouble.scalar.Time;

/**
 * Simple implementation without history that can be used inside a generic context where also implementations with history can
 * be used.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <T> value type
 */
public class NullHistorical<T> implements Historical<T>
{

    /** Value. */
    private T val;

    /**
     * Constructor.
     * @param value value
     */
    public NullHistorical(final T value)
    {
        this.val = value;
    }

    @Override
    public void set(final T value)
    {
        this.val = value;
    }

    @Override
    public T get()
    {
        return this.val;
    }

    @Override
    public T get(final Time time)
    {
        return this.val;
    }

    @Override
    public String toString()
    {
        return "NullHistorical [val=" + this.val + "]";
    }

}
