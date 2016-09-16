package org.opentrafficsim.imb.simulators;

import java.rmi.RemoteException;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.DoubleScalar.Abs;
import org.djunits.value.vdouble.scalar.DoubleScalar.Rel;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
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
     * @param imbConnector
     * @throws SimRuntimeException
     * @throws NamingException
     * @throws PropertyException
     */
    public SimpleIMBAnimator(final Time startTime, final Duration warmupPeriod, final Duration runLength,
            final OTSModelInterface model, OTSIMBConnector imbConnector) throws SimRuntimeException, NamingException, PropertyException
    {
        super(startTime, warmupPeriod, runLength, new ModelWrapper(model, imbConnector));
        this.imbConnector = imbConnector;
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


/**
 * Wrap a OTSModelInterface to allow setting the OTSIMBConnector just before the constructModel method is called.
 */
class ModelWrapper implements OTSModelInterface
{
    /** */
    private static final long serialVersionUID = 20160916L;

    /** The simulator. */
    private SimulatorInterface<Abs<TimeUnit>, Rel<TimeUnit>, OTSSimTimeDouble> simulator = null;
  
    /** The wrapped model. */
    private final OTSModelInterface wrappedModel;
    
    /** The IMB connector. */
    private final OTSIMBConnector imbConnector;
    
    /**
     * Construct a new ModelWrapper.
     * @param model OTSModelInterface; the model that will be wrapped
     * @param imbConnector OTSIMBConnector; the IMB connector
     */
    public ModelWrapper(final OTSModelInterface model, final OTSIMBConnector imbConnector)
    {
        System.out.println("ModelWrapper: constructor called");
        this.wrappedModel = model;
        this.imbConnector = imbConnector;
    }

    /** {@inheritDoc} */
    @Override
    public void constructModel(SimulatorInterface<Abs<TimeUnit>, Rel<TimeUnit>, OTSSimTimeDouble> theSimulator)
            throws SimRuntimeException, RemoteException
    {
        System.out.println("ModelWrapper: constructModel called");
        this.simulator = theSimulator;
        if (theSimulator instanceof SimpleIMBAnimator)
        {
            SimpleIMBAnimator imbAnimator = (SimpleIMBAnimator) theSimulator;
            imbAnimator.setIMBConnector(this.imbConnector);
        }
        this.wrappedModel.constructModel(theSimulator);
    }

    /** {@inheritDoc} */
    @Override
    public SimulatorInterface<Abs<TimeUnit>, Rel<TimeUnit>, OTSSimTimeDouble> getSimulator() throws RemoteException
    {
        return this.simulator;
    }
    
}
