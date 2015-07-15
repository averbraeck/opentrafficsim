package org.opentrafficsim.core.network.lane;

import java.util.HashSet;
import java.util.Set;

import org.opentrafficsim.core.gtu.GTUType;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$, initial versionAug 21, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @param <ID> to identify the ID type (can be String, numeric, object).
 */
public class LaneType<ID>
{
    /** The id of the LaneType to make it identifiable. */
    private final ID id;

    /** the compatibility of GTUs with this lane type. */
    private final Set<GTUType<?>> compatibilitySet = new HashSet<>();

    /**
     * @param id the id of the lane type.
     */
    public LaneType(final ID id)
    {
        super();
        this.id = id;
    }

    /**
     * @param gtuType GTU type to add compatibility for.
     */
    public final void addCompatibility(final GTUType<?> gtuType)
    {
        this.compatibilitySet.add(gtuType);
    }

    /**
     * @param gtuType GTU type to look for compatibility.
     * @return whether the LaneType is compatible with the GTU type.
     */
    public final boolean isCompatible(final GTUType<?> gtuType)
    {
        return this.compatibilitySet.contains(gtuType);
    }

    /**
     * @return id.
     */
    public final ID getId()
    {
        return this.id;
    }
    
    /** {@inheritDoc} */
    public final String toString()
    {
        String out = "LaneType: " + this.id;
        return out;
    }
}
