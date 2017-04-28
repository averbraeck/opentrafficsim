package org.opentrafficsim.road.gtu.lane.tactical.util.lmrs;

import static org.opentrafficsim.core.gtu.behavioralcharacteristics.AbstractParameterType.Check.UNITINTERVAL;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.AbstractParameterType;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypeDouble;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypeSpeed;

import nl.tudelft.simulation.language.Throw;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
    ParameterTypeDouble DFREE = new ParameterTypeDouble("dFree", "Free lane change desire threshold.", 0.365, UNITINTERVAL)
    {
        /** */
        private static final long serialVersionUID = 20160413L;

        public void check(final double value, final BehavioralCharacteristics bc) throws ParameterException
        {
            Throw.when(bc.contains(DSYNC) && value >= bc.getParameter(DSYNC), ParameterException.class,
                    "Value of dFree is above or equal to dSync.");
            Throw.when(bc.contains(DCOOP) && value >= bc.getParameter(DCOOP), ParameterException.class,
                    "Value of dFree is above or equal to dCoop.");
        }
    };

    /** Synchronized lane change desire threshold. */
    ParameterTypeDouble DSYNC =
            new ParameterTypeDouble("dSync", "Synchronized lane change desire threshold.", 0.577, UNITINTERVAL)
            {
                /** */
                private static final long serialVersionUID = 20160413L;

                public void check(final double value, final BehavioralCharacteristics bc) throws ParameterException
                {
                    Throw.when(bc.contains(DFREE) && value <= bc.getParameter(DFREE), ParameterException.class,
                            "Value of dSync is below or equal to dFree.");
                    Throw.when(bc.contains(DCOOP) && value >= bc.getParameter(DCOOP), ParameterException.class,
                            "Value of dSync is above or equal to dCoop.");
                }
            };

    /** Cooperative lane change desire threshold. */
    ParameterTypeDouble DCOOP =
            new ParameterTypeDouble("dCoop", "Cooperative lane change desire threshold.", 0.788, UNITINTERVAL)
            {
                /** */
                private static final long serialVersionUID = 20160413L;

                public void check(final double value, final BehavioralCharacteristics bc) throws ParameterException
                {
                    Throw.when(bc.contains(DFREE) && value <= bc.getParameter(DFREE), ParameterException.class,
                            "Value of dCoop is below or equal to dFree.");
                    Throw.when(bc.contains(DSYNC) && value <= bc.getParameter(DSYNC), ParameterException.class,
                            "Value of dCoop is below or equal to dSync.");
                }
            };

    /** Current left lane change desire. */
    ParameterTypeDouble DLEFT = new ParameterTypeDouble("dLeft", "Left lane change desire.", 0.0);

    /** Current right lane change desire. */
    ParameterTypeDouble DRIGHT = new ParameterTypeDouble("dRight", "Right lane change desire.", 0.0);

    /** Lane change desire of current lane change. */
    ParameterTypeDouble DLC = new ParameterTypeDouble("dLaneChange", "Desire of current lane change.", 0.0);

    /** Anticipation speed difference at full lane change desired. */
    ParameterTypeSpeed VGAIN =
            new ParameterTypeSpeed("vGain", "Anticipation speed difference at " + "full lane change desired.",
                    new Speed(69.6, SpeedUnit.KM_PER_HOUR), AbstractParameterType.Check.POSITIVE);

    /** Courtesy parameter. */
    ParameterTypeDouble COURTESY = new ParameterTypeDouble("Courtesy", "Courtesy level for courtesy lane changes.", 1.0);

    /** Hierarchy parameter. */
    ParameterTypeDouble HIERARCHY = new ParameterTypeDouble("Hierarchy", "Hierarchy level for hierarchal lane changes.", 1.0);

}
