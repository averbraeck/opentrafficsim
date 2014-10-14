package org.opentrafficsim.demo.ntm;

import java.rmi.RemoteException;

import javax.media.j3d.Bounds;

import com.vividsolutions.jts.geom.Geometry;

import nl.tudelft.simulation.dsol.animation.LocatableInterface;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 14 Oct 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */
public class FlowCell extends GeoObject implements LocatableInterface
{

    /** */
    private double cellLength;

    /** */
    private double capacity;

    /**
     * @param geometry
     * @param cellLength
     * @param capacity
     */
    public FlowCell(Geometry geometry, double cellLength, double capacity)
    {
        super(geometry);
        this.cellLength = cellLength;
        this.capacity = capacity;
    }

    /**
     * @return cellLength.
     */
    public double getCellLength()
    {
        return this.cellLength;
    }

    /**
     * @param cellLength set cellLength.
     */
    public void setCellLength(double cellLength)
    {
        this.cellLength = cellLength;
    }

    /**
     * @return capacity.
     */
    public double getCapacity()
    {
        return this.capacity;
    }

    /**
     * @param capacity set capacity.
     */
    public void setCapacity(double capacity)
    {
        this.capacity = capacity;
    }

    /** {@inheritDoc} */
    @Override
    public DirectedPoint getLocation() throws RemoteException
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Bounds getBounds() throws RemoteException
    {
        return null;
    }

}
