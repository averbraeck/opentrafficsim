package org.opentrafficsim.core.network;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.opentrafficsim.core.Type;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.immutablecollections.ImmutableHashSet;
import org.opentrafficsim.core.immutablecollections.ImmutableSet;

import nl.tudelft.simulation.language.Throw;

/**
 * Link type to indicate compatibility with GTU types. The id of a LinkType should be unique within a simulation. This is,
 * however, not checked or enforced, as different simulations running in the same JVM can have different compatibilitySets for
 * LinkTypes with the same id. Therefore, uniqueness is not enforced.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-08-30 00:16:51 +0200 (Sun, 30 Aug 2015) $, @version $Revision: 1329 $, by $Author: averbraeck $,
 * initial version Aug 21, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class LinkType extends Type<LinkType> implements Serializable
{
    /** */
    private static final long serialVersionUID = 20140821L;

    /** The id of the link type to make it identifiable. Should be unique in the simulation. */
    private final String id;

    /** The compatibility of GTU types with this link type. */
    private final ImmutableSet<GTUType> compatibilitySet;

    /** The link type that does not allow any vehicles. */
    public static final LinkType NONE;

    /** The link type that allows all vehicles. */
    public static final LinkType ALL;

    static
    {
        NONE = new LinkType("NONE", new HashSet<GTUType>());
        Set<GTUType> allSet = new HashSet<>();
        allSet.add(GTUType.ALL);
        ALL = new LinkType("ALL", allSet);
    }

    /**
     * Create a new Link type with an immutable compatibility set.
     * @param id the id of the lane type.
     * @param compatibility the collection of compatible GTUTypes for this LinkType
     * @throws NullPointerException if either the id is null, or the compatibilitySet is null
     */
    public LinkType(final String id, final Collection<GTUType> compatibility) throws NullPointerException
    {
        Throw.whenNull(id, "id cannot be null for LinkType");
        Throw.whenNull(compatibility, "compatibilitySet cannot be null for LinkType with id = %s", id);

        this.id = id;
        this.compatibilitySet = new ImmutableHashSet<>(compatibility);
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
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "LinkType [id=" + this.id + ", compatibilitySet=" + this.compatibilitySet + "]";
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.compatibilitySet.hashCode();
        result = prime * result + this.id.hashCode();
        return result;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings({ "checkstyle:designforextension", "checkstyle:needbraces" })
    public boolean equals(final Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LinkType other = (LinkType) obj;
        if (!this.compatibilitySet.equals(other.compatibilitySet))
            return false;
        if (!this.id.equals(other.id))
            return false;
        return true;
    }

}
