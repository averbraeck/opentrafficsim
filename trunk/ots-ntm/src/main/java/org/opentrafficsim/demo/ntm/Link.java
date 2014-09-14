package org.opentrafficsim.demo.ntm;

import java.rmi.RemoteException;

import javax.media.j3d.Bounds;
import javax.vecmath.Point3d;

import nl.tudelft.simulation.dsol.animation.LocatableInterface;
import nl.tudelft.simulation.language.d3.BoundingBox;
import nl.tudelft.simulation.language.d3.DirectedPoint;

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
 */
public class Link implements LocatableInterface
{
    /** node A */
    private Node nodeA;

    /** node B */
    private Node nodeB;

    /** name */
    private String name;

    /** length */
    private double length;

    /** speed */
    private double speed;

    /** capacity */
    private double capacity;

    /**
     * @param nodeA
     * @param nodeB
     * @param name
     */
    public Link(Node nodeA, Node nodeB, String name)
    {
        super();
        this.nodeA = nodeA;
        this.nodeB = nodeB;
        this.name = name;
    }

    /**
     * @see nl.tudelft.simulation.dsol.animation.LocatableInterface#getLocation()
     */
    @Override
    public DirectedPoint getLocation() throws RemoteException
    {
        return new DirectedPoint(new double[]{this.nodeA.getCentroid().getX(), this.nodeA.getCentroid().getY(), 0.0d});
    }

    /**
     * @see nl.tudelft.simulation.dsol.animation.LocatableInterface#getBounds()
     */
    @Override
    public Bounds getBounds() throws RemoteException
    {
        return new BoundingBox(new Point3d(0.0d, 0.0d, 0.0d),
                new Point3d(this.nodeB.getCentroid().getX() - this.nodeA.getCentroid().getX(), this.nodeB.getCentroid()
                        .getY() - this.nodeA.getCentroid().getY(), 0.0d));
    }

    /**
     * @return nodeA
     */
    public Node getNodeA()
    {
        return this.nodeA;
    }

    /**
     * @param nodeA set nodeA
     */
    public void setNodeA(Node nodeA)
    {
        this.nodeA = nodeA;
    }

    /**
     * @return nodeB
     */
    public Node getNodeB()
    {
        return this.nodeB;
    }

    /**
     * @param nodeB set nodeB
     */
    public void setNodeB(Node nodeB)
    {
        this.nodeB = nodeB;
    }

    /**
     * @return name
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * @param name set name
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return length
     */
    public double getLength()
    {
        return this.length;
    }

    /**
     * @param length set length
     */
    public void setLength(double length)
    {
        this.length = length;
    }

    /**
     * @return speed
     */
    public double getSpeed()
    {
        return this.speed;
    }

    /**
     * @param speed set speed
     */
    public void setSpeed(double speed)
    {
        this.speed = speed;
    }

    /**
     * @return capacity
     */
    public double getCapacity()
    {
        return this.capacity;
    }

    /**
     * @param capacity set capacity
     */
    public void setCapacity(double capacity)
    {
        this.capacity = capacity;
    }

}
