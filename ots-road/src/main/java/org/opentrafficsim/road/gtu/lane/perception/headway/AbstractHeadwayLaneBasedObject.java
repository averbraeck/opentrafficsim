package org.opentrafficsim.road.gtu.lane.perception.headway;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * Lane based object headway with constructors for stationary information.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public abstract class AbstractHeadwayLaneBasedObject extends AbstractHeadwayCopy implements HeadwayLaneBasedObject
{

    /** */
    private static final long serialVersionUID = 20190515L;

    /** Lane. */
    private final Lane lane;

    /**
     * Construct a new Headway information object, for a non-moving object parallel with us.
     * @param objectType the perceived object type, can be null if object type unknown.
     * @param id the id of the object for comparison purposes, can not be null.
     * @param overlapFront the front-front distance to the other object; if this constructor is used, this value cannot be null.
     * @param overlap the 'center' overlap with the other object; if this constructor is used, this value cannot be null.
     * @param overlapRear the rear-rear distance to the other object; if this constructor is used, this value cannot be null.
     * @param length if this constructor is used, length cannot be null.
     * @param lane the lane.
     * @throws GtuException when id is null, or parameters are inconsistent
     */
    public AbstractHeadwayLaneBasedObject(final ObjectType objectType, final String id, final Length overlapFront,
            final Length overlap, final Length overlapRear, final Length length, final Lane lane) throws GtuException
    {
        super(objectType, id, overlapFront, overlap, overlapRear, length);
        this.lane = lane;
    }

    /**
     * Construct a new Headway information object, for a non-moving object parallel with us.
     * @param objectType the perceived object type, can be null if object type unknown.
     * @param id the id of the object for comparison purposes, can not be null.
     * @param overlapFront the front-front distance to the other object; if this constructor is used, this value cannot be null.
     * @param overlap the 'center' overlap with the other object; if this constructor is used, this value cannot be null.
     * @param overlapRear the rear-rear distance to the other object; if this constructor is used, this value cannot be null.
     * @param lane the lane.
     * @throws GtuException when id is null, or parameters are inconsistent
     */
    public AbstractHeadwayLaneBasedObject(final ObjectType objectType, final String id, final Length overlapFront,
            final Length overlap, final Length overlapRear, final Lane lane) throws GtuException
    {
        super(objectType, id, overlapFront, overlap, overlapRear);
        this.lane = lane;
    }

    /**
     * Construct a new Headway information object, for a non-moving object ahead of us or behind us.
     * @param objectType the perceived object type, can be null if object type unknown.
     * @param id the id of the object for comparison purposes, can not be null.
     * @param distance if this constructor is used, distance cannot be null.
     * @param length if this constructor is used, length cannot be null.
     * @param lane the lane.
     * @throws GtuException when id is null, or parameters are inconsistent
     */
    public AbstractHeadwayLaneBasedObject(final ObjectType objectType, final String id, final Length distance,
            final Length length, final Lane lane) throws GtuException
    {
        super(objectType, id, distance, length);
        this.lane = lane;
    }

    /**
     * Construct a new Headway information object, for a non-moving object ahead of us or behind us.
     * @param objectType the perceived object type, can be null if object type unknown.
     * @param id the id of the object for comparison purposes, can not be null.
     * @param distance if this constructor is used, distance cannot be null.
     * @param lane the lane.
     * @throws GtuException when id is null, or parameters are inconsistent
     */
    public AbstractHeadwayLaneBasedObject(final ObjectType objectType, final String id, final Length distance, final Lane lane)
            throws GtuException
    {
        super(objectType, id, distance);
        this.lane = lane;
    }

    @Override
    public Lane getLane()
    {
        return this.lane;
    }

}
