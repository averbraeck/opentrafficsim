package org.opentrafficsim.road.gtu.lane.perception.mental.sdm;

import java.rmi.RemoteException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.mental.Fuller;
import org.opentrafficsim.road.gtu.lane.perception.mental.Mental;
import org.opentrafficsim.road.gtu.lane.perception.mental.Task;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;
import nl.tudelft.simulation.event.EventInterface;
import nl.tudelft.simulation.event.EventListenerInterface;

/**
 * Stochastic Distraction Model by Manuel Lindorfer.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 28 jun. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class StochasticDistractionModel implements EventListenerInterface
{

    /** Whether to allow multi-tasking. */
    private final boolean allowMultiTasking;

    /** List of distractions. */
    private final List<Distraction> distractions;

    /** Simulator. */
    private final DEVSSimulatorInterface.TimeDoubleUnit simulator;

    /** Network. */
    private final OTSNetwork network;

    /** Set of distracted GTUs. */
    private final Set<String> distractedGTUs = new LinkedHashSet<>();

    /** Queue of distractions per GTU. */
    private final Map<String, Queue<Distraction>> distractionQueues = new LinkedHashMap<>();

    /**
     * Constructor. This model will react to GTU's being created in simulation and apply distractions.
     * @param allowMultiTasking boolean; whether to allow multi-tasking
     * @param distractions List&lt;Distraction&gt;; list of distractions
     * @param simulator DEVSSimulatorInterface.TimeDoubleUnit; simulator
     * @param network OTSNetwork; network
     */
    public StochasticDistractionModel(final boolean allowMultiTasking, final List<Distraction> distractions,
            final DEVSSimulatorInterface.TimeDoubleUnit simulator, final OTSNetwork network)
    {
        Throw.whenNull(distractions, "List of tasks may not be null.");
        Throw.whenNull(simulator, "Simulator may not be null.");
        Throw.whenNull(network, "Network may not be null.");
        this.allowMultiTasking = allowMultiTasking;
        this.distractions = distractions;
        this.simulator = simulator;
        this.network = network;
        network.addListener(this, Network.GTU_ADD_EVENT);
        network.addListener(this, Network.GTU_REMOVE_EVENT);
    }

    /**
     * Start a distraction.
     * @param gtu LaneBasedGTU; gtu to start the distraction on
     * @param distraction Distraction; distraction
     * @param scheduleNext boolean; whether to schedule the next distraction (not if starting from queue)
     * @throws SimRuntimeException on time error
     */
    public void startDistraction(final LaneBasedGTU gtu, final Distraction distraction, final boolean scheduleNext)
            throws SimRuntimeException
    {
        if (gtu.isDestroyed())
        {
            return;
        }
        String gtuId = gtu.getId();
        if (this.allowMultiTasking || !this.distractedGTUs.contains(gtuId))
        {
            // start the distraction now
            if (!this.allowMultiTasking)
            {
                this.distractedGTUs.add(gtuId);
            }
            Task task = distraction.getTask(gtu);
            ((Fuller) gtu.getTacticalPlanner().getPerception().getMental()).addTask(task);
            // stop the distraction
            this.simulator.scheduleEventRel(distraction.nextDuration(), this, this, "stopDistraction",
                    new Object[] { gtu, task });
        }
        else
        {
            // need to queue distraction
            if (!this.distractionQueues.containsKey(gtuId))
            {
                this.distractionQueues.put(gtuId, new LinkedList<>());
            }
            this.distractionQueues.get(gtuId).add(distraction);
        }
        if (scheduleNext)
        {
            // schedule next distraction
            this.simulator.scheduleEventRel(distraction.nextInterArrival(), this, this, "startDistraction",
                    new Object[] { gtu, distraction, true });
        }
    }

    /**
     * Stops a distraction task.
     * @param gtu LaneBasedGTU; gtu to stop the task for
     * @param task Task; task to stop
     * @throws SimRuntimeException on time error
     */
    public void stopDistraction(final LaneBasedGTU gtu, final Task task) throws SimRuntimeException
    {
        if (gtu.isDestroyed())
        {
            return;
        }
        String gtuId = gtu.getId();
        ((Fuller) gtu.getTacticalPlanner().getPerception().getMental()).removeTask(task);
        // start next distraction if any in queue
        if (!this.allowMultiTasking)
        {
            this.distractedGTUs.remove(gtuId);
            if (this.distractionQueues.containsKey(gtuId))
            {
                Queue<Distraction> queue = this.distractionQueues.get(gtuId);
                Distraction distraction = queue.poll();
                startDistraction(gtu, distraction, false);
                if (queue.isEmpty())
                {
                    this.distractionQueues.remove(gtuId);
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void notify(final EventInterface event) throws RemoteException
    {
        if (event.getType().equals(Network.GTU_ADD_EVENT))
        {
            // The GTU is not initialized yet, so we can't obtain the tactical planner
            String gtuId = (String) event.getContent();
            GTU gtu = this.network.getGTU(gtuId);
            gtu.addListener(this, GTU.INIT_EVENT);
        }
        else if (event.getType().equals(GTU.INIT_EVENT))
        {
            String gtuId = (String) ((Object[]) event.getContent())[0];
            LaneBasedGTU gtu = (LaneBasedGTU) this.network.getGTU(gtuId);
            Mental mental = gtu.getTacticalPlanner().getPerception().getMental();
            if (mental != null && mental instanceof Fuller)
            {
                for (Distraction distraction : this.distractions)
                {
                    if (distraction.nextExposure())
                    {
                        Try.execute(
                                () -> this.simulator.scheduleEventRel(distraction.nextInterArrival(), this, this,
                                        "startDistraction", new Object[] { gtu, distraction, true }),
                                "Exception while scheduling distraction start.");
                    }
                }
            }
        }
        else if (event.getType().equals(Network.GTU_REMOVE_EVENT))
        {
            String gtuId = (String) event.getContent();
            if (!this.allowMultiTasking)
            {
                this.distractedGTUs.remove(gtuId);
            }
            this.distractionQueues.remove(gtuId);
        }
    }

}
