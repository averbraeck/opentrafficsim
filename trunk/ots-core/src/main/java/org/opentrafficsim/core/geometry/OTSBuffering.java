package org.opentrafficsim.core.geometry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opentrafficsim.core.network.NetworkException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.linearref.LengthIndexedLine;
import com.vividsolutions.jts.operation.buffer.BufferParameters;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-16 10:20:53 +0200 (Thu, 16 Jul 2015) $, @version $Revision: 1124 $, by $Author: pknoppers $,
 * initial version Jul 22, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public final class OTSBuffering
{
    /** Precision of buffer operations. */
    private static final int QUADRANTSEGMENTS = 16;

    /**
     * 
     */
    private OTSBuffering()
    {
        // cannot be instantiated.
    }

    /**
     * normalize an angle between 0 and 2 * PI.
     * @param angle original angle.
     * @return angle between 0 and 2 * PI.
     */
    private static double norm(final double angle)
    {
        double normalized = angle % (2 * Math.PI);
        if (normalized < 0.0)
        {
            normalized += 2 * Math.PI;
        }
        return normalized;
    }

    /**
     * @param c1 first coordinate
     * @param c2 second coordinate
     * @return the normalized angle of the line between c1 and c2
     */
    private static double angle(final Coordinate c1, final Coordinate c2)
    {
        return norm(Math.atan2(c2.y - c1.y, c2.x - c1.x));
    }

    /**
     * Generate a Geometry that has a fixed offset from a reference Geometry.
     * @param referenceLine Geometry; the reference line
     * @param offset double; offset distance from the reference line; positive is LEFT, negative is RIGHT
     * @return Geometry; the Geometry of a line that has the specified offset from the reference line
     * @throws OTSGeometryException on failure
     */
    @SuppressWarnings("checkstyle:methodlength")
    public static OTSLine3D offsetGeometry(final OTSLine3D referenceLine, final double offset) throws OTSGeometryException
    {
        Coordinate[] referenceCoordinates = referenceLine.getCoordinates();
        // printCoordinates("reference", referenceCoordinates);
        double bufferOffset = Math.abs(offset);
        final double precision = 0.00001;
        if (bufferOffset < precision) // if this is not added, and offset = 1E-16: CRASH
        {
            // return a copy of the reference line
            try
            {
                return new OTSLine3D(referenceCoordinates);
            }
            catch (NetworkException exception)
            {
                // System.err.println("CANNOT HAPPEN");
                throw new Error("Caught impossible exception while creating OTSLine3D: " + exception.getMessage());
            }
        }
        Geometry geometryLine = referenceLine.getLineString();
        Coordinate[] bufferCoordinates =
            geometryLine.buffer(bufferOffset, QUADRANTSEGMENTS, BufferParameters.CAP_FLAT).getCoordinates();
        // Z coordinates may be NaN at this point 
        
        // find the coordinate indices closest to the start point and end point, at a distance of approximately the
        // offset
        Coordinate sC0 = referenceCoordinates[0];
        Coordinate sC1 = referenceCoordinates[1];
        Coordinate eCm1 = referenceCoordinates[referenceCoordinates.length - 1];
        Coordinate eCm2 = referenceCoordinates[referenceCoordinates.length - 2];
        Set<Integer> startIndexSet = new HashSet<>();
        Set<Coordinate> startSet = new HashSet<Coordinate>();
        Set<Integer> endIndexSet = new HashSet<>();
        Set<Coordinate> endSet = new HashSet<Coordinate>();
        for (int i = 0; i < bufferCoordinates.length; i++) // Note: the last coordinate = the first coordinate
        {
            Coordinate c = bufferCoordinates[i];
            if (Math.abs(c.distance(sC0) - bufferOffset) < bufferOffset * precision && !startSet.contains(c))
            {
                startIndexSet.add(i);
                startSet.add(c);
            }
            if (Math.abs(c.distance(eCm1) - bufferOffset) < bufferOffset * precision && !endSet.contains(c))
            {
                endIndexSet.add(i);
                endSet.add(c);
            }
        }
        if (startIndexSet.size() != 2)
        {
            throw new OTSGeometryException("offsetGeometry: startIndexSet.size() = " + startIndexSet.size());
        }
        if (endIndexSet.size() != 2)
        {
            throw new OTSGeometryException("offsetGeometry: endIndexSet.size() = " + endIndexSet.size());
        }

        // which point(s) are in the right direction of the start / end?
        int startIndex = -1;
        int endIndex = -1;
        double expectedStartAngle = norm(angle(sC0, sC1) + Math.signum(offset) * Math.PI / 2.0);
        double expectedEndAngle = norm(angle(eCm2, eCm1) + Math.signum(offset) * Math.PI / 2.0);
        for (int ic : startIndexSet)
        {
            if (norm(expectedStartAngle - angle(sC0, bufferCoordinates[ic])) < Math.PI / 4.0
                || norm(angle(sC0, bufferCoordinates[ic]) - expectedStartAngle) < Math.PI / 4.0)
            {
                startIndex = ic;
            }
        }
        for (int ic : endIndexSet)
        {
            if (norm(expectedEndAngle - angle(eCm1, bufferCoordinates[ic])) < Math.PI / 4.0
                || norm(angle(eCm1, bufferCoordinates[ic]) - expectedEndAngle) < Math.PI / 4.0)
            {
                endIndex = ic;
            }
        }
        if (startIndex == -1 || endIndex == -1)
        {
            throw new OTSGeometryException("offsetGeometry: could not find startIndex or endIndex");
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
            throw new OTSGeometryException("offsetGeometry: could not find path from start to end for offset");
        }
        if (use1 && use2)
        {
            throw new OTSGeometryException("offsetGeometry: Both paths from start to end for offset were found to be ok");
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
        try
        {
            return new OTSLine3D(coordinates);
        }
        catch (NetworkException exception)
        {
            // System.err.println("CANNOT HAPPEN");
            throw new Error("Caught impossible exception in OTSLine3D: " + exception.getMessage());
        }
    }

    /**
     * Create the Geometry of a line at offset from a reference line. The offset may change linearly from its initial value at
     * the start of the reference line to its final offset value at the end of the reference line.
     * @param referenceLine Geometry; the Geometry of the reference line
     * @param offsetAtStart double; offset at the start of the reference line (positive value is Left, negative value is Right)
     * @param offsetAtEnd double; offset at the end of the reference line (positive value is Left, negative value is Right)
     * @return Geometry; the Geometry of the line at linearly changing offset of the reference line
     * @throws OTSGeometryException when this method fails to create the offset line
     */
    public static OTSLine3D offsetLine(final OTSLine3D referenceLine, final double offsetAtStart, final double offsetAtEnd)
        throws OTSGeometryException
    {
        OTSLine3D offsetLineAtStart = offsetGeometry(referenceLine, offsetAtStart);
        if (offsetAtStart == offsetAtEnd)
        {
            return offsetLineAtStart; // offset does not change
        }
        OTSLine3D offsetLineAtEnd = offsetGeometry(referenceLine, offsetAtEnd);
        Geometry startGeometry = offsetLineAtStart.getLineString();
        Geometry endGeometry = offsetLineAtEnd.getLineString();
        LengthIndexedLine first = new LengthIndexedLine(startGeometry);
        double firstLength = startGeometry.getLength();
        LengthIndexedLine second = new LengthIndexedLine(endGeometry);
        double secondLength = endGeometry.getLength();
        ArrayList<Coordinate> out = new ArrayList<Coordinate>();
        Coordinate[] firstCoordinates = startGeometry.getCoordinates();
        Coordinate[] secondCoordinates = endGeometry.getCoordinates();
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
                secondIndex < secondCoordinates.length ? second.indexOf(secondCoordinates[secondIndex]) / secondLength
                    : Double.MAX_VALUE;
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
                new Coordinate((1 - ratio) * firstCoordinate.x + ratio * secondCoordinate.x, (1 - ratio) * firstCoordinate.y
                    + ratio * secondCoordinate.y);
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
        try
        {
            return new OTSLine3D(resultCoordinates);
        }
        catch (NetworkException exception)
        {
            // System.err.println("CANNOT HAPPEN");
            throw new Error("Caught impossible exception in OTSLine3D " + exception.getMessage());
        }
    }
}
