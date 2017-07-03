package org.opentrafficsim.road.gtu.lane.perception.headway;

import java.util.EnumSet;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;
import org.opentrafficsim.road.network.speed.SpeedLimitTypes;

/**
 * Container for a reference to information about a (lane based) GTU and a headway. The Headway can store information about GTUs
 * or objects ahead of the reference GTU, behind the reference GTU, or (partially) parallel to the reference GTU. In addition to
 * the (perceived) headway, several other pieces of information can be stored, such as (perceived) speed, (perceived)
 * acceleration, (perceived) turn indicators, and (perceived) braking lights. <br>
 * This particular version returns behavioral information about the observed GTU objects based on their real state.<br>
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
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version May 27, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class HeadwayGTUReal extends AbstractHeadway implements HeadwayGTU
{
    /** */
    private static final long serialVersionUID = 20170324L;

    /** Stored speed limit info of the observed GTU. */
    private final SpeedLimitInfo speedLimitInfo;

    /** Wrapped GTU. */
    private final LaneBasedGTU gtu;

    /** Whether the GTU is facing the same direction. */
    private final boolean facingSameDirection;

    /**
     * Construct a new Headway information object, for a GTU ahead of us or behind us.
     * @param gtu the observed GTU, can not be null.
     * @param distance the distance to the other object; if this constructor is used, distance cannot be null.
     * @param facingSameDirection whether the GTU is facing the same direction.
     * @throws GTUException when id is null, objectType is null, or parameters are inconsistent
     */
    public HeadwayGTUReal(final LaneBasedGTU gtu, final Length distance, final boolean facingSameDirection) throws GTUException
    {
        super(distance);
        this.gtu = gtu;
        this.facingSameDirection = facingSameDirection;
        this.speedLimitInfo = getSpeedLimitInfo(gtu);
    }

    /**
     * Construct a new Headway information object, for a GTU parallel with us.
     * @param gtu the observed GTU, can not be null.
     * @param overlapFront the front-front distance to the other GTU; if this constructor is used, this value cannot be null.
     * @param overlap the 'center' overlap with the other GTU; if this constructor is used, this value cannot be null.
     * @param overlapRear the rear-rear distance to the other GTU; if this constructor is used, this value cannot be null.
     * @param facingSameDirection whether the GTU is facing the same direction.
     * @throws GTUException when id is null, or parameters are inconsistent
     */
    public HeadwayGTUReal(final LaneBasedGTU gtu, final Length overlapFront, final Length overlap, final Length overlapRear,
            final boolean facingSameDirection) throws GTUException
    {
        super(overlapFront, overlap, overlapRear);
        this.gtu = gtu;
        this.facingSameDirection = facingSameDirection;
        this.speedLimitInfo = getSpeedLimitInfo(gtu);
    }

    /**
     * Creates speed limit prospect for given GTU.
     * @param wrappedGtu gtu to the the speed limit prospect for
     * @return speed limit prospect for given GTU
     */
    // TODO this is a simple fix
    private SpeedLimitInfo getSpeedLimitInfo(final LaneBasedGTU wrappedGtu)
    {
        SpeedLimitInfo sli = new SpeedLimitInfo();
        sli.addSpeedInfo(SpeedLimitTypes.MAX_VEHICLE_SPEED, wrappedGtu.getMaximumSpeed());
        try
        {
            sli.addSpeedInfo(SpeedLimitTypes.FIXED_SIGN,
                    wrappedGtu.getReferencePosition().getLane().getSpeedLimit(wrappedGtu.getGTUType()));
        }
        catch (NetworkException | GTUException exception)
        {
            throw new RuntimeException("Could not obtain speed limit from lane for perception.", exception);
        }
        return sli;
    }

    /** {@inheritDoc} */
    @Override
    public final CarFollowingModel getCarFollowingModel()
    {
        return this.gtu.getTacticalPlanner().getCarFollowingModel();
    }

    /** {@inheritDoc} */
    @Override
    public final Parameters getParameters()
    {
        return this.gtu.getParameters();
    }

    /** {@inheritDoc} */
    @Override
    public final SpeedLimitInfo getSpeedLimitInfo()
    {
        return this.speedLimitInfo;
    }

    /** {@inheritDoc} */
    @Override
    public final Route getRoute()
    {
        return this.gtu.getStrategicalPlanner().getRoute();
    }

    /**
     * {@inheritDoc} <br>
     * <br>
     * <b>Note: when moving a {@code HeadwayGTURealDirect}, only headway, speed and acceleration may be considered to be delayed
     * and anticipated. Other information is taken from the actual GTU at the time {@code moved()} is called.</b>
     */
    @Override
    public HeadwayGTU moved(final Length headway, final Speed speed, final Acceleration acceleration)
    {
        try
        {
            return new HeadwayGTURealCopy(getId(), getGtuType(), headway, getLength(), speed, acceleration,
                    getCarFollowingModel(), getParameters(), getSpeedLimitInfo(), getRoute(), getGtuStatus());
        }
        catch (GTUException exception)
        {
            // input should be consistent
            throw new RuntimeException("Exception while copying Headway GTU.", exception);
        }
    }

    /**
     * Returns an array with GTU status.
     * @return array with GTU status
     */
    private GTUStatus[] getGtuStatus()
    {
        EnumSet<GTUStatus> gtuStatus = EnumSet.noneOf(GTUStatus.class);
        if (isLeftTurnIndicatorOn())
        {
            gtuStatus.add(GTUStatus.LEFT_TURNINDICATOR);
        }
        if (isRightTurnIndicatorOn())
        {
            gtuStatus.add(GTUStatus.RIGHT_TURNINDICATOR);
        }
        if (isBrakingLightsOn())
        {
            gtuStatus.add(GTUStatus.BRAKING_LIGHTS);
        }
        if (isEmergencyLightsOn())
        {
            gtuStatus.add(GTUStatus.EMERGENCY_LIGHTS);
        }
        if (isHonking())
        {
            gtuStatus.add(GTUStatus.HONK);
        }
        return gtuStatus.toArray(new GTUStatus[gtuStatus.size()]);
    }

    /** {@inheritDoc} */
    @Override
    public String getId()
    {
        return this.gtu.getId();
    }

    /** {@inheritDoc} */
    @Override
    public Length getLength()
    {
        return this.gtu.getLength();
    }

    /** {@inheritDoc} */
    @Override
    public Speed getSpeed()
    {
        return this.gtu.getSpeed();
    }

    /** {@inheritDoc} */
    @Override
    public ObjectType getObjectType()
    {
        return ObjectType.GTU;
    }

    /** {@inheritDoc} */
    @Override
    public Acceleration getAcceleration()
    {
        return this.gtu.getAcceleration();
    }

    /** {@inheritDoc} */
    @Override
    public GTUType getGtuType()
    {
        return this.gtu.getGTUType();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isFacingSameDirection()
    {
        return this.facingSameDirection;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isBrakingLightsOn()
    {
        // TODO
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isLeftTurnIndicatorOn()
    {
        return this.gtu.getTurnIndicatorStatus().isLeft();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isRightTurnIndicatorOn()
    {
        return this.gtu.getTurnIndicatorStatus().isRight();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEmergencyLightsOn()
    {
        return this.gtu.getTurnIndicatorStatus().isHazard();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isHonking()
    {
        // TODO
        return false;
    }

}
