package org.opentrafficsim.importexport.osm;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 31 dec. 2014 <br>
 * @author <a>Moritz Bergmann</a>
 */
public class OSMTag
{
    /** (Non-Unique) Key of the tag. */
    private final String key;

    /** Value of the tag. */
    private final String value;

    /**
     * @return value
     */
    public final String getValue()
    {
        return this.value;
    }

    /**
     * @return key
     */
    public final String getKey()
    {
        return this.key;
    }

    /**
     * @param k
     * @param v
     */
    public OSMTag(final String k, final String v)
    {
        this.key = k;
        this.value = v;
    }

    /** {@inheritDoc} */
    public final String toString()
    {
        return "Tag: Key: " + this.key + " Value: " + this.value;
    }
}
