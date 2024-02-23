package org.opentrafficsim.road.gtu.strategical;

import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.gtu.plan.strategical.StrategicalPlanner;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedTacticalPlanner;

/**
 * Interface for lane-based strategical planners. It specifies output of certain methods to produce lane-based objects.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public interface LaneBasedStrategicalPlanner extends StrategicalPlanner
{

    /** {@inheritDoc} */
    @Override
    LaneBasedGtu getGtu();

    /** {@inheritDoc} */
    @Override
    LaneBasedTacticalPlanner getTacticalPlanner();

    /** {@inheritDoc} */
    @Override
    LaneBasedTacticalPlanner getTacticalPlanner(Time time);

}
