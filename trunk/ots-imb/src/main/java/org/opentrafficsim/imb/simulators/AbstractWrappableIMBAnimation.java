package org.opentrafficsim.imb.simulators;

import java.awt.Rectangle;
import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.event.EventInterface;
import nl.tudelft.simulation.event.EventListenerInterface;
import nl.tudelft.simulation.event.TimedEvent;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.imb.observers.OTSIMBConnector;
import org.opentrafficsim.simulationengine.AbstractWrappableAnimation;
import org.opentrafficsim.simulationengine.OTSSimulationException;
import org.opentrafficsim.simulationengine.properties.AbstractProperty;
import org.opentrafficsim.simulationengine.properties.CompoundProperty;
import org.opentrafficsim.simulationengine.properties.PropertyException;

/**
 * Animator that instruments all GTUs to report their INIT, MOVE and DESTROY events to an IMB hub.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Sep 2, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public abstract class AbstractWrappableIMBAnimation extends AbstractWrappableAnimation implements WrappableIMBAnimation,
        EventListenerInterface
{

    /** */
    private static final long serialVersionUID = 20160902L;

    /** The network. */
    private OTSNetwork network = null;

    /** The animator. */
    private SimpleIMBAnimator animator;

    @Override
    @SuppressWarnings("checkstyle:designforextension")
    protected SimpleIMBAnimator buildSimpleAnimator(final Time startTime, final Duration warmupPeriod,
            final Duration runLength, final OTSModelInterface model) throws SimRuntimeException, NamingException,
            PropertyException
    {
        this.animator = new SimpleIMBAnimator(startTime, warmupPeriod, runLength, model);
        return this.animator;
    }

    /** {@inheritDoc} */
    @Override
    public SimpleIMBAnimator buildAnimator(final Time startTime, final Duration warmupPeriod, final Duration runLength,
            final ArrayList<AbstractProperty<?>> userModifiedProperties, final Rectangle rect, final boolean eoc)
            throws SimRuntimeException, NamingException, OTSSimulationException, PropertyException
    {
        SimpleIMBAnimator simulator =
                (SimpleIMBAnimator) super.buildAnimator(startTime, warmupPeriod, runLength, userModifiedProperties, rect, eoc);
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
                simulator.setIMBTransmitter(new OTSIMBConnector(imbSettings));
                new SimulatorConnector(simulator);
            }
            catch (Exception exception)
            {
                exception.printStackTrace();
            }
        }
        addListeners();
        return simulator;
    }

    /** {@inheritDoc} */
    @Override
    public OTSNetwork getNetwork()
    {
        return this.network;
    }

    /** {@inheritDoc} */
    @Override
    public void notify(EventInterface event) throws RemoteException
    {
        if (event.getType().equals(Network.GTU_ADD_EVENT))
        {
            String gtuId = event.getContent().toString();
            GTU gtu = this.network.getGTU(gtuId);
            OTSIMBConnector transmitter = this.animator.getIMBTransmitter();
            if (null != transmitter)
            {
                gtu.addListener(transmitter, GTU.INIT_EVENT, true);
                gtu.addListener(transmitter, GTU.MOVE_EVENT, true);
                gtu.addListener(transmitter, GTU.DESTROY_EVENT, true);
            }
        }
        else if (event.getType().equals(Network.GTU_REMOVE_EVENT))
        {
            String gtuId = event.getContent().toString();
            GTU gtu = this.network.getGTU(gtuId);
            OTSIMBConnector transmitter = this.animator.getIMBTransmitter();
            if (null != transmitter)
            {
                gtu.removeListener(transmitter, GTU.INIT_EVENT);
                gtu.removeListener(transmitter, GTU.MOVE_EVENT);
                gtu.removeListener(transmitter, GTU.DESTROY_EVENT);
            }
        }
    }

    /**
     * @return OTSNetwork
     */
    public final OTSNetwork createNetwork()
    {
        this.network = new OTSNetwork("Network");
        return this.network;
    }

    /**
     * Ensure that we get notified about newly created and destroyed GTUs and for each already existing GTU generate a
     * GTU_ADD_EVENT.
     */
    private void addListeners()
    {
        this.network.addListener(this, Network.GTU_ADD_EVENT);
        this.network.addListener(this, Network.GTU_REMOVE_EVENT);
        // Also add all GTUs that were instantiated when the model was constructed
        for (GTU gtu : this.network.getGTUs())
        {
            try
            {
                this.notify(new TimedEvent<OTSSimTimeDouble>(Network.GTU_ADD_EVENT, this.network, gtu.getId(), gtu
                        .getSimulator().getSimulatorTime()));
            }
            catch (RemoteException exception)
            {
                exception.printStackTrace();
            }
        }
        OTSIMBConnector transmitter = this.animator.getIMBTransmitter();
        if (null != transmitter)
        {
            this.animator.addListener(transmitter, SimulatorInterface.START_EVENT);
            this.animator.addListener(transmitter, SimulatorInterface.STOP_EVENT);
            
        }
    }

}
