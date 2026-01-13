package org.opentrafficsim.road.gtu.lane.perception.mental;

import static org.opentrafficsim.base.parameters.constraint.NumericConstraint.POSITIVEZERO;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeDouble;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.road.gtu.lane.perception.mental.channel.AdaptationSpeedChannel;

/**
 * Behavioral adaptation which reduces the desired speed to reduce task-demand. This implementation applies a linear reduction
 * based on task saturation. To use multiplicative adaptation by a standard factor see {@link AdaptationSpeedChannel}.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class AdaptationSpeed implements BehavioralAdaptation
{

    /** Parameter for desired speed scaling. */
    public static final ParameterTypeDouble BETA_V0 =
            new ParameterTypeDouble("Beta_v0", "max desired speed scaling", 1.0, POSITIVEZERO);

    /** Base value for the desired speed. */
    private Double fSpeed0;

    /**
     * Constructor.
     */
    public AdaptationSpeed()
    {
        //
    }

    @Override
    public void adapt(final Parameters parameters) throws ParameterException
    {
        if (this.fSpeed0 == null)
        {
            this.fSpeed0 = parameters.getParameter(ParameterTypes.FSPEED);
        }
        Double tsCrit = parameters.getParameterOrNull(SumFuller.TS_CRIT);
        double eps = parameters.getParameter(Fuller.TS) - (tsCrit == null ? 1.0 : tsCrit);
        eps = eps < 0.0 ? 0.0 : (eps >= 0.999 ? 0.999 : eps);
        double factor = 1.0 - parameters.getParameter(BETA_V0) * eps;
        parameters.setClaimedParameter(ParameterTypes.FSPEED, this.fSpeed0 * factor, this);
    }

}
