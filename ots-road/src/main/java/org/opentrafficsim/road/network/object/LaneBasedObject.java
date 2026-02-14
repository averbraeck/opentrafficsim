package org.opentrafficsim.road.network.object;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.point.DirectedPoint2d;
import org.djutils.draw.point.Point2d;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.object.LocatedObject;
import org.opentrafficsim.road.network.Lane;

/**
 * Objects that can be encountered on a Lane like conflict areas, GTUs, traffic lights, stop lines, etc.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface LaneBasedObject extends LocatedObject
{

    /**
     * Returns the lane.
     * @return lane
     */
    Lane getLane();

    /**
     * Returns the longitudinal position.
     * @return position (between 0.0 and the length of the lane)
     */
    Length getLongitudinalPosition();

    /**
     * Returns the length of the object. The default value is zero.
     * @return length of the object
     */
    default Length getLength()
    {
        return Length.ZERO;
    }

    @Override
    DirectedPoint2d getLocation();

    /**
     * Returns the line that represent the location of this object on the lane.
     * @return the line that represent the location of this object on the lane
     */
    default PolyLine2d getLine()
    {
        return makeLine(getLane(), getLongitudinalPosition());
    }

    /**
     * Returns the simulator.
     * @return simulator
     */
    default OtsSimulatorInterface getSimulator()
    {
        return getLane().getLink().getSimulator();
    }

    /**
     * Make a geometry perpendicular to the center line of the lane with a length 90% of the width of the lane.
     * @param lane the lane for which to make a perpendicular geometry
     * @param longitudinalPosition the position on the lane
     * @return lateral line at position
     */
    static PolyLine2d makeLine(final Lane lane, final Length longitudinalPosition)
    {
        return makeLine(lane, longitudinalPosition, 0.9);
    }

    /**
     * Make a geometry perpendicular to the center line of the lane with a length of given fraction of the width of the lane.
     * @param lane the lane for which to make a perpendicular geometry
     * @param longitudinalPosition the position on the lane
     * @param relativeWidth lane width to use
     * @return lateral line at position
     */
    static PolyLine2d makeLine(final Lane lane, final Length longitudinalPosition, final double relativeWidth)
    {
        Throw.whenNull(lane, "lane is null");
        Throw.whenNull(longitudinalPosition, "position is null");
        Throw.whenNull(relativeWidth, "relatve width is null");
        double w50 = lane.getWidth(longitudinalPosition).si * 0.5 * relativeWidth;
        DirectedPoint2d c = lane.getCenterLine().getLocationExtended(longitudinalPosition);
        double a = c.getDirZ();
        Point2d p1 = new Point2d(c.x + w50 * Math.cos(a + Math.PI / 2), c.y - w50 * Math.sin(a + Math.PI / 2));
        Point2d p2 = new Point2d(c.x - w50 * Math.cos(a + Math.PI / 2), c.y + w50 * Math.sin(a + Math.PI / 2));
        return new PolyLine2d(0.0, p1, p2);
    }

}
