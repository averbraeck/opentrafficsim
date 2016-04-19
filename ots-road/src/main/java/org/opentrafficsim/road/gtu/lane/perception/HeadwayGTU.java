package org.opentrafficsim.road.gtu.lane.perception;

import java.util.EnumSet;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;

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
public class HeadwayGTU extends AbstractHeadway
{
    /** */
    private static final long serialVersionUID = 20160410L;

    /** The perceived GTU Type, or null if unknown. */
    private final GTUType gtuType;

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
     * @param speed the (perceived) speed of the other object; can be null if unknown.
     * @param acceleration the (perceived) acceleration of the other object; can be null if unknown.
     * @param gtuStatus the observable characteristics of the GTU.
     * @throws GTUException when id is null, objectType is null, or parameters are inconsistent
     */
    public HeadwayGTU(final String id, final GTUType gtuType, final Length.Rel distance, final Speed speed,
            final Acceleration acceleration, final GTUStatus... gtuStatus) throws GTUException
    {
        super(ObjectType.GTU, id, distance, speed, acceleration);
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
     * @param distance Length.Rel; the distance to the other GTU; if this constructor is used, distance cannot be null.
     * @throws GTUException when id is null, or parameters are inconsistent
     */
    public HeadwayGTU(final String id, final GTUType gtuType, final Length.Rel distance) throws GTUException
    {
        super(ObjectType.GTU, id, distance);
        this.gtuType = gtuType;
    }

    /**
     * Construct a new Headway information object, for a moving GTU parallel with us.
     * @param id the id of the GTU for comparison purposes, can not be null.
     * @param gtuType the perceived GTU Type, or null if unknown.
     * @param overlapFront the front-front distance to the other GTU; if this constructor is used, this value cannot be null.
     * @param overlap the 'center' overlap with the other GTU; if this constructor is used, this value cannot be null.
     * @param overlapRear the rear-rear distance to the other GTU; if this constructor is used, this value cannot be null.
     * @param speed the (perceived) speed of the other GTU; can be null if unknown.
     * @param acceleration the (perceived) acceleration of the other GTU; can be null if unknown.
     * @throws GTUException when id is null, or parameters are inconsistent
     */
    public HeadwayGTU(final String id, final GTUType gtuType, final Length.Rel overlapFront, final Length.Rel overlap,
            final Length.Rel overlapRear, final Speed speed, final Acceleration acceleration) throws GTUException
    {
        super(ObjectType.GTU, id, overlapFront, overlap, overlapRear, speed, acceleration);
        this.gtuType = gtuType;
    }

    /**
     * Construct a new Headway information object, for a non-moving GTU parallel with us.
     * @param id the id of the GTU for comparison purposes, can not be null.
     * @param gtuType the perceived GTU Type, or null if unknown.
     * @param overlapFront the front-front distance to the other GTU; if this constructor is used, this value cannot be null.
     * @param overlap the 'center' overlap with the other GTU; if this constructor is used, this value cannot be null.
     * @param overlapRear the rear-rear distance to the other GTU; if this constructor is used, this value cannot be null.
     * @throws GTUException when id is null, or parameters are inconsistent
     */
    public HeadwayGTU(final String id, final GTUType gtuType, final Length.Rel overlapFront, final Length.Rel overlap,
            final Length.Rel overlapRear) throws GTUException
    {
        super(ObjectType.GTU, id, overlapFront, overlap, overlapRear);
        this.gtuType = gtuType;
    }

    /**
     * @return gtuType
     */
    public final GTUType getGtuType()
    {
        return this.gtuType;
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

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "HeadwayGTU [gtuType=" + this.gtuType + ", gtuStatus=" + this.gtuStatus + "]";
    }

}
