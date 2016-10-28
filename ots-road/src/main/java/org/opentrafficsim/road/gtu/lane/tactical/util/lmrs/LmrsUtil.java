package org.opentrafficsim.road.gtu.lane.tactical.util.lmrs;

import static org.opentrafficsim.core.gtu.behavioralcharacteristics.AbstractParameterType.Check.UNITINTERVAL;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.djunits.unit.AccelerationUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.TurnIndicatorStatus;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypeDouble;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypes;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.AbstractHeadwayGTU;
import org.opentrafficsim.road.gtu.lane.plan.operational.LaneOperationalPlanBuilder.LaneChange;
import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.util.CarFollowingUtil;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;
import org.opentrafficsim.road.network.speed.SpeedLimitProspect;

import nl.tudelft.simulation.language.Throw;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jul 26, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public final class LmrsUtil
{

    /** Free lane change desire threshold. */
    public static final ParameterTypeDouble DFREE =
            new ParameterTypeDouble("dFree", "Free lane change desire threshold.", 0.365, UNITINTERVAL)
            {
                /** */
                private static final long serialVersionUID = 20160413L;

                public void check(final double value, final BehavioralCharacteristics bc) throws ParameterException
                {
                    if (bc.contains(DSYNC))
                    {
                        Throw.when(value >= bc.getParameter(DSYNC), ParameterException.class,
                                "Value of dFree is above or equal to dSync.");
                    }
                    if (bc.contains(DCOOP))
                    {
                        Throw.when(value >= bc.getParameter(DCOOP), ParameterException.class,
                                "Value of dFree is above or equal to dCoop.");
                    }
                }
            };

    /** Synchronized lane change desire threshold. */
    public static final ParameterTypeDouble DSYNC =
            new ParameterTypeDouble("dSync", "Synchronized lane change desire threshold.", 0.577, UNITINTERVAL)
            {
                /** */
                private static final long serialVersionUID = 20160413L;

                public void check(final double value, final BehavioralCharacteristics bc) throws ParameterException
                {
                    if (bc.contains(DFREE))
                    {
                        Throw.when(value <= bc.getParameter(DFREE), ParameterException.class,
                                "Value of dSync is below or equal to dFree.");
                    }
                    if (bc.contains(DCOOP))
                    {
                        Throw.when(value >= bc.getParameter(DCOOP), ParameterException.class,
                                "Value of dSync is above or equal to dCoop.");
                    }
                }
            };

    /** Cooperative lane change desire threshold. */
    public static final ParameterTypeDouble DCOOP =
            new ParameterTypeDouble("dCoop", "Cooperative lane change desire threshold.", 0.788, UNITINTERVAL)
            {
                /** */
                private static final long serialVersionUID = 20160413L;

                public void check(final double value, final BehavioralCharacteristics bc) throws ParameterException
                {
                    if (bc.contains(DFREE))
                    {
                        Throw.when(value <= bc.getParameter(DFREE), ParameterException.class,
                                "Value of dCoop is below or equal to dFree.");
                    }
                    if (bc.contains(DSYNC))
                    {
                        Throw.when(value <= bc.getParameter(DSYNC), ParameterException.class,
                                "Value of dCoop is below or equal to dSync.");
                    }
                }
            };

    /** Current left lane change desire. */
    public static final ParameterTypeDouble DLEFT = new ParameterTypeDouble("dLeft", "Left lane change desire.", 0);

    /** Current right lane change desire. */
    public static final ParameterTypeDouble DRIGHT = new ParameterTypeDouble("dLeft", "Left lane change desire.", 0);

    /**
     * Do not instantiate.
     */
    private LmrsUtil()
    {
        //
    }

    /**
     * Determines a simple representation of an operational plan.
     * @param gtu gtu
     * @param startTime start time
     * @param lmrsStatus LMRS status
     * @param carFollowingModel car-following model
     * @param laneChange lane change status
     * @param perception perception
     * @param mandatoryIncentives set of mandatory lane change incentives
     * @param voluntaryIncentives set of voluntary lane change incentives
     * @return simple operational plan
     * @throws GTUException gtu exception
     * @throws NetworkException network exception
     * @throws ParameterException parameter exception
     * @throws OperationalPlanException operational plan exception
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public static SimpleOperationalPlan determinePlan(final LaneBasedGTU gtu, final Time startTime, final LmrsStatus lmrsStatus,
            final CarFollowingModel carFollowingModel, final LaneChange laneChange, final LanePerception perception,
            final LinkedHashSet<MandatoryIncentive> mandatoryIncentives,
            final LinkedHashSet<VoluntaryIncentive> voluntaryIncentives)
            throws GTUException, NetworkException, ParameterException, OperationalPlanException
    {

        // obtain objects to get info
        SpeedLimitProspect slp =
                perception.getPerceptionCategory(InfrastructurePerception.class).getSpeedLimitProspect(RelativeLane.CURRENT);
        SpeedLimitInfo sli = slp.getSpeedLimitInfo(Length.ZERO);
        BehavioralCharacteristics bc = gtu.getBehavioralCharacteristics();

        // update T as response to new leader
        Set<AbstractHeadwayGTU> leaders =
                perception.getPerceptionCategory(NeighborsPerception.class).getLeaders(RelativeLane.CURRENT);
        for (AbstractHeadwayGTU leader : leaders)
        {
            if (!lmrsStatus.getLastLeaders().contains(leader))
            {
                // TODO updated headway based on (estimated) desire of other vehicle
                // uses current headway, this results in larger deceleration than using the desire
                // we require a desire where T = desire*Tmin + (1-desire)*Tmax gives a deceleration of b*desire
                //
                // idea: acceleration for actual T
                // acceleration for Tmin
                // interpolate appropriately (which is... ? )
                // Duration tLead = Duration.max(bc.getParameter(ParameterTypes.TMIN),
                // leader.getDistance().minus(bc.getParameter(ParameterTypes.S0)).divideBy(gtu.getSpeed()));
                // if (tLead.lt(bc.getParameter(ParameterTypes.T)))
                // {
                // bc.setParameter(ParameterTypes.T, tLead);
                // }
            }
        }
        lmrsStatus.setLastLeaders(leaders);

        // regular car-following
        Speed speed = gtu.getSpeed();
        Acceleration a = CarFollowingUtil.followLeaders(carFollowingModel, bc, speed, sli,
                perception.getPerceptionCategory(NeighborsPerception.class).getLeaders(RelativeLane.CURRENT));

        // during a lane change, both leaders are followed
        LateralDirectionality initiatedLaneChange;
        if (laneChange.isChangingLane())
        {
            // RelativeLane tar = this.laneChangeDirectionality.isLeft() ? RelativeLane.LEFT : RelativeLane.RIGHT;
            RelativeLane tar = laneChange.getTargetLane();
            initiatedLaneChange = tar.getLateralDirectionality();
            Acceleration aTar = CarFollowingUtil.followLeaders(carFollowingModel, bc, speed, sli,
                    perception.getPerceptionCategory(NeighborsPerception.class).getLeaders(tar));
            a = Acceleration.min(a, aTar);

        }
        else
        {
            // relaxation
            exponentialHeadwayRelaxation(bc);

            // determine lane change desire based on incentives
            Desire desire = getLaneChangeDesire(bc, perception, carFollowingModel, mandatoryIncentives, voluntaryIncentives);

            // gap acceptance
            boolean acceptLeft = perception.getLaneStructure().getCrossSection().contains(RelativeLane.LEFT)
                    && acceptGap(perception, bc, sli, carFollowingModel, desire.getLeft(), speed, LateralDirectionality.LEFT);
            boolean acceptRight = perception.getLaneStructure().getCrossSection().contains(RelativeLane.RIGHT)
                    && acceptGap(perception, bc, sli, carFollowingModel, desire.getRight(), speed, LateralDirectionality.RIGHT);

            // lane change decision
            double dFree = bc.getParameter(DFREE);
            double dSync = bc.getParameter(DSYNC);
            double dCoop = bc.getParameter(DCOOP);
            // decide
            TurnIndicatorStatus turnIndicatorStatus;
            if (desire.leftIsLargerOrEqual() && desire.getLeft() >= dFree && acceptLeft)
            {
                // change left
                initiatedLaneChange = LateralDirectionality.LEFT;
                turnIndicatorStatus = TurnIndicatorStatus.LEFT;
                Duration tRel = Duration.interpolate(bc.getParameter(ParameterTypes.TMAX), bc.getParameter(ParameterTypes.TMIN),
                        desire.getLeft());
                if (tRel.lt(bc.getParameter(ParameterTypes.T)))
                {
                    bc.setParameter(ParameterTypes.T, tRel);
                }
                // TODO headway of other driver...
            }
            else if (!desire.leftIsLargerOrEqual() && desire.getRight() >= dFree && acceptRight)
            {
                // change right
                initiatedLaneChange = LateralDirectionality.RIGHT;
                turnIndicatorStatus = TurnIndicatorStatus.RIGHT;
                Duration tRel = Duration.interpolate(bc.getParameter(ParameterTypes.TMAX), bc.getParameter(ParameterTypes.TMIN),
                        desire.getRight());
                if (tRel.lt(bc.getParameter(ParameterTypes.T)))
                {
                    bc.setParameter(ParameterTypes.T, tRel);
                }
                // TODO headway of other driver...
            }
            else
            {
                initiatedLaneChange = null;
                turnIndicatorStatus = TurnIndicatorStatus.NONE;
            }
            laneChange.setLaneChangeDuration(gtu.getBehavioralCharacteristics().getParameter(ParameterTypes.LCDUR));

            // take action if we cannot change lane
            Acceleration aSync;
            if (initiatedLaneChange == null)
            {
                // synchronize
                if (desire.leftIsLargerOrEqual() && desire.getLeft() >= dSync)
                {
                    aSync = synchronize(perception, bc, sli, carFollowingModel, desire.getLeft(), speed,
                            LateralDirectionality.LEFT);
                    a = Acceleration.min(a, aSync);
                }
                else if (!desire.leftIsLargerOrEqual() && desire.getRight() >= dSync)
                {
                    aSync = synchronize(perception, bc, sli, carFollowingModel, desire.getRight(), speed,
                            LateralDirectionality.RIGHT);
                    a = Acceleration.min(a, aSync);
                }
                // use indicators to indicate lane change need
                if (desire.leftIsLargerOrEqual() && desire.getLeft() >= dCoop)
                {
                    // switch on left indicator
                    turnIndicatorStatus = TurnIndicatorStatus.LEFT;
                }
                else if (!desire.leftIsLargerOrEqual() && desire.getRight() >= dCoop)
                {
                    // switch on right indicator
                    turnIndicatorStatus = TurnIndicatorStatus.RIGHT;
                }
            }
            gtu.setTurnIndicatorStatus(turnIndicatorStatus);

            // cooperate
            aSync = cooperate(perception, bc, sli, carFollowingModel, speed, LateralDirectionality.LEFT);
            a = Acceleration.min(a, aSync);
            aSync = cooperate(perception, bc, sli, carFollowingModel, speed, LateralDirectionality.RIGHT);
            a = Acceleration.min(a, aSync);

        }

        return new SimpleOperationalPlan(a, initiatedLaneChange);

    }

    /**
     * Updates the desired headway following an exponential shape approximated with fixed time step <tt>DT</tt>.
     * @param bc Behavioral characteristics.
     * @throws ParameterException In case of a parameter exception.
     */
    private static void exponentialHeadwayRelaxation(final BehavioralCharacteristics bc) throws ParameterException
    {
        double ratio = bc.getParameter(ParameterTypes.DT).si / bc.getParameter(ParameterTypes.TAU).si;
        bc.setParameter(ParameterTypes.T, Duration.interpolate(bc.getParameter(ParameterTypes.T),
                bc.getParameter(ParameterTypes.TMAX), ratio <= 1.0 ? ratio : 1.0));
    }

    /**
     * Determines lane change desire for the given RSU. Mandatory desire is deduced as the maximum of a set of mandatory
     * incentives, while voluntary desires are added. Depending on the level of mandatory lane change desire, voluntary desire
     * may be included partially. If both are positive or negative, voluntary desire is fully included. Otherwise, voluntary
     * desire is less considered within the range dSync &lt; |mandatory| &lt; dCoop. The absolute value is used as large
     * negative mandatory desire may also dominate voluntary desire.
     * @param behavioralCharacteristics behavioral characteristics
     * @param perception perception
     * @param carFollowingModel car-following model
     * @param mandatoryIncentives mandatory incentives
     * @param voluntaryIncentives voluntary incentives
     * @return lane change desire for gtu
     * @throws ParameterException if a parameter is not defined
     * @throws GTUException if there is no mandatory incentive, the model requires at least one
     * @throws OperationalPlanException perception exception
     */
    private static Desire getLaneChangeDesire(final BehavioralCharacteristics behavioralCharacteristics,
            final LanePerception perception, final CarFollowingModel carFollowingModel,
            final LinkedHashSet<MandatoryIncentive> mandatoryIncentives,
            final LinkedHashSet<VoluntaryIncentive> voluntaryIncentives)
            throws ParameterException, GTUException, OperationalPlanException
    {

        double dSync = behavioralCharacteristics.getParameter(DSYNC);
        double dCoop = behavioralCharacteristics.getParameter(DCOOP);

        // Mandatory desire
        double dLeftMandatory = Double.NEGATIVE_INFINITY;
        double dRightMandatory = Double.NEGATIVE_INFINITY;
        Desire mandatoryDesire = new Desire(dLeftMandatory, dRightMandatory);
        for (MandatoryIncentive incentive : mandatoryIncentives)
        {
            Desire d = incentive.determineDesire(behavioralCharacteristics, perception, carFollowingModel, mandatoryDesire);
            dLeftMandatory = d.getLeft() > dLeftMandatory ? d.getLeft() : dLeftMandatory;
            dRightMandatory = d.getRight() > dRightMandatory ? d.getRight() : dRightMandatory;
            mandatoryDesire = new Desire(dLeftMandatory, dRightMandatory);
        }

        // Voluntary desire
        double dLeftVoluntary = 0;
        double dRightVoluntary = 0;
        Desire voluntaryDesire = new Desire(dLeftVoluntary, dRightVoluntary);
        for (VoluntaryIncentive incentive : voluntaryIncentives)
        {
            Desire d = incentive.determineDesire(behavioralCharacteristics, perception, carFollowingModel, mandatoryDesire,
                    voluntaryDesire);
            dLeftVoluntary += d.getLeft();
            dRightVoluntary += d.getRight();
            voluntaryDesire = new Desire(dLeftVoluntary, dRightVoluntary);
        }

        // Total desire
        double thetaLeft = 0;
        if (dLeftMandatory <= dSync || dLeftMandatory * dLeftVoluntary >= 0)
        {
            // low mandatory desire, or same sign
            thetaLeft = 1;
        }
        else if (dSync < dLeftMandatory && dLeftMandatory < dCoop && dLeftMandatory * dLeftVoluntary < 0)
        {
            // linear from 1 at dSync to 0 at dCoop
            thetaLeft = (dCoop - Math.abs(dLeftMandatory)) / (dCoop - dSync);
        }
        double thetaRight = 0;
        if (dRightMandatory <= dSync || dRightMandatory * dRightVoluntary >= 0)
        {
            // low mandatory desire, or same sign
            thetaRight = 1;
        }
        else if (dSync < dRightMandatory && dRightMandatory < dCoop && dRightMandatory * dRightVoluntary < 0)
        {
            // linear from 1 at dSync to 0 at dCoop
            thetaRight = (dCoop - Math.abs(dRightMandatory)) / (dCoop - dSync);
        }

        return new Desire(dLeftMandatory + thetaLeft * dLeftVoluntary, dRightMandatory + thetaRight * dRightVoluntary);

    }

    /**
     * Determine whether a gap is acceptable.
     * @param perception perception
     * @param bc behavioral characteristics
     * @param sli speed limit info
     * @param cfm car-following model
     * @param desire level of lane change desire
     * @param ownSpeed own speed
     * @param lat lateral direction for synchronization
     * @return whether a gap is acceptable
     * @throws ParameterException if a parameter is not defined
     * @throws OperationalPlanException perception exception
     */
    private static boolean acceptGap(final LanePerception perception, final BehavioralCharacteristics bc,
            final SpeedLimitInfo sli, final CarFollowingModel cfm, final double desire, final Speed ownSpeed,
            final LateralDirectionality lat) throws ParameterException, OperationalPlanException
    {
        Acceleration b = bc.getParameter(ParameterTypes.B);
        if (perception.getPerceptionCategory(InfrastructurePerception.class).getLegalLaneChangePossibility(RelativeLane.CURRENT,
                lat).si > 0 && !perception.getPerceptionCategory(NeighborsPerception.class).isGtuAlongside(lat))
        {
            Acceleration aFollow = new Acceleration(Double.POSITIVE_INFINITY, AccelerationUnit.SI);
            for (AbstractHeadwayGTU follower : perception.getPerceptionCategory(NeighborsPerception.class)
                    .getFirstFollowers(lat))
            {
                Acceleration a = singleAcceleration(follower.getDistance(), follower.getSpeed(), ownSpeed, desire,
                        follower.getBehavioralCharacteristics(), follower.getSpeedLimitInfo(), follower.getCarFollowingModel());
                aFollow = Acceleration.min(aFollow, a);
            }
            Acceleration aSelf = new Acceleration(Double.POSITIVE_INFINITY, AccelerationUnit.SI);
            for (AbstractHeadwayGTU leader : perception.getPerceptionCategory(NeighborsPerception.class).getFirstLeaders(lat))
            {
                Acceleration a = singleAcceleration(leader.getDistance(), ownSpeed, leader.getSpeed(), desire, bc, sli, cfm);
                aSelf = Acceleration.min(aSelf, a);
            }
            Acceleration threshold = b.multiplyBy(-desire);
            return aFollow.ge(threshold) && aSelf.ge(threshold);
        }
        return false;
    }

    /**
     * Determine acceleration for synchronization.
     * @param perception perception
     * @param bc behavioral characteristics
     * @param sli speed limit info
     * @param cfm car-following model
     * @param desire level of lane change desire
     * @param ownSpeed own speed
     * @param lat lateral direction for synchronization
     * @return acceleration for synchronization
     * @throws ParameterException if a parameter is not defined
     * @throws OperationalPlanException perception exception
     */
    private static Acceleration synchronize(final LanePerception perception, final BehavioralCharacteristics bc,
            final SpeedLimitInfo sli, final CarFollowingModel cfm, final double desire, final Speed ownSpeed,
            final LateralDirectionality lat) throws ParameterException, OperationalPlanException
    {
        Acceleration b = bc.getParameter(ParameterTypes.B);
        Acceleration a = new Acceleration(Double.POSITIVE_INFINITY, AccelerationUnit.SI);
        for (AbstractHeadwayGTU leader : perception.getPerceptionCategory(NeighborsPerception.class).getFirstLeaders(lat))
        {
            Acceleration aSingle = singleAcceleration(leader.getDistance(), ownSpeed, leader.getSpeed(), desire, bc, sli, cfm);
            a = Acceleration.min(a, aSingle);
        }
        return Acceleration.max(a, b.multiplyBy(-1));
    }

    /**
     * Determine acceleration for cooperation.
     * @param perception perception
     * @param bc behavioral characteristics
     * @param sli speed limit info
     * @param cfm car-following model
     * @param ownSpeed own speed
     * @param lat lateral direction for cooperation
     * @return acceleration for synchronization
     * @throws ParameterException if a parameter is not defined
     * @throws OperationalPlanException perception exception
     */
    private static Acceleration cooperate(final LanePerception perception, final BehavioralCharacteristics bc,
            final SpeedLimitInfo sli, final CarFollowingModel cfm, final Speed ownSpeed, final LateralDirectionality lat)
            throws ParameterException, OperationalPlanException
    {
        if (!perception.getLaneStructure().getCrossSection().contains(new RelativeLane(lat, 1)))
        {
            return new Acceleration(Double.MAX_VALUE, AccelerationUnit.SI);
        }
        Acceleration b = bc.getParameter(ParameterTypes.B);
        Acceleration a = new Acceleration(Double.POSITIVE_INFINITY, AccelerationUnit.SI);
        for (AbstractHeadwayGTU leader : perception.getPerceptionCategory(NeighborsPerception.class).getFirstLeaders(lat))
        {
            if ((lat == LateralDirectionality.LEFT && leader.isRightTurnIndicatorOn())
                    || (lat == LateralDirectionality.RIGHT && leader.isLeftTurnIndicatorOn()))
            {
                BehavioralCharacteristics bc2 = leader.getBehavioralCharacteristics();
                double desire = lat == LateralDirectionality.LEFT && bc2.contains(DRIGHT) ? bc2.getParameter(DRIGHT)
                        : lat == LateralDirectionality.RIGHT && bc2.contains(DLEFT) ? bc2.getParameter(DLEFT) : 0;
                Acceleration aSingle =
                        singleAcceleration(leader.getDistance(), ownSpeed, leader.getSpeed(), desire, bc, sli, cfm);
                a = Acceleration.min(a, aSingle);
            }
        }
        return Acceleration.max(a, b.multiplyBy(-1));
    }

    /**
     * Determine acceleration from car-following.
     * @param distance distance from follower to leader
     * @param followerSpeed speed of follower
     * @param leaderSpeed speed of leader
     * @param desire level of lane change desire
     * @param bc behavioral characteristics
     * @param sli speed limit info
     * @param cfm car-following model
     * @return acceleration from car-following
     * @throws ParameterException if a parameter is not defined
     */
    private static Acceleration singleAcceleration(final Length distance, final Speed followerSpeed, final Speed leaderSpeed,
            final double desire, final BehavioralCharacteristics bc, final SpeedLimitInfo sli, final CarFollowingModel cfm)
            throws ParameterException
    {
        // set T
        bc.setParameter(ParameterTypes.T,
                Duration.interpolate(bc.getParameter(ParameterTypes.TMAX), bc.getParameter(ParameterTypes.TMIN), desire));
        // calculate acceleration
        SortedMap<Length, Speed> leaders = new TreeMap<>();
        leaders.put(distance, leaderSpeed);
        Acceleration a = cfm.followingAcceleration(bc, followerSpeed, sli, leaders);
        // reset T
        bc.resetParameter(ParameterTypes.T);
        return a;
    }

    /**
     * Status of LMRS.
     * <p>
     * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jul 27, 2016 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    public static class LmrsStatus implements Serializable
    {

        /** */
        private static final long serialVersionUID = 20160811L;

        /** Remembered leaders. */
        private Set<AbstractHeadwayGTU> lastLeaders = new HashSet<>();

        /**
         * @return lastLeaders.
         */
        public final Set<AbstractHeadwayGTU> getLastLeaders()
        {
            return new HashSet<>(this.lastLeaders);
        }

        /**
         * @param lastLeaders set lastLeaders.
         */
        public final void setLastLeaders(final Set<AbstractHeadwayGTU> lastLeaders)
        {
            this.lastLeaders.clear();
            this.lastLeaders.addAll(lastLeaders);
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "LmrsStatus";
        }

    }

}
