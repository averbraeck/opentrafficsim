package org.opentrafficsim.road.gtu.lane.perception.mental;

import org.djunits.value.vdouble.scalar.Duration;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;

import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

/**
 * Class for exponential demand.
 */
public class ExponentialTask extends AbstractTask
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
     * @param id String; id
     * @param initialTaskDemand double; initial level of task demand
     * @param finalTaskDemand double; final level of task demand
     * @param tau Duration; time scale at which task demand changes from the initial to the final value
     * @param simulator SimulatorInterface.TimeDoubleUnit; simulator
     */
    public ExponentialTask(final String id, final double initialTaskDemand, final double finalTaskDemand, final Duration tau,
            final SimulatorInterface.TimeDoubleUnit simulator)
    {
        super(id);
        this.initialTaskDemand = initialTaskDemand;
        this.additionalTaskDemand = finalTaskDemand - initialTaskDemand;
        this.tau = tau;
        this.start = simulator.getSimulatorTime().si;
    }

    /** {@inheritDoc} */
    @Override
    public double calculateTaskDemand(final LanePerception perception, final LaneBasedGTU gtu, final Parameters parameters)
            throws ParameterException, GTUException
    {
        double t = gtu.getSimulator().getSimulatorTime().si - this.start;
        return this.initialTaskDemand + this.additionalTaskDemand * (1.0 - Math.exp(-t / this.tau.si));
    }

}
