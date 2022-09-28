package org.opentrafficsim.road.gtu.strategical;

import java.io.Serializable;

import org.djutils.exceptions.Throw;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;

/**
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 26, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public abstract class AbstractLaneBasedStrategicalPlanner implements LaneBasedStrategicalPlanner, Serializable
{
    /** */
    private static final long serialVersionUID = 20151126L;

    /** GTU. */
    private final LaneBasedGTU gtu;

    /**
     * @param gtu LaneBasedGTU; GTU
     */
    public AbstractLaneBasedStrategicalPlanner(final LaneBasedGTU gtu)
    {
        Throw.whenNull(gtu, "GTU may not be null.");
        this.gtu = gtu;
    }

    /** {@inheritDoc} */
    @Override
    public final LaneBasedGTU getGtu()
    {
        return this.gtu;
    }

}
