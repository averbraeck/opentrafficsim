package org.opentrafficsim.road.gtu.lane.perception.headway;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;
import org.opentrafficsim.road.network.speed.SpeedLimitTypes;

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
 * positions. See examples in
 * <a href= "http://simulation.tudelft.nl:8085/browse/OTS-113">http://simulation.tudelft.nl:8085/browse/OTS-113</a>.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision: 1368 $, $LastChangedDate: 2015-09-02 00:20:20 +0200 (Wed, 02 Sep 2015) $, by $Author: averbraeck $,
 *          initial version 11 feb. 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public abstract class AbstractHeadwayGTU extends AbstractHeadwayCopy implements HeadwayGTU
{
    /** */
    private static final long serialVersionUID = 20160410L;

    /** The perceived GTU Type, or null if unknown. */
    private final GTUType gtuType;

    /** Whether the GTU is facing the same direction. */
    private final boolean facingSameDirection;

    /** The observable characteristics of the GTU. */
    private final EnumSet<GTUStatus> gtuStatus = EnumSet.noneOf(GTUStatus.class);

    /** Perceived desired speed. */
    private final Speed desiredSpeed;

    /** Perceived width. */
    private final Length width;

    /**
     * Construct a new Headway information object, for a moving GTU ahead of us or behind us.
     * @param id String; the id of the GTU for comparison purposes, can not be null.
     * @param gtuType GTUType; the perceived GTU Type, or null if unknown.
     * @param distance the distance to the other object; if this constructor is used, distance cannot be null.
     * @param facingSameDirection boolean; whether the GTU is facing the same direction.
     * @param length the (perceived) length of the other object; can not be null.
     * @param width the (perceived) width of the other object; can not be null.
     * @param speed the (perceived) speed of the other object; can be null if unknown.
     * @param acceleration the (perceived) acceleration of the other object; can be null if unknown.
     * @param desiredSpeed Speed; desired speed
     * @param gtuStatus GTUStatus...; the observable characteristics of the GTU.
     * @throws GTUException when id is null, objectType is null, or parameters are inconsistent
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public AbstractHeadwayGTU(final String id, final GTUType gtuType, final Length distance, final boolean facingSameDirection,
            final Length length, final Length width, final Speed speed, final Acceleration acceleration,
            final Speed desiredSpeed, final GTUStatus... gtuStatus) throws GTUException
    {
        super(ObjectType.GTU, id, distance, length, speed, acceleration);
        Throw.whenNull(width, "Width may not be null.");
        this.width = width;
        this.facingSameDirection = facingSameDirection;
        this.gtuType = gtuType;
        this.desiredSpeed = desiredSpeed;
        for (GTUStatus status : gtuStatus)
        {
            this.gtuStatus.add(status);
        }
    }

    /**
     * Construct a new Headway information object, for a non-moving GTU ahead of us or behind us.
     * @param id String; the id of the GTU for comparison purposes, can not be null.
     * @param gtuType GTUType; the perceived GTU Type, or null if unknown.
     * @param distance Length; the distance to the other GTU; if this constructor is used, distance cannot be null.
     * @param facingSameDirection boolean; whether the GTU is facing the same direction.
     * @param length the (perceived) length of the other object; can not be null.
     * @param width the (perceived) width of the other object; can not be null.
     * @param desiredSpeed Speed; desired speed
     * @param gtuStatus GTUStatus...; the observable characteristics of the GTU.
     * @throws GTUException when id is null, or parameters are inconsistent
     */
    public AbstractHeadwayGTU(final String id, final GTUType gtuType, final Length distance, final boolean facingSameDirection,
            final Length length, final Length width, final Speed desiredSpeed, final GTUStatus... gtuStatus) throws GTUException
    {
        super(ObjectType.GTU, id, distance, length);
        Throw.whenNull(width, "Width may not be null.");
        this.width = width;
        this.facingSameDirection = facingSameDirection;
        this.gtuType = gtuType;
        this.desiredSpeed = desiredSpeed;
        for (GTUStatus status : gtuStatus)
        {
            this.gtuStatus.add(status);
        }
    }

    /**
     * Construct a new Headway information object, for a moving GTU parallel with us.
     * @param id String; the id of the GTU for comparison purposes, can not be null.
     * @param gtuType GTUType; the perceived GTU Type, or null if unknown.
     * @param overlapFront the front-front distance to the other GTU; if this constructor is used, this value cannot be null.
     * @param overlap the 'center' overlap with the other GTU; if this constructor is used, this value cannot be null.
     * @param overlapRear the rear-rear distance to the other GTU; if this constructor is used, this value cannot be null.
     * @param facingSameDirection boolean; whether the GTU is facing the same direction.
     * @param length the (perceived) length of the other object; can not be null.
     * @param width the (perceived) width of the other object; can not be null.
     * @param speed the (perceived) speed of the other GTU; can be null if unknown.
     * @param acceleration the (perceived) acceleration of the other GTU; can be null if unknown.
     * @param desiredSpeed Speed; desired speed
     * @param gtuStatus GTUStatus...; the observable characteristics of the GTU.
     * @throws GTUException when id is null, or parameters are inconsistent
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public AbstractHeadwayGTU(final String id, final GTUType gtuType, final Length overlapFront, final Length overlap,
            final Length overlapRear, final boolean facingSameDirection, final Length length, final Length width,
            final Speed speed, final Acceleration acceleration, final Speed desiredSpeed, final GTUStatus... gtuStatus)
            throws GTUException
    {
        super(ObjectType.GTU, id, overlapFront, overlap, overlapRear, length, speed, acceleration);
        Throw.whenNull(width, "Width may not be null.");
        this.width = width;
        this.facingSameDirection = facingSameDirection;
        this.gtuType = gtuType;
        this.desiredSpeed = desiredSpeed;
        for (GTUStatus status : gtuStatus)
        {
            this.gtuStatus.add(status);
        }
    }

    /**
     * Construct a new Headway information object, for a non-moving GTU parallel with us.
     * @param id String; the id of the GTU for comparison purposes, can not be null.
     * @param gtuType GTUType; the perceived GTU Type, or null if unknown.
     * @param overlapFront the front-front distance to the other GTU; if this constructor is used, this value cannot be null.
     * @param overlap the 'center' overlap with the other GTU; if this constructor is used, this value cannot be null.
     * @param overlapRear the rear-rear distance to the other GTU; if this constructor is used, this value cannot be null.
     * @param facingSameDirection boolean; whether the GTU is facing the same direction.
     * @param length the (perceived) length of the other object; can not be null.
     * @param width the (perceived) width of the other object; can not be null.
     * @param desiredSpeed Speed; desired speed
     * @param gtuStatus GTUStatus...; the observable characteristics of the GTU.
     * @throws GTUException when id is null, or parameters are inconsistent
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public AbstractHeadwayGTU(final String id, final GTUType gtuType, final Length overlapFront, final Length overlap,
            final Length overlapRear, final boolean facingSameDirection, final Length length, final Length width,
            final Speed desiredSpeed, final GTUStatus... gtuStatus) throws GTUException
    {
        super(ObjectType.GTU, id, overlapFront, overlap, overlapRear, length);
        Throw.whenNull(width, "Width may not be null.");
        this.width = width;
        this.facingSameDirection = facingSameDirection;
        this.gtuType = gtuType;
        this.desiredSpeed = desiredSpeed;
        for (GTUStatus status : gtuStatus)
        {
            this.gtuStatus.add(status);
        }
    }

    /**
     * @return gtuType
     */
    @Override
    public final GTUType getGtuType()
    {
        return this.gtuType;
    }

    /** {@inheritDoc} */
    @Override
    public final Speed getDesiredSpeed()
    {
        return this.desiredSpeed;
    }

    /**
     * @return facingSameDirection
     */
    @Override
    public final boolean isFacingSameDirection()
    {
        return this.facingSameDirection;
    }

    /** @return were the braking lights on? */
    @Override
    public final boolean isBrakingLightsOn()
    {
        return this.gtuStatus.contains(GTUStatus.BRAKING_LIGHTS);
    }

    /** @return was the left turn indicator on? */
    @Override
    public final boolean isLeftTurnIndicatorOn()
    {
        return this.gtuStatus.contains(GTUStatus.LEFT_TURNINDICATOR);
    }

    /** @return was the right turn indicator on? */
    @Override
    public final boolean isRightTurnIndicatorOn()
    {
        return this.gtuStatus.contains(GTUStatus.RIGHT_TURNINDICATOR);
    }

    /** @return were the emergency lights on? */
    @Override
    public final boolean isEmergencyLightsOn()
    {
        return this.gtuStatus.contains(GTUStatus.EMERGENCY_LIGHTS);
    }

    /** @return was the vehicle honking or ringing its bell when being observed for the headway? */
    @Override
    public final boolean isHonking()
    {
        return this.gtuStatus.contains(GTUStatus.HONK);
    }

    /**
     * For subclasses that create a copy of themselves.
     * @return set of gtu status
     */
    protected final GTUStatus[] getGtuStatus()
    {
        return this.gtuStatus.toArray(new GTUStatus[this.gtuStatus.size()]);
    }

    /**
     * Collects GTU statuses from a gtu.
     * @param gtu LaneBasedGTU; gtu
     * @param when Time; time
     * @return GTU statuses
     */
    public static final GTUStatus[] getGTUStatuses(final LaneBasedGTU gtu, final Time when)
    {
        List<GTUStatus> statuses = new ArrayList<>();
        if (gtu.isBrakingLightsOn(when))
        {
            statuses.add(GTUStatus.BRAKING_LIGHTS);
        }
        if (gtu.getTurnIndicatorStatus(when).isHazard())
        {
            statuses.add(GTUStatus.EMERGENCY_LIGHTS);
        }
        else if (gtu.getTurnIndicatorStatus(when).isLeft())
        {
            statuses.add(GTUStatus.LEFT_TURNINDICATOR);
        }
        else if (gtu.getTurnIndicatorStatus(when).isRight())
        {
            statuses.add(GTUStatus.RIGHT_TURNINDICATOR);
        }
        return statuses.toArray(new GTUStatus[statuses.size()]);
    }

    /**
     * Creates speed limit info for given GTU.
     * @param gtu LaneBasedGTU; gtu to the the speed limit info for
     * @return speed limit info for given GTU
     */
    public static SpeedLimitInfo getSpeedLimitInfo(final LaneBasedGTU gtu)
    {
        SpeedLimitInfo sli = new SpeedLimitInfo();
        sli.addSpeedInfo(SpeedLimitTypes.MAX_VEHICLE_SPEED, gtu.getMaximumSpeed());
        try
        {
            sli.addSpeedInfo(SpeedLimitTypes.FIXED_SIGN, gtu.getReferencePosition().getLane().getSpeedLimit(gtu.getGTUType()));
        }
        catch (NetworkException | GTUException exception)
        {
            throw new RuntimeException("Could not obtain speed limit from lane for perception.", exception);
        }
        return sli;
    }

    /** {@inheritDoc} */
    @Override
    public Length getWidth()
    {
        return this.width;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "AbstractHeadwayGTU [gtuType=" + this.gtuType + ", gtuStatus=" + this.gtuStatus + ", getSpeed()="
                + this.getSpeed() + ", getDistance()=" + this.getDistance() + ", getAcceleration()=" + this.getAcceleration()
                + "]";
    }

}
