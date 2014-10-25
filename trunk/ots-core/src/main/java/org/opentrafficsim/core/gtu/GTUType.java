package org.opentrafficsim.core.gtu;

import org.opentrafficsim.core.network.LaneType;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Jul 8, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @param <ID> the ID-type of the GTU, e.g. String or a certain Enum type.
 */
public class GTUType<ID>
{
    /** The id of the GTUType to make it identifiable. */
    private final ID id;

    /** ALL GTUType to define e.g., permeability for all GTU Types. */
    public static final GTUType<String> ALL = new GTUType<>("ALL");
    
    /**
     * @param id the id of the GTU type.
     */
    public GTUType(final ID id)
    {
        super();
        this.id = id;
    }

    /**
     * @param laneType lane type to look for compatibility.
     * @return whether the GTUType is compatible with the lane type.
     */
    public final boolean isCompatible(final LaneType<?> laneType)
    {
        return laneType.isCompatible(this);
    }

    /**
     * @return id.
     */
    public final ID getId()
    {
        return this.id;
    }
}
