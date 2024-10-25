package org.opentrafficsim.road.gtu.lane.perception.mental;

import static org.opentrafficsim.base.parameters.constraint.NumericConstraint.POSITIVEZERO;

import org.djunits.value.vdouble.scalar.Duration;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeDouble;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.road.gtu.lane.perception.mental.Fuller.BehavioralAdaptation;

/**
 * Behavioral adaptation which increases the desired headway to reduce task-demand.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class AdaptationHeadway implements BehavioralAdaptation
{

    /** Parameter for desired headway scaling. */
    public static final ParameterTypeDouble BETA_T =
            new ParameterTypeDouble("Beta_T", "max headway scaling", 1.0, POSITIVEZERO);

    /** Base value for the minimum desired headway. */
    private Duration t0Min;

    /** Base value for the maximum desired headway. */
    private Duration t0Max;

    @Override
    public void adapt(final Parameters parameters, final double taskSaturation) throws ParameterException
    {
        if (this.t0Min == null)
        {
            this.t0Min = parameters.getParameterOrNull(ParameterTypes.TMIN);
            this.t0Max = parameters.getParameterOrNull(ParameterTypes.TMAX);
        }
        double eps = parameters.getParameter(Fuller.TS) - parameters.getParameter(Fuller.TS_CRIT);
        eps = eps < 0.0 ? 0.0 : (eps > 1.0 ? 1.0 : eps);
        double factor = 1.0 + parameters.getParameter(BETA_T) * eps;
        Duration tMin = this.t0Min.times(factor);
        Duration tMax = this.t0Max.times(factor);
        if (tMax.si <= parameters.getParameter(ParameterTypes.TMIN).si)
        {
            parameters.setParameter(ParameterTypes.TMIN, tMin);
            parameters.setParameter(ParameterTypes.TMAX, tMax);
        }
        else
        {
            parameters.setParameter(ParameterTypes.TMAX, tMax);
            parameters.setParameter(ParameterTypes.TMIN, tMin);
        }
    }

}
