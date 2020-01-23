package org.opentrafficsim.road.gtu.lane.perception.categories.neighbors;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.base.parameters.ParameterTypeDuration;
import org.opentrafficsim.base.parameters.ParameterTypeLength;
import org.opentrafficsim.base.parameters.constraint.NumericConstraint;
import org.opentrafficsim.core.gtu.perception.PerceptionCategory;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTU;

/**
 * Perception category for longitudinal control such as ACC and CACC.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Mar 12, 2019 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface LongitudinalControllerPerception extends PerceptionCategory<LaneBasedGTU, LanePerception>
{

    /** Sensor range parameter. */
    ParameterTypeLength RANGE =
            new ParameterTypeLength("range", "Sensor range", Length.instantiateSI(200), NumericConstraint.POSITIVE);
    
    /** Sensor delay parameter. */
    ParameterTypeDuration DELAY =
            new ParameterTypeDuration("delay", "Sensor delay", Duration.instantiateSI(0.2), NumericConstraint.POSITIVE);

    /**
     * Returns the leaders.
     * @return leaders
     */
    PerceptionCollectable<HeadwayGTU, LaneBasedGTU> getLeaders();

}
