package org.opentrafficsim.road.gtu.lane.tactical.following;

import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;

/**
 * Interface that can be implemented by desired headway models and desired speed models, such that they can be coupled to their
 * simulation context. Note that these models need to be able to work without this context, as part of peeking by GTU
 * generators. For instance, they can provide some default or average value. When the GTU is successfully generated, the
 * {@code init()} method is invoked by {@code AbstractCarFollowingModel}, and the model can work fully throughout the GTUs life
 * span thereafter.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public interface Initialisable
{

    /**
     * Initialize car-following model.
     * @param gtu LaneBasedGTU; gtu
     */
    void init(LaneBasedGTU gtu);

}
