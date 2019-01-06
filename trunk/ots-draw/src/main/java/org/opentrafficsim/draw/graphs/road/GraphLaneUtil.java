package org.opentrafficsim.draw.graphs.road;

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
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.network.DirectedLinkPosition;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.draw.graphs.GraphCrossSection;
import org.opentrafficsim.draw.graphs.GraphPath;
import org.opentrafficsim.draw.graphs.GraphPath.Section;
import org.opentrafficsim.kpi.sampling.KpiGtuDirectionality;
import org.opentrafficsim.kpi.sampling.KpiLaneDirection;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneDirection;
import org.opentrafficsim.road.network.sampling.LaneData;

/**
 * Utilities to create {@code GraphPath}s and {@GraphCrossSection}s for graphs, based on lanes.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 19 okt. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
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
     * @param first LaneDirection; first lane
     * @return GraphPath&lt;KpiLaneDirection&gt; path
     * @throws NetworkException when the lane does not have any set speed limit
     */
    public static GraphPath<KpiLaneDirection> createPath(final String name, final LaneDirection first) throws NetworkException
    {
        Throw.whenNull(name, "Name may not be null.");
        Throw.whenNull(first, "First may not be null.");
        List<Section<KpiLaneDirection>> sections = new ArrayList<>();
        Set<LaneDirection> set = new LinkedHashSet<>();
        LaneDirection lane = first;
        while (lane != null && !set.contains(lane))
        {
            KpiLaneDirection kpiLaneDirection = new KpiLaneDirection(new LaneData(lane.getLane()),
                    lane.getDirection().isPlus() ? KpiGtuDirectionality.DIR_PLUS : KpiGtuDirectionality.DIR_MINUS);
            List<KpiLaneDirection> list = new ArrayList<>();
            list.add(kpiLaneDirection);
            Speed speed = lane.getLane().getLowestSpeedLimit();
            Length length = lane.getLength();
            sections.add(new Section<KpiLaneDirection>()
            {
                /** {@inheritDoc} */
                @Override
                public Iterator<KpiLaneDirection> iterator()
                {
                    return list.iterator();
                }

                /** {@inheritDoc} */
                @Override
                public Length getLength()
                {
                    return length;
                }

                /** {@inheritDoc} */
                @Override
                public Speed getSpeedLimit()
                {
                    return speed;
                }

                /** {@inheritDoc} */
                @Override
                public KpiLaneDirection getSource(final int series)
                {
                    return kpiLaneDirection;
                }
            });
            set.add(lane);
            Map<Lane, GTUDirectionality> map = lane.getLane().downstreamLanes(lane.getDirection(), null);
            if (map.size() == 1)
            {
                Map.Entry<Lane, GTUDirectionality> entry = map.entrySet().iterator().next();
                lane = new LaneDirection(entry.getKey(), entry.getValue());
            }
        }
        return new GraphPath<>(name, sections);
    }

    /**
     * Creates a path starting at the provided lanes and moving downstream for as long as no lane finds a loop (on to any of the
     * lanes) and there's a unique link all lanes have downstream. The length and speed limit are taken from the first lane.
     * @param names List&lt;String&gt;; lane names
     * @param first List&lt;LaneDirection&gt;; first lanes
     * @return GraphPath&lt;KpiLaneDirection&gt; path
     * @throws NetworkException when a lane does not have any set speed limit
     */
    public static GraphPath<KpiLaneDirection> createPath(final List<String> names, final List<LaneDirection> first)
            throws NetworkException
    {
        Throw.whenNull(names, "Names may not be null.");
        Throw.whenNull(first, "First may not be null.");
        Throw.when(names.size() != first.size(), IllegalArgumentException.class, "Size of 'names' and 'first' must be equal.");
        List<Section<KpiLaneDirection>> sections = new ArrayList<>();
        Set<LaneDirection> set = new LinkedHashSet<>();
        List<LaneDirection> lanes = first;
        while (lanes != null && Collections.disjoint(set, lanes))
        {
            List<KpiLaneDirection> list = new ArrayList<>();
            Speed speed = null;
            for (LaneDirection lane : lanes)
            {
                speed = speed == null ? lane.getLane().getLowestSpeedLimit()
                        : Speed.min(speed, lane.getLane().getLowestSpeedLimit());
                KpiLaneDirection kpiLaneDirection = new KpiLaneDirection(new LaneData(lane.getLane()),
                        lane.getDirection().isPlus() ? KpiGtuDirectionality.DIR_PLUS : KpiGtuDirectionality.DIR_MINUS);
                list.add(kpiLaneDirection);
            }
            Speed finalSpeed = speed;
            Length length = lanes.get(0).getLane().getLength();
            sections.add(new Section<KpiLaneDirection>()
            {
                /** {@inheritDoc} */
                @Override
                public Iterator<KpiLaneDirection> iterator()
                {
                    return list.iterator();
                }

                /** {@inheritDoc} */
                @Override
                public Length getLength()
                {
                    return length;
                }

                /** {@inheritDoc} */
                @Override
                public Speed getSpeedLimit()
                {
                    return finalSpeed;
                }

                /** {@inheritDoc} */
                @Override
                public KpiLaneDirection getSource(final int series)
                {
                    return list.get(series);
                }
            });
            set.addAll(lanes);
            // per link and then per lane, find the downstream lane
            Map<Link, List<LaneDirection>> linkMap = new LinkedHashMap<>();
            Link link = lanes.get(0).getLane().getParentLink();
            ImmutableSet<Link> links =
                    (lanes.get(0).getDirection().isPlus() ? link.getEndNode() : link.getStartNode()).getLinks();
            for (Link nextLink : links)
            {
                if (!link.equals(nextLink))
                {
                    List<LaneDirection> nextLanes = new ArrayList<>();
                    for (LaneDirection laneDir : lanes)
                    {
                        Map<Lane, GTUDirectionality> map = laneDir.getLane().downstreamLanes(laneDir.getDirection(), null);
                        int n = 0;
                        for (Map.Entry<Lane, GTUDirectionality> entry : map.entrySet())
                        {
                            if (entry.getKey().getParentLink().equals(nextLink))
                            {
                                n++;
                                nextLanes.add(new LaneDirection(entry.getKey(), entry.getValue()));
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
                            nextLanes.addAll(null);
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
                Iterator<List<LaneDirection>> it = linkMap.values().iterator();
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
     * @param lane LaneDirection; lane
     * @return GraphPath&lt;KpiLaneDirection&gt; path
     * @throws NetworkException when a lane does not have any set speed limit
     */
    public static GraphPath<KpiLaneDirection> createSingleLanePath(final String name, final LaneDirection lane)
            throws NetworkException
    {
        List<KpiLaneDirection> lanes = new ArrayList<>();
        lanes.add(new KpiLaneDirection(new LaneData(lane.getLane()),
                lane.getDirection().isPlus() ? KpiGtuDirectionality.DIR_PLUS : KpiGtuDirectionality.DIR_MINUS));
        List<Section<KpiLaneDirection>> sections = new ArrayList<>();
        Speed speed = lane.getLane().getLowestSpeedLimit();
        sections.add(new Section<KpiLaneDirection>()
        {

            /** {@inheritDoc} */
            @Override
            public Iterator<KpiLaneDirection> iterator()
            {
                return lanes.iterator();
            }

            /** {@inheritDoc} */
            @Override
            public Length getLength()
            {
                return lane.getLength();
            }

            /** {@inheritDoc} */
            @Override
            public Speed getSpeedLimit()
            {
                return speed;
            }

            /** {@inheritDoc} */
            @Override
            public KpiLaneDirection getSource(final int series)
            {
                return lanes.get(0);
            }

        });
        return new GraphPath<>(name, sections);
    }

    /**
     * Creates a cross section at the provided lane and position.
     * @param name String; name
     * @param lanePosition DirectedLanePosition; lane position
     * @return GraphCrossSection&lt;KpiLaneDirection&gt; cross section
     * @throws NetworkException when the lane does not have any set speed limit
     */
    public static GraphCrossSection<KpiLaneDirection> createCrossSection(final String name,
            final DirectedLanePosition lanePosition) throws NetworkException
    {
        Throw.whenNull(name, "Name may not be null.");
        Throw.whenNull(lanePosition, "Lane position may not be null.");
        List<KpiLaneDirection> list = new ArrayList<>();
        List<String> names = new ArrayList<>();
        List<Length> positions = new ArrayList<>();
        names.add(name);
        positions.add(lanePosition.getPosition());
        list.add(new KpiLaneDirection(new LaneData(lanePosition.getLane()),
                lanePosition.getGtuDirection().isPlus() ? KpiGtuDirectionality.DIR_PLUS : KpiGtuDirectionality.DIR_MINUS));
        Speed speed = lanePosition.getLane().getLowestSpeedLimit();
        return createCrossSection(names, list, positions, speed);
    }

    /**
     * Creates a cross section at the provided link and position.
     * @param names List&lt;String&gt;; lane names
     * @param linkPosition DirectedLinkPosition; link position
     * @return GraphCrossSection&lt;KpiLaneDirection&gt; cross section
     * @throws NetworkException when a lane does not have any set speed limit
     */
    public static GraphCrossSection<KpiLaneDirection> createCrossSection(final List<String> names,
            final DirectedLinkPosition linkPosition) throws NetworkException
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
                int comp = o1.getDesignLineOffsetAtBegin().compareTo(o2.getDesignLineOffsetAtEnd());
                return linkPosition.getDirection().isPlus() ? comp : -comp;
            }

        });
        List<KpiLaneDirection> list = new ArrayList<>();
        List<Length> positions = new ArrayList<>();
        Speed speed = null;
        for (Lane lane : lanes)
        {
            speed = speed == null ? lane.getLowestSpeedLimit() : Speed.min(speed, lane.getLowestSpeedLimit());
            list.add(new KpiLaneDirection(new LaneData(lane),
                    linkPosition.getDirection().isPlus() ? KpiGtuDirectionality.DIR_PLUS : KpiGtuDirectionality.DIR_MINUS));
            positions.add(lane.getLength().multiplyBy(linkPosition.getFractionalLongitudinalPosition()));
        }
        return createCrossSection(names, list, positions, speed);
    }

    /**
     * Creates a cross section.
     * @param names List&lt;String&gt;;; names
     * @param lanes List&lt;KpiLaneDirection&gt;;; lanes
     * @param positions List&lt;Length&gt;; positions
     * @param speed Speed; speed
     * @return GraphCrossSection&lt;KpiLaneDirection&gt;; cross section
     */
    public static GraphCrossSection<KpiLaneDirection> createCrossSection(final List<String> names,
            final List<KpiLaneDirection> lanes, final List<Length> positions, final Speed speed)
    {
        Section<KpiLaneDirection> section = new Section<KpiLaneDirection>()
        {
            /** {@inheritDoc} */
            @Override
            public Iterator<KpiLaneDirection> iterator()
            {
                return lanes.iterator();
            }

            /** {@inheritDoc} */
            @Override
            public Length getLength()
            {
                return lanes.get(0).getLaneData().getLength();
            }

            /** {@inheritDoc} */
            @Override
            public Speed getSpeedLimit()
            {
                return speed;
            }

            /** {@inheritDoc} */
            @Override
            public KpiLaneDirection getSource(final int series)
            {
                return lanes.get(series);
            }
        };
        return new GraphCrossSection<>(names, section, positions);
    }

}
