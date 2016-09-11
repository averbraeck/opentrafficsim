package org.opentrafficsim.imb.simulators;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.imb.transceiver.OTSIMBConnector;
import org.opentrafficsim.simulationengine.SimpleAnimator;
import org.opentrafficsim.simulationengine.properties.PropertyException;

/**
 * SimpleAnimator plus IMB connector.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Sep 2, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class SimpleIMBAnimator extends SimpleAnimator implements IMBConnectability
{
    /** */
    private static final long serialVersionUID = 20160902L;

    /** The currently registered IMB transmitter. */
    private OTSIMBConnector imbConnector = null;

    /**
     * @param startTime
     * @param warmupPeriod
     * @param runLength
     * @param model
     * @throws SimRuntimeException
     * @throws NamingException
     * @throws PropertyException
     */
    public SimpleIMBAnimator(final Time startTime, final Duration warmupPeriod, final Duration runLength,
            final OTSModelInterface model) throws SimRuntimeException, NamingException, PropertyException
    {
        super(startTime, warmupPeriod, runLength, model);
    }

    /** {@inheritDoc} */
    @Override
    public final OTSIMBConnector getIMBConnector()
    {
        return this.imbConnector;
    }

    /** {@inheritDoc} */
    @Override
    public final void setIMBConnector(final OTSIMBConnector newIMBConnector)
    {
        this.imbConnector = newIMBConnector;
    }

}
