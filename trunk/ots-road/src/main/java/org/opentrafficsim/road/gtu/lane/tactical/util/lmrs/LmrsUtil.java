package org.opentrafficsim.road.gtu.lane.tactical.util.lmrs;

import static org.opentrafficsim.core.gtu.behavioralcharacteristics.AbstractParameterType.Check.UNITINTERVAL;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.TimeUnit;
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
import org.opentrafficsim.core.gtu.perception.EgoPerception;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.InfrastructureLaneChangeInfo;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.IntersectionPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.AbstractHeadwayGTU;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayConflict;
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
    public static final ParameterTypeDouble DLEFT = new ParameterTypeDouble("dLeft", "Left lane change desire.", 0.0);

    /** Current right lane change desire. */
    public static final ParameterTypeDouble DRIGHT = new ParameterTypeDouble("dRight", "Right lane change desire.", 0.0);

    /** Lane change desire of current lane change. */
    public static final ParameterTypeDouble DLC = new ParameterTypeDouble("dLaneChange", "Desire of current lane change.", 0.0);

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
     * @param carFollowingModel car-following model
     * @param laneChange lane change status
     * @param lmrsData LMRS data
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
    public static SimpleOperationalPlan determinePlan(final LaneBasedGTU gtu, final Time startTime,
            final CarFollowingModel carFollowingModel, final LaneChange laneChange, final LmrsData lmrsData,
            final LanePerception perception, final LinkedHashSet<MandatoryIncentive> mandatoryIncentives,
            final LinkedHashSet<VoluntaryIncentive> voluntaryIncentives)
            throws GTUException, NetworkException, ParameterException, OperationalPlanException
    {

        // TODO this is a hack to prevent right lane changes of all vehicles on the left lane when placed in network at t=0
        if (startTime.si == 0.0)
        {
            return new SimpleOperationalPlan(Acceleration.ZERO, LateralDirectionality.NONE);
        }

        // obtain objects to get info
        SpeedLimitProspect slp =
                perception.getPerceptionCategory(InfrastructurePerception.class).getSpeedLimitProspect(RelativeLane.CURRENT);
        SpeedLimitInfo sli = slp.getSpeedLimitInfo(Length.ZERO);
        BehavioralCharacteristics bc = gtu.getBehavioralCharacteristics();

        // regular car-following
        Speed speed = gtu.getSpeed();
        SortedSet<AbstractHeadwayGTU> leaders =
                perception.getPerceptionCategory(NeighborsPerception.class).getLeaders(RelativeLane.CURRENT);
        if (!leaders.isEmpty() && lmrsData.isNewLeader(leaders.first()))
        {
            initHeadwayRelaxation(bc, leaders.first());
        }
        Acceleration a = CarFollowingUtil.followLeaders(carFollowingModel, bc, speed, sli, leaders);

        // stop for end
        Length remainingDist = null;
        for (InfrastructureLaneChangeInfo ili : perception.getPerceptionCategory(InfrastructurePerception.class)
                .getInfrastructureLaneChangeInfo(RelativeLane.CURRENT))
        {
            if (remainingDist == null || remainingDist.gt(ili.getRemainingDistance()))
            {
                remainingDist = ili.getRemainingDistance();
            }
        }
        if (remainingDist != null)
        {
            Acceleration bCrit = bc.getParameter(ParameterTypes.BCRIT);
            remainingDist = remainingDist.minus(bc.getParameter(ParameterTypes.S0)).minus(gtu.getFront().getDx());
            if (remainingDist.le(Length.ZERO))
            {
                if (speed.gt(Speed.ZERO))
                {
                    a = Acceleration.min(a, bCrit.neg());
                }
                else
                {
                    a = Acceleration.min(a, Acceleration.ZERO);
                }
            }
            else
            {
                Acceleration bMin = new Acceleration(.5 * speed.si * speed.si / remainingDist.si, AccelerationUnit.SI);
                if (bMin.ge(bCrit))
                {
                    a = Acceleration.min(a, bMin.neg());
                }
            }
        }

        // during a lane change, both leaders are followed
        LateralDirectionality initiatedLaneChange;
        if (laneChange.isChangingLane())
        {
            RelativeLane secondLane = laneChange.getSecondLane(gtu);
            initiatedLaneChange = LateralDirectionality.NONE;
            SortedSet<AbstractHeadwayGTU> secondLeaders =
                    perception.getPerceptionCategory(NeighborsPerception.class).getLeaders(secondLane);
            Acceleration aSecond = CarFollowingUtil.followLeaders(carFollowingModel, bc, speed, sli, secondLeaders);
            if (!secondLeaders.isEmpty() && lmrsData.isNewLeader(secondLeaders.first()))
            {
                initHeadwayRelaxation(bc, secondLeaders.first());
            }
            a = Acceleration.min(a, aSecond);
        }
        else
        {

            // determine lane change desire based on incentives
            Desire desire = getLaneChangeDesire(bc, perception, carFollowingModel, mandatoryIncentives, voluntaryIncentives);

            // gap acceptance
            boolean acceptLeft = perception.getLaneStructure().getRootLSR().getLeft() != null
                    && acceptGap(perception, bc, sli, carFollowingModel, desire.getLeft(), speed, LateralDirectionality.LEFT);
            boolean acceptRight = perception.getLaneStructure().getRootLSR().getRight() != null
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
                bc.setParameter(DLC, desire.getLeft());
                setDesiredHeadway(bc, desire.getLeft());
                leaders = perception.getPerceptionCategory(NeighborsPerception.class).getLeaders(RelativeLane.LEFT);
                if (!leaders.isEmpty())
                {
                    // don't respond on its lane change desire, but remember it such that it isn't a new leader in the next step
                    lmrsData.isNewLeader(leaders.first());
                }
            }
            else if (!desire.leftIsLargerOrEqual() && desire.getRight() >= dFree && acceptRight)
            {
                // change right
                initiatedLaneChange = LateralDirectionality.RIGHT;
                turnIndicatorStatus = TurnIndicatorStatus.RIGHT;
                bc.setParameter(DLC, desire.getRight());
                setDesiredHeadway(bc, desire.getRight());
                leaders = perception.getPerceptionCategory(NeighborsPerception.class).getLeaders(RelativeLane.RIGHT);
                if (!leaders.isEmpty())
                {
                    // don't respond on its lane change desire, but remember it such that it isn't a new leader in the next step
                    lmrsData.isNewLeader(leaders.first());
                }
            }
            else
            {
                initiatedLaneChange = LateralDirectionality.NONE;
                turnIndicatorStatus = TurnIndicatorStatus.NONE;
            }
            laneChange.setLaneChangeDuration(gtu.getBehavioralCharacteristics().getParameter(ParameterTypes.LCDUR));

            // take action if we cannot change lane
            Acceleration aSync;
            if (initiatedLaneChange.equals(LateralDirectionality.NONE))
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
                bc.setParameter(DLEFT, desire.getLeft());
                bc.setParameter(DRIGHT, desire.getRight());
            }
            else
            {
                bc.setParameter(DLEFT, 0.0);
                bc.setParameter(DRIGHT, 0.0);
            }
            gtu.setTurnIndicatorStatus(turnIndicatorStatus);

            // cooperate
            aSync = cooperate(perception, bc, sli, carFollowingModel, speed, LateralDirectionality.LEFT);
            a = Acceleration.min(a, aSync);
            aSync = cooperate(perception, bc, sli, carFollowingModel, speed, LateralDirectionality.RIGHT);
            a = Acceleration.min(a, aSync);

            // relaxation
            exponentialHeadwayRelaxation(bc);

        }
        lmrsData.finalizeStep();

        return new SimpleOperationalPlan(a, initiatedLaneChange);

    }

    /**
     * Sets the headway as a response to a new leader.
     * @param bc behavioral characteristics
     * @param leader leader
     * @throws ParameterException if DLC is not present
     */
    private static void initHeadwayRelaxation(final BehavioralCharacteristics bc, final AbstractHeadwayGTU leader)
            throws ParameterException
    {
        if (leader.getBehavioralCharacteristics().contains(DLC))
        {
            setDesiredHeadway(bc, leader.getBehavioralCharacteristics().getParameter(DLC));
        }
        // else could not be perceived
    }

    /**
     * Updates the desired headway following an exponential shape approximated with fixed time step <tt>DT</tt>.
     * @param bc behavioral characteristics
     * @throws ParameterException in case of a parameter exception
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
     * Sets value for T depending on level of lane change desire.
     * @param bc behavioral characteristics
     * @param desire lane change desire
     * @throws ParameterException if T, TMIN or TMAX is not in the behavioral characteristics
     */
    private static void setDesiredHeadway(final BehavioralCharacteristics bc, final double desire) throws ParameterException
    {
        double limitedDesire = desire < 0 ? 0 : desire > 1 ? 1 : desire;
        double tDes = limitedDesire * bc.getParameter(ParameterTypes.TMIN).si
                + (1 - limitedDesire) * bc.getParameter(ParameterTypes.TMAX).si;
        double t = bc.getParameter(ParameterTypes.T).si;
        bc.setParameter(ParameterTypes.T, new Duration(tDes < t ? tDes : t, TimeUnit.SI));
    }

    /**
     * Resets value for T depending on level of lane change desire.
     * @param bc behavioral characteristics
     * @throws ParameterException if T is not in the behavioral characteristics
     */
    private static void resetDesiredHeadway(final BehavioralCharacteristics bc) throws ParameterException
    {
        bc.resetParameter(ParameterTypes.T);
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
        if ((lat.isLeft() && !perception.getLaneStructure().getCrossSection().contains(RelativeLane.LEFT))
                || (lat.isRight() && !perception.getLaneStructure().getCrossSection().contains(RelativeLane.RIGHT)))
        {
            return new Acceleration(Double.POSITIVE_INFINITY, AccelerationUnit.SI);
        }
        Acceleration b = bc.getParameter(ParameterTypes.B);
        Acceleration a = new Acceleration(Double.POSITIVE_INFINITY, AccelerationUnit.SI);
        RelativeLane relativeLane = new RelativeLane(lat, 1);
        SortedSet<AbstractHeadwayGTU> set = removeAllUpstreamOfConflicts(removeAllUpstreamOfConflicts(
                perception.getPerceptionCategory(NeighborsPerception.class).getLeaders(relativeLane), perception, relativeLane),
                perception, RelativeLane.CURRENT);
        if (!set.isEmpty())
        {
            Acceleration aSingle =
                    singleAcceleration(set.first().getDistance(), ownSpeed, set.first().getSpeed(), desire, bc, sli, cfm);
            a = Acceleration.min(a, aSingle);
        }
        return Acceleration.max(a, b.neg());
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
        if ((lat.isLeft() && !perception.getLaneStructure().getCrossSection().contains(RelativeLane.LEFT))
                || (lat.isRight() && !perception.getLaneStructure().getCrossSection().contains(RelativeLane.RIGHT)))
        {
            return new Acceleration(Double.MAX_VALUE, AccelerationUnit.SI);
        }
        Acceleration b = bc.getParameter(ParameterTypes.B);
        Acceleration a = new Acceleration(Double.MAX_VALUE, AccelerationUnit.SI);
        double dCoop = bc.getParameter(DCOOP);
        RelativeLane relativeLane = new RelativeLane(lat, 1);
        for (AbstractHeadwayGTU leader : removeAllUpstreamOfConflicts(removeAllUpstreamOfConflicts(
                perception.getPerceptionCategory(NeighborsPerception.class).getLeaders(relativeLane), perception, relativeLane),
                perception, RelativeLane.CURRENT))
        {
            BehavioralCharacteristics bc2 = leader.getBehavioralCharacteristics();
            double desire = lat.equals(LateralDirectionality.LEFT) && bc2.contains(DRIGHT) ? bc2.getParameter(DRIGHT)
                    : lat.equals(LateralDirectionality.RIGHT) && bc2.contains(DLEFT) ? bc2.getParameter(DLEFT) : 0;
            if (desire >= dCoop && (perception.getPerceptionCategory(EgoPerception.class).getSpeed().gt(Speed.ZERO)
                    || leader.getSpeed().gt(Speed.ZERO)))
            {
                Acceleration aSingle =
                        singleAcceleration(leader.getDistance(), ownSpeed, leader.getSpeed(), desire, bc, sli, cfm);
                a = Acceleration.min(a, aSingle);
            }
        }

        return Acceleration.max(a, b.neg());
    }

    /**
     * Removes all GTUs from the set, that are found upstream on the conflicting lane of a conflict in the current lane.
     * @param set set of GTUs
     * @param perception perception
     * @param relativeLane relative lane
     * @return the input set
     * @throws OperationalPlanException if the {@code IntersectionPerception} category is not present
     */
    private static SortedSet<AbstractHeadwayGTU> removeAllUpstreamOfConflicts(final SortedSet<AbstractHeadwayGTU> set,
            final LanePerception perception, final RelativeLane relativeLane) throws OperationalPlanException
    {
        for (HeadwayConflict conflict : perception.getPerceptionCategory(IntersectionPerception.class)
                .getConflicts(relativeLane))
        {
            if (conflict.isCrossing())
            {
                for (AbstractHeadwayGTU conflictGtu : conflict.getUpstreamConflictingGTUs())
                {
                    for (AbstractHeadwayGTU gtu : set)
                    {
                        if (conflictGtu.getId().equals(gtu.getId()))
                        {
                            set.remove(gtu);
                            break;
                        }
                    }
                }
            }
        }
        return set;
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
        setDesiredHeadway(bc, desire);
        // calculate acceleration
        SortedMap<Length, Speed> leaders = new TreeMap<>();
        leaders.put(distance, leaderSpeed);
        Acceleration a = cfm.followingAcceleration(bc, followerSpeed, sli, leaders);
        // reset T
        resetDesiredHeadway(bc);
        return a;
    }

    /**
     * Keeps data for LMRS for a specific GTU.
     * <p>
     * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 8 nov. 2016 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    public static final class LmrsData
    {

        /** Most recent leaders. */
        private final Set<String> leaders = new HashSet<>();

        /** Current leaders. */
        private final Set<String> tempLeaders = new HashSet<>();

        /**
         * Checks if the given leader is a new leader.
         * @param gtu gtu to check
         * @return whether the gtu is a new leader
         */
        public boolean isNewLeader(final AbstractHeadwayGTU gtu)
        {
            this.tempLeaders.add(gtu.getId());
            return !this.leaders.contains(gtu.getId());
        }

        /**
         * Remembers the leaders of the current time step (those forwarded to isNewLeader()) for the next time step.
         */
        public void finalizeStep()
        {
            this.leaders.clear();
            this.leaders.addAll(this.tempLeaders);
            this.tempLeaders.clear();
        }

    }

}
