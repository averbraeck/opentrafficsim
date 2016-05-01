package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import static org.opentrafficsim.core.gtu.behavioralcharacteristics.AbstractParameterType.Check.POSITIVE;
import static org.opentrafficsim.core.gtu.behavioralcharacteristics.AbstractParameterType.Check.UNITINTERVAL;

import java.util.HashSet;
import java.util.Set;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.opentrafficsim.core.Throw;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypeDouble;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypeDuration;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypes;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.lane.tactical.AbstractLaneBasedTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;

/**
 * Houses common functionality for LMRS models.
 * <ul>
 * <li>Set of mandatory lane change incentives, with check on having at least one.</li>
 * <li>Set of voluntary lane change incentives.</li>
 * <li>Static parameter type definitions for dfree, dsync and dcoop, which are LMRS specific parameters.</li>
 * <li>Method to get the minimum acceleration.</i>
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

    /** Set of mandatory lane change incentives. */
    private HashSet<MandatoryIncentive> mandatoryIncentives;

    /** Set of voluntary lane change incentives. */
    private HashSet<VoluntaryIncentive> voluntaryIncentives;

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
    public abstract Set<MandatoryIncentive> getDefaultMandatoryIncentives();

    /**
     * Returns a set of default voluntary incentives.
     * @return Set of default voluntary incentives.
     */
    public abstract Set<VoluntaryIncentive> getDefaultVoluntaryIncentives();

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
     * Limits the supplied acceleration to a given maximum deceleration.
     * @param a Acceleration to limit.
     * @param b Maximum deceleration to limit to.
     * @return Limited acceleration &ge;<tt>-b</tt>.
     */
    protected final Acceleration limitDeceleration(final Acceleration a, final Acceleration b)
    {
        if (a.si >= -b.si)
        {
            return a;
        }
        return new Acceleration(-b.si, AccelerationUnit.SI);
    }

    /**
     * Returns the minimum of two accelerations.
     * @param a1 First acceleration.
     * @param a2 Second acceleration.
     * @return Minimum of two accelerations.
     */
    public final Acceleration minOf(final Acceleration a1, final Acceleration a2)
    {
        return a1.lt(a2) ? a1 : a2;
    }

    /**
     * Returns a defensive copy of the mandatory incentives.
     * @return Defensive copy of the mandatory incentives.
     * @throws OperationalPlanException If there is no mandatory incentive. The model requires at least one.
     */
    public final Set<MandatoryIncentive> getMandatoryIncentives() throws OperationalPlanException
    {
        // Check existence of mandatory incentive
        if (this.mandatoryIncentives.isEmpty())
        {
            throw new OperationalPlanException("At the least the LMRS requires one mandatory lane change incentive.");
        }
        return new HashSet<MandatoryIncentive>(this.mandatoryIncentives);
    }

    /**
     * Returns a defensive copy of the voluntary incentives.
     * @return Defensive copy of the voluntary incentives.
     */
    public final Set<VoluntaryIncentive> getVoluntaryIncentives()
    {
        return new HashSet<VoluntaryIncentive>(this.voluntaryIncentives);
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

}
