package org.opentrafficsim.road.definitions;

import java.io.Serializable;

import javax.naming.NamingException;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.dsol.AbstractOTSModel;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimulator;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.road.network.OTSRoadNetwork;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEvent;
import nl.tudelft.simulation.dsol.simtime.SimTimeDoubleUnit;

/**
 * Demonstration of problem.
 * @author pknoppers
 */
public class DSOLProblem
{
    /** The simulation engine. */
    private OTSSimulatorInterface simulator;

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
     * @throws SimRuntimeException 
     */
    public void execute() throws SimRuntimeException, NamingException
    {
        this.simulator = new OTSSimulator("DSOL problem");
        OTSModelInterface model = new DummyModel(simulator);
        this.simulator.initialize(Time.ZERO, Duration.ZERO, new Duration(1, DurationUnit.HOUR), model);
        Time eventTime = this.simulator.getSimulatorTime().plus(new Duration(10, DurationUnit.SECOND));
        SimEvent<SimTimeDoubleUnit> se = new SimEvent<>(new SimTimeDoubleUnit(eventTime),
                this, this, "move", new Object[] {});
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
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, @version $Revision: 1401 $, by $Author: averbraeck $,
 * initial version 4 jan. 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
class DummyModel extends AbstractOTSModel
{
    /** */
    private static final long serialVersionUID = 20150114L;

    /**
     * @param simulator the simulator to use
     */
    DummyModel(final OTSSimulatorInterface simulator)
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
    public final OTSRoadNetwork getNetwork()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Serializable getSourceId()
    {
        return "AbstractLaneBasedGTUTest.DummyModel";
    }

}

