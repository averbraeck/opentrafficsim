package org.opentrafficsim.core.network;

import java.io.Serializable;

import org.opentrafficsim.base.HierarchicalType;
import org.opentrafficsim.base.Identifiable;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUType;

import compatibility.Compatibility;
import compatibility.Compatible;
import compatibility.GTUCompatibility;

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
public class LinkType extends HierarchicalType<LinkType> implements Serializable, Identifiable, Compatible
{
    /** */
    private static final long serialVersionUID = 20140821L;

    /** The compatibility of GTU types with this link type. */
    private final Compatibility<GTUType, LinkType> compatibility;

    /** The link type that does not allow any vehicles, or pedestrians. */
    public static final LinkType NONE;

    /** Two-directional road, accessible to all GTU types (including PEDESTRIAN). */
    public static final LinkType ROAD;

    /** Two-directional water way. */
    public static final LinkType WATER_WAY;

    /** Two-directional rail link. */
    public static final LinkType RAIL_WAY;

    static
    {
        GTUCompatibility<LinkType> compatibility = new GTUCompatibility<LinkType>();
        NONE = new LinkType("NONE", null, compatibility);
        compatibility.addAllowedGTUType(GTUType.ROAD_USER, LongitudinalDirectionality.DIR_BOTH);
        ROAD = new LinkType("ROAD", null, compatibility);
        compatibility = new GTUCompatibility<>();
        compatibility.addAllowedGTUType(GTUType.WATER_WAY_USER, LongitudinalDirectionality.DIR_BOTH);
        WATER_WAY = new LinkType("WATER_WAY", null, compatibility);
        compatibility = new GTUCompatibility<>();
        compatibility.addAllowedGTUType(GTUType.RAIL_WAY_USER, LongitudinalDirectionality.DIR_BOTH);
        RAIL_WAY = new LinkType("WATER_WAY", null, compatibility);
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
        this.compatibility = new GTUCompatibility<LinkType>(compatibility);
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isCompatible(final GTUType gtuType, final GTUDirectionality directionality)
    {
        for (LinkType linkType = this; null != linkType; linkType = linkType.getParent())
        {
            Boolean c = this.compatibility.isCompatible(gtuType, directionality);
            if (null != c)
            {
                return c;
            }
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "LinkType [id=" + getId() + ", compatibilitySet=" + this.compatibility + "]";
    }

    /**
     * Get the LongitudinalDirectionality for a GTUType. Will recursively check parent type of GTUType if needed.
     * @param gtuType GTUType; the type of the GTU to retrieve LongitudinalDirectinality for
     * @return LongitudinalityDirectionality for the GTUType, or DIR_NONE if none of the parent types of the GTUType has a
     *         specified LongitudinalDirectionality
     */
    public final LongitudinalDirectionality getDirectionality(final GTUType gtuType)
    {
        return this.compatibility.getDirectionality(gtuType);
    }

}
