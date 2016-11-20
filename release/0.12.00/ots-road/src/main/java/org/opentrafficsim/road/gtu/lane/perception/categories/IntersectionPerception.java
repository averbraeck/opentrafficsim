package org.opentrafficsim.road.gtu.lane.perception.categories;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.base.TimeStampedObject;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.road.gtu.lane.perception.EnvironmentState.ViewingDirection;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayConflict;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayTrafficLight;
import org.opentrafficsim.road.network.lane.conflict.Conflict;
import org.opentrafficsim.road.network.lane.object.trafficlight.SimpleTrafficLight;

/**
 * Perceives traffic lights and intersection conflicts.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jul 22, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */

public class IntersectionPerception extends LaneBasedAbstractPerceptionCategory
{

    /** */
    private static final long serialVersionUID = 20160811L;

    /** Set of traffic lights. */
    private Map<RelativeLane, TimeStampedObject<SortedSet<HeadwayTrafficLight>>> trafficLights = new HashMap<>();

    /** Set of conflicts. */
    private Map<RelativeLane, TimeStampedObject<SortedSet<HeadwayConflict>>> conflicts = new HashMap<>();

    /**
     * @param perception perception
     */
    public IntersectionPerception(final LanePerception perception)
    {
        super(perception);
    }

    /** {@inheritDoc} */
    @Override
    public final void updateAll() throws GTUException, ParameterException
    {
        updateTrafficLights();
        updateConflicts();
    }

    /**
     * Updates set of traffic lights along the route. Traffic lights are sorted by headway value.
     * @throws GTUException if the GTU has not been initialized
     * @throws ParameterException if lane structure cannot be made due to missing parameter
     */
    public final void updateTrafficLights() throws GTUException, ParameterException
    {
        this.trafficLights.clear();
        for (RelativeLane lane : getPerception().getLaneStructure().getCrossSection())
        {
            SortedSet<HeadwayTrafficLight> set = new TreeSet<>();
            this.trafficLights.put(lane, new TimeStampedObject<>(set, getTimestamp()));
            Map<Length, Set<SimpleTrafficLight>> map = new HashMap<>();
            // TODO SimpleTrafficLight is not yet a LaneBasedObject
            //        getPerception().getEnvironmentState().getSortedObjects(ViewingDirection.FORWARD, lane, SimpleTrafficLight.class);
            for (Length length : map.keySet())
            {
                for (SimpleTrafficLight trafficLight : map.get(length))
                {
                    set.add(new HeadwayTrafficLight(trafficLight, length));
                }
            }
        }
    }

    /**
     * Updates set of conflicts along the route. Traffic lights are sorted by headway value.
     * @throws GTUException if the GTU has not been initialized
     * @throws ParameterException if lane structure cannot be made due to missing parameter
     */
    public final void updateConflicts() throws GTUException, ParameterException
    {
        this.conflicts.clear();
        for (RelativeLane lane : getPerception().getLaneStructure().getCrossSection())
        {
            SortedSet<HeadwayConflict> set = new TreeSet<>();
            this.conflicts.put(lane, new TimeStampedObject<>(set, getTimestamp()));
            Map<Length, Set<Conflict>> map =
                    getPerception().getEnvironmentState().getSortedObjects(ViewingDirection.FORWARD, lane, Conflict.class);
            for (Length length : map.keySet())
            {
                for (Conflict conflict : map.get(length))
                {
                    // TODO needs a lot of input
                    //set.add(new HeadwayConflict(...))
                }
            }
        }
    }

    /**
     * Returns a set of traffic lights along the route. Traffic lights are sorted by headway value.
     * @param lane lane
     * @return set of traffic lights along the route
     */
    public final SortedSet<HeadwayTrafficLight> getTrafficLights(final RelativeLane lane)
    {
        return this.trafficLights.get(lane).getObject();
    }

    /**
     * Returns a set of traffic lights along the route. Traffic lights are sorted by headway value.
     * @param lane lane
     * @return set of traffic lights along the route
     */
    public final SortedSet<HeadwayConflict> getConflicts(final RelativeLane lane)
    {
        return this.conflicts.get(lane).getObject();
    }

    /**
     * Returns a time stamped set of traffic lights along the route. Traffic lights are sorted by headway value.
     * @param lane lane
     * @return set of traffic lights along the route
     */
    public final TimeStampedObject<SortedSet<HeadwayTrafficLight>> getTimeStampedTrafficLights(final RelativeLane lane)
    {
        return this.trafficLights.get(lane);
    }

    /**
     * Returns a time stamped set of traffic lights along the route. Traffic lights are sorted by headway value.
     * @param lane lane
     * @return set of traffic lights along the route
     */
    public final TimeStampedObject<SortedSet<HeadwayConflict>> getTimeStampedConflicts(final RelativeLane lane)
    {
        return this.conflicts.get(lane);
    }

    /** {@inheritDoc} */
    public final String toString()
    {
        return "IntersectionCategory";
    }

}
