package org.opentrafficsim.road.gtu.lane.perception.mental.ar;

import org.djunits.value.vdouble.scalar.Duration;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;

/**
 * Class for exponential demand.
 * <p>
 * Copyright (c) 2024-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class ArTaskExponential extends AbstractArTask
{

    /** Initial level of task demand. */
    private final double initialTaskDemand;

    /** Additional task demand. */
    private final double additionalTaskDemand;

    /** Time scale at which task demand changes from the initial to the final value. */
    private final Duration tau;

    /** Start time of the distraction. */
    private final double start;

    /**
     * Constructor.
     * @param id id
     * @param initialTaskDemand initial level of task demand
     * @param finalTaskDemand final level of task demand
     * @param tau time scale at which task demand changes from the initial to the final value
     * @param simulator simulator
     */
    public ArTaskExponential(final String id, final double initialTaskDemand, final double finalTaskDemand, final Duration tau,
            final OtsSimulatorInterface simulator)
    {
        super(id);
        this.initialTaskDemand = initialTaskDemand;
        this.additionalTaskDemand = finalTaskDemand - initialTaskDemand;
        this.tau = tau;
        this.start = simulator.getSimulatorTime().si;
    }

    @Override
    public double calculateTaskDemand(final LanePerception perception) throws ParameterException
    {
        double t = perception.getGtu().getSimulator().getSimulatorTime().si - this.start;
        return this.initialTaskDemand + this.additionalTaskDemand * (1.0 - Math.exp(-t / this.tau.si));
    }

}
