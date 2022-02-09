package org.opentrafficsim.road.gtu.lane.perception.mental;

import static org.opentrafficsim.base.parameters.constraint.NumericConstraint.POSITIVE;
import static org.opentrafficsim.base.parameters.constraint.NumericConstraint.POSITIVEZERO;

import org.djunits.value.vdouble.scalar.Duration;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeDouble;
import org.opentrafficsim.base.parameters.ParameterTypeDuration;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.road.gtu.lane.perception.mental.Fuller.BehavioralAdaptation;

/**
 * Behavioral adaptation which sets parameters for situational awareness and reaction time.
 * 
 * <pre>
 *      / SA_MAX,                                                                         taskSaturation &lt; TS_CRIT
 * SA = | SA_MAX - (SA_MAX - SA_MIN) * (taskSaturation - TS_CRIT) / (TS_MAX - TS_CRIT),   TS_CRIT &lt;= taskSaturation &lt; TS_MAX 
 *      \ SA_MIN,                                                                         taskSaturation &gt;= TS_MAX
 * 
 * TR = (S_MAX - SA) * TR_MAX
 * </pre>
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 5 apr. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class AdaptationSituationalAwareness implements BehavioralAdaptation
{

    /** Situational awareness. */
    public static final ParameterTypeDouble SA = new ParameterTypeDouble("SA", "Situational awareness", 1.0, POSITIVEZERO);

    /** Minimum situational awareness. */
    public static final ParameterTypeDouble SA_MIN =
            new ParameterTypeDouble("SAmin", "Min. situational awareness", 0.5, POSITIVE)
            {
                /** */
                private static final long serialVersionUID = 20180403L;

                /** {@inheritDoc} */
                @Override
                public void check(final Double value, final Parameters params) throws ParameterException
                {
                    Double saMax = params.getParameterOrNull(SA_MAX);
                    Throw.when(saMax != null && value > saMax, ParameterException.class,
                            "Value for SA_MIN should not be larger than SA_MAX.");
                }
            };

    /** Maximum situational awareness. */
    public static final ParameterTypeDouble SA_MAX =
            new ParameterTypeDouble("SAmax", "Max. situational awareness", 1.0, POSITIVE)
            {
                /** */
                private static final long serialVersionUID = 20180403L;

                /** {@inheritDoc} */
                @Override
                public void check(final Double value, final Parameters params) throws ParameterException
                {
                    Double saMin = params.getParameterOrNull(SA_MIN);
                    Throw.when(saMin != null && value < saMin, ParameterException.class,
                            "Value for SA_MAX should not be larger than SA_MIN.");
                }
            };

    /** Maximum reaction time at 0 situational awareness. */
    public static final ParameterTypeDuration TR_MAX =
            new ParameterTypeDuration("TRmax", "Maximum reaction time", Duration.instantiateSI(2.0), POSITIVE);

    /** {@inheritDoc} */
    @Override
    public void adapt(final Parameters parameters, final double taskSaturation) throws ParameterException
    {
        // situational awareness
        double tsCrit = parameters.getParameter(Fuller.TS_CRIT);
        double tsMax = parameters.getParameter(Fuller.TS_MAX);
        double saMin = parameters.getParameter(SA_MIN);
        double saMax = parameters.getParameter(SA_MAX);
        double sa = taskSaturation < tsCrit ? saMax
                : (taskSaturation >= tsMax ? saMin : saMax - (saMax - saMin) * (taskSaturation - tsCrit) / (tsMax - tsCrit));
        parameters.setParameter(SA, sa);
        // reaction time
        parameters.setParameter(ParameterTypes.TR, parameters.getParameter(TR_MAX).times(saMax - sa));
    }

}
