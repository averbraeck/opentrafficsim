package org.opentrafficsim.road.network.lane.object;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.point.OrientedPoint2d;
import org.djutils.draw.point.Point2d;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.object.LocatedObject;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * Objects that can be encountered on a Lane like conflict areas, GTUs, traffic lights, stop lines, etc.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface LaneBasedObject extends LocatedObject
{
    
    /** @return The lane for which this is a sensor. */
    Lane getLane();

    /** @return the position (between 0.0 and the length of the Lane) of the sensor on the design line of the lane. */
    Length getLongitudinalPosition();

    /** {@inheritDoc} */
    @Override
    OrientedPoint2d getLocation();

    /**
     * Make a geometry perpendicular to the center line of the lane with a length 90% of the width of the lane.
     * @param lane Lane; the lane for which to make a perpendicular geometry
     * @param longitudinalPosition Length; the position on the lane
     * @return an OtsLine2d that describes the line
     */
    static PolyLine2d makeGeometry(final Lane lane, final Length longitudinalPosition)
    {
        return makeGeometry(lane, longitudinalPosition, 0.9);
    }

    /**
     * Make a geometry perpendicular to the center line of the lane with a length of given fraction of the width of the lane.
     * @param lane Lane; the lane for which to make a perpendicular geometry
     * @param longitudinalPosition Length; the position on the lane
     * @param relativeWidth double; lane width to use
     * @return an OtsLine2d that describes the line
     */
    static PolyLine2d makeGeometry(final Lane lane, final Length longitudinalPosition, final double relativeWidth)
    {
        Throw.whenNull(lane, "lane is null");
        Throw.whenNull(longitudinalPosition, "position is null");
        Throw.whenNull(relativeWidth, "relatve width is null");
        double w50 = lane.getWidth(longitudinalPosition).si * 0.5 * relativeWidth;
        OrientedPoint2d c = lane.getCenterLine().getLocationExtended(longitudinalPosition);
        double a = c.getDirZ();
        Point2d p1 = new Point2d(c.x + w50 * Math.cos(a + Math.PI / 2), c.y - w50 * Math.sin(a + Math.PI / 2));
        Point2d p2 = new Point2d(c.x - w50 * Math.cos(a + Math.PI / 2), c.y + w50 * Math.sin(a + Math.PI / 2));
        return new PolyLine2d(p1, p2);
    }

}
