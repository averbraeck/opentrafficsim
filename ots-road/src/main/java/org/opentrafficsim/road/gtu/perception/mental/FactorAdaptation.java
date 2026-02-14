package org.opentrafficsim.road.gtu.perception.mental;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeDouble;
import org.opentrafficsim.base.parameters.Parameters;

/**
 * Super class for behavioral adaptations using a factor based task saturation. The factor is defined as max(1, 1 + beta * (ts -
 * tsCrit)), where beta is the behavioral adaptation scaling, ts is task saturation, and tsCrit is the critical task saturation.
 * The latter does not need to be specified, in which case a value of 1 is used.
 * <p>
 * Copyright (c) 2024-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public abstract class FactorAdaptation implements BehavioralAdaptation
{

    /**
     * Returns factor for behavioral adaptation. This is given by factor = max(1, 1 + beta * (ts - tsCrit)), where ts is the
     * task saturation and tsCrit is the critical task saturation (assumed 1.0 if not given in the parameters). For behavioral
     * adaptations that reduce something the effective factor can be used as 1.0 / factor.
     * @param parameters parameters
     * @param beta behavioral adaptation scaling parameter, assumed non-negative
     * @return factor for behavioral adaptation
     * @throws ParameterException if a used parameter (other than tsCrit) is not given
     */
    protected double getFactor(final Parameters parameters, final ParameterTypeDouble beta) throws ParameterException
    {
        double ts = parameters.getParameter(Fuller.TS);
        double tsCrit = parameters.contains(SumFuller.TS_CRIT) ? parameters.getParameter(SumFuller.TS_CRIT) : 1.0;
        return ts < tsCrit ? 1.0 : Math.max(0, 1.0 + parameters.getParameter(beta) * (ts - tsCrit));
    }

}
