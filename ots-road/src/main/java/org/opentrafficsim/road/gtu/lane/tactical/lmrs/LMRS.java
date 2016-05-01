package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import java.util.HashSet;
import java.util.Set;

import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypes;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.HeadwayGTU;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.tactical.AbstractLaneBasedTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;

/**
 * Implementation of the LMRS (Lane change Model with Relaxation and Synchronization). See Schakel, W.J., Knoop, V.L., and Van
 * Arem, B. (2012), <a href="http://victorknoop.eu/research/papers/TRB2012_LMRS_reviewed.pdf">LMRS: Integrated Lane Change Model
 * with Relaxation and Synchronization</a>, Transportation Research Records: Journal of the Transportation Research Board, No.
 * 2316, pp. 47-57. Note in the official versions of TRB and TRR some errors appeared due to the typesetting of the papers (not
 * in the preprint provided here). A list of errata for the official versions is found <a
 * href="http://victorknoop.eu/research/papers/Erratum_LMRS.pdf">here</a>.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 13, 2016 <br>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class LMRS extends AbstractLMRS
{

    /** Serialization id. */
    private static final long serialVersionUID = 20160300L;

    /**
     * Constructor setting the car-following model.
     * @param carFollowingModel Car-following model.
     */
    public LMRS(final CarFollowingModel carFollowingModel)
    {
        super(carFollowingModel);
    }

    /** {@inheritDoc} */
    @Override
    public final Set<MandatoryIncentive> getDefaultMandatoryIncentives()
    {
        HashSet<MandatoryIncentive> set = new HashSet<MandatoryIncentive>();
        set.add(new IncentiveRoute());
        return set;
    }

    /** {@inheritDoc} */
    @Override
    public final Set<VoluntaryIncentive> getDefaultVoluntaryIncentives()
    {
        HashSet<VoluntaryIncentive> set = new HashSet<VoluntaryIncentive>();
        set.add(new IncentiveSpeedWithCourtesy());
        set.add(new IncentiveKeep());
        return set;
    }

    /** {@inheritDoc} */
    @Override
    public final OperationalPlan generateOperationalPlan(final GTU gtu, final Time startTime,
        final DirectedPoint locationAtStartTime) throws OperationalPlanException, GTUException, NetworkException,
        ParameterException
    {

        // TODO: Remove this when other todo's are done, it is used as a placeholder where some acceleration needs to be
        // determined.
        Acceleration dummy = new Acceleration(0, AccelerationUnit.SI);

        // Obtain objects to get info
        LaneBasedGTU gtuLane = (LaneBasedGTU) gtu;
        LanePerception perception = (LanePerception) gtu.getPerception();
        CarFollowingModel cfm = ((AbstractLaneBasedTacticalPlanner) gtuLane.getTacticalPlanner()).getCarFollowingModel();

        // TODO: Throw ParameterException
        BehavioralCharacteristics bc = gtuLane.getBehavioralCharacteristics();
        Acceleration b = bc.getParameter(ParameterTypes.B);
        double dFree = bc.getParameter(DFREE);
        double dSync = bc.getParameter(DSYNC);
        double dCoop = bc.getParameter(DCOOP);

        // Determine tactical plan
        if (Math.random() > .5)
        { // TODO: if changing lane (rand to disable annoying warnings)

            /*
             * During a lane change, both leaders are followed.
             */
            Acceleration a = dummy;
            a = minOf(a, dummy);

            /*
             * Operational plan.
             */
            // TODO: Build the operational plan using minimum acceleration
            // LaneOperationalPlanBuilder
            return null;

        }

        // Relaxation 
        exponentialHeadwayRelaxation(bc);

        /*
         * Determine desire Mandatory is deduced as the maximum of a set of mandatory incentives, while voluntary desires are
         * added. Depending on the level of mandatory lane change desire, voluntary desire may be included partially. If both
         * are positive or negative, voluntary desire is fully included. Otherwise, voluntary desire is less considered within
         * the range dSync < |mandatory| < dCoop. The absolute value is used as large negative mandatory desire may also
         * dominate voluntary desire.
         */
        // Mandatory desire
        double dLeftMandatory = Double.NEGATIVE_INFINITY;
        double dRightMandatory = Double.NEGATIVE_INFINITY;
        for (MandatoryIncentive incentive : getMandatoryIncentives())
        {
            Desire d = incentive.determineDesire(gtuLane, perception);
            dLeftMandatory = d.getLeft() > dLeftMandatory ? d.getLeft() : dLeftMandatory;
            dRightMandatory = d.getRight() > dRightMandatory ? d.getRight() : dRightMandatory;
        }
        Desire mandatoryDesire = new Desire(dLeftMandatory, dRightMandatory);
        // Voluntary desire
        double dLeftVoluntary = 0;
        double dRightVoluntary = 0;
        for (VoluntaryIncentive incentive : getVoluntaryIncentives())
        {
            Desire d = incentive.determineDesire(gtuLane, perception, mandatoryDesire);
            dLeftVoluntary += d.getLeft();
            dRightVoluntary += d.getRight();
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
        Desire totalDesire =
            new Desire(dLeftMandatory + thetaLeft * dLeftVoluntary, dRightMandatory + thetaRight * dRightVoluntary);

        /*
         * Gap acceptance The adjacent gap is accepted if acceleration is safe for the potential follower and for this driver.
         */
        // TODO: get neighboring vehicles, use their (perceived) car-following model with altered headway
        Acceleration aFollow = dummy;
        Acceleration aSelf = dummy;
        boolean leftAllowed = true; // TODO: get from perception / infrastructure, with relaxation
        boolean acceptLeft =
            aSelf.getSI() >= -b.si * totalDesire.getLeft() && aFollow.getSI() >= -b.si * totalDesire.getLeft()
                && leftAllowed;
        aFollow = dummy;
        aSelf = dummy;
        boolean rightAllowed = true; // TODO: get from perception / infrastructure, with relaxation
        boolean acceptRight =
            aSelf.getSI() >= -b.si * totalDesire.getRight() && aFollow.getSI() >= -b.si * totalDesire.getRight()
                && rightAllowed;

        /*
         * Lane change decision A lane change is initiated for the largest desire if this is above the threshold and the gap is
         * accepted. Otherwise, the indicator may be switched on.
         */
        // TODO: switch on indicators (and off)?
        boolean changeLeft = false;
        boolean changeRight = false;
        if (totalDesire.leftIsLargerOrEqual() && totalDesire.getLeft() >= dFree && acceptLeft)
        {
            // change left
            changeLeft = true;
        }
        else if (!totalDesire.leftIsLargerOrEqual() && totalDesire.getRight() >= dFree && acceptRight)
        {
            // change right
            changeRight = true;
        }
        else if (totalDesire.leftIsLargerOrEqual() && totalDesire.getLeft() >= dCoop)
        {
            // switch on left indicator

        }
        else if (!totalDesire.leftIsLargerOrEqual() && totalDesire.getRight() >= dCoop)
        {
            // switch on right indicator

        }

        /*
         * Acceleration Acceleration is determined by the leader, and possibly adjacent vehicles for synchronization and
         * cooperation.
         */
        // follow leader
        Acceleration a = dummy;
        // synchronize
        // TODO: get neighboring vehicles, use car-following model with altered headway
        if (totalDesire.leftIsLargerOrEqual() && totalDesire.getLeft() >= dSync)
        {
            // sync left
            a = minOf(a, limitDeceleration(dummy, b));
        }
        else if (!totalDesire.leftIsLargerOrEqual() && totalDesire.getRight() >= dSync)
        {
            // sync right
            a = minOf(a, limitDeceleration(dummy, b));
        }
        // cooperate
        // TODO: get neighboring vehicles, their indicators, use car-following model with altered headway
        if (Math.random() > 0)
        {
            // cooperate left
            a = minOf(a, limitDeceleration(dummy, b));
        }
        if (Math.random() > 0)
        {
            // cooperate right
            a = minOf(a, limitDeceleration(dummy, b));
        }

        /*
         * Operational plan.
         */
        // TODO: Build the operational plan using minimum acceleration and including a possible lane change using
        // a
        // changeLeft/changeRight
        // LaneOperationalPlanBuilder
        return null;

    }

    protected Acceleration calculateAcceleration(final LaneBasedGTU follower, final HeadwayGTU leader, final double d)
    {
        // TODO: adjust desired headway based on desire
        // set T
        Acceleration a = calculateAcceleration(follower, leader);
        // reset T
        return a;
    }

    protected Acceleration calculateAcceleration(final LaneBasedGTU follower, final HeadwayGTU leader)
    {
        // TODO: speed limit
        // TODO: follower != self
        Speed speedLimit = new Speed(0, SpeedUnit.SI);
        return Acceleration.ZERO;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        String mandatory;
        try
        {
            mandatory = "mandatoryIncentives=" + getMandatoryIncentives() + ", ";
        }
        catch (OperationalPlanException ope)
        {
            // thrown if no mandatory incentives
            mandatory = "mandatoryIncentives=[]";
        }
        String voluntary;
        if (!getVoluntaryIncentives().isEmpty())
        {
            voluntary = "voluntaryIncentives=" + getVoluntaryIncentives();
        }
        else
        {
            voluntary = "voluntaryIncentives=[]";
        }
        return "LMRS [" + mandatory + voluntary + "]";
    }

}
