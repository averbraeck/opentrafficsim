package org.opentrafficsim.road.gtu.generator.od;

import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGTUCharacteristics;
import org.opentrafficsim.road.gtu.strategical.od.Category;

import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Interface for classes that generate GTU characteristics based on an OD. Contrary to lower-level vehicle generation, the OD
 * can pre-determine some information, such as GTU type and route.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 9 dec. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface GTUCharacteristicsGeneratorOD
{

    /**
     * Generate new {@code LaneBasedGTUCharacteristics} using given input from OD.
     * @param origin Node; origin
     * @param destination Node; destination
     * @param category Category; category (GTU type, route, or more)
     * @param randomStream StreamInterface; stream for random numbers
     * @return LaneBasedGTUCharacteristics
     * @throws GTUException if characteristics could not be generated for the GTUException
     */
    LaneBasedGTUCharacteristics draw(Node origin, Node destination, Category category, StreamInterface randomStream)
            throws GTUException;

}
