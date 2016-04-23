package org.opentrafficsim.core.perception;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.math.Rotation3D;
import org.opentrafficsim.core.math.Rotation3D.Abs;
import org.opentrafficsim.core.math.Speed3D;

/**
 * Information about a perceived object, which can be a GTU, a road sign, a traffic light, or an obstacle. This implementation
 * stores the information internally as (perceived) absolute locations.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Dec 10, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class PerceivedObject2D implements PerceivedObject
{
    /** */
    private static final long serialVersionUID = 20151210L;

    /** The perceived object type. */
    PerceivedObjectType perceivedObjectType;

    // TODO finish PerceivedObject2D

    /** {@inheritDoc} */
    @Override
    public final PerceivedObjectType getPerceivedObjectType()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final Length getPerceivedDistance()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final Rotation3D.Rel getPerceivedRelativeAngle()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final OTSPoint3D getPerceivedLocation()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final Rotation3D.Rel getPerceivedRelativeMovingDirection()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final Abs getPerceivedMovingDirection()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final Speed getPerceivedSpeed()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final Speed3D getPerceivedSpeed3D()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "PerceivedObject2D [perceivedObjectType=" + this.perceivedObjectType + "]";
    }

}
