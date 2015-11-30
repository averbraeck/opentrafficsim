package org.opentrafficsim.road.gtu.strategical.route;

import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.plan.tactical.TacticalPlanner;
import org.opentrafficsim.road.gtu.lane.driver.LaneBasedDrivingCharacteristics;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedCFLCTacticalPlanner;
import org.opentrafficsim.road.gtu.strategical.AbstractLaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;

/**
 * Strategical planner, route-based, with personal driving characteristics, which contain settings for the tactical planner.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 26, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class LaneBasedStrategicalRoutePlanner extends AbstractLaneBasedStrategicalPlanner implements LaneBasedStrategicalPlanner
{
    /**
     * @param drivingCharacteristics the personal driving characteristics, which contain settings for the tactical planner
     */
    public LaneBasedStrategicalRoutePlanner(LaneBasedDrivingCharacteristics drivingCharacteristics)
    {
        super(drivingCharacteristics);
    }

    /** {@inheritDoc} */
    @Override
    public TacticalPlanner generateTacticalPlanner(final GTU gtu)
    {
        return new LaneBasedCFLCTacticalPlanner(this);
    }

}
