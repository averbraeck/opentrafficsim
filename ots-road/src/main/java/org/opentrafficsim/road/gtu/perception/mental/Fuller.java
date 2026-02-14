package org.opentrafficsim.road.gtu.perception.mental;

import static org.opentrafficsim.base.parameters.constraint.NumericConstraint.POSITIVE;
import static org.opentrafficsim.base.parameters.constraint.NumericConstraint.POSITIVEZERO;

import java.util.Set;

import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;
import org.djutils.immutablecollections.ImmutableSet;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeDouble;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.road.gtu.LaneBasedGtu;
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
 */
public abstract class Fuller implements Mental
{

    /** Task capability in nominal task capability units, i.e. mean is 1. */
    public static final ParameterTypeDouble TC = new ParameterTypeDouble("TC", "Task capability", 1.0, POSITIVE);

    /** Task saturation. */
    public static final ParameterTypeDouble TS = new ParameterTypeDouble("TS", "Task saturation", 0.0, POSITIVEZERO);

    /** Over-estimation parameter type. Negative values reflect under-estimation. */
    public static final ParameterTypeDouble OVER_EST = new ParameterTypeDouble("OVER_EST", "Over estimation factor.", 1.0);

    /** Behavioral adaptations depending on task saturation. */
    private final Set<BehavioralAdaptation> behavioralAdapatations;

    /**
     * Constructor with custom situational awareness.
     * @param behavioralAdapatations behavioralAdapatations
     */
    public Fuller(final Set<BehavioralAdaptation> behavioralAdapatations)
    {
        Throw.whenNull(behavioralAdapatations, "Behavioral adaptations may not be null.");
        this.behavioralAdapatations = behavioralAdapatations;
    }

    @Override
    public void apply(final LanePerception perception) throws ParameterException
    {
        LaneBasedGtu gtu = Try.assign(() -> perception.getGtu(), "Could not obtain GTU.");
        Parameters parameters = gtu.getParameters();
        // a) the fundamental diagrams of task workload are defined in the tasks
        // b) sum task demand (possibly with anticipation reliance in sub-class)
        parameters.setClaimedParameter(TS, getTotalTaskDemand(perception) / parameters.getParameter(TC), this);
        // c) behavioral adaptation
        for (BehavioralAdaptation behavioralAdapatation : this.behavioralAdapatations)
        {
            behavioralAdapatation.adapt(parameters);
        }
        // d) situational awareness can be implemented by one of the behavioral adaptations
        // e) perception errors from situational awareness or otherwise by sub-class and included in the perception step
        // f) reaction time from situational awareness or otherwise by sub-class and included in the perception step
    }

    /**
     * Returns the total level of task demand, possibly after anticipation reliance.
     * @param perception perception
     * @return level of task demand
     * @throws ParameterException if a parameter is missing or out of bounds
     */
    protected abstract double getTotalTaskDemand(LanePerception perception) throws ParameterException;

    /**
     * Returns the currently active tasks.
     * @return tasks
     */
    public abstract ImmutableSet<? extends Task> getTasks();

}
