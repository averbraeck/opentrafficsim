package org.opentrafficsim.road.gtu.lane;

import java.util.Set;

import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.GTUCharacteristics;
import org.opentrafficsim.road.gtu.lane.perception.LanePerceptionFull;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;

/**
 * Characteristics for a lane base GTU. This class is used to store all characteristics of a (not-yet constructed) LaneBasedGTU.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Mar 8, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class LaneBasedGTUCharacteristics extends GTUCharacteristics
{
    /** The lane perception of the GTU. */
    final LanePerceptionFull perception;

    /** The strategical planner of the GTU. */
    final LaneBasedStrategicalPlanner strategicalPlanner;

    /** The maximum speed of the GTU. */
    final Speed speed;

    /** The initial lanes, positions and direction of the GTU. */
    final Set<DirectedLanePosition> initialLongitudinalPositions;

    /**
     * Construct a new set of lane based GTU characteristics.
     * @param gtuCharacteristics GTUCharacteristics; characteristics of the super GTU type to be used for the GTU
     * @param lanePerceptionFull LanePerceptionFull; the perception for the GTU
     * @param laneBasedStrategicalPlanner LaneBasedStrategicalPlanner; the strategical planner for the GTU
     * @param speed Speed; the initial speed of the GTU
     * @param initialLongitudinalPositions Set&lt;DirectedLanePosition&gt;; the lane, initial position and direction of the GTU
     */
    public LaneBasedGTUCharacteristics(final GTUCharacteristics gtuCharacteristics, LanePerceptionFull lanePerceptionFull,
            LaneBasedStrategicalPlanner laneBasedStrategicalPlanner, Speed speed,
            Set<DirectedLanePosition> initialLongitudinalPositions)
    {
        super(gtuCharacteristics.getGTUType(), gtuCharacteristics.getIdGenerator(), gtuCharacteristics.getLength(),
                gtuCharacteristics.getWidth(), gtuCharacteristics.getMaximumSpeed(), gtuCharacteristics.getSimulator(),
                gtuCharacteristics.getNetwork());
        this.perception = lanePerceptionFull;
        this.strategicalPlanner = laneBasedStrategicalPlanner;
        this.speed = speed;
        this.initialLongitudinalPositions = initialLongitudinalPositions;
    }

    /**
     * @return LanePerceptionFull; the lane perception of the GTU
     */
    public LanePerceptionFull getPerception()
    {
        return this.perception;
    }

    /**
     * @return LaneBasedStrategicalPlanner; the strategical planner for the GTU
     */
    public LaneBasedStrategicalPlanner getStrategicalPlanner()
    {
        return this.strategicalPlanner;
    }

    /**
     * @return Speed; the maximum speed of the GTU
     */
    public Speed getSpeed()
    {
        return this.speed;
    }

    /**
     * @return Set&lt;DirectedLanePosition&gt;; the position and direction on each lane that the GTU will initially be on
     */
    public Set<DirectedLanePosition> getInitialLongitudinalPositions()
    {
        return this.initialLongitudinalPositions;
    }

}
