package org.opentrafficsim.road.gtu.perception.mental;

import static org.opentrafficsim.base.parameters.constraint.NumericConstraint.POSITIVEZERO;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.djutils.exceptions.Throw;
import org.djutils.immutablecollections.Immutable;
import org.djutils.immutablecollections.ImmutableLinkedHashSet;
import org.djutils.immutablecollections.ImmutableSet;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeDouble;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.road.gtu.perception.LanePerception;

/**
 * Task-capability interface in accordance to Fuller (2011). Task demand is the sum of demands described by individual
 * {@code Task}s. These take exogenous information to describe the workload in fundamental relations. Task demand is divided by
 * task capability to arrive at a task saturation. Task saturation is input to {@code BehavioralAdaptation}s which alter
 * parameters describing personal traits, such as desired headway and desired speed. In this way, task demand is kept at an
 * equilibrium as described by Fuller.
 * <p>
 * A {@code BehavioralAdaptation} may also determine what the level of situational awareness is, which includes determining
 * reaction time. Both situational awareness and reaction time parameters can be used in perception to model deteriorated
 * perception due to a task demand imbalance.
 * <p>
 * Fuller, R., Driver control theory: From task difficulty homeostasis to risk allostasis, in Handbook of Traffic Psychology.
 * 2011. p. 13-26
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <T> task type
 */
public class SumFuller<T extends Task> extends Fuller
{

    /** Critical task saturation. */
    public static final ParameterTypeDouble TS_CRIT =
            new ParameterTypeDouble("TScrit", "Critical task saturation", 0.8, POSITIVEZERO)
            {
                @Override
                public void check(final Double value, final Parameters params) throws ParameterException
                {
                    Optional<Double> tsMax = params.getOptionalParameter(TS_MAX);
                    Throw.when(tsMax.isPresent() && value > tsMax.get(), ParameterException.class,
                            "Value for TS_CRIT should not be larger than TS_MAX.");
                }
            };

    /** Maximum task saturation, pertaining to maximum deterioration. */
    public static final ParameterTypeDouble TS_MAX =
            new ParameterTypeDouble("TSmax", "Maximum task saturation", 2.0, POSITIVEZERO)
            {
                @Override
                public void check(final Double value, final Parameters params) throws ParameterException
                {
                    Optional<Double> tsCrit = params.getOptionalParameter(TS_CRIT);
                    Throw.when(tsCrit.isPresent() && value < tsCrit.get(), ParameterException.class,
                            "Value for TS_MAX should not be smaller than TS_CRIT.");
                }
            };

    /** Tasks causing task demand. */
    private final Set<T> tasks;

    /**
     * Constructor with custom situational awareness.
     * @param tasks tasks
     * @param behavioralAdapatations behavioralAdapatations
     */
    public SumFuller(final Set<T> tasks, final Set<BehavioralAdaptation> behavioralAdapatations)
    {
        super(behavioralAdapatations);
        Throw.whenNull(tasks, "Tasks may not be null.");
        this.tasks = new LinkedHashSet<>();
        this.tasks.addAll(tasks);
    }

    @Override
    protected double getTotalTaskDemand(final LanePerception perception) throws ParameterException
    {
        double taskDemand = 0.0;
        for (T task : this.tasks)
        {
            taskDemand += task.getTaskDemand(perception);
        }
        return taskDemand;
    }

    /**
     * Adds a task.
     * @param task task to add
     */
    public void addTask(final T task)
    {
        this.tasks.add(task);
    }

    /**
     * Removes a task.
     * @param task task to remove
     */
    public void removeTask(final T task)
    {
        this.tasks.remove(task);
    }

    @Override
    public ImmutableSet<T> getTasks()
    {
        return new ImmutableLinkedHashSet<>(this.tasks, Immutable.WRAP);
    }

    @Override
    public String toString()
    {
        return "SumFuller [tasks=" + this.tasks + "]";
    }

}
