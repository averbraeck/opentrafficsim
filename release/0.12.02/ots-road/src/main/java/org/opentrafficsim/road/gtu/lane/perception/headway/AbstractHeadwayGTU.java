package org.opentrafficsim.road.gtu.lane.perception.headway;

import java.util.EnumSet;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

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
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision: 1368 $, $LastChangedDate: 2015-09-02 00:20:20 +0200 (Wed, 02 Sep 2015) $, by $Author: averbraeck $,
 *          initial version 11 feb. 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public abstract class AbstractHeadwayGTU extends AbstractHeadway
{
    /** */
    private static final long serialVersionUID = 20160410L;

    /** The perceived GTU Type, or null if unknown. */
    private final GTUType gtuType;

    /** Whether the GTU is facing the same direction. */
    private final boolean facingSameDirection;

    /** Observable characteristics of a GTU. */
    public enum GTUStatus
    {
        /** Braking lights are on when observing the headway. */
        BRAKING_LIGHTS,

        /** Left turn indicator was on when observing the headway. */
        LEFT_TURNINDICATOR,

        /** Right turn indicator was on when observing the headway. */
        RIGHT_TURNINDICATOR,

        /** Alarm lights are on. */
        EMERGENCY_LIGHTS,

        /** GTU was honking (car) or ringing a bell (cyclist) when observing the headway. */
        HONK;
    }

    /** The observable characteristics of the GTU. */
    private final EnumSet<GTUStatus> gtuStatus = EnumSet.noneOf(GTUStatus.class);

    /**
     * Construct a new Headway information object, for a moving GTU ahead of us or behind us.
     * @param id the id of the GTU for comparison purposes, can not be null.
     * @param gtuType the perceived GTU Type, or null if unknown.
     * @param distance the distance to the other object; if this constructor is used, distance cannot be null.
     * @param facingSameDirection whether the GTU is facing the same direction.
     * @param length the (perceived) length of the other object; can not be null.
     * @param speed the (perceived) speed of the other object; can be null if unknown.
     * @param acceleration the (perceived) acceleration of the other object; can be null if unknown.
     * @param gtuStatus the observable characteristics of the GTU.
     * @throws GTUException when id is null, objectType is null, or parameters are inconsistent
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public AbstractHeadwayGTU(final String id, final GTUType gtuType, final Length distance, final boolean facingSameDirection,
            final Length length, final Speed speed, final Acceleration acceleration, final GTUStatus... gtuStatus)
            throws GTUException
    {
        super(ObjectType.GTU, id, distance, length, speed, acceleration);
        this.facingSameDirection = facingSameDirection;
        this.gtuType = gtuType;
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
     * @param facingSameDirection whether the GTU is facing the same direction.
     * @param length the (perceived) length of the other object; can not be null.
     * @throws GTUException when id is null, or parameters are inconsistent
     */
    public AbstractHeadwayGTU(final String id, final GTUType gtuType, final Length distance, final boolean facingSameDirection,
            final Length length) throws GTUException
    {
        super(ObjectType.GTU, id, distance, length);
        this.facingSameDirection = facingSameDirection;
        this.gtuType = gtuType;
    }

    /**
     * Construct a new Headway information object, for a moving GTU parallel with us.
     * @param id the id of the GTU for comparison purposes, can not be null.
     * @param gtuType the perceived GTU Type, or null if unknown.
     * @param overlapFront the front-front distance to the other GTU; if this constructor is used, this value cannot be null.
     * @param overlap the 'center' overlap with the other GTU; if this constructor is used, this value cannot be null.
     * @param overlapRear the rear-rear distance to the other GTU; if this constructor is used, this value cannot be null.
     * @param facingSameDirection whether the GTU is facing the same direction.
     * @param length the (perceived) length of the other object; can not be null.
     * @param speed the (perceived) speed of the other GTU; can be null if unknown.
     * @param acceleration the (perceived) acceleration of the other GTU; can be null if unknown.
     * @throws GTUException when id is null, or parameters are inconsistent
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public AbstractHeadwayGTU(final String id, final GTUType gtuType, final Length overlapFront, final Length overlap,
            final Length overlapRear, final boolean facingSameDirection, final Length length, final Speed speed,
            final Acceleration acceleration) throws GTUException
    {
        super(ObjectType.GTU, id, overlapFront, overlap, overlapRear, length, speed, acceleration);
        this.facingSameDirection = facingSameDirection;
        this.gtuType = gtuType;
    }

    /**
     * Construct a new Headway information object, for a non-moving GTU parallel with us.
     * @param id the id of the GTU for comparison purposes, can not be null.
     * @param gtuType the perceived GTU Type, or null if unknown.
     * @param overlapFront the front-front distance to the other GTU; if this constructor is used, this value cannot be null.
     * @param overlap the 'center' overlap with the other GTU; if this constructor is used, this value cannot be null.
     * @param overlapRear the rear-rear distance to the other GTU; if this constructor is used, this value cannot be null.
     * @param facingSameDirection whether the GTU is facing the same direction.
     * @param length the (perceived) length of the other object; can not be null.
     * @throws GTUException when id is null, or parameters are inconsistent
     */
    public AbstractHeadwayGTU(final String id, final GTUType gtuType, final Length overlapFront, final Length overlap,
            final Length overlapRear, final boolean facingSameDirection, final Length length) throws GTUException
    {
        super(ObjectType.GTU, id, overlapFront, overlap, overlapRear, length);
        this.facingSameDirection = facingSameDirection;
        this.gtuType = gtuType;
    }

    /**
     * @return gtuType
     */
    public final GTUType getGtuType()
    {
        return this.gtuType;
    }

    /**
     * @return facingSameDirection
     */
    public final boolean isFacingSameDirection()
    {
        return this.facingSameDirection;
    }

    /** @return were the braking lights on? */
    public final boolean isBrakingLightsOn()
    {
        return this.gtuStatus.contains(GTUStatus.BRAKING_LIGHTS);
    }

    /** @return was the left turn indicator on? */
    public final boolean isLeftTurnIndicatorOn()
    {
        return this.gtuStatus.contains(GTUStatus.LEFT_TURNINDICATOR);
    }

    /** @return was the right turn indicator on? */
    public final boolean isRightTurnIndicatorOn()
    {
        return this.gtuStatus.contains(GTUStatus.RIGHT_TURNINDICATOR);
    }

    /** @return were the emergency lights on? */
    public final boolean isEmergencyLightsOn()
    {
        return this.gtuStatus.contains(GTUStatus.EMERGENCY_LIGHTS);
    }

    /** @return was the vehicle honking or ringing its bell when being observed for the headway? */
    public final boolean isHonking()
    {
        return this.gtuStatus.contains(GTUStatus.HONK);
    }

    /**
     * Many models that observe a GTU need to predict the imminent behavior of that GTU. Having a car following model of the
     * observed GTU can help with that. The car following model that is returned can be on a continuum between the actual car
     * following model of the observed GTU and the own car following model of the observing GTU, not making any assumptions
     * about the observed GTU. When successive observations of the GTU take place, parameters about its behavior can be
     * estimated more accurately. Another interesting easy-to-implement solution is to return a car following model per GTU
     * type, where the following model of a truck can differ from that of a car.
     * @return a car following model that represents the expected behavior of the observed GTU
     */
    public abstract CarFollowingModel getCarFollowingModel();

    /**
     * Many models that observe a GTU need to predict the imminent behavior of that GTU. Having an estimate of the behavioral
     * characteristics of the observed GTU can help with that. The behavioral characteristics that are returned can be on a
     * continuum between the actual behavioral characteristics of the observed GTU and the own behavioral characteristics of the
     * observing GTU, not making any assumptions about the observed GTU. When successive observations of the GTU take place,
     * parameters about its behavior can be estimated more accurately. Another interesting easy-to-implement solution is to
     * return a set of behavioral characteristics per GTU type, where the behavioral characteristics of a truck can differ from
     * that of a car.
     * @return the behavioral characteristics that represent the expected behavior of the observed GTU
     */
    public abstract BehavioralCharacteristics getBehavioralCharacteristics();

    /**
     * Many models that observe a GTU need to predict the imminent behavior of that GTU. Having a model of the speed info
     * profile for the observed GTU can help with predicting its future behavior. The speed limit info that is returned can be
     * on a continuum between the actual speed limit model of the observed GTU and the own speed limit model of the observing
     * GTU, not making any assumptions about the observed GTU. When successive observations of the GTU take place, parameters
     * about its behavior, such as the maximum speed it accepts, can be estimated more accurately. Another interesting
     * easy-to-implement solution is to return a speed limit info object per GTU type, where the returned information of a truck
     * -- with a maximum allowed speed on 80 km/h -- can differ from that of a car -- which can have a maximum allowed speed of
     * 100 km/h on the same road.
     * @return a speed limit model that helps in determining the expected behavior of the observed GTU
     */
    public abstract SpeedLimitInfo getSpeedLimitInfo();

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "HeadwayGTU [gtuType=" + this.gtuType + ", gtuStatus=" + this.gtuStatus + "]";
    }

}
