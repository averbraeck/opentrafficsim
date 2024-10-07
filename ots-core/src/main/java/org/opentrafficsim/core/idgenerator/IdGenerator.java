package org.opentrafficsim.core.idgenerator;

import java.io.Serializable;
import java.util.function.Supplier;

/**
 * Generate names for any kind of object.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public class IdGenerator implements Serializable, Supplier<String>
{
    /** */
    private static final long serialVersionUID = 1L;

    /** All generated names start with this string. */
    private final String baseName;

    /** Number of the last generated id. */
    private long last = 0;

    /**
     * Construct a new IdGenerator.
     * @param baseName all generated names start with this string
     */
    public IdGenerator(final String baseName)
    {
        this.baseName = baseName;
    }

    /**
     * Generate an id.
     * @return the generated id
     */
    @Override
    public final synchronized String get()
    {
        long number;
        synchronized (this)
        {
            number = ++this.last;
        }
        return this.baseName + number;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "IdGenerator [baseName=" + this.baseName + ", last=" + this.last + "]";
    }

}
