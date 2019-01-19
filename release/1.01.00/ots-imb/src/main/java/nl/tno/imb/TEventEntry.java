package nl.tno.imb;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * This class implements the events that can be send and received over the IMB framework.<br>
 * Handlers can be attached to events that get called when specified events are received.
 * @author hans.cornelissen@tno.nl
 */
public class TEventEntry
{
    // other sub classes
    TEventEntry(TConnection aConnection, int aID, String aEventName)
    {
        this.connection = aConnection;
        this.ID = aID;
        this.feventName = aEventName;
        this.fparent = null;
        this.fisPublished = false;
        this.fisSubscribed = false;
    }

    static final int IC_INVALID_COMMAND = -1; // to signal corrupt command
    // private static final int icHeartBeat = -4;

    static final int IC_END_OF_SESSION = -5;

    // private static final int icFlushQueue = -6;
    static final int IC_UNIQUE_CLIENT_ID = -7;
    // private static final int icTimeStamp = -8;

    static final int IC_EVENT = -15;

    // private static final int icEndClientSession = -21;
    // private static final int icFlushClientQueue = -22;
    // private static final int icConnectToGateway = -23;

    static final int IC_SET_CLIENT_INFO = -31;

    static final int IC_SET_VARIABLE = -32;

    static final int IC_ALL_VARIABLES = -33;

    static final int IC_SET_STATE = -34;

    static final int IC_SET_THROTTLE = -35;

    // private static final int icsSetNoDelay = -36;
    static final int IC_SET_VARIABLE_PREFIXED = -37;

    static final int IC_REQUEST_EVENT_NAMES = -41;

    static final int IC_EVENT_NAMES = -42;
    // private static final int icRequestSubscribers = -43;
    // private static final int icRequestPublishers = -44;

    static final int IC_SUBSCRIBE = -45;

    static final int IC_UNSUBSCRIBE = -46;

    static final int IC_PUBLISH = -47;

    static final int IC_UNPUBLISH = -48;

    static final int IC_SET_EVENT_ID_TRANSLATION = -49;

    // private static final int icStatusEvent = -52;
    // private static final int icStatusClient = -53;
    // private static final int icStatusEventPlus = -54;
    // private static final int icStatusClientPlus = -55;
    // private static final int icStatusHUB = -56;
    // private static final int icStatusTimer = -57;

    // private static final int icHumanReadableHeader = -60;
    // private static final int icSetMonitor = -61;
    // private static final int icResetMonitor = -62;

    static final int IC_CREATE_TIMER = -73;

    // locator commands = UDP)
    static final int IC_HUB_LOCATE = -81;

    static final int IC_HUB_FOUND = -82;

    // private static final int icLogClear = -91;
    // private static final int icLogRequest = -92;
    // private static final int icLogContents = -93;

    // TEventKind
    // IMB version 1
    /** event kind: change object */
    public static final int EK_CHANGE_OBJECT_EVENT = 0;

    // IMB version 2
    /** event kind: header of a stream */
    public static final int EK_STREAM_HEADER = 1;

    /** event kind: body of a stream */
    public static final int EK_STREAM_BODY = 2;

    /** event kind: end of a stream */
    public static final int EK_STREAM_TAIL = 3;

    /** event kind: buffer event */
    public static final int EK_BUFFER = 4;

    /** event kind: normal event */
    public static final int EK_NORMAL_EVENT = 5;

    // IMB version 3
    /** event kind: change object including changed data */
    public static final int EK_CHANGE_OBJECT_DATA_EVENT = 6;

    /** event kind: a child event was created */
    public static final int EK_CHILD_EVENT_ADD = 11;

    /** event kind: a child event was removed */
    public static final int EK_CHILD_EVENT_REMOVE = 12;

    /** event kind: send a line to the log */
    public static final int EK_LOG_WRITELN = 30;

    /** event kind: cancel/remove a running timer */
    public static final int EK_TIMER_CANCEL = 40;

    /** event kind: reset a timer */
    public static final int EK_TIMER_PREPARE = 41;

    /** event kind: start or continue a timer */
    public static final int EK_TIMER_START = 42;

    /** event kind: stop a running timer */
    public static final int EK_TIMER_STOP = 43;

    /** event kind: add client to the acknowledge list of a timer */
    public static final int EK_TIMER_ACKNOWLEDGE_LIST_ADD = 45;

    /** event kind: remove client from the acknowledge list of a timer */
    public static final int EK_TIMER_ACKNOWLEDGE_LIST_REMOVE = 46;

    /** event kind: set the relative speed of the timer */
    public static final int EK_TIMER_SET_SPEED = 47;

    /** event kind: timer tick */
    public static final int EK_TIMER_TICK = 48;

    /** event kind: acknowledge timer tick */
    public static final int EK_TIMER_ACKNOWLEDGE = 49;

