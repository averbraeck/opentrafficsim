package org.opentrafficsim.road.network.lane;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.OTSLink;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;

import nl.tudelft.simulation.event.EventType;

/**
 * A CrossSectionLink is a link with lanes where GTUs can possibly switch between lanes.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-09-16 19:20:07 +0200 (Wed, 16 Sep 2015) $, @version $Revision: 1405 $, by $Author: averbraeck $,
 * initial version Aug 19, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public class CrossSectionLink extends OTSLink implements Serializable
{
    /** */
    private static final long serialVersionUID = 20141015L;

    /** List of cross-section elements. */
    private final List<CrossSectionElement> crossSectionElementList = new ArrayList<>();

    /** List of lanes. */
    private final List<Lane> lanes = new ArrayList<>();

    /** The policy to generally keep left, keep right, or keep lane. */
    private final LaneKeepingPolicy laneKeepingPolicy;

    /**
     * The (regular, not timed) event type for pub/sub indicating the addition of a Lane to a CrossSectionLink. <br>
     * Payload: Object[] { String networkId, String linkId, String LaneId, Lane lane, int laneNumber } <br>
     * TODO work in a different way with lane numbers to align to standard lane numbering.
     */
    public static final EventType LANE_ADD_EVENT = new EventType("LANE.ADD");

    /**
     * The (regular, not timed) event type for pub/sub indicating the removal of a Lane from a CrossSectionLink. <br>
     * Payload: Object[] { String networkId, String linkId, String LaneId } <br>
     * TODO allow for the removal of a Lane; currently this is not possible.
     */
    public static final EventType LANE_REMOVE_EVENT = new EventType("LANE.REMOVE");

    /**
     * Construction of a cross section link.
     * @param network Network; the network
     * @param id String; the link id.
     * @param startNode OTSNode; the start node (directional).
     * @param endNode OTSNode; the end node (directional).
     * @param linkType LinkType; the link type
     * @param designLine OTSLine3D; the design line of the Link
     * @param directionalityMap Map&lt;GTUType, LongitudinalDirectionality&gt;; the directions (FORWARD, BACKWARD, BOTH, NONE)
     *            that various GTUtypes can traverse this link
     * @param laneKeepingPolicy LaneKeepingPolicy; the policy to generally keep left, keep right, or keep lane
     * @throws NetworkException if link already exists in the network, if name of the link is not unique, or if the start node
     *             or the end node of the link are not registered in the network.
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public CrossSectionLink(final Network network, final String id, final Node startNode, final Node endNode,
            final LinkType linkType, final OTSLine3D designLine,
            final Map<GTUType, LongitudinalDirectionality> directionalityMap, final LaneKeepingPolicy laneKeepingPolicy)
            throws NetworkException
    {
        super(network, id, startNode, endNode, linkType, designLine, directionalityMap);
        this.laneKeepingPolicy = laneKeepingPolicy;
    }

    /**
     * Construction of a link, with a general directionality for GTUType.ALL. Other directionalities can be added with the
     * method addDirectionality(...) later.
     * @param network Network; the network
     * @param id String; the link id.
     * @param startNode OTSnode; the start node (directional).
     * @param endNode OTSNode; the end node (directional).
     * @param linkType LinkType; the link type
     * @param designLine OTSLine3D; the design line of the Link
     * @param directionality LongitudinalDirectionality; the default directionality for all GTUs
     * @param laneKeepingPolicy LaneKeepingPolicy; the policy to generally keep left, keep right, or keep lane
     * @throws NetworkException if link already exists in the network, if name of the link is not unique, or if the start node
     *             or the end node of the link are not registered in the network.
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public CrossSectionLink(final Network network, final String id, final Node startNode, final Node endNode,
            final LinkType linkType, final OTSLine3D designLine, final LongitudinalDirectionality directionality,
            final LaneKeepingPolicy laneKeepingPolicy) throws NetworkException
    {
        super(network, id, startNode, endNode, linkType, designLine, directionality);
        this.laneKeepingPolicy = laneKeepingPolicy;
    }

    /**
     * Construction of a link, on which no traffic is allowed after construction of the link. Directionality for GTUTypes can be
     * added with the method addDirectionality(...) later.
     * @param network Network; the network
     * @param id String; the link id.
     * @param startNode OTSNode; the start node (directional).
     * @param endNode OTSNode; the end node (directional).
     * @param linkType LinkType; the link type
     * @param designLine OTSLine3D; the design line of the Link
     * @param laneKeepingPolicy LaneKeepingPolicy; the policy to generally keep left, keep right, or keep lane
     * @throws NetworkException if link already exists in the network, if name of the link is not unique, or if the start node
     *             or the end node of the link are not registered in the network.
     */
    public CrossSectionLink(final Network network, final String id, final OTSNode startNode, final OTSNode endNode,
            final LinkType linkType, final OTSLine3D designLine, final LaneKeepingPolicy laneKeepingPolicy)
            throws NetworkException
    {
        this(network, id, startNode, endNode, linkType, designLine, new HashMap<GTUType, LongitudinalDirectionality>(),
                laneKeepingPolicy);
    }

    /**
     * Add a cross section element at the end of the list. <br>
     * <b>Note:</b> LEFT is seen as a positive lateral direction, RIGHT as a negative lateral direction.
     * @param cse CrossSectionElement; the cross section element to add.
     */
    protected final void addCrossSectionElement(final CrossSectionElement cse)
    {
        this.crossSectionElementList.add(cse);
        if (cse instanceof Lane)
        {
            this.lanes.add((Lane) cse);
            fireEvent(LANE_ADD_EVENT,
                    new Object[] { getNetwork().getId(), getId(), cse.getId(), (Lane) cse, this.lanes.indexOf(cse) });
        }
    }

    /**
     * Retrieve a safe copy of the cross section element list.
     * @return List&lt;CrossSectionElement&gt;; the cross section element list.
     */
    public final List<CrossSectionElement> getCrossSectionElementList()
    {
        return this.crossSectionElementList == null ? new ArrayList<>() : new ArrayList<>(this.crossSectionElementList);
    }

    /**
     * Retrieve the lane keeping policy.
     * @return LaneKeepingPolicy; the lane keeping policy on this CrossSectionLink
     */
    public final LaneKeepingPolicy getLaneKeepingPolicy()
    {
        return this.laneKeepingPolicy;
    }

    /**
     * Find a cross section element with a specified id.
     * @param id String; the id to search for
     * @return CrossSectionElement; the cross section element with the given id, or null if not found
     */
    public final CrossSectionElement getCrossSectionElement(final String id)
    {
        for (CrossSectionElement cse : this.crossSectionElementList)
        {
            if (cse.getId().equals(id))
            {
                return cse;
            }
        }
        return null;
    }

    /**
     * Return a safe copy of the list of lanes of this CrossSectionLink.
     * @return List&lt;Lane&gt;; the list of lanes.
     */
    public final List<Lane> getLanes()
    {
        return this.lanes == null ? new ArrayList<>() : new ArrayList<>(this.lanes);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "CrossSectionLink [crossSectionElementList=" + this.crossSectionElementList + ", lanes=" + this.lanes
                + ", laneKeepingPolicy=" + this.laneKeepingPolicy + "]";
    }

}
