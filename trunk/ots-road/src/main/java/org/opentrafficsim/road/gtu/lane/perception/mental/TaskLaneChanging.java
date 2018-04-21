package org.opentrafficsim.road.gtu.lane.perception.mental;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.Try;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.mental.Fuller.Task;
import org.opentrafficsim.road.gtu.lane.perception.mental.TaskCarFollowing.TaskCarFollowingCollector;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsParameters;

/**
 * Lane changing task based on car-following (as gap-acceptance proxy), and an underlying fraction to include adjacent lanes.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 10 apr. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class TaskLaneChanging implements Task
{

    /** Lateral consideration. */
    private final LateralConsideration lateralConsideration;

    /**
     * Constructor.
     * @param lateralConsideration LateralConsideration; lateral consideration
     */
    public TaskLaneChanging(final LateralConsideration lateralConsideration)
    {
        this.lateralConsideration = lateralConsideration;
    }

    /**
     * Returns the demand of this task.
     * @param perception LanePerception; perception
     * @param gtu LaneBasedGTU; gtu
     * @param parameters Parameters; parameters
     * @return double; demand of this task
     * @throws ParameterException if a parameter is missing or out of bounds
     * @throws GTUException exceptions pertaining to the GTU
     */
    public double demand(final LanePerception perception, final LaneBasedGTU gtu, final Parameters parameters)
            throws ParameterException, GTUException
    {
        double lat = this.lateralConsideration.getConsideration(perception, gtu, parameters);
        if (lat == 0.0)
        {
            return 0.0;
        }
        RelativeLane lane;
        if (lat < 0.0)
        {
            lane = RelativeLane.LEFT;
            lat *= -1;
        }
        else
        {
            lane = RelativeLane.RIGHT;
        }
        NeighborsPerception neighbors = Try.assign(() -> perception.getPerceptionCategory(NeighborsPerception.class),
                "NeighborsPerception not available.");
        Try.execute(() -> neighbors.updateLeaders(lane), "Exception while updating leaders.");
        Try.execute(() -> neighbors.updateFollowers(lane), "Exception while updating leaders.");
        return lat * (0.6 * neighbors.getLeaders(lane).collect(new TaskCarFollowingCollector(gtu, parameters))
                + 0.4 * neighbors.getFollowers(lane).collect(new TaskCarFollowingCollector(gtu, parameters)));
    }

    /**
     * Lateral consideration leading to lane change task demand.
     * <p>
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 10 apr. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    public interface LateralConsideration
    {

        /** Desire based lateral consideration. */
        public static final LateralConsideration DESIRE = new LateralConsideration()
        {
            @Override
            public double getConsideration(final LanePerception perception, final LaneBasedGTU gtu, final Parameters parameters)
                    throws ParameterException, GTUException
            {
                double dLeft = gtu.getParameters().getParameter(LmrsParameters.DLEFT);
                double dRight = gtu.getParameters().getParameter(LmrsParameters.DRIGHT);
                if (dLeft > dRight && dLeft > 0.0)
                {
                    return dLeft > 1.0 ? -1.0 : -dLeft;
                }
                else if (dRight > dLeft && dRight > 0.0)
                {
                    return dRight > 1.0 ? 1.0 : dRight;
                }
                return 0.0;
            }
        };

        /**
         * Returns fraction of lateral consideration, &lt0 for left lane, &gt0 for right lane. Should be in the range -1 ... 1.
         * @param perception LanePerception; perception
         * @param gtu LaneBasedGTU; gtu
         * @param parameters Parameters; parameters
         * @return double; demand of this task
         * @throws ParameterException if a parameter is missing or out of bounds
         * @throws GTUException exceptions pertaining to the GTU
         */
        double getConsideration(LanePerception perception, LaneBasedGTU gtu, Parameters parameters)
                throws ParameterException, GTUException;
    }

}
