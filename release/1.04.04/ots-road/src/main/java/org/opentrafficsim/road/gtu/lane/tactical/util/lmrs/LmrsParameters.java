package org.opentrafficsim.road.gtu.lane.tactical.util.lmrs;

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
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 3 apr. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
@SuppressWarnings("checkstyle:interfaceistype")
public interface LmrsParameters
{

    /** Free lane change desire threshold. */
    ParameterTypeDouble DFREE =
            new ParameterTypeDouble("dFree", "Free lane change desire threshold", 0.365, ConstraintInterface.UNITINTERVAL)
            {
                /** */
                private static final long serialVersionUID = 20160413L;

                /** {@inheritDoc} */
                @Override
                public void check(final Double value, final Parameters params) throws ParameterException
                {
                    Double dSync = params.getParameterOrNull(DSYNC);
                    Throw.when(dSync != null && value >= dSync, ParameterException.class,
                            "Value of dFree is above or equal to dSync.");
                    Double dCoop = params.getParameterOrNull(DCOOP);
                    Throw.when(dCoop != null && value >= dCoop, ParameterException.class,
                            "Value of dFree is above or equal to dCoop.");
                }
            };

    /** Synchronized lane change desire threshold. */
    ParameterTypeDouble DSYNC = new ParameterTypeDouble("dSync", "Synchronized lane change desire threshold", 0.577,
            ConstraintInterface.UNITINTERVAL)
    {
        /** */
        private static final long serialVersionUID = 20160413L;

        /** {@inheritDoc} */
        @Override
        public void check(final Double value, final Parameters params) throws ParameterException
        {
            Double dFree = params.getParameterOrNull(DFREE);
            Throw.when(dFree != null && value <= dFree, ParameterException.class, "Value of dSync is below or equal to dFree.");
            Double dCoop = params.getParameterOrNull(DCOOP);
            Throw.when(dCoop != null && value >= dCoop, ParameterException.class, "Value of dSync is above or equal to dCoop.");
        }
    };

    /** Cooperative lane change desire threshold. */
    ParameterTypeDouble DCOOP = new ParameterTypeDouble("dCoop", "Cooperative lane change desire threshold", 0.788,
            ConstraintInterface.UNITINTERVAL)
    {
        /** */
        private static final long serialVersionUID = 20160413L;

        /** {@inheritDoc} */
        @Override
        public void check(final Double value, final Parameters params) throws ParameterException
        {
            Double dFree = params.getParameterOrNull(DFREE);
            Throw.when(dFree != null && value <= dFree, ParameterException.class, "Value of dCoop is below or equal to dFree.");
            Double dSync = params.getParameterOrNull(DSYNC);
            Throw.when(dSync != null && value <= dSync, ParameterException.class, "Value of dCoop is below or equal to dSync.");
        }
    };

    /** Current left lane change desire. */
    ParameterTypeDouble DLEFT = new ParameterTypeDouble("dLeft", "Left lane change desire", 0.0);

    /** Current right lane change desire. */
    ParameterTypeDouble DRIGHT = new ParameterTypeDouble("dRight", "Right lane change desire", 0.0);

    /** Lane change desire of current lane change. */
    ParameterTypeDouble DLC = new ParameterTypeDouble("dLaneChange", "Desire of current lane change", 0.0);

    /** Anticipation speed difference at full lane change desired. */
    ParameterTypeSpeed VGAIN =
            new ParameterTypeSpeed("vGain", "Anticipation speed difference at full lane change desire",
                    new Speed(69.6, SpeedUnit.KM_PER_HOUR), ConstraintInterface.POSITIVE);

    /** Courtesy parameter. */
    ParameterTypeDouble COURTESY = new ParameterTypeDouble("courtesy", "Courtesy level for courtesy lane changes", 1.0);

    /** Socio-speed sensitivity parameter. */
    ParameterTypeDouble SOCIO = new ParameterTypeDouble("socio", "Sensitivity level for speed of others",
            1.0, ConstraintInterface.UNITINTERVAL);

}
