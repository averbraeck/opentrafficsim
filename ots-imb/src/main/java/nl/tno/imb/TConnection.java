package nl.tno.imb;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;

/**
 * The connection to the framework and starting point to use IMB. All further actions are started through a new object of this
 * class. Main actions are:<br>
 * subscribe to events to receive events<br>
 * publish events to send events (called signal). if autoPublish is true you start sending events immediately<br>
 * set global framework variables<br>
 * send streams over the framework<br>
 * set/update the current status for the connected client<br>
 * optionally define the owner (specific: connected model name and id)<br>
 * optionally set specific socket and connection options<br>
 * <br>
 * Subscribe and publish return TEventEntry objects that can be used to set handlers for receiving specific events.<br>
 * Default a reading thread is started to handle all socket reading. The thread calls registered event handlers for the received
 * events.<br>
 * @author hans.cornelissen@tno.nl
 */
public class TConnection
{

    // constructors
    /**
     * Create an IMB connection to the framework
     * @param aRemoteHost String; IP address or DNS name of the IMB hub to connect to
     * @param aRemotePort int; TCP port of the IMB hub to connect to
     * @param aOwnerName String; optional description of the connecting client
     * @param aOwnerID int; optional id of the connecting client
     * @param aFederation federation to connect with; this is default prefixed to subscribed and published event names
     */
    public TConnection(String aRemoteHost, int aRemotePort, String aOwnerName, int aOwnerID, String aFederation)
    {
        this(aRemoteHost, aRemotePort, aOwnerName, aOwnerID, aFederation, true);
    }

    /**
     * Create an IMB connection to the framework
     * @param aRemoteHost String; IP address or DNS name of the IMB hub to connect to
     * @param aRemotePort int; TCP port of the IMB hub to connect to
     * @param aOwnerName String; optional description of the connecting client
     * @param aOwnerID int; optional id of the connecting client
     * @param aFederation federation to connect with; this is default prefixed to subscribed and published event names
     * @param aStartReadingThread boolean; use an internal reader thread for processing events and commands from the connected
     *            hub
     */
    public TConnection(String aRemoteHost, int aRemotePort, String aOwnerName, int aOwnerID, String aFederation,
            boolean aStartReadingThread)
    {
        this.ffederation = aFederation;
        this.fownerName = aOwnerName;
        this.fownerID = aOwnerID;
        open(aRemoteHost, aRemotePort, aStartReadingThread);
    }

    // destructor
    protected void finalize()
    {
        close();
    }

    // internals/privates

    /** event id's on the hub are different from local id's. this list is used to translate incoming hub event id's */
    private class TEventTranslation
    {
        public final static int INVALID_TRANSLATED_EVENT_ID = -1;

        private int[] feventTranslation;

        public TEventTranslation()
        {
            this.feventTranslation = new int[32];
            // mark all entries as invalid
            for (int i = 0; i < this.feventTranslation.length; i++)
                this.feventTranslation[i] = INVALID_TRANSLATED_EVENT_ID;
        }

        public int getTranslateEventID(int aRxEventID)
        {
            if ((0 <= aRxEventID) && (aRxEventID < this.feventTranslation.length))
                return this.feventTranslation[aRxEventID];
            else
                return INVALID_TRANSLATED_EVENT_ID;
        }

        public void setEventTranslation(int aRxEventID, int aTxEventID)
        {
            if (aRxEventID >= 0)
            {
                // grow event translation list until it can contain the
                // requested id
                while (aRxEventID >= this.feventTranslation.length)
                {
                    int FormerSize = this.feventTranslation.length;
                    // resize event translation array to double the size
                    this.feventTranslation = Arrays.copyOf(this.feventTranslation, this.feventTranslation.length * 2);
                    // mark all new entries as invalid
                    for (int i = FormerSize; i < this.feventTranslation.length; i++)
                        this.feventTranslation[i] = INVALID_TRANSLATED_EVENT_ID;
                }
                this.feventTranslation[aRxEventID] = aTxEventID;
            }
        }
    }

    /** internal list of all local events */
    private class TEventEntryList
    {

        TEventEntryList(int aInitialSize)
        {
            this.FCount = 0;
            this.fevents = new TEventEntry[aInitialSize];
        }

        private TEventEntry[] fevents;

        private int FCount = 0;

        public TEventEntry getEventEntry(int aEventID)
        {
            if (0 <= aEventID && aEventID < this.FCount)
                return this.fevents[aEventID];
            else
                return null;
        }

        public String getEventName(int aEventID)
        {
            if (0 <= aEventID && aEventID < this.FCount)
            {
                if (this.fevents[aEventID] != null)
                    return this.fevents[aEventID].getEventName();
                else
                    return null;
            }
            else
                return "";
        }

        public TEventEntry addEvent(TConnection aConnection, String aEventName)
        {
            this.FCount++;
            if (this.FCount > this.fevents.length)
                this.fevents = Arrays.copyOf(this.fevents, this.fevents.length * 2);
            this.fevents[this.FCount - 1] = new TEventEntry(aConnection, this.FCount - 1, aEventName);

            return this.fevents[this.FCount - 1];
        }

        public TEventEntry getEventEntryOnName(String aEventName)
        {
            int i = this.FCount - 1;
            while (i >= 0 && !getEventName(i).equals(aEventName))
                i--;
            if (i >= 0)
                return this.fevents[i];
            else
                return null;
        }
    }

    // TODO: description
    public static final String EVENT_FILTER_POST_FIX = "*";

    /** postfix of the variable name for the model status */
    private static final String MODEL_Status_VAR_NAME = "ModelStatus";

    /** separator char for the postfix of the variable name for the model status */
    private static final String MODEL_STATUS_VAR_SEP_CHAR = "|";

    // constants
    /** magic bytes to identify the start of a valid IMB packet */
    public static final byte[] MAGIC_BYTES =
            new byte[] {0x2F, 0x47, 0x61, 0x71, (byte) 0x95, (byte) 0xAD, (byte) 0xC5, (byte) 0xFB};

    // private static final long MagicBytesInt64 = 0xFBC5AD957161472FL;
    /** magic bytes to identify the end of the payload on a valid IMB packet (as 32 bit integer) */
    private static final int MAGIC_STRING_CHECK_INT32 = 0x10F13467;

    /** magic bytes to identify the end of the payload on a valid IMB packet (as array of bytes) */
    public static final byte[] MAGIC_STRING_CHECK = new byte[] {0x67, 0x34, (byte) 0xF1, 0x10};

    // fields
    /** TCP Socket the connection is based on */
    private Socket fsocket = null;

    /** output stream linked to the socket */
    private OutputStream foutputStream = null;

    /** input stream linked to the socket */
    private InputStream finputStream = null;

    /** address the socket is connected to */
    private String fremoteHost = "";

