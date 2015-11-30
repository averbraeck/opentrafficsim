package org.opentrafficsim.road.gtu.lane.object;

import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.object.StaticObject;
import org.opentrafficsim.road.network.lane.CrossSectionElement;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 26, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public abstract class AbstractCSEObject extends StaticObject
{
    /**
     * @param geometry the geometry of the object
     * @param height the height of the object
     */
    public AbstractCSEObject(final OTSLine3D geometry, final Length.Rel height)
    {
        super(geometry, height);
    }

    /**
     * Create an object around a position on the center of a cross section element (e.g. lane).
     * @param cse the cross section element, e.g. lane, where the block is located
     * @param position the relative position on the design line of the link for this block
     * @param length the length of the object, parallel to the center line of the cse
     * @param width the width of the object, perpendicular to the center line of the cse
     * @return a new Geometry on the right position on the cse
     * @throws NetworkException in case of degenerate line or position beyond the center line
     */
    protected static OTSLine3D createRectangleOnCSE(final CrossSectionElement cse, final Length.Rel position,
        final Length.Rel length, final Length.Rel width) throws NetworkException
    {
        double fraction = position.si / cse.getParentLink().getLength().si;
        double w2 = width.si / 2.0;
        double l2 = length.si / 2.0;
        DirectedPoint cp = cse.getCenterLine().getLocationFraction(fraction);
        double a = cp.getRotZ();
        double l2ca = l2 * Math.cos(a);
        double l2sa = l2 * Math.sin(a);
        double w2ca = w2 * Math.cos(a + Math.PI / 2.0);
        double w2sa = w2 * Math.sin(a + Math.PI / 2.0);
        OTSPoint3D p1 = new OTSPoint3D(cp.x - l2ca - w2ca, cp.y - l2sa - w2sa, cp.z);
        OTSPoint3D p3 = new OTSPoint3D(cp.x + l2ca + w2ca, cp.y + l2sa + w2sa, cp.z);
        OTSPoint3D p2 = new OTSPoint3D(cp.x + l2ca - w2ca, cp.y + l2sa - w2sa, cp.z);
        OTSPoint3D p4 = new OTSPoint3D(cp.x - l2ca + w2ca, cp.y - l2sa + w2sa, cp.z);
        return new OTSLine3D(p1, p2, p3, p4, p1);
    }

    /**
     * Create an object around a position on the center of a cross section element (e.g. lane).
     * @param cse the cross section element, e.g. lane, where the block is located
     * @param position the relative position on the design line of the link for this block
     * @param length the length of the object, parallel to the center line of the cse
     * @param width the width of the object, perpendicular to the center line of the cse
     * @param distance the lateral distance of the object to the lane's center line; note: plus is left, minus is right
     * @return a new Geometry on the right position on the cse
     * @throws NetworkException in case of degenerate line or position beyond the center line
     */
    protected static OTSLine3D createRectangleNextToCSE(final CrossSectionElement cse, final Length.Rel position,
        final Length.Rel length, final Length.Rel width, final Length.Rel distance) throws NetworkException
    {
        double fraction = position.si / cse.getParentLink().getLength().si;
        double w2 = width.si / 2.0;
        double l2 = length.si / 2.0;
        double d = distance.si;
        DirectedPoint cp = cse.getCenterLine().getLocationFraction(fraction);
        double a = cp.getRotZ();
        cp =
            new DirectedPoint(cp.x + d * Math.cos(a + Math.PI / 2.0), cp.y + d * Math.sin(a + Math.PI / 2.0), cp.z, cp
                .getRotX(), cp.getRotY(), cp.getRotZ());
        double l2ca = l2 * Math.cos(a);
        double l2sa = l2 * Math.sin(a);
        double w2ca = w2 * Math.cos(a + Math.PI / 2.0);
        double w2sa = w2 * Math.sin(a + Math.PI / 2.0);
        OTSPoint3D p1 = new OTSPoint3D(cp.x - l2ca - w2ca, cp.y - l2sa - w2sa, cp.z);
        OTSPoint3D p3 = new OTSPoint3D(cp.x + l2ca + w2ca, cp.y + l2sa + w2sa, cp.z);
        OTSPoint3D p2 = new OTSPoint3D(cp.x + l2ca - w2ca, cp.y + l2sa - w2sa, cp.z);
        OTSPoint3D p4 = new OTSPoint3D(cp.x - l2ca + w2ca, cp.y - l2sa + w2sa, cp.z);
        return new OTSLine3D(p1, p2, p3, p4, p1);
    }

}
