package org.opentrafficsim.base.geometry;

import java.util.List;

import org.djunits.value.vdouble.scalar.Direction;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.line.Ray2d;
import org.djutils.draw.point.DirectedPoint2d;
import org.djutils.draw.point.Point2d;
import org.djutils.exceptions.Throw;

/**
 * Adds a direction at the start and end point relative to its super class {@code OtsLine2d}, as the first and last segment may
 * not have the same direction as a theoretical line the segments are a numerical approach of. These directions are used in a
 * few methods which alter the result from the super class. The most notable addition of this class is
 * {@code directionalOffsetLine}.
 */
public class DirectionalPolyLine extends OtsLine2d
{
    /** */
    private static final long serialVersionUID = 20241130L;

    /** Start direction. */
    private final Direction startDirection;

    /** End direction. */
    private final Direction endDirection;

    /**
     * Constructor.
     * @param line base line
     * @param startDirection start direction
     * @param endDirection end direction
     */
    public DirectionalPolyLine(final PolyLine2d line, final Direction startDirection, final Direction endDirection)
    {
        super(line);
        Throw.whenNull(startDirection, "startDirection");
        Throw.whenNull(endDirection, "endDirection");
        this.startDirection = startDirection;
        this.endDirection = endDirection;
    }

    /**
     * Returns line at a fixed offset, adhering to end-point directions.
     * @param offset offset
     * @return offset line
     */
    public DirectionalPolyLine directionalOffsetLine(final double offset)
    {
        PolyLine2d offsetLine = offsetLine(offset);
        DirectedPoint2d start = new DirectedPoint2d(getFirst().x, getFirst().y, this.startDirection.si);
        DirectedPoint2d end = new DirectedPoint2d(getLast().x, getLast().y, this.endDirection.si);
        List<Point2d> points = offsetLine.getPointList();
        points.set(0, OtsGeometryUtil.offsetPoint(start, offset));
        points.set(points.size() - 1, OtsGeometryUtil.offsetPoint(end, offset));
        return new DirectionalPolyLine(new PolyLine2d(points), this.startDirection, this.endDirection);
    }

    /**
     * Returns line at a fixed offset, adhering to end-point directions.
     * @param startOffset offset at start
     * @param endOffset offset at end
     * @return offset line
     */
    public DirectionalPolyLine directionalOffsetLine(final double startOffset, final double endOffset)
    {
        PolyLine2d start = directionalOffsetLine(startOffset);
        PolyLine2d end = directionalOffsetLine(endOffset);
        return new DirectionalPolyLine(start.transitionLine(end, (f) -> f), this.startDirection, this.endDirection);
    }

    @Override
    public DirectionalPolyLine extractFractional(final double start, final double end)
    {
        return new DirectionalPolyLine(super.extractFractional(start, end),
                Direction.instantiateSI(getLocationFraction(start).phi), Direction.instantiateSI(getLocationFraction(end).phi));
    }

    @Override
    public Ray2d getLocationFraction(final double fraction)
    {
        Ray2d ray = super.getLocationFraction(fraction);
        if (fraction == 0.0)
        {
            ray = new Ray2d(ray, this.startDirection.si);
        }
        else if (fraction == 1.0)
        {
            ray = new Ray2d(ray, this.endDirection.si);
        }
        return ray;
    }

    /**
     * Fractional projection applied with the internal start and end direction.
     * @param x x-coordinate
     * @param y y-coordinate
     * @param fallback fallback method
     * @return fraction along line which it the projection of the given coordinate
     */
    public double projectFractional(final double x, final double y, final FractionalFallback fallback)
    {
        return projectFractional(this.startDirection, this.endDirection, x, y, fallback);
    }

    /**
     * Returns the start direction.
     * @return start direction
     */
    public Direction getStartDirection()
    {
        return this.startDirection;
    }

    /**
     * Returns the end direction.
     * @return end direction
     */
    public Direction getEndDirection()
    {
        return this.endDirection;
    }
}
