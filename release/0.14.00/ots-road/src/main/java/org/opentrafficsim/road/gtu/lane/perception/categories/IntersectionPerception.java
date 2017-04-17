package org.opentrafficsim.road.gtu.lane.perception.categories;

import java.util.SortedSet;

import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.perception.PerceptionCategory;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayConflict;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayTrafficLight;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 14 feb. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */

public interface IntersectionPerception extends PerceptionCategory
{

    /**
     * Updates set of traffic lights along the route. Traffic lights are sorted by headway value.
     * @throws GTUException if the GTU has not been initialized
     * @throws ParameterException if lane structure cannot be made due to missing parameter
     */
    void updateTrafficLights() throws GTUException, ParameterException;

    /**
     * Updates set of conflicts along the route. Traffic lights are sorted by headway value.
     * @throws GTUException if the GTU has not been initialized
     * @throws ParameterException if lane structure cannot be made due to missing parameter
     */
    void updateConflicts() throws GTUException, ParameterException;

    /**
     * Returns a set of traffic lights along the route. Traffic lights are sorted by headway value.
     * @param lane lane
     * @return set of traffic lights along the route
     */
    SortedSet<HeadwayTrafficLight> getTrafficLights(RelativeLane lane);

    /**
     * Returns a set of conflicts along the route. Conflicts are sorted by headway value.
     * @param lane lane
     * @return set of conflicts along the route
     */
    SortedSet<HeadwayConflict> getConflicts(RelativeLane lane);

    /**
     * Returns whether there is a conflict alongside to the left.
     * @return whether there is a conflict alongside to the left
     */
    boolean isAlongsideConflictLeft();

    /**
     * Returns whether there is a conflict alongside to the right.
     * @return whether there is a conflict alongside to the right
     */
    boolean isAlongsideConflictRight();

    /** {@inheritDoc} */
    @Override
    default void updateAll() throws GTUException, ParameterException
    {
        updateTrafficLights();
        updateConflicts();
    }
    
}
