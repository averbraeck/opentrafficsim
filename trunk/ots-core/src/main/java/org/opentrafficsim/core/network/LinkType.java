package org.opentrafficsim.core.network;

import java.io.Serializable;

import org.opentrafficsim.base.HierarchicalType;
import org.opentrafficsim.base.Identifiable;
import org.opentrafficsim.core.compatibility.Compatibility;
import org.opentrafficsim.core.compatibility.GTUCompatibility;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUType;

/**
 * Link type to indicate compatibility with GTU types. The id of a LinkType should be unique within a simulation. This is,
 * however, not checked or enforced, as different simulations running in the same JVM can have different compatibilitySets for
 * LinkTypes with the same id. Therefore, uniqueness is not enforced.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-08-30 00:16:51 +0200 (Sun, 30 Aug 2015) $, @version $Revision: 1329 $, by $Author: averbraeck $,
 * initial version Aug 21, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class LinkType extends HierarchicalType<LinkType> implements Serializable, Identifiable, Compatibility<GTUType, LinkType>
{
    /** */
    private static final long serialVersionUID = 20140821L;

    /** The compatibility of GTU types with this link type. */
    private final GTUCompatibility<LinkType> compatibility;

    /** Reversed link type. */
    private LinkType reversed = null;

    /** The link type that does not allow any vehicles, or pedestrians. */
    public static final LinkType NONE;

    /** Two-directional road, accessible to all road GTU types (including PEDESTRIAN). */
    public static final LinkType ROAD;

    /** One-directional road, accessible to all road GTU types (excluding PEDESTRIAN and BICYCLE). */
    public static final LinkType FREEWAY;

    /** Two-directional water way. */
    public static final LinkType WATER_WAY;

    /** Two-directional rail link. */
    public static final LinkType RAIL_WAY;

    /** Virtual connection between nodes, e.g. to distribute demand. */
    public static final LinkType CONNECTOR;

    static
    {
        GTUCompatibility<LinkType> compatibility = new GTUCompatibility<>((LinkType) null);
        NONE = new LinkType("NONE", null, compatibility);
        //
        compatibility = new GTUCompatibility<>((LinkType) null);
        compatibility.addAllowedGTUType(GTUType.ROAD_USER, LongitudinalDirectionality.DIR_BOTH);
        ROAD = new LinkType("ROAD", null, compatibility);
        //
        compatibility = new GTUCompatibility<>((LinkType) null);
        compatibility.addAllowedGTUType(GTUType.ROAD_USER, LongitudinalDirectionality.DIR_PLUS);
        compatibility.addAllowedGTUType(GTUType.PEDESTRIAN, LongitudinalDirectionality.DIR_NONE);
        compatibility.addAllowedGTUType(GTUType.BICYCLE, LongitudinalDirectionality.DIR_NONE);
        FREEWAY = new LinkType("FREEWAY", ROAD, compatibility);
        //
        compatibility = new GTUCompatibility<>((LinkType) null);
        compatibility.addAllowedGTUType(GTUType.WATER_WAY_USER, LongitudinalDirectionality.DIR_BOTH);
        WATER_WAY = new LinkType("WATER_WAY", null, compatibility);
        //
        compatibility = new GTUCompatibility<>((LinkType) null);
        compatibility.addAllowedGTUType(GTUType.RAIL_WAY_USER, LongitudinalDirectionality.DIR_BOTH);
        RAIL_WAY = new LinkType("WATER_WAY", null, compatibility);
        //
        compatibility = new GTUCompatibility<>((LinkType) null);
        compatibility.addAllowedGTUType(GTUType.ROAD_USER, LongitudinalDirectionality.DIR_PLUS);
        compatibility.addAllowedGTUType(GTUType.WATER_WAY_USER, LongitudinalDirectionality.DIR_PLUS);
        compatibility.addAllowedGTUType(GTUType.RAIL_WAY_USER, LongitudinalDirectionality.DIR_PLUS);
        CONNECTOR = new LinkType("CONNECTOR", null, compatibility);
    }

    /**
     * Create a new Link type with compatibility set.
     * @param id String; the id of the lane type (may not be null)
     * @param parent LinkType; the parent type (may be null)
     * @param compatibility the collection of compatible GTUTypes for this LinkType; can be null (resulting in a LinkType that
     *            is inaccessible to all GTU types). This constructor makes a deep copy of the <code>compatibility</code>.
     */
    public LinkType(final String id, final LinkType parent, final GTUCompatibility<LinkType> compatibility)
    {
        super(id, parent);
        this.compatibility = new GTUCompatibility<>(compatibility);
    }

    /** {@inheritDoc} */
    @Override
    public Boolean isCompatible(final GTUType gtuType, final GTUDirectionality directionality)
    {
        return this.compatibility.isCompatible(gtuType, directionality);
    }

    /**
     * Returns a link type with directionality reversed.
     * @return LinkType; link type with directionality reversed
     */
    public final LinkType reverse()
    {
        if (this.reversed == null)
        {
            this.reversed = new ReversedLinkType(this);
        }
        return this.reversed;
    }

    /**
     * @return whether this is {@code NONE}
     */
    public final boolean isNone()
    {
        return this.equals(NONE);
    }

    /**
     * @return whether this is {@code ROAD}
     */
    public final boolean isRoad()
    {
        return this.equals(ROAD);
    }

    /**
     * @return whether this is {@code WATER_WAY}
     */
    public final boolean isWaterWay()
    {
        return this.equals(WATER_WAY);
    }

    /**
     * @return whether this is {@code RAIL_WAY}
     */
    public final boolean isRailWay()
    {
        return this.equals(RAIL_WAY);
    }

    /**
     * @return whether this is {@code CONNECTOR}
     */
    public final boolean isConnector()
    {
        return this.equals(CONNECTOR);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "LinkType [id=" + getId() + ", compatibilitySet=" + this.compatibility + "]";
    }

    /** {@inheritDoc} */
    @Override
    public final LongitudinalDirectionality getDirectionality(final GTUType gtuType, final boolean tryParentsOfGTUType)
    {
        return this.compatibility.getDirectionality(gtuType, tryParentsOfGTUType);
    }

    /**
     * Reversed version of an original and wrapped link type.
     * <p>
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 24 aug. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private class ReversedLinkType extends LinkType
    {

        /** */
        private static final long serialVersionUID = 20180824L;
        
        /** Original link type. */
        private final LinkType original;
        
        /**
         * Constructor.
         * @param original LinkType; the original type (may not be null)
         */
        ReversedLinkType(final LinkType original)
        {
            super(original.getId() + "_rev", original.getParent().reverse(), new GTUCompatibility<>((LinkType) null));
            this.original = original;
        }

        /** {@inheritDoc} */
        @Override
        public Boolean isCompatible(final GTUType gtuType, final GTUDirectionality directionality)
        {
            return this.original.isCompatible(gtuType, directionality.flip());
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "ReversedLinkType [original=" + this.original + "]";
        }

    }

}
