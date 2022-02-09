package org.opentrafficsim.trafficcontrol;

import org.djutils.event.EventListenerInterface;
import org.djutils.event.EventProducerInterface;
import org.djutils.event.EventType;
import org.djutils.event.TimedEventType;
import org.opentrafficsim.base.Identifiable;
import org.opentrafficsim.core.object.InvisibleObjectInterface;

/**
 * Interface for traffic light controllers.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Oct 14, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface TrafficController
        extends EventProducerInterface, EventListenerInterface, InvisibleObjectInterface, Identifiable
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
    TimedEventType TRAFFICCONTROL_CONTROLLER_CREATED = new TimedEventType("TRAFFICCONTROL.CONTROLLER_CREATED");

    /**
     * The <b>timed</b> event type for pub/sub that a traffic controller emits when it begins the computations to determine its
     * response to the current input (detector states).<br>
     * Payload: Object[] { String trafficControllerId }
     */
    TimedEventType TRAFFICCONTROL_CONTROLLER_EVALUATING = new TimedEventType("TRAFFICCONTROL.CONTROLLER_EVALUATING");

    /**
     * The <b>timed</b> event type for pub/sub that a traffic controller uses to convey warnings.<br>
     * Payload: Object[] { String trafficControllerId, String message }
     */
    TimedEventType TRAFFICCONTROL_CONTROLLER_WARNING = new TimedEventType("TRAFFICCONTROL.CONTROLLER_WARNING");

    /**
     * The <b>timed</b> event for pub/sub emitted by a traffic control machine when it changes state (STARTING_UP, RUNNING,
     * SHUTTING_DOWN, OFF, etc. The exact set of states may vary depending on the type of traffic control machine. <br>
     * Payload: Object[] { String trafficControllerId, String oldState, String newState }
     */
    TimedEventType TRAFFICCONTROL_STATE_CHANGED = new TimedEventType("TRAFFIC_CONTROL.STATE_CHANGED");

    /**
     * The <b>timed</b>event that is fired by a traffic control program when a traffic light must change state. <br>
     * Payload: Object[] { String trafficControllerId, Integer stream, TrafficLightColor newColor }
     */
    TimedEventType TRAFFIC_LIGHT_CHANGED = new TimedEventType("TrafficLightChanged");

    /**
     * The <b>timed</b> event type for pub/sub indicating the creation of a traffic control program variable. <br>
     * Listeners to this event can send <code>TRAFFICCONTROL_SET_TRACE</code> messages to set the tracing level of a
     * variable.<br>
     * Payload: Object[] {String trafficControllerId, String variableId, Integer trafficStream, Double initialValue}
     */
    TimedEventType TRAFFICCONTROL_VARIABLE_CREATED = new TimedEventType("TRAFFICCONTROL.VARIABLE_CREATED");

    /**
     * The <b>timed</b> event type that instruct a traffic controller to change the tracing level of a variable. <br>
     * Payload: Object[] { String trafficControllerId, String variableId, Integer trafficStream, Boolean trace } <br>
     * Remark 1: an empty string for the variableId sets or clears tracing for all variables associated with the traffic stream.
     * <br>
     * Remark 2: The stream number <code>NO_STREAM</code> selects variable(s) that are not associated with a particular traffic
     * stream. <br>
     * Remark 3: setting the tracing level of a variable changes the amount of
     * <code>TRAFFICCONTROL_TRACED_VARIABLE_UPDATED</code> events sent to <b>all</b> listeners; i.e. it is not possible to
     * affect tracing on a per listener basis.
     */
    EventType TRAFFICCONTROL_SET_TRACING = new EventType("TRAFFICCONTROL.SET_TRACING");

    /**
     * The <b>timed</b> event type for pub/sub indicating the update of a traced control program variable. <br>
     * Payload: Object[] {String trafficControllerId, String variableId, Integer trafficStream, Double oldValue, Double
     * newValue, String expressionOrDescription} <br>
     * Remark 1: for variable that are not associated with a particular traffic stream, the trafficStream value shall be
     * <code>NO_STREAM</code> <br>
     * Remark 2: if the variable is a timer that has just been initialized; newValue will reflect the duration in seconds
     */
    TimedEventType TRAFFICCONTROL_TRACED_VARIABLE_UPDATED = new TimedEventType("TRAFFICCONTROL.VARIABLE_UPDATED");

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
    TimedEventType TRAFFICCONTROL_CONFLICT_GROUP_CHANGED = new TimedEventType("TRAFFICCONTROL.CONFLICT_GROUP_CHANGED");

}
