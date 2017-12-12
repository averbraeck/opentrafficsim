package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import org.djunits.unit.AccelerationUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTU;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.GapAcceptance;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsUtil;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 27 nov. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */

public enum GapAcceptanceModels implements GapAcceptance
{

    /** Being informed of the model and parameters of other drivers (default LMRS). */
    INFORMED
    {

        @Override
        public boolean acceptGap(final LanePerception perception, final Parameters params, final SpeedLimitInfo sli,
                final CarFollowingModel cfm, final double desire, final Speed ownSpeed, final LateralDirectionality lat)
                throws ParameterException, OperationalPlanException
        {
            NeighborsPerception neighbors = perception.getPerceptionCategory(NeighborsPerception.class);
            if (neighbors.isGtuAlongside(lat))
            {
                // gtu alongside
                return false;
            }

            // TODO
            /*-
             * Followers and are accepted if the acceleration and speed is 0, a leader is accepted if the ego speed is 0. This 
             * is in place as vehicles that provide courtesy, will decelerate for us and overshoot the stand-still distance. As
             * a consequence, they will cease cooperation as they are too close. A pattern will arise where followers slow down
             * to (near) stand-still, and accelerate again, before we could ever accept the gap.
             * 
             * By accepting the gap in the moment that they reach stand-still, this vehicle can at least accept the gap at some 
             * point. All of this is only a problem if the own vehicle is standing still. Otherwise the stand-still distance is 
             * not important and movement of our own will create an acceptable situation.
             * 
             * What needs to be done, is to find a better way to deal with the cooperation and gap-acceptance, such that this  
             * hack is not required.
             */
            Acceleration b = params.getParameter(ParameterTypes.B);
            Acceleration aFollow = new Acceleration(Double.POSITIVE_INFINITY, AccelerationUnit.SI);
            for (

    HeadwayGTU follower : neighbors.getFirstFollowers(lat))
            {
                if (follower.getSpeed().gt0() || follower.getAcceleration().gt0())
                {
                    Acceleration a = LmrsUtil.singleAcceleration(follower.getDistance(), follower.getSpeed(), ownSpeed, desire,
                            follower.getParameters(), follower.getSpeedLimitInfo(), follower.getCarFollowingModel());
                    aFollow = Acceleration.min(aFollow, a);
                }
            }

            Acceleration aSelf = egoAcceleration(perception, params, sli, cfm, desire, ownSpeed, lat);
            Acceleration threshold = b.multiplyBy(-desire);
            return aFollow.ge(threshold) && aSelf.ge(threshold);
        }

    },

    /** Being informed of the model and parameters of other drivers, but applying own headway value. */
    EGO_HEADWAY
    {

        @Override
        public boolean acceptGap(final LanePerception perception, final Parameters params, final SpeedLimitInfo sli,
                final CarFollowingModel cfm, final double desire, final Speed ownSpeed, final LateralDirectionality lat)
                throws ParameterException, OperationalPlanException
        {
            NeighborsPerception neigbors = perception.getPerceptionCategory(NeighborsPerception.class);
            if (neigbors.isGtuAlongside(lat))
            {
                // gtu alongside
                return false;
            }

            Acceleration b = params.getParameter(ParameterTypes.B);
            Acceleration aFollow = new Acceleration(Double.POSITIVE_INFINITY, AccelerationUnit.SI);
            for (

    HeadwayGTU follower : neigbors.getFirstFollowers(lat))
            {
                if (follower.getSpeed().gt0() || follower.getAcceleration().gt0())
                {
                    // Change headway parameter
                    Parameters folParams = follower.getParameters();
                    folParams.setParameter(ParameterTypes.TMAX, params.getParameter(ParameterTypes.TMAX));
                    Acceleration a = LmrsUtil.singleAcceleration(follower.getDistance(), follower.getSpeed(), ownSpeed, desire,
                            folParams, follower.getSpeedLimitInfo(), follower.getCarFollowingModel());
                    aFollow = Acceleration.min(aFollow, a);
                }
            }

            Acceleration aSelf = egoAcceleration(perception, params, sli, cfm, desire, ownSpeed, lat);
            Acceleration threshold = b.multiplyBy(-desire);
            return aFollow.ge(threshold) && aSelf.ge(threshold);
        }

    };

    /**
     * Determine whether a gap is acceptable.
     * @param perception perception
     * @param params parameters
     * @param sli speed limit info
     * @param cfm car-following model
     * @param desire level of lane change desire
     * @param ownSpeed own speed
     * @param lat lateral direction for synchronization
     * @return whether a gap is acceptable
     * @throws ParameterException if a parameter is not defined
     * @throws OperationalPlanException perception exception
     */
    static Acceleration egoAcceleration(final LanePerception perception, final Parameters params, final SpeedLimitInfo sli,
            final CarFollowingModel cfm, final double desire, final Speed ownSpeed, final LateralDirectionality lat)
            throws ParameterException, OperationalPlanException
    {
        Acceleration aSelf = new Acceleration(Double.POSITIVE_INFINITY, AccelerationUnit.SI);
        if (ownSpeed.gt0())
        {
            for (

            HeadwayGTU leader : perception.getPerceptionCategory(NeighborsPerception.class).getFirstLeaders(lat))
            {
                Acceleration a = LmrsUtil.singleAcceleration(leader.getDistance(), ownSpeed, leader.getSpeed(), desire, params,
                        sli, cfm);
                aSelf = Acceleration.min(aSelf, a);
            }
        }
        return aSelf;
    }

}
