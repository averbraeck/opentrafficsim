package org.opentrafficsim.importexport.osm;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 31 dec. 2014 <br>
 * @author <a>Moritz Bergmann</a>
 */
public class Tag
{
    /** */
    private String key;

    /** */
    private String value;

    /**
     * @return value
     */
    public final String getValue()
    {
        return this.value;
    }

    /**
     * @param value 
     */
    public final void setValue(final String value)
    {
        this.value = value;
    }

    /**
     * @return key 
     */
    public final String getKey()
    {
        return this.key;
    }

    /**
     * @param key 
     */
    public final void setKey(final String key)
    {
        this.key = key;
    }

    /**
     * @param k 
     * @param v 
     */
    public Tag(final String k, final String v)
    {
        this.key = k;
        this.value = v;
    }
}
