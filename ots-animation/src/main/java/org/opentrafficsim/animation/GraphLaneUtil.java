package org.opentrafficsim.animation;

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
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.LinkPosition;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.draw.graphs.GraphCrossSection;
import org.opentrafficsim.draw.graphs.GraphPath;
import org.opentrafficsim.draw.graphs.GraphPath.Section;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LanePosition;
import org.opentrafficsim.road.network.lane.Shoulder;
import org.opentrafficsim.road.network.sampling.LaneDataRoad;

/**
 * Utilities to create {@code GraphPath}s and {@code GraphCrossSection}s for graphs, based on lanes.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
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
     * @param name path name
     * @param first first lane
     * @return path
     * @throws NetworkException when the lane does not have any set speed limit
     */
    public static GraphPath<LaneDataRoad> createPath(final String name, final Lane first) throws NetworkException
    {
        return createPath(name, first, null);
    }

    /**
     * Creates a path starting at the provided lane and moving downstream until a dead-end, split, or loop.
     * @param name path name
     * @param first first lane
     * @param last last lane
     * @return path
     * @throws NetworkException when the lane does not have any set speed limit
     */
    public static GraphPath<LaneDataRoad> createPath(final String name, final Lane first, final Lane last)
            throws NetworkException
    {
        Throw.whenNull(name, "Name may not be null.");
        Throw.whenNull(first, "First may not be null.");
        List<Section<LaneDataRoad>> sections = new ArrayList<>();
        Set<Lane> set = new LinkedHashSet<>();
        Lane lane = first;
        do
        {
            LaneDataRoad laneData = new LaneDataRoad(lane);
            List<LaneDataRoad> list = new ArrayList<>();
            list.add(laneData);
            Speed speed = lane.getLowestSpeedLimit();
            Length length = lane.getLength();
            sections.add(new Section<>(length, speed, list));
            set.add(lane);
            Set<Lane> nextLaneSet = lane.nextLanes(null);
            if (nextLaneSet.size() == 1 && !(nextLaneSet.iterator().next() instanceof Shoulder))
            {
                lane = nextLaneSet.iterator().next();
            }
        }
        while (lane != null && !set.contains(lane) && !lane.equals(last));
        return new GraphPath<>(name, sections);
    }

    /**
     * Creates a path starting at the provided lanes and moving downstream for as long as no lane finds a loop (on to any of the
     * lanes) and there's a unique link all lanes have downstream. The length and speed limit are taken from the first lane.
     * @param names lane names
     * @param first first lanes
     * @return path
     * @throws NetworkException when a lane does not have any set speed limit
     */
    public static GraphPath<LaneDataRoad> createPath(final List<String> names, final List<Lane> first) throws NetworkException
    {
        return createPath(names, first, Collections.emptyList());
    }

    /**
     * Creates a path starting at the provided lanes and moving downstream for as long as no lane finds a loop (on to any of the
     * lanes) and there's a unique link all lanes have downstream. The length is taken from the first lane. The speed is the
     * minimum of all lanes in each section.
     * @param names lane names
     * @param first first lanes
     * @param last last lanes
     * @return path
     * @throws NetworkException when a lane does not have any set speed limit
     */
    public static GraphPath<LaneDataRoad> createPath(final List<String> names, final List<Lane> first, final List<Lane> last)
            throws NetworkException
    {
        Throw.whenNull(names, "Names may not be null.");
        Throw.whenNull(first, "First may not be null.");
        Throw.when(names.size() != first.size(), IllegalArgumentException.class, "Size of 'names' and 'first' must be equal.");
        List<Section<LaneDataRoad>> sections = new ArrayList<>();
        Set<Lane> seenLanes = new LinkedHashSet<>();
        List<Lane> currentLanes = first;
        while (currentLanes != null && Collections.disjoint(seenLanes, currentLanes) && Collections.disjoint(seenLanes, last))
        {
            // create next section
            List<LaneDataRoad> sectionLanes = new ArrayList<>();
            Speed sectionSpeed = null;
            for (Lane lane : currentLanes)
            {
                if (lane == null)
                {
                    sectionLanes.add(null);
                }
                else
                {
                    sectionSpeed = sectionSpeed == null ? lane.getLowestSpeedLimit()
                            : Speed.min(sectionSpeed, lane.getLowestSpeedLimit());
                    sectionLanes.add(new LaneDataRoad(lane));
                    seenLanes.add(lane);
                }
            }
            Lane firstCurrentLane = currentLanes.stream().filter((l) -> l != null).findFirst().get();
            Length sectionLength = firstCurrentLane.getLength();
            sections.add(new Section<>(sectionLength, sectionSpeed, sectionLanes));

            // per link and then per lane, find the downstream lane
            Map<Link, List<Lane>> nextLinks = new LinkedHashMap<>();
            Link link = firstCurrentLane.getLink();
            for (Link nextLink : link.getEndNode().getLinks())
            {
                if (!link.equals(nextLink)) // only other links
                {
                    List<Lane> nextLanes = new ArrayList<>();
                    for (Lane currentLane : currentLanes)
                    {
                        Set<Lane> nextLanesOfLane = currentLane.nextLanes(null);
                        int n = 0;
                        for (Lane nextLane : nextLanesOfLane)
                        {
                            if (nextLane.getLink().equals(nextLink))
                            {
                                n++;
                                nextLanes.add(nextLane);
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
                    if (nextLanes.size() == currentLanes.size())
                    {
                        nextLinks.put(nextLink, nextLanes);
                    }
                }
            }
            // in case there are multiple downstream links, remove all links for which some lanes had no downstream lane
            if (nextLinks.size() > 1)
            {
                Iterator<List<Lane>> it = nextLinks.values().iterator();
                while (it.hasNext())
                {
                    if (it.next().contains(null))
                    {
                        it.remove();
                    }
                }
            }
            if (nextLinks.size() == 1)
            {
                currentLanes = nextLinks.values().iterator().next();
            }
            else
            {
                currentLanes = null;
            }
        }
        return new GraphPath<>(names, sections);
    }

    /**
     * Creates a single-lane path.
     * @param name name
     * @param lane lane
     * @return path
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
     * @param name name
     * @param lanePosition lane position
     * @return cross section
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
        positions.add(lanePosition.position());
        list.add(new LaneDataRoad(lanePosition.lane()));
        Speed speed = lanePosition.lane().getLowestSpeedLimit();
        return createCrossSection(names, list, positions, speed);
    }

    /**
     * Creates a cross section at the provided link and position.
     * @param names lane names
     * @param linkPosition link position
     * @return cross section
     * @throws NetworkException when a lane does not have any set speed limit
     */
    public static GraphCrossSection<LaneDataRoad> createCrossSection(final List<String> names, final LinkPosition linkPosition)
            throws NetworkException
    {
        Throw.whenNull(names, "Names may not be null.");
        Throw.whenNull(linkPosition, "Link position may not be null.");
        Throw.when(!(linkPosition.link() instanceof CrossSectionLink), IllegalArgumentException.class,
                "The link is not a CrossEctionLink.");
        List<Lane> lanes = ((CrossSectionLink) linkPosition.link()).getLanes();
        Throw.when(names.size() != lanes.size(), IllegalArgumentException.class,
                "Size of 'names' not equal to the number of lanes.");
        Collections.sort(lanes, new Comparator<Lane>()
        {
            /** {@ingeritDoc} */
            @Override
            public int compare(final Lane o1, final Lane o2)
            {
                return o1.getOffsetAtBegin().compareTo(o2.getOffsetAtEnd());
            }

        });
        List<LaneDataRoad> list = new ArrayList<>();
        List<Length> positions = new ArrayList<>();
        Speed speed = null;
        for (Lane lane : lanes)
        {
            speed = speed == null ? lane.getLowestSpeedLimit() : Speed.min(speed, lane.getLowestSpeedLimit());
            list.add(new LaneDataRoad(lane));
            positions.add(lane.getLength().times(linkPosition.fractionalLongitudinalPosition()));
        }
        return createCrossSection(names, list, positions, speed);
    }

    /**
     * Creates a cross section.
     * @param names names
     * @param lanes lanes
     * @param positions positions
     * @param speed speed
     * @return cross section
     */
    public static GraphCrossSection<LaneDataRoad> createCrossSection(final List<String> names, final List<LaneDataRoad> lanes,
            final List<Length> positions, final Speed speed)
    {
        Section<LaneDataRoad> section = new Section<>(lanes.get(0).getLength(), speed, lanes);
        return new GraphCrossSection<>(names, section, positions);
    }

}
