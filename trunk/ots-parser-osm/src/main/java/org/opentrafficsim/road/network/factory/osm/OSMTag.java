package org.opentrafficsim.road.network.factory.osm;

import java.io.Serializable;

/**
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-26 01:01:13 +0200 (Sun, 26 Jul 2015) $, @version $Revision: 1155 $, by $Author: averbraeck $,
 * initial version 31 dec. 2014 <br>
 * @author <a>Moritz Bergmann</a>
 */
public class OSMTag implements Serializable
{
    /** */
    private static final long serialVersionUID = 20141231L;

    /** The (not necessarily unique) Key of the tag. */
    private final String key;

    /** The value of the tag. */
    private final String value;

    /**
     * Retrieve the value of this OSMTag.
     * @return String; the value of this OSMTag
     */
    public final String getValue()
    {
        return this.value;
    }

    /**
     * Retrieve the key of this OSMTag.
     * @return String; the key of this OSMTag
     */
    public final String getKey()
    {
        return this.key;
    }

    /**
     * Construct a new OSMTag.
     * @param key String; the key of the new OSMTag
     * @param value String; the value of the new OSMTag
     */
    public OSMTag(final String key, final String value)
    {
        this.key = key;
        this.value = value;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "Tag: Key: " + this.key + " Value: " + this.value;
    }

}
