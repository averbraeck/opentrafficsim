package org.opentrafficsim.sim0mq.kpi;

/**
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class GtuTypeDataDeprecated
{
    /** type name. */
    private final String gtuTypeName;

    /**
     * @param gtuTypeName gtu type name
     */
    public GtuTypeDataDeprecated(final String gtuTypeName)
    {
        this.gtuTypeName = gtuTypeName;
    }

    /**
     * @return gtuTypeName
     */
    public final String getGtuTypeName()
    {
        return this.gtuTypeName;
    }

    /** {@inheritDoc} */
    // @Override
    public String getId()
    {
        return this.gtuTypeName;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.gtuTypeName == null) ? 0 : this.gtuTypeName.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GtuTypeDataDeprecated other = (GtuTypeDataDeprecated) obj;
        if (this.gtuTypeName == null)
        {
            if (other.gtuTypeName != null)
                return false;
        }
        else if (!this.gtuTypeName.equals(other.gtuTypeName))
            return false;
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "GtuTypeData [gtuTypeName=" + this.gtuTypeName + "]";
    }

}
