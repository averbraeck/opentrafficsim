package org.opentrafficsim.imb.simulators;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.imb.observers.IMBTransmitter;
import org.opentrafficsim.simulationengine.SimpleSimulator;

/**
 * SimpleSimulator plus IMB transmitter.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$,
 *          initial version Sep 2, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class SimpleIMBSimulator extends SimpleSimulator implements IMBTransmitterInterface
{

    /** */
    private static final long serialVersionUID = 20160902L;

    /** The currently registered IMB transmitter. */
    private IMBTransmitter imbTransmitter = null;

    /**
     * @param startTime
     * @param warmupPeriod
     * @param runLength
     * @param model
     * @throws SimRuntimeException
     * @throws NamingException
     */
    public SimpleIMBSimulator(Time startTime, Duration warmupPeriod, Duration runLength, OTSModelInterface model)
            throws SimRuntimeException, NamingException
    {
        super(startTime, warmupPeriod, runLength, model);
    }

    /** {@inheritDoc} */
    @Override
    public final IMBTransmitter getIMBTransmitter()
    {
        return this.imbTransmitter;
    }

    /** {@inheritDoc} */
    @Override
    public final void setIMBTransmitter(final IMBTransmitter newIMBTransmitter)
    {
        this.imbTransmitter = newIMBTransmitter;
    }

}
