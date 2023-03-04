package org.opentrafficsim.core.network;

import org.opentrafficsim.core.compatibility.GtuCompatibleInfraType;

/**
 * Link type to indicate compatibility with GTU types. The id of a LinkType should be unique within a simulation. This is,
 * however, not checked or enforced, as different simulations running in the same JVM can have different compatibilitySets for
 * LinkTypes with the same id. Therefore, uniqueness is not enforced.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class LinkType extends GtuCompatibleInfraType<LinkType, Link>
{
    /** */
    private static final long serialVersionUID = 20140821L;

    /**
     * Constructor for root-level Link types. The parent will be {@code null}.
     * @param id String; the id of the link type (may not be {@code null}).
     */
    public LinkType(final String id)
    {
        super(id, null);
    }

    /**
     * Create a new Link type.
     * @param id String; the id of the link type (may not be {@code null}).
     * @param parent LinkType; the parent type (may be {@code null}).
     */
    public LinkType(final String id, final LinkType parent)
    {
        super(id, parent);
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "LinkType [id=" + getId() + ", gtuCompatibility=" + getGtuCompatibility() + "]";
    }

}
