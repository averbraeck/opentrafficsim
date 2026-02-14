package org.opentrafficsim.road.gtu.tactical.lmrs;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djutils.immutablecollections.Immutable;
import org.djutils.immutablecollections.ImmutableLinkedHashMap;
import org.djutils.immutablecollections.ImmutableLinkedHashSet;
import org.djutils.immutablecollections.ImmutableMap;
import org.djutils.immutablecollections.ImmutableSet;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.LaneBasedGtu;
import org.opentrafficsim.road.gtu.perception.LanePerception;
import org.opentrafficsim.road.gtu.perception.RelativeLane;
import org.opentrafficsim.road.gtu.tactical.AbstractLaneBasedTacticalPlanner;
import org.opentrafficsim.road.gtu.tactical.TacticalContextEgo;
import org.opentrafficsim.road.gtu.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.tactical.util.lmrs.Desire;
import org.opentrafficsim.road.gtu.tactical.util.lmrs.Incentive;
import org.opentrafficsim.road.gtu.tactical.util.lmrs.MandatoryIncentive;
import org.opentrafficsim.road.gtu.tactical.util.lmrs.VoluntaryIncentive;

/**
 * Tactical planner with mandatory lane change incentives, voluntary lane change incentives, and acceleration incentives. The
 * order in which the incentives are given is relevant. A later incentive can request the result from an earlier incentive at
 * the same time instance. Otherwise, it can only obtain the value of another incentive from the previous time step.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public abstract class AbstractIncentivesTacticalPlanner extends AbstractLaneBasedTacticalPlanner
        implements DesireBased, AccelerationBased
{

    /** Set of mandatory lane change incentives. */
    private final LinkedHashSet<MandatoryIncentive> mandatoryIncentives = new LinkedHashSet<>();

    /** Set of voluntary lane change incentives. */
    private final LinkedHashSet<VoluntaryIncentive> voluntaryIncentives = new LinkedHashSet<>();

    /** Set of acceleration incentives. */
    private final LinkedHashSet<AccelerationIncentive> accelerationIncentives = new LinkedHashSet<>();

    /** Immutable set of mandatory lane change incentives. */
    private ImmutableSet<MandatoryIncentive> mandatoryIncentivesImmutable =
            new ImmutableLinkedHashSet<>(this.mandatoryIncentives, Immutable.WRAP);

    /** Immutable set of voluntary lane change incentives. */
    private ImmutableSet<VoluntaryIncentive> voluntaryIncentivesImmutable =
            new ImmutableLinkedHashSet<>(this.voluntaryIncentives, Immutable.WRAP);

    /** Immutable set of acceleration incentives. */
    private ImmutableSet<AccelerationIncentive> accelerationIncentivesImmutable =
            new ImmutableLinkedHashSet<>(this.accelerationIncentives, Immutable.WRAP);

    /** Map of mandatory desire. */
    private Map<Class<? extends MandatoryIncentive>, Desire> mandatoryDesire = new LinkedHashMap<>();

    /** Map of voluntary desire. */
    private Map<Class<? extends VoluntaryIncentive>, Desire> voluntaryDesire = new LinkedHashMap<>();

    /** Map of acceleration. */
    private Map<Class<? extends AccelerationIncentive>, Acceleration> acceleration = new LinkedHashMap<>();

    /** Immutable map of mandatory desire. */
    private ImmutableMap<Class<? extends MandatoryIncentive>, Desire> mandatoryDesireImmutable =
            new ImmutableLinkedHashMap<>(this.mandatoryDesire, Immutable.WRAP);

    /** Immutable map of voluntary desire. */
    private ImmutableMap<Class<? extends VoluntaryIncentive>, Desire> voluntaryDesireImmutable =
            new ImmutableLinkedHashMap<>(this.voluntaryDesire, Immutable.WRAP);

    /** Latest total mandatory desire. */
    private Desire mandatory;

    /** Latest total voluntary desire. */
    private Desire voluntary;

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
    public void addMandatoryIncentive(final MandatoryIncentive incentive)
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
    public void addVoluntaryIncentive(final VoluntaryIncentive incentive)
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
    public void addAccelerationIncentive(final AccelerationIncentive incentive)
    {
        if (incentive != null)
        {
            this.accelerationIncentives.add(incentive);
        }
    }

    /**
     * Returns the mandatory incentives.
     * @return mandatory incentives
     */
    public ImmutableSet<MandatoryIncentive> getMandatoryIncentives()
    {
        return this.mandatoryIncentivesImmutable;
    }

    /**
     * Returns the voluntary incentives.
     * @return voluntary incentives
     */
    public ImmutableSet<VoluntaryIncentive> getVoluntaryIncentives()
    {
        return this.voluntaryIncentivesImmutable;
    }

    /**
     * Returns the acceleration incentives.
     * @return acceleration incentives
     */
    public ImmutableSet<AccelerationIncentive> getAccelerationIncentives()
    {
        return this.accelerationIncentivesImmutable;
    }

    /**
     * Determines level of lane change desire for mandatory lane change incentives.
     * @param context tactical information such as parameters and car-following model
     * @return level of lane change desire for this incentive
     * @throws ParameterException if a parameter is not given or out of bounds
     * @throws OperationalPlanException in case of a perception exception
     */
    public Desire getMandatoryDesire(final TacticalContextEgo context) throws OperationalPlanException, ParameterException
    {
        double dLeftMandatory = 0.0;
        double dRightMandatory = 0.0;
        for (MandatoryIncentive incentive : this.mandatoryIncentives)
        {
            Desire d = incentive.determineDesire(context, this.mandatoryDesireImmutable);
            this.mandatoryDesire.put(incentive.getClass(), d);
            dLeftMandatory = Math.abs(d.left()) > Math.abs(dLeftMandatory) ? d.left() : dLeftMandatory;
            dRightMandatory = Math.abs(d.right()) > Math.abs(dRightMandatory) ? d.right() : dRightMandatory;
        }
        this.mandatory = new Desire(dLeftMandatory, dRightMandatory);
        return this.mandatory;
    }

    /**
     * Determines level of lane change desire for voluntary lane change incentives.
     * @param context tactical information such as parameters and car-following model
     * @return level of lane change desire for this incentive
     * @throws ParameterException if a parameter is not given or out of bounds
     * @throws OperationalPlanException in case of a perception exception
     */
    public Desire getVoluntaryDesire(final TacticalContextEgo context) throws OperationalPlanException, ParameterException
    {
        double dLeftVoluntary = 0;
        double dRightVoluntary = 0;
        for (VoluntaryIncentive incentive : this.voluntaryIncentives)
        {
            Desire d = incentive.determineDesire(context, this.mandatory, this.voluntaryDesireImmutable);
            this.voluntaryDesire.put(incentive.getClass(), d);
            dLeftVoluntary += d.left();
            dRightVoluntary += d.right();
        }
        this.voluntary = new Desire(dLeftVoluntary, dRightVoluntary);
        return this.voluntary;
    }

    /**
     * Returns the latest total mandatory desire.
     * @return latest total mandatory desire
     */
    public Desire getLatestMandatoryDesire()
    {
        return this.mandatory;
    }

    /**
     * Returns the latest total voluntary desire.
     * @return latest total voluntary desire
     */
    public Desire getLatestVoluntaryDesire()
    {
        return this.voluntary;
    }

    @Override
    public Optional<Desire> getLatestDesire(final Class<? extends Incentive> incentive)
    {
        if (this.voluntaryDesire.containsKey(incentive))
        {
            return Optional.ofNullable(this.voluntaryDesire.get(incentive));
        }
        return Optional.ofNullable(this.mandatoryDesire.get(incentive));
    }

    /**
     * Determine acceleration.
     * @param context tactical information such as parameters and car-following model
     * @param lane lane on which to consider the acceleration
     * @param mergeDistance distance over which a lane change is impossible towards the lane, zero if current lane
     * @return acceleration
     * @throws ParameterException on missing parameter
     * @throws GtuException when there is a problem with the state of the GTU when planning a path
     */
    public Acceleration getAcceleration(final TacticalContextEgo context, final RelativeLane lane, final Length mergeDistance)
            throws ParameterException, GtuException
    {
        Acceleration a = Acceleration.POS_MAXVALUE;
        for (AccelerationIncentive incentive : this.accelerationIncentives)
        {
            Acceleration aIncentive = incentive.accelerate(context, lane, mergeDistance);
            this.acceleration.put(incentive.getClass(), aIncentive);
            a = Acceleration.min(a, aIncentive);
        }
        return a;
    }

    @Override
    public Optional<Acceleration> getLatestAcceleration(final Class<? extends AccelerationIncentive> incentiveClass)
    {
        return Optional.ofNullable(this.acceleration.get(incentiveClass));
    }

}
