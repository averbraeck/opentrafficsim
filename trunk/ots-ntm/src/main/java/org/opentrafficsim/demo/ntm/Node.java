package org.opentrafficsim.demo.ntm;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Comparator;

import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.vecmath.Point3d;

import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.geotools.geometry.jts.JTSFactoryFinder;
import org.opentrafficsim.core.network.AbstractNode;
import org.opentrafficsim.core.network.geotools.NodeGeotools;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$, initial version7 Oct 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */
public class Node extends NodeGeotools<String> implements Comparable<Node>
{
    /**
     * <p>
     * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
     * reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * $LastChangedDate$, @version $Revision$, by $Author$, initial version10 Oct 2014 <br>
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
    //private final long id;
    
    /**
     * @param nr to Identify
     * @param point ...
     * @param behaviourType describes traffic behaviour of units moving through the "node"
     */
    public Node(String nr, Coordinate point, TrafficBehaviourType behaviourType)
    {
        super(nr, point);
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
            if (other.getPoint() != null)
                return false;
        }
        else if (!getPoint().equals(other.getPoint()))
            return false;
        return true;
    }

    /** {@inheritDoc} */
    public int compareTo(Node o)
    {
        return  this.getId().compareTo(o.getId());
    }



}
