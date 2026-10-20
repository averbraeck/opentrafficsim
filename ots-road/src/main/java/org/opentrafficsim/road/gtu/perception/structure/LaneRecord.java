package org.opentrafficsim.road.gtu.perception.structure;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.point.Point2d;
import org.opentrafficsim.base.collections.SmallArraySet;
import org.opentrafficsim.base.geometry.FractionalProjectionHelper.FractionalFallback;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.perception.RelativeLane;
import org.opentrafficsim.road.network.Lane;

/**
 * Record of a lane within the lane structure.
 * <p>
 * Copyright (c) 2024-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 */
public class LaneRecord implements LaneRecordInterface<LaneRecord>
{

    /** Lane. */
    private final Lane lane;

    /** Relative lane. */
    private final RelativeLane relativeLane;

    /** Start distance. */
    private double startDistance;

    /** Merge distance, i.e. the distance after which this road merges with the road the GTU is at. */
    private Length mergeDistance;

    /** Set of downstream records. */
    private final Set<LaneRecord> next = new SmallArraySet<>();

    /** Set of upstream records. */
    private final Set<LaneRecord> prev = new SmallArraySet<>();

    /** Set of lateral records. */
    private final Set<LaneRecord> lat = new SmallArraySet<>();

    /** Links to child lane records of which the start distance is defined relative to this lane record. */
    private final List<LinkedRecord> linked = new ArrayList<>(4);

    /**
     * Constructor.
     * @param lane lane.
     * @param relativeLane relative lane.
     * @param startDistance start distance.
     * @param mergeDistance merge distance, i.e. the distance after which this road merges with the road the GTU is at.
     */
    public LaneRecord(final Lane lane, final RelativeLane relativeLane, final double startDistance, final Length mergeDistance)
    {
        this.lane = lane;
        this.relativeLane = relativeLane;
        this.startDistance = startDistance;
        this.mergeDistance = mergeDistance;
    }

    /**
     * Returns the lane.
     * @return lane.
     */
    @Override
    public Lane getLane()
    {
        return this.lane;
    }

    /**
     * Returns the relative lane.
     * @return relative lane.
     */
    public RelativeLane getRelativeLane()
    {
        return this.relativeLane;
    }

    /**
     * Returns the start distance. This value is negative for anything upstream of the reference point of the GTU.
     * @return start distance.
     */
    @Override
    public double getStartDistance()
    {
        return this.startDistance;
    }

    /**
     * Returns the end distance. This value is negative for anything upstream of the reference point of the GTU.
     * @return end distance.
     */
    public double getEndDistance()
    {
        return this.startDistance + this.lane.getLength().si;
    }

    @Override
    public Length getMergeDistance()
    {
        return this.mergeDistance;
    }

    /**
     * Add downstream lane. This optionally creates a linkage to update the start distance of the input downstream record.
     * @param downstream downstream lane.
     * @param link whether to create a linkage to update the start distance
     */
    public void addNext(final LaneRecord downstream, final boolean link)
    {
        this.next.add(downstream);
        if (link)
        {
            this.linked.add(new LinkedRecord(downstream, Linkage.DOWNSTREAM));
        }
    }

    @Override
    public Set<LaneRecord> getNext()
    {
        return this.next;
    }

    /**
     * Add downstream lane. This optionally creates a linkage to update the start distance of the input upstream record.
     * @param upstream downstream lane.
     * @param link whether to create a linkage to update the start distance
     */
    public void addPrev(final LaneRecord upstream, final boolean link)
    {
        this.prev.add(upstream);
        if (link)
        {
            this.linked.add(new LinkedRecord(upstream, Linkage.UPSTREAM));
        }
    }

    @Override
    public Set<LaneRecord> getPrev()
    {
        return this.prev;
    }

    /**
     * Add lateral lane. This creates a linkage to update the start distance of the input lateral record. The lane is only
     * included as part of the set returned by {@link #lateral()} if {@code down == false}.
     * @param lateral downstream lane.
     * @param down part of a downstream search
     */
    public void addLateral(final LaneRecord lateral, final boolean down)
    {
        if (down)
        {
            this.linked.add(new LinkedRecord(lateral, Linkage.LATERAL_DOWNSTREAM));
        }
        else
        {
            this.lat.add(lateral);
            this.linked.add(new LinkedRecord(lateral, Linkage.LATERAL_UPSTREAM));
        }
    }

