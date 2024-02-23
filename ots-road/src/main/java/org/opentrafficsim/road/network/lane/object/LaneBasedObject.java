package org.opentrafficsim.road.network.lane.object;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.bounds.Bounds2d;
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
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public interface LaneBasedObject extends LocatedObject
{
    /** @return The lane for which this is a sensor. */
    Lane getLane();

    /** @return the position (between 0.0 and the length of the Lane) of the sensor on the design line of the lane. */
    Length getLongitudinalPosition();

    /**
     * Return the location without throwing a RemoteException.
     * @return OrientedPoint2d; the location
     */
    @Override
    OrientedPoint2d getLocation();

    /**
     * Return the bounds without throwing a RemoteException.
     * @return Bounds; the (usually rectangular) bounds of the object
     */
    @Override
    Bounds2d getBounds();

    /**
     * Make a geometry perpendicular to the center line of the lane at the given position.
     * @param lane Lane; the lane where the sensor resides
     * @param position Length; The length of the object in the longitudinal direction, on the center line of the lane
     * @return a geometry perpendicular to the center line that describes the sensor
     */
    static PolyLine2d makeGeometry(final Lane lane, final Length position)
    {
        Throw.whenNull(lane, "lane is null");
        Throw.whenNull(position, "position is null");
        OrientedPoint2d sp = lane.getCenterLine().getLocationExtended(position);
        double w45 = 0.45 * lane.getWidth(position).si;
        double a = sp.getDirZ() + Math.PI / 2.0;
        Point2d p1 = new Point2d(sp.x + w45 * Math.cos(a), sp.y - w45 * Math.sin(a));
        Point2d p2 = new Point2d(sp.x - w45 * Math.cos(a), sp.y + w45 * Math.sin(a));
        return new PolyLine2d(p1, p2);
    }

}
