package org.opentrafficsim.road.gtu.lane.plan.operational;

import java.util.List;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;

import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * An operational plan with some extra information about the lanes and lane changes so this information does not have to be
 * recalculated multiple times. Furthermore, it is quite expensive to check whether a lane change is part of the oprtational
 * plan based on geographical data.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jan 20, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class LaneBasedOperationalPlan extends OperationalPlan
{
    /** */
    private static final long serialVersionUID = 20160120L;

    /** Deviative; meaning not along lane center lines. */
    private final boolean deviative;

    /**
     * Construct an operational plan with or without a lane change.
     * @param gtu the GTU for debugging purposes
     * @param path the path to follow from a certain time till a certain time. The path should have <i>at least</i> the length
     * @param startTime the absolute start time when we start executing the path
     * @param startSpeed the GTU speed when we start executing the path
     * @param operationalPlanSegmentList the segments that make up the path with an acceleration, constant speed or deceleration
     *            profile
     * @param deviative whether the path is not along lane center lines
     * @throws OperationalPlanException when the path is too short for the operation
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public LaneBasedOperationalPlan(final LaneBasedGTU gtu, final OTSLine3D path, final Time startTime, final Speed startSpeed,
            final List<Segment> operationalPlanSegmentList, final boolean deviative) throws OperationalPlanException
    {
        super(gtu, path, startTime, startSpeed, operationalPlanSegmentList);
        this.deviative = deviative;
    }

    /**
     * Build a plan where the GTU will wait for a certain time. Of course no lane change takes place.
     * @param gtu the GTU for debugging purposes
     * @param waitPoint the point at which the GTU will wait
     * @param startTime the current time or a time in the future when the plan should start
     * @param duration the waiting time
     * @param deviative whether the path is not along lane center lines
     * @throws OperationalPlanException when construction of a waiting path fails
     */
    public LaneBasedOperationalPlan(final LaneBasedGTU gtu, final DirectedPoint waitPoint, final Time startTime,
            final Duration duration, final boolean deviative) throws OperationalPlanException
    {
        super(gtu, waitPoint, startTime, duration);
        this.deviative = deviative;
    }

    /**
     * Check if we deviate from the center line.
     * @return whether this maneuver involves deviation from the center line.
     */
    public final boolean isDeviative()
    {
        return this.deviative;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "LaneBasedOperationalPlan [deviative=" + this.deviative + "]";
    }
}
