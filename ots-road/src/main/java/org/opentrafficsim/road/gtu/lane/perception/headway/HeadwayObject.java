package org.opentrafficsim.road.gtu.lane.perception.headway;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.GtuException;

/**
 * Container for a reference to information about a (lane based) object and a headway. The Headway can store information about
 * objects ahead of the reference object, behind the reference object, or (partially) parallel to the reference object. In
 * addition to the (perceived) headway, several other pieces of information can be stored, such as (perceived) speed,
 * (perceived) acceleration, (perceived) turn indicators, and (perceived) braking lights. <br>
 * Special care must be taken in curves when perceiving headway of an object on an adjacent lane.The question is whether we
 * perceive the parallel or ahead/behind based on a line perpendicular to the front/back of the object (rectangular), or
 * perpendicular to the center line of the lane (wedge-shaped in case of a curve). The difficulty of a wedge-shaped situation is
 * that reciprocity might be violated: in case of a clothoid, for instance, it is not sure that the point on the center line
 * when projected from lane 1 to lane 2 is the same as the projection from lane 2 to lane 1. The same holds for shapes with
 * sharp bends. Therefore, algorithms implementing headway should only project the <i>reference point</i> of the reference
 * object on the center line of the adjacent lane, and then calculate the forward position and backward position on the adjacent
 * lane based on the reference point. Still, our human perception of what is parallel and what not, is not reflected by
 * fractional positions.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public class HeadwayObject extends AbstractHeadwayCopy
{
    /** */
    private static final long serialVersionUID = 20160410L;

    /**
     * Construct a new Headway information object, for a moving object ahead of us or behind us.
     * @param id the id of the object for comparison purposes, can not be null.
     * @param distance if this constructor is used, distance cannot be null.
     * @param speed the (perceived) speed of the other object; can be null if unknown.
     * @param acceleration the (perceived) acceleration of the other object; can be null if unknown.
     * @throws GtuException when id is null, objectType is null, or parameters are inconsistent
     */
    public HeadwayObject(final String id, final Length distance, final Speed speed, final Acceleration acceleration)
            throws GtuException
    {
        super(ObjectType.OBJECT, id, distance, speed, acceleration);
    }

    /**
     * Construct a new Headway information object, for a non-moving object ahead of us or behind us.
     * @param id the id of the object for comparison purposes, can not be null.
     * @param distance if this constructor is used, distance cannot be null.
     * @throws GtuException when id is null, or parameters are inconsistent
     */
    public HeadwayObject(final String id, final Length distance) throws GtuException
    {
        super(ObjectType.OBJECT, id, distance);
    }

    /**
     * Construct a new Headway information object, for a moving object parallel with us.
     * @param id the id of the object for comparison purposes, can not be null.
     * @param overlapFront the front-front distance to the other object; if this constructor is used, this value cannot be null.
     * @param overlap the 'center' overlap with the other object; if this constructor is used, this value cannot be null.
     * @param overlapRear the rear-rear distance to the other object; if this constructor is used, this value cannot be null.
     * @param speed the (perceived) speed of the other object; can be null if unknown.
     * @param acceleration the (perceived) acceleration of the other object; can be null if unknown.
     * @throws GtuException when id is null, or parameters are inconsistent
     */
    public HeadwayObject(final String id, final Length overlapFront, final Length overlap, final Length overlapRear,
            final Speed speed, final Acceleration acceleration) throws GtuException
    {
        super(ObjectType.OBJECT, id, overlapFront, overlap, overlapRear, speed, acceleration);
    }

    /**
     * Construct a new Headway information object, for a non-moving object parallel with us.
     * @param id the id of the object for comparison purposes, can not be null.
     * @param overlapFront the front-front distance to the other object; if this constructor is used, this value cannot be null.
     * @param overlap the 'center' overlap with the other object; if this constructor is used, this value cannot be null.
     * @param overlapRear the rear-rear distance to the other object; if this constructor is used, this value cannot be null.
     * @throws GtuException when id is null, or parameters are inconsistent
     */
    public HeadwayObject(final String id, final Length overlapFront, final Length overlap, final Length overlapRear)
            throws GtuException
    {
        super(ObjectType.OBJECT, id, overlapFront, overlap, overlapRear);
    }

    /**
     * Construct a new Headway information object, for a moving object ahead of us or behind us.
     * @param id the id of the object for comparison purposes, can not be null.
     * @param distance if this constructor is used, distance cannot be null.
     * @param length the length of the other object, can be null of unknown.
     * @param speed the (perceived) speed of the other object; can be null if unknown.
     * @param acceleration the (perceived) acceleration of the other object; can be null if unknown.
     * @throws GtuException when id is null, objectType is null, or parameters are inconsistent
     */
    public HeadwayObject(final String id, final Length distance, final Length length, final Speed speed,
            final Acceleration acceleration) throws GtuException
    {
        super(ObjectType.OBJECT, id, distance, length, speed, acceleration);
    }

    /**
     * Construct a new Headway information object, for a non-moving object ahead of us or behind us.
     * @param id the id of the object for comparison purposes, can not be null.
     * @param distance if this constructor is used, distance cannot be null.
     * @param length the length of the other object, can be null of unknown.
     * @throws GtuException when id is null, or parameters are inconsistent
     */
    public HeadwayObject(final String id, final Length distance, final Length length) throws GtuException
    {
        super(ObjectType.OBJECT, id, distance, length);
    }

    /**
     * Construct a new Headway information object, for a moving object parallel with us.
     * @param id the id of the object for comparison purposes, can not be null.
     * @param overlapFront the front-front distance to the other object; if this constructor is used, this value cannot be null.
     * @param overlap the 'center' overlap with the other object; if this constructor is used, this value cannot be null.
     * @param overlapRear the rear-rear distance to the other object; if this constructor is used, this value cannot be null.
     * @param length the length of the other object, can be null of unknown.
     * @param speed the (perceived) speed of the other object; can be null if unknown.
     * @param acceleration the (perceived) acceleration of the other object; can be null if unknown.
     * @throws GtuException when id is null, or parameters are inconsistent
     */
    public HeadwayObject(final String id, final Length overlapFront, final Length overlap, final Length overlapRear,
            final Length length, final Speed speed, final Acceleration acceleration) throws GtuException
    {
        super(ObjectType.OBJECT, id, overlapFront, overlap, overlapRear, length, speed, acceleration);
    }

    /**
     * Construct a new Headway information object, for a non-moving object parallel with us.
     * @param id the id of the object for comparison purposes, can not be null.
     * @param overlapFront the front-front distance to the other object; if this constructor is used, this value cannot be null.
     * @param overlap the 'center' overlap with the other object; if this constructor is used, this value cannot be null.
     * @param overlapRear the rear-rear distance to the other object; if this constructor is used, this value cannot be null.
     * @param length the length of the other object, can be null of unknown.
     * @throws GtuException when id is null, or parameters are inconsistent
     */
    public HeadwayObject(final String id, final Length overlapFront, final Length overlap, final Length overlapRear,
            final Length length) throws GtuException
    {
        super(ObjectType.OBJECT, id, overlapFront, overlap, overlapRear, length);
    }

    @Override
    public final String toString()
    {
        return "HeadwayObject []";
    }

}
