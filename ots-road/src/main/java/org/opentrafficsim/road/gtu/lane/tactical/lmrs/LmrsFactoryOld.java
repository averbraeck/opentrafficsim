package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Supplier;

import org.opentrafficsim.base.logger.Logger;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterSet;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionFactory;
import org.opentrafficsim.road.gtu.lane.tactical.AbstractLaneBasedTacticalPlannerFactory;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModelFactory;
import org.opentrafficsim.road.gtu.lane.tactical.following.IdmPlusFactory;
import org.opentrafficsim.road.gtu.lane.tactical.util.ConflictUtil;
import org.opentrafficsim.road.gtu.lane.tactical.util.TrafficLightUtil;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Cooperation;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.GapAcceptance;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsParameters;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsUtil;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.MandatoryIncentive;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Synchronization;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Tailgating;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.VoluntaryIncentive;

import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Factory for a tactical planner using LMRS with any car-following model.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
@Deprecated
public class LmrsFactoryOld extends AbstractLaneBasedTacticalPlannerFactory<Lmrs>
{

    /** Type of synchronization. */
    private final Synchronization synchronization;

    /** Type of cooperation. */
    private final Cooperation cooperation;

    /** Type of gap-acceptance. */
    private final GapAcceptance gapAcceptance;

    /** Type of tail gating. */
    private final Tailgating tailgating;

    /** Mandatory incentives. */
    private final Set<Supplier<? extends MandatoryIncentive>> mandatoryIncentives;

    /** Mandatory incentives. */
    private final Set<Supplier<? extends VoluntaryIncentive>> voluntaryIncentives;

    /** Mandatory incentives. */
    private final Set<Supplier<? extends AccelerationIncentive>> accelerationIncentives;

    /**
     * Constructor with full control over incentives and type of synchronization.
     * @param carFollowingModelFactory factory of the car-following model
     * @param perceptionFactory perception factory
     * @param synchronization type of synchronization
     * @param cooperation type of cooperation
     * @param gapAcceptance gap-acceptance
     * @param tailgating tail gating
     * @param mandatoryIncentives note that order may matter
     * @param voluntaryIncentives note that order may matter
     * @param accelerationIncentives acceleration incentives
     */
    @SuppressWarnings("parameternumber")
    protected LmrsFactoryOld(final CarFollowingModelFactory<? extends CarFollowingModel> carFollowingModelFactory,
            final PerceptionFactory perceptionFactory, final Synchronization synchronization, final Cooperation cooperation,
            final GapAcceptance gapAcceptance, final Tailgating tailgating,
            final Set<Supplier<? extends MandatoryIncentive>> mandatoryIncentives,
            final Set<Supplier<? extends VoluntaryIncentive>> voluntaryIncentives,
            final Set<Supplier<? extends AccelerationIncentive>> accelerationIncentives)
    {
        super(carFollowingModelFactory, perceptionFactory);
        this.synchronization = synchronization;
        this.cooperation = cooperation;
        this.gapAcceptance = gapAcceptance;
        this.tailgating = tailgating;
        this.mandatoryIncentives = mandatoryIncentives;
        this.voluntaryIncentives = voluntaryIncentives;
        this.accelerationIncentives = accelerationIncentives;
    }

    @Override
    public final Parameters getParameters(final GtuType gtuType) throws ParameterException
    {
        ParameterSet parameters = new ParameterSet();
        parameters.setDefaultParameters(LmrsUtil.class);
        parameters.setDefaultParameters(LmrsParameters.class);
        parameters.setDefaultParameters(ConflictUtil.class);
        parameters.setDefaultParameters(TrafficLightUtil.class);
        getCarFollowingParameters(gtuType).setAllIn(parameters);
        getPerceptionFactory().getParameters(gtuType).setAllIn(parameters);
        parameters.setDefaultParameter(ParameterTypes.VCONG);
        parameters.setDefaultParameter(ParameterTypes.T0);
        parameters.setDefaultParameter(ParameterTypes.LCDUR);
        return parameters;
    }

    @Override
    public final Lmrs create(final LaneBasedGtu gtu) throws GtuException
    {
        Lmrs lmrs = new Lmrs(nextCarFollowingModel(gtu), gtu, getPerceptionFactory().generatePerception(gtu),
                this.synchronization, this.cooperation, this.gapAcceptance, this.tailgating);
        this.mandatoryIncentives.forEach(supplier -> lmrs.addMandatoryIncentive(supplier.get()));
        this.voluntaryIncentives.forEach(supplier -> lmrs.addVoluntaryIncentive(supplier.get()));
        this.accelerationIncentives.forEach(supplier -> lmrs.addAccelerationIncentive(supplier.get()));
        return lmrs;
    }

    @Override
    public final String toString()
    {
        return "LmrsFactory [car-following=" + getCarFollowingModelFactoryString() + "]";
    }

    /**
     * Factory of LmrsFactory.
     */
    @Deprecated
    public static class Factory
    {
        /** Car-following model. */
        private CarFollowingModelFactory<? extends CarFollowingModel> carFollowingModelFactory;

        /** Perception factory. */
        private PerceptionFactory perceptionFactory = new DefaultLmrsPerceptionFactory();

        /** Type of synchronization. */
        private Synchronization synchronization = Synchronization.PASSIVE;

        /** Type of cooperation. */
        private Cooperation cooperation = Cooperation.PASSIVE;

