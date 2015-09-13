package org.opentrafficsim.core.dsol;

import nl.tudelft.simulation.dsol.simulators.DEVSRealTimeClock;

import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.OTS_SCALAR;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$,
 *          initial version Aug 15, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class OTSDEVSRealTimeClock extends
    DEVSRealTimeClock<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> implements
    OTSDEVSSimulatorInterface, OTSAnimatorInterface, OTS_SCALAR
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
    protected final Time.Rel relativeMillis(final double factor)
    {
        return new Time.Rel(factor, MILLISECOND);
    }
}
