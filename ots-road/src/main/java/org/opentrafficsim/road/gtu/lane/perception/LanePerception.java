package org.opentrafficsim.road.gtu.lane.perception;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.perception.Perception;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.mental.Mental;
import org.opentrafficsim.road.gtu.lane.perception.structure.LaneStructure;

/**
 * Interface for perception in a lane-based model. The following information can be perceived:
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface LanePerception extends Perception<LaneBasedGtu>
{

    /**
     * Return the lane structure.
     * @return lane structure.
     * @throws ParameterException on exception.
     */
    LaneStructure getLaneStructure() throws ParameterException;

    /**
     * Returns the mental module of perception.
     * @return mental module of perception, may be {@code null} if not used
     */
    Mental getMental();

}
