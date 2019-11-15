package org.opentrafficsim.road.gtu.lane.tactical.following;

import org.opentrafficsim.road.gtu.lane.tactical.ModelComponentFactory;

/**
 * Factory for car-following models.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Sep 15, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <T> class of car following model
 */
public interface CarFollowingModelFactory<T extends CarFollowingModel> extends ModelComponentFactory
{

    /**
     * Returns a new instance of a car-following model.
     * @return new instance of a car-following model
     */
    T generateCarFollowingModel();

}
