package org.opentrafficsim.road.gtu.lane.perception.mental;

import org.djunits.value.vdouble.scalar.Duration;
import org.djutils.exceptions.Try;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.TaskHeadwayCollector;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsParameters;

/**
 * Lane changing task based on car-following (as gap-acceptance proxy), and an underlying consideration to include adjacent
 * lanes.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 10 apr. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class TaskLaneChanging extends TaskHeadwayBased
{

    /** Lateral consideration. */
    private final LateralConsideration lateralConsideration;

    /**
     * Constructor.
     * @param lateralConsideration LateralConsideration; lateral consideration
     */
    public TaskLaneChanging(final LateralConsideration lateralConsideration)
    {
        super("lane-changing");
        this.lateralConsideration = lateralConsideration;
    }

    /** {@inheritDoc} */
    @Override
    protected Duration getHeadway(final LanePerception perception, final LaneBasedGTU gtu, final Parameters parameters)
            throws ParameterException
    {
        NeighborsPerception neighbors = Try.assign(() -> perception.getPerceptionCategory(NeighborsPerception.class),
                "NeighborsPerception not available.");
        double lat = Try.assign(() -> this.lateralConsideration.getConsideration(perception, gtu, parameters),
                "Exception during lateral consideration.");
        RelativeLane lane;
        if (Math.abs(lat) < 1e-9)
        {
            return null;
        }
        if (lat < 0.0)
        {
            lane = RelativeLane.LEFT;
            lat = -lat;
        }
        else
        {
            lane = RelativeLane.RIGHT;
        }
        if (neighbors.isGtuAlongside(lane.getLateralDirectionality()))
        {
            return Duration.ZERO;
        }
        Duration h1 = neighbors.getLeaders(lane).collect(new TaskHeadwayCollector(getSpeed()));
        Duration h2 = neighbors.getFollowers(lane).collect(new TaskHeadwayCollector(getSpeed()));
        if (h1 == null)
        {
            return h2 == null ? null : h2.divide(lat);
        }
        if (h2 == null)
        {
            return h1 == null ? null : h1.divide(lat);
        }
        if (h1.eq0() && h2.eq0())
        {
            return Duration.ZERO;
        }
        return Duration.instantiateSI(h1.si * h2.si / (lat * (h1.si + h2.si)));
    }

    /**
     * Lateral consideration leading to lane change task demand.
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
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
        LateralConsideration DESIRE = new LateralConsideration()
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
         * Returns fraction of lateral consideration, &lt;0 for left lane, &gt;0 for right lane. Should be in the range -1 ...
         * 1.
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
