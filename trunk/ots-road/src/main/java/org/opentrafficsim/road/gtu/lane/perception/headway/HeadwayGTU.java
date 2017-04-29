package org.opentrafficsim.road.gtu.lane.perception.headway;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

/**
 * Interface for perceived surrounding GTU's.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 24 mrt. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface HeadwayGTU extends Headway
{

    /**
     * @return gtuType
     */
    GTUType getGtuType();

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
     * @param headway headway
     * @param speed speed
     * @param acceleration acceleration
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
     * characteristics of the observed GTU can help with that. The behavioral characteristics that are returned can be on a
     * continuum between the actual behavioral characteristics of the observed GTU and the own behavioral characteristics of the
     * observing GTU, not making any assumptions about the observed GTU. When successive observations of the GTU take place,
     * parameters about its behavior can be estimated more accurately. Another interesting easy-to-implement solution is to
     * return a set of behavioral characteristics per GTU type, where the behavioral characteristics of a truck can differ from
     * that of a car.
     * @return the behavioral characteristics that represent the expected behavior of the observed GTU
     */
    BehavioralCharacteristics getBehavioralCharacteristics();

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

}
