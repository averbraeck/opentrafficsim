package org.opentrafficsim.road.gtu.lane.perception.headway;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

/**
 * Interface for perceived surrounding GTU's.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public interface HeadwayGTU extends Headway
{

    /**
     * @return gtuType
     */
    GtuType getGtuType();

    /**
     * @return facingSameDirection
     */
    boolean isFacingSameDirection();

    /**
     * @return were the braking lights on?
     */
    boolean isBrakingLightsOn();

    /**
     * @return was the left turn indicator on?
     */
    boolean isLeftTurnIndicatorOn();

    /**
     * @return was the right turn indicator on?
     */
    boolean isRightTurnIndicatorOn();

    /**
     * @return were the emergency lights on?
     */
    boolean isEmergencyLightsOn();

    /**
     * @return was the vehicle honking or ringing its bell when being observed for the headway?
     */
    boolean isHonking();

    /**
     * Creates a copy with different headway, speed and possibly acceleration. It may not be alongside. This method is used to
     * anticipate movement of a neighboring GTU.
     * @param headway Length; headway
     * @param speed Speed; speed
     * @param acceleration Acceleration; acceleration
     * @return copy with different headway, speed and possibly acceleration
     */
    HeadwayGTU moved(Length headway, Speed speed, Acceleration acceleration);

    /**
     * Many models that observe a GTU need to predict the imminent behavior of that GTU. Having a car following model of the
     * observed GTU can help with that. The car following model that is returned can be on a continuum between the actual car
     * following model of the observed GTU and the own car following model of the observing GTU, not making any assumptions
     * about the observed GTU. When successive observations of the GTU take place, parameters about its behavior can be
     * estimated more accurately. Another interesting easy-to-implement solution is to return a car following model per GTU
     * type, where the following model of a truck can differ from that of a car.
     * @return a car following model that represents the expected behavior of the observed GTU
     */
    CarFollowingModel getCarFollowingModel();

    /**
     * Many models that observe a GTU need to predict the imminent behavior of that GTU. Having an estimate of the behavioral
     * characteristics of the observed GTU can help with that. The parameters that are returned can be on a continuum between
     * the actual parameters of the observed GTU and the own parameters of the observing GTU, not making any assumptions about
     * the observed GTU. When successive observations of the GTU take place, parameters about its behavior can be estimated more
     * accurately. Another interesting easy-to-implement solution is to return a set of parameters per GTU type, where the
     * parameters of a truck can differ from that of a car.
     * @return the parameters that represent the expected behavior of the observed GTU
     */
    Parameters getParameters();

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
    SpeedLimitInfo getSpeedLimitInfo();

    /**
     * Models responding to other GTU may assume a route of the vehicle, for instance at intersections. The route may be short,
     * i.e. only over the next intersection. Implementations may return anything from the actual route, a route based on
     * indicators and other assumptions, or {@code null} if simply not known/estimated.
     * @return route of gtu
     */
    Route getRoute();

    /**
     * Returns the perceived desired speed of the neighbor.
     * @return Speed; perceived desired speed of the neighbor
     */
    Speed getDesiredSpeed();

    /**
     * Returns the width of the GTU.
     * @return Length; width of the GTU
     */
    Length getWidth();

}
