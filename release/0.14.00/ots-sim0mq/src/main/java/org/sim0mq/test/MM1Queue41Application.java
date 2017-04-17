package org.sim0mq.test;

import java.rmi.RemoteException;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.experiment.Replication;
import nl.tudelft.simulation.dsol.experiment.ReplicationMode;
import nl.tudelft.simulation.dsol.simtime.SimTimeDouble;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulator;

/**
 * <p>
 * Copyright (c) 2002-2014 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved.
 * <p>
 * See for project information <a href="http://www.simulation.tudelft.nl/"> www.simulation.tudelft.nl</a>.
 * <p>
 * The DSOL project is distributed under a BSD-style license.<br>
 * @version Aug 15, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class MM1Queue41Application
{
    /** */
    private DEVSSimulator.TimeDouble simulator;

    /** */
    private MM1Queue41Model model;

    /**
     * Construct a console application.
     * @throws SimRuntimeException on error
     * @throws RemoteException on error
     * @throws NamingException on error
     */
    protected MM1Queue41Application() throws SimRuntimeException, RemoteException, NamingException
    {
        this.model = new MM1Queue41Model();
        this.simulator = new DEVSSimulator.TimeDouble();
        Replication<Double, Double, SimTimeDouble> replication =
                new Replication<>("rep1", new SimTimeDouble(0.0), 0.0, 100.0, this.model);
        this.simulator.initialize(replication, ReplicationMode.TERMINATING);
        this.simulator.scheduleEventAbs(100.0, this, this, "terminate", null);
        this.simulator.start();
    }

    /** stop the simulation. */
    protected final void terminate()
    {
        System.out.println("average queue length = " + this.model.qN.getSampleMean());
        System.out.println("average queue wait   = " + this.model.dN.getSampleMean());
        System.out.println("average utilization  = " + this.model.uN.getSampleMean());
        System.exit(0);
    }

    /**
     * @param args can be left empty
     * @throws SimRuntimeException on error
     * @throws RemoteException on error
     * @throws NamingException on error
     */
    public static void main(final String[] args) throws SimRuntimeException, RemoteException, NamingException
    {
        new MM1Queue41Application();
    }

}
