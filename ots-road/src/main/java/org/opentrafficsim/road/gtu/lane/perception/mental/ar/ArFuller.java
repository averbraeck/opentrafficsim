package org.opentrafficsim.road.gtu.lane.perception.mental.ar;

import java.util.Set;

import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeDouble;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.base.parameters.constraint.DualBound;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.mental.BehavioralAdaptation;
import org.opentrafficsim.road.gtu.lane.perception.mental.SumFuller;

/**
 * Extends Fuller with the concept of anticipation reliance (AR).
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class ArFuller extends SumFuller<ArTask>
{

    /** Fraction of primary task that can be reduced by anticipation reliance. */
    public static final ParameterTypeDouble ALPHA = new ParameterTypeDouble("alpha",
            "Fraction of primary task that can be reduced by anticipation reliance.", 0.8, DualBound.UNITINTERVAL);

    /** Fraction of auxiliary tasks that can be reduced by anticipation reliance. */
    public static final ParameterTypeDouble BETA = new ParameterTypeDouble("beta",
            "Fraction of auxiliary tasks that can be reduced by anticipation reliance.", 0.6, DualBound.UNITINTERVAL);

    /** Primary task id. */
    private final String primaryTaskId;

    /**
     * Constructor.
     * @param tasks tasks
     * @param behavioralAdapatations behavioralAdapatations
     * @param primaryTaskId String; primary task id
     */
    public ArFuller(final Set<ArTask> tasks, final Set<BehavioralAdaptation> behavioralAdapatations, final String primaryTaskId)
    {
        super(tasks, behavioralAdapatations);
        this.primaryTaskId = primaryTaskId;
    }

    @Override
    protected double getTotalTaskDemand(final LanePerception perception) throws ParameterException
    {
        manage(perception);
        return getTasks().stream().mapToDouble((t) -> t.getTaskDemand() - t.getAnticipationReliance()).sum();
    }

    /**
     * Manage task demand and anticipation reliance levels.
     * @param perception perception
     * @throws ParameterException if a parameter is missing or out of bounds
     */
    // protected such that it may be overridden
    protected void manage(final LanePerception perception) throws ParameterException
    {
        ArTask primary = null;
        for (ArTask task : getTasks())
        {
            if (task.getId().equals(this.primaryTaskId))
            {
                primary = task;
                break;
            }
        }
        Throw.whenNull(primary, "There is no primary task with id '%s'.", this.primaryTaskId);
        double primaryTaskDemand = primary.getTaskDemand(perception);
        // max AR is alpha of TD, actual AR approaches 0 for increasing TD
        Parameters parameters = perception.getGtu().getParameters();
        double a = parameters.getParameter(ALPHA);
        double b = parameters.getParameter(BETA);
        primary.setAnticipationReliance(a * primaryTaskDemand * (1.0 - primaryTaskDemand));
        for (ArTask auxiliary : getTasks())
        {
            if (!auxiliary.getId().equals(this.primaryTaskId))
            {
                double auxiliaryTaskLoad = auxiliary.getTaskDemand(perception);
                // max AR is beta of TD, actual AR approaches 0 as primary TD approaches 0
                auxiliary.setAnticipationReliance(b * auxiliaryTaskLoad * primaryTaskDemand);
            }
        }
    }

    @Override
    public String toString()
    {
        return "ArFuller [tasks=" + getTasks() + "]";
    }

}
