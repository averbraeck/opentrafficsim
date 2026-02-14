package org.opentrafficsim.road.gtu.perception.categories.neighbors;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.base.parameters.ParameterTypeDuration;
import org.opentrafficsim.base.parameters.ParameterTypeLength;
import org.opentrafficsim.base.parameters.constraint.NumericConstraint;
import org.opentrafficsim.core.gtu.perception.PerceptionCategory;
import org.opentrafficsim.road.gtu.LaneBasedGtu;
import org.opentrafficsim.road.gtu.perception.LanePerception;
import org.opentrafficsim.road.gtu.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.perception.object.PerceivedGtu;

/**
 * Perception category for longitudinal control such as ACC and CACC.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface LongitudinalControllerPerception extends PerceptionCategory<LaneBasedGtu, LanePerception>
{

    /** Sensor range parameter. */
    ParameterTypeLength RANGE = new ParameterTypeLength("range", "Sensor range", Length.ofSI(200), NumericConstraint.POSITIVE);

    /** Sensor delay parameter. */
    ParameterTypeDuration DELAY =
            new ParameterTypeDuration("delay", "Sensor delay", Duration.ofSI(0.2), NumericConstraint.POSITIVE);

    /**
     * Returns the leaders.
     * @return leaders
     */
    PerceptionCollectable<PerceivedGtu, LaneBasedGtu> getLeaders();

}
