package org.opentrafficsim.road.gtu.lane.perception.headway;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.lane.perception.GtuTypeAssumptions;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

/**
 * Container for a reference to information about a (lane based) GTU and a headway. The Headway can store information about GTUs
 * or objects ahead of the reference GTU, behind the reference GTU, or (partially) parallel to the reference GTU. In addition to
 * the (perceived) headway, several other pieces of information can be stored, such as (perceived) speed, (perceived)
 * acceleration, (perceived) turn indicators, and (perceived) braking lights. <br>
 * This particular version returns behavioral information about the observed GTU objects based on their type.<br>
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
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class HeadwayGtuType extends AbstractHeadwayGtu
{
    /** */
    private static final long serialVersionUID = 20160527L;

    /** a pointer to centrally kept or GTU specific GTUTypeAssumptions. */
    private final GtuTypeAssumptions gtuTypeAssumptions;

    /**
     * Construct a new Headway information object, for a moving GTU ahead of us or behind us.
     * @param id the id of the GTU for comparison purposes, can not be null.
     * @param gtuType the perceived GTU Type, or null if unknown.
     * @param gtuTypeAssumptions centrally kept or GTU specific GTUTypeAssumptions
     * @param distance if this constructor is used, distance cannot be null.
     * @param length if this constructor is used, length cannot be null.
     * @param width the (perceived) width of the other object; can not be null.
     * @param speed the (perceived) speed of the other object; can be null if unknown.
     * @param acceleration the (perceived) acceleration of the other object; can be null if unknown.
     * @param desiredSpeed desired speed
     * @param deviation lateral deviation
     * @param laneChangeDirection lane change direction
     * @param gtuStatus the observable characteristics of the GTU.
     * @throws GtuException when id is null, objectType is null, or parameters are inconsistent
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public HeadwayGtuType(final String id, final GtuType gtuType, final GtuTypeAssumptions gtuTypeAssumptions,
            final Length distance, final Length length, final Length width, final Speed speed, final Acceleration acceleration,
            final Speed desiredSpeed, final Length deviation, final LateralDirectionality laneChangeDirection,
            final GtuStatus... gtuStatus) throws GtuException
    {
        super(id, gtuType, distance, true, length, width, speed, acceleration, desiredSpeed, deviation, laneChangeDirection,
                gtuStatus);
        this.gtuTypeAssumptions = gtuTypeAssumptions;
    }

    /**
     * Construct a new Headway information object, for a non-moving GTU ahead of us or behind us.
     * @param id the id of the GTU for comparison purposes, can not be null.
     * @param gtuType the perceived GTU Type, or null if unknown.
     * @param gtuTypeAssumptions centrally kept or GTU specific GTUTypeAssumptions
     * @param distance the distance to the other Gtu; if this constructor is used, distance cannot be null.
     * @param length if this constructor is used, length cannot be null.
     * @param width the (perceived) width of the other object; can not be null.
     * @param desiredSpeed desired speed
     * @param deviation lateral deviation
     * @param laneChangeDirection lane change direction
     * @throws GtuException when id is null, or parameters are inconsistent
     */
    public HeadwayGtuType(final String id, final GtuType gtuType, final GtuTypeAssumptions gtuTypeAssumptions,
            final Length distance, final Length length, final Length width, final Speed desiredSpeed, final Length deviation,
            final LateralDirectionality laneChangeDirection) throws GtuException
    {
        super(id, gtuType, distance, true, length, width, desiredSpeed, deviation, laneChangeDirection);
        this.gtuTypeAssumptions = gtuTypeAssumptions;
    }

    /**
     * Construct a new Headway information object, for a moving GTU parallel with us.
     * @param id the id of the GTU for comparison purposes, can not be null.
     * @param gtuType the perceived GTU Type, or null if unknown.
     * @param gtuTypeAssumptions centrally kept or GTU specific GTUTypeAssumptions
     * @param overlapFront the front-front distance to the other Gtu; if this constructor is used, this value cannot be null.
     * @param overlap the 'center' overlap with the other Gtu; if this constructor is used, this value cannot be null.
     * @param overlapRear the rear-rear distance to the other Gtu; if this constructor is used, this value cannot be null.
     * @param length if this constructor is used, length cannot be null.
     * @param width the (perceived) width of the other object; can not be null.
     * @param speed the (perceived) speed of the other Gtu; can be null if unknown.
     * @param acceleration the (perceived) acceleration of the other Gtu; can be null if unknown.
     * @param desiredSpeed desired speed
     * @param deviation lateral deviation
     * @param laneChangeDirection lane change direction
     * @throws GtuException when id is null, or parameters are inconsistent
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public HeadwayGtuType(final String id, final GtuType gtuType, final GtuTypeAssumptions gtuTypeAssumptions,
            final Length overlapFront, final Length overlap, final Length overlapRear, final Length length, final Length width,
            final Speed speed, final Acceleration acceleration, final Speed desiredSpeed, final Length deviation,
            final LateralDirectionality laneChangeDirection) throws GtuException
    {
        super(id, gtuType, overlapFront, overlap, overlapRear, true, length, width, speed, acceleration, desiredSpeed,
                deviation, laneChangeDirection);
        this.gtuTypeAssumptions = gtuTypeAssumptions;
    }

    /**
     * Construct a new Headway information object, for a non-moving GTU parallel with us.
     * @param id the id of the GTU for comparison purposes, can not be null.
     * @param gtuType the perceived GTU Type, or null if unknown.
     * @param gtuTypeAssumptions centrally kept or GTU specific GTUTypeAssumptions
     * @param overlapFront the front-front distance to the other Gtu; if this constructor is used, this value cannot be null.
     * @param overlap the 'center' overlap with the other Gtu; if this constructor is used, this value cannot be null.
     * @param overlapRear the rear-rear distance to the other Gtu; if this constructor is used, this value cannot be null.
     * @param length if this constructor is used, length cannot be null.
     * @param width the (perceived) width of the other object; can not be null.
     * @param desiredSpeed desired speed
     * @param deviation lateral deviation
     * @param laneChangeDirection lane change direction
     * @throws GtuException when id is null, or parameters are inconsistent
     */
    public HeadwayGtuType(final String id, final GtuType gtuType, final GtuTypeAssumptions gtuTypeAssumptions,
            final Length overlapFront, final Length overlap, final Length overlapRear, final Length length, final Length width,
            final Speed desiredSpeed, final Length deviation, final LateralDirectionality laneChangeDirection)
            throws GtuException
    {
        super(id, gtuType, overlapFront, overlap, overlapRear, true, length, width, desiredSpeed, deviation,
                laneChangeDirection);
        this.gtuTypeAssumptions = gtuTypeAssumptions;
    }

    @Override
    public final CarFollowingModel getCarFollowingModel()
    {
        return this.gtuTypeAssumptions.getCarFollowingModel(getGtuType());
    }

    @Override
    public final Parameters getParameters()
    {
        return this.gtuTypeAssumptions.getParameters(getGtuType());
    }

    @Override
    public final SpeedLimitInfo getSpeedLimitInfo()
    {
        return null; // TODO create SpeedLimitInfo on the basis of this.gtuTypeAssumptions.getLaneTypeMaxSpeed(...)
    }

    @Override
    public final Route getRoute()
    {
        return null;
    }

    @Override
    public final AbstractHeadwayGtu moved(final Length headway, final Speed speed, final Acceleration acceleration)
    {
        try
        {
            LateralDirectionality lcDirection = isChangingLeft() ? LateralDirectionality.LEFT
                    : (isChangingRight() ? LateralDirectionality.RIGHT : LateralDirectionality.NONE);
            return new HeadwayGtuType(getId(), getGtuType(), this.gtuTypeAssumptions, headway, getLength(), getWidth(), speed,
                    acceleration, getDesiredSpeed(), getDeviation(), lcDirection, getGtuStatus());
        }
        catch (GtuException exception)
        {
            // input should be consistent
            throw new RuntimeException("Exception while copying Headway GTU.", exception);
        }
    }

    @Override
    public final String toString()
    {
        return "HeadwayGtuType [gtuTypeAssumptions=" + this.gtuTypeAssumptions + "]";
    }

}
