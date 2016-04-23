package org.opentrafficsim.road.gtu.lane.perception;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.Throw;
import org.opentrafficsim.core.gtu.GTUException;

/**
 * Container for a reference to information about a (lane based) GTU and a headway. The Headway can store information about GTUs
 * or objects ahead of the reference GTU, behind the reference GTU, or (partially) parallel to the reference GTU. In addition to
 * the (perceived) headway, several other pieces of information can be stored, such as (perceived) speed, (perceived)
 * acceleration, (perceived) turn indicators, and (perceived) braking lights. <br>
 * Special care must be taken in curves when perceiving headway of a GTU or object on an adjacent lane.The question is whether
 * we perceive the parallel or ahead/behind based on a line perpendicular to the front/back of the GTU (rectangular), or
 * perpendicular to the center line of the lane (wedge-shaped in case of a curve). The difficulty of a wedge-shaped situation is
 * that reciprocity might be violated: in case of a clothoid, for instance, it is not sure that the point on the center line
 * when projected from lane 1 to lane 2 is the same as the projection from lane 2 to lane 1. The same holds for shapes with
 * sharp bends. Therefore, algorithms implementing headway should only project the <i>reference point</i> of the reference GTU
 * on the center line of the adjacent lane, and then calculate the forward position and backward position on the adjacent lane
 * based on the reference point. Still, our human perception of what is parallel and what not, is not reflected by fractional
 * positions. See examples in <a href=
 * "http://simulation.tudelft.nl:8085/browse/OTS-113">http://simulation.tudelft.nl:8085/browse/OTS-113</a>.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision: 1368 $, $LastChangedDate: 2015-09-02 00:20:20 +0200 (Wed, 02 Sep 2015) $, by $Author: averbraeck $,
 *          initial version 11 feb. 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public abstract class AbstractHeadway implements Headway
{
    /** */
    private static final long serialVersionUID = 20160410L;

    /** The id of the other object for comparison purposes, cannot be null. */
    private final String id;

    /** The (perceived) speed of the other object. Can be null if unknown. */
    private final Speed speed;

    /** The (perceived) acceleration of the other object. Can be null if unknown. */
    private final Acceleration acceleration;

    /** The (perceived) distance to the other object. When objects are parallel, the distance is null. */
    private final Length distance;

    /**
     * The (perceived) front overlap to the other object. This value should be null if there is no overlap. In the figure below
     * for two GTUs, it is distance c, positive for GTU1, negative for GTU2.
     * 
     * <pre>
     * ----------
     * |  GTU 1 |          ----->
     * ----------
     *      ---------------
     *      |    GTU 2    |          ----->
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
     * |  GTU 1 |          ----->
     * ----------
     *      ---------------
     *      |    GTU 2    |          ----->
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
     * |  GTU 1 |          ----->
     * ----------
     *      ---------------
     *      |    GTU 2    |          ----->
     *      ---------------
     * | a  | b |     c   |
     * </pre>
     */
    private final Length overlap;

    /** The object type. */
    private final ObjectType objectType;

    /**
     * Construct a new Headway information object, for an object in front, behind, or in parallel with us. TODO fix this
     * javadoc; there are obvious inconsistencies between this javadoc and the code below.
     * @param objectType the perceived object type, can be null if object type unknown.
     * @param id the id of the object for comparison purposes, can not be null.
     * @param distance the distance to the other object; if this constructor is used, distance cannot be null.
     * @param overlapFront the front-front distance to the other object; if this constructor is used, this value cannot be null.
     * @param overlap the 'center' overlap with the other object; if this constructor is used, this value cannot be null.
     * @param overlapRear the rear-rear distance to the other object; if this constructor is used, this value cannot be null.
     * @param speed the (perceived) speed of the other object; can be null if unknown.
     * @param acceleration the (perceived) acceleration of the other object; can be null if unknown.
     * @throws GTUException when id is null, or parameters are inconsistent
     */
    @SuppressWarnings("checkstyle:parameternumber")
    private AbstractHeadway(final ObjectType objectType, final String id, final Length distance, final Speed speed,
            final Acceleration acceleration, final Length overlapFront, final Length overlap,
            final Length overlapRear) throws GTUException
    {
        Throw.when(id == null, GTUException.class, "Object id of a headway cannot be null");
        this.id = id;

        this.objectType = objectType;
        this.speed = speed;
        this.acceleration = acceleration;

        Throw.when(distance != null && (overlap != null || overlapFront != null || overlapRear != null), GTUException.class,
                "overlap parameter cannot be null for front / rear headway with id = %s", id);
        this.distance = distance;

        Throw.when(distance == null && (overlap == null || overlapFront == null || overlapRear == null), GTUException.class,
                "overlap parameter cannot be null for parallel headway with id = %s", id);
        Throw.when(overlap != null && overlap.si < 0, GTUException.class, "overlap cannot be negative; id = %s", id);
        this.overlap = overlap;
        this.overlapFront = overlapFront;
        this.overlapRear = overlapRear;
    }

    /**
     * Construct a new Headway information object, for a moving object ahead of us or behind us.
     * @param objectType the perceived object type, can be null if object type unknown.
     * @param id the id of the object for comparison purposes, can not be null.
     * @param distance the distance to the other object; if this constructor is used, distance cannot be null.
     * @param speed the (perceived) speed of the other object; can be null if unknown.
     * @param acceleration the (perceived) acceleration of the other object; can be null if unknown.
     * @throws GTUException when id is null, or parameters are inconsistent
     */
    public AbstractHeadway(final ObjectType objectType, final String id, final Length distance, final Speed speed,
            final Acceleration acceleration) throws GTUException
    {
        this(objectType, id, distance, speed, acceleration, null, null, null);
    }

    /**
     * Construct a new Headway information object, for a non-moving object ahead of us or behind us.
     * @param objectType the perceived object type, can be null if object type unknown.
     * @param id the id of the object for comparison purposes, can not be null.
     * @param distance the distance to the other object; if this constructor is used, distance cannot be null.
     * @throws GTUException when id is null, or parameters are inconsistent
     */
    public AbstractHeadway(final ObjectType objectType, final String id, final Length distance) throws GTUException
    {
        this(objectType, id, distance, null, null, null, null, null);
    }

    /**
     * Construct a new Headway information object, for a moving object parallel with us.
     * @param objectType the perceived object type, can be null if object type unknown.
     * @param id the id of the object for comparison purposes, can not be null.
     * @param overlapFront the front-front distance to the other object; if this constructor is used, this value cannot be null.
     * @param overlap the 'center' overlap with the other object; if this constructor is used, this value cannot be null.
     * @param overlapRear the rear-rear distance to the other object; if this constructor is used, this value cannot be null.
     * @param speed the (perceived) speed of the other object; can be null if unknown.
     * @param acceleration the (perceived) acceleration of the other object; can be null if unknown.
     * @throws GTUException when id is null, or parameters are inconsistent
     */
    public AbstractHeadway(final ObjectType objectType, final String id, final Length overlapFront,
            final Length overlap, final Length overlapRear, final Speed speed, final Acceleration acceleration)
            throws GTUException
    {
        this(objectType, id, null, speed, acceleration, overlapFront, overlap, overlapRear);
    }

    /**
     * Construct a new Headway information object, for a non-moving object parallel with us.
     * @param objectType the perceived object type, can be null if object type unknown.
     * @param id the id of the object for comparison purposes, can not be null.
     * @param overlapFront the front-front distance to the other object; if this constructor is used, this value cannot be null.
     * @param overlap the 'center' overlap with the other object; if this constructor is used, this value cannot be null.
     * @param overlapRear the rear-rear distance to the other object; if this constructor is used, this value cannot be null.
     * @throws GTUException when id is null, or parameters are inconsistent
     */
    public AbstractHeadway(final ObjectType objectType, final String id, final Length overlapFront,
            final Length overlap, final Length overlapRear) throws GTUException
    {
        this(objectType, id, overlapFront, overlap, overlapRear, null, null);
    }

    /** {@inheritDoc} */
    @Override
    public final String getId()
    {
        return this.id;
    }

    /** {@inheritDoc} */
    @Override
    public final Speed getSpeed()
    {
        return this.speed;
    }

    /** {@inheritDoc} */
    @Override
    public final Length getDistance()
    {
        return this.distance;
    }

    /** {@inheritDoc} */
    @Override
    public final ObjectType getObjectType()
    {
        return this.objectType;
    }

    /** {@inheritDoc} */
    @Override
    public final Acceleration getAcceleration()
    {
        return this.acceleration;
    }

    /** {@inheritDoc} */
    @Override
    public final Length getOverlapFront()
    {
        return this.overlapFront;
    }

    /** {@inheritDoc} */
    @Override
    public final Length getOverlapRear()
    {
        return this.overlapRear;
    }

    /** {@inheritDoc} */
    @Override
    public final Length getOverlap()
    {
        return this.overlap;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isAhead()
    {
        return this.distance != null && this.distance.si > 0.0;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isBehind()
    {
        return this.distance != null && this.distance.si < 0.0;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isParallel()
    {
        return this.overlap != null;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.acceleration == null) ? 0 : this.acceleration.hashCode());
        result = prime * result + ((this.distance == null) ? 0 : this.distance.hashCode());
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        result = prime * result + ((this.objectType == null) ? 0 : this.objectType.hashCode());
        result = prime * result + ((this.overlap == null) ? 0 : this.overlap.hashCode());
        result = prime * result + ((this.overlapFront == null) ? 0 : this.overlapFront.hashCode());
        result = prime * result + ((this.overlapRear == null) ? 0 : this.overlapRear.hashCode());
        result = prime * result + ((this.speed == null) ? 0 : this.speed.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @SuppressWarnings({ "checkstyle:designforextension", "checkstyle:needbraces" })
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
        if (this.acceleration == null)
        {
            if (other.acceleration != null)
                return false;
        }
        else if (!this.acceleration.equals(other.acceleration))
            return false;
        if (this.distance == null)
        {
            if (other.distance != null)
                return false;
        }
        else if (!this.distance.equals(other.distance))
            return false;
        if (this.id == null)
        {
            if (other.id != null)
                return false;
        }
        else if (!this.id.equals(other.id))
            return false;
        if (this.objectType != other.objectType)
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
        if (this.speed == null)
        {
            if (other.speed != null)
                return false;
        }
        else if (!this.speed.equals(other.speed))
            return false;
        return true;
    }

    /** {@inheritDoc} */
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
