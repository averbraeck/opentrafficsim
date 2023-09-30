package org.opentrafficsim.core.animation.network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.exceptions.Throw;
import org.djutils.immutablecollections.ImmutableSet;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.LinkPosition;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.draw.graphs.GraphCrossSection;
import org.opentrafficsim.draw.graphs.GraphPath;
import org.opentrafficsim.draw.graphs.GraphPath.Section;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LanePosition;
import org.opentrafficsim.road.network.sampling.LaneDataRoad;

/**
 * Utilities to create {@code GraphPath}s and {@code GraphCrossSection}s for graphs, based on lanes.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public final class GraphLaneUtil
{

    /**
     * Constructor.
     */
    private GraphLaneUtil()
    {
        //
    }

    /**
     * Creates a path starting at the provided lane and moving downstream until a dead-end, split, or loop.
     * @param name String; path name
     * @param first Lane; first lane
     * @return GraphPath&lt;LaneDataRoad&gt; path
     * @throws NetworkException when the lane does not have any set speed limit
     */
    public static GraphPath<LaneDataRoad> createPath(final String name, final Lane first) throws NetworkException
    {
        Throw.whenNull(name, "Name may not be null.");
        Throw.whenNull(first, "First may not be null.");
        List<Section<LaneDataRoad>> sections = new ArrayList<>();
        Set<Lane> set = new LinkedHashSet<>();
        Lane lane = first;
        while (lane != null && !set.contains(lane))
        {
            LaneDataRoad laneData = new LaneDataRoad(lane);
            List<LaneDataRoad> list = new ArrayList<>();
            list.add(laneData);
            Speed speed = lane.getLowestSpeedLimit();
            Length length = lane.getLength();
            sections.add(new Section<>(length, speed, list));
            set.add(lane);
            Set<Lane> nextLaneSet = lane.nextLanes(null);
            if (nextLaneSet.size() == 1)
            {
                lane = nextLaneSet.iterator().next();
            }
        }
        return new GraphPath<>(name, sections);
    }

    /**
     * Creates a path starting at the provided lanes and moving downstream for as long as no lane finds a loop (on to any of the
     * lanes) and there's a unique link all lanes have downstream. The length and speed limit are taken from the first lane.
     * @param names List&lt;String&gt;; lane names
     * @param first List&lt;Lane&gt;; first lanes
     * @return GraphPath&lt;LaneDataRoad&gt; path
     * @throws NetworkException when a lane does not have any set speed limit
     */
    public static GraphPath<LaneDataRoad> createPath(final List<String> names, final List<Lane> first) throws NetworkException
    {
        Throw.whenNull(names, "Names may not be null.");
        Throw.whenNull(first, "First may not be null.");
        Throw.when(names.size() != first.size(), IllegalArgumentException.class, "Size of 'names' and 'first' must be equal.");
        List<Section<LaneDataRoad>> sections = new ArrayList<>();
        Set<Lane> set = new LinkedHashSet<>();
        List<Lane> lanes = first;
        while (lanes != null && Collections.disjoint(set, lanes))
        {
            List<LaneDataRoad> list = new ArrayList<>();
            Speed speed = null;
            for (Lane lane : lanes)
            {
                if (lane == null)
                {
                    list.add(null);
                    continue;
                }
                speed = speed == null ? lane.getLowestSpeedLimit() : Speed.min(speed, lane.getLowestSpeedLimit());
                list.add(new LaneDataRoad(lane));
            }
            Speed finalSpeed = speed;
            Lane firstNextLane = null;
            for (Lane lane : lanes)
            {
                if (lane != null)
                {
                    firstNextLane = lane;
                    continue;
                }
            }
            Length length = firstNextLane.getLength();
            sections.add(new Section<>(length, finalSpeed, list));
            set.addAll(lanes);
            // per link and then per lane, find the downstream lane
            Map<Link, List<Lane>> linkMap = new LinkedHashMap<>();
            Link link = firstNextLane.getLink();
            ImmutableSet<Link> links = link.getEndNode().getLinks();
            for (Link nextLink : links)
            {
                if (!link.equals(nextLink))
                {
                    List<Lane> nextLanes = new ArrayList<>();
                    for (Lane nextLane : lanes)
                    {
                        Set<Lane> nextLaneSet = nextLane.nextLanes(null);
                        int n = 0;
                        for (Lane nl : nextLaneSet)
                        {
                            if (nl.getLink().equals(nextLink))
                            {
                                n++;
                                nextLanes.add(nl);
                            }
                        }
                        if (n > 1)
                        {
                            // multiple downstream lanes of this lane go to this link, this is not allowed
                            nextLanes.clear();
                            break;
                        }
                        else if (n == 0)
                        {
                            nextLanes.add(null);
                        }
                    }
                    if (nextLanes.size() == lanes.size())
                    {
                        linkMap.put(nextLink, nextLanes);
                    }
                }
            }
            // in case there are multiple downstream links, remove all links for which some lanes had no downstream lane
            if (linkMap.size() > 1)
            {
                Iterator<List<Lane>> it = linkMap.values().iterator();
                while (it.hasNext())
                {
                    if (it.next().contains(null))
                    {
                        it.remove();
                    }
                }
            }
            if (linkMap.size() == 1)
            {
                lanes = linkMap.values().iterator().next();
            }
            else
            {
                lanes = null;
            }
        }
        return new GraphPath<>(names, sections);
    }

    /**
     * Creates a single-lane path.
     * @param name String; name
     * @param lane Lane; lane
     * @return GraphPath&lt;LaneDataRoad&gt; path
     * @throws NetworkException when a lane does not have any set speed limit
     */
    public static GraphPath<LaneDataRoad> createSingleLanePath(final String name, final Lane lane) throws NetworkException
    {
        List<LaneDataRoad> lanes = new ArrayList<>();
        lanes.add(new LaneDataRoad(lane));
        List<Section<LaneDataRoad>> sections = new ArrayList<>();
        Speed speed = lane.getLowestSpeedLimit();
        sections.add(new Section<>(lane.getLength(), speed, lanes));
        return new GraphPath<>(name, sections);
    }

    /**
     * Creates a cross section at the provided lane and position.
     * @param name String; name
     * @param lanePosition LanePosition; lane position
     * @return GraphCrossSection&lt;LaneDataRoad&gt; cross section
     * @throws NetworkException when the lane does not have any set speed limit
     */
    public static GraphCrossSection<LaneDataRoad> createCrossSection(final String name, final LanePosition lanePosition)
            throws NetworkException
    {
        Throw.whenNull(name, "Name may not be null.");
        Throw.whenNull(lanePosition, "Lane position may not be null.");
        List<LaneDataRoad> list = new ArrayList<>();
        List<String> names = new ArrayList<>();
        List<Length> positions = new ArrayList<>();
        names.add(name);
        positions.add(lanePosition.getPosition());
        list.add(new LaneDataRoad(lanePosition.getLane()));
        Speed speed = lanePosition.getLane().getLowestSpeedLimit();
        return createCrossSection(names, list, positions, speed);
    }

    /**
     * Creates a cross section at the provided link and position.
     * @param names List&lt;String&gt;; lane names
     * @param linkPosition LinkPosition; link position
     * @return GraphCrossSection&lt;LaneDataRoad&gt; cross section
     * @throws NetworkException when a lane does not have any set speed limit
     */
    public static GraphCrossSection<LaneDataRoad> createCrossSection(final List<String> names, final LinkPosition linkPosition)
            throws NetworkException
    {
        Throw.whenNull(names, "Names may not be null.");
        Throw.whenNull(linkPosition, "Link position may not be null.");
        Throw.when(!(linkPosition.getLink() instanceof CrossSectionLink), IllegalArgumentException.class,
                "The link is not a CrossEctionLink.");
        List<Lane> lanes = ((CrossSectionLink) linkPosition.getLink()).getLanes();
        Throw.when(names.size() != lanes.size(), IllegalArgumentException.class,
                "Size of 'names' not equal to the number of lanes.");
        Collections.sort(lanes, new Comparator<Lane>()
        {
            /** {@ingeritDoc} */
            @Override
            public int compare(final Lane o1, final Lane o2)
            {
                return o1.getDesignLineOffsetAtBegin().compareTo(o2.getDesignLineOffsetAtEnd());
            }

        });
        List<LaneDataRoad> list = new ArrayList<>();
        List<Length> positions = new ArrayList<>();
        Speed speed = null;
        for (Lane lane : lanes)
        {
            speed = speed == null ? lane.getLowestSpeedLimit() : Speed.min(speed, lane.getLowestSpeedLimit());
            list.add(new LaneDataRoad(lane));
            positions.add(lane.getLength().times(linkPosition.getFractionalLongitudinalPosition()));
        }
        return createCrossSection(names, list, positions, speed);
    }

    /**
     * Creates a cross section.
     * @param names List&lt;String&gt;;; names
     * @param lanes List&lt;LaneDataRoad&gt;;; lanes
     * @param positions List&lt;Length&gt;; positions
     * @param speed Speed; speed
     * @return GraphCrossSection&lt;LaneDataRoad&gt;; cross section
     */
    public static GraphCrossSection<LaneDataRoad> createCrossSection(final List<String> names, final List<LaneDataRoad> lanes,
            final List<Length> positions, final Speed speed)
    {
        Section<LaneDataRoad> section = new Section<>(lanes.get(0).getLength(), speed, lanes);
        return new GraphCrossSection<>(names, section, positions);
    }

}
