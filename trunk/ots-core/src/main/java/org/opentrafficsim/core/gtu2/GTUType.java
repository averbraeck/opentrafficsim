package org.opentrafficsim.core.gtu2;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A GTU type identifies the type of a GTU. <br>
 * GTU types are used to check whether a particular GTU can travel over a particular part of infrastructure. E.g. a
 * (LaneBased)GTU with GTUType CAR can travel over lanes that have a LaneType that has the GTUType CAR in the compatibility set.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision: 1320 $, $LastChangedDate: 2015-08-29 13:54:21 +0200 (Sat, 29 Aug 2015) $, by $Author: averbraeck $,
 *          initial version Dec 31, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public final class GTUType implements Serializable
{
    /** */
    private static final long serialVersionUID = 20141231L;

    /** The id of the GTUType to make it identifiable. */
    private final String id;

    /** ALL GTUType to be used only for permeability and accessibility. */
    public static final GTUType ALL;

    /** NONE GTUType to be used only for permeability and accessibility. */
    public static final GTUType NONE;

    /** The set of previously instantiated GTUTypes. */
    private static final Map<String, GTUType> INSTANTIATEDGTUTYPES = new LinkedHashMap<String, GTUType>();

    static
    {
        ALL = new GTUType("ALL");
        INSTANTIATEDGTUTYPES.put("ALL", GTUType.ALL);

        NONE = new GTUType("NONE");
        INSTANTIATEDGTUTYPES.put("NONE", GTUType.NONE);
    }

    /**
     * @param id The id of the GTUType to make it identifiable.
     */
    private GTUType(final String id)
    {
        this.id = id;
        INSTANTIATEDGTUTYPES.put(id, this);
    }

    /**
     * Construct a new GTUType or (if it already exists) return an existing GTUType.
     * @param id String; the id of the GTUType
     * @return GTUType&lt;ID&gt;
     */
    public static GTUType makeGTUType(final String id)
    {
        synchronized (INSTANTIATEDGTUTYPES)
        {
            GTUType result = INSTANTIATEDGTUTYPES.get(id);
            if (null == result)
            {
                result = new GTUType(id);
            }
            return result;
        }
    }

    /**
     * @return id.
     */
    public String getId()
    {
        return this.id;
    }

    /** {@inheritDoc} */
    public String toString()
    {
        return "GTUType: " + this.id;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:needbraces")
    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GTUType other = (GTUType) obj;
        if (this.id == null)
        {
            if (other.id != null)
                return false;
        }
        else if (!this.id.equals(other.id))
            return false;
        return true;
    }
}
