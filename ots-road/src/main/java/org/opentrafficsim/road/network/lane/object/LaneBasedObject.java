package org.opentrafficsim.road.network.lane.object;



import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.geometry.Bounds;
import org.opentrafficsim.core.geometry.DirectedPoint;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.object.ObjectInterface;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * Objects that can be encountered on a Lane like conflict areas, GTUs, traffic lights, stop lines, etc.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Sep 9, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface LaneBasedObject extends ObjectInterface
{
    /** @return The lane for which this is a sensor. */
    Lane getLane();

    /** @return Longitudinal direction. */
    LongitudinalDirectionality getDirection();

    /** @return the position (between 0.0 and the length of the Lane) of the sensor on the design line of the lane. */
    Length getLongitudinalPosition();

    /**
     * Return the location without throwing a RemoteException.
     * @return DirectedPoint; the location
     */
    @Override
    DirectedPoint getLocation();

    /**
     * Return the bounds without throwing a RemoteException.
     * @return Bounds; the (usually rectangular) bounds of the object
     */
    @Override
    Bounds getBounds();

    /**
     * Make a geometry perpendicular to the center line of the lane at the given position.
     * @param lane Lane; the lane where the sensor resides
     * @param position Length; The length of the object in the longitudinal direction, on the center line of the lane
     * @return a geometry perpendicular to the center line that describes the sensor
     */
    static OTSLine3D makeGeometry(final Lane lane, final Length position)
    {
        Throw.whenNull(lane, "lane is null");
        Throw.whenNull(position, "position is null");
        DirectedPoint sp = lane.getCenterLine().getLocationExtended(position);
        double w45 = 0.45 * lane.getWidth(position).si;
        double a = sp.getRotZ() + Math.PI / 2.0;
        OTSPoint3D p1 = new OTSPoint3D(sp.x + w45 * Math.cos(a), sp.y - w45 * Math.sin(a), sp.z + 0.0001);
        OTSPoint3D p2 = new OTSPoint3D(sp.x - w45 * Math.cos(a), sp.y + w45 * Math.sin(a), sp.z + 0.0001);
        try
        {
            return new OTSLine3D(p1, p2);
        }
        catch (OTSGeometryException exception)
        {
            throw new RuntimeException(exception);
        }
    }

}
