package org.opentrafficsim.road.gtu.perception.mental;

import static org.opentrafficsim.base.parameters.constraint.NumericConstraint.POSITIVEZERO;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeDouble;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.Stateless;
import org.opentrafficsim.road.gtu.tactical.util.lmrs.LmrsParameters;

/**
 * Reduces the voluntary lane change desire as behavioral adaptation by setting lambda_v (applied by the LMRS on voluntary lane
 * change desire). The equation is lambda_v = min(1, 1/(1+Beta_d*(TS-1))).
 * <p>
 * Copyright (c) 2024-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class AdaptationLaneChangeDesire extends FactorAdaptation implements Stateless<AdaptationLaneChangeDesire>
{

    /** Parameter for desired speed scaling. */
    public static final ParameterTypeDouble BETA_D =
            new ParameterTypeDouble("Beta_d", "voluntary lane change desire scaling", 1.0, POSITIVEZERO);

    /** Singleton instance. */
    public static final AdaptationLaneChangeDesire SINGLETON = new AdaptationLaneChangeDesire();

    /**
     * Constructor.
     */
    private AdaptationLaneChangeDesire()
    {
        //
    }

    @Override
    public void adapt(final Parameters parameters) throws ParameterException
    {
        parameters.setClaimedParameter(LmrsParameters.LAMBDA_V, 1.0 / getFactor(parameters, BETA_D), this);
    }

    @Override
    public AdaptationLaneChangeDesire get()
    {
        return SINGLETON;
    }

}
