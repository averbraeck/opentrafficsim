package org.opentrafficsim.road.gtu.perception.structure;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.function.Function;
import java.util.function.IntToDoubleFunction;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.point.Point2d;
import org.djutils.immutablecollections.ImmutableArrayList;
import org.djutils.immutablecollections.ImmutableList;
import org.opentrafficsim.base.collections.IndexedMap;
import org.opentrafficsim.base.collections.SmallArraySet;
import org.opentrafficsim.base.collections.SortedKeySetMap;
import org.opentrafficsim.base.geometry.FractionalProjectionHelper.FractionalFallback;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.LaneBasedGtu;
import org.opentrafficsim.road.gtu.perception.RelativeLane;
import org.opentrafficsim.road.gtu.perception.structure.NavigatingIterable.Entry;
import org.opentrafficsim.road.network.Lane;
import org.opentrafficsim.road.network.LanePosition;
import org.opentrafficsim.road.network.object.LaneBasedObject;

/**
 * The lane structure provides a way to see the world for a lane based model.
 * <p>
 * Copyright (c) 2024-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 */
public class LaneStructure
{

    /** GTU. */
    private final LaneBasedGtu egoGtu;

    /** Length to build lane structure upstream of GTU, or upstream relative to a downstream merge. */
    private Length upstream;

    /** Length to build lane structure downstream of GTU. */
    private Length downstream;

    /** Effective length to build lane structure downstream of GTU anticipating in-lane movement before next update. */
    private double effectiveDownstream;

    /** Time at which the structure was updated. */
    private Duration updateTime = null;

    /** Route in last update. */
    private Route route;

    /** Cross section of lane records at different relative lanes. */
    private final Map<RelativeLane, Set<LaneRecord>> crossSection = new IndexedMap<>();

    /** Cross section of lane records directly found laterally from the root. */
    private final SortedKeySetMap<RelativeLane, LaneRecord> rootCrossSection = new IndexedMap<>();

    /** This set allows O(1) lookup rather than O(n) in rootCrossSection.values().contains(). */
    private final Set<LaneRecord> rootCrossSectionRecords = new HashSet<>();

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
     * <p>
     * This method does not support finding objects that start at upstream lanes, but who's length reach the GTU.
     * @param <T> type of {@code LaneBasedObject}.
     * @param relativeLane lane.
     * @param clazz class of lane-based object type.
     * @param position relative position relative to which objects are found and distances are given.
     * @param onRoute whether the objects have to be on-route.
     * @return iterator over objects.
     */
    public <T extends LaneBasedObject> Iterable<Entry<T>> getDownstreamObjects(final RelativeLane relativeLane,
            final Class<T> clazz, final RelativePosition.Type position, final boolean onRoute)
    {
        update();
        Length dx = LaneStructure.this.egoGtu.getRelativePosition(position).dx();
        return new NavigatingIterable<>(clazz, this.downstream, true,
                start((record) -> startDownstream(record, position), relativeLane), (record) ->
                {
                    // this navigator only includes records of lanes on the route
                    return onRoute ? onRoute(record.getNext()) : record.getNext();
                }, (record) ->
                {
                    // this navigator selects all objects fully or partially downstream
                    List<LaneBasedObject> list = record.getLane().getLaneBasedObjects();
                    if (list.isEmpty())
                    {
                        return list;
                    }
                    double pos = -record.getStartDistance() + dx.si;
                    int from = findFirstAfterOrAt((i) -> list.get(i).getLongitudinalPosition().si + list.get(i).getLength().si,
                            list.size(), pos);
                    if (from > list.size() - 1)
                    {
                        return new ImmutableArrayList<>(Collections.emptyList());
                    }
                    return list.subList(from, list.size());
                }, (t, r) -> r.getStartDistance() + t.getLongitudinalPosition().si - dx.si);
    }

    /**
     * Returns a set that only contains the element in the input set that are on the route.
     * @param set input set
     * @return a set that only contains the element in the input set that are on the route
     */
    private Set<LaneRecord> onRoute(final Set<LaneRecord> set)
    {
        Set<LaneRecord> filtered = null;
        for (LaneRecord r : set)
        {
            if (!r.isOnRoute(this.route))
            {
                if (filtered == null)
                {
                    filtered = new SmallArraySet<>();
                    filtered.addAll(set); // copy only when needed
                }
                filtered.remove(r);
            }
        }
        return filtered == null ? set : filtered;
    }

