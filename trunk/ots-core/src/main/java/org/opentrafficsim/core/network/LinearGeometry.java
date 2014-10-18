package org.opentrafficsim.core.network;

import java.io.Serializable;

import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.CRS;
import org.geotools.referencing.GeodeticCalculator;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.coordinate.PointArray;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;

/**
 * This class implements a geotools-based geometry for a link in a network.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Aug 21, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public class LinearGeometry implements Serializable
{
    /** */
    private static final long serialVersionUID = 20141008L;

    /** link the geometry belongs to. */
    private final AbstractLink<?, ?> link;

    /** geotools (or jts in this case) geometry that represents the link in 3D. */
    private final LineString line;

    /** Coordinate Reference System. */
    private final CoordinateReferenceSystem crs;

    /** length in meters. */
    private final DoubleScalar.Rel<LengthUnit> length;

    /**
     * @param link the link for which this geometry applies.
     * @param pointArray the geometry from geotools that represents the link in a CRS.
     * @throws NetworkException when transformation for distance calculation failed.
     */
    public LinearGeometry(final AbstractLink<?, ?> link, final PointArray pointArray) throws NetworkException
    {
        super();
        this.link = link;
        this.crs = pointArray.getCoordinateReferenceSystem();
        double len = 0.0;
        GeodeticCalculator calc = new GeodeticCalculator(this.crs);
        Coordinate[] coords = new Coordinate[pointArray.size()];
        DirectPosition prevPos = null;
        for (int i = 0; i < pointArray.size(); i++)
        {
            DirectPosition p = pointArray.get(i).getDirectPosition();
            double x = p.getOrdinate(0);
            double y = p.getDimension() > 1 ? p.getOrdinate(1) : 0;
            double z = p.getDimension() > 2 ? p.getOrdinate(2) : 0;
            coords[i] = new Coordinate(x, y, z);
            if (i > 0)
            {
                try
                {
                    calc.setStartingPosition(prevPos);
                    calc.setDestinationPosition(p);
                    len += calc.getOrthodromicDistance();
                }
                catch (TransformException te)
                {
                    throw new NetworkException("When constructing LinearGeometry for link=" + link.toString()
                            + ": transformation for distance calculation failed in CRS=" + this.crs.toWKT(), te);
                }
            }
            prevPos = p;
        }
        CoordinateSequence points = new CoordinateArraySequence(coords);
        GeometryFactory factory = new GeometryFactory();
        this.line = new LineString(points, factory);
        this.length = new DoubleScalar.Rel<LengthUnit>(len, LengthUnit.METER);
        link.setGeometry(this);
    }

    /**
     * @param link the link for which this geometry applies.
     * @param lineString a JTS LineString representing the geometry.
     * @param crs the Coordinate Reference System for this line.
     * @throws NetworkException when transformation for distance calculation failed.
     */
    public LinearGeometry(final AbstractLink<?, ?> link, final LineString lineString, final CoordinateReferenceSystem crs)
            throws NetworkException
    {
        super();
        this.link = link;
        this.crs = crs;
        double len = 0.0;
        DirectPosition prevPos = null;
        GeodeticCalculator calc = null;
        if (CRS.getEllipsoid(this.crs) != null)
        {
            calc = new GeodeticCalculator(this.crs);
        }
        for (int i = 0; i < lineString.getNumPoints(); i++)
        {
            Point point = lineString.getPointN(i);
            DirectPosition pos = new DirectPosition2D(this.crs, point.getX(), point.getY());
            if (i > 0)
            {
                try
                {
                    if (calc != null)
                    {
                        calc.setStartingPosition(prevPos);
                        calc.setDestinationPosition(pos);
                        len += calc.getOrthodromicDistance();
                    }
                    else
                    {
                        // TODO: see if CRS is in meters...
                        double dx = prevPos.getDirectPosition().getCoordinate()[0] - pos.getDirectPosition().getCoordinate()[0];
                        double dy = prevPos.getDirectPosition().getCoordinate()[1] - pos.getDirectPosition().getCoordinate()[1];
                        len += Math.sqrt(dx * dx + dy * dy);
                    }
                }
                catch (TransformException te)
                {
                    throw new NetworkException("When constructing LinearGeometry for link=" + link.toString()
                            + ": transformation for distance calculation failed in CRS=" + this.crs.toWKT(), te);
                }
            }
            prevPos = pos;
        }
        this.line = lineString;
        this.length = new DoubleScalar.Rel<LengthUnit>(len, LengthUnit.METER);
        link.setGeometry(this);
    }

    // TODO: possibly add a couple of other constructors for convenience.

    /**
     * @return link.
     */
    public final AbstractLink<?, ?> getLink()
    {
        return this.link;
    }

    /**
     * @return line.
     */
    public final LineString getLineString()
    {
        return this.line;
    }

    /**
     * @return crs.
     */
    public final CoordinateReferenceSystem getCRS()
    {
        return this.crs;
    }

    /**
     * @return line length.
     */
    public final DoubleScalar.Rel<LengthUnit> getLineLength()
    {
        return this.length;
    }
}
