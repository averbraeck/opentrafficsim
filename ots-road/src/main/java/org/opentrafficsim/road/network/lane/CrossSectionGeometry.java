package org.opentrafficsim.road.network.lane;

import java.util.List;

import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.line.Polygon2d;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.geometry.OtsGeometryException;
import org.opentrafficsim.core.geometry.ContinuousLine;
import org.opentrafficsim.core.geometry.Flattener;
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
 * @param slices slices for offset and width
 */
@SuppressWarnings("javadoc") 
public record CrossSectionGeometry(OtsLine2d centerLine, Polygon2d contour, List<CrossSectionSlice> slices)
{

    /**
     * Constructor.
     * @throws NullPointerException when centerLine, contour or slices is {@code null}
     * @throws OtsGeometryException when slices is empty
     */
    public CrossSectionGeometry
    {
        Throw.whenNull(centerLine, "Center line may not be null.");
        Throw.whenNull(contour, "Contour may not be null.");
        Throw.whenNull(slices, "Cross section slices may not be null.");
        Throw.when(slices.isEmpty(), OtsGeometryException.class, "Need at least 1 cross section slice.");
    }
    
    /**
     * Create geometry based on design line, flattener, offset and width information.
     * @param designLine design line relative to which offsets are defined
     * @param flattener flattener to flatten center line and contour
     * @param offsetWidth offset and width information
     * @return geometry for cross-section element
     */
    public static CrossSectionGeometry of(final ContinuousLine designLine, final Flattener flattener,
            final List<CrossSectionSlice> offsetWidth)
    {
        PolyLine2d line = designLine.flattenOffset(LaneGeometryUtil.getCenterOffsets(designLine, offsetWidth), flattener);
        PolyLine2d left = designLine.flattenOffset(LaneGeometryUtil.getLeftEdgeOffsets(designLine, offsetWidth), flattener);
        PolyLine2d right = designLine.flattenOffset(LaneGeometryUtil.getRightEdgeOffsets(designLine, offsetWidth), flattener);
        Polygon2d cont = LaneGeometryUtil.getContour(left, right);
        return new CrossSectionGeometry(new OtsLine2d(line), cont, offsetWidth);
    }

}
