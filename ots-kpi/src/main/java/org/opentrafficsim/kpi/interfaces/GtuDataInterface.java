package org.opentrafficsim.kpi.interfaces;

import org.opentrafficsim.base.Identifiable;

/**
 * Represents a GTU for sampling.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public interface GtuDataInterface extends Identifiable
{

    /**
     * @return unique id of the gtu
     */
    @Override
    String getId();

    /**
     * @return origin node of the gtu
     */
    NodeDataInterface getOriginNodeData();

    /**
     * @return destination node of the gtu
     */
    NodeDataInterface getDestinationNodeData();

    /**
     * @return type of the gtu
     */
    GtuTypeDataInterface getGtuTypeData();

    /**
     * @return route of the gtu
     */
    RouteDataInterface getRouteData();

}
