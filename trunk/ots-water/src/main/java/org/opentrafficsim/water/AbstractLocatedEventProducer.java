/**
 * 
 */
package org.opentrafficsim.water;

import org.djutils.draw.point.Point3d;
import org.djutils.event.EventProducer;
import org.locationtech.jts.geom.Coordinate;

/**
 * Base abstract class for a located object that can produce events.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * <p>
 * Based on software from the IDVV project, which is Copyright (c) 2013 Rijkswaterstaat - Dienst Water, Verkeer en Leefomgeving
 * and licensed without restrictions to Delft University of Technology, including the right to sub-license sources and derived
 * products to third parties.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 6, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public abstract class AbstractLocatedEventProducer extends EventProducer implements Located
{
    /** */
    private static final long serialVersionUID = 1L;

    /** coordinate on the map. */
    private Coordinate coordinate;

    /**
     * @param coordinate Coordinate; the coordinate
     */
    public AbstractLocatedEventProducer(final Coordinate coordinate)
    {
        this.coordinate = coordinate;
    }

    /** {@inheritDoc} */
    @Override
    public final Coordinate getCoordinate()
    {
        return this.coordinate;
    }

    /** {@inheritDoc} */
    @Override
    public final Point3d getPoint3d()
    {
        return new Point3d(this.coordinate.x, this.coordinate.y, this.coordinate.z);
    }

}
