package org.opentrafficsim.road.gtu.perception.categories;

import org.opentrafficsim.core.gtu.perception.PerceptionCategory;
import org.opentrafficsim.road.gtu.LaneBasedGtu;
import org.opentrafficsim.road.gtu.perception.LanePerception;

/**
 * Lane based perception category.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Alexander Verbraeck
 * @author Peter Knoppers
 * @author Wouter Schakel
 * @param <C> category type
 */
public interface LaneBasedPerceptionCategory<C extends LaneBasedPerceptionCategory<C>>
        extends PerceptionCategory<LaneBasedGtu, LanePerception, C>
{
    // implements type arguments
}