    /** TCP port the socket is connected to */
    private int fremotePort = 0;

    /** optional reader thread */
    private Thread freadingThread = null;

    /** event id's on the hub are different from local id's. this list is used to translate incoming hub event id's */
    private TEventTranslation feventTranslation = new TEventTranslation();

    /** internal list of all local events */
    private TEventEntryList feventEntryList = new TEventEntryList(8);

    /** active federation */
    private String ffederation = DEFAULT_FEDERATION;

    // standard event references
    private static final String FOCUS_EVENT_NAME = "Focus";

    private TEventEntry ffocusEvent = null;

    private static final String CHANGE_FEDERATION_EVENT_NAME = "META_CurrentSession";

    private TEventEntry fchangeFederationEvent = null;

    private TEventEntry flogEvent = null;

    // broker time
    // private long fbrokerAbsoluteTime = 0;
    // private int fbrokerTick = 0;
    // private int fbrokerTickDelta = 0;
    private int funiqueClientID = 0;

    private int fclientHandle = 0;

    private int fownerID = 0;

    private String fownerName = "";

    private TEventEntry eventIDToEventL(int aEventID)
    {
        synchronized (this.feventEntryList)
        {
            return this.feventEntryList.getEventEntry(aEventID);
        }
    }

    private TEventEntry addEvent(String aEventName)
    {
        int EventID = 0;
        TEventEntry Event;
        while (EventID < this.feventEntryList.FCount && !this.feventEntryList.getEventEntry(EventID).isEmpty())
            EventID += 1;
        if (EventID < this.feventEntryList.FCount)
        {
            Event = this.feventEntryList.getEventEntry(EventID);
            Event.feventName = aEventName;
            Event.fparent = null;
        }
        else
            Event = this.feventEntryList.addEvent(this, aEventName);
        return Event;
    }

    private TEventEntry addEventL(String aEventName)
    {
        TEventEntry Event;
        synchronized (this.feventEntryList)
        {
            Event = addEvent(aEventName);
        }
        return Event;
    }

    private TEventEntry findOrAddEventL(String aEventName)
    {
        synchronized (this.feventEntryList)
        {
            TEventEntry Event = this.feventEntryList.getEventEntryOnName(aEventName);
            if (Event == null)
            {
                int EventID = 0;
                while (EventID < this.feventEntryList.FCount && !this.feventEntryList.getEventEntry(EventID).isEmpty())
                    EventID += 1;
                if (EventID < this.feventEntryList.FCount)
                {
                    Event = this.feventEntryList.getEventEntry(EventID);
                    Event.feventName = aEventName;
                }
                else
                    Event = this.feventEntryList.addEvent(this, aEventName);
            }
            return Event;
        }
    }

    private TEventEntry findEventL(String aEventName)
    {
        synchronized (this.feventEntryList)
        {
            return this.feventEntryList.getEventEntryOnName(aEventName);
        }
    }

    private TEventEntry findEventParentL(String aEventName)
    {
        String ParentEventName;
        String EventName;
        ParentEventName = "";
        TEventEntry Event = null;
        synchronized (this.feventEntryList)
        {
            for (int EventID = 0; EventID < this.feventEntryList.FCount; EventID++)
            {
                EventName = this.feventEntryList.getEventName(EventID);
                if (EventName.endsWith(EVENT_FILTER_POST_FIX))
                {
                    EventName = EventName.substring(0, EventName.length() - 2);
                    if (aEventName.startsWith(EventName))
                    {
                        if (ParentEventName.length() < EventName.length())
                        {
                            Event = this.feventEntryList.getEventEntry(EventID);
                            ParentEventName = EventName;
                        }
                    }
                }
            }
            return Event;
        }
    }

    private TEventEntry findEventAutoPublishL(String aEventName)
    {
        TEventEntry Event = findEventL(aEventName);
        if (Event == null && this.autoPublish)
            Event = publish(aEventName, false);
        return Event;
    }

    private int readBytesFromNetStream(TByteBuffer aBuffer)
    {
        try
        {
            int Count = 0;
            int NumBytesRead = -1;
            while (aBuffer.getwriteAvailable() > 0 && NumBytesRead != 0)
            {
                NumBytesRead =
                        this.finputStream.read(aBuffer.getBuffer(), aBuffer.getWriteCursor(), aBuffer.getwriteAvailable());
                aBuffer.written(NumBytesRead);
                Count += NumBytesRead;
            }
            return Count;
        }
        catch (IOException ex)
        {
            return 0; // signal connection error
        }
    }

    // function returns payload of command, fills found command and returns
    // problems during read in result
    // commandmagic + command + payloadsize [ + payload + payloadmagic]
    private int readCommand(TByteBuffer aFixedCommandPart, TByteBuffer aPayload, TByteBuffer aPayloadCheck) throws IOException
    {
        aFixedCommandPart.clear(aFixedCommandPart.getLength());
        int NumBytesRead = readBytesFromNetStream(aFixedCommandPart);
        // int NumBytesRead = finputStream.read(aFixedCommandPart.getBuffer(), 0, aFixedCommandPart.getLength());
        if (NumBytesRead > 0)
        {
            while (!aFixedCommandPart.compare(MAGIC_BYTES, 0))
            {
                int rbr = this.finputStream.read();
                // skipped bytes because of invalid magic in read command
                if (rbr != -1)
                    aFixedCommandPart.shiftLeftOneByte((byte) rbr);
                else
                    return TEventEntry.IC_END_OF_SESSION;
            }
            // we found the magic in the stream
            int aCommand = aFixedCommandPart.peekInt32(MAGIC_BYTES.length);
            int PayloadSize = aFixedCommandPart.peekInt32(MAGIC_BYTES.length + TByteBuffer.SIZE_OF_INT32);
            if (PayloadSize <= MAX_PAYLOAD_SIZE)
            {
                aPayload.clear(PayloadSize);
                if (PayloadSize > 0)
                {
                    int Len = readBytesFromNetStream(aPayload);
                    if (Len == aPayload.getLength())
                    {
                        // NumBytesRead = finputStream.read(aPayloadCheck.getBuffer(), 0, aPayloadCheck.getLength());
                        aPayloadCheck.clear(TByteBuffer.SIZE_OF_INT32);
                        NumBytesRead = readBytesFromNetStream(aPayloadCheck);
                        if (NumBytesRead == TByteBuffer.SIZE_OF_INT32 && aPayloadCheck.compare(MAGIC_STRING_CHECK, 0))
                            return aCommand;
                        else
                            return TEventEntry.IC_INVALID_COMMAND;
                    }
                    else
                        // error, payload size mismatch
                        return TEventEntry.IC_INVALID_COMMAND;
                }
                else
                    return aCommand; // OK, no payload
            }
            else
                return TEventEntry.IC_INVALID_COMMAND; // error, payload is over max size
        }
        else
            return TEventEntry.IC_END_OF_SESSION; // error, no valid connection
    }

