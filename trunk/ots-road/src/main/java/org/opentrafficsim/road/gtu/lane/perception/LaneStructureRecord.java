package org.opentrafficsim.road.gtu.lane.perception;

import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 13 aug. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface LaneStructureRecord extends LaneRecord<LaneStructureRecord>
{

    /**
     * Returns the the 'from' node of the link belonging to this lane, in the driving direction.
     * @return Node; the 'from' node of the link belonging to this lane, in the driving direction
     */
    Node getFromNode();
    
    /**
     * Returns the the 'to' node of the link belonging to this lane, in the driving direction.
     * @return Node; the 'to' node of the link belonging to this lane, in the driving direction
     */
    Node getToNode();
    
    /**
     * Returns the left LSR or null if not available. Left and right are relative to the <b>driving</b> direction.
     * @return LaneStructureRecord; the left LSR or null if not available
     */
    LaneStructureRecord getLeft();
    
    /**
     * Returns the right LSR or null if not available. Left and right are relative to the <b>driving</b> direction.
     * @return LaneStructureRecord; the right LSR or null if not available
     */
    LaneStructureRecord getRight();

    /**
     * Returns whether a left lane change is legal.
     * @return whether a left lane change is legal
     */
    boolean legalLeft();
    
    /**
     * Returns whether a right lane change is legal.
     * @return whether a right lane change is legal
     */
    boolean legalRight();
    
    /**
     * Returns whether a left lane change is physically possible.
     * @return whether a left lane change is physically possible
     */
    boolean physicalLeft();
    
    /**
     * Returns whether a right lane change is physically possible.
     * @return whether a right lane change is physically possible
     */
    boolean physicalRight();

    /**
     * Returns whether this lane has no next records as the lane structure was cut-off.
     * @return whether this lane has no next records as the lane structure was cut-off
     */
    boolean isCutOffEnd();
    
    /**
     * Returns whether this lane has no previous records as the lane structure was cut-off.
     * @return whether this lane has no previous records as the lane structure was cut-off
     */
    boolean isCutOffStart();
    
    /**
     * Returns whether the record forms a dead-end.
     * @return whether the record forms a dead-end
     */
    boolean isDeadEnd();

    /**
     * Returns whether this lane allows the route to be followed.
     * @param route Route; the route to follow
     * @param gtuType GTUType; gtu type
     * @return whether this lane allows the route to be followed
     * @throws NetworkException if no destination node
     */
    boolean allowsRoute(Route route, GTUType gtuType) throws NetworkException;

    /**
     * Returns whether the end of this lane allows the route to be followed.
     * @param route Route; the route to follow
     * @param gtuType GTUType; gtu type
     * @return whether the end of this lane allows the route to be followed
     * @throws NetworkException if no destination node
     */
    boolean allowsRouteAtEnd(Route route, GTUType gtuType) throws NetworkException;

}
