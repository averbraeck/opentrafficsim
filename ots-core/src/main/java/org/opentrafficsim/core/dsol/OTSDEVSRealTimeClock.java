package org.opentrafficsim.core.dsol;

import nl.tudelft.simulation.dsol.simulators.DEVSRealTimeClock;

import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * 
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$,
 *          initial version Aug 15, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class OTSDEVSRealTimeClock extends
    DEVSRealTimeClock<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> implements
    OTSDEVSSimulatorInterface, OTSAnimatorInterface
{
    /** */
    private static final long serialVersionUID = 20140909L;

    /**
     * Create a new OTSRealTimeClock.
     */
    public OTSDEVSRealTimeClock()
    {
        super();
    }

    /** {@inheritDoc} */
    @Override
    protected final DoubleScalar.Rel<TimeUnit> relativeMillis(final double factor)
    {
        return new DoubleScalar.Rel<TimeUnit>(factor, TimeUnit.MILLISECOND);
    }
}
