package nl.tno.imb.mc;

import org.opentrafficsim.imb.IMBException;
import org.opentrafficsim.imb.ObjectArrayToIMB;

import nl.tno.imb.TByteBuffer;
import nl.tno.imb.TConnection;
import nl.tno.imb.TEventEntry;
import nl.tno.imb.mc.ModelEvent.ModelCommand;

/**
 * IMB Model Control starter.
 * <p>
 * Copyright (c) TNO & Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$,
 *          initial version Oct 21, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
/**
 * Model starter.
 */
public abstract class ModelStarter
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

    /** Connection to the IMB hub. */
    protected final TConnection connection;

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
     * Start the model.
     * @param parameters ModelParameters; ModelParameters
     * @param imbConnection TConnection; connection to the IMB hub
     */
    public abstract void startModel(ModelParameters parameters, TConnection imbConnection);

    /**
     * Stop the model.
     */
    public abstract void stopModel();

    /**
     * Kill the model; called before this application exits.
     */
    public abstract void quitApplication();

    /**
     * The model must fill in its parameters.
     * @param parameters ModelParameters; ModelParameters
     */
    public abstract void parameterRequest(ModelParameters parameters);

    /**
     * Create a new ModelStarter.
     * @param args String[]; the command line arguments
     * @param providedModelName String; name of the model
     * @param providedModelId int; id of the model
     * @throws IMBException
     */
    public ModelStarter(final String[] args, final String providedModelName, final int providedModelId) throws IMBException
    {
        StandardSettings settings = new StandardSettings(args);
        this.remoteHost = settings.getSetting(REMOTE_HOST_SWITCH, DEFAULT_REMOTE_HOST);
        this.remotePort = Integer.parseInt(settings.getSetting(REMOTE_PORT_SWITCH, DEFAULT_REMOTE_PORT));
        this.idleFederation = settings.getSetting(IDLE_FEDERATION_SWITCH, DEFAULT_IDLE_FEDERATION);
        this.controller = settings.getSetting(CONTROLLER_SWITCH, DEFAULT_CONTROLLER);
        this.controllersEventName =
                settings.getSetting(CONTROLLERS_EVENT_NAME_SWITCH, this.idleFederation + "." + CONTROLLERS_ROOT_EVENT_NAME);
        this.controllerPrivateEventName = settings.getSwitch(CONTROLLER_PRIVATE_EVENT_NAME_SWITCH,
                this.idleFederation + "." + CONTROLLERS_ROOT_EVENT_NAME);
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
        System.out.println("IMB " + this.remoteHost + ":" + this.getRemotePort());
        System.out.println("Controller " + this.controller);
        System.out.println("ControllersEventName " + this.controllersEventName);
        System.out.println("ControllerPrivateEventName " + this.controllerPrivateEventName);
        System.out.println("LinkID " + linkId);
        System.out.println("ModelName " + modelName);
        System.out.println("ModelID " + modelId);
        this.connection = new TConnection(this.remoteHost, this.getRemotePort(), modelName, modelId, "");
        if (!this.connection.isConnected())
        {
            throw new IMBException("Could not connect to " + this.remoteHost + ":" + this.getRemotePort());
        }
        this.privateModelEvent = this.connection.subscribe(this.controllerPrivateEventName + EVENT_NAME_PART_SEPARATOR
                + modelName + EVENT_NAME_PART_SEPARATOR + String.format("%08x", this.connection.getUniqueClientID()), false);
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
     * @throws IMBException ...
     */
    public void doStartModel(ModelParameters parameters) throws IMBException
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
        signalModelState(ModelState.BUSY);
        startModel(parameters, this.connection);
    }

    /**
     * Stop the model and go to idle state.
     * @throws IMBException ...
     */
    public void doStopModel() throws IMBException
    {
        stopModel();
        signalModelProgress(0);
        signalModelState(ModelState.IDLE);
    }

    /**
     * Terminate the application.
     */
    public void doQuitApplication()
    {
        quitApplication();
        try
        {
            signalModelExit();
        }
        catch (IMBException exception)
        {
            exception.printStackTrace();
        }
        System.out.println("Closing IMB connection...");
        this.connection.close();
        System.out.println("Exiting...");
        System.exit(0); // TODO more gently
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
                        ChangeEvent modelChange = new ChangeEvent(payload);
                        if (this.connection.getUniqueClientID() == modelChange.uid)
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
                                        this.state = ModelState.IDLE;
                                    }
                                    break;

                                default:
                                    System.err
                                            .println("Received unsupported external model state change: " + modelChange.state);
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
                    parameterRequest(modelParameters); // let it be modified
                    this.connection.signalEvent(returnEventName, TEventEntry.EK_NORMAL_EVENT,
                            ObjectArrayToIMB.objectArrayToIMBPayload(new Object[] {ModelCommand.DEFAULT_PARAMETERS.getValue(),
                                    this.connection.getUniqueClientID(), modelParameters}),
                            false);
                }
                break;
            }
            case CLAIM:
            {
                int uid = payload.readInt32();
                if (this.connection.getUniqueClientID() == uid)
                {
                    ModelParameters modelParameters = new ModelParameters(payload);
                    doStartModel(modelParameters);
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
                // System.err.println("Ignoring unhandled control event " + modelCommand);
                break;
        }
    }

    /**
     * Report that the model has exited.
     * @throws IMBException ...
     */
    private void signalModelExit() throws IMBException
    {
        this.controllersEvent.signalEvent(TEventEntry.EK_NORMAL_EVENT, ObjectArrayToIMB.objectArrayToIMBPayload(
                new Object[] {ModelCommand.MODEL.getValue(), TEventEntry.ACTION_DELETE, this.connection.getUniqueClientID()})
                .getBuffer());
    }

    /**
     * Report that the model has been initialized.
     * @param linkId long; the link Id
     * @param modelName String; the name of the model
     * @throws IMBException ...
     */
    private void signalModelInit(final long linkId, final String modelName) throws IMBException
    {
        this.privateControllerEvent.signalEvent(TEventEntry.EK_NORMAL_EVENT,
                ObjectArrayToIMB.objectArrayToIMBPayload(new Object[] {ModelCommand.INIT.getValue()}).getBuffer());
    }

    /**
     * Report that a new model has been constructed.
     * @param eventName String; name of the event that will be transmitted
     * @throws IMBException ...
     */
    private void signalModelNew(final String eventName) throws IMBException
    {
        NewEvent newEvent = new NewEvent(this.connection.getUniqueClientID(), this.connection.getOwnerName(), this.controller,
                this.priority, this.state, this.connection.getFederation(), this.privateModelEvent.getEventName(),
                this.privateControllerEvent.getEventName());
        TByteBuffer payload = null;
        payload = ObjectArrayToIMB
                .objectArrayToIMBPayload(new Object[] {ModelCommand.MODEL.getValue(), TEventEntry.ACTION_NEW, newEvent});
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
            payload = ObjectArrayToIMB.objectArrayToIMBPayload(
                    new Object[] {ModelCommand.PROGRESS.getValue(), this.connection.getUniqueClientID(), this.progress});
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
     * @throws IMBException
     */
    public void signalModelProgress(int currentProgress) throws IMBException
    {
        this.controllersEvent.signalEvent(TEventEntry.EK_NORMAL_EVENT,
                ObjectArrayToIMB.objectArrayToIMBPayload(
                        new Object[] {ModelCommand.PROGRESS.getValue(), this.connection.getUniqueClientID(), currentProgress})
                        .getBuffer());
        this.progress = currentProgress;
    }

    /**
     * Inform a federation about a state change.
     * @param newState ModelState; the new state
     * @param federation String; the federation
     * @throws IMBException ...
     */
    public void signalModelState(final ModelState newState, final String federation) throws IMBException
    {
        ChangeEvent modelChangeEvent = new ChangeEvent(this.connection.getUniqueClientID(), newState, federation);
        this.controllersEvent.signalEvent(TEventEntry.EK_NORMAL_EVENT,
                ObjectArrayToIMB
                        .objectArrayToIMBPayload(
                                new Object[] {ModelCommand.MODEL.getValue(), TEventEntry.ACTION_CHANGE, modelChangeEvent})
                        .getBuffer());
        this.state = newState;
        System.out.println("New model state " + newState + " on " + federation);
    }

    /**
     * Inform our federation about a state change.
     * @param newState ModelState; the new state
     * @throws IMBException ...
     */
    public void signalModelState(final ModelState newState) throws IMBException
    {
        signalModelState(newState, this.connection.getFederation());
    }

    /**
     * Retrieve the remote port number.
     * @return int; the remote port number
     */
    public int getRemotePort()
    {
        return this.remotePort;
    }

    /**
     * Retrieve the remote host name.
     * @return String; the name of the remote host
     */
    public String getRemoteHost()
    {
        return this.remoteHost;
    }

}
