package org.opentrafficsim.core.geometry;

import org.djunits.value.vdouble.scalar.Direction;
import org.djutils.draw.function.ContinuousPiecewiseLinearFunction;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.point.DirectedPoint2d;

/**
 * A continuous line defines a line in an exact manner, from which numerical polylines can be derived. The continuous definition
 * is useful to accurately connect different lines, e.g. based on the direction of the point where they meet. Moreover, this
 * direction may be accurately be determined by either of the lines. For example, an arc can be defined up to a certain angle.
 * Whatever the angle of the last line segment in a polyline for the arc may be, the continuous line contains the final
 * direction exactly. The continuous definition is also useful to define accurate offset lines, which depend on accurate
 * directions especially at the line end points.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface ContinuousLine
{

    /**
     * Start point.
     * @return start point
     */
    DirectedPoint2d getStartPoint();

    /**
     * End point.
     * @return end point
     */
    DirectedPoint2d getEndPoint();

    /**
     * Start direction.
     * @return start point
     */
    default Direction getStartDirection()
    {
        return Direction.ofSI(getStartPoint().dirZ);
    }

    /**
     * End direction.
     * @return end point
     */
    default Direction getEndDirection()
    {
        return Direction.ofSI(getEndPoint().dirZ);
    }

    /**
     * Start curvature.
     * @return start curvature
     */
    double getStartCurvature();

    /**
     * End curvature.
     * @return end curvature
     */
    double getEndCurvature();

    /**
     * Start radius.
     * @return start radius
     */
    default double getStartRadius()
    {
        return 1.0 / getStartCurvature();
    }

    /**
     * End radius.
     * @return end radius
     */
    default double getEndRadius()
    {
        return 1.0 / getEndCurvature();
    }

    /**
     * Flatten continuous line in to a polyline. Implementations should use the flattener when relevant and possible.
     * @param flattener flattener
     * @return flattened line
     */
    PolyLine2d flatten(Flattener flattener);

    /**
     * Flatten continuous line offset in to a polyline. Implementations should use the flattener when relevant and possible.
     * @param offset offset data
     * @param flattener flattener
     * @return flattened line
     */
    PolyLine2d flattenOffset(ContinuousPiecewiseLinearFunction offset, Flattener flattener);

    /**
     * Return the length of the line.
     * @return length of the line
     */
    double getLength();

}
