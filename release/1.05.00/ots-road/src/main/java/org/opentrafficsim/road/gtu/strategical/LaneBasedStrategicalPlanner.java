package org.opentrafficsim.road.gtu.strategical;

import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.gtu.plan.strategical.StrategicalPlanner;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedTacticalPlanner;

/**
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 26, 2015 <br>
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
