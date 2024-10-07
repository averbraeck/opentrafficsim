package org.opentrafficsim.road.gtu.generator.characteristics;

import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.road.od.Category;

import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Interface for classes that generate GTU characteristics based on OD information. Additional to
 * {@code LaneBasedGtuCharacteristicsGenerator} this class draws based on origin, destination, category, and a random stream.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
@FunctionalInterface
public interface LaneBasedGtuCharacteristicsGeneratorOd
{

    /**
     * Generate new {@code LaneBasedGtuCharacteristics} using given input from OD.
     * @param origin origin
     * @param destination destination
     * @param category category (GTU type, route, or more)
     * @param randomStream stream for random numbers
     * @return LaneBasedGtuCharacteristics
     * @throws GtuException if characteristics could not be generated for the GTUException
     */
    LaneBasedGtuCharacteristics draw(Node origin, Node destination, Category category, StreamInterface randomStream)
            throws GtuException;

}
