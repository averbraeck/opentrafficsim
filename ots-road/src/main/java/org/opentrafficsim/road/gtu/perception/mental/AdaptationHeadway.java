package org.opentrafficsim.road.gtu.perception.mental;

import static org.opentrafficsim.base.parameters.constraint.NumericConstraint.POSITIVEZERO;

import org.djunits.value.vdouble.scalar.Duration;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeDouble;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;

/**
 * Behavioral adaptation which increases the desired headway to reduce task-demand.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class AdaptationHeadway extends FactorAdaptation
{

    /** Parameter for desired headway scaling. */
    public static final ParameterTypeDouble BETA_T =
            new ParameterTypeDouble("Beta_T", "max headway scaling", 1.0, POSITIVEZERO);

    /** Base value for the minimum desired headway. */
    private Duration t0Min;

    /** Base value for the maximum desired headway. */
    private Duration t0Max;

    /**
     * Constructor.
     */
    public AdaptationHeadway()
    {
        //
    }

    @Override
    public void adapt(final Parameters parameters) throws ParameterException
    {
        if (this.t0Min == null)
        {
            this.t0Min = parameters.getParameter(ParameterTypes.TMIN);
            this.t0Max = parameters.getParameter(ParameterTypes.TMAX);
        }
        double factor = getFactor(parameters, BETA_T);
        Duration tMin = this.t0Min.times(factor);
        Duration tMax = this.t0Max.times(factor);
        if (tMax.si <= parameters.getParameter(ParameterTypes.TMIN).si)
        {
            parameters.setClaimedParameter(ParameterTypes.TMIN, tMin, this);
            parameters.setClaimedParameter(ParameterTypes.TMAX, tMax, this);
        }
        else
        {
            parameters.setClaimedParameter(ParameterTypes.TMAX, tMax, this);
            parameters.setClaimedParameter(ParameterTypes.TMIN, tMin, this);
        }
    }

}
