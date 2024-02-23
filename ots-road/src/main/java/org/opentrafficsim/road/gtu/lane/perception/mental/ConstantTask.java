package org.opentrafficsim.road.gtu.lane.perception.mental;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;

/**
 * Class for constant demand.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class ConstantTask extends AbstractTask
{
    /**
     * Constructor.
     * @param id String; id
     * @param taskDemand double; task demand
     */
    public ConstantTask(final String id, final double taskDemand)
    {
        super(id);
        setTaskDemand(taskDemand);
    }

    /** {@inheritDoc} */
    @Override
    public double calculateTaskDemand(final LanePerception perception, final LaneBasedGtu gtu, final Parameters parameters)
            throws ParameterException, GtuException
    {
        return getTaskDemand();
    }
}
