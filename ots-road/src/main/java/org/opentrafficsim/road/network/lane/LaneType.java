package org.opentrafficsim.road.network.lane;

import java.io.Serializable;

import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.HierarchicalType;
import org.opentrafficsim.core.compatibility.Compatibility;
import org.opentrafficsim.core.compatibility.GTUCompatibility;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.road.network.RoadNetwork;

/**
 * Lane type to indicate compatibility with GTU types. The id of a LaneType should be unique. This is, however, not checked or
 * enforced, as the LaneType is not a singleton as the result of the compatibilitySet. Different simulations running in the same
 * GTU can have different compatibilitySets for LaneTypes with the same id. Therefore, uniqueness is not enforced.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-08-30 00:16:51 +0200 (Sun, 30 Aug 2015) $, @version $Revision: 1329 $, by $Author: averbraeck $,
 * initial version Aug 21, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public class LaneType extends HierarchicalType<LaneType> implements Serializable, Compatibility<GTUType, LaneType>
{
    /** */
    private static final long serialVersionUID = 20140821L;

    /** The compatibility of GTUs with this lane type. */
    private final GTUCompatibility<LaneType> compatibility;

    /** the network to which the LinkType belongs. */
    private final RoadNetwork network;

    /** Default types with their name. */
    public enum DEFAULTS
    {
        /** The lane type used for lanes that are forbidden to all GTU types. */
        NONE("NONE"),

        /** Vehicular roads (Dutch: weg); allows all road vehicles and pedestrians. */
        TWO_WAY_LANE("TWO_WAY_LANE"),

        /** Vehicular lane that is two-way for PEDESTRIANS but only permitted in design direction for all other road users. */
        ONE_WAY_LANE("ONE_WAY_LANE"),

        /** Controlled access roads (Dutch: snelweg). */
        FREEWAY("FREEWAY"),

        /** High speed vehicular roads (Dutch: autoweg). */
        HIGHWAY("HIGHWAY"),

        /** Lane on rural vehicular roads (Dutch: weg buiten bebouwde kom). */
        RURAL_ROAD_LANE("RURAL_ROAD_LANE"),

        /** Lane on urban vehicular roads (Dutch: weg binnen bebouwde kom). */
        URBAN_ROAD_LANE("URBAN_ROAD_LANE"),

        /** Residential vehicular roads (Dutch: woonerf). */
        RESIDENTIAL_ROAD_LANE("RESIDENTIAL_ROAD_LANE"),

        /** Bidirectional bus lane (Dutch: busstrook). */
        BUS_LANE("BUS_LANE"),

        /** Bidirectional bicycle lane (Dutch: (brom)fietspad). */
        MOPED_PATH("MOPED_PATH"),

        /** Bicycle path (Dutch: fietspad). */
        BICYCLE_PATH("BICYCLE_PATH"),

        /** Bidirectional footpath (Dutch: voetpad). */
        FOOTPATH("FOOTPATH");

        /** The name. */
        private final String id;

        /**
         * Construct the enum.
         * @param id String; the id
         */
        DEFAULTS(final String id)
        {
            this.id = id;
        }

        /** @return the id */
        public String getId()
        {
            return this.id;
        }
    }

    /**
     * Create a new Lane type with a compatibility set.
     * @param id String; the id of the lane type.
     * @param compatibility GTUCompatibility&lt;LaneType&gt;; the collection of compatible GTUTypes for this LaneType.
     *            Compatibility is solely determined by a specific lane type, and independent of compatibility in super or sub
     *            types.
     * @param network RoadNetwork; The network to which the LaneType belongs
     * @throws NullPointerException if either the id is null, or the compatibilitySet is null
     */
    public LaneType(final String id, final GTUCompatibility<LaneType> compatibility, final RoadNetwork network)
            throws NullPointerException
    {
        super(id);
        Throw.whenNull(compatibility, "compatibility collection cannot be null for LaneType with id = %s", id);
        Throw.whenNull(network, "network cannot be null for LaneType with id = %s", id);
        this.compatibility = new GTUCompatibility<>(compatibility);
        this.network = network;
        this.network.addLaneType(this);
    }

    /**
     * Create a new Lane type with a compatibility set.
     * @param id String; the id of the lane type.
     * @param parent LaneType; parent type
     * @param compatibility GTUCompatibility&lt;LaneType&gt;; the collection of compatible GTUTypes for this LaneType.
     *            Compatibility is solely determined by a specific lane type, and independent of compatibility in super or sub
     *            types.
     * @param network RoadNetwork; The network to which the LaneType belongs
     * @throws NullPointerException if either the id is null, or the compatibilitySet is null
     */
    public LaneType(final String id, final LaneType parent, final GTUCompatibility<LaneType> compatibility,
            final RoadNetwork network) throws NullPointerException
    {
        super(id, parent);
        Throw.whenNull(compatibility, "compatibility collection cannot be null for LaneType with id = %s", id);
        Throw.whenNull(parent, "parent cannot be null for LaneType with id = %s", id);
        this.compatibility = new GTUCompatibility<>(compatibility);
        this.network = network;
        this.network.addLaneType(this);
    }

    /**
     * Private constructor for a LaneType.
     * @param id String; id of the new LaneType
     * @param inverted boolean; if true; the compatibility is longitudinally inverted
     * @param network RoadNetwork; The network to which the LaneType belongs
     */
    private LaneType(final String id, final boolean inverted, final RoadNetwork network)
    {
        super(id);
        this.compatibility = null;
        this.network = network;
        this.network.addLaneType(this);
    }

    /**
     * Whether this, or any of the parent types, equals the given type.
     * @param type DEFAULTS; type
     * @return whether this, or any of the parent types, equals the given type
     */
    public boolean isOfType(final DEFAULTS type)
    {
        if (this.getId().equals(type.getId()))
        {
            return true;
        }
        if (getParent() != null)
        {
            return getParent().isOfType(type);
        }
        return false;
    }

    /**
     * Construct a new Lane type based on another Lane type with longitudinally inverted compatibility.
     * @return LaneType; the new lane type
     */
    public final LaneType inv()
    {
        return new LaneType(getId(), true, getNetwork());
    }

    /**
     * @return the gtu compatibility for this LaneType
     */
    public GTUCompatibility<LaneType> getCompatibility()
    {
        return this.compatibility;
    }

    /**
     * @return the network to which the LinkType belongs
     */
    public RoadNetwork getNetwork()
    {
        return this.network;
    }

    /**
     * Compatibility is solely determined by a specific lane type, and independent of compatibility in super or sub types.
     * @param gtuType GTUType; GTU type to look for compatibility.
     * @param direction GTUDirectionality; the direction that the GTU is moving (with respect to the direction of the design
     *            line of the Link)
     * @return boolean; true if this LaneType permits GTU type in the given direction
     */
    @Override
    public final Boolean isCompatible(final GTUType gtuType, final GTUDirectionality direction)
    {
        // OTS-338
        // return this.compatibilitySet.contains(gtuType) || this.compatibilitySet.contains(GTUType.ALL);
        return getDirectionality(gtuType).permits(direction);
    }

    /**
     * Get the permitted driving directions for a given GTU type on this Lane.
     * @param gtuType GTUType; the GTU type
     * @return LongitudinalDirectionality; the permitted directions of the GTU type on this Lane
     */
    public final LongitudinalDirectionality getDirectionality(final GTUType gtuType)
    {
        LongitudinalDirectionality result = this.compatibility.getDirectionality(gtuType, true);
        if (null == this.compatibility)
        {
            return result.invert();
        }
        return result;
    }

    /**
     * Add GTU type to compatibility.
     * @param gtuType GTUType; the GTU type to add
     * @param direction LongitudinalDirectionality; permitted direction of movement
     */
    public final void addGtuCompatability(final GTUType gtuType, final LongitudinalDirectionality direction)
    {
        if (null == this.compatibility)
        {
            getParent().addGtuCompatability(gtuType, direction.invert());
        }
        else
        {
            this.compatibility.addAllowedGTUType(gtuType, direction);
        }
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "LaneType [id=" + this.getId() + ", compatibilitySet=" + this.compatibility + "]";
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.compatibility == null) ? 0 : this.compatibility.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
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
        if (this.compatibility == null)
        {
            if (other.compatibility != null)
            {
                return false;
            }
        }
        else if (!this.compatibility.equals(other.compatibility))
        {
            return false;
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public final LongitudinalDirectionality getDirectionality(final GTUType gtuType, final boolean tryParentsOfGTUType)
    {
        LongitudinalDirectionality result = this.compatibility.getDirectionality(gtuType, tryParentsOfGTUType);
        if (null == this.compatibility)
        {
            return result.invert();
        }
        return result;
    }

}
