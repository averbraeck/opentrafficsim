package org.opentrafficsim.cosim.sim0mq;

import java.util.LinkedHashMap;
import java.util.Map;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.cli.CliUtil;
import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.serialization.Endianness;
import org.djutils.serialization.SerializationException;
import org.djutils.serialization.TypedMessage;
import org.opentrafficsim.base.geometry.OtsGeometryException;
import org.opentrafficsim.base.logger.Logger;
import org.opentrafficsim.cosim.AbstractOtsTransceiver;
import org.opentrafficsim.cosim.OdMatrixJson;
import org.opentrafficsim.cosim.RoutesJson;
import org.opentrafficsim.cosim.adapters.AccelerationAdapter;
import org.opentrafficsim.cosim.adapters.CommandTypeAdapter;
import org.opentrafficsim.cosim.adapters.DurationAdapter;
import org.opentrafficsim.cosim.adapters.FrequencyAdapter;
import org.opentrafficsim.cosim.adapters.LengthAdapter;
import org.opentrafficsim.cosim.adapters.SpeedAdapter;
import org.opentrafficsim.cosim.adapters.TimeAdapter;
import org.opentrafficsim.cosim.messages.CommandMessage;
import org.opentrafficsim.cosim.messages.CommandMessage.CommandType;
import org.opentrafficsim.cosim.messages.DeleteMessage;
import org.opentrafficsim.cosim.messages.ExternalMessage;
import org.opentrafficsim.cosim.messages.ModeMessage;
import org.opentrafficsim.cosim.messages.ModeMessage.ControlMode;
import org.opentrafficsim.cosim.messages.NetworkMessage;
import org.opentrafficsim.cosim.messages.NetworkMessage.NetworkType;
import org.opentrafficsim.cosim.messages.OdMatrixMessage;
import org.opentrafficsim.cosim.messages.PlanMessage;
import org.opentrafficsim.cosim.messages.ProgressMessage;
import org.opentrafficsim.cosim.messages.ReadyMessage;
import org.opentrafficsim.cosim.messages.ResetMessage;
import org.opentrafficsim.cosim.messages.RoutesMessage;
import org.opentrafficsim.cosim.messages.StartMessage;
import org.opentrafficsim.cosim.messages.StopMessage;
import org.opentrafficsim.cosim.messages.TerminateMessage;
import org.opentrafficsim.cosim.messages.VehicleMessage;
import org.sim0mq.Sim0MQException;
import org.sim0mq.message.Sim0MQMessage;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ch.qos.logback.classic.Level;
import nl.tudelft.simulation.dsol.SimRuntimeException;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * OTS co-simulation transceiver based on Sim0mq.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 */
@Command(description = "OTS Transceiver for co-simulation", name = "OTS", mixinStandardHelpOptions = true,
        showDefaultValues = true, version = "20260410")
public class OtsTransceiverSim0mq extends AbstractOtsTransceiver
{

    /** {@code Gson} with all adapters registered. */
    public static final Gson GSON;

