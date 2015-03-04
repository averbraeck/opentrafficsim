package org.opentrafficsim.core.network.lane;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.media.j3d.Bounds;
import javax.vecmath.Point3d;

import nl.tudelft.simulation.dsol.animation.LocatableInterface;
import nl.tudelft.simulation.language.d3.BoundingBox;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.geotools.LinearGeometry;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

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
     * @throws NetworkException when creation of the geometry fails
     */
    public CrossSectionElement(final CrossSectionLink<?, ?> parentLink,
            final DoubleScalar.Rel<LengthUnit> lateralOffsetAtBegin,
            final DoubleScalar.Rel<LengthUnit> lateralOffsetAtEnd, final DoubleScalar.Rel<LengthUnit> beginWidth,
            final DoubleScalar.Rel<LengthUnit> endWidth) throws NetworkException
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

    /** Precision of buffer operations. */
    private final int quadrantSegments = 8;

    // FIXME put in utility class. Also exists in XmlNetworkLaneParser.
    /**
     * normalize an angle between 0 and 2 * PI.
     * @param angle original angle.
     * @return angle between 0 and 2 * PI.
     */
    private double norm(final double angle)
    {
        double normalized = angle % (2 * Math.PI);
        if (normalized < 0.0)
        {
            normalized += 2 * Math.PI;
        }
        return normalized;
    }

    /**
     * Generate a Geometry that has a fixed offset from a reference Geometry.
     * @param referenceLine Geometry; the reference line
     * @param offset double; offset distance from the reference line; positive is Left, negative is Right
     * @return Geometry; the Geometry of a line that has the specified offset from the reference line
     * @throws NetworkException on failure
     */
    @SuppressWarnings("checkstyle:methodlength")
    private Geometry offsetGeometry(final Geometry referenceLine, final double offset) throws NetworkException
    {
        Coordinate[] referenceCoordinates = referenceLine.getCoordinates();
        // printCoordinates("reference", referenceCoordinates);
        double bufferOffset = Math.abs(offset);
        if (bufferOffset == 0)
        {
            // return a copy of the reference line
            GeometryFactory factory = new GeometryFactory();
            Geometry result = factory.createLineString(referenceCoordinates);
            return result;
        }
        Coordinate[] bufferCoordinates =
                referenceLine.buffer(bufferOffset, this.quadrantSegments, BufferParameters.CAP_FLAT).getCoordinates();
        // find the coordinate indices closest to the start point and end point, at a distance of approximately the
        // offset
        Coordinate sC = referenceCoordinates[0];
        Coordinate sC1 = referenceCoordinates[1];
        Coordinate eC = referenceCoordinates[referenceCoordinates.length - 1];
        Coordinate eC1 = referenceCoordinates[referenceCoordinates.length - 2];
        Set<Integer> startIndexSet = new HashSet<>();
        Set<Coordinate> startSet = new HashSet<Coordinate>();
        Set<Integer> endIndexSet = new HashSet<>();
        Set<Coordinate> endSet = new HashSet<Coordinate>();
        final double precision = 0.000001; //
        for (int i = 0; i < bufferCoordinates.length; i++) // Note: the last coordinate = the first coordinate
        {
            Coordinate c = bufferCoordinates[i];
            if (Math.abs(c.distance(sC) - bufferOffset) < bufferOffset * precision && !startSet.contains(c))
            {
                startIndexSet.add(i);
                startSet.add(c);
            }
            if (Math.abs(c.distance(eC) - bufferOffset) < bufferOffset * precision && !endSet.contains(c))
            {
                endIndexSet.add(i);
                endSet.add(c);
            }
        }
        if (startIndexSet.size() != 2)
        {
            throw new NetworkException("offsetGeometry: startIndexSet.size() = " + startIndexSet.size());
        }
        if (endIndexSet.size() != 2)
        {
            throw new NetworkException("offsetGeometry: endIndexSet.size() = " + endIndexSet.size());
        }

        // which point(s) are in the right direction of the start / end?
        int startIndex = -1;
        int endIndex = -1;
        double expectedStartAngle = norm(Math.atan2(sC1.y - sC.y, sC1.x - sC.x) + Math.signum(offset) * Math.PI / 2.0);
        double expectedEndAngle = norm(Math.atan2(eC.y - eC1.y, eC.x - eC1.x) + Math.signum(offset) * Math.PI / 2.0);
        for (int ic : startIndexSet)
        {
            if (Math.abs(norm(Math.atan2(bufferCoordinates[ic].y - sC.y, bufferCoordinates[ic].x - sC.x)
                    - expectedStartAngle)) < Math.PI / 4.0
                    || Math.abs(norm(Math.atan2(bufferCoordinates[ic].y - sC.y, bufferCoordinates[ic].x - sC.x)
                            - expectedStartAngle)
                            - 2.0 * Math.PI) < Math.PI / 4.0)
            {
                startIndex = ic;
            }
        }
        for (int ic : endIndexSet)
        {
            if (Math.abs(norm(Math.atan2(bufferCoordinates[ic].y - eC.y, bufferCoordinates[ic].x - eC.x)
                    - expectedEndAngle)) < Math.PI / 4.0
                    || Math.abs(norm(Math.atan2(bufferCoordinates[ic].y - eC.y, bufferCoordinates[ic].x - eC.x)
                            - expectedEndAngle)
                            - 2.0 * Math.PI) < Math.PI / 4.0)
            {
                endIndex = ic;
            }
        }
        if (startIndex == -1 || endIndex == -1)
        {
            throw new NetworkException("offsetGeometry: could not find startIndex or endIndex");
        }
        startIndexSet.remove(startIndex);
        endIndexSet.remove(endIndex);

        // Make two lists, one in each direction; start at "start" and end at "end".
        List<Coordinate> coordinateList1 = new ArrayList<>();
        List<Coordinate> coordinateList2 = new ArrayList<>();
        boolean use1 = true;
        boolean use2 = true;

        int i = startIndex;
        while (i != endIndex)
        {
            if (!coordinateList1.contains(bufferCoordinates[i]))
            {
                coordinateList1.add(bufferCoordinates[i]);
            }
            i = (i + 1) % bufferCoordinates.length;
            if (startIndexSet.contains(i) || endIndexSet.contains(i))
            {
                use1 = false;
            }
        }
        if (!coordinateList1.contains(bufferCoordinates[endIndex]))
        {
            coordinateList1.add(bufferCoordinates[endIndex]);
        }

        i = startIndex;
        while (i != endIndex)
        {
            if (!coordinateList2.contains(bufferCoordinates[i]))
            {
                coordinateList2.add(bufferCoordinates[i]);
            }
            i = (i == 0) ? bufferCoordinates.length - 1 : i - 1;
            if (startIndexSet.contains(i) || endIndexSet.contains(i))
            {
                use2 = false;
            }
        }
        if (!coordinateList2.contains(bufferCoordinates[endIndex]))
        {
            coordinateList2.add(bufferCoordinates[endIndex]);
        }

        if (!use1 && !use2)
        {
            throw new NetworkException("offsetGeometry: could not find path from start to end for offset");
        }
        if (use1 && use2)
        {
            throw new NetworkException("offsetGeometry: Both paths from start to end for offset were found to be ok");
        }
        Coordinate[] coordinates;
        if (use1)
        {
            coordinates = new Coordinate[coordinateList1.size()];
            coordinateList1.toArray(coordinates);
        }
        else
        {
            coordinates = new Coordinate[coordinateList2.size()];
            coordinateList2.toArray(coordinates);
        }
        GeometryFactory factory = new GeometryFactory();
        Geometry result = factory.createLineString(coordinates);
        return result;
    }

    /**
     * Create the Geometry of a line at offset from a reference line. The offset may change linearly from its initial
     * value at the start of the reference line to its final offset value at the end of the reference line.
     * @param referenceLine Geometry; the Geometry of the reference line
     * @param offsetAtStart double; offset at the start of the reference line (positive value is Left, negative value is
     *            Right)
     * @param offsetAtEnd double; offset at the end of the reference line (positive value is Left, negative value is
     *            Right)
     * @return Geometry; the Geometry of the line at linearly changing offset of the reference line
     * @throws NetworkException when this method fails to create the offset line
     */
    private Geometry offsetLine(final Geometry referenceLine, final double offsetAtStart, final double offsetAtEnd)
            throws NetworkException
    {
        // printCoordinates("referenceLine    ", referenceLine);
        Geometry offsetLineAtStart = offsetGeometry(referenceLine, offsetAtStart);
        // System.out.println("offsetAtStart  " + offsetAtStart);
        // printCoordinates("offsetLineAtStart", offsetLineAtStart);
        if (offsetAtStart == offsetAtEnd)
        {
            return offsetLineAtStart; // offset does not change
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
        final double tooClose = 0.05; // 5 cm
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
     * @throws NetworkException when construction of the geometry fails (which should never happen)
     */
    private Geometry constructGeometry() throws NetworkException
    {
        GeometryFactory factory = new GeometryFactory();
        LinearGeometry parentGeometry = this.parentLink.getGeometry();
        if (null == parentGeometry)
        {
            return null; // If the Link does not have a Geometry; this CrossSectionElement can't have one either
        }
        Coordinate[] referenceCoordinates = parentGeometry.getLineString().getCoordinates();
        if (referenceCoordinates.length < 2)
        {
            throw new NetworkException("Parent Link has bad Geometry");
        }
        // printCoordinates("Link design line:", referenceCoordinates);
        Geometry referenceGeometry = factory.createLineString(referenceCoordinates);
        Geometry resultLine =
                offsetLine(referenceGeometry, this.designLineOffsetAtBegin.getSI(), this.designLineOffsetAtEnd.getSI());
        // printCoordinates("Lane design line:", resultLine);
        this.crossSectionDesignLine = factory.createLineString(resultLine.getCoordinates());
        Coordinate[] rightBoundary =
                offsetLine(this.crossSectionDesignLine, -this.beginWidth.getSI() / 2, -this.endWidth.getSI() / 2)
                        .getCoordinates();
        // printCoordinates("Right boundary:  ", rightBoundary);
        Coordinate[] leftBoundary =
                offsetLine(this.crossSectionDesignLine, this.beginWidth.getSI() / 2, this.endWidth.getSI() / 2)
                        .getCoordinates();
        // printCoordinates("Left boundary:   ", leftBoundary);
        Coordinate[] result = new Coordinate[rightBoundary.length + leftBoundary.length + 1];
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
     * Retrieve the lateral offset from the Link design line at the specified longitudinal position.
     * @param fractionalPosition double; fractional longitudinal position on this Lane
     * @return DoubleScalar.Rel&lt;LengthUnit&gt; the lateralCenterPosition at the specified longitudinal position
     */
    public final DoubleScalar.Rel<LengthUnit> getLateralCenterPosition(final double fractionalPosition)
    {
        return DoubleScalar.interpolate(this.designLineOffsetAtBegin, this.designLineOffsetAtEnd, fractionalPosition)
                .immutable();
    }

    /**
     * Retrieve the lateral offset from the Link design line at the specified longitudinal position.
     * @param longitudinalPosition DoubleScalar.Rel&lt;LengthUnit&gt;; the longitudinal position on this Lane
     * @return DoubleScalar.Rel&lt;LengthUnit&gt; the lateralCenterPosition at the specified longitudinal position
     */
    public final DoubleScalar<LengthUnit> getLateralCenterPosition(
            final DoubleScalar.Rel<LengthUnit> longitudinalPosition)
    {
        return getLateralCenterPosition(longitudinalPosition.getSI() / getLength().getSI());
    }

    /**
     * Return the width of this CrossSectionElement at a specified longitudinal position.
     * @param longitudinalPosition DoubleScalar&lt;LengthUnit&gt;; the longitudinal position
     * @return DoubleScalar.Rel&lt;LengthUnit&gt;; the width of this CrossSectionElement at the specified longitudinal
     *         position.
     */
    public final DoubleScalar.Rel<LengthUnit> getWidth(final DoubleScalar.Rel<LengthUnit> longitudinalPosition)
    {
        return getWidth(longitudinalPosition.getSI() / getLength().getSI());
    }

    /**
     * Return the width of this CrossSectionElement at a specified fractional longitudinal position.
     * @param fractionalPosition double; the fractional longitudinal position
     * @return DoubleScalar.Rel&lt;LengthUnit&gt;; the width of this CrossSectionElement at the specified fractional
     *         longitudinal position.
     */
    public final DoubleScalar.Rel<LengthUnit> getWidth(final double fractionalPosition)
    {
        return DoubleScalar.interpolate(this.beginWidth, this.endWidth, fractionalPosition).immutable();
    }

    /** @return the z-value to determine "stacking" for animation. */
    protected abstract double getZ();
    
    /** {@inheritDoc} */
    @Override
    public final DirectedPoint getLocation() throws RemoteException
    {
        Envelope e = this.contour.getEnvelopeInternal(); // cached, so not expensive
        return new DirectedPoint(0.5 * (e.getMaxX() - e.getMinX()), 0.5 * (e.getMaxY() - e.getMinY()), getZ());
    }

    /** {@inheritDoc} */
    @Override
    public final Bounds getBounds() throws RemoteException
    {
        Envelope e = this.contour.getEnvelopeInternal(); // cached, so not expensive
        double dx = 0.5 * (e.getMaxX() - e.getMinX());
        double dy = 0.5 * (e.getMaxY() - e.getMinY());
        return new BoundingBox(new Point3d(e.getMinX() - dx, e.getMinY() - dy, 0.0), new Point3d(e.getMinX() + dx,
                e.getMinY() + dy, getZ()));
    }

    /**
     * @return the contour of this CrossSectionElement. <br>
     *         <b>Do not modify the returned object or chaos will ensue.</b>
     */
    public final Geometry getContour()
    {
        return this.contour;
    }

    /**
     * Retrieve the center line or design line of this CrossSectionElement. <br>
     * <b>Do not modify the returned object or chaos will ensue.</b>
     * @return LineString; the design line of this CrossSectionElement (which equals the center line of this
     *         CrossSectionElement)
     */
    public final LineString getCenterLine()
    {
        return this.crossSectionDesignLine;
    }

    /**
     * Print one Coordinate on the console.
     * @param prefix String; text to put before the output
     * @param coordinate Coordinate; the coordinate to print
     */
    public static void printCoordinate(final String prefix, final Coordinate coordinate)
    {
        System.out.print(String.format(Locale.US, "%s %8.3f,%8.3f   ", prefix, coordinate.x, coordinate.y));
    }

    /**
     * Print coordinates of a Geometry on the console.
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
     * Print coordinates of a Geometry on the console.
     * @param prefix String; text to put before the output
     * @param geometry Geometry; the coordinates to print
     */
    public static void printCoordinates(final String prefix, final Geometry geometry)
    {
        printCoordinates(prefix, geometry.getCoordinates());
    }

    /**
     * Print an array of coordinates on the console.
     * @param prefix String; text to put before the coordinates
     * @param coordinates Coordinate[]; the coordinates to print
     */
    public static void printCoordinates(final String prefix, final Coordinate[] coordinates)
    {
        printCoordinates(prefix + "(" + coordinates.length + " pts)", coordinates, 0, coordinates.length);
    }

    /**
     * Print part of an array of coordinates on the console.
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
            printCoordinate(operator, coordinates[i]);
            operator = "L"; // LineTo Absolute
        }
        System.out.println("");
    }

    /**
     * Return the length of this CrossSectionElement as measured along the design line (which equals the center line).
     * @return DoubleScalar.Rel&lt;LengthUnit&gt;; the length of this CrossSectionElement
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
     * Return the lateral offset from the design line of the parent Link of the Left or Right boundary of this
     * CrossSectionElement at the specified fractional longitudinal position.
     * @param lateralDirection LateralDirectionality; LEFT, or RIGHT
     * @param fractionalLongitudinalPosition double; ranges from 0.0 (begin of parentLink) to 1.0 (end of parentLink)
     * @return DoubleScalar.Rel&lt;LengthUnit&gt;
     */
    public final DoubleScalar.Rel<LengthUnit> getLateralBoundaryPosition(final LateralDirectionality lateralDirection,
            final double fractionalLongitudinalPosition)
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
                return DoubleScalar.minus(designLineOffset, halfWidth).immutable();
            case RIGHT:
                return DoubleScalar.plus(designLineOffset, halfWidth).immutable();
            default:
                throw new Error("Bad switch on LateralDirectionality " + lateralDirection);
        }
    }

    /**
     * Return the lateral offset from the design line of the parent Link of the Left or Right boundary of this
     * CrossSectionElement at the specified longitudinal position.
     * @param lateralDirection LateralDirectionality; LEFT, or RIGHT
     * @param longitudinalPosition DoubleScalar.Rel&lt;LengthUnit&gt;; the position along the length of this
     *            CrossSectionElement
     * @return DoubleScalar.Rel&lt;LengthUnit&gt;
     */
    public final DoubleScalar.Rel<LengthUnit> getLateralBoundaryPosition(final LateralDirectionality lateralDirection,
            final DoubleScalar.Rel<LengthUnit> longitudinalPosition)
    {
        return getLateralBoundaryPosition(lateralDirection, longitudinalPosition.getSI() / getLength().getSI());
    }

}