    /** place holder for the Lock for writing commands to the framework */
    private Integer fwiteCommandLock = new Integer(0);

    /**
     * Write a single command to the framework
     * @param aCommand int;
     * @param aPayload byte[];
     * @return see ICE_* constants
     */
    protected int writeCommand(int aCommand, byte[] aPayload)
    {
        synchronized (this.fwiteCommandLock)
        {
            TByteBuffer Buffer = new TByteBuffer();

            Buffer.prepare(MAGIC_BYTES);
            Buffer.prepare(aCommand);

            if ((aPayload != null) && (aPayload.length > 0))
            {
                Buffer.prepare(aPayload.length);
                Buffer.prepare(aPayload);
                Buffer.prepare(MAGIC_STRING_CHECK_INT32);
            }
            else
                Buffer.prepare((int) 0); // payload size=0
            Buffer.prepareApply();
            Buffer.qWrite(MAGIC_BYTES);
            Buffer.qWrite(aCommand);
            if ((aPayload != null) && (aPayload.length > 0))
            {
                Buffer.qWrite(aPayload.length);
                Buffer.qWrite(aPayload);
                Buffer.qWrite(MAGIC_STRING_CHECK_INT32);
            }
            else
                Buffer.qWrite((int) 0);
            // send buffer over socket
            if (isConnected())
            {
                try
                {
                    this.foutputStream.write(Buffer.getBuffer(), 0, Buffer.getLength());
                    return Buffer.getLength();
                }
                catch (Exception ex)
                {
                    close();
                    return ICE_CONNECTION_CLOSED;
                }
            }
            else
            {
                return ICE_CONNECTION_CLOSED;
            }
        }
    }

    protected String prefixFederation(String aName)
    {
        return prefixFederation(aName, true);
    }

    protected String prefixFederation(String aName, boolean aUseFederationPrefix)
    {
        if (!this.ffederation.equals("") && aUseFederationPrefix)
            return this.ffederation + "." + aName;
        else
            return aName;
    }

    /**
     * Main framework command dispatcher
     * @param aCommand int;
     * @param aPayload TByteBuffer;
     */
    private void handleCommand(int aCommand, TByteBuffer aPayload)
    {
        switch (aCommand)
        {
            case TEventEntry.IC_EVENT:
                handleCommandEvent(aPayload);
                break;
            case TEventEntry.IC_SET_VARIABLE:
                handleCommandVariable(aPayload);
                break;
            case TEventEntry.IC_SET_EVENT_ID_TRANSLATION:
                this.feventTranslation.setEventTranslation(aPayload.peekInt32(0, TEventTranslation.INVALID_TRANSLATED_EVENT_ID),
                        aPayload.peekInt32(TByteBuffer.SIZE_OF_INT32, TEventTranslation.INVALID_TRANSLATED_EVENT_ID));
                break;
            case TEventEntry.IC_UNIQUE_CLIENT_ID:
                this.funiqueClientID = aPayload.readInt32();
                this.fclientHandle = aPayload.readInt32();
                break;
            /*
             * case icTimeStamp: // ignore for now, only when using and syncing local time (we trust hub time for now)
             * fbrokerAbsoluteTime = aPayload.ReadInt64(); fbrokerTick = aPayload.ReadInt32(); fbrokerTickDelta =
             * aPayload.ReadInt32(); break;
             */
            case TEventEntry.IC_EVENT_NAMES:
                handleCommandEventNames(aPayload);
                break;
            case TEventEntry.IC_END_OF_SESSION:
                close();
                break;
            case TEventEntry.IC_SUBSCRIBE:
            case TEventEntry.IC_PUBLISH:
            case TEventEntry.IC_UNSUBSCRIBE:
            case TEventEntry.IC_UNPUBLISH:
                handleSubAndPub(aCommand, aPayload);
                break;
            default:
                handleCommandOther(aCommand, aPayload);
                break;
        }
    }

    private void handleCommandEvent(TByteBuffer aPayload)
    {
        int TxEventID = this.feventTranslation.getTranslateEventID(aPayload.readInt32());
        if (TxEventID != TEventTranslation.INVALID_TRANSLATED_EVENT_ID)
            eventIDToEventL(TxEventID).handleEvent(aPayload);
    }

    private void handleCommandVariable(TByteBuffer aPayload)
    {
        if (this.onVariable != null || this.onStatusUpdate != null)
        {
            String VarName = aPayload.readString();
            // check if it is a status update
            // TODO: model name is prefixed by federation. Is this correct?
            if (VarName.toUpperCase().endsWith(MODEL_STATUS_VAR_SEP_CHAR + MODEL_Status_VAR_NAME.toUpperCase()))
            {
                VarName = VarName.substring(0,
                        VarName.length() - (MODEL_STATUS_VAR_SEP_CHAR.length() + MODEL_Status_VAR_NAME.length()));
                String ModelName = VarName.substring(8, VarName.length());
                String ModelUniqueClientID = VarName.substring(0, 8);
                aPayload.readInt32();
                int Status = aPayload.readInt32(-1);
                int Progress = aPayload.readInt32(-1);
                if (this.onStatusUpdate != null)
                    this.onStatusUpdate.dispatch(this, ModelUniqueClientID, ModelName, Progress, Status);
            }
            else
            {
                TByteBuffer VarValue = aPayload.readByteBuffer();
                TByteBuffer PrevValue = new TByteBuffer();
                if (this.onVariable != null)
                    this.onVariable.dispatch(this, VarName, VarValue.getBuffer(), PrevValue.getBuffer());
            }
        }
    }

    private void handleCommandEventNames(TByteBuffer aPayload)
    {
        if (this.onEventNames != null)
        {
            int ec = aPayload.readInt32();
            TEventNameEntry[] EventNames = new TEventNameEntry[ec];
            for (int en = 0; en < EventNames.length; en++)
            {
                EventNames[en] = new TEventNameEntry();
                EventNames[en].eventName = aPayload.readString();
                EventNames[en].publishers = aPayload.readInt32();
                EventNames[en].subscribers = aPayload.readInt32();
                EventNames[en].timers = aPayload.readInt32();
            }
            this.onEventNames.dispatch(this, EventNames);
        }
    }