    /**
     * Returns an iterator over objects perceived on a relative lane, ordered close to far. This can be objects on different
     * roads, e.g. from the main line on the right-most lane, the right-hand relative lane can give objects upstream of two
     * on-ramps that are very close by, or even the shoulder. Objects that are partially upstream are also included.
     * @param <T> type of {@code LaneBasedObject}.
     * @param relativeLane lane.
     * @param clazz class of lane-based object type.
     * @param position relative position relative to which objects are found and distances are given.
     * @return iterator over objects.
     */
    public <T extends LaneBasedObject> Iterable<Entry<T>> getUpstreamObjects(final RelativeLane relativeLane,
            final Class<T> clazz, final RelativePosition.Type position)
    {
        update();
        Length dx = LaneStructure.this.egoGtu.getRelativePosition(position).dx();
        return new NavigatingIterable<>(clazz, this.upstream, false,
                start((record) -> startUpstream(record, position), relativeLane), (record) ->
                {
                    // this navigator combines the upstream and lateral records
                    Set<LaneRecord> set = new SmallArraySet<>();
                    set.addAll(record.getPrev());
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
                    double pos = -record.getStartDistance() + dx.si;
                    int to = findLastBeforeOrAt((i) -> list.get(i).getLongitudinalPosition().si, list.size(), pos);
                    if (to < 0)
                    {
                        return new ImmutableArrayList<>(Collections.emptyList());
                    }
                    List<LaneBasedObject> subList = list.subList(0, to + 1);
                    return reversed((i) -> subList.get(i), subList.size());
                }, (t, r) -> -r.getStartDistance() - t.getLongitudinalPosition().si - dx.si);
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
        Length dx = LaneStructure.this.egoGtu.getRelativePosition(egoPosition).dx();
        Length dxDistance = LaneStructure.this.egoGtu.getRelativePosition(egoDistancePosition).dx();
        return new NavigatingIterable<>(LaneBasedGtu.class, this.downstream, true,
                start((record) -> startDownstream(record, egoPosition), relativeLane), (record) ->
                {
                    // this navigator ignores downstream lanes that are not on the route, if the current record is on the route
                    Set<LaneRecord> next = new SmallArraySet<>();
                    boolean ignoreNotOnRoute =
                            this.route != null && record.getLane().getLink().getEndNode().getLinks().size() > 2
                                    && this.route.containsLink(record.getLane().getLink());
                    for (LaneRecord nextLane : record.getNext())
                    {
                        if (!ignoreNotOnRoute || this.route.containsLink(nextLane.getLane().getLink()))
                        {
                            next.add(nextLane);
                        }
                    }
                    return next;
                }, (record) ->
                {
                    // this lister finds the relevant sublist of GTUs
                    ImmutableList<LaneBasedGtu> gtus = record.getLane().getGtuList();
                    if (gtus.isEmpty())
                    {
                        return gtus;
                    }
                    double pos = Math.max(-record.getStartDistance() + dx.si, 0.0);
                    int from = findFirstAfterOrAt((i) -> position(gtus.get(i), record, otherPosition), gtus.size(), pos);
                    while (from < gtus.size() && gtus.get(from).getId().equals(this.egoGtu.getId()))
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
                        return gtus.subList(from, to + 1);
                    }
                    return gtus;
                }, (t, r) -> r.getStartDistance() + position(t, r, otherDistancePosition) - dxDistance.si);
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
        Length dx = LaneStructure.this.egoGtu.getRelativePosition(egoPosition).dx();
        Length dxDistance = LaneStructure.this.egoGtu.getRelativePosition(egoDistancePosition).dx();
        return new NavigatingIterable<>(LaneBasedGtu.class, this.upstream, false,
                start((record) -> startUpstream(record, egoPosition), relativeLane), (record) ->
                {
                    // this navigator combines the upstream and lateral records
                    Set<LaneRecord> set = new SmallArraySet<>();
                    set.addAll(record.getPrev());
                    set.addAll(record.lateral());
                    return set;
                }, (record) ->
                {
                    // this lister finds the relevant sublist of GTUs and reverses it
                    ImmutableList<LaneBasedGtu> gtus = record.getLane().getGtuList();
                    if (gtus.isEmpty())
                    {
                        return gtus;
                    }
                    int from = 0;
                    while (from < gtus.size() && gtus.get(from).getId().equals(this.egoGtu.getId()))
                    {
                        from++;
                    }
                    double pos = Math.min(-record.getStartDistance() + dx.si, record.getLength().si);
                    int to = findLastBeforeOrAt((i) -> position(gtus.get(i), record, otherPosition), gtus.size(), pos);
                    while (to >= 0 && gtus.get(to).getId().equals(this.egoGtu.getId()))
                    {
                        to--;
                    }
                    if (from > to)
                    {
                        return Collections.emptyList();
                    }
                    ImmutableList<LaneBasedGtu> subList = from > 0 || to < gtus.size() - 1 ? gtus.subList(from, to + 1) : gtus;
                    return reversed((i) -> subList.get(i), subList.size());
                }, (t, r) -> dxDistance.si - r.getStartDistance() - position(t, r, otherDistancePosition));
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
        Length dx = LaneStructure.this.egoGtu.getRelativePosition(egoPosition).dx();
        Length dxDistance = LaneStructure.this.egoGtu.getRelativePosition(egoDistancePosition).dx();
        return new NavigatingIterable<>(LaneBasedGtu.class, this.downstream, true,
                start((record) -> startDownstream(record, egoPosition), relativeLane), (record) ->
                {
                    // this navigator only returns records when there are no GTUs on the lane
                    return record.getLane()
                            .getGtuAhead(Length.ofSI(-record.getStartDistance() + dx.si), otherPosition, this.updateTime)
                            .isEmpty() ? record.getNext() : Collections.emptySet();
                }, (record) ->
                {
                    // this lister finds the first GTU and returns it as the only GTU in the list
                    Optional<LaneBasedGtu> down = record.getLane().getGtuAhead(Length.ofSI(-record.getStartDistance() + dx.si),
                            otherPosition, this.updateTime);
                    return down.isEmpty() ? Collections.emptyList() : List.of(down.get());
                }, (t, r) -> r.getStartDistance() + position(t, r, otherDistancePosition) - dxDistance.si);
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
        Length dx = LaneStructure.this.egoGtu.getRelativePosition(egoPosition).dx();
        Length dxDistance = LaneStructure.this.egoGtu.getRelativePosition(egoDistancePosition).dx();
        return new NavigatingIterable<>(LaneBasedGtu.class, this.upstream, false,
                start((record) -> startUpstream(record, egoPosition), relativeLane), (record) ->
                {
                    // this navigator only returns records when there are no GTUs on the lane (it may thus ignore a GTU on a
                    // lateral lane that is closer) and combines the upstream and lateral records
                    Optional<LaneBasedGtu> gtu = record.getLane().getGtuBehind(Length.ofSI(-record.getStartDistance() + dx.si),
                            otherPosition, this.updateTime);
                    if (!gtu.isEmpty())
                    {
                        return Collections.emptySet();
                    }
                    Set<LaneRecord> set = new SmallArraySet<>();
                    set.addAll(record.getPrev());
                    set.addAll(record.lateral());
                    return set;
                }, (record) ->
                {
                    // this lister finds the first GTU and returns it as the only GTU in the list
                    Optional<LaneBasedGtu> up = record.getLane().getGtuBehind(Length.ofSI(-record.getStartDistance() + dx.si),
                            otherPosition, this.updateTime);
                    return up.isEmpty() ? Collections.emptyList() : List.of(up.get());
                }, (t, r) -> dxDistance.si - r.getStartDistance() - position(t, r, otherDistancePosition));
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
        Collection<LaneRecord> collection = new SmallArraySet<>();
        if (this.crossSection.containsKey(relativeLane))
        {
            for (LaneRecord record : this.crossSection.get(relativeLane))
            {
                collection.addAll(starter.apply(record));
            }
        }
        return collection;
    }

    /**
     * Recursively move to upstream records if the relative position is upstream of the record, to start a downstream search
     * from these upstream records.
     * @param record current record in search.
     * @param position relative position type.
     * @return records to start from.
     */
    private Collection<LaneRecord> startDownstream(final LaneRecord record, final RelativePosition.Type position)
    {
        Collection<LaneRecord> result = new SmallArraySet<>();
        startDownstream(record, position, result);
        return result;
    }

    /**
     * Recursively move to upstream records if the relative position is upstream of the record, to start a downstream search
     * from these upstream records.
     * @param record current record in search.
     * @param position relative position type.
     * @param result result to store found lane records in
     */
    private void startDownstream(final LaneRecord record, final RelativePosition.Type position,
            final Collection<LaneRecord> result)
    {
        if (egoPosition(record, position) >= 0.0)
        {
            result.add(record);
            return; // position is on the lane
        }
        for (LaneRecord up : record.getPrev())
        {
            startDownstream(up, position, result);
        }
    }

    /**
     * Recursively move to downstream records if the relative position is downstream of the record, to start an upstream search
     * from these downstream records.
     * @param record current record in search.
     * @param position relative position type.
     * @return records to start from.
     */
    private Collection<LaneRecord> startUpstream(final LaneRecord record, final RelativePosition.Type position)
    {
        Collection<LaneRecord> result = new SmallArraySet<>();
        startUpstream(record, position, result);
        return result;
    }

    /**
     * Recursively move to downstream records if the relative position is downstream of the record, to start an upstream search
     * from these downstream records.
     * @param record current record in search.
     * @param position relative position type.
     * @param result result to store found lane records in
     */
    private void startUpstream(final LaneRecord record, final RelativePosition.Type position,
            final Collection<LaneRecord> result)
    {
        if (egoPosition(record, position) < record.getLane().getLength().si)
        {
            result.add(record);
            return; // position is on the lane
        }
        for (LaneRecord down : record.getNext())
        {
            startUpstream(down, position, result);
        }
    }

    /**
     * Returns the position of the GTU on the lane of the given record.
     * @param record lane record.
     * @param positionType relative position type.
     * @return position of the GTU on the lane of the given record.
     */
    private double egoPosition(final LaneRecordInterface<?> record, final RelativePosition.Type positionType)
    {
        return -record.getStartDistance() + LaneStructure.this.egoGtu.getRelativePosition(positionType).dx().si;
    }

    /**
     * Returns the position of the GTU on the lane of the given record.
     * @param gtu gtu.
     * @param record lane record.
     * @param positionType relative position type.
     * @return position of the GTU on the lane of the given record.
     */
    private double position(final LaneBasedGtu gtu, final LaneRecordInterface<?> record,
            final RelativePosition.Type positionType)
    {
        return gtu.getPosition(record.getLane(), gtu.getRelativePosition(positionType)).si;
    }

    /**
     * Updates the structure when required.
     */
    private synchronized void update()
    {
        if (this.updateTime != null && this.updateTime.si == this.egoGtu.getSimulator().getSimulatorTime().si)
        {
            return;
        }
        this.updateTime = this.egoGtu.getSimulator().getSimulatorTime();

        LanePosition position = this.egoGtu.getPosition();
        Lane lane = position.lane();
        double positionSI = position.position().si;

        // Check if only the start distance needs to be updated as the GTU is still on the same lane with the same route
        LaneRecord prevRootRecord = this.rootCrossSection.get(RelativeLane.CURRENT);
        Route prevRoute = this.route;
        this.route = this.egoGtu.getStrategicalPlanner().getRoute().orElse(null);
        if (prevRootRecord != null && prevRootRecord.getLane().equals(lane) && Objects.equals(this.route, prevRoute))
        {
            // Skip entirely on same position
            if (prevRootRecord.getStartDistance() != -positionSI)
            {
                prevRootRecord.setStartDistance(-positionSI, this);
            }
            return;
        }

        // Rebuild the structure
        this.effectiveDownstream = lane.getLength().si - positionSI + this.downstream.si;
        this.crossSection.clear();
        this.rootCrossSection.clear();
        this.rootCrossSectionRecords.clear();
        Set<Lane> visited = new HashSet<>();
        Deque<LaneRecord> downQueue = new ArrayDeque<>();
        Deque<LaneRecord> upQueue = new ArrayDeque<>();
        Deque<LaneRecord> latDownQueue = new ArrayDeque<>();
        Deque<LaneRecord> latUpQueue = new ArrayDeque<>();
        LaneRecord root = new LaneRecord(lane, RelativeLane.CURRENT, -positionSI, Length.ZERO);
        visited.add(lane);
        checkCrossSection(root);
        downQueue.add(root); // given effectiveDownstream there is an inherent downstream move, no need to check distance
        if (-root.getStartDistance() < this.upstream.si)
        {
            upQueue.add(root);
        }
        latDownQueue.add(root); // does not matter which lat queue this is, it is the root cross section
        this.rootCrossSection.put(root.getRelativeLane(), root);
        this.rootCrossSectionRecords.add(root);
        for (;;)
        {
            if (!downQueue.isEmpty())
            {
                nextDown(visited, downQueue, latDownQueue);
            }
            else if (!upQueue.isEmpty())
            {
                nextUp(visited, upQueue, latUpQueue);
            }
            else if (!latDownQueue.isEmpty())
            {
                nextLateral(visited, downQueue, upQueue, latDownQueue, latUpQueue, true);
            }
            else if (!latUpQueue.isEmpty())
            {
                nextLateral(visited, downQueue, upQueue, latDownQueue, latUpQueue, false);
            }
            else
            {
                break;
            }
        }
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
        /*
         * Progress downstream from an incompatible lane only to other incompatible lanes. Compatible lanes downstream of an
         * incompatible lane will have to be found through a lateral move. Only in this way can a merge be detected.
         */
        Set<Lane> nextToSkip = !record.getLane().getType().isCompatible(this.egoGtu.getType())
                ? record.getLane().nextLanes(this.egoGtu.getType()) : null;
        for (Lane lane : downstreamLanes)
        {
            if (nextToSkip != null && nextToSkip.contains(lane))
            {
                continue;
            }
            LaneRecord down = new LaneRecord(lane, record.getRelativeLane(), record.getEndDistance(), Length.ZERO);
            record.addNext(down, true);
            down.addPrev(record, false);
            visited.add(lane);
            checkCrossSection(down);
            if (down.getEndDistance() < this.effectiveDownstream)
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
            if (!visited.contains(lane) || record.getMergeDistance().si == 0.0)
            {
                LaneRecord up = new LaneRecord(lane, record.getRelativeLane(), record.getStartDistance() - lane.getLength().si,
                        record.getMergeDistance());
                record.addPrev(up, true);
                up.addNext(record, false);
                visited.add(lane);
                checkCrossSection(up);
                if (-up.getStartDistance() + up.getMergeDistance().si < this.upstream.si)
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
     * @param down whether this step is a part of a downstream or upstream search
     */
    private void nextLateral(final Set<Lane> visited, final Deque<LaneRecord> downQueue, final Deque<LaneRecord> upQueue,
            final Deque<LaneRecord> latDownQueue, final Deque<LaneRecord> latUpQueue, final boolean down)
    {
        LaneRecord record = down ? latDownQueue.poll() : latUpQueue.poll();
        for (LateralDirectionality latDirection : LateralDirectionality.LEFT_AND_RIGHT)
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
                    double startDistance;
                    if (record.getStartDistance() < 0.0 && record.getEndDistance() > 0.0)
                    {
                        Point2d p = record.getLane().getCenterLine().getLocation(-record.getStartDistance());
                        double f = lane.projectFractionalAt(p.x, p.y, FractionalFallback.ENDPOINT);
                        startDistance = -f * lane.getLength().si;
                    }
                    else if (down)
                    {
                        startDistance = record.getStartDistance();
                    }
                    else
                    {
                        startDistance = record.getEndDistance() - lane.getLength().si;
                    }

                    /*
                     * If the adjacent lane is found in a downstream search and its upstream links are different, we are dealing
                     * with a merge at a distance of the start of these two lanes.
                     */
                    Length mergeDistance;
                    if (down && record.getStartDistance() > 0.0)
                    {
                        if (!lane.prevLinks(null).equals(record.getLane().prevLinks(null)))
                        {
                            mergeDistance = Length.ofSI(record.getStartDistance());
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
                    record.addLateral(lat, down);
                    visited.add(lane);
                    checkCrossSection(lat);
                    // from the cross-section directly from the root, we initiate both an upstream and downstream search
                    if (this.rootCrossSectionRecords.contains(record))
                    {
                        this.rootCrossSection.put(lat.getRelativeLane(), lat);
                        this.rootCrossSectionRecords.add(lat);
                        latDownQueue.add(lat); // does not matter which lat queue this is, it is the root cross section
                        if (lat.getEndDistance() < this.effectiveDownstream)
                        {
                            downQueue.add(lat);
                        }
                        if (-lat.getStartDistance() < this.upstream.si)
                        {
                            upQueue.add(lat);
                        }
                    }
                    else if (down)
                    {
                        latDownQueue.add(lat);
                        if (lat.getEndDistance() < this.effectiveDownstream)
                        {
                            downQueue.add(lat);
                        }
                        if (mergeDistance.si > 0.0)
                        {
                            upQueue.add(lat);
                        }
                    }
                    else
                    {
                        latUpQueue.add(lat);
                        if (-lat.getStartDistance() + lat.getMergeDistance().si < this.upstream.si)
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
    private void checkCrossSection(final LaneRecord record)
    {
        if (record.getStartDistance() <= 0.0 && record.getEndDistance() > 0.0)
        {
            addToCrossSection(record);
        }
    }

    /**
     * Removes the record from the cross-section. This is used when the start distance of the record is changed and the record
     * is no longer in the cross-section.
     * @param record record to remove from the cross-section
     */
    void removeFromCrossSection(final LaneRecord record)
    {
        Set<LaneRecord> set = this.crossSection.get(record.getRelativeLane());
        if (set != null)
        {
            set.remove(record);
        }
    }

    /**
     * Adds the record to the cross-section. This is used when the start distance of the record is changed and the record
     * becomes part of the cross-section.
     * @param record record to remove from the cross-section
     */
    void addToCrossSection(final LaneRecord record)
    {
        this.crossSection.computeIfAbsent(record.getRelativeLane(), (r) -> new SmallArraySet<>()).add(record);
    }

    /**
     * Returns all the lanes that are in the root cross-section, i.e. to our direct left and right.
     * @return set of lanes in the root cross-section.
     */
    public SortedSet<RelativeLane> getRootCrossSection()
    {
        update();
        return Collections.unmodifiableSortedSet(this.rootCrossSection.keySet());
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
     * Returns an {@link Iterable} that returns iterators that iterate in the reversed order over the random access.
     * @param <T> element type
     * @param randomAccess random access
     * @param size size of the random access
     * @return an {@link Iterable} that returns iterators that iterate in the reversed order over the random access
     */
    public static <T> Iterable<T> reversed(final Function<Integer, T> randomAccess, final int size)
    {
        return () -> new Iterator<>()
        {
            private int index = size;

            @Override
            public boolean hasNext()
            {
                return this.index > 0;
            }

            @Override
            public T next()
            {
                if (!hasNext())
                {
                    throw new NoSuchElementException();
                }
                return randomAccess.apply(--this.index);
            }
        };
    }

    /**
     * Binary search to find the first object after (or at) the given bound.
     * @param location position function for i'th object
     * @param size size of list of objects being searched
     * @param bound bound
     * @return index of first object after (or at) the given bound, or {@code size} if no such object
     */
    public static int findFirstAfterOrAt(final IntToDoubleFunction location, final int size, final double bound)
    {
        // return fast for intermediate lane boundaries between GTU and the range
        if (size == 0 || location.applyAsDouble(0) >= bound)
        {
            return 0; // all elements are after
        }
        if (location.applyAsDouble(size - 1) < bound)
        {
            return size; // no element is after
        }
        int low = 0;
        int high = size;
        while (low < high)
        {
            int mid = (low + high) >>> 1;
            if (location.applyAsDouble(mid) < bound)
            {
                low = mid + 1;
            }
            else
            {
                high = mid;
            }
        }
        return low;
    }

    /**
     * Binary search to find the first object before (or at) the given bound.
     * @param location position function for i'th object
     * @param size size of list of objects being searched
     * @param bound bound
     * @return index of first object before (or at) the given bound, or {@code -1} if no such object
     */
    public static int findLastBeforeOrAt(final IntToDoubleFunction location, final int size, final double bound)
    {
        // return fast for intermediate lane boundaries between GTU and the range
        if (size == 0 || location.applyAsDouble(0) > bound)
        {
            return -1; // no element is before
        }
        if (location.applyAsDouble(size - 1) <= bound)
        {
            return size - 1; // all elements are before
        }
        int low = -1;
        int high = size - 1;
        while (low < high)
        {
            int mid = (low + high + 1) >>> 1;
            if (location.applyAsDouble(mid) > bound)
            {
                high = mid - 1;
            }
            else
            {
                low = mid;
            }
        }
        return low;
    }

}
