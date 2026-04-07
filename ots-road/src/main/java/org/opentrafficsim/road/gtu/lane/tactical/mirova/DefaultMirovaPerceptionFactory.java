package org.opentrafficsim.road.gtu.lane.tactical.mirova;

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
import org.opentrafficsim.road.gtu.lane.perception.categories.DirectDefaultSimplePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.DirectInfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.DirectIntersectionPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.DirectNeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.HeadwayGtuType;

/**
 * Perception factory for the MiRoVA framework.
 * <p>
 * Initializes the essential perception categories for the GTU, forming the basic data
 * foundation for Layer 1 (Perception & Context) in the MiRoVA architecture. This includes:
 * <ul>
 * <li>Ego perception (speed, acceleration, etc.)</li>
 * <li>Infrastructure perception (lanes, speed limits, lane drops)</li>
 * <li>Neighbors perception (leaders, followers, adjacent vehicles)</li>
 * <li>Intersection perception (traffic lights, conflicts)</li>
 * <li>Anticipation of downstream traffic states</li>
 * </ul>
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 *
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public class DefaultMirovaPerceptionFactory implements PerceptionFactory
{

    /**
     * Constructor for the default MiRoVA perception factory.
     */
    public DefaultMirovaPerceptionFactory()
    {
        // default constructor
    }

    /**
     * Generates the perception module for the given GTU with all required categories.
     *
     * @param gtu the lane-based GTU for which to generate the perception module
     * @return the fully initialized {@link LanePerception} module
     */
    @Override
    public LanePerception generatePerception(final LaneBasedGtu gtu)
    {
        LanePerception perception = new CategoricalLanePerception(gtu);
        perception.addPerceptionCategory(new DirectDefaultSimplePerception(perception));
        perception.addPerceptionCategory(new DirectEgoPerception<>(perception));
        perception.addPerceptionCategory(new DirectInfrastructurePerception(perception));
        perception.addPerceptionCategory(new DirectNeighborsPerception(perception, HeadwayGtuType.WRAP));
        perception.addPerceptionCategory(new AnticipationTrafficPerception(perception));
        perception.addPerceptionCategory(new DirectIntersectionPerception(perception, HeadwayGtuType.WRAP));
        return perception;
    }

    /**
     * Retrieves the default parameters required for the perception categories.
     *
     * @return a {@link Parameters} set containing default perception parameters
     * @throws ParameterException if a parameter fails to initialize
     */
    @Override
    public Parameters getParameters() throws ParameterException
    {
        return new ParameterSet()
                .setDefaultParameter(ParameterTypes.LOOKAHEAD)
                .setDefaultParameter(ParameterTypes.LOOKBACKOLD)
                .setDefaultParameter(ParameterTypes.PERCEPTION)
                .setDefaultParameter(ParameterTypes.LOOKBACK);
    }

}