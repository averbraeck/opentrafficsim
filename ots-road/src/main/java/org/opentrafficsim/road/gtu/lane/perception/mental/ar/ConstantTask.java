package org.opentrafficsim.road.gtu.lane.perception.mental.ar;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;

/**
 * Class for constant demand.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class ConstantTask extends AbstractArTask
{

    /** Constant task demand level. */
    private final double taskDemand;

    /**
     * Constructor.
     * @param id id
     * @param taskDemand task demand
     */
    public ConstantTask(final String id, final double taskDemand)
    {
        super(id);
        this.taskDemand = taskDemand;
    }

    @Override
    public double calculateTaskDemand(final LanePerception perception) throws ParameterException
    {
        return this.taskDemand;
    }

}
