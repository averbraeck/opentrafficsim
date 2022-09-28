package org.opentrafficsim.sim0mq.kpi;

import org.opentrafficsim.kpi.interfaces.GtuTypeDataInterface;

/**
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 13 okt. 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class GtuTypeData implements GtuTypeDataInterface
{
    /** type name. */
    private final String gtuTypeName;

    /**
     * @param gtuTypeName String; gtu type name
     */
    public GtuTypeData(final String gtuTypeName)
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
    @Override
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
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GtuTypeData other = (GtuTypeData) obj;
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
