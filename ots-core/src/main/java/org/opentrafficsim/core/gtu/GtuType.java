package org.opentrafficsim.core.gtu;

import org.opentrafficsim.base.HierarchicalType;

/**
 * A GTU type is used to identify all sorts of properties and compatibilities for GTUs. For example, what lanes a GTU may drive
 * on, and what length of vehicle to get.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public final class GtuType extends HierarchicalType<GtuType, Gtu>
{
    /**
     * Constructor for root-level GTU types. The parent will be {@code null}.
     * @param id The id of the GtuType to make it identifiable.
     * @throws NullPointerException if the id is null
     */
    public GtuType(final String id) throws NullPointerException
    {
        super(id);
    }

    /**
     * Constructor.
     * @param id The id of the GtuType to make it identifiable.
     * @param parent parent GTU type.
     * @throws NullPointerException if the id is null
     */
    public GtuType(final String id, final GtuType parent) throws NullPointerException
    {
        super(id, parent);
    }

    @Override
    public String toString()
    {
        return "GtuType: " + this.getId();
    }

}
