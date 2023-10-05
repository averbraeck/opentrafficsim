package org.opentrafficsim.trafficcontrol;

import org.djutils.event.EventListener;
import org.djutils.event.EventProducer;
import org.djutils.event.EventType;
import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;
import org.opentrafficsim.core.object.NonLocatedObject;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLightColor;

/**
 * Interface for traffic light controllers.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public interface TrafficController extends EventProducer, EventListener, NonLocatedObject
{
    /**
     * Retrieve the Id of the traffic light controller.
     * @return String; the id of the traffic light controller
     */
    @Override
    String getId();

    /** Traffic controller is starting up. Particular traffic control programs may use additional states not listed here. */
    String STARTING_UP = "starting up";

    /** Traffic controller is being cloned. Particular traffic control programs may use additional states not listed here. */
    String BEING_CLONED = "being cloned";

    /** Traffic controller is running. */
    String RUNNING = "running";

    /** Traffic controller is shutting down. */
    String SHUTTING_DOWN = "shutting down";

    /** Traffic controller is off. */
    String OFF = "off";

    /** Constant to select variables that have no associated traffic stream. */
    int NO_STREAM = -1;

    /**
     * The <b>timed</b> event type for pub/sub that a newly created traffic controller emits. <br>
     * Payload: Object[] { String trafficControllerId, String initialState }
     */
    EventType TRAFFICCONTROL_CONTROLLER_CREATED = new EventType("TRAFFICCONTROL.CONTROLLER_CREATED",
            new MetaData("Controller created", "Controller id, initial state",
                    new ObjectDescriptor("Controller id", "Id of the controller", String.class),
                    new ObjectDescriptor("Initial state", "Initial state", String.class)));

    /**
     * The <b>timed</b> event type for pub/sub that a traffic controller emits when it begins the computations to determine its
     * response to the current input (detector states).<br>
     * Payload: Object[] { String trafficControllerId }
     */
    EventType TRAFFICCONTROL_CONTROLLER_EVALUATING =
            new EventType("TRAFFICCONTROL.CONTROLLER_EVALUATING", new MetaData("Controller eveluating", "Controller id",
                    new ObjectDescriptor("Controller id", "Id of the controller", String.class)));

    /**
     * The <b>timed</b> event type for pub/sub that a traffic controller uses to convey warnings.<br>
     * Payload: Object[] { String trafficControllerId, String message }
     */
    EventType TRAFFICCONTROL_CONTROLLER_WARNING = new EventType("TRAFFICCONTROL.CONTROLLER_WARNING",
            new MetaData("Controller warning", "Controller id, warning message",
                    new ObjectDescriptor("Controller id", "Id of the controller", String.class),
                    new ObjectDescriptor("Message", "Message", String.class)));

    /**
     * The <b>timed</b> event for pub/sub emitted by a traffic control machine when it changes state (STARTING_UP, RUNNING,
     * SHUTTING_DOWN, OFF, etc. The exact set of states may vary depending on the type of traffic control machine. <br>
     * Payload: Object[] { String trafficControllerId, String oldState, String newState }
     */
    EventType TRAFFICCONTROL_STATE_CHANGED = new EventType("TRAFFIC_CONTROL.STATE_CHANGED",
            new MetaData("Controller state changed", "Controller id, old state, new state",
                    new ObjectDescriptor("Controller id", "Id of the controller", String.class),
                    new ObjectDescriptor("Old state", "Old state", String.class),
                    new ObjectDescriptor("New state", "New state", String.class)));

    /**
     * The <b>timed</b>event that is fired by a traffic control program when a traffic light must change state. <br>
     * Payload: Object[] { String trafficControllerId, Short stream, TrafficLightColor newColor }
     */
    EventType TRAFFIC_LIGHT_CHANGED = new EventType("TrafficLightChanged",
            new MetaData("Controller state changed", "Controller id, old state, new state",
                    new ObjectDescriptor("Controller id", "Id of the controller", String.class),
                    new ObjectDescriptor("Stream", "Stream number", Short.class),
                    new ObjectDescriptor("Light color", "New traffic light color", TrafficLightColor.class)));

    /**
     * The <b>timed</b> event type for pub/sub indicating the creation of a traffic control program variable. <br>
     * Listeners to this event can send <code>TRAFFICCONTROL_SET_TRACE</code> messages to set the tracing level of a
     * variable.<br>
     * Payload: Object[] {String trafficControllerId, String variableId, Short trafficStream, Double initialValue}
     */
    EventType TRAFFICCONTROL_VARIABLE_CREATED = new EventType("TRAFFICCONTROL.VARIABLE_CREATED",
            new MetaData("Controller state changed", "Controller id, old state, new state",
                    new ObjectDescriptor("Controller id", "Id of the controller", String.class),
                    new ObjectDescriptor("Variable id", "Id of the variable", String.class),
                    new ObjectDescriptor("Stream", "Stream number", Short.class),
                    new ObjectDescriptor("Initial value", "Initial value", Double.class)));

    /**
     * The <b>timed</b> event type that instruct a traffic controller to change the tracing level of a variable. <br>
     * Payload: Object[] { String trafficControllerId, String variableId, Short trafficStream, Boolean trace } <br>
     * Remark 1: an empty string for the variableId sets or clears tracing for all variables associated with the traffic stream.
     * <br>
     * Remark 2: The stream number <code>NO_STREAM</code> selects variable(s) that are not associated with a particular traffic
     * stream. <br>
     * Remark 3: setting the tracing level of a variable changes the amount of
     * <code>TRAFFICCONTROL_TRACED_VARIABLE_UPDATED</code> events sent to <b>all</b> listeners; i.e. it is not possible to
     * affect tracing on a per listener basis.
     */
    EventType TRAFFICCONTROL_SET_TRACING = new EventType("TRAFFICCONTROL.SET_TRACING",
            new MetaData("Controller state changed", "Controller id, old state, new state",
                    new ObjectDescriptor("Controller id", "Id of the controller", String.class),
                    new ObjectDescriptor("Variable id", "Id of the variable", String.class),
                    new ObjectDescriptor("Stream", "Stream number", Short.class),
                    new ObjectDescriptor("Trace", "Trace", Boolean.class)));

    /**
     * The <b>timed</b> event type for pub/sub indicating the update of a traced control program variable. <br>
     * Payload: Object[] {String trafficControllerId, String variableId, Short trafficStream, Double oldValue, Double newValue,
     * String expressionOrDescription} <br>
     * Remark 1: for variable that are not associated with a particular traffic stream, the trafficStream value shall be
     * <code>NO_STREAM</code> <br>
     * Remark 2: if the variable is a timer that has just been initialized; newValue will reflect the duration in seconds
     */
    EventType TRAFFICCONTROL_TRACED_VARIABLE_UPDATED = new EventType("TRAFFICCONTROL.VARIABLE_UPDATED",
            new MetaData("Controller state changed", "Controller id, old state, new state",
                    new ObjectDescriptor("Controller id", "Id of the controller", String.class),
                    new ObjectDescriptor("Variable id", "Id of the variable", String.class),
                    new ObjectDescriptor("Stream", "Stream number", Short.class),
                    new ObjectDescriptor("Old value", "Old value", Double.class),
                    new ObjectDescriptor("New value", "New value", Double.class),
                    new ObjectDescriptor("Expression", "Expression or description", String.class)));

    /**
     * The <b>timed</b> event for pub/sub emitted by a traffic control machine when it changes to another conflict group. <br>
     * Payload: Object[] { String trafficControllerId, String oldConflictGroupStreams, String newConflictGroupStreams } <br>
     * Remark 1: a conflict group is described as a space-separated list of traffic stream numbers. The traffic streams within a
     * conflict group should be compatible; i.e. not conflicting. <br>
     * Remark 2: The value <code>00</code> can be used as a place holder for a stream in a conflict groups that have fewer than
     * the maximum number of traffic streams that occur in any conflict group.<br>
     * Remark 3: The very first event of this type may use an empty string for <code>oldConflictGroupStreams</code>.<br>
     * Remark 4: Some traffic control systems may not operate in a conflict group by conflict group fashion and therefore not
     * emit these events.
     */
    EventType TRAFFICCONTROL_CONFLICT_GROUP_CHANGED = new EventType("TRAFFICCONTROL.CONFLICT_GROUP_CHANGED",
            new MetaData("Controller state changed", "Controller id, old state, new state",
                    new ObjectDescriptor("Controller id", "Id of the controller", String.class),
                    new ObjectDescriptor("Old stream", "Old conflict group stream", String.class),
                    new ObjectDescriptor("New stream", "New conflict group stream", String.class)));

}