    private void handleSubAndPub(int aCommand, TByteBuffer aPayload)
    {
        String EventName;
        TEventEntry EE;
        TEventEntry EP;
        switch (aCommand)
        {
            case TEventEntry.IC_SUBSCRIBE:
            case TEventEntry.IC_PUBLISH:
                aPayload.readInt32(); // event id
                aPayload.readInt32(); // event entry type
                EventName = aPayload.readString();
                EE = findEventL(EventName);
                if (EE == null)
                {
                    // find parent
                    EP = findEventParentL(EventName);
                    if (EP != null)
                    {
                        EE = addEventL(EventName);
                        EE.fparent = EP;
                        EE.copyHandlersFrom(EP);
                    }
                }
                else
                {
                    if ((this.onSubAndPub != null) && !EE.isEmpty())
                        this.onSubAndPub.dispatch(this, aCommand, EventName);

                }
                if (EE != null)
                    EE.handleSubAndPub(aCommand);
                break;
            case TEventEntry.IC_UNSUBSCRIBE:
            case TEventEntry.IC_UNPUBLISH:
                EventName = aPayload.readString();
                if (this.onSubAndPub != null)
                    this.onSubAndPub.dispatch(this, aCommand, EventName);
                EE = findEventL(EventName);
                if (EE != null)
                    EE.handleSubAndPub(aCommand);
                break;
        }
    }

    protected void handleCommandOther(int aCommand, TByteBuffer aPayload)
    {
        // override to implement protocol extensions
    }

    private int requestUniqueClientID()
    {
        TByteBuffer Payload = new TByteBuffer();
        Payload.prepare((int) 0);
        Payload.prepare((int) 0);
        Payload.prepareApply();
        Payload.qWrite((int) 0);
        Payload.qWrite((int) 0);
        return writeCommand(TEventEntry.IC_UNIQUE_CLIENT_ID, Payload.getBuffer());
    }

    private int setOwner()
    {
        if (isConnected())
        {
            TByteBuffer Payload = new TByteBuffer();
            Payload.prepare(this.fownerID);
            Payload.prepare(this.fownerName);
            Payload.prepareApply();
            Payload.qWrite(this.fownerID);
            Payload.qWrite(this.fownerName);
            return writeCommand(TEventEntry.IC_SET_CLIENT_INFO, Payload.getBuffer());
        }
        else
            return ICE_CONNECTION_CLOSED;
    }