    @Override
    public Set<LaneRecord> lateral()
    {
        return this.lat;
    }

    /**
     * Returns whether the record is on the route.
     * @param route route.
     * @return whether the record is on the route
     */
    public boolean isOnRoute(final Route route)
    {
        return route == null ? true : route.containsLink(getLane().getLink());
    }

    @Override
    public boolean isDownstreamBranch()
    {
        return this.mergeDistance.si == 0.0 || this.getEndDistance() > 0.0;
    }

    /**
     * Sets the start distance of this record, and invokes setting the start distance to all linked records.
     * @param distance start distance
     * @param structure lane structure a
     */
    public void setStartDistance(final double distance, final LaneStructure structure)
    {
        boolean wasCrossSection = this.startDistance < 0.0 && this.startDistance + getLength().si > 0.0;
        this.startDistance = distance;
        boolean isCrossSection = distance < 0.0 && distance + getLength().si > 0.0;
        if (wasCrossSection != isCrossSection)
        {
            if (wasCrossSection)
            {
                structure.removeFromCrossSection(this);
            }
            else
            {
                structure.addToCrossSection(this);
            }
        }
        for (LinkedRecord linkedRecord : this.linked)
        {
            linkedRecord.updateStartDistance(this, this::getPoint, structure);
        }
    }

    /**
     * Returns the point location along the lane given the start distance.
     * @return point location along the lane given the start distance
     */
    private Point2d getPoint()
    {
        return getLane().getCenterLine().getLocation(-this.startDistance);
    }

    @Override
    public String toString()
    {
        return "LaneRecord [lane=" + this.lane + ", relativeLane=" + this.relativeLane + ", startDistance=" + this.startDistance
                + ", mergeDistance=" + this.mergeDistance + "]";
    }

    /**
     * Lane record combined with a linkage type.
     * @param child lane record of which the start distance is defined relative to a parent (the record holding the link)
     * @param linkage linkage type
     */
    private record LinkedRecord(LaneRecord child, Linkage linkage)
    {

        /**
         * Update the start distance depending on the linkage type.
         * @param parent parent lane record, from which the contained child record was found
         * @param point point supplier for the case lateral linkages
         * @param structure lane structure (required to update cross-section records)
         */
        public void updateStartDistance(final LaneRecord parent, final Supplier<Point2d> point, final LaneStructure structure)
        {
            double startDistance = switch (linkage())
            {
                case DOWNSTREAM -> parent.startDistance + parent.getLength().si;
                case UPSTREAM -> parent.startDistance - child().getLength().si;
                case LATERAL_DOWNSTREAM ->
                {
                    if (parent.startDistance < 0.0 && parent.getEndDistance() > 0.0)
                    {
                        yield getProjectedPoint(point);
                    }
                    yield parent.startDistance;
                }
                case LATERAL_UPSTREAM ->
                {
                    if (parent.startDistance < 0.0 && parent.getEndDistance() > 0.0)
                    {
                        yield getProjectedPoint(point);
                    }
                    yield parent.startDistance + parent.getLength().si - child().getLength().si;
                }
            };
            if (child().mergeDistance.si != 0.0)
            {
                child().mergeDistance =
                        parent.mergeDistance.si == 0.0 ? Length.ofSI(parent.getStartDistance()) : parent.mergeDistance;
            }
            child().setStartDistance(startDistance, structure);
        }

        /**
         * Returns the point projected to the lane of the child lane record.
         * @param point point supplier for the case lateral linkages
         * @return point projected to the lane of the child lane record
         */
        private double getProjectedPoint(final Supplier<Point2d> point)
        {
            Point2d p = point.get();
            double f = child().lane.projectFractionalAt(p.x, p.y, FractionalFallback.ENDPOINT);
            return -f * child().lane.getLength().si;
        }

    }

    /**
     * Linkage type between lane records concerning how the start distance is relatively defined.
     */
    public enum Linkage
    {

        /** Child downstream of parent. */
        DOWNSTREAM,

        /** Child upstream of parent. */
        UPSTREAM,

        /** Child lateral to parent in a downstream search. */
        LATERAL_DOWNSTREAM,

        /** Child lateral to parent in an upstream search. */
        LATERAL_UPSTREAM;

    }

}
