package org.opentrafficsim.road.gtu.strategical;

import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.gtu.plan.strategical.StrategicalPlanner;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedTacticalPlanner;

/**
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public interface LaneBasedStrategicalPlanner extends StrategicalPlanner
{

    /** {@inheritDoc} */
    @Override
    LaneBasedGTU getGtu();

    /** {@inheritDoc} */
    @Override
    LaneBasedTacticalPlanner getTacticalPlanner();

    /** {@inheritDoc} */
    @Override
    LaneBasedTacticalPlanner getTacticalPlanner(Time time);

}
