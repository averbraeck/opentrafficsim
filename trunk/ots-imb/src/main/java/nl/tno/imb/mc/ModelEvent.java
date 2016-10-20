package nl.tno.imb.mc;

import java.util.HashMap;
import java.util.Map;

import nl.tno.imb.TByteBuffer;
import nl.tno.imb.TConnection;
import nl.tno.imb.TEventEntry;

import org.opentrafficsim.imb.IMBException;

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

    /** Controllers root event name. */
    static final String CONTROLLERS_ROOT_EVENT_NAME = "Controllers";

    /** Default clients root event name. */
    static final String CLIENTS_ROOT_EVENT_NAME = "Clients";

    /** Federation parameter name. */
    static final String FEDERATION_PARAMETER_NAME = "Federation";

    /** Data source parameter name. */
    static final String DATA_SOURCE_PARAMETER_NAME = "DataSource";

    /** Command line remote host switch. */
    static final String REMOTE_HOST_SWITCH = "RemoteHost";

    /** Default remote host. */
    static final String DEFAULT_REMOTE_HOST = "localhost";

    /** Command line remote port switch. */
    static final String REMOTE_PORT_SWITCH = "RemotePort";

    /** Default remote port. */
    static final String DEFAULT_REMOTE_PORT = "4000";

    /** Command line idle federation switch. */
    static final String IDLE_FEDERATION_SWITCH = "IdleFederation";

    /** Default idle federation. */
    static final String DEFAULT_IDLE_FEDERATION = "USidle";

    /** Command line link id switch. */
    static final String LINK_ID_SWITCH = "LinkID";

    /** Command line controllers event name switch. */
    static final String CONTROLLERS_EVENT_NAME_SWITCH = "ControllersEventName";

    /** Command line Controller private event name switch. */
    static final String CONTROLLER_PRIVATE_EVENT_NAME_SWITCH = "ControllerPrivateEventName";

    /** Command line controller switch. */
    static final String CONTROLLER_SWITCH = "ControllerName";

    /** The default controller. */
    static final String DEFAULT_CONTROLLER = "Test";

    /** Event name part separator. */
    static final String EVENT_NAME_PART_SEPARATOR = ".";

    /** Command line model name switch. */
    static final String MODEL_NAME_SWITCH = "ModelName";

    /** Default for model name. */
    static final String DEFAULT_MODEL_NAME = "Undefined model name";

    /** Command line model id switch. */
    static final String MODEL_ID_SWITCH = "ModelID";

    /** Default model id (must be numeric). */
    static final String DEFAULT_MODEL_ID = "99";

    /** Command line model priority switch. */
    static final String MODEL_PRIORITY_SWITCH = "ModelPriority";

    /** Default model priority. */
    static final String DEFAULT_MODEL_PRIORITY = "1";

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
         * @param modelName string; the name of the model
         * @param controller String; the name of the controller (?)
         * @param priority int; the priority
         * @param state ModelState; the state of the model
         * @param federation String; the IMB federation
         * @param privateEventName String; ???
         * @param controllerPrivateEventName String; ???
         */
        public NewEvent(final int uid, final String modelName, final String controller, final int priority,
                final ModelState state, final String federation, final String privateEventName,
                final String controllerPrivateEventName)
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
        void prepare(TByteBuffer payload)
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
        void qWrite(TByteBuffer payload)
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
         * @param state int; the state of the new ChangeEvent
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
        void prepare(TByteBuffer payload)
        {
            payload.prepare(this.uid);
            payload.prepare(this.state.getValue());
            payload.prepare(this.federation);
        }

        /** {@inheritDoc} */
        @Override
        void qWrite(TByteBuffer payload)
        {
            payload.qWrite(this.uid);
            payload.qWrite(this.state.getValue());
            payload.qWrite(this.federation);
        }

    }

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
        void prepare(TByteBuffer payload)
        {
            payload.prepare(this.uid);
        }

        /** {@inheritDoc} */
        @Override
        void qWrite(TByteBuffer payload)
        {
            payload.qWrite(this.uid);
        }

    }

    /**
     * Model starter.
     */
    class ModelStarter
    {
        /** Connection to the IMB hub. */
        private final TConnection connection;

        /** ??? */
        private final String controller;

        /** ??? */
        private final TEventEntry privateModelEvent;

        /** ??? */
        private final TEventEntry controllersEvent;

        /** ??? */
        private final TEventEntry privateControllerEvent;
        
        /** This is c# specific */
        // private final EventWaitHandle quitApplicationEvent;

        /** The remote host (IMB server). */
        private final String remoteHost;

        /** The remote IP port. */
        private final int remotePort;

        /** The idle federation. */
        private final String idleFederation;

        /** The controllers event name. */
        private final String controllersEventName;

        /** The controller private event name. */
        private final String controllerPrivateEventName;

        /** State of the model. */
        ModelState state;

        /** Priority. */
        int priority;

        /** Progress. */
        int progress;

        /**
         * Create a new ModelStarter.
         * @param args String[]; the command line arguments
         * @param providedModelName String; name of the model
         * @param providedModelId int; id of the model
         */
        public ModelStarter(final String[] args, final String providedModelName, final int providedModelId)
        {
            StandardSettings settings = new StandardSettings(args);
            this.remoteHost = settings.getSetting(REMOTE_HOST_SWITCH, DEFAULT_REMOTE_HOST);
            this.remotePort = Integer.parseInt(settings.getSetting(REMOTE_PORT_SWITCH, DEFAULT_REMOTE_PORT));
            this.idleFederation = settings.getSetting(IDLE_FEDERATION_SWITCH, DEFAULT_IDLE_FEDERATION);
            this.controller = settings.getSetting(CONTROLLER_SWITCH, DEFAULT_CONTROLLER);
            this.controllersEventName =
                    settings.getSetting(CONTROLLERS_EVENT_NAME_SWITCH, this.idleFederation + "." + CONTROLLERS_ROOT_EVENT_NAME);
            this.controllerPrivateEventName =
                    settings.getSwitch(CONTROLLER_PRIVATE_EVENT_NAME_SWITCH, this.idleFederation + "."
                            + CONTROLLERS_ROOT_EVENT_NAME);
            long linkId = 0;
            try
            {
                linkId = Long.parseLong(settings.getSwitch(LINK_ID_SWITCH, "0"));
            }
            catch (NumberFormatException nfe)
            {
                System.err.println("Ignoring bad LinkId");
            }
            String modelName;
            int modelId;
            if (null != providedModelName && providedModelName.length() > 0)
            {
                modelName = providedModelName;
                if (providedModelId != 0)
                {
                    modelId = providedModelId;
                }
                else
                {
                    modelId = Integer.parseInt(settings.getSetting(MODEL_ID_SWITCH, DEFAULT_MODEL_ID));
                }
            }
            else
            {
                modelName = settings.getSetting(MODEL_NAME_SWITCH, DEFAULT_MODEL_NAME);
                modelId = Integer.parseInt(settings.getSetting(MODEL_ID_SWITCH, DEFAULT_MODEL_ID));
            }
            System.out.println("IMB " + this.remoteHost + ":" + this.remotePort);
            System.out.println("Controller " + this.controller);
            System.out.println("ControllersEventName " + this.controllersEventName);
            System.out.println("ControllerPrivateEventName " + this.controllerPrivateEventName);
            System.out.println("LinkID " + linkId);
            System.out.println("ModelName " + modelName);
            System.out.println("ModelID " + modelId);
            // TODO quitApplicationEvent = new EventWaitHandle(false, EventResetMode.ManualReset);
            this.connection = new TConnection(this.remoteHost, this.remotePort, modelName, modelId, "");
            this.privateModelEvent =
                    this.connection.subscribe(this.controllerPrivateEventName + EVENT_NAME_PART_SEPARATOR + modelName
                            + EVENT_NAME_PART_SEPARATOR + String.format("%08x", this.connection.getUniqueClientID()), false);
            this.privateModelEvent.onNormalEvent = new TEventEntry.TOnNormalEvent()
            {
                @Override
                public void dispatch(final TEventEntry aEvent, final TByteBuffer aPayload)
                {
                    try
                    {
                        handleControlEvents(aEvent, aPayload);
                    }
                    catch (IMBException exception)
                    {
                        exception.printStackTrace();
                    }
                }
            };
            this.controllersEvent = this.connection.subscribe(this.controllersEventName, false);
            this.controllersEvent.onNormalEvent = new TEventEntry.TOnNormalEvent()
            {
                @Override
                public void dispatch(final TEventEntry aEvent, final TByteBuffer aPayload)
                {
                    try
                    {
                        handleControlEvents(aEvent, aPayload);
                    }
                    catch (IMBException exception)
                    {
                        exception.printStackTrace();
                    }
                }
            };
            this.privateControllerEvent = this.connection.subscribe(this.controllerPrivateEventName, false);
            this.privateControllerEvent.onNormalEvent = new TEventEntry.TOnNormalEvent()
            {
                public void dispatch(final TEventEntry aEvent, final TByteBuffer aPayload)
                {
                    try
                    {
                        handleControlEvents(aEvent, aPayload);
                    }
                    catch (IMBException exception)
                    {
                        exception.printStackTrace();
                    }
                }
            };
            signalModelInit(linkId, modelName);
            this.state = ModelState.IDLE;
            this.progress = 0;
            this.priority = Integer.parseInt(settings.getSetting(MODEL_PRIORITY_SWITCH, DEFAULT_MODEL_PRIORITY));
            signalModelNew("");
        }

        /**
         * Start a model.
         * @param parameters ModelParameters; the parameters needed to start the model
         */
        public void doStartModel(ModelParameters parameters)
        {
            if (parameters.parameterExists(FEDERATION_PARAMETER_NAME))
            {
                try
                {
                    this.connection.setFederation((String) parameters.getParameterValue(FEDERATION_PARAMETER_NAME));
                }
                catch (IMBException exception)
                {
                    exception.printStackTrace(); // cannot happen; we just checked that is exists
                }
            }
            // TODO lots of code that appears to deal with threads
            signalModelState(ModelState.BUSY);
            // TODO onStartModel(this, this.connection, parameters, whatever);
            // TODO more stuff
        }

        /**
         * Stop the model and go to idle state.
         */
        public void doStopModel()
        {
            // TODO
            signalModelProgress(0);
            signalModelState(ModelState.IDLE);
        }
        
        /**
         * Terminate the application.
         */
        public void doQuitApplication()
        {
            // TODO onQuitApplication
            signalModelExit();
            // TODO quitApplicationEvent.set();
        }

        /**
         * Change to another federation.
         * @param conn TConnection; the connection on which the federation should be changed
         * @param newFederationId int; id of the new federation
         * @param newFederation string; the name of the new federation
         */
        private void handleChangeFederation(final TConnection conn, final int newFederationId, final String newFederation)
        {
            String federation = newFederation;
            // TODO OnChangeFederation
            if (federation != null && federation.length() > 0)
            {
                this.connection.setFederation(federation);
            }
        }

        /**
         * Execute a control event.
         * @param event TEventEntry; the event
         * @param payload TByteBuffer; details of the event
         * @throws IMBException when the payload is malformed.
         */
        void handleControlEvents(TEventEntry event, TByteBuffer payload) throws IMBException
        {
            final int command = payload.readInt32();
            final ModelCommand modelCommand = ModelCommand.byValue(command);
            switch (modelCommand)
            {
                case MODEL:
                {
                    int action = payload.readInt32();
                    switch (action)
                    {
                        case TEventEntry.ACTION_CHANGE:
                        {
                            ModelChangeEvent modelChange = new ModelChangeEvent(payload);
                            if (this.connection.getUniqueClientID() == modelChange.getUid())
                            {
                                switch (modelChange.state)
                                {
                                    case LOCK:
                                        if (ModelState.IDLE == this.state)
                                        {
                                            this.state = ModelState.LOCK;
                                        }
                                        break;

                                    case IDLE:
                                        if (ModelState.LOCK == this.state)
                                        {
                                            this.state = ModelState.LOCK;
                                        }
                                        break;

                                    default:
                                        System.err.println("Received unsupported external model state change: "
                                                + modelChange.state);
                                        break;

                                }
                            }
                            break;
                        }
                        default:
                            System.err.println("Ignoring action command " + action);
                    }
                    break;
                }
                case REQUEST_DEFAULT_PARAMETERS:
                {
                    String returnEventName = payload.readString();
                    if (payload.getReadAvailable() > 0)
                    {
                        ModelParameters modelParameters = new ModelParameters(payload);
                        // TODO onparameterrequest
                        TByteBuffer parameterPayload = new TByteBuffer();
                        parameterPayload.prepare(ModelCommand.DEFAULT_PARAMETERS.getValue());
                        parameterPayload.prepare(this.connection.getUniqueClientID());
                        modelParameters.prepare(parameterPayload);
                        parameterPayload.prepareApply();
                        parameterPayload.qWrite(ModelCommand.DEFAULT_PARAMETERS.getValue());
                        parameterPayload.qWrite(this.connection.getUniqueClientID());
                        modelParameters.qWrite(parameterPayload);
                        this.connection.signalEvent(returnEventName, TEventEntry.EK_NORMAL_EVENT, parameterPayload, false);
                    }
                    break;
                    // TODO verify that the try - finally in cSharp was an attempt to prevent a memory leak
                }
                case CLAIM:
                {
                    int uid = payload.readInt32();
                    if (this.connection.getUniqueClientID() == uid /** TODO && null != onStartModel*/)
                    {
                        ModelParameters modelParameters = new ModelParameters(payload);
                        doStartModel(modelParameters);
                        // TODO verify that the try - finally in cSharp was an attempt to prevent a memory leak
                    }
                    break;
                }
                case UNCLAIM:
                    if (this.connection.getUniqueClientID() == payload.readInt32())
                    {
                        doStopModel();
                    }
                    break;

                case QUIT_APPLICATION:
                    if (this.connection.getUniqueClientID() == payload.readInt32())
                    {
                        doQuitApplication();
                    }
                    break;

                case REQUEST_MODELS:
                    signalModelNew(payload.readString());
                    break;
                    
                default:
                    System.err.println("Ignoring unhandled control event " + modelCommand);
                    break;

            }
        }

        /**
         * Report that the model has exited.
         */
        private void signalModelExit()
        {
            TByteBuffer payload = new TByteBuffer();
            payload.prepare(ModelCommand.MODEL.getValue());
            payload.prepare(TEventEntry.ACTION_DELETE);
            payload.prepare(this.connection.getUniqueClientID());
            payload.prepareApply();
            payload.qWrite(ModelCommand.MODEL.getValue());
            payload.qWrite(TEventEntry.ACTION_DELETE);
            payload.qWrite(this.connection.getUniqueClientID());
            this.controllersEvent.signalEvent(TEventEntry.EK_NORMAL_EVENT, payload.getBuffer());
        }

        /**
         * Report that the model has been initialized.
         * @param linkId long; the link Id
         * @param modelName String; the name of the model
         */
        private void signalModelInit(final long linkId, final String modelName)
        {
            InitEvent modelInitEvent =
                    new InitEvent(linkId, this.connection.getUniqueClientID(), modelName, this.privateModelEvent.getEventName());
            TByteBuffer payload = new TByteBuffer();
            payload.prepare(ModelCommand.INIT.getValue());
            modelInitEvent.prepare(payload);
            payload.prepareApply();
            payload.qWrite(ModelCommand.INIT.getValue());
            modelInitEvent.qWrite(payload);
            this.privateControllerEvent.signalEvent(TEventEntry.EK_NORMAL_EVENT, payload.getBuffer());
        }

        /**
         * Report that a new model has been constructed.
         * @param eventName String; name of the event that will be transmitted
         */
        private void signalModelNew(final String eventName)
        {
            NewEvent newEvent =
                    new NewEvent(this.connection.getUniqueClientID(), this.connection.getOwnerName(), this.controller,
                            this.priority, this.state, this.connection.getFederation(), this.privateModelEvent.getEventName(),
                            this.privateControllerEvent.getEventName());
            TByteBuffer payload = new TByteBuffer();
            payload.prepare(ModelCommand.MODEL.getValue());
            payload.prepare(TEventEntry.ACTION_NEW);
            newEvent.prepare(payload);
            payload.prepareApply();
            payload.qWrite(ModelCommand.MODEL.getValue());
            payload.qWrite(TEventEntry.ACTION_NEW);
            newEvent.qWrite(payload);
            if (eventName.length() == 0)
            {
                this.controllersEvent.signalEvent(TEventEntry.EK_NORMAL_EVENT, payload.getBuffer());
            }
            else
            {
                this.connection.signalEvent(eventName, TEventEntry.EK_NORMAL_EVENT, payload, false);
            }
            if (0 != this.progress)
            {
                payload.clear();
                payload.prepare(ModelCommand.PROGRESS.getValue());
                payload.prepare(this.connection.getUniqueClientID());
                payload.prepare(this.progress);
                payload.prepareApply();
                payload.qWrite(ModelCommand.PROGRESS.getValue());
                payload.qWrite(this.connection.getUniqueClientID());
                payload.qWrite(this.progress);
                if (eventName.length() == 0)
                {
                    this.controllersEvent.signalEvent(TEventEntry.EK_NORMAL_EVENT, payload.getBuffer());
                }
                else
                {
                    this.connection.signalEvent(eventName, TEventEntry.EK_NORMAL_EVENT, payload, false);
                }
            }
        }
        
        /**
         * Report a new progress value.
         * @param currentProgress int; the new progress value
         */
        public void signalModelProgress(int currentProgress)
        {
            TByteBuffer payload = new TByteBuffer();
            payload.prepare(ModelCommand.PROGRESS.getValue());
            payload.prepare(this.connection.getUniqueClientID());
            payload.prepare(currentProgress);
            payload.prepareApply();
            payload.qWrite(ModelCommand.PROGRESS.getValue());
            payload.qWrite(this.connection.getUniqueClientID());
            payload.qWrite(currentProgress);
            this.controllersEvent.signalEvent(TEventEntry.EK_NORMAL_EVENT, payload.getBuffer());
            this.progress = currentProgress;
        }

        /**
         * Inform a federation about a state change.
         * @param newState ModelState; the new state
         * @param federation String; the federation
         */
        public void signalModelState(final ModelState newState, final String federation)
        {
            ChangeEvent modelChangeEvent = new ChangeEvent(this.connection.getUniqueClientID(), newState
            , federation);
            TByteBuffer payload = new TByteBuffer();
            payload.prepare(newState.getValue());
            payload.prepare(TEventEntry.ACTION_CHANGE);
            modelChangeEvent.prepare(payload);
            payload.prepareApply();
            payload.qWrite(newState.getValue());
            payload.qWrite(TEventEntry.ACTION_CHANGE);
            modelChangeEvent.qWrite(payload);
            this.controllersEvent.signalEvent(TEventEntry.EK_NORMAL_EVENT, payload.getBuffer());
            System.out.println("New model state " + newState + " on " + federation);
            // TODO verify that this method should not set this.state to newState
        }

        /**
         * Inform our federation about a state change.
         * @param newState ModelState; the new state
         */
        public void signalModelState(final ModelState newState)
        {
            signalModelState(newState, this.connection.getFederation());
        }
        
    }

    /**
     * The possible states of a model.
     */
    public enum ModelState
    {

        /** Ready. */
        READY(0),
        /** Calculating. */
        CALCULATING(1),
        /** Busy. */
        BUSY(2),
        /** Model is available for use. */
        IDLE(3),
        /** Claim model for use in this session. */
        LOCK(4),
        /** Free model to be used in other session. */
        UNLOCK(5),
        /** Force termination of model. */
        // TODO TERMINATE(6),
        /** Model is no longer available. */
        REMOVED(-1);

        /** Map to translate numeric value to enum. */
        protected static Map<Integer, ModelState> commandMap = new HashMap<>();

        static
        {
            for (ModelState modelState : values())
            {
                commandMap.put(modelState.getValue(), modelState);
            }
        }

        /**
         * Construct a ModelState.
         * @param value int; the result of the getValue method
         */
        private ModelState(final int value)
        {
            this.value = value;
        }

        /**
         * Retrieve the integer value that represents this ModelState for transmission over IMB.
         * @return int; the value that represents this ModelState in transmission over IMB
         */
        public int getValue()
        {
            return this.value;
        }

        /** The value that represents this ModalState when being transmitted over IMB. */
        private final int value;

        /**
         * Lookup the ModelState that corresponds to the value.
         * @param value int; the value to look up
         * @return ModelState; the ModelState that corresponds to the value, or null if no ModelState with the specified value
         *         is defined
         */
        protected static ModelState byValue(final int value)
        {
            return commandMap.get(value);
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
        protected static Map<Integer, ModelCommand> commandMap = new HashMap<>();

        static
        {
            for (ModelCommand modelCommand : values())
            {
                commandMap.put(modelCommand.getValue(), modelCommand);
            }
        }

        /**
         * Construct a new ModelCommand
         * @param value
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
