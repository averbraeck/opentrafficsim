package org.opentrafficsim.core.gtu;

import org.opentrafficsim.core.gtu.plan.tactical.TacticalPlanner;

/**
 * A strategicalPlanner is the planner responsible for the overall 'mission' of the GTU, usually indicating where it needs to
 * go. It operates by instantiating tactical planners to do the actual work, which is generating operational plans (paths over
 * time) to follow to reach the destination that the strategical plan is aware of.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 14, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public interface StrategicalPlanner
{
    /**
     * Generate a new tactical planner for the GTU.
     * @param gtu the gtu to generate the plan for
     * @return a new tactical planner
     */
    TacticalPlanner generateTacticalPlanner(GTU gtu);
}