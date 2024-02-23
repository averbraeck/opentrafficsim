package org.opentrafficsim.road.gtu.lane.perception.headway;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterSet;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

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
 * positions. 
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class HeadwayGtuRealCopy extends AbstractHeadwayGtu
{
    /** */
    private static final long serialVersionUID = 20160527L;

    /** stored car following model of the observed GTU. */
    private final CarFollowingModel carFollowingModel;

    /** stored parameters of the observed GTU. */
    private final Parameters parameters;

    /** stored speed limit info of the observed GTU. */
    private final SpeedLimitInfo speedLimitInfo;

    /** stored route of the observed GTU. */
    private final Route route;

    /**
     * Protected constructor for moved copies or subclasses.
     * @param id String; id
     * @param gtuType GtuType; GTU type
     * @param distance Length; distance
     * @param length Length; length
     * @param width Length; width
     * @param speed Speed; speed
     * @param acceleration Acceleration; acceleration
     * @param carFollowingModel CarFollowingModel; car-following model
     * @param parameters Parameters; parameters
     * @param speedLimitInfo SpeedLimitInfo; speed limit info
     * @param route Route; route
     * @param desiredSpeed Speed; desired speed
     * @param gtuStatus GtuStatus...; gtu status
     * @throws GtuException when id is null, objectType is null, or parameters are inconsistent
     */
    @SuppressWarnings("checkstyle:parameternumber")
    HeadwayGtuRealCopy(final String id, final GtuType gtuType, final Length distance, final Length length, final Length width,
            final Speed speed, final Acceleration acceleration, final CarFollowingModel carFollowingModel,
            final Parameters parameters, final SpeedLimitInfo speedLimitInfo, final Route route, final Speed desiredSpeed,
            final GtuStatus... gtuStatus) throws GtuException
    {
        super(id, gtuType, distance, true, length, width, speed, acceleration, desiredSpeed, gtuStatus);
        this.carFollowingModel = carFollowingModel;
        this.parameters = parameters;
        this.speedLimitInfo = speedLimitInfo;
        this.route = route;
    }

    /**
     * Protected constructor for moved copies or subclasses.
     * @param id String; id
     * @param gtuType GtuType; GTU type
     * @param overlapFront the front-front distance to the other Gtu; if this constructor is used, this value cannot be null.
     * @param overlap the 'center' overlap with the other Gtu; if this constructor is used, this value cannot be null.
     * @param overlapRear the rear-rear distance to the other Gtu; if this constructor is used, this value cannot be null.
     * @param length Length; length
     * @param width Length; width
     * @param speed Speed; speed
     * @param acceleration Acceleration; acceleration
     * @param carFollowingModel CarFollowingModel; car-following model
     * @param parameters Parameters; parameters
     * @param speedLimitInfo SpeedLimitInfo; speed limit info
     * @param route Route; route
     * @param desiredSpeed Speed; desired speed
     * @param gtuStatus GtuStatus...; gtu status
     * @throws GtuException when id is null, objectType is null, or parameters are inconsistent
     */
    @SuppressWarnings("checkstyle:parameternumber")
    HeadwayGtuRealCopy(final String id, final GtuType gtuType, final Length overlapFront, final Length overlap,
            final Length overlapRear, final Length length, final Length width, final Speed speed,
            final Acceleration acceleration, final CarFollowingModel carFollowingModel, final Parameters parameters,
            final SpeedLimitInfo speedLimitInfo, final Route route, final Speed desiredSpeed, final GtuStatus... gtuStatus)
            throws GtuException
    {
        super(id, gtuType, overlapFront, overlap, overlapRear, true, length, width, speed, acceleration, desiredSpeed,
                gtuStatus);
        this.carFollowingModel = carFollowingModel;
        this.parameters = parameters;
        this.speedLimitInfo = speedLimitInfo;
        this.route = route;
    }

    /**
     * Construct a new Headway information object, for a GTU ahead of us or behind us.
     * @param gtu LaneBasedGtu; the observed GTU, can not be null.
     * @param distance the distance to the other object; if this constructor is used, distance cannot be null.
     * @throws GtuException when id is null, objectType is null, or parameters are inconsistent
     */
    public HeadwayGtuRealCopy(final LaneBasedGtu gtu, final Length distance) throws GtuException
    {
        super(gtu.getId(), gtu.getType(), distance, true, gtu.getLength(), gtu.getWidth(), gtu.getSpeed(),
                gtu.getAcceleration(), gtu.getDesiredSpeed(), getGtuStatuses(gtu, gtu.getSimulator().getSimulatorAbsTime()));
        this.carFollowingModel = gtu.getTacticalPlanner().getCarFollowingModel();
        this.parameters = new ParameterSet(gtu.getParameters());
        this.speedLimitInfo = getSpeedLimitInfo(gtu);
        this.route = gtu.getStrategicalPlanner().getRoute();
    }

    /**
     * Construct a new Headway information object, for a GTU parallel with us.
     * @param gtu LaneBasedGtu; the observed GTU, can not be null.
     * @param overlapFront the front-front distance to the other Gtu; if this constructor is used, this value cannot be null.
     * @param overlap the 'center' overlap with the other Gtu; if this constructor is used, this value cannot be null.
     * @param overlapRear the rear-rear distance to the other Gtu; if this constructor is used, this value cannot be null.
     * @throws GtuException when id is null, or parameters are inconsistent
     */
    public HeadwayGtuRealCopy(final LaneBasedGtu gtu, final Length overlapFront, final Length overlap, final Length overlapRear)
            throws GtuException
    {
        super(gtu.getId(), gtu.getType(), overlapFront, overlap, overlapRear, true, gtu.getLength(), gtu.getWidth(),
                gtu.getSpeed(), gtu.getAcceleration(), gtu.getDesiredSpeed(),
                getGtuStatuses(gtu, gtu.getSimulator().getSimulatorAbsTime()));
        this.carFollowingModel = gtu.getTacticalPlanner().getCarFollowingModel();
        this.parameters = new ParameterSet(gtu.getParameters());
        this.speedLimitInfo = getSpeedLimitInfo(gtu);
        this.route = gtu.getStrategicalPlanner().getRoute();
    }

    /** {@inheritDoc} */
    @Override
    public final CarFollowingModel getCarFollowingModel()
    {
        return this.carFollowingModel;
    }

    /** {@inheritDoc} */
    @Override
    public final Parameters getParameters()
    {
        return this.parameters;
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
        return this.route;
    }

    /** {@inheritDoc} */
    @Override
    public final AbstractHeadwayGtu moved(final Length headway, final Speed speed, final Acceleration acceleration)
    {
        try
        {
            return new HeadwayGtuRealCopy(getId(), getGtuType(), headway, getLength(), getWidth(), speed, acceleration,
                    getCarFollowingModel(), getParameters(), getSpeedLimitInfo(), getRoute(), getDesiredSpeed(),
                    getGtuStatus());
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
        return "HeadwayGtuRealCopy [carFollowingModel=" + this.carFollowingModel + ", parameters=" + this.parameters
                + ", speedLimitInfo=" + this.speedLimitInfo + ", route=" + this.route + "]";
    }

}
