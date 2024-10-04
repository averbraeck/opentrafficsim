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
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
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
    private final LaneBasedGtu gtu;

    /** Length to build lane structure upstream of GTU, or upstream relative to a downstream merge. */
    private Length upstream;

    /** Length to build lane structure downstream of GTU. */
    private Length downstream;

    /** Time at which the structure was updated. */
    private Time updated = null;

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
        this.gtu = gtu;
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
        Length dx = LaneStructure.this.gtu.getRelativePositions().get(position).dx();
        return new NavigatingIterable<>(relativeLane, clazz, this.downstream, (record) -> startDownstream(record, position),
                (record) ->
                {
                    // this navigator only includes records of lanes on the route
                    Set<LaneRecord> set = record.getNext();
                    set.removeIf((r) -> onRoute && !r.isOnRoute(LaneStructure.this.gtu.getStrategicalPlanner().getRoute()));
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
                        return Collections.EMPTY_LIST;
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
        Length dx = LaneStructure.this.gtu.getRelativePositions().get(position).dx();
        return new NavigatingIterable<T>(relativeLane, clazz, this.upstream, (record) -> startUpstream(record, position),
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
                        return Collections.EMPTY_LIST;
                    }
                    list = list.subList(0, to);
                    Collections.reverse(list);
                    return list;
                }, (t, r) -> r.getStartDistance().plus(t.getLongitudinalPosition()).plus(dx).neg());
    }

    /**
     * Returns an iterator over GTUs perceived on a relative lane, ordered close to far. This can be GTUs on different roads,
     * e.g. from the main line on the right-most lane, the right-hand relative lane can give objects upstream of two on-ramps
     * that are very close by, or even the shoulder.
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
        Length dx = LaneStructure.this.gtu.getRelativePositions().get(egoPosition).dx();
        Length dxDistance = LaneStructure.this.gtu.getRelativePositions().get(egoDistancePosition).dx();
        return new NavigatingIterable<>(relativeLane, LaneBasedGtu.class, this.downstream,
                (record) -> startDownstream(record, egoPosition), (record) -> record.getNext(), (record) ->
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
                            || gtus.get(from).getId().equals(this.gtu.getId())))
                    {
                        from++;
                    }
                    int to = gtus.size() - 1;
                    while (to >= 0 && (position(gtus.get(to), record, otherPosition).gt(record.getLane().getLength())
                            || gtus.get(to).getId().equals(this.gtu.getId())))
                    {
                        to--;
                    }
                    if (from > to)
                    {
                        return Collections.EMPTY_LIST;
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
        Length dx = LaneStructure.this.gtu.getRelativePositions().get(egoPosition).dx();
        Length dxDistance = LaneStructure.this.gtu.getRelativePositions().get(egoDistancePosition).dx();
        return new NavigatingIterable<>(relativeLane, LaneBasedGtu.class, this.upstream,
                (record) -> startUpstream(record, egoPosition), (record) ->
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
                    while (from < gtus.size() && (position(gtus.get(from), record, otherPosition).lt0()
                            || gtus.get(from).getId().equals(this.gtu.getId())))
                    {
                        from++;
                    }
                    int to = gtus.size() - 1;
                    Length pos = Length.min(record.getStartDistance().neg().plus(dx), record.getLane().getLength());
                    while (to >= 0 && (position(gtus.get(to), record, otherPosition).gt(pos)
                            || gtus.get(to).getId().equals(this.gtu.getId())))
                    {
                        to--;
                    }
                    if (from > to)
                    {
                        return Collections.EMPTY_LIST;
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
        Length dx = LaneStructure.this.gtu.getRelativePositions().get(egoPosition).dx();
        Length dxDistance = LaneStructure.this.gtu.getRelativePositions().get(egoDistancePosition).dx();
        return new NavigatingIterable<>(relativeLane, LaneBasedGtu.class, this.downstream,
                (record) -> startDownstream(record, egoPosition), (record) ->
                {
                    // this navigator only returns records when there are no GTUs on the lane
                    return Try.assign(
                            () -> record.getLane().getGtuAhead(record.getStartDistance().neg().plus(dx), otherPosition,
                                    record.getLane().getNetwork().getSimulator().getSimulatorAbsTime()),
                            "Problem with GTU") == null ? record.getNext() : new LinkedHashSet<>();
                }, (record) ->
                {
                    // this lister finds the first GTU and returns it as the only GTU in the list
                    LaneBasedGtu down =
                            Try.assign(
                                    () -> record.getLane().getGtuAhead(record.getStartDistance().neg().plus(dx), otherPosition,
                                            record.getLane().getNetwork().getSimulator().getSimulatorAbsTime()),
                                    "Problem with GTU");
                    return down == null ? Collections.EMPTY_LIST : List.of(down);
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
        Length dx = LaneStructure.this.gtu.getRelativePositions().get(egoPosition).dx();
        Length dxDistance = LaneStructure.this.gtu.getRelativePositions().get(egoDistancePosition).dx();
        return new NavigatingIterable<>(relativeLane, LaneBasedGtu.class, this.upstream,
                (record) -> startUpstream(record, egoPosition), (record) ->
                {
                    // this navigator only returns records when there are no GTUs on the lane (it may thus ignore a GTU on a
                    // lateral
                    // lane that is closer) and combines the upstream and lateral records
                    LaneBasedGtu gtu =
                            Try.assign(
                                    () -> record.getLane().getGtuBehind(record.getStartDistance().neg().plus(dx), otherPosition,
                                            record.getLane().getNetwork().getSimulator().getSimulatorAbsTime()),
                                    "Problem with GTU");
                    Set<LaneRecord> set = new LinkedHashSet<>();
                    if (gtu == null)
                    {
                        set.addAll(record.getPrev());
                        set.addAll(record.lateral());
                    }
                    return set;
                }, (record) ->
                {
                    // this lister finds the first GTU and returns it as the only GTU in the list
                    LaneBasedGtu up =
                            Try.assign(
                                    () -> record.getLane().getGtuBehind(record.getStartDistance().neg().plus(dx), otherPosition,
                                            record.getLane().getNetwork().getSimulator().getSimulatorAbsTime()),
                                    "Problem with GTU");
                    return up == null ? Collections.EMPTY_LIST : List.of(up);
                }, (t, r) -> dxDistance.minus(r.getStartDistance().plus(position(t, r, otherDistancePosition))));
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
        if (position(LaneStructure.this.gtu, record, position).ge0())
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
        if (position(LaneStructure.this.gtu, record, position).lt(record.getLane().getLength()))
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
    private final Length position(final LaneBasedGtu gtu, final LaneRecord record, final RelativePosition.Type positionType)
    {
        if (gtu.equals(LaneStructure.this.gtu))
        {
            return record.getStartDistance().neg().plus(gtu.getRelativePositions().get(positionType).dx());
        }
        return Try.assign(() -> gtu.position(record.getLane(), gtu.getRelativePositions().get(positionType)),
                "Unable to obtain position %s of GTU.", positionType);
    }

    /**
     * Updates the structure when required.
     */
    private synchronized void update()
    {
        if (this.updated != null && this.updated.equals(this.gtu.getSimulator().getSimulatorAbsTime()))
        {
            return;
        }

        this.crossSection.clear();
        Set<Lane> visited = new LinkedHashSet<>();
        Deque<LaneRecord> downQueue = new LinkedList<>();
        Deque<LaneRecord> upQueue = new LinkedList<>();
        Deque<LaneRecord> latDownQueue = new LinkedList<>();
        Deque<LaneRecord> latUpQueue = new LinkedList<>();
        LanePosition position = Try.assign(() -> this.gtu.getReferencePosition(), "GTU does not have a reference position.");
        LaneRecord root = new LaneRecord(position.lane(), RelativeLane.CURRENT, position.position().neg(), Length.ZERO);
        visited.add(position.lane());
        addToCrossSection(root);
        downQueue.add(root);
        upQueue.add(root);
        latDownQueue.add(root);
        this.rootCrossSection.put(root.getRelativeLane(), root);
        while (!downQueue.isEmpty() || !upQueue.isEmpty() || !latDownQueue.isEmpty() || !latUpQueue.isEmpty())
        {
            if (!downQueue.isEmpty())
            {
                LaneRecord record = downQueue.poll();
                Set<Lane> downstreamLanes = record.getLane().nextLanes(null);
                if (!record.getLane().getType().isCompatible(this.gtu.getType()))
                {
                    /*
                     * Progress downstream from an incompatible lane only to other incompatible lanes. Compatible lanes
                     * downstream of an incompatible lane will have to be found through a lateral move. Only in this way can a
                     * merge be detected.
                     */
                    downstreamLanes.removeAll(record.getLane().nextLanes(this.gtu.getType()));
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
            else if (!upQueue.isEmpty())
            {
                LaneRecord record = upQueue.poll();
                for (Lane lane : record.getLane().prevLanes(null))
                {
                    /*
                     * Upstream of a merge we ignore visited lanes. Upstream not of a merge, we just continue. I.e. on a
                     * roundabout one lane can be both upstream and downstream.
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
            else
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
                             * The relative lane stays the same if we are searching upstream. This is because traffic on this
                             * adjacent lane will have to change lane to this existing relative lane, before it can be in
                             * relevant interaction with the perceiving GTU. One can think of two lanes merging in to one just
                             * before an on-ramp. Traffic on both lanes is then considered to be on the same relative lane as
                             * the acceleration lane. Otherwise the lateral lane is shifted once.
                             */
                            RelativeLane relativeLane = !down ? record.getRelativeLane() : (latDirection.isLeft()
                                    ? record.getRelativeLane().getLeft() : record.getRelativeLane().getRight());

                            /*
                             * If the zero-position is on the record, the fractional position is used. Otherwise the start
                             * distance is such that the start is equal in a downstream search, and the end is equal in an
                             * upstream search.
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
                             * If the adjacent lane is found in a downstream search and its upstream links are different, we are
                             * dealing with a merge at a distance of the start of these two lanes.
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
                                    mergeDistance = Length.ZERO;
                                }
                            }
                            else
                            {
                                mergeDistance = record.getMergeDistance();
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
                                // this.rootCrossSection.add(lat);
                                this.rootCrossSection.put(lat.getRelativeLane(), lat);
                                latDownQueue.add(lat); // does not matter which lat queue this is, it is root the cross section
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
        }
        this.updated = this.gtu.getSimulator().getSimulatorAbsTime();
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

    /**
     * Iterable over entries (with distance and merge distance stored) of objects. The iterable uses a navigator, lister and
     * distancer.
     * <ul>
     * <li><i>navigator</i>; returns a collection of lane records to continue a search from a covered lane record.</li>
     * <li><i>lister</i>; returns a list of objects from a lane record. The list must be ordered in the search direction (close
     * to far). Objects of any type may be returned as the navigating iterator will check whether objects are of type {@code T}.
     * In order to only include objects in the correct range, the lister must account for the start distance of the record, and
     * any possible relative position of the GTU.</li>
     * <li><i>distancer</i>; returns distance of an object. The distancer must account for any possible relative position of the
     * GTUs.</li>
     * </ul>
     * <p>
     * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     * @param <T> type of object.
     */
    // TODO: Generalize this class so it can be used by conflicts as well. This will probably require the relative lane to be
    // removed, and the starter to include obtaining the cross section.
    private class NavigatingIterable<T> implements Iterable<Entry<T>>
    {
        /** Relative lane. */
        private final RelativeLane relativeLane;

        /** Class of lane-based object type. */
        private final Class<T> clazz;

        /** Range within which objects are included. */
        private final Length range;

        /** Moves up- or downstream as the relevant relative position may be on those lanes. */
        private final Function<LaneRecord, Collection<LaneRecord>> starter;

        /** Navigator which gives the next records. */
        private final Function<LaneRecord, Collection<LaneRecord>> navigator;

        /** Obtains ordered list of objects from lane. */
        private final Function<LaneRecord, List<?>> lister;

        /** Returns distance of object. */
        private final BiFunction<T, LaneRecord, Length> distancer;

        /**
         * Constructor.
         * @param relativeLane relative lane.
         * @param clazz class of lane-based object type.
         * @param range range within which objects are included.
         * @param starter starter.
         * @param navigator navigator.
         * @param lister obtains ordered list of objects from lane.
         * @param distancer returns distance of object.
         */
        public NavigatingIterable(final RelativeLane relativeLane, final Class<T> clazz, final Length range,
                final Function<LaneRecord, Collection<LaneRecord>> starter,
                final Function<LaneRecord, Collection<LaneRecord>> navigator, final Function<LaneRecord, List<?>> lister,
                final BiFunction<T, LaneRecord, Length> distancer)
        {
            this.relativeLane = relativeLane;
            this.clazz = clazz;
            this.range = range;
            this.starter = starter;
            this.navigator = navigator;
            this.lister = lister;
            this.distancer = distancer;
        }

        /** {@inheritDoc} */
        @Override
        public Iterator<Entry<T>> iterator()
        {
            // map of currently iterated records and their object iterators, create map from initial cross-section
            Map<LaneRecord, ObjectIterator<T>> map = new LinkedHashMap<>();
            // TODO: the next two for-loops should be external in something that just gives a set of LaneRecords
            for (LaneRecord record : LaneStructure.this.crossSection.computeIfAbsent(NavigatingIterable.this.relativeLane,
                    (l) -> new LinkedHashSet<>()))
            {
                for (LaneRecord start : this.starter.apply(record))
                {
                    map.put(start, new ObjectIterator<>(start, NavigatingIterable.this.clazz, NavigatingIterable.this.lister,
                            NavigatingIterable.this.distancer));
                }
            }
            return new Iterator<Entry<T>>()
            {
                /** Next entry as found by {@code hasNext()}. */
                private Entry<T> next;

                /** {@inheritDoc} */
                @Override
                public boolean hasNext()
                {
                    if (this.next != null)
                    {
                        return true;
                    }
                    // update the map with records that have something to produce
                    Map<LaneRecord, ObjectIterator<T>> mapCopy = new LinkedHashMap<>(map);
                    for (Map.Entry<LaneRecord, ObjectIterator<T>> mapEntry : mapCopy.entrySet())
                    {
                        if (!mapEntry.getValue().hasNext())
                        {
                            updateMapRecursive(mapEntry.getKey());
                        }
                    }
                    if (map.isEmpty())
                    {
                        this.next = null;
                    }
                    else if (map.size() == 1)
                    {
                        this.next = map.values().iterator().next().next();
                    }
                    else
                    {
                        // loop map and find closest object
                        Length minDistance = Length.POSITIVE_INFINITY;
                        ObjectIterator<T> closestObjectIterator = null;
                        for (ObjectIterator<T> objectIterator : map.values())
                        {
                            Entry<T> entry = objectIterator.poll();
                            if (entry.distance().lt(minDistance))
                            {
                                minDistance = entry.distance();
                                closestObjectIterator = objectIterator;
                            }
                        }
                        this.next = closestObjectIterator.next(); // advance the object iterator; it was only polled above
                    }
                    if (this.next != null && this.next.distance().gt(NavigatingIterable.this.range))
                    {
                        this.next = null; // next object out of range
                    }
                    return this.next != null;
                }

                /** {@inheritDoc} */
                @Override
                public Entry<T> next()
                {
                    Throw.when(!hasNext(), NoSuchElementException.class, "No more object of type %s.",
                            NavigatingIterable.this.clazz);
                    Entry<T> n = this.next;
                    this.next = null;
                    return n;
                }

                /**
                 * Updates the map so it contains a record only if it has an object to return. If not, further records are added
                 * to the map through the navigator and consecutively checked.
                 * @param record lane record.
                 */
                private void updateMapRecursive(final LaneRecord record)
                {
                    if (!map.containsKey(record) || map.get(record).hasNext())
                    {
                        return;
                    }
                    map.remove(record);
                    for (LaneRecord next : NavigatingIterable.this.navigator.apply(record))
                    {
                        map.put(next, new ObjectIterator<>(next, NavigatingIterable.this.clazz, NavigatingIterable.this.lister,
                                NavigatingIterable.this.distancer));
                        updateMapRecursive(next);
                    }
                }
            };
        }
    }

    /**
     * Iterator over objects on a {@code LaneRecord}. This is used by {@code NavigatingIterable} to find object.
     * <p>
     * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     * @param <T> type of object.
     */
    private class ObjectIterator<T> implements Iterator<Entry<T>>
    {
        /** Lane record. */
        private final LaneRecord record;

        /** Class of lane-based object type. */
        private final Class<T> clazz;

        /** List of objects from lane, within correct range for downstream. */
        private final List<?> list;

        /** Index of current entry. */
        private int index;

        /** Returns distance of object. */
        private final BiFunction<T, LaneRecord, Length> distancer;

        /** Poll entry, that next will return. */
        private Entry<T> poll;

        /**
         * Constructor. The lister may return objects of any type. This class will check whether objects are of type T.
         * @param record lane record.
         * @param clazz class of lane-based object type.
         * @param lister obtains ordered list of objects from lane.
         * @param distancer returns distance of object.
         */
        public ObjectIterator(final LaneRecord record, final Class<T> clazz, final Function<LaneRecord, List<?>> lister,
                final BiFunction<T, LaneRecord, Length> distancer)
        {
            this.record = record;
            this.clazz = clazz;
            this.list = lister.apply(record);
            this.distancer = distancer;
        }

        /** {@inheritDoc} */
        @Override
        public boolean hasNext()
        {
            while (this.index < this.list.size() && !this.list.get(this.index).getClass().isAssignableFrom(this.clazz))
            {
                this.index++;
            }
            return this.index < this.list.size();
        }

        /** {@inheritDoc} */
        @Override
        public Entry<T> next()
        {
            Throw.when(!hasNext(), NoSuchElementException.class, "No more object of type %s.", this.clazz);
            Entry<T> entry = poll();
            this.index++;
            this.poll = null;
            return entry;
        }

        /**
         * Returns the entry that {@code next()} will return, without advancing the iterator.
         * @return poll entry.
         */
        public Entry<T> poll()
        {
            if (this.poll == null)
            {
                @SuppressWarnings("unchecked") // isAssignableFrom in hasNext() checks this
                T t = (T) this.list.get(this.index);
                this.poll = new Entry<>(this.distancer.apply(t, this.record), this.record.getMergeDistance(), t);
            }
            return this.poll;
        }

    }

    /**
     * Container for a perceived object with the distance towards it and the distance until the road of the object and the road
     * of the perceiving GTU merge.
     * <p>
     * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     * @param <T> type of object.
     * @param distance distance to object.
     * @param merge distance until the road of the object and the road of the perceiving GTU merge.
     * @param object the perceived object.
     */
    public record Entry<T>(Length distance, Length merge, T object)
    {
    }

}
