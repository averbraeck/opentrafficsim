package org.opentrafficsim.demo.ntm;

import com.vividsolutions.jts.geom.Point;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 7 Oct 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */
public class AreaNode extends Node
{
    /** */
    private static final long serialVersionUID = 1L;
    /** the area to which the node belongs. */
    private final Area area;
    /**
     * @param nr
     * @param point
     */
    public AreaNode(String nr, Point point, Area area)
    {
        super(nr, point);
        this.area = area;
    }

    /**
     * @return area
     */
    public final Area getArea()
    {
        return this.area;
    }
    

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.area == null) ? 0 : this.area.hashCode());
        result = prime * result + ((getPoint() == null) ? 0 : getPoint().hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @SuppressWarnings({"checkstyle:needbraces", "checkstyle:designforextension"})
    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AreaNode other = (AreaNode) obj;
        if (this.area == null)
        {
            if (other.getArea() != null)
                return false;
        }
        else if (!this.area.equals(other.getArea()))
            return false;
        if (getPoint() == null)
        {
            if (other.getPoint() != null)
                return false;
        }
        else if (!getPoint().equals(other.getPoint()))
            return false;
        return true;
    }
}
