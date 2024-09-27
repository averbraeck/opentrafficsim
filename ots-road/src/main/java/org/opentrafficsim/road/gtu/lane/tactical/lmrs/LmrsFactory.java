package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterSet;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionFactory;
import org.opentrafficsim.road.gtu.lane.tactical.AbstractLaneBasedTacticalPlannerFactory;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModelFactory;
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

/**
 * Factory for a tactical planner using LMRS with any car-following model.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class LmrsFactory extends AbstractLaneBasedTacticalPlannerFactory<Lmrs> implements Serializable
{

    /** */
    private static final long serialVersionUID = 20160811L;

    /** Type of synchronization. */
    private final Synchronization synchronization;

    /** Type of cooperation. */
    private final Cooperation cooperation;

    /** Type of gap-acceptance. */
    private final GapAcceptance gapAcceptance;

    /** Type of tail gating. */
    private final Tailgating tailgating;

    /** Mandatory incentives. */
    private final Set<MandatoryIncentive> mandatoryIncentives = new LinkedHashSet<>();

    /** Mandatory incentives. */
    private final Set<VoluntaryIncentive> voluntaryIncentives = new LinkedHashSet<>();

    /** Mandatory incentives. */
    private final Set<AccelerationIncentive> accelerationIncentives = new LinkedHashSet<>();

    /**
     * Constructor using default incentives and passive synchronization.
     * @param carFollowingModelFactory factory of the car-following model
     * @param perceptionFactory perception factory
     */
    public LmrsFactory(final CarFollowingModelFactory<? extends CarFollowingModel> carFollowingModelFactory,
            final PerceptionFactory perceptionFactory)
    {
        super(carFollowingModelFactory, perceptionFactory);
        this.synchronization = Synchronization.PASSIVE;
        this.cooperation = Cooperation.PASSIVE;
        this.gapAcceptance = GapAcceptance.INFORMED;
        this.tailgating = Tailgating.NONE;
    }

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
    public LmrsFactory(final CarFollowingModelFactory<? extends CarFollowingModel> carFollowingModelFactory,
            final PerceptionFactory perceptionFactory, final Synchronization synchronization, final Cooperation cooperation,
            final GapAcceptance gapAcceptance, final Tailgating tailgating, final Set<MandatoryIncentive> mandatoryIncentives,
            final Set<VoluntaryIncentive> voluntaryIncentives, final Set<AccelerationIncentive> accelerationIncentives)
    {
        super(carFollowingModelFactory, perceptionFactory);
        this.synchronization = synchronization;
        this.cooperation = cooperation;
        this.gapAcceptance = gapAcceptance;
        this.tailgating = tailgating;
        this.mandatoryIncentives.addAll(mandatoryIncentives);
        this.voluntaryIncentives.addAll(voluntaryIncentives);
        this.accelerationIncentives.addAll(accelerationIncentives);
    }

    // TODO: use factory instead of constructors

    /** {@inheritDoc} */
    @Override
    public final Parameters getParameters() throws ParameterException
    {
        ParameterSet parameters = new ParameterSet();
        parameters.setDefaultParameters(LmrsUtil.class);
        parameters.setDefaultParameters(LmrsParameters.class);
        parameters.setDefaultParameters(ConflictUtil.class);
        parameters.setDefaultParameters(TrafficLightUtil.class);
        getCarFollowingParameters().setAllIn(parameters);
        getPerceptionFactory().getParameters().setAllIn(parameters);
        parameters.setDefaultParameter(ParameterTypes.VCONG);
        parameters.setDefaultParameter(ParameterTypes.T0);
        parameters.setDefaultParameter(ParameterTypes.LCDUR);
        return parameters;
    }

    /** {@inheritDoc} */
    @Override
    public final Lmrs create(final LaneBasedGtu gtu) throws GtuException
    {
        Lmrs lmrs = new Lmrs(nextCarFollowingModel(gtu), gtu, getPerceptionFactory().generatePerception(gtu),
                this.synchronization, this.cooperation, this.gapAcceptance, this.tailgating);
        if (this.mandatoryIncentives.isEmpty())
        {
            lmrs.setDefaultIncentives();
        }
        else
        {
            this.mandatoryIncentives.forEach(incentive -> lmrs.addMandatoryIncentive(incentive));
            this.voluntaryIncentives.forEach(incentive -> lmrs.addVoluntaryIncentive(incentive));
            this.accelerationIncentives.forEach(incentive -> lmrs.addAccelerationIncentive(incentive));
        }
        return lmrs;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "LMRSFactory [car-following=" + getCarFollowingModelFactoryString() + "]";
    }

}
