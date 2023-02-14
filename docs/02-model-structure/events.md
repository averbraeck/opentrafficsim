# Event types in OTS

Below is a complete list of all event types in OTS. For these events we have the following contract:

1. `ADD` events make no guarantee whatsoever as to the state of the added object, other than it having an id. The object may be in a non-operable state during initialization. These events can be used to register listeners with the added object, and to allocate resources for the object (e.g. `Map.put(gtuId, new ArrayList<Speed>());`.
2. Any other events, such as `MOVE`, must occur with the object in question in a complete state.

This contract means that in some cases, bookkeeping has to take place specifically for the first event of a certain type. For example, the `RoadSampler` cannot start up a trajectory on an `ADD` event, as the GTU is not yet able to report for example its route. Listeners are however added, and on the first `MOVE`, the trajectory may be initiated.

In some cases it could be valuable during an `ADD` to register as a listener to some other event, e.g. `MOVE`, and to unregister as a listener on the first occurrence of that event.

_Table 2.1: Overview of events in OTS._
<table border="1" width="800px">
  <tr style="font-weight: bold"><td>Class</td><td>Event field</td><td>Listeners (excluding test code)</td><td>Used</td></tr>
  <tr><td>Network</td><td>GTU_ADD_EVENT</td><td>DefaultAnimationFactory, NetworkAnimation<sup>1</sup>, AbstractLaneBasedMoveChecker, Publisher<sup>2</sup>, NetworkModel, StochasticDistractionModel, RampMeteringDemo, OtsAnimationPanel</td><td>yes</td></tr>
  <tr><td></td><td>GTU_REMOVE_EVENT</td><td>DefaultAnimationFactory, NetworkAnimation<sup>1</sup>, AbstractLaneBasedMoveChecker, Publisher<sup>2</sup>, NetworkModel, StochasticDistractionModel, RampMeteringDemo, OtsAnimationPanel</td><td>yes</td></tr>
  <tr><td></td><td>NONLOCATED_OBJECT_ADD_EVENT</td><td>NetworkAnimation<sup>1</sup></td><td>yes</td></tr>
  <tr><td></td><td>NONLOCATED_OBJECT_REMOVE_EVENT</td><td>NetworkAnimation<sup>1</sup></td><td>yes</td></tr>
  <tr><td></td><td>LINK_ADD_EVENT</td><td>NetworkAnimation<sup>1</sup>, Publisher<sup>2</sup></td><td>yes</td></tr>
  <tr><td></td><td>LINK_REMOVE_EVENT</td><td>NetworkAnimation<sup>1</sup>, Publisher<sup>2</sup></td><td>yes</td></tr>
  <tr><td></td><td>NODE_ADD_EVENT</td><td>NetworkAnimation<sup>1</sup>, Publisher<sup>2</sup></td><td>yes</td></tr>
  <tr><td></td><td>NODE_REMOVE_EVENT</td><td>NetworkAnimation<sup>1</sup>, Publisher<sup>2</sup></td><td>yes</td></tr>
  <tr><td></td><td>OBJECT_ADD_EVENT</td><td>DefaultAnimationFactory, NetworkAnimation<sup>1</sup></td><td>yes</td></tr>
  <tr><td></td><td>OBJECT_REMOVE_EVENT</td><td>DefaultAnimationFactory, NetworkAnimation<sup>1</sup></td><td>yes</td></tr>
  <tr><td></td><td>ROUTE_ADD_EVENT</td><td>NetworkAnimation<sup>1</sup></td><td>yes</td></tr>
  <tr><td></td><td>ROUTE_REMOVE_EVENT</td><td>NetworkAnimation<sup>1</sup></td><td>yes</td></tr>
  <tr><td>Link</td><td>GTU_ADD_EVENT</td><td>Publisher<sup>2</sup></td><td>yes</td></tr>
  <tr><td></td><td>GTU_REMOVE_EVENT</td><td>Publisher<sup>2</sup></td><td>yes</td></tr>
  <tr><td>CrossSectionLink</td><td>LANE_ADD_EVENT</td><td>Publisher<sup>2</sup></td><td>yes</td></tr>
  <tr><td></td><td>LANE_REMOVE_EVENT</td><td>Publisher<sup>2</sup></td><td>yes</td></tr>
  <tr><td>Gtu</td><td>MOVE_EVENT</td><td>Publisher<sup>2</sup>, GtuTransceiver<sup>2</sup></td><td>yes</td></tr>
  <tr><td></td><td>DESTROY_EVENT</td><td></td><td>no</td></tr>  
  <tr><td>LaneBasedGtu</td><td>LANEBASED_MOVE_EVENT</td><td>AbstractLaneBasedMoveChecker, RoadSampler</td><td>yes</td></tr>
  <tr><td></td><td>LANEBASED_DESTROY_EVENT</td><td></td><td>no</td></tr>
  <tr><td></td><td>LANE_CHANGE_EVENT</td><td>Conflict, StrategiesDemo</td><td>yes</td></tr>
  <tr><td></td><td>LANE_ENTER_EVENT</td><td><i>not thrown</i></td><td>no</td></tr>
  <tr><td></td><td>LANE_EXIT_EVENT</td><td><i>not thrown</i></td><td>no</td></tr>
  <tr><td>Lane</td><td>GTU_ADD_EVENT</td><td>RoadSampler, TrafficLightDetector</td><td>yes</td></tr>
  <tr><td></td><td>GTU_REMOVE_EVENT</td><td>RoadSampler, TrafficLightDetector</td><td>yes</td></tr>
  <tr><td></td><td>OBJECT_ADD_EVENT</td><td></td><td>no</td></tr>
  <tr><td></td><td>OBJECT_REMOVE_EVENT</td><td></td><td>no</td></tr>
  <tr><td></td><td>DETECTOR_ADD_EVENT</td><td></td><td>no</td></tr>
  <tr><td></td><td>DETECTOR_REMOVE_EVENT</td><td></td><td>no</td></tr>
  <tr><td>LaneBasedGtuGenerator</td><td>GTU_GENERATED_EVENT</td><td></td><td>no</td></tr>
  <tr><td>Detector</td><td>DETECTOR_TRIGGER_EVENT</td><td></td><td>no</td></tr>
  <tr><td>LoopDetector</td><td>LOOP_DETECTOR_TRIGGERED</td><td></td><td>no</td></tr>
  <tr><td></td><td>LOOP_DETECTOR_AGGREGATE</td><td></td><td>no</td></tr>
  <tr><td>TrafficLightDetector</td><td>TRAFFIC_LIGHT_DETECTOR_ TRIGGER_ENTRY_EVENT</td><td>TrafficLightDetector, TrafCod, DetectrorImage, Variable</td><td>yes</td></tr>
  <tr><td></td><td>TRAFFIC_LIGHT_DETECTOR_ TRIGGER_EXIT_EVENT</td><td>TrafficLightDetector, TrafCod, DetectrorImage, Variable</td><td>yes</td></tr>
  <tr><td>DirectionalOccupancyDetector</td><td>DIRECTIONAL_OCCUPANCY_DETECTOR_ TRIGGER_ENTRY_EVENT</td><td><i>not thrown</i></td><td>no</td></tr>
  <tr><td></td><td>DIRECTIONAL_OCCUPANCY_DETECTOR_ TRIGGER_EXIT_EVENT</td><td><i>not thrown</i></td><td>no</td></tr>
  <tr><td>TrafficController</td><td>TRAFFICCONTROL_CONTROLLER_CREATED</td><td></td><td>no</td></tr>
  <tr><td></td><td>TRAFFICCONTROL_CONTROLLER_EVALUATING</td><td>TrafCodModel</td><td>yes</td></tr>
  <tr><td></td><td>TRAFFICCONTROL_CONTROLLER_WARNING</td><td>TrafCodModel</td><td>yes</td></tr>
  <tr><td></td><td>TRAFFICCONTROL_STATE_CHANGED</td><td><i>not thrown</i></td><td>no</td></tr>
  <tr><td></td><td>TRAFFIC_LIGHT_CHANGED</td><td></td><td>no</td></tr>
  <tr><td></td><td>TRAFFICCONTROL_VARIABLE_CREATED</td><td></td><td>no</td></tr>
  <tr><td></td><td>TRAFFICCONTROL_SET_TRACING</td><td><i>not thrown</i>, TrafCod</td><td>yes</td></tr>
  <tr><td></td><td>TRAFFICCONTROL_TRACED_VARIABLE_UPDATED</td><td>TrafCodModel</td><td>yes</td></tr>
  <tr><td></td><td>TRAFFICCONTROL_CONFLICT_GROUP_CHANGED</td><td>TrafCodModel</td><td>yes</td></tr>
  <tr><td>TrafficLight</td><td>TRAFFICLIGHT_CHANGE_EVENT</td><td></td><td>no</td></tr>
  <tr><td>AbstractPlot</td><td>GRAPH_ADD_EVENT</td><td><i>not thrown</i></td><td>no</td></tr>
  <tr><td></td><td>GRAPH_REMOVE_EVENT</td><td><i>not thrown</i></td><td>no</td></tr>
</table>
<sup>1</sup>) Registers as listener, but does (effectively) nothing with it in `notify()`.<br>
<sup>2</sup>) Part of `ots-sim0mq`.<br>