package org.opentrafficsim.imb.simulators;

import java.util.ArrayList;

import javax.naming.NamingException;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.imb.transceiver.OTSIMBConnector;
import org.opentrafficsim.simulationengine.AbstractWrappableSimulation;
import org.opentrafficsim.simulationengine.OTSSimulationException;
import org.opentrafficsim.simulationengine.properties.AbstractProperty;
import org.opentrafficsim.simulationengine.properties.CompoundProperty;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * Simulator that links OTS to the IMB bus by:
 * <ul>
 * <li>instrumenting all GTUs to report their INIT, MOVE and DESTROY events to an IMB hub</li>
 * <li>instrumenting the Simulator to listen to, and send start, stop, and change speed events</li>
 * </ul>
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Sep 2, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public abstract class AbstractWrappableIMBSimulation extends AbstractWrappableSimulation
{
    /** */
    private static final long serialVersionUID = 20160902L;
    
    /** the network to use for subscription purposes. */
    protected final OTSNetwork network = new OTSNetwork("network");

    @Override
    @SuppressWarnings("checkstyle:designforextension")
    protected SimpleIMBSimulator buildSimpleSimulator(final Time startTime, final Duration warmupPeriod,
            final Duration runLength, final OTSModelInterface model) throws SimRuntimeException, NamingException
    {
        return new SimpleIMBSimulator(startTime, warmupPeriod, runLength, model);
    }

    /** {@inheritDoc} */
    @Override
    public SimpleIMBSimulator buildSimulator(final Time startTime, final Duration warmupPeriod, final Duration runLength,
            final ArrayList<AbstractProperty<?>> userModifiedProperties)
            throws SimRuntimeException, NamingException, OTSSimulationException
    {
        SimpleIMBSimulator simulator =
                (SimpleIMBSimulator) super.buildSimulator(startTime, warmupPeriod, runLength, userModifiedProperties);
        // This is probably where we have to act on the imb settings (if present among the userModifiedProperties)
        CompoundProperty imbSettings = null;
        for (AbstractProperty<?> property : userModifiedProperties)
        {
            if (property.getKey().equals(OTSIMBConnector.PROPERTY_KEY))
            {
                imbSettings = (CompoundProperty) property;
            }
        }
        if (null != imbSettings)
        {
            try
            {
                simulator.setIMBConnector(OTSIMBConnector.create(imbSettings, "OTS"));
                new GTUTransceiver(simulator.getIMBConnector(), simulator, getNetwork());
                new SimulatorTransceiver(simulator.getIMBConnector(), simulator);
            }
            catch (Exception exception)
            {
                exception.printStackTrace();
            }
        }
        return simulator;
    }

    /**
     * @return network the OTS network for registration purposes.
     */
    public final OTSNetwork getNetwork()
    {
        return this.network;
    }

}
