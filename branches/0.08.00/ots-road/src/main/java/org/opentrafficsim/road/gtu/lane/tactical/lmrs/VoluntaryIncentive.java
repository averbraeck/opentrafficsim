package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;

/**
 * Interface for lane change incentives that determine a level of lane change
 * desire. Different incentives may determine lane change desire, which the lane
 * change model combines in a total lane change desire.
 * @author Wouter Schakel
 */
public interface VoluntaryIncentive {
    
    /**
     * Determines level of lane change desire for a lane change incentive.
     * @param gtu GTU to determine the lane change desire for.
     * @param perception Perception which supplies the situation.
     * @param mandatory Level of total mandatory desire, may be used to ignore or reduce a voluntary incentive.
     * @return Level of lane change desire for this incentive.
     */
    public Desire determineDesire(LaneBasedGTU gtu, LanePerception perception, Desire mandatory);
    
}