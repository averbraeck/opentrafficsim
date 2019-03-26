package org.opentrafficsim.road.gtu.lane.control;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeDuration;
import org.opentrafficsim.base.parameters.ParameterTypeLength;
import org.opentrafficsim.base.parameters.constraint.NumericConstraint;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.LongitudinalControllerPerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTU;

/**
 * Simple linear CACC controller.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Mar 12, 2019 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public abstract class AbstractActuatedControl implements LongitudinalControl
{

    /** Time headway setting for ACC mode. */
    public static final ParameterTypeDuration TDACC = new ParameterTypeDuration("td ACC",
            "User defined time headway in ACC mode.", Duration.createSI(1.2), NumericConstraint.POSITIVE);

    /** Time headway setting for CACC mode. */
    public static final ParameterTypeDuration TDCACC = new ParameterTypeDuration("td CACC",
            "User defined time headway in CACC mode.", Duration.createSI(0.5), NumericConstraint.POSITIVE);

    /** (C)ACC stopping distance. */
    public static final ParameterTypeLength X0 =
            new ParameterTypeLength("x0 (C)ACC", "stopping distance (C)ACC", Length.createSI(3.0), NumericConstraint.POSITIVE);

    /** Delayed actuation. */
    private final DelayedActuation delayedActuation;

    /**
     * Constructor using default sensors with no delay.
     * @param delayedActuation DelayedActuation; delayed actuation
     */
    public AbstractActuatedControl(final DelayedActuation delayedActuation)
    {
        this.delayedActuation = delayedActuation;
    }

    /**
     * Delays the actuation of acceleration.
     * @param desiredAcceleration Acceleration; desired acceleration
     * @param gtu LaneBasedGTU; gtu
     * @return Acceleration; delayed acceleration
     */
    public Acceleration delayActuation(final Acceleration desiredAcceleration, final LaneBasedGTU gtu)
    {
        return this.delayedActuation.delayActuation(desiredAcceleration, gtu);
    }

    /** {@inheritDoc} */
    @Override
    public Acceleration getAcceleration(final LaneBasedGTU gtu)
    {
        try
        {
            PerceptionCollectable<HeadwayGTU, LaneBasedGTU> leaders = gtu.getTacticalPlanner().getPerception()
                    .getPerceptionCategory(LongitudinalControllerPerception.class).getLeaders();
            return this.delayedActuation.delayActuation(getDesiredAcceleration(gtu, leaders), gtu);
        }
        catch (OperationalPlanException exception)
        {
            throw new RuntimeException("Missing perception category LongitudinalControllerPerception", exception);
        }
        catch (ParameterException exception)
        {
            throw new RuntimeException("Missing parameter", exception);
        }
    }

    /**
     * Returns the desired acceleration from the longitudinal control.
     * @param gtu LaneBasedGTU; gtu
     * @param leaders PerceptionCollectable&lt;HeadwayGTU, LaneBasedGTU&gt;; leaders
     * @return Acceleration; desired acceleration
     * @throws ParameterException if parameter is not present
     */
    public abstract Acceleration getDesiredAcceleration(LaneBasedGTU gtu,
            PerceptionCollectable<HeadwayGTU, LaneBasedGTU> leaders) throws ParameterException;

}
