package org.opentrafficsim.road.network.sampling;

import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.kpi.interfaces.GtuTypeDataInterface;

/**
 * GTU type representation in road sampler.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class GtuTypeData implements GtuTypeDataInterface
{

    /** Gtu type. */
    private final GtuType gtuType;

    /**
     * @param gtuType GtuType; gtu type
     */
    public GtuTypeData(final GtuType gtuType)
    {
        this.gtuType = gtuType;
    }

    /**
     * @return gtuType.
     */
    @Override
    public final String getId()
    {
        return this.gtuType.getId();
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.gtuType == null) ? 0 : this.gtuType.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        GtuTypeData other = (GtuTypeData) obj;
        if (this.gtuType == null)
        {
            if (other.gtuType != null)
            {
                return false;
            }
        }
        else if (!this.gtuType.equals(other.gtuType))
        {
            return false;
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "GtuTypeData [gtuType=" + this.gtuType + "]";
    }

}
