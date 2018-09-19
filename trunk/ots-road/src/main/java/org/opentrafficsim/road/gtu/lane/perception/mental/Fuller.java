package org.opentrafficsim.road.gtu.lane.perception.mental;

import static org.opentrafficsim.base.parameters.constraint.NumericConstraint.POSITIVE;
import static org.opentrafficsim.base.parameters.constraint.NumericConstraint.POSITIVEZERO;

import java.util.Set;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeDouble;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.Try;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;

import nl.tudelft.simulation.language.Throw;

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
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 3 apr. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class Fuller implements Mental
{

    // Parameters

    /** Task capability in nominal task capability units, i.e. mean is 1. */
    public static final ParameterTypeDouble TC = new ParameterTypeDouble("TC", "Task capability", 1.0, POSITIVE);

    /** Critical task saturation. */
    public static final ParameterTypeDouble TS_CRIT =
            new ParameterTypeDouble("TScrit", "Critical task saturation", 0.8, POSITIVEZERO)
            {
                /** */
                private static final long serialVersionUID = 20180403L;

                /** {@inheritDoc} */
                @Override
                public void check(final Double value, final Parameters params) throws ParameterException
                {
                    Double tsMax = params.getParameterOrNull(TS_MAX);
                    Throw.when(tsMax != null && value > tsMax, ParameterException.class,
                            "Value for TS_CRIT should not be larger than TS_MAX.");
                }
            };

    /** Maximum task saturation, pertaining to maximum deterioration. */
    public static final ParameterTypeDouble TS_MAX =
            new ParameterTypeDouble("TSmax", "Maximum task saturation", 2.0, POSITIVEZERO)
            {
                /** */
                private static final long serialVersionUID = 20180403L;

                /** {@inheritDoc} */
                @Override
                public void check(final Double value, final Parameters params) throws ParameterException
                {
                    Double tsCrit = params.getParameterOrNull(TS_CRIT);
                    Throw.when(tsCrit != null && value < tsCrit, ParameterException.class,
                            "Value for TS_MAX should not be smaller than TS_CRIT.");
                }
            };

    /** Task saturation. */
    public static final ParameterTypeDouble TS = new ParameterTypeDouble("TS", "Task saturation", 0.0, POSITIVEZERO);

    // Properties

    /** Tasks causing task demand. */
    private final Set<Task> tasks;

    /** Behavioral adaptations depending on task saturation. */
    private final Set<BehavioralAdaptation> behavioralAdapatations;

    /**
     * Constructor with custom situational awareness.
     * @param tasks Set&lt;Task&gt;; tasks
     * @param behavioralAdapatations Set&lt;BehavioralAdaptation&gt;; behavioralAdapatations
     */
    public Fuller(final Set<Task> tasks, final Set<BehavioralAdaptation> behavioralAdapatations)
    {
        Throw.whenNull(tasks, "Tasks may not be null.");
        Throw.whenNull(behavioralAdapatations, "Behavioral adaptations may not be null.");
        this.tasks = tasks;
        this.behavioralAdapatations = behavioralAdapatations;
    }

    /**
     * Adds a task.
     * @param task Task; task to add
     */
    public void addTask(final Task task)
    {
        this.tasks.add(task);
    }
    
    /**
     * Removes a task.
     * @param task Task; task to remove
     */
    public void removeTask(final Task task)
    {
        this.tasks.remove(task);
    }
    
    /** {@inheritDoc} */
    @Override
    public void apply(final LanePerception perception) throws ParameterException, GTUException
    {
        LaneBasedGTU gtu = Try.assign(() -> perception.getGtu(), "Could not obtain GTU.");
        Parameters parameters = gtu.getParameters();
        double taskDemand = 0.0;
        // a) the fundamental diagrams of task workload are defined in the tasks
        // b) sum task demand
        for (Task task : this.tasks)
        {
            taskDemand += task.demand(perception, gtu, parameters);
        }
        double taskSaturation = taskDemand / parameters.getParameter(TC);
        if (taskSaturation < 0.0)
        {
            System.out.println("oh dear");
        }
        parameters.setParameter(TS, taskSaturation);
        // c) behavioral adaptation
        for (BehavioralAdaptation behavioralAdapatation : this.behavioralAdapatations)
        {
            behavioralAdapatation.adapt(parameters, taskSaturation);
        }
        // d) situational awareness can be implemented by one of the behavioral responses
        // e) perception errors from situational awareness are included in the perception step
        // f) reaction time from situational awareness are included in the perception step
    }

    /**
     * Interface for tasks, where each describes a fundamental relation between exogenous inputs causing a mental demand.
     * <p>
     * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 3 apr. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    @FunctionalInterface
    public interface Task
    {
        /**
         * Returns the demand of this task.
         * @param perception LanePerception; perception
         * @param gtu LaneBasedGTU; gtu
         * @param parameters Parameters; parameters
         * @return double; demand of this task
         * @throws ParameterException if a parameter is missing or out of bounds
         * @throws GTUException exceptions pertaining to the GTU
         */
        double demand(LanePerception perception, LaneBasedGTU gtu, Parameters parameters)
                throws ParameterException, GTUException;
        
        /**
         * Class for constant demand.
         */
        class Constant implements Task
        {
            /** Task demand. */
            private double taskDemand;
            
            /**
             * Constructor.
             * @param taskDemand double; task demand
             */
            public Constant(final double taskDemand)
            {
                this.taskDemand = taskDemand;
            }
            
            /** {@inheritDoc} */
            @Override
            public double demand(final LanePerception perception, final LaneBasedGTU gtu, final Parameters parameters)
                    throws ParameterException, GTUException
            {
                return this.taskDemand;
            }
        }
    }

    /**
     * Behavioral adaptation by changing parameter values.
     * <p>
     * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 3 apr. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    @FunctionalInterface
    public interface BehavioralAdaptation
    {
        /**
         * Adapt to task saturation by changing parameter values.
         * @param parameters Parameters; parameters
         * @param taskSaturation double; task saturation
         * @throws ParameterException if a parameter is missing or out of bounds
         */
        void adapt(Parameters parameters, double taskSaturation) throws ParameterException;
    }

}
