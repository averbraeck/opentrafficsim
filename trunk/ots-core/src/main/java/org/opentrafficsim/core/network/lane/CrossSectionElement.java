package org.opentrafficsim.core.network.lane;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.media.j3d.Bounds;
import javax.vecmath.Point3d;

import nl.tudelft.simulation.dsol.animation.LocatableInterface;
import nl.tudelft.simulation.language.d3.BoundingBox;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import com.vividsolutions.jts.linearref.LengthIndexedLine;
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

    /** The lateral offset from the design line of the parentLink at the start of the parentLink. */
    private final DoubleScalar.Rel<LengthUnit> designLineOffsetAtBegin;

    /** The lateral offset from the design line of the parentLink at the end of the parentLink. */
    private final DoubleScalar.Rel<LengthUnit> designLineOffsetAtEnd;

    /** Start width, positioned <i>symmetrically around</i> the lateral start position. */
    private final DoubleScalar.Rel<LengthUnit> beginWidth;

    /** End width, positioned <i>symmetrically around</i> the lateral end position. */
    private final DoubleScalar.Rel<LengthUnit> endWidth;

    /** geometry matching the contours of the cross section element. */
    private final Geometry contour;

    /** The offset line as calculated. */
    private LineString crossSectionDesignLine;

    /** The length of the line. Calculated once at the creation. */
    private final DoubleScalar.Rel<LengthUnit> length;

    /**
     * <b>Note:</b> LEFT is seen as a positive lateral direction, RIGHT as a negative lateral direction, with the
     * direction from the StartNode towards the EndNode as the longitudinal direction.
     * @param parentLink CrossSectionLink; Link to which the element belongs.
     * @param lateralOffsetAtBegin DoubleScalar.Rel&lt;LengthUnit&gt;; the lateral offset of the design line of the new
     *            CrossSectionLink with respect to the design line of the parent Link at the start of the parent Link
     * @param lateralOffsetAtEnd DoubleScalar.Rel&lt;LengthUnit&gt;; the lateral offset of the design line of the new
     *            CrossSectionLink with respect to the design line of the parent Link at the end of the parent Link
     * @param beginWidth DoubleScalar.Rel&lt;LengthUnit&gt;; width at start, positioned <i>symmetrically around</i> the
     *            design line
     * @param endWidth DoubleScalar.Rel&lt;LengthUnit&gt;; width at end, positioned <i>symmetrically around</i> the
     *            design line
     * @throws NetworkException
     */
    public CrossSectionElement(final CrossSectionLink<?, ?> parentLink,
            final DoubleScalar.Rel<LengthUnit> lateralOffsetAtBegin, Rel<LengthUnit> lateralOffsetAtEnd,
            final DoubleScalar.Rel<LengthUnit> beginWidth, final DoubleScalar.Rel<LengthUnit> endWidth)
            throws NetworkException
    {
        super();
        this.parentLink = parentLink;
        this.designLineOffsetAtBegin = lateralOffsetAtBegin;
        this.designLineOffsetAtEnd = lateralOffsetAtEnd;
        this.beginWidth = beginWidth;
        this.endWidth = endWidth;
        this.contour = constructGeometry();
        // TODO LengthUnit and width might depend on CRS
        this.length = new DoubleScalar.Rel<LengthUnit>(this.crossSectionDesignLine.getLength(), LengthUnit.METER);
        this.parentLink.addCrossSectionElement(this);
    }

    /**
     * Find the coordinate in an array that is closest to a given reference.
     * @param reference Coordinate; the reference
     * @param list Coordinate[]; the array
     * @return int index of the Coordinate in the list that is closest to the reference
     */
    private int findClosest(Coordinate reference, Coordinate[] list)
    {
        double closest = Double.MAX_VALUE;
        int result = -1;
        for (int index = 0; index < list.length; index++)
        {
            Coordinate c = list[index];
            double distance = c.distance(reference);
            if (distance < closest)
            {
                result = index;
                closest = distance;
            }
        }
        return result;
    }

    /**
     * Create a point at a specified offset from a reference point perpendicularly left to a direction specified by an
     * additional point.
     * @param referencePoint Coordinate; the reference point
     * @param directionPoint Coordinate; the point that is used to determine the direction AT the reference point
     * @param offset double; distance of the result from the reference point
     * @return Coordinate
     */
    private Coordinate offsetPoint(Coordinate referencePoint, Coordinate directionPoint, double offset)
    {
        double angle = Math.atan2(directionPoint.y - referencePoint.y, directionPoint.x - referencePoint.x);
        angle += Math.PI / 2;
        return new Coordinate(referencePoint.x + offset * Math.sin(angle), referencePoint.y + offset * Math.cos(angle));
    }

    /** Precision of buffer operations */
    private final int quadrantSegments = 8;

    /**
     * Check if two directions are approximately equal (possibly plus or minus 2 * PI).
     * @param angle1 double; the first angle (in Radians)
     * @param angle2 double; the second angle (in Radians)
     * @param tolerance double; the tolerance (in Radians)
     * @return boolean; true if the angles are approximately equal; false otherwise
     */
    private boolean anglesApproximatelyEqual(double angle1, double angle2, double tolerance)
    {
        double deltaAngle = angle2 - angle1;
        if (Math.abs(deltaAngle) <= tolerance)
            return true;
        if (deltaAngle > 0)
        {
            deltaAngle -= Math.PI * 2;
        }
        else
        {
            deltaAngle += Math.PI * 2;
        }
        return Math.abs(deltaAngle) <= tolerance;
    }

    /**
     * Compute the direction from a reference to another point
     * @param reference Coordinate; the reference point
     * @param other Coordinate; the other point
     * @return double; the angle of the direction from reference to other in Radians
     */
    double angle(Coordinate reference, Coordinate other)
    {
        return Math.atan2(other.y - reference.y, other.x - reference.x);
    }

    /**
     * Generate a Geometry that has a fixed offset from a reference Geometry.
     * @param referenceLine Geometry; the reference line
     * @param offset double; offset distance from the reference line; positive is Left, negative is Right
     * @return Geometry; the Geometry of a line that has the specified offset from the reference line
     * @throws NetworkException on failure
     */
    private Geometry offsetGeometry(Geometry referenceLine, double offset) throws NetworkException
    {
        Coordinate[] referenceCoordinates = referenceLine.getCoordinates();
        Coordinate[] bufferCoordinates =
                referenceLine.buffer(Math.abs(offset), this.quadrantSegments, BufferParameters.CAP_FLAT)
                        .getCoordinates();
        Coordinate startCoordinate = offsetPoint(referenceCoordinates[0], referenceCoordinates[1], offset);
        int startIndex = findClosest(startCoordinate, bufferCoordinates);
        final int referenceLast = referenceCoordinates.length - 1;
        Coordinate endCoordinate =
                offsetPoint(referenceCoordinates[referenceLast], referenceCoordinates[referenceLast - 1], -offset);
        int endIndex = findClosest(endCoordinate, bufferCoordinates);
        if (endIndex == startIndex)
        {
            // Trouble; probably a circular referenceLine.
            // There should be another point very close to the current one
            double closest = Double.MAX_VALUE;
            endIndex = -1;
            for (int index = 0; index < bufferCoordinates.length; index++)
            {
                if (index == startIndex)
                {
                    continue;
                }
                double distance = bufferCoordinates[endIndex].distance(bufferCoordinates[startIndex]);
                if (distance < closest)
                {
                    endIndex = index;
                    closest = distance;
                }
            }
            if (endIndex < 0)
            {
                throw new Error("Cannot find endIndex");
            }
        }
        // Figure out which part of the buffer we need and in which direction.
        // The initial direction should be approximately parallel to the initial direction of the reference line
        double expectedAngle = angle(referenceCoordinates[0], referenceCoordinates[1]);
        final double tolerance = Math.PI / 6; // 30 degrees
        final int initialIndex;
        final int finalIndex;
        final boolean forward;
        if (anglesApproximatelyEqual(expectedAngle,
                angle(bufferCoordinates[startIndex], bufferCoordinates[(startIndex + 1) % bufferCoordinates.length]),
                tolerance))
        {
            initialIndex = startIndex;
            finalIndex = endIndex;
            forward = true;
        }
        else if (anglesApproximatelyEqual(
                expectedAngle,
                angle(bufferCoordinates[startIndex], bufferCoordinates[(startIndex + bufferCoordinates.length - 1)
                        % bufferCoordinates.length]), tolerance))
        {
            initialIndex = startIndex;
            finalIndex = endIndex;
            forward = false;
        }
        else if (anglesApproximatelyEqual(expectedAngle,
                angle(bufferCoordinates[endIndex], bufferCoordinates[(endIndex + 1) % bufferCoordinates.length]),
                tolerance))
        {
            initialIndex = endIndex;
            finalIndex = startIndex;
            forward = true;
        }
        else if (anglesApproximatelyEqual(
                expectedAngle,
                angle(bufferCoordinates[endIndex], bufferCoordinates[(endIndex + bufferCoordinates.length - 1)
                        % bufferCoordinates.length]), tolerance))
        {
            initialIndex = endIndex;
            finalIndex = startIndex;
            forward = false;
        }
        else
        {
            throw new NetworkException("Cannot determine start and end coordinates in buffer");
        }
        // Figure out how many Coordinates we will use
        int size;
        if (forward)
        {
            size = finalIndex - initialIndex;
        }
        else
        {
            size = initialIndex - finalIndex;
        }
        if (size < 0)
        {
            size += bufferCoordinates.length;
        }
        size += 1; // add room for the final Coordinate
        int index = initialIndex;
        int step = forward ? 1 : -1;
        Coordinate[] resultCoordinates = new Coordinate[size];
        for (int resultIndex = 0; resultIndex < size; resultIndex++)
        {
            resultCoordinates[resultIndex++] = bufferCoordinates[index];
            index += step;
            if (index < 0)
            {
                index = bufferCoordinates.length - 1;
            }
            else if (index >= bufferCoordinates.length)
            {
                index = 0;
            }
        }
        GeometryFactory factory = new GeometryFactory();
        Geometry result = factory.createLineString(resultCoordinates);
        return result;
    }

    /**
     * Create the Geometry of a line at offset from a reference line. The offset changes linearly from its initial value
     * at the start of the reference line to its final offset value at the end of the reference line.
     * @param referenceLine Geometry; the Geometry of the reference line
     * @param offsetAtStart double; offset at the start of the reference line (positive value is Left, negative value is
     *            Right)
     * @param offsetAtEnd double; offset at the end of the reference line (positive value is Left, negative value is
     *            Right)
     * @return Geometry; the Geometry of the line at linearly changing offset of the reference line
     * @throws NetworkException when this method fails to create the offset line
     */
    private Geometry offsetLine(Geometry referenceLine, double offsetAtStart, double offsetAtEnd)
            throws NetworkException
    {
        Geometry offsetLineAtStart = offsetGeometry(referenceLine, offsetAtStart);
        if (offsetAtStart == offsetAtEnd)
        {
            return offsetLineAtStart;
        }
        Geometry offsetLineAtEnd = offsetGeometry(referenceLine, offsetAtEnd);
        LengthIndexedLine first = new LengthIndexedLine(offsetLineAtStart);
        double firstLength = offsetLineAtStart.getLength();
        LengthIndexedLine second = new LengthIndexedLine(offsetLineAtEnd);
        double secondLength = offsetLineAtEnd.getLength();
        Coordinate[] coordinatesAtStart = offsetLineAtStart.getCoordinates();
        Coordinate[] coordinatesAtEnd = offsetLineAtEnd.getCoordinates();
        int size = Math.max(coordinatesAtStart.length, coordinatesAtEnd.length);
        Coordinate[] resultCoordinates = new Coordinate[size];
        for (int index = 0; index < size; index++)
        {
            double ratio = 1.0 * index / size;
            Coordinate firstCoordinate = first.extractPoint(ratio * firstLength);
            Coordinate secondCoordinate = second.extractPoint(ratio * secondLength);
            resultCoordinates[index] =
                    new Coordinate((1 - ratio) * firstCoordinate.x + ratio * secondCoordinate.x, (1 - ratio)
                            * firstCoordinate.y + ratio * secondCoordinate.y);
        }
        GeometryFactory factory = new GeometryFactory();
        return factory.createLineString(resultCoordinates);
    }

    /**
     * Construct a buffer geometry by offsetting the linear geometry line with a distance and constructing a so-called
     * "buffer" around it.
     * @return the geometry belonging to this CrossSectionElement.
     * @throws NetworkException
     */
    private Geometry constructGeometry() throws NetworkException
    {

        GeometryFactory factory = new GeometryFactory();
        Coordinate[] referenceCoordinates = this.parentLink.getGeometry().getLineString().getCoordinates();
        Geometry referenceGeometry = factory.createLineString(referenceCoordinates);
        Geometry resultLine;
        resultLine =
                offsetLine(referenceGeometry, this.designLineOffsetAtBegin.getSI(), this.designLineOffsetAtEnd.getSI());
        this.crossSectionDesignLine = factory.createLineString(resultLine.getCoordinates());
        Coordinate[] rightBoundary =
                offsetLine(this.crossSectionDesignLine, -this.beginWidth.getSI() / 2, -this.endWidth.getSI() / 2)
                        .getCoordinates();
        Coordinate[] leftBoundary =
                offsetLine(this.crossSectionDesignLine, this.beginWidth.getSI() / 2, this.endWidth.getSI() / 2)
                        .getCoordinates();
        int size = rightBoundary.length + leftBoundary.length;
        Coordinate[] result = new Coordinate[size];
        int resultIndex = 0;
        for (int index = 0; index < rightBoundary.length; index++)
        {
            result[resultIndex++] = rightBoundary[index];
        }
        for (int index = leftBoundary.length; --index >= 0;)
        {
            result[resultIndex++] = leftBoundary[index];
        }
        return factory.createLineString(result);
        /*
         * LineString line = this.parentLink.getGeometry().getLineString(); double width = this.beginWidth.doubleValue()
         * > 0 ? Math .max(this.beginWidth.doubleValue(), this.endWidth.doubleValue()) : Math.min(
         * this.beginWidth.doubleValue(), this.endWidth.doubleValue()); this.offsetLine =
         * (this.designLineOffsetAtBegin.doubleValue() == 0.0) ? line : offsetLineString(line,
         * this.designLineOffsetAtBegin.doubleValue()); // CoordinateReferenceSystem crs =
         * this.parentLink.getGeometry().getCRS(); if (this.beginWidth.equals(this.endWidth)) { // TODO This is done in
         * meters. Does that always fit the geometry? return this.offsetLine.buffer(0.5 * width, 8,
         * BufferParameters.CAP_FLAT); } else { // TODO algorithm to make the gradual offset change... return
         * this.offsetLine.buffer(0.5 * width, 8, BufferParameters.CAP_FLAT); }
         */
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
        return this.designLineOffsetAtBegin;
    }

    /**
     * Compute the width of this CrossSectionElement at a specified longitudinal position.
     * @param longitudinalPosition DoubleScalar&lt;LengthUnit&gt;; the longitudinal position
     * @return DoubleScalar.Rel&lt;LengthUnit&gt;; the width of this CrossSectionElement at the specified longitudinal
     *         position.
     */
    public final DoubleScalar<LengthUnit> getWidth(DoubleScalar.Rel<LengthUnit> longitudinalPosition)
    {
        return getWidth(longitudinalPosition.getSI() / getLength().getSI());
    }

    /**
     * Compute the width of this CrossSectionElement at a specified fractional longitudinal position.
     * @param fractionalPosition double; the fractional longitudinal position
     * @return DoubleScalar.Rel&lt;LengthUnit&gt;; the width of this CrossSectionElement at the specified fractional
     *         longitudinal position.
     */
    public final DoubleScalar<LengthUnit> getWidth(double fractionalPosition)
    {
        return DoubleScalar.interpolate(this.beginWidth, this.endWidth, fractionalPosition);
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
        return this.crossSectionDesignLine;
    }

    /**
     * Print the coordinates on the console.
     * @param prefix String; text to put before the output
     * @param geometry Geometry; the coordinates to print
     * @param fromIndex int; index of the first coordinate to print
     * @param toIndex int; one higher than the index of the last coordinate to print
     */
    public static void printCoordinates(final String prefix, final Geometry geometry, final int fromIndex,
            final int toIndex)
    {
        System.out.print(prefix);
        for (int i = fromIndex; i < toIndex; i++)
        {
            System.out.print(String.format(Locale.US, " %8.3f,%8.3f   ", geometry.getCoordinates()[i].x,
                    geometry.getCoordinates()[i].y));
        }
        System.out.println("");
    }

    /**
     * @param line original line
     * @param offset offset in meters (negative: left; positive: right)
     * @return line with a certain offset towards the original line
     */
    private LineString offsetLineString(final LineString line, final double offset)
    {
        // printCoordinates("      Line:", line, 0, line.getNumPoints());
        // System.out.println("  Offset: " + offset);
        // create the buffer around the line
        double offsetPlus = Math.abs(offset);
        boolean right = offset < 0.0;
        Geometry bufferLine = line.buffer(offsetPlus, 8, BufferParameters.CAP_FLAT);
        // printCoordinates("bufferLine:", bufferLine, 0, bufferLine.getNumPoints());
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
            for (int i = Math.max(is0, ie1); i >= Math.min(is0, ie1); i--)
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
            /*- original code
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
             */
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
        // printCoordinates("Result CSE: ", ls, 0, ls.getNumPoints());
        return ls;
    }

    /**
     * @param bufferLine Geometry
     * @param c0 Coordinate
     * @param c1 Coordinate
     * @param offset double; offset with respoct to the bufferLine
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
        int start = coords[0].equals(coords[coords.length - 1]) ? 1 : 0;
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

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return String.format("offset %.2fm", this.designLineOffsetAtBegin.getSI());
    }

    /**
     * Return the lateral offset from the design line of the parent Link of the Left or Right edge of this
     * CrossSectionElement at the specified fractional longitudinal position.
     * @param lateralDirection LateralDirectionality; LEFT, or RIGHT
     * @param fractionalLongitudinalPosition double; ranges from 0.0 (begin of parentLink) to 1.0 (end of parentLink)
     * @return DoubleScalar.Rel&lt;LengthUnit&gt;
     */
    public final DoubleScalar.Rel<LengthUnit> getLateralBeginPosition(final LateralDirectionality lateralDirection,
            double fractionalLongitudinalPosition)
    {
        DoubleScalar.Rel<LengthUnit> designLineOffset =
                DoubleScalar.interpolate(this.designLineOffsetAtBegin, this.designLineOffsetAtEnd,
                        fractionalLongitudinalPosition).immutable();
        DoubleScalar.Rel<LengthUnit> halfWidth =
                (DoubleScalar.Rel<LengthUnit>) DoubleScalar
                        .interpolate(this.beginWidth, this.endWidth, fractionalLongitudinalPosition).multiply(0.5)
                        .immutable();
        switch (lateralDirection)
        {
            case LEFT:
                return DoubleScalar.plus(designLineOffset, halfWidth).immutable();
            case RIGHT:
                return DoubleScalar.minus(designLineOffset, halfWidth).immutable();
            default:
                throw new Error("Bad switch on LateralDirectionality " + lateralDirection);
        }
    }
}
