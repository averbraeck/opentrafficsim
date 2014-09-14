package org.opentrafficsim.demo.ntm;

import java.rmi.RemoteException;

import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.vecmath.Point3d;

import nl.tudelft.simulation.dsol.animation.LocatableInterface;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import com.vividsolutions.jts.geom.Point;

/**
 * <p>
 * Copyright (c) 2002-2014 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights
 * reserved.
 * <p>
 * See for project information <a href="http://www.simulation.tudelft.nl/"> www.simulation.tudelft.nl</a>.
 * <p>
 * The OpenTrafficSim project is distributed under the following BSD-style license:<br>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * <ul>
 * <li>Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.</li>
 * <li>Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.</li>
 * <li>Neither the name of Delft University of Technology, nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.</li>
 * </ul>
 * This software is provided by the copyright holders and contributors "as is" and any express or implied warranties,
 * including, but not limited to, the implied warranties of merchantability and fitness for a particular purpose are
 * disclaimed. In no event shall the copyright holder or contributors be liable for any direct, indirect, incidental,
 * special, exemplary, or consequential damages (including, but not limited to, procurement of substitute goods or
 * services; loss of use, data, or profits; or business interruption) however caused and on any theory of liability,
 * whether in contract, strict liability, or tort (including negligence or otherwise) arising in any way out of the use
 * of this software, even if advised of the possibility of such damage.
 * @version Sep 12, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */
public class Node implements LocatableInterface
{
    /** the center of the area for the simplified graph */
    private final Point centroid;

    /** the area to which the node belongs */
    private final Area area;
    
    /**
     * @param centroid
     * @param area
     */
    public Node(Point centroid, Area area)
    {
        super();
        this.centroid = centroid;
        this.area = area;
    }

    /**
     * @see nl.tudelft.simulation.dsol.animation.LocatableInterface#getLocation()
     */
    @Override
    public DirectedPoint getLocation() throws RemoteException
    {
        return new DirectedPoint(new double[]{this.centroid.getX(), this.centroid.getY(), 0.0d});
    }

    /**
     * @see nl.tudelft.simulation.dsol.animation.LocatableInterface#getBounds()
     */
    @Override
    public Bounds getBounds() throws RemoteException
    {
        return new BoundingSphere(new Point3d(0.0d, 0.0d, 0.0d), 10.0d);
    }

    /**
     * @return centroid
     */
    public Point getCentroid()
    {
        return this.centroid;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "Node [centroid=" + this.centroid + "]";
    }

    /**
     * @return area
     */
    public Area getArea()
    {
        return this.area;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.area == null) ? 0 : this.area.hashCode());
        result = prime * result + ((this.centroid == null) ? 0 : this.centroid.hashCode());
        return result;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Node other = (Node) obj;
        if (this.area == null)
        {
            if (other.area != null)
                return false;
        }
        else if (!this.area.equals(other.area))
            return false;
        if (this.centroid == null)
        {
            if (other.centroid != null)
                return false;
        }
        else if (!this.centroid.equals(other.centroid))
            return false;
        return true;
    }
    
}
