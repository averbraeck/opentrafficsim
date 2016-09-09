package org.opentrafficsim.road.gtu.lane.perception;

import java.util.SortedSet;

import org.opentrafficsim.road.network.lane.LaneBasedObject;

/**
 * Provides 'friendly' access to the LaneStructure from a GTU point of view.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Sep 9, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface EnvironmentState
{
    <T extends LaneBasedObject> SortedSet<T> getSortedObjects(final ViewingDirection viewingDirection, final RelativeLane relativeLane, final Class<T> clazz);
    
    public enum ViewingDirection
    {
        /** Forward direction. */
        FORWARD,
        
        /** Backward direction.*/
        BACKWARD;
    }
}

