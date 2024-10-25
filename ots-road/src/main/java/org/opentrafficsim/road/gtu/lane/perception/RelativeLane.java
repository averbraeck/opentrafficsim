package org.opentrafficsim.road.gtu.lane.perception;

import java.io.Serializable;

import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.network.LateralDirectionality;

/**
 * Defines a lane relative to the current lane.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class RelativeLane implements Comparable<RelativeLane>, Serializable
{

    /** */
    private static final long serialVersionUID = 20160502L;

    /** Second left lane. */
    public static final RelativeLane SECOND_LEFT = new RelativeLane(LateralDirectionality.LEFT, 2);

    /** Left lane. */
    public static final RelativeLane LEFT = new RelativeLane(LateralDirectionality.LEFT, 1);

    /** Current lane. */
    public static final RelativeLane CURRENT = new RelativeLane(LateralDirectionality.NONE, 0);

    /** right lane. */
    public static final RelativeLane RIGHT = new RelativeLane(LateralDirectionality.RIGHT, 1);

    /** Second right lane. */
    public static final RelativeLane SECOND_RIGHT = new RelativeLane(LateralDirectionality.RIGHT, 2);

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
     * Constructor.
     * @param lat lateral direction (use {@code null} for the current lane)
     * @param numLanes number of lanes in the lateral direction (not important for the current lane)
     * @throws IllegalArgumentException if numLanes is not at least 1, except if {@code lat == null} (current lane)
     * @throws IllegalArgumentException if numLanes is not 0 if {@code lat == null} (current lane)
     */
    public RelativeLane(final LateralDirectionality lat, final int numLanes)
    {
        Throw.whenNull(lat, "Lateral directionality may not be null.");
        Throw.when(lat.isNone() && numLanes != 0, IllegalArgumentException.class,
                "Number of lanes must be zero if the lateral directionality is NONE.");
        Throw.when((!lat.isNone()) && numLanes <= 0, IllegalArgumentException.class,
                "Relative lane with %d lanes in %s direction is not allowed, use values > 0.", numLanes, lat);
        this.rank = lat.isLeft() ? -numLanes : numLanes;
    }

    /**
     * Returns the lateral direction.
     * @return the lateral direction
     */
    public final LateralDirectionality getLateralDirectionality()
    {
        // return this.lat;
        return this.rank == 0 ? LateralDirectionality.NONE
                : this.rank < 0 ? LateralDirectionality.LEFT : LateralDirectionality.RIGHT;
    }

    /**
     * Returns the number of lanes in the lateral direction.
     * @return number of lanes in the lateral direction
     */
    public final int getNumLanes()
    {
        return Math.abs(this.rank);
    }

    /**
     * Returns whether the second left lane is referred to.
     * @return whether the second left lane is referred to
     */
    public final boolean isSecondLeft()
    {
        return this.equals(SECOND_LEFT);
    }

    /**
     * Returns whether the left lane is referred to.
     * @return whether the left lane is referred to
     */
    public final boolean isLeft()
    {
        return this.equals(LEFT);
    }

    /**
     * Returns whether the current lane is referred to.
     * @return whether the current lane is referred to
     */
    public final boolean isCurrent()
    {
        return this.equals(CURRENT);
    }

    /**
     * Returns whether the right lane is referred to.
     * @return whether the right lane is referred to
     */
    public final boolean isRight()
    {
        return this.equals(RIGHT);
    }

    /**
     * Returns whether the second right lane is referred to.
     * @return whether the second right lane is referred to
     */
    public final boolean isSecondRight()
    {
        return this.equals(SECOND_RIGHT);
    }

    /**
     * Returns the left hand relative lane of this relative lane.
     * @return left hand relative lane of this relative lane.
     */
    public final RelativeLane getLeft()
    {
        return this.add(LEFT);
    }

    /**
     * Returns the right hand relative lane of this relative lane.
     * @return right hand relative lane of this relative lane.
     */
    public final RelativeLane getRight()
    {
        return this.add(RIGHT);
    }

    /**
     * Returns the relative lane relative to this lane, for example "the left lane" of "the 3rd right lane" is "the 2nd right
     * lane".
     * @param relativeLane relative lane to get of this lane
     * @return relative lane relative to this lane
     */
    public final RelativeLane add(final RelativeLane relativeLane)
    {
        return new RelativeLane(this.rank + relativeLane.rank);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.rank;
        return result;
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
        if (this.rank != other.rank) // relative lane is uniquely defined by the rank
        {
            return false;
        }
        return true;
    }

    @Override
    public final String toString()
    {
        if (this.equals(CURRENT))
        {
            return "RelativeLane [CURRENT]";
        }
        return new StringBuilder("RelativeLane [").append(getLateralDirectionality()).append(", ").append(getNumLanes())
                .append("]").toString();
    }

    @Override
    public final int compareTo(final RelativeLane rel)
    {
        return this.rank - rel.rank;
    }

}