    static
    {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Frequency.class, new FrequencyAdapter());
        builder.registerTypeAdapter(Acceleration.class, new AccelerationAdapter());
        builder.registerTypeAdapter(CommandType.class, new CommandTypeAdapter());
        builder.registerTypeAdapter(Duration.class, new DurationAdapter());
        builder.registerTypeAdapter(Length.class, new LengthAdapter());
        builder.registerTypeAdapter(Speed.class, new SpeedAdapter());
        builder.registerTypeAdapter(Time.class, new TimeAdapter());
        GSON = builder.create();
    }

    /** Federation id to receive/sent messages. */
    @Option(names = "--federationId", description = "Federation id to receive/sent messages", defaultValue = "Ots_ExternalSim")
    private String federation;

    /** OTS id to receive/sent messages. */
    @Option(names = "--otsId", description = "Ots id to receive/sent messages", defaultValue = "Ots")
    private String ots;

    /** Client id to receive/sent messages. */
    @Option(names = "--clientId", description = "Client id to receive/sent messages", defaultValue = "ExternalSim")
    private String client;

    /** Endianness. */
    @Option(names = "--bigEndian", description = "Big-endianness", defaultValue = "false", negatable = true)
    private Boolean bigEndian;

    /** Port number. */
    @Option(names = "--port", description = "Port number", defaultValue = "5556")
    private int port;

    /** Logging level. */
    @Option(names = "--logLevel", description = "Logging level: OFF, ERROR, WARN, INFO, DEBUG or TRACE")
    private Level logLevel = Level.INFO;

    /** */
    private ZContext context;

    /** the socket. */
    private ZMQ.Socket responder;

    /** Next message id. */
    private int messageId = 0;

    /**
     * Constructor.
     * @param args command line arguments.
     */
    protected OtsTransceiverSim0mq(final String... args)
    {
        CliUtil.execute(this, args);
        Logger.setLogLevel(OtsTransceiverSim0mq.this.logLevel);
    }

    /**
     * Main method.
     * @param args command line arguments
     * @throws Exception exception
     */
    public static void main(final String[] args) throws Exception
    {
        new OtsTransceiverSim0mq(args).start();
    }

    /**
     * Starts worker thread.
     */
    private void start()
    {
        new Worker().start();
    }

    /**
     * Worker thread to listen to messages and respond.
     */
    protected class Worker extends Thread implements EventListener
    {

        @Override
        public void notify(final Event event)
        {
            OtsTransceiverSim0mq.this.context = new ZContext(1);
            OtsTransceiverSim0mq.this.responder = OtsTransceiverSim0mq.this.context.createSocket(SocketType.PAIR);
            OtsTransceiverSim0mq.this.responder.bind("tcp://*:" + OtsTransceiverSim0mq.this.port);
            Logger.ots().debug("OTS is running");

            try
            {
                while (!Thread.currentThread().isInterrupted())
                {
                    // Wait for next request from the client
                    byte[] request = OtsTransceiverSim0mq.this.responder.recv();
                    Object[] array = TypedMessage.decodeToPrimitiveDataTypes(
                            request[11] == 1 ? Endianness.BIG_ENDIAN : Endianness.LITTLE_ENDIAN, request);
                    Sim0MQMessage message = new Sim0MQMessage(array, array.length - 8, array[5]);
                    if (ExternalMessage.ID.equals(message.getMessageTypeId()))
                    {
                        Object[] payload = message.createObjectArray();
                        processExternalPayload(payload);
                    }
                    else if (ProgressMessage.ID.equals(message.getMessageTypeId()))
                    {
                        Object[] payload = message.createObjectArray();
                        Duration until = (Duration) payload[8];
                        receive(new ProgressMessage(until, payload[6]));
                    }
                    else if (VehicleMessage.ID.equals(message.getMessageTypeId()))
                    {
                        Object[] payload = message.createObjectArray();
                        processVehiclePayload(payload);
                    }
                    else if (ModeMessage.ID.equals(message.getMessageTypeId()))
                    {
                        Object[] payload = message.createObjectArray();
                        Duration time = (Duration) payload[8];
                        String vehicleId = (String) payload[9];
                        ControlMode mode = ControlMode.valueOf(((String) (String) payload[10]).toUpperCase());
                        receive(new ModeMessage(time, vehicleId, mode));
                    }
                    else if (CommandMessage.ID.equals(message.getMessageTypeId()))
                    {
                        Object[] payload = message.createObjectArray();
                        String vehicleId = (String) payload[8];
                        CommandMessage.Command command = GSON.fromJson((String) payload[9], CommandMessage.Command.class);
                        receive(new CommandMessage(vehicleId, command));
                    }
                    else if (DeleteMessage.ID.equals(message.getMessageTypeId()))
                    {
                        Object[] payload = message.createObjectArray();
                        Duration time = (Duration) payload[8];
                        String vehicleId = (String) payload[9];
                        receive(new DeleteMessage(time, vehicleId));
                    }
                    else if (RoutesMessage.ID.equals(message.getMessageTypeId()))
                    {
                        Object[] payload = message.createObjectArray();
                        RoutesJson routes = GSON.fromJson((String) payload[8], RoutesJson.class);
                        receive(new RoutesMessage(routes, payload[6]));
                    }
                    else if (OdMatrixMessage.ID.equals(message.getMessageTypeId()))
                    {
                        Object[] payload = message.createObjectArray();
                        OdMatrixJson routes = GSON.fromJson((String) payload[8], OdMatrixJson.class);
                        receive(new OdMatrixMessage(routes, payload[6]));
                    }
                    else if (NetworkMessage.ID.equals(message.getMessageTypeId()))
                    {
                        Object[] payload = message.createObjectArray();
                        receive(new NetworkMessage(NetworkType.OPENDRIVE, (String) payload[8], payload[6]));
                    }
                    else if (StartMessage.ID.equals(message.getMessageTypeId()))
                    {
                        receive(new StartMessage());
                    }
                    else if (StopMessage.ID.equals(message.getMessageTypeId()))
                    {
                        receive(new StopMessage());
                    }
                    else if (ResetMessage.ID.equals(message.getMessageTypeId()))
                    {
                        receive(new ResetMessage(message.createObjectArray()[6]));
                    }
                    else if (TerminateMessage.ID.equals(message.getMessageTypeId()))
                    {
                        receive(new TerminateMessage());
                        break;
                    }
                    else
                    {
                        Logger.ots().error("Cannot process a {} message", message.getMessageTypeId());
                    }
                }
            }
            catch (Sim0MQException | SerializationException | NumberFormatException | OtsGeometryException
                    | SimRuntimeException e)
            {
                e.printStackTrace();
            }
            OtsTransceiverSim0mq.this.responder.close();
            OtsTransceiverSim0mq.this.context.destroy();
            OtsTransceiverSim0mq.this.context.close();
            Logger.ots().debug("Ots terminated");
            System.exit(0);
        }

        /**
         * Process external message payload.
         * @param payload message payload
         */
        private void processExternalPayload(final Object[] payload)
        {
            int index = 8;
            Duration time = (Duration) payload[index++];
            String vehicleId = (String) payload[index++];
            Length xCoordinate = (Length) payload[index++];
            Length yCoordinate = (Length) payload[index++];
            Direction direction = (Direction) payload[index++];
            Speed speed = (Speed) payload[index++];
            Acceleration acceleration = (Acceleration) payload[index++];
            receive(new ExternalMessage(time, vehicleId, xCoordinate, yCoordinate, direction, speed, acceleration));
        }

        /**
         * Process vehicle message payload.
         * @param payload message payload
         */
        private void processVehiclePayload(final Object[] payload)
        {
            int index = 8;
            Duration time = (Duration) payload[index++];
            String vehicleId = (String) payload[index++];
            ControlMode mode = ControlMode.valueOf(((String) payload[index++]).toUpperCase());
            Length initX = (Length) payload[index++];
            Length initY = (Length) payload[index++];
            Direction initDirection = (Direction) payload[index++];
            Speed initSpeed = (Speed) payload[index++];
            VehicleMessage.VehicleType vehicleType =
                    VehicleMessage.VehicleType.valueOf(((String) payload[index++]).toUpperCase());
            Length vehicleLength = (Length) payload[index++];
            Length vehicleWidth = (Length) payload[index++];
            Length refToNose = (Length) payload[index++];
            int numParams = (int) payload[index++];
            Map<String, Object> parameterMap = new LinkedHashMap<>();
            for (int i = 0; i < numParams; i++)
            {
                parameterMap.put((String) payload[index++], payload[index++]);
            }
            String routeId = (String) payload[index++];
            Object responseId = payload[6];
            receive(new VehicleMessage(time, vehicleId, mode, initX, initY, initDirection, initSpeed, vehicleType,
                    vehicleLength, vehicleWidth, refToNose, parameterMap, routeId, responseId));
        }

    }

    @Override
    protected void send(final ReadyMessage readyMessage)
    {
        send(readyMessage.getId(), new Object[] {readyMessage.responseId()});
    }

    @Override
    protected void send(final PlanMessage planMessage)
    {
        Object[] payload = new Object[7];
        payload[0] = planMessage.vehicleId();
        payload[1] = planMessage.speed();
        payload[2] = planMessage.xCoordinates();
        payload[3] = planMessage.yCoordinates();
        payload[4] = planMessage.steps();
        payload[5] = planMessage.acceleration();
        payload[6] = planMessage.turnIndicator().name();
        send(planMessage.getId(), payload);
    }

    @Override
    protected void send(final DeleteMessage deleteMessage)
    {
        send(deleteMessage.getId(), new Object[] {deleteMessage.vehicleId()});
    }

    @Override
    protected void send(final VehicleMessage vehicleMessage)
    {
        Object[] payload = new Object[12];
        payload[0] = vehicleMessage.vehicleId();
        payload[1] = vehicleMessage.controlMode().name();
        payload[2] = vehicleMessage.xCoordinate();
        payload[3] = vehicleMessage.yCoordinate();
        payload[4] = vehicleMessage.direction();
        payload[5] = vehicleMessage.speed();
        payload[6] = vehicleMessage.type().name();
        payload[7] = vehicleMessage.length();
        payload[8] = vehicleMessage.width();
        payload[9] = vehicleMessage.refToNose();
        payload[10] = 0;
        payload[11] = vehicleMessage.route();
        send(vehicleMessage.getId(), payload);
    }

    /**
     * Send message over data bus.
     * @param messageTypeId message type ID
     * @param payload payload
     */
    private void send(final String messageTypeId, final Object[] payload)
    {
        try
        {
            byte[] bytes = Sim0MQMessage.encodeUTF8(OtsTransceiverSim0mq.this.bigEndian, OtsTransceiverSim0mq.this.federation,
                    OtsTransceiverSim0mq.this.ots, OtsTransceiverSim0mq.this.client, messageTypeId, this.messageId++, payload);
            this.responder.send(bytes, ZMQ.DONTWAIT);
        }
        catch (Sim0MQException | SerializationException exception)
        {
            exception.printStackTrace();
        }
    }

}
