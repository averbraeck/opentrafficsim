package org.opentrafficsim.road.gtu.strategical;

import org.djunits.value.vdouble.scalar.Duration;
import org.opentrafficsim.core.gtu.plan.strategical.StrategicalPlanner;
import org.opentrafficsim.road.gtu.LaneBasedGtu;
import org.opentrafficsim.road.gtu.tactical.LaneBasedTacticalPlanner;

/**
 * Interface for lane-based strategical planners. It specifies output of certain methods to produce lane-based objects.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Alexander Verbraeck
 * @author Peter Knoppers
 */
public interface LaneBasedStrategicalPlanner extends StrategicalPlanner
{

    @Override
    LaneBasedGtu getGtu();

    @Override
    LaneBasedTacticalPlanner getTacticalPlanner();

    @Override
    LaneBasedTacticalPlanner getTacticalPlanner(Duration time);

}
