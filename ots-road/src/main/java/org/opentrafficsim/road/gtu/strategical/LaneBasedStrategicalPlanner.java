package org.opentrafficsim.road.gtu.strategical;

import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.plan.strategical.StrategicalPlanner;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
     * get the personal parameters of the driver of a GTU.
     * @return the personal parameters of the driver of a GTU.
     */
    Parameters getParameters();

    /**
     * set the personal parameters of the driver of a GTU.
     * @param parameters set parameters, if the driver of the GTU changes, or the driver is using
     *            different characteristics
     */
    void setParameters(Parameters parameters);

    /** {@inheritDoc} */
    LaneBasedGTU getGtu();

}
