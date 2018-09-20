package org.opentrafficsim.road.gtu.lane.perception.categories;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayBusStop;
import org.opentrafficsim.road.network.lane.object.BusStop;

/**
 * Bus stop perception category.
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 14 feb. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface BusStopPerception extends LaneBasedPerceptionCategory
{

    /**
     * Updates the bus stops.
     * @throws GTUException if the GTU has not been initialized
     * @throws ParameterException if lane structure cannot be made due to missing parameter
     */
    void updateBusStops() throws GTUException, ParameterException;

    /**
     * Returns the bus stops.
     * @return bus stops
     */
    PerceptionCollectable<HeadwayBusStop, BusStop> getBusStops();

    /** {@inheritDoc} */
    @Override
    default void updateAll() throws GTUException, NetworkException, ParameterException
    {
        updateBusStops();
    }

}
