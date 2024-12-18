package org.opentrafficsim.road.gtu.lane.perception.headway;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.gtu.GtuException;

/**
 * Super class for non-delayed and non-erroneous perception. Sub classes should wrap the actual simulation object to obtain
 * information. One exception to this is {@link AbstractHeadwayCopy} (and all it's sub classes), which contains such information
 * directly, and is a super class for delayed and/or erroneous perception.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public abstract class AbstractHeadway implements Headway
{

    /** */
    private static final long serialVersionUID = 20170324L;

    /** The (perceived) distance to the other object. When objects are parallel, the distance is null. */
    private final Length distance;

    /**
     * The (perceived) front overlap to the other object. This value should be null if there is no overlap. In the figure below
     * for two GTUs, it is distance c, positive for GTU1, negative for GTU2.
     * 
     * <pre>
     * ----------
     * |  GTU 1 |          -----&gt;
     * ----------
     *      ---------------
     *      |    GTU 2    |          -----&gt;
     *      ---------------
     * | a  | b |     c   |
     * </pre>
     */
    private final Length overlapFront;

    /**
     * The (perceived) rear overlap to the other object. This value should be null if there is no overlap. In the figure below
     * for two GTUs, it is distance a, positive for GTU1, negative for GTU2.
     * 
     * <pre>
     * ----------
     * |  GTU 1 |          -----&gt;
     * ----------
     *      ---------------
     *      |    GTU 2    |          -----&gt;
     *      ---------------
     * | a  | b |     c   |
     * </pre>
     */
    private final Length overlapRear;

    /**
     * The (perceived) overlap with the other object. This value should be null if there is no overlap. In the figure below for
     * two GTUs, it is distance b, positive for GTU1 and GTU2.
     * 
     * <pre>
     * ----------
     * |  GTU 1 |          -----&gt;
     * ----------
     *      ---------------
     *      |    GTU 2    |          -----&gt;
     *      ---------------
     * | a  | b |     c   |
     * </pre>
     */
    private final Length overlap;

    /**
     * Construct a new Headway information object, for an object in front, behind, or in parallel with us. <br>
     * @param distance the distance to the other object
     * @param overlapFront the front-front distance to the other object
     * @param overlap the 'center' overlap with the other object
     * @param overlapRear the rear-rear distance to the other object
     * @throws GtuException when id is null, or parameters are inconsistent
     */
    protected AbstractHeadway(final Length distance, final Length overlapFront, final Length overlap, final Length overlapRear)
            throws GtuException
    {
        Throw.when(distance != null && (overlap != null || overlapFront != null || overlapRear != null), GtuException.class,
                "overlap parameter cannot be null for front / rear headway");
        this.distance = distance;

        Throw.when(distance == null && (overlap == null || overlapFront == null || overlapRear == null), GtuException.class,
                "overlap parameter cannot be null for parallel headway");
        Throw.when(overlap != null && overlap.si < 0, GtuException.class, "overlap cannot be negative");
        this.overlap = overlap;
        this.overlapFront = overlapFront;
        this.overlapRear = overlapRear;
    }

    /**
     * Construct a new Headway information object, for an object ahead of us or behind us.
     * @param distance if this constructor is used, distance cannot be null.
     * @throws GtuException when id is null, or parameters are inconsistent
     */
    public AbstractHeadway(final Length distance) throws GtuException
    {
        this(distance, null, null, null);
    }

    /**
     * Construct a new Headway information object, for an object parallel with us.
     * @param overlapFront the front-front distance to the other object; if this constructor is used, this value cannot be null.
     * @param overlap the 'center' overlap with the other object; if this constructor is used, this value cannot be null.
     * @param overlapRear the rear-rear distance to the other object; if this constructor is used, this value cannot be null.
     * @throws GtuException when id is null, or parameters are inconsistent
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public AbstractHeadway(final Length overlapFront, final Length overlap, final Length overlapRear) throws GtuException
    {
        this(null, overlapFront, overlap, overlapRear);
    }

    @Override
    public final Length getDistance()
    {
        return this.distance;
    }

    @Override
    public final Length getOverlapFront()
    {
        return this.overlapFront;
    }

    @Override
    public final Length getOverlapRear()
    {
        return this.overlapRear;
    }

    @Override
    public final Length getOverlap()
    {
        return this.overlap;
    }

    @Override
    public final boolean isAhead()
    {
        return this.distance != null && this.distance.si > 0.0;
    }

    @Override
    public final boolean isBehind()
    {
        return this.distance != null && this.distance.si < 0.0;
    }

    @Override
    public final boolean isParallel()
    {
        return this.overlap != null;
    }

    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.distance == null) ? 0 : this.distance.hashCode());
        result = prime * result + ((this.overlap == null) ? 0 : this.overlap.hashCode());
        result = prime * result + ((this.overlapFront == null) ? 0 : this.overlapFront.hashCode());
        result = prime * result + ((this.overlapRear == null) ? 0 : this.overlapRear.hashCode());
        return result;
    }

    @SuppressWarnings({"checkstyle:designforextension", "checkstyle:needbraces"})
    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AbstractHeadway other = (AbstractHeadway) obj;
        if (this.distance == null)
        {
            if (other.distance != null)
                return false;
        }
        else if (!this.distance.equals(other.distance))
            return false;
        if (this.overlap == null)
        {
            if (other.overlap != null)
                return false;
        }
        else if (!this.overlap.equals(other.overlap))
            return false;
        if (this.overlapFront == null)
        {
            if (other.overlapFront != null)
                return false;
        }
        else if (!this.overlapFront.equals(other.overlapFront))
            return false;
        if (this.overlapRear == null)
        {
            if (other.overlapRear != null)
                return false;
        }
        else if (!this.overlapRear.equals(other.overlapRear))
            return false;
        return true;
    }

    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public String toString()
    {
        if (isParallel())
        {
            return String.format("Parallel to object %s of type %s with speed %s", getId(), getObjectType(), getSpeed());
        }
        return String.format("Headway %s to object %s of type %s with speed %s", getDistance(), getId(), getObjectType(),
                getSpeed());
    }

}
