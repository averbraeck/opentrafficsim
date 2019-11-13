package org.opentrafficsim.core.perception;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.math.Angle3D;
import org.opentrafficsim.core.math.Direction3D;
import org.opentrafficsim.core.math.Speed3D;

/**
 * Information about a perceived object, which can be a GTU, a road sign, a traffic light, or an obstacle. This interface works
 * with 2D locations to retrieve the information.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Dec 10, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public interface PerceivedObject2D extends PerceivedObject
{
    /**
     * Get the perceived distance to the object. When we combine relative distance and angle with our own location, the object's
     * location can be calculated.
     * @return the perceived distance to the object
     */
    Length getPerceivedDistance();

    /**
     * Get the perceived location of the object. The perceived location of the object is providing the same information as the
     * combination of perceived distance and relative angle, combined with our own location and RPY direction.
     * @return the perceived XYZ-location of the object
     */
    OTSPoint3D getPerceivedLocation();

    /**
     * Get the perceived 3D moving angle from our reference point to the object.
     * @return the perceived 3D-angle of movement of the object
     */
    Angle3D getPerceivedRelativeMovingDirection();

    /**
     * Get the perceived absolute 3D moving angle.
     * @return the perceived 3D-angle of movement of the object
     */
    Direction3D getPerceivedMovingDirection();

    /**
     * Get the perceived speed of the object.
     * @return the perceived speed of the object, in the RPY-direction it is facing
     */
    Speed getPerceivedSpeed();

    /**
     * Get the perceived speed of the object, split into movement in the X, Y, and Z direction.
     * @return a speed vector in 3 dimensions.
     */
    Speed3D getPerceivedSpeed3D();
}
