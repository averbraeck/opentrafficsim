package nl.tno.imb.mc;

import nl.tno.imb.TByteBuffer;

/**
 * The IMB Model events.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Oct 14, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public abstract class ModelEvent
{

    /**
     * Prepare this ModelEvent for transmission over IMB.
     * @param payload TByteBuffer; the transmit buffer
     */
    abstract void prepare(TByteBuffer payload);

    /**
     * Transmit this ModelEvent over IMB.
     * @param payload TByteBuffer; the transmit buffer
     */
    abstract void qWrite(TByteBuffer payload);

    /**
     * Init event.
     */
    class InitEvent extends ModelEvent
    {
        /** Id of the link. */
        final long linkId;

        /** UID. */
        final int uid;

        /** Model name. */
        final String modelName;

        /** Private event name. */
        final String modelPrivateEventName;

        /**
         * Construct an InitEvent from a TByteBuffer.
         * @param payload TByteBuffer; the received IMB data
         */
        public InitEvent(final TByteBuffer payload)
        {
            this.linkId = payload.readInt64();
            this.uid = payload.readInt32();
            this.modelName = payload.readString();
            this.modelPrivateEventName = payload.readString();
        }

        /** {@inheritDoc} */
        @Override
        public void prepare(final TByteBuffer payload)
        {
            payload.prepare(this.linkId);
            payload.prepare(this.uid);
            payload.prepare(this.modelName);
            payload.prepare(this.modelPrivateEventName);
        }

        /** {@inheritDoc} */
        @Override
        public void qWrite(final TByteBuffer payload)
        {
            payload.qWrite(this.linkId);
            payload.qWrite(this.uid);
            payload.qWrite(this.modelName);
            payload.qWrite(this.modelPrivateEventName);
        }

    }
    
    /**
     * New event.
     */
    class NewEvent extends ModelEvent
    {
        /** UID. */
        final int uid;
        
        /** Name of the model. */
        final String modelName;
        
        /** The controller. */
        final String controller;
        
        /** The priority. */
        final int priority;
        
        /** The state. */
        final int state;
        
        /** The IMB federation. */
        final String federation;
        
        /** Private event name. */
        final String privateEventName;
        
        /** Controller private event name. */
        final String controllerPrivateEventName;
        
        /**
         * Construct a new NewEvent from data received over IMB.
         * @param payload TByteBuffer; the data that was receive over IMB
         */
        public NewEvent(final TByteBuffer payload)
        {
            this.uid = payload.readInt32();
            this.modelName = payload.readString();
            this.controller = payload.readString();
            this.priority = payload.readInt32();
            this.state = payload.readInt32();
            this.federation = payload.readString();
            this.privateEventName = payload.readString();
            this.controllerPrivateEventName = payload.readString();
        }

        /** {@inheritDoc} */
        @Override
        void prepare(TByteBuffer payload)
        {
            payload.prepare(this.uid);
            payload.prepare(this.modelName);
            payload.prepare(this.controller);
            payload.prepare(this.priority);
            payload.prepare(this.state);
            payload.prepare(this.federation);
            payload.prepare(this.privateEventName);
            payload.prepare(this.controllerPrivateEventName);
        }

        /** {@inheritDoc} */
        @Override
        void qWrite(TByteBuffer payload)
        {
            payload.qWrite(this.uid);
            payload.qWrite(this.modelName);
            payload.qWrite(this.controller);
            payload.qWrite(this.priority);
            payload.qWrite(this.state);
            payload.qWrite(this.federation);
            payload.qWrite(this.privateEventName);
            payload.qWrite(this.controllerPrivateEventName);
        }
        
    }

}
