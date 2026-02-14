package org.opentrafficsim.road.gtu.perception.mental.ar;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.road.gtu.LaneBasedGtu;
import org.opentrafficsim.road.gtu.perception.LanePerception;
import org.opentrafficsim.road.gtu.perception.mental.DistractionField;

/**
 * Task-demand for road-side distraction.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class ArTaskRoadSideDistraction extends AbstractArTask
{

    /** Distraction field. */
    private final DistractionField distractionField;

    /**
     * Constructor.
     * @param gtu GTU
     */
    public ArTaskRoadSideDistraction(final LaneBasedGtu gtu)
    {
        super("road-side distraction");
        this.distractionField = new DistractionField(gtu);
    }

    @Override
    public double calculateTaskDemand(final LanePerception perception) throws ParameterException
    {
        return this.distractionField.getDistraction((lane, distraction) -> lane.isCurrent());
    }

}
