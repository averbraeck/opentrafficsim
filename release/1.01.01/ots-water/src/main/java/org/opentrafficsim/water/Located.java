/**
 * 
 */
package org.opentrafficsim.water;

import java.io.Serializable;

import javax.vecmath.Point3d;

import org.locationtech.jts.geom.Coordinate;
import org.opentrafficsim.core.geometry.OTSPoint3D;

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * A located object can report its location.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
public interface Located extends Locatable, Serializable
{
    /** @return the directed point */
    @Override
    DirectedPoint getLocation();

    /** @return the GIS coordinate */
    default Coordinate getCoordinate()
    {
        return new Coordinate(getLocation().x, getLocation().y);
    }

    /** @return the coordinate for DSOL 2D or 3D */
    default Point3d getPoint3d()
    {
        return new Point3d(getLocation().x, getLocation().y, getLocation().z);
    }

    /** @return the coordinate for DSOL 2D or 3D */
    default OTSPoint3D getOTSPoint3D()
    {
        return new OTSPoint3D(getLocation());
    }

}
