package org.opentrafficsim.road.gtu.lane.perception.mental.channel;

import java.util.Optional;

import org.djunits.value.vdouble.scalar.Duration;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeDouble;
import org.opentrafficsim.base.parameters.ParameterTypeDuration;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.base.parameters.constraint.NumericConstraint;
import org.opentrafficsim.core.gtu.Stateless;
import org.opentrafficsim.road.gtu.lane.perception.mental.BehavioralAdaptation;

/**
 * Behavioral adaptation which increases the update time (time step) for lower levels of attention (maximum in steady-state in
 * the Attention Matrix).
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class AdaptationUpdateTime implements BehavioralAdaptation, Stateless<AdaptationUpdateTime>
{

    /** Update time. */
    public static final ParameterTypeDuration DT = ParameterTypes.DT;

    /** Minimum update time. */
    public static final ParameterTypeDuration DT_MIN =
            new ParameterTypeDuration("dt_min", "Minimum update time.", Duration.ofSI(0.3), NumericConstraint.POSITIVEZERO)
            {
                @Override
                public void check(final Duration value, final Parameters params) throws ParameterException
                {
                    Optional<Duration> dtMax = params.getOptionalParameter(DT_MAX);
                    Throw.when(dtMax.isPresent() && value.si >= dtMax.get().si, ParameterException.class,
                            "Value of DT_MIN is above or equal to DT_MAX");
                }
            };

    /** Minimum update time. */
    public static final ParameterTypeDuration DT_MAX =
            new ParameterTypeDuration("dt_max", "Maximum update time.", Duration.ofSI(2.0), NumericConstraint.POSITIVE)
            {
                @Override
                public void check(final Duration value, final Parameters params) throws ParameterException
                {
                    Optional<Duration> dtMin = params.getOptionalParameter(DT_MIN);
                    Throw.when(dtMin.isPresent() && value.si <= dtMin.get().si, ParameterException.class,
                            "Value of DT_MAX is below or equal to DT_MIN");
                }
            };

    /** Level of attention, which is the maximum in the steady state of the Attention Matrix. */
    public static final ParameterTypeDouble ATT = ChannelFuller.ATT;

    /** Singleton instance. */
    public static final AdaptationUpdateTime SINGLETON = new AdaptationUpdateTime();

    /**
     * Constructor.
     */
    private AdaptationUpdateTime()
    {
        //
    }

    @Override
    public void adapt(final Parameters parameters) throws ParameterException
    {
        parameters.setClaimedParameter(DT, Duration.interpolate(parameters.getParameter(DT_MAX),
                parameters.getParameter(DT_MIN), parameters.getParameter(ATT)), this);
    }

    @Override
    public AdaptationUpdateTime get()
    {
        return SINGLETON;
    }

}
