package org.opentrafficsim.road.gtu.generator;

import java.util.Map;

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * Interface for GTU generators. As generators can be quite distinct and function autonomously, this interface only regards
 * information for animation.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 11 jan. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
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
