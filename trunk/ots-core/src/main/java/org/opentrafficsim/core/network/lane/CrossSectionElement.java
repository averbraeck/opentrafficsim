package org.opentrafficsim.core.network.lane;

import java.rmi.RemoteException;
import java.util.ArrayList;
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
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
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
        return new Coordinate(referencePoint.x + offset * Math.cos(angle), referencePoint.y + offset * Math.sin(angle));
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
        // printCoordinates("reference", referenceCoordinates);
        Coordinate[] bufferCoordinates =
                referenceLine.buffer(Math.abs(offset), this.quadrantSegments, BufferParameters.CAP_FLAT)
                        .getCoordinates();
        // printCoordinates("buffer           ", bufferCoordinates);
        boolean ringDetected = bufferCoordinates[0].distance(bufferCoordinates[bufferCoordinates.length - 1]) > 0;
        if (!ringDetected)
        {
            //System.out.println("Removing last Coordinate from buffer");
            Coordinate[] tempBuffer = new Coordinate[bufferCoordinates.length - 1];
            for (int i = 0; i < tempBuffer.length; i++)
            {
                tempBuffer[i] = bufferCoordinates[i];
            }
            bufferCoordinates = tempBuffer;
        }
        else
        {
            //System.out.println("NOT removing last coordinate from bufferCoordinates");
        }
        // printCoordinates("buffer           ", bufferCoordinates);
        Coordinate startCoordinate = offsetPoint(referenceCoordinates[0], referenceCoordinates[1], offset);
        int startIndex = findClosest(startCoordinate, bufferCoordinates);
        final int referenceLast = referenceCoordinates.length - 1;
        Coordinate endCoordinate =
                offsetPoint(referenceCoordinates[referenceLast], referenceCoordinates[referenceLast - 1], -offset);
        int endIndex = findClosest(endCoordinate, bufferCoordinates);
        //System.out.println(String.format("startIndex: %d, (%8.3f,%8.3f) endIndex: %d (%8.3f, %8.3f), distance %f",
        //        startIndex, bufferCoordinates[startIndex].x, bufferCoordinates[startIndex].y, endIndex,
        //        bufferCoordinates[endIndex].x, bufferCoordinates[endIndex].y,
        //        bufferCoordinates[startIndex].distance(bufferCoordinates[endIndex])));
        double expectedAngle = angle(referenceCoordinates[0], referenceCoordinates[1]);
        final double tolerance = Math.PI / 6; // 30 degrees
        final double tooClose = 0.001;
        if (ringDetected)
        {
            // Trouble; probably a circular referenceLine.
            // This generates two sets of coordinates that are stored consecutively as a single polygon
            //System.out.println("Trouble");
            //printCoordinates("bufferCoordinates", bufferCoordinates);
            for (int i = 0; i < bufferCoordinates.length; i++)
            {
                if (startCoordinate.distance(bufferCoordinates[i]) < tooClose)
                {
                    //System.out.println(String.format("coordinate %d matches startcoordinate", i));
                }
                if (endCoordinate.distance(bufferCoordinates[i]) < tooClose)
                {
                    //System.out.println(String.format("coordinate %d matches endcoordinate", i));
                }
            }
            // Separate the bufferCoordinates in an inner an outer ring
            // Some experimentation has shown (but NOT proved) that there is only one transition between the inner and
            // outer rings and both rings are closed.
            int boundary = -1;
            for (int index = 1; index < bufferCoordinates.length; index++)
            {
                if (bufferCoordinates[index].distance(bufferCoordinates[0]) == 0)
                {
                    boundary = index + 1;
                    break;
                }
            }
            //System.out.println(String.format("boundary %d: %8.3f,%8.3f", boundary, bufferCoordinates[boundary].x,
            //        bufferCoordinates[boundary].y));
            if (boundary < 0 || bufferCoordinates.length - boundary < 3)
            {
                throw new NetworkException("Cannot figure out offsetGeometry (ring1 took too many coordinates)");
            }
            if (bufferCoordinates[boundary].distance(bufferCoordinates[bufferCoordinates.length - 1]) > 0)
            {
                throw new NetworkException("Cannot figure out offsetGeometry (ring2 is not closed)");
            }
            // Figure out which ring must be discarded
            // startIndex may be wrong; search again
            double endExpectedAngle =
                    angle(referenceCoordinates[referenceCoordinates.length - 2],
                            referenceCoordinates[referenceCoordinates.length - 1]);
            for (int i = 0; i < bufferCoordinates.length; i++)
            {
                if (startIndex != i)
                {
                    Coordinate c = bufferCoordinates[i];
                    if (startCoordinate.distance(c) < tooClose)
                    {
                        double angle = angle(c, bufferCoordinates[(i + 1) % bufferCoordinates.length]);
                        if (anglesApproximatelyEqual(expectedAngle, angle, tolerance))
                        {
                            //System.out.println("Updating startIndex to " + i + " (forward match)");
                            startIndex = i;
                        }
                        angle =
                                angle(c, bufferCoordinates[(i + bufferCoordinates.length - 1)
                                        % bufferCoordinates.length]);
                        if (anglesApproximatelyEqual(expectedAngle, angle, tolerance))
                        {
                            //System.out.println("Updating startIndex to " + i + " (backward match)");
                            startIndex = i;
                        }
                    }
                }
                if (endIndex != i)
                {
                    Coordinate c = bufferCoordinates[i];
                    if (endCoordinate.distance(c) < tooClose)
                    {
                        double angle = angle(bufferCoordinates[(i + 1) % bufferCoordinates.length], c);
                        if (anglesApproximatelyEqual(endExpectedAngle, angle, tolerance))
                        {
                            //System.out.println("Updating endIndex to " + i + " (backward match)");
                            endIndex = i;
                        }
                        angle =
                                angle(bufferCoordinates[(i + bufferCoordinates.length - 1) % bufferCoordinates.length],
                                        c);
                        if (anglesApproximatelyEqual(endExpectedAngle, angle, tolerance))
                        {
                            //System.out.println("Updating endIndex to " + i + " (forward match)");
                            endIndex = i;
                        }
                    }

                }
            }
            if (startIndex >= boundary)
            {
                if (endIndex < boundary)
                {
                    throw new NetworkException("startIndex and endIndex are not at same side of boundary");
                }
                // Discard the part before boundary
                Coordinate[] newSet = new Coordinate[bufferCoordinates.length - boundary - 1];
                // Copy all except the last (which is the wrap around of the first)
                for (int i = boundary; i < bufferCoordinates.length - 1; i++)
                {
                    newSet[i - boundary] = bufferCoordinates[i];
                }
                bufferCoordinates = newSet;
                // Update startIndex and endIndex
                startIndex -= boundary;
                endIndex -= boundary;
            }
            else
            {
                if (endIndex >= boundary)
                {
                    throw new NetworkException("startIndex and endIndex are not at same side of boundary");
                }
                // Discard the part starting at boundary
                Coordinate[] newSet = new Coordinate[boundary - 1];
                for (int i = 0; i < newSet.length; i++)
                {
                    newSet[i] = bufferCoordinates[i];
                }
                bufferCoordinates = newSet;
                // The values of startIndex and endIndex are correct
            }
            //printCoordinates("selection ", bufferCoordinates);
            //System.out.println("startIndex " + startIndex + ", endIndex " + endIndex);
        }
        // Figure out which part of the buffer we need and in which direction.
        // The initial direction should be approximately parallel to the initial direction of the reference line
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
            System.out.println("ringDetected is " + ringDetected + " offset is " + offset + " startCoordinate is "
                    + startCoordinate);
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
        if (0 == size)
        {
            size += bufferCoordinates.length;
        }
        size += 1; // add room for the final Coordinate
        int index = initialIndex;
        int step = forward ? 1 : -1;
        Coordinate[] resultCoordinates = new Coordinate[size];
        for (int resultIndex = 0; resultIndex < size; resultIndex++)
        {
            resultCoordinates[resultIndex] = bufferCoordinates[index];
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
        // printCoordinates("result           ", resultCoordinates);
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
        // printCoordinates("referenceLine    ", referenceLine);
        Geometry offsetLineAtStart = offsetGeometry(referenceLine, offsetAtStart);
        // System.out.println("offsetAtStart  " + offsetAtStart);
        // printCoordinates("offsetLineAtStart", offsetLineAtStart);
        if (offsetAtStart == offsetAtEnd)
        {
            return offsetLineAtStart;
        }
        Geometry offsetLineAtEnd = offsetGeometry(referenceLine, offsetAtEnd);
        // System.out.println("offsetAtEnd    " + offsetAtEnd);
        // printCoordinates("offsetLineAtEnd  ", offsetLineAtEnd);
        LengthIndexedLine first = new LengthIndexedLine(offsetLineAtStart);
        double firstLength = offsetLineAtStart.getLength();
        LengthIndexedLine second = new LengthIndexedLine(offsetLineAtEnd);
        double secondLength = offsetLineAtEnd.getLength();
        ArrayList<Coordinate> out = new ArrayList<Coordinate>();
        Coordinate[] firstCoordinates = offsetLineAtStart.getCoordinates();
        Coordinate[] secondCoordinates = offsetLineAtEnd.getCoordinates();
        int firstIndex = 0;
        int secondIndex = 0;
        Coordinate prevCoordinate = null;
        final double tooClose = 0.05; // cm
        while (firstIndex < firstCoordinates.length && secondIndex < secondCoordinates.length)
        {
            double firstRatio =
                    firstIndex < firstCoordinates.length ? first.indexOf(firstCoordinates[firstIndex]) / firstLength
                            : Double.MAX_VALUE;
            double secondRatio =
                    secondIndex < secondCoordinates.length ? second.indexOf(secondCoordinates[secondIndex])
                            / secondLength : Double.MAX_VALUE;
            double ratio;
            if (firstRatio < secondRatio)
            {
                ratio = firstRatio;
                firstIndex++;
            }
            else
            {
                ratio = secondRatio;
                secondIndex++;
            }
            Coordinate firstCoordinate = first.extractPoint(ratio * firstLength);
            Coordinate secondCoordinate = second.extractPoint(ratio * secondLength);
            Coordinate resultCoordinate =
                    new Coordinate((1 - ratio) * firstCoordinate.x + ratio * secondCoordinate.x, (1 - ratio)
                            * firstCoordinate.y + ratio * secondCoordinate.y);
            // System.out.println(String.format(Locale.US,
            // "ratio: %7.5f, first  %8.3f,%8.3f, second: %8.3f,%8.3f -> %8.3f,%8.3f", ratio, firstCoordinate.x,
            // firstCoordinate.y, secondCoordinate.x, secondCoordinate.y, resultCoordinate.x, resultCoordinate.y));
            if (null == prevCoordinate || resultCoordinate.distance(prevCoordinate) > tooClose)
            {
                out.add(resultCoordinate);
                prevCoordinate = resultCoordinate;
            }
        }
        Coordinate[] resultCoordinates = new Coordinate[out.size()];
        for (int index = 0; index < out.size(); index++)
        {
            resultCoordinates[index] = out.get(index);
        }
        // printCoordinates("resultCoordinates", resultCoordinates);
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
        // printCoordinates("Link design line:", referenceCoordinates);
        Geometry referenceGeometry = factory.createLineString(referenceCoordinates);
        Geometry resultLine;
        resultLine =
                offsetLine(referenceGeometry, this.designLineOffsetAtBegin.getSI(), this.designLineOffsetAtEnd.getSI());
        //printCoordinates("Lane design line:", resultLine);
        this.crossSectionDesignLine = factory.createLineString(resultLine.getCoordinates());
        Coordinate[] rightBoundary =
                offsetLine(this.crossSectionDesignLine, -this.beginWidth.getSI() / 2, -this.endWidth.getSI() / 2)
                        .getCoordinates();
        // printCoordinates("Right boundary:  ", rightBoundary);
        Coordinate[] leftBoundary =
                offsetLine(this.crossSectionDesignLine, this.beginWidth.getSI() / 2, this.endWidth.getSI() / 2)
                        .getCoordinates();
        // printCoordinates("Left boundary:   ", leftBoundary);
        int size = rightBoundary.length + leftBoundary.length + 1;
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
        result[resultIndex] = rightBoundary[0]; // close the contour
        // printCoordinates("Lane contour:    ", result);
        return factory.createLineString(result);
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
        printCoordinates(prefix, geometry.getCoordinates(), fromIndex, toIndex);
    }

    /**
     * Print the coordinates on the console.
     * @param prefix String; text to put before the output
     * @param geometry Geometry; the coordinates to print
     */
    public static void printCoordinates(final String prefix, final Geometry geometry)
    {
        printCoordinates(prefix, geometry.getCoordinates());
    }

    /**
     * Print the coordinates on the console.
     * @param prefix String; text to put before the output
     * @param coordinates Coordinate[]; the coordinates to print
     */
    public static void printCoordinates(final String prefix, final Coordinate[] coordinates)
    {
        printCoordinates(prefix + "(" + coordinates.length + " pts)", coordinates, 0, coordinates.length);
    }

    /**
     * Print the coordinates on the console.
     * @param prefix String; text to put before the output
     * @param coordinates Coordinate[]; the coordinates to print
     * @param fromIndex int; index of the first coordinate to print
     * @param toIndex int; one higher than the index of the last coordinate to print
     */
    public static void printCoordinates(final String prefix, final Coordinate[] coordinates, final int fromIndex,
            final int toIndex)
    {
        System.out.print(prefix);
        String operator = "M"; // Move absolute
        for (int i = fromIndex; i < toIndex; i++)
        {
            System.out.print(String
                    .format(Locale.US, "%s %8.3f,%8.3f   ", operator, coordinates[i].x, coordinates[i].y));
            operator = "L"; // LineTo Absolute
        }
        System.out.println("");
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
