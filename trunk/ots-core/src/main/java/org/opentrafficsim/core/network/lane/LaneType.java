package org.opentrafficsim.core.network.lane;

import java.util.HashSet;
import java.util.Set;

import org.opentrafficsim.core.gtu.GTUType;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Aug 21, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public class LaneType
{
    /** The id of the LaneType to make it identifiable. */
    private final String id;

    /** the compatibility of GTUs with this lane type. */
    private final Set<GTUType> compatibilitySet = new HashSet<>();

    /** lane type that does not allow any vehicles. */
    public static final LaneType NONE = new LaneType("NONE");

    /**
     * @param id the id of the lane type.
     */
    public LaneType(final String id)
    {
        super();
        this.id = id;
    }

    /**
     * @param gtuType GTU type to add compatibility for.
     */
    public final void addCompatibility(final GTUType gtuType)
    {
        this.compatibilitySet.add(gtuType);
    }

    /**
     * @param gtuType GTU type to look for compatibility.
     * @return whether the LaneType is compatible with the GTU type.
     */
    public final boolean isCompatible(final GTUType gtuType)
    {
        return this.compatibilitySet.contains(gtuType);
    }

    /**
     * @return id.
     */
    public final String getId()
    {
        return this.id;
    }

    /** {@inheritDoc} */
    public final String toString()
    {
        return "LaneType: " + this.id;
    }
}
