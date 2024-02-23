package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterSet;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.perception.DirectEgoPerception;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.CategoricalLanePerception;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionFactory;
import org.opentrafficsim.road.gtu.lane.perception.categories.AnticipationTrafficPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.DirectInfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.DirectIntersectionPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.DirectNeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.HeadwayGtuType;

/**
 * Perception factory with EgoPerception, InfrastructurePerception, NeighborsPerception and IntersectionPerception.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class DefaultLmrsPerceptionFactory implements PerceptionFactory
{

    /** {@inheritDoc} */
    @Override
    public LanePerception generatePerception(final LaneBasedGtu gtu)
    {
        LanePerception perception = new CategoricalLanePerception(gtu);
        perception.addPerceptionCategory(new DirectEgoPerception<>(perception));
        perception.addPerceptionCategory(new DirectInfrastructurePerception(perception));
        perception.addPerceptionCategory(new DirectNeighborsPerception(perception, HeadwayGtuType.WRAP));
        perception.addPerceptionCategory(new AnticipationTrafficPerception(perception));
        perception.addPerceptionCategory(new DirectIntersectionPerception(perception, HeadwayGtuType.WRAP));
        return perception;
    }

    /** {@inheritDoc} */
    @Override
    public Parameters getParameters() throws ParameterException
    {
        return new ParameterSet().setDefaultParameter(ParameterTypes.LOOKAHEAD).setDefaultParameter(ParameterTypes.LOOKBACKOLD)
                .setDefaultParameter(ParameterTypes.PERCEPTION).setDefaultParameter(ParameterTypes.LOOKBACK);
    }

}
