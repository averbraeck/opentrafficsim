package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import java.util.LinkedHashSet;

import org.djutils.immutablecollections.Immutable;
import org.djutils.immutablecollections.ImmutableLinkedHashSet;
import org.djutils.immutablecollections.ImmutableSet;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.tactical.AbstractLaneBasedTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.MandatoryIncentive;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.VoluntaryIncentive;

/**
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public abstract class AbstractIncentivesTacticalPlanner extends AbstractLaneBasedTacticalPlanner
{

    /** */
    private static final long serialVersionUID = 20190731L;

    /** Set of mandatory lane change incentives. */
    private final LinkedHashSet<MandatoryIncentive> mandatoryIncentives = new LinkedHashSet<>();

    /** Set of voluntary lane change incentives. */
    private final LinkedHashSet<VoluntaryIncentive> voluntaryIncentives = new LinkedHashSet<>();

    /** Set of acceleration incentives. */
    private final LinkedHashSet<AccelerationIncentive> accelerationIncentives = new LinkedHashSet<>();

    /** Immutable set of mandatory lane change incentives. */
    private final ImmutableSet<MandatoryIncentive> immutableMandatoryIncentives =
            new ImmutableLinkedHashSet<>(this.mandatoryIncentives, Immutable.WRAP);

    /** Immutable set of voluntary lane change incentives. */
    private final ImmutableSet<VoluntaryIncentive> immutableVoluntaryIncentives =
            new ImmutableLinkedHashSet<>(this.voluntaryIncentives, Immutable.WRAP);

    /** Immutable set of acceleration lane change incentives. */
    private final ImmutableSet<AccelerationIncentive> immutableAccelerationIncentives =
            new ImmutableLinkedHashSet<>(this.accelerationIncentives, Immutable.WRAP);

    /**
     * Constructor.
     * @param carFollowingModel Car-following model.
     * @param gtu GTU
     * @param lanePerception perception
     */
    public AbstractIncentivesTacticalPlanner(final CarFollowingModel carFollowingModel, final LaneBasedGtu gtu,
            final LanePerception lanePerception)
    {
        super(carFollowingModel, gtu, lanePerception);
    }

    /**
     * Adds a mandatory incentive. Ignores {@code null}.
     * @param incentive Incentive to add.
     */
    public final void addMandatoryIncentive(final MandatoryIncentive incentive)
    {
        if (incentive != null)
        {
            this.mandatoryIncentives.add(incentive);
        }
    }

    /**
     * Adds a voluntary incentive. Ignores {@code null}.
     * @param incentive Incentive to add.
     */
    public final void addVoluntaryIncentive(final VoluntaryIncentive incentive)
    {
        if (incentive != null)
        {
            this.voluntaryIncentives.add(incentive);
        }
    }

    /**
     * Adds an acceleration incentive. Ignores {@code null}.
     * @param incentive Incentive to add.
     */
    public final void addAccelerationIncentive(final AccelerationIncentive incentive)
    {
        if (incentive != null)
        {
            this.accelerationIncentives.add(incentive);
        }
    }

    /**
     * Returns the mandatory incentives.
     * @return set of mandatory incentives
     */
    public final ImmutableSet<MandatoryIncentive> getMandatoryIncentives()
    {
        return this.immutableMandatoryIncentives;
    }

    /**
     * Returns the voluntary incentives.
     * @return set of voluntary incentives
     */
    public final ImmutableSet<VoluntaryIncentive> getVoluntaryIncentives()
    {
        return this.immutableVoluntaryIncentives;
    }

    /**
     * Returns the acceleration incentives.
     * @return set of acceleration incentives
     */
    public final ImmutableSet<AccelerationIncentive> getAccelerationIncentives()
    {
        return this.immutableAccelerationIncentives;
    }

}
