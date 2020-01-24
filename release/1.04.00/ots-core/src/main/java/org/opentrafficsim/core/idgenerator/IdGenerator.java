package org.opentrafficsim.core.idgenerator;

import java.io.Serializable;

/**
 * Generate names for any kind of object.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Mar 4, 2016 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class IdGenerator implements Serializable
{
    /** */
    private static final long serialVersionUID = 1L;

    /** All generated names start with this string. */
    private final String baseName;

    /** Number of the last generated id. */
    private long last = 0;

    /**
     * Construct a new IdGenerator.
     * @param baseName String; all generated names start with this string
     */
    public IdGenerator(final String baseName)
    {
        this.baseName = baseName;
    }

    /**
     * Generate an id.
     * @return String; the generated id
     */
    public final synchronized String nextId()
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
