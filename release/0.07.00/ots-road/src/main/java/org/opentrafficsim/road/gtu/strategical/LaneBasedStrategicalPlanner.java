package org.opentrafficsim.road.gtu.strategical;

import org.opentrafficsim.core.gtu.plan.strategical.StrategicalPlanner;
import org.opentrafficsim.road.gtu.lane.driver.LaneBasedDrivingCharacteristics;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 26, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public interface LaneBasedStrategicalPlanner extends StrategicalPlanner
{
    /**
     * get the personal drivingCharacteristics of the driver of a GTU.
     * @return the personal drivingCharacteristics of the driver of a GTU.
     */
    LaneBasedDrivingCharacteristics getDrivingCharacteristics();

    /**
     * set the personal drivingCharacteristics of the driver of a GTU.
     * @param drivingCharacteristics set drivingCharacteristics, if the driver of the GTU changes, or the driver is using
     *            different characteristics
     */
    void setDrivingCharacteristics(final LaneBasedDrivingCharacteristics drivingCharacteristics);
}
