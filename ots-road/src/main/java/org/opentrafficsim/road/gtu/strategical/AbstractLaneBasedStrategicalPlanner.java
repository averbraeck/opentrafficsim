package org.opentrafficsim.road.gtu.strategical;

import java.io.Serializable;

import org.djutils.exceptions.Throw;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;

/**
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
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
