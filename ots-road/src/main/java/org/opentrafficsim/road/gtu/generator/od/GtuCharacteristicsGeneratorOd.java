package org.opentrafficsim.road.gtu.generator.od;

import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGtuCharacteristics;
import org.opentrafficsim.road.gtu.strategical.od.Category;

import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Interface for classes that generate GTU characteristics based on an OD. Contrary to lower-level vehicle generation, the OD
 * can pre-determine some information, such as GTU type and route.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public interface GtuCharacteristicsGeneratorOd
{

    /**
     * Generate new {@code LaneBasedGtuCharacteristics} using given input from OD.
     * @param origin Node; origin
     * @param destination Node; destination
     * @param category Category; category (GTU type, route, or more)
     * @param randomStream StreamInterface; stream for random numbers
     * @return LaneBasedGtuCharacteristics
     * @throws GtuException if characteristics could not be generated for the GTUException
     */
    LaneBasedGtuCharacteristics draw(Node origin, Node destination, Category category, StreamInterface randomStream)
            throws GtuException;

}
