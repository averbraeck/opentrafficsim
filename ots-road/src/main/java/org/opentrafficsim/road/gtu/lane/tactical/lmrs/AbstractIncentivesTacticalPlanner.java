package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import java.util.LinkedHashSet;

import org.djutils.immutablecollections.Immutable;
import org.djutils.immutablecollections.ImmutableLinkedHashSet;
import org.djutils.immutablecollections.ImmutableSet;
import org.opentrafficsim.base.parameters.ParameterTypeClassList;
import org.opentrafficsim.base.parameters.constraint.ClassCollectionConstraint;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.tactical.AbstractLaneBasedTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.MandatoryIncentive;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.VoluntaryIncentive;

/**
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public abstract class AbstractIncentivesTacticalPlanner extends AbstractLaneBasedTacticalPlanner
{

    /** */
    private static final long serialVersionUID = 20190731L;

    /**
     * Constructor.
     * @param carFollowingModel CarFollowingModel; Car-following model.
     * @param gtu LaneBasedGtu; GTU
     * @param lanePerception LanePerception; perception
     */
    public AbstractIncentivesTacticalPlanner(final CarFollowingModel carFollowingModel, final LaneBasedGTU gtu,
            final LanePerception lanePerception)
    {
        super(carFollowingModel, gtu, lanePerception);
    }

    /** Parameter type for mandatory lane change incentives. */
    public static final ParameterTypeClassList<MandatoryIncentive> MANDATORY = new ParameterTypeClassList<>("man.incent.",
            "Mandatory lane-change incentives.", ParameterTypeClassList.getValueClass(MandatoryIncentive.class));

    /** Parameter type for voluntary lane change incentives. */
    public static final ParameterTypeClassList<VoluntaryIncentive> VOLUNTARY = new ParameterTypeClassList<>("vol.incent.",
            "Voluntary lane-change incentives.", ParameterTypeClassList.getValueClass(VoluntaryIncentive.class));

    /** Parameter type for acceleration incentives. */
    public static final ParameterTypeClassList<AccelerationIncentive> ACCELERATION = new ParameterTypeClassList<>("acc.incent.",
            "Acceleration incentives.", ParameterTypeClassList.getValueClass(AccelerationIncentive.class),
            ClassCollectionConstraint.newInstance(AccelerationBusStop.class));

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
     * Adds a mandatory incentive. Ignores {@code null}.
     * @param incentive MandatoryIncentive; Incentive to add.
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
     * @param incentive VoluntaryIncentive; Incentive to add.
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
     * @param incentive AccelerationIncentive; Incentive to add.
     */
    public final void addAccelerationIncentive(final AccelerationIncentive incentive)
    {
        if (incentive != null)
        {
            this.accelerationIncentives.add(incentive);
        }
    }

    /**
     * Sets the default lane change incentives.
     */
    public final void setDefaultIncentives()
    {
        this.mandatoryIncentives.clear();
        this.voluntaryIncentives.clear();
        this.accelerationIncentives.clear();
        this.mandatoryIncentives.add(new IncentiveRoute());
        // this.mandatoryIncentives.add(new IncentiveGetInLane());
        this.voluntaryIncentives.add(new IncentiveSpeedWithCourtesy());
        this.voluntaryIncentives.add(new IncentiveKeep());
        this.voluntaryIncentives.add(new IncentiveQueue());
        this.accelerationIncentives.add(new AccelerationSpeedLimitTransition());
        this.accelerationIncentives.add(new AccelerationTrafficLights());
        this.accelerationIncentives.add(new AccelerationConflicts());
    }

    /**
     * Returns the mandatory incentives.
     * @return ImmutableSet&lt;MandatoryIncentive&gt;; set of mandatory incentives
     */
    public final ImmutableSet<MandatoryIncentive> getMandatoryIncentives()
    {
        return this.immutableMandatoryIncentives;
    }

    /**
     * Returns the voluntary incentives.
     * @return ImmutableSet&lt;VoluntaryIncentive&gt;; set of voluntary incentives
     */
    public final ImmutableSet<VoluntaryIncentive> getVoluntaryIncentives()
    {
        return this.immutableVoluntaryIncentives;
    }

    /**
     * Returns the acceleration incentives.
     * @return ImmutableSet&lt;AccelerationIncentive&gt;; set of acceleration incentives
     */
    public final ImmutableSet<AccelerationIncentive> getAccelerationIncentives()
    {
        return this.immutableAccelerationIncentives;
    }

}
