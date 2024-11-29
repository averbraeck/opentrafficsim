package org.opentrafficsim.road.network.lane;

import java.io.Serializable;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.point.OrientedPoint2d;
import org.opentrafficsim.base.geometry.OtsLine2d;

/**
 * Store one position and lane of a GTU.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param lane the lane for the position
 * @param position the position on the lane, relative to the cross section link (design line) line, or against it
 */
public record LanePosition(Lane lane, Length position) implements Serializable
{

    /** */
    private static final long serialVersionUID = 20151111L;

    /**
     * Retrieve the location and direction of the GTU on the lane.
     * @return the location and direction of the GTU on the lane
     */
    public final OrientedPoint2d getLocation()
    {
        // double fraction = this.position.si / this.lane.getParentLink().getLength().si;
        OtsLine2d centerLine = this.lane.getCenterLine();
        double centerLineLength = centerLine.getLength();
        double fraction = this.position.si / centerLineLength;
        return centerLine.getLocationPointFractionExtended(fraction);
    }

}
