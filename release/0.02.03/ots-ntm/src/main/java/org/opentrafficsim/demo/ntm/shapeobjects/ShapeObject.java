package org.opentrafficsim.demo.ntm.shapeobjects;

import java.awt.geom.Path2D;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Set;

import javax.media.j3d.Bounds;

import nl.tudelft.simulation.dsol.animation.LocatableInterface;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import com.vividsolutions.jts.geom.Geometry;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author: pknoppers
 * $, initial version 13 Nov 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */
public class ShapeObject implements LocatableInterface
{
    /** */
    private Geometry geometry;

    /** */
    private ArrayList<String> values;

    /** the lines for the animation, relative to the centroid. */
    private Set<Path2D> lines = null;

    /**
     * @param theGeom
     * @param values
     */
    public ShapeObject(Geometry geometry, ArrayList<String> values)
    {
        super();
        this.geometry = geometry;
        this.values = values;
    }

    /*    *//**
     * @return polygon
     * @throws RemoteException
     */
    /*
     * public Set<Path2D> getLines() throws RemoteException { // create the polygon if it did not exist before if
     * (this.lines == null) { double dx = this.getLocation().getX(); double dy = this.getLocation().getY(); // double dx
     * = 0; // double dy = 0; this.lines = new HashSet<Path2D>(); for (int i = 0; i <
     * this.getDesignLine().getLineString().getNumGeometries(); i++) { Path2D line = new Path2D.Double(); Geometry g =
     * this.getDesignLine().getLineString().getDesignLineN(i); boolean start = true; for (Coordinate c : g.getCoordinates())
     * { if (start) { line.moveTo(c.x - dx, dy - c.y); start = false; } else { line.lineTo(c.x - dx, dy - c.y); } }
     * this.lines.add(line); } } return this.lines; }
     */

    /**
     * @return theGeom.
     */
    public Geometry getDesignLine()
    {
        return this.geometry;
    }

    /**
     * @param theGeom set theGeom.
     */
    public void setGeometry(Geometry geometry)
    {
        this.geometry = geometry;
    }

    /**
     * @return table.
     */
    public ArrayList<String> getValues()
    {
        return this.values;
    }

    /**
     * @param table set table.
     */
    public void setValues(ArrayList<String> values)
    {
        this.values = values;
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
