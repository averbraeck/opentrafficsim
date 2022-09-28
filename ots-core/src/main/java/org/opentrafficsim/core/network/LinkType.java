package org.opentrafficsim.core.network;

import java.io.Serializable;

import org.opentrafficsim.base.HierarchicalType;
import org.opentrafficsim.base.Identifiable;
import org.opentrafficsim.core.compatibility.Compatibility;
import org.opentrafficsim.core.compatibility.GtuCompatibility;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GtuType;

/**
 * Link type to indicate compatibility with GTU types. The id of a LinkType should be unique within a simulation. This is,
 * however, not checked or enforced, as different simulations running in the same JVM can have different compatibilitySets for
 * LinkTypes with the same id. Therefore, uniqueness is not enforced.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class LinkType extends HierarchicalType<LinkType> implements Serializable, Identifiable, Compatibility<GtuType, LinkType>
{
    /** */
    private static final long serialVersionUID = 20140821L;

    /** The compatibility of GTU types with this link type. */
    private final GtuCompatibility<LinkType> compatibility;

    /** Reversed link type. */
    private LinkType reversed = null;

    /** the network to which the LinkType belongs. */
    private final Network network;

    /** Default types with their name. */
    public enum DEFAULTS
    {
        /** The link type that does not allow any vehicles, or pedestrians. */
        NONE("NONE"),

        /** Two-directional road, accessible to all road GTU types (including PEDESTRIAN). */
        ROAD("ROAD"),

        /** One-directional road, accessible to all road GTU types (excluding PEDESTRIAN and BICYCLE). */
        FREEWAY("FREEWAY"),

        /** Two-directional water way. */
        WATERWAY("WATERWAY"),

        /** Two-directional rail link. */
        RAILWAY("RAILWAY"),

        /** Virtual connection between nodes, e.g. to distribute demand. */
        CONNECTOR("CONNECTOR");

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
     * Create a new Link type with compatibility set.
     * @param id String; the id of the lane type (may not be null)
     * @param parent LinkType; the parent type (may be null)
     * @param compatibility the collection of compatible GtuTypes for this LinkType; can be null (resulting in a LinkType that
     *            is inaccessible to all GTU types). This constructor makes a deep copy of the <code>compatibility</code>.
     * @param network Network; The network to which the LinkType belongs
     */
    public LinkType(final String id, final LinkType parent, final GtuCompatibility<LinkType> compatibility,
            final Network network)
    {
        super(id, parent);
        this.compatibility = new GtuCompatibility<>(compatibility);
        this.network = network;
        this.network.addLinkType(this);
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

    /** {@inheritDoc} */
    @Override
    public Boolean isCompatible(final GtuType gtuType, final GTUDirectionality directionality)
    {
        return this.compatibility.isCompatible(gtuType, directionality);
    }

    /**
     * This method won't work correctly in a ReverseLinkType; it should only be used to clone a network; nowhere else.
     * @return GtuCompatibility&lt;LinkType&gt;; the GTU compatibility for this LinkType
     */
    public GtuCompatibility<LinkType> getCompatibility()
    {
        return this.compatibility;
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
        return this.getId().equals(DEFAULTS.NONE.getId());
    }

    /**
     * @return whether this is {@code ROAD}
     */
    public final boolean isRoad()
    {
        return this.getId().equals(DEFAULTS.ROAD.getId());
    }

    /**
     * @return whether this is {@code WATER_WAY}
     */
    public final boolean isWaterWay()
    {
        return this.getId().equals(DEFAULTS.WATERWAY.getId());
    }

    /**
     * @return whether this is {@code RAIL_WAY}
     */
    public final boolean isRailWay()
    {
        return this.getId().equals(DEFAULTS.RAILWAY.getId());
    }

    /**
     * @return whether this is {@code CONNECTOR}
     */
    public final boolean isConnector()
    {
        return this.getId().equals(DEFAULTS.CONNECTOR.getId());
    }

    /**
     * @return the network to which the LinkType belongs
     */
    public Network getNetwork()
    {
        return this.network;
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
    public LongitudinalDirectionality getDirectionality(final GtuType gtuType, final boolean tryParentsOfGtuType)
    {
        return this.compatibility.getDirectionality(gtuType, tryParentsOfGtuType);
    }

    /**
     * Reversed version of an original and wrapped link type.
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
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
            super(original.getId() + "_rev", null == original.getParent() ? null : original.getParent().reverse(),
                    new GtuCompatibility<>((LinkType) null), original.getNetwork());
            this.original = original;
        }

        /** {@inheritDoc} */
        @Override
        public final LongitudinalDirectionality getDirectionality(final GtuType gtuType, final boolean tryParentsOfGtuType)
        {
            return super.compatibility.getDirectionality(gtuType, tryParentsOfGtuType).invert();
        }

        /** {@inheritDoc} */
        @Override
        public Boolean isCompatible(final GtuType gtuType, final GTUDirectionality directionality)
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
