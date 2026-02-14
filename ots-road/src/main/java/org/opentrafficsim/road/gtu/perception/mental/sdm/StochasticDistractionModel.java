package org.opentrafficsim.road.gtu.perception.mental.sdm;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;

import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.exceptions.Throw;
import org.djutils.multikeymap.MultiKeyMap;
import org.opentrafficsim.base.OtsRuntimeException;
import org.opentrafficsim.base.logger.Logger;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.road.gtu.LaneBasedGtu;
import org.opentrafficsim.road.gtu.perception.LanePerception;
import org.opentrafficsim.road.gtu.perception.mental.Fuller;
import org.opentrafficsim.road.gtu.perception.mental.Mental;
import org.opentrafficsim.road.gtu.perception.mental.SumFuller;
import org.opentrafficsim.road.gtu.perception.mental.Task;
import org.opentrafficsim.road.gtu.perception.mental.ar.ArTaskConstant;
import org.opentrafficsim.road.gtu.perception.mental.channel.ChannelFuller;
import org.opentrafficsim.road.gtu.perception.mental.channel.ChannelTask;
import org.opentrafficsim.road.gtu.perception.mental.channel.ChannelTaskConstant;
import org.opentrafficsim.road.network.RoadNetwork;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * Stochastic Distraction Model by Manuel Lindorfer.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class StochasticDistractionModel implements EventListener
{

    /** Whether to allow multi-tasking. */
    private final boolean allowMultiTasking;

    /** List of distractions. */
    private final List<Distraction> distractions;

    /** Network. */
    private final RoadNetwork network;

    /** GTU types. */
    private final Set<GtuType> gtuTypes;

    /** Set of distracted GTUs. */
    private final Set<String> distractedGTUs = new LinkedHashSet<>();

    /** Queue of distractions per GTU. */
    private final Map<String, Queue<Distraction>> distractionQueues = new LinkedHashMap<>();

    /** Task per GTU id and distraction id. */
    private final MultiKeyMap<Task> tasks = new MultiKeyMap<>(String.class, String.class, Task.class);

    /** Task per GTU id and distraction id. */
    private final MultiKeyMap<Function<LanePerception, Set<ChannelTask>>> taskSuppliers =
            new MultiKeyMap<>(String.class, String.class, Function.class);

    /**
     * Constructor. This model will react to GTU's being created in simulation and apply distractions.
     * @param allowMultiTasking whether to allow multi-tasking
     * @param distractions list of distractions
     * @param network network
     * @param gtuTypes GTU types to which the distractions apply
     */
    public StochasticDistractionModel(final boolean allowMultiTasking, final List<Distraction> distractions,
            final RoadNetwork network, final Set<GtuType> gtuTypes)
    {
        Throw.whenNull(distractions, "distractions");
        Throw.whenNull(network, "network");
        Throw.whenNull(gtuTypes, "gtuTypes");
        this.allowMultiTasking = allowMultiTasking;
        this.distractions = distractions;
        this.network = network;
        this.gtuTypes = gtuTypes;
        network.addListener(this, Network.GTU_ADD_EVENT);
        network.addListener(this, Network.GTU_REMOVE_EVENT);
    }

    /**
     * Start a distraction.
     * @param gtu gtu to start the distraction on
     * @param distraction distraction
     * @param scheduleNext whether to schedule the next distraction (not if starting from queue)
     * @throws SimRuntimeException on time error
     */
    @SuppressWarnings("unchecked")
    public void startDistraction(final LaneBasedGtu gtu, final Distraction distraction, final boolean scheduleNext)
            throws SimRuntimeException
    {
        if (gtu.isDestroyed())
        {
            return;
        }
        String gtuId = gtu.getId();
        if (this.allowMultiTasking || !this.distractedGTUs.contains(gtuId))
        {
            Optional<Mental> mental = gtu.getTacticalPlanner().getPerception().getMental();
            if (mental.isEmpty())
            {
                return;
            }
            if (mental.get() instanceof Fuller fuller)
            {
                // start the distraction now
                if (!this.allowMultiTasking)
                {
                    this.distractedGTUs.add(gtuId);
                }
                if (mental.get() instanceof SumFuller sumFuller)
                {
                    Task task = new ArTaskConstant(distraction.getId(), distraction.getTaskDemand());
                    ((SumFuller<Task>) sumFuller).addTask(task);
                    this.tasks.put(task, gtuId, distraction.getId());
                }
                else if (mental.get() instanceof ChannelFuller channelFuller)
                {
                    boolean internal = !DefaultDistraction.EXTERNAL_DISTRACTION.getId().equals(distraction.getId());
                    ChannelTask task = new ChannelTaskConstant(distraction.getId(),
                            internal ? ChannelTask.IN_VEHICLE : ChannelTask.FRONT, distraction.getTaskDemand());
                    Set<ChannelTask> set = Set.of(task);
                    Function<LanePerception, Set<ChannelTask>> taskSupplier = (perception) -> set;
                    channelFuller.addTaskSupplier(taskSupplier);
                    this.taskSuppliers.put(taskSupplier, gtuId, distraction.getId());
                }
                else
                {
                    Logger.ots().warn("Fuller implementation {} not supported by {}", fuller.getClass().getName(),
                            getClass().getName());
                }
                // stop the distraction
                this.network.getSimulator().scheduleEventRel(distraction.nextDuration(),
                        () -> stopDistraction(gtu, distraction.getId()));
            }
            else
            {
                return; // this vehicle (currently) does not use Fuller model
            }
        }
        else
        {
            // need to queue distraction
            this.distractionQueues.computeIfAbsent(gtuId, (id) -> new LinkedList<>()).add(distraction);
        }
        if (scheduleNext)
        {
            // schedule next distraction
            this.network.getSimulator().scheduleEventRel(distraction.nextInterArrival(),
                    () -> startDistraction(gtu, distraction, true));
        }
    }

    /**
     * Stops a distraction task.
     * @param gtu gtu to stop the task for
     * @param distractionId distraction id
     * @throws SimRuntimeException on time error
     */
    @SuppressWarnings("unchecked")
    public void stopDistraction(final LaneBasedGtu gtu, final String distractionId) throws SimRuntimeException
    {
        if (gtu.isDestroyed())
        {
            return;
        }
        boolean isFuller = false;
        String gtuId = gtu.getId();
        Optional<Mental> mental = gtu.getTacticalPlanner().getPerception().getMental();
        if (mental.isPresent() && mental.get() instanceof SumFuller sumFuller)
        {
            ((SumFuller<Task>) sumFuller).removeTask((Task) this.tasks.clear(gtuId, distractionId));
            isFuller = true;
        }
        else if (mental.isPresent() && mental.get() instanceof ChannelFuller channelFuller)
        {
            channelFuller.removeTaskSupplier(
                    (Function<LanePerception, Set<ChannelTask>>) this.taskSuppliers.clear(gtuId, distractionId));
            isFuller = true;
        }
        else
        {
            Logger.ots().warn("Disabling distraction " + distractionId + " on GTU " + gtuId
                    + ", but it (no longer) has a tactical planner with mental module that can do this.");
        }
        if (isFuller)
        {
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
        else
        {
            // this vehicle (currently) does not use Fuller model
            this.distractedGTUs.remove(gtuId);
            this.distractionQueues.remove(gtuId);
        }
    }

    @Override
    public void notify(final Event event)
    {
        if (event.getType().equals(Network.GTU_ADD_EVENT))
        {
            // The GTU is not initialized yet, so we can't obtain the tactical planner
            String gtuId = (String) event.getContent();
            Gtu gtu = this.network.getGTU(gtuId).orElseThrow(
                    () -> new OtsRuntimeException("Distraction event for GTU " + gtuId + " which is not in the network."));
            if (gtu instanceof LaneBasedGtu && this.gtuTypes.contains(gtu.getType()))
            {
                gtu.addListener(this, LaneBasedGtu.LANEBASED_MOVE_EVENT);
            }
        }
        else if (event.getType().equals(LaneBasedGtu.LANEBASED_MOVE_EVENT))
        {
            String gtuId = (String) ((Object[]) event.getContent())[0];
            LaneBasedGtu gtu = (LaneBasedGtu) this.network.getGTU(gtuId).get();
            Optional<Mental> mental = gtu.getTacticalPlanner().getPerception().getMental();
            if (mental.isPresent() && mental.get() instanceof Fuller)
            {
                for (Distraction distraction : this.distractions)
                {
                    if (distraction.nextExposure())
                    {
                        this.network.getSimulator().scheduleEventRel(distraction.nextInterArrival(),
                                () -> startDistraction(gtu, distraction, true));
                    }
                }
            }
            gtu.removeListener(this, LaneBasedGtu.LANEBASED_MOVE_EVENT);
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
