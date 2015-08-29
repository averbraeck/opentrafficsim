package org.opentrafficsim.demo.ntm;

import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.network.OTSNode;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author: pknoppers
 * $, initial version 7 Oct 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */
public class Node extends OTSNode implements Comparable<Node>
{
    /**
     * <p>
     * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
     * reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * $LastChangedDate$, @version $Revision$, by $Author:
     * pknoppers $, initial version 10 Oct 2014 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
     * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
     */
    public enum TrafficBehaviourType {
        NTM,
        CORDON,
        FLOW,
        CENTROID,
        ROAD
    };

    /** */

    /** */
    // private static long indexNumber = 0;

    private static final long serialVersionUID = 7273393005308265130L;

    /** */
    private TrafficBehaviourType behaviourType;

    /** */
    // private final long id;

    /**
     * @param nr to Identify
     * @param point ...
     * @param behaviourType describes traffic behaviour of units moving through the "node"
     */
    public Node(String nr, Coordinate point, TrafficBehaviourType behaviourType)
    {
        super(nr, new OTSPoint3D(point));
        // long index = indexNumber++;
        this.behaviourType = behaviourType;

    }

    /**
     * create a ShpNode Point
     * @param x1 coord
     * @param y1 coord
     * @return new Point
     */
    public static Coordinate createPoint(double x1, double y1)
    {
        return new Coordinate(x1, y1);
    }

    /**
     * @return behaviourType.
     */
    public final TrafficBehaviourType getBehaviourType()
    {
        return this.behaviourType;
    }

    /**
     * @param behaviourType set behaviourType.
     */
    public final void setBehaviourType(final TrafficBehaviourType behaviourType)
    {
        this.behaviourType = behaviourType;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
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
        Node other = (Node) obj;
        if (getPoint() == null)
        {
            if (other.getPoint().getCoordinate() != null)
                return false;
        }
        else if (!getPoint().equals(other.getPoint().getCoordinate()))
            return false;
        return true;
    }

    /** {@inheritDoc} */
    public int compareTo(Node o)
    {
        return this.getId().compareTo(o.getId());
    }

}