    /** event kind: request status update of a timer */
    public static final int EK_TIMER_STATUS_REQUEST = 50;

    /** defines the type of log entry to send */
    public enum TLogLevel
    {
        llRemark,
        llDump,
        llNormal,
        llStart,
        llFinish,
        llPush,
        llPop,
        llStamp,
        llSummary,
        llWarning,
        llError
    }

    /** no limit on the number of timer events to send */
    public static final int TRC_INFINITE = Integer.MAX_VALUE;

    /** the maximum size a stream body or stream tail data part may be */
    private static final int MAX_STREAM_BODY_BUFFER_SIZE = 16 * 1024;

    // private/internal
    private static final int EVENT_KIND_MASK = 0x000000FF;

    private static final int EVENT_FLAGS_MASK = 0x0000FF00;

    private class TStreamCacheEntry
    {
        private int fstreamID;

        private OutputStream fstream;

        private String fname;

        public TStreamCacheEntry(int aStreamID, OutputStream aStream, String aStreamName)
        {
            this.fstreamID = aStreamID;
            this.fstream = aStream;
            this.fname = aStreamName;
        }
    }

    private class TStreamCache
    {
        private List<TStreamCacheEntry> fstreamCacheList = new ArrayList<TStreamCacheEntry>();

        public TStreamCacheEntry find(int aStreamID)
        {
            for (int i = 0; i < this.fstreamCacheList.size(); i++)
            {
                TStreamCacheEntry sce = this.fstreamCacheList.get(i);
                if (sce.fstreamID == aStreamID)
                    return sce;
            }
            return null;
        }

        public void cache(int aStreamID, OutputStream aStream, String aStreamName)
        {
            this.fstreamCacheList.add(new TStreamCacheEntry(aStreamID, aStream, aStreamName));
        }

        public void remove(int aStreamID)
        {
            int i = 0;
            while ((i < this.fstreamCacheList.size()) && (this.fstreamCacheList.get(i).fstreamID != aStreamID))
                i++;
            if (i < this.fstreamCacheList.size())
                this.fstreamCacheList.remove(i);
        }
    }

    private boolean fisPublished;

    private boolean fisSubscribed;

    String feventName; // scope=package

    TEventEntry fparent;

    private TStreamCache fstreamCache = new TStreamCache();

    private int timerBasicCmd(int aEventKind, String aTimerName)
    {
        TByteBuffer Payload = new TByteBuffer();
        Payload.prepare(aTimerName);
        Payload.prepareApply();
        Payload.qWrite(aTimerName);
        return signalEvent(aEventKind, Payload.getBuffer());
    }

    private int timerAcknowledgeCmd(int aEventKind, String aTimerName, String aClientName)
    {
        TByteBuffer Payload = new TByteBuffer();
        Payload.prepare(aTimerName);
        Payload.prepare(aClientName);
        Payload.prepareApply();
        Payload.qWrite(aTimerName);
        Payload.qWrite(aClientName);
        return signalEvent(aEventKind, Payload.getBuffer());
    }

    void subscribe()
    {
        this.fisSubscribed = true;
        // send command
        TByteBuffer Payload = new TByteBuffer();
        Payload.prepare(this.ID);
        Payload.prepare(0); // EET
        Payload.prepare(getEventName());
        Payload.prepareApply();
        Payload.qWrite(this.ID);
        Payload.qWrite(0); // EET
        Payload.qWrite(getEventName());
        this.connection.writeCommand(IC_SUBSCRIBE, Payload.getBuffer());
    }

    void publish()
    {
        this.fisPublished = true;
        // send command
        TByteBuffer Payload = new TByteBuffer();
        Payload.prepare(this.ID);
        Payload.prepare(0); // EET
        Payload.prepare(getEventName());
        Payload.prepareApply();
        Payload.qWrite(this.ID);
        Payload.qWrite(0); // EET
        Payload.qWrite(getEventName());
        this.connection.writeCommand(IC_PUBLISH, Payload.getBuffer());
    }

    boolean isEmpty()
    {
        return !(this.fisSubscribed || this.fisPublished);
    }

    private boolean fSubscribers;

    private boolean fPublishers;

    public boolean subscribers()
    {
        return this.fSubscribers;
    }

    public boolean publishers()
    {
        return this.fPublishers;
    }

    void unSubscribe(boolean aChangeLocalState)
    {
        if (aChangeLocalState)
            this.fisSubscribed = false;
        // send command
        TByteBuffer Payload = new TByteBuffer();
        Payload.prepare(getEventName());
        Payload.prepareApply();
        Payload.qWrite(getEventName());
        this.connection.writeCommand(IC_UNSUBSCRIBE, Payload.getBuffer());
    }

