package org.opentrafficsim.road.gtu.lane.perception.categories;

import org.opentrafficsim.core.Type;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jul 14, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public abstract class AbstractPerceptionCategory extends Type<AbstractPerceptionCategory>
{

    /** Connected perception. */
    private final LanePerception perception;
    
    /**
     * Constructor setting the perception.
     * @param perception perception
     */
    public AbstractPerceptionCategory(final LanePerception perception)
    {
        this.perception = perception;
    }
    
    /**
     * Returns the connected perception.
     * @return connected perception
     */
    public final LanePerception getPerception()
    {
        return this.perception;
    }
    
    /**
     * Returns the connected GTU.
     * @return connected GTU
     */
    public final LaneBasedGTU getGTU()
    {
        return this.perception.getGTU();
    }
    
    /**
     * Updates all information in the category.
     */
    public abstract void update();

}