    private void readCommands()
    {
        // TODO: more like Delphi code
        int Command = TEventEntry.IC_INVALID_COMMAND;
        // define once
        // magic + command + payload size
        TByteBuffer FixedCommandPart =
                new TByteBuffer(MAGIC_BYTES.length + TByteBuffer.SIZE_OF_INT32 + TByteBuffer.SIZE_OF_INT32);
        TByteBuffer Payload = new TByteBuffer();
        TByteBuffer PayloadCheck = new TByteBuffer(TByteBuffer.SIZE_OF_INT32);
        do
        {
            try
            {
                try
                {
                    Command = readCommand(FixedCommandPart, Payload, PayloadCheck);
                    if (Command != TEventEntry.IC_INVALID_COMMAND)
                        handleCommand(Command, Payload);
                }
                catch (ThreadDeath ex)
                {
                    Command = TEventEntry.IC_END_OF_SESSION;
                }
            }
            catch (Exception ex)
            {
                if (isConnected())
                {
                    System.out.println("## Exception in ReadCommands loop: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }
        while ((Command != TEventEntry.IC_END_OF_SESSION) && isConnected());
    }

    protected enum TConnectionState
    {
        icsUninitialized(0),
        icsInitialized(1),
        icsClient(2),
        icsHub(3),
        icsEnded(4),
        // room for extensions ..
        // gateway values are used over network and should be same over all connected clients/brokers
        icsGateway(100), // equal
        icsGatewayClient(101), // this gateway acts as a client; subscribes are not received
        icsGatewayServer(102); // this gateway treats connected broker as client

        public final int value;

        TConnectionState(int aValue)
        {
            this.value = aValue;
        }
    }

    protected void setState(TConnectionState aState)
    {
        TByteBuffer Payload = new TByteBuffer();
        Payload.prepare(aState.ordinal());
        Payload.prepareApply();
        Payload.qWrite(aState.ordinal());
        writeCommand(TEventEntry.IC_SET_STATE, Payload.getBuffer());
    }

    protected boolean open(String aHost, int aPort)
    {
        return open(aHost, aPort, true);
    }

    protected boolean open(String aHost, int aPort, boolean aStartReadingThread)
    {
        close();
        try
        {
            this.fremoteHost = aHost;
            this.fremotePort = aPort;
            this.fsocket = new Socket(this.fremoteHost, this.fremotePort);
            if (this.fsocket.isConnected())
            {
                this.foutputStream = this.fsocket.getOutputStream();
                this.finputStream = this.fsocket.getInputStream();
                // FClient.Connect(FRemoteHost, FRemotePort);
                // FNetStream = FClient.GetStream();
                if (aStartReadingThread)
                {
                    this.freadingThread = new Thread(new Runnable()
                    {
                        public void run()
                        {
                            readCommands();
                        }
                    });
                    this.freadingThread.setName("imb command reader");
                    this.freadingThread.start();
                }
                if (this.imb2Compatible)
                    requestUniqueClientID();
                // SetState(State.icsClient);
                setOwner();
                // request all variables if delegates defined
                if (this.onVariable != null || this.onStatusUpdate != null)
                    writeCommand(TEventEntry.IC_ALL_VARIABLES, null);
            }
            return this.fsocket.isConnected();
        }
        catch (Exception ex)
        {
            return false;
        }
    }

    // publics

    /** returned object (array) on an event name list query */
    public class TEventNameEntry
    {
        public String eventName;

        public int publishers;

        public int subscribers;

        public int timers;
    }

    public enum TVarPrefix
    {
        vpUniqueClientID,
        vpClientHandle
    }

    // constants
    /** The maximum size of the payload in a low level IMB command */
    public static final int MAX_PAYLOAD_SIZE = 10 * 1024 * 1024; // in bytes

    /** value to be used when no specific remote server is used */
    public static final String DEFAULT_HUB = "localhost";

    /** value to be used when no specific port is used */
    public static final int DEFAULT_PORT = 4000;

    /** value to be used when no specific federation is used */
    public static final String DEFAULT_FEDERATION = "TNOdemo";

    // command results
    /** command result: the connection is closed */
    public static final int ICE_CONNECTION_CLOSED = -1;

    /** command result: the event was not published */
    public static final int ICE_EVENT_NOT_PUBLISHED = -2;

    // fields
    /** Returns the current federation */
    public String getFederation()
    {
        return this.ffederation;
    }

    /**
     * Set the current federation. All subscribed and published events are unsubscribed/unpublished, then the federation is
     * changed and all previously subscribed/publuished events are re-subscribed/re-published
     * @param aFederation String; the new federation
     */
    public void setFederation(String aFederation)
    {
        String OldFederation = this.ffederation;
        TEventEntry Event;
        if (isConnected() && (OldFederation != ""))
        {
            // un-publish and un-subscribe all
            for (int i = 0; i < this.feventEntryList.FCount; i++)
            {
                String EventName = this.feventEntryList.getEventName(i);
                if (!EventName.equals("") && EventName.startsWith(OldFederation + "."))
                {
                    Event = this.feventEntryList.getEventEntry(i);
                    if (Event.isSubscribed())
                        Event.unSubscribe(false);
                    if (Event.isPublished())
                        Event.unPublish(false);
                }
            }
        }
        this.ffederation = aFederation;
        if (isConnected() && (OldFederation != ""))
        {
            // publish and subscribe all
            for (int i = 0; i < this.feventEntryList.FCount; i++)
            {
                String EventName = this.feventEntryList.getEventName(i);
                if (!EventName.equals("") && EventName.startsWith(OldFederation + "."))
                {
                    Event = this.feventEntryList.getEventEntry(i);
                    Event.feventName = this.ffederation + Event.feventName.substring(0, OldFederation.length());
                    if (Event.isSubscribed())
                        Event.subscribe();
                    if (Event.isPublished())
                        Event.publish();
                }
            }
        }
    }

    /** when true events send on not-publuished events are automatically published */
    public boolean autoPublish = true;

    // TODO: check what should be the default, for now backwards compatible?
    /** when true IMB2 features are used if possible to emulate IMB3 behavior */
    public boolean imb2Compatible = true;

    // connection
    /** Returns the IP address or DNS name of the currently connected hub */
    public String getRemoteHost()
    {
        return this.fremoteHost;
    }

    /** Returns the TCP port of the currently connected hub */
    public int getRemotePort()
    {
        return this.fremotePort;
    }

    /**
     * Returns the state of the NAGLE algorithm on the connected socket
     * @return if true NAGLE is disabled (default false)
     * @throws SocketException
     */
    public boolean getNoDelay() throws SocketException
    {
        if (isConnected())
            return this.fsocket.getTcpNoDelay();
        else
            return false;
    }

    /**
     * Sets the state of the NAGLE algorithm on the socket
     * @param aValue boolean; if true the NAGLE algorithm is DISABLED (default false)
     * @throws SocketException
     */
    public void setNoDelay(boolean aValue) throws SocketException
    {
        if (isConnected())
            this.fsocket.setTcpNoDelay(aValue);
    }

    /**
     * Returns the status of the linger option on the connected socket
     * @return if true the linger option is enabled
     * @throws SocketException
     */
    public boolean getLinger() throws SocketException
    {
        if (isConnected())
            return this.fsocket.getSoLinger() != -1;
        else
            return false;
    }

    /**
     * Sets the status of the linger option on the connected socket
     * @param aValue boolean; if true the linger option is enabled with a linger time of 2 seconds
     * @throws SocketException
     */
    public void setLinger(boolean aValue) throws SocketException
    {
        if (isConnected())
            this.fsocket.setSoLinger(aValue, 2); // set linger time to 2 seconds
    }

    /** Returns the connected state of the connection */
    public boolean isConnected()
    {
        return (this.fsocket != null) && this.fsocket.isConnected();
    }

    /** Closes the connection and cleans up socket, streams and thread */
    public void close()
    {
        if ((this.fsocket != null) && this.fsocket.isConnected())
        {
            if (this.onDisconnect != null)
                this.onDisconnect.dispatch(this);
            writeCommand(TEventEntry.IC_END_OF_SESSION, null);
            try
            {
                this.foutputStream.close();
                this.foutputStream = null;
                this.finputStream.close();
                this.finputStream = null;
                this.fsocket.close();
                this.fsocket = null;
                this.freadingThread = null;
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /** Override dispatch to implement a disconnect handler */
    public interface TOnDisconnect
    {
        public void dispatch(TConnection aConnection);
    }

    /** Handler to be called on a disconnect */
    public TOnDisconnect onDisconnect = null;

    /**
     * Throttle down buffer events send to this client if specific flags are set on events
     * @param aThrottle int;
     */
    public void setThrottle(int aThrottle)
    {
        TByteBuffer Payload = new TByteBuffer();
        Payload.prepare(aThrottle);
        Payload.prepareApply();
        Payload.qWrite(aThrottle);
        writeCommand(TEventEntry.IC_SET_THROTTLE, Payload.getBuffer());
    }

    /**
     * Manually reading commands when not using a reader thread. Commands are read until connection is idle.
     * @throws IOException
     */
    public void readCommandsNonBlocking() throws IOException
    {
        if (this.finputStream.available() != 0)
        {
            int Command = TEventEntry.IC_INVALID_COMMAND;
            // define once
            // magic + command + payload size
            TByteBuffer FixedCommandPart =
                    new TByteBuffer(MAGIC_BYTES.length + TByteBuffer.SIZE_OF_INT32 + TByteBuffer.SIZE_OF_INT32);
            TByteBuffer Payload = new TByteBuffer();
            TByteBuffer PayloadCheck = new TByteBuffer(TByteBuffer.SIZE_OF_INT32);
            do
            {
                try
                {
                    try
                    {
                        Command = readCommand(FixedCommandPart, Payload, PayloadCheck);
                        if (Command != TEventEntry.IC_INVALID_COMMAND)
                            handleCommand(Command, Payload);
                    }
                    catch (ThreadDeath ex)
                    {
                        Command = TEventEntry.IC_END_OF_SESSION;
                    }
                }
                catch (Exception ex)
                {
                    if (isConnected())
                        System.out.println("## Exception in ReadCommands loop: " + ex.getMessage());
                }
            }
            while ((Command != TEventEntry.IC_END_OF_SESSION) && isConnected() && (this.finputStream.available() != 0));
        }
    }

    /**
     * Manually reading commands when not using a reader thread. Commands are processed until the reading on the connection
     * times out
     * @param aTimeOut int;
     * @throws SocketException
     */
    public void readCommandsNonThreaded(int aTimeOut) throws SocketException
    {
        this.fsocket.setSoTimeout(aTimeOut);
        int Command = TEventEntry.IC_INVALID_COMMAND;
        // define once
        // magic + command + payload size
        TByteBuffer FixedCommandPart =
                new TByteBuffer(MAGIC_BYTES.length + TByteBuffer.SIZE_OF_INT32 + TByteBuffer.SIZE_OF_INT32);
        TByteBuffer Payload = new TByteBuffer();
        TByteBuffer PayloadCheck = new TByteBuffer(TByteBuffer.SIZE_OF_INT32);
        do
        {
            try
            {
                try
                {
                    Command = readCommand(FixedCommandPart, Payload, PayloadCheck);
                    if (Command != TEventEntry.IC_INVALID_COMMAND)
                        handleCommand(Command, Payload);
                }
                catch (ThreadDeath ex)
                {
                    Command = TEventEntry.IC_END_OF_SESSION;
                }
            }
            catch (Exception ex)
            {
                if (isConnected())
                    System.out.println("## Exception in ReadCommands loop: " + ex.getMessage());
            }
        }
        while ((Command != TEventEntry.IC_END_OF_SESSION) && isConnected());
    }

    // owner
    /** Returns the currently specified owner id */
    public int getOwnerID()
    {
        return this.fownerID;
    }

    /**
     * Changes the owner id
     * @param aValue int; the new owner id
     */
    public void setOwnerID(int aValue)
    {
        if (this.fownerID != aValue)
        {
            this.fownerID = aValue;
            setOwner();
        }
    }

    /** Returns the currently specified owner name */
    public String getOwnerName()
    {
        return this.fownerName;
    }

    /**
     * Changes the owner name
     * @param aValue String; the new owner name
     */
    public void setOwnerName(String aValue)
    {
        if (this.fownerName != aValue)
        {
            this.fownerName = aValue;
            setOwner();
        }
    }

    /** Returns the unique client id the hub assigned to this connection */
    public int getUniqueClientID()
    {
        return this.funiqueClientID;
    }

    /** Returns the client handle the hub assigned to this connection */
    public int getClientHandle()
    {
        return this.fclientHandle;
    }

    // subscribe/publish
    /**
     * Subscribe to the specified event
     * @param aEventName String; the event name to subscribe to (it will be prefixed with the current federation)
     * @return event entry that is to be used to assign the handler for the received events
     */
    public TEventEntry subscribe(String aEventName)
    {
        return subscribe(aEventName, true);
    }

    /**
     * Subscribe to the specified event
     * @param aEventName String; the event name to subscribe to
     * @param aUseFederationPrefix boolean; if true the given event name will be prefixed with the current federation
     * @return event entry that is to be used to assign the handler for the received events
     */
    public TEventEntry subscribe(String aEventName, boolean aUseFederationPrefix)
    {
        TEventEntry Event = findOrAddEventL(prefixFederation(aEventName, aUseFederationPrefix));
        if (!Event.isSubscribed())
            Event.subscribe();
        return Event;
    }

    /**
     * Publishes on the specified event
     * @param aEventName String; the event name to publish on (it will be prefixed with the current federation)
     * @return event entry that is to be used to signal events on
     */
    public TEventEntry publish(String aEventName)
    {
        return publish(aEventName, true);
    }

    /**
     * Publishes on the specified event
     * @param aEventName String; the event name to publish on
     * @param aUseFederationPrefix boolean; if true the given event name will be prefixed with the current federation
     * @return event entry that is to be used to signal events on
     */
    public TEventEntry publish(String aEventName, boolean aUseFederationPrefix)
    {
        TEventEntry Event = findOrAddEventL(prefixFederation(aEventName, aUseFederationPrefix));
        if (!Event.isPublished())
            Event.publish();
        return Event;
    }

    /**
     * Unsubscribe from the specified event
     * @param aEventName String; the event name to unsubscribe from (it will be prefixed with the current federation)
     */
    public void unSubscribe(String aEventName)
    {
        unSubscribe(aEventName, true);
    }

    /**
     * Unsubscribe from the specified event
     * @param aEventName String; the event name to unsubscribe from
     * @param aUseFederationPrefix boolean; if true the given event name will be prefixed with the current federation
     */
    public void unSubscribe(String aEventName, boolean aUseFederationPrefix)
    {
        TEventEntry Event = findEventL(prefixFederation(aEventName, aUseFederationPrefix));
        if (Event != null && Event.isSubscribed())
            Event.unSubscribe(true);
    }

    /**
     * Unpublish on the specified event.
     * @param aEventName String; the event name to unpublish on (it will be prefixed with the current federation)
     */
    public void unPublish(String aEventName)
    {
        unPublish(aEventName, true);
    }

    /**
     * Unpublish on the specified event.
     * @param aEventName String; the event name to unpublish on
     * @param aUseFederationPrefix boolean; if true the given event name will be prefixed with the current federation
     */
    public void unPublish(String aEventName, boolean aUseFederationPrefix)
    {
        TEventEntry Event = findEventL(prefixFederation(aEventName, aUseFederationPrefix));
        if (Event != null && Event.isPublished())
            Event.unPublish(true);
    }

    /**
     * Send an event to the framework. This is the simple way to send events. More performance can be gained by using the
     * returned event entry from publish().
     * @param aEventName String;
     * @param aEventKind int;
     * @param aEventPayload TByteBuffer;
     * @return result of the command (see ICE_* constants)
     */
    public int signalEvent(String aEventName, int aEventKind, TByteBuffer aEventPayload)
    {
        return signalEvent(aEventName, aEventKind, aEventPayload, true);
    }

    /**
     * Send an event to the framework. This is the simple way to send events. More performance can be gained by using the
     * returned event entry from publish().
     * @param aEventName String;
     * @param aEventKind int;
     * @param aEventPayload TByteBuffer;
     * @param aUseFederationPrefix boolean; if true the given event name will be prefixed with the current federation
     * @return result of the command (see ICE_* constants)
     */
    public int signalEvent(String aEventName, int aEventKind, TByteBuffer aEventPayload, boolean aUseFederationPrefix)
    {
        TEventEntry Event = findEventAutoPublishL(prefixFederation(aEventName, aUseFederationPrefix));
        if (Event != null)
            return Event.signalEvent(aEventKind, aEventPayload.getBuffer());
        else
            return ICE_EVENT_NOT_PUBLISHED;
    }

    /**
     * Send a buffer event to the framework. This is the simple way to send events. More performance can be gained by using the
     * returned event entry from publish().
     * @param aEventName String;
     * @param aBufferID int;
     * @param aBuffer byte[];
     * @return result of the command (see ICE_* constants)
     */
    public int signalBuffer(String aEventName, int aBufferID, byte[] aBuffer)
    {
        return signalBuffer(aEventName, aBufferID, aBuffer, 0, true);
    }

    /**
     * Send a buffer event to the framework. This is the simple way to send events. More performance can be gained by using the
     * returned event entry from publish().
     * @param aEventName String;
     * @param aBufferID int;
     * @param aBuffer byte[];
     * @param aEventFlags int;
     * @param aUseFederationPrefix boolean; if true the given event name will be prefixed with the current federation
     * @return result of the command (see ICE_* constants)
     */
    public int signalBuffer(String aEventName, int aBufferID, byte[] aBuffer, int aEventFlags, boolean aUseFederationPrefix)
    {
        TEventEntry Event = findEventAutoPublishL(prefixFederation(aEventName, aUseFederationPrefix));
        if (Event != null)
            return Event.signalBuffer(aBufferID, aBuffer, aEventFlags);
        else
            return ICE_EVENT_NOT_PUBLISHED;
    }

    /**
     * Send a ChangeObject event to the framework This is the simple way to send events. More performance can be gained by using
     * the returned event entry from publish().
     * @param aEventName String;
     * @param aAction int;
     * @param aObjectID int;
     * @param aAttribute String;
     * @return result of the command (see ICE_* constants)
     */
    public int signalChangeObject(String aEventName, int aAction, int aObjectID, String aAttribute)
    {
        return signalChangeObject(aEventName, aAction, aObjectID, aAttribute, true);
    }

    /**
     * Send a ChangeObject event to the framework This is the simple way to send events. More performance can be gained by using
     * the returned event entry from publish().
     * @param aEventName String;
     * @param aAction int;
     * @param aObjectID int;
     * @param aAttribute String;
     * @param aUseFederationPrefix boolean; if true the given event name will be prefixed with the current federation
     * @return result of the command (see ICE_* constants)
     */
    public int signalChangeObject(String aEventName, int aAction, int aObjectID, String aAttribute,
            boolean aUseFederationPrefix)
    {
        TEventEntry Event = findEventAutoPublishL(prefixFederation(aEventName, aUseFederationPrefix));
        if (Event != null)
            return Event.signalChangeObject(aAction, aObjectID, aAttribute);
        else
            return ICE_EVENT_NOT_PUBLISHED;
    }

    /**
     * Send a stream to the framework
     * @param aEventName String;
     * @param aStreamName String; name of the stream to identify the stream by the receiver
     * @param aStream InputStream;
     * @return result of the command (see ICE_* constants)
     */
    public int signalStream(String aEventName, String aStreamName, InputStream aStream)
    {
        return signalStream(aEventName, aStreamName, aStream, true);
    }

    /**
     * Send a stream to the framework
     * @param aEventName String;
     * @param aStreamName String;
     * @param aStream InputStream;
     * @param aUseFederationPrefix boolean;
     * @return result of the command (see ICE_* constants)
     */
    public int signalStream(String aEventName, String aStreamName, InputStream aStream, boolean aUseFederationPrefix)
    {
        TEventEntry Event = findEventAutoPublishL(prefixFederation(aEventName, aUseFederationPrefix));
        if (Event != null)
            return Event.signalStream(aStreamName, aStream);
        else
            return ICE_EVENT_NOT_PUBLISHED;
    }

    // variables
    /** Override dispatch to implement a variable change handler */
    public interface TOnVariable
    {
        public void dispatch(TConnection aConnection, String aVarName, byte[] aVarValue, byte[] aPrevValue);
    }

    // extra calls when adding a delegate to OnVariable: requestAllVariables();
    /** Handler to be called on a variable change */
    private TOnVariable onVariable = null;

    /**
     * Set the callback handler for framework variable changes
     * @param aValue TOnVariable;
     */
    public void setOnVariable(TOnVariable aValue)
    {
        this.onVariable = aValue;
        requestAllVariables();
    }

    /** Send a request to the framework to send all variables with their contents to this client */
    protected void requestAllVariables()
    {
        writeCommand(TEventEntry.IC_ALL_VARIABLES, null); // request all variables for initial values
    }

    /**
     * Set the value of a global framework variable
     * @param aVarName String;
     * @param aVarValue String;
     */
    public void setVariableValue(String aVarName, String aVarValue)
    {
        TByteBuffer Payload = new TByteBuffer();
        Payload.prepare(aVarName);
        Payload.prepare(aVarValue);
        Payload.prepareApply();
        Payload.qWrite(aVarName);
        Payload.qWrite(aVarValue);
        writeCommand(TEventEntry.IC_SET_VARIABLE, Payload.getBuffer());
    }

    /**
     * Set the value of a global framework variable
     * @param aVarName String;
     * @param aVarValue TByteBuffer;
     */
    public void setVariableValue(String aVarName, TByteBuffer aVarValue)
    {
        TByteBuffer Payload = new TByteBuffer();
        Payload.prepare(aVarName);
        Payload.prepare(aVarValue);
        Payload.prepareApply();
        Payload.qWrite(aVarName);
        Payload.qWrite(aVarValue);
        writeCommand(TEventEntry.IC_SET_VARIABLE, Payload.getBuffer());
    }

    /**
     * Set the value of a global framework variable
     * @param aVarName String;
     * @param aVarValue String;
     * @param aVarPrefix TVarPrefix;
     */
    public void setVariableValue(String aVarName, String aVarValue, TVarPrefix aVarPrefix)
    {
        TByteBuffer Payload = new TByteBuffer();
        Payload.prepare(aVarPrefix.ordinal());
        Payload.prepare(aVarName);
        Payload.prepare(aVarValue);
        Payload.prepareApply();
        Payload.qWrite(aVarPrefix.ordinal());
        Payload.qWrite(aVarName);
        Payload.qWrite(aVarValue);
        writeCommand(TEventEntry.IC_SET_VARIABLE_PREFIXED, Payload.getBuffer());
    }

    /**
     * Set the value of a global framework variable
     * @param aVarName String;
     * @param aVarValue TByteBuffer;
     * @param aVarPrefix TVarPrefix;
     */
    public void setVariableValue(String aVarName, TByteBuffer aVarValue, TVarPrefix aVarPrefix)
    {
        TByteBuffer Payload = new TByteBuffer();
        Payload.prepare(aVarPrefix.ordinal());
        Payload.prepare(aVarName);
        Payload.prepare(aVarValue);
        Payload.prepareApply();
        Payload.qWrite(aVarPrefix.ordinal());
        Payload.qWrite(aVarName);
        Payload.qWrite(aVarValue);
        writeCommand(TEventEntry.IC_SET_VARIABLE_PREFIXED, Payload.getBuffer());
    }

    /** Override dispatch to implement a status change handler */
    public interface TOnStatusUpdate
    {
        public void dispatch(TConnection aConnection, String aModelUniqueClientID, String aModelName, int aProgress,
                int aStatus);
    }

    // TODO: extra calls when adding a delegate to OnVariable: RequestAllVariables();
    /** Handler to be called on a status update */
    private TOnStatusUpdate onStatusUpdate = null;

    /**
     * Set the callback handler for status updates
     * @param aValue TOnStatusUpdate;
     */
    public void setOnStatusUpdate(TOnStatusUpdate aValue)
    {
        this.onStatusUpdate = aValue;
        requestAllVariables();
    }

    // status for UpdateStatus
    /** signal client status: ready (see updateStatus) */
    public final static int STATUS_READY = 0; // R

    /** signal client status: calculating (see updateStatus) */
    public final static int STATUS_CALCULATING = 1; // C

    /** signal client status: busy (see updateStatus) */
    public final static int STATUS_BUSY = 2; // B

    /**
     * Update the central status for this client
     * @param aProgress int; the progress, if available, from 0 to 100 or counting down to 0
     * @param aStatus int; the current status of the client (see STATUS_* constants)
     * @throws InterruptedException
     */
    public void updateStatus(int aProgress, int aStatus) throws InterruptedException
    {
        TByteBuffer Payload = new TByteBuffer();
        Payload.prepare(aStatus);
        Payload.prepare(aProgress);
        Payload.prepareApply();
        Payload.qWrite(aStatus);
        Payload.qWrite(aProgress);
        if (this.imb2Compatible)
        {
            // wait for unique client id
            if (this.funiqueClientID == 0)
            {
                int SpinCount = 10; // 10*500 ms
                while (this.funiqueClientID == 0 && SpinCount > 0)
                {
                    Thread.sleep(500);
                    SpinCount--;
                }
            }
            // set variable using unique client id
            setVariableValue(Integer.toHexString(this.funiqueClientID) + prefixFederation(this.fownerName).toUpperCase()
                    + MODEL_STATUS_VAR_SEP_CHAR + MODEL_Status_VAR_NAME, Payload);
        }
        else
            setVariableValue(
                    prefixFederation(this.fownerName).toUpperCase() + MODEL_STATUS_VAR_SEP_CHAR + MODEL_Status_VAR_NAME,
                    Payload, TVarPrefix.vpUniqueClientID);
    }

    /** Removes the current status for this client */
    public void removeStatus()
    {
        if (this.imb2Compatible)

            setVariableValue(Integer.toHexString(this.funiqueClientID) + prefixFederation(this.fownerName)
                    + MODEL_STATUS_VAR_SEP_CHAR + MODEL_Status_VAR_NAME, "");
        else
            setVariableValue(prefixFederation(this.fownerName) + MODEL_STATUS_VAR_SEP_CHAR + MODEL_Status_VAR_NAME, "",
                    TVarPrefix.vpUniqueClientID);
    }

    // TODO: delegates
    /**
     * Subscribe to focus events and registers the callback handler for these events.
     * @param aOnFocus TEventEntry.TOnFocus; callback event handler
     */
    public void subscribeOnFocus(TEventEntry.TOnFocus aOnFocus)
    {
        if (this.ffocusEvent == null)
            this.ffocusEvent = subscribe(FOCUS_EVENT_NAME);
        this.ffocusEvent.onFocus = aOnFocus;
    }

    /**
     * Signal a new focus point to the framework
     * @param aX double;
     * @param aY double;
     * @return result of the command (see ICE_* constants)
     */
    public int signalFocus(double aX, double aY)
    {
        if (this.ffocusEvent == null)
            this.ffocusEvent = findEventAutoPublishL(prefixFederation(FOCUS_EVENT_NAME));
        if (this.ffocusEvent != null)
        {
            TByteBuffer Payload = new TByteBuffer();
            Payload.prepare(aX);
            Payload.prepare(aY);
            Payload.prepareApply();
            Payload.qWrite(aX);
            Payload.qWrite(aY);
            return this.ffocusEvent.signalEvent(TEventEntry.EK_CHANGE_OBJECT_EVENT, Payload.getBuffer());
        }
        else
            return ICE_EVENT_NOT_PUBLISHED;
    }

    // IMB 2 change federation
    /**
     * Subscribe to federation change events and register the callback handler for these events
     * @param aOnChangeFederation TEventEntry.TOnChangeFederation;
     */
    public void subscribeOnFederationChange(TEventEntry.TOnChangeFederation aOnChangeFederation)
    {
        if (this.fchangeFederationEvent == null)
            this.fchangeFederationEvent = subscribe(CHANGE_FEDERATION_EVENT_NAME);
        this.fchangeFederationEvent.onChangeFederation = aOnChangeFederation;
    }

    /**
     * Signal a new federation to the framework
     * @param aNewFederationID int;
     * @param aNewFederation String;
     * @return result of the command (see ICE_* constants)
     */
    public int signalChangeFederation(int aNewFederationID, String aNewFederation)
    {
        if (this.fchangeFederationEvent == null)
            this.fchangeFederationEvent = findEventAutoPublishL(prefixFederation(CHANGE_FEDERATION_EVENT_NAME));
        if (this.fchangeFederationEvent != null)
            return this.fchangeFederationEvent.signalChangeObject(TEventEntry.ACTION_CHANGE, aNewFederationID, aNewFederation);
        else
            return ICE_EVENT_NOT_PUBLISHED;
    }

    // log
    /**
     * Log an entry to the framework
     * @param aLogEventName String;
     * @param aLine String;
     * @param aLevel TEventEntry.TLogLevel;
     * @return result of the command (see ICE_* constants)
     */
    public int logWriteLn(String aLogEventName, String aLine, TEventEntry.TLogLevel aLevel)
    {
        if (this.flogEvent == null)
            this.flogEvent = findEventAutoPublishL(prefixFederation(aLogEventName));
        if (this.flogEvent != null)
            return this.flogEvent.logWriteLn(aLine, aLevel);
        else
            return ICE_EVENT_NOT_PUBLISHED;
    }

    // remote event info

    // TODO: delegates
    /** Override dispatch to implement an event names request callback handler */
    public interface TOnEventnames
    {
        public void dispatch(TConnection aConnection, TEventNameEntry[] aEventNames);
    }

    /** Handler to be called on a event names request callback */
    public TOnEventnames onEventNames = null;

    // TODO: description
    public interface TOnSubAndPub
    {
        public void dispatch(TConnection aConnection, int aCommand, String aEventName);
    }

    // TODO: description
    public TOnSubAndPub onSubAndPub = null;

    // event filters
    /** request event name filter: requesting publisher counts */
    public static final int EF_PUBLISHERS = 1;

    /** request event name filter: requesting subscriber counts */
    public static final int EF_SUBSCRIBERS = 2;

    /** request event name filter: requesting timer counts */
    public static final int EF_TIMERS = 4;

    /**
     * Query the framework for registered event names
     * @param aEventNameFilter String;
     * @param aEventFilters int;
     * @return result of the command (see ICE_* constants)
     */
    public int requestEventname(String aEventNameFilter, int aEventFilters)
    {
        TByteBuffer Payload = new TByteBuffer();
        Payload.prepare(aEventNameFilter);
        Payload.prepare(aEventFilters);
        Payload.prepareApply();
        Payload.qWrite(aEventNameFilter);
        Payload.qWrite(aEventFilters);
        return writeCommand(TEventEntry.IC_REQUEST_EVENT_NAMES, Payload.getBuffer());
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "TConnection [remoteHost=" + this.fremoteHost + ", remotePort=" + this.fremotePort + ", federation="
                + this.ffederation + ", isConnected()=" + this.isConnected() + "]";
    }
}
