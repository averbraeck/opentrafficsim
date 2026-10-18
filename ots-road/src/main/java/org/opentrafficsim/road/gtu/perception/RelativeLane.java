package org.opentrafficsim.road.gtu.perception;

import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.collections.Indexed;
import org.opentrafficsim.core.network.LateralDirectionality;

/**
 * Defines a lane relative to the current lane.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 */
public final class RelativeLane implements Comparable<RelativeLane>, Indexed
{

    /** Cache minimum rank. */
    private static final int MIN_CACHE = -5;

    /** Cache maximum rank. */
    private static final int MAX_CACHE = 5;

    /** Cache. */
    private static final RelativeLane[] CACHE;

    static
    {
        CACHE = new RelativeLane[MAX_CACHE - MIN_CACHE + 1];
        for (int rank = MIN_CACHE; rank <= MAX_CACHE; rank++)
        {
            CACHE[rank - MIN_CACHE] = new RelativeLane(rank);
        }
    }

    /** Second left lane. */
    public static final RelativeLane SECOND_LEFT = RelativeLane.of(LateralDirectionality.LEFT, 2);

    /** Left lane. */
    public static final RelativeLane LEFT = RelativeLane.of(LateralDirectionality.LEFT, 1);

    /** Current lane. */
    public static final RelativeLane CURRENT = RelativeLane.of(LateralDirectionality.NONE, 0);

    /** right lane. */
    public static final RelativeLane RIGHT = RelativeLane.of(LateralDirectionality.RIGHT, 1);

    /** Second right lane. */
    public static final RelativeLane SECOND_RIGHT = RelativeLane.of(LateralDirectionality.RIGHT, 2);

    /**
     * Rank, summarizes both the lateral directionality and the number of lanes. Is zero for CURRENT, otherwise equal to number
     * of lanes for RIGHT, negative number of lanes for LEFT.
     */
    private final int rank;

    /**
     * Private constructor.
     * @param rank the rank
     */
    private RelativeLane(final int rank)
    {
        this.rank = rank;
    }

    /**
     * Returns instance by either creating it, or taking it from a store.
     * @param lat lateral direction (use {@code null} for the current lane)
     * @param numLanes number of lanes in the lateral direction (not important for the current lane)
     * @return relative lane instance
     * @throws IllegalArgumentException if numLanes is not at least 1, except if {@code lat == null} (current lane)
     * @throws IllegalArgumentException if numLanes is not 0 if {@code lat == null} (current lane)
     */
    public static RelativeLane of(final LateralDirectionality lat, final int numLanes)
    {
        Throw.whenNull(lat, "Lateral directionality may not be null.");
        Throw.when(lat.isNone() && numLanes != 0, IllegalArgumentException.class,
                "Number of lanes must be zero if the lateral directionality is NONE.");
        Throw.when((!lat.isNone()) && numLanes <= 0, IllegalArgumentException.class,
                "Relative lane with %d lanes in %s direction is not allowed, use values > 0.", numLanes, lat);
        return of(lat.isLeft() ? -numLanes : numLanes);

    }

    /**
     * Returns relative lane for the given rank.
     * @param rank rank
     * @return relative lane for the given rank
     */
    private static RelativeLane of(final int rank)
    {
        return rank >= MIN_CACHE && rank <= MAX_CACHE ? CACHE[rank - MIN_CACHE] : new RelativeLane(rank);
    }

    /**
     * Returns the lateral direction.
     * @return the lateral direction
     */
    public LateralDirectionality getLateralDirectionality()
    {
        return this.rank == 0 ? LateralDirectionality.NONE
                : this.rank < 0 ? LateralDirectionality.LEFT : LateralDirectionality.RIGHT;
    }

    /**
     * Returns the number of lanes in the lateral direction.
     * @return number of lanes in the lateral direction
     */
    public int getNumLanes()
    {
        return Math.abs(this.rank);
    }

    /**
     * Returns whether the second left lane is referred to.
     * @return whether the second left lane is referred to
     */
    public boolean isSecondLeft()
    {
        return this.rank == -2;
    }

    /**
     * Returns whether the left lane is referred to.
     * @return whether the left lane is referred to
     */
    public boolean isLeft()
    {
        return this.rank == -1;
    }

    /**
     * Returns whether the current lane is referred to.
     * @return whether the current lane is referred to
     */
    public boolean isCurrent()
    {
        return this.rank == 0;
    }

    /**
     * Returns whether the right lane is referred to.
     * @return whether the right lane is referred to
     */
    public boolean isRight()
    {
        return this.rank == 1;
    }

    /**
     * Returns whether the second right lane is referred to.
     * @return whether the second right lane is referred to
     */
    public boolean isSecondRight()
    {
        return this.rank == 2;
    }

    /**
     * Returns the left hand relative lane of this relative lane.
     * @return left hand relative lane of this relative lane.
     */
    public RelativeLane getLeft()
    {
        return of(this.rank - 1);
    }

    /**
     * Returns the right hand relative lane of this relative lane.
     * @return right hand relative lane of this relative lane.
     */
    public RelativeLane getRight()
    {
        return of(this.rank + 1);
    }

    /**
     * Returns the relative lane relative to this lane, for example "the left lane" of "the 3rd right lane" is "the 2nd right
     * lane".
     * @param relativeLane relative lane to get of this lane
     * @return relative lane relative to this lane
     */
    public RelativeLane add(final RelativeLane relativeLane)
    {
        return of(this.rank + relativeLane.rank);
    }

    @Override
    public int hashCode()
    {
        return this.rank;
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        RelativeLane other = (RelativeLane) obj;
        return this.rank == other.rank; // relative lane is uniquely defined by the rank
    }

    @Override
    public String toString()
    {
        if (this.equals(CURRENT))
        {
            return "RelativeLane [CURRENT]";
        }
        return new StringBuilder("RelativeLane [").append(getLateralDirectionality()).append(", ").append(getNumLanes())
                .append("]").toString();
    }

    @Override
    public int compareTo(final RelativeLane rel)
    {
        return this.rank - rel.rank;
    }

    @Override
    public int index()
    {
        return this.rank;
    }

}
