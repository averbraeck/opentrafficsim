package org.opentrafficsim.road.network.lane;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.opentrafficsim.base.HierarchalType;
import org.opentrafficsim.core.gtu.GTUType;

import nl.tudelft.simulation.immutablecollections.ImmutableHashSet;
import nl.tudelft.simulation.immutablecollections.ImmutableSet;
import nl.tudelft.simulation.language.Throw;

/**
 * Lane type to indicate compatibility with GTU types. The id of a LaneType should be unique. This is, however, not checked or
 * enforced, as the LaneType is not a singleton as the result of the compatibilitySet. Different simulations running in the same
 * GTU can have different compatibilitySets for LaneTypes with the same id. Therefore, uniqueness is not enforced.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-08-30 00:16:51 +0200 (Sun, 30 Aug 2015) $, @version $Revision: 1329 $, by $Author: averbraeck $,
 * initial version Aug 21, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public class LaneType extends HierarchalType<LaneType> implements Serializable
{
    /** */
    private static final long serialVersionUID = 20140821L;

    /**
     * The compatibility of GTUs with this lane type. Compatibility is solely determined by a specific lane type, and
     * independent of compatibility in super or sub types.
     */
    private final Set<GTUType> compatibilitySet;

    /** Lane type that does not allow any vehicles. */
    public static final LaneType NONE;

    /** Lane type that allows all vehicles. */
    public static final LaneType ALL;

    /** Vehicular roads (Dutch: weg). */
    public static final LaneType ROAD;

    /** Controlled access roads (Dutch: snelweg). */
    public static final LaneType FREEWAY;

    /** High speed vehicular roads (Dutch: autoweg). */
    public static final LaneType HIGHWAY;

    /** Rural vehicular roads (Dutch: weg buiten bebouwde kom). */
    public static final LaneType RURAL_ROAD;

    /** Urban vehicular roads (Dutch: weg binnen bebouwde kom). */
    public static final LaneType URBAN_ROAD;

    /** Residential vehicular roads (Dutch: woonerf). */
    public static final LaneType RESIDENTIAL_ROAD;

    /** Bus lane (Dutch: busstrook). */
    public static final LaneType BUS_LANE;

    /** Bicycle path (Dutch: (brom)fietspad). */
    public static final LaneType MOPED_PATH;

    /** Bicycle path (Dutch: fietspad). */
    public static final LaneType BICYCLE_PATH;

    /** Footpath (Dutch: voetpad). */
    public static final LaneType FOOTPATH;

    static
    {
        NONE = new LaneType("NONE", new HashSet<GTUType>());
        ALL = new LaneType("ALL", Arrays.asList(new GTUType[] { GTUType.ALL }));
        ROAD = new LaneType("ROAD", ALL, Arrays.asList(new GTUType[] { GTUType.VEHICLE }));
        FREEWAY = new LaneType("FREEWAY", ROAD, Arrays.asList(new GTUType[] { GTUType.VEHICLE }));
        HIGHWAY = new LaneType("HIGHWAY", ROAD, Arrays.asList(new GTUType[] { GTUType.VEHICLE }));
        RURAL_ROAD = new LaneType("RURAL_ROAD", ROAD,
                Arrays.asList(new GTUType[] { GTUType.VEHICLE, GTUType.BICYCLE, GTUType.MOPED, GTUType.PEDESTRIAN }));
        URBAN_ROAD = new LaneType("URBAN_ROAD", ROAD,
                Arrays.asList(new GTUType[] { GTUType.VEHICLE, GTUType.BICYCLE, GTUType.MOPED, GTUType.PEDESTRIAN }));
        RESIDENTIAL_ROAD = new LaneType("RESIDENTIAL_ROAD", ROAD,
                Arrays.asList(new GTUType[] { GTUType.VEHICLE, GTUType.BICYCLE, GTUType.MOPED, GTUType.PEDESTRIAN }));
        BUS_LANE = new LaneType("BUS_LANE", ALL, Arrays.asList(new GTUType[] { GTUType.BUS }));
        MOPED_PATH = new LaneType("MOPED_PATH", ALL, Arrays.asList(new GTUType[] { GTUType.BICYCLE, GTUType.MOPED }));
        BICYCLE_PATH = new LaneType("BICYCLE_PATH", ALL, Arrays.asList(new GTUType[] { GTUType.BICYCLE }));
        FOOTPATH = new LaneType("FOOTPATH", ALL, Arrays.asList(new GTUType[] { GTUType.PEDESTRIAN }));
    }

    /**
     * Create a new Lane type with a compatibility set.
     * @param id the id of the lane type.
     * @param compatibility the collection of compatible GTUTypes for this LaneType. Compatibility is solely determined by a
     *            specific lane type, and independent of compatibility in super or sub types.
     * @throws NullPointerException if either the id is null, or the compatibilitySet is null
     */
    private LaneType(final String id, final Collection<GTUType> compatibility) throws NullPointerException
    {
        super(id);
        Throw.whenNull(compatibility, "compatibility collection cannot be null for LaneType with id = %s", id);
        this.compatibilitySet = new HashSet<>(compatibility);
    }

    /**
     * Create a new Lane type with a compatibility set.
     * @param id the id of the lane type.
     * @param parent parent type
     * @param compatibility the collection of compatible GTUTypes for this LaneType. Compatibility is solely determined by a
     *            specific lane type, and independent of compatibility in super or sub types.
     * @throws NullPointerException if either the id is null, or the compatibilitySet is null
     */
    public LaneType(final String id, final LaneType parent, final Collection<GTUType> compatibility) throws NullPointerException
    {
        super(id, parent);
        Throw.whenNull(compatibility, "compatibility collection cannot be null for LaneType with id = %s", id);
        this.compatibilitySet = new HashSet<>(compatibility);
    }

    /**
     * Compatibility is solely determined by a specific lane type, and independent of compatibility in super or sub types.
     * @param gtuType GTU type to look for compatibility.
     * @return whether the LaneType is compatible with the GTU type, or compatible with all GTU types.
     */
    public final boolean isCompatible(final GTUType gtuType)
    {
        return this.compatibilitySet.contains(gtuType) || this.compatibilitySet.contains(GTUType.ALL);
    }

    /**
     * Get the set of compatible GTU types.
     * @return set of compatible GTU types
     */
    public final ImmutableSet<GTUType> getCompatbilitySet()
    {
        return new ImmutableHashSet<>(this.compatibilitySet);
    }

    /**
     * Remove GTU type from compatibility.
     * @param gtuType GTU type to remove
     */
    public final void removeGtuCompatibility(final GTUType gtuType)
    {
        this.compatibilitySet.remove(gtuType);
    }

    /**
     * Add GTU type to compatibility.
     * @param gtuType GTU type to add
     */
    public final void addGtuCompatability(final GTUType gtuType)
    {
        this.compatibilitySet.add(gtuType);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "LaneType [id=" + this.getId() + ", compatibilitySet=" + this.compatibilitySet + "]";
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.compatibilitySet == null) ? 0 : this.compatibilitySet.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!super.equals(obj))
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        LaneType other = (LaneType) obj;
        if (this.compatibilitySet == null)
        {
            if (other.compatibilitySet != null)
            {
                return false;
            }
        }
        else if (!this.compatibilitySet.equals(other.compatibilitySet))
        {
            return false;
        }
        return true;
    }

}
