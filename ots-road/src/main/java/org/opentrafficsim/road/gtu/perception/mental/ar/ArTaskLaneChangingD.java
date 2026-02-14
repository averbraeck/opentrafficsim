package org.opentrafficsim.road.gtu.perception.mental.ar;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.Stateless;
import org.opentrafficsim.road.gtu.perception.LanePerception;
import org.opentrafficsim.road.gtu.tactical.util.lmrs.LmrsParameters;

/**
 * Lane change task demand depending on lane change desire.
 * <p>
 * Copyright (c) 2024-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class ArTaskLaneChangingD extends AbstractArTask implements Stateless<ArTaskLaneChangingD>
{

    /** Singleton instance. */
    public static final ArTaskLaneChangingD SINGLETON = new ArTaskLaneChangingD();

    /**
     * Constructor.
     */
    public ArTaskLaneChangingD()
    {
        super("lane-changing");
    }

    @Override
    public double calculateTaskDemand(final LanePerception perception) throws ParameterException
    {
        Parameters parameters = perception.getGtu().getParameters();
        return Math.max(0.0,
                Math.max(parameters.getParameter(LmrsParameters.DLEFT), parameters.getParameter(LmrsParameters.DRIGHT)));
    }

    @Override
    public ArTaskLaneChangingD get()
    {
        return SINGLETON;
    }

}
