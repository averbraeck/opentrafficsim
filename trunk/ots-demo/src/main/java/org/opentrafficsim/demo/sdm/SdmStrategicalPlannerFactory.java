package org.opentrafficsim.demo.sdm;

import java.util.LinkedHashSet;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterSet;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterFactoryByType;
import org.opentrafficsim.core.gtu.perception.DirectEgoPerception;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.road.gtu.generator.od.StrategicalPlannerFactorySupplierOD;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.CategoricalLanePerception;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionFactory;
import org.opentrafficsim.road.gtu.lane.perception.categories.AnticipationTrafficPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.DirectInfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.Anticipation;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.DirectNeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.Estimation;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.HeadwayGtuType;
import org.opentrafficsim.road.gtu.lane.perception.mental.AdaptationHeadway;
import org.opentrafficsim.road.gtu.lane.perception.mental.AdaptationSituationalAwareness;
import org.opentrafficsim.road.gtu.lane.perception.mental.Fuller;
import org.opentrafficsim.road.gtu.lane.perception.mental.Fuller.BehavioralAdaptation;
import org.opentrafficsim.road.gtu.lane.perception.mental.Fuller.Task;
import org.opentrafficsim.road.gtu.lane.perception.mental.TaskCarFollowing;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlusFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.AccelerationIncentive;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveKeep;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveRoute;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveSpeedWithCourtesy;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveStayRight;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.LMRSFactory;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Cooperation;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.GapAcceptance;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.MandatoryIncentive;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Synchronization;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Tailgating;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.VoluntaryIncentive;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;
import org.opentrafficsim.road.gtu.strategical.od.Category;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlannerFactory;

import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Regular strategical planner with LMRS using Fuller perception.
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 6 nov. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */

public class SdmStrategicalPlannerFactory implements StrategicalPlannerFactorySupplierOD
{

    /** Car factory. */
    private final LaneBasedStrategicalPlannerFactory<?> carFactory;

    /** Truck factory. */
    private final LaneBasedStrategicalPlannerFactory<?> truckFactory;

    /**
     * Constructor.
     * @param stream StreamInterface; random number stream
     */
    @SuppressWarnings("synthetic-access")
    SdmStrategicalPlannerFactory(final StreamInterface stream)
    {
        ParameterFactoryByType paramFactory = new ParameterFactoryByType();
        paramFactory.addParameter(ParameterTypes.TMAX, Duration.createSI(1.0));
        paramFactory.addParameter(ParameterTypes.T, Duration.createSI(1.0));
        paramFactory.addParameter(GTUType.TRUCK, ParameterTypes.A, Acceleration.createSI(0.8));

        Set<MandatoryIncentive> mandatoryIncentives = new LinkedHashSet<>();
        mandatoryIncentives.add(new IncentiveRoute());
        Set<VoluntaryIncentive> voluntaryIncentives = new LinkedHashSet<>();
        voluntaryIncentives.add(new IncentiveSpeedWithCourtesy());
        voluntaryIncentives.add(new IncentiveKeep());
        Set<AccelerationIncentive> accelerationIncentives = new LinkedHashSet<>();
        PerceptionFactory perceptionFactory = new SdmPerception();
        this.carFactory = new LaneBasedStrategicalRoutePlannerFactory(new LMRSFactory(new IDMPlusFactory(stream),
                perceptionFactory, Synchronization.PASSIVE, Cooperation.PASSIVE, GapAcceptance.INFORMED, Tailgating.NONE,
                mandatoryIncentives, voluntaryIncentives, accelerationIncentives), paramFactory);

        voluntaryIncentives = new LinkedHashSet<>(voluntaryIncentives);
        voluntaryIncentives.add(new IncentiveStayRight());
        this.truckFactory = new LaneBasedStrategicalRoutePlannerFactory(new LMRSFactory(new IDMPlusFactory(stream),
                perceptionFactory, Synchronization.PASSIVE, Cooperation.PASSIVE, GapAcceptance.INFORMED, Tailgating.NONE,
                mandatoryIncentives, voluntaryIncentives, accelerationIncentives), paramFactory);
    }

    /** {@inheritDoc} */
    @Override
    public LaneBasedStrategicalPlannerFactory<?> getFactory(final Node origin, final Node destination, final Category category,
            final StreamInterface randomStream) throws GTUException
    {
        if (category.get(GTUType.class).isOfType(GTUType.TRUCK))
        {
            return this.truckFactory;
        }
        return this.carFactory;
    }

    /**
     * Factory for perception with Fuller.
     * <p>
     * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 6 nov. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private class SdmPerception implements PerceptionFactory
    {
        /** {@inheritDoc} */
        @Override
        public Parameters getParameters() throws ParameterException
        {
            ParameterSet params = new ParameterSet();
            params.setDefaultParameters(Fuller.class);
            params.setDefaultParameters(AdaptationSituationalAwareness.class);
            // params.setParameter(AdaptationSituationalAwareness.TR_MAX, Duration.createSI(1.0));
            params.setDefaultParameter(AdaptationHeadway.BETA_T);
            params.setDefaultParameter(ParameterTypes.PERCEPTION);
            params.setDefaultParameter(ParameterTypes.LOOKBACK);
            params.setDefaultParameter(ParameterTypes.LOOKAHEAD);
            params.setParameter(ParameterTypes.TR, Duration.ZERO);
            return params;
        }

        /** {@inheritDoc} */
        @Override
        public LanePerception generatePerception(final LaneBasedGTU gtu)
        {
            Set<Task> tasks = new LinkedHashSet<>();
            tasks.add(new TaskCarFollowing());
            Set<BehavioralAdaptation> adaptations = new LinkedHashSet<>();
            adaptations.add(new AdaptationSituationalAwareness());
            adaptations.add(new AdaptationHeadway());
            CategoricalLanePerception perception = new CategoricalLanePerception(gtu, new Fuller(tasks, adaptations));
            perception.addPerceptionCategory(new DirectEgoPerception<>(perception));
            perception.addPerceptionCategory(new AnticipationTrafficPerception(perception));
            perception.addPerceptionCategory(new DirectInfrastructurePerception(perception));
            perception.addPerceptionCategory(new DirectNeighborsPerception(perception,
                    new HeadwayGtuType.PerceivedHeadwayGtuType(Estimation.UNDERESTIMATION, Anticipation.CONSTANT_SPEED)));
            return perception;
        }
    }

}
