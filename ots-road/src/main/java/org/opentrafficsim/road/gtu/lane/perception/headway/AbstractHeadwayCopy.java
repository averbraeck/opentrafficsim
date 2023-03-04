package org.opentrafficsim.road.gtu.lane.perception.headway;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.gtu.GtuException;

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
 * positions. 
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public abstract class AbstractHeadwayCopy extends AbstractHeadway
{
    /** */
    private static final long serialVersionUID = 20160410L;

    /** The id of the other object for comparison purposes, cannot be null. */
    private final String id;

    /** The (perceived) length of the other object. Can be null if unknown. */
    private final Length length;

    /** The (perceived) speed of the other object. v&gt;0 seen from driver chair. Can be null if unknown. */
    private final Speed speed;

    /** The (perceived) acceleration of the other object. Can be null if unknown. */
    private final Acceleration acceleration;

    /** The object type. */
    private final ObjectType objectType;

    /**
     * Construct a new Headway information object, for an object in front, behind, or in parallel with us. <br>
     * @param objectType ObjectType; the perceived object type, can be null if object type unknown.
     * @param id String; the id of the object for comparison purposes, can not be null.
     * @param distance Length; the distance to the other object
     * @param length Length; the length of the other object, can be null if not applicable.
     * @param overlapFront Length; the front-front distance to the other object
     * @param overlap Length; the 'center' overlap with the other object
     * @param overlapRear Length; the rear-rear distance to the other object
     * @param speed the (perceived) speed of the other object; can be null if unknown.
     * @param acceleration the (perceived) acceleration of the other object; can be null if unknown.
     * @throws GtuException when id is null, or parameters are inconsistent
     */
    @SuppressWarnings("checkstyle:parameternumber")
    private AbstractHeadwayCopy(final ObjectType objectType, final String id, final Length distance, final Length length,
            final Speed speed, final Acceleration acceleration, final Length overlapFront, final Length overlap,
            final Length overlapRear) throws GtuException
    {
        super(distance, overlapFront, overlap, overlapRear);
        Throw.when(id == null, GtuException.class, "Object id of a headway cannot be null");
        this.id = id;

        this.objectType = objectType;
        this.length = length;
        this.speed = speed;
        this.acceleration = acceleration;
    }

    /**
     * Construct a new Headway information object, for a moving object ahead of us or behind us.
     * @param objectType ObjectType; the perceived object type, can be null if object type unknown.
     * @param id String; the id of the object for comparison purposes, can not be null.
     * @param distance the distance to the other object; if this constructor is used, distance cannot be null.
     * @param speed the (perceived) speed of the other object; can be null if unknown.
     * @param acceleration the (perceived) acceleration of the other object; can be null if unknown.
     * @throws GtuException when id is null, or parameters are inconsistent
     */
    public AbstractHeadwayCopy(final ObjectType objectType, final String id, final Length distance, final Speed speed,
            final Acceleration acceleration) throws GtuException
    {
        this(objectType, id, distance, null, speed, acceleration, null, null, null);
    }

    /**
     * Construct a new Headway information object, for a non-moving object ahead of us or behind us.
     * @param objectType ObjectType; the perceived object type, can be null if object type unknown.
     * @param id String; the id of the object for comparison purposes, can not be null.
     * @param distance the distance to the other object; if this constructor is used, distance cannot be null.
     * @throws GtuException when id is null, or parameters are inconsistent
     */
    public AbstractHeadwayCopy(final ObjectType objectType, final String id, final Length distance) throws GtuException
    {
        this(objectType, id, distance, null, Speed.ZERO, Acceleration.ZERO, null, null, null);
    }

    /**
     * Construct a new Headway information object, for a moving object parallel with us.
     * @param objectType ObjectType; the perceived object type, can be null if object type unknown.
     * @param id String; the id of the object for comparison purposes, can not be null.
     * @param overlapFront the front-front distance to the other object; if this constructor is used, this value cannot be null.
     * @param overlap the 'center' overlap with the other object; if this constructor is used, this value cannot be null.
     * @param overlapRear the rear-rear distance to the other object; if this constructor is used, this value cannot be null.
     * @param speed the (perceived) speed of the other object; can be null if unknown.
     * @param acceleration the (perceived) acceleration of the other object; can be null if unknown.
     * @throws GtuException when id is null, or parameters are inconsistent
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public AbstractHeadwayCopy(final ObjectType objectType, final String id, final Length overlapFront, final Length overlap,
            final Length overlapRear, final Speed speed, final Acceleration acceleration) throws GtuException
    {
        this(objectType, id, null, null, speed, acceleration, overlapFront, overlap, overlapRear);
    }

    /**
     * Construct a new Headway information object, for a non-moving object parallel with us.
     * @param objectType ObjectType; the perceived object type, can be null if object type unknown.
     * @param id String; the id of the object for comparison purposes, can not be null.
     * @param overlapFront the front-front distance to the other object; if this constructor is used, this value cannot be null.
     * @param overlap the 'center' overlap with the other object; if this constructor is used, this value cannot be null.
     * @param overlapRear the rear-rear distance to the other object; if this constructor is used, this value cannot be null.
     * @throws GtuException when id is null, or parameters are inconsistent
     */
    public AbstractHeadwayCopy(final ObjectType objectType, final String id, final Length overlapFront, final Length overlap,
            final Length overlapRear) throws GtuException
    {
        this(objectType, id, null, null, null, null, overlapFront, overlap, overlapRear);
    }

    /**
     * Construct a new Headway information object, for a moving object ahead of us or behind us.
     * @param objectType ObjectType; the perceived object type, can be null if object type unknown.
     * @param id String; the id of the object for comparison purposes, can not be null.
     * @param distance the distance to the other object; if this constructor is used, distance cannot be null.
     * @param length the length of the other object; if this constructor is used, length cannot be null.
     * @param speed the (perceived) speed of the other object; can be null if unknown.
     * @param acceleration the (perceived) acceleration of the other object; can be null if unknown.
     * @throws GtuException when id is null, or parameters are inconsistent
     */
    public AbstractHeadwayCopy(final ObjectType objectType, final String id, final Length distance, final Length length,
            final Speed speed, final Acceleration acceleration) throws GtuException
    {
        this(objectType, id, distance, length, speed, acceleration, null, null, null);
        Throw.whenNull(length, "Length may not be null.");
    }

    /**
     * Construct a new Headway information object, for a non-moving object ahead of us or behind us.
     * @param objectType ObjectType; the perceived object type, can be null if object type unknown.
     * @param id String; the id of the object for comparison purposes, can not be null.
     * @param distance the distance to the other object; if this constructor is used, distance cannot be null.
     * @param length the length of the other object; if this constructor is used, length cannot be null.
     * @throws GtuException when id is null, or parameters are inconsistent
     */
    public AbstractHeadwayCopy(final ObjectType objectType, final String id, final Length distance, final Length length)
            throws GtuException
    {
        this(objectType, id, distance, length, null, null, null, null, null);
        Throw.whenNull(length, "Length may not be null.");
    }

    /**
     * Construct a new Headway information object, for a moving object parallel with us.
     * @param objectType ObjectType; the perceived object type, can be null if object type unknown.
     * @param id String; the id of the object for comparison purposes, can not be null.
     * @param overlapFront the front-front distance to the other object; if this constructor is used, this value cannot be null.
     * @param overlap the 'center' overlap with the other object; if this constructor is used, this value cannot be null.
     * @param overlapRear the rear-rear distance to the other object; if this constructor is used, this value cannot be null.
     * @param length the length of the other object; if this constructor is used, length cannot be null.
     * @param speed the (perceived) speed of the other object; can be null if unknown.
     * @param acceleration the (perceived) acceleration of the other object; can be null if unknown.
     * @throws GtuException when id is null, or parameters are inconsistent
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public AbstractHeadwayCopy(final ObjectType objectType, final String id, final Length overlapFront, final Length overlap,
            final Length overlapRear, final Length length, final Speed speed, final Acceleration acceleration)
            throws GtuException
    {
        this(objectType, id, null, length, speed, acceleration, overlapFront, overlap, overlapRear);
        Throw.whenNull(length, "Length may not be null.");
    }

    /**
     * Construct a new Headway information object, for a non-moving object parallel with us.
     * @param objectType ObjectType; the perceived object type, can be null if object type unknown.
     * @param id String; the id of the object for comparison purposes, can not be null.
     * @param overlapFront the front-front distance to the other object; if this constructor is used, this value cannot be null.
     * @param overlap the 'center' overlap with the other object; if this constructor is used, this value cannot be null.
     * @param overlapRear the rear-rear distance to the other object; if this constructor is used, this value cannot be null.
     * @param length the length of the other object; if this constructor is used, length cannot be null.
     * @throws GtuException when id is null, or parameters are inconsistent
     */
    public AbstractHeadwayCopy(final ObjectType objectType, final String id, final Length overlapFront, final Length overlap,
            final Length overlapRear, final Length length) throws GtuException
    {
        this(objectType, id, null, length, null, null, overlapFront, overlap, overlapRear);
        Throw.whenNull(length, "Length may not be null.");
    }

    /** {@inheritDoc} */
    @Override
    public final String getId()
    {
        return this.id;
    }

    /** {@inheritDoc} */
    @Override
    public final Length getLength()
    {
        return this.length;
    }

    /** {@inheritDoc} */
    @Override
    public final Speed getSpeed()
    {
        return this.speed;
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
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.acceleration == null) ? 0 : this.acceleration.hashCode());
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        result = prime * result + ((this.length == null) ? 0 : this.length.hashCode());
        result = prime * result + ((this.objectType == null) ? 0 : this.objectType.hashCode());
        result = prime * result + ((this.speed == null) ? 0 : this.speed.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!super.equals(obj))
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        AbstractHeadwayCopy other = (AbstractHeadwayCopy) obj;
        if (this.acceleration == null)
        {
            if (other.acceleration != null)
            {
                return false;
            }
        }
        else if (!this.acceleration.equals(other.acceleration))
        {
            return false;
        }
        if (this.id == null)
        {
            if (other.id != null)
            {
                return false;
            }
        }
        else if (!this.id.equals(other.id))
        {
            return false;
        }
        if (this.length == null)
        {
            if (other.length != null)
            {
                return false;
            }
        }
        else if (!this.length.equals(other.length))
        {
            return false;
        }
        if (this.objectType != other.objectType)
        {
            return false;
        }
        if (this.speed == null)
        {
            if (other.speed != null)
            {
                return false;
            }
        }
        else if (!this.speed.equals(other.speed))
        {
            return false;
        }
        return true;
    }

}
