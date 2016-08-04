package org.opentrafficsim.road.gtu.lane.perception.categories;

import java.util.SortedSet;
import java.util.TreeSet;

import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.perception.TimeStampedObject;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayConflict;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayTrafficLight;

/**
 * Perceives traffic lights and intersection conflicts.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jul 22, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */

public class IntersectionCategory extends LaneBasedAbstractPerceptionCategory
{

    /** Set of traffic lights. */
    private TimeStampedObject<SortedSet<HeadwayTrafficLight>> trafficLights;
    
    /** Set of traffic lights. */
    private TimeStampedObject<SortedSet<HeadwayConflict>> conflicts;
    
    /**
     * @param perception perception
     */
    public IntersectionCategory(final LanePerception perception)
    {
        super(perception);
    }

    /** {@inheritDoc} */
    @Override
    public final void updateAll() throws GTUException
    {
        updateTrafficLights();
        updateConflicts();
    }
    
    /**
     * Updates set of traffic lights along the route. Traffic lights are sorted by headway value.
     * @throws GTUException if the GTU has not been initialized
     */
    public final void updateTrafficLights() throws GTUException
    {
        this.trafficLights = new TimeStampedObject<>(new TreeSet<>(), getTimestamp());
    }
    
    /**
     * Updates set of conflicts along the route. Traffic lights are sorted by headway value.
     * @throws GTUException if the GTU has not been initialized
     */
    public final void updateConflicts() throws GTUException
    {
        this.conflicts = new TimeStampedObject<>(new TreeSet<>(), getTimestamp());
    }
    
    /**
     * Returns a set of traffic lights along the route. Traffic lights are sorted by headway value.
     * @return set of traffic lights along the route
     */
    public final SortedSet<HeadwayTrafficLight> getTrafficLights()
    {
        return this.trafficLights.getObject();
    }
    
    /**
     * Returns a set of traffic lights along the route. Traffic lights are sorted by headway value.
     * @param lane lane
     * @return set of traffic lights along the route
     */
    public final SortedSet<HeadwayConflict> getConflicts(final RelativeLane lane)
    {
        return this.conflicts.getObject();
    }
    
    /**
     * Returns a time stamped set of traffic lights along the route. Traffic lights are sorted by headway value.
     * @return set of traffic lights along the route
     */
    public final TimeStampedObject<SortedSet<HeadwayTrafficLight>> getTimeStampedTrafficLights()
    {
        return this.trafficLights;
    }
    
    /**
     * Returns a time stamped set of traffic lights along the route. Traffic lights are sorted by headway value.
     * @return set of traffic lights along the route
     */
    public final TimeStampedObject<SortedSet<HeadwayConflict>> getTimeStampedConflicts()
    {
        return this.conflicts;
    }
    
    /** {@inheritDoc} */
    public final String toString()
    {
        return "IntersectionCategory";
    }

}
