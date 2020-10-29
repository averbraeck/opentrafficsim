package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterSet;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.perception.DirectEgoPerception;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
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
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 15 feb. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class DefaultLMRSPerceptionFactory implements PerceptionFactory
{

    /** {@inheritDoc} */
    @Override
    public LanePerception generatePerception(final LaneBasedGTU gtu)
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
