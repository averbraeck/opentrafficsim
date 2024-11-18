package org.opentrafficsim.road.network.lane;

import java.util.LinkedHashMap;
import java.util.Map;

import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.line.Polygon2d;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.geometry.ContinuousLine;
import org.opentrafficsim.core.geometry.ContinuousLine.ContinuousDoubleFunction;
import org.opentrafficsim.core.geometry.Flattener;
import org.opentrafficsim.core.geometry.FractionalLengthData;
import org.opentrafficsim.core.geometry.OtsLine2d;

/**
 * Cross-section element geometry. A static method {@code of(...)} is available to generate geometry based on a design line and
 * information on offset and width.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param centerLine center line
 * @param contour contour
 * @param offset offset
 * @param width width
 */
@SuppressWarnings("javadoc")
public record CrossSectionGeometry(OtsLine2d centerLine, Polygon2d contour, ContinuousDoubleFunction offset,
        ContinuousDoubleFunction width)
{

    /**
     * Constructor.
     * @throws NullPointerException when centerLine, contour or slices is {@code null}
     */
    public CrossSectionGeometry

    {
        Throw.whenNull(centerLine, "Center line may not be null.");
        Throw.whenNull(contour, "Contour may not be null.");
        Throw.whenNull(offset, "Offset may not be null.");
        Throw.whenNull(width, "Width may not be null.");
    }

    /**
     * Create geometry based on design line, flattener, offset and width information.
     * @param designLine design line relative to which offsets are defined
     * @param flattener flattener to flatten center line and contour
     * @param offset offset information
     * @param width offset information
     * @return geometry for cross-section element
     */
    public static CrossSectionGeometry of(final ContinuousLine designLine, final Flattener flattener,
            final ContinuousDoubleFunction offset, final ContinuousDoubleFunction width)
    {
        PolyLine2d line = designLine.flattenOffset(offset, flattener);
        Map<Double, Double> leftMap = new LinkedHashMap<>();
        Map<Double, Double> rightMap = new LinkedHashMap<>();
        for (double f : offset.getKnots())
        {
            leftMap.put(f, offset.apply(f) + .5 * width.apply(f));
            rightMap.put(f, offset.apply(f) - .5 * width.apply(f));
        }
        for (double f : width.getKnots())
        {
            leftMap.put(f, offset.apply(f) + .5 * width.apply(f));
            rightMap.put(f, offset.apply(f) - .5 * width.apply(f));
        }
        PolyLine2d left = designLine.flattenOffset(new FractionalLengthData(leftMap), flattener);
        PolyLine2d right = designLine.flattenOffset(new FractionalLengthData(rightMap), flattener);
        Polygon2d cont = LaneGeometryUtil.getContour(left, right);
        return new CrossSectionGeometry(new OtsLine2d(line), cont, offset, width);
    }

}
