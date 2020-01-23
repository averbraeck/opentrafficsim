package org.opentrafficsim.road.gtu.lane.perception.headway;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * Lane based object headway with constructors for stationary information.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version May 15, 2019 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public abstract class AbstractHeadwayLaneBasedObject extends AbstractHeadwayCopy implements HeadwayLaneBasedObject
{

    /** */
    private static final long serialVersionUID = 20190515L;
    
    /** Lane. */
    private final Lane lane;

    /**
     * Construct a new Headway information object, for a non-moving object parallel with us.
     * @param objectType ObjectType; the perceived object type, can be null if object type unknown.
     * @param id String; the id of the object for comparison purposes, can not be null.
     * @param overlapFront the front-front distance to the other object; if this constructor is used, this value cannot be null.
     * @param overlap the 'center' overlap with the other object; if this constructor is used, this value cannot be null.
     * @param overlapRear the rear-rear distance to the other object; if this constructor is used, this value cannot be null.
     * @param length the length of the other object; if this constructor is used, length cannot be null.
     * @param lane Lane; the lane.
     * @throws GTUException when id is null, or parameters are inconsistent
     */
    public AbstractHeadwayLaneBasedObject(final ObjectType objectType, final String id, final Length overlapFront,
            final Length overlap, final Length overlapRear, final Length length, final Lane lane) throws GTUException
    {
        super(objectType, id, overlapFront, overlap, overlapRear, length);
        this.lane = lane;
    }

    /**
     * Construct a new Headway information object, for a non-moving object parallel with us.
     * @param objectType ObjectType; the perceived object type, can be null if object type unknown.
     * @param id String; the id of the object for comparison purposes, can not be null.
     * @param overlapFront the front-front distance to the other object; if this constructor is used, this value cannot be null.
     * @param overlap the 'center' overlap with the other object; if this constructor is used, this value cannot be null.
     * @param overlapRear the rear-rear distance to the other object; if this constructor is used, this value cannot be null.
     * @param lane Lane; the lane.
     * @throws GTUException when id is null, or parameters are inconsistent
     */
    public AbstractHeadwayLaneBasedObject(final ObjectType objectType, final String id, final Length overlapFront,
            final Length overlap, final Length overlapRear, final Lane lane) throws GTUException
    {
        super(objectType, id, overlapFront, overlap, overlapRear);
        this.lane = lane;
    }

    /**
     * Construct a new Headway information object, for a non-moving object ahead of us or behind us.
     * @param objectType ObjectType; the perceived object type, can be null if object type unknown.
     * @param id String; the id of the object for comparison purposes, can not be null.
     * @param distance the distance to the other object; if this constructor is used, distance cannot be null.
     * @param length the length of the other object; if this constructor is used, length cannot be null.
     * @param lane Lane; the lane.
     * @throws GTUException when id is null, or parameters are inconsistent
     */
    public AbstractHeadwayLaneBasedObject(final ObjectType objectType, final String id, final Length distance,
            final Length length, final Lane lane) throws GTUException
    {
        super(objectType, id, distance, length);
        this.lane = lane;
    }

    /**
     * Construct a new Headway information object, for a non-moving object ahead of us or behind us.
     * @param objectType ObjectType; the perceived object type, can be null if object type unknown.
     * @param id String; the id of the object for comparison purposes, can not be null.
     * @param distance the distance to the other object; if this constructor is used, distance cannot be null.
     * @param lane Lane; the lane.
     * @throws GTUException when id is null, or parameters are inconsistent
     */
    public AbstractHeadwayLaneBasedObject(final ObjectType objectType, final String id, final Length distance, final Lane lane)
            throws GTUException
    {
        super(objectType, id, distance);
        this.lane = lane;
    }

    /** {@inheritDoc} */
    @Override
    public Lane getLane()
    {
        return this.lane;
    }

}
