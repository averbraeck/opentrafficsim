package org.sim0mq.test;

import java.rmi.RemoteException;

import nl.tudelft.simulation.dsol.DSOLModel;
import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.formalisms.Resource;
import nl.tudelft.simulation.dsol.formalisms.flow.Delay;
import nl.tudelft.simulation.dsol.formalisms.flow.Generator;
import nl.tudelft.simulation.dsol.formalisms.flow.Release;
import nl.tudelft.simulation.dsol.formalisms.flow.Seize;
import nl.tudelft.simulation.dsol.formalisms.flow.StationInterface;
import nl.tudelft.simulation.dsol.formalisms.flow.statistics.Utilization;
import nl.tudelft.simulation.dsol.simtime.SimTimeDouble;
import nl.tudelft.simulation.dsol.simtime.dist.DistContinuousSimTime;
import nl.tudelft.simulation.dsol.simtime.dist.DistContinuousTime;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.dsol.statistics.Tally;
import nl.tudelft.simulation.jstats.distributions.DistConstant;
import nl.tudelft.simulation.jstats.distributions.DistDiscreteConstant;
import nl.tudelft.simulation.jstats.distributions.DistExponential;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * The M/M/1 example as published in Simulation Modeling and Analysis by A.M. Law &amp; W.D. Kelton section 1.4 and 2.4.
 * <p>
 * (c) copyright 2002-2016 <a href="http://www.simulation.tudelft.nl">Delft University of Technology </a>, the
 * Netherlands. <br>
 * See for project information <a href="http://www.simulation.tudelft.nl">www.simulation.tudelft.nl </a> <br>
 * License of use: <a href="http://www.gnu.org/copyleft/lesser.html">Lesser General Public License (LGPL) </a>, no
 * warranty.
 * @version 2.0 21.09.2003 <br>
 * @author <a href="https://www.linkedin.com/in/peterhmjacobs">Peter Jacobs </a>
 */
public class MM1Queue41Model implements DSOLModel.TimeDouble
{
    /** The default serial version UID for serializable classes. */
    private static final long serialVersionUID = 1L;

    /** the simulator. */
    private DEVSSimulatorInterface.TimeDouble devsSimulator;

    /** tally dN. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Tally<Double, Double, SimTimeDouble> dN;
    
    /** tally qN. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Tally<Double, Double, SimTimeDouble> qN;
    
    /** utilization uN. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Utilization uN;
    
    /** {@inheritDoc} */
    @Override
    public final void constructModel(final SimulatorInterface<Double, Double, SimTimeDouble> simulator)
            throws SimRuntimeException, RemoteException
    {
        this.devsSimulator = (DEVSSimulatorInterface.TimeDouble) simulator;
        StreamInterface defaultStream = new MersenneTwister();

        // The Generator
        Generator.TimeDouble generator = new Generator.TimeDouble(this.devsSimulator, Object.class, null);
        generator.setInterval(new DistContinuousTime.TimeDouble(new DistExponential(defaultStream, 1.0)));
        generator.setStartTime(new DistContinuousSimTime.TimeDouble(new DistConstant(defaultStream, 0.0)));
        generator.setBatchSize(new DistDiscreteConstant(defaultStream, 1));
        generator.setMaxNumber(1000);

        // The queue, the resource and the release
        Resource<Double, Double, SimTimeDouble> resource = new Resource<>(this.devsSimulator, 1.0);

        // created a resource
        StationInterface queue = new Seize.TimeDouble(this.devsSimulator, resource);
        StationInterface release = new Release.TimeDouble(this.devsSimulator, resource, 1.0);

        // The server
        DistContinuousTime.TimeDouble serviceTime =
                new DistContinuousTime.TimeDouble(new DistExponential(defaultStream, 0.5));
        StationInterface server = new Delay.TimeDouble(this.devsSimulator, serviceTime);

        // The flow
        generator.setDestination(queue);
        queue.setDestination(server);
        server.setDestination(release);

        // Statistics
        this.dN = new Tally<>("d(n)", this.devsSimulator, queue, Seize.DELAY_TIME);
        this.qN = new Tally<>("q(n)", this.devsSimulator, queue, Seize.QUEUE_LENGTH_EVENT);
        this.uN = new Utilization("u(n)", this.devsSimulator, server);
    }

    /** {@inheritDoc} */
    @Override
    public final SimulatorInterface.TimeDouble getSimulator()
    {
        return this.devsSimulator;
    }
}
