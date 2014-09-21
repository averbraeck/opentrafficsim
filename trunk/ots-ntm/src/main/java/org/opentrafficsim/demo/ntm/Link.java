package org.opentrafficsim.demo.ntm;

import java.rmi.RemoteException;

import javax.media.j3d.Bounds;
import javax.vecmath.Point3d;

import nl.tudelft.simulation.dsol.animation.LocatableInterface;
import nl.tudelft.simulation.language.d3.BoundingBox;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Sep 12, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class Link implements LocatableInterface
{
    /** node A. */
    private Node nodeA;

    /** node B. */
    private Node nodeB;

    /** name. */
    private String name;

    /** length. */
    private double length;

    /** speed. */
    private double speed;

    /** capacity. */
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

    /** {@inheritDoc} */
    @Override
    public DirectedPoint getLocation() throws RemoteException
    {
        return new DirectedPoint(new double[]{this.nodeA.getCentroid().getX(), this.nodeA.getCentroid().getY(), 0.0d});
    }

    /** {@inheritDoc} */
    @Override
    public Bounds getBounds() throws RemoteException
    {
        return new BoundingBox(new Point3d(0.0d, 0.0d, 0.0d), new Point3d(this.nodeB.getCentroid().getX()
                - this.nodeA.getCentroid().getX(), this.nodeB.getCentroid().getY() - this.nodeA.getCentroid().getY(),
                0.0d));
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
