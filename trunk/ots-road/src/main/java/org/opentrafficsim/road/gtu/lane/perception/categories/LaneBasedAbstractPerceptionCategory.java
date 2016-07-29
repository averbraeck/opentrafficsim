package org.opentrafficsim.road.gtu.lane.perception.categories;

import org.opentrafficsim.core.gtu.perception.AbstractPerceptionCategory;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jul 29, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */

public abstract class LaneBasedAbstractPerceptionCategory extends AbstractPerceptionCategory
{

    /**
     * @param perception perception
     */
    public LaneBasedAbstractPerceptionCategory(final LanePerception perception)
    {
        super(perception);
    }

    /**
     * Returns the connected GTU.
     * @return connected GTU
     */
    @Override
    public final LaneBasedGTU getGtu()
    {
        return ((LanePerception) getPerception()).getGtu();
    }
    
}
