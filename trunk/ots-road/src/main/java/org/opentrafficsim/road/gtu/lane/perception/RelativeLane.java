package org.opentrafficsim.road.gtu.lane.perception;

import java.io.Serializable;

import org.opentrafficsim.core.Throw;
import org.opentrafficsim.core.network.LateralDirectionality;

/**
 * Defines a lane relative to the current lane.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version May 2, 2016 <br>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
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
    public static final RelativeLane CURRENT = new RelativeLane(null, 0);

    /** right lane. */
    public static final RelativeLane RIGHT = new RelativeLane(LateralDirectionality.RIGHT, 1);

    /** Second right lane. */
    public static final RelativeLane SECOND_RIGHT = new RelativeLane(LateralDirectionality.RIGHT, 2);

    /** Lateral direction. */
    private final LateralDirectionality lat;

    /** Number of lanes to lateral direction. */
    private final int numLanes;

    /**
     * Constructor.
     * @param lat lateral direction (use {@code null} for the current lane)
     * @param numLanes number of lanes in the lateral direction (not important for the current lane)
     * @throws IllegalArgumentException if numLanes is not at least 1, except if {@code lat == null} (current lane)
     * @throws IllegalArgumentException if numLanes is not 0 if {@code lat == null} (current lane)
     */
    public RelativeLane(final LateralDirectionality lat, final int numLanes)
    {
        Throw.when(lat != null && numLanes <= 0, IllegalArgumentException.class,
            "Relative lane with %d lanes in %s direction is not allowed, use values > 0.", numLanes, lat);
        Throw.when(lat == null && numLanes != 0, IllegalArgumentException.class,
            "Number of lanes must be zero if the lateral directionality is null.");
        this.lat = lat;
        this.numLanes = numLanes;
    }

    /**
     * Returns the lateral direction.
     * @return lat lateral direction
     */
    public final LateralDirectionality getLateralDirectionality()
    {
        return this.lat;
    }

    /**
     * Returns the number of lanes in the lateral direction.
     * @return number of lanes in the lateral direction
     */
    public final int getNumLanes()
    {
        return this.numLanes;
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
     * Returns the relative lane relative to this lane, for example "the left lane" of "the 3rd right lane" is
     * "the 2nd right lane".
     * @param relativeLane relative lane to get of this lane
     * @return relative lane relative to this lane
     */
    public final RelativeLane add(final RelativeLane relativeLane)
    {
        int nThis = this.lat.isNone() ? 0 : this.lat.isLeft() ? -this.numLanes : this.numLanes;
        int nOther = relativeLane.lat.isNone() ? 0 : relativeLane.lat.isLeft() ? -relativeLane.numLanes : relativeLane.numLanes;
        int nSum = nThis + nOther;
        if (nSum < 0)
        {
            return new RelativeLane(LateralDirectionality.LEFT, -nSum);
        }
        if (nSum > 0)
        {
            return new RelativeLane(LateralDirectionality.RIGHT, nSum);
        }
        return CURRENT;
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode()
    {
        int result = 17;
        result = 31 * result + (this.lat != null ? this.lat.hashCode() : 0);
        result = 31 * result + this.numLanes;
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (!(obj instanceof RelativeLane))
        {
            return false;
        }
        RelativeLane rel = (RelativeLane) obj;
        if (rel.lat == this.lat && rel.numLanes == this.numLanes)
        {
            return true;
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        if (this.equals(CURRENT))
        {
            return "RelativeLane [CURRENT]";
        }
        return new StringBuilder("RelativeLane [").append(this.lat).append(", ").append(this.numLanes).append("]")
            .toString();
    }

    /** {@inheritDoc} */
    @Override
    public final int compareTo(final RelativeLane rel)
    {
        int nThis = this.lat.isNone() ? 0 : this.lat.isLeft() ? -this.numLanes : this.numLanes;
        int nRel = rel.lat.isNone() ? 0 : rel.lat.isLeft() ? -rel.numLanes : rel.numLanes;
        return nThis - nRel;
    }

}
