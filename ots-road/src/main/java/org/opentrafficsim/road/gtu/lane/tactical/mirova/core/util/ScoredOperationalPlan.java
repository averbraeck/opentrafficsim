package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.util;

import java.io.Serializable;

import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ActionState;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPattern;

/**
 * Wrapper class that binds a simple operational plan to a specific utility score and its source.
 * <p>
 * This class is a core component of the MiRoVA arbitration layer. It allows the arbitrator
 * to evaluate multiple proposed plans while retaining the exact source pattern and state
 * that generated the winning proposal.
 * </p>
 * <p>
 * Copyright (c) 2026 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public class ScoredOperationalPlan implements Serializable, Comparable<ScoredOperationalPlan>
{

    /** Serial version UID for serialization. */
    private static final long serialVersionUID = 20260410L;

    /** The proposed operational plan containing the trajectory. */
    private final SimpleOperationalPlan operationalPlan;

    /** The calculated utility of this plan, typically derived from cognitive desires. */
    private final double utility;

    /** The maneuver pattern that generated this plan. */
    private final ManeuverPattern sourcePattern;

    /** The action state of the maneuver pattern that generated this plan. */
    private final ActionState sourceState;

    /**
     * Constructs a new scored operational plan.
     * * @param operationalPlan SimpleOperationalPlan; the physical plan proposed by the pattern
     * @param utility double; the evaluated utility or fitness score of this specific plan
     * @param sourcePattern ManeuverPattern; the pattern responsible for proposing this plan
     * @param sourceState ActionState; the exact internal state of the pattern at generation
     */
    public ScoredOperationalPlan(final SimpleOperationalPlan operationalPlan, final double utility,
                                 final ManeuverPattern sourcePattern, final ActionState sourceState)
    {
        this.operationalPlan = operationalPlan;
        this.utility = utility;
        this.sourcePattern = sourcePattern;
        this.sourceState = sourceState;
    }

    /**
     * Retrieves the underlying simple operational plan.
     * * @return SimpleOperationalPlan; the physical plan to be executed
     */
    public SimpleOperationalPlan getOperationalPlan()
    {
        return this.operationalPlan;
    }

    /**
     * Retrieves the utility score of this plan.
     * * @return double; the evaluated utility
     */
    public double getUtility()
    {
        return this.utility;
    }

    /**
     * Retrieves the maneuver pattern that generated this plan.
     * * @return ManeuverPattern; the source pattern
     */
    public ManeuverPattern getSourcePattern()
    {
        return this.sourcePattern;
    }

    /**
     * Retrieves the action state that generated this plan.
     * * @return ActionState; the source state
     */
    public ActionState getSourceState()
    {
        return this.sourceState;
    }

    /**
     * Compares this scored plan with another based on their utility.
     * <p>
     * Note: This method defines the natural ordering such that higher utilities are considered greater.
     * </p>
     * * @param other ScoredOperationalPlan; the other scored plan to compare against
     * @return int; a negative integer, zero, or a positive integer as this plan's utility is less than,
     * equal to, or greater than the specified plan's utility
     */
    @Override
    public int compareTo(final ScoredOperationalPlan other)
    {
        return Double.compare(this.utility, other.utility);
    }
}