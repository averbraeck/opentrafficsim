package org.opentrafficsim.road.gtu.lane.tactical.following;

import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;

/**
 * Interface that can be implemented by desired headway models and desired speed models, such that they can be coupled to their
 * simulation context. Note that these models need to be able to work without this context, as part of peeking by GTU
 * generators. For instance, they can provide some default or average value. When the GTU is successfully generated, the
 * {@code init()} method is invoked by {@code AbstractCarFollowingModel}, and the model can work fully throughout the GTUs life
 * span thereafter.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 3 mrt. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface Initialisable
{

    /**
     * Initialize car-following model.
     * @param gtu LaneBasedGTU; gtu
     */
    void init(LaneBasedGTU gtu);

}
