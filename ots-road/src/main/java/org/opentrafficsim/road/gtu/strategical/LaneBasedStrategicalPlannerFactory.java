package org.opentrafficsim.road.gtu.strategical;

import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;

/**
 * A factory class is used to generate strategical planners as the strategical planner is state-full.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jul 29, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <T> class of the strategical planner generated
 */

public interface LaneBasedStrategicalPlannerFactory<T extends LaneBasedStrategicalPlanner>
{

    /**
     * Returns a set of behavioral characteristics with default values for the next strategical planner that will be generated.
     * @return set of behavioral characteristics with default values for the next strategical planner that will be generated
     */
    BehavioralCharacteristics getDefaultBehavioralCharacteristics();

    /**
     * Set behavioral characteristics to use with the next creation of a strategical planner. Only the next planner will use
     * this.
     * @param behavioralCharacteristics behavioral characteristics to use with the next creation of a strategical planner
     */
    void setBehavioralCharacteristics(BehavioralCharacteristics behavioralCharacteristics);

    /**
     * Creates a new strategical planner for the given GTU. If no default behavioral characteristics are set, the default values
     * will be used.
     * @param gtu GTU
     * @return strategical planner for the given GTU
     * @throws GTUException if the gtu is not suitable in any way for the creation of the strategical planner
     */
    T create(LaneBasedGTU gtu) throws GTUException;

}
