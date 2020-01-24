package org.opentrafficsim.imb.transceiver;

import org.djutils.event.Event;
import org.djutils.event.EventInterface;
import org.djutils.event.EventType;
import org.opentrafficsim.imb.IMBException;

import nl.tno.imb.TByteBuffer;
import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;

/**
 * The PubSubIMBMessageHandler handles the IMB message by transforming it into a DSOL Event and sending it to an EventListener
 * at the current simulation time through a simulation event. The EventListener is identified by the OTSToIMBTransformer and
 * stored in an IMBTransformResult after parsing the IMB message.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Sep 11, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class PubSubIMBMessageHandler implements IMBMessageHandler
{
    /** The simulator to schedule the incoming notifications on. */
    private final DEVSSimulatorInterface.TimeDoubleUnit simulator;

    /** The IMB event name (String). */
    private final String imbEventName;

    /** The corresponding EventType for OTS. */
    private final EventType eventType;

    /** The Transformer to use for the given IMB message type. */
    private final IMBToOTSTransformer imbToOTSTransformer;

    /**
     * Construct a new PubSubIMBMessageHandler. The PubSubIMBMessageHandler handles the IMB message by transforming it into a
     * DSOL Event and sending it to the EventListener at the current simulation time through a simulation event.
     * @param imbEventName String; the name of the IMB event
     * @param eventType EventType; the event type that the listener subscribes to
     * @param imbToOTSTransformer IMBToOTSTransformer; the transformer that creates the event content and identifies the exact
     *            listener on the basis of the IBM event payload, e.g., on the basis of an id within the payload
     * @param simulator DEVSSimulatorInterface.TimeDoubleUnit; The simulator to schedule the incoming notifications on
     * @throws IMBException in case the construction fails
     */
    public PubSubIMBMessageHandler(final String imbEventName, final EventType eventType,
            final IMBToOTSTransformer imbToOTSTransformer, final DEVSSimulatorInterface.TimeDoubleUnit simulator)
            throws IMBException
    {
        this.imbEventName = imbEventName;
        this.eventType = eventType;
        this.imbToOTSTransformer = imbToOTSTransformer;
        this.simulator = simulator;
    }

    /**
     * Handle the event by notifying the EventListener that was identified by the imbPayload through the imbToOTSTransformer.
     * The notification is scheduled on the simulator to make sure that the separate thread that receives events from IMB does
     * not interfere with the execution of the OTS simulation.
     * @param imbPayload TByteBuffer; the IMB payload
     * @throws IMBException in case the message cannot be handled
     */
    @Override
    public void handle(final TByteBuffer imbPayload) throws IMBException
    {
        IMBTransformResult imbTransformResult = this.imbToOTSTransformer.transform(imbPayload);
        EventInterface event = new Event(this.eventType, this, imbTransformResult.getEventContent());
        try
        {
            this.simulator.scheduleEventNow(this, imbTransformResult.getEventListener(), "notify", new Object[] {event});
        }
        catch (SimRuntimeException exception)
        {
            throw new IMBException(exception);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final String getIMBEventName()
    {
        return this.imbEventName;
    }

}
