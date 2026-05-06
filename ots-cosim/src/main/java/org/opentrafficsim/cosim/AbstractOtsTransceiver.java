package org.opentrafficsim.cosim;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;

import javax.naming.NamingException;
import javax.xml.parsers.ParserConfigurationException;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vfloat.vector.FloatAccelerationVector;
import org.djunits.value.vfloat.vector.FloatDurationVector;
import org.djunits.value.vfloat.vector.FloatLengthVector;
import org.djutils.draw.point.DirectedPoint2d;
import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.event.EventType;
import org.djutils.exceptions.Throw;
import org.djutils.immutablecollections.ImmutableList;
import org.djutils.metadata.MetaData;
import org.opentrafficsim.animation.gtu.colorer.SynchronizationGtuColorer;
import org.opentrafficsim.base.OtsRuntimeException;
import org.opentrafficsim.base.geometry.OtsGeometryException;
import org.opentrafficsim.base.geometry.OtsLine2d;
import org.opentrafficsim.base.logger.Logger;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterType;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.dsol.AbstractOtsModel;
import org.opentrafficsim.core.dsol.OtsAnimator;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan;
import org.opentrafficsim.core.gtu.plan.operational.Segment;
import org.opentrafficsim.core.idgenerator.IdSupplier;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.parameters.ParameterFactoryOneShot;
import org.opentrafficsim.core.perception.HistoryManagerDevs;
import org.opentrafficsim.cosim.messages.CommandMessage;
import org.opentrafficsim.cosim.messages.DeleteMessage;
import org.opentrafficsim.cosim.messages.ExternalMessage;
import org.opentrafficsim.cosim.messages.ModeMessage;
import org.opentrafficsim.cosim.messages.NetworkMessage;
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
import org.opentrafficsim.cosim.messages.ModeMessage.ControlMode;
import org.opentrafficsim.cosim.messages.NetworkMessage.NetworkType;
import org.opentrafficsim.cosim.messages.VehicleMessage.VehicleType;
import org.opentrafficsim.cosim.tactical.CommandsHandler;
import org.opentrafficsim.cosim.tactical.ScenarioTacticalPlanner;
import org.opentrafficsim.draw.colorer.Colorer;
import org.opentrafficsim.road.gtu.LaneBasedGtu;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGtuCharacteristicsGeneratorOd;
import org.opentrafficsim.road.gtu.tactical.lmrs.LmrsFactory;
import org.opentrafficsim.road.gtu.tactical.lmrs.LmrsFactory.CarFollowingModelConverter;
import org.opentrafficsim.road.gtu.tactical.lmrs.LmrsFactory.CooperationConverter;
import org.opentrafficsim.road.gtu.tactical.lmrs.LmrsFactory.FullerImplementation;
import org.opentrafficsim.road.gtu.tactical.lmrs.LmrsFactory.GapAcceptanceConverter;
import org.opentrafficsim.road.gtu.tactical.lmrs.LmrsFactory.Setting;
import org.opentrafficsim.road.gtu.tactical.lmrs.LmrsFactory.SynchronizationConverter;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.od.OdApplier;
import org.opentrafficsim.road.od.OdMatrix;
import org.opentrafficsim.road.od.OdOptions;
import org.opentrafficsim.swing.gui.OtsSimulationApplication;
import org.opentrafficsim.swing.gui.OtsSimulationPanel;
import org.opentrafficsim.swing.gui.OtsSimulationPanelDecorator;
import org.xml.sax.SAXException;

import jakarta.xml.bind.JAXBException;
import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.language.DsolException;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

