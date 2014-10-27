package org.opentrafficsim.core.network;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.media.j3d.Bounds;
import javax.vecmath.Point3d;

import nl.tudelft.simulation.dsol.animation.LocatableInterface;
import nl.tudelft.simulation.language.d3.BoundingBox;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import com.vividsolutions.jts.operation.buffer.BufferParameters;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Aug 19, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public abstract class CrossSectionElement implements LocatableInterface
{
    /** Cross Section Link to which the element belongs. */
    private final CrossSectionLink<?, ?> parentLink;

    /** the lateral start position compared to the linear geometry of the Cross Section Link. */
    private final DoubleScalar.Rel<LengthUnit> lateralCenterPosition;

    /**
     * the lowest value lateral position of the edge at the begin compared to the linear geometry of the Cross Section
     * Link.
     */
    private final DoubleScalar.Rel<LengthUnit> lateralBeginStartPosition;

    /**
     * the highest value lateral position of the edge at the begin compared to the linear geometry of the Cross Section
     * Link.
     */
    private final DoubleScalar.Rel<LengthUnit> lateralBeginEndPosition;

    /** start width, positioned <i>symmetrically around</i> the lateral start position. */
    private final DoubleScalar.Rel<LengthUnit> beginWidth;

    /** end width, positioned <i>symmetrically around</i> the lateral end position. */
    private final DoubleScalar.Rel<LengthUnit> endWidth;

    /** geometry matching the contours of the cross section element. */
    private final Geometry contour;

    /** the offset line as calculated. */
    private LineString offsetLine;

    /** the length of the line. Calculated once at the creation. */
    private DoubleScalar.Rel<LengthUnit> length;

    /**
     * <b>Note:</b> LEFT is seen as a positive lateral direction, RIGHT as a negative lateral direction, with the
     * direction from the StartNode towards the EndNode as the longitudinal direction.
     * @param parentLink Cross Section Link to which the element belongs.
     * @param lateralCenterPosition the lateral start position compared to the linear geometry of the Cross Section
     *            Link.
     * @param beginWidth start width, positioned <i>symmetrically around</i> the lateral start position.
     * @param endWidth end width, positioned <i>symmetrically around</i> the lateral end position.
     */
    public CrossSectionElement(final CrossSectionLink<?, ?> parentLink,
            final DoubleScalar.Rel<LengthUnit> lateralCenterPosition, final DoubleScalar.Rel<LengthUnit> beginWidth,
            final DoubleScalar.Rel<LengthUnit> endWidth)
    {
        super();
        this.parentLink = parentLink;
        this.lateralCenterPosition = lateralCenterPosition;
        this.beginWidth = beginWidth;
        this.endWidth = endWidth;
        this.contour = constructGeometry();
        // TODO: LengthUnit and width might depend on CRS
        this.length = new DoubleScalar.Rel<LengthUnit>(this.offsetLine.getLength(), LengthUnit.METER);
        DoubleScalar.Rel<LengthUnit> halfWidth =
                new DoubleScalar.Rel<>(beginWidth.getInUnit() / 2, beginWidth.getUnit());
        this.lateralBeginStartPosition = DoubleScalar.minus(lateralCenterPosition, halfWidth).immutable();
        this.lateralBeginEndPosition = DoubleScalar.plus(lateralCenterPosition, halfWidth).immutable();
    }

    /**
     * Construct a buffer geometry by offsetting the linear geometry line with a distance and constructing a so-called
     * "buffer" around it.
     * @return the geometry belonging to this CrossSectionElement.
     */
    private Geometry constructGeometry()
    {
        LineString line = this.parentLink.getGeometry().getLineString();
        double width =
                this.beginWidth.doubleValue() > 0 ? Math
                        .max(this.beginWidth.doubleValue(), this.endWidth.doubleValue()) : Math.min(
                        this.beginWidth.doubleValue(), this.endWidth.doubleValue());
        this.offsetLine =
                (this.lateralCenterPosition.doubleValue() == 0.0) ? line : offsetLineString(line,
                        this.lateralCenterPosition.doubleValue());
        // CoordinateReferenceSystem crs = this.parentLink.getGeometry().getCRS();
        if (this.beginWidth.equals(this.endWidth))
        {
            // TODO: This is done in meters. Does that always fit the geometry?
            return this.offsetLine.buffer(0.5 * width, 8, BufferParameters.CAP_FLAT);
        }
        else
        {
            // TODO: algorithm to make the gradual offset change...
            return this.offsetLine.buffer(0.5 * width, 8, BufferParameters.CAP_FLAT);
        }
    }

    /**
     * @return parentLink.
     */
    public final CrossSectionLink<?, ?> getParentLink()
    {
        return this.parentLink;
    }

    /**
     * @return lateralCenterPosition.
     */
    public final DoubleScalar<LengthUnit> getLateralCenterPosition()
    {
        return this.lateralCenterPosition;
    }

    /**
     * @return beginWidth.
     */
    public final DoubleScalar<LengthUnit> getBeginWidth()
    {
        return this.beginWidth;
    }

    /**
     * @return endWidth.
     */
    public final DoubleScalar<LengthUnit> getEndWidth()
    {
        return this.endWidth;
    }

    /** {@inheritDoc} */
    @Override
    public final DirectedPoint getLocation() throws RemoteException
    {
        Envelope e = this.contour.getEnvelopeInternal();
        return new DirectedPoint(0.5 * (e.getMaxX() - e.getMinX()), 0.5 * (e.getMaxY() - e.getMinY()), 0.0);
    }

    /** {@inheritDoc} */
    @Override
    public final Bounds getBounds() throws RemoteException
    {
        Envelope e = this.contour.getEnvelopeInternal();
        double dx = 0.5 * (e.getMaxX() - e.getMinX());
        double dy = 0.5 * (e.getMaxY() - e.getMinY());
        return new BoundingBox(new Point3d(e.getMinX() - dx, e.getMinY() - dy, 0.0), new Point3d(e.getMinX() + dx,
                e.getMinY() + dy, 0.0));
    }

    /**
     * @return the contour of the cross section element.
     */
    public final Geometry getContour()
    {
        return this.contour;
    }

    /**
     * @return offsetLine.
     */
    public final LineString getOffsetLine()
    {
        return this.offsetLine;
    }

    /**
     * @param line original line
     * @param offset offset in meters (negative: left; positive: right)
     * @return line with a certain offset towards the original line
     */
    private LineString offsetLineString(final LineString line, final double offset)
    {
        // create the buffer around the line
        double offsetPlus = Math.abs(offset);
        boolean right = (offset < 0.0);
        Geometry bufferLine = line.buffer(offsetPlus, 8, BufferParameters.CAP_FLAT);
        Coordinate[] bufferCoords = bufferLine.getCoordinates();
        // intersect with perpendicular lines at the start and end
        Coordinate[] lineCoords = line.getCoordinates();
        Coordinate[] sc = perpBufferCoords(line, lineCoords[0], lineCoords[1], offsetPlus);
        Coordinate[] ec =
                perpBufferCoords(line, lineCoords[lineCoords.length - 1], lineCoords[lineCoords.length - 2], offsetPlus);
        GeometryFactory factory = new GeometryFactory();
        CoordinateSequence cs;
        int is0 = smallestIndex(bufferCoords, sc[0]);
        int is1 = smallestIndex(bufferCoords, sc[1]);
        int ie0 = smallestIndex(bufferCoords, ec[0]);
        int ie1 = smallestIndex(bufferCoords, ec[1]);
        List<Coordinate> cList = new ArrayList<Coordinate>();
        if (right)
        {
            // from sc[0] to ec[1]
            for (int i = Math.min(is0, ie1); i <= Math.max(is0, ie1); i++)
            {
                cList.add(bufferCoords[i]);
            }
            if (cList.contains(sc[1]) || cList.contains(ec[0]))
            {
                // wrong path (U-shape) -- take the other one
                cList = new ArrayList<Coordinate>();
                for (int i = Math.max(is0, ie1); i <= Math.min(is0, ie1) + bufferCoords.length; i++)
                {
                    int index = i % bufferCoords.length;
                    cList.add(bufferCoords[index]);
                }
            }
        }
        else
        {
            // from sc[1] to ec[0]
            for (int i = Math.min(is1, ie0); i <= Math.max(is1, ie0); i++)
            {
                cList.add(bufferCoords[i]);
            }
            if (cList.contains(sc[0]) || cList.contains(ec[1]))
            {
                // wrong path (U-shape) -- take the other one
                cList = new ArrayList<Coordinate>();
                for (int i = Math.max(is1, ie0); i <= Math.min(is1, ie0) + bufferCoords.length; i++)
                {
                    int index = i % bufferCoords.length;
                    cList.add(bufferCoords[index]);
                }
            }
        }
        Coordinate[] cc = new Coordinate[cList.size()];
        cs = new CoordinateArraySequence(cList.toArray(cc));
        LineString ls = new LineString(cs, factory);
        return ls;
    }

    /**
     * @param bufferLine
     * @param c0
     * @param c1
     * @param offset
     * @return perpendicular buffer line
     */
    private Coordinate[] perpBufferCoords(final Geometry bufferLine, final Coordinate c0, final Coordinate c1,
            final double offset)
    {
        double sdx = c1.x - c0.x;
        double sdy = c1.y - c0.y;
        double norm = Math.abs(offset / Math.sqrt(sdx * sdx + sdy * sdy));
        Coordinate p0 = new Coordinate(c0.x + norm * sdy, c0.y - norm * sdx);
        Coordinate p1 = new Coordinate(c0.x - norm * sdy, c0.y + norm * sdx);
        return new Coordinate[]{p0, p1};
    }

    /**
     * Find the index of the closest coordinate to the search coordinate.
     * @param coords the coordinates to search in
     * @param search the coordinate to search
     * @return the index of the closest coordinate to the search coordinate
     */
    private int smallestIndex(final Coordinate[] coords, final Coordinate search)
    {
        int bestI = -1;
        double bestD = Double.MAX_VALUE;
        int start = (coords[0].equals(coords[coords.length - 1])) ? 1 : 0;
        for (int i = start; i < coords.length; i++)
        {
            double d = search.distance(coords[i]);
            if (d < bestD)
            {
                bestD = d;
                bestI = i;
            }
        }
        return bestI;
    }

    /**
     * @return length.
     */
    public final DoubleScalar.Rel<LengthUnit> getLength()
    {
        return this.length;
    }

    /**
     * @return lateralBeginStartPosition.
     */
    public final DoubleScalar.Rel<LengthUnit> getLateralBeginStartPosition()
    {
        return this.lateralBeginStartPosition;
    }

    /**
     * @return lateralBeginEndPosition.
     */
    public final DoubleScalar.Rel<LengthUnit> getLateralBeginEndPosition()
    {
        return this.lateralBeginEndPosition;
    }
}
