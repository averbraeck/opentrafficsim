package org.opentrafficsim.core.object;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.base.Identifyable;
import org.opentrafficsim.core.geometry.OTSLine3D;

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.event.EventProducerInterface;

/**
 * Generic object that can be placed in the model. This could be implemented for a traffic light, a road sign, or an obstacle.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Dec 16, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public interface ObjectInterface extends Locatable, Identifyable, EventProducerInterface
{
    /** @return the outline geometry of the object. */
    OTSLine3D getGeometry();
    
    /** @return the height of the object (can be Length.ZERO). */
    Length getHeight();
}