/**
 * OTS co-simulation transceiver template. This class performs all OTS-side interaction between messages and simulation.
 * Extensions of this class should implement a communication protocol and call methods to receive the various messages. This
 * class will invoke several send methods that need to be implemented. All these send methods are invoked on a dedicated thread
 * and can run in parallel with receiving messages in so far the implemented communication protocol allows.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
@Command(description = "OTS Transceiver for co-simulation", name = "OTS", mixinStandardHelpOptions = true,
        showDefaultValues = true, version = "20260410")
public abstract class AbstractOtsTransceiver implements EventListener
{

    /** Event when simulator has ran up to a time. */
    public static final EventType PROGRESSED_EVENT =
            new EventType(new MetaData("PROGRESSED", "Simulation ran up to PROGRESS time."));

    /** Map from vehicle type for {@link VehicleMessage} to GTU type. */
    private static final Map<VehicleType, GtuType> GTU_TYPES =
            Map.of(VehicleType.CAR, DefaultsNl.CAR, VehicleType.TRUCK, DefaultsNl.TRUCK);

    /** Show GUI. */
    @Option(names = "--gui", description = "Whether to show the GUI", defaultValue = "false", negatable = true)
    private boolean showGui;

    /** ID prefix of generated GTUs. */
    @Option(names = "--idPrefix", description = "Prefix of ID of generated vehicles", defaultValue = "OTS_")
    private String idPrefix;

    /** Whether to use the road name to identify origins and destinations. */
    @Option(names = "--useRoadName", description = "Whether to use the road name to identify origins and destinations",
            defaultValue = "false")
    private boolean useRoadName;

    /** Mixed in model arguments. */
    @Mixin
    private LmrsFactory<?> tacticalFactory = new LmrsFactory<>(ScenarioTacticalPlanner::new);

    /** List of vehicle messages before simulation start. */
    private final List<VehicleMessage> preStartVehicleMessages = new ArrayList<>();

    /** Last OD matrix sent. */
    private OdMatrixJson lastOdJson;

    /** Last routes sent. */
    private RoutesJson lastRoutesJson;

    /** Last network message. */
    private NetworkMessage lastNetworkMessage;

    /** GTU spawner. */
    private GtuSpawnerOd gtuSpawner;

    /** Simulator. */
    private OtsAnimator simulator;

    /** Network. */
    private RoadNetwork network;

    /** Application. */
    private OtsSimulationApplication<AbstractOtsModel> app;

    /** GTU that is being externally generated, for which no VEHICLE message should be sent. */
    private String externallyGeneratedGtuId;

    /** IDs of GTUs for which plan messages are sent. */
    private final Set<String> planGtuIds = new LinkedHashSet<>();

    /** IDs of GTUs that are externally controlled. */
    private final Set<String> externalGtuIds = new LinkedHashSet<>();

    /** Command handlers. */
    private final Map<String, CommandsHandler> commandHandlers = new LinkedHashMap<>();

    /** IDs of GTUs that are externally deleted, i.e. do not sent back a delete message. */
    private final Set<String> deleteGtuIds = new LinkedHashSet<>();

    /** Run until time. */
    private Duration runUntil;

    /** Message id of the last progress message. */
    private Object progressMessageId;

    /** Queue of runnable to send messages. */
    private final BlockingQueue<Runnable> sendQueue = new LinkedBlockingQueue<>();

    /**
     * Constructor.
     */
    public AbstractOtsTransceiver()
    {
        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    AbstractOtsTransceiver.this.sendQueue.take().run();
                }
                catch (InterruptedException exception)
                {
                    Logger.ots().debug("AbstractOtsTransceiver sender interrupted");
                }
            }
        }, "AbstractOtsTransceiver sender");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Whether a simulation is running.
     * @return whether a simulation is running
     */
    private boolean isRunning()
    {
        return this.simulator != null && this.simulator.getSimulatorTime().gt0();
    }

    /**
     * Setup simulation.
     */
    private void setupSimulation()
    {
        stopSimulation();

        if (this.lastNetworkMessage == null)
        {
            return;
        }

        // An animator supports real-time running. No GUI will be shown if no animation panel is created.
        this.simulator = new OtsAnimator("OTS animator");
        this.simulator.addListener(this, PROGRESSED_EVENT);

        try
        {
            String networkString = this.lastNetworkMessage.network();
            NetworkType networkType = this.lastNetworkMessage.type();
            CoSimModel model = new CoSimModel(this.simulator, networkString, networkType);
            Duration runtime = Duration.ofSI(360000.0);

            this.simulator.initialize(Duration.ZERO, Duration.ZERO, runtime, model,
                    new HistoryManagerDevs(this.simulator, Duration.ofSI(5.0), Duration.ofSI(10.0)));
            this.network = model.getNetwork();
            LaneBasedGtuCharacteristicsGeneratorOd characteristicsGeneratorOd =
                    model.getSim0mqSimulation().getGtuCharacteristicsGeneratorOd();
            this.gtuSpawner = new GtuSpawnerOd(this.network, characteristicsGeneratorOd);

            this.network.addListener(this, Network.GTU_ADD_EVENT);
            this.network.addListener(this, Network.GTU_REMOVE_EVENT);

            if (this.showGui)
            {
                OtsSimulationPanel animationPanel =
                        new OtsSimulationPanel(this.network.getExtent(), this.network, new OtsSimulationPanelDecorator()
                        {
                            @Override
                            public List<Colorer<? super Gtu>> getGtuColorers()
                            {
                                List<Colorer<? super Gtu>> colorers = new ArrayList<>();
                                colorers.addAll(OtsSimulationPanelDecorator.DEFAULT_GTU_COLORERS);
                                colorers.add(new SynchronizationGtuColorer());
                                return colorers;
                            }
                        });
                animationPanel.enableSimulationControlButtons();
                this.app = new OtsSimulationApplication<AbstractOtsModel>(model, animationPanel);
            }

            // Routes
            if (this.lastRoutesJson != null)
            {
                this.lastRoutesJson.createRoutes(this.network, DefaultsNl.VEHICLE, model.getSim0mqSimulation());
            }

            // OD background traffic with default model
            if (this.lastOdJson != null)
            {
                OdMatrix od = this.lastOdJson.asOdMatrix(this.network, GTU_TYPES, model.getSim0mqSimulation());
                OdOptions odOptions = new OdOptions().set(OdOptions.GTU_TYPE, characteristicsGeneratorOd).set(OdOptions.GTU_ID,
                        new IdSupplier(this.idPrefix));
                OdApplier.applyOd(this.network, od, odOptions, DefaultsNl.ROAD_USERS);
            }
        }
        catch (SimRuntimeException | NamingException | RemoteException | DsolException | NetworkException
                | ParameterException ex)
        {
            throw new OtsRuntimeException("Unable to setup simulation.", ex);
        }

        // Reset pre-start vehicles
        for (VehicleMessage vehiclesMessage : this.preStartVehicleMessages)
        {
            generateVehicle(vehiclesMessage);
        }

    }

    /**
     * Stops any running simulation. It leaves data to be able to reset the simulation.
     */
    private void stopSimulation()
    {
        if (this.simulator != null && !this.simulator.isStoppingOrStopped())
        {
            this.simulator.stop();
            this.simulator.removeListener(this, PROGRESSED_EVENT);
            this.simulator = null;
        }
        if (this.network != null)
        {
            this.network.removeListener(this, Network.GTU_ADD_EVENT);
            this.network.removeListener(this, Network.GTU_REMOVE_EVENT);
            this.network = null;
        }
        this.planGtuIds.clear();
        this.externalGtuIds.clear();
        this.commandHandlers.clear();
        if (this.app != null)
        {
            this.app.dispose();
            this.app = null;
        }
    }

    /**
     * Clears the information that is used to setup a simulation. This is the information that would be reused in a reset
     * message.
     */
    private void clearSimulationSetupData()
    {
        this.preStartVehicleMessages.clear();
        this.lastRoutesJson = null;
        this.lastOdJson = null;
        this.lastNetworkMessage = null;
    }

    /**
     * Generate vehicle from message.
     * @param vehicleMessage vehicle message
     */
    private void generateVehicle(final VehicleMessage vehicleMessage)
    {
        DirectedPoint2d position = new DirectedPoint2d(vehicleMessage.xCoordinate().si, vehicleMessage.yCoordinate().si,
                vehicleMessage.direction().si);
        GtuType gtuType = GTU_TYPES.get(vehicleMessage.type());
        Route route = this.network.getRoute(vehicleMessage.route()).orElseThrow();

        this.externallyGeneratedGtuId = vehicleMessage.vehicleId();
        if (isRunning())
        {
            this.simulator.scheduleEventNow(this, "spawnGtu",
                    new Object[] {vehicleMessage.vehicleId(), gtuType, vehicleMessage.length(), vehicleMessage.width(),
                            vehicleMessage.refToNose(), route, vehicleMessage.speed(), position, vehicleMessage.controlMode(),
                            vehicleMessage.parameters()});
        }
        else
        {
            spawnGtu(vehicleMessage.vehicleId(), gtuType, vehicleMessage.length(), vehicleMessage.width(),
                    vehicleMessage.refToNose(), route, vehicleMessage.speed(), position, vehicleMessage.controlMode(),
                    vehicleMessage.parameters());
        }
    }

    /**
     * Spawn GTU scheduled.
     * @param id id
     * @param gtuType GTU type
     * @param vehicleLength length
     * @param vehicleWidth width
     * @param refToNose distance from reference point to front
     * @param route route
     * @param initSpeed speed
     * @param position position
     * @param mode mode
     * @param parameterMap map of parameters
     * @param <T> helper casting type
     */
    @SuppressWarnings("checkstyle:parameternumber")
    private <T extends Enum<T>> void spawnGtu(final String id, final GtuType gtuType, final Length vehicleLength,
            final Length vehicleWidth, final Length refToNose, final Route route, final Speed initSpeed,
            final DirectedPoint2d position, final ControlMode mode, final Map<String, Object> parameterMap)
    {
        Set<ParameterType<?>> setParameters = new LinkedHashSet<>();
        for (Entry<String, Object> parameterEntry : parameterMap.entrySet())
        {
            String parameter = parameterEntry.getKey();
            Object value = parameterEntry.getValue();
            boolean singleShot = false;
            if (parameter.startsWith("--"))
            {
                if (!singleShot)
                {
                    this.tacticalFactory.setOneShotMode();
                    singleShot = true;
                }
                setSetting(parameter, value);
            }
            else
            {
                setParameterValue(parameter, value, setParameters, id);
            }
        }
        /*-
         * Method notify() needs a GTU inside planGtuIds to send an OperationPlan to ExternalSim. For different modes:
         *  OTS: register here, spawn (plan is sent), schedule control mode change (without effect)
         *  HYBRID: spawn, schedule control mode change (starts dead reckoning, plan is sent)
         *  EXTERNAL: spawn, schedule control mode change (starts dead reckoning, never sent plan)
         * Note that dead reckoning can only be started after a spawn.
         */
        if (ControlMode.OTS.equals(mode))
        {
            this.planGtuIds.add(id);
        }
        try
        {
            this.gtuSpawner.spawnGtu(id, gtuType, vehicleLength, vehicleWidth, refToNose, route, initSpeed, position);
        }
        catch (GtuException | OtsGeometryException | NetworkException ex)
        {
            throw new OtsRuntimeException("Unable to spawn GTU " + id, ex);
        }
        changeControlMode(id, mode);
    }

    /**
     * Set setting from Vehicle message.
     * @param <V> setting type
     * @param settingName setting name, including "--"
     * @param value setting value
     */
    @SuppressWarnings("unchecked")
    private <V> void setSetting(final String settingName, final Object value)
    {
        try
        {
            Field field = LmrsFactory.class.getDeclaredField(settingName.substring(2));
            field.setAccessible(true);
            List<?> list = (List<?>) field.get(this.tacticalFactory);
            for (Field settingField : Setting.class.getDeclaredFields())
            {
                if (Modifier.isStatic(settingField.getModifiers()) && settingField.get(null) instanceof Setting rawSetting)
                {
                    Setting<V> setting = (Setting<V>) rawSetting;
                    if (list.equals(setting.getListFunction().apply(this.tacticalFactory)))
                    {
                        V castValue;
                        try
                        {
                            if (setting.equals(Setting.CAR_FOLLOWING_MODEL))
                            {

                                castValue = (V) (new CarFollowingModelConverter().convert((String) value));
                            }
                            else if (setting.equals(Setting.SYNCHRONIZATION))
                            {
                                castValue = (V) (new SynchronizationConverter().convert((String) value));
                            }
                            else if (setting.equals(Setting.COOPERATION))
                            {
                                castValue = (V) (new CooperationConverter().convert((String) value));
                            }
                            else if (setting.equals(Setting.GAP_ACCEPTANCE))
                            {
                                castValue = (V) (new GapAcceptanceConverter().convert((String) value));
                            }
                            else if (setting.equals(Setting.FULLER_IMPLEMENTATION))
                            {
                                castValue = (V) FullerImplementation.valueOf((String) value);
                            }
                            else
                            {
                                // boolean, double or String
                                castValue = (V) value;
                            }
                        }
                        catch (Exception ex)
                        {
                            throw new OtsRuntimeException(ex);
                        }
                        this.tacticalFactory.set(setting, castValue);
                        return;
                    }
                }
            }
            throw new NoSuchFieldException();
        }
        catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException ex)
        {
            throw new OtsRuntimeException("Unable to set " + settingName, ex);
        }
    }

    /**
     * Sets parameter in parameter factory. This is inherently one-shot in the tactical factory which is a
     * {@link ParameterFactoryOneShot}.
     * @param <T> value type
     * @param parameter parameter type id
     * @param value value
     * @param setParameters set of parameters that are set, to which the parameter should be added
     * @param id GTU id
     */
    @SuppressWarnings("unchecked")
    private <T> void setParameterValue(final String parameter, final Object value, final Set<ParameterType<?>> setParameters,
            final String id)
    {
        ParameterType<T> param = (ParameterType<T>) Parameters.get(parameter);
        this.tacticalFactory.addParameter(param, (T) value);
        Logger.ots().info(
                "Parameter " + param.getId() + " set in the parameter factory to value " + value + " for vehicle " + id + ".");
        setParameters.add(param);
    }

    /**
     * Returns the scenario based tactical planner for GTU with given id.
     * @param gtuId GTU id
     * @return scenario based tactical planner for GTU with given id, {@code null} if the vehicle does not exist
     */
    private ScenarioTacticalPlanner getTacticalPlanner(final String gtuId)
    {
        Gtu gtu = this.network.getGTU(gtuId).orElseThrow();
        if (gtu == null)
        {
            return null;
        }
        return (ScenarioTacticalPlanner) gtu.getTacticalPlanner();
    }

    @Override
    public void notify(final Event event)
    {
        EventType eventType = event.getType();
        if (eventType.equals(LaneBasedGtu.LANEBASED_MOVE_EVENT))
        {
            String gtuId = (String) ((Object[]) event.getContent())[0];
            if (!this.planGtuIds.contains(gtuId))
            {
                return;
            }
            /*
             * When the GTU is in hybrid mode, it sends a plan every so often. ScenarioTacticalPlanner will schedule a second
             * move event at the same time that will trigger the dead-reckoning to continue until we receive new dead-reckoning
             * information.
             */
            sendPlanMessage(gtuId);
        }
        else if (eventType.equals(Network.GTU_ADD_EVENT))
        {
            String gtuId = (String) event.getContent();
            Gtu gtu = this.network.getGTU(gtuId).orElseThrow();
            if (!gtuId.equals(this.externallyGeneratedGtuId))
            {
                this.simulator.scheduleEventNow(() -> sendVehicleMessage(gtu));
                this.planGtuIds.add(gtuId);
                this.externallyGeneratedGtuId = null;
            }
            gtu.addListener(this, LaneBasedGtu.LANEBASED_MOVE_EVENT);
        }
        else if (eventType.equals(Network.GTU_REMOVE_EVENT))
        {
            String gtuId = (String) event.getContent();
            Gtu gtu = this.network.getGTU(gtuId).orElseThrow();
            gtu.removeListener(this, LaneBasedGtu.LANEBASED_MOVE_EVENT);
            this.planGtuIds.remove(gtuId);
            this.externalGtuIds.remove(gtuId);
            this.commandHandlers.remove(gtuId);
            if (!this.deleteGtuIds.remove(gtuId))
            {
                this.sendQueue.add(() ->
                {
                    DeleteMessage deleteMessage = new DeleteMessage(this.simulator.getSimulatorTime(), gtuId);
                    send(deleteMessage);
                    Logger.ots().debug("Ots sent {} message for GTU {}", deleteMessage.getId(), gtuId);
                });
            }
        }
        else if (eventType.equals(SimulatorInterface.STOP_EVENT))
        {
            if (this.runUntil != null)
            {
                this.runUntil = null;
                this.sendQueue.add(() -> sendAndLog(new ReadyMessage(this.progressMessageId)));
            }
            // if not, stopped for some other reason, perhaps a stop button in the GUI
        }
        else if (eventType.equals(PROGRESSED_EVENT))
        {
            if (this.runUntil == null)
            {
                Logger.ots().error("PROGRESSED_EVENT but no runUntil value");
            }
            this.runUntil = null;
            this.sendQueue.add(() -> sendAndLog(new ReadyMessage(this.progressMessageId)));
        }
    }

    /**
     * Sent vehicle message.
     * @param gtu GTU
     */
    private void sendVehicleMessage(final Gtu gtu)
    {
        String gtuId = gtu.getId();
        DirectedPoint2d p = gtu.getLocation();
        String routeId = gtu.getStrategicalPlanner().getRoute().get().getId();
        VehicleType vehicleType = null;
        for (Entry<VehicleType, GtuType> entry : GTU_TYPES.entrySet())
        {
            if (gtu.getType().isOfType(entry.getValue()))
            {
                Throw.when(vehicleType != null, IllegalArgumentException.class,
                        "GTU type {} is mapped to from multiple vehicle types.");
                vehicleType = entry.getKey();
            }
        }
        VehicleType vehicleTypeFinal = vehicleType;
        this.sendQueue.add(() ->
        {
            VehicleMessage vehicleMessage = new VehicleMessage(this.simulator.getSimulatorTime(), gtuId, ControlMode.OTS,
                    Length.ofSI(p.x), Length.ofSI(p.y), Direction.ofSI(p.dirZ), gtu.getSpeed(), vehicleTypeFinal,
                    gtu.getLength(), gtu.getWidth(), gtu.getFront().dx(), Collections.emptyMap(), routeId, new Object());
            send(vehicleMessage);
            Logger.ots().debug("Ots sent {} message for GTU {} on route {}", vehicleMessage.getId(), gtuId, routeId);
        });
    }

    /**
     * Send operational plan.
     * @param gtuId GTU id
     */
    private void sendPlanMessage(final String gtuId)
    {
        Gtu gtu = this.network.getGTU(gtuId).get();
        OperationalPlan plan = ((ScenarioTacticalPlanner) gtu.getTacticalPlanner()).pullLastIntendedPlan();
        if (plan == null)
        {
            // Do not sent plan upon a move triggered by external control
            return;
        }
        Speed speed = plan.getStartSpeed();
        OtsLine2d line = plan.getPath();
        float[] x = new float[line.size()];
        float[] y = new float[line.size()];
        try
        {
            for (int i = 0; i < line.size(); i++)
            {
                x[i] = (float) line.get(i).x;
                y[i] = (float) line.get(i).y;
            }
        }
        catch (OtsGeometryException ex)
        {
            throw new OtsRuntimeException("Unable to obtain coordinate from path.", ex);
        }
        ImmutableList<Segment> segments = plan.getOperationalPlanSegmentList();
        float[] t = new float[segments.size()];
        float[] a = new float[segments.size()];
        for (int i = 0; i < segments.size(); i++)
        {
            t[i] = (float) segments.get(i).duration().si;
            a[i] = (float) segments.get(i).acceleration().si;
        }

        this.sendQueue.add(() ->
        {
            PlanMessage planMessage = new PlanMessage(this.simulator.getSimulatorTime(), gtuId, speed, new FloatLengthVector(x),
                    new FloatLengthVector(y), new FloatDurationVector(t), new FloatAccelerationVector(a),
                    ((LaneBasedGtu) gtu).getTurnIndicatorStatus());
            send(planMessage);
            Logger.ots().debug("Ots sent {} message for GTU {} with acceleration {}", planMessage.getId(), gtuId, a[0]);
        });
    }

    /**
     * Delete GTU.
     * @param id GTU id
     */
    private void delete(final String id)
    {
        this.network.getGTU(id).ifPresent((g) -> g.destroy());
    }

    /**
     * Method that runs scheduled in the simulator to apply dead reckoning.
     * @param id GTU id
     * @param loc location
     * @param speed speed
     * @param acceleration acceleration
     */
    private void deadReckoning(final String id, final DirectedPoint2d loc, final Speed speed, final Acceleration acceleration)
    {
        ScenarioTacticalPlanner planner = getTacticalPlanner(id);
        if (planner != null)
        {
            planner.deadReckoning(loc, speed, acceleration);
        }
    }

    /**
     * Perform command on GTU.
     * @param id GTU id
     * @param command command
     */
    private void performCommand(final String id, final CommandMessage.Command command)
    {
        Function<String, CommandsHandler> function = (gtuId) -> new CommandsHandler(this.network, gtuId);
        this.commandHandlers.computeIfAbsent(id, function).scheduleCommand(command);
    }

    /**
     * Initializes or changes the control mode of the GTU with given id.
     * @param id GTU id
     * @param mode control mode
     */
    private void changeControlMode(final String id, final ControlMode mode)
    {
        switch (mode)
        {
            case OTS:
            {
                this.planGtuIds.add(id);
                if (this.externalGtuIds.remove(id))
                {
                    getTacticalPlanner(id).stopDeadReckoning();
                }
                break;
            }
            case HYBRID:
            {
                this.planGtuIds.add(id);
                if (this.externalGtuIds.add(id))
                {
                    getTacticalPlanner(id).startDeadReckoning(true);
                }
                break;
            }
            case EXTERNAL:
            {
                this.planGtuIds.remove(id);
                if (this.externalGtuIds.add(id))
                {
                    getTacticalPlanner(id).startDeadReckoning(false);
                }
                break;
            }
            default:
                System.err.println("Unknown control mode " + mode);
                break;
        }
    }

    // ===== Receive methods =====

    /**
     * Receive external message.
     * @param externalMessage external message
     * @throws NullPointerException when the message is {@code null}
     */
    protected void receive(final ExternalMessage externalMessage)
    {
        Throw.whenNull(externalMessage, "externalMessage");
        Logger.ots().debug("OTS received {} message for GTU {}", externalMessage.getId(), externalMessage.vehicleId());
        DirectedPoint2d loc = new DirectedPoint2d(externalMessage.xCoordinate().si, externalMessage.yCoordinate().si,
                externalMessage.direction().si);
        this.simulator.scheduleEventNow(
                () -> deadReckoning(externalMessage.vehicleId(), loc, externalMessage.speed(), externalMessage.acceleration()));
    }

    /**
     * Receive vehicle message.
     * @param vehicleMessage vehicle message
     * @throws NullPointerException when the message is {@code null}
     */
    protected void receive(final VehicleMessage vehicleMessage)
    {
        Throw.whenNull(vehicleMessage, "vehicleMessage");
        Logger.ots().debug("OTS received {} message for GTU {}", vehicleMessage.getId(), vehicleMessage.vehicleId());
        generateVehicle(vehicleMessage); // schedules on simulator when running
        if (!isRunning())
        {
            this.preStartVehicleMessages.add(vehicleMessage);
            this.sendQueue.add(() -> sendAndLog(new ReadyMessage(vehicleMessage.responseId())));
        }
    }

    /**
     * Receive mode message.
     * @param modeMessage mode message
     * @throws NullPointerException when the message is {@code null}
     */
    protected void receive(final ModeMessage modeMessage)
    {
        Throw.whenNull(modeMessage, "modeMessage");
        Logger.ots().debug("OTS received {} message for GTU {}", modeMessage.getId(), modeMessage.vehicleId());
        this.simulator.scheduleEventNow(() -> changeControlMode(modeMessage.vehicleId(), modeMessage.controlMode()));
    }

    /**
     * Receive command message.
     * @param commandMessage command message
     * @throws NullPointerException when the message is {@code null}
     */
    protected void receive(final CommandMessage commandMessage)
    {
        Throw.whenNull(commandMessage, "commandMessage");
        Logger.ots().debug("Ots received {} message for GTU {}", commandMessage.getId(), commandMessage.vehicleId());
        this.simulator.scheduleEventNow(() -> performCommand(commandMessage.vehicleId(), commandMessage.command()));
    }

    /**
     * Receive delete message.
     * @param deleteMessage delete message
     * @throws NullPointerException when the message is {@code null}
     */
    protected void receive(final DeleteMessage deleteMessage)
    {
        Throw.whenNull(deleteMessage, "deleteMessage");
        Logger.ots().debug("Ots received {} message for GTU {}", deleteMessage.getId(), deleteMessage.vehicleId());
        this.deleteGtuIds.add(deleteMessage.vehicleId());
        this.simulator.scheduleEventNow(() -> delete(deleteMessage.vehicleId()));
    }

    /**
     * Receive routes message.
     * @param routesMessage routes message
     * @throws NullPointerException when the message is {@code null}
     */
    protected void receive(final RoutesMessage routesMessage)
    {
        Throw.whenNull(routesMessage, "routesMessage");
        Logger.ots().debug("Ots received {} message", routesMessage.getId());
        this.lastRoutesJson = routesMessage.routes();
        this.sendQueue.add(() -> sendAndLog(new ReadyMessage(routesMessage.responseId())));
    }

    /**
     * Receive OD matrix message.
     * @param odMatrixMessage OD matrix message
     * @throws NullPointerException when the message is {@code null}
     */
    protected void receive(final OdMatrixMessage odMatrixMessage)
    {
        Throw.whenNull(odMatrixMessage, "odMatrixMessage");
        Logger.ots().debug("Ots received {} message", odMatrixMessage.getId());
        this.lastOdJson = odMatrixMessage.odMatrix();
        this.sendQueue.add(() -> sendAndLog(new ReadyMessage(odMatrixMessage.responseId())));
    }

    /**
     * Receive network message.
     * @param networkMessage network message
     * @throws NullPointerException when the message is {@code null}
     */
    protected void receive(final NetworkMessage networkMessage)
    {
        Throw.whenNull(networkMessage, "networkMessage");
        Logger.ots().debug("Ots received {} message", networkMessage.getId());
        this.lastNetworkMessage = networkMessage;
        setupSimulation();
        this.sendQueue.add(() -> sendAndLog(new ReadyMessage(networkMessage.responseId())));
    }

    /**
     * Receive start message.
     * @param startMessage start message
     * @throws NullPointerException when the message is {@code null}
     */
    protected void receive(final StartMessage startMessage)
    {
        Throw.whenNull(startMessage, "startMessage");
        Logger.ots().debug("Ots received {} message", startMessage.getId());
        if (this.simulator != null)
        {
            this.simulator.setSpeedFactor(1.0);
            if (!this.simulator.isStartingOrRunning())
            {
                this.simulator.start();
            }
        }
    }

    /**
     * Receive stop message.
     * @param stopMessage stop message
     * @throws NullPointerException when the message is {@code null}
     */
    protected void receive(final StopMessage stopMessage)
    {
        Throw.whenNull(stopMessage, "stopMessage");
        Logger.ots().debug("Ots received {} message", stopMessage.getId());
        stopSimulation();
        clearSimulationSetupData();
    }

    /**
     * Receive reset message.
     * @param resetMessage reset message
     * @throws NullPointerException when the message is {@code null}
     */
    protected void receive(final ResetMessage resetMessage)
    {
        Throw.whenNull(resetMessage, "resetMessage");
        Logger.ots().debug("Ots received {} message", resetMessage.getId());
        setupSimulation();
        this.sendQueue.add(() -> sendAndLog(new ReadyMessage(resetMessage.responseId())));
    }

    /**
     * Receive terminate message.
     * @param terminateMessage terminate message
     * @throws NullPointerException when the message is {@code null}
     */
    protected void receive(final TerminateMessage terminateMessage)
    {
        Throw.whenNull(terminateMessage, "terminateMessage");
        Logger.ots().debug("Ots received {} message", terminateMessage.getId());
        stopSimulation();
        clearSimulationSetupData();
    }

    /**
     * Receive progress message.
     * @param progressMessage progress message
     * @throws NullPointerException when the message is {@code null}
     */
    protected void receive(final ProgressMessage progressMessage)
    {
        Throw.whenNull(progressMessage, "progressMessage");
        Duration until = progressMessage.untilTime();
        Logger.ots().debug("Ots received {} message until {}", progressMessage.getId(), until);
        this.simulator.setSpeedFactor(1000.0);
        while (this.simulator.isStartingOrRunning())
        {
            try
            {
                // Simulator is still stopping from previous step
                Logger.ots().debug("Waiting for next PROGRESS");
                Thread.sleep(3);
            }
            catch (InterruptedException e)
            {
            }
        }
        this.progressMessageId = progressMessage.responseId();
        this.runUntil = until;
        this.simulator.scheduleEventAbs(until, () -> this.simulator.fireEvent(PROGRESSED_EVENT));
        this.simulator.runUpToAndIncluding(until);
    }

    // ===== Send methods =====

    /**
     * Send ready message and log this.
     * @param readyMessage ready message
     */
    private void sendAndLog(final ReadyMessage readyMessage)
    {
        send(readyMessage);
        Logger.ots().debug("Ots sent {} message regarding {}", readyMessage.getId(), readyMessage.responseId());
    }

    /**
     * Send ready message. All send methods are invoked on a dedicated thread of {@link AbstractOtsTransceiver}.
     * @param readyMessage ready message
     */
    protected abstract void send(ReadyMessage readyMessage);

    /**
     * Send plan message. All send methods are invoked on a dedicated thread of {@link AbstractOtsTransceiver}.
     * @param planMessage plan message
     */
    protected abstract void send(PlanMessage planMessage);

    /**
     * Send delete message. All send methods are invoked on a dedicated thread of {@link AbstractOtsTransceiver}.
     * @param deleteMessage delete message
     */
    protected abstract void send(DeleteMessage deleteMessage);

    /**
     * Send vehicle message. Response ID can be ignored. All send methods are invoked on a dedicated thread of
     * {@link AbstractOtsTransceiver}.
     * @param vehicleMessage vehicle message
     */
    protected abstract void send(VehicleMessage vehicleMessage);

    // ===== CoSim model =====

    /**
     * Co-simulation model. This intermediates between an OTS model, and the different supported network/OD types.
     */
    private final class CoSimModel extends AbstractOtsModel
    {

        /** String definition of the network. */
        private final String networkString;

        /** Network type. */
        private final NetworkType networkType;

        /** Simulation. */
        private CoSimulation simulation;

        /**
         * Constructor.
         * @param simulator simulator
         * @param networkString network string
         * @param networkType network type
         */
        private CoSimModel(final OtsSimulatorInterface simulator, final String networkString, final NetworkType networkType)
        {
            super(simulator);
            this.networkString = networkString;
            this.networkType = networkType;
        }

        @Override
        public RoadNetwork getNetwork()
        {
            return this.simulation.getNetwork();
        }

        /**
         * Returns the sim0mq simulation.
         * @return sim0mq simulation
         */
        public CoSimulation getSim0mqSimulation()
        {
            return this.simulation;
        }

        @Override
        public void constructModel() throws SimRuntimeException
        {
            try
            {
                switch (this.networkType)
                {
                    case OTS:
                        this.simulation = new OtsSimulation(this.simulator, AbstractOtsTransceiver.this.tacticalFactory,
                                this.networkString);
                        break;
                    case OPENDRIVE:
                        this.simulation = new OpenDriveSimulation(this.simulator, AbstractOtsTransceiver.this.tacticalFactory,
                                this.networkString, AbstractOtsTransceiver.this.useRoadName);
                        break;
                    default:
                        throw new SimRuntimeException("Network type " + this.networkType + " is not supported.");
                }
            }
            catch (OtsGeometryException | NetworkException | JAXBException | SAXException | ParserConfigurationException ex)
            {
                throw new SimRuntimeException(ex);
            }
        }

    }

}
