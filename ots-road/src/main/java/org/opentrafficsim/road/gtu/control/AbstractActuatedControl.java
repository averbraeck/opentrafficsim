package org.opentrafficsim.road.gtu.control;

import java.util.Optional;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.base.OtsRuntimeException;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeDuration;
import org.opentrafficsim.base.parameters.ParameterTypeLength;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.base.parameters.constraint.NumericConstraint;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.LaneBasedGtu;
import org.opentrafficsim.road.gtu.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.perception.categories.neighbors.LongitudinalControllerPerception;
import org.opentrafficsim.road.gtu.perception.object.PerceivedGtu;

/**
 * Simple linear CACC controller.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public abstract class AbstractActuatedControl implements LongitudinalControl
{

    /** Time headway setting for ACC mode. */
    public static final ParameterTypeDuration TDACC = new ParameterTypeDuration("td ACC",
            "User defined time headway in ACC mode", Duration.ofSI(1.2), NumericConstraint.POSITIVE);

    /** Time headway setting for CACC mode. */
    public static final ParameterTypeDuration TDCACC = new ParameterTypeDuration("td CACC",
            "User defined time headway in CACC mode", Duration.ofSI(0.5), NumericConstraint.POSITIVE);

    /** (C)ACC stopping distance. */
    public static final ParameterTypeLength X0 =
            new ParameterTypeLength("x0 (C)ACC", "Stopping distance (C)ACC", Length.ofSI(3.0), NumericConstraint.POSITIVE);

    /** Delayed actuation. */
    private final DelayedActuation delayedActuation;

    /**
     * Constructor using default sensors with no delay.
     * @param delayedActuation delayed actuation
     */
    public AbstractActuatedControl(final DelayedActuation delayedActuation)
    {
        this.delayedActuation = delayedActuation;
    }

    /**
     * Delays the actuation of acceleration.
     * @param desiredAcceleration desired acceleration
     * @param gtu gtu
     * @return delayed acceleration
     */
    public Acceleration delayActuation(final Acceleration desiredAcceleration, final LaneBasedGtu gtu)
    {
        return this.delayedActuation.delayActuation(desiredAcceleration, gtu);
    }

    @Override
    public Optional<Acceleration> getAcceleration(final LaneBasedGtu gtu, final Parameters settings)
    {
        try
        {
            PerceptionCollectable<PerceivedGtu, LaneBasedGtu> leaders = gtu.getTacticalPlanner().getPerception()
                    .getPerceptionCategory(LongitudinalControllerPerception.class).getLeaders();
            return Optional.ofNullable(delayActuation(getDesiredAcceleration(gtu, leaders, settings), gtu));
        }
        catch (OperationalPlanException exception)
        {
            throw new OtsRuntimeException("Missing perception category LongitudinalControllerPerception", exception);
        }
        catch (ParameterException exception)
        {
            throw new OtsRuntimeException("Missing parameter", exception);
        }
    }

    /**
     * Returns the desired acceleration from the longitudinal control.
     * @param gtu gtu
     * @param leaders leaders
     * @param settings system settings
     * @return desired acceleration
     * @throws ParameterException if parameter is not present
     */
    public abstract Acceleration getDesiredAcceleration(LaneBasedGtu gtu,
            PerceptionCollectable<PerceivedGtu, LaneBasedGtu> leaders, Parameters settings) throws ParameterException;

}
