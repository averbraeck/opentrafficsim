package org.opentrafficsim.road.gtu.lane.perception.mental;

import static org.opentrafficsim.base.parameters.constraint.NumericConstraint.POSITIVEZERO;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeDouble;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.road.gtu.lane.perception.mental.Fuller.BehavioralAdaptation;

/**
 * Behavioral adaptation which reduces the desired speed to reduce task-demand.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class AdaptationSpeed implements BehavioralAdaptation
{

    /** Parameter for desired speed scaling. */
    public static final ParameterTypeDouble BETA_V0 =
            new ParameterTypeDouble("Beta_v0", "max desired speed scaling", 1.0, POSITIVEZERO);

    /** Base value for the desired speed. */
    private Double fSpeed0;

    /** {@inheritDoc} */
    @Override
    public void adapt(final Parameters parameters, final double taskSaturation) throws ParameterException
    {
        if (this.fSpeed0 == null)
        {
            this.fSpeed0 = parameters.getParameter(ParameterTypes.FSPEED);
        }
        double eps = parameters.getParameter(Fuller.TS) - parameters.getParameter(Fuller.TS_CRIT);
        eps = eps < 0.0 ? 0.0 : (eps > 1.0 ? 1.0 : eps);
        double factor = 1.0 - parameters.getParameter(BETA_V0) * eps;
        parameters.setParameter(ParameterTypes.FSPEED, this.fSpeed0 * factor);
    }

}
