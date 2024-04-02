package org.opentrafficsim.road.gtu.lane.perception;

import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.mental.Mental;

/**
 * Standard perception completely based on connected perception categories.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class CategoricalLanePerception extends AbstractLanePerception
{

    /** */
    private static final long serialVersionUID = 20160729L;

    /**
     * @param gtu LaneBasedGtu; GTU
     */
    public CategoricalLanePerception(final LaneBasedGtu gtu)
    {
        super(gtu);
    }

    /**
     * @param gtu LaneBasedGtu; GTU
     * @param mental Mental; Mental
     */
    public CategoricalLanePerception(final LaneBasedGtu gtu, final Mental mental)
    {
        super(gtu, mental);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return super.toString();
    }

}
