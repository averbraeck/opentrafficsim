package org.opentrafficsim.kpi.interfaces;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 12 okt. 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface GtuDataInterface
{

    /**
     * @return unique id of the gtu
     */
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
