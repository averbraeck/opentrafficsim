package org.opentrafficsim.road.gtu.lane.perception.structure;

import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Try;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.structure.NavigatingIterable.Entry;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LanePosition;
import org.opentrafficsim.road.network.lane.object.LaneBasedObject;

/**
 * The lane structure provides a way to see the world for a lane based model.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class LaneStructure
{

    /** GTU. */
    private final LaneBasedGtu egoGtu;

    /** Length to build lane structure upstream of GTU, or upstream relative to a downstream merge. */
    private Length upstream;

    /** Length to build lane structure downstream of GTU. */
    private Length downstream;

    /** Time at which the structure was updated. */
    private Duration updated = null;

    /** Cross section of lane records at different relative lanes. */
    private final Map<RelativeLane, Set<LaneRecord>> crossSection = new LinkedHashMap<>();

    /** Cross section of lane records directly found laterally from the root. */
    private final Map<RelativeLane, LaneRecord> rootCrossSection = new LinkedHashMap<>();

    /**
     * Constructor.
     * @param gtu the GTU.
     * @param upstream guaranteed distance within which objects are found upstream of the GTU, or upstream of downstream merge.
     * @param downstream guaranteed distance within which objects are found downstream of the GTU.
     */
    public LaneStructure(final LaneBasedGtu gtu, final Length upstream, final Length downstream)
    {
        this.egoGtu = gtu;
        this.upstream = upstream;
        this.downstream = downstream;
    }

    /**
     * Returns an iterator over objects perceived on a relative lane, ordered close to far. This can be objects on different
     * roads, e.g. from the main line on the right-most lane, the right-hand relative lane can give objects upstream of two
     * on-ramps that are very close by, or even the shoulder. Objects that are partially downstream are also included.
     * @param <T> type of {@code LaneBasedObject}.
     * @param relativeLane lane.
     * @param clazz class of lane-based object type.
     * @param position RelativePosition.Type; position relative to which objects are found and distances are given.
     * @param onRoute whether the objects have to be on-route.
     * @return iterator over objects.
     */
    public <T extends LaneBasedObject> Iterable<Entry<T>> getDownstreamObjects(final RelativeLane relativeLane,
            final Class<T> clazz, final RelativePosition.Type position, final boolean onRoute)
    {
        update();
        Length dx = LaneStructure.this.egoGtu.getRelativePositions().get(position).dx();
        return new NavigatingIterable<>(clazz, this.downstream,
                start((record) -> startDownstream(record, position), relativeLane), (record) ->
                {
                    // this navigator only includes records of lanes on the route
                    Set<LaneRecord> set = record.getNext();
                    set.removeIf((r) -> onRoute
                            && !r.isOnRoute(LaneStructure.this.egoGtu.getStrategicalPlanner().getRoute().orElse(null)));
                    return set;
                }, (record) ->
                {
                    // this navigator selects all objects fully or partially downstream
                    List<LaneBasedObject> list = record.getLane().getLaneBasedObjects();
                    if (list.isEmpty())
                    {
                        return list;
                    }
                    Length pos = record.getStartDistance().neg().plus(dx);
                    int from = 0;
                    while (from < list.size()
                            && list.get(from).getLongitudinalPosition().plus(list.get(from).getLength()).lt(pos))
                    {
                        from++;
                    }
                    if (from > list.size() - 1)
                    {
                        return Collections.emptyList();
                    }
                    return list.subList(from, list.size());
                }, (t, r) -> r.getStartDistance().plus(t.getLongitudinalPosition()).minus(dx));
    }

    /**
     * Returns an iterator over objects perceived on a relative lane, ordered close to far. This can be objects on different
     * roads, e.g. from the main line on the right-most lane, the right-hand relative lane can give objects upstream of two
     * on-ramps that are very close by, or even the shoulder. Objects that are partially upstream are also included.
     * @param <T> type of {@code LaneBasedObject}.
     * @param relativeLane lane.
     * @param clazz class of lane-based object type.
     * @param position RelativePosition.Type; position relative to which objects are found and distances are given.
     * @return iterator over objects.
     */
    public <T extends LaneBasedObject> Iterable<Entry<T>> getUpstreamObjects(final RelativeLane relativeLane,
            final Class<T> clazz, final RelativePosition.Type position)
    {
        update();
        Length dx = LaneStructure.this.egoGtu.getRelativePositions().get(position).dx();
        return new NavigatingIterable<>(clazz, this.upstream, start((record) -> startUpstream(record, position), relativeLane),
                (record) ->
                {
                    // this navigator combines the upstream and lateral records
                    Set<LaneRecord> set = new LinkedHashSet<>(record.getPrev());
                    set.addAll(record.lateral());
                    return set;
                }, (record) ->
                {
                    // this lister reverses the list
                    List<LaneBasedObject> list = record.getLane().getLaneBasedObjects();
                    if (list.isEmpty())
                    {
                        return list;
                    }
                    Length pos = record.getStartDistance().neg().plus(dx);
                    int to = list.size();
                    while (to >= 0 && list.get(to).getLongitudinalPosition().gt(pos))
                    {
                        to--;
                    }
                    if (to < 0)
                    {
                        return Collections.emptyList();
                    }
                    list = list.subList(0, to);
                    Collections.reverse(list);
                    return list;
                }, (t, r) -> r.getStartDistance().plus(t.getLongitudinalPosition()).plus(dx).neg());
    }

    /**
     * Returns an iterator over GTUs perceived on a relative lane, ordered close to far. This can be GTUs on different roads,
     * e.g. from the main line on the right-most lane, the right-hand relative lane can give objects upstream of two on-ramps
     * that are very close by, or even the shoulder. When from a lane on the route, a downstream lane is not on the route, GTUs
     * on the downstream lane are not included. A split conflict should deal with possible GTUs there.
     * @param relativeLane lane.
     * @param egoPosition position of ego GTU relative to which objects are found.
     * @param otherPosition position of other GTU that must be downstream of egoPosition.
     * @param egoDistancePosition position of ego GTU from which the distance is determined.
     * @param otherDistancePosition position of other GTU to which the distance is determined.
     * @return iterator over GTUs.
     */
    public Iterable<Entry<LaneBasedGtu>> getDownstreamGtus(final RelativeLane relativeLane,
            final RelativePosition.Type egoPosition, final RelativePosition.Type otherPosition,
            final RelativePosition.Type egoDistancePosition, final RelativePosition.Type otherDistancePosition)
    {
        update();
        Length dx = LaneStructure.this.egoGtu.getRelativePositions().get(egoPosition).dx();
        Length dxDistance = LaneStructure.this.egoGtu.getRelativePositions().get(egoDistancePosition).dx();
        Optional<Route> route = LaneStructure.this.egoGtu.getStrategicalPlanner().getRoute();
        return new NavigatingIterable<>(LaneBasedGtu.class, this.downstream,
                start((record) -> startDownstream(record, egoPosition), relativeLane), (record) ->
                {
                    // this navigator ignores downstream lanes that are not on the route, if the current record is on the route
                    Set<LaneRecord> next = new LinkedHashSet<>(record.getNext());
                    if (route.isPresent() && record.getLane().getLink().getEndNode().getLinks().size() > 2
                            && route.get().containsLink(record.getLane().getLink()))
                    {
                        Iterator<LaneRecord> it = next.iterator();
                        while (it.hasNext())
                        {
                            LaneRecord down = it.next();
                            if (!route.get().containsLink(down.getLane().getLink()))
                            {
                                it.remove();
                            }
                        }
                    }
                    return next;
                }, (record) ->
                {
                    // this lister finds the relevant sublist of GTUs
                    List<LaneBasedGtu> gtus = record.getLane().getGtuList().toList();
                    if (gtus.isEmpty())
                    {
                        return gtus;
                    }
                    int from = 0;
                    Length pos = Length.max(record.getStartDistance().neg().plus(dx), Length.ZERO);
                    while (from < gtus.size() && (position(gtus.get(from), record, otherPosition).lt(pos)
                            || gtus.get(from).getId().equals(this.egoGtu.getId())))
                    {
                        from++;
                    }
                    int to = gtus.size() - 1;
                    while (to >= 0 && gtus.get(to).getId().equals(this.egoGtu.getId()))
                    {
                        to--;
                    }
                    if (from > to)
                    {
                        return Collections.emptyList();
                    }
                    if (from > 0 || to < gtus.size() - 1)
                    {
                        gtus = gtus.subList(from, to + 1);
                    }
                    return gtus;
                }, (t, r) -> r.getStartDistance().plus(position(t, r, otherDistancePosition)).minus(dxDistance));
    }

    /**
     * Returns an iterator over GTUs perceived on a relative lane, ordered close to far. This can be GTUs on different roads,
     * e.g. from the main line on the right-most lane, the right-hand relative lane can give objects upstream of two on-ramps
     * that are very close by, or even the shoulder.
     * @param relativeLane lane.
     * @param egoPosition position of ego GTU relative to which objects are found.
     * @param otherPosition position of other GTU that must be upstream of egoPosition.
     * @param egoDistancePosition position of ego GTU from which the distance is determined.
     * @param otherDistancePosition position of other GTU to which the distance is determined.
     * @return iterator over GTUs.
     */
    public Iterable<Entry<LaneBasedGtu>> getUpstreamGtus(final RelativeLane relativeLane,
            final RelativePosition.Type egoPosition, final RelativePosition.Type otherPosition,
            final RelativePosition.Type egoDistancePosition, final RelativePosition.Type otherDistancePosition)
    {
        update();
        Length dx = LaneStructure.this.egoGtu.getRelativePositions().get(egoPosition).dx();
        Length dxDistance = LaneStructure.this.egoGtu.getRelativePositions().get(egoDistancePosition).dx();
        return new NavigatingIterable<>(LaneBasedGtu.class, this.upstream,
                start((record) -> startUpstream(record, egoPosition), relativeLane), (record) ->
                {
                    // this navigator combines the upstream and lateral records
                    Set<LaneRecord> set = new LinkedHashSet<>(record.getPrev());
                    set.addAll(record.lateral());
                    return set;
                }, (record) ->
                {
                    // this lister finds the relevant sublist of GTUs and reverses it
                    List<LaneBasedGtu> gtus = record.getLane().getGtuList().toList();
                    if (gtus.isEmpty())
                    {
                        return gtus;
                    }
                    int from = 0;
                    while (from < gtus.size() && gtus.get(from).getId().equals(this.egoGtu.getId()))
                    {
                        from++;
                    }
                    int to = gtus.size() - 1;
                    Length pos = Length.min(record.getStartDistance().neg().plus(dx), record.getLength());
                    while (to >= 0 && (position(gtus.get(to), record, otherPosition).gt(pos)
                            || gtus.get(to).getId().equals(this.egoGtu.getId())))
                    {
                        to--;
                    }
                    if (from > to)
                    {
                        return Collections.emptyList();
                    }
                    if (from > 0 || to < gtus.size() - 1)
                    {
                        gtus = gtus.subList(from, to + 1);
                    }
                    Collections.reverse(gtus);
                    return gtus;
                }, (t, r) -> dxDistance.minus(r.getStartDistance().plus(position(t, r, otherDistancePosition))));
    }

    /**
     * Returns an iterator over GTUs perceived on a relative lane, ordered close to far. This can be GTUs on different roads,
     * e.g. from the main line on the right-most lane, the right-hand relative lane can give objects upstream of two on-ramps
     * that are very close by, or even the shoulder. This function differs from {@code getDownstreamGtus()} in that it will halt
     * further searching on on branch it finds a GTU on.
     * @param relativeLane lane.
     * @param egoPosition position of ego GTU relative to which objects are found.
     * @param otherPosition position of other GTU that must be downstream of egoPosition.
     * @param egoDistancePosition position of ego GTU from which the distance is determined.
     * @param otherDistancePosition position of other GTU to which the distance is determined.
     * @return iterator over GTUs.
     */
    public Iterable<Entry<LaneBasedGtu>> getFirstDownstreamGtus(final RelativeLane relativeLane,
            final RelativePosition.Type egoPosition, final RelativePosition.Type otherPosition,
            final RelativePosition.Type egoDistancePosition, final RelativePosition.Type otherDistancePosition)
    {
        update();
        Length dx = LaneStructure.this.egoGtu.getRelativePositions().get(egoPosition).dx();
        Length dxDistance = LaneStructure.this.egoGtu.getRelativePositions().get(egoDistancePosition).dx();
        return new NavigatingIterable<>(LaneBasedGtu.class, this.downstream,
                start((record) -> startDownstream(record, egoPosition), relativeLane), (record) ->
                {
                    // this navigator only returns records when there are no GTUs on the lane
                    return Try
                            .assign(() -> record.getLane().getGtuAhead(record.getStartDistance().neg().plus(dx), otherPosition,
                                    record.getLane().getNetwork().getSimulator().getSimulatorTime()), "Problem with GTU")
                            .isEmpty() ? record.getNext() : new LinkedHashSet<>();
                }, (record) ->
                {
                    // this lister finds the first GTU and returns it as the only GTU in the list
                    Optional<LaneBasedGtu> down =
                            Try.assign(
                                    () -> record.getLane().getGtuAhead(record.getStartDistance().neg().plus(dx), otherPosition,
                                            record.getLane().getNetwork().getSimulator().getSimulatorTime()),
                                    "Problem with GTU");
                    return down.isEmpty() ? Collections.emptyList() : List.of(down.get());
                }, (t, r) -> r.getStartDistance().plus(position(t, r, otherDistancePosition)).minus(dxDistance));
    }

    /**
     * Returns an iterator over GTUs perceived on a relative lane, ordered close to far. This can be GTUs on different roads,
     * e.g. from the main line on the right-most lane, the right-hand relative lane can give objects upstream of two on-ramps
     * that are very close by, or even the shoulder. This function differs from {@code getDownstreamGtus()} in that it will halt
     * further searching on on branch it finds a GTU on.
     * @param relativeLane lane.
     * @param egoPosition position of ego GTU relative to which objects are found.
     * @param otherPosition position of other GTU that must be upstream of egoPosition.
     * @param egoDistancePosition position of ego GTU from which the distance is determined.
     * @param otherDistancePosition position of other GTU to which the distance is determined.
     * @return iterator over GTUs.
     */
    public Iterable<Entry<LaneBasedGtu>> getFirstUpstreamGtus(final RelativeLane relativeLane,
            final RelativePosition.Type egoPosition, final RelativePosition.Type otherPosition,
            final RelativePosition.Type egoDistancePosition, final RelativePosition.Type otherDistancePosition)
    {
        update();
        Length dx = LaneStructure.this.egoGtu.getRelativePositions().get(egoPosition).dx();
        Length dxDistance = LaneStructure.this.egoGtu.getRelativePositions().get(egoDistancePosition).dx();
        return new NavigatingIterable<>(LaneBasedGtu.class, this.upstream,
                start((record) -> startUpstream(record, egoPosition), relativeLane), (record) ->
                {
                    // this navigator only returns records when there are no GTUs on the lane (it may thus ignore a GTU on a
                    // lateral lane that is closer) and combines the upstream and lateral records
                    Optional<LaneBasedGtu> gtu =
                            Try.assign(
                                    () -> record.getLane().getGtuBehind(record.getStartDistance().neg().plus(dx), otherPosition,
                                            record.getLane().getNetwork().getSimulator().getSimulatorTime()),
                                    "Problem with GTU");
                    Set<LaneRecord> set = new LinkedHashSet<>();
                    if (gtu.isEmpty())
                    {
                        set.addAll(record.getPrev());
                        set.addAll(record.lateral());
                    }
                    return set;
                }, (record) ->
                {
                    // this lister finds the first GTU and returns it as the only GTU in the list
                    Optional<LaneBasedGtu> up =
                            Try.assign(
                                    () -> record.getLane().getGtuBehind(record.getStartDistance().neg().plus(dx), otherPosition,
                                            record.getLane().getNetwork().getSimulator().getSimulatorTime()),
                                    "Problem with GTU");
                    return up.isEmpty() ? Collections.emptyList() : List.of(up.get());
                }, (t, r) -> dxDistance.minus(r.getStartDistance().plus(position(t, r, otherDistancePosition))));
    }

    /**
     * Gathers the records using a starter logic on all records in the cross section on the relative lane.
     * @param starter starter logic
     * @param relativeLane relative lane
     * @return the records using a starter logic on all records in the cross section on the relative lane
     */
    private Collection<LaneRecord> start(final Function<LaneRecord, Collection<LaneRecord>> starter,
            final RelativeLane relativeLane)
    {
        Collection<LaneRecord> collection = new LinkedHashSet<>();
        for (LaneRecord record : LaneStructure.this.crossSection.computeIfAbsent(relativeLane, (l) -> new LinkedHashSet<>()))
        {
            for (LaneRecord start : starter.apply(record))
            {
                collection.add(start);
            }
        }
        return collection;
    }

    /**
     * Recursively move to upstream records if the relative position is upstream of the record, to start a downstream search
     * from these upstream records.
     * @param record current record in search.
     * @param position RelativePosition.Type; relative position type.
     * @return records to start from.
     */
    private Collection<LaneRecord> startDownstream(final LaneRecord record, final RelativePosition.Type position)
    {
        if (position(LaneStructure.this.egoGtu, record, position).ge0())
        {
            return Set.of(record); // position is on the lane
        }
        Set<LaneRecord> set = new LinkedHashSet<>();
        for (LaneRecord up : record.getPrev())
        {
            set.addAll(startDownstream(up, position));
        }
        return set;
    }

    /**
     * Recursively move to downstream records if the relative position is downstream of the record, to start an upstream search
     * from these downstream records.
     * @param record current record in search.
     * @param position RelativePosition.Type; relative position type.
     * @return records to start from.
     */
    private Collection<LaneRecord> startUpstream(final LaneRecord record, final RelativePosition.Type position)
    {
        if (position(LaneStructure.this.egoGtu, record, position).lt(record.getLane().getLength()))
        {
            return Set.of(record); // position is on the lane
        }
        Set<LaneRecord> set = new LinkedHashSet<>();
        for (LaneRecord down : record.getNext())
        {
            set.addAll(startUpstream(down, position));
        }
        return set;
    }

    /**
     * Returns the position of the GTU on the lane of the given record.
     * @param gtu gtu.
     * @param record lane record.
     * @param positionType RelativePosition.Type; relative position type.
     * @return position of the GTU on the lane of the given record.
     */
    private Length position(final LaneBasedGtu gtu, final LaneRecordInterface<?> record,
            final RelativePosition.Type positionType)
    {
        if (gtu.equals(LaneStructure.this.egoGtu))
        {
            return record.getStartDistance().neg().plus(gtu.getRelativePositions().get(positionType).dx());
        }
        return Try.assign(() -> gtu.getPosition(record.getLane(), gtu.getRelativePositions().get(positionType)),
                "Unable to obtain position %s of GTU.", positionType);
    }

    /**
     * Updates the structure when required.
     */
    private synchronized void update()
    {
        if (this.updated != null && this.updated.equals(this.egoGtu.getSimulator().getSimulatorTime()))
        {
            return;
        }

        this.crossSection.clear();
        this.rootCrossSection.clear();
        Set<Lane> visited = new LinkedHashSet<>();
        Deque<LaneRecord> downQueue = new LinkedList<>();
        Deque<LaneRecord> upQueue = new LinkedList<>();
        Deque<LaneRecord> latDownQueue = new LinkedList<>();
        Deque<LaneRecord> latUpQueue = new LinkedList<>();
        LanePosition position = Try.assign(() -> this.egoGtu.getPosition(), "GTU does not have a reference position.");
        LaneRecord root = new LaneRecord(position.lane(), RelativeLane.CURRENT, position.position().neg(), Length.ZERO);
        visited.add(position.lane());
        addToCrossSection(root);
        downQueue.add(root);
        upQueue.add(root);
        latDownQueue.add(root); // does not matter which lat queue this is, it is the root cross section
        this.rootCrossSection.put(root.getRelativeLane(), root);
        while (!downQueue.isEmpty() || !upQueue.isEmpty() || !latDownQueue.isEmpty() || !latUpQueue.isEmpty())
        {
            if (!downQueue.isEmpty())
            {
                nextDown(visited, downQueue, latDownQueue);
            }
            else if (!upQueue.isEmpty())
            {
                nextUp(visited, upQueue, latUpQueue);
            }
            else
            {
                nextLateral(visited, downQueue, upQueue, latDownQueue, latUpQueue);
            }
        }
        this.updated = this.egoGtu.getSimulator().getSimulatorTime();
    }

    /**
     * Progress to downstream lanes of first record in the downstream queue.
     * @param visited visited records in the entire structure so far
     * @param downQueue queue of records to be processed in downstream search
     * @param latDownQueue queue of records to be processed in lateral direction, as part of a downstream search
     */
    private void nextDown(final Set<Lane> visited, final Deque<LaneRecord> downQueue, final Deque<LaneRecord> latDownQueue)
    {
        LaneRecord record = downQueue.poll();
        Set<Lane> downstreamLanes = record.getLane().nextLanes(null);
        if (!record.getLane().getType().isCompatible(this.egoGtu.getType()))
        {
            /*
             * Progress downstream from an incompatible lane only to other incompatible lanes. Compatible lanes downstream of an
             * incompatible lane will have to be found through a lateral move. Only in this way can a merge be detected.
             */
            downstreamLanes = new LinkedHashSet<>(downstreamLanes); // safe copy
            downstreamLanes.removeAll(record.getLane().nextLanes(this.egoGtu.getType()));
        }
        for (Lane lane : downstreamLanes)
        {
            LaneRecord down = new LaneRecord(lane, record.getRelativeLane(), record.getEndDistance(), Length.ZERO);
            record.addNext(down);
            down.addPrev(record);
            visited.add(lane);
            addToCrossSection(down);
            if (down.getEndDistance().lt(this.downstream))
            {
                downQueue.add(down);
            }
            latDownQueue.add(down);
        }
    }

    /**
     * Progress to upstream lanes of first record in the upstream queue.
     * @param visited visited records in the entire structure so far
     * @param upQueue queue of records to be processed in upstream search
     * @param latUpQueue queue of records to be processed in lateral direction, as part of an upstream search
     */
    private void nextUp(final Set<Lane> visited, final Deque<LaneRecord> upQueue, final Deque<LaneRecord> latUpQueue)
    {
        LaneRecord record = upQueue.poll();
        for (Lane lane : record.getLane().prevLanes(null))
        {
            /*
             * Upstream of a merge we ignore visited lanes. Upstream not of a merge, we just continue. I.e. on a roundabout one
             * lane can be both upstream and downstream.
             */
            if (!visited.contains(lane) || record.getMergeDistance().eq0())
            {
                LaneRecord up = new LaneRecord(lane, record.getRelativeLane(),
                        record.getStartDistance().minus(lane.getLength()), record.getMergeDistance());
                record.addPrev(up);
                up.addNext(record);
                visited.add(lane);
                addToCrossSection(up);
                if (up.getStartDistance().neg().plus(up.getMergeDistance()).lt(this.upstream))
                {
                    upQueue.add(up);
                }
                latUpQueue.add(up);
            }
        }
    }

    /**
     * Progress to lateral lanes for downstream search if any, or for upstream search otherwise.
     * @param visited visited records in the entire structure so far
     * @param downQueue queue of records to be processed in downstream search
     * @param upQueue queue of records to be processed in upstream search
     * @param latDownQueue queue of records to be processed in lateral direction, as part of a downstream search
     * @param latUpQueue queue of records to be processed in lateral direction, as part of an upstream search
     */
    private void nextLateral(final Set<Lane> visited, final Deque<LaneRecord> downQueue, final Deque<LaneRecord> upQueue,
            final Deque<LaneRecord> latDownQueue, final Deque<LaneRecord> latUpQueue)
    {
        boolean down;
        Deque<LaneRecord> latQueue;
        if (!latDownQueue.isEmpty())
        {
            down = true;
            latQueue = latDownQueue;
        }
        else
        {
            down = false;
            latQueue = latUpQueue;
        }
        LaneRecord record = latQueue.poll();
        for (LateralDirectionality latDirection : new LateralDirectionality[] {LateralDirectionality.LEFT,
                LateralDirectionality.RIGHT})
        {
            for (Lane lane : record.getLane().accessibleAdjacentLanesPhysical(latDirection, null))
            {
                if (!visited.contains(lane))
                {
                    /*
                     * The relative lane stays the same if we are searching upstream. This is because traffic on this adjacent
                     * lane will have to change lane to this existing relative lane, before it can be in relevant interaction
                     * with the perceiving GTU. One can think of two lanes merging in to one just before an on-ramp. Traffic on
                     * both lanes is then considered to be on the same relative lane as the acceleration lane. Otherwise the
                     * lateral lane is shifted once.
                     */
                    RelativeLane relativeLane = !down ? record.getRelativeLane() : (latDirection.isLeft()
                            ? record.getRelativeLane().getLeft() : record.getRelativeLane().getRight());

                    /*
                     * If the zero-position is on the record, the fractional position is used. Otherwise the start distance is
                     * such that the start is equal in a downstream search, and the end is equal in an upstream search.
                     */
                    Length startDistance;
                    if (record.getStartDistance().lt0() && record.getEndDistance().gt0())
                    {
                        startDistance = lane.getLength()
                                .times(record.getStartDistance().neg().si / record.getLane().getLength().si).neg();
                    }
                    else if (down)
                    {
                        startDistance = record.getStartDistance();
                    }
                    else
                    {
                        startDistance = record.getEndDistance().minus(lane.getLength());
                    }

                    /*
                     * If the adjacent lane is found in a downstream search and its upstream links are different, we are dealing
                     * with a merge at a distance of the start of these two lanes.
                     */
                    Length mergeDistance;
                    if (down && record.getStartDistance().gt0())
                    {
                        if (!getUpstreamLinks(lane).equals(getUpstreamLinks(record.getLane())))
                        {
                            mergeDistance = record.getStartDistance();
                        }
                        else
                        {
                            mergeDistance = record.getMergeDistance(); // zero, or continue same value in downstream branch
                        }
                    }
                    else
                    {
                        mergeDistance = record.getMergeDistance(); // zero, or continue same value in upstream branch
                    }
                    LaneRecord lat = new LaneRecord(lane, relativeLane, startDistance, mergeDistance);
                    if (!down)
                    {
                        record.addLateral(lat);
                    }
                    visited.add(lane);
                    addToCrossSection(lat);
                    // from the cross-section directly from the root, we initiate both an upstream and downstream search
                    if (this.rootCrossSection.containsValue(record))
                    {
                        this.rootCrossSection.put(lat.getRelativeLane(), lat);
                        latDownQueue.add(lat); // does not matter which lat queue this is, it is the root cross section
                        if (lat.getEndDistance().lt(this.downstream))
                        {
                            downQueue.add(lat);
                        }
                        if (lat.getStartDistance().neg().lt(this.upstream))
                        {
                            upQueue.add(lat);
                        }
                    }
                    else if (down)
                    {
                        latDownQueue.add(lat);
                        if (lat.getEndDistance().lt(this.downstream))
                        {
                            downQueue.add(lat);
                            if (mergeDistance.gt0())
                            {
                                upQueue.add(lat);
                            }
                        }
                    }
                    else
                    {
                        latUpQueue.add(lat);
                        if (lat.getStartDistance().neg().plus(lat.getMergeDistance()).lt(this.upstream))
                        {
                            upQueue.add(lat);
                        }
                    }
                }
            }
        }
    }

    /**
     * Adds the lane to the cross-section, if the zero position is somewhere on the lane (negative start distance, positive end
     * distance).
     * @param record record.
     */
    private void addToCrossSection(final LaneRecord record)
    {
        if (record.getStartDistance().le0() && record.getEndDistance().gt0())
        {
            this.crossSection.computeIfAbsent(record.getRelativeLane(), (r) -> new LinkedHashSet<>()).add(record);
        }
    }

    /**
     * Returns the links upstream of the lane.
     * @param lane lane.
     * @return upstream lanes.
     */
    private Set<Link> getUpstreamLinks(final Lane lane)
    {
        Set<Link> set = new LinkedHashSet<>();
        for (Lane prev : lane.prevLanes(null))
        {
            set.add(prev.getLink());
        }
        return set;
    }

    /**
     * Returns all the lanes that are in the root cross-section, i.e. to our direct left and right.
     * @return set of lanes in the root cross-section.
     */
    public SortedSet<RelativeLane> getRootCrossSection()
    {
        update();
        return new TreeSet<>(this.rootCrossSection.keySet());
    }

    /**
     * Returns whether the lane exists within the structure.
     * @param lane lane.
     * @return whether the lane exists within the structure.
     */
    public boolean exists(final RelativeLane lane)
    {
        update();
        return this.crossSection.containsKey(lane);
    }

    /**
     * Returns the root record on the given lane.
     * @param lane lane.
     * @return root record on the lane.
     */
    public LaneRecord getRootRecord(final RelativeLane lane)
    {
        update();
        return this.rootCrossSection.get(lane);
    }

    /**
     * Returns the set of records in the cross-section on the given lane.
     * @param lane lane.
     * @return set of records in the cross-section on the given lane.
     */
    public Set<LaneRecord> getCrossSectionRecords(final RelativeLane lane)
    {
        return new LinkedHashSet<>(this.crossSection.get(lane));
    }

}