    void unPublish(boolean aChangeLocalState)
    {
        if (aChangeLocalState)
            this.fisPublished = false;
        // send command
        TByteBuffer Payload = new TByteBuffer();
        Payload.prepare(getEventName());
        Payload.prepareApply();
        Payload.qWrite(getEventName());
        this.connection.writeCommand(IC_UNPUBLISH, Payload.getBuffer());
    }

    // dispatcher for all events
    void handleEvent(TByteBuffer aPayload)
    {
        int EventTick;
        int EventKindInt;
        EventTick = aPayload.readInt32();
        EventKindInt = aPayload.readInt32();
        int eventKind = EventKindInt & EVENT_KIND_MASK;
        switch (eventKind)
        {
            case EK_CHANGE_OBJECT_EVENT:
                handleChangeObject(aPayload);
                break;
            case EK_CHANGE_OBJECT_DATA_EVENT:
                handleChangeObjectData(aPayload);
                break;
            case EK_BUFFER:
                handleBuffer(EventTick, aPayload);
                break;
            case EK_NORMAL_EVENT:
                if (this.onNormalEvent != null)
                    this.onNormalEvent.dispatch(this, aPayload);
                break;
            case EK_TIMER_TICK:
                handleTimerTick(aPayload);
                break;
            case EK_TIMER_PREPARE:
                handleTimerCmd(EK_TIMER_PREPARE, aPayload);
                break;
            case EK_TIMER_START:
                handleTimerCmd(EK_TIMER_START, aPayload);
                break;
            case EK_TIMER_STOP:
                handleTimerCmd(EK_TIMER_STOP, aPayload);
                break;
            case EK_STREAM_HEADER:
                handleStreamEvent(EK_STREAM_HEADER, aPayload);
                break;
            case EK_STREAM_BODY:
                handleStreamEvent(EK_STREAM_BODY, aPayload);
                break;
            case EK_STREAM_TAIL:
                handleStreamEvent(EK_STREAM_TAIL, aPayload);
                break;
            case EK_CHILD_EVENT_ADD:
                handleChildEvent(EK_CHILD_EVENT_ADD, aPayload);
                break;
            case EK_CHILD_EVENT_REMOVE:
                handleChildEvent(EK_CHILD_EVENT_REMOVE, aPayload);
                break;
            default:
                if (this.onOtherEvent != null)
                    this.onOtherEvent.dispatch(this, EventTick, eventKind, aPayload);
                break;
        }

    }

    // dispatchers for specific events
    private void handleChangeObject(TByteBuffer aPayload)
    {
        if (this.onFocus != null)
        {
            double X;
            double Y;
            X = aPayload.readDouble();
            Y = aPayload.readDouble();
            this.onFocus.dispatch(X, Y);
        }
        else
        {
            if (this.onChangeFederation != null)
            {
                aPayload.readInt32(); // read action, not used
                int NewFederationID = aPayload.readInt32();
                String NewFederation = aPayload.readString();
                this.onChangeFederation.dispatch(this.connection, NewFederationID, NewFederation);
            }
            else
            {
                if (this.onChangeObject != null)
                {
                    int Action = aPayload.readInt32();
                    int ObjectID = aPayload.readInt32();
                    String Attribute = aPayload.readString();
                    this.onChangeObject.dispatch(Action, ObjectID, getShortEventName(), Attribute);
                }
            }
        }
    }

    private void handleChangeObjectData(TByteBuffer aPayload)
    {
        if (this.onChangeObjectData != null)
        {
            int Action = aPayload.readInt32();
            int ObjectID = aPayload.readInt32();
            String Attribute = aPayload.readString();
            TByteBuffer NewValues = aPayload.readByteBuffer();
            TByteBuffer OldValues = aPayload.readByteBuffer();
            this.onChangeObjectData.dispatch(this, Action, ObjectID, Attribute, NewValues, OldValues);
        }
    }

    private void handleBuffer(int aEventTick, TByteBuffer aPayload)
    {
        if (this.onBuffer != null)
        {
            int BufferID = aPayload.readInt32();
            TByteBuffer Buffer = aPayload.readByteBuffer();
            this.onBuffer.dispatch(this, aEventTick, BufferID, Buffer);
        }
    }

    private void handleTimerTick(TByteBuffer aPayload)
    {
        if (this.onTimerTick != null)
        {
            String TimerName = aPayload.readString();
            int Tick = aPayload.readInt32();
            long TickTime = aPayload.readInt64();
            long StartTime = aPayload.readInt64();
            this.onTimerTick.dispatch(this, TimerName, Tick, TickTime, StartTime);
        }
    }

    private void handleTimerCmd(int aEventKind, TByteBuffer aPayload)
    {
        if (this.onTimerCmd != null)
        {
            String TimerName = aPayload.readString();
            this.onTimerCmd.dispatch(this, aEventKind, TimerName);
        }
    }

