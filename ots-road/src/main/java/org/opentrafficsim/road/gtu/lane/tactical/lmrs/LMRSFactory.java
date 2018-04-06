package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterSet;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
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
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Aug 2, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class LMRSFactory extends AbstractLaneBasedTacticalPlannerFactory<LMRS> implements Serializable
{

    /** */
    private static final long serialVersionUID = 20160811L;

    /** Factory for perception. */
    private final PerceptionFactory perceptionFactory;

    /** Type of synchronization. */
    private final Synchronization synchronization;

    /** Type of cooperation. */
    private final Cooperation cooperation;

    /** Type of gap-acceptance. */
    private final GapAcceptance gapAcceptance;
    
    /** Type of tail gating. */
    private final Tailgating tailGating;

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
     * @throws GTUException if the supplied car-following model does not have an accessible empty constructor
     */
    public LMRSFactory(final CarFollowingModelFactory<? extends CarFollowingModel> carFollowingModelFactory,
            final PerceptionFactory perceptionFactory) throws GTUException
    {
        super(carFollowingModelFactory);
        this.perceptionFactory = perceptionFactory;
        this.synchronization = Synchronization.PASSIVE;
        this.cooperation = Cooperation.PASSIVE;
        this.gapAcceptance = GapAcceptance.INFORMED;
        this.tailGating = Tailgating.NONE;
    }

    /**
     * Constructor with full control over incentives and type of synchronization.
     * @param carFollowingModelFactory factory of the car-following model
     * @param perceptionFactory perception factory
     * @param synchronization type of synchronization
     * @param cooperation type of cooperation
     * @param gapAcceptance gap-acceptance
     * @param tailGating tail gating
     * @param mandatoryIncentives mandatory incentives
     * @param voluntaryIncentives voluntary incentives
     * @param accelerationIncentives acceleration incentives
     * @throws GTUException if the supplied car-following model does not have an accessible empty constructor
     */
    public LMRSFactory(final CarFollowingModelFactory<? extends CarFollowingModel> carFollowingModelFactory,
            final PerceptionFactory perceptionFactory, final Synchronization synchronization, final Cooperation cooperation,
            final GapAcceptance gapAcceptance, final Tailgating tailGating, final Set<MandatoryIncentive> mandatoryIncentives,
            final Set<VoluntaryIncentive> voluntaryIncentives, final Set<AccelerationIncentive> accelerationIncentives)
            throws GTUException
    {
        super(carFollowingModelFactory);
        this.perceptionFactory = perceptionFactory;
        this.synchronization = synchronization;
        this.cooperation = cooperation;
        this.gapAcceptance = gapAcceptance;
        this.tailGating = tailGating;
        this.mandatoryIncentives.addAll(mandatoryIncentives);
        this.voluntaryIncentives.addAll(voluntaryIncentives);
        this.accelerationIncentives.addAll(accelerationIncentives);
    }

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
        this.perceptionFactory.getParameters().setAllIn(parameters);
        try
        {
            parameters.setDefaultParameter(ParameterTypes.VCONG);
            parameters.setDefaultParameter(ParameterTypes.T0);
        }
        catch (ParameterException exception)
        {
            throw new RuntimeException(exception);
        }
        return parameters;
    }

    /** {@inheritDoc} */
    @Override
    public final LMRS create(final LaneBasedGTU gtu) throws GTUException
    {
        LMRS lmrs = new LMRS(nextCarFollowingModel(gtu), gtu, this.perceptionFactory.generatePerception(gtu),
                this.synchronization, this.cooperation, this.gapAcceptance, this.tailGating);
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
