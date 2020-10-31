package nl.tno.imb.mc;

import java.util.LinkedHashMap;
import java.util.Map;

import org.opentrafficsim.imb.SelfWrapper;

import nl.tno.imb.TByteBuffer;

/**
 * The IMB Model events.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Oct 14, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public abstract class ModelEvent implements SelfWrapper
{

    /**
     * Prepare this ModelEvent for transmission over IMB.
     * @param payload TByteBuffer; the transmit buffer
     */
    @Override
    public abstract void prepare(TByteBuffer payload);

    /**
     * Transmit this ModelEvent over IMB.
     * @param payload TByteBuffer; the transmit buffer
     */
    @Override
    public abstract void qWrite(TByteBuffer payload);

    /**
     * Delete event.
     */
    class DeleteEvent extends ModelEvent
    {
        /** UID. */
        final int uid;

        /**
         * Construct a new DeleteEvent from data received over IMB.
         * @param payload TByteBuffer; the received IMB data
         */
        public DeleteEvent(TByteBuffer payload)
        {
            this.uid = payload.readInt32();
        }

        /** {@inheritDoc} */
        @Override
        public void prepare(TByteBuffer payload)
        {
            payload.prepare(this.uid);
        }

        /** {@inheritDoc} */
        @Override
        public void qWrite(TByteBuffer payload)
        {
            payload.qWrite(this.uid);
        }

    }

    /**
     * Parse and store the content of an IMB model change event.
     */
    class ModelChangeEvent
    {
        /** Id. */
        final public int uid;

        /** Mode state. */
        final ModelState state;

        /** The IMB federation. */
        final String federation;

        /**
         * Construct a new ModelChangeEvent from an IMB message.
         * @param payload TByteBuffer; the IMB message
         */
        ModelChangeEvent(TByteBuffer payload)
        {
            this.uid = payload.readInt32();
            int stateCode = payload.readInt32();
            this.state = ModelState.byValue(stateCode);
            this.federation = payload.readString();
        }

        /**
         * Retrieve the Id.
         * @return int
         */
        public int getUid()
        {
            return this.uid;
        }

        /**
         * Retrieve the model state.
         * @return ModelState; the model state
         */
        public ModelState getState()
        {
            return this.state;
        }

        /**
         * Retrieve the federation.
         * @return String; the federation
         */
        public String getFederation()
        {
            return this.federation;
        }

    }

    /**
     * Commands handled by the command interpreter.
     */
    enum ModelCommand
    {

        /** Claim the model. */
        CLAIM(11),
        /** Unclaim the model. */
        UNCLAIM(12),

        /** ???. */
        MODEL(21),
        /** Request models. */
        REQUEST_MODELS(22),
        /** Default parameters. */

        DEFAULT_PARAMETERS(31),
        /** Request default parameters. */
        REQUEST_DEFAULT_PARAMETERS(32),

        /** Folder contents. */
        FOLDER_CONTENTS(41),
        /** Request folder contents. */
        REQUEST_FOLDER_CONTENTS(42),

        /** Controller. */
        CONTROLLER(51),
        /** Request controllers. */
        REQUEST_CONTROLLERS(52),
        /** Controller change. */
        CONTROLLER_CHANGE(53),

        /** Controller model setup. */
        CONTROLLER_MODEL_SETUP(61),
        /** Request controller model setups. */
        REQUEST_CONTROLLER_MODEL_SETUPS(62),
        /** Controller model setup new. */
        CONTROLLER_MODEL_SETUP_NDW(63),
        /** Controller model setup delete. */
        CONTROLLER_MODEL_SETUP_DELETE(64),
        /** Controller model setup change. */
        CONTROLLER_MODEL_SETUP_CHANGE(65),

        /** Init. */
        INIT(71),
        /** Quit application. */
        QUIT_APPLICATION(72),
        /** Progress. */
        PROGRESS(73);

        /** Value that represent this model command in transit over IMB. */
        private final int value;

        /** Map to translate numeric value to enum. */
        protected static Map<Integer, ModelCommand> commandMap = new LinkedHashMap<>();

        static
        {
            for (ModelCommand modelCommand : values())
            {
                commandMap.put(modelCommand.getValue(), modelCommand);
            }
        }

        /**
         * Construct a new ModelCommand
         * @param value int;
         */
        private ModelCommand(final int value)
        {
            this.value = value;
        }

        /**
         * Retrieve the value that represents this model command during transmission over IMB.
         * @return int; the value that represents this model command during transmission over IMB
         */
        public final int getValue()
        {
            return this.value;
        }

        /**
         * Lookup the ModelCommand that corresponds to the value.
         * @param value int; the value to look up
         * @return ModelCommand; the ModelCommand that corresponds to the value, or null if no ModelCommand with the specified
         *         value is defined
         */
        protected static ModelCommand byValue(final int value)
        {
            return commandMap.get(value);
        }

    }

}

