package org.opentrafficsim.road.network.lane;

import org.opentrafficsim.core.compatibility.GtuCompatibleInfraType;

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

    /**
     * Constructor for root-level Lane types. The parent will be {@code null}.
     * @param id String; the id of the lane type.
     * @throws NullPointerException if the id is null
     */
    public LaneType(final String id) throws NullPointerException
    {
        super(id);
    }

    /**
     * Create a new Lane type with a compatibility set.
     * @param id String; the id of the lane type.
     * @param parent LaneType; parent type
     * @throws NullPointerException if the id is null
     */
    public LaneType(final String id, final LaneType parent) throws NullPointerException
    {
        super(id, parent);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "LaneType [id=" + this.getId() + ", compatibilitySet=" + getGtuCompatibility() + "]";
    }

}
