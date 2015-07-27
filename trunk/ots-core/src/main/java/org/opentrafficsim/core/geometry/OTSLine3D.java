package org.opentrafficsim.core.geometry;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;

import javax.media.j3d.Bounds;

import nl.tudelft.simulation.dsol.animation.LocatableInterface;
import nl.tudelft.simulation.language.d3.BoundingBox;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-16 10:20:53 +0200 (Thu, 16 Jul 2015) $, @version $Revision: 1124 $, by $Author: pknoppers $,
 * initial version Jul 22, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public class OTSLine3D implements LocatableInterface, Serializable
{
    /** */
    private static final long serialVersionUID = 20150722L;

    /** the points of the line. */
    private final OTSPoint3D[] points;

    /** the cached length; will be calculated when needed for the first time. */
    private double length = Double.NaN;

    /** the cached centroid; will be calculated when needed for the first time. */
    private OTSPoint3D centroid = null;

    /** the cached bounds; will be calculated when needed for the first time. */
    private Bounds bounds = null;

    /**
     * @param points the array of points to construct this OTSLine3D from.
     */
    public OTSLine3D(final OTSPoint3D[] points)
    {
        this.points = points;
    }

    /**
     * @param coordinates the array of coordinates to construct this OTSLine3D from.
     */
    public OTSLine3D(final Coordinate[] coordinates)
    {
        this.points = new OTSPoint3D[coordinates.length];
        int i = 0;
        for (Coordinate c : coordinates)
        {
            this.points[i++] = new OTSPoint3D(c);
        }
    }

    /**
     * @param lineString the lineString to construct this OTSLine3D from.
     */
    public OTSLine3D(final LineString lineString)
    {
        this(lineString.getCoordinates());
    }

    /**
     * @param geometry the geometry to construct this OTSLine3D from.
     */
    public OTSLine3D(final Geometry geometry)
    {
        this(geometry.getCoordinates());
    }

    /**
     * @param pointList the list of points to construct this OTSLine3D from.
     */
    public OTSLine3D(final List<OTSPoint3D> pointList)
    {
        this(pointList.toArray(new OTSPoint3D[pointList.size()]));
    }

    /**
     * @return an array of Coordinates corresponding to this OTSLine.
     */
    public final Coordinate[] getCoordinates()
    {
        Coordinate[] result = new Coordinate[size()];
        for (int i = 0; i < size(); i++)
        {
            result[i] = this.points[i].getCoordinate();
        }
        return result;
    }

    /**
     * @return a LineString corresponding to this OTSLine.
     */
    public final LineString getLineString()
    {
        GeometryFactory factory = new GeometryFactory();
        Coordinate[] coordinates = getCoordinates();
        CoordinateSequence cs = factory.getCoordinateSequenceFactory().create(coordinates);
        return new LineString(cs, factory);
    }

    /**
     * @return the number of points on the line.
     */
    public final int size()
    {
        return this.points.length;
    }

    /**
     * @param i the index of the point to retrieve
     * @return the i-th point of the line.
     * @throws OTSGeometryException when i &lt; 0 or i &gt; the number of points
     */
    public final OTSPoint3D get(final int i) throws OTSGeometryException
    {
        if (i < 0 || i > size() - 1)
        {
            throw new OTSGeometryException("OTSLine3D.get(i=" + i + "); i<0 or i>=size(), which is " + size());
        }
        return this.points[i];
    }

    /**
     * @return the length of the line in SI units.
     */
    public final synchronized double getLengthSI()
    {
        if (Double.isNaN(this.length))
        {
            this.length = 0.0;
            for (int i = 0; i < size() - 1; i++)
            {
                this.length += this.points[i].distanceSI(this.points[i + 1]);
            }
        }
        return this.length;
    }

    /**
     * @return the length of the line.
     */
    public final DoubleScalar.Rel<LengthUnit> getLength()
    {
        return new DoubleScalar.Rel<LengthUnit>(getLengthSI(), LengthUnit.SI);
    }

    /**
     * @return the points of this line.
     */
    public final OTSPoint3D[] getPoints()
    {
        return this.points;
    }

    /**
     * Calculate the centroid of this line, and the bounds, and cache for later use. Make sure the dx, dy and dz are at least
     * 0.5 m wide.
     */
    private void calcCentroidBounds()
    {
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;
        for (OTSPoint3D p : this.points)
        {
            minX = Math.min(minX, p.x);
            minY = Math.min(minY, p.y);
            minZ = Math.min(minZ, p.z);
            maxX = Math.max(maxX, p.x);
            maxY = Math.max(maxY, p.y);
            maxZ = Math.max(maxZ, p.z);
        }
        this.centroid = new OTSPoint3D((maxX + minX) / 2, (maxY + minY) / 2, (maxZ + minZ) / 2);
        double deltaX = Math.max(maxX - minX, 0.5);
        double deltaY = Math.max(maxY - minY, 0.5);
        double deltaZ = Math.max(maxZ - minZ, 0.5);
        this.bounds = new BoundingBox(deltaX, deltaY, deltaZ);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public DirectedPoint getLocation() throws RemoteException
    {
        if (this.centroid == null)
        {
            calcCentroidBounds();
        }
        return this.centroid.getDirectedPoint();
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public Bounds getBounds() throws RemoteException
    {
        if (this.bounds == null)
        {
            calcCentroidBounds();
        }
        return this.bounds;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return Arrays.toString(this.points);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.bounds == null) ? 0 : this.bounds.hashCode());
        result = prime * result + ((this.centroid == null) ? 0 : this.centroid.hashCode());
        long temp;
        temp = Double.doubleToLongBits(this.length);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + Arrays.hashCode(this.points);
        return result;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings({"checkstyle:designforextension", "checkstyle:needbraces"})
    public boolean equals(final Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OTSLine3D other = (OTSLine3D) obj;
        if (this.bounds == null)
        {
            if (other.bounds != null)
                return false;
        }
        else if (!this.bounds.equals(other.bounds))
            return false;
        if (this.centroid == null)
        {
            if (other.centroid != null)
                return false;
        }
        else if (!this.centroid.equals(other.centroid))
            return false;
        if (Double.doubleToLongBits(this.length) != Double.doubleToLongBits(other.length))
            return false;
        if (!Arrays.equals(this.points, other.points))
            return false;
        return true;
    }

}
