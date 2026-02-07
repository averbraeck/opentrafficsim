package org.opentrafficsim.road.gtu.lane.perception.mental.channel;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeDouble;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.road.gtu.lane.perception.mental.AdaptationSpeed;
import org.opentrafficsim.road.gtu.lane.perception.mental.FactorAdaptation;

/**
 * Reduces the desired speed as behavioral adaptation. The equation is v0 = v0_base * min(1, 1/(1+Beta_v0*(TS-1))).
 * <p>
 * Copyright (c) 2024-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class AdaptationSpeedChannel extends FactorAdaptation
{

    /** Parameter for desired speed scaling. */
    public static final ParameterTypeDouble BETA_V0 = AdaptationSpeed.BETA_V0;

    /** Base value for the desired speed. */
    private Double fSpeed0;

    /**
     * Constructor.
     */
    public AdaptationSpeedChannel()
    {
        //
    }

    /** {@inheritDoc} */
    @Override
    public void adapt(final Parameters parameters) throws ParameterException
    {
        if (this.fSpeed0 == null)
        {
            this.fSpeed0 = parameters.getParameter(ParameterTypes.FSPEED);
        }
        parameters.setClaimedParameter(ParameterTypes.FSPEED, this.fSpeed0 / getFactor(parameters, BETA_V0), this);
    }

}
