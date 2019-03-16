package org.opentrafficsim.road.network.lane;

import java.io.Serializable;

import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.HierarchicalType;
import org.opentrafficsim.core.compatibility.Compatibility;
import org.opentrafficsim.core.compatibility.GTUCompatibility;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.LongitudinalDirectionality;

/**
 * Lane type to indicate compatibility with GTU types. The id of a LaneType should be unique. This is, however, not checked or
 * enforced, as the LaneType is not a singleton as the result of the compatibilitySet. Different simulations running in the same
 * GTU can have different compatibilitySets for LaneTypes with the same id. Therefore, uniqueness is not enforced.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
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

    /** The lane type used for lanes that are forbidden to all GTU types. */
    public static final LaneType NONE;

    /** Vehicular roads (Dutch: weg); allows all road vehicles and pedestrians. */
    public static final LaneType TWO_WAY_LANE;

    /** Vehicular lane that is two-way for PEDESTRIANS but only permitted in design direction for all other road users. */
    public static final LaneType ONE_WAY_LANE;

    /** Controlled access roads (Dutch: snelweg). */
    public static final LaneType FREEWAY;

    /** High speed vehicular roads (Dutch: autoweg). */
    public static final LaneType HIGHWAY;

    /** Lane on rural vehicular roads (Dutch: weg buiten bebouwde kom). */
    public static final LaneType RURAL_ROAD_LANE;

    /** Lane on urban vehicular roads (Dutch: weg binnen bebouwde kom). */
    public static final LaneType URBAN_ROAD_LANE;

    /** Residential vehicular roads (Dutch: woonerf). */
    public static final LaneType RESIDENTIAL_ROAD_LANE;

    /** Bidirectional bus lane (Dutch: busstrook). */
    public static final LaneType BUS_LANE;

    /** Bidirectional bicycle lane (Dutch: (brom)fietspad). */
    public static final LaneType MOPED_PATH;

    /** Bicycle path (Dutch: fietspad). */
    public static final LaneType BICYCLE_PATH;

    /** Bidirectional footpath (Dutch: voetpad). */
    public static final LaneType FOOTPATH;

    static
    {
        GTUCompatibility<LaneType> noTrafficCompatibility = new GTUCompatibility<>((LaneType) null);
        NONE = new LaneType("NONE", null, noTrafficCompatibility);
        GTUCompatibility<LaneType> roadCompatibility = new GTUCompatibility<>((LaneType) null);
        roadCompatibility.addAllowedGTUType(GTUType.ROAD_USER, LongitudinalDirectionality.DIR_BOTH);
        TWO_WAY_LANE = new LaneType("TWO_WAY_LANE", null, roadCompatibility);
        RURAL_ROAD_LANE = new LaneType("RURAL_ROAD", TWO_WAY_LANE, new GTUCompatibility<>(roadCompatibility));
        URBAN_ROAD_LANE = new LaneType("URBAN_ROAD", TWO_WAY_LANE, new GTUCompatibility<>(roadCompatibility));
        RESIDENTIAL_ROAD_LANE = new LaneType("RESIDENTIAL_ROAD", TWO_WAY_LANE, new GTUCompatibility<>(roadCompatibility));
        GTUCompatibility<LaneType> oneWayLaneCompatibility = new GTUCompatibility<>(roadCompatibility);
        oneWayLaneCompatibility.addAllowedGTUType(GTUType.ROAD_USER, LongitudinalDirectionality.DIR_PLUS);
        oneWayLaneCompatibility.addAllowedGTUType(GTUType.PEDESTRIAN, LongitudinalDirectionality.DIR_BOTH);
        ONE_WAY_LANE = new LaneType("ONE_WAY_LANE", oneWayLaneCompatibility);
        GTUCompatibility<LaneType> highwayLaneCompatibility = new GTUCompatibility<>(oneWayLaneCompatibility)
                .addAllowedGTUType(GTUType.PEDESTRIAN, LongitudinalDirectionality.DIR_NONE);
        FREEWAY = new LaneType("FREEWAY", highwayLaneCompatibility);
        HIGHWAY = new LaneType("HIGHWAY", highwayLaneCompatibility);
        GTUCompatibility<LaneType> busLaneCompatibility = new GTUCompatibility<>(roadCompatibility);
        busLaneCompatibility.addAllowedGTUType(GTUType.BUS, LongitudinalDirectionality.DIR_BOTH);
        busLaneCompatibility.addAllowedGTUType(GTUType.ROAD_USER, LongitudinalDirectionality.DIR_NONE);
        BUS_LANE = new LaneType("BUS_LANE", busLaneCompatibility);
        GTUCompatibility<LaneType> mopedAndBicycleLaneCompatibility = new GTUCompatibility<>(roadCompatibility);
        mopedAndBicycleLaneCompatibility.addAllowedGTUType(GTUType.BICYCLE, LongitudinalDirectionality.DIR_BOTH);
        mopedAndBicycleLaneCompatibility.addAllowedGTUType(GTUType.ROAD_USER, LongitudinalDirectionality.DIR_NONE);
        MOPED_PATH = new LaneType("MOPED_PATH", mopedAndBicycleLaneCompatibility);
        GTUCompatibility<LaneType> bicycleOnlyCompatibility = new GTUCompatibility<>(mopedAndBicycleLaneCompatibility);
        bicycleOnlyCompatibility.addAllowedGTUType(GTUType.MOPED, LongitudinalDirectionality.DIR_NONE);
        BICYCLE_PATH = new LaneType("BICYCLE_PATH", bicycleOnlyCompatibility);
        GTUCompatibility<LaneType> pedestriansOnly = new GTUCompatibility<>(roadCompatibility);
        pedestriansOnly.addAllowedGTUType(GTUType.ROAD_USER, LongitudinalDirectionality.DIR_NONE);
        FOOTPATH = new LaneType("FOOTPATH", pedestriansOnly);
    }

    /**
     * Create a new Lane type with a compatibility set.
     * @param id String; the id of the lane type.
     * @param compatibility GTUCompatibility&lt;LaneType&gt;; the collection of compatible GTUTypes for this LaneType.
     *            Compatibility is solely determined by a specific lane type, and independent of compatibility in super or sub
     *            types.
     * @throws NullPointerException if either the id is null, or the compatibilitySet is null
     */
    private LaneType(final String id, final GTUCompatibility<LaneType> compatibility) throws NullPointerException
    {
        super(id);
        Throw.whenNull(compatibility, "compatibility collection cannot be null for LaneType with id = %s", id);
        this.compatibility = new GTUCompatibility<>(compatibility);
    }

    /**
     * Private constructor for a LaneType.
     * @param id String; id of the new LaneType
     * @param inverted boolean; if true; the compatibility is longitudinally inverted
     */
    private LaneType(final String id, final boolean inverted)
    {
        super(id);
        this.compatibility = null;
    }

    /**
     * Construct a new Lane type based on another Lane type with longitudinally inverted compatibility.
     * @return LaneType; the new lane type
     */
    public final LaneType inv()
    {
        return new LaneType(getId(), true);
    }

    /**
     * Create a new Lane type with a compatibility set.
     * @param id String; the id of the lane type.
     * @param parent LaneType; parent type
     * @param compatibility GTUCompatibility&lt;LaneType&gt;; the collection of compatible GTUTypes for this LaneType.
     *            Compatibility is solely determined by a specific lane type, and independent of compatibility in super or sub
     *            types.
     * @throws NullPointerException if either the id is null, or the compatibilitySet is null
     */
    public LaneType(final String id, final LaneType parent, final GTUCompatibility<LaneType> compatibility)
            throws NullPointerException
    {
        super(id, parent);
        Throw.whenNull(compatibility, "compatibility collection cannot be null for LaneType with id = %s", id);
        this.compatibility = new GTUCompatibility<>(compatibility);
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
