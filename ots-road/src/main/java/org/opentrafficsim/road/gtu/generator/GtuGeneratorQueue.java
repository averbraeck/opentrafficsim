package org.opentrafficsim.road.gtu.generator;

import java.util.Map;

import org.opentrafficsim.core.geometry.DirectedPoint;

import nl.tudelft.simulation.dsol.animation.Locatable;

/**
 * Interface for GTU generators. As generators can be quite distinct and function autonomously, this interface only regards
 * information for animation.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public interface GtuGeneratorQueue extends Locatable
{

    /**
     * Returns the locations and lengths of generation queues. Note that the provided location may differ from the generator
     * location, as one generator may generate GTU's at different points.
     * @return Map&lt;DirectedPoint, Integer&gt;; locations and lengths of generation queues
     */
    Map<DirectedPoint, Integer> getQueueLengths();

}
