package org.opentrafficsim.road.definitions;

import java.io.Serializable;

import javax.naming.NamingException;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.dsol.AbstractOtsModel;
import org.opentrafficsim.core.dsol.OtsModelInterface;
import org.opentrafficsim.core.dsol.OtsSimulator;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.road.network.OtsRoadNetwork;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEvent;

/**
 * Demonstration of problem.
 * @author pknoppers
 */
public class DSOLProblem
{
    /** The simulation engine. */
    private OtsSimulatorInterface simulator;

    /**
     * Program entry point.
     * @param args String[]; not used
     * @throws SimRuntimeException ...
     * @throws NamingException ...
     */
    public static void main(final String[] args) throws SimRuntimeException, NamingException
    {
        DSOLProblem dsolProblem = new DSOLProblem();
        dsolProblem.execute();
    }

    /**
     * Demonstrate the problem.
     * @throws NamingException ...
     * @throws SimRuntimeException on error
     */
    public void execute() throws SimRuntimeException, NamingException
    {
        this.simulator = new OtsSimulator("DSOL problem");
        OtsModelInterface model = new DummyModel(this.simulator);
        this.simulator.initialize(Time.ZERO, Duration.ZERO, new Duration(1, DurationUnit.HOUR), model);
        Time eventTime = this.simulator.getSimulatorAbsTime().plus(new Duration(10, DurationUnit.SECOND));
        SimEvent<Duration> se = new SimEvent<>(new Duration(eventTime.minus(this.simulator.getStartTimeAbs())), this, this,
                "move", new Object[] {});
        this.simulator.scheduleEvent(se);
        double step = 0.01d;
        for (int i = 0;; i++)
        {
            Time stepTime = new Time(i * step, TimeUnit.BASE_SECOND);
            if (stepTime.gt(eventTime))
            {
                break;
            }
            if (stepTime.getSI() > 0.5)
            {
                step = 0.1; // Reduce testing time by increasing the step size
            }
            if (this.simulator.getSimulatorTime().si > 10)
            {
                System.out.println("This should not have happened");
                break;
            }
            System.out.println("Simulating until " + stepTime.getSI());
            this.simulator.runUpTo(stepTime);
            while (this.simulator.isStartingOrRunning())
            {
                try
                {
                    Thread.sleep(1);
                }
                catch (InterruptedException ie)
                {
                    ie = null; // ignore
                }
            }
            System.out.println("stepTime is " + stepTime);
            System.out.println("Simulator time is now " + this.simulator.getSimulatorTime());
            if (this.simulator.getSimulatorTime().si > 10)
            {
                System.out.println("This should not have happened");
                break;
            }
        }
    }

    /**
     * The event.
     */
    public final void move()
    {
        System.out.println("Move called - The current simulator time is " + this.simulator.getSimulatorTime());
    }
}

/**
 * Dummy OTSModelInterface.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
class DummyModel extends AbstractOtsModel
{
    /** */
    private static final long serialVersionUID = 20150114L;

    /**
     * @param simulator the simulator to use
     */
    DummyModel(final OtsSimulatorInterface simulator)
    {
        super(simulator);
    }

    /** {@inheritDoc} */
    @Override
    public final void constructModel() throws SimRuntimeException
    {
        //
    }

    /** {@inheritDoc} */
    @Override
    public final OtsRoadNetwork getNetwork()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Serializable getSourceId()
    {
        return "AbstractLaneBasedGtuTest.DummyModel";
    }

}
