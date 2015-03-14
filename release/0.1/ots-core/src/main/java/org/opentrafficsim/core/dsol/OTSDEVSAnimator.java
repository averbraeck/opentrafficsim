package org.opentrafficsim.core.dsol;

import nl.tudelft.simulation.dsol.simulators.DEVSAnimator;

import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Aug 15, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class OTSDEVSAnimator extends DEVSAnimator<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble>
    implements OTSDEVSSimulatorInterface, OTSAnimatorInterface
{
    /** */
    private static final long serialVersionUID = 20140909L;

    /**
     * Create a new OTSDEVSAnimator..
     */
    public OTSDEVSAnimator()
    {
        super();
    }
}
