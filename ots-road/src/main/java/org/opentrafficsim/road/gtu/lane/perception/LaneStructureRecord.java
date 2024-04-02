package org.opentrafficsim.road.gtu.lane.perception;

import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;

/**
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface LaneStructureRecord extends LaneRecordInterface<LaneStructureRecord>
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
     * Returns the left lane change possibility.
     * @param legal boolean; legal, or otherwise physical, possibility
     * @return boolean; left lane change possibility
     */
    default boolean possibleLeft(final boolean legal)
    {
        return legal ? legalLeft() : physicalLeft();
    }

    /**
     * Returns the right lane change possibility.
     * @param legal boolean; legal, or otherwise physical, possibility
     * @return boolean; right lane change possibility
     */
    default boolean possibleRight(final boolean legal)
    {
        return legal ? legalRight() : physicalRight();
    }

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
     * @param gtuType GtuType; gtu type
     * @return whether this lane allows the route to be followed
     * @throws NetworkException if no destination node
     */
    boolean allowsRoute(Route route, GtuType gtuType) throws NetworkException;

    /**
     * Returns whether the end of this lane allows the route to be followed.
     * @param route Route; the route to follow
     * @param gtuType GtuType; gtu type
     * @return whether the end of this lane allows the route to be followed
     * @throws NetworkException if no destination node
     */
    boolean allowsRouteAtEnd(Route route, GtuType gtuType) throws NetworkException;

}