/**
 * New event.
 */
class ChangeEvent extends ModelEvent
{
    /** UID. */
    final int uid;

    /** The state. */
    final ModelState state;

    /** The IMB federation. */
    final String federation;

    /**
     * Construct a new ChangeEvent from data received over IMB.
     * @param payload TByteBuffer; the received IMB data
     */
    public ChangeEvent(TByteBuffer payload)
    {
        this.uid = payload.readInt32();
        this.state = ModelState.byValue(payload.readInt32());
        this.federation = payload.readString();
    }

    /**
     * Construct a new ChangeEvent from uid, state and federation parameters.
     * @param uid int; the uid of the new ChangeEvent
     * @param state ModelState; the state of the new ChangeEvent
     * @param federation String; the federation of the new ChangeEvent
     */
    public ChangeEvent(final int uid, final ModelState state, final String federation)
    {
        this.uid = uid;
        this.state = state;
        this.federation = federation;
    }

    /** {@inheritDoc} */
    @Override
    public void prepare(TByteBuffer payload)
    {
        payload.prepare(this.uid);
        payload.prepare(this.state.getValue());
        payload.prepare(this.federation);
    }

    /** {@inheritDoc} */
    @Override
    public void qWrite(TByteBuffer payload)
    {
        payload.qWrite(this.uid);
        payload.qWrite(this.state.getValue());
        payload.qWrite(this.federation);
    }

}

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

    /**
     * Construct an InitEvent.
     * @param linkId long; the link id
     * @param uid int; the uid
     * @param modelName String; the model name
     * @param modelPrivateEventName String; the model private event name
     */
    public InitEvent(final long linkId, final int uid, final String modelName, final String modelPrivateEventName)
    {
        this.linkId = linkId;
        this.uid = uid;
        this.modelName = modelName;
        this.modelPrivateEventName = modelPrivateEventName;
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

    /** The model state. */
    final ModelState state;

    /** The IMB federation. */
    final String federation;

    /** Private event name. */
    final String privateEventName;

    /** Controller private event name. */
    final String controllerPrivateEventName;

    /**
     * Construct a new NewEvent from data received over IMB.
     * @param payload TByteBuffer; the received IMB data
     */
    public NewEvent(final TByteBuffer payload)
    {
        this.uid = payload.readInt32();
        this.modelName = payload.readString();
        this.controller = payload.readString();
        this.priority = payload.readInt32();
        this.state = ModelState.byValue(payload.readInt32());
        this.federation = payload.readString();
        this.privateEventName = payload.readString();
        this.controllerPrivateEventName = payload.readString();
    }

    /**
     * Construct a new NewEvent.
     * @param uid int; the uid
     * @param modelName String; the name of the model
     * @param controller String; the name of the controller (?)
     * @param priority int; the priority
     * @param state ModelState; the state of the model
     * @param federation String; the IMB federation
     * @param privateEventName String; ???
     * @param controllerPrivateEventName String; ???
     */
    public NewEvent(final int uid, final String modelName, final String controller, final int priority, final ModelState state,
            final String federation, final String privateEventName, final String controllerPrivateEventName)
    {
        this.uid = uid;
        this.modelName = modelName;
        this.controller = controller;
        this.priority = priority;
        this.state = state;
        this.federation = federation;
        this.privateEventName = privateEventName;
        this.controllerPrivateEventName = controllerPrivateEventName;
    }

    /** {@inheritDoc} */
    @Override
    public void prepare(TByteBuffer payload)
    {
        payload.prepare(this.uid);
        payload.prepare(this.modelName);
        payload.prepare(this.controller);
        payload.prepare(this.priority);
        payload.prepare(this.state.getValue());
        payload.prepare(this.federation);
        payload.prepare(this.privateEventName);
        payload.prepare(this.controllerPrivateEventName);
    }

    /** {@inheritDoc} */
    @Override
    public void qWrite(TByteBuffer payload)
    {
        payload.qWrite(this.uid);
        payload.qWrite(this.modelName);
        payload.qWrite(this.controller);
        payload.qWrite(this.priority);
        payload.qWrite(this.state.getValue());
        payload.qWrite(this.federation);
        payload.qWrite(this.privateEventName);
        payload.qWrite(this.controllerPrivateEventName);
    }

}