    private void handleChildEvent(int aEventKind, TByteBuffer aPayload)
    {
        if (this.onChildEvent != null)
        {
            String EventName = aPayload.readString();
            this.onChildEvent.dispatch(this, aEventKind, EventName);
        }
    }

    private void handleStreamEvent(int aEventKind, TByteBuffer aPayload)
    {
        int StreamID;
        String StreamName;
        OutputStream stream;
        TStreamCacheEntry sce;
        switch (aEventKind)
        {
            case EK_STREAM_HEADER:
                if (this.onStreamCreate != null)
                {
                    StreamID = aPayload.readInt32();
                    StreamName = aPayload.readString();
                    stream = this.onStreamCreate.dispatch(this, StreamName);
                    if (stream != null)
                        this.fstreamCache.cache(StreamID, stream, StreamName);
                }
                break;
            case EK_STREAM_BODY:
                StreamID = aPayload.readInt32();
                sce = this.fstreamCache.find(StreamID);
                if ((sce != null) && (sce.fstream != null))
                {
                    try
                    {
                        sce.fstream.write(aPayload.getBuffer(), aPayload.getReadCursor(), aPayload.getReadAvailable());
                    }
                    catch (IOException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                break;
            case EK_STREAM_TAIL:
                StreamID = aPayload.readInt32();
                sce = this.fstreamCache.find(StreamID);
                if ((sce != null) && (sce.fstream != null))
                {
                    try
                    {
                        sce.fstream.write(aPayload.getBuffer(), aPayload.getReadCursor(), aPayload.getReadAvailable());
                        if (this.onStreamEnd != null)
                            this.onStreamEnd.dispatch(this, sce.fstream, sce.fname);
                        sce.fstream.close();
                    }
                    catch (IOException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    this.fstreamCache.remove(StreamID);
                }
                break;
        }
    }

    void handleSubAndPub(int aCommand)
    {
        if (this.fparent == null && this.onSubAndPub != null)
            this.onSubAndPub.dispatch(this, aCommand);
        switch (aCommand)
        {
            case IC_SUBSCRIBE:
                if (this.fparent != null && !isPublished())
                    publish();
                this.fSubscribers = true;
                break;
            case IC_PUBLISH:
                if (this.fparent != null && !isSubscribed())
                    subscribe();
                this.fPublishers = true;
                break;
            case IC_UNSUBSCRIBE:
                if (this.fparent != null && isPublished())
                    unPublish(true);
                this.fSubscribers = false;
                break;
            case IC_UNPUBLISH:
                if (this.fparent != null && isSubscribed())
                    unSubscribe(true);
                this.fPublishers = false;
                break;
        }
    }

    // public

    private final TConnection connection;

    /** The local ID related to this event */
    public final int ID;

    /** Returns the fully qualified name of this event */
    public String getEventName()
    {
        return this.feventName;
    }

    public String getShortEventName()
    {
        String federationPrefix = this.connection.getFederation() + ".";
        if (this.feventName.startsWith(federationPrefix))
            return this.feventName.substring(federationPrefix.length());
        else
            return this.feventName;
    }

    /** Returns true if this event is published */
    public boolean isPublished()
    {
        return this.fisPublished;
    }

    /** Returns true if this event is subscribed */
    public boolean isSubscribed()
    {
        return this.fisSubscribed;
    }

    public void copyHandlersFrom(TEventEntry aEventEntry)
    {
        this.onChangeObject = aEventEntry.onChangeObject;
        this.onFocus = aEventEntry.onFocus;
        this.onNormalEvent = aEventEntry.onNormalEvent;
        this.onBuffer = aEventEntry.onBuffer;
        this.onStreamCreate = aEventEntry.onStreamCreate;
        this.onStreamEnd = aEventEntry.onStreamEnd;
        this.onChangeFederation = aEventEntry.onChangeFederation;
        this.onTimerTick = aEventEntry.onTimerTick;
        this.onTimerCmd = aEventEntry.onTimerCmd;
        this.onChangeObjectData = aEventEntry.onChangeObjectData;
        this.onOtherEvent = aEventEntry.onOtherEvent;
        this.onSubAndPub = aEventEntry.onSubAndPub;
    }

    // IMB 1
    /** Override dispatch to implement a change object event handler */
    public interface TOnChangeObject
    {
        void dispatch(int aAction, int aObjectID, String aObjectName, String aAttribute);
    }

    /** Handler to be called on receive of a change object event */
    public TOnChangeObject onChangeObject = null;

    /** Override dispatch to implement a focus event handler */
    public interface TOnFocus
    {
        public void dispatch(double x, double y);
    }

    /** Handler to be called on receive of a focus event */
    public TOnFocus onFocus = null;

    // IMB 2
    /** Override dispatch to implement a normal event handler */
    public interface TOnNormalEvent
    {
        public void dispatch(TEventEntry aEvent, TByteBuffer aPayload);
    }

    /** Handler to be called on receive of a normal event */
    public TOnNormalEvent onNormalEvent = null;

    /** Override dispatch to implement a buffer event handler */
    public interface TOnBuffer
    {
        public void dispatch(TEventEntry aEvent, int aTick, int aBufferID, TByteBuffer aBuffer);
    }

    /** Handler to be called on receive of a buffer event */
    public TOnBuffer onBuffer = null;

    /** Override dispatch to implement a handler of received streams, creating the local stream */
    public interface TOnStreamCreate
    {
        public OutputStream dispatch(TEventEntry aEvent, String aStreamName);
    }

    /** Handler to be called on receive of a stream header event */
    public TOnStreamCreate onStreamCreate = null;

    /** Override dispatch to implement a handler of received streams, action on end of stream */
    public interface TOnStreamEnd
    {
        public void dispatch(TEventEntry aEvent, /* ref */OutputStream aStream, String aStreamName);
    }

    /** Handler to be called on receive of a stream tail event */
    public TOnStreamEnd onStreamEnd = null;

    /** Override dispatch to implement a federation change handler */
    public interface TOnChangeFederation
    {
        public void dispatch(TConnection aConnection, int aNewFederationID, String aNewFederation);
    }

    /** Handler to be called on receive of a federation change event */
    public TOnChangeFederation onChangeFederation = null;

    // IMB 3
    /** Override dispatch to implement a timer tick handler */
    public interface TOnTimerTick
    {
        public void dispatch(TEventEntry aEvent, String aTimerName, int aTick, long aTickTime, long aStartTime);
    }

    /** Handler to be called on receive of a timer tick event */
    public TOnTimerTick onTimerTick = null;

    /** Override dispatch to implement a timer command handler for commands reset/start/stop */
    public interface TOnTimerCmd
    {
        public void dispatch(TEventEntry aEvent, int aEventKind, String aTimerName);
    }

    /** Handler to be called on receive of a timer command reset/start/stop */
    public TOnTimerCmd onTimerCmd = null;

    /** Override dispatch to implement a handler for hub child event creation events */
    public interface TOnChildEvent
    {
        public void dispatch(TEventEntry aEvent, int aEventKind, String aEventName);
    }

    /** Handler to be called on receive of a child add/remove event */
    public TOnChildEvent onChildEvent = null;

    /** Override dispatch to implement a change object data event handler */
    public interface TOnChangeObjectData
    {
        public void dispatch(TEventEntry aEvent, int aAction, int aObjectID, String aAttribute, TByteBuffer aNewValues,
                TByteBuffer aOldValues);
    }

    /** Handler to be called on receive of a change object with data event */
    public TOnChangeObjectData onChangeObjectData = null;

    // TODO: description
    public interface TOnSubAndPubEvent
    {
        public void dispatch(TEventEntry aEvent, int aCommand);
    }

    // TODO: description
    public TOnSubAndPubEvent onSubAndPub = null;

    /** Override dispatch to implement a event handler for non-standard events */
    public interface TOnOtherEvent
    {
        public void dispatch(TEventEntry aEvent, int aTick, int aEventKind, TByteBuffer aPayload);
    }

    /** Handler to be called on receive of an unhandled event */
    public TOnOtherEvent onOtherEvent = null;

    // signals (send events)

    /**
     * Send an event to the framework
     * @param aEventKind int;
     * @param aEventPayload byte[];
     * @return status of the request (TConnection.ICE_* constants)
     */
    public int signalEvent(int aEventKind, byte[] aEventPayload)
    {
        TByteBuffer Payload = new TByteBuffer();
        if (!isPublished() && this.connection.autoPublish)
            publish();
        if (isPublished())
        {
            Payload.prepare(this.ID);
            Payload.prepare((int) 0); // tick
            Payload.prepare(aEventKind);
            Payload.prepare(aEventPayload);
            Payload.prepareApply();
            Payload.qWrite(this.ID);
            Payload.qWrite((int) (0)); // tick
            Payload.qWrite(aEventKind);
            Payload.qWrite(aEventPayload);
            return this.connection.writeCommand(IC_EVENT, Payload.getBuffer());
        }
        else
            return TConnection.ICE_EVENT_NOT_PUBLISHED;
    }

    /**
     * Send a buffer event to the framework
     * @param aBufferID int; self chosen ID to separate streams of buffer events
     * @param aBuffer byte[];
     * @return status of the request (TConnection.ICE_* constants)
     */
    public int signalBuffer(int aBufferID, byte[] aBuffer)
    {
        return signalBuffer(aBufferID, aBuffer, 0);
    }

    /**
     * Send a buffer event to the framework
     * @param aBufferID int; self chosen ID to separate streams of buffer events
     * @param aBuffer byte[];
     * @param aEventFlags flags for special processing within the hub; not fully implemented, use 0
     * @return status of the request (TConnection.ICE_* constants)
     */
    public int signalBuffer(int aBufferID, byte[] aBuffer, int aEventFlags)
    {
        TByteBuffer Payload = new TByteBuffer();
        if (!isPublished() && this.connection.autoPublish)
            publish();
        if (isPublished())
        {
            Payload.prepare(this.ID);
            Payload.prepare((int) 0); // tick
            Payload.prepare(EK_BUFFER | (aEventFlags & EVENT_FLAGS_MASK));
            Payload.prepare(aBufferID);
            Payload.prepare(aBuffer.length);
            Payload.prepare(aBuffer);
            Payload.prepareApply();
            Payload.qWrite(this.ID);
            Payload.qWrite((int) (0)); // tick
            Payload.qWrite(EK_BUFFER | (aEventFlags & EVENT_FLAGS_MASK));
            Payload.qWrite(aBufferID);
            Payload.qWrite(aBuffer.length);
            Payload.qWrite(aBuffer);
            return this.connection.writeCommand(IC_EVENT, Payload.getBuffer());
        }
        else
            return TConnection.ICE_EVENT_NOT_PUBLISHED;
    }

    private int readBytesFromStream(TByteBuffer aBuffer, InputStream aStream)
    {
        try
        {
            // TODO: cleanup code, in java stream read returns -1 when eos ?
            int Count = 0;
            int NumBytesRead = 1; // sentinel
            while (aBuffer.getwriteAvailable() > 0 && NumBytesRead > 0)
            {
                NumBytesRead = aStream.read(aBuffer.getBuffer(), aBuffer.getWriteCursor(), aBuffer.getwriteAvailable());
                if (NumBytesRead > 0)
                {
                    aBuffer.written(NumBytesRead);
                    Count += NumBytesRead;
                }
            }
            return Count;
        }
        catch (IOException ex)
        {
            return 0; // signal stream read error
        }
    }

    /**
     * Send a stream to the framework
     * @param aStreamName String;
     * @param aStream InputStream;
     * @return status of the request (TConnection.ICE_* constants)
     */
    public int signalStream(String aStreamName, InputStream aStream)
    {
        TByteBuffer Payload = new TByteBuffer();
        int ReadSize;
        int BodyIndex;
        int EventKindIndex;
        if (!isPublished() && this.connection.autoPublish)
            publish();
        if (isPublished())
        {
            // ekStreamHeader, includes stream name, no stream data
            byte[] StreamNameUTF8 = aStreamName.getBytes(Charset.forName("UTF-8"));
            // TODO: generate semi-unique stream id from connection URI and stream name
            int StreamID = StreamNameUTF8.hashCode() + this.connection.hashCode();
            Payload.prepare(this.ID);
            Payload.prepare((int) 0); // tick
            Payload.prepare(EK_STREAM_HEADER); // event kind
            Payload.prepare(StreamID);
            Payload.prepare(aStreamName);
            Payload.prepareApply();
            Payload.qWrite(this.ID);
            Payload.qWrite((int) 0); // tick
            EventKindIndex = Payload.getWriteCursor();
            Payload.qWrite(EK_STREAM_HEADER); // event kind
            Payload.qWrite(StreamID);
            BodyIndex = Payload.getWriteCursor();
            Payload.qWrite(aStreamName);
            int res = this.connection.writeCommand(IC_EVENT, Payload.getBuffer());
            if (res > 0)
            {
                // ekStreamBody, only buffer size chunks of data
                // prepare payload to same value but aStreamName stripped
                // fix-up event kind
                Payload.writeStart(EventKindIndex);
                Payload.qWrite(EK_STREAM_BODY);
                Payload.writeStart(BodyIndex);
                // prepare room for body data
                Payload.prepareStart();
                Payload.prepareSize(MAX_STREAM_BODY_BUFFER_SIZE);
                Payload.prepareApply();
                // write pointer in ByteBuffer is still at beginning of stream read buffer!
                // but buffer is already created on correct length
                do
                {
                    ReadSize = readBytesFromStream(Payload, aStream);
                    // ReadSize = aStream.Read(Payload.Buffer, BodyIndex, Connection.MaxStreamBodyBuffer);
                    if (ReadSize == MAX_STREAM_BODY_BUFFER_SIZE)
                        res = this.connection.writeCommand(IC_EVENT, Payload.getBuffer());
                    // reset write position
                    Payload.writeStart(BodyIndex);
                }
                while ((ReadSize == MAX_STREAM_BODY_BUFFER_SIZE) && (res > 0));
                if (res > 0)
                {
                    // clip ByteBuffer to bytes read from stream
                    // write pointer in ByteBuffer is still at beginning of stream read buffer!
                    Payload.prepareStart();
                    Payload.prepareSize(ReadSize);
                    Payload.prepareApplyAndTrim();
                    // fixup event kind
                    Payload.writeStart(EventKindIndex);
                    Payload.qWrite(EK_STREAM_TAIL);
                    res = this.connection.writeCommand(IC_EVENT, Payload.getBuffer());
                }
            }
            return res;
        }
        else
            return TConnection.ICE_EVENT_NOT_PUBLISHED;
    }

    /** signal an object change: a new object is created */
    public static final int ACTION_NEW = 0;

    /** signal an object change: an object is deleted */
    public static final int ACTION_DELETE = 1;

    /** signal an object change: an existing object has changed */
    public static final int ACTION_CHANGE = 2;

    /**
     * Send a change object event to the framework
     * @param aAction int; see ACTION_* constants
     * @param aObjectID int; ID of the object that has changed
     * @param aAttribute String; optional name of the attribute that has changed
     * @return status of the request (TConnection.ICE_* constants)
     */
    public int signalChangeObject(int aAction, int aObjectID, String aAttribute)
    {
        TByteBuffer Payload = new TByteBuffer();
        if (!isPublished() && this.connection.autoPublish)
            publish();
        if (isPublished())
        {
            Payload.prepare(this.ID);
            Payload.prepare((int) 0); // tick
            Payload.prepare(EK_CHANGE_OBJECT_EVENT);
            Payload.prepare(aAction);
            Payload.prepare(aObjectID);
            Payload.prepare(aAttribute);
            Payload.prepareApply();
            Payload.qWrite(this.ID);
            Payload.qWrite((int) (0)); // tick
            Payload.qWrite(EK_CHANGE_OBJECT_EVENT);
            Payload.qWrite(aAction);
            Payload.qWrite(aObjectID);
            Payload.qWrite(aAttribute);
            return this.connection.writeCommand(IC_EVENT, Payload.getBuffer());
        }
        else
            return TConnection.ICE_EVENT_NOT_PUBLISHED;
    }

    // timers
    /**
     * Create a timer on the connected HUB
     * @param aTimerName String; unique name of the timer within this event
     * @param aStartTimeUTCorRelFT long; 0 means now&lt;br&gt; larger than 0 means in absolute system time (UTC)<br>
     *            less than 0 means system timer relative to now
     * @param aResolutionms int; the resolution of a timer tick (step) in milliseconds
     * @param aSpeedFactor double; 1 means same speed as real time, 0 means the timer runs in simulation time
     * @return status of the request (TConnection.ICE_* constants)
     */
    public int timerCreate(String aTimerName, long aStartTimeUTCorRelFT, int aResolutionms, double aSpeedFactor)
    {
        return timerCreate(aTimerName, aStartTimeUTCorRelFT, aResolutionms, aSpeedFactor, TRC_INFINITE);
    }

    /**
     * Create a timer on the connected HUB
     * @param aTimerName String; unique name of the timer within this event
     * @param aStartTimeUTCorRelFT long; 0 means now&lt;br&gt; larger than 0 means in absolute system time (UTC)<br>
     *            less than 0 means system timer relative to now
     * @param aResolutionms int; the resolution of a timer tick (step) in milliseconds
     * @param aSpeedFactor double; 1 means same speed as real time, 0 means the timer runs in simulation time
     * @param aRepeatCount int; number of timer the timer must send a timer tick (TRC_INFINITE for infinite)
     * @return status of the request (TConnection.ICE_* constants)
     */
    public int timerCreate(String aTimerName, long aStartTimeUTCorRelFT, int aResolutionms, double aSpeedFactor,
            int aRepeatCount)
    {
        TByteBuffer Payload = new TByteBuffer();
        if (!isPublished() && this.connection.autoPublish)
            publish();
        if (isPublished())
        {
            Payload.prepare(this.ID);
            Payload.prepare(aTimerName);
            Payload.prepare(aStartTimeUTCorRelFT);
            Payload.prepare(aResolutionms);
            Payload.prepare(aSpeedFactor);
            Payload.prepare(aRepeatCount);
            Payload.prepareApply();
            Payload.qWrite(this.ID);
            Payload.qWrite(aTimerName);
            Payload.qWrite(aStartTimeUTCorRelFT);
            Payload.qWrite(aResolutionms);
            Payload.qWrite(aSpeedFactor);
            Payload.qWrite(aRepeatCount);
            return this.connection.writeCommand(IC_CREATE_TIMER, Payload.getBuffer());
        }
        else
            return TConnection.ICE_EVENT_NOT_PUBLISHED;
    }

    /**
     * Cancel a running timer; the timer is destroyed.
     * @param aTimerName String;
     * @return status of the request (TConnection.ICE_* constants)
     */
    public int timerCancel(String aTimerName)
    {
        return timerBasicCmd(EK_TIMER_CANCEL, aTimerName);
    }

    /**
     * Prepare a timer; the timer is stopped and reset to an initial state
     * @param aTimerName String;
     * @return status of the request (TConnection.ICE_* constants)
     */
    public int timerPrepare(String aTimerName)
    {
        return timerBasicCmd(EK_TIMER_PREPARE, aTimerName);
    }

    /**
     * Start or continue the timer
     * @param aTimerName String;
     * @return status of the request (TConnection.ICE_* constants)
     */
    public int timerStart(String aTimerName)
    {
        return timerBasicCmd(EK_TIMER_START, aTimerName);
    }

    /**
     * Stop or pause the timer
     * @param aTimerName String;
     * @return status of the request (TConnection.ICE_* constants)
     */
    public int timerStop(String aTimerName)
    {
        return timerBasicCmd(EK_TIMER_STOP, aTimerName);
    }

    /**
     * Set the relative running speed of the timer
     * @param aTimerName String;
     * @param aSpeedFactor double; 1 means the timer is running in real time, 0 means the timer runs in simulation time
     * @return status of the request (TConnection.ICE_* constants)
     */
    public int timerSetSpeed(String aTimerName, double aSpeedFactor)
    {
        TByteBuffer Payload = new TByteBuffer();
        Payload.prepare(aTimerName);
        Payload.prepare(aSpeedFactor);
        Payload.prepareApply();
        Payload.qWrite(aTimerName);
        Payload.qWrite(aSpeedFactor);
        return signalEvent(EK_TIMER_SET_SPEED, Payload.getBuffer());
    }

    /**
     * Add a client name to the acknowledge list of a timer.<br>
     * All entries in this list must send an acknowledge on each timer tick for the timer to advance.
     * @param aTimerName String;
     * @param aClientName String;
     * @return status of the request (TConnection.ICE_* constants)
     */
    public int timerAcknowledgeAdd(String aTimerName, String aClientName)
    {
        return timerAcknowledgeCmd(EK_TIMER_ACKNOWLEDGE_LIST_ADD, aTimerName, aClientName);
    }

    /**
     * Remove a client name from the acknowledge list of a timer.<br>
     * All entries in this list must send an acknowledge on each timer tick for the timer to advance.
     * @param aTimerName String;
     * @param aClientName String;
     * @return status of the request (TConnection.ICE_* constants)
     */
    public int timerAcknowledgeRemove(String aTimerName, String aClientName)
    {
        return timerAcknowledgeCmd(EK_TIMER_ACKNOWLEDGE_LIST_REMOVE, aTimerName, aClientName);
    }

    /**
     * Acknowledge a timer tick.<br>
     * All clients on the timer acknowledge list must send an acknowledge on each timer tick for the timer to advance.
     * @param aTimerName String;
     * @param aClientName String;
     * @param aProposedTimeStep int; clients can specify the next step that they wish. The overall lowest next step is used if
     *            the timer is not running in real time
     * @return status of the request (TConnection.ICE_* constants)
     */
    public int timerAcknowledge(String aTimerName, String aClientName, int aProposedTimeStep)
    {
        TByteBuffer Payload = new TByteBuffer();
        Payload.prepare(aClientName);
        Payload.prepare(aTimerName);
        Payload.prepare(aProposedTimeStep);
        Payload.prepareApply();
        Payload.qWrite(aClientName);
        Payload.qWrite(aTimerName);
        Payload.qWrite(aProposedTimeStep);
        return signalEvent(EK_TIMER_ACKNOWLEDGE, Payload.getBuffer());
    }

    // log
    /**
     * Send a line to the central framework log
     * @param aLine String; text to enter into the log
     * @param aLevel TLogLevel; severity of the entry to log. See TLogLevel for values.
     * @return status of the request (TConnection.ICE_* constants)
     */
    public int logWriteLn(String aLine, TLogLevel aLevel)
    {
        TByteBuffer Payload = new TByteBuffer();
        if (!isPublished() && this.connection.autoPublish)
            publish();
        if (isPublished())
        {
            Payload.prepare((int) 0); // client id filled in by hub
            Payload.prepare(aLine);
            Payload.prepare(aLevel.ordinal());
            Payload.prepareApply();
            Payload.qWrite((int) 0); // client id filled in by hub
            Payload.qWrite(aLine);
            Payload.qWrite(aLevel.ordinal());
            return signalEvent(EK_LOG_WRITELN, Payload.getBuffer());
        }
        else
            return TConnection.ICE_EVENT_NOT_PUBLISHED;
    }
}
