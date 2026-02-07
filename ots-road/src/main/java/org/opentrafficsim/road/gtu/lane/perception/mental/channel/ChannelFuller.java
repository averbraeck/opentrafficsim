package org.opentrafficsim.road.gtu.lane.perception.mental.channel;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

import org.djunits.value.vdouble.scalar.Duration;
import org.djutils.exceptions.Throw;
import org.djutils.immutablecollections.Immutable;
import org.djutils.immutablecollections.ImmutableLinkedHashSet;
import org.djutils.immutablecollections.ImmutableSet;
import org.opentrafficsim.base.logger.Logger;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeDouble;
import org.opentrafficsim.base.parameters.ParameterTypeDuration;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.base.parameters.constraint.DualBound;
import org.opentrafficsim.base.parameters.constraint.NumericConstraint;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.mental.BehavioralAdaptation;
import org.opentrafficsim.road.gtu.lane.perception.mental.FactorEstimation;
import org.opentrafficsim.road.gtu.lane.perception.mental.Fuller;

/**
 * Fuller implementation with perception channels. This is based on a set of task suppliers, which may either provide static
 * tasks (always the same) or a dynamic set of tasks (e.g. per conflicting road present). When relevant, task suppliers need to
 * map objects to channel keys when they are invoked to return the currently applicable channel tasks. For example mapping a
 * single conflict to a common key that refers to a channel based on a group of conflicts. In this way the correct perception
 * delay can be found when only knowing the single conflict, without knowing how it was grouped or what then defines the key.
 * <p>
 * Copyright (c) 2024-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class ChannelFuller extends Fuller implements ChannelMental
{

    /** Task capability in nominal task capability units, i.e. mean is 1. */
    public static final ParameterTypeDouble TC = Fuller.TC;

    /** Task saturation. */
    public static final ParameterTypeDouble TS = Fuller.TS;

    /** Over-estimation parameter type. Negative values reflect under-estimation. */
    public static final ParameterTypeDouble OVER_EST = Fuller.OVER_EST;

    /** Erroneous estimation factor on distance and speed difference. */
    public static final ParameterTypeDouble EST_FACTOR = FactorEstimation.EST_FACTOR;

    /** Level of attention, which is the maximum in the steady state of the Attention Matrix. */
    public static final ParameterTypeDouble ATT =
            new ParameterTypeDouble("ATT", "Attention (maximum of all channels).", 0.0, DualBound.UNITINTERVAL);

    /** Minimum perception delay. */
    public static final ParameterTypeDuration TAU_MIN = new ParameterTypeDuration("tau_min", "Minimum perception delay",
            Duration.ofSI(0.32), NumericConstraint.POSITIVEZERO)
    {
        /** {@inheritDoc} */
        @Override
        public void check(final Duration value, final Parameters params) throws ParameterException
        {
            Throw.when(params.contains(TAU_MAX) && params.getParameter(TAU_MAX).lt(value), ParameterException.class,
                    "Value of tau_max less smaller than tau_min.");

        }
    };

    /** Maximum perception delay. */
    public static final ParameterTypeDuration TAU_MAX = new ParameterTypeDuration("tau_max", "Maximum perception delay",
            Duration.ofSI(0.32 + 0.87), NumericConstraint.POSITIVE)
    {
        /** {@inheritDoc} */
        @Override
        public void check(final Duration value, final Parameters params) throws ParameterException
        {
            Throw.when(params.contains(TAU_MIN) && params.getParameter(TAU_MIN).gt(value), ParameterException.class,
                    "Value of tau_min is greater than tau_max.");
        }
    };

    /** Task suppliers. */
    private Set<Function<LanePerception, Set<ChannelTask>>> taskSuppliers = new LinkedHashSet<>();

    /** Set of tasks as derived from suppliers. */
    private Set<ChannelTask> tasks;

    /** Mappings from object to channel. */
    private Map<Object, Object> channelMapping = new LinkedHashMap<>();

    /** Stored perception delay per channel. */
    private Map<Object, Duration> perceptionDelay = new LinkedHashMap<>();

    /** Stored level of attention per channel. */
    private Map<Object, Double> attention = new LinkedHashMap<>();

    /**
     * Constructor.
     * @param taskSuppliers task suppliers.
     * @param behavioralAdapatations behavioral adaptations.
     */
    public ChannelFuller(final Collection<Function<LanePerception, Set<ChannelTask>>> taskSuppliers,
            final Set<BehavioralAdaptation> behavioralAdapatations)
    {
        super(behavioralAdapatations);
        this.taskSuppliers.addAll(taskSuppliers);
    }

    @Override
    protected double getTotalTaskDemand(final LanePerception perception) throws ParameterException
    {
        // Clear mappings
        this.channelMapping.clear();

        // Gather all channels and their maximum task demand
        Map<Object, Double> channelTaskDemand = new LinkedHashMap<>();
        Set<ChannelTask> gatheredTasks = new LinkedHashSet<>();
        for (Function<LanePerception, Set<ChannelTask>> taskFunction : this.taskSuppliers)
        {
            for (ChannelTask task : taskFunction.apply(perception)) // if applicable will (re)map objects to channel keys
            {
                double td = task.getTaskDemand(perception);
                if (td >= 1.0)
                {
                    td = 0.999;
                    Logger.ots().warn("Task {} produced task demand that is greater than, or equal to, 1.0.", task.getId());
                }
                channelTaskDemand.merge(task.getChannel(), td, Math::max); // map to max value
                gatheredTasks.add(task);
            }
        }
        this.tasks = gatheredTasks;

        // Apply attention matrix and couple channel to indices
        double[] tdArray = new double[channelTaskDemand.size()];
        int index = 0;
        double sumTaskDemand = 0.0;
        Map<Object, Integer> channelIndex = new LinkedHashMap<>();
        for (Entry<Object, Double> entry : channelTaskDemand.entrySet())
        {
            channelIndex.put(entry.getKey(), index);
            double td = entry.getValue();
            tdArray[index] = td;
            sumTaskDemand += td;
            index++;
        }
        AttentionMatrix matrix = new AttentionMatrix(tdArray);

        // Determine attention and perception delay per channel
        double maxAttention = 0.0;
        this.perceptionDelay.clear();
        this.attention.clear();
        Parameters parameters = perception.getGtu().getParameters();
        Duration tauMin = parameters.getParameter(TAU_MIN);
        Duration tauMax = parameters.getParameter(TAU_MAX);
        double tc = parameters.getParameter(TC);
        for (Entry<Object, Integer> entry : channelIndex.entrySet())
        {
            index = entry.getValue();
            this.perceptionDelay.put(entry.getKey(),
                    Duration.interpolate(tauMin, tauMax, matrix.getDeterioration(index)).divide(tc));
            double att = matrix.getAttention(index);
            maxAttention = Double.max(maxAttention, att);
            this.attention.put(entry.getKey(), att);
        }

        // Results
        double ts = sumTaskDemand / tc;
        parameters.setClaimedParameter(EST_FACTOR, Math.pow(Math.max(ts, 1.0), parameters.getParameter(OVER_EST)), this);
        parameters.setClaimedParameter(ATT, maxAttention, this);
        return sumTaskDemand;

        // super sets task saturation
        // super applies behavioral adaptations
    }

    @Override
    public ImmutableSet<ChannelTask> getTasks()
    {
        return new ImmutableLinkedHashSet<ChannelTask>(this.tasks, Immutable.WRAP);
    }

    /**
     * Add task supplier.
     * @param taskSupplier task supplier to add
     */
    public void addTaskSupplier(final Function<LanePerception, Set<ChannelTask>> taskSupplier)
    {
        this.taskSuppliers.add(taskSupplier);
    }

    /**
     * Remove task supplier.
     * @param taskSupplier task supplier to remove
     */
    public void removeTaskSupplier(final Function<LanePerception, Set<ChannelTask>> taskSupplier)
    {
        this.taskSuppliers.remove(taskSupplier);
    }

    @Override
    public Duration getPerceptionDelay(final Object obj)
    {
        return this.perceptionDelay.get(getChannel(obj));
    }

    @Override
    public double getAttention(final Object obj)
    {
        return this.attention.get(getChannel(obj));
    }

    @Override
    public void mapToChannel(final Object obj, final Object channel)
    {
        this.channelMapping.put(obj, channel);
    }

    /**
     * Returns the relevant channel key for the object. This is a channel key mapped to the object, or the object itself if
     * there is no such mapping (in which case the object should itself directly be a channel key).
     * @param obj object.
     * @return relevant channel key for the object.
     */
    private Object getChannel(final Object obj)
    {
        if (this.channelMapping.containsKey(obj))
        {
            return this.channelMapping.get(obj);
        }
        Throw.when(!this.perceptionDelay.containsKey(obj), IllegalArgumentException.class, "Channel %s is not present.", obj);
        return obj;
    }

    /**
     * Returns the current channels.
     * @return set of channels
     */
    public Set<Object> getChannels()
    {
        return new LinkedHashSet<>(this.attention.keySet());
    }

}
