package org.opentrafficsim.core.network.lane;

import java.io.Serializable;
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
public class LaneType implements Serializable
{
    /** */
    private static final long serialVersionUID = 20140821L;

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
     * @return whether the LaneType is compatible with the GTU type, or compatible with all GTU types.
     */
    public final boolean isCompatible(final GTUType gtuType)
    {
        return this.compatibilitySet.contains(gtuType) || this.compatibilitySet.contains(GTUType.ALL);
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

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @SuppressWarnings({"checkstyle:designforextension", "checkstyle:needbraces"})
    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LaneType other = (LaneType) obj;
        if (this.id == null)
        {
            if (other.id != null)
                return false;
        }
        else if (!this.id.equals(other.id))
            return false;
        return true;
    }

}
