package org.opentrafficsim.road.gtu.lane.perception.headway;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

/**
 * Container for a reference to information about a (lane based) GTU and a headway. The Headway can store information about GTUs
 * or objects ahead of the reference GTU, behind the reference GTU, or (partially) parallel to the reference GTU. In addition to
 * the (perceived) headway, several other pieces of information can be stored, such as (perceived) speed, (perceived)
 * acceleration, (perceived) turn indicators, and (perceived) braking lights. <br>
 * This particular version has only limited behavioral information about the observed GTU.<br>
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
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class HeadwayGtuSimple extends AbstractHeadwayGtu
{
    /** */
    private static final long serialVersionUID = 20160527L;

    /**
     * Construct a new Headway information object, for a moving GTU ahead of us or behind us.
     * @param id String; the id of the GTU for comparison purposes, can not be null.
     * @param gtuType GtuType; the perceived GTU Type, or null if unknown.
     * @param distance the distance to the other object; if this constructor is used, distance cannot be null.
     * @param length the length of the other object; if this constructor is used, length cannot be null.
     * @param width the (perceived) width of the other object; can not be null.
     * @param speed the (perceived) speed of the other object; can be null if unknown.
     * @param acceleration the (perceived) acceleration of the other object; can be null if unknown.
     * @param desiredSpeed Speed; desired speed
     * @param gtuStatus GtuStatus...; the observable characteristics of the GTU.
     * @throws GtuException when id is null, objectType is null, or parameters are inconsistent
     */
    public HeadwayGtuSimple(final String id, final GtuType gtuType, final Length distance, final Length length,
            final Length width, final Speed speed, final Acceleration acceleration, final Speed desiredSpeed,
            final GtuStatus... gtuStatus) throws GtuException
    {
        super(id, gtuType, distance, true, length, width, speed, acceleration, desiredSpeed, gtuStatus);
    }

    /**
     * Construct a new Headway information object, for a non-moving GTU ahead of us or behind us.
     * @param id String; the id of the GTU for comparison purposes, can not be null.
     * @param gtuType GtuType; the perceived GTU Type, or null if unknown.
     * @param distance Length; the distance to the other Gtu; if this constructor is used, distance cannot be null.
     * @param length the length of the other object; if this constructor is used, length cannot be null.
     * @param width the (perceived) width of the other object; can not be null.
     * @param desiredSpeed Speed; desired speed
     * @param gtuStatus GtuStatus...; the observable characteristics of the GTU.
     * @throws GtuException when id is null, or parameters are inconsistent
     */
    public HeadwayGtuSimple(final String id, final GtuType gtuType, final Length distance, final Length length,
            final Length width, final Speed desiredSpeed, final GtuStatus... gtuStatus) throws GtuException
    {
        super(id, gtuType, distance, true, length, width, desiredSpeed, gtuStatus);
    }

    /**
     * Construct a new Headway information object, for a moving GTU parallel with us.
     * @param id String; the id of the GTU for comparison purposes, can not be null.
     * @param gtuType GtuType; the perceived GTU Type, or null if unknown.
     * @param overlapFront the front-front distance to the other Gtu; if this constructor is used, this value cannot be null.
     * @param overlap the 'center' overlap with the other Gtu; if this constructor is used, this value cannot be null.
     * @param overlapRear the rear-rear distance to the other Gtu; if this constructor is used, this value cannot be null.
     * @param length the length of the other object; if this constructor is used, length cannot be null.
     * @param width the (perceived) width of the other object; can not be null.
     * @param speed the (perceived) speed of the other Gtu; can be null if unknown.
     * @param acceleration the (perceived) acceleration of the other Gtu; can be null if unknown.
     * @param desiredSpeed Speed; desired speed
     * @param gtuStatus GtuStatus...; the observable characteristics of the GTU.
     * @throws GtuException when id is null, or parameters are inconsistent
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public HeadwayGtuSimple(final String id, final GtuType gtuType, final Length overlapFront, final Length overlap,
            final Length overlapRear, final Length length, final Length width, final Speed speed,
            final Acceleration acceleration, final Speed desiredSpeed, final GtuStatus... gtuStatus) throws GtuException
    {
        super(id, gtuType, overlapFront, overlap, overlapRear, true, length, width, speed, acceleration, desiredSpeed,
                gtuStatus);
    }

    /**
     * Construct a new Headway information object, for a non-moving GTU parallel with us.
     * @param id String; the id of the GTU for comparison purposes, can not be null.
     * @param gtuType GtuType; the perceived GTU Type, or null if unknown.
     * @param overlapFront the front-front distance to the other Gtu; if this constructor is used, this value cannot be null.
     * @param overlap the 'center' overlap with the other Gtu; if this constructor is used, this value cannot be null.
     * @param overlapRear the rear-rear distance to the other Gtu; if this constructor is used, this value cannot be null.
     * @param length the length of the other object; if this constructor is used, length cannot be null.
     * @param width the (perceived) width of the other object; can not be null.
     * @param desiredSpeed Speed; desired speed
     * @param gtuStatus GtuStatus...; the observable characteristics of the GTU.
     * @throws GtuException when id is null, or parameters are inconsistent
     */
    public HeadwayGtuSimple(final String id, final GtuType gtuType, final Length overlapFront, final Length overlap,
            final Length overlapRear, final Length length, final Length width, final Speed desiredSpeed,
            final GtuStatus... gtuStatus) throws GtuException
    {
        super(id, gtuType, overlapFront, overlap, overlapRear, true, length, width, desiredSpeed, gtuStatus);
    }

    /** {@inheritDoc} */
    @Override
    public final CarFollowingModel getCarFollowingModel()
    {
        throw new UnsupportedOperationException("HeadwayGtuSimple does not support the getCarFollowingModel() method.");
    }

    /** {@inheritDoc} */
    @Override
    public final Parameters getParameters()
    {
        throw new UnsupportedOperationException("HeadwayGtuSimple does not support the getParameters() method.");
    }

    /** {@inheritDoc} */
    @Override
    public final SpeedLimitInfo getSpeedLimitInfo()
    {
        throw new UnsupportedOperationException("HeadwayGtuSimple does not support the getSpeedLimitInfo() method.");
    }

    /** {@inheritDoc} */
    @Override
    public final Route getRoute()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final AbstractHeadwayGtu moved(final Length headway, final Speed speed, final Acceleration acceleration)
    {
        try
        {
            return new HeadwayGtuSimple(getId(), getGtuType(), headway, getLength(), getWidth(), speed, acceleration,
                    getDesiredSpeed(), getGtuStatus());
        }
        catch (GtuException exception)
        {
            // input should be consistent
            throw new RuntimeException("Exception while copying Headway GTU.", exception);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "HeadwayGtuSimple [length=" + super.getLength() + "]";
    }

}
