package org.opentrafficsim.imb.demo.generators;

import static org.opentrafficsim.core.gtu.GTUType.VEHICLE;

import java.util.HashMap;
import java.util.Map;

import org.djunits.value.ValueException;
import org.djunits.value.vdouble.scalar.Time;
import org.djunits.value.vdouble.vector.TimeVector;
import org.opentrafficsim.core.distributions.ProbabilityException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.network.route.RouteGenerator;

import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.language.Throw;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 18 nov. 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class RouteGeneratorProbability implements RouteGenerator
{

    /** Network. */
    private final OTSNetwork network;

    /** Vector of time. */
    private final TimeVector timeVector;

    /** Source node. */
    private final OTSNode from;

    /** Simulator. */
    private final SimulatorInterface.TimeDoubleUnit simulator;

    /** Stream name of GTU class generation. */
    private final static String GTU_ROUTE_STREAM = "gtuRoute";

    /** Demand map. */
    private final HashMap<OTSNode, Object> demandMap = new HashMap<>();

    /** Route map. */
    private final HashMap<OTSNode, Route> routeMap = new HashMap<>();

    /**
     * @param network the network
     * @param timeVector a time vector
     * @param from start node
     * @param simulator the simulator
     */
    public RouteGeneratorProbability(final OTSNetwork network, final TimeVector timeVector, final OTSNode from,
            final SimulatorInterface.TimeDoubleUnit simulator)
    {
        Throw.whenNull(network, "Network may not be null.");
        Throw.whenNull(timeVector, "Time vector may not be null.");
        Throw.whenNull(from, "From node may not be null.");
        Throw.whenNull(simulator, "Simulator may not be null.");
        Throw.whenNull(simulator.getReplication().getStream(GTU_ROUTE_STREAM),
                "Could not obtain random stream '" + GTU_ROUTE_STREAM + "'.");
        this.network = network;
        this.timeVector = timeVector;
        this.from = from;
        this.simulator = simulator;
    }

    /**
     * Add demand for route to 'to' node.
     * @param to destination node
     * @param demand demand
     */
    public void addDemand(final OTSNode to, final double[] demand)
    {
        this.demandMap.put(to, demand);
        try
        {
            this.routeMap.put(to, this.network.getShortestRouteBetween(VEHICLE, this.from, to));
        }
        catch (NullPointerException | NetworkException exception)
        {
            exception.printStackTrace();
        }
    }

    /** {@inheritDoc} */
    @Override
    public Route draw() throws ProbabilityException
    {
        Time time = this.simulator.getSimulatorTime();
        try
        {
            Throw.when(time.lt(this.timeVector.get(0)), IllegalArgumentException.class,
                    "Cannot return a headway at time before first time in vector.");

            // get time period of current time
            int i = 0;
            while (this.timeVector.get(i + 1).lt(time) && i < this.timeVector.size() - 1)
            {
                i++;
            }

            // escape if beyond specified time
            if (i == this.timeVector.size() - 1)
            {
                throw new RuntimeException("Cannot draw route for GTU after time period of data.");
            }

            // draw destination
            double sum = 0.0;
            Map<OTSNode, Double> probMap = new HashMap<>();
            for (OTSNode to : this.demandMap.keySet())
            {
                double p = ((double[]) this.demandMap.get(to))[i];
                sum += p;
                probMap.put(to, p);
            }
            Throw.when(sum == 0.0, RuntimeException.class, "Trying to draw route while no route has demand.");
            double cumul = 0.0;
            double r;
            r = this.simulator.getReplication().getStream(GTU_ROUTE_STREAM).nextDouble() * sum;
            for (OTSNode to : this.demandMap.keySet())
            {
                double p = probMap.get(to);
                if (r >= cumul && r <= cumul + p)
                {
                    return this.routeMap.get(to);
                }
                cumul += p;
            }

            throw new RuntimeException("Probability error, no route was drawn.");

        }
        catch (ValueException exception)
        {
            throw new RuntimeException(
                    "Value out of range of time or demand vector. Note that HeadwayGenerator does not create safe copies.",
                    exception);
        }
    }

}
