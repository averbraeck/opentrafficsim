package org.opentrafficsim.road.network.lane;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.opentrafficsim.base.immutablecollections.ImmutableHashSet;
import org.opentrafficsim.base.immutablecollections.ImmutableSet;
import org.opentrafficsim.core.Type;
import org.opentrafficsim.core.gtu.GTUType;

import nl.tudelft.simulation.language.Throw;

/**
 * Lane type to indicate compatibility with GTU types. The id of a LaneType should be unique. This is, however, not checked or
 * enforced, as the LaneType is not a singleton as the result of the compatibilitySet. Different simulations running in the same
 * GTU can have different compatibilitySets for LaneTypes with the same id. Therefore, uniqueness is not enforced.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-08-30 00:16:51 +0200 (Sun, 30 Aug 2015) $, @version $Revision: 1329 $, by $Author: averbraeck $,
 * initial version Aug 21, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public class LaneType extends Type<LaneType> implements Serializable
{
    /** */
    private static final long serialVersionUID = 20140821L;

    /** The id of the LaneType to make it identifiable. */
    private final String id;

    /** The compatibility of GTUs with this lane type. */
    private final ImmutableSet<GTUType> compatibilitySet;

    /** Lane type that does not allow any vehicles. */
    public static final LaneType NONE;

    /** Lane type that allows all vehicles. */
    public static final LaneType ALL;

    static
    {
        NONE = new LaneType("NONE", new HashSet<GTUType>());
        Set<GTUType> allSet = new HashSet<>();
        allSet.add(GTUType.ALL);
        ALL = new LaneType("ALL", allSet);
    }

    /**
     * Create a new Lane type with an immutable compatibility set.
     * @param id the id of the lane type.
     * @param compatibility the collection of compatible GTUTypes for this LaneType
     * @throws NullPointerException if either the id is null, or the compatibilitySet is null
     */
    public LaneType(final String id, final Collection<GTUType> compatibility) throws NullPointerException
    {
        Throw.whenNull(id, "id cannot be null for LaneType");
        Throw.whenNull(compatibility, "compatibility collection cannot be null for LaneType with id = %s", id);

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
        return "LaneType [id=" + this.id + ", compatibilitySet=" + this.compatibilitySet + "]";
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
        LaneType other = (LaneType) obj;
        if (!this.compatibilitySet.equals(other.compatibilitySet))
            return false;
        if (!this.id.equals(other.id))
            return false;
        return true;
    }

}