        /** Type of gap-acceptance. */
        private GapAcceptance gapAcceptance = GapAcceptance.INFORMED;

        /** Type of tail gating. */
        private Tailgating tailgating = Tailgating.NONE;

        /** Mandatory incentives. */
        private Set<Supplier<? extends MandatoryIncentive>> mandatoryIncentives = new LinkedHashSet<>();

        /** Mandatory incentives. */
        private Set<Supplier<? extends VoluntaryIncentive>> voluntaryIncentives = new LinkedHashSet<>();

        /** Mandatory incentives. */
        private Set<Supplier<? extends AccelerationIncentive>> accelerationIncentives = new LinkedHashSet<>();

        /**
         * Constructor.
         */
        public Factory()
        {
            //
        }

        /**
         * Sets car-following model factory.
         * @param carFollowingModelFactory car-following model factory.
         * @return this factory for method changing
         */
        @SuppressWarnings("hiddenfield")
        public Factory setCarFollowingModelFactory(
                final CarFollowingModelFactory<? extends CarFollowingModel> carFollowingModelFactory)
        {
            this.carFollowingModelFactory = carFollowingModelFactory;
            return this;
        }

        /**
         * Sets perception factory.
         * @param perceptionFactory perception factory.
         * @return this factory for method changing
         */
        @SuppressWarnings("hiddenfield")
        public Factory setPerceptionFactory(final PerceptionFactory perceptionFactory)
        {
            this.perceptionFactory = perceptionFactory;
            return this;
        }

        /**
         * Sets synchronization.
         * @param synchronization synchronization
         * @return this factory for method changing
         */
        @SuppressWarnings("hiddenfield")
        public Factory setSynchonization(final Synchronization synchronization)
        {
            this.synchronization = synchronization;
            return this;
        }

        /**
         * Sets cooperation.
         * @param cooperation cooperation
         * @return this factory for method changing
         */
        @SuppressWarnings("hiddenfield")
        public Factory setCooperation(final Cooperation cooperation)
        {
            this.cooperation = cooperation;
            return this;
        }

        /**
         * Sets gap acceptance.
         * @param gapAcceptance gap acceptance
         * @return this factory for method changing
         */
        @SuppressWarnings("hiddenfield")
        public Factory setGapAcceptance(final GapAcceptance gapAcceptance)
        {
            this.gapAcceptance = gapAcceptance;
            return this;
        }

        /**
         * Sets gap tailgating.
         * @param tailgating tailgating
         * @return this factory for method changing
         */
        @SuppressWarnings("hiddenfield")
        public Factory setTailgating(final Tailgating tailgating)
        {
            this.tailgating = tailgating;
            return this;
        }

        /**
         * Sets default lane change and acceleration incentives.
         * @return this factory for method changing
         */
        public Factory withDefaultIncentives()
        {
            this.mandatoryIncentives.add(() -> IncentiveRoute.SINGLETON);
            this.voluntaryIncentives.add(() -> IncentiveSpeedWithCourtesy.SINGLETON);
            this.voluntaryIncentives.add(() -> IncentiveKeep.SINGLETON);
            this.voluntaryIncentives.add(() -> IncentiveQueue.SINGLETON);
            this.accelerationIncentives.add(() -> AccelerationSpeedLimitTransition.SINGLETON);
            this.accelerationIncentives.add(() -> AccelerationTrafficLights.SINGLETON);
            this.accelerationIncentives.add(() -> new AccelerationConflicts());
            return this;
        }

        /**
         * Add mandatory incentive.
         * @param incentive mandatory incentive
         * @return this factory for method changing
         */
        public Factory addMandatoryIncentive(final Supplier<? extends MandatoryIncentive> incentive)
        {
            this.mandatoryIncentives.add(incentive);
            return this;
        }

        /**
         * Add voluntary incentive.
         * @param incentive voluntary incentive
         * @return this factory for method changing
         */
        public Factory addVoluntaryIncentive(final Supplier<? extends VoluntaryIncentive> incentive)
        {
            this.voluntaryIncentives.add(incentive);
            return this;
        }

        /**
         * Add acceleration incentive.
         * @param incentive acceleration incentive
         * @return this factory for method changing
         */
        public Factory addAccelerationIncentive(final Supplier<? extends AccelerationIncentive> incentive)
        {
            this.accelerationIncentives.add(incentive);
            return this;
        }

        /**
         * Builds an {@code LmrsFactory}.
         * @param stream random stream, may be {@code null} if a car-following model factory was provided in this factory
         * @return LMRS factory
         */
        public LmrsFactoryOld build(final StreamInterface stream)
        {
            if (this.mandatoryIncentives.isEmpty())
            {
                this.mandatoryIncentives.add(IncentiveDummy.SINGLETON);
                if (this.voluntaryIncentives.isEmpty() && this.accelerationIncentives.isEmpty())
                {
                    Logger.ots().info("LmrsFactory uses no incentives at all.");
                }
            }
            if (this.carFollowingModelFactory == null)
            {
                this.carFollowingModelFactory = new IdmPlusFactory(stream);
            }
            return new LmrsFactoryOld(this.carFollowingModelFactory, this.perceptionFactory, this.synchronization,
                    this.cooperation, this.gapAcceptance, this.tailgating, this.mandatoryIncentives, this.voluntaryIncentives,
                    this.accelerationIncentives);
        }
    }

}
