package org.opentrafficsim.road.network.lane.object;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.geometry.OTSLine3D;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 30, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class AbstractTrafficLight extends AbstractCSEObject
{
    /**
     * @param geometry the geometry of the traffic light
     * @param height the height of the "barrier"
     */
    public AbstractTrafficLight(final OTSLine3D geometry, final Length height)
    {
        super(geometry, height);
    }

}
