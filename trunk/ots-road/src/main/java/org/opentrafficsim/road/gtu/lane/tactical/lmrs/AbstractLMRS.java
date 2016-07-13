package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import static org.opentrafficsim.core.gtu.behavioralcharacteristics.AbstractParameterType.Check.POSITIVE;
import static org.opentrafficsim.core.gtu.behavioralcharacteristics.AbstractParameterType.Check.UNITINTERVAL;

import java.util.LinkedHashSet;
import java.util.Set;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.Throw;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypeDouble;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypeDuration;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypes;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.tactical.AbstractLaneBasedTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.following.AbstractCarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.util.SpeedLimitUtil;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;
import org.opentrafficsim.road.network.speed.SpeedLimitProspect;
import org.opentrafficsim.road.network.speed.SpeedLimitType;
import org.opentrafficsim.road.network.speed.SpeedLimitTypes;

/**
 * Houses common functionality for LMRS models.
 * <ul>
 * <li>Set of mandatory lane change incentives, with check on having at least one.</li>
 * <li>Set of voluntary lane change incentives.</li>
 * <li>Static parameter type definitions for dfree, dsync and dcoop, which are LMRS specific parameters.</li>
 * <li>Method to get the minimum acceleration.</li>
 * </ul>
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 13, 2016 <br>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public abstract class AbstractLMRS extends AbstractLaneBasedTacticalPlanner
{

    /** Serialization id. */
    private static final long serialVersionUID = 20160413L;

    /** Fixed model time step. */
    public static final ParameterTypeDuration DT = new ParameterTypeDuration("dt", "Fixed model time step.", new Duration(
        0.5, TimeUnit.SI), POSITIVE);

    /** Free lane change desire threshold. */
    public static final ParameterTypeDouble DFREE = new ParameterTypeDouble("dFree", "Free lane change desire threshold.",
        0.365, UNITINTERVAL)
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
    public static final ParameterTypeDouble DSYNC = new ParameterTypeDouble("dSync",
        "Synchronized lane change desire threshold.", 0.577, UNITINTERVAL)
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
    public static final ParameterTypeDouble DCOOP = new ParameterTypeDouble("dCoop",
        "Cooperative lane change desire threshold.", 0.788, UNITINTERVAL)
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

    /** Set of mandatory lane change incentives. */
    private final Set<MandatoryIncentive> mandatoryIncentives = new LinkedHashSet<>();

    /** Set of voluntary lane change incentives. */
    private final Set<VoluntaryIncentive> voluntaryIncentives = new LinkedHashSet<>();;

    /**
     * Constructor setting the car-following model.
     * @param carFollowingModel Car-following model.
     */
    public AbstractLMRS(final CarFollowingModel carFollowingModel)
    {
        super(carFollowingModel);
    }

    /**
     * Adds a mandatory incentive. Ignores <tt>null</tt>.
     * @param incentive Incentive to add.
     */
    public final void addMandatoryIncentive(final MandatoryIncentive incentive)
    {
        if (incentive != null)
        {
            this.mandatoryIncentives.add(incentive);
        }
    }

    /**
     * Adds a voluntary incentive. Ignores <tt>null</tt>.
     * @param incentive Incentive to add.
     */
    public final void addVoluntaryIncentive(final VoluntaryIncentive incentive)
    {
        if (incentive != null)
        {
            this.voluntaryIncentives.add(incentive);
        }
    }

    /**
     * Sets the default lane change incentives.
     */
    public final void setDefaultIncentives()
    {
        this.mandatoryIncentives.clear();
        this.voluntaryIncentives.clear();
        this.mandatoryIncentives.addAll(getDefaultMandatoryIncentives());
        this.voluntaryIncentives.addAll(getDefaultVoluntaryIncentives());
    }

    /**
     * Returns a set of default mandatory incentives.
     * @return Set of default mandatory incentives.
     */
    public abstract LinkedHashSet<MandatoryIncentive> getDefaultMandatoryIncentives();

    /**
     * Returns a set of default voluntary incentives.
     * @return Set of default voluntary incentives.
     */
    public abstract LinkedHashSet<VoluntaryIncentive> getDefaultVoluntaryIncentives();

    /**
     * Disables lane changes by clearing all incentives and setting a dummy incentive as mandatory incentive.
     */
    public final void disableLaneChanges()
    {
        this.mandatoryIncentives.clear();
        this.voluntaryIncentives.clear();
        this.mandatoryIncentives.add(new IncentiveDummy());
    }

    /**
     * Returns a defensive copy of the mandatory incentives.
     * @return defensive copy of the mandatory incentives.
     * @throws GTUException if there is no mandatory incentive, the model requires at least one
     */
    public final Set<MandatoryIncentive> getMandatoryIncentives() throws GTUException
    {
        Throw.when(this.mandatoryIncentives.isEmpty(), GTUException.class,
            "At the least the LMRS requires one mandatory lane change incentive.");
        return new LinkedHashSet<>(this.mandatoryIncentives);
    }

    /**
     * Returns a defensive copy of the voluntary incentives.
     * @return Defensive copy of the voluntary incentives.
     */
    public final Set<VoluntaryIncentive> getVoluntaryIncentives()
    {
        return new LinkedHashSet<>(this.voluntaryIncentives);
    }

    /**
     * Updates the desired headway following an exponential shape approximated with fixed time step <tt>DT</tt>.
     * @param bc Behavioral characteristics.
     * @throws ParameterException In case of a parameter exception.
     */
    protected final void exponentialHeadwayRelaxation(final BehavioralCharacteristics bc) throws ParameterException
    {
        double ratio = bc.getParameter(DT).si / bc.getParameter(ParameterTypes.TAU).si;
        bc.setParameter(ParameterTypes.T, Duration.interpolate(bc.getParameter(ParameterTypes.T), bc
            .getParameter(ParameterTypes.TMAX), ratio <= 1.0 ? ratio : 1.0));
    }

    /**
     * Determines lane change desire for the given RSU. Mandatory desire is deduced as the maximum of a set of mandatory
     * incentives, while voluntary desires are added. Depending on the level of mandatory lane change desire, voluntary desire
     * may be included partially. If both are positive or negative, voluntary desire is fully included. Otherwise, voluntary
     * desire is less considered within the range dSync &lt; |mandatory| &lt; dCoop. The absolute value is used as large
     * negative mandatory desire may also dominate voluntary desire.
     * @param gtu gtu to determine desire for
     * @return lane change desire for gtu
     * @throws ParameterException if a parameter is not defined
     * @throws GTUException if there is no mandatory incentive, the model requires at least one
     */
    protected final Desire getLaneChangeDesire(final LaneBasedGTU gtu) throws ParameterException, GTUException
    {

        BehavioralCharacteristics bc = gtu.getBehavioralCharacteristics();
        double dSync = bc.getParameter(DSYNC);
        double dCoop = bc.getParameter(DCOOP);

        // Mandatory desire
        double dLeftMandatory = Double.NEGATIVE_INFINITY;
        double dRightMandatory = Double.NEGATIVE_INFINITY;
        Desire mandatoryDesire = new Desire(dLeftMandatory, dRightMandatory);
        for (MandatoryIncentive incentive : getMandatoryIncentives())
        {
            Desire d = incentive.determineDesire(gtu, mandatoryDesire);
            dLeftMandatory = d.getLeft() > dLeftMandatory ? d.getLeft() : dLeftMandatory;
            dRightMandatory = d.getRight() > dRightMandatory ? d.getRight() : dRightMandatory;
            mandatoryDesire = new Desire(dLeftMandatory, dRightMandatory);
        }
        
        // Voluntary desire
        double dLeftVoluntary = 0;
        double dRightVoluntary = 0;
        Desire voluntaryDesire = new Desire(dLeftVoluntary, dRightVoluntary);
        for (VoluntaryIncentive incentive : getVoluntaryIncentives())
        {
            Desire d = incentive.determineDesire(gtu, mandatoryDesire, voluntaryDesire);
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
     * Acceleration for speed limit transitions. This implementation decelerates before curves and speed bumps. For this it uses
     * {@code approachTargetSpeed()} of the abstract car-following model implementation. All remaining transitions happen in the
     * default manner, i.e. deceleration and acceleration after the speed limit change and governed by the car-following model.
     * @param behavioralCharacteristics behavioral characteristics
     * @param speed current speed
     * @param speedLimitProspect speed limit prospect
     * @param carFollowingModel car following model
     * @return acceleration for speed limit transitions
     * @throws ParameterException if a required parameter is not found
     */
    protected final Acceleration considerSpeedLimitTransitions(final BehavioralCharacteristics behavioralCharacteristics,
        final Speed speed, final SpeedLimitProspect speedLimitProspect, final CarFollowingModel carFollowingModel)
        throws ParameterException
    {
        Acceleration out = new Acceleration(Double.POSITIVE_INFINITY, AccelerationUnit.SI);
        SpeedLimitInfo currentSpeedLimitInfo = speedLimitProspect.getSpeedLimitInfo(Length.ZERO);

        // decelerate for curves and speed bumps
        for (SpeedLimitType<?> speedLimitType : new SpeedLimitType[] {SpeedLimitTypes.CURVATURE, SpeedLimitTypes.SPEED_BUMP})
        {
            for (Length distance : speedLimitProspect.getDownstreamDistances(speedLimitType))
            {
                SpeedLimitInfo speedLimitInfo = speedLimitProspect.buildSpeedLimitInfo(distance, speedLimitType);
                Speed targetSpeed = carFollowingModel.desiredSpeed(behavioralCharacteristics, speedLimitInfo);
                Acceleration a =
                    SpeedLimitUtil.approachTargetSpeed(carFollowingModel, behavioralCharacteristics, speed,
                        currentSpeedLimitInfo, distance, targetSpeed);
                if (a.lt(out))
                {
                    out = a;
                }
            }
        }

        // For lower legal speed limits (road class, fixed sign, dynamic sign), we assume that the car-following model will
        // apply some reasonable deceleration after the change. For higher speed limits, we assume car-following acceleration
        // after the change.

        return out;
    }

}
