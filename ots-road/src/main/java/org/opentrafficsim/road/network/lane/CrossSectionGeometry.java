package org.opentrafficsim.road.network.lane;

import java.util.LinkedHashMap;
import java.util.Map;

import org.djutils.draw.curve.OffsetCurve2d;
import org.djutils.draw.curve.OffsetFlattener2d;
import org.djutils.draw.function.ContinuousPiecewiseLinearFunction;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.DirectedPoint2d;
import org.djutils.exceptions.Throw;
import org.djutils.math.functions.MathFunction.TupleSt;
import org.opentrafficsim.base.geometry.OtsLine2d;
import org.opentrafficsim.base.geometry.OtsShape;

/**
 * Cross-section element geometry. A static method {@code of(...)} is available to generate geometry based on a design line and
 * information on offset and width.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param centerLine center line
 * @param absoluteContour contour
 * @param offset offset
 * @param width width
 */
public record CrossSectionGeometry(OtsLine2d centerLine, Polygon2d absoluteContour, ContinuousPiecewiseLinearFunction offset,
        ContinuousPiecewiseLinearFunction width)
{

    /**
     * Constructor.
     * @throws NullPointerException when centerLine, contour or slices is {@code null}
     */
    public CrossSectionGeometry

    {
        Throw.whenNull(centerLine, "centerLine");
        Throw.whenNull(absoluteContour, "absoluteContour");
        Throw.whenNull(offset, "offset");
        Throw.whenNull(width, "width");
    }

    /**
     * Create geometry based on design line, flattener, offset and width information.
     * @param designLine design line relative to which offsets are defined
     * @param flattener flattener to flatten center line and contour
     * @param offset offset information
     * @param width offset information
     * @return geometry for cross-section element
     */
    public static CrossSectionGeometry of(final OffsetCurve2d designLine, final OffsetFlattener2d flattener,
            final ContinuousPiecewiseLinearFunction offset, final ContinuousPiecewiseLinearFunction width)
    {
        PolyLine2d line = designLine.toPolyLine(flattener, offset);
        Map<Double, Double> leftMap = new LinkedHashMap<>();
        Map<Double, Double> rightMap = new LinkedHashMap<>();
        for (TupleSt st : offset)
        {
            leftMap.put(st.s(), st.t() + .5 * width.get(st.s()));
            rightMap.put(st.s(), st.t() - .5 * width.get(st.s()));
        }
        for (TupleSt st : width)
        {
            leftMap.put(st.s(), offset.get(st.s()) + .5 * width.get(st.s()));
            rightMap.put(st.s(), offset.get(st.s()) - .5 * width.get(st.s()));
        }
        PolyLine2d left = designLine.toPolyLine(flattener, new ContinuousPiecewiseLinearFunction(leftMap));
        PolyLine2d right = designLine.toPolyLine(flattener, new ContinuousPiecewiseLinearFunction(rightMap));
        Polygon2d cont = LaneGeometryUtil.getContour(left, right);
        return new CrossSectionGeometry(new OtsLine2d(line), cont, offset, width);
    }

    /**
     * Returns the location.
     * @return returns the location of this geometry
     */
    public DirectedPoint2d getLocation()
    {
        return centerLine().getLocationFractionExtended(0.5);
    }

    /**
     * Returns the relative contour.
     * @return relative contour
     */
    public Polygon2d getRelativeContour()
    {
        return new Polygon2d(OtsShape.toRelativeTransform(getLocation()).transform(absoluteContour().iterator()));
    }

}
