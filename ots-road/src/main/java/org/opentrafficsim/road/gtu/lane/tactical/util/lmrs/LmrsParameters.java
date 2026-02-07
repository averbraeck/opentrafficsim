package org.opentrafficsim.road.gtu.lane.tactical.util.lmrs;

import java.util.Optional;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeDouble;
import org.opentrafficsim.base.parameters.ParameterTypeSpeed;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.base.parameters.constraint.ConstraintInterface;

/**
 * Interface with LMRS parameters.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
@SuppressWarnings("checkstyle:interfaceistype")
public interface LmrsParameters
{

    /** Free lane change desire threshold. */
    // @docs/06-behavior/parameters.md
    ParameterTypeDouble DFREE =
            new ParameterTypeDouble("dFree", "Free lane change desire threshold", 0.365, ConstraintInterface.UNITINTERVAL)
            {
                @Override
                public void check(final Double value, final Parameters params) throws ParameterException
                {
                    // @docs/06-behavior/parameters.md
                    Optional<Double> dSync = params.getOptionalParameter(DSYNC);
                    Throw.when(dSync.isPresent() && value >= dSync.get(), ParameterException.class,
                            "Value of dFree is above or equal to dSync.");
                    // @end
                    Optional<Double> dCoop = params.getOptionalParameter(DCOOP);
                    Throw.when(dCoop.isPresent() && value >= dCoop.get(), ParameterException.class,
                            "Value of dFree is above or equal to dCoop.");
                }
            };

    /** Synchronized lane change desire threshold. */
    // @docs/06-behavior/parameters.md
    ParameterTypeDouble DSYNC = new ParameterTypeDouble("dSync", "Synchronized lane change desire threshold", 0.577,
            ConstraintInterface.UNITINTERVAL)
    {
        @Override
        public void check(final Double value, final Parameters params) throws ParameterException
        {
            // @docs/06-behavior/parameters.md
            Optional<Double> dFree = params.getOptionalParameter(DFREE);
            Throw.when(dFree.isPresent() && value <= dFree.get(), ParameterException.class,
                    "Value of dSync is below or equal to dFree.");
            // @end
            Optional<Double> dCoop = params.getOptionalParameter(DCOOP);
            Throw.when(dCoop.isPresent() && value >= dCoop.get(), ParameterException.class,
                    "Value of dSync is above or equal to dCoop.");
        }
    };

    /** Cooperative lane change desire threshold. */
    ParameterTypeDouble DCOOP = new ParameterTypeDouble("dCoop", "Cooperative lane change desire threshold", 0.788,
            ConstraintInterface.UNITINTERVAL)
    {
        @Override
        public void check(final Double value, final Parameters params) throws ParameterException
        {
            Optional<Double> dFree = params.getOptionalParameter(DFREE);
            Throw.when(dFree.isPresent() && value <= dFree.get(), ParameterException.class,
                    "Value of dCoop is below or equal to dFree.");
            Optional<Double> dSync = params.getOptionalParameter(DSYNC);
            Throw.when(dSync.isPresent() && value <= dSync.get(), ParameterException.class,
                    "Value of dCoop is below or equal to dSync.");
        }
    };

    /** Current left lane change desire. */
    ParameterTypeDouble DLEFT = new ParameterTypeDouble("dLeft", "Left lane change desire", 0.0);

    /** Current right lane change desire. */
    ParameterTypeDouble DRIGHT = new ParameterTypeDouble("dRight", "Right lane change desire", 0.0);

    /** Lane change desire of current lane change. */
    ParameterTypeDouble DLC = new ParameterTypeDouble("dLaneChange", "Desire of current lane change", 0.0);

    /** Anticipation speed difference at full lane change desired. */
    ParameterTypeSpeed VGAIN = new ParameterTypeSpeed("vGain", "Anticipation speed difference at full lane change desire",
            new Speed(69.6, SpeedUnit.KM_PER_HOUR), ConstraintInterface.POSITIVE);

    /** Socio-speed sensitivity parameter. */
    ParameterTypeDouble SOCIO =
            new ParameterTypeDouble("socio", "Sensitivity level for speed of others", 1.0, ConstraintInterface.UNITINTERVAL);

    /** Behavioral adaptation factor on voluntary lane change desire. */
    ParameterTypeDouble LAMBDA_V = new ParameterTypeDouble("lambda_v",
            "Factor on voluntary lane change desire due to behavioral adaptation", 1.0, ConstraintInterface.POSITIVEZERO);

}
