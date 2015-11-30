package org.opentrafficsim.core.network;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.opentrafficsim.core.gtu.GTUType;

/**
 * Link type to indicate compatibility with GTU types. 
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-08-30 00:16:51 +0200 (Sun, 30 Aug 2015) $, @version $Revision: 1329 $, by $Author: averbraeck $,
 * initial version Aug 21, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class LinkType implements Serializable
{
    /** */
    private static final long serialVersionUID = 20140821L;

    /** The id of the link type to make it identifiable. Should be unique in the simulation. */
    private final String id;

    /** the compatibility of GTU types with this link type. */
    private final Set<GTUType> compatibilitySet = new HashSet<>();

    /** link type that does not allow any vehicles. */
    public static final LinkType NONE = new LinkType("NONE");

    /** link type that allows all vehicles. */
    public static final LinkType ALL = new LinkType("ALL");

    static
    {
        ALL.addCompatibility(GTUType.ALL);
    }
    
    /**
     * @param id the id of the lane type.
     */
    public LinkType(final String id)
    {
        super();
        this.id = id;
    }

    /**
     * @param gtuType GTU type to add compatibility for.
     */
    public final void addCompatibility(final GTUType gtuType)
    {
        this.compatibilitySet.add(gtuType);
    }

    /**
     * @param gtuType GTU type to look for compatibility.
     * @return whether the LaneType is compatible with the GTU type, or compatible with all GTU types.
     */
    public final boolean isCompatible(final GTUType gtuType)
    {
        return this.compatibilitySet.contains(gtuType) || this.compatibilitySet.contains(GTUType.ALL);
    }

    /**
     * @return id.
     */
    public final String getId()
    {
        return this.id;
    }

    /** {@inheritDoc} */
    public final String toString()
    {
        return "LinkType: " + this.id;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @SuppressWarnings({"checkstyle:designforextension", "checkstyle:needbraces"})
    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LinkType other = (LinkType) obj;
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
