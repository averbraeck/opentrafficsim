package org.opentrafficsim.road.network.lane;

import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.compatibility.GtuCompatibleInfraType;
import org.opentrafficsim.road.network.RoadNetwork;

/**
 * Lane type to indicate compatibility with GTU types. The id of a LaneType should be unique. This is, however, not checked or
 * enforced, as the LaneType is not a singleton as the result of the compatibilitySet. Different simulations running in the same
 * GTU can have different compatibilitySets for LaneTypes with the same id. Therefore, uniqueness is not enforced.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public class LaneType extends GtuCompatibleInfraType<LaneType, Lane>
{
    /** */
    private static final long serialVersionUID = 20140821L;

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
     * @param network RoadNetwork; The network to which the LaneType belongs
     * @throws NullPointerException if either the id is null, or the compatibilitySet is null
     */
    public LaneType(final String id, final RoadNetwork network) throws NullPointerException
    {
        super(id);
        Throw.whenNull(network, "network cannot be null for LaneType with id = %s", id);
        this.network = network;
        this.network.addLaneType(this);
    }

    /**
     * Create a new Lane type with a compatibility set.
     * @param id String; the id of the lane type.
     * @param parent LaneType; parent type
     * @param network RoadNetwork; The network to which the LaneType belongs
     * @throws NullPointerException if either the id is null, or the compatibilitySet is null
     */
    public LaneType(final String id, final LaneType parent, final RoadNetwork network) throws NullPointerException
    {
        super(id, parent);
        Throw.whenNull(parent, "parent cannot be null for LaneType with id = %s", id);
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
     * @return the network to which the LinkType belongs
     */
    public RoadNetwork getNetwork()
    {
        return this.network;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "LaneType [id=" + this.getId() + ", compatibilitySet=" + getGtuCompatibility() + "]";
    }

}
