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
import org.opentrafficsim.core.network.OTSLink;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;

/**
 * A CrossSectionLink is a link with lanes where GTUs can possibly switch between lanes.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
     * Construction of a cross section link.
     * @param id the link id.
     * @param startNode start node (directional).
     * @param endNode end node (directional).
     * @param linkType the linktype
     * @param designLine the OTSLine3D design line of the Link
     * @param directionalityMap the directions (FORWARD, BACKWARD, BOTH, NONE) that GTUtypes can traverse this link
     * @param laneKeepingPolicy the policy to generally keep left, keep right, or keep lane
     */
    public CrossSectionLink(final String id, final OTSNode startNode, final OTSNode endNode, final LinkType linkType,
        final OTSLine3D designLine, final Map<GTUType, LongitudinalDirectionality> directionalityMap,
        final LaneKeepingPolicy laneKeepingPolicy)
    {
        super(id, startNode, endNode, linkType, designLine, directionalityMap);
        this.laneKeepingPolicy = laneKeepingPolicy;
    }

    /**
     * Construction of a link, with a general directionality for GTUType.ALL. Other directionalities can be added with the
     * method addDirectionality(...) later.
     * @param id the link id.
     * @param startNode start node (directional).
     * @param endNode end node (directional).
     * @param linkType the linktype
     * @param designLine the OTSLine3D design line of the Link
     * @param directionality the default directionality for all GTUs
     * @param laneKeepingPolicy the policy to generally keep left, keep right, or keep lane
     */
    public CrossSectionLink(final String id, final OTSNode startNode, final OTSNode endNode, final LinkType linkType,
        final OTSLine3D designLine, final LongitudinalDirectionality directionality,
        final LaneKeepingPolicy laneKeepingPolicy)
    {
        super(id, startNode, endNode, linkType, designLine, directionality);
        this.laneKeepingPolicy = laneKeepingPolicy;
    }

    /**
     * Construction of a link, on which no traffic is allowed after construction of the link. Directionality for GTUTypes can be
     * added with the method addDirectionality(...) later.
     * @param id the link id.
     * @param startNode start node (directional).
     * @param endNode end node (directional).
     * @param linkType the linktype
     * @param designLine the OTSLine3D design line of the Link
     * @param laneKeepingPolicy the policy to generally keep left, keep right, or keep lane
     */
    public CrossSectionLink(final String id, final OTSNode startNode, final OTSNode endNode, final LinkType linkType,
        final OTSLine3D designLine, final LaneKeepingPolicy laneKeepingPolicy)
    {
        this(id, startNode, endNode, linkType, designLine, new HashMap<GTUType, LongitudinalDirectionality>(),
            laneKeepingPolicy);
    }

    /**
     * Add a cross section element at the end of the list. <br>
     * <b>Note:</b> LEFT is seen as a positive lateral direction, RIGHT as a negative lateral direction.
     * @param cse the cross section element to add.
     */
    protected final void addCrossSectionElement(final CrossSectionElement cse)
    {
        this.crossSectionElementList.add(cse);
        if (cse instanceof Lane)
        {
            this.lanes.add((Lane) cse);
        }
    }

    /**
     * Add a cross section element at specified index in the list.<br>
     * <b>Note:</b> LEFT is seen as a positive lateral direction, RIGHT as a negative lateral direction.
     * @param index the location to insert the element.
     * @param cse the cross section element to add.
     */
    protected final void addCrossSectionElement(final CrossSectionElement cse, final int index)
    {
        this.crossSectionElementList.add(index, cse);
        if (cse instanceof Lane)
        {
            this.lanes.add((Lane) cse);
        }
    }

    /**
     * @return crossSectionElementList.
     */
    public final List<CrossSectionElement> getCrossSectionElementList()
    {
        return this.crossSectionElementList;
    }

    /**
     * @return laneKeepingPolicy
     */
    public final LaneKeepingPolicy getLaneKeepingPolicy()
    {
        return this.laneKeepingPolicy;
    }
    
    /**
     * @param id the cse.id to search for
     * @return the cross section element with the given id, or null if not found
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
     * @return lanes
     */
    public final List<Lane> getLanes()
    {
        return this.lanes;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "CrossSectionLink [crossSectionElementList=" + this.crossSectionElementList + ", lanes=" + this.lanes
                + ", laneKeepingPolicy=" + this.laneKeepingPolicy + "]";
    }
}
